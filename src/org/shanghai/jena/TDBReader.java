package org.shanghai.jena;

import java.util.logging.Logger;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.update.UpdateAction;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.arp.JenaReader;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A Jena TDB Reader
   @date 2013-01-16
*/
public class TDBReader {

    private static String tdbData;
    private static final Logger logger =
                         Logger.getLogger(TDBReader.class.getName());

    private Model model;
    private Dataset dataset;
    private int count = 0;

    private Location location;
    private String graph;

    public TDBReader(String tdbData) {
        this.tdbData = tdbData;
    }

    public TDBReader(String tdbData, String uri) {
        this.tdbData = tdbData;
        this.graph = uri;
    }

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
    }

    public void create() {
        if (tdbData==null) 
            log("something is terribly wrong: no valid tdb source.");
        if (model==null) {
            location = new Location (tdbData);
            dataset = TDBFactory.createDataset(location) ;
            if (graph==null) {
                log("init " + tdbData);
		        model = dataset.getDefaultModel();
            } else {
                log("init " + tdbData + " graph " + graph);
		        model = dataset.getNamedModel(graph);
            }
        }
    }

    public void dispose() {
        if (model!=null) 
            model.close();
        model=null;
        if (dataset!=null) 
            dataset.close();
        dataset=null;
        log("closed " + tdbData);
    }

    public void clean() {
        model.begin();
        String action = "DELETE WHERE { ?s ?p ?o . }";
        UpdateAction.parseExecute(action, model);
        model.commit();
    }

    public QueryExecution getExecutor(String q) {
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
        model.begin();
        boolean b = execute("DELETE WHERE { <" + about + "> ?p ?o. }");
        model.commit();
        return b;
    }

    /** relplace model with a new one */
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

    public boolean add(Model m) {
        model.begin();
        model.add(m);
        model.commit();
        return true;
    }

    /** expensive */
    public String getSubject(Model m) {
        int count = 0;
        Resource subject = null;
        ResIterator iter = m.listSubjects();
        try {
            while (iter.hasNext()) {
                Resource s = iter.nextResource();
                if ( s.isAnon() )
                     continue;
                if ( subject==null ) {
                    subject = s;
                } else if (s.equals(subject)) {
                    count++;
                } else if (!s.equals(subject)) {
                    count--;
                    if (count<0) {
                        count=0;
                        subject=s;
                    }
                }
            }
        } finally {
            if ( iter != null ) iter.close();
            if (subject==null)
                return null;
            return subject.toString();
        }
    }

    Model newModel() {
        Model m;
        if (graph==null)
            m = ModelFactory.createDefaultModel();
        else
            m = ModelFactory.createModelForGraph(model.getGraph());
        return m;
    }
 
}
