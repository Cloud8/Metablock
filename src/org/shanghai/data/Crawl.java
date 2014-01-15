package org.shanghai.data;

import org.shanghai.rdf.Config;
//import org.shanghai.pdf.PDFScanner;
//import org.shanghai.pdf.Cermine;
//import org.shanghai.pdf.Grobid;
import org.shanghai.bones.Language;
//import org.shanghai.epub.EpubScanner;
import org.shanghai.data.FileScanner;
import org.shanghai.crawl.TrivialScanner;
import org.shanghai.crawl.FileTransporter;
import org.shanghai.crawl.MetaCrawl;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Data Crawler with lots of Scanners
    @date 2013-02-23
*/
public class Crawl extends org.shanghai.crawl.Crawl {

    String iri;

    public Crawl(Config config) {
        super(config);
    }
    
    @Override
    public void create() {
        super.create();
        iri = config.get("crawl.iri");
    }
    
    @Override
    public void createCrawler() {
        super.createCrawler();
        //createTransporter(config.get("crawl.source"));
        //createStorage(config.get("crawl.target"));
        //int logC = config.getInt("crawl.count");
        //String test = config.get("crawl.test");
        //if (storage==null) {//test case
        //    log("No storage!");
        //    crawler = new MetaCrawl(transporter);
        //}
        //else crawler = new MetaCrawl(transporter,storage,test,logC);
        //crawler.create();
        //silence();
        String nlp = config.get("crawl.nlp");
        if (nlp!=null) {
        //    if (nlp.contains("cermine"))
        //        crawler.inject(new Cermine(iri).create());
        //    if (nlp.contains("grobid"))
        //        crawler.inject(new Grobid(iri).create());
            if (nlp.contains("lang"))
                crawler.inject(new Language(iri).create());
        }
    }

    @Override
    public void createTransporter(String crawl) {
        if ("files".equals(crawl)) {
            String suffix = config.get("files.suffix");
            log("data file transporter for [" + suffix + "]");
            int depth = config.getInt("files.depth");
            int logC = config.getInt("files.count");
            fc = new FileTransporter(suffix,depth,logC);
            fc.create();
            String cache = config.get("files.cache");
            String base = config.get("files.base");
            if (suffix.contains(".java")||suffix.contains(".php")) {
                fc.inject(new FileScanner(base).create());
            }
            //if (suffix.contains(".pdf")) {
            //    fc.inject(new PDFScanner(base,cache).create());
            //}
            //if (suffix.contains(".epub")) {
            //    base = config.get("files.book");
            //    fc.inject(new EpubScanner(base,cache).create());
            //}
            if (suffix.contains(".rdf")) {
                fc.inject(new TrivialScanner().create());
            }
            if (suffix.contains(".csv")) {
                fc.inject(new CSVScanner().create());
            }
            if (suffix.contains(".tei")) {
                String xslt = config.get("files.tei");
                fc.inject(new TEIScanner(base,xslt).create());
            }
            transporter = fc;
        } else if ("mysql".equals(crawl)) {
            String h = config.get("mysql.server");
            String d = config.get("mysql.database");
            String u = config.get("mysql.user");
            String p = config.get("mysql.pass");
            String i = config.get("mysql.enum");
            String a = config.get("mysql.dump");
            transporter = new DBTransporter(h,d,u,p,i,a,iri);//...
            transporter.create();
            log("createTransporter " + crawl);
        } else if (crawl.startsWith("solr")) {
            String solr=config.get(crawl+".url")+"/"+config.get(crawl+".core");
            String xslt = config.get(crawl+".transporter");
            if (null==xslt) {
                log("createTransporter " + solr + " " + iri);
                transporter = new SolrTransporter(solr, iri);
            } else {
                log("createTransporter " + solr + " " + xslt);
                transporter = new SolrTransporter(solr, iri, xslt);
            }
            transporter.create();
        } else if ("web".equals(crawl)) {
            //TODO: this is now in Engine.java 
            String site = config.get("web.crawl");
            String cache = config.get("web.cache");
            int robots = config.getInt("web.robots");
            transporter = new WebCrawl(cache,robots,storage,iri);
            transporter.create();
        } else {
            super.createTransporter(crawl);
        }
    }

    private void silence() {
        Logger.getLogger("org.apache.pdfbox.util.PDFStreamEngine")
              .setLevel(Level.OFF);
        Logger.getLogger("org.apache.pdfbox.encoding.Encoding")
              .setLevel(Level. OFF);
        Logger.getLogger("org.apache.pdfbox.util.PDFStreamEngine")
              .setLevel(Level. OFF);
        Logger.getLogger("org.apache.pdfbox.pdmodel.font.PDTrueTypeFont")
              .setLevel(Level. OFF);
        Logger.getLogger("org.apache.pdfbox.util.PDFStreamEngine")
              .setLevel(Level. OFF);
    }

    private static final Logger logger =
                         Logger.getLogger(Crawl.class.getName());

}

