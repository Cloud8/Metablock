package org.seaview.pdf;

import org.shanghai.crawl.FileTransporter;
import org.shanghai.util.PrefixModel;

import com.hp.hpl.jena.rdf.arp.JenaReader;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.vocabulary.DCTerms;

import java.util.logging.Logger;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop 
  @title A PDF Document Scanner
  @date 2012-10-23
*/
public class PDFScanner implements FileTransporter.Delegate {

    static final String foaf = "http://xmlns.com/foaf/0.1/";
    static final String dct = DCTerms.getURI();
    static final String ore = "http://www.openarchives.org/ore/terms/";

    private static final String concept = dct + "BibliographicResource";
    private String iri; // about
    private String path; // relation
    //private boolean stop4RDF;

    public PDFScanner() {
        this.iri = "http://localhost";
        this.path = System.getProperty("user.home");
    }

    //public PDFScanner(boolean stop4RDF) {
    //    this();
    //    this.stop4RDF = stop4RDF;
    //}

    @Override
    public FileTransporter.Delegate create() {
        return this;
    }

    @Override
    public void dispose() {
	}

    @Override
    public Model read(String fname) {
        Model mod = PrefixModel.create();
        String id = fname;
        if (id.startsWith(path)) {
            fname = fname.substring(path.length()+1);
            id = id.substring(path.length()+1).replace("/",":");
            //log("read [" + path + "] " + fname);
        } else {
            id = id.replace("/",":");
            if (id.startsWith(":")) {
                id = id.substring(1);
            }
        }

        mod.setNsPrefix("dct", dct);
        mod.setNsPrefix("foaf", foaf);
        mod.setNsPrefix("ore", ore);
        Resource rcC = mod.createResource(concept);
        Resource rc = mod.createResource(iri + "/" + fname, rcC);
        //try {
            rc.addProperty(mod.createProperty(dct, "identifier"), id);
            rc.addProperty(mod.createProperty(ore, "aggregates"), 
                           mod.createResource(iri + "/" + fname));
        //} //catch (FileNotFoundException e) { log(e); }
        //  catch (Exception e) { log(e); }
        //finally {
           return mod;
        //}
    }

    @Override
    public boolean canRead(String file) {
        //log("canRead " + file.getName());
        //if (stop4RDF) {
        //    return false;
        //}
        if (file.endsWith(".pdf")) {
            return true;
        }
        return false;
    }

    private static final Logger logger =
                         Logger.getLogger(PDFScanner.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e ) {
        log(e.toString());
    }
}
