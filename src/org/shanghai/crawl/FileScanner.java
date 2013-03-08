package org.shanghai.crawl;

import org.shanghai.bones.BiblioRecord;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @license http://www.apache.org/licenses/LICENSE-2.0
 * @author Goetz Hatop <fb.com/goetz.hatop>
 * @title Simple Scanner able to deliver some file Metadata. 
 * @date 2012-10-11
 * @abstract reads a (preferable php or java) code file and tries
             to guess some metadata like author and title.
 */
public class FileScanner implements TDBTransporter.Scanner {

    private String directory;
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
        return this;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void setStartDirectory(String d) {
        this.directory = d;
    }

    @Override
    public BiblioRecord getRecord(File file) {
        BiblioRecord b = RecordFactory.getRecord(directory, file);
        try {
		    this.scanFile(file, b);
        } catch(FileNotFoundException e) { log(e); }
          catch(IOException e) { log(e); }
        return b;
    }

    @Override
    public String getDescription(File file) {
        return getRecord(file).toString();
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

    /** look some for tags. */
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
	    else if ( (found=line.indexOf("@abstract")) >=0 )
		     b.description += line.substring(found+9).trim();
	    else { // guess a title
             if (lineCount<5 && (found=line.indexOf(" * ")) >=0) {
		     if (line.contains("VuFind"))
		         b.title = line.substring(found+3).trim();
	         }
	    }
    }
}
