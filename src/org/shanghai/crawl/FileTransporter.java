package org.shanghai.crawl;

import org.shanghai.util.FileUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;

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
        public boolean canRead(String fname);
        public Model read(String fname);
    }

    private int count = 0;
	private int logC = 0;
    private String suffix;
	private int depth = 0;
	private int level = 0;
    //private String directory;
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
        for(Delegate d: delegates) {
            d.dispose();
        }
    }

    @Override
    public String probe() {
        return FileTransporter.class.getName();
    }

    @Override
    public Model read(String fname) {
        Model mod = null;
        if (fname.startsWith("http://")) {
			return mod;
        }
        for(Delegate d: delegates) {
            if (d.canRead(fname)) {
                //log(d.getClass().getName() + " reads " + fname);
                //if (directory==null) {
                    mod = d.read(fname);
                //} else {
                //    mod = d.read(fname);
                //    //log("read " + directory + "/" + fname);
                //    //mod = d.read(directory + "/" + fname);
                //}
                break;
            }
        }
		return mod;
    }

    /*
    private String findName(String fname) {
	    String key = fname.substring(fname.indexOf("/",8));
		//log("find " + key);
		for (String str : identifiers) {
		    if (str.contains(key)) {
                log("read " + str + " " + fname);
			    return str;
			}
		}
		return key;
    }
    */

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
        log("crawl " + resource);
        identifiers.clear();
        count=0;
        Path path = Paths.get(resource);
        if (Files.isDirectory(path)) {
            // this.directory = resource;
        } else if (Files.isRegularFile(path)) {
            // crawl(path, 0);
            // return 1;
        } else {
            String home = System.getProperty("user.home");
            path = Paths.get(home + "/" + resource);
			// if (Files.isDirectory(path)) {
            //     // this.directory = home + "/" + resource;
            //     // this.directory = home;
            // } else {
            //     return 0;
			// }
        }
        level = 0;
		crawl(path, depth);
        return count;
    }

    /*
    public int oldCrawl(String resource) {
        log("crawl " + resource);
        identifiers.clear();
        
	    File f = new File(resource);
        if (f.exists()) {
            this.directory = resource;
        } else {
            String home = System.getProperty("user.home");
            f = new File(home + "/" + resource);
            if (f.exists()) {
                //setDirectory(home);
                this.directory = home + "/" + resource;
            } else {
                return 0;
            } 
        }
        level = 0;
        count=0;
		crawl(f, depth);
        return count;
    }
    */

    //public void setDirectory(String directory) {
    //     this.directory = directory;
    //}

    public FileTransporter inject(Delegate d) {
         delegates.add(d);
         return this;
    }

    //depth==0 means unlimited recurse. 
    private void crawl(Path path, int mDepth) {
        if (mDepth!=0 && level>mDepth)
            return;
    	if (Files.isDirectory(path) && Files.isReadable(path)) {
            //log("crawling " + path.getFileName() + " level " + level); 
			try {
			    DirectoryStream<Path> paths = Files.newDirectoryStream(path);
		        level++;
                for (Path p:paths) {
				    crawl(p, mDepth);
				}
                level--;
			} catch(IOException e) { log(e); }
        } else {
            checkFile(path);
        }
        return;
    }

    /*
    private void crawl(File f, int mDepth) {
        if (mDepth!=0 && level>mDepth)
            return;
    	if (f.isDirectory() && f.canRead()) {
            //log("crawling " + f.getName() + " level " + level); 
        	File[] subFiles = f.listFiles();
			if (subFiles==null) {
    	        log(" problem: " + f.getAbsolutePath() + " [" + count + "]");
            } else {
		        level++;
        	    for (int i = 0; i < subFiles.length; i++) {
                  	crawl(subFiles[i], mDepth);
        	    }
                level--;
            }
        } else {
            checkFile(f.getName());
        }
        return;
    }
    */

    private void checkFile(Path path) {
        //log(" checkFile " + path.toString());
        boolean b = true;
        if (suffix!=null) {
            b = false;
            for (int i=0; i<suffixes.length; i++) {
                 if (path.toString().endsWith(suffixes[i])) 
                     b=true;
            }
        }
        if (!b)
            return;
        for(Delegate d: delegates) {
            if (d.canRead(path.toString())) {
                //log(d.getClass().getName() + " canRead " + path.toString());
                identifiers.add(path.toString());
                count++;
            } else {
                b = false;
            }
        }
        if (b&&logC!=0&&count%logC==0) {
            //log(count + ": " + f.getAbsolutePath() +" ["+ level +"]");
            log(count + ": " + path.toString() +" ["+ level +"]");
        }
    }

    /*
    private void checkFile(String fname) {
        boolean b = true;
        if (suffix!=null) {
            b = false;
            for (int i=0; i<suffixes.length; i++) {
                 if (fname.endsWith(suffixes[i])) 
                     b=true;
            }
        }
        if (!b)
            return;
        for(Delegate d: delegates) {
            if (d.canRead(fname)) {
                //log(d.getClass().getName() + " canRead " + f.getName());
                identifiers.add(fname);
                count++;
            } else {
                b = false;
            }
        }
        if (b&&logC!=0&&count%logC==0) {
            //log(count + ": " + f.getAbsolutePath() +" ["+ level +"]");
            log(count + ": " + fname +" ["+ level +"]");
        }
    }
    */

    private static final Logger logger = 
                         Logger.getLogger(FileTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());    
    }

}
