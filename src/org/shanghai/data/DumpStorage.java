package org.shanghai.data;

import org.shanghai.rdf.XMLTransformer;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;
import org.shanghai.util.DocUtil;

import org.w3c.dom.Document;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import java.util.logging.Logger;
import org.shanghai.util.ModelUtil;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.io.StringWriter;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Krystoff Nieszczęście
  @title Write Models to Dump File
  @date 2015-11-16
*/
public class DumpStorage implements MetaCrawl.Storage {

    private Document document = null;
    private String dumpFile = null;
    private XMLTransformer transformer = null;
    private OutputStream os;
    private int count;
    private StringWriter sw;
    private DocUtil docUtil;

    public DumpStorage(String dumpFile) {
        this.dumpFile = dumpFile;
    }

    public DumpStorage(OutputStream os) {
        this.os = os;
    }

    public DumpStorage(String dumpFile, String xslt) {
        this(dumpFile);
        createTransformer(xslt);
    }

    public DumpStorage(OutputStream os, String xslt) {
        this(os);
        createTransformer(xslt);
    }

    @Override
    public void create() {
        sw = new StringWriter();
        docUtil = new DocUtil().create();
        count = 0;
        if (dumpFile==null) {
            // nothing
        } else try {
            os = new FileOutputStream(dumpFile);
        } catch(FileNotFoundException e) { log(e); }
        if (transformer==null) {
		    // nothing
        } else {
            transformer.create();
        }
    }

    @Override
    public void dispose() {
	    finish();
        docUtil.dispose();
        if (dumpFile==null) try {
            os.close();
            // log(" dumped " + count + " records.");
        } catch(IOException ex) { log(ex); }
        else log("see " + dumpFile + " # " + count + " records.");
    }

    @Override
    public boolean test(Resource rc) {
        count++;
        System.out.println("console test: " + rc.getURI());
        return true;
    }

    @Override
    public boolean delete(String resource) {
        System.out.println("dump storage delete: " + resource); 
        return true;
    }

    @Override
    public boolean write(Resource rc) {
        if (transformer==null) { 
            // RDFDataMgr.write(os, rc.getModel(), RDFLanguages.RDFXML) ;
            document = docUtil.append(document, rc);
        } else {
            //String xml = transformer.transform(rc);
            //if (os==null) log("zero os ? created ?");
            //os.write(xml.getBytes(Charset.forName("UTF-8")));
            document = docUtil.append(document, rc);
        } // catch(IOException e) { log(e); }
        count++;
        return true;
    }

    private void finish() {
        try { 
		    if (transformer==null) {
                os.write(docUtil.asString(document).getBytes("UTF-8"));
            } else {
                os.write(transformer._transform(document).getBytes("UTF-8"));
                transformer.dispose();
		    }
		} catch(IOException ex) { log(ex); }
	}

    // unused -- concat subsequent RDF based on string manipulation
    private void writeCut(Resource rc) {
        sw.getBuffer().setLength(0);
        try {
            RDFDataMgr.write(sw, rc.getModel(), RDFLanguages.RDFXML) ;
            StringBuffer sb = sw.getBuffer();
            if (count==0) {
                sb = sb.delete(sb.length() - 10, sb.length());
            } else {
                sb = sb.delete(sb.length() - 10, sb.length());
                sb = sb.delete(0, sb.indexOf("<",1)+1);
            }
            os.write(sw.toString().getBytes()); 
        } catch(IOException e) { log(e); }
    }

    @Override
    public void destroy() {
        if (dumpFile==null) {
            return;
        } else if (Files.isRegularFile(Paths.get(dumpFile))) {
            try {
                Files.delete(Paths.get(dumpFile));
                log("destroyed " + dumpFile + " ;-)"); 
                dumpFile=null;
            } catch(IOException e) { log(e); }
        }
    }

    // make some guess on whether this is a filename or already xslt
    private void createTransformer(String xslt) {
        if (xslt.startsWith("/")) {
            this.transformer = new  XMLTransformer(FileUtil.read(xslt));
        } else {
            this.transformer = new XMLTransformer(xslt);
        }
    }

    private static final Logger logger =
                         Logger.getLogger(DumpStorage.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception e) {
        e.printStackTrace();
        log(e.toString());
    }

}
