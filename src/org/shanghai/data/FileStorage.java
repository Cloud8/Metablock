package org.shanghai.data;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;
import org.shanghai.util.ModelUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.FileAlreadyExistsException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.shared.BadURIException;
import org.apache.jena.shared.CannotEncodeCharacterException;
import org.apache.jena.vocabulary.RDF;

public class FileStorage implements MetaCrawl.Storage {

    private String store; 
    private String suffix;
    private int count = 0;
    static private boolean homestore = false;
    static private boolean force = false;
    static private boolean test = false;

    public FileStorage(String store, boolean force) {
        //log("FileStorage [" + store + "]");
        this.store = store;
        this.force = force;
        this.suffix = ".rdf";
    }

    @Override
    public void create() {
        if (store==null) {
        } else if (Files.isDirectory(Paths.get(store))) {
        } else if (Files.isDirectory(Paths.get(
                   System.getProperty("user.home") + "/" + store))) {
          store = System.getProperty("user.home") + "/" + store;
          homestore = true;
          //log("store " + store);
        } else if (store.endsWith(".rdf")) {
        } else {
            store = null;
        }
    }

    @Override
    public void dispose() {
        store = null;
    }

    @Override
    public boolean test(Resource rc) {
        test = true;
		boolean b = false;
        if (store==null) {
            b = true;
        } else if (store.endsWith(".rdf")) {
            b = write(rc);
        }
        Path path = getPath(store, rc);
		b = Files.exists(path);
        log("test: " + path + " " + b);// + " " + rc.getURI());
        return b;
    }

    @Override
    public boolean delete(String resource) {
        return false;
    }

    @Override
    public boolean write(Resource rc) {
        StringWriter writer = new StringWriter();
        Path path = getPath(store, rc);
        if (path==null) {
		    return false;
        }
		Path parent = path.getParent();
        if (parent!=null && !Files.isDirectory(parent) && force) {
                FileUtil.mkdir(parent);
        }
        boolean b = ModelUtil.write(path, rc);
        if (count++%100==0) {
            log("wrote [" + path + "]");
        }
        return b;
    }

    public static Path getPath(Path store, Resource rc) {
        return getPath(store.toString(), rc, ".rdf");
    }

    public static Path getPath(String store, Resource rc) {
        return getPath(store, rc, ".rdf");
    }

    public static Path getPath(String store, Resource rc, String suffix) {
        Path path = getMaster(store, rc);
        if (path==null) {
            return path;
        } else if (store.endsWith(".rdf")) {
            return path;
        } 
        return path.resolveSibling(path.getName(path.getNameCount()-1)+suffix);
    }

    private static Path getMaster(String store, Resource rc) {
	    Path path = null;
        if (store==null) {
            log("Unwilling to perform [" + store + "]");
        } else if (store.endsWith(".rdf")) {
            path = Paths.get(store);
        } else {
            if (rc.getURI().length()<7) {
                log("bad resource " + rc.getURI());
                return path;
            } else if (homestore) {
                //log("home store " + store);
                store += rc.getURI().substring(rc.getURI().lastIndexOf("/"));
            } else {
                store += rc.getURI().substring(rc.getURI().indexOf("/",7));
            }

            path = Paths.get(store);
            if (test) log("first path " + path);
            String name = rc.getPropertyResourceValue(RDF.type).getLocalName();
            int slash = rc.getURI().lastIndexOf("/");
            int dot = rc.getURI().lastIndexOf(".");
            if (name.equals("JournalArticle") && slash>0) {
                path = path.resolve(rc.getURI().substring(slash+1));
            } else if (slash>0 && dot>slash) {
                path = path.resolveSibling(rc.getURI().substring(slash+1,dot));
            } else {
                path = path.resolve("about");
            }
            if (test) log("second path " + path);
        }
		return path;
    }

    @Override
    public void destroy() {
        log("Unwilling to perform.");
    }

    private static final Logger logger =
                         Logger.getLogger(FileStorage.class.getName());

    protected static void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        log(e.toString());
    }

}
