package org.shanghai.crawl;

import com.hp.hpl.jena.rdf.model.Model;

public class EmptyStorage implements MetaCrawl.Storage {

    @Override
    public void create() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean test(String resource) {
        return true;
    }

    @Override
    public boolean delete(String resource) {
        //System.out.println("empty delete"); 
        return true;
    }

    @Override
    public boolean write(Model model, String resource) {
        //System.out.println("empty write"); 
        //System.out.println(); 
        return true;
    }

    @Override
    public boolean update(String id, String field, String value) {
        //System.out.println("empty update"); 
        return true;
    }

    @Override
    public void destroy() {
    }

}
