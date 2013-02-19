package org.shanghai.jena;

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
        logger.info(msg);
        //System.out.println("Main: " + msg);
    }

    static void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    public void create() {
        suffix = prop.getProperty("crawl.suffix");
        String s = prop.getProperty("crawl.depth");
        if (s!=null) depth = Integer.parseInt(s);
    }

    public void dispose() {
    }

    private void crawl(String[] dirs) {
        long start = System.currentTimeMillis();
        FileCrawl crawler = new FileCrawl(prop);
        crawler.create();
        for (String dir: dirs) {
            File f = new File(dir);
            if (f.isDirectory()) {
                log("crawling " + dir + " with suffix " + suffix 
                    + " and depth " + depth + ".");
                crawler.crawl(dir, "", suffix, depth);
            }
        }
        crawler.dispose();
        long end = System.currentTimeMillis();
        log("crawled " + crawler.count + " records in "
                       + ((end - start)/1000) + " sec");
    }

    private void probe() {
        log("RDFTransporter test:");
        rdfTransporter = new RDFTransporter(prop);
        rdfTransporter.create();    
        rdfTransporter.probe();
        rdfTransporter.dispose();    
    }

    private void probe(String what) {
        int x = Integer.parseInt(what);
        rdfTransporter = new RDFTransporter(prop);
        rdfTransporter.create();    
        int size = rdfTransporter.size();
        log("store size: " + size);
        if (size==0)
            return;
        for(String id: rdfTransporter.getIdentifiers(0,x)) {
            log(id);
        }
        rdfTransporter.dispose();    
    }

    private void post(String what) {
        log("post: " + what);
        rdfCrawl = new RDFCrawl(prop);
        rdfCrawl.create();
        rdfCrawl.post(what);
        rdfCrawl.dispose();
    }

    private void test() {
        probe();
        log("RDFCrawl test:");
        RDFCrawl rdfCrawl = new RDFCrawl(prop);
        rdfCrawl.create();
        rdfCrawl.test();
        rdfCrawl.dispose();
    }

    @Deprecated
    private void crawl(String dir, String prae, String suf, String lev) {
        int level = Integer.parseInt(lev);
        long start = System.currentTimeMillis();
        FileCrawl crawler = new FileCrawl(prop);
        log(dir + " " + prae + " " + suf + " " + level);
        crawler.crawl(dir, prae, suf, level);
        //crawler.dispose();
        long end = System.currentTimeMillis();
        log("crawled " + crawler.count + " records from " + dir + " in "
                       + ((end - start)/1000) + " sec");
    }

    @Deprecated
    private void crawl(String dir) {
        long start = System.currentTimeMillis();
        FileCrawl crawler = new FileCrawl(prop);
        log(dir);
        // all files ending with rdf, recurse 3 levels into directories
        crawler.crawl(dir, "", ".rdf", 3);
        //crawler.dispose();
        long end = System.currentTimeMillis();
        log("crawled " + crawler.count + " records from " + dir + " in "
                       + ((end - start)/1000) + " sec");
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

    /** update triple store with description found in the file */
    private void update(String filename) {
        rdfTransporter = new RDFTransporter(prop);
        rdfTransporter.create();    
        rdfTransporter.update(filename);
        rdfTransporter.dispose();    
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

    /** delete everything where uri is a subject */
    private void delete(String uri) {
        rdfTransporter = new RDFTransporter(prop);
        rdfTransporter.create();    
        rdfTransporter.delete(uri);
        rdfTransporter.dispose();    
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

        if (args.length>argc && args[argc].endsWith("-test")) {
			myself.test();
        } else if (args.length-1>argc && args[argc].endsWith("-probe")) {
		    argc++;
            myself.probe(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-probe")) {
            myself.probe();
        } else if (args.length>argc && args[argc].endsWith("-crawl")) {
            myself.crawl(args);
        } else if (args.length-2>argc && args[argc].endsWith("-index")) {
		    argc++;
            myself.index(args[argc++], args[argc]);
        } else if (args.length-1>argc && args[argc].endsWith("-index")) {
		    argc++;
            myself.index(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-index")) {
            myself.index();
        } else if (args.length>argc && args[argc].endsWith("-upd")) {
		    argc++;
            myself.update(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-get")) {
		    argc++;
            if (args.length-1>argc)
                myself.getDescription(args[argc], args[argc+1]);
            else myself.getDescription(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-del")) {
		    argc++;
            myself.delete(args[argc]);
        } else if (args.length-1>argc && args[argc].endsWith("-post")) {
		    argc++;
            myself.post(args[argc]);
        } else {
            myself.talk();
        }
		myself.dispose();
    }
}
