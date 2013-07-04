package org.shanghai.jena;

import java.util.logging.Logger;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFReader;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.update.UpdateAction;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.arp.JenaReader;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title Jena TDB Writer. 
   @date 2013-03-16
*/
public class TDBWriter {

    private TDBReader tdbReader;
    private static final Logger logger =
                         Logger.getLogger(TDBWriter.class.getName());
    private boolean created = false;
    private int count;

    public TDBWriter(String storage, String graph) {
        this.tdbReader = new TDBReader(storage, graph);
    }

    public TDBWriter(TDBReader tdbReader) {
        this.tdbReader = tdbReader;
        created = true;
    }

    private void log(String msg) {
        //logger.info(msg);    
        System.out.println("TDBWriter: " + msg);
    }

    private void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    public void create() {
        if (!created)
            tdbReader.create();
    }

    public void dispose() {
        if (created)
            tdbReader.dispose();
    }

    /** delete knowledge about resource before adding new statements */
    public boolean update(String about) {
        return update(about, false);
    }

    /** just add the statements */
    public boolean add(String about) {
        return update(about, true);
    }

    private boolean update(String what, boolean create) {
        if (what==null)
            return false;
        boolean b = false;
        try {
            InputStream in = new ByteArrayInputStream(what.getBytes("UTF-8"));
            b = this.update(in, create);
            if (in!=null) in.close();
        } catch(IOException e) { log(e); }
        finally { return b; }
    }

    public Model getModel(InputStream in) {
        Model m = ModelFactory.createDefaultModel();
        RDFReader reader = new JenaReader(); 
        reader.read(m, in, null);
        return m;
    }

    private boolean update(InputStream in, boolean create) {
        //Model m = tdbReader.newModel();
        //RDFReader reader = new JenaReader(); 
        //reader.read(m, in, null);
        Model m = getModel(in);
        String about = tdbReader.getSubject(m);
        log(about);
        if (create)
            tdbReader.delete(about);
        tdbReader.add(m);
        return true;
    }

}
