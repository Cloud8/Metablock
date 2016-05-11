package org.shanghai.ojs;

import org.shanghai.crawl.MetaCrawl.Analyzer;
import org.shanghai.util.FileUtil;
import org.shanghai.util.Language;
import org.shanghai.util.ModelUtil;
import org.shanghai.data.FileStorage;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.DCTypes;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.ResourceUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/** 
  * @title Archive to File System
  * @date 2015-11-22 
  */
public class ArchiveAnalyzer implements Analyzer {

    private Path store;
    private Language language;
    private int count;
    private HashMap<String,String> hash;

    public ArchiveAnalyzer(String store) {
        this.store = Paths.get(store);
    }

    @Override
    public Analyzer create() {
        language = new Language();
        language.create();
        count = 0;
        hash = new HashMap<String,String>();
        return this;
    }

    @Override
    public void dispose() {
        language.dispose();
        hash.clear();
    }

    @Override
    public String probe() {
        return " store " + store;
    }

    @Override
    public Resource analyze(Resource rc) {
        Resource sub = null;
        String name = rc.getPropertyResourceValue(DCTerms.type).getLocalName();
        if (name.equals("JournalArticle")) {
            if (rc.hasProperty(DCTerms.language)) {
                // good.
            } else if (rc.hasProperty(DCTerms.abstract_)) {
                log("analyze language " + rc.getURI().substring(32));
                language.analyze(rc);
                // log("has property abstract");
            }
        }
        StmtIterator si = rc.listProperties(DCTerms.isPartOf);
        while(si.hasNext()) {
            Resource obj = si.nextStatement().getResource();
            name = obj.getPropertyResourceValue(RDF.type).getLocalName();
            if (name.equals("JournalIssue")) {
                sub = getPartOf(obj);
                sub.addProperty(DCTerms.hasPart, rc);
            }
        }
        boolean b = false;
        if (store==null) { // no writes from here
            // log("analyze " + id + " " + rc.getURI());
        } else { 
	        b = writeParts(store, rc); // write article
            // b = true ; // write issue only
            if (b && sub!=null && !hash.containsKey(sub.getURI())) {
                b = writeParts(store, sub); // write issue
                if (b) hash.put(sub.getURI(), sub.getURI());
            }
            count++;
        }
        return rc;
    }

    @Override
    public Resource test(Resource rc) {
        log("test # " + rc.getURI() + " " + store);
        return rc;
    }

    // return a copy of isPartOf Resource 
    private Resource getPartOf(Resource rc) {
        Model model = ModelUtil.createModel();
        StmtIterator si = rc.listProperties();
        while( si.hasNext() ) {
            Statement stmt = si.nextStatement();
            model.add(stmt);
            if (stmt.getObject().isResource()) {
                Resource obj = stmt.getObject().asResource();
                StmtIterator sub = obj.listProperties();
                while( sub.hasNext() ) {
                    model.add(sub.nextStatement());
                }
            }
        }
        return model.getResource(rc.getURI());
    }

    /** write resource parts to file system */
    private boolean writeParts(Path store, Resource rc) {
        Path path = FileStorage.getPath(store, rc);
        boolean b = false;
	    if (path==null) {
		    return b;
		}
        String name = rc.getPropertyResourceValue(RDF.type).getLocalName();
        if (name.equals("BibliographicResource")) {
            name = rc.getPropertyResourceValue(DCTerms.type).getLocalName();
        }

        if (Files.isRegularFile(path.resolveSibling("index.html"))) {
            //log(" file exists: index.html"); 
        } else if (rc.hasProperty(DCTerms.source)) {
            String url = rc.getProperty(DCTerms.source).getResource().getURI();
            FileUtil.mkdir(path.getParent());
            FileUtil.copy(url, path.resolveSibling("index.html"));
            if (url.length()>32 && rc.getURI().length()>32 && 
                url.substring(0,32).equals(rc.getURI().substring(0,32))) {
                //log("remove " + url);
            }
        }

        StmtIterator si = rc.listProperties(DCTerms.hasPart);
		String fname = path.getName(path.getNameCount()-1).toString();
        String uri = rc.getURI().substring(0, rc.getURI().indexOf("/",9));
        HashMap<Resource,String> hash = new HashMap<Resource,String>();
        while (si.hasNext()) {
            Statement stmt = si.nextStatement();
            Resource obj = stmt.getResource();
            if (obj.hasProperty(DCTerms.format)) {
                String format = obj.getProperty(DCTerms.format).getResource()
                                   .getProperty(RDFS.label).getString();
                if (name.startsWith("Journal") && format.contains("pdf")) {
				    String pdf = fname.substring(0, fname.indexOf(".rdf")) + ".pdf";
                    //Path base = store.relativize(path.resolveSibling(pdf));
                    if (Files.isRegularFile(path.resolveSibling(pdf))) {
                        //log(" file exists: " + pdf); 
				    } else {
                        //log("write " + obj.getURI() + " to " + pdf);
                        FileUtil.mkdir(path.getParent());
                        FileUtil.copy(obj.getURI(), path.resolveSibling(pdf));
				    }
                    hash.put(obj, rc.getURI() + "/" + pdf);
                } else {
			        // may be zip or so
                    log("unknown format " + format);
			    }
            }
        }

        for(Map.Entry<Resource, String> entry : hash.entrySet()) {
            Resource obj = entry.getKey();
            //log("rename " + obj.getURI() + " to " + entry.getValue());
            obj = ResourceUtils.renameResource(obj, entry.getValue()); 
        }
        hash.clear();

		String cover = "cover.png";
        if (name.startsWith("Journal")) {
			cover = fname.substring(0,fname.indexOf(".rdf")) + ".png";
        }
		if (rc.hasProperty(FOAF.img)) {
		    String source = rc.getProperty(FOAF.img).getString();
            if (Files.isRegularFile(path.resolveSibling(cover))) {
                // log(" file exists: " + cover); 
			} else {
                FileUtil.copyIfExists(source, path.resolveSibling(cover));
			}
            rc.removeAll(FOAF.img);
		}
        //do not add cover if we could not copy one ?
        Path base = store.relativize(path.resolveSibling(cover));
        rc.addProperty(FOAF.img, uri + "/" + base);
        if (name.equals("JournalIssue")) {
            log(" write parts : " + rc.getURI() + " " + path); 
            ModelUtil.write(path, rc);
        }
        return true;
    }

    private void log(Exception e) {
        logger.info(e.toString());
    }

    private void log(String msg) {
        logger.info(msg);
    }

    private static final Logger logger =
                         Logger.getLogger(ArchiveAnalyzer.class.getName());

}

