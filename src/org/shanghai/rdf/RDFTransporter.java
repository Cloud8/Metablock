package org.shanghai.rdf;

import org.shanghai.util.FileUtil;

import java.util.Iterator;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.io.StringReader;
import java.io.StringWriter;


/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A RDF Transporter with CRUD functionality
   @date 2013-01-17
*/
public class RDFTransporter {

    public interface Reader {
        public Reader create();
        public void dispose();
        public String[] getSubjects(String q, int offset, int limit);
        public String getDescription(String query, String subject);
        public String query(String query);
    }

    public Properties prop;

    private static final Logger logger =
                         Logger.getLogger(RDFTransporter.class.getName());

    private Reader rdfReader;

    private String probeQuery;
    private String indexQuery;
    private String descrQuery;

    private int size;

    public RDFTransporter(RDFReader.Interface modelTalk) {
        this.rdfReader = new RDFReader(modelTalk);
    }

    public RDFTransporter(Properties prop) {
        this.prop = prop;
        String s = prop.getProperty("index.sparql");
        s=s==null?s=prop.getProperty("store.tdb"):s;
        if (s.equals("http://localhost/shanghai/store"))
            s=prop.getProperty("store.tdb");
        RDFReader.Interface modelTalk = 
                         new ModelTalk( s, prop.getProperty("store.graph")); 
        this.rdfReader = new RDFReader(modelTalk);
    }

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    /** create queries, resolve <date> */
    public void create() {
        String date = prop.getProperty("index.days");
        probeQuery = FileUtil.read(prop.getProperty("index.probe"));
        indexQuery = FileUtil.read(prop.getProperty("index.enum"));
        if (probeQuery==null || probeQuery.trim().length()==0) 
            log("missing " + prop.getProperty("index.probe"));
        else if (indexQuery==null) 
            log("missing " + prop.getProperty("index.enum"));
        else if (date!=null) {
            probeQuery = probeQuery.replace("<date>", date);
            indexQuery = indexQuery.replace("<date>", date);
        } else {
            probeQuery = probeQuery.replace("<date>", "1970-01-01");
            indexQuery = indexQuery.replace("<date>", "1970-01-01");
        }
        descrQuery = FileUtil.read(prop.getProperty("index.dump"));
        if (descrQuery==null)
            log("missing " + prop.getProperty("index.dump"));
        rdfReader.create();
        size=0;
    }

    public void dispose() {
        if (rdfReader!=null) 
            rdfReader.dispose();
    }

    public String[] getIdentifiers(int offset, int limit) {
        //log("offset " + offset + " limit " + limit);
        return rdfReader.getSubjects(indexQuery, offset, limit);
    }

    /** return a transformed record as xml String */
    public String getDescription(String subject) {
        return rdfReader.getDescription(descrQuery, subject);
    }

    // public boolean delete(String bid) {
    //     return rdfReader.delete(bid);
    // }

    /** should deliver the number of triples in the store */
    public int size() {
        if (size>0)
            return size;
        String result = rdfReader.query(probeQuery);
        try { 
          if (result==null) {
              log("problem with probeQuery [" + probeQuery + "]");
              prop.list(System.out);
              return 0;
          }
          size = Integer.parseInt(result);
          if (size==0) {
              log("query: " + probeQuery);
              log("result: [" + result + "]");
          }
        } catch (NumberFormatException e) { log(e); }
        // log("size " + size);
        return size;
    }

    public void talk(String what) {
        String rdf = rdfReader.getDescription(descrQuery, what);
        String testRdf = prop.getProperty("index.test");
        if (testRdf==null) {
            System.out.println(rdf);
        } else {
            FileUtil.write(testRdf, rdf);
            log("wrote " + testRdf);
        } 
    }

    private void write(String outfile) {
        String talk = getRecord();
        FileUtil.write(outfile, talk);
    }

    public String getRecord() {
        String[] ids = rdfReader.getSubjects(indexQuery, 0, 1);
        String rdf = rdfReader.getDescription(descrQuery, ids[0]);
        return rdf;
    }

    /** test a random record */
    public void probe() {
        int max = size();
        if (max==0) {
            log("No triples in the store. Check size.");
            return;
        }
        int off = (int)(Math.random() * max);
        String[] identifiers = rdfReader.getSubjects(indexQuery, off, 1);
        String what = identifiers[0];
        log( "resource " + off + ": " + what );
        talk(what);
    }
}
