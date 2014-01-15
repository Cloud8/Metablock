package org.shanghai.store;

import java.util.logging.Logger;
import java.net.URL;
import java.io.StringReader;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.rdf.arp.JenaReader;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QueryParseException;

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

    public Store(String sparqlService) {
        this.sparqlService = sparqlService;
        if (sparqlService.startsWith("http://")) {
            tdb = false;
         } else {
            tripleStore = new TripleStore(sparqlService);
            tdb = true;
         }
    }

    public Store(String sparqlService, String construct) {
        this(sparqlService);
        this.sparqlService = sparqlService;
        if (sparqlService.startsWith("http://")) {
            tdb = false;
            this.construct = construct;
         } else {//construct may be a graph uri
            if (construct!=null && construct.startsWith("http://")) {
                tripleStore = new TripleStore(sparqlService,construct);
                tdb = true;
            } else {
                tripleStore = new TripleStore(sparqlService);
                this.construct = construct;
                tdb = true;
            }
         }
    }

    /** virtuoso support */
    public Store(String uri, String graph, String dbuser, String dbpass) {
        this.tripleStore = new TripleStore(uri,graph,dbuser,dbpass);
        tdb = true;
    }

    public void create() {
        if (tdb)
            tripleStore.create();
        if (construct==null)
            construct = "CONSTRUCT { <subject> ?p ?o }";
    }

    public synchronized void dispose() {
        if (tdb)
            tdb=false;
        if (tripleStore!=null) {
            tripleStore.dispose();
            tripleStore=null;
        }
    }

    public boolean write(String rdf) {
        if (!tdb)
            return false;
        StringReader in = new StringReader(rdf);
        Model mod = ModelFactory.createDefaultModel();
        RDFReader reader = new JenaReader(); 
        reader.read(mod, in, null);
        in.close();
        return write(mod);
    }

    public boolean write(Model mod) {
        boolean b = false;
        if (tdb)
            b = tripleStore.write(mod);
        return b;
    }

    public Model read(String resource) {
        return getModel(resource);
    }

    public boolean update(String rdf) {
        int x = rdf.indexOf("rdf:about");
        if (x>0) {
            String about = rdf.substring(x+11,rdf.indexOf("\"",x+12));
            delete(about);
        }
        return write(rdf);
    }

    public boolean update(Model mod) {
        boolean b = false;
        if (tdb)
            b = tripleStore.update(mod);
        return b;
    }

    public boolean delete(String about) {
        if (tdb)
            return tripleStore.delete(about);
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
         } catch(Exception e) { log(query); log(e); }
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
        if (tdb)
            return tripleStore.getExecutor(query);
        try {
          Query q = QueryFactory.create(query);
          QueryExecution qexec = 
              QueryExecutionFactory.sparqlService(sparqlService, q);
          return qexec;
        } catch(QueryParseException e) {
          log(query);
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
