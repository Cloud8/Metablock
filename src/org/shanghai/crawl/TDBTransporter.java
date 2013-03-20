package org.shanghai.crawl;

import org.shanghai.bones.BiblioRecord;
import org.shanghai.jena.Bean2RDF;
import org.shanghai.jena.TDBReader;
import org.shanghai.jena.TDBWriter;
import org.shanghai.util.FileUtil;

import java.util.logging.Logger;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.io.StringWriter;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

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
        public BiblioRecord getRecord(File file);
        public String getDescription(File file);
        public void setStartDirectory(String dir);
        public boolean canTalk(File file);
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
    private Bean2RDF writer;
    private List<Scanner> fileScanner;
    private static final Logger logger =
                         Logger.getLogger(TDBTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    private void createBeanWriter() {
        writer = new Bean2RDF(tdbReader);
        writer.create();
    }

    private void createRDFWriter() {
        tdbWriter = new TDBWriter(tdbReader);
        tdbWriter.create();
    }

    @Override
    public void create() {
        tdbReader.create();
        fileScanner = new ArrayList<Scanner>();
        addScanner(new FileScanner().create());
    }

    @Override
    public void dispose() {
        tdbReader.dispose();
        if (writer!=null)
            writer.dispose();
        if (tdbWriter!=null)
            tdbWriter.dispose();
        for(Scanner s: fileScanner)
            s.dispose();
    }

    @Override 
    public void addScanner(Scanner scanner) {
         fileScanner.add(scanner); 
    }

    @Override
    public void clean() {
        tdbReader.clean();
    }

    @Override
    public void setStartDirectory(String dir) {
        for(Scanner s: fileScanner)
            s.setStartDirectory(dir);
    }

    public BiblioRecord read(String resource) {
        return writer.read(resource);
    }

    @Override
    /** simple default resource reader */
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
        return update(file, true);
    }

    @Override
    public boolean update(File file) {
        return update(file, false);
    }

    private boolean update(File file, boolean create) {
        if (file.getName().endsWith(".rdf")) {
            boolean b = update(readFile(file), create);
            if (!b) log("update failed for " + file.getAbsolutePath());
            return b;
        } else {
            for (Scanner s : fileScanner) {
		        if (s.canTalk(file)) {
                    BiblioRecord bib = s.getRecord(file);
                    boolean b = save(bib);
                    if (!b) 
					    log("addBean failed for " + file.getAbsolutePath());
                    return b;
			    }
            }
		}
        return false;
    }

    private String readFile(File f) {
        return FileUtil.read(f);
    }

    /* GH2013-03-01 TODO: create or not */
    public boolean save(BiblioRecord bib) {
        if (writer==null)
            createBeanWriter();
        return writer.save(bib);
    }

    /** create or update an entity */
    private boolean update(String what, boolean create) {
        if (tdbWriter==null)
            createRDFWriter();
        if (create)
            return tdbWriter.update(what);
        else
            return tdbWriter.add(what);
    }

}
