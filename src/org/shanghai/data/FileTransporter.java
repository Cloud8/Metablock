package org.shanghai.data;

import org.shanghai.util.FileUtil;
import org.shanghai.crawl.MetaCrawl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.Collections;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

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
        public Resource read(String fname);
    }

    private int count = 0;
	private int logC = 0;
	private int depth = 0;
	private int level = 0;
    private String[] suffixes = null;
    private List<String> identifiers;

    private List<Delegate> delegates;

    //public FileTransporter() {
    //    this("rdf", 1, 1);
    //}

    public FileTransporter(String suffix, int depth, int logC) {
        this.depth = depth;
        this.logC = logC;
        if (suffix!=null) {
            suffixes = suffix.split(" ");
        }
    }

    @Override
    public void create() {
        delegates = new ArrayList<Delegate>();
        identifiers = new ArrayList<String>();
    }

    @Override
    public void dispose() {
        identifiers.clear();
        for(Delegate d: delegates) {
            d.dispose();
        }
        delegates.clear();
    }

    @Override
    public String probe() {
        return FileTransporter.class.getSimpleName();
    }

    @Override
    public Resource read(String fname) {
        if (fname.startsWith("http://")) {
			return null;
        }
        Resource rc = null;
        for(Delegate d: delegates) {
            if (d.canRead(fname)) {
                rc = d.read(fname);
                break;
            }
        }
        return rc;
    }

    @Override 
    public List<String> getIdentifiers(int off, int limit) {
        //log("get " + off + " " + limit + " " + identifiers.size());
        if (off>identifiers.size()) {
            return null;
        } 
        int to = (off+limit)>identifiers.size()?identifiers.size():off+limit;
        return identifiers.subList(off, to);
    }

    @Override
    public int index(String resource) {
        identifiers.clear();
        Path path = Paths.get(resource);
        if (Files.isDirectory(path)) {
            // this.directory = resource;
        } else if (Files.isRegularFile(path)) {
            // single file not detected ?
            log("regular file " + path);
        } else {
            String home = System.getProperty("user.home");
            path = Paths.get(home + "/" + resource);
        }
        level = 0;
        crawl(path, depth);
        log("index " + path + " " + identifiers.size());
        return identifiers.size();
    }

    public Resource test(String resource) {
        int found = index(resource);
        if (found==1) {
            resource = identifiers.get(0);
        }
        Resource rc = null;
        for(Delegate d: delegates) {
            if (d.canRead(resource)) {
                log(d.getClass().getSimpleName() + " can read " + resource);
                rc = d.read(resource);
            } else {
                log(d.getClass().getName() + " can not read " + resource);
            }
        }
        return rc;
    }

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
                List<Path> dirs = new ArrayList<>();
                for (Path sub : paths) {
                    dirs.add(sub);
                }
                Collections.sort(dirs);

		        level++;
                for (Path p:dirs) {
                    //log("crawl " + p.toString());
				    crawl(p, mDepth);
				}
                level--;
                paths.close();
			} catch(IOException e) { log(e); }
        } else {
            checkFile(path);
        }
        return;
    }

    private void checkFile(Path path) {
        boolean b = true;
        if (suffixes!=null) {
            b = false;
            for (int i=0; i<suffixes.length; i++) {
            //   log("checkFile " + path.toString() + " suffix " + suffixes[i]);
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
            log(count + ": " + path.toString() +" ["+ level +"]");
        }
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
