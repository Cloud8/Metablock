package org.shanghai.jena;

import java.util.Iterator;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
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

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.update.UpdateAction;

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
   @title A Jena TDB Reader 
   @date 2013-01-16
*/
public class TDBReader {

    private static String tdbData = "/ub01/data/gnd.tdb";
    private static final Logger logger =
                         Logger.getLogger(TDBReader.class.getName());

    Model model;
    Dataset dataset;
    int count = 0;

    Location location;
    //DatasetGraph dsg;
    String uri = null;

    public TDBReader(String tdbData) {
        this.tdbData = tdbData;
    }

    public TDBReader(String tdbData, String uri) {
        this.tdbData = tdbData;
        this.uri = uri;
    }

    private void log(String msg) {
        logger.info(msg);    
        //System.out.println(msg);
    }

    private void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    public void create() {
      if (model==null) {
        location = new Location (tdbData);
        dataset = TDBFactory.createDataset(location) ;
        if (uri==null) {
            log("init " + tdbData);
		    model = dataset.getDefaultModel();
        } else {
            log("init " + tdbData + " graph " + uri);
		    model = dataset.getNamedModel(uri);
        }
      }
    }

    public void dispose() {
        model.close();
        if (dataset!=null) 
            dataset.close();
        model=null;
        log("closed " + tdbData);
    }

    public String[] getSubjects(String q, int offset, int limit) {
        String[] result = new String[limit];
        String lquery = q + " offset " + offset + " limit " + limit;
        // log(lquery);
        Query query = QueryFactory.create(lquery);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        try {
            ResultSet results = qexec.execSelect();
            while ( results.hasNext() ) {
	           int i = results.getRowNumber();
               QuerySolution soln = results.nextSolution();
		   	   // showSolution( soln );
               // String identifier = "" + soln.get("identifier");
			   // log("identifier " + i + ": " + soln.get("identifier"));
               String subject = getSubject(soln);
			   // log("subject " + i + ": " + subject);
			   // log(varName + ": " + soln.get(varName));
               if (subject!=null)
                   result[i] = subject;
               // else 
               //  result[i] = identifier;
           }
         } catch(Exception e) { log(e); }
           finally {
           qexec.close();
           return result;
         }
    }

    /** return a concise bounded description for the subject as xml string 
      */
    public String getDescription(String query, String subject) {
         // String desc = query + " DESCRIBE <" + subject + ">\n";
         String desc = query.replace("%param%", "<" + subject + ">");
         return getData(desc);
    }

    /** execute an update query */
    public boolean execute(String action) {
        UpdateAction.parseExecute(action, model);
        return true;
    }

    /** execute query and return first row first subject */
    public String query(String query) {
        //log(query);
        //Query selectQuery = QueryFactory.create(query);
        //QueryExecution qe = QueryExecutionFactory
        //               .sparqlService(Constant.SPARQL_ENDPOINT, selectQuery);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results=qe.execSelect();
        QuerySolution soln = results.next();
        String result = soln.getLiteral("subject").getString();
        return result;
    }

    private String getDescription(String subject) {
       String q = "# concise bounded description\n"
                + "PREFIX  dcterms: <http://purl.org/dc/terms/>\n"
                + "PREFIX  dc: <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  gnd: <http://d-nb.info/gnd/>\n"
    + "PREFIX  thesis: <http://www.ndltd.org/standards/metadata/etdms/1.0/>\n"
    + "PREFIX  dini: <http://www.d-nb.de/standards/xmetadissplus/type/>\n"
                + "DESCRIBE <" + subject + ">\n";
          //  + "DESCRIBE <http://archiv.ub.uni-marburg.de/diss/z2010/0061>\n";
       return getData(q);
    }

    /** return sparql query result as xml */
    public String getData(String q) {
        String result = null;
        Query query = QueryFactory.create(q);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        try {
            //ResultSet results = qexec.execSelect();
            Model model;
            if (q.contains("DESCRIBE")) {
                model = qexec.execDescribe();
            } else {
                model = qexec.execConstruct();
            }
            StringWriter out = new StringWriter();
            model.write(out, "RDF/XML-ABBREV");
            result = out.toString();
         } catch(Exception e) { log(e); }
           finally {
           qexec.close();
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

    /** return sparql qeury result as xml */
    /****
    private HashMap<String,String> getRecord(String identifier) {
        String q = "PREFIX  dcterms: <http://purl.org/dc/terms/>"
                 + "PREFIX  dc: <http://purl.org/dc/elements/1.1/>"
                 + " select ?s ?p ?o where {"
                 + " ?s ?p ?o ."
                 + " ?s dc:identifier '" + identifier + "'"
                 + "}";
	    HashMap<String,String> hash = new HashMap<String,String>();
        ResultSet results = null;
        Query query = QueryFactory.create(q);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        try {
            results = qexec.execSelect();
            //this works, but after close result is gone
			count = 0;
            for ( ; results.hasNext() ; ) {
                QuerySolution soln = results.nextSolution() ;
				//testSolution(soln);
		        //Resource p = soln.getResource("p");
		        //Resource o = soln.getResource("o");
				//log(p.getLocalName() + ": " + o.toString());
				String[] result = getSolution(soln);
				hash.put(result[0], result[1]);
            }
            //showResult(results); 
         } finally {
           qexec.close();
           return hash;
         }
    }
    **/

    /**
    private String[] getSolution(QuerySolution soln) {
	    String[] result = new String[2];
        if (count==0) {
		    count++;
            RDFNode z = soln.get("s"); 
            if (z.isResource()) {
		        Resource o = soln.getResource("s");
			    //log("resource s " + count + " " + o.toString());
                result[0] = "url";
		        result[1] = o.toString();
                return result;
            } else if (z.isLiteral()) {
		    	log("literal s " + count + " " + z.toString());
		    	// log("literal " + count);
		        //result[1] = y.toString();
		    } else {
		    	log("nothing s " + count + " " + z.toString());
		        //result[1] = y.toString();
            }
        }

		count++;
        RDFNode x = soln.get("p"); 
        if (x.isLiteral()) {
			log("literal " + count);
        } else if (x.isResource()) {
		    Resource r = soln.getResource("p");
		    result[0] = r.getLocalName();
			// log("resource p " + count + " " + r.getLocalName());
        } else if (x.isAnon()) {
			log("anon " + count);
		} else {
            log("nothing " + count); 
		}

        RDFNode y = soln.get("o"); 
        if (y.isResource()) {
		    Resource o = soln.getResource("o");
		    result[1] = o.toString();
			log("resource o " + count + " " + o.getLocalName());
        } else if (y.isLiteral()) {
			// log("literal " + count);
		    result[1] = y.toString();
		} else {
			log("no resource o " + count + " " + y.toString());
		    result[1] = y.toString();
		}

		return result;
	}
    **/

    private void testSolution(QuerySolution soln) {
		count++;
        // log("nothing " + count++); 
        // RDFNode x = soln.get("varName"); // Get a result variable by name.
        // Resource r = soln.getResource("VarR");// - must be a resource
        // Literal l = soln.getLiteral("VarL") ;// - must be a literal
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
             // Literal literal = soln.getLiteral(varName);
             // Resource resource = soln.getResource(varName);
			 // log(literal.getString());
			 // log(resource.toString());
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

    /** test */
    void test() {
        String q = "PREFIX  dcterms: <http://purl.org/dc/terms/>"
                 + "PREFIX  dc: <http://purl.org/dc/elements/1.1/>"
                 + " select ?s ?identifier  where {"
                 + " ?s dc:identifier ?identifier"
                 + "}"
                 + "";
        String result[] = getSubjects(q,1117,32);
        for (String identifier : result) {
             System.out.println(identifier);
        }
        int count = result.length;
        //showRecord(result[count-1]);
        String subject = result[count-1];
        log( getDescription(subject) );
    }

    /**
    private void showRecord(String identifier) {
        HashMap<String,String> hash = getRecord(identifier);
		Iterator it = hash.keySet().iterator();
		log("solution:");
		while (it.hasNext()) {
			String p = (String)it.next();
			String o = hash.get(p);
			if (!"abstract".equals(p))
                log(p + ": " + o);
	    }
    }
    **/

    public void dump(int what, String identifier) {
        log("dump " + what);
        String q = "PREFIX  dcterms: <http://purl.org/dc/terms/>"
                 + " select ?s ?p ?o where {"
                 + " ?s ?p ?o"
                 + " . ?s dc:identifier '" + identifier + "' "
                 + "}";
        Query query = QueryFactory.create(q);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
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

    public boolean delete(String about) {
        model.begin();
        boolean b = execute("DELETE WHERE { <" + about + "> ?p ?o. }");
        model.commit();
        return b;
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
            //log("about " + what);
            model.begin();
            delete(what);
            model.add(m);
            model.commit();
        }
        return true;
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

    public static void main(String[] args) {
	    TDBReader myself = new TDBReader(TDBReader.tdbData);
        int argc=0;
        myself.create();
        if (args.length>argc && args[argc].endsWith("-dump")) {
		    argc++;
			if (args.length>argc)
                 myself.dump(0, args[argc]);
			else myself.dump(0, "000000019");
        } else if (args.length>argc && args[argc].endsWith("-test")) {
		    argc++;
			myself.test();
        }
        myself.dispose();
    }
}
