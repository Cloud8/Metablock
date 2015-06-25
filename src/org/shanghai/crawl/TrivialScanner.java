package org.shanghai.crawl;

import org.shanghai.store.Store;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.arp.JenaReader;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop
   @title Scanner for RDF data
   @date 2013-02-21
*/
public class TrivialScanner implements FileTransporter.Delegate {

    private String current = "";

    public FileTransporter.Delegate create() {
        return this;
    }

    public void dispose() {}

    public void setDirectory(String dir) {}

    public boolean canRead(String fname) {
        if (fname.endsWith(".rdf")) {
            return true;
        }
        return false;
    }

    class TrivialErrorHandler implements RDFErrorHandler {
        public void error(Exception e) {
            log(current + " : " + e);
        }
        public void fatalError(Exception e) {
            e.printStackTrace();
        }
        public void warning(Exception e) {
        }
    }

    public Model read(String file) {
        try {
            //InputStream in = new FileInputStream(new File(file));
            Path path = Paths.get(file);
			InputStream in = path.getFileSystem().provider().newInputStream(path);
            Model m = ModelFactory.createDefaultModel();
            RDFReader reader = new JenaReader(); 
            reader.setErrorHandler(new TrivialErrorHandler());
            reader.read(m, in, null);
            in.close();
            return m;
        } catch(FileNotFoundException e) { 
            log(file); log(e);
        } catch(IOException e) { 
            log(file); log(e); 
        } catch(Exception e) { 
            e.printStackTrace(); 
            log(file);  
        }
        return null;
    }

    private static final Logger logger =
                         Logger.getLogger(FileTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception e) {
        //log(e.toString());
        e.printStackTrace();
        //System.exit(0);
    }

}
