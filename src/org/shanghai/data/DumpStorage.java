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

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Krystoff Nieszczęście
  @title Write Models to Dump File, simple without memory management 
  @date 2015-11-16
*/
public class DumpStorage implements MetaCrawl.Storage {

    private Model dump = null;
    private String dumpFile = null;

    public DumpStorage(String dumpFile) {
        this.dumpFile = dumpFile;
    }

    @Override
    public void create() {
    }

    @Override
    public void dispose() {
        if (dumpFile==null) {
            return; 
        } else if (dump==null) {
            log("nothing to dump.");
        } else {
            log("writing " + dumpFile);
            ModelUtil.write(dumpFile, dump);        
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
        if (dump==null) {
		    dump = rc.getModel();
        } else {
		    dump.add(rc.getModel());
        }
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
