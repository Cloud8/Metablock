package org.shanghai.oai;

import org.shanghai.crawl.MetaCrawl.Analyzer;
import org.shanghai.oai.URN;
import org.shanghai.util.FileUtil;
import org.shanghai.util.Language;
import org.shanghai.util.ModelUtil;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
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

    private static final String fabio = "http://purl.org/spar/fabio/";
    private DocumentBuilderFactory docfactory;

    private String store;
    private URN urn;
    private Language language;
    private int count;
    private HashMap<String,String> hash;

    Property issueId;    // fabio:hasIssueIdentifier -- issue identifier
    Property article;  // fabio:hasElectronicArticleIdentifier
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
        log("store " + store + " " + urn);
        return this;
    }

    @Override
    public void dispose() {
        language.dispose();
        hash.clear();
    }

    @Override
    public Resource analyze(Resource rc, String id) {
        Resource sub = null;
        createProperties(rc.getModel());
        String name = rc.getPropertyResourceValue(RDF.type).getLocalName();
        if (name.equals("JournalArticle")) {
		    makeIdentifier(rc);
            language.analyze(rc, id);
        }
        StmtIterator si = rc.listProperties(DCTerms.isPartOf);
        while( si.hasNext() ) {
            RDFNode node = si.nextStatement().getObject();
            if (node.isResource()) {
                Resource obj = node.asResource();
                name = obj.getPropertyResourceValue(RDF.type).getLocalName();
                if (name.equals("JournalIssue")) {
		            makeIdentifier(obj);
                    sub = getPartOf(obj);
                    sub.addProperty(DCTerms.hasPart, rc);
                }
            }
        }
        boolean b = false;
        if (store==null) { // no writes from here
            // log("analyze " + id + " " + rc.getURI());
        } else { 
	        b = writeModel(rc, store); // write article
            if (b && sub!=null && !hash.containsKey(sub.getURI())) {
                b = writeModel(sub, store); // write issue
                if (b) hash.put(sub.getURI(), sub.getURI());
            }
            count++;
        }
        return rc;
    }

    @Override
    public Resource test(Resource rc, String id) {
        log("test # " + id + " " + store);
        return rc;
    }

    private void createProperties(Model model) {
        issueId = model.createProperty(fabio, "hasIssueIdentifier");
        article = model.createProperty(fabio, "hasElectronicArticleIdentifier");
        hasURL = model.createProperty(fabio, "hasURL");
    }

    private void makeIdentifier(Resource rc) {
        if (rc.hasProperty(DCTerms.identifier) ) {
            String id = rc.getProperty(DCTerms.identifier).getString();
            if (id==null || id.length()==0) {
                rc.removeAll(DCTerms.identifier);
			    id = urn.getUrn(rc.getURI());
			    rc.addProperty(DCTerms.identifier, id);
            }
            //log("analyzing " + rc.getURI() + "[" + id + "]");
        } else {
			String id = urn.getUrn(rc.getURI());
            if (id==null) {
                log("URN failed : " + rc.getURI() + " " + id);
                id = rc.getURI().replaceAll("[^a-zA-Z0-9\\:\\.]","");
            } else {
                //log("makeIdentifier " + rc.getURI() + " [" + id + "]");
			    rc.addProperty(DCTerms.identifier, id);
            }
        }
    }

    // return a copy of the isPartOf Resource and set identifiers
    private Resource getPartOf(Resource rc) {
        Model model = ModelUtil.createModel();
        StmtIterator si = rc.listProperties();
        while( si.hasNext() ) {
            Statement stmt = si.nextStatement();
            if (stmt.getPredicate().getLocalName().equals("isPartOf")) {
                makeIdentifier(stmt.getResource());
                //log("make identifier " + stmt.getResource().getURI() 
                //     + " " + stmt.getResource().getProperty(
                //             DCTerms.identifier).getString());
            }
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

    /** archive index page to file system */
    private boolean writeIndex(Model model, Resource rc, String fname) {
        String path = fname.substring(0, fname.lastIndexOf("/"));
        if (rc.hasProperty(hasURL) && Files.isDirectory(Paths.get(path))) {
            String url = rc.getProperty(hasURL).getString();
            log("write index to " + path + "/index.html");
            FileUtil.copy(url, path + "/index.html");
            return true;
        } else {
            log("no hasURL " + path);
            return false;
        }
    }

    /** write model files to file system */
    private boolean writeModel(Resource rc, String store) {
        boolean write = false;
	    if (store==null) {
		    return write;
		}
        if ( rc.hasProperty(DCTerms.identifier) ) {
            String name = rc.getPropertyResourceValue(RDF.type).getLocalName();
            String path = store;
            String id = rc.getProperty(DCTerms.identifier).getString();
            if (rc.hasProperty(DCTerms.created)) {
                path += "/" + rc.getProperty(DCTerms.created).getString();
            }
            if (rc.hasProperty(issueId)) {
                path += "/" + rc.getProperty(issueId).getString();
            }
            if (rc.hasProperty(article)) {
                path += "/" + rc.getProperty(article).getString();
            } 
            if (name.equals("JournalIssue")) {
                path += "/about.rdf";
            } else if (name.equals("JournalArticle")) {
                path += "/" + rc.getProperty(article).getString()+".rdf";
            }
            StringWriter writer = new StringWriter();
            rc.getModel().write(writer, "RDF/XML-ABBREV");
            write = FileUtil.write(path, writer.toString());
            //log("write to " + path);
        } else {
            log("complicated [" + rc.getURI() + "]");
        }
        return write;
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

