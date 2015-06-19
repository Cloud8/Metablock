package org.shanghai.oai;

import org.shanghai.crawl.MetaCrawl.Analyzer;
import org.shanghai.oai.URN;
import org.shanghai.util.FileUtil;
import org.shanghai.util.Language;
import org.shanghai.util.PrefixModel;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.io.File;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

public class NLMAnalyzer implements Analyzer {

    private DocumentBuilderFactory docfactory;

    private String store;
    private URN urn;
    private Language language;
    private int count;
    private HashMap<String,String> hash;

    Property title;
    Property identifier; // dct
    Property created;    // dct
    Property modified;   // dct
    Property issueId;    // fabio:hasIssueIdentifier -- issue identifier
    //Property number;     // fabio:hasSequenceIdentifier -- issue sequence
    //Property volume;     // fabio:hasVolumeIdentifier -- issue volume
    Property article;  // fabio:hasElectronicArticleIdentifier
    Property type;       // rdf
    Property hasURL;     // fabio:hasURL

    public NLMAnalyzer(String prefix, String store) {
        urn = new URN(prefix);
        this.store = store;
    }

    @Override
    public Analyzer create() {
        docfactory = DocumentBuilderFactory.newInstance();
        docfactory.setNamespaceAware(true);
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

    private void createProperties(Model model) {
        title = model.createProperty(dct, "title");
        identifier = model.createProperty(dct, "identifier");
        created = model.createProperty(dct, "created");
        modified = model.createProperty(dct, "modified");
        issueId = model.createProperty(fabio, "hasIssueIdentifier");
        //number = model.createProperty(fabio, "hasSequenceIdentifier");
        //volume = model.createProperty(fabio, "hasVolumeIdentifier");
        hasURL = model.createProperty(fabio, "hasURL");
        type = model.createProperty(rdf, "type");
        article = model.createProperty(fabio, "hasElectronicArticleIdentifier");
        //log("analyze " + id);
    }

    @Override
    public Resource analyze(Model model, String id) {
        Resource rc = null;
        Resource rci = null;
        createProperties(model);
        ResIterator ri = model.listResourcesWithProperty(type);
        while( ri.hasNext() ) {
            Resource object = ri.nextResource();
            String name = object.getPropertyResourceValue(type).getLocalName();
            if (name.equals("JournalArticle")) {
                rc = object;
		        makeIdentifier(rc);
                language.analyze(model, rc, id);
            } else if (name.equals("JournalIssue")) {
                rci = object;
		        makeIdentifier(rci);
            } else if (name.equals("Journal")) {
		        makeIdentifier(object);
            }
        }
        boolean b = false;
        if (store==null) { // no writes from here
            // log("analyze " + id + " " + rc.getURI());
        } else if (store.equals("files") && rci!=null) { // help parent ??
            if (!hash.containsKey(rci.getURI())) {
                Model sub = getPartOf(rci);
                b = writeParent(sub, rci, id);
                hash.put(rci.getURI(), rci.getURI());
            }
        } else { 
	        b = writeModel(model, rc, store); // write articel
            if (b && rci!=null && !hash.containsKey(rci.getURI())) {
                Model sub = getPartOf(rci);
                b = writeModel(sub, rci, store); // write issue
                hash.put(rci.getURI(), rci.getURI());
            }
            count++;
        }
        if (store.equals("files")) {
            //archive index files
            //writeIndex(model, rc, id);
        }
        //if (b) log(" issue " + id);
        return rc;
    }

    @Override
    public boolean test() {
        log("test: " + this.getClass().getName());
        return true;
    }

    @Override
    public Resource test(Model model, String id) {
        log("test # " + id + " " + store);
        createProperties(model);
        Resource rc = null;
        ResIterator ri = model.listResourcesWithProperty(type);
        while( ri.hasNext() ) {
            rc = ri.nextResource();
            String name = rc.getPropertyResourceValue(type).getLocalName();
            if (name.equals("JournalArticle")) {
			    makeIdentifier(rc);
                language.analyze(model, rc, id);
                break;
            }
        }
        return rc;
    }

    @Override
    public void dump(Model model, String id, String fname) {
        log("dump not implemented");
    }

    public void makeIdentifier(Resource rc) {
        if (rc.hasProperty(identifier) ) {
            String id = rc.getProperty(identifier).getString();
            if (id==null || id.length()==0) {
                rc.removeAll(identifier);
			    id = urn.getUrn(rc.getURI());
			    rc.addProperty(identifier, id);
            }
            //log("analyzing " + rc.getURI() + "[" + id + "]");
        } else {
			String id = urn.getUrn(rc.getURI());
            if (id==null) {
                log("URN failed : " + rc.getURI() + " " + id);
                id = rc.getURI().replaceAll("[^a-zA-Z0-9\\:\\.]","");
            } 
            //log("makeIdentifier " + rc.getURI() + "[" + id + "]");
			rc.addProperty(identifier, id);
        }
    }

    private boolean checkTime(String path) {
        boolean write = false;
        File check = new File(path);
        if (check.exists()) {
            //int days = (int)((System.currentTimeMillis() 
            //           - check.lastModified())/(1000*60*60*24));
            int modified = (int)((System.currentTimeMillis() 
                         - check.lastModified())/(1000*60));
			if (modified > 2) {
                write = true;
                // log("write " + path + " " + modified);
           }
        } else {
           write = true;
        }
        return write;
    }

    /** write issue resource description */
    private boolean writeParent(Model model, Resource rc, String resource) {
        if ( (new File(resource)).exists() ) { // archive issue
            String path = null;
            if (resource.lastIndexOf("/")>0) {
                path = resource.substring(0, resource.lastIndexOf("/"));
                path = path.substring(0, path.lastIndexOf("/")) + "/about.rdf";
            } else {
                return false;
            }
            if (checkTime(path)) {
                log("write " + path);
                StringWriter writer = new StringWriter();
                model.write(writer, "RDF/XML-ABBREV");
                FileUtil.write(path, writer.toString());
                return true;
            }
        }
        return false;
    }

    /** archive index page to file system */
    private boolean writeIndex(Model model, Resource rc, String fname) {
        String path = fname.substring(0, fname.lastIndexOf("/"));
        // archive index page
        if (rc.hasProperty(hasURL)) {
            String url = rc.getProperty(hasURL).getString();
            //log("write index to " + path + "/index.html");
            FileUtil.copy(url, path + "/index.html");
            return true;
        } else {
            log("no hasURL " + path);
            return false;
        }
    }

    /** write model files to file system */
    private boolean writeModel(Model model, Resource rc, String store) {
        boolean write = false;
	    if (store==null) {
		    return false;
		}
        if ( rc.hasProperty(identifier) ) {
            String name = rc.getPropertyResourceValue(type).getLocalName();
            String path = store;
            String id = rc.getProperty(identifier).getString();
            if (rc.hasProperty(created)) {
                path += "/" + rc.getProperty(created).getString();
            }
            if (rc.hasProperty(issueId)) {
                path += "/" + rc.getProperty(issueId).getString();
                //log("issue " + rc.getProperty(issueId).getString());
                if (!new File(path).exists()) {
                    new File(path).mkdirs();
                }
            }
            if (rc.hasProperty(article)) {
                path += "/" + rc.getProperty(article).getString();
                //log("number " + rc.getProperty(article).getString());
            } 
            //log("writeModel to " + path);
            if (name.equals("JournalIssue")) {
                path += "/about.rdf";
                write = checkTime(path);
            } else if (name.equals("JournalArticle")) {
                path += "/" + rc.getProperty(article).getString()+".rdf";
                write = true;
            }
            if (write) {
                StringWriter writer = new StringWriter();
                model.write(writer, "RDF/XML-ABBREV");
                write = FileUtil.write(path, writer.toString());
                if (write) log("write " + name + " : " + path );
            }
        } else {
            log("complicated [" + rc.getURI() + "]");
        }
        return write;
    }

    private Model getPartOf(Resource rc) {
        Model model = PrefixModel.create();
        StmtIterator si = rc.listProperties();
        while( si.hasNext() ) {
            Statement stmt = si.nextStatement();
            model.add(stmt);
            if (stmt.getObject().isResource()) {
                StmtIterator sub = stmt.getObject()
                                       .asResource().listProperties();
                while( sub.hasNext() ) {
                    model.add(sub.nextStatement());
                }
            }
        }
        return model;
    }

    private void log(Exception e) {
        logger.info(e.toString());
    }

    private void log(String msg) {
        logger.info(msg);
    }

    private static final Logger logger =
                         Logger.getLogger(NLMAnalyzer.class.getName());

}

