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
import java.nio.file.Paths;
import java.nio.file.Files;

/*
 * Language detection
 */
public class Language implements Analyzer {

    private static final String dct = DCTerms.getURI();
    private Detector detector;

    private Property language;
    private Property abstract_;
    private Property title;
    private boolean loaded = false;
    private String path;

    @Override
    public Analyzer create() {
        path = Language.class.getProtectionDomain()
                              .getCodeSource().getLocation().getPath();
        path = path.substring(0,path.lastIndexOf("/")+1) + "languages";
        if (!Files.isDirectory(Paths.get(path))) {
            path = path.substring(0,path.lastIndexOf("/"));
            path = path.substring(0,path.lastIndexOf("/")) 
                   + "/lib/languages";
        }
        if (loaded) {
            return this;
        }
        try {
            DetectorFactory.loadProfile(path);
            loaded = true;
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
            analyze(model, rc, id);
        }
        return rc;
    }

    @Override
    public boolean test() {
        log("test: " + this.getClass().getName() + " detect from " + path);
        return true;
    }

    @Override
    public Resource test(Model model, String id) {
        return analyze(model, id);
    }

    @Override
    public void dump(Model model, String id, String fname) {
        log("dump not implemented");
    }

    public Resource analyze(Model model, Resource rc, String id) {
        createProperties(model);
        if (rc.hasProperty(language)) {
            return rc;
        }
        String text = null;
        if (rc.hasProperty(abstract_)) {
            text = rc.getProperty(abstract_).getLiteral().getString();
        } else if (rc.hasProperty(title)) {
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
            //rc.removeAll(language);
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
        //e.printStackTrace();
    }

    private void log(Model mod) {
        mod.write(System.out, "RDF/XML");
    }

}
