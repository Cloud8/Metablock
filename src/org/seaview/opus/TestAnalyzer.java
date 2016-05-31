package org.seaview.opus;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.util.FileUtil;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.vocabulary.DCTerms;

import java.util.logging.Logger;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop
  @title (Really simple-dumb) Test analysis
  @date 2015-07-01
*/
public class TestAnalyzer implements MetaCrawl.Analyzer {

    private XMLTransformer transformer;

    public TestAnalyzer() {
    }

    public TestAnalyzer(String xsltFile) {
        String xslt = FileUtil.read(xsltFile);
        transformer = new XMLTransformer(xslt); 
    }

    @Override
    public MetaCrawl.Analyzer create() {
        if (transformer==null) {
            return this;
        }
        transformer.create();
        return this;
    }

    @Override
    public void dispose() {
        //log("data analyze disposed.");
        if (transformer!=null) {
            transformer.dispose();
            transformer = null;
        }
    }

    @Override
    public String probe() {
        return " " + this.getClass().getName();
    }

    @Override
    public Resource analyze(Resource rc) {
        if (rc==null) {
            log("data analyze with zero resource");
            return rc;
        }
        String test = transformer.transform(rc).trim();
        if (test.equals("")) {
        } else {
            log("test [" + test + "] # " + rc.getURI());
        }
        return rc;
    }

    @Override
    public Resource test(Resource rc) {
        log("test " + rc.getURI());
        analyze(rc);
        //rc.getModel().write(System.out, "RDF/JSON");
        return rc;
    }

    private static final Logger logger =
                         Logger.getLogger(TestAnalyzer.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        logger.info(e.toString());
    }

}
