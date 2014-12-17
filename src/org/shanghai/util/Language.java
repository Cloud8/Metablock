package org.shanghai.util;

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
import java.io.File;

/*
 * Language detection
 */
public class Language implements Analyzer {

    private static final String dct = DCTerms.getURI();
    private Detector detector;

    private Property language;
    private Property abstract_;
    private Property title;

    @Override
    public Analyzer create() {
        String path = Language.class.getProtectionDomain()
                              .getCodeSource().getLocation().getPath();
        path = path.substring(0,path.lastIndexOf("/")+1) + "languages";
        if (!new File(path).isDirectory()) {
            path = path.substring(0,path.lastIndexOf("/"));
            path = path.substring(0,path.lastIndexOf("/")) 
                   + "/lib/languages";
        }
        log("Language detect from " + path);
        try {
            DetectorFactory.loadProfile(path);
        } catch(LangDetectException e) { log(e); }
        return this;
    }

    @Override
    public void dispose() {
    }

    private void createProperties(Model model) {
        if (abstract_==null) {
            abstract_ = model.createProperty(dct, "abstract");
        }
        if (title==null) {
            title = model.createProperty(dct, "title");
        }
        if (language==null) {
            language = model.createProperty(dct, "language");
        }
    }

    @Override
    public Resource analyze(Model model, String id) {
        Resource rc = null;
        createProperties(model);
        ResIterator ri = model.listResourcesWithProperty(title);
        if (ri.hasNext() ) {
            rc = model.listResourcesWithProperty(title).nextResource();
            analyze(model, rc);
        }
        return rc;
    }

    public Resource analyze(Model model, Resource rc) {
        createProperties(model);
        String text = null;
        if (rc.hasProperty(abstract_)) {
            text = rc.getProperty(abstract_).getLiteral().getString();
        } else {
            text = rc.getProperty(title).getLiteral().getString();
        }
        analyzeText(model, rc, text);
        return rc;
    }

    private void analyzeText(Model model, Resource rc, String text) {
        String lang = null;
        try {
            detector = DetectorFactory.create();
            detector.append(text);
            lang = detector.detect();
            //log("language for " + rc.getURI() + " " + lang);
            rc.removeAll(language);
            if (lang==null) {
                log("no language for " + rc.getURI());
                return ;
            }
            if (lang.equals("de") || lang.equals("en") || lang.equals("fr")) {
                rc.addProperty(language, lang);
            } else {
                log("detected language " + lang + " " + rc.getURI());
                rc.addProperty(language, "de");
            }
            //detector.dispose();
        } catch(LangDetectException e) { 
            lang = null;
            log(e + " : [" + rc.getURI() + "] " + text); 
        } finally {
            return ;
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
