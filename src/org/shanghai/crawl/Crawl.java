package org.shanghai.crawl;

import org.shanghai.rdf.Config;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.crawl.RDFStorage;
import org.shanghai.crawl.FileTransporter;

import java.io.File;
import java.util.logging.Logger;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop 
   @title The Crawler API
   @date 2013-02-23
   @abstract Creates Transporter and Storage and starts crawling 
*/
public class Crawl {

    protected Config config;
    protected MetaCrawl.Transporter transporter;
    protected MetaCrawl.Storage storage;
    protected MetaCrawl crawler;
    protected FileTransporter fc;
    protected int count=0;
    protected String testFile;
    protected String source;
    protected String target;

    public Crawl(Config config) {
        this.config = config;
    }

    public void dispose() {
        if (crawler!=null)
            crawler.dispose();
        crawler=null;
	    if (transporter!=null)
		    transporter.dispose();
        transporter=null;
	    if (storage!=null)
		    storage.dispose();
        storage=null;
    }

    public void create() {
        testFile = config.get("crawl.test");
        source = config.get("crawl.source");
        target = config.get("crawl.target");
    }

    //public void create(String source, String target) {
    //    testFile = config.get("crawl.test");
    //    this.source = source;
    //    this.target = target;
    //}

    protected void createCrawler() {
        if (transporter==null) {
            createTransporter(source);
        }
        if ("test".equals(target)) {
            crawler = new MetaCrawl(transporter,testFile);
            crawler.create();
        } else {
            int logC = config.getInt("crawl.count");
            boolean create = config.getBoolean("crawl.create");
            if (storage==null) 
                createStorage(target);
            if (storage==null) 
                log("Bad storage : will break.");
            crawler = new MetaCrawl(transporter,storage,testFile,logC,create);
            crawler.create();
        }
    }

    public void createTransporter(String crawl) {
        if ("files".equals(crawl)) {
            String suffix = config.get("files.suffix");
            int depth = config.getInt("files.depth");
            int logC = config.getInt("files.count");
            fc = new FileTransporter(suffix,depth,logC);
            fc.create(); 
            transporter = fc.inject(new TrivialScanner().create());
            log("createTransporter " + crawl);
        } else if (crawl.equals("tdb") || crawl.equals("virt")) {
            String store = config.get(crawl+".store");
            String probe = config.get(crawl+".probe");
            String enum_ = config.get(crawl+".enum");
            String dump  = config.get(crawl+".dump");
            String date  = config.get(crawl+".date");
            transporter  = new TDBTransporter(store, probe, enum_, dump, date);
            transporter.create();
            log("createTransporter " + crawl);
            //log("probe " + probe);
        } else {
            log("createTransporter failed");
        }
    }

    public MetaCrawl.Storage createStorage(String store) {
        if (store.equals("tdb")) {
            String tdb = config.get("tdb.store");
            String graph = config.get("tdb.graph");
            storage = new RDFStorage(tdb,graph);
            storage.create();
        } else if (store.startsWith("virt")) {
            String virt = config.get(store+".store");
            String graph = config.get(store+".graph");
            String dbuser = config.get(store+".dbuser");
            String dbpass = config.get(store+".dbpass");
            storage = new RDFStorage(virt,graph,dbuser,dbpass);
            storage.create();
        } else if (store.startsWith("solr")) {
            String solr=config.get(store+".url")+"/"+config.get(store+".core");
            String transformer = config.get(store+".transformer");
            if (transformer==null)
                log("Missing transformer [" + transformer + "]. Will break.");
            log("createStorage [" + store + "] [" + transformer + "]");
            storage = new SolrStorage(solr,transformer);
            storage.create();
        } else if (store.startsWith("files")) {
            String directory = config.get(store+".store");
            storage = new FileStorage(directory);
            storage.create();
            log("createStorage [" + store + "]");
        } else {
            log("No storage! [" + store + "]");
        }
        return storage;
    }

    public void probe() {
        createTransporter(source);
        crawler = new MetaCrawl(transporter,testFile);
        crawler.create();
        crawler.probe();
    }

    public void test() {
        createTransporter(source);
        crawler = new MetaCrawl(transporter,testFile);
        crawler.create();
        crawler.test();
        crawler.dispose();
    }

    public void test(String rc) {
        createTransporter(source);
        crawler = new MetaCrawl(transporter,testFile);
        crawler.create();
        crawler.test(rc);
        crawler.dispose();
    }

    public void dump() {
        createTransporter(source);
        crawler = new MetaCrawl(transporter,testFile);
        crawler.create();
        crawler.test();
        crawler.dump();
        crawler.dispose();
    }

    public void dump(String rc) {
        createTransporter(source);
        crawler = new MetaCrawl(transporter,testFile);
        crawler.create();
        crawler.dump(rc);
        crawler.dispose();
    }

    public void clean() {
        createCrawler();
        crawler.destroy();
        crawler.dispose();
    }

    public void delete(String rc) {
        createCrawler();
        crawler.delete(rc);
        crawler.dispose();
    }

    public void post(String rc) {
        createCrawler();
        crawler.post(rc);
        crawler.dispose();
    }

    public void sync() {
        createCrawler();
        crawler.sync();
        crawler.dispose();
    }

    public void sync(String source) {
        createTransporter(source);
        createCrawler();
        sync();
    }

    public void sync(String source, String target) {
        createTransporter(source);
        createStorage(target);
        sync();
    }

    public void sync(String source, String target, String directory) {
        createTransporter(source);
        createStorage(target);
        createCrawler();
        crawl(directory);
        crawler.sync();
        crawler.dispose();
    }

    public void crawl(String[] directories) {
        if (directories.length<2)
            return;
        count=0;
        int dirCount=0;
        long start = System.currentTimeMillis();
        for (String dir: directories) {
            if (dir.startsWith("-"))
                continue;
            dirCount++;
            crawl(dir);
            crawl(); // copy transporter to storage
        }
        long end = System.currentTimeMillis();
        if (dirCount>1 && count>1)
        log("crawled " + count + " items in " + dirCount + " directories "
                       + ((double)Math.round(end - start)/1000) + " sec");
    }

    public void crawl(String dir) {
        File file = new File(dir);
        if (file.exists()) {
            count += crawler.crawl(dir);
        } else {
            String home = System.getProperty("user.home");
            if (new File(home + "/" + dir).exists()) {
                if (fc!=null)
                    fc.setDirectory(home);
                else log("fc was zero");
                count += crawler.crawl(home + "/" + dir);
            } 
        }
        log("crawled " + dir + " " + count);
    }

    public void crawl() {
        crawler.crawl();
    }

    private static final Logger logger =
                         Logger.getLogger(Crawl.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

}
