package org.shanghai.util;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.DCTypes;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.sparql.vocabulary.FOAF;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

import java.util.logging.Logger;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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
        model.setNsPrefix("rdf", RDF.uri);
        model.setNsPrefix("dcterms", DCTerms.NS);
        model.setNsPrefix("dctypes", DCTypes.NS);
        model.setNsPrefix("skos", SKOS.uri);
        model.setNsPrefix("foaf", FOAF.NS);
        model.setNsPrefix("aiiso", "http://purl.org/vocab/aiiso/schema#");
        model.setNsPrefix("void", "http://rdfs.org/ns/void#");
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
                remove(stmt.getResource());
            }
        }
        rc.removeAll(prop);
    }

    private static void remove(Resource rc) {
        StmtIterator si = rc.listProperties();
        while (si.hasNext()) {
            Statement stmt = si.nextStatement();
            if (stmt.getObject().isResource()) {
                remove(stmt.getResource());
            }
        }
        rc.removeProperties();
    }

    public static String getIdentifier(Resource rc, String prefix) {
        String id = null;
        StmtIterator si = rc.listProperties(DCTerms.identifier);
        while (si.hasNext()) {
            String literal = si.nextStatement().getString();
            if (literal.startsWith(prefix) && prefix.equals("urn:")) {
                id = literal;
                break;
            } else if (literal.startsWith(prefix)) {
                id = literal.substring(prefix.length());;
                break;
            }
        }
        return id;
    }

    public static String asString(Resource rc) {
        StringWriter sw = new StringWriter();
        Model model = ModelUtil.prefix(rc.getModel());
        try {
            model.write(sw, "RDF/XML-ABBREV");
        } catch(Exception e) {
            model.write(System.out,"RDF/XML-ABBREV");
            e.printStackTrace();
        } finally {
            return sw.toString();
        }
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
