package org.shanghai.rdf;

import org.shanghai.util.FileUtil;

import java.io.IOException;
import java.util.logging.Logger;
import com.hp.hpl.jena.rdf.model.Model;
import java.io.StringReader;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop 
   @title RDF Crawler for the purpose of Solr indexing
   @date 2013-01-31
*/
public class MetaCrawl {

    /** read from here */
    public interface Transporter {
        public void create();
        public void dispose();
        public String probe();
        public Model read(String resource);
        public String[] getIdentifiers(int off, int limit);
    }

    /** write to there */
    public interface Storage {
        public void create();
        public void dispose();
        public boolean delete(String resource);
        public boolean write(Model mod);
        public boolean update(Model mod);
        public void destroy();
    }

	private Transporter transporter;
    private Storage storage;
    private XMLTransformer xmlTransformer;

    private String xsltFile;
    private String testFile;
    private int chunkSize;
    private int logC = 0;
    private int count;

    public MetaCrawl(Transporter r, Storage s, String x, String t, int c) {
        this.transporter = r;
        this.storage = s;
        this.xsltFile = x;
        this.testFile = t;
        this.logC = c;
    }

    public void create() {
        chunkSize = 200;
        count = 0;
        String xslt = FileUtil.read(xsltFile);
        if (xslt==null) 
            log(xsltFile + " not found!");
        xmlTransformer = new XMLTransformer(xslt);
        xmlTransformer.create();
    }

    public void dispose() {
        xmlTransformer.dispose();
    }

    public int index() {
        int i=0;
        for (boolean b=true; b; b=index((i-1)*chunkSize, chunkSize)) {
            i++;
        }
        return count;
    }

    /** no difference ! */
    public boolean index(int offset, int limit) {
        //return indexFast(offset, limit);
        return indexSlow(offset, limit);
    }

/*
    //reduce write operations
    private boolean indexFast(int offset, int limit) {
        boolean result = true;
        String[] identifiers = transporter.getIdentifiers(offset,limit);
        Document doc = null;

        for (String id : identifiers) {
             if (id==null) {
                 return false;
             }
             Model mod = transporter.read(id);
             if (mod==null) {//if server sends garbage, dont care.
                 continue;
             }
             String solrDoc = xmlTransformer.transform(mod);
             if (solrDoc.length() <42) {
                 continue;
             }

             try {
                 InputSource is = new InputSource(new StringReader(solrDoc));
                 Document solr = builder.parse(is); 
                 if (doc==null) {
                     doc = solr;
                 } else {
                     doc.getDocumentElement().appendChild(
                     doc.importNode(solr.getDocumentElement(),true));
                 }
             } catch( SAXException e ) { log(e); }
               catch( IOException e ) { log(e); }

             count++;
             if (logC!=0 && count%logC==0)
                 log(id + " index " + count);
        }

        String solrDoc = xmlTransformer.asString( doc );
        result=storage.post(solrDoc);
        if(!result) {
            log("problem: solr doc length " +solrDoc.length());
            result=true;//dont stop the show
        }
        result=storage.write(mod);
        return result;
    }
*/

    private boolean indexSlow(int offset, int limit) {
        boolean result = true;
        String[] identifiers = transporter.getIdentifiers(offset,limit);
        for (String id : identifiers) {
             if (id==null) {
                 return false;
             }
             Model mod = transporter.read(id);
             if (mod==null) {//if server sends garbage, dont care.
                 continue;
             }
             result=storage.write(mod);
             if(!result) {
                 log("problem: " + id);
                 dump(id);
                 result=true;//dont stop the show
             }
             count++;
             if (logC!=0 && count%logC==0)
                 log(id + " index " + count);
        }
        return result;
    }

    public String read(String resource) {
        Model mod = transporter.read(resource);
        String rdf = xmlTransformer.asString(mod);
        return rdf;
    }

    public void post(String resource) {
        Model mod = transporter.read(resource);
        if (mod==null) {
            log("cant post " + resource);
        } else {
            storage.write(mod);
        }
    }

    public void test() {
        String[] identifiers = transporter.getIdentifiers(0,7);
        String resource = null;
        for (String id : identifiers) {
             if (id==null)
                 continue;
             resource = id;
        }
        if (resource!=null) {
            log(resource);
            post(resource);
        }
    }

    private void dump(String id, String data) {
        if (testFile==null) {
            System.out.println(data);
        } else {
            FileUtil.write(testFile, data);
            log("dump " + id + " to " + testFile);
        } 
    }

    private void dump(String resource) {
        String rdf = read(resource);
        dump(resource,rdf);
    }

    private static final Logger logger =
                         Logger.getLogger(MetaCrawl.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception e) {
        log(e.toString());
    }
}