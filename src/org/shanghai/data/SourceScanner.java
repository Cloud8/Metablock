package org.shanghai.data;

import org.shanghai.data.FileTransporter;
import org.shanghai.util.FileUtil;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.sparql.vocabulary.FOAF;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.io.StringReader;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.logging.Logger;

/**
 * @license http://www.apache.org/licenses/LICENSE-2.0
 * @author Alfred Anders
 * @title Experimental Source File Scanner to guess some Metadata. 
 * @date 2012-10-11
 */
public class SourceScanner extends FileScanner 
    implements FileTransporter.Delegate {

    private int count;
    private static final Logger logger =
                         Logger.getLogger(SourceScanner.class.getName());

    public void log(String msg) {
        logger.info(msg);
    }

    @Override
    public FileTransporter.Delegate create() {
        count = 0;
        return this;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Resource read(String fname) {
        Resource rc = super.read(fname);
        try {
		    this.scanFile(fname, rc);
        } //catch(FileNotFoundException e) { log(e); }
          catch(IOException e) { log(e); }
        finally {
            return rc;
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
    private String author;
    private String title;
    private void scanFile(String fname, Resource rc) 
              throws IOException
    {
        author = null;
        title = null;
	    StringBuilder sb = new StringBuilder();
    
	    rc.addProperty(DCTerms.type, "Software"); //dini publ type
	    rc.addProperty(DCTerms.format, "Computer File"); //RDA Content Type 

        int x = fname.lastIndexOf(".")+1;
        String lang = x>0 ? fname.substring(x) : "Polish";
	    rc.addProperty(DCTerms.language, lang); 

        lineCount = 0;
		String content = FileUtil.read(fname);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
		         new ByteArrayInputStream(content.getBytes())));
        String line = reader.readLine();
        while(line != null) {
			lineCount++;
            scanTag(line, rc);
			sb.append(line);
			sb.append("\n");
            line = reader.readLine();
        }
	    rc.addProperty(DCTerms.extent, ""+lineCount + " lines"); 
        if (title==null) {
            title = fname.substring(fname.lastIndexOf("/")+1);
        }
	    rc.addProperty(DCTerms.title, title); 
	}

    /** look for tags */
    private void scanTag(String line, Resource rc) {
	    int found = -1;
	    int x = -1;
	    if ( (found=line.indexOf("@author"))>=0 && author==null) {
            author = line.substring(found+7).trim();
            author = author.replaceAll("<.*?>","");
            String[] str = author.split(" ");
            author = "";
            for (String s : str) {
                if (!s.contains("@"))
                    author += s + " ";
            } 
            author = author.trim();
            if (author.length()>0) {
                String uri = iri + "/" + author.replaceAll("[^A-Za-z]", "");
                Resource person = rc.getModel().createResource(uri, FOAF.Person);
                person.addProperty(FOAF.name, author);
	            rc.addProperty(DCTerms.creator, person); 
            }
	    } else if ( title==null && (found=line.indexOf("@title")) >=0 ) {
		    title = line.substring(found+6).trim();
	    }
    }
}
