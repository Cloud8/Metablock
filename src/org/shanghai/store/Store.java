package org.shanghai.store;

import org.shanghai.util.ModelUtil;

import java.util.logging.Logger;
import java.net.URL;
import java.io.StringReader;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.rdfxml.xmlinput.JenaReader;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.riot.RiotException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.QueryParseException;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Storage for RDF data
    @date 2013-10-22
*/
public class Store {

    private String sparqlService;
    private TripleStore tripleStore;
    private String construct;
    private boolean tdb;

    public Store(String srv, String construct) {
        if (construct!=null && construct.startsWith("http://")) {
            //RDFStorage write only
            String graph = construct;
            log("Store " + srv + " " + graph);
            tripleStore = new TripleStore(srv,graph);
            tdb = true;
        } else {
            //RDFTransporter sparql read only
            this.construct = construct;
            if (srv.startsWith("http://")) {
                this.sparqlService = srv;
                tdb = false;
            } else {
                //TDB reading support
                tripleStore = new TripleStore(srv);
                tdb = true;
            }
        }
    }

    /** four store support 
     * @param uri String identifier
     * @param kb String knowledgebase
     * @param name String 
     */
    public Store(String uri, String kb, String name) {
        this.tripleStore = new TripleStore(uri, kb, name);
        tdb = true;
    }

    /** virtuoso write support 
     * @param uri String identifier
     * @param graph String 
     * @param dbuser String 
     * @param dbpass String 
     */
    public Store(String uri, String graph, String dbuser, String dbpass) {
        this.tripleStore = new TripleStore(uri,graph,dbuser,dbpass);
        tdb = true;
    }

    public void create() {
        if (tdb)
            tripleStore.create();
        if (construct==null) {
            construct = "CONSTRUCT { <subject> ?p ?o . }"
                      + " where { <subject> ?p ?o . }";
        }
    }

    public synchronized void dispose() {
        if (tdb) {
            tdb=false;
        }
        if (tripleStore!=null) {
            tripleStore.dispose();
            tripleStore=null;
        }
    }

    public boolean write(String rdf) {
        if (!tdb) {
            return false;
        }
        StringReader in = new StringReader(rdf);
        Model mod = ModelUtil.createModel();
        RDFReader reader = new JenaReader(); 
        reader.read(mod, in, null);
        in.close();
        return write(mod);
    }

    public boolean write(Model mod) {
        boolean b = false;
        if (tdb) {
            b = tripleStore.write(mod);
        }
        return b;
    }

    public Model read(String resource) {
        Model model = null;
        try {
            model = getModel(resource);
            model = ModelUtil.prefix(model);
        } catch (RiotException e) { 
            log(e); log(resource); 
        }
        return model;
    }

    public boolean update(Model mod) {
        boolean b = false;
        if (tdb)
            b = tripleStore.update(mod);
        return b;
    }

    public boolean delete(String resource) {
        //log("delete " + resource);
        boolean b = false;
        if (tdb) {
            // Model m = getModel(resource);
            // b = update(m); 
            b = tripleStore.delete(resource);
        }
        return false;
    }

    public boolean delete(Model mod) {
        if (tdb)
            return tripleStore.delete(mod);
        return false;
    }

    public void destroy() {
        if (tdb)
            tripleStore.clean();
    }

    public String probe(String query) {
        String result = null;
        QueryExecution qe = getExecutor(query);
        try {
            ResultSet results=qe.execSelect();
            QuerySolution soln = results.next();
            if (soln.contains("subject")) {
                result = soln.getLiteral("subject").getString();
            } else {
                result = soln.toString();
            }
        } catch( Exception e ) {
          log(query);
          log(e);
        }
        return result;
    }

    private Model getModel(String resource) {
        Model model = null;
        if ( !isValidURI(resource) ) {
            log("invalid resource URI !");
            // return model;
        } 
        String query=construct.replace("<subject>","<"+resource+">");
        QueryExecution qexec = getExecutor(query);
        try {
            model = qexec.execConstruct();
            if (model.isEmpty()) {
                log("empty model " + resource);
                return null;
            }
            //else log("size " + model.size());
         } catch(Exception e) { log("[" + query + "]"); log(resource); log(e); }
            finally {
            if (model!=null)
               qexec.close();
            return model;
         }
    }

    private boolean isValidURI(String uri) {
        final URL url;
        try {
            url = new URL(uri);
        } catch (Exception e1) {
            return false;
        }
        return "http".equals(url.getProtocol());
    }

    public QueryExecution getExecutor(String query) {
        if (tdb) {
            return tripleStore.getExecutor(query);
        }
        try {
          Query q = QueryFactory.create(query);
          QueryExecution qexec = 
              QueryExecutionFactory.sparqlService(sparqlService, q);
          return qexec;
        } catch(QueryParseException e) {
          log("[" + query + "]");
          log(e);
        }
        return null;
    }

    private static final Logger logger =
                         Logger.getLogger(Store.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        e.printStackTrace();
        log(e.toString());
    }

}