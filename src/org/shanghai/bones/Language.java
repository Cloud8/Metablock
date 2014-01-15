package org.shanghai.bones;

import org.shanghai.crawl.MetaCrawl.Analyzer;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import java.util.logging.Logger;

/*
 * Language detection
 */
public class Language implements Analyzer {

    private static final String dct = DCTerms.getURI();
    private String iri;
    private Detector detector;
    private String directory = "dlib/languages";

    public Language(String iri) {
        this.iri = iri;
    }

    //TODO : load directory as resource stream
    //public Language(String iri, String directory) {
    //    this.iri = iri;
    //    this.directory = directory;
    //}

    @Override
    public Analyzer create() {
        try {
            DetectorFactory.loadProfile(directory);
        } catch(LangDetectException e) { log(e); }
        return this;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Model analyze(String name, Model model) {
        Property id = model.createProperty(dct,"identifier");
        ResIterator ri = model.listSubjectsWithProperty(id);
        if (ri==null||!ri.hasNext())
            return model;
        Resource rc = ri.nextResource();

        //log("language " + id);
        String term = "title";
        //Resource rc = mod.getResource(iri + id);
        //Resource rc = model.getResource(id);

        String text = Helper.readObject(rc, term, model);
        if (text==null) {
            log("bad statement " + id + ": " + term);
            return model;
        }
        
        try {
            detector = DetectorFactory.create();
            detector.append(text);
            String language = detector.detect();
            //log("detected language " + language + " for " + name);
            rc = Helper.addProperty(rc, model, "language", language); 
            //log(model);
        } catch(LangDetectException e) { log(e); }
        finally {
            return model;
        }
    }

    private static final Logger logger =
                         Logger.getLogger(Language.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        logger.info(e.toString());
    }

    private void log(Model mod) {
        mod.write(System.out, "RDF/XML");
    }

}
