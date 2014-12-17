package org.seaview.nlp;

import org.shanghai.crawl.MetaCrawl.Analyzer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;

import java.util.logging.Logger;

/*
 * (Really simple-dumb) Sentiment analysis
 *
 */
public class AbstractAnalyzer implements Analyzer {

    private static final String dct = DCTerms.getURI();

    @Override
    public Analyzer create() {
        return this;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Resource analyze(Model model, String id) {
        //log("analyze " + " [" + id + "]");
        Resource rc = findResource(model, id);
        //log("analyze " + rc.getURI());
        analyze(model, rc, id);
        return rc;
    }

    public void analyze(Model model, Resource rc, String id) {
        log("abstract analyze " + rc.getURI() + " [" + id + "]");
    }

    private Resource findResource(Model model, String id) {
        Resource rc = null;
        if (id.startsWith("http://")) {
            rc = model.getResource(id);
        } else {
            ResIterator ri = model.listSubjectsWithProperty(
                     model.createProperty(DCTerms.getURI(), "relation"));
            if (ri.hasNext()) {
                rc = ri.nextResource();
            } else {
                ri = model.listSubjectsWithProperty(
                     model.createProperty(DCTerms.getURI(), "identifier"));
                if (ri.hasNext()) {
                    rc = ri.nextResource();
                } else {
                    log("no resource for " + id);
                    //ri = model.listSubjects();
                    //if (ri.hasNext()) {
                    //    rc = ri.nextResource();
                    //}
                }
            }
        }
        return rc;
    }

    private static final Logger logger =
                         Logger.getLogger(AbstractAnalyzer.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        logger.info(e.toString());
    }

    protected void log(Model mod) {
        mod.write(System.out, "RDF/XML");
    }

}
