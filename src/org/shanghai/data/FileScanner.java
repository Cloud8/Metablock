package org.shanghai.data;

import org.shanghai.crawl.FileTransporter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @license http://www.apache.org/licenses/LICENSE-2.0
 * @author Goetz Hatop <fb.com/goetz.hatop>
 * @title Experimental File Scanner which can deliver some Metadata. 
 * @date 2012-10-11
 * @abstract reads a (preferable php or java) code file and tries
 *           to guess some metadata like author and title.
 */
public class FileScanner implements FileTransporter.Delegate {

    private static final String dct = "http://purl.org/dc/terms/";
    private String concept = dct + "BibliographicResource";
    //private String directory;
    private String base; // view base
    private int count;
    private static final Logger logger =
                         Logger.getLogger(FileScanner.class.getName());

    public FileScanner(String b) {
        this.base = b;
    }

    public void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception e ) {
        log(e.toString());
    }

    @Override
    public FileScanner create() {
        //directory = "";
        log("base " + base);
        count = 0;
        return this;
    }

    @Override
    public void dispose() {
    }

    //@Override
    //public void setDirectory(String d) {
    //    this.directory = d;
    //}

    @Override
    public Model read(String id, String fname) {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("dct", dct);
        String path = base + new File(fname).getAbsolutePath();
        Resource rcCon = model.createResource(concept);
        Resource rc = model.createResource(base+id, rcCon);
        //String id = fname.substring(directory.length()+1).replace("/",":");
	    rc.addProperty(model.createProperty(dct,"identifier"), id); 
	    rc.addProperty(model.createProperty(dct,"relation"), path); 
        try {
		    this.scanFile(id, fname, rc, model);
        } catch(FileNotFoundException e) { log(e); }
          catch(IOException e) { log(e); }
        finally {
            return model;
        }
    }

    @Override
    public boolean canRead(File file) {
	    if (file.getName().endsWith(".php")) {
             return true;
        } else if (file.getName().endsWith(".java")) {
             return true;
        }
        return false;
    }

    private int lineCount;
    private void scanFile(String id, String fname, Resource rc, Model mod) 
              throws FileNotFoundException, IOException
    {
	    StringBuilder fulltextBuf;
        BufferedReader reader;
        File file = new File(fname);
    
		//RDA Content Type 
	    rc.addProperty(mod.createProperty(dct,"format"), "Computer File"); 
        String language = fname.lastIndexOf(".")>0 ?
                          fname.substring(fname.lastIndexOf(".")+1) : "Polish";
	    rc.addProperty(mod.createProperty(dct,"language"), language); 

        //String title = fname.substring(directory.length()+1).replace("/",":");
        String title = id;
	    rc.addProperty(mod.createProperty(dct,"title"), title); 

        lineCount = 0;
        reader = new BufferedReader(new InputStreamReader(
                 new FileInputStream(file), "UTF-8"));
        fulltextBuf = new StringBuilder();
        String line = reader.readLine();
        while(line != null) {
			lineCount++;
            scanTag(line, rc, mod);
			fulltextBuf.append(line);
			fulltextBuf.append("\n");
            line = reader.readLine();
        }
	    rc.addProperty(mod.createProperty(dct,"extend"), 
                                              ""+lineCount + " lines"); 
	    rc.addProperty(mod.createProperty(dct,"fulltext"), 
                                                 fulltextBuf.toString());
	}

    /** look for some tags. */
    private void scanTag(String line, Resource rc, Model mod) {
	    int found = -1;
	    int x = -1;
	    if ( (found=line.indexOf("@author"))>=0 ) {
            String author = null;
            //if ( (x=line.indexOf("<"))>0 ) {
		    //    author = line.substring(found+7, x).trim();
		    //} else 
            author = line.substring(found+7).trim();
            author = author.replaceAll("<.*?>","");
            String[] str = author.split(" ");
            author = "";
            for (String s : str) {
                if (!s.contains("@"))
                    author += s + " ";
            } 
            author = author.trim();
            if (author.length()>0)
	        rc.addProperty(mod.createProperty(dct,"creator"), author); 
        } else if ( (found=line.indexOf("@package")) >=0 ) {
		    String provenance = line.substring(found+8).trim();
	        rc.addProperty(mod.createProperty(dct,"provenance"), provenance); 
	    } else if ( (found=line.indexOf("Copyright (C)")) >=0 ) {
		    String publisher = line.substring(found+14).trim();
	        rc.addProperty(mod.createProperty(dct,"publisher"), publisher); 
	    } else if ( (found=line.indexOf("@title")) >=0 ) {
		    String title = line.substring(found+6).trim();
            Property prop = mod.createProperty(dct,"title");
            rc.removeAll(prop);
	        rc.addProperty(prop, title); 
	    } else if ( (found=line.indexOf("@abstract")) >=0 ) {
		    String abstract_ = line.substring(found+9).trim();
	        rc.addProperty(mod.createProperty(dct,"abstract"), abstract_); 
	    } else { 
             //if (lineCount<5 && (found=line.indexOf(" * ")) >=0) {
		     //if (line.contains("VuFind"))
		     //    b.title = line.substring(found+3).trim();
	         //}
	    }
    }
}
