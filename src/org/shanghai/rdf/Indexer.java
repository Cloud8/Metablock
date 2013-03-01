package org.shanghai.rdf;

import org.shanghai.crawl.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A Command Interface for the Shanghai RDF Indexer
   @date 2013-01-17
   @abstract Indexer : RDFCrawl : RDFTransporter : RDFReader : ModelTalk
                    RDFCrawl : XMLTransformer, SolrPost 
*/

public class Indexer {

    RDFCrawl rdfCrawl = null;
    RDFTransporter rdfTransporter = null;
    Properties prop;

    private static final Logger logger =
                         Logger.getLogger(Indexer.class.getName());

    public Indexer(Properties prop) {
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

    public Indexer create() {
        return this;
    }

    public void dispose() {
    }

    public void dump() {
        rdfTransporter = new RDFTransporter(prop);
        rdfTransporter.create();    
        rdfTransporter.probe();
        rdfTransporter.dispose();    
    }

    public void test(String offset) {
        int off = Integer.parseInt(offset);
        test(off);
    }

    public void test() {
        rdfTransporter = new RDFTransporter(prop);
        rdfTransporter.create();    
        int size = rdfTransporter.size();
        log("store size: " + size);
        if (size==0)
            return;
        int rand = (int)(Math.random()*size);
        test(rand);
        rdfTransporter.dispose();    
    }

    private void test(int off) {
        // int x = Integer.parseInt(what);
        rdfTransporter = new RDFTransporter(prop);
        rdfTransporter.create();    
        int x = 22;
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

    public void post(String what) {
        log("post: " + what);
        rdfCrawl = new RDFCrawl(prop);
        rdfCrawl.create();
        rdfCrawl.post(what);
        rdfCrawl.dispose();
    }

    public void index(String offset, String limit) {
        int off = Integer.parseInt(offset);
        int max = Integer.parseInt(limit);
        log("index offset " + off + " limit " + max);
        rdfCrawl = new RDFCrawl(prop);
        rdfCrawl.create();
        boolean b = rdfCrawl.index(off, max);
        rdfCrawl.dispose();
    }

    public void index(String offset) {
        if (offset.equals("test"))
            testCrawl();
        else
            index(offset, "42");
    }

    public void index() {
        log("index routine starts.");
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
    /**
    public void getDescription(String uri) {
        rdfTransporter = new RDFTransporter(prop);
        rdfTransporter.create();    
        rdfTransporter.talk(uri);    
        rdfTransporter.dispose();    
    }
    **/

    public void dump(String uri) {
        String rdf = getDescription(uri);
        System.out.println(rdf);
    }

    private String getDescription(String uri) {
        log(uri);
        rdfTransporter = new RDFTransporter(prop);
        rdfTransporter.create();    
        String rdf = rdfTransporter.getDescription(uri); 
        rdfTransporter.dispose();    
        return rdf;
    }

    public void dump(String uri, String filename) {
        try {
            FileUtil.writeFile(filename, getDescription(uri));
        } catch(IOException e) { log(e); }
    }

    /** clean the solr index, not the triple store data */
    public void cleanSolr() {
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

    public void talk() {
        log("usage: to do");
    }
}
