package org.shanghai.crawl;

import org.shanghai.rdf.Config;
import org.shanghai.crawl.FileCrawl;

import java.io.File;
import java.util.logging.Logger;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop 
   @title The Main Crawler
   @date 2013-02-23
   @abstract Creates Transporter and Storage and starts crawling 
*/
public class Crawl {

    protected Config config;
    protected MetaCrawl.Transporter transporter;
    protected MetaCrawl.Storage storage;
    protected MetaCrawl crawler;

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
        String testFile = config.get("crawl.test");
        int logC = config.getInt("crawl.count");
        boolean create = config.getBoolean("crawl.create");
        createTransporter(config.get("crawl.source"));

        if ("test".equals(config.get("crawl.target"))) {
            crawler = new MetaCrawl(transporter,testFile);
            crawler.create();
        } else {
            createStorage(config.get("crawl.target"));
            if (storage==null) 
                log("Bad storage : will break.");
            crawler = new MetaCrawl(transporter,storage,testFile,logC,create);
            crawler.create();
        }
    }

    protected void createTransporter(String crawl) {
        if ("files".equals(crawl)) {
            String suffix = config.get("files.suffix");
            int depth = config.getInt("files.depth");
            int logC = config.getInt("files.count");
            FileCrawl fc = new FileCrawl(suffix,depth,logC);
            fc.create(); 
            transporter = fc.addScanner(new TrivialScanner().create());
            log("createTransporter " + crawl);
        } else if ("data".equals(crawl)) {
            String store = config.get("data.store");
            if (store.equals("tdb")) {
                store = config.get("tdb.store");
            }
            String probe = config.get("data.probe");
            String enum_ = config.get("data.enum");
            String dump = config.get("data.dump");
            String date = config.get("data.date");
            transporter = new TDBTransporter(store, probe, enum_, dump, date);
            transporter.create();
            log("createTransporter " + crawl);
        } else {
            log("createTransporter failed");
        }
    }

    protected void createStorage(String store) {
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
            String directory = config.get(store+".directory");
            String transformer = config.get(store+".transformer");
            storage = new FileStorage(directory,transformer);
            storage.create();
            log("createStorage [" + store + "] [" + transformer + "]");
        } else {
            log("No storage! [" + store + "]");
        }
    }

    public void crawl(String[] directories) {
        //log("crawl ");
        if (directories.length<2)
            return;
        if (directories[1].startsWith("-")) {
            if (directories.length==3)
                make(directories[1], directories[2]);
            else make(directories[1]);
            return; //dont crawl !
        }

        int count=0;
        int dirCount=0;
        long start = System.currentTimeMillis();
        for (String dir: directories) {
            if (dir.startsWith("-"))
                continue;
            File file = new File(dir);
            if (file.isDirectory()) {
                count += crawler.crawl(dir);
                dirCount++;
            } else {
                String home = System.getProperty("user.home") + "/" + dir;
                if ( new File(home).isDirectory()) {
                    count += crawler.crawl(home);
                } else {//single file support
                    boolean b = crawler.update(dir);
                    if(b) count++;
                }
            }
        }
        long end = System.currentTimeMillis();
        if (dirCount>1)
        log("crawled " + dirCount + " directories in "
                       + ((double)Math.round(end - start)/1000) + " sec");
    }

    protected void make(String cmd) {
        //log("make " + cmd);
        if (cmd.startsWith("-test"))
            crawler.test();
        else if (cmd.startsWith("-probe"))
            crawler.probe();
        else if (cmd.startsWith("-destroy"))
            crawler.destroy();
    }

    protected void make(String cmd, String resource) {
        //log("make " + cmd + ": " +resource);
        if (cmd.startsWith("-dump"))
            crawler.dump(resource);
        else if (cmd.startsWith("-post"))
            crawler.post(resource);
        else if (cmd.startsWith("-del"))
            crawler.delete(resource);
        else if (cmd.startsWith("-test"))
            crawler.test(resource);
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
