package org.shanghai.crawl;

import org.shanghai.util.FileUtil;
import org.shanghai.bones.FileScanner;

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

    protected Properties prop;
    protected FileCrawl crawler;
    private String base;
    private FileCrawl.Transporter transporter;

    public Crawl(FileCrawl.Transporter transporter, Properties prop) {
        this.prop = prop;
        this.transporter = transporter;
        crawler = new FileCrawl(transporter, prop);
        base = prop.getProperty("crawl.base");
    }

    public Crawl(Properties prop) {
        this.prop = prop;
        transporter = new TDBTransporter(
               prop.getProperty("crawl.store"), 
               prop.getProperty("crawl.graph"));
        crawler = new FileCrawl(transporter, prop);
        base = prop.getProperty("crawl.base");
    }

    private static final Logger logger =
                         Logger.getLogger(Crawl.class.getName());

    static void log(String msg) {
        //logger.info(msg);
        System.out.println(msg);
    }

    static void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    public Crawl create() {
        transporter.create();
        transporter.addScanner(new FileScanner(base).create());
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
            if (dir.equals("-crawl"))
                continue;
            File f = new File(dir);
            if (f.isDirectory()) {
                crawler.crawl(dir);
            } else {
                String check = System.getProperty("user.home") + "/" + dir;
                if ( new File(check).isDirectory()) {
                    crawler.crawl(check);
                } else {
                    crawler.add(f);
                }
            }
        }
        long end = System.currentTimeMillis();
        log("crawled " + crawler.count + " records in "
                       + ((double)Math.round(end - start)/1000) + " sec");
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
            FileUtil.write(testFile, about);
        } else {
            System.out.println(about);
        }
    }

}
