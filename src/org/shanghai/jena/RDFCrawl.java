package org.shanghai.jena;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A Jena RDF Triple Crawler
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

    public RDFCrawl(Properties prop) {
        this.prop = prop;
    }

    private void log(String msg) {
        //logger.info(msg);
        System.out.println("crawl: " + msg);
    }

    private void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    public void create() {
        count = 0;
        // String indexSPQL = prop.getProperty("index.sparql");
        // String aboutSPQL = prop.getProperty("about.sparql");
        // log("index.sparql : " + indexSPQL);
        // log("about.sparql : " + aboutSPQL);
        // log("description.xslt : " + prop.getProperty("description.xslt"));
        try {
            String xslt = Util.readFile(prop.getProperty("transform.xslt"));
            xmlTransformer = new XMLTransformer(xslt);
        } catch(IOException e) { log(e); }
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

    void index() {
	    solrPost.clean();
		// index(100); 
        int chunkSize = 400;
        int i = 0;
        for (boolean b=true; b; b=index((i-1)*chunkSize, chunkSize)) {
             i++;
        }
	    solrPost.commit();
    }

    private void unClean() {
        int count = rdfTransporter.probe();
        String[] identifiers = rdfTransporter.getIdentifiers(0, count);
        for (String bid : identifiers) {
            rdfTransporter.delete(bid);
        }
    }

    /**
    private boolean index(int chunks) {
        int chunkSize = 400;
		for (int i=0; i<chunks; i++) {
            boolean b = index((i-1)*chunkSize, chunkSize);
            if (!b)
               return b;
		}
        return true;
    }
    **/

    /**
    private boolean index(int offset, int limit) {
        log("index " + offset + " limit " + limit );
        if (offset>3000)
            return false;
        return true;
    }
    **/

    private boolean index(int offset, int limit) {
        boolean result = false;
        String[] identifiers = rdfTransporter.getIdentifiers(offset,limit);
        //log("index " + offset + " limit " + limit 
        //             + " length " + identifiers.length);
        for (String bid : identifiers) {
             if (bid==null) {
                 return false;
             }
             String rdfDoc = rdfTransporter.getDescription(bid);
             String solrDoc = xmlTransformer.transform(rdfDoc);
             //GH2013-01 HACK
             if (solrDoc.length() <42) {
                 log("problem: " + bid + " no index!"); 
                 rdfTransporter.talk(bid);
                 continue;
             }
             result = solrPost.post(solrDoc);
             if(!result) {
                 log("problem: " + bid + " solr doc length " +solrDoc.length());
                 // log(solrDoc);
                 rdfTransporter.talk(bid);
                 //return false;
             }
             // log(count + ": " + bid);
             count++;
             // if (count%100==0)
             if (count%limit==0)
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
        try {
            Util.writeFile(testfile, solrDoc);
        } catch(IOException e) { log(e); }
    }

    private void talk() {
        long start = System.currentTimeMillis();
        index();
        long end = System.currentTimeMillis();
        log("indexed " + count + " records in " 
                       + ((end - start)/1000) + " sec");
    }

    public static void main(String[] args) {
        int argc=0;
        Properties prop = new Properties();
        try {
            prop.load(
            RDFCrawl.class.getResourceAsStream("/shanghai.properties"));
        } catch(IOException e) { System.out.println(e); }
        RDFCrawl myself = new RDFCrawl(prop);
        myself.create();
        if (args.length>argc && args[argc].endsWith("-test")) {
		    argc++;
			myself.test();
        } else if (args.length>argc && args[argc].endsWith("-clean")) {
            myself.log("not implemented");
            // myself.clean();
        } else {
            myself.talk();
        }
		myself.dispose();
    }
}
