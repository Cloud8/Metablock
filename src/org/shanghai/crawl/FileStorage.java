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
import com.hp.hpl.jena.shared.CannotEncodeCharacterException;

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
    public boolean write(Model model, String uri) {
        StringWriter writer = new StringWriter();
        boolean b = false;
        count++;
        if (new File(uri).isFile()) {
            String fname = uri;
            if (!fname.endsWith(".rdf") && fname.contains(".")) {
                fname = fname.substring(0,fname.lastIndexOf(".")) + ".rdf";
            }
            try {
                model.write(writer, "RDF/XML-ABBREV");
                log("write " + fname);
                FileUtil.write(fname, writer.toString());
            } catch(CannotEncodeCharacterException e) { log(e); log(uri); }
            finally {
                b = true; 
            }
        } else if (new File(store).isDirectory()) {
            if (!uri.startsWith("http")) {
                Property i = model.createProperty(dct, "identifier");
                if (model.listResourcesWithProperty(i).hasNext()) {
                    Resource rc = model.listResourcesWithProperty(i)
                                       .nextResource();
				    uri = rc.getURI();
                }
            }
			uri = uri.substring(7);
            String path = store;
            String[] parts = uri.substring(uri.indexOf("/")+1).split("/");
            for (String part : parts) {
                if (path.contains(part)) {
                    //System.out.print("[" + part + "]");
                } else {
					path += "/" + part;
                }
            }
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
            model.write(writer, "RDF/XML-ABBREV");
            if (count%100==0) {
                log("write " + uri + " to [" + path + "]");
            }
            FileUtil.write( path, writer.toString());
            //log("FileUtil.write( " + path + ")");
            b = true;
        } else {
            log("Unwilling to perform.");
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
