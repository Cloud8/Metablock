package org.shanghai.solr;

import org.shanghai.crawl.FileCrawl;
import org.shanghai.crawl.Crawl;
import java.util.Properties;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title The Main Solr File Crawler
   @date 2013-03-03
   @abstract Instruments the FileCrawl Class with a FileTransporter
             and starts it according to command line settings.
             FileCrawl : FileTransporter : SolrTransporter : SolrClient
*/
public class Main extends org.shanghai.crawl.Main {

    // Crawl crawl;
    @Override
    public Crawl getCrawl(Properties prop) {
        if (crawl==null) {
            FileCrawl.Transporter transporter
                  = new SolrTransporter(prop.getProperty("index.solr"),
                                        prop.getProperty("crawl.base"));
            // FileCrawl crawler = new FileCrawl(transporter, prop);
            crawl = new Crawl(transporter, prop);
            crawl.create();
        }
        return crawl;
    }

    public static void main(String[] args) {
	    Main main = new org.shanghai.solr.Main();
		main.make(args);
		main.dispose();
    }
}
