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
        createStorage(config.get("crawl.store"));
        if (storage==null) {
            log("Bad storage : will break.");
            return;
        }
        createTransporter("crawl");
        String testFile = config.get("crawl.test");
        int logC = config.getInt("crawl.count");
        boolean create = config.getBoolean("crawl.create");
        crawler = new MetaCrawl(transporter,storage,testFile,logC,create);
        crawler.create();
    }

    protected void createTransporter(String crawl) {
        if ("crawl".equals(crawl)) {
            String suffix = config.get("crawl.suffix");
            int depth = config.getInt("crawl.depth");
            int logC = config.getInt("crawl.count");
            FileCrawl fc = new FileCrawl(suffix,depth,logC);
            fc.create(); 
            transporter = fc.addScanner(new TrivialScanner().create());
            //log("createTransporter " + crawl);
        } else {
            //log("createTransporter failed");
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
