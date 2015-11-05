package org.shanghai.data;

import org.shanghai.data.FileTransporter;
import org.shanghai.util.ModelUtil;

import org.apache.jena.rdfxml.xmlinput.JenaReader;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.vocabulary.DCTerms;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.util.logging.Logger;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop 
  @title A File Scanner to create a very basic Model 
  @date 2012-10-23
*/
public class FileScanner implements FileTransporter.Delegate {

    protected static final String iri = "http://localhost";
    protected String docbase; 

    public FileScanner() {
        this.docbase = System.getProperty("user.home");
    }

    @Override
    public FileTransporter.Delegate create() {
        return this;
    }

    @Override
    public void dispose() {
	}

    @Override
    public Resource read(String fname) {
        String uri = null;
        if (fname.startsWith(docbase)) {
            fname = fname.substring(docbase.length()+1);
        }
        if (fname.startsWith("Dropbox")) {
            uri = iri + "/" + fname;
        } else {
            uri = Paths.get(fname).toUri().toString();
        }
        //log("read [" + docbase + "] " + fname + " " + uri);
        Model model = ModelUtil.createModel();
        Resource rc = model.createResource(uri, DCTerms.BibliographicResource); 
        fname = fname.replace("/",":");
        fname = fname.startsWith(":") ? fname.substring(1):fname;
        rc.addProperty(DCTerms.identifier, fname);
        return rc;
    }

    @Override
    public boolean canRead(String file) {
        if (Files.isReadable(Paths.get(file))) {
            return true;
        } else {
            String check = file.substring(0, file.lastIndexOf(".") + 1) + "rdf";
            if (Files.isReadable(Paths.get(check))) {
                //log("exists: " + check);
                return false;
            } else {
                //log("check read [" + docbase + "] " + Paths.get(file));
                return Files.isReadable(Paths.get(file));
            }
        }
    }

    private static final Logger logger =
                         Logger.getLogger(FileScanner.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e ) {
        log(e.toString());
    }
}
