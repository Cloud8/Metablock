package org.shanghai.util;

import org.shanghai.crawl.MetaCrawl.Analyzer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.DCTerms;

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

    private Detector detector;
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

    @Override
    public Resource analyze(Resource rc, String id) {
        if (rc.hasProperty(DCTerms.language)) {
            return rc;
        }
        String text = null;
        if (rc.hasProperty(DCTerms.abstract_)) {
            text = rc.getProperty(DCTerms.abstract_).getLiteral().getString();
        } else if (rc.hasProperty(DCTerms.title)) {
            text = rc.getProperty(DCTerms.title).getLiteral().getString();
        }
        analyzeText(rc, text);
        return rc;
    }

    @Override
    public Resource test(Resource rc, String id) {
        return analyze(rc, id);
    }

    private void analyzeText(Resource rc, String text) {
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
                rc.addProperty(DCTerms.language, lang);
            } else {
                log("detected language " + lang + " " + rc.getURI());
                rc.addProperty(DCTerms.language, "de");
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

}
