package org.shanghai.bones;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.bones.LexSummary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;

import java.util.logging.Logger;

public class Summarizer implements MetaCrawl.Analyzer {

    private static final String dct = DCTerms.getURI();
    private LexSummary summarizer;
    private String iri;
    private int bad;

    public Summarizer(String iri) {
        this.iri = iri;
    }

    @Override
    public MetaCrawl.Analyzer create() {
        summarizer = new LexSummary();
        return this;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Model analyze(String resource, Model model) {
        Property id = model.createProperty(dct,"identifier");
        ResIterator ri = model.listSubjectsWithProperty(id);
        if (ri==null||!ri.hasNext())
            return model;
        Resource rc = ri.nextResource();
        //Resource rc = model.getResource(iri + resource);

        Property prop = model.getProperty(iri , "fulltext");
        Statement stmt = model.getProperty(rc,prop);
        if (stmt!=null && stmt.getObject().isLiteral()) {
            String fulltext = stmt.getObject().asLiteral().getString();
            String text = summarizer.summary(fulltext);
            //rc = Helper.addProperty(rc, model, "language", language); 
            Property summary = model.createProperty(iri,"summary");
            rc.removeAll(summary);
            rc.addProperty(summary, text);
            //String title = summarizer.title(text);
            //Property tit = model.createProperty(iri,"title");
            //res.removeAll(tit);
            //res.addProperty(tit,title);
            //model.removeAll(); //(r,p,null);
            //model.commit();
        } else {
            log("bad statement " + resource);
            bad++;
            if (bad>7) {
                log(model);
                System.exit(0);
            }
        }
        return model;
    }

    private static final Logger logger =
                         Logger.getLogger(Summarizer.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    private void log(Model mod) {
        mod.write(System.out, "RDF/XML");
    }

}
