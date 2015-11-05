package org.shanghai.oai;

import org.shanghai.data.FileTransporter;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.rdfxml.xmlinput.JenaReader;

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

    private static final String dct = "http://purl.org/dc/terms/";
    private String concept = dct + "BibliographicResource";

    private XMLTransformer transformer;
    private String directory;
    private int count;
    private static final Logger logger =
                         Logger.getLogger(NLMScanner.class.getName());

    public NLMScanner(String xsltFile) {
        this.transformer = new XMLTransformer(FileUtil.read(xsltFile));
    }

    @Override
    public FileTransporter.Delegate create() {
        transformer.create();
        directory = "";
        count = 0;
        return this;
    }

    @Override
    public void dispose() {
        transformer.dispose();
    }

    @Override
    public Resource read(String fname) {
        String xml = FileUtil.read(fname);
        return transformer.transform(xml);
    }

    @Override
    public boolean canRead(String fname) {
	    if (fname.endsWith(".nlm")) {
             return true;
        }
	    if (fname.endsWith(".xml")) {
            if (Character.isDigit(fname.charAt(0))) {
                //log("can read " + file.getPath());
                return true;
            } else if (fname.startsWith("ojs")) {
                return true;
            }
        }
        return false;
    }

    public void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception e ) {
        log(e.toString());
    }

}
