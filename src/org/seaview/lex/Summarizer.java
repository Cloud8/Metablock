package org.seaview.lex;

import org.shanghai.crawl.MetaCrawl.Analyzer;
import org.shanghai.util.FileUtil;
import org.seaview.pdf.PDFLoader;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.DCTerms;

import java.util.logging.Logger;
import java.util.List;
import java.util.Arrays;

public class Summarizer implements Analyzer {

    private PDFLoader pl;
    private LexSummary summarizer;
    private int bad;

    public Summarizer(PDFLoader pl) {
        this.pl = pl;
    }

    @Override
    public Analyzer create() {
        return this;
    }

    @Override
    public void dispose() {
    }

    @Override
    public String probe() {
        return " " + this.getClass().getName();
    }

    @Override
    public Resource analyze(Resource rc) {
        if (rc.hasProperty(DCTerms.abstract_)) {
            //
        } else {
            pl.analyze(rc);
            String text = pl.fulltext(rc);
		    String[] lines = text.split(System.getProperty("line.separator"));
            int min = 0;
            int max = 120;
            if (max>lines.length) {
                max = lines.length-1;
            }
            if (max>min) {
                List<String> sub = Arrays.asList(lines).subList(min, max);
		        String summary = new LexSummary(sub).summary(sub.size()-1);
                //summary = summary.replaceAll("\\<.*?>",""); // remove tags
                log("summary " + rc.getURI());
                rc.addProperty(DCTerms.abstract_, summary);
            }
        }
        return rc;
    }

    @Override
    public Resource test(Resource rc) {
	    return analyze(rc);
    }

    private static final Logger logger =
                         Logger.getLogger(Summarizer.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

}
