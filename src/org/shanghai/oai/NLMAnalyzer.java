package org.shanghai.oai;

import org.shanghai.crawl.MetaCrawl.Analyzer;
import org.shanghai.oai.URN;
import org.shanghai.util.FileUtil;
import org.shanghai.util.Language;

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
import javax.xml.transform.dom.DOMSource;

import java.util.logging.Logger;

public class NLMAnalyzer implements Analyzer {

    private static final String dct = DCTerms.getURI();
    private static final String prism = 
                         "http://prismstandard.org/namespaces/basic/2.0/";
    private static final String fabio = "http://purl.org/spar/fabio/";
    private static final String foaf = "http://xmlns.com/foaf/0.1/";
    private static final String aiiso = "http://purl.org/vocab/aiiso/schema#";
    private DocumentBuilderFactory docfactory;

    private String store;
    private URN urn;
    private Language language;

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
        return this;
    }

    @Override
    public void dispose() {
        language.dispose();
    }

    @Override
    public void analyze(Model model, Resource rc) {
        Property title = model.createProperty(dct, "title");
        Property identifier = model.createProperty(dct, "identifier");
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
            }
			rc.addProperty(identifier, id);
        }
        language.analyze(model, rc);
         
        if (store!=null) {
            writeNLM(model);
        }
    }

    @Override
    public void analyze(Model model) {
        Property title = model.createProperty(dct, "title");
        ResIterator ri = model.listResourcesWithProperty(title);
        while( ri.hasNext() ) {
            Resource rc = ri.nextResource();
            analyze(model, rc);
        }
    }

    public String archive(String xml) {
        String path = store;
        if (path==null || ! new File(path).isDirectory()) {
            return null;
        }

        NLM nlm = new NLM(xml);
        if (nlm.year!=null)
            path += "/" + nlm.year;
        if (nlm.issue!=null) // volume is another option
            path += "/" + nlm.issue;
        if (nlm.article!=null)
            path += "/" + nlm.article;
        File check = new File(path);
        if (!check.exists())
            if (!new File(path).mkdirs())
                return path;
        if (nlm.url!=null) {
            FileUtil.copy(nlm.url, path + "/index.html");
            String from = nlm.url.replace("view", "viewFile"); // OJS
            //log("read [" + from + "]");
            FileUtil.copy(from, path + "/" + nlm.article + ".pdf");
        }
        if (nlm.article!=null) {
            FileUtil.write(path + "/" + nlm.article + ".nlm", xml);
            log("wrote " + path + "/" + nlm.article + ".nlm");
        } else {
            //log(nlm.toString());
            log("failed " + path + "/" + nlm.article + ".nlm");
        }
        return path;
    }

    /** write model files to file system */
    private boolean writeNLM(Model model) {
        Property title = model.createProperty(dct, "title");
        Property identifier = model.createProperty(dct, "identifier");
        Property created = model.createProperty(dct, "created");
        Property issue = model.createProperty(prism, "issueIdentifier");
        Property number = model.createProperty(prism, "number");

        ResIterator ri = model.listResourcesWithProperty(identifier);
        Statement stmt;
        StringWriter writer;
        while( ri.hasNext() ) {
            Resource rc = ri.nextResource();
            if ( rc.hasProperty(identifier) ) {
                String path = store;
                String id = rc.getProperty(identifier).getString();
                if (rc.hasProperty(created)) {
                    if (path.endsWith(rc.getProperty(created).getString())) {
                        //log("created " + rc.getProperty(created).getString());
                    } else {
                        path += "/" + rc.getProperty(created).getString();
                    }
                } else {
                    String about = path + "/about.rdf";
                    if (!new File(about).exists()) {
                        log("Journal " + about + " "
                                       + rc.getProperty(title).getString());
                        Model sub = getPartOf(rc);
                        writer = new StringWriter();
                        sub.write(writer, "RDF/XML-ABBREV");
                        FileUtil.write(about, writer.toString());
                    }
                }
                if (rc.hasProperty(issue)) {
                    path += "/" + rc.getProperty(issue).getString();
                    //log("issue " + rc.getProperty(issue).getString());
                    if (!new File(path).exists()) {
                        new File(path).mkdirs();
                    }
                }
                if (rc.hasProperty(number)) {
                    path += "/" + rc.getProperty(number).getString();
                    //path += "/" + rc.getProperty(number).getString() + ".rdf";
                    path += "/about.rdf";
                    //log("number " + rc.getProperty(number).getString());
                } else {
                    //write issue description
                    String about = path + "/about.rdf";
                    //log("about " + about);
                    if (!new File(about).exists()) {
                        Model sub = getPartOf(rc);
                        writer = new StringWriter();
                        sub.write(writer, "RDF/XML-ABBREV");
                        FileUtil.write(about, writer.toString());
                    }
                }
                //if (path.equals(store)) {
                //    path = path + "/" + id + ".rdf";
                //}
                //log("write " + path);
                writer = new StringWriter();
                model.write(writer, "RDF/XML-ABBREV");
                FileUtil.write(path, writer.toString());
            } else {
                log("complicated [" + rc.getURI() + "]");
                //String id = urn.getUrn(rc.getURI());
                //rc.addProperty(identifier, id);
            }
        }
        return true;
    }

    private Model getPartOf(Resource rc) {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("dct", dct);
        model.setNsPrefix("fabio", fabio);
        model.setNsPrefix("foaf", foaf);
        model.setNsPrefix("aiiso", aiiso);
        model.setNsPrefix("prism", prism);
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

    public class NLM {
        String journal;
        String year;
        String issue;
        String volume;
        String article;
        String url;

        public NLM(String xml) {
            Document doc = null;
            DocumentBuilder db = null;
            try {
                db = docfactory.newDocumentBuilder();
                InputSource is = new InputSource(new StringReader(xml));
                doc = db.parse(is);
            } catch(SAXException e) { log(e); }
              catch(IOException e) { log(e); }
              catch(ParserConfigurationException e) { log(e); }
            NodeList nodes = doc.getElementsByTagName("journal-id");
            journal = nodes.getLength()==0?null:nodes.item(0).getTextContent();
            nodes = doc.getElementsByTagName("year");
            year = nodes.getLength()==0?null:nodes.item(0).getTextContent();
            nodes = doc.getElementsByTagName("volume");
            volume = nodes.getLength()==0?null:nodes.item(0).getTextContent();
            nodes = doc.getElementsByTagName("issue-id");
            issue = nodes.getLength()==0?null:nodes.item(0).getTextContent();
            nodes = doc.getElementsByTagName("article-id");
            article = nodes.getLength()==0?null:nodes.item(0).getTextContent();
            if (article!=null) {
                nodes = doc.getElementsByTagName("self-uri");
                for (int j = 0; j < nodes.getLength(); j++) {
                    Element el = (Element)nodes.item(j);
                    if (el.hasAttribute("content-type")) {
                        String str = el.getAttribute("content-type");
                        if ( str.equals("application/pdf")) {
                             url = el.getAttribute("xlink:href");
                        }
                    }
                }
            }
        }

        public String toString() {
            return journal + " " + year + " " + volume + " " + issue
                   + " " + article + " " + url;
        }
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

