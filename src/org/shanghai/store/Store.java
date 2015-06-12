package org.shanghai.store;

import org.shanghai.util.PrefixModel;

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
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Property;

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

    /** four store support */
    public Store(String uri, String kb, String name) {
        this.tripleStore = new TripleStore(uri, kb, name);
        tdb = true;
    }

    /** virtuoso write support */
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
        Model mod = ModelFactory.createDefaultModel();
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
        Model m = getModel(resource);
        return PrefixModel.prefix(m);
        //return m;
        //return sort(m, resource);
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

    /** obsolete : topological sort */
    /**
    private static final String dct = DCTerms.getURI();
    private static final String prism =
                         "http://prismstandard.org/namespaces/basic/2.0/";
    private static final String fabio = "http://purl.org/spar/fabio/";
    private static final String foaf = "http://xmlns.com/foaf/0.1/";
    private static final String aiiso = "http://purl.org/vocab/aiiso/schema#";
    private static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private Model sort(Model m, String resource) {
        //return m; 
        Model model = ModelFactory.createDefaultModel();
        Resource rc = m.getResource(resource);
        RDFNode node = null;
        Property prop = null;
        StmtIterator si = m.listStatements(rc, prop, node);
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
    **/

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
