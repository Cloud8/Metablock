package org.shanghai.crawl;

import org.shanghai.bones.BiblioRecord;
import org.shanghai.jena.TDBReader;

import thewebsemantic.Bean2RDF;
import thewebsemantic.binding.Jenabean;

import java.util.logging.Logger;
import java.util.Iterator;

import java.io.StringWriter;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
        public void create();
        public void dispose();
        public BiblioRecord getRecord(File file);
        public String getDescription(File file);
        public void setStartDirectory(String dir);
    }

    public TDBTransporter(String storage) {
        this.tdbReader = new TDBReader(storage);
    }

    /** Be prepared to graphs */
    public TDBTransporter(String storage, String uri) {
        this.tdbReader = new TDBReader(storage, uri);
    }

    private TDBReader tdbReader;
    private Scanner fileScanner;
    private static final Logger logger =
                         Logger.getLogger(TDBTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    @Override
    public void create() {
        tdbReader.create();
        fileScanner = new FileScanner();
        fileScanner.create();
    }

    @Override
    public void dispose() {
        tdbReader.dispose();
        fileScanner.dispose();
    }

    @Override
    public void clean() {
        tdbReader.clean();
    }

    @Override
    public void setStartDirectory(String dir) {
        fileScanner.setStartDirectory(dir);
    }

    //private QueryExecution getExecutor(String q) {
    //    return tdbReader.getExecutor(q);
    //}

    @Override
    public String read(String resource) {
        String query = "CONSTRUCT { "
                     + "<" + resource + ">" + " ?p ?o }"
                     + " where { "
                     + "<" + resource + ">" + " ?p ?o }";
        String result = null;
        QueryExecution qexec = tdbReader.getExecutor(query);
        try {
            Model model;
            model = qexec.execConstruct();
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

    /** GH201302 : TODO */
    private boolean update(File file, boolean create) {
        if (file.getName().endsWith(".rdf")) {
            boolean b = update(readFile(file), create);
            if (!b) log("update failed for " + file.getAbsolutePath());
            return b;
        } else if (file.getName().endsWith(".php")) {
            String rdf = fileScanner.getDescription(file);
            //boolean b = update(rdf, create);
            //if (!b) log("update failed for " + file.getAbsolutePath());
            boolean b = true;
            //System.out.println(rdf);
            return b;
        } else if (file.getName().endsWith(".java")) {
            BiblioRecord bib = fileScanner.getRecord(file);
            boolean b = update(bib, create);
            if (!b) log("update failed for " + file.getAbsolutePath());
            return b;
        }
        return false;
    }

    private String readFile(File f) {
        try {
            return FileUtil.readFile(f);
        } catch(IOException e) { log(e); }
        return null;
    }

    private boolean update(BiblioRecord bib, boolean create) {
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefix("dct", "http://purl.org/dc/terms/");
        m.setNsPrefix("sem", "http://thewebsemantic.com/");
        m.setNsPrefix("shi", "http://shanghai.org/");
        Jenabean.instance().bind(m);
        Bean2RDF writer = new Bean2RDF(m);
        log("about " + bib.getId());
        writer.save(bib);
        // m.write(System.out, "RDF/XML-ABBREV");
        String about = getSubject(m);
        if (about==null) {
            log("about zero");
            return false;
        } else {
            log("about " + about);
            if (!create) {
                tdbReader.delete(about);
            }
            tdbReader.add(m);
        } 
        return true;
    }

    /** create or update an entity */
    private boolean update(String what, boolean create) {
        if (what==null)
            return false;
        boolean b = false;
        try {
            InputStream in = new ByteArrayInputStream(what.getBytes("UTF-8"));
            b = this.update(in, create);
            if (in!=null) in.close();
            //if (!b) log("failed " + what);
        } catch(IOException e) { log(e); }
        finally { return b; }
    }

    private boolean update(InputStream in, boolean create) {
        Model m;
        // if (graph==null)
            m = ModelFactory.createDefaultModel();
        // else
        //     m = ModelFactory.createModelForGraph(model.getGraph());
        RDFReader reader = new JenaReader(); 
        reader.read(m, in, null);
        String about = getSubject(m);
        if (about==null) {
            //log("about zero");
            return false;
        } else {
            if (!create) {
                tdbReader.delete(about);
            } else {
                log("add " + about);
                tdbReader.add(m);
            }
        }
        return true;
    }

    private String getSubject(Model m) {
        String result = null;
        ResIterator iter = m.listSubjects();
        try {
            while ( iter.hasNext() && result==null) {
                Resource s = iter.nextResource();
                if ( s.isURIResource() ) {
                    result = s.getURI();
                    // System.out.print("URI " + s.getURI());
                } else if ( s.isAnon() ) {
                    // System.out.print("blank");
                }
            }
        } finally {
            if ( iter != null ) iter.close();
            return result;
        }
    }

}
