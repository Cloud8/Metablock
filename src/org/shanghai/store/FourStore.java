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
import java.io.UnsupportedEncodingException;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title FourStore Storage Driver (Experimental)
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

    private int count;

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
        //log("create " + this.baseURL + " :: " + this.graph);
        try {
            statusURL = new URL(baseURL + "/status");
            sizeURL = new URL(statusURL + "/size/");
            dataURL = new URL(baseURL + "/data/");
            sparqlURL = new URL(baseURL + "/sparql/");
            updateURL = new URL(baseURL + "/update/");
        } catch (MalformedURLException e) {
            log(e);
        }
        count = 0;
    }

    public void dispose() {
        log("dispose count " + count);
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
        //log("write to " + dataURL + " " + graph);
        try {
            HttpURLConnection connection = (HttpURLConnection) 
                              dataURL.openConnection();
                              //updateURL.openConnection();
                              //new URL(dataURL + graph).openConnection();

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", 
                                          "application/x-www-form-urlencoded");

            DataOutputStream ps = new DataOutputStream(connection.getOutputStream());
            String data = ""
                           + "graph=" + URLEncoder.encode(graph, "UTF-8") 
                        // + "mime-type=application/rdf+xml"
                           + "&data=" + URLEncoder.encode(rdf, "UTF-8");
            ps.writeBytes(data);
            ps.flush();
            ps.close();

            String response = readResponse(connection);
            if (response.equals("OK")) {
                return true;
            }
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
        delete(graph);
    }

    public boolean delete(String about) {
        log("delete " + about);
        try {
	        URL deleteURL = new URL(dataURL + URLEncoder.encode(graph, "UTF-8"));
            HttpURLConnection connection = (HttpURLConnection) deleteURL.openConnection();

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("DELETE");
            String result = readResponse(connection);
            return result.equals("OK");
        } catch (UnsupportedEncodingException e) { log(e); }
          catch (MalformedURLException e) { log(e); }
          catch (ProtocolException e) { log(e); }
          catch (IOException e) { log(e); }
        return false;
    }

    public boolean update(Model m) {
        log("update not implemented");
        return false;
    }

    public boolean remove(Model m, String graph) {
        return false;
    }

    public boolean save(Model model) {
        //log("save ");
        count++;
        StringWriter writer = new StringWriter();
        try {
           //model.write(writer, "RDF/XML-ABBREV");
           // base null means write only absolute URI's.
           model.write(writer, "RDF/XML", null); 
           //model.write(writer); 
        } catch(Exception e) {
           log(e);
        }
        String rdf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                     + writer.toString();
        return write(rdf);
    }

    public QueryExecution getExecutor(String q) {
        try {
            Query query = QueryFactory.create(q);
            return QueryExecutionFactory.create(query);
        } catch(QueryParseException e) {
            log(e);
            //log("tragedy " + count + " query [" + q + "]");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Enumeration of available result formats and their mime types
     *
     * @author Dan
     *
     */
/****
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
****/

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
/*******
    private String query(String sparql) throws MalformedURLException,
            ProtocolException, IOException {
            //Integer.MIN_VALUE means no soft-limit specified
        return query(sparql, OutputFormat.SPARQL_XML, Integer.MIN_VALUE);
    }
**********/

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
/************
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
**************/

    private String readResponse(HttpURLConnection connection)
              throws MalformedURLException, ProtocolException, IOException {
        InputStream is = null;
        boolean b = false;
        if (connection.getResponseCode() >= 400) {
            is = connection.getErrorStream();
            log("error " + connection.getResponseCode());
            b = true;
        } else {
            is = connection.getInputStream();
        }
        //new InputStreamReader(connection.getInputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        StringBuilder responseBuilder = new StringBuilder();
        String str;
        while (null != ((str = in.readLine()))) {
            responseBuilder.append(str + System.getProperty("line.separator"));
        }
        in.close();
        if (b) log( responseBuilder.toString() );
        return "OK";
    }

    private String readResponse(URL u) throws IOException {
        log("read response from " + u);
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

    public static void main(String[] args) {
        FourStore main = new FourStore();
        main.create();
        //main.probe();

        String rdf = "<?xml version=\"1.0\"?>"
        + "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
        + " xmlns:si=\"http://www.w3schools.com/rdf/\">"
        + "<rdf:Description rdf:about=\"http://www.w3schools.com\">"
        + " <si:title>How to operate with a blown mind</si:title>"
        + " <si:author>Lo Fidelity Allstars</si:author>"
        + " </rdf:Description>"
        + "</rdf:RDF>";
        //main.write(rdf);

        String rdf2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<rdf:RDF xmlns:dct=\"http://purl.org/dc/terms/\""
        + " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
        + " <rdf:Description rdf:about=\"http://localhost/pdfa.pdf\">"
        + " <dct:subject>Geographie</dct:subject>"
        + " <dct:creator>Träger-Wallküry, Bärbel</dct:creator>"
        + " <dct:title>Analysis of Summers in Central Europe</dct:title>"
        + " <rdf:type rdf:resource=\"http://purl.org/spar/fabio/DoctoralThesis\"/>"
        + " </rdf:Description>"
        + " </rdf:RDF>";
        main.write(rdf2);
    }

}
