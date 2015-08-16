package org.seaview.data;

import org.shanghai.rdf.XMLTransformer;
import org.shanghai.crawl.MetaCrawl;
import com.hp.hpl.jena.rdf.model.Model;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop
  @title Write Model to Console
  @date 2015-07-01
*/
public class ConsoleStorage implements MetaCrawl.Storage {

    private XMLTransformer transformer;

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
    public boolean test(String resource) {
        System.out.println("console delete: " + resource); 
        return true;
    }

    @Override
    public boolean delete(String resource) {
        System.out.println("console delete: " + resource); 
        return true;
    }

    @Override
    public boolean write(Model model, String resource) {
        System.out.println(transformer.asString(model));
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
