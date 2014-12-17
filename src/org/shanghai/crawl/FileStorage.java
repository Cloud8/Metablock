package org.shanghai.crawl;

import org.shanghai.util.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.shared.BadURIException;

public class FileStorage implements MetaCrawl.Storage {

    private static final String dct = DCTerms.getURI();
    private String store; // relation, directory or filename
    private String suffix;
    private int count = 0;

    public FileStorage(String store) {
        this.store = store;
        this.suffix = ".rdf";
    }

    @Override
    public void create() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean test(String resource) {
        return (new File(resource).exists());
    }

    @Override
    public boolean delete(String resource) {
        return false;
    }

    @Override
    public boolean write(Model model, String id) {
        StringWriter writer = new StringWriter();
        boolean b = false;
        count++;
        if (new File(store).isDirectory()) {
            Property ident = model.createProperty(dct, "identifier");
            String path = store;
            if (model.listResourcesWithProperty(ident).hasNext()) {
                Resource rc = model.listResourcesWithProperty(ident).nextResource();
                String uri = rc.getURI().substring(8);
                path += "/" + uri.substring(uri.indexOf("/")+1); 
                String base = path.substring(0, path.lastIndexOf("/"));
                if (new File(base).exists()) {
                } else if (!new File(base).mkdirs()) {
                     log("failed path [" + base + "]");
                     return true;
                }
                if (new File(path).isDirectory()) {
                    File check = new File(path + "/about.rdf");
                    if (check.exists()) {
                        if (!new File(path + "/about.old").exists()) 
                            check.renameTo(new File(path + "/about.old"));
                    }
                    path = path + "/about.rdf";
                } else if (path.endsWith(".pdf")) {
                    path = path.substring(0, path.lastIndexOf('.')) + ".rdf";
                } else if (new File(path).mkdirs()) {
                    path += "/about.rdf";
                } else {
                    path += ".rdf";
                }
            } else { // its time for something completely different
                path += "/" + id + ".rdf";
            }
            model.write(writer, "RDF/XML-ABBREV");
            //if (count%100==0) {
                log("write to [" + path + "]");
            //}
            FileUtil.write( path, writer.toString());
            b = true;
        } else if (store.indexOf(":")>0) {
            String[] str = store.split(":");
            if (str.length==2 && str[0].equals("dct")) {
                Property prop = model.createProperty(dct, str[1]);
                String target = model.listResourcesWithProperty(prop)
                               .nextResource().getProperty(prop).getString();
                //log("write to [" + store + "] " + target);
                if (target.startsWith("http://")) {
                    b = true; //silence
                } else if (new File(target).exists()) {
                    model.write(writer, "RDF/XML-ABBREV");
                    target = target.substring(0,target.lastIndexOf("."));
                    FileUtil.write(target + suffix, writer.toString());
                    b = true; 
                } else {
                    target = System.getProperty("user.home") + "/" + target;
                    if (new File(target).exists()) {
                        try {
                            model.write(writer, "RDF/XML-ABBREV");
                            target = target.substring(0, target.lastIndexOf("."));
                            FileUtil.write(target + suffix, writer.toString());
                        } catch(BadURIException e) {
                            log(e);
                            log("Could not write " + target);
                        } finally {
                            b = true;
                        }
                    } else {
                        b = false; //nowhere to write to
                    }
                }
            }
        } else {
            model.write(writer, "RDF/XML-ABBREV");
            FileUtil.write(store, writer.toString());
            b = true; 
        }
        return b;
    }

    @Override
    public boolean update(String id, String field, String value) {
        log("Unwilling to perform.");
        return false;
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
