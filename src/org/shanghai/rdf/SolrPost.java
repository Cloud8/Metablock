package org.shanghai.rdf;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.DirectXmlRequest;
import org.apache.solr.common.SolrInputDocument;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;


/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop
   @title Solr XML Post Class 
   @date 2013-02-02
*/
public class SolrPost implements RDFCrawl.Storage {

    protected SolrServer server;
    private String solr;

    public SolrPost(String solr) {
       this.solr = solr;
    }

    @Override
    public void create() {
        if (solr.startsWith("http://")) {
            server = new HttpSolrServer(solr);
        } else if (solr.startsWith("/")) {
            String solrDir = solr.substring(0,solr.lastIndexOf("/"));
            String core = solr.substring(solr.lastIndexOf("/")+1);
            log("solr emb " + solrDir + " core " + core);
            CoreContainer container = new CoreContainer(solrDir);
            container.load(solrDir, new File("solr.xml"));
            server = new EmbeddedSolrServer( container, core);
        }
    }

    @Override
    public void dispose() {
        //if (server==null)
        //    return;
        log("solr commit");
        try {
          server.commit();
        } catch(SolrServerException e) { log(e); }
          catch(IOException e) { log(e); }
    }

    @Override
    public boolean post(String data) {
       boolean b = false;
       if (data==null || data.length()==0)
           return b;
       try {
         DirectXmlRequest up = new DirectXmlRequest( "/update", data );
         server.request( up ); 
         b=true;
        } catch(SolrServerException e) {log(e); }
          catch(IOException e) { log(e); }
        finally {
          return b;
        }
    }

    @Override
    public boolean delete(String id) {
        boolean b = false;
        String delete = "id:"+id.replace(":","\\:");
        try {
            server.deleteByQuery(delete);
            b=true;
        } catch(IOException e) { log(e); }
          catch(SolrServerException e) { log(e); }
        finally {
          return b;
        }
    }

    @Override
    public void destroy() {
        try {
          server.deleteByQuery( "*:*" );
        } catch(SolrServerException e) { log(e); }
        catch(IOException e) { log(e); }
    }

    private static final Logger logger =
                         Logger.getLogger(SolrPost.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
    }

}

