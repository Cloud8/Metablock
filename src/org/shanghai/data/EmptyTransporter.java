package org.shanghai.data;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.ModelUtil;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.ArrayList;
import java.util.logging.Logger;

public class EmptyTransporter implements MetaCrawl.Transporter {

    private List<String> parts;

    @Override
    public void create() {
        parts = new ArrayList<String>();
    }

    @Override
    public void dispose() {
        parts.clear();
    }

    @Override
    public String probe() {
        return "probed.";
    }

    @Override
    public Resource read(String uri) {
        Model model = ModelUtil.createModel();
        Resource rc = model.createResource(uri);
        return rc;
    }

    @Override
    public Resource test(String uri) {
        return read(uri);
    }

    @Override
    public List<String> getIdentifiers(int off, int limit) {
        return parts;
    }

    @Override
    public int index(String uri) {
        return 0;
    }
}
