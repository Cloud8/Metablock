package org.shanghai.crawl;

import org.shanghai.util.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;

public class FileStorage implements MetaCrawl.Storage {

    private static final String dct = DCTerms.getURI();
    private String store;
    private int count;

    public FileStorage(String store) {
        this.store = store;
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
        Property ident = model.createProperty(dct, "identifier");
        Resource rc = model.listResourcesWithProperty(ident).nextResource();
        String id = rc.getProperty(ident).getString();
        boolean b = false;
        if (new File(store).isDirectory()) {
            model.write(writer, "RDF/XML-ABBREV");
            FileUtil.write( store + "/" + id + ".rdf", writer.toString());
            b = true;
        } else {
            String[] str = store.split(":");
            if (str.length==2 && str[0].equals("dct")) {
                Property prop = model.createProperty(dct, str[1]);
                String target = model.listResourcesWithProperty(ident)
                               .nextResource().getProperty(prop).getString();
                if (target.startsWith("http://")) {
                    b = true; //silence
                } else if (new File(target).exists()) {
                    model.write(writer, "RDF/XML-ABBREV");
                    target = target.substring(0,target.lastIndexOf("."));
                    FileUtil.write(target + ".rdf", writer.toString());
                    b = true; 
                } else {
                    target = System.getProperty("user.home") + "/" + target;
                    if (new File(target).exists()) {
                        model.write(writer, "RDF/XML-ABBREV");
                        target = target.substring(0, target.lastIndexOf("."));
                        FileUtil.write(target + ".rdf", writer.toString());
                        b = true;
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
    }

}
