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
        return false;
    }

    @Override
    public boolean write(Model model) {
        return true;
    }

    @Override
    public boolean update(Model mod) {
        return write(mod);
    }

    @Override
    public void destroy() {
    }

}
