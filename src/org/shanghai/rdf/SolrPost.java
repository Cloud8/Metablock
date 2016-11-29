package org.shanghai.rdf;

import org.shanghai.util.FileUtil;

//import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrClient;
//import org.apache.solr.client.solrj.impl.HttpSolrServer;
//import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.apache.solr.client.solrj.request.DirectXmlRequest;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.client.solrj.util.ClientUtils;

import org.apache.jena.rdf.model.Model;

import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;


/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop
   @title Solr XML Post Class 
   @date 2013-02-02
*/
public class SolrPost {

    protected SolrClient client;
    private String solr;

    public SolrPost(String solr) {
       this.solr = solr;
    }

    public void create() {
        //server = new HttpSolrServer(solr);
        //server = new CommonsHttpSolrServer(solr);
        client = new HttpSolrClient(solr);
    }

    public void dispose() {
        // log("solr commit");
        try {
          client.commit();
        } catch(SolrServerException e) { log(e); }
          catch(IOException e) { log(e); }
    }

    public boolean test(String resource) {
        SolrQuery q = new SolrQuery("id:" + resource.replace(":","\\:"));
        q.setRows(0); // don't actually request any data
        boolean b = false;
        try {
            long found = client.query(q).getResults().getNumFound();
            b = (found!=0L);
        } catch(SolrServerException e) { log(e); }
        finally {
            return b;
        }
    }

    public boolean post(String data) {
       //log("post " + data);
       boolean b = false;
       if (data==null || data.length()==0)
           return b;
       try {
           DirectXmlRequest up = new DirectXmlRequest( "/update", data );
           client.request( up ); 
           b=true;
        } catch(SolrServerException e) {/*log(e);*/log(data.substring(0,512));}
          catch(IOException e) { log(e); }
          catch(SolrException e) {/*log(e);*/log(data.substring(0,512));}
        finally {
          return b;
        }
    }

    public boolean delete(String id) {
        boolean b = false;
        String delete = "id:"+id.replace(":","\\:").replace("#","*");
        if (id.startsWith("http://")) {
            delete = "uri_str:"+id.replace(":","\\:");
        }
        //log("delete [" + id + "] [" + delete + "]");
        try {
            client.deleteByQuery(delete);
            b=true;
        } catch(IOException e) { log(e); }
          catch(SolrServerException e) { log(e); }
        finally {
          return b;
        }
    }

    public boolean update(String id, String field, String value) {
        //log("update " + id + " " + field + " " + value);
        boolean b = false;
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", id);
        Map<String, String> oper = new HashMap<String, String>();
        oper.put("set", value);
        doc.addField(field, oper);
        b = true;
        try {
            client.add(doc);
        } catch(SolrServerException e) { log(e); }
          catch(IOException e) { log(e); }
         finally {
            return b;
         }
    }

    public void destroy() {
        try {
          client.deleteByQuery( "*:*" );
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

