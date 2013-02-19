package org.shanghai.jena;

import java.util.logging.Logger;

/*
  Very straight class design:
    Crawler uses Transporter
    Transporter uses Reader
    Reader has access to TDB storage.
*/

class Readme {

    private static final Logger logger =
                         Logger.getLogger(Readme.class.getName());

    public static void log(String msg) {
        logger.info(msg);    
    }

    /* things to come : remote query */
    /*************************************************************************
      Query query = QueryFactory.create(sparqlQueryString1);
      QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
      ResultSet results = qexec.execSelect();
      ResultSetFormatter.out(System.out, results, query);       
     qexec.close() ;
     **********************************************************************/
}
