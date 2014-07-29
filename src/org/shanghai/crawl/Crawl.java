package org.shanghai.crawl;

import org.shanghai.rdf.Config;
import org.shanghai.crawl.SolrStorage;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.crawl.RDFStorage;
import org.shanghai.crawl.FileTransporter;
import org.shanghai.oai.OAITransporter;

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

    public void createCrawler() {
        if (transporter==null) {
            createTransporter(source);
        }
        if ("test".equals(target)) {
            crawler = new MetaCrawl(transporter,testFile);
            crawler.create();
        } else {
            int logC = config.getInt("crawl.count");
            boolean create = config.getBoolean("crawl.create");
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
        if ("files".equals(crawl)) {
            String suffix = config.get("files.suffix");
            int depth = config.getInt("files.depth");
            int logC = config.getInt("files.count");
            fc = new FileTransporter(suffix,depth,logC);
            fc.create(); 
            transporter = fc.inject(new TrivialScanner().create());
            log("createTransporter [" + crawl + "]");
        } else if ("oai".equals(crawl)) {
            Config.OAI settings = config.getOAIList().get(0);
            boolean archive = settings.archive!=null;
            transporter = new OAITransporter(settings, archive);
            transporter.create();
        } else //if (crawl.equals("tdb") || crawl.equals("virt") 
               // || crawl.equals("4store")) 
            {
            //String store = config.get(crawl+".store");
            //if (store==null) {
            //    store = config.get(crawl+".sparql");
            //}
            String store = config.get(crawl+".sparql");
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
                log("createTransporter [" + crawl + "]");
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
            log("createStorage [" + store + "]");
        } else if (store.startsWith("four")) {
            String four = config.get(store+".store");
            String graph = config.get(store+".graph");
            storage = new RDFStorage(four,graph);
            storage.create();
        } else if (store.startsWith("solr")) {
            String solr=config.get(store+".url")+"/"+config.get(store+".core");
            String transformer = config.get(store+".transformer");
            if (transformer==null)
                log("Missing transformer [" + solr + "]. Will break.");
            log("createStorage [" + store + "] [" + transformer + "]");
            storage = new SolrStorage(solr,transformer);
            storage.create();
        } else if (store.startsWith("files")) {
            String directory = config.get("files.store");
            storage = new FileStorage(directory);
            storage.create();
            log("createStorage [" + store + "]");
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

    public void test(String rc) {
        createTransporter(source);
        crawler = new MetaCrawl(transporter,testFile);
        crawler.create();
        crawler.test(rc);
        crawler.dispose();
    }

    public void test(String from, String until) {
        createTransporter(source);
        crawler = new MetaCrawl(transporter,testFile);
        crawler.create();
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
        //for (String s : directories) System.out.print(s + " ");
        //System.out.println(directories.length);

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
            for (String dir: directories) {
                if (dir.startsWith("-")) {
                    continue;
                }
                if (dir.startsWith(":")) {
                    target = dir.substring(1);
                }
                dirCount++;
                createCrawler();
                crawl(dir);
            }
        }
        long end = System.currentTimeMillis();
        if (dirCount>1 && count>1)
        log("crawled " + count + " items in " + dirCount + " directories "
                       + ((double)Math.round(end - start)/1000) + " sec");
    }

    public void crawl() {
        //if (crawler==null) //GH201402: wtf.
        //    createCrawler();
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
                    createCrawler();
                    crawl();
                }
                transporter.dispose();
            }
        }
    }

    private void crawl(String dir) {
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
        crawl(); // copy transporter to storage
        log("crawled " + dir + " " + count);
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
