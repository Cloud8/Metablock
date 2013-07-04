package org.shanghai.solr;

import org.shanghai.bones.BiblioRecord;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrServerException;
import java.util.logging.Logger;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop <fb.com/goetz.hatop>
  @title Solr Client to Index a Bibliographic Record
  @date 2012-10-23
*/
public class SolrClient {

	private HttpSolrServer server;
    private String solrServer;
    private static final Logger logger =
                         Logger.getLogger(SolrClient.class.getName());

    public SolrClient(String s) {
       this.solrServer = s;
    }

    public void log(String message) {
        logger.info(message);
    }

    private void log(Exception e) {
        log(e.toString());
    }

    public void create() {
        //try {
	       server = new HttpSolrServer(solrServer);
		//} catch(MalformedURLException e) { log(e); }
    }

    public void dispose() {
        commit();
    }

    public void commit() {
	    try {
	        server.commit();
		} catch(IOException e) { log(e); }
		  catch(SolrServerException e) { log(e); }
	}

    /** CAUTION: deletes everything! */
    public void clean() {
        try {
		    server.deleteByQuery( "*:*" );
		} catch(IOException e) { log(e); }
		  catch(SolrServerException e) { log(e); }
    }

    public void add(BiblioRecord b) {
		try {
		   server.addBean(b);
           // log("add " + b.id);
		} catch(IOException e) { log(e); }
		  catch(SolrServerException e) { log(e); }
		  // catch(SolrException e) { log(e); }
	}

    public BiblioRecord getRecord(String id) {
        String query = "id:"+id.replace(":","\\:");
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRows(1);
        solrQuery.setQuery(query);
        try {
            QueryResponse rsp = server.query( solrQuery );
            List<BiblioRecord> beans = rsp.getBeans(BiblioRecord.class);
            return beans.get(0);
        } catch(SolrServerException e) { log(e); }
        return null;
    }

    public List<BiblioRecord> getUpdRecords(int days, int rows) {
        String query = "upd_date:[NOW-" + days + "DAY TO NOW]";
        return getRecords(query, rows);
    }

    private List<BiblioRecord> getRecords(String query, int rows) {
        SolrQuery solrQuery = new SolrQuery();
        log("solr query [" + query + "]");
        solrQuery.setRows(rows);
        solrQuery.setQuery(query);
        try {
            QueryResponse rsp = server.query( solrQuery );
            List<BiblioRecord> beans = rsp.getBeans(BiblioRecord.class);
            return beans;
        } catch(SolrServerException e) { log(e); }
        return new ArrayList<BiblioRecord>();
    }

}
