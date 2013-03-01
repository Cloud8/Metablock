package org.shanghai.crawl;

import org.shanghai.bones.BiblioRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;

// import org.apache.solr.schema.DateField;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop <fb.com/goetz.hatop>
  @title An Experimental Code File Scanner
  @date 2012-10-07
  @abstract reads a (preferable php or java) code file and tries
            to guess some metadata like author and title.
*/
public class TextFileScanner {

    private static final Logger logger =
                         Logger.getLogger(TextFileScanner.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());    
    }

    public void create() {
    }

    public void dispose() {
    }

    public void update(File file, BiblioRecord b) {
        try {
            scanFile(file, b);
        } catch(FileNotFoundException e) { log(e); }
          catch(IOException e) { log(e); }
    }
 
    int lineCount;
    public void scanFile(File file, BiblioRecord b) 
              throws FileNotFoundException, IOException
    {
	    StringBuilder fulltextBuf;
        BufferedReader reader;
    
		//RDA Content Type 
	    b.setFormat("Computer File"); 
        b.setLanguage(b.id.lastIndexOf(":")>0 ?
                      b.id.substring(b.id.lastIndexOf(":")+1) : "Polish");
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
        //GH2013-02 : good for indexing
		//b.fulltext = fulltextBuf.toString();
	}

    /** look some for tags. */
    private void scanTag(String line, BiblioRecord b) {
	    int found = -1;
	    int x = -1;
	    if ( (found=line.indexOf("@author")) >=0 ) {
             if ( (x=line.indexOf("<"))>0 )
		         b.author = line.substring(found+7, x).trim();
		     else b.author = line.substring(found+7).trim();
        }
	    else if ( (found=line.indexOf("@package")) >=0 )
		     b.setInstitution(line.substring(found+8).trim());
	    else if ( (found=line.indexOf("Copyright (C)")) >=0 )
		     b.publisher = line.substring(found+14).trim();
	    if ( (found=line.indexOf("@title")) >=0 )
		     b.title = line.substring(found+6).trim();
	    else { // find a title
             if (lineCount<5 && (found=line.indexOf(" * ")) >=0) {
		     if (line.contains("VuFind"))
		         b.title = line.substring(found+3).trim();
	         }
	    }
    }
}
