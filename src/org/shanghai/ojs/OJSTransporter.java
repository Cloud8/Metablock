package org.shanghai.ojs;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;
import org.shanghai.data.DBTransporter;
import org.shanghai.ojs.URN;
import org.shanghai.ojs.NLMScanner;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.sparql.vocabulary.FOAF;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Never Mind
    @title OJS Database Transporter
    @date 2015-11-02
*/
public class OJSTransporter implements MetaCrawl.Transporter {

    protected DBTransporter ojs;
    private int count;
	private boolean dump;
    private String docbase;
    private URN urn;
    private static final Pattern REMOVE_TAGS = Pattern.compile("<.+?>");

    public OJSTransporter(String[] db, String[] idx, String[] srv)
    {
        ojs = new DBTransporter(db, idx, 0);
        ojs.transformer.setParameter("server", srv[0]);
        this.docbase = srv[1];
        urn = new URN(srv[2]);
    }

    @Override
    public void create() {
        count = 0;
        ojs.create();
        urn.create();
    }

    @Override
    public void dispose() {
        ojs.dispose();
        urn.dispose();
    }

    @Override
    public String probe() {
        return ojs.probe();
    }

    @Override
    public Resource read(String oid) {
	    count++;
        Resource rc = ojs.read(oid);
        rc = clean(rc, DCTerms.abstract_);
        NLMScanner.analyze(rc, urn);
        return rc;
    }

    @Override
    public List<String> getIdentifiers(int off, int limit) {
        return ojs.getIdentifiers(off, limit);
    }

    @Override
    public int index(String str) {
        log("crawl " + str + "?");
        // return series.index(str);
        return 0;
    }

    @Override
    public Resource test(String oid) {
        Resource rc = ojs.test(oid);
        if (rc==null) {
            dump(ojs.document, null, oid);
        } else {
            log("testing " + rc.getURI());
            clean(rc, DCTerms.abstract_);
            dump(ojs.document, rc.getURI(), oid);
        }
        NLMScanner.analyze(rc, urn);
        return rc;
    }

    /** remove all tags from literal value */
    private Resource clean(Resource rc, Property property) {
        StmtIterator si = rc.listProperties(property);
        List<Statement> old = new ArrayList<Statement>();
        List<String> object = new ArrayList<String>();
        while( si.hasNext() ) {
            Statement stmt = si.nextStatement();
            Matcher m = REMOVE_TAGS.matcher(stmt.getString());
            if (m.matches()) {
                object.add(m.replaceAll(""));
                old.add(stmt);
            }
        }
        rc.getModel().remove(old);
        for (String data : object) {
            rc.addProperty(property, data);
        } 
        return rc;
    }

    private void cleanTest(Resource rc) {
        StmtIterator si = rc.listProperties(DCTerms.abstract_);
        while( si.hasNext() ) {
            String data = si.nextStatement().getString();
            int x = data.indexOf("&lt;");
            while (x>0) {
                data = data.substring(0, x) 
                     + data.substring(data.indexOf("&gt;", x));
                x = data.indexOf("&lt;");
            }
            log(data);
        }
    }

    private void dump(Document doc, String uri, String oid) {
        if (docbase==null) {
            //FileUtil.write("opus-" + oid + ".xml", transformer.asString(doc));
        } else if (uri==null || uri.indexOf("/",7)<0) {
            log("No uri for " + oid + " [" + uri + "]");
            FileUtil.write("data/ojs-test.xml", ojs.transformer.asString(doc));
        } else if (uri!=null && Files.isDirectory(Paths.get(docbase))) {
            Path path = Paths.get(docbase + uri.substring(uri.indexOf("/",7)));
            FileUtil.mkdir(path);
            Path out = path.resolve("ojs-" + oid + ".xml");
            if (Files.isWritable(path)) {
                if (FileUtil.write(out, ojs.transformer.asString(doc)))
                    log("wrote " + out.toString());
            }
        }
    }

    private static final Logger logger =
                         Logger.getLogger(OJSTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception ex) {
        logger.info(ex.toString());
        ex.printStackTrace();
    }
}
