package org.shanghai.crawl;

import org.shanghai.jena.TDBReader;
import org.shanghai.jena.TDBWriter;
import org.shanghai.util.FileUtil;
import org.shanghai.bones.FileScanner;

import java.util.logging.Logger;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.io.StringWriter;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.DC;

import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.ResIterator;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.rdf.arp.JenaReader;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title Transporter for RDF data
   @date 2013-02-21
*/
public class TDBTransporter implements FileCrawl.Transporter {

    public interface Scanner {
        public Scanner create();
        public void dispose();
        public void setStartDirectory(String dir);
        public boolean canTalk(File file);
        public Model getModel(File file);
    }

    public TDBTransporter(String storage) {
        this.tdbReader = new TDBReader(storage);
    }

    /** Be prepared to graphs */
    public TDBTransporter(String storage, String uri) {
        this.tdbReader = new TDBReader(storage, uri);
    }

    private TDBReader tdbReader;
    private TDBWriter tdbWriter;
    private List<Scanner> scanner;
    private static final Logger logger =
                         Logger.getLogger(TDBTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
        //e.printStackTrace(System.out);
    }

    @Override
    public void create() {
        tdbReader.create();
        tdbWriter = new TDBWriter(tdbReader);
        tdbWriter.create();
        scanner = new ArrayList<Scanner>();
        addScanner(new TrivialScanner().create());
        // addScanner(new FileScanner(base).create());
    }

    @Override
    public void dispose() {
        tdbReader.dispose();
        tdbWriter.dispose();
        for(Scanner s: scanner)
            s.dispose();
    }

    @Override 
    public void addScanner(Scanner s) {
         scanner.add(s); 
    }

    @Override
    public void clean() {
        tdbReader.clean();
    }

    @Override
    public void setStartDirectory(String dir) {
        for(Scanner s: scanner)
            s.setStartDirectory(dir);
    }

    @Override
    /** very simple default resource reader */
    public String readAsString(String resource) {
        String query = "CONSTRUCT { "
                     + "<" + resource + ">" + " ?p ?o }"
                     + " where { "
                     + "<" + resource + ">" + " ?p ?o }";
        String result = null;
        QueryExecution qexec = tdbReader.getExecutor(query);
        try {
            Model model;
            model = qexec.execConstruct();
            model.setNsPrefix("dct", DCTerms.NS);
            StringWriter out = new StringWriter();
            model.write(out, "RDF/XML-ABBREV");
            result = out.toString();
         } catch(Exception e) { log(query); log(e); }
           finally {
           qexec.close();
           return result;
         }
    }

    @Override
    public void delete(String about) {
        tdbReader.delete(about);
    }

    @Override
    public boolean create(File file) {
        //log("create " + file.getName());
        return update(file, true);
    }

    @Override
    public boolean update(File file) {
        //log("update " + file.getName());
        return update(file, false);
    }

    private boolean update(File file, boolean create) {
        for (Scanner s : scanner) {
		     if (s.canTalk(file)) {
		         Model m = s.getModel(file);
                 if (m==null)
                     continue;
                 if (create)
                     tdbReader.remove(m);
                 if (tdbReader.save(m)) {
                     //log("update for " + tdbReader.getSubject(m));
                     return true;
                 } else {
                     log("FAILED " + file.getAbsolutePath());
                     return false;
                 }
             }
        }
        //log("No scanner for " + file.getName());
        return false;
    }

    private class TrivialScanner implements Scanner {
        public Scanner create() {
            return this;
        }
        public void dispose() {}
        public void setStartDirectory(String dir) {}
        public boolean canTalk(File file) {
            if (file.getName().endsWith(".rdf")) 
                return true;
            return false;
        }
        public Model getModel(File file) {
            try {
                InputStream in = new FileInputStream(file);
                Model m = tdbWriter.getModel(in);
                in.close();
                return m;
            } catch(FileNotFoundException e) { log(e); }
              catch(IOException e) { log(e); }
            return null;
        }
    }

}
