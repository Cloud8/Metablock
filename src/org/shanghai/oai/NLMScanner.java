package org.shanghai.oai;

import org.shanghai.crawl.FileTransporter;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.util.FileUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.rdf.arp.JenaReader;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
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
 * @abstract Reads a NLM file and calls xslt transformer to produce RDF
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
    public Model read(String fname) {
        //log("read " + fname);
        Model model = ModelFactory.createDefaultModel();
        String xml = FileUtil.read(new File(fname));
        String rdf = transformer.transform(xml);
        RDFReader reader = new JenaReader();
        reader.read(model, new StringReader(rdf), null);        
        return model;
    }

    @Override
    public boolean canRead(File file) {
	    if (file.getName().endsWith(".nlm")) {
             return true;
        }
	    if (file.getName().endsWith(".xml")) {
            if (Character.isDigit(file.getName().charAt(0))) {
                //log("can read " + file.getPath());
                return true;
            } else if (file.getName().startsWith("ojs")) {
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
