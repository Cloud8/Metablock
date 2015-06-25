package org.shanghai.rdf;

import org.shanghai.util.FileUtil;
import java.util.logging.Logger;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Interface for the Shanghai RDF Indexer
    @date 2013-10-17
*/
public class Indexer {

    protected Config config;
    private MetaCrawl rdfCrawl = null;
    private MetaCrawl.Storage storage = null;
    private MetaCrawl.Transporter transporter = null;
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
        String store = idx.store;
        String xsltFile = idx.transformer;
        if (store.startsWith("solr")) {
            String solr = config.get(store+".url")
                        + "/" + config.get(store+".core");
            xsltFile = config.get(store+".transformer");
            storage = new SolrStorage(solr, xsltFile);
        } else { // support http:// signature
            storage = new SolrStorage(store, xsltFile);
        }
        storage.create();

        String testFile = idx.test;
        int logC = idx.count;
        rdfCrawl = new MetaCrawl(transporter,storage,xsltFile,testFile,logC);
        rdfCrawl.create();
    }

    /** check source */
    public void probe() {
        transporter.probe();
    }

    /** test source */
    public void test() {
        test(0,12);
    }

    /** test source */
    public void test(String off) {
        test(Integer.parseInt(off), 12);
    }

    /** resource dump */
    public void dump() {
        String uri = transporter.getIdentifiers(0,1)[0];
        createStorage();
        String rdf = rdfCrawl.read(uri); 
        System.out.println(rdf);
    }

    /** resource dump */
    public void dump(String uri) {
        //createStorage();
        //log("short dump");
        rdfCrawl = new MetaCrawl(transporter,null,null,null,0);
        rdfCrawl.create();
        String rdf = rdfCrawl.read(uri);
        System.out.println(rdf);
    }

    public void dump(String uri, String filename) {
        //createStorage();
        rdfCrawl = new MetaCrawl(transporter,null,null,null,0);
        rdfCrawl.create();
        String rdf = rdfCrawl.read(uri);
        FileUtil.write(filename, rdf);
    }

    public void post(String resource) {
        log("post: " + resource);
        createStorage();
        rdfCrawl.post(resource);
    }

    /** index source to target */
    public void index() {
        createStorage();
        index(idx);
    }

    public void index(String from, String to) {
        createStorage();
        int off = Integer.parseInt(from);
        int limit = Integer.parseInt(to);
        rdfCrawl.index(off, limit);
    }

    public void clean() {
        createStorage();
        storage.destroy();
    }

    //public void index(String cmd, String arg1, String arg2) {
    //    if (cmd.endsWith("-dump")) {
    //        dump(arg1,arg2);
    //    } else if (cmd.equals("-test")) {
	//		test(arg1,arg2);
    //    }
    //}

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

    private void index(Config.Index idx) {
        if (idx==null) {
            log("invalid.");
            return;
        }
        log("index routine " + idx.name + " starts.");
        long start = System.currentTimeMillis();
        int count = rdfCrawl.index();
        long end = System.currentTimeMillis();
        double rs = ((end - start)/1000);
        if ( rs>0.0 ) //prevent div by zero
             rs = count / rs ;
        log("indexed " + count + " records in " 
                       + ((end - start)/1000) + " sec [" + rs +" rec/s]");
    }

    public void remove(String id) {
        log("remove ["+ id + "]");
        createStorage();
        storage.delete(id);
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
