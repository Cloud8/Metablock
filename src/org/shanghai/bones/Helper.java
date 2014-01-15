package org.shanghai.bones;

import org.shanghai.crawl.MetaCrawl.Analyzer;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;

import java.util.logging.Logger;

/*
 * Helper class
 */
public class Helper {

    private static final String dct = DCTerms.getURI();

    public static String readObject(Resource rc, String term, Model mod) {
        Property prop = mod.getProperty(dct, term);
        Statement stmt = mod.getProperty(rc,prop);
        if (stmt!=null && stmt.getObject().isLiteral()) {
            return stmt.getObject().asLiteral().getString();
        }
        return null;
    }

    /* add or replace property value */
    public static Resource addProperty(Resource rc, Model mod, 
                                  String term, String val) {
       if (val==null) {
           System.out.println("zero " + term);
           return rc;
       }
       try {
           //mod.begin();
           Property prop = mod.createProperty(dct,term);
           if (rc.hasProperty(prop)) 
               rc.removeAll(prop);
           rc.addProperty(prop, val);
           //mod.commit();
       } catch(Exception e) { e.printStackTrace(); }
           return rc;
    }

    /* add property value */
    public static Resource add(Resource rc, Model mod, 
                                  String term, String val) {
       if (val==null) 
           return rc;
       try {
           Property prop = mod.createProperty(dct,term);
           rc.addProperty(prop, val);
       } finally {
           return rc;
       }
    }
}
