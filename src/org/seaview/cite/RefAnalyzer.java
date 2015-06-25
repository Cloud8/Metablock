package org.seaview.cite;

import org.shanghai.crawl.MetaCrawl.Analyzer;
import org.shanghai.crawl.MetaCrawl.Storage;
import org.shanghai.crawl.MetaCrawl.Transporter;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.util.FileUtil;
import org.shanghai.util.PrefixModel;
import org.seaview.data.AbstractAnalyzer;
import org.seaview.data.DOI;

import org.apache.jena.riot.system.IRIResolver;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import java.util.logging.Logger;

/**
    (c) reserved.
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Inserts isReferencedBy statements into referenced Model
    @date 2015-05-27
*/
public class RefAnalyzer extends AbstractAnalyzer {

    protected Property references;
    protected Property bibliographicCitation;
    protected Property title;
    protected Property creator;
    protected Property date;
    protected Property aggregates;
    protected Property language;
    protected Property isReferencedBy;

    private int ref_count = 0; // all references
    private int uri_count = 0; // with external uri
    private int doi_count = 0; // with doi
    private int loc_count = 0; // local virtual uri
    private int rdf_count = 0; // with rdf data behind uri
    private int upd_count = 0; // update 
    private int nop_count = 0; // documents with no references
    private int doc_count = 0; // documents with found references
    private int all_count = 0; // all documents

    private boolean test = false;

    //private Transporter transporter;
    //private Storage storage;

    //public RefAnalyzer() {}

    //public RefAnalyzer(Transporter transporter, Storage storage) {
    //    this.transporter = transporter;
    //    this.storage = storage;
    //    log("transporter " + transporter.getClass().getName());
    //    log("storage " + storage.getClass().getName());
    //}

    @Override
    public AbstractAnalyzer create() {
        return this;
    }

    @Override
    public void dispose() {
        log("ref: " + ref_count + " uri: " + uri_count + " doi: " + doi_count
         + " loc: " + loc_count + " rdf: " + rdf_count + " upd: " + upd_count 
         + " nop: " + nop_count + " doc: " + doc_count + " all: " + all_count
         + ": " + (100*doc_count/all_count) + "%");
    }

    @Override
    public void analyze(Model model, Resource rc, String id) {
        if (rc==null) {
            log("reference analyze with zero resource [" + id + "]");
            return;
        }
        references = model.createProperty(dct, "references");
        bibliographicCitation = model
                                .createProperty(dct, "bibliographicCitation");
        title = model.createProperty(dct, "title");
        creator = model.createProperty(dct, "creator");
        date = model.createProperty(dct, "date");
        language = model.createProperty(dct, "language");
        aggregates = model.createProperty(ore, "aggregates");
        isReferencedBy = model.createProperty(dct, "isReferencedBy");
        if (rc.hasProperty(references)) {
            //log("reference analyze " + rc.getURI());
            Seq seq = rc.getProperty(references).getSeq();
            if (seq!=null && seq.size()>0) {
                for (int i = 1; i< seq.size(); i++) {
                    try {
                        analyzeReference(model, rc, seq.getResource(i));
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
        //if (test) {
        //    return;
        //} else if (storage==null) {
        //    // no storage to write to
        //} else {
        //    // storage.write(model, id);
        //}
    }

    protected void analyzeReference(Model model, Resource rc, Resource obj) {
        cleanRef(rc, obj);
        ref_count++;
        String uri = obj.getURI();
        if (uri==null) {
            log("bad reference " + ref_count); 
        } else if (uri.startsWith("http://localhost/")) {
            loc_count++;
            // this breaks the model:
            // model.add(model.createStatement(obj, isReferencedBy, rc));
        } else if (uri.contains("doi")) {
            doi_count++;
            //if (!test) {
            //    boolean b = testModel(uri);
            //    if (b) {
            //        rdf_count++;
            //        log("linked data uri: " + uri);
            //    }
            //}
        } else if (uri.contains("uni-marburg.de")) {
            uri_count++;
            rdf_count++;
            log("repository [" + uri + "]"); 
            if (!test) updateRemote(uri, rc.getURI());
        } else if (uri.contains("uni-")) {
            uri_count++;
            log("repository [" + uri + "]"); 
            // if (!test) updateRemote(uri, rc.getURI());
        } else if (uri.contains("econstor")) {
            uri_count++;
            log("econstor [" + uri + "]"); 
            // if (!test) updateRemote(uri, rc.getURI());
        } else {
            uri_count++;
            // if (!test) updateRemote(uri, rc.getURI());
        }
    }

    @Override
    public boolean test() {
        String id = "http://doi.org/10.17192/z2015.0047";
        Model model = PrefixModel.retrieve(id);
        if (model==null) {
            return false;
        } else {
            log(model);
        }
        String uri = DOI.resolve(id);
        //String uri = "http://archiv.ub.uni-marburg.de/diss/z2015/0047";
        log("resolved " + id + " " + uri); 
        model = PrefixModel.retrieve(uri);
        if (model==null) {
            return false;
        }
        Resource rc = findResource(model, uri);
        test = true;
        log("analyze " + rc.getURI()); 
        //log(model);
        analyze(model, rc, uri);
        return true;
    }

    @Override
    public Resource test(Model model, String id) {
        Resource rc = findResource(model, id);
        test = true;
        analyze(model, rc, id);
        return rc;
    }

    /*
    private void updateModel(Model model, String uri, String resource) {
        Resource rc = model.createResource(uri);
        Resource obj = model.createResource(resource);
        Property prp = model.createProperty(dct, "isReferencedBy");
        model.add(model.createStatement(rc, prp, obj));
        storage.delete(uri);
        log("write " + uri);
        storage.write(model, uri);
    }
    */

    private boolean testModel(String uri) {
        Model model = PrefixModel.retrieve(uri);
        if (model==null) {
            return false;
        }
        //log(model);
        return true;
    }

    /*
    private void updateModel(String uri, String resource) {
        if (transporter==null || storage==null) {
            return;
        }
        Model model = transporter.read(uri);
        if (model==null) {
            // problem: file transporter does not read uris
            model = PrefixModel.retrieve(uri);
        }
        if (model==null) {
            log("no rdf " + uri);
        } else if (test) {
            rdf_count++;
            log("update test " + uri + " : " + resource);
        } else {
            rdf_count++;
            updateModel(model, uri, resource);
        }
    }
    */
 
    /*
    private void updateStorage(Model model, Resource rc, Resource obj) {
        //storage.delete(rc.getURI());
        model.add(model.createStatement(obj, isReferencedBy, rc));
        //log("write " + rc.getURI());
        storage.write(model, rc.getURI());
    }
    */
 
    /** uri is referenced by resource */
    private void updateRemote(String uri, String resource) {
        Model model = PrefixModel.retrieve(uri);
        if (model==null) {
        //    // no rdf available
        } else {
            log(uri + " is referenced by " + resource); 
            String sparql = findService(model, uri);
            if (sparql!=null) {
                sparqlUpdate(sparql, uri, resource);
            }
        }
    }

    /** look up void dataset for this model and see if sparql is supported */
    private String findService(Model model, String uri) {
        if (uri.startsWith("http://archiv.ub.uni-marburg.de")) {
            upd_count++;
            return "http://localhost:8890/sparql"; // GH201506 dev test
        }
        String sparql = null;
        Property sp = model.createProperty(voidNS, "sparqlEndpoint");
        ResIterator ri = model.listSubjectsWithProperty(sp);
        if (ri.hasNext()) {
            upd_count++;
            sparql = ri.nextResource().getProperty(sp).getResource().getURI();
        } else {
            Property inSet = model.createProperty(voidNS, "inDataset");
            NodeIterator ni = model.listObjectsOfProperty(inSet);
            if(ni.hasNext()) {
                String dataset = ni.next().asResource().getURI();
                log("check dataset: " + dataset);
                return findService(model, dataset);
            }
        }
        return sparql;
    }

    private void sparqlUpdate(String service, String uri, String resource) {
        String graph = uri.substring(0, uri.indexOf('/', 8));
        String upd = "PREFIX dct: <http://purl.org/dc/terms/>\n"
                   + "INSERT DATA {\n GRAPH <" + graph + ">"
                   + "{ <" + uri + "> dct:isReferencedBy " 
                   + "<" + resource + "> . }\n}";
        //log("sparql: " + sparqlSrv + " update: [" + upd + "] " + graph); 
        log(service + "\n[" + upd + "]");
        //Dataset ds = DatasetFactory.create(graph);
        //GraphStore graphStore = GraphStoreFactory.create(ds) ;
        GraphStore graphStore = GraphStoreFactory.create() ;
        UpdateRequest request = UpdateFactory.create() ;
        request.add(upd);
        UpdateProcessor qexec=UpdateExecutionFactory
                             .createRemote(request, service);
        //UpdateAction.execute(request, graphStore) ;
        qexec.execute();
    }

    /** check some plausabilty for a referenced resource, 
        give back citation string if possible */
    private char last = ' ';
    private String cleanRef(Resource rc, Resource obj) {
       String cite = null;
       String text = obj.getProperty(bibliographicCitation).getString(); 
       if (text==null) {
           log("zero reference " + obj.getURI());
           return null;
       }
       // String char = text.substring(0,1);
       char current = text.charAt(0);
       if (Character.isLetter(current)) {
           // current = text.charAt(0);
       //} else if (Character.isValidCodePoint(current)) {
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
            obj.removeAll(bibliographicCitation);
            obj.addProperty(bibliographicCitation, text);
            return text;
        }
        return " ";
    }

    private void updateModelDoi(String doi, String uri) {
        // log("reference doi " + uri); 
        // Model model = PrefixModel.retrieve(doi);
        // log(model);
        String resolved = DOI.resolve(doi);
        if (resolved==null) {
            log("failed to resolve doi [" + doi + "]"); 
        }
    }

    private void validate(String url) {
        try {
            IRIResolver.validateIRI(url);
        } catch(Exception e) {
            log(e);
        }
    }

    private static final Logger logger =
                         Logger.getLogger(RefAnalyzer.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        //e.printStackTrace();
        logger.info(e.toString());
    }

    protected void log(Model mod) {
        mod.write(System.out, "RDF/XML-ABBREV");
    }

}
