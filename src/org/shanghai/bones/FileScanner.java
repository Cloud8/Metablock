package org.shanghai.bones;

import org.shanghai.bones.BiblioRecord;
import org.shanghai.bones.BiblioModel;
import org.shanghai.bones.RecordFactory;
import org.shanghai.crawl.TDBTransporter;

import com.hp.hpl.jena.rdf.model.Model;

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
 * @title Experimental File Scanner able to deliver some Metadata. 
 * @date 2012-10-11
 * @abstract reads a (preferable php or java) code file and tries
             to guess some metadata like author and title.
 */
public class FileScanner implements TDBTransporter.Scanner {

    private String directory;
    private static final Logger logger =
                         Logger.getLogger(FileScanner.class.getName());

    private BiblioModel bibModel;
    private int count;
    private String base;

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
        count = 0;
        bibModel = new BiblioModel(base);
        bibModel.create();
        return this;
    }

    @Override
    public void dispose() {
        bibModel.dispose();
    }

    @Override
    public void setStartDirectory(String d) {
        this.directory = d;
    }

    @Override
    public Model getModel(File file) {
        BiblioRecord b = RecordFactory.getRecord(directory, file);
        try {
		    this.scanFile(file, b);
        } catch(FileNotFoundException e) { log(e); }
          catch(IOException e) { log(e); }
        Model m = bibModel.getModel(b,
                  "http://purl.org/spar/fabio/ComputerProgram");
        //if (count++==3)
        //    m.write(new PrintWriter(System.out));
        return m;
    }

    @Override
    public boolean canTalk(File file) {
	    if (file.getName().endsWith(".php")) {
             return true;
        } else if (file.getName().endsWith(".java")) {
             return true;
        }
        return false;
    }

    private int lineCount;
    private void scanFile(File file, BiblioRecord b) 
              throws FileNotFoundException, IOException
    {
	    StringBuilder fulltextBuf;
        BufferedReader reader;
    
		//RDA Content Type 
	    b.setFormat("Computer File"); 
        b.setLanguage(b.id.lastIndexOf(".")>0 ?
                      b.id.substring(b.id.lastIndexOf(".")+1) : "Polish");
        //May be overridden below
        b.title = b.id.replace(":"," ");
        lineCount = 0;
        reader = new BufferedReader( new InputStreamReader(
                 new FileInputStream(file), "UTF-8"));
        fulltextBuf = new StringBuilder();
        String line = reader.readLine();
        while(line != null) {
			lineCount++;
            scanTag(line, b);
			fulltextBuf.append(line);
			fulltextBuf.append("\n");
            line = reader.readLine();
        }
		b.fulltext = fulltextBuf.toString();
	}

    /** look for some tags. */
    private void scanTag(String line, BiblioRecord b) {
	    int found = -1;
	    int x = -1;
	    if ( b.author==null && (found=line.indexOf("@author")) >=0 ) {
             if ( (x=line.indexOf("<"))>0 )
		         b.author = line.substring(found+7, x).trim();
		     else b.author = line.substring(found+7).trim();
        }
	    else if ( (found=line.indexOf("@package")) >=0 )
		     b.setInstitution(line.substring(found+8).trim());
	    else if ( (found=line.indexOf("Copyright (C)")) >=0 )
		     b.publisher = line.substring(found+14).trim();
	    if ( b.title==null && (found=line.indexOf("@title")) >=0 )
		     b.title = line.substring(found+6).trim();
	    // else if ( (found=line.indexOf("@abstract")) >=0 )
		//      b.setDescription(line.substring(found+9).trim());
	    else { // guess a title
             if (lineCount<5 && (found=line.indexOf(" * ")) >=0) {
		     if (line.contains("VuFind"))
		         b.title = line.substring(found+3).trim();
	         }
	    }
    }
}
