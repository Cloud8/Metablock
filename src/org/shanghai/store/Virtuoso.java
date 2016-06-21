package org.shanghai.store;

import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.Statement;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.update.UpdateException;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;
import virtuoso.jena.driver.VirtuosoUpdateFactory;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop 
   @title Virtuoso Storage Driver
   @date 2013-09-19
*/
public class Virtuoso {

    private static String jdbc;
    private static final Logger logger =
                         Logger.getLogger(Virtuoso.class.getName());

    private VirtGraph virtgraph;
    private VirtModel virtmodel;
    private String dbuser="dba";
    private String dbpass="dba";

    private String graph;

    public Virtuoso() {
        this.jdbc = "jdbc:virtuoso://localhost:1111";
    }

    public Virtuoso(String jdbc) {
        this.jdbc = jdbc;
    }

    public Virtuoso(String jdbc, String graph) {
        this.jdbc = jdbc;
        this.graph = graph;
    }

    public Virtuoso(String jdbc, String graph, String dbuser, String dbpass) {
        this.jdbc = jdbc;
        this.graph = graph;
        this.dbuser = dbuser;
        this.dbpass = dbpass;
    }

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
    }

    public void create() {
        if (jdbc==null) 
            log("something is terribly wrong: no valid jdbc.");
        if (graph==null) {
            virtgraph = new VirtGraph (jdbc, dbuser, dbpass);
            virtmodel = VirtModel.openDefaultModel(jdbc, dbuser, dbpass);
        } else {
            virtgraph = new VirtGraph (graph, jdbc, dbuser, dbpass);
            virtmodel = VirtModel.openDatabaseModel(graph, jdbc, dbuser, dbpass);
        }
    }

    public void dispose() {
    }

    public void clean() {
        log("virtuoso clear graph");
        execute("SPARQL CLEAR GRAPH <" + graph + ">");
        log("virtuoso removeAll");
        virtmodel.removeAll();
    }

    public QueryExecution getExecutor(String sparql) {
        return VirtuosoQueryExecutionFactory.create(sparql, virtmodel);
    }

    private boolean execute(String action) {
        VirtuosoUpdateRequest vur = 
                              VirtuosoUpdateFactory.create(action, virtgraph);
        boolean b = false;
        try {
            vur.exec();
            b = true;
        } catch(UpdateException e) {
            log("execute failed [" + action + "]");
            log(e);
        } finally {
            return b;
        }
    }

    public boolean delete(String about) {
        String cmd;
        if (graph==null) 
            cmd = "DELETE WHERE { <" + about + "> ?p ?o. }"; 
        else
            cmd = "DELETE FROM <" + graph +"> "+" { <" + about + "> ?p ?o }"
                  + " WHERE { GRAPH  <" + graph +"> "
                  + "       { <" + about + "> ?p ?o }"
                  + "}";
        //log("delete [" + cmd + "]");
        return execute(cmd);
    }

    public boolean update(Model m) {
        StmtIterator si = m.listStatements();
        boolean b=false;
        while(si.hasNext()) {
           Statement st = si.nextStatement();
           virtmodel.removeAll(st.getSubject(),st.getPredicate(),null);
           b=true;
        }
        virtmodel.add(m);
        return b;
    }

    public boolean remove(Model m, String graph) {
        virtmodel.remove(m);
        return true;
    }

    public boolean save(Model m) {
        boolean b=true;
        virtmodel.add(m);
        return b;
    }

}
