package org.shanghai.jena;

import java.util.logging.Logger;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.RDFReader;

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
   @title Old Jena TDB Reader. Better use sparql service.
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

    /** just to be prepared to graphs */
    public TDBReader(String tdbData, String uri) {
        this.tdbData = tdbData;
        this.graph = uri;
    }

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
        //e.printStackTrace(System.out);
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
        UpdateAction.parseExecute(action, model);
        return true;
    }

    public boolean delete(String about) {
        model.begin();
        boolean b = execute("DELETE WHERE { <" + about + "> ?p ?o. }");
        model.commit();
        return b;
    }

    public boolean add(Model m) {
        model.begin();
        model.add(m);
        model.commit();
        return true;
    }

    Model newModel() {
        Model m;
        if (graph==null)
            m = ModelFactory.createDefaultModel();
        else
            m = ModelFactory.createModelForGraph(model.getGraph());
        return m;
    }
 
    /** delete knowledge about resource before adding new statements */
    /***
    public boolean update(String about) {
        return update(about, false);
    }

    public boolean add(String about) {
        return update(about, true);
    }

    private boolean update(String what, boolean create) {
        if (what==null)
            return false;
        boolean b = false;
        try {
            InputStream in = new ByteArrayInputStream(what.getBytes("UTF-8"));
            b = this.update(in, create);
            if (in!=null) in.close();
            //if (!b) log("failed " + what);
        } catch(IOException e) { log(e); }
        finally { return b; }
    }

    private boolean update(InputStream in, boolean create) {
        Model m = newModel();
        RDFReader reader = new JenaReader(); 
        reader.read(m, in, null);
        String about = getSubject(m);
        if (about==null) {
            return false;
        } else {
            if (!create) {
                this.delete(about);
            } else {
                this.add(m);
            }
        }
        return true;
    }

    private String getSubject(Model m) {
        String result = null;
        ResIterator iter = m.listSubjects();
        try {
            while ( iter.hasNext() && result==null) {
                Resource s = iter.nextResource();
                if ( s.isURIResource() ) {
                    result = s.getURI();
                    // System.out.print("URI " + s.getURI());
                } else if ( s.isAnon() ) {
                    // System.out.print("blank");
                }
            }
        } finally {
            if ( iter != null ) iter.close();
            return result;
        }
    }
    ***/

}
