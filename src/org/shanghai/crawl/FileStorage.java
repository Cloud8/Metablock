package org.shanghai.crawl;

import org.shanghai.util.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.FileAlreadyExistsException;

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
        if (store!=null) {
            log("store " + store);
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean test(String resource) {
        return (Files.exists(Paths.get(resource)));
    }

    @Override
    public boolean delete(String resource) {
        return false;
    }

    @Override
    public boolean write(Model model, String uri) {
        log("write " + uri);
        StringWriter writer = new StringWriter();
        boolean b = false;
        count++;
        if (Files.isRegularFile(Paths.get(uri))) {
            String fname = uri;
            if (!fname.endsWith(".rdf") && fname.contains(".")) {
                fname = fname.substring(0,fname.lastIndexOf(".")) + ".rdf";
            }
            try {
                model.write(writer, "RDF/XML-ABBREV");
                FileUtil.write(fname, writer.toString());
                //log("wrote " + fname);
            } catch(CannotEncodeCharacterException e) { log(e); log(uri); }
            finally {
                b = true; 
            }
        } else if (Files.isDirectory(Paths.get(store))) {
            if (uri.startsWith("http")) {
			    uri = uri.substring(7);
            } 
            String path = store;
            String[] parts = uri.substring(uri.indexOf("/")+1).split("/");
            for (String part : parts) {
                if (path.contains(part)) {
                    //System.out.print("[" + part + "]");
                } else {
					path += "/" + part;
                }
            }
            //log("path " + path + " " + uri.substring(uri.indexOf("/")+1));
            String about = uri.contains(".")? "about.rdf" : uri+".rdf";
            if (Files.isDirectory(Paths.get(path))) {
				Path check = Paths.get(path + "/" + about);
				if (Files.exists(check)) {
					try {
					    Files.move(check, check.resolveSibling("about.old"));
				    } catch(FileAlreadyExistsException e) { /* skip */ }
				      catch(IOException e) { log(e); }
                }
                path = path + "/" + about;
            } else if (path.endsWith(".pdf")) {
                path = path.substring(0, path.lastIndexOf('.')) + ".rdf";
            } else {
				try {
			        Path p = Files.createDirectories(Paths.get(path));
				} catch(IOException e) { log(e); }
                path += "/" + about;
            }
            try {
                model.write(writer, "RDF/XML-ABBREV");
                FileUtil.write( path, writer.toString());
                log("wrote " + path);
            } catch(BadURIException e) { log(path); log(e); }
            if (count%100==0) {
                log("write " + uri + " to [" + path + "]");
            }
            b = true;
        } else {
            log("Unwilling to perform [" + store + "]");
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
