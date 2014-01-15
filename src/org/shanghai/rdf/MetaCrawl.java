package org.shanghai.rdf;

import org.shanghai.util.FileUtil;

import java.io.IOException;
import java.util.logging.Logger;
import com.hp.hpl.jena.rdf.model.Model;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.StringReader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
        public boolean post(String data);
        public boolean write(Model mod);
        public boolean update(Model mod);
        public void destroy();
    }

    int count;

	private Transporter transporter;
    private Storage storage;
    private XMLTransformer xmlTransformer;

    private String xsltFile;
    private String testFile;
    private int chunkSize;
    private int logC = 0;

    private DocumentBuilder builder;

    public MetaCrawl(Transporter r, Storage s, String x, String t, int c) {
        this.transporter = r;
        this.storage = s;
        this.xsltFile = x;
        this.logC = c;
        this.testFile = t;
    }

    public void create() {
        chunkSize = 200;
        count = 0;
        String xslt = FileUtil.read(xsltFile);
        if (xslt==null) 
            log(xsltFile + " not found!");
        xmlTransformer = new XMLTransformer(xslt);
        xmlTransformer.create();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try { builder = factory.newDocumentBuilder();
        } catch(ParserConfigurationException e) { log(e); }

    }

    public void dispose() {
        xmlTransformer.dispose();
    }

    public void index() {
        int i = 0;
        for (boolean b=true; b; b=index((i-1)*chunkSize, chunkSize)) {
             i++;
        }
    }

    /** no difference ! */
    public boolean index(int offset, int limit) {
        //return indexFast(offset, limit);
        return indexSlow(offset, limit);
    }

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
        return result;
    }

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
             String solrDoc = xmlTransformer.transform(mod);
             if (solrDoc.length() <42) {
                 continue;
             }
             result=storage.post(solrDoc);
             if(!result) {
                 log("problem: " + id + " solr doc length " +solrDoc.length());
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
            String solr = xmlTransformer.transform(mod);
            storage.post(solr);
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
