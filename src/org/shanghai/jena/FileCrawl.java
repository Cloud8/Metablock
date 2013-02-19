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
import java.util.logging.Logger;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title The File Crawl Class
   @date 2013-02-19
*/

public class FileCrawl {

    RDFTransporter transporter;
	int depth = 0;
	int count = 0;

    private static final Logger logger =
                         Logger.getLogger(FileUtil.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    public FileCrawl(Properties prop) {
		transporter = new RDFTransporter(prop);
    }

    public void create() {
        transporter.create();
    }

    public void dispose() {
        transporter.dispose();
    }

    public void crawl(String start, String pref, String suf, int maxDepth) {
	    File f = new File(start);
        //prop.list(System.out);
		crawl(f, pref, suf, maxDepth);
    }

    private void crawl(File f, String pref, String suf, int maxDepth) {
        if (depth>maxDepth)
            return;
    	if (f.isDirectory()) {
        	File[] subFiles = f.listFiles();
			if (subFiles==null) {
    	        log(" problem: " + f.getName() + " " + count);
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

}
