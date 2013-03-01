package org.shanghai.crawl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title The Main File Crawler
   @date 2013-02-23
   @abstract Instruments the FileCrawl Class with a FileTransporter
             and starts it according to command line settings.
             FileCrawl : FileTransporter : TDBTransporter : TDBReader
*/
public class Crawl {

    Properties prop;
    FileCrawl crawler;
    FileCrawl.Transporter transporter;

    private static final Logger logger =
                         Logger.getLogger(Crawl.class.getName());

    public Crawl(Properties prop) {
        this.prop = prop;
        transporter = new TDBTransporter(prop.getProperty("jena.tdb"),
                                         prop.getProperty("jena.graph"));
        crawler = new FileCrawl(transporter, prop.getProperty("crawl.depth"));
        if (prop.getProperty("crawl.create")!=null
            && prop.getProperty("crawl.create").equals("true"))
        crawler.create=true;
        if (prop.getProperty("crawl.count")!=null) {
            crawler.logC = Integer.parseInt(prop.getProperty("crawl.count"));
        }
        if (prop.getProperty("crawl.suffix")!=null) {
            crawler.suffix = prop.getProperty("crawl.suffix");
        }
    }

    static void log(String msg) {
        //logger.info(msg);
        System.out.println(msg);
    }

    static void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    public Crawl create() {
        crawler.create();
        return this;
    }

    public void dispose() {
        crawler.dispose();
    }

    public void clean() {
        crawler.clean();
    }

    public void test() {
        prop.list(System.out);
    }

    public void crawl(String[] dirs) {
        long start = System.currentTimeMillis();
        for (String dir: dirs) {
            File f = new File(dir);
            if (f.isDirectory()) {
                crawler.crawl(dir);
            }
        }
        long end = System.currentTimeMillis();
        log("crawled " + crawler.count + " records in "
                       + ((end - start)/1000) + " sec");
    }

    public void delete(String uri) {
        crawler.delete(uri);
    }

    public void add(String file) {
        crawler.add(new File(file));
    }

    /** update from a single file */
    public void update(String file) {
        crawler.update(new File(file));
    }

    public void read(String resource) {
        String about = crawler.read(resource);
        String testFile = prop.getProperty("test.file");
        if (testFile!=null) {
            try {
                FileUtil.writeFile(testFile, about);
            } catch(IOException e) { log(e); }
        } else {
            System.out.println(about);
        }
    }

}
