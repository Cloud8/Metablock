package org.shanghai.crawl;

import org.shanghai.rdf.Config;
import org.shanghai.crawl.SolrStorage;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.crawl.RDFStorage;
import org.shanghai.crawl.FileTransporter;
import org.shanghai.oai.OAITransporter;
import org.shanghai.oai.NLMAnalyzer;

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
    protected String engine;
    private int oai_counter;

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
        oai_counter = 0;
    }

    public void create(String src, String trg, String eng) {
        testFile = config.get("crawl.test");
        source = src==null?config.get("crawl.source"):src;
        target = trg==null?config.get("crawl.target"):trg;
        engine = eng==null?config.get("crawl.engine"):eng;
    }

    protected void createCrawler() {
        if (transporter==null) {
            createTransporter(source);
        }
        if ("test".equals(target)) {
            crawler = new MetaCrawl(transporter,testFile);
            crawler.create();
        } else {
            int logC = config.getInt("crawl.count");
            boolean create = true; // insert statements
            if (engine!=null && engine.contains("del")) {
                create = false; // update: delete before insert
            }
            if (storage==null) {
                createStorage(target);
            } if (storage==null) {
                log("Bad storage : will break.");
            }
            crawler = new MetaCrawl(transporter,storage,testFile,logC,create);
            crawler.create();
        }
        if (engine!=null && engine.contains("nlm")) {
            String prefix = config.get("oai.urnPrefix");
            String directory = null;
            if (source.equals("oai")) { // write model files to oai archve
                directory = config.getOAIList().get(oai_counter).archive;
            } else if (target.startsWith("files")) { // help file target 
                if (target.contains(":")) {
                    directory = target.substring(target.indexOf(":")+1);
                } else {
                    directory = target;
                }
            }
            crawler.inject(new NLMAnalyzer(prefix, directory).create());
            log("injected NLMAnalyzer for [" + directory+ "]");
        }
    }

    public void createTransporter(String crawl) {
        if (crawl.contains("files")) {
            String suffix = config.get("files.suffix");
            //String base = config.get("files.base");
            //if (crawl.contains("#")) {
			//    base = crawl.substring(crawl.indexOf("#")+1);
            //}
            int depth = config.getInt("files.depth");
            int logC = config.getInt("files.count");
            fc = new FileTransporter(suffix,depth,logC);
            fc.create(); 
            transporter = fc.inject(new TrivialScanner().create());
            //log("createTransporter [" + crawl + "]");
        } else if ("oai".equals(crawl)) {
            Config.OAI settings = config.getOAIList().get(0);
            boolean archive = settings.archive!=null;
            transporter = new OAITransporter(settings, archive);
            transporter.create();
        } else {
            String store = config.get(crawl+".sparql");
            if (store!=null && store.equals("tdb")) {
                store = config.get("tdb.store");
                log("tdb store " + store);
            }
            String probe = config.get(crawl+".probe");
            if (probe==null) {
                log("no probe ? " + crawl+".probe");
            }
            String enum_ = config.get(crawl+".enum");
            String dump  = config.get(crawl+".dump");
            String date  = config.get(crawl+".date");
            if (store==null) {
                log("createTransporter failed");
            } else {
                //log("createTransporter [" + crawl + "]");
                transporter  = new RDFTransporter(store, probe, enum_, dump, date);
                transporter.create();
            }
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
            //log("createStorage [" + store + "]");
        } else if (store.startsWith("four")) {
            log("createStorage [" + store + "]");
            String four = config.get(store+".store");
            String graph = config.get(store+".graph");
            storage = new RDFStorage(four, graph, store);
            storage.create();
        } else if (store.startsWith("solr")) {
            String solr=config.get(store+".url")+"/"+config.get(store+".core");
            String transformer = config.get(store+".transformer");
            if (transformer==null)
                log("Missing transformer [" + solr + "]. Will break.");
            else log("createStorage [" + store + "] [" + transformer + "]");
            storage = new SolrStorage(solr,transformer);
            storage.create();
        } else if (store.startsWith("files")) {
            String directory = config.get("files.store");
            if (source.equals("oai")) { // write model files to oai archive
                directory = config.getOAIList().get(oai_counter).archive;
            } 
            if (store.contains(":")) {
                directory = store.substring(store.indexOf(":")+1);
            }
            storage = new FileStorage(directory);
            storage.create();
            log("createStorage [files:" + directory + "]");
        } else if ("empty".equals(store)) {
            storage = new EmptyStorage();
            storage.create();
        } else {
            log("No storage! [" + store + "]");
        }
        return storage;
    }

    public void probe() {
        createTransporter(source);
        crawler = new MetaCrawl(transporter,testFile);
        crawler.create();
        log(crawler.probe());
        crawler.dispose();
    }

    public void test() {
        log("test " + source + " target " + target);
        createTransporter(source);
        crawler = new MetaCrawl(transporter);
        crawler.create();
        crawler.test();
        crawler.dispose();
    }

    public void test(String resource) {
        log("test source: " + source + " target: " + target);
        createCrawler();
        createTransporter(source);
        if (source.equals("void")) {
            int found = crawler.index(resource);
            if (found>0) crawler.test();
        } else {
            crawler.test(resource);
        }
        crawler.dispose();
    }

    public void test(String from, String until) {
        target = "test";
        createCrawler();
        crawler.test(from, until);
        crawler.dispose();
    }

    public void dump() {
        createTransporter(source);
        crawler = new MetaCrawl(transporter,testFile);
        crawler.create();
        crawler.dump();
        crawler.dispose();
    }

    public void dump(String rc) {
        log("dump " + rc);
        createTransporter(source);
        crawler = new MetaCrawl(transporter,testFile);
        crawler.create();
        crawler.dump(rc);
        crawler.dispose();
    }

    public void dump(String rc, String fn) {
        createTransporter(source);
        crawler = new MetaCrawl(transporter,testFile);
        crawler.create();
        crawler.dump(rc, fn);
        crawler.dispose();
    }

    public void clean() {
        createStorage(target).destroy();
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

    public void crawl(String[] directories) {
        count=0;
        int dirCount=0;
        long start = System.currentTimeMillis();
        if (directories[1].equals("oai")) {
            source = "oai";
            if (directories.length>2) {
                target = directories[2];
            }
            crawl(source, target);
        } else {
            createCrawler();
            for (String dir: directories) {
                if (dir.startsWith("-")) {
                    continue;
                }
                dirCount++;
                crawl(dir);
            }
        }
        long end = System.currentTimeMillis();
        if (dirCount>1 && count>1)
        log("crawled " + count + " items in " + dirCount + " directories "
                       + ((double)Math.round(end - start)/1000) + " sec");
    }

    public void crawl() {
        if (crawler==null) { //GH201402: wtf.
            createCrawler();
        }
        crawler.crawl();
    }

    private boolean checkTarget(String target) {
        File f = new File(target);
        if (f.isDirectory() && f.canWrite()) {
            return true;
        }
        return false;
    }

    protected void crawl(String source, String target) {
        if ("oai".equals(source)) {
            boolean archive = checkTarget(target);
            if (archive) {
                createStorage("empty");
            } else if (!target.startsWith("-")) {
                createStorage(target);
            }
            for (int i=0; i<config.getOAIList().size(); i++) {
			    Config.OAI settings = config.getOAIList().get(i);
                if (!archive) {
                    archive = settings.archive!=null;
                } else {
                    settings.archive = target;
                    log("archive to " + settings.archive + " directory.");
                }
                transporter = new OAITransporter(settings, archive);
                transporter.create();
                if ("-probe".equals(target)) {
                    System.out.println(transporter.probe());
                } else if ("-test".equals(target)) {
                    ((OAITransporter)transporter).test();
                } else if ("-dump".equals(target)) {
                    dump();
                } else {
                    oai_counter = i; // use right setting
                    createCrawler();
                    crawl();
                }
                transporter.dispose();
            }
        }
    }

    private void crawl(String resource) {
        //log("crawl # " + resource);
        if (source.equals("files")) {
            //log("crawl files: " + resource);
            crawlFiles(resource);
        } else if (source.equals("void")) {
            int found = crawler.index(resource);
            if (found==0) {
                crawler.crawl(resource);
            } else {
                crawl();
            }
        } else {
            int b = crawler.crawl(resource);
        }
    }

    private void crawlFiles(String resource) {
        File file = new File(resource);
        if (file.exists()) {
            if (file.isDirectory()) {
                log("crawl dir: " + resource);
                count += crawler.index(resource);
            } else {
                count += crawler.crawl(resource);
            }
        } else {
            log("crawl home: " + resource);
            String home = System.getProperty("user.home");
            if (new File(home + "/" + resource).exists()) {
                if (fc!=null)
                    fc.setDirectory(home);
                else log("fc was zero");
                count += crawler.index(home + "/" + resource);
            } 
        }
        crawl(); // copy transporter to storage
        log("crawled " + resource + " " + count);
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
