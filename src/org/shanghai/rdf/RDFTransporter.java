package org.shanghai.rdf;

import org.shanghai.util.FileUtil;
import org.shanghai.store.Store;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Logger;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop 
   @title RDF Transporter with CRUD functionality
   @date 2013-01-17
*/
public class RDFTransporter implements MetaCrawl.Transporter {

    public interface Reader {
        public void create(); 
        public void dispose(); 
        public String probe(String query); 
        public Model read(String resoure);
        public String[] getIdentifiers(String query, int off, int limit);
    }

    private Reader reader;

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
        probeQuery = FileUtil.readResource(probeQueryFile);
        indexQuery = FileUtil.readResource(indexQueryFile);
        if (indexQuery==null || indexQuery.trim().length()==0) {
            log("Everything is wrong.");
        }
        if (date!=null) {
            probeQuery = probeQuery.replace("<date>", date);
            indexQuery = indexQuery.replace("<date>", date);
        } else {
            probeQuery = probeQuery.replace("<date>", "1970-01-01");
            indexQuery = indexQuery.replace("<date>", "1970-01-01");
        }
        dumpQuery = FileUtil.readResource(descrQueryFile);
        reader = new RDFReader(new Store(sparqlService,dumpQuery));
        reader.create();
        //log(probeQueryFile + ":" + indexQueryFile);
    }

    @Override
    public void dispose() {
        reader.dispose();
    }

    @Override
    public String probe() {
        //log("probed " + this.getClass().getName());
        if(probeQuery==null) {
            log("no probe query");
            return null;
        }
        String result = reader.probe(probeQuery);
        log("probed result: " + result);
        //if(result.equals("0"))
        //   log(probeQuery);
        return result;
    }

    @Override
    public Resource read(String resource) {
        Model model = reader.read(resource);
        return model.getResource(resource);
    }

    @Override
    public List<String> getIdentifiers(int offset, int limit) {
        //return reader.getIdentifiers(indexQuery, offset, limit);
        return Arrays.asList(reader.getIdentifiers(indexQuery, offset, limit));
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
