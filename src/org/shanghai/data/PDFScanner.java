package org.shanghai.data;

import org.shanghai.data.FileTransporter.Delegate;
import org.shanghai.data.FileScanner;
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
  @title A PDF File Scanner 
  @date 2015-11-05
*/
public class PDFScanner extends FileScanner implements Delegate {

    private boolean skip = true;

    public PDFScanner() {}

    public PDFScanner(boolean skip) {
        this.skip = skip;
    }

    @Override
    public boolean canRead(String file) {
        String check = file.substring(0, file.lastIndexOf(".") + 1) + "rdf";
        if (skip && Files.isReadable(Paths.get(check))) {
            return false;
        } else {
            //log("check read [" + docbase + "] " + Paths.get(file));
            return Files.isReadable(Paths.get(file));
        }
    }
}
