package org.shanghai.rdf;

import org.shanghai.util.FileUtil;
import org.shanghai.store.Store;

import com.hp.hpl.jena.rdf.model.Model;
import java.util.logging.Logger;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop 
   @title RDF Transporter with CRUD functionality
   @date 2013-01-17
*/
public class RDFTransporter implements RDFCrawl.Transporter {

    public interface Storage {
        public void create(); 
        public void dispose(); 
        public String probe(String query); 
        public Model read(String resoure);
        public String[] getIdentifiers(String query, int off, int limit);
    }

    private Storage storage;

    public RDFTransporter(String sparqlService, 
            String probe, String enum_, String dump, String date) {
        this.sparqlService = sparqlService;
        this.probeQueryFile = probe;
        this.indexQueryFile = enum_;
        this.descrQueryFile = dump;
        this.date = date;
    }

    private String sparqlService;
    private String probeQuery;
    private String indexQuery;
    private String dumpQuery;
    private String probeQueryFile;
    private String indexQueryFile;
    private String descrQueryFile;
    private String date;

    private int size;

    @Override
    public void create() {
        size=0;
        probeQuery = FileUtil.read(probeQueryFile);
        indexQuery = FileUtil.read(indexQueryFile);
        if (indexQuery==null || indexQuery.trim().length()==0) 
            log("Everything wrong.");
        if (date!=null) {
            probeQuery = probeQuery.replace("<date>", date);
            indexQuery = indexQuery.replace("<date>", date);
        } else {
            probeQuery = probeQuery.replace("<date>", "1970-01-01");
            indexQuery = indexQuery.replace("<date>", "1970-01-01");
        }
        dumpQuery = FileUtil.read(descrQueryFile);
        storage = new RDFStorage(new Store(sparqlService,dumpQuery));
        storage.create();
    }

    @Override
    public void dispose() {
        storage.dispose();
    }

    @Override
    public void probe() {
        if(probeQuery==null) {
            log("no probe query");
            return;
        }
        String result = storage.probe(probeQuery);
        log("probed result: " + result);
    }

    @Override
    public Model read(String resource) {
        return storage.read(resource);
    }

    @Override
    public String[] getIdentifiers(int offset, int limit) {
        return storage.getIdentifiers(indexQuery, offset, limit);
    }

    private static final Logger logger =
                         Logger.getLogger(RDFTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

}
