package org.shanghai.rdf;

import org.shanghai.util.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A Command Line Interface for the Shanghai RDF Indexer
   @date 2013-01-17
*/

public class Main {

    RDFCrawl rdfCrawl = null;
    RDFTransporter rdfTransporter = null;
    Properties prop;
    String suffix;
    int depth;

    private static final Logger logger =
                         Logger.getLogger(Main.class.getName());

    public Main(Properties prop) {
        this.prop = prop;
    }

    static void log(String msg) {
        //logger.info(msg);
        System.out.println(msg);
    }

    static void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    public void create() {
    }

    public void dispose() {
    }

    private void probe() {
        log("RDFTransporter probe:");
        rdfTransporter = new RDFTransporter(prop);
        rdfTransporter.create();    
        rdfTransporter.probe();
        rdfTransporter.dispose();    
    }

    private void test(String offset) {
        int off = Integer.parseInt(offset);
        test(off);
    }

    private void test() {
        test(0);
    }

    private void test(int off) {
        // int x = Integer.parseInt(what);
        int x = 22;
        rdfTransporter = new RDFTransporter(prop);
        rdfTransporter.create();    
        int size = rdfTransporter.size();
        log("store size: " + size);
        if (size==0)
            return;
        for(String id: rdfTransporter.getIdentifiers(off,x-1)) {
            log( "[" + id + "]");
        }
        rdfTransporter.dispose();    
    }

    private void testCrawl() {
        log("RDFCrawl test:");
        RDFCrawl rdfCrawl = new RDFCrawl(prop);
        rdfCrawl.create();
        rdfCrawl.test();
        rdfCrawl.dispose();
    }

    private void post(String what) {
        log("post: " + what);
        rdfCrawl = new RDFCrawl(prop);
        rdfCrawl.create();
        rdfCrawl.post(what);
        rdfCrawl.dispose();
    }

    private void index(String offset, String limit) {
        int off = Integer.parseInt(offset);
        int max = Integer.parseInt(limit);
        log("index offset " + off + " limit " + max);
        rdfCrawl = new RDFCrawl(prop);
        rdfCrawl.create();
        boolean b = rdfCrawl.index(off, max);
        rdfCrawl.dispose();
    }

    private void index(String offset) {
        if (offset.equals("test"))
            testCrawl();
        else
            index(offset, "42");
    }

    private void index() {
        long start = System.currentTimeMillis();
        rdfCrawl = new RDFCrawl(prop);
        rdfCrawl.create();
        rdfCrawl.index();
        rdfCrawl.dispose();
        long end = System.currentTimeMillis();
        log("indexed " + rdfCrawl.count + " records in " 
                       + ((end - start)/1000) + " sec");
    }

    /** write everything known about the resource identified 
        by uri to the file given in the properties */
    private void getDescription(String uri) {
        rdfTransporter = new RDFTransporter(prop);
        rdfTransporter.create();    
        rdfTransporter.talk(uri);    
        rdfTransporter.dispose();    
    }

    private void getDescription(String uri, String filename) {
        log(uri + " " + filename);
        rdfTransporter = new RDFTransporter(prop);
        rdfTransporter.create();    
        String rdfDoc = rdfTransporter.getDescription(uri); 
        rdfTransporter.dispose();    
        try {
            FileUtil.writeFile(filename, rdfDoc);
        } catch(IOException e) { log(e); }
    }

    /** clean the solr index, not the triple store data */
    private void cleanSolr() {
        SolrPost solrPost = new SolrPost(prop.getProperty("index.solr"));
        solrPost.create();
        solrPost.clean();
        solrPost.dispose();
    }

    @Deprecated
    private void cleanStorage() {
        rdfCrawl = new RDFCrawl(prop);
        rdfCrawl.create();
        rdfCrawl.solrPost.clean();
        rdfCrawl.dispose();
    }

    private void talk() {
        log("usage: to do");
    }

    public static void main(String[] args) {
        int argc=0;

        // for (String s: args) log("arg " + s);
        Properties prop = new Properties();
        boolean b = false;
        if (args.length>argc && args[argc].endsWith("-prop")) {
		    argc++;
            try {
              log("loading " + args[argc]);
              prop.load(new FileReader(args[argc]));
              b=true;
            } catch(IOException e) { e.printStackTrace(); }
		    argc++;
        }
        if (!b) try {
            prop.load(Main.class.getResourceAsStream("/shanghai.properties"));
        } catch(IOException e) { e.printStackTrace(); }

        Main myself = new Main(prop);
        if (args.length>argc && args[argc].endsWith("-clean")) {
            myself.cleanSolr();
            return;
        }

        myself.create();

        if (args.length-1>argc && args[argc].endsWith("-test")) {
		    argc++;
			myself.test(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-test")) {
			myself.test();
        } else if (args.length>argc && args[argc].endsWith("-probe")) {
            myself.probe();
        } else if (args.length-2>argc && args[argc].endsWith("-index")) {
		    argc++;
            myself.index(args[argc++], args[argc]);
        } else if (args.length-1>argc && args[argc].endsWith("-index")) {
		    argc++;
            myself.index(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-index")) {
            myself.index();
        } else if (args.length>argc && args[argc].endsWith("-dump")) {
		    argc++;
            if (args.length-1>argc)
                myself.getDescription(args[argc], args[argc+1]);
            else myself.getDescription(args[argc]);
        } else if (args.length-1>argc && args[argc].endsWith("-post")) {
		    argc++;
            myself.post(args[argc]);
        } else {
            myself.talk();
        }
		myself.dispose();
    }
}
