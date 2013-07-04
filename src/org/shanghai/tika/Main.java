package org.shanghai.tika;

import org.shanghai.solr.SolrTransporter;
import org.shanghai.crawl.Crawl;
import org.shanghai.crawl.FileCrawl;
import org.shanghai.crawl.TDBTransporter;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title The Tika File Crawler
   @date 2013-02-23
   @abstract This time, more Scanners are injected to the Transporter.
*/
public class Main extends org.shanghai.crawl.Main {

    Crawl crawl;
    FileCrawl.Transporter transporter;

    @Override
    public Crawl getCrawl(Properties prop) {
        super.prop = prop;
        if (crawl==null) {
            //transporter = new TDBTransporter(
            // prop.getProperty("store.tdb"), prop.getProperty("store.graph"));
            transporter = new SolrTransporter(
                              prop.getProperty("index.solr"),
                              prop.getProperty("crawl.base"));
            String cache = prop.getProperty("crawl.cache");
            String base = prop.getProperty("crawl.base");
            transporter.create();
            transporter.addScanner(new PDFScanner(base,cache).create());
            crawl = new Crawl(transporter,prop);
            crawl.create();
        }
        return crawl;
    }

    public void dispose() {
        if (crawl!=null)
            crawl.dispose();
        if (transporter!=null)
            transporter.dispose();
    }

    public static void main(String[] args) {
	    Main main = new Main();
        Logger.getLogger("org.apache.pdfbox.util.PDFStreamEngine")
              .setLevel(Level.OFF);
        Logger.getLogger("org.apache.pdfbox.encoding.Encoding")
              .setLevel(Level. OFF);
        Logger.getLogger("org.apache.pdfbox.util.PDFStreamEngine")
              .setLevel(Level. OFF);
        Logger.getLogger("org.apache.pdfbox.pdmodel.font.PDTrueTypeFont")
              .setLevel(Level. OFF);
		main.make(args);
		main.dispose();
    }

}

