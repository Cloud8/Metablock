package org.shanghai.crawl;

import org.shanghai.util.FileUtil;
import org.shanghai.util.PrefixModel;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.hp.hpl.jena.rdf.model.Model;
import java.util.Collections;

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
    private String[] suffixes;
    private List<String> identifiers;
    private boolean test = false;
    private Path testPath;

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
                mod = d.read(fname);
                break;
            }
        }
		return mod;
    }

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
        } else {
            String home = System.getProperty("user.home");
            path = Paths.get(home + "/" + resource);
        }
        level = 0;
		if (test) testPath = path;
        else crawl(path, depth);
        return count;
    }

    public Model test(String resource) {
        test = true;
        int found = crawl(resource);
        Model model = null;
        for(Delegate d: delegates) {
            if (d.canRead(resource)) {
                log(d.getClass().getName() + " canRead " + resource);
                model = d.read(testPath.toString());
            }
        }
        return model;
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
