package org.shanghai.crawl;

import org.shanghai.rdf.Config;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.data.FileTransporter;
import org.shanghai.data.FileStorage;
import org.shanghai.data.EmptyTransporter;
import org.shanghai.data.EmptyStorage;
import org.shanghai.solr.SolrStorage;
import org.shanghai.ojs.OAITransporter;
import org.shanghai.data.FileTransporter;
import org.shanghai.data.VoidTransporter;
import org.shanghai.data.SourceScanner;
import org.shanghai.data.DumpStorage;
import org.shanghai.data.PDFScanner;
import org.shanghai.ojs.OJSTransporter;
import org.shanghai.ojs.NLMScanner;
import org.shanghai.data.SourceScanner;
import org.shanghai.data.FileScanner;

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
    }

    public void createTransporter(String crawl) {
        if (crawl.contains("files")) {
            source = "files"; // make parent understand
            String suffix = config.get("files.suffix");
            if (crawl.contains(":")) {
                suffix = "." + crawl.substring(crawl.indexOf(":")+1);
            }
            log("file transporter for [" + suffix + "]");
            int depth = config.getInt("files.depth");
            int logC = config.getInt("files.count");
            fc = new FileTransporter(suffix,depth,logC);
            fc.create();
            if (suffix.contains(".java")||suffix.contains(".php")) {
                fc.inject(new SourceScanner().create());
            }
            if (suffix.contains(".pdf")) {
                fc.inject(new PDFScanner(target.startsWith(source)).create());
            }
            //if (suffix.contains(".epub")) {
            //    fc.inject(new EpubScanner().create());
            //}
            if (suffix.contains(".rdf") || suffix.contains(".abd")) {
                fc.inject(new TrivialScanner().create());
            }
            if (suffix.contains(".nlm")) {
                String server = config.get("ojs.server");
                String xslt = config.get("files.nlm");
                String schema = config.get("schema.urn");
                fc.inject(new NLMScanner(server, xslt, schema).create());
                log("injected NLMScanner " + xslt);
            }
            if (suffix.contains(".txt")) {
                fc.inject(new FileScanner().create());
            }
            transporter = fc;
        } else if ("oai".equals(crawl)) {
            Config.OAI settings = config.getOAIList().get(0);
            settings.urn_prefix = config.get("schema.urn");
            transporter = new OAITransporter(settings);
            transporter.create();
        } else if (crawl.equals("void")) {
            transporter = new VoidTransporter();
			transporter.create();
        } else if (crawl.equals("empty")) {
            transporter = new EmptyTransporter();
			transporter.create();
        } else if (crawl.startsWith("ojs")) {
            String[] db = {
                     config.get("ojs.dbhost"), config.get("ojs.dbase"),
                     config.get("ojs.dbuser"), config.get("ojs.dbpass")};
            String[] idx = { config.get("ojs.enum"), config.get("ojs.dump"),
                     config.get("ojs.transporter") };
            String[] srv = { config.get("ojs.server"),
                     config.get("ojs.docbase"), config.get("schema.urn")};
            transporter = new OJSTransporter(db, idx, srv);
            transporter.create();
        } else {
            String store = config.get(crawl+".sparql");
            if (crawl.equals("tdb")) {
                store = crawl;
                log("tdb transporter " + store);
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
            String directory = config.get("files.docbase");
            if (source.equals("oai")) { // write model files to oai archive
                String archive = config.getOAIList().get(oai_counter).archive;
                if (archive!=null) {
                    directory = config.getOAIList().get(oai_counter).archive;
                }
            } 
            boolean force = config.getBool("files.force");
            if (store.contains(":")) {
                directory = store.substring(store.indexOf(":")+1);
                if (store.equals("files:data")) {
                    force = true;
                }
            }
            storage = new FileStorage(directory, force);
            storage.create();
        } else if ("empty".equals(store)) {
            storage = new EmptyStorage();
            storage.create();
        } else if ("console".equals(store)) {
            storage = new EmptyStorage(true);
            storage.create();
        } else if (store.startsWith("dump")) {
            String dumpFile = config.get("crawl.test");
            storage = new DumpStorage(dumpFile);
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
        createCrawler();
        int found = crawler.index(resource);
        log("crawl #" + found + " " + resource);
        if (found==0) {
            crawler.test(resource);
        } else {
            crawler.test(transporter.getIdentifiers(0,1).get(0));
        }
        //crawler.dispose();
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

    public void dump(String resource) {
        //log("dump # " + resource);
        createTransporter(source);
        crawler = new MetaCrawl(transporter,testFile);
        crawler.create();
        crawler.dump(resource);
        crawler.dispose();
    }

    public void dump(String rc, String fn) {
        log("dump " + rc + " : " + fn);
        createCrawler();
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
        createCrawler();
        for (String dir: directories) {
            if (dir.startsWith("-")) {
                continue;
            }
            dirCount++;
            crawl(dir);
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

    private void crawl(String resource) {
        int found = crawler.index(resource);
        log("crawl #" + found + " " + resource);
        if (found==0) {
            found = crawler.crawl(resource);
        } else {
            found = crawler.crawl();
        }
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
