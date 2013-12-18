package org.shanghai.crawl;

import org.shanghai.util.FileUtil;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.hp.hpl.jena.rdf.model.Model;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop 
    @title The File Crawl Class
    @date 2013-02-19
    @abstract Crawl the local filesytem and call
              transporter on every file found.
*/
public class FileCrawl implements MetaCrawl.Transporter {

    public interface Delegate {
        public void create();
        public void dispose();
        public void setDirectory(String dir);
        public boolean canRead(File file);
        public void addScanner(FileTransporter.Scanner scanner);
        public Model read(String resource);
    }

    private int count = 0;
	private int logC = 0;
    private String suffix;
	private int depth = 0;
	private int level = 0;
    private String directory;
    private String[] suffixes;
    private List<String> identifiers;

    private Delegate delegate;

    public FileCrawl(String suffix, int depth, int logC) {
        this.suffix = suffix;
        this.depth = depth;
        this.logC = logC;
        delegate = new FileTransporter();
        identifiers = new ArrayList<String>();
    }

    @Override
    public void create() {
        if (suffix!=null) {
            suffixes = suffix.split(" ");
        }
        //log("created " + suffix);
        //for (String str : suffixes)
        //     log("[" + str + "]");
        delegate.create();
    }

    @Override
    public void dispose() {
        delegate.dispose();
        identifiers.clear();
        //log("disposed " + suffix);
    }

    @Override
    public String probe() {
        return FileCrawl.class.getName();
    }

    @Override
    public Model read(String file) {
		return delegate.read(file);
    }

    @Override 
    public String[] getIdentifiers(int off, int limit) {
        String[] list = new String[limit];
        for(int i=off; i<identifiers.size()&&i<off+limit; i++) 
            list[i-off] = identifiers.get(i);
        return list;
    }

    @Override
    public int crawl(String directory) {
        identifiers.clear();
        this.directory = directory;
	    File f = new File(directory);
        level = 0;
        if (depth==0)
            log("crawling " + directory );
        else
            log("crawling "+directory+" with depth "+depth+":" + logC);
        delegate.setDirectory(directory);
        count=0;
		crawl(f, depth);
        return count;
    }

    @Override
    public boolean canRead(String resource) {
        //log("canRead " + resource);
		return delegate.canRead(new File(resource));
    }

    //@Override
    public FileCrawl addScanner(FileTransporter.Scanner s) {
         delegate.addScanner(s); 
         return this;
    }

    //depth==0 means unlimited recurse. 
    private void crawl(File f, int mDepth) {
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
                  	crawl(subFiles[i], mDepth);
        	    }
                level--;
            }
        } else {
            checkFile(f);
        }
        return;
    }

    private void checkFile(File f) {
        boolean b = true;
        if (suffix!=null) {
            b = false;
            for (int i=0; i<suffixes.length; i++) {
                 //log(f.getName() + " " + suffixes[i]);
                 if (f.getName().endsWith(suffixes[i])) 
                     b=true;
            }
        }
        if (!b)
            return;
        if (delegate.canRead(f)) {
            //log("delegate.canRead " + f.getName());
            identifiers.add(f.getPath());
            count++;
        }
        if (logC!=0&&count%logC==0)
            log("" + count + ": " + f.getAbsolutePath() +" ["+ level +"]");
    }

    private static final Logger logger = 
                         Logger.getLogger(FileCrawl.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());    
    }

}
