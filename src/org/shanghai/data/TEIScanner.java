package org.shanghai.data;

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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @license http://www.apache.org/licenses/LICENSE-2.0
 * @author Goetz Hatop 
 * @title TEI File Scanner to tansform TEI to RDF
 * @date 2014-01-09
 * @abstract Reads a TEI file and calls xslt transformer to produce RDF
 */
public class TEIScanner implements FileTransporter.Delegate {

    private static final String dct = "http://purl.org/dc/terms/";
    private String concept = dct + "BibliographicResource";

    private XMLTransformer transformer;
    private String directory;
    private String base; // view base
    private int count;
    private static final Logger logger =
                         Logger.getLogger(TEIScanner.class.getName());

    public TEIScanner(String base, String xsltFile) {
        this.base = base;
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

    //@Override
    //public void setDirectory(String d) {
    //    this.directory = d;
    //}

    @Override
    public Model read(String id, String fname) {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("dct", dct);
        String tei = FileUtil.read(new File(fname));
        String rdf = transformer.transform(tei);
        RDFReader reader = new JenaReader();
        reader.read(model, new StringReader(rdf), null);        
        //String about = base + fname;
        //String path = base + new File(fname).getAbsolutePath();
        //Resource rcCon = model.createResource(concept);
        //Resource rc = model.createResource(about, rcCon);
        //String id = fname.substring(directory.length()+1).replace("/",":");
	    //rc.addProperty(model.createProperty(dct,"identifier"), id); 
	    //rc.addProperty(model.createProperty(dct,"relation"), path); 
        //try {
        //} catch(FileNotFoundException e) { log(e); }
        //  catch(IOException e) { log(e); }
        //finally {
            return model;
        //}
    }

    @Override
    public boolean canRead(File file) {
	    if (file.getName().endsWith(".tei")) {
             return true;
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
