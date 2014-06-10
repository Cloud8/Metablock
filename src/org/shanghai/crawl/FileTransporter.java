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
    @abstract Crawl the local filesytem and call delegate on every file.
*/
public class FileTransporter implements MetaCrawl.Transporter {

    public interface Delegate {
        public Delegate create();
        public void dispose();
        public boolean canRead(File file);
        public Model read(String fname);
    }

    private int count = 0;
	private int logC = 0;
    private String suffix;
	private int depth = 0;
	private int level = 0;
    private String directory;
    private String[] suffixes;
    private List<String> identifiers;

    private List<Delegate> delegates;

    public FileTransporter(String suffix, int depth, int logC) {
        this.suffix = suffix;
        this.depth = depth;
        this.logC = logC;
        identifiers = new ArrayList<String>();
    }

    @Override
    public void create() {
        if (suffix!=null) {
            suffixes = suffix.split(" ");
        }
        delegates = new ArrayList<Delegate>();
    }

    @Override
    public void dispose() {
        identifiers.clear();
        for(Delegate d: delegates)
            d.dispose();
    }

    @Override
    public String probe() {
        return FileTransporter.class.getName();
    }

    @Override
    public Model read(String fname) {
        Model mod = null;
        for(Delegate d: delegates) {
            //log(d.getClass().getName() + " " + file);
            if (d.canRead(new File(fname))) {
                //mod = d.read(getIdentifier(fname));
                mod = d.read(fname);
                break;
            }
        }
		return mod;
    }

    //@Override 
    //public String getIdentifier(String fname) {
    //    if (directory==null)
    //        return fname;
    //    String id = fname.substring(directory.length()+1);
    //    return id;
    //}

    @Override 
    public String[] getIdentifiers(int off, int limit) {
        String[] list = new String[limit];
        for(int i=off; i<identifiers.size()&&i<off+limit; i++) 
            list[i-off] = identifiers.get(i);
        return list;
    }

    @Override
    public int crawl(String resource) {
        identifiers.clear();
	    File f = new File(resource);
        level = 0;
        if (depth==0)
            log("crawling " + resource );
        else
            log("crawling "+resource+" with depth "+depth+":" + logC);
        count=0;
		crawl(f, depth);
        return count;
    }

    public void setDirectory(String directory) {
         this.directory = directory;
    }

    public FileTransporter inject(Delegate d) {
         delegates.add(d);
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
        for(Delegate d: delegates) {
            if (d.canRead(f)) {
                //log(d.getClass().getName() + " canRead " + f.getName());
                identifiers.add(f.getPath());
                count++;
            }
        }
        if (logC!=0&&count%logC==0)
            log("" + count + ": " + f.getAbsolutePath() +" ["+ level +"]");
    }

    private static final Logger logger = 
                         Logger.getLogger(FileTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());    
    }

}
