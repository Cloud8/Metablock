package org.shanghai.data;

import org.shanghai.rdf.XMLTransformer;
import org.shanghai.crawl.MetaCrawl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop
  @title Write Model to Console
  @date 2015-07-01
*/
public class EmptyStorage implements MetaCrawl.Storage {

    private XMLTransformer transformer;
    private boolean console = false;

    public EmptyStorage() {
    }

    public EmptyStorage(boolean console) {
        this.console = console;
    }

    @Override
    public void create() {
        transformer = new XMLTransformer();
        transformer.create();
    }

    @Override
    public void dispose() {
        transformer.dispose();
    }

    @Override
    public boolean test(Resource rc) {
        if (console)
            System.out.println(transformer.asString(rc));
        System.out.println("console test: " + rc.getURI());
        return true;
    }

    @Override
    public boolean delete(String resource) {
        if (console)
            System.out.println("console delete: " + resource); 
        return true;
    }

    @Override
    public boolean write(Resource rc) {
        if (console)
            System.out.println(transformer.asString(rc));
        //else System.out.println(rc.getURI());
        return true;
    }

    @Override
    public void destroy() {
        System.out.println("destroying console ;-)"); 
    }

}
