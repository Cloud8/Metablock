package org.shanghai.store;

import java.util.logging.Logger;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.Statement;

import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.base.file.Location;
import org.apache.jena.update.UpdateAction;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdfxml.xmlinput.JenaReader;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A Jena TDB Reader
   @date 2013-01-16
*/
public class JenaTDB {

    private static String tdbData;

    private Model model;
    private Dataset dataset;
    private int count = 0;

    private Location location;
    private String graph;
    private static boolean destroyed;

    public JenaTDB(final String tdbData) {
        this.tdbData = tdbData;
    }

    public JenaTDB(final String tdbData, String uri) {
        this.tdbData = tdbData;
        this.graph = uri;
    }

    public void create() {
        if (tdbData==null) {
            log("something is terribly wrong: no valid tdb source.");
        }
        if (model==null) {
            dataset = TDBFactory.createDataset(tdbData) ;
            if (graph==null) {
                log("init " + tdbData);
		        model = dataset.getDefaultModel();
            } else {
                log("init " + tdbData + " graph " + graph);
		        model = dataset.getNamedModel(graph);
            }
        }
        destroyed = false;
    }

    public synchronized void dispose() {
        if (destroyed) return; // only close tdb once
        destroyed = true;
        if (count==0) {
            return;
        }
        if (dataset==null || model==null) {
            return;
        }
        log("closing " + tdbData + " [" + count + "]");
        if (model!=null) {
            model.commit();
            model.close();
        }
        model=null;
        if (dataset!=null) {
            dataset.close();
        }
        dataset=null;
    }

    public void clean() {
        log("cleaning");
        model.begin();
        String action = "DELETE WHERE { ?s ?p ?o . }";
        UpdateAction.parseExecute(action, model);
        model.commit();
    }

    public QueryExecution getExecutor(String q) {
        //if (count==0) log(q);
        count++;
        try {
            Query query = QueryFactory.create(q);
            return QueryExecutionFactory.create(query, model);
        } catch(QueryParseException e) {
            log(e);
            log("tragedy " + count + " query [" + q + "]"); 
            e.printStackTrace();
        }
        return null;
    }

    private boolean execute(String action) {
        try {
            UpdateAction.parseExecute(action, model);
        } catch(QueryParseException e) {
            log("execute " + action);
            throw e;
        }
        return true;
    }

    public boolean delete(String about) {
        log("delete [" + about + "]");
        model.begin();
        boolean b = execute("DELETE WHERE { <" + about + "> ?p ?o. }");
        model.commit();
        return b;
    }

    public boolean remove(Model m) {
        model.remove(m);
        return true;
    }

    public boolean remove(Model m, String path) {
        return remove(m);
    }

    public boolean update(Model m) {
        boolean b=false;
        StmtIterator si = m.listStatements();
        while(si.hasNext()) {
           Statement st = si.nextStatement();
           model.removeAll(st.getSubject(),st.getPredicate(),null);
           b=true;
        }
        model.add(m);
        return b;
    }

    public boolean save(Model m) {
        count++;
        model.begin();
        model.add(m);
        model.commit();
        return true;
    }

    /**
    public boolean save(Model m) {
        ResIterator ri = m.listSubjects();
        if (!ri.hasNext()) {
            StringWriter out = new StringWriter();
            m.write(out, "TURTLE");
            log("ERROR: " + out.toString());
            return false;
        }
        Resource r = ri.nextResource();
        while (ri.hasNext() && r.isAnon()) {
            r = ri.nextResource();
        }
        model.begin();
        if (!r.isAnon()) 
            execute("DELETE WHERE { <" + r.toString() + "> ?p ?o. }");
        model.add(m);
        model.commit();
        return true;
    }
    **/

    private static final Logger logger =
                         Logger.getLogger(JenaTDB.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        //log(e.toString());
        e.printStackTrace();
    }

}
