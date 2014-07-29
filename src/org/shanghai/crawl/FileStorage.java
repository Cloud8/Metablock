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
    private String store;
    private String suffix;
    private int count;

    public FileStorage(String store) {
        this.store = store;
        this.suffix = ".rdf";
    }

    public FileStorage(String store, String suffix) {
        this.store = store;
        this.suffix = suffix;
    }

    @Override
    public void create() {
        count=0;
        if (new File(store).exists());
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
    public boolean write(Model model) {
        StringWriter writer = new StringWriter();
        boolean b = false;
        if (new File(store).isDirectory()) {
            Property ident = model.createProperty(dct, "identifier");
            Resource rc = model.listResourcesWithProperty(ident).nextResource();
            String id = rc.getProperty(ident).getString();
            model.write(writer, "RDF/XML-ABBREV");
            FileUtil.write( store + "/" + id + suffix, writer.toString());
            b = true;
        } else {
            String[] str = store.split(":");
            if (str.length==2 && str[0].equals("dct")) {
                Property prop = model.createProperty(dct, str[1]);
                String target = model.listResourcesWithProperty(prop)
                               .nextResource().getProperty(prop).getString();
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
        }
        return b;
    }

    @Override
    public boolean update(Model mod) {
        return write(mod);
    }

    @Override
    public void destroy() {
        //System.out.println("No.");
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
