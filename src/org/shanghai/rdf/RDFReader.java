package org.shanghai.rdf;

import org.shanghai.store.Store;

import java.util.logging.Logger;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop
   @title Storage for RDF data
   @date 2013-10-21
*/
public class RDFReader implements RDFTransporter.Reader {

    protected Store store;

    public RDFReader(Store store) {
        this.store = store;
    } 

    @Override
    public void create() {
        store.create();
    }

    @Override
    public void dispose() {
        if (store!=null) {
            store.dispose();
            store=null;
         }
    }

    @Override
    public Model read(String resource) {
        return store.read(resource);
    }

    @Override
    public String[] getIdentifiers(String q, int offset, int limit) {
        String query = q + " offset " + offset + " limit " + limit;
        return getSubjects(query, limit);
    }

    @Override
    public String probe(String query) {
        return store.probe(query);
    }

    private String[] getSubjects(String query, int limit) {
        int k=0;
        int r=0;
        String[] result = new String[limit];
        QueryExecution qexec = store.getExecutor(query);
        try {
            ResultSet results = qexec.execSelect();
            while ( results.hasNext() ) {
	           r = results.getRowNumber();
               QuerySolution soln = results.nextSolution();
               String subject = getSubject(soln);
               if (subject!=null)
                   result[k++] = subject;
           }
         } catch(Exception e) { log(query + " [" + limit + "]"); log(e); }
           finally {
           qexec.close();
           if (0<k && k<limit && 0<r) {
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

    private static final Logger logger =
                         Logger.getLogger(RDFReader.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
    }

}
