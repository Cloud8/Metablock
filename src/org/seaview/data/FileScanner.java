package org.seaview.data;

import org.shanghai.crawl.FileTransporter;
import org.shanghai.util.PrefixModel;
import org.shanghai.util.FileUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.io.StringReader;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @license http://www.apache.org/licenses/LICENSE-2.0
 * @author Alfred Anders
 * @title Experimental File Scanner to guess some Metadata. 
 * @date 2012-10-11
 */
public class FileScanner implements FileTransporter.Delegate {

    //private static final String dct = "http://purl.org/dc/terms/";
    //private String concept = dct + "BibliographicResource";
    private int count;
    private static final Logger logger =
                         Logger.getLogger(FileScanner.class.getName());

    public void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception e ) {
        log(e.toString());
    }

    @Override
    public FileScanner create() {
        count = 0;
        return this;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Model read(String path) {
        String id = path.replace("/",":");
        Model model = PrefixModel.create();
        //String path = new File(fname).getAbsolutePath();
        Resource rcCon = model.createResource(DCTerms.BibliographicResource);
        Resource rc = model.createResource(id, rcCon);
	    rc.addProperty(DCTerms.identifier, id); 
	    rc.addProperty(DCTerms.relation, 
                       Paths.get(path).toAbsolutePath().toString()); 
        try {
		    this.scanFile(id, path, rc, model);
        } //catch(FileNotFoundException e) { log(e); }
          catch(IOException e) { log(e); }
        finally {
            return model;
        }
    }

    @Override
    public boolean canRead(String fname) {
	    if (fname.endsWith(".php")) {
             return true;
        } else if (fname.endsWith(".java")) {
             return true;
        }
        return false;
    }

    private int lineCount;
    private void scanFile(String id, String fname, Resource rc, Model mod) 
              throws IOException
    {
	    StringBuilder fulltextBuf;
        BufferedReader reader;
    
		//RDA Content Type 
	    rc.addProperty(DCTerms.format, "Computer File"); 
        String language = fname.lastIndexOf(".")>0 ?
                          fname.substring(fname.lastIndexOf(".")+1) : "Polish";
	    rc.addProperty(DCTerms.language, language); 

        String title = id;
	    rc.addProperty(DCTerms.title, title); 

        lineCount = 0;
		String content = FileUtil.read(fname);
		reader = new BufferedReader(new InputStreamReader(
		         new ByteArrayInputStream(content.getBytes())));
        fulltextBuf = new StringBuilder();
        String line = reader.readLine();
        while(line != null) {
			lineCount++;
            scanTag(line, rc, mod);
			fulltextBuf.append(line);
			fulltextBuf.append("\n");
            line = reader.readLine();
        }
	    rc.addProperty(DCTerms.extent, ""+lineCount + " lines"); 
	    rc.addProperty(mod.createProperty(DCTerms.NS,"fulltext"), 
                                                 fulltextBuf.toString());
	}

    /** look for some tags. */
    private void scanTag(String line, Resource rc, Model mod) {
	    int found = -1;
	    int x = -1;
	    if ( (found=line.indexOf("@author"))>=0 ) {
            String author = null;
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
	        rc.addProperty(DCTerms.creator, author); 
        } else if ( (found=line.indexOf("@package")) >=0 ) {
		    String provenance = line.substring(found+8).trim();
	        rc.addProperty(DCTerms.provenance, provenance); 
	    } else if ( (found=line.indexOf("Copyright (C)")) >=0 ) {
		    String publisher = line.substring(found+14).trim();
	        rc.addProperty(DCTerms.publisher,  publisher); 
	    } else if ( (found=line.indexOf("@title")) >=0 ) {
		    String title = line.substring(found+6).trim();
            rc.removeAll(DCTerms.title);
	        rc.addProperty(DCTerms.title, title); 
	    } else if ( (found=line.indexOf("@abstract")) >=0 ) {
		    String abstract_ = line.substring(found+9).trim();
	        rc.addProperty(DCTerms.abstract_, abstract_); 
	    }
    }
}
