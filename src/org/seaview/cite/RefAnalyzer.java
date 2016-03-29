package org.seaview.cite;

import org.shanghai.crawl.MetaCrawl.Analyzer;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.util.FileUtil;
import org.shanghai.util.ModelUtil;
import org.shanghai.util.TextUtil;

import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.Update;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PropertyNotFoundException;
import java.util.logging.Logger;
import java.nio.file.Paths;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @title Inserts isReferencedBy statements into referenced Resource
    @date 2015-05-27
*/
public class RefAnalyzer implements Analyzer {

    protected static final String c4o = "http://purl.org/spar/c4o/";
    protected boolean test = false;

    private static final String voidNS = "http://rdfs.org/ns/void#";
    private int ref_count = 0; // all references
    private int uri_count = 0; // with external uri
    private int doi_count = 0; // with doi
    private int loc_count = 0; // local virtual uri
    private int rdf_count = 0; // with rdf data 
    private int upd_count = 0; // update 
    private int nop_count = 0; // documents with no references
    private int doc_count = 0; // documents with found references
    private int all_count = 0; // all documents

    @Override
    public Analyzer create() {
        return this;
    }

    @Override
    public void dispose() {
        log("ref: " + ref_count + " uri: " + uri_count + " doi: " + doi_count
         + " loc: " + loc_count + " rdf: " + rdf_count //+ " upd: " + upd_count 
         + " nop: " + nop_count + " doc: " + doc_count + " all: " + all_count
         + " " + (100*doc_count/all_count) + "%");
    }

    @Override
    public Resource analyze(Resource rc) {
        if (rc.hasProperty(DCTerms.references)) {
            //log("reference analyze " + rc.getURI());
            Seq seq = rc.getProperty(DCTerms.references).getSeq();
            if (seq!=null && seq.size()>0) {
                for (int i = 1; i<= seq.size(); i++) {
                    try {
                        analyzeReference(rc, seq.getResource(i));
                    } catch (PropertyNotFoundException e) { log(e); }
                    finally {}
                }
                doc_count++;
            } else {
                nop_count++;
            }
        } else {
            nop_count++;
        }
        all_count++;
        return rc;
    }

    @Override
    public String probe() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Resource test(Resource rc) {
        test = true;
        return analyze(rc);
    }

    protected void analyzeReference(Resource rc, Resource obj) {
        cleanRef(rc, obj);
        ref_count++;
        String uri = obj.getURI();
        if (uri==null) {
            log("bad reference " + ref_count); 
        } else if (uri.startsWith("http://localhost/")) {
            loc_count++;
        } else if (uri.contains("doi.org")) {
            doi_count++;
        } else if (uri.contains("uni-marburg.de")) {
            uri_count++;
            rdf_count++;
            log("repository [" + uri + "]"); 
            if (test) {
                modelUpdate(rc, obj);
            }
        } else if (uri.contains("uni-")) {
            uri_count++;
            log("repository [" + uri + "]"); 
        } else if (uri.contains("econstor")) {
            uri_count++;
            log("econstor [" + uri + "]"); 
        } else {
            uri_count++;
        }
    }

    /** update obj with a dct:isReferencedBy statemement */
    private void modelUpdate(Resource rc, Resource obj) {
        String sparql = findService(obj);
        if (sparql==null) {
            log("no sparql service found");
            return;
        }
        sparqlUpdate(sparql, obj.getURI(), rc.getURI());
    }

    /** find sparql service */
    private String findService(Resource rc) {
        if (rc.getURI().startsWith("http://archiv.ub.uni-marburg.de")) {
            upd_count++;
            return "http://localhost:8890/sparql"; // dev test
        }

        String sparql = null;
        Property inDataset = rc.getModel().createProperty(voidNS, "inDataset");
        Property sp = rc.getModel().createProperty(voidNS, "sparqlEndpoint");

        if (rc.hasProperty(inDataset)) {
            String uri = rc.getProperty(inDataset).getString();
            Model model = ModelFactory.createDefaultModel().read(uri);
            Resource ds = model.getResource(uri);
            if (ds.hasProperty(sp)) {
                sparql = ds.getProperty(sp).getResource().getURI();
            }
        }
        return sparql;
    }

    private void sparqlUpdate(String sparql, String uri, String resource) {
        String upd = "PREFIX dcterms: <http://purl.org/dc/terms/>\n"
                   + "{ <" + uri + "> dcterms:isReferencedBy " 
                   + "<" + resource + "> . }"
                   + "\n";
        log(sparql + "\n[" + upd + "]");
        Dataset dataset = DatasetFactory.create(sparql);
        UpdateRequest request = UpdateFactory.create() ;
        request.add(upd);
        UpdateAction.execute(request, dataset) ;
    }

    /** plausabilty ckeck for a referenced resource, 
        to return better citation string if possible */
    private char last = ' ';
    private String cleanRef(Resource rc, Resource obj) {
       String cite = null;
       String text = obj.getProperty(DCTerms.bibliographicCitation).getString(); 
       if (text==null) {
           log("zero reference " + obj.getURI());
           return null;
       }
       char current = text.charAt(0);
       if (Character.isLetter(current)) {
           // current = text.charAt(0);
       } else if (Character.isDigit(current)) {
           text = cleanRef(text, obj);
           current = text.charAt(0);
       } else if (current=='•') {
           text = cleanRef(text, obj);
           current = text.charAt(0);
       } else if (!Character.isAlphabetic(current)) {
           text = cleanRef(text, obj);
           current = text.charAt(0);
       } else if (current=='„') {
       } else if (current=='[' && text.contains("]")) {
           cite = text.substring(0, text.indexOf("]"));
       } else if (current=='(' && text.contains(")")) {
           cite = text.substring(0, text.indexOf(")"));
       } else {
           log("bad reference [" + text + "]");
       }
       if (current < last) {
           // log("bad order [" + text + "]" + current + ":" + last);
       }
       last = current;
       return cite;
    }

    private String cleanRef(String text, Resource obj) {
        int x = 0;
        for (x = 0; x < text.length(); x++) {
            if (Character.isLetter(text.charAt(x))) {
                break;
            }
        }
        text = text.substring(x);
        if (text.length()>0) {
            obj.removeAll(DCTerms.bibliographicCitation);
            obj.addProperty(DCTerms.bibliographicCitation, text);
            return text;
        }
        return " ";
    }

    private static final Logger logger =
                         Logger.getLogger(RefAnalyzer.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        logger.info(e.toString());
    }

}
