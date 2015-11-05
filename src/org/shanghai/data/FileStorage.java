package org.shanghai.data;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;

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

public class FileStorage implements MetaCrawl.Storage {

    private static final String dct = DCTerms.getURI();
    private String store; // relation, directory or filename
    private String suffix;
    private int count = 0;
    private boolean force;

    public FileStorage(String store, boolean force) {
        this.store = store;
        this.force = force;
        this.suffix = ".rdf";
    }

    @Override
    public void create() {
        if (Files.isDirectory(Paths.get(store))) {
        } else {
            store = null;
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean test(Resource rc, String resource) {
        log("store " + store);
        return (Files.exists(Paths.get(resource)));
    }

    @Override
    public boolean delete(String resource) {
        return false;
    }

    @Override
    public boolean write(Resource rc, String fname) {
        StringWriter writer = new StringWriter();
        boolean b = false;
        Path path = null;
        count++;
        if (store==null && Files.isRegularFile(Paths.get(fname))) {
            if (!fname.endsWith(".rdf") && fname.contains(".")) {
                path = Paths.get(
                       fname.substring(0, fname.lastIndexOf(".")) + ".rdf");
            }
        } else if (store==null) {
            log("Unwilling to perform [" + store + "]");
            return false;
        } else {
            String test = store;
            if (rc.getURI().length()<7) {
                log("bad resource " + rc.getURI());
                return false;
            } else {
                test += rc.getURI().substring(rc.getURI().indexOf("/",7));
            }

            path = Paths.get(test);
            log("first path " + path);
            if (!Files.isDirectory(path.getParent()) && force) {
                FileUtil.mkdir(path.getParent());
            }
            if (Files.isDirectory(path)) {
                path = path.resolve("about.rdf");
            } else if (Files.isDirectory(path.getParent())) {
                int slash = rc.getURI().lastIndexOf("/");
                int dot = rc.getURI().lastIndexOf(".");
                if (slash>0 && dot>slash) {
                    path = path.resolveSibling(
                           rc.getURI().substring(slash+1, dot) + ".rdf");
                }
            } 
        }
        if (path!=null && path.toString().endsWith(".rdf")) try {
            if (!Files.isRegularFile(path) || force) {
                rc.getModel().write(writer, "RDF/XML-ABBREV");
                boolean write = FileUtil.write(path, writer.toString());
                if (!write) log("could not write " + path);
            } else {
                log("could not overwrite " + path);
            }
        } catch(BadURIException e) { log(path.toString()); log(e); }
          catch(CannotEncodeCharacterException e) { log(path.toString()); log(e); }
        finally {
            if (count%100==0) {
                log("write " + rc.getURI() + " to [" + path + "]");
            }
            b = true;
        }
        return b;
    }

    @Override
    public void destroy() {
        log("Unwilling to perform.");
    }

    private static final Logger logger =
                         Logger.getLogger(FileStorage.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        log(e.toString());
    }

}
