package org.shanghai.jena;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.DirectXmlRequest;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.net.MalformedURLException;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A Solr XML Post Class 
   @date 2013-01-31
*/
public class SolrPost {

  CommonsHttpSolrServer server;

  static final String POST_URL = "http://localhost:8080/solr/code/update";
  public static final String POST_ENCODING = "UTF-8";
  public static final String VERSION_OF_THIS_TOOL = "1.2";
  static final String SOLR_OK_RESPONSE_EXCERPT = "<int name=\"status\">0</int>";

  private String solrServer;

  public SolrPost() {
     this.solrServer = POST_URL;
  }

  public SolrPost(String solrServer) {
     this.solrServer = solrServer;
  }

  void fatal(String msg) {
    System.err.println("SolrPost: FATAL: " + msg);
    // System.exit(1);
  }

  void log(String msg) {
    System.out.println("SolrPost: " + msg);
  }

  void log(Exception e) {
     log(e.toString());
  }

  public void create() {
    try {
      server = new CommonsHttpSolrServer(solrServer);
      //server.setParser(new XMLResponseParser());
    } catch (MalformedURLException e) {
      log("System Property 'url' is not a valid URL: " + solrServer);
    }
  }

  public void dispose() {
      commit();
  }

  public void commit() {
      try {
        server.commit();
      } catch(SolrServerException e) { log(e); }
        catch(IOException e) { log(e); }
  }

  public void clean() {
      try {
        server.deleteByQuery( "*:*" );
      } catch(SolrServerException e) { log(e); }
        catch(IOException e) { log(e); }
  }

  public boolean post(String data) {
     boolean b = true;
     try {
       DirectXmlRequest up = new DirectXmlRequest( "/update", data );
       server.request( up ); 
      } catch(SolrServerException e) {b=false; log(e); }
        catch(IOException e) { b=false; log(e); }
      finally {
        return b;
     }
  }

}

