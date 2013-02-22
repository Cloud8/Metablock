package org.shanghai.jena;

import java.util.logging.Logger;
import java.util.Iterator;

import java.io.StringWriter;
import java.io.InputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.ResIterator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.arp.JenaReader;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title Maybe later: Federated SPARQL Query evaluation
   @date 2013-02-21
*/
public class ModelTalk {

    private static final Logger logger =
                         Logger.getLogger(ModelTalk.class.getName());

    private String servicePoint;
    private TDBReader tdbReader;
    private int count;
    boolean tdb;

    /** experimental */
    public ModelTalk(String storage) {
        if (storage.startsWith("http://")) {
            servicePoint = storage;
        } else {
            this.tdbReader = new TDBReader(storage);
            tdb = true;
        }
    }

    /** just to be prepared to graphs */
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

    /** shit happens */
    private void log(Exception e) {
        log(e.toString());
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

    /* seeAlso: remote query evaluation like SERVICE keyword */
    /*****************************************************************
      Query query = QueryFactory.create(sparqlQueryString1);
      QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
      ResultSet results = qexec.execSelect();
      ResultSetFormatter.out(System.out, results, query);
     qexec.close() ;
    **********************************************************************/

    private QueryExecution getExecutor(String sparql) {
        if (tdb)
            return tdbReader.getExecutor(sparql);
        Query query = QueryFactory.create(sparql);
        QueryExecution qexec = 
                       QueryExecutionFactory.sparqlService(servicePoint, query);
        return qexec;
    }

    public String[] getSubjects(String query, int limit) {
        //log(query + " [" + limit + "]");
        int k=0;
        String[] result = new String[limit];
        QueryExecution qexec = getExecutor(query);
        try {
            ResultSet results = qexec.execSelect();
            while ( results.hasNext() ) {
	           int i = results.getRowNumber();
               QuerySolution soln = results.nextSolution();
               String subject = getSubject(soln);
               //log(subject + ": " +i + " : " + k);
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
            // if (q.contains("DESCRIBE")) {
            //     model = qexec.execDescribe();
            // } else {
                model = qexec.execConstruct();
            // }
            StringWriter out = new StringWriter();
            model.write(out, "RDF/XML-ABBREV");
            result = out.toString();
         } catch(Exception e) { log(query); log(e); }
           finally {
           qexec.close();
           return result;
         }
    }

    public boolean delete(String what) {
        return tdbReader.delete(what);
    } 

    /** delete and add what is known about the record */
    public boolean update(InputStream in) {
        // Model m = ModelFactory.createModelForGraph(dsg.getDefaultGraph()) ;
        Model m = ModelFactory.createDefaultModel();
        //model - The model to which statements are added
        //in - The InputStream from which to read
        //base - The base to use when converting relative to absolute URIs
        RDFReader reader = new JenaReader(); 
        reader.read(m, in, null);
        // model.add(m);
        // showModel(m);
        // showSubjects(m);
        String what = getSubject(m);
        if (what==null) {
            log("about zero");
            return false;
        } else {
            //tdbReader.execute("DELETE WHERE { <" + about + "> ?p ?o. }");
            tdbReader.delete(what);
            tdbReader.add(m);
        }
        return true;
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

    private void testSolution(QuerySolution soln) {
		count++;
        RDFNode x = soln.get("p"); 
        if (x.isLiteral()) {
			log("literal " + count);
        } else if (x.isResource()) {
		    Resource r = soln.getResource("p");
			log("resource " + count + " " + r.getLocalName());
        } else if (x.isAnon()) {
			log("anon " + count);
		} else {
            log("nothing " + count); 
		}
	    Iterator<String> iter = soln.varNames();
		while ( iter.hasNext() ) {
		     String varName = iter.next();
			 log(varName + " [" + count + "]" + ": " + soln.get(varName));
	    }
	}

    // directly work with the data
    private void showSolution(QuerySolution soln) {
	     Iterator<String> iter = soln.varNames();
		 while ( iter.hasNext() ) {
		     String varName = iter.next();
             // Literal literal = soln.getLiteral(varName);
             // Resource resource = soln.getResource(varName);
			 // log(literal.getString());
			 // log(resource.toString());
			 log("solution " + varName + ": " + soln.get(varName));
		 }
	}

    private void dump(int what, String identifier) {
        log("dump " + what);
        String q = "PREFIX  dcterms: <http://purl.org/dc/terms/>"
                 + " select ?s ?p ?o where {"
                 + " ?s ?p ?o"
                 + " . ?s dc:identifier '" + identifier + "' "
                 + "}";
        Query query = QueryFactory.create(q);
        QueryExecution qexec = getExecutor(q);
        try {
            ResultSet results = qexec.execSelect();
            if (what==0)
                ResultSetFormatter.out(System.out, results, query) ;
            if (what==1) {
                String res = ResultSetFormatter.asXMLString(results);
                log(res);
            }
         } finally {
            qexec.close();
         }
    }

    /** return a reasonable subject */
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

    private void showModel(Model m) {
        StmtIterator iter = m.listStatements();
        try {
            while ( iter.hasNext() ) {
                Statement stmt = iter.next();
                
                Resource s = stmt.getSubject();
                Resource p = stmt.getPredicate();
                RDFNode o = stmt.getObject();
                
                if ( s.isURIResource() ) {
                    System.out.print("URI " + s.getURI());
                } else if ( s.isAnon() ) {
                    System.out.print("blank");
                }
                
                if ( p.isURIResource() )
                    System.out.print(" URI ");
                
                if ( o.isURIResource() ) {
                    System.out.print("URI");
                } else if ( o.isAnon() ) {
                    System.out.print("blank");
                } else if ( o.isLiteral() ) {
                    System.out.print("literal");
                }
                
                System.out.println();
            }
        } finally {
            if ( iter != null ) iter.close();
        }
    }

    /** test support */
    public static void main(String[] args) {
	    ModelTalk myself = new ModelTalk("/vol/vol01/data/jena.tdb");
        int argc=0;
        myself.create();
        if (args.length>argc && args[argc].endsWith("-dump")) {
		    argc++;
			if (args.length>argc)
                 myself.dump(0, args[argc]);
			else myself.dump(0, "000000019");
        } 
        myself.dispose();
    }
}
