package org.shanghai.rdf;

import org.shanghai.crawl.FileUtil;

import java.util.Iterator;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;

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

    private static final Logger logger =
                         Logger.getLogger(RDFTransporter.class.getName());

    RDFReader rdfReader;
    XMLTransformer transformer;

    String probeQuery;
    String indexQuery;
    String descrQuery;

    Properties prop;
    int size;

    public RDFTransporter(Properties prop) {
        this.prop = prop;
    }

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    public void create() {
        try {
            probeQuery = FileUtil.readFile(prop.getProperty("probe.sparql"));
            indexQuery = FileUtil.readFile(prop.getProperty("index.sparql"));
            descrQuery = FileUtil.readFile(prop.getProperty("about.sparql"));
            rdfReader = new RDFReader(prop.getProperty("service.sparql"), 
                                      prop.getProperty("data.graph"));
		    rdfReader.create();
        } catch(IOException e) { log(e); }
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
    public String getDescription(String bid) {
        return rdfReader.getDescription(descrQuery, bid);
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
        String testRdf = prop.getProperty("test.rdf");
        if (testRdf==null) {
            System.out.println(rdf);
        } else try {
            FileUtil.writeFile(testRdf, rdf);
            log("wrote " + testRdf);
        } catch(IOException e) { log(e); }
    }

    private void write(String outfile) {
        String talk = getRecord();
        try {
            FileUtil.writeFile(outfile, talk);
        } catch(IOException e) { log(e); }
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
