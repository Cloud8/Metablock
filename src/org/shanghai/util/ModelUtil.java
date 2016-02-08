package org.shanghai.util;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.StmtIterator;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

import java.util.logging.Logger;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Create nicely prefixed Model and some other utilities
    @date 2015-05-12
*/
public final class ModelUtil {

    public static Model createModel() {
        Model model = ModelFactory.createDefaultModel();
        return prefix(model);
    }

    public static Model prefix(Model model) {
        if (model==null) return model;
        for(int i=0; i<9; i++) model.removeNsPrefix("ns"+i);
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("dcterms", "http://purl.org/dc/terms/");
        model.setNsPrefix("dctypes", "http://purl.org/dc/dcmitype/");
        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        model.setNsPrefix("fabio", "http://purl.org/spar/fabio/");
        model.setNsPrefix("aiiso", "http://purl.org/vocab/aiiso/schema#");
        model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        model.setNsPrefix("void", "http://rdfs.org/ns/void#");
        model.setNsPrefix("c4o", "http://purl.org/spar/c4o/");
        model.setNsPrefix("prism", "http://prismstandard.org/namespaces/basic/2.1/");
        model.setNsPrefix("bibo", "http://purl.org/ontology/bibo/");
        return model;
    }

    public static ByteArrayOutputStream getBaos(Resource rc) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4000);
        RDFWriter writer = rc.getModel().getWriter("RDF/XML-ABBREV");
        writer.write(rc.getModel(), baos, null);
        return baos;
    }

    public static boolean write(Path path, Resource rc) {
        boolean b = false;
        try {
            OutputStream os = Files.newOutputStream(path);
            RDFDataMgr.write(os, rc.getModel(), RDFLanguages.RDFXML) ;
            os.close();
            b = true;
        } catch(FileNotFoundException e) { log(e); }
          catch(IOException e) { 
		    e.printStackTrace();
            log(e); 
          }
        return b;
    }

    public static void write(String file, Model model) {
        try {
            OutputStream os = new FileOutputStream(file);
            RDFDataMgr.write(os, model, RDFLanguages.RDFXML) ;
            os.close();
        } catch(FileNotFoundException e) { log(e); }
          catch(IOException e) { 
		    e.printStackTrace();
            log(e); 
          }
    }

    public static Resource read(String file) {
        String rdf = FileUtil.read(file);
        int x = rdf.indexOf("rdf:about");
        int y = rdf.indexOf("\"", x+11);
        if (x<0 || y<0 || y<x) {
            return null;
        }
        String uri = rdf.substring(x+11, y);
        Model model = createModel();
        RDFDataMgr.read(model, new StringReader(rdf), 
                       (String)null, RDFLanguages.RDFXML);
        return model.getResource(uri);
    }

    public static void remove(Resource rc, Property prop) {
        StmtIterator si = rc.listProperties(prop);
        while (si.hasNext()) {
            Statement stmt = si.nextStatement();
            if (stmt.getObject().isResource()) {
                removeProperties(stmt.getResource());
            }
        }
        rc.removeAll(prop);
    }

    public static void removeProperties(Resource rc) {
        StmtIterator si = rc.listProperties();
        while (si.hasNext()) {
            Statement stmt = si.nextStatement();
            if (stmt.getObject().isResource()) {
                stmt.getResource().removeProperties();
            }
        }
        rc.removeProperties();
    }

    private static final Logger log = Logger.getLogger(ModelUtil.class.getName());

    private static void log(String msg) {
        log.info(msg);
    }

    private static void log(Exception e) {
        //e.printStackTrace();
        log(e.toString());
    }

}
