package org.shanghai.data;

import org.shanghai.crawl.MetaCrawl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public class EmptyStorage implements MetaCrawl.Storage {

    @Override
    public void create() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean test(Resource rc, String resource) {
        return true;
    }

    @Override
    public boolean delete(String resource) {
        //System.out.println("empty delete"); 
        return true;
    }

    @Override
    public boolean write(Resource rc, String resource) {
        //System.out.println("empty write"); 
        //System.out.println(); 
        return true;
    }

    //@Override
    //public boolean update(String id, String field, String value) {
    //    //System.out.println("empty update"); 
    //    return true;
    //}

    @Override
    public void destroy() {
    }

}
