package org.shanghai.crawl;

import org.shanghai.store.Store;
import org.shanghai.rdf.RDFReader;

import java.util.logging.Logger;

import java.io.StringWriter;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.vocabulary.DCTerms;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop
   @title Storage for RDF data
   @date 2013-10-21
*/
public class RDFStorage extends RDFReader implements MetaCrawl.Storage {

    public RDFStorage(String store, String graph) {
        super(new Store(store, graph));
    }

    public RDFStorage(String store, String kbd, String name) {
        super(new Store(store, kbd, name));
    }

    public RDFStorage(String store, String graph, String u, String p) {
        super(new Store(store, graph, u, p));
    }

    @Override
    public boolean test(Resource rc) {
        Model test = store.read(rc.getURI());
        if (test==null)
            return false;
        return true;
    }

    @Override
    public boolean write(Resource rc) {
        return store.write(rc.getModel());
    }

    @Override
    public boolean delete(String about) {
        return store.delete(about);
    }

    @Override
    public void destroy() {
        store.destroy();
    }

}
