package org.shanghai.data;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.crawl.SolrStorage;
import org.shanghai.crawl.FileStorage;
import org.shanghai.bones.Summarizer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.DCTerms;

import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.crawler.Page;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import de.l3s.boilerpipe.extractors.DefaultExtractor;
import de.l3s.boilerpipe.BoilerpipeProcessingException;

import java.util.regex.Pattern;
import java.util.List;
import java.util.logging.Logger;

public class WebCrawl extends WebCrawler implements MetaCrawl.Transporter {

    //needs to be static because controller instatiates its own class
    private static String site;
    private static MetaCrawl.Storage storage;

    private static String cache;
    private static int robots;
    private static String iri;
    private static MetaCrawl.Analyzer summarizer;

    private int count = 0;

    public WebCrawl() {
    }

    public WebCrawl(String cache, int robots, MetaCrawl.Storage storage,
                    String iri) {
        this.cache = cache;
        this.robots = robots;
        this.storage = storage;
        this.iri = iri;
    }

    @Override
    public void create() {
        log("created.");
        storage.create();
        summarizer = new Summarizer(iri).create();
    }

    @Override
    public void dispose() {
        log("disposed.");
        storage.dispose();
        summarizer.dispose();
    }

    @Override
    public String probe() {
        log("alive");
        return site;
    }

    @Override
    public Model read(String resource) {
        return null;
    }

    @Override
    public String getIdentifier(String id) {
        return id;
    }

    @Override
    public String[] getIdentifiers(int off, int limit) {
        return null;
    }

    @Override
    public int crawl(String site) {
        log("crawl site " + site);
        this.site = site;
        WebCrawlController wcc = new WebCrawlController(this,site,cache,robots);
        wcc.create();
        wcc.make();
        wcc.dispose();
        return 0;
    }

    //@Override
    //public boolean canRead(String resource) {
    //    return false;
    //}

    /** create model : oid url fulltext */
    private void save(String url, String title, String text) {
        log("url: " + url);
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("dct", DCTerms.getURI());
        model.setNsPrefix("nlp", iri);
        count++;
        int oid=count;

        try {
            Resource rc = model.createResource(iri + oid);
            String boiler = DefaultExtractor.INSTANCE.getText(text);
            //int x = text.indexOf('\n');
            //x=x>0?x:text.length();
            //String title = text.substring(0,Math.min(x,64));
            rc.addProperty( model.createProperty(DCTerms.getURI(), 
                            "identifier"), "web:" + oid);
            rc.addProperty(model.createProperty(iri, "fulltext"), text);
            rc.addProperty(model.createProperty(iri, "url"), url);
            rc.addProperty(model.createProperty(iri, "title"), title);
            rc.addProperty(model.createProperty(iri, "description"), boiler);
            model = summarizer.analyze(iri+oid, model);
            storage.write(model);
        } catch(BoilerpipeProcessingException e) { log(e); }
    }

    private static final Logger logger =
                         Logger.getLogger(WebCrawl.class.getName());

    protected static void log(String msg) {
        logger.info(msg);
    }

    protected static void log(Exception e) {
        log(e.toString());
    }

    private static void log(Model mod) {
        mod.write(System.out, "RDF/XML");
    }


    private final static Pattern FILTERS = Pattern.compile(
                  ".*(\\.(css|js|bmp|gif|jpe?g" 
                + "|png|tiff?|mid|mp2|mp3|mp4"
                + "|wav|avi|mov|mpeg|ram|m4v|pdf" 
                + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    /**
     * You should implement this function to specify whether
     * the given url should be crawled or not (based on your
     * crawling logic).
     */
    @Override
    public boolean shouldVisit(WebURL url) {
        String href = url.getURL().toLowerCase();
        //log("shouldVisit " + href);
        boolean b= !FILTERS.matcher(href).matches() && href.startsWith(site);
        if (b) {
            // log("shouldVisit " + href);
        } else {
             // String page = href.substring(href.lastIndexOf("/"));
             // if (page==null) 
             //     b=false;
             // else if (page.contains("."))
             //     b=false;
             // else 
             //     b=true;
             // if (b)
             //     log("see " + href);
             // else log("skip [" + page + "]");
        }
        return b;
    }

    /**
     * This function is called when a page is fetched and ready 
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {          
        String url = page.getWebURL().getURL();
        //System.out.println("URL: " + url);
        //log("URL: " + url);
        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String title = htmlParseData.getTitle();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            List<WebURL> links = htmlParseData.getOutgoingUrls();
            //System.out.println("Text length: " + text.length());
            //System.out.println("Html length: " + html.length());
            //System.out.println("Number of outgoing links: " + links.size());
            save(url, title, text);
            }
    }

}
