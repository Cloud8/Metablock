package org.shanghai.rdf;

import org.shanghai.util.FileUtil;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A RDF Crawler for the purpose of Solr indexing
   @date 2013-01-31
*/
public class RDFCrawl {

    private static final Logger logger =
                         Logger.getLogger(RDFCrawl.class.getName());

	RDFTransporter rdfTransporter;
    XMLTransformer xmlTransformer;

    SolrPost solrPost;
    private Properties prop;
    int count;
    int chunkSize;
    int logC = 0;

    public RDFCrawl(Properties prop) {
        this.prop = prop;
        if (prop.getProperty("index.count")!=null) {
            this.logC = Integer.parseInt(prop.getProperty("index.count"));
        }
    }

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception e) {
        log(e.toString());
        //e.printStackTrace(System.out);
    }

    public void create() {
        chunkSize = 200;
        count = 0;
        String xslt = FileUtil.read(prop.getProperty("index.transformer"));
        if (xslt==null)
            log(prop.getProperty("index.transformer") + " not found!");
        xmlTransformer = new XMLTransformer(xslt);
	    rdfTransporter = new RDFTransporter(prop);
        rdfTransporter.create();
        solrPost = new SolrPost(prop.getProperty("index.solr"));
        solrPost.create();
    }

    public void dispose() {
	    if (solrPost!=null)
		    solrPost.dispose();
	    if (rdfTransporter!=null)
            rdfTransporter.dispose();
    }

    public void index() {
        int i = 0;
        for (boolean b=true; b; b=index((i-1)*chunkSize, chunkSize)) {
             i++;
        }
	    solrPost.commit();
    }

    public boolean index(int offset, int limit) {
        boolean result = false;
        String[] identifiers = rdfTransporter.getIdentifiers(offset,limit);
        for (String bid : identifiers) {
             if (bid==null) {
                 return false;
             }
             String rdf = rdfTransporter.getDescription(bid);
             if (rdf==null) {
                 //if server sends garbage, we dont care.
                 continue;
             }
             String solrDoc = xmlTransformer.transform(rdf);
             if (solrDoc.length() <42) {
                 // log("problem: " + bid + " no index!"); 
                 // rdfTransporter.talk(bid);
                 continue;
             }
             result = solrPost.post(solrDoc);
             if(!result) {
                 log("problem: " + bid + " solr doc length " +solrDoc.length());
                 rdfTransporter.talk(bid);
             }
             count++;
             if (logC!=0 && count%logC==0)
                 log(bid + " index " + count);
        }
        return result;
    }

    public void post(String bid) {
        String rdfDoc = rdfTransporter.getDescription(bid);
        String solrDoc = xmlTransformer.transform(rdfDoc);
        solrPost.post(solrDoc);
    }

    void test() {
        String[] identifiers = rdfTransporter.getIdentifiers(0,7);
        for (String bid : identifiers) {
             if (bid==null)
                 continue;
             log(bid);
        }
        String rdfDoc = rdfTransporter.getRecord();
        String solrDoc = xmlTransformer.transform(rdfDoc);
        solrPost.post(solrDoc);
        String testfile = prop.getProperty("test.solr");
        FileUtil.write(testfile, solrDoc);
    }

}
