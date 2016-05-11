package org.shanghai.data;

import org.shanghai.rdf.XMLTransformer;
import org.shanghai.crawl.MetaCrawl;
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

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Krystoff Nieszczęście
  @title Write Models to Dump File
  @date 2015-11-16
*/
public class DumpStorage implements MetaCrawl.Storage {

    private Model dump = null;
    private String dumpFile = null;
    private OutputStream os;
    private XMLTransformer transformer = null;

    public DumpStorage(String dumpFile) {
        this.dumpFile = dumpFile;
    }

    public DumpStorage(OutputStream os) {
        this.os = os;
    }

    public DumpStorage(String dumpFile, String xslt) {
        this.dumpFile = dumpFile;
        this.transformer = new XMLTransformer(xslt);
    }

    public DumpStorage(OutputStream os, String xslt) {
        this.os = os;
        this.transformer = new XMLTransformer(xslt);
    }

    @Override
    public void create() {
        if (dumpFile==null) {
            //
        } else try {
            os = new FileOutputStream(dumpFile);
        } catch(FileNotFoundException e) { log(e); }
        if (transformer==null) {
            //
        } else {
            transformer.create();
        }
    }

    @Override
    public void dispose() {
        try {
            os.close();
        } catch(IOException ex) { log(ex); }
        log("disposed [" + dumpFile + "]");
        if (transformer!=null) {
            transformer.dispose();
        }
    }

    @Override
    public boolean test(Resource rc) {
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
            RDFDataMgr.write(os, rc.getModel(), RDFLanguages.RDFXML) ;
        } else try {
            String xml = transformer.transform(rc);
            os.write(xml.getBytes(Charset.forName("UTF-8")));
        } catch(IOException e) { log(e); }
        return true;
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
