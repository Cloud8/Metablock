package org.shanghai.jena;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;

import java.util.Properties;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A little File Crawl Class
   @date 2013-01-22
*/


public class FileCrawl {

    RDFTransporter transporter;
	int depth = 0;
	int count = 0;

    public FileCrawl(Properties prop) {
		transporter = new RDFTransporter(prop);
    }

    public void crawl(String start, String pref, String suf, int maxDepth) {
	    File f = new File(start);
        // prop.list(System.out);
		crawl(f, pref, suf, maxDepth);
    }

    public void create() {
        transporter.create();
    }

    public void dispose() {
        transporter.dispose();
    }

    private void log(String msg) {
        System.out.println(msg);
    }

    public void crawl(File f, String pref, String suf, int maxDepth) {
        if (depth>maxDepth)
            return;
    	if (f.isDirectory()) {
        	File[] subFiles = f.listFiles();
			if (subFiles==null) {
    	        System.out.println("" + count + " problem: " + f.getName());
            } else {
		        depth++;
        	    for (int i = 0; i < subFiles.length; i++) {
                  	crawl(subFiles[i], pref, suf, maxDepth);
        	    }
                depth--;
            }
        } else {
		    if (f.getName().startsWith(pref)
			    && f.getName().endsWith(suf)) {
				count++;
				transporter.update(f);
                if (count%100==0)
                    log("" + count + ": " + f.getAbsolutePath() + " " + depth);
		    }
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
		Properties prop = new Properties();
		try {
		    prop.load(
			RDFTransporter.class.getResourceAsStream("/shanghai.properties"));
		} catch(IOException e) { e.printStackTrace(); }
	    FileCrawl myself = new FileCrawl(prop);
        myself.log(args[0]);
	    myself.crawl(args[0], "rdf-", ".xml", 3);
        long end = System.currentTimeMillis();
        System.out.println("indexed " + myself.count + " records in "
                       + ((end - start)/1000) + " sec");

	}
}
