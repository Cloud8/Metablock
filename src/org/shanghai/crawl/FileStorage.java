package org.shanghai.crawl;

import org.shanghai.util.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Logger;
import com.hp.hpl.jena.rdf.model.Model;

public class FileStorage implements MetaCrawl.Storage {

    private String directory;
    private int count;

    public FileStorage(String directory) {
        this.directory = directory;
    }

    @Override
    public void create() {
        count=0;
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean exists(String resource) {
        return (new File(resource).exists());
    }

    @Override
    public boolean delete(String resource) {
        return false;
    }

    @Override
    public synchronized boolean post(String rdf) {
        count++;
        String fname = "/data-"+count+".rdf";
        int x = rdf.indexOf("rdf:about");
        int y = rdf.indexOf("\"",x+12);
        if (x>0 && y>x+12) {
            String about = rdf.substring(x+11,y);
            x = about.lastIndexOf("/") + 1;
            y = about.lastIndexOf(".");
            y = y>0?y:about.length();
            about = about.substring(x,y) + ".rdf";
            //System.out.println("[" + about + "]");
            x = about.lastIndexOf(":")+1;
            fname = about.substring(x);
        }
        FileUtil.write(directory + "/" + fname, rdf);
        return true;
    }

    @Override
    public boolean write(Model mod) {
        StringWriter writer = new StringWriter();
        boolean b = false;
        try {
            mod.write(writer, "RDF/XML-ABBREV");
            b = post(writer.toString());
        } finally {
            return b;
        }
    }

    @Override
    public boolean update(Model mod) {
        return write(mod);
    }

    @Override
    public void destroy() {
    }
}
