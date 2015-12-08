package org.shanghai.data;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.ModelUtil;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Void Net Transporter
    @date 2015-06-10
*/
public class VoidTransporter implements MetaCrawl.Transporter {

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
    public int index(String uri) {
        Resource rc = read(uri);
        parts.clear();
        //parts.add(uri);
        getParts(rc);
        Set<String> hs = new HashSet<>();
		hs.addAll(parts);
		parts.clear();
		parts.addAll(hs);
        Collections.sort(parts);
        //int ii = 0; for (String str : parts) {
        //    log(str + " " + (ii++));
        //}
        log("index " + uri + " size " + parts.size());
        return parts.size();
    }

    @Override
    public synchronized Resource read(String uri) {
        Model model = ModelUtil.createModel().read(uri);
        if (model==null) {
            log("crawl " + uri + " zero size " + parts.size());
            return null;
        }
        return model.getResource(uri);
    }

    @Override
    public List<String> getIdentifiers(int off, int limit) {
        //log("identifiers : " + off + " " + limit + " size " + parts.size());
        if (parts.size()<off+limit) {
            List<String> subList = parts.subList(off, parts.size());
            subList.add((String)null);
            //return subList.toArray(new String[subList.size()]);
            return subList;
        }
        List<String> subList = parts.subList(off, off + limit);
        //return subList.toArray(new String[subList.size()]);
        return subList;
    }

    @Override
    public Resource test(String uri) {
        Resource rc = read(uri);
        if (uri.equals(rc.getURI())) {
            log("OK " + rc.getURI());
        } else {
            log("no match " + uri + " # " + rc.getURI());
        }
        return rc;
    }

    private void getParts(Resource rc) {
        if (rc==null) {
            log("bad resource");
        } else if (rc.hasProperty(RDF.type)) {
            String type = rc.getProperty(RDF.type).getResource().getLocalName();
            String tns = rc.getProperty(RDF.type).getResource().getNameSpace();
            if (tns.equals("http://rdfs.org/ns/void#")) {
                //log("getParts " + tns + " " + type + " " + rc.getURI());
                StmtIterator si = rc.listProperties(DCTerms.hasPart);
                while(si.hasNext()) {
                    RDFNode node = si.nextStatement().getObject();
                    if (node.isResource()) {
                        String uri = node.asResource().getURI();
                        //log("uri " + uri);
                        parts.add(uri);
                        getParts(read(uri));
                    }
                }
            }
        } else {
            log("No type " + rc.getURI() + " ?");
        }
    }

    private static final Logger logger =
                         Logger.getLogger(VoidTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception ex) {
        logger.info(ex.toString());
        ex.printStackTrace();
    }
}
