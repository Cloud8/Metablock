package org.shanghai.jena;

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
   @title A Jena RDF Transporter 
   @date 2013-01-17
*/
public class RDFTransporter {

    private static final Logger logger =
                         Logger.getLogger(RDFTransporter.class.getName());

    TDBReader tdbReader;
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
            probeQuery = Util.readFile(prop.getProperty("probe.sparql"));
            indexQuery = Util.readFile(prop.getProperty("index.sparql"));
            descrQuery = Util.readFile(prop.getProperty("about.sparql"));
            tdbReader = new TDBReader(prop.getProperty("jena.tdb"), 
                                      prop.getProperty("jena.graph"));
		    tdbReader.create();
        } catch(IOException e) { log(e); }
    }

    public void dispose() {
        if (tdbReader!=null) 
            tdbReader.dispose();
    }

    public String[] getIdentifiers(int offset, int limit) {
        log("offset " + offset + " limit " + limit);
        return tdbReader.getSubjects(indexQuery, offset, limit);
    }

    /** update description with content from file */
    public boolean update(File f) {
        return loadDescription(f);
    }

    public boolean update(String filename) {
        return loadDescription(new File(filename));
    }

    /** return a transformed record as xml String */
    public String getDescription(String bid) {
        return tdbReader.getDescription(descrQuery, bid);
    }

    public boolean delete(String bid) {
        return tdbReader.delete(bid);
    }

    /** intended to deliver the number of triples in the store */
    public int probe() {
        String result = tdbReader.query(probeQuery);
        try { 
          size = Integer.parseInt(result);
          if (size==0) {
              log("query: " + probeQuery);
              log("result: [" + result + "]");
          }
        } catch (NumberFormatException e) { log(e); }
        //log("size: " + size);
        return size;
    }

    private boolean loadDescription(File file) {
        boolean b = false;
        try {
            String description = Util.readFile(file);
            InputStream in = 
                     new ByteArrayInputStream(description.getBytes("UTF-8"));
            b = tdbReader.update(in);
            if (in!=null) in.close();
            // log("loadDescription " + file.getName() + " " + b);
        } catch(IOException e) { log(e); }
        finally { return b; }
    }

    public void talk(String probe) {
        String rdf = tdbReader.getDescription(descrQuery, probe);
        String testRdf = prop.getProperty("test.rdf");
        if (testRdf==null)
            testRdf = "test-rdf.xml"; 
        try {
            Util.writeFile(testRdf, rdf);
        } catch(IOException e) { log(e); }
        log("wrote " + testRdf);
    }

    private void write(String outfile) {
        String talk = getRecord();
        try {
            Util.writeFile(outfile, talk);
        } catch(IOException e) { log(e); }
    }

    private void talk() {
        String talk = getRecord();
        System.out.println(talk);
    }

    public String getRecord() {
        String[] ids = tdbReader.getSubjects(indexQuery, 0, 1);
        String rdf = tdbReader.getDescription(descrQuery, ids[0]);
        //String solr = transformer.transform(rdf);
        //return solr;
        return rdf;
    }

    /** test */
    void test() {
        int max = probe();
        if (max==0) {
            log("No triples in the store. Check probe.");
            return;
        }
        int off = Math.min(22, max-1);
        int limit = Math.min(42, max);
        //int random = (int) (Math.random() * (high - low) + low)
        int random = (int) (Math.random() * (limit-1));

        //log("offset " + off + " limit " + limit);
        String[] identifiers = tdbReader.getSubjects(indexQuery, off, limit);
        //for (String identifier: identifiers) {
        //     System.out.println(identifier);
        //}
        // tdbReader.test();
        String probe = identifiers[random];
        log( "resource: " + probe );
        talk(probe);
    }

    public static void main(String[] args) {
	    int argc=0;
        Properties prop = new Properties();
        String properties = "shanghai.properties";
        if (args.length>argc && args[argc].endsWith("-prop")) {
            argc++;
            properties = args[argc];
            argc++;
        }
        try {
            prop.load(Main.class.getResourceAsStream("/" + properties));
        } catch(IOException e) { e.printStackTrace(); }
	    RDFTransporter myself = new RDFTransporter(prop);
        myself.create();
		if (args.length>argc && args[argc].endsWith("-test")) {
		    myself.test();
		} else if (args.length>argc && args[argc].endsWith("-talk")) {
		    myself.talk();
		} else if (args.length>argc && args[argc].endsWith("-probe")) {
		    myself.probe();
        } else if (args.length>argc && args[argc].endsWith("-get")) {
            argc++;
            if (args.length>argc)
                myself.talk(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-put")) {
            argc++;
            if (args.length>argc) {
                myself.update(args[argc]);
            }
        } else if (args.length>argc && args[argc].endsWith("-del")) {
            argc++;
            if (args.length>argc) {
                myself.delete(args[argc]);
            }
        } 
        myself.dispose();
    }
}
