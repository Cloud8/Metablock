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
    static private boolean test = false;

    public FileStorage(String store) {
        this.store = store;
        this.suffix = ".rdf";
        log("FileStorage [" + store + "]");
    }

    @Override
    public void create() {
        if (store==null) {
        } else if (Files.isDirectory(Paths.get(store))) {
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
        Path path = getPath(store, rc);
		b = Files.exists(path);
        if (store.endsWith(".rdf")) {
            b = ModelUtil.write(path, rc);
        }
        log("test: " + path + " " + b + " [" + store + "]");
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
        if (parent!=null && !Files.isDirectory(parent)) {
            FileUtil.mkdir(parent);
        }
        boolean b = ModelUtil.write(path, rc);
        if (count++%100==0) {
            log("wrote [" + path + "]");
        }
        return b;
    }

    // used by ArchiveAnalyzer
    public static Path getPath(Path store, Resource rc) {
        return getPath(store.toString(), rc, ".rdf");
    }

    public static Path getPath(String store, Resource rc) {
        return getPath(store, rc, ".rdf");
    }

    // used by OAITransporter
    public static Path getPath(String store, Resource rc, String suffix) {
    /*
        Path path = getMaster(store, rc);
        if (path==null) {
            return path;
        } else if (store==null) {
            return path;
        } else if (store.endsWith(".rdf")) {
            return path;
        } else if (path.endsWith(".rdf")) {
            return path;
        } 
        //if (test) log("plain path: " + path);
        path = path.resolveSibling(path.getName(path.getNameCount()-1)+suffix);
        //if (test) log("path: " + path);
        return path;
        }

        private static Path getMaster(String store, Resource rc) {
    */
	    Path path = null;
        if (rc.getURI().startsWith("file:///")) {
            path = Paths.get(rc.getURI().substring(7));
        } else if (rc.getURI().startsWith("file://")) {
            path = Paths.get(System.getProperty("user.home") 
                             + "/" + rc.getURI().substring(7));
        } else if (store==null) {
            log("Unwilling to perform [" + store + "]");
            return path;
        } else if (store.endsWith(".rdf")) {
            path = Paths.get(store);
            return path;
        } else if (rc.getURI().length()<7) {
            log("bad resource " + rc.getURI());
        } else {
            store += rc.getURI().substring(rc.getURI().indexOf("/",7));
            path = Paths.get(store);
        }
        
        if (test) log("first path " + path);
        String name = rc.getPropertyResourceValue(RDF.type).getLocalName();
        if (rc.hasProperty(RDF.type, DCTerms.BibliographicResource)
            && rc.hasProperty(DCTerms.type)) {
            name = rc.getPropertyResourceValue(DCTerms.type).getLocalName();
        }
        int slash = rc.getURI().lastIndexOf("/");
        int dot = rc.getURI().lastIndexOf(".");
        if (name.equals("JournalArticle") && slash>0) {
            path = path.resolve(rc.getURI().substring(slash+1));
        } else if (slash>0 && dot>slash) {
            path = path.resolveSibling(rc.getURI().substring(slash+1,dot));
        } else {
            path = path.resolve("about");
        }
        //if (test) log("master path " + path);
        path = path.resolveSibling(path.getName(path.getNameCount()-1)+suffix);
        //if (test) log("final path: " + path);
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
