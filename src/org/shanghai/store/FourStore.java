package org.shanghai.store;

import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.Statement;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.update.UpdateException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.StringWriter;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title FourStore Storage Driver
   @date 2014-02-28
*/
public class FourStore {

    private static String graph;

    private URL baseURL;
    private URL statusURL;
    private URL sizeURL;
    private URL dataURL;
    private URL sparqlURL;
    private URL updateURL;

    public FourStore() {
        this("http://localhost:9000", "archiv");
    }

    public FourStore(String server) {
        this(server, "archiv");
    }

    public FourStore(String server, String graph) {
        try {
            this.baseURL = new URL(server);
            this.graph = graph;
        } catch (MalformedURLException e) {
            log(e);
        }
    }

    public void create() {
        try {
            statusURL = new URL(baseURL + "/status");
            sizeURL = new URL(statusURL + "/size/");
            dataURL = new URL(baseURL + "/data/");
            sparqlURL = new URL(baseURL + "/sparql/");
            updateURL = new URL(baseURL + "/update/");
        } catch (MalformedURLException e) {
            log(e);
        }
    }

    public void dispose() {
    }

    public String probe() {
        try {
            return readResponse(statusURL);
        } catch (IOException e) {
            log(e);
        }
        return null;
    }

    public String size() {
        try {
            return readResponse(sizeURL);
        } catch (IOException e) {
            log(e);
        }
        return null;
    }

    private boolean write(String rdf) {
        log("write to " + dataURL + graph);
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(dataURL
                + graph).openConnection();

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("PUT");
        //  -- fails ??
        //  connection.setRequestProperty("Content-Type","application/rdf+xml");
            connection.setRequestProperty("Content-Type",
                                          "application/rdf+xml-abbrev");

            DataOutputStream ps = new DataOutputStream(connection.getOutputStream());
            ps.writeBytes(rdf);
            ps.flush();
            ps.close();

            String response = readResponse(connection);
            log("response: " + response);
            return true;
        } catch (MalformedURLException e) {
            log(e);
        } catch (ProtocolException e) {
            log(e);
        } catch (IOException e) {
            log(e);
            e.printStackTrace();
        }
        return false;
    }

    public void clean() {
        //try {
            log("4store clean : not implemented");
            //store.delete(graph);
        //} catch (MalformedURLException e) {
        //    log(e);
        //} catch (ProtocolException e) {
        //    log(e);
        //} catch (IOException e) {
        //    log(e);
        //}
    }

    public QueryExecution getExecutor(String query) {
        try {
          Query q = QueryFactory.create(query);
          QueryExecution qexec = 
              QueryExecutionFactory.sparqlService(baseURL.toString(), q);
          return qexec;
        } catch(QueryParseException e) {
          log(query);
          log(e);
        }
        return null;
    }

    /**
    private boolean execute(String action) {
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
    **/

    public boolean delete(String about) {
    /**
        String cmd;
        if (graph==null) 
            cmd = "DELETE WHERE { <" + about + "> ?p ?o. }"; 
        else
            cmd = "DELETE FROM <" + graph +"> "+" { <" + about + "> ?p ?o }"
                  + " WHERE { <" + about + "> ?p ?o }";
        //log("delete [" + cmd + "]");
        return execute(cmd);
    **/
        return false;
    }

    public boolean update(Model m) {
    /*
        StmtIterator si = m.listStatements();
        boolean b=false;
        while(si.hasNext()) {
           Statement st = si.nextStatement();
           virtmodel.removeAll(st.getSubject(),st.getPredicate(),null);
           b=true;
        }
        virtmodel.add(m);
        return b;
    */
        return false;
    }

    public boolean remove(Model m, String graph) {
        return false;
    }

    public boolean save(Model model) {
        StringWriter writer = new StringWriter();
        try {
           //model.write(writer, "RDF/XML-ABBREV");
           // base null means write only absolute URI's.
           //model.write(writer, "RDF/XML", null); 
           model.write(writer, "RDF/XML-ABBREV", null); 
           //model.write(writer); 
        } catch(Exception e) {
           log(e);
        }
        return write(writer.toString());
    }

    /**
     * Enumeration of available result formats and their mime types
     *
     * @author Dan
     *
     */
    private static enum OutputFormat {
        TAB_SEPARATED("text/tab-separated-values"),
        JSON("application/sparql-results+json"),
        SPARQL_XML("application/sparql-results+xml");

        private final String mimeType;

        OutputFormat(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getMimeType() {
            return mimeType;
        };
    };

    /**
     * Queries the repository
     *
     * @param sparql
     * @return the result in SparQL-XML - could be rewritten to pass back a
     *         proper Document
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws IOException
     */
    private String query(String sparql) throws MalformedURLException,
            ProtocolException, IOException {
            //Integer.MIN_VALUE means no soft-limit specified
        return query(sparql, OutputFormat.SPARQL_XML, Integer.MIN_VALUE);
    }

    /**
     * Queries the repository and returns the result in the requested format
     *
     * @param sparql
     * @param format
     * @param softLimit
     * @return the result in the requested format
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws IOException
     */
    private String query(String sparql, OutputFormat format, int softLimit)
            throws MalformedURLException, ProtocolException, IOException {
        HttpURLConnection connection = (HttpURLConnection) sparqlURL
                .openConnection();

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", format.getMimeType());

        DataOutputStream ps = new DataOutputStream(connection.getOutputStream());
        if(softLimit != Integer.MIN_VALUE){  //a soft limit has been set
            ps.writeBytes("&query=" + URLEncoder.encode(sparql, "UTF-8") + "&soft-limit=" + softLimit);
        } else {  //no soft limit specified
            ps.writeBytes("&query=" + URLEncoder.encode(sparql, "UTF-8"));
        }
        ps.flush();
        ps.close();

        return readResponse(connection);
    }

    private String readResponse(HttpURLConnection connection)
              throws MalformedURLException, ProtocolException, IOException {
        InputStream is = null;
        //if (connection.getResponseCode() >= 400) {
        //    is = connection.getErrorStream();
        //} else {
            is = connection.getInputStream();
        //}
        BufferedReader in = new BufferedReader(
                //new InputStreamReader(connection.getInputStream()));
                new InputStreamReader(is));

        StringBuilder responseBuilder = new StringBuilder();
        String str;
        while (null != ((str = in.readLine()))) {
            responseBuilder.append(str + System.getProperty("line.separator"));
        }
        in.close();
        return responseBuilder.toString();
    }

    private String readResponse(URL u) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(u
                .openStream()));
        String response;
        while ((response = in.readLine()) != null)
            System.out.println(response);
        in.close();
        return response;
    }

    private static final Logger logger =
                         Logger.getLogger(FourStore.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
    }

}
