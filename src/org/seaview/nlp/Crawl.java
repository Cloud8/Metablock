package org.seaview.nlp;

import org.seaview.pdf.Cermine;
import org.shanghai.rdf.Config;
import org.shanghai.crawl.TrivialScanner;
import org.shanghai.crawl.FileTransporter;
import org.shanghai.crawl.FileStorage;
import org.shanghai.crawl.RDFTransporter;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.solr.SolrTransporter;
import org.shanghai.util.Language;
import org.seaview.data.FileScanner;
import org.seaview.pdf.PDFScanner;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Crawler with pdf Analyzer
    @date 2014-10-10
*/
public class Crawl extends org.shanghai.crawl.Crawl {

    protected int count;
    protected String engine;

    public Crawl(Config config) {
        super(config);
    }
    
    @Override
    public void create() {
        testFile = config.get("crawl.test");
        source = config.get("crawl.source");
        target = config.get("crawl.target");
        engine = config.get("crawl.engine");
    }
    
    public void create(String src, String trg, String eng) {
        testFile = config.get("crawl.test");
        if (src==null) {
            source = config.get("crawl.source");
        } else {
            source = src;
        }
        if (trg==null) {
            target = config.get("crawl.target");
        } else {
            target = trg;
        }
        if (eng==null) {
            engine = config.get("crawl.engine");
        } else {
            engine = eng;
        }
    }
    
    @Override
    public void createCrawler() {
        log("source: "+source+" target: "+ target +" engine ["+engine+"]");
        super.createCrawler();
        if (engine==null) {
            return;
        }
        if (engine.contains("cermine")) {
            log("source: "+source+" target: "+ target +" engine ["+engine+"]");
            crawler.inject(new Cermine(false, true).create());
        }
    }

    @Override
    public MetaCrawl.Storage createStorage(String store) {
        if (store.startsWith("files")) {
            String directory = config.get("files.store");
            if (engine!=null && engine.equals("opus")) {
                log("createStorage nlp / opus [" + store + "]");
                directory = config.get("opus.store");
            }
            storage = new FileStorage(directory);
            storage.create();
            return storage;
        }
        return super.createStorage(store);
    }

    @Override
    public void createTransporter(String crawl) {
        if ("files".equals(crawl)) {
            String suffix = config.get("files.suffix");
            log("nlp file transporter for [" + suffix + "]");
            int depth = config.getInt("files.depth");
            int logC = config.getInt("files.count");
            fc = new FileTransporter(suffix,depth,logC);
            fc.create();
            if (suffix.contains(".java")||suffix.contains(".php")) {
                fc.inject(new FileScanner().create());
            }
            if (suffix.contains(".pdf")) {
                if (suffix.contains(".rdf")) {
                    fc.inject(new PDFScanner(true).create());
                } else {
                    fc.inject(new PDFScanner().create());
                }
            }
            if (suffix.contains(".rdf") || suffix.contains(".abd")) {
                fc.inject(new TrivialScanner().create());
            }
            transporter = fc;
        } else {
            super.createTransporter(crawl);
        }
    }

    private static final Logger logger =
                         Logger.getLogger(Crawl.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

}

