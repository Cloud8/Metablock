package org.seaview.cite;

import org.shanghai.rdf.Config;
import org.seaview.pdf.PDFAnalyzer;
import org.seaview.pdf.PDFLoader;
import org.seaview.pdf.Cover;
import org.seaview.cite.RefAnalyzer;
import org.seaview.cite.RefContext;
import org.seaview.cite.RefSentiment;
import org.seaview.cite.RefSentiment.Classifier;
import org.seaview.cite.ClassifierZero;


/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @title Crawler with pdf Analyzer
  @date 2014-10-10
*/
public class Crawl extends org.shanghai.crawl.Crawl {

    protected boolean probe = false;

    public Crawl(Config config) {
        super(config);
    }
    
    public void create(String src, String trg, String eng) {
        super.create(src, trg, eng);
    }
    
    @Override
    public void createCrawler() {
        super.createCrawler();
        if (engine==null) {
            return;
        }

        for(String eng: engine.split(" ")) { 
            if (eng.equals("pdf:dump")) {
                crawler.inject(new PDFLoader(target).create());
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
                String s = config.get("pdf.metadata");
                boolean meta = s==null?true:s.equals("true");
                s = config.get("pdf.references");
                boolean refs = s==null?false:s.equals("true");
                crawler.inject(
                new PDFAnalyzer(extractor, meta, refs, ghome, base).create());
            }
            if (eng.contains("ref")) {
                crawler.inject(new RefAnalyzer().create());
            }
            if (eng.contains("ctx")) {
                crawler.inject(new RefContext().create());
            }
            if (eng.contains("sentiment")) {
                String model = config.get("sentiment.model");
                Classifier classifier = new ClassifierBayes(model);
                crawler.inject(new RefSentiment(classifier.create()));
            }
            if (eng.contains("cover")) {
                crawler.inject(new Cover(new PDFLoader()).create());
            }
        }
    }

    public void probe() {
        this.probe = true;
        createCrawler();
        log(crawler.probe());
    }

}

