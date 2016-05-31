package org.seaview.opus;

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
import java.util.Map;
import java.util.HashMap;
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
    @title OJS Database Transporter
    @date 2015-11-02
*/
public class OMPTransporter implements MetaCrawl.Transporter {

    private List<DBTransporter> dbt;
    private int count;
	private boolean dump;
    private String docbase;
    private URN urn;
    private static final Pattern REMOVE_TAGS = Pattern.compile("<.+?>");
    private Map<String,Integer> map = new HashMap<String,Integer>();
    private int index;
    private int mark;

    public OMPTransporter(String[] db, String[] idx, String[] srv)
    {
        dbt = new ArrayList<DBTransporter>();
        dbt.add(new DBTransporter(db, idx, 0));
        for (DBTransporter odb : dbt) {
            odb.transformer.setParameter("server", srv[0]);
        }
        this.docbase = srv[1];
        urn = new URN(srv[2]);
    }

    @Override
    public void create() {
        index = 0;
        count = 0;
        for (DBTransporter db : dbt) {
            db.create();
        }
        urn.create();
    }

    @Override
    public void dispose() {
        for (DBTransporter db : dbt) {
            db.dispose();
        }
        urn.dispose();
        index = 0;
    }

    @Override
    public String probe() {
        String result = this.getClass().getSimpleName();
        for (DBTransporter db : dbt) {
            result += " " + db.probe();
        }
        return result;
    }

    @Override
    public List<String> getIdentifiers(int off, int limit) {
        map.clear();
        for (String oid : dbt.get(index).getIdentifiers(off, limit)) {
            map.put(oid, index);
        }
        if (map.size() < limit && index < dbt.size()-1) {
            index++;
            mark = off;
            log("switch " + index +"/"+dbt.size()+" "+ dbt.get(index).probe());
            //map.remove(map.size()-1); // remove zero marker
            //List<String> ids = dbt.get(index).getIdentifiers(off, limit);
            //for (String oid : ids) {
            for (String oid : dbt.get(index).getIdentifiers(off-mark, limit)) {
                //log("issue " + oid + " " + ids.size());
                map.put(oid, index);
            }
        }
        List<String> list = new ArrayList<String>(map.keySet());
        // for (String oid: list) log(oid + " # " + map.get(oid));
        return list; // map.keySet();
        // return dbt.get(0).getIdentifiers(off, limit);
    }

    @Override
    public int index(String fname) {
        if (Files.isRegularFile(Paths.get(fname))) {
            String index = FileUtil.read(fname);
            if (index.contains("select")) { //not mistaken
                log("not implemented : index # " + fname);
                return 1;
            }
        }
        return 0;
    }

    @Override
    public Resource read(String oid) {
	    count++;
        Resource rc = null;
        if (map.size()==0) {
            rc = dbt.get(index).read(oid);
        } else {
            rc = dbt.get(map.get(oid)).read(oid);
        }
        if (map.get(oid)==1) log("issue " + oid);
        if (map.get(oid)==2) log("journal " + oid);
        rc = clean(rc, DCTerms.abstract_);
        NLMScanner.analyze(rc, urn);
        return rc;
    }

    @Override
    public Resource test(String oid) {
        Resource rc = null; 
        if (oid.startsWith("i")) {
            index = 1;
            oid = oid.substring(1);
        } else if (oid.startsWith("j")) {
            index = 2;
            oid = oid.substring(1);
        }
        rc = dbt.get(index).test(oid);
        if (rc==null) {
            dump(dbt.get(index).document, null, oid);
        } else {
            log("testing " + rc.getURI());
            clean(rc, DCTerms.abstract_);
            dump(dbt.get(index).document, rc.getURI(), oid);
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
            String document = dbt.get(0).transformer.asString(doc);
            FileUtil.write("data/omp-test.xml", document);
        } else if (uri!=null && Files.isDirectory(Paths.get(docbase))) {
            Path path = Paths.get(docbase + uri.substring(uri.indexOf("/",7)));
            FileUtil.mkdir(path);
            Path out = path.resolve("omp-" + oid + ".xml");
            if (Files.isWritable(path)) {
                String document = dbt.get(0).transformer.asString(doc);
                if (FileUtil.write(out, document)) {
                    log("wrote " + out.toString());
                }
            }
        }
    }

    private static final Logger logger =
                         Logger.getLogger(OMPTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception ex) {
        logger.info(ex.toString());
        ex.printStackTrace();
    }
}
