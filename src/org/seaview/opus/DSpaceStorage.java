package org.seaview.opus;

import org.seaview.opus.REST;
import org.seaview.opus.BaosRecord;

import org.seaview.pdf.PDFLoader;

import org.shanghai.rdf.XMLTransformer;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;
import org.shanghai.util.ModelUtil;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDFS;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.XML;

import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.io.StringWriter;
import java.io.DataOutputStream;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Meta Block
  @title DSpace REST Storage
  @date 2015-12-14
*/
public class DSpaceStorage implements MetaCrawl.Storage {

    private static final String USER_AGENT = "Seaview/1.1";
    private String rest; // dspace rest
    private String user; // dspace user
    private String pass; 
    private String collection;
    private String xsltFile;
    private String test;
    private XMLTransformer transformer;
    private String token;
    private BaosRecord br;

    public DSpaceStorage(String[] rest, String[] collection) {
        this.rest = rest[0];
        this.user = rest[1];
        this.pass = rest[2];
        this.collection = collection[0];
        this.xsltFile = collection[1];
        this.test = collection[2];
        br = new BaosRecord(new PDFLoader(collection[3]));
    }

    @Override
    public void create() {
        transformer = new XMLTransformer(FileUtil.readResource(xsltFile));
        transformer.create();
        br.create();
        token = null;
    }

    @Override
    public void dispose() {
        transformer.dispose();
        token = null;
        br.dispose();
    }

    @Override
    public boolean test(Resource rc) {
        log("dspace test # " + rc.getURI()); 
        String xml = transformer.transform(rc);
        FileUtil.write(test, xml);
        boolean b = post(rest, rc, xml, true);
        return b;
    }

    @Override
    public boolean delete(String resource) {
        log("dspace delete: [" + resource + "]"); 
        return true;
    }

    @Override
    public boolean write(Resource rc) {
        String xml = transformer.transform(rc);
        boolean b = post(rest, rc, xml, false);
        return b;
    }

    @Override
    public void destroy() {
        log("sword destroy: unwilling to perform."); 
    }

    private String login(String rest, String user, String pass) {
        String json = "{\"email\":\""+user+"\", \"password\":\""+pass+"\"}";
        Properties header = new Properties();
        header.setProperty("Content-Type", "application/json");
        header.setProperty("accept", "application/json");
        String token = REST.post(rest+"/login", header, json);
        if (token==null) {
            throw new AssertionError("REST login failed.");
        }
        return token;
    }

    /** https://github.com/DSpace/DSpace/tree/master/dspace-rest */
    /* first create the item POST /collections/{collectionID}/items
       (Don't post the bitstream, only the item metadata is respected, 
       item bitstream is ignored)

       That returns you an Item, and (including its ID). 
       Then, you can pass the data of your file to
       POST /items/{itemID}/bitstreams
    */
    private boolean post(String rest, Resource rc, String xml, boolean test) {
        boolean b = true;
        String response = null;
        if (token==null) {
            token = login(rest, user, pass);
        }
        Properties header = new Properties();
        header.setProperty("rest-dspace-token", token);
        header.setProperty("Content-Type", "application/xml");
        header.setProperty("accept", "application/json");
        String url = rest + "/collections/" + collection + "/items";
        if (test) log("post " + url);
		String json = REST.post(url, header, xml, user, pass);
        if (test) log("create response " + json);
        if (json==null) {
            return false;
        }
        String uuid = new JSONObject(json).getString("uuid");
        if (uuid==null) {
            return false;
        } else if (test) {
            log("uuid " + uuid);
        }
        url = rest + "/items/" + uuid + "/bitstreams";
        header.clear();
        header.setProperty("rest-dspace-token", token);
        //header.setProperty("Content-Type", "application/pdf");
        header.setProperty("accept", "application/json");
        List<BaosRecord.Record> records = br.getData(rc);
		for (BaosRecord.Record record : records) {
            header.remove("Content-Type");
            header.setProperty("Content-Type", record.format);
            if (record.baos==null) {
                log("storage invalid " + rc.getURI() + " " + record.name);
                return false;
            } else {
                log("storage name: " + record.name);
            }
            //String purl = url + "?name=" + record.name + "&description=text";
            String purl = url + "?name=" + record.name;
		    json = REST.post(purl, header, record.baos, user, pass);
            if (test) log("bitstream response " + json);
		}
        return b;
    }

    private static final Logger logger =
                         Logger.getLogger(DSpaceStorage.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception ex) {
        logger.info(ex.toString());
        ex.printStackTrace();
    }
}
