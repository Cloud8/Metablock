package org.shanghai.solr;

import org.shanghai.rdf.SolrPost;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.request.DirectXmlRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrException;

import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.SolrDocument;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Logger;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Solr Client to Index a Bibliographic Record
    @date 2012-10-23
*/
public class SolrClient {

    public class MySolrPost extends SolrPost {
        public SolrServer server;
        public MySolrPost(String solr) {
            super(solr);
        }
        @Override
        public void create() {
            super.create();
            this.server = super.server;
            log("MySolrPost.create exposes server! ");
        }
    }

    private MySolrPost solrPost;

    public SolrClient(String solr) {
       solrPost = new MySolrPost(solr);
    }

    public void create() {
        solrPost.create();
    }

    public void dispose() {
        solrPost.dispose();
    }

    public void clean() {
        solrPost.destroy();
    }

    public long probe() {
        return probe("id:*");//wtf
    }

    public long probe(String query) {
        try {
            SolrQuery q = new SolrQuery(query);
            q.setRows(0); // don't actually request any data
            return solrPost.server.query(q).getResults().getNumFound();
        } catch(SolrServerException e) { log(e); }
        return 0;
    }

    public boolean post(String data) {
        return solrPost.post(data);
    }

    public boolean delete(String id) {
        return solrPost.delete(id);
	}

    public SolrDocument read(String oid) {
        if (oid==null || oid.length()==0)
            return null;
        SolrDocument sdoc = null;
        String query = "id:"+oid.replace(":","\\:");
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRows(1);
        solrQuery.setQuery(query);
        try {
            QueryResponse rsp = solrPost.server.query( solrQuery );
            SolrDocumentList docs = rsp.getResults();
            sdoc = docs.get(0);
        } catch(SolrServerException e) { log(e); log(oid);}
        finally {
            return sdoc;
        }
    }

    /** Note: solr is not designed for this use case;
        looping over all results only makes sense for small cores. 
        If sharding is involved, queue up to go insane.
     */
    public String[] getIdentifiers(final int off, final int limit) {
        String[] result = new String[limit];
        int found = 0;
        int count = 0;
        int fetchSize = limit;

        //int fetchSize = 1000;
        SolrQuery query = new SolrQuery();
        //query.setQuery("id:*");
        query.setQuery("uri_str:*");
        query.setRows(fetchSize);
        try {
            QueryResponse rsp = solrPost.server.query(query);

            long offset = 0;
            long totalResults = rsp.getResults().getNumFound();

            while (offset < totalResults)
            {
                query.setStart((int) offset);  // requires an int? wtf?
                query.setRows(fetchSize);

                for (SolrDocument doc : solrPost.server.query(query).getResults())
                {
                     count++;
                     if (off<count && found<limit) {
                         //result[found] = (String) doc.getFieldValue("id"); 
                         result[found] = (String) doc.getFieldValue("uri_str"); 
                         if (result[found].length()==0) {
                             log(" empty : [" + count + "]");
                             result[found]=null;
                         }
                         found++;
                     }
                }
                if (off+limit<count)
                   break;
                offset += fetchSize;
            }
        } catch(SolrServerException e) { log(e); }
        return result;
    }

    private static final Logger logger =
                         Logger.getLogger(SolrClient.class.getName());

    private void log(String message) {
        logger.info(message);
    }

    private void log(Exception e) {
        log(e.toString());
    }

}
