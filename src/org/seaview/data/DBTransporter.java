package org.seaview.data;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;
import org.shanghai.util.PrefixModel;
import org.shanghai.rdf.XMLTransformer;
import org.seaview.data.Database;
import org.seaview.util.TextUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;

import java.io.InputStream;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.List;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Never Mind
    @title Simple Database Transporter
    @date 2013-11-16
*/
public class DBTransporter implements MetaCrawl.Transporter {

    protected Database database;
    protected String table;
    private String idxQuery;
    private String dumpQuery;
    private XMLTransformer transformer = null;
	//private boolean test = false;
    private String iri = "http://localhost/";
    private String identifier;
    private DocumentBuilderFactory factory;
    Document document;

    private DBTransporter() {
        database = new Database();
    }

    public DBTransporter(String host, String db, String user, String pass) {
        database = new Database(host, db, user, pass);
    }

    public DBTransporter(String host, String db, String user, String pass,
                         String idxFile, String dumpFile, String xslt) {
        this(host, db, user, pass);
        idxQuery = FileUtil.readResource(idxFile);
        dumpQuery = FileUtil.readResource(dumpFile);
        if (xslt==null) {
            this.transformer = new XMLTransformer();
        } else {
            this.transformer = new XMLTransformer(FileUtil.readResource(xslt));
        }
    }

    public DBTransporter(String host, String db, String user, String pass,
            String idxFile, String dumpFile, String xslt, int days, boolean b) {
        this(host, db, user, pass, idxFile, dumpFile, xslt);
        if (days>=0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 0-days);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            idxQuery = idxQuery.replace("<date>", df.format(cal.getTime()));
            //if (b) log(df.format(cal.getTime()));
            //if (b) log(idxQuery);
        }
    }

    @Override
    public void create() {
        database.create();
        table = table(idxQuery);
        //log("guessed table [" + table + "]");
        if (transformer!=null)
            transformer.create();
        identifier = null;
        factory = DocumentBuilderFactory.newInstance();
    }

    @Override
    public void dispose() {
        database.dispose();
        if (transformer!=null)
            transformer.dispose();
    }

    private String table(String query) {
        int x = query.indexOf("from ")+5;
        int y1 = query.indexOf(" ",x);
        int y2 = query.indexOf(",",x);
        int y3 = query.indexOf("\n",x);
        if (y3>0) y1=y1>y3?y3:y1;
        if (y2>0) y1=y1>y2?y2:y1;
        y1=y1>0?y1:query.length();
        if (x>0 && y1>x)
            return query.substring(x,y1).trim();
        return "zero";
    }

    @Override
    public String probe() {
        String probe = database.getSingleText("select count(*) from " + table);
        //log("probe: " + probe);
        return table + " " + probe;
    }

    @Override
    public Model read(String oid) {
        document = createDocument();
        Element root = document.createElement("document");
        document.appendChild(root);
        String[] queries = dumpQuery.replace("<oid>",oid).split(";");
        for (String query : queries) {
            ResultSet rs = database.getResult(query);
            if (rs==null)
                continue;
            try {
                if (rs.isBeforeFirst()) {
                    //log("[" + query + "]");
                    Element results = document.createElement("resultset");
			        results.setAttribute("table", table(query));
                    root.appendChild(results);
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();
                    while (rs.next()) {
                        Element row = document.createElement("row");
                        results.appendChild(row);
                        for (int ii = 1; ii <= colCount; ii++) {
                           String columnName = meta.getColumnName(ii);
                           //Object value = rs.getObject(ii);
                           String value = rs.getString(ii);
					       if (value!=null && value.toString().length()>0) {
                               Element node = document.createElement("field");
						       node.setAttribute("name", columnName);
                               node.appendChild(document.createTextNode(
                                 TextUtil.cleanUTF(value.toString()).trim()));
                               row.appendChild(node);
					       }
                        }
                    }
                }
            } catch(SQLException e) { log(e); }
        }
        //if (test) {
            //FileUtil.write("data/test.xml", transformer.asString(doc));
            //log("wrote " + "data/test.xml");
        //}
        return transformer.transform(document);
    }

    @Override
    public String[] getIdentifiers(int off, int limit) {
        if (identifier!=null) {
		    return new String[] {identifier, null};
        }
        List<String> results = identifiers(off, limit);
        return results.toArray(new String[results.size()]);
    }

    public List<String> identifiers(int off, int limit) {
        String query = idxQuery + " limit " + off +"," + limit;
        return database.getColumn(query,1,limit);
    }

    @Override
    public int crawl(String str) {
        //log("crawl " + str);
        identifier = str;
        return 1;
    }

    @Override
    public Model test(String resource) {
        //test = true;
        return read(resource);
    }

    private Document createDocument() {
		Document doc = null;
		try {
            DocumentBuilder builder =factory.newDocumentBuilder();
            doc = builder.newDocument();
		} catch (ParserConfigurationException e) { log(e); }
        return doc;
    }

    private static final Logger logger =
                         Logger.getLogger(DBTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception ex) {
        logger.info(ex.toString());
        ex.printStackTrace();
    }
}
