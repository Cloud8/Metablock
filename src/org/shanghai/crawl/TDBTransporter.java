package org.shanghai.crawl;

import org.shanghai.store.Store;
import org.shanghai.util.FileUtil;

import java.util.logging.Logger;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.io.StringWriter;
import java.io.StringReader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.rdf.arp.JenaReader;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop 
   @title Transporter for Filesystem data
   @date 2013-02-21
*/
public class TDBTransporter implements FileCrawl.Delegate {

    public interface Scanner {
        public Scanner create();
        public void dispose();
        public void setStartDirectory(String dir);
        public boolean canTalk(File file);
        public Model getModel(File file);
    }

    private List<Scanner> scanner;

    public TDBTransporter() {
    }

    @Override
    public void create() {
        scanner = new ArrayList<Scanner>();
        //addScanner(new TrivialScanner().create());
    }

    @Override
    public void dispose() {
        for(Scanner s: scanner)
            s.dispose();
    }

    @Override
    public void setDirectory(String dir) {
        for(Scanner s: scanner)
            s.setStartDirectory(dir);
    }

    @Override
    public boolean canRead(File file) {
        boolean b = false;
        for (Scanner s : scanner) {
		     if (s.canTalk(file)) {
                 b=true;
                 break;
             } else {
                 //log("canNotRead " + s.getClass().getCanonicalName() + " " + file.getName());
             }
        }
        return b;
    }

    @Override 
    public void addScanner(Scanner s) {
         //log("addScanner " + s.getClass().getCanonicalName());
         if (scanner==null)
             scanner = new ArrayList<Scanner>();
         scanner.add(s); 
    }

    @Override
    public Model read(String resource) {
        Model mod = null;
        File file = new File(resource);
        for (Scanner s : scanner) {
		     if (s.canTalk(file)) {
                 mod = s.getModel(file);
                 break;
             }
        }
        return mod;
    }

    private static final Logger logger =
                         Logger.getLogger(TDBTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
        //e.printStackTrace(System.out);
    }

}
