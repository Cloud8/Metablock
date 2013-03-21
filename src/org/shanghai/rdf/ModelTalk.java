package org.shanghai.rdf;

import org.shanghai.jena.TDBReader;
import org.shanghai.rdf.RDFReader;

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
import com.hp.hpl.jena.query.QueryParseException;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title Reading RDF from SPARQL
   @date 2013-02-25
*/
/** @abstract Reads RDF data from a sparql service endpoint.
              This class can also read from a jena TDB store. */
public class ModelTalk implements RDFReader.Interface {

    private static final Logger logger =
                         Logger.getLogger(ModelTalk.class.getName());

    private String servicePoint;
    protected TDBReader tdbReader;
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
            //log(storage);
            this.tdbReader = new TDBReader(storage, uri);
            tdb = true;
        }
    }

    protected void log(String msg) {
        logger.info(msg);    
    }

    protected void log(Exception e) {
        log("/* shit happens */ " + e.toString());
        // e.printStackTrace(System.out);
    }

    @Override
    public RDFReader.Interface create() {
        if (tdb)
            tdbReader.create();
        return this;
    }

    @Override
    public void dispose() {
        if (tdb)
            tdbReader.dispose();
    }

    private QueryExecution getExecutor(String sparql) {
        if (tdb)
            return tdbReader.getExecutor(sparql);
        try {
          Query query = QueryFactory.create(sparql);
          QueryExecution qexec = 
                       QueryExecutionFactory.sparqlService(servicePoint, query);
          return qexec;
        } catch(QueryParseException e) {
          log(sparql);
          log(e);
        }
        return null;
    }

    @Override
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
           //log("subjects: " + k + " limit " + limit);
           if (0<k && k<limit) {
               log("subject array crunch " + k + " " + limit);
               String resultCopy[] = new String[k];
               for (int i=0; i<k; i++)
                    resultCopy[i] = result[i];
               result = null;
               return resultCopy;
           }
           return result;
         }
    }

    /** execute query and return first row first subject */
    @Override
    public String query(String query) {
        String result = null;
        QueryExecution qe = getExecutor(query);
        try {
            ResultSet results=qe.execSelect();
            QuerySolution soln = results.next();
            result = soln.getLiteral("subject").getString();
        } catch( Exception e ) {
          log(query);
          log(e);
        }
        return result;
    }

    /** return sparql query result as xml */
    @Override
    public String getDescription(String query) {
        String result = null;
        QueryExecution qexec = getExecutor(query);
        if (qexec==null)
            return ""; // try to continue
        try {
            Model model = qexec.execConstruct();
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
