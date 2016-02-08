package org.shanghai.ojs;

import org.shanghai.data.FileTransporter;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;
import org.shanghai.util.ModelUtil;
import org.shanghai.ojs.URN;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;

import java.lang.Character;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @license http://www.apache.org/licenses/LICENSE-2.0
 * @author Goetz Hatop 
 * @title NLM File Scanner to tansform NLM to RDF
 * @date 2014-01-09
 * @abstract Reads a NLM file and makes RDF therefrom
 */
public class NLMScanner implements FileTransporter.Delegate {

    private XMLTransformer transformer;
    private URN urn;
    private int count;
    private static final Logger logger =
                         Logger.getLogger(NLMScanner.class.getName());

    public NLMScanner(String server, String xsltFile, String schema) {
        this.transformer = new XMLTransformer(FileUtil.read(xsltFile));
        this.transformer.setParameter("server", server + "/");
        urn = new URN(schema);
    }

    @Override
    public FileTransporter.Delegate create() {
        transformer.create();
        urn.create();
        count = 0;
        return this;
    }

    @Override
    public void dispose() {
        urn.dispose();
        transformer.dispose();
    }

    @Override
    public Resource read(String fname) {
        String xml = FileUtil.read(fname);
        Resource rc = transformer.transform(xml);
        return NLMScanner.analyze(rc, urn); // make identifiers
    }

    @Override
    public boolean canRead(String fname) {
	    if (fname.endsWith(".nlm")) {
             return true;
        }
        return false;
    }

    public static Resource analyze(Resource rc, URN urn) {
        if (rc==null) { 
            return rc;
        }
        StmtIterator si = rc.listProperties(DCTerms.isPartOf);
        while(si.hasNext()) {
            Resource obj = si.nextStatement().getResource();
            String name = obj.getPropertyResourceValue(RDF.type).getLocalName();
            if (name.startsWith("Journal")) {
                analyze(obj, urn);
            }
        }
        makeIdentifier(rc, urn);
        return rc;
    }

    private static void makeIdentifier(Resource rc, URN urn) {
        if (rc.hasProperty(DCTerms.identifier) ) {
            String id = rc.getProperty(DCTerms.identifier).getString();
            if (id==null || id.length()==0) {
                rc.removeAll(DCTerms.identifier);
                id = urn.getUrn(rc.getURI());
                rc.addProperty(DCTerms.identifier, id);
            }
        } else {
            String id = urn.getUrn(rc.getURI());
            if (id==null) {
                //logger.info("URN failed : " + rc.getURI());
                id = rc.getURI().substring(rc.getURI().indexOf(":")+1);
                id = id.startsWith("//")?id.substring(2):id;
                id = id.replaceAll("/","-");
                id = id.replaceAll("[^a-zA-Z0-9\\:\\.\\-]","");
                rc.addProperty(DCTerms.identifier, id);
            } else {
                rc.addProperty(DCTerms.identifier, id);
            }
        }
    }

    private static void log(String msg) {
        logger.info(msg);
    }

}
