package org.shanghai.data;

import org.shanghai.crawl.MetaCrawl;
import com.hp.hpl.jena.rdf.model.Model;

import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.crawler.Page;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import java.util.regex.Pattern;
import java.util.List;
import java.util.logging.Logger;

public class WebCrawlController {

	/*
	 * crawlStorageFolder is a folder where intermediate crawl data is
	 * stored.
	 */
	private String crawlStorageFolder = "/var/tmp";

	/*
	  * numberOfCrawlers shows the number of concurrent threads that should
	  * be initiated for crawling.
	 */
	private int numberOfCrawlers = 0;
	private WebCrawler webCrawler;
	private String site;
	private CrawlConfig config;
	private PageFetcher pageFetcher;
	private RobotstxtServer robotstxtServer;

	public WebCrawlController(WebCrawler webCrawler, String site,
		String crawlStorageFolder, int numberOfCrawlers) {
		this.webCrawler = webCrawler; 
		this.site = site; 
		this.crawlStorageFolder = crawlStorageFolder; 
		this.numberOfCrawlers = numberOfCrawlers;
	}

	public void dispose() {
	}

	public void create() {
		config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);

		/*
		 * Be polite: Make sure that we don't send more than 1 request per
		 * second (1000 milliseconds between requests).
		 */
		config.setPolitenessDelay(1000);

		 /*
		  * You can set the maximum crawl depth here. 
		  * The default value is -1 for unlimited depth
		  */
		 //config.setMaxDepthOfCrawling(7);

		 /*
		  * You can set the maximum number of pages to crawl. 
		  * The default value is -1 for unlimited number of pages
		  */
		 config.setMaxPagesToFetch(1000);

		 /*
		  * Do you need to set a proxy? If so, you can use:
		  * config.setProxyHost("proxyserver.example.com");
		  * config.setProxyPort(8080);
		  * If your proxy also needs authentication:
		  * config.setProxyUsername(username); 
		  * config.getProxyPassword(password);
		  * Isn't it proxy.setProxyPassword(password) ?
		  */

		  /*
		   * This config parameter can be used to set your crawl to be resumable
		   * (meaning that you can resume the crawl from a previously
		   * interrupted/crashed crawl). 
		   * Note: if you enable resuming feature and
		   * want to start a fresh crawl, you need to delete the contents of
		   * rootFolder manually.
		   */
		   config.setResumableCrawling(false);

		   pageFetcher = new PageFetcher(config);
		   RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		   robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		}

		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		public void make() {
		   /*
			* Instantiate the controller for this crawl.
			*/
		   try {
			   CrawlController controller = 
			   new CrawlController(config, pageFetcher, robotstxtServer);

			   /*
				* For each crawl, you need to add some seed urls. 
				* These are the first URLs that are fetched and then 
				* the crawler starts following links which are found 
				* in these pages
				*/

				//controller.addSeed("http://www.ics.uci.edu/");
				//controller.addSeed("http://www.ics.uci.edu/~lopes/");
				//controller.addSeed("http://www.ics.uci.edu/~welling/");
				controller.addSeed(site);
				//controller.start(WebCrawlImpl.class, numberOfCrawlers);
				controller.start(WebCrawl.class, numberOfCrawlers);
				//controller.start(webCrawler.getClass(), numberOfCrawlers);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private static final Logger logger =
					 Logger.getLogger(WebCrawl.class.getName());

		protected void log(String msg) {
			logger.info(msg);
		}

		private void log(Model mod) {
			mod.write(System.out, "RDF/XML");
		}

}
