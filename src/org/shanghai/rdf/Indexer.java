package org.shanghai.rdf;

import org.shanghai.util.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;
import java.lang.Character;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Command Line Interface for the Shanghai RDF Indexer
    @date 2013-10-17
*/
public class Indexer {

    protected Config config;
    private RDFCrawl rdfCrawl = null;
    private RDFCrawl.Storage storage = null;
    private RDFCrawl.Transporter transporter = null;
    private Config.Index idx;
    private int count = 0;

    public Indexer(Config c) {
        this.config = c;
        this.idx = config.getIndex();
    }

    public Indexer(Config c, String index) {
        this.config = c;
        this.idx = config.getIndex(index);
        if (idx==null)
             log("Bad Index definition. Will break");
        else log("active enum: " + idx.enum_);
    }

    public void dispose() {
        if (transporter!=null)
            transporter.dispose();
        transporter = null;
        if (storage!=null)
            storage.dispose();
        storage = null;
        if (rdfCrawl!=null)
            rdfCrawl.dispose();
        rdfCrawl = null;
    }

    public void create() {
        String sparql = idx.sparql;
        String probe = idx.probe;
        String enum_ = idx.enum_;
        String dump  = idx.dump;
        String date  = idx.date;
        if (sparql.equals("tdb")) {
            sparql = config.get("tdb.store");
        }
        transporter = new RDFTransporter(sparql,probe,enum_,dump,date); 
        transporter.create();
    }

    private void createStorage() {
        String solr = idx.store;
        if (solr.equals("solr")) {
            solr = config.get("solr.url")+config.get("solr.core");
        } 
        storage = new SolrPost(solr);
        storage.create();

        String xsltFile = idx.transformer;
        String testFile = idx.test;
        int logC = idx.count;
        rdfCrawl = new RDFCrawl(transporter,storage,xsltFile,testFile,logC);
        rdfCrawl.create();
    }

    public void index(String[] args) {
        int argc=0;
        if (args.length==0 || args[argc].startsWith("-c") 
            || args[argc].startsWith("-d") || args[argc].startsWith("-post") ) 
            createStorage();
        if (args.length==0) {
            index(idx);
        } else if (args[argc].startsWith("-clean")) {
            clean();
        } else if (args[argc].startsWith("-destroy")) {
            clean();
        } else if (args[argc].startsWith("-probe")) {
			probe();
        } else if (args[argc].startsWith("-test")) {
            argc++;
            if (args.length-argc==2)
			    test(args[argc++],args[argc++]);
            else if (args.length-argc==1)
			    test(args[argc++]);
			else
                test();
        } else if (args[argc].endsWith("-dump")) {
            argc++;
            if (args.length-argc==2) {
                dump(args[argc++], args[argc++]);
            } else if (args.length-argc==1) {
                dump(args[argc++]);
            } else {
                dump();
            }
        } else if (args[argc].endsWith("-post")) {
            argc++;
            createStorage();
            post(args[argc++]);
        } else if (args[argc].startsWith("-del")) {
            argc++;
            if (args.length-argc==1) {
                System.out.println("# remove solr record " + args[argc]);
                remove(args[argc++]);
            }
        }
    }

    private void probe() {
        transporter.probe();
    }

    private void test() {
        test(0,12);
    }

    private void test(String offset) {
        int off = Integer.parseInt(offset);
        test(off,22);
    }

    private void test(String offset, String limit) {
        int x = Integer.parseInt(offset);
        int y = Integer.parseInt(limit) +1;
        test(x,y);
    }

    private void test(int off, int count) {
        int x = count;
        int i = 0;
        for(String id: transporter.getIdentifiers(off,count-1)) {
            System.out.println(" " + (i++) + " [" + id + "]");
        }
    }

    private void post(String resource) {
        log("post: " + resource);
        rdfCrawl.post(resource);
    }

    private void index(String offset, String limit) {
        int off = Integer.parseInt(offset);
        int max = Integer.parseInt(limit);
        log("index offset " + off + " limit " + max);
        boolean b = rdfCrawl.index(off, max);
        log("indexed " + rdfCrawl.count + " records."); 
    }

    private void index(Config.Index idx) {
        if (idx==null) {
            log("invalid.");
            return;
        }
        log("index routine " + idx.name + " starts.");
        long start = System.currentTimeMillis();
        rdfCrawl.index();
        long end = System.currentTimeMillis();
        double rs = ((end - start)/1000);
        if ( rs>0.0 ) //prevent div by zero
             rs = rdfCrawl.count / rs ;
        log("indexed " + rdfCrawl.count + " records in " 
                       + ((end - start)/1000) + " sec [" + rs +" rec/s]");
    }

    private String getDescription(String uri) {
        log(uri);
        return rdfCrawl.read(uri); 
    }

    private void dump(String uri, String filename) {
        FileUtil.write(filename, getDescription(uri));
    }

    private void dump(String uri) {
        String rdf = getDescription(uri);
        System.out.println(rdf);
    }

    private void dump() {
        String uri = transporter.getIdentifiers(0,1)[0];
        String rdf = getDescription(uri); 
        System.out.println(rdf);
    }

    private void remove(String id) {
        storage.delete(id);
    }

    private void clean() {
        storage.destroy();
    }

    private void log(String[] msg) {
        String str = new String("[");
        for(String s : msg) 
            str += " "+s;
        str += "]";
        logger.info("args: " + str);
    }

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    private static final Logger logger =
                         Logger.getLogger(Indexer.class.getName());

}
