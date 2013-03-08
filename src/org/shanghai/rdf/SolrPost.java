package org.shanghai.rdf;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.DirectXmlRequest;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Logger;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A Solr XML Post Class 
   @date 2013-02-02
*/
public class SolrPost {

    private CommonsHttpSolrServer server;
    private String solrServer;
    private static final Logger logger =
                         Logger.getLogger(SolrPost.class.getName());

    public SolrPost(String solrServer) {
       this.solrServer = solrServer;
    }

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
    }

    public void create() {
        try {
          server = new CommonsHttpSolrServer(solrServer);
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

