package org.shanghai.crawl;

import org.shanghai.store.Store;
import org.shanghai.rdf.RDFReader;

import java.util.logging.Logger;

import java.io.StringWriter;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.DCTerms;

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
    public boolean test(String resource) {
        Model test = store.read(resource);
        if (test==null)
            return false;
        return true;
    }

    @Override
    public boolean write(Model mod, String id) {
        return store.write(mod);
    }

    @Override
    public boolean update(String id, String field, String value) {
        Model model = store.read(id);
        Resource rc = model.getResource(id);
        rc.addProperty(model.createProperty(DCTerms.getURI(), field), value);
        return store.write(model);
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
