package org.shanghai.rdf;

import java.util.logging.Logger;
import java.io.StringWriter;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Model;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import org.shanghai.rdf.RDFReader.Interface;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title Reading RDF from SPARQL
   @date 2013-02-25
*/
/** @abstract Reads RDF data from a sparql service endpoint.
    This class can also read from a jena TDB store. */
public class ModelTalk implements Interface {

    private static final Logger logger =
                         Logger.getLogger(ModelTalk.class.getName());

    private String servicePoint;
    private TDBReader tdbReader;
    private int count;
    private boolean tdb;

    public ModelTalk(String service) {
        if (service.startsWith("http://")) {
            servicePoint = service;
        } else {
            this.tdbReader = new TDBReader(service);
            tdb = true;
        }
    }

    /** Be prepared to graphs */
    public ModelTalk(String storage, String uri) {
        if (storage.startsWith("http://")) {
            servicePoint = storage;
        } else {
            log(storage);
            this.tdbReader = new TDBReader(storage, uri);
            tdb = true;
        }
    }

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log("/* shit happens */" + e.toString());
        e.printStackTrace(System.out);
    }

    public void create() {
        if (tdb)
            tdbReader.create();
    }

    public void dispose() {
        if (tdb)
            tdbReader.dispose();
    }

    private QueryExecution getExecutor(String sparql) {
        if (tdb)
            return tdbReader.getExecutor(sparql);
        Query query = QueryFactory.create(sparql);
        QueryExecution qexec = 
                       QueryExecutionFactory.sparqlService(servicePoint, query);
        return qexec;
    }

    public String[] getSubjects(String query, int limit) {
        int k=0;
        String[] result = new String[limit];
        QueryExecution qexec = getExecutor(query);
        try {
            ResultSet results = qexec.execSelect();
            while ( results.hasNext() ) {
	           int i = results.getRowNumber();
               QuerySolution soln = results.nextSolution();
               String subject = getSubject(soln);
               if (subject!=null)
                   result[k++] = subject;
           }
         } catch(Exception e) { log(query + " [" + limit + "]"); log(e); }
           finally {
           qexec.close();
           return result;
         }
    }

    /** execute query and return first row first subject */
    public String query(String query) {
        QueryExecution qe = getExecutor(query);
        ResultSet results=qe.execSelect();
        QuerySolution soln = results.next();
        String result = soln.getLiteral("subject").getString();
        return result;
    }

    /** return sparql query result as xml */
    public String getDescription(String query) {
        String result = null;
        QueryExecution qexec = getExecutor(query);
        try {
            Model model;
            model = qexec.execConstruct();
            StringWriter out = new StringWriter();
            model.write(out, "RDF/XML-ABBREV");
            result = out.toString();
         } catch(Exception e) { log(query); log(e); }
           finally {
           qexec.close();
           return result;
         }
    }

    /** TODO: find out how to retrieve better subject */
    private String getSubject(QuerySolution soln) {
        // Return the value of the named variable in this binding, 
        // casting to a Resource. 
        // A return of null indicates that the variable is not present 
        // in this solution. 
        // An exception indicates it was present but not a resource.
        Resource subject = soln.getResource("s");
        if (subject==null)
            subject = soln.getResource("subject");
        if (subject==null)
            subject = soln.getResource("identifier");
        if (subject==null)
            return null;
        return subject.toString();
    }

}
