package org.seaview.main;

import org.seaview.pdf.PDFAnalyzer;
import org.seaview.pdf.PDFLoader;
import org.seaview.data.DOIAnalyzer;
import org.seaview.data.DataAnalyzer;
import org.seaview.data.OpusAnalyzer;
import org.seaview.data.FileScanner;
import org.seaview.data.VoidTransporter;
import org.seaview.data.ConsoleStorage;
import org.seaview.data.OpusTransporter;
import org.seaview.data.DBTransporter;
import org.seaview.cite.RefAnalyzer;
import org.seaview.cite.RefContext;

import org.shanghai.oai.NLMScanner;
import org.shanghai.rdf.Config;
import org.shanghai.crawl.TrivialScanner;
import org.shanghai.crawl.FileTransporter;
import org.shanghai.crawl.FileStorage;
import org.shanghai.crawl.RDFTransporter;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.solr.SolrTransporter;
import org.shanghai.util.Language;
import org.seaview.pdf.PDFScanner;

import java.util.Calendar;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Crawler with pdf Analyzer
    @date 2014-10-10
*/
public class Crawl extends org.shanghai.crawl.Crawl {

    private String directory;

    public Crawl(Config config) {
        super(config);
    }
    
    public void create(String src, String trg, String eng) {
        super.create(src, trg, eng);
        log("create " + src + " target " + trg + " " + eng);
    }
    
    @Override
    public void createCrawler() {
        super.createCrawler();
        if (engine==null) {
            return;
        }

        for(String eng: engine.split(" ")) { 
            if (eng.equals("pdf:none")) {
                crawler.inject(new PDFLoader().create());
            } else if (eng.contains("pdf")) {
                String extractor = config.get("pdf.extractor");
                String ghome = null;
                String base = config.get("opus.docbase");
                int x = eng.indexOf(":", eng.indexOf("pdf")) + 1;
                if (x>0) {
                    extractor = eng.substring(x);
                }
                if (extractor==null) {
                    extractor = "cermine";
                } else if (extractor.equals("grobid")) {
                    ghome = config.get("pdf.grobid");
                }
                log("source: " + source + " target: " + target 
                               + " eng [" + eng + "] ex:" + extractor);
                String s = config.get("pdf.metadata");
                boolean meta = s==null?true:s.equals("true");
                s = config.get("pdf.references");
                boolean refs = s==null?false:s.equals("true");
                crawler.inject(
                new PDFAnalyzer(extractor, meta, refs, ghome, base).create());
            }
            if (eng.contains("doi")) {
                String p = config.get("doi.prefix");
                String s = config.get("doi.store");
                String u = config.get("doi.user");
                String v = config.get("doi.pass");
                String x = config.get("doi.transformer");
                boolean quiet = config.getBoolean("doi.quiet");
                DOIAnalyzer doi = new DOIAnalyzer(p, s, u, v, x, quiet);
                doi.create();
                if (eng.equals("doi:opus")) {
                    s = config.get("opus.dbhost");
                    p = config.get("opus.dbase");
                    u = config.get("opus.dbuser");
                    v = config.get("opus.dbpass");
                    doi.createDb(s, p, u, v);
                } else if (eng.equals("doi:ojs")) {
                    s = config.get("ojs.dbhost");
                    p = config.get("ojs.dbase");
                    u = config.get("ojs.dbuser");
                    v = config.get("ojs.dbpass");
                    doi.createDb(s, p, u, v);
                } else if (eng.equals("doi:sub")) {
                    doi.submit();
                } else if (eng.equals("doi:reg")) {
                    doi.register();
                } else if (eng.equals("doi:del")) {
                    doi.delete();
                }
                crawler.inject(doi);
            }
            if (eng.contains("ref")) {
                crawler.inject(new RefAnalyzer().create());
            }
            if (eng.contains("ctx")) {
                String base = config.get("opus.docbase");
                crawler.inject(new RefContext(base).create());
            }
            if (eng.contains("data")) {
                crawler.inject(new DataAnalyzer().create());
                //String xslt = config.get("nlp.transformer");
                //crawler.inject(new DataAnalyzer(xslt).create());
            }
            if (eng.contains("opus") && !eng.contains("doi")) {
                String s = config.get("opus.dbhost");
                String p = config.get("opus.dbase");
                String u = config.get("opus.dbuser");
                String v = config.get("opus.dbpass");
                String up = config.get("urn.prefix");
                boolean w = config.getBoolean("opus.write");
                if (eng.equals("opus:none")) {
                    w = false;
                }
                crawler.inject(new OpusAnalyzer(s, p, u, v, up, w).create());
            }
        }
    }

    @Override
    public void createTransporter(String crawl) {
        //log("create transporter " + crawl);
        if (crawl.contains("files")) {
            source = "files"; // make parent understand
            String suffix = config.get("files.suffix");
            if (crawl.contains(":")) {
                suffix = "." + crawl.substring(crawl.indexOf(":")+1);
            }
            log("nlp file transporter for [" + suffix + "]");
            int depth = config.getInt("files.depth");
            int logC = config.getInt("files.count");
            fc = new FileTransporter(suffix,depth,logC);
            fc.create();
            if (suffix.contains(".java")||suffix.contains(".php")) {
                fc.inject(new FileScanner().create());
            }
            if (suffix.contains(".pdf")) {
                //if (suffix.contains(".rdf")) {
                //    fc.inject(new PDFScanner(true).create());
                //} else {
                    fc.inject(new PDFScanner().create());
                //}
            }
            if (suffix.contains(".rdf") || suffix.contains(".abd")) {
                fc.inject(new TrivialScanner().create());
            }
            if (suffix.contains(".nlm")) {
                String xslt = config.get("ojs.transporter");
                fc.inject(new NLMScanner(xslt).create());
                log("injected NLMScanner " + xslt);
            }
            transporter = fc;
        } else if (crawl.startsWith("solr")) {
		    String srv = config.get(crawl + ".url") 
                       + "/" + config.get(crawl+".core");
            log("sorl transporter " + srv);
            transporter = new SolrTransporter(srv);
			transporter.create();
        } else if (crawl.startsWith("void")) {
            transporter = new VoidTransporter();
			transporter.create();
        } else if (crawl.startsWith("opus")) {
            String s = config.get("opus.dbhost");
            String p = config.get("opus.dbase");
            String u = config.get("opus.dbuser");
            String v = config.get("opus.dbpass");
            String i = config.get("opus.enum");
            String d = config.get("opus.dump");
            String x = config.get("opus.transporter");
            int days = config.getInt("opus.days");
            if (crawl.equals("opus:test")) {
                i = days>=0?config.get("opus.enumd"):i;
                transporter = new OpusTransporter(s, p, u, v, i, d, x, null, days, true);
            } else if (crawl.equals("opus:dump")) {
                String b = config.get("opus.docbase");
                i = days>=0?config.get("opus.enumd"):i;
                transporter = new OpusTransporter(s, p, u, v, i, d, x, b, days, true);
            } else {
                String si = config.get("opus.series");
                String sd = config.get("opus.seriesd");
                transporter = new OpusTransporter(s, p, u, v, i, d, si, sd, x);
            }
            transporter.create();
        } else {
            super.createTransporter(crawl);
        }
    }

    @Override
    public MetaCrawl.Storage createStorage(String store) {
        if (store.equals("console")) {
            storage = new ConsoleStorage();
            storage.create();
        } else {
            storage = super.createStorage(store);
        }
        return storage;
    }

    private static final Logger logger =
                         Logger.getLogger(Crawl.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

}

