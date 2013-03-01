package org.shanghai.crawl;

import org.shanghai.crawl.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;
import java.util.logging.Logger;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title The File Crawl Class
   @date 2013-02-19
   @abstract This class can crawl the local filesytem and call a
             transporter on every file found.
*/
public class FileCrawl {

    public interface Transporter {
        public void create(); 
        public void dispose(); 
        public void clean(); 
        public boolean create(File file); 
        public boolean update(File file); 
        public String read(String what); 
        public void delete(String what); 
        public void setStartDirectory(String dir); 
    }

	public int count = 0;
	public int logC = 0;
    public boolean create; 

    private Transporter transporter;
	private int depth = 0;
	private int level = 0;

    private static final Logger logger =
                         Logger.getLogger(FileCrawl.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());    
    }

    public FileCrawl(Transporter transporter) {
        this.transporter = transporter;
    }

    public FileCrawl(Transporter transporter, String depth) {
        this.transporter = transporter;
        if (depth!=null)
            this.depth = Integer.parseInt(depth);
    }

    public void create() {
        transporter.create();
    }

    public void dispose() {
        transporter.dispose();
    }

    public void clean() {
        transporter.clean();
    }

    public void crawl(String dir) {
	    File f = new File(dir);
        level = 0;
        if (depth==0)
            log("crawling " + dir );
        else
            log("crawling " + dir + " with depth " + depth + ".");
        transporter.setStartDirectory(dir);
		crawl(f, depth, create);
    }

    //depth==0 means recurse unlimited. Create or Update.
    private void crawl(File f, int mDepth, boolean create) {
        if (mDepth!=0 && level>mDepth)
            return;
    	if (f.isDirectory()) {
            //log("crawling " + f.getName() + " level " + level); 
        	File[] subFiles = f.listFiles();
			if (subFiles==null) {
    	        log(" problem: " + f.getName() + " [" + count + "]");
            } else {
		        level++;
        	    for (int i = 0; i < subFiles.length; i++) {
                  	crawl(subFiles[i], mDepth, create);
        	    }
                level--;
            }
        } else {
            boolean b;
            if (create)
                b = transporter.create(f);
            else
                b = transporter.update(f);
			if (b) {
                count++;
                if (logC!=0 && count%logC==1)
                log("" + count + ": " + f.getAbsolutePath() +" ["+ level +"]");
            }
        }
    }

    public void delete(String resource) {
		transporter.delete(resource);
    }

    public void update(File file) {
		transporter.update(file);
    }

    public String read(String resource) {
        String about = transporter.read(resource);
        return about;
    }

}
