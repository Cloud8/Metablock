package org.shanghai.jena;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;

import org.openjena.riot.RIOT;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
// import com.hp.hpl.jena.util.FileManager;

/**
   Don’t count on me
   I engineer
   On every move we make from here

   I'll take the lead
   You take the pain
   You see I engineer

   Don’t count on me
   I engineer

   (Bangbros – I Engineer)

   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A Simple Jena Funcionality Check
   @date 2013-01-16
*/
public class Test {

    Model model;

    private void read(String file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch(FileNotFoundException e) { 
            System.err.println("file not found: " + e.toString());
        }
        RIOT.init();
    
        model = ModelFactory.createDefaultModel();
        if (is != null) {
            // model.read(is, null, "N-TRIPLE");
            model.read(is, null, "RDF/XML");
        } else {
            System.err.println("cannot read input " + file);
        }
	}

    private void write(String file) {
        OutputStream os = null;
        if (file!=null) {
            try {
                os = new FileOutputStream(new File(file));
            } catch(java.io.FileNotFoundException e) {}
	    }

		if (os==null) 
            model.write(System.out, "TURTLE");
        else model.write(os, "TURTLE");
    }

    public static void main(String[] args) {

    	String outfile = null;
    	String infile = null;

        if (args.length>0) {
	        infile = args[0];
        } else {
	        infile = "data.rdf";
        }

        if (args.length>1) {
	        outfile = args[1];
        }

	    Test reader = new Test();
	    reader.read(infile);
		reader.write(outfile);
    }
}
