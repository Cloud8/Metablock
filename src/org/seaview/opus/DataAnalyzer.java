package org.seaview.opus;

import org.shanghai.crawl.MetaCrawl.Analyzer;
import org.shanghai.util.ModelUtil;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.DCTypes;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.ResourceUtils;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/** 
  * @title Archive Datastreams to Storage Backend
  * @date 2016-04-10 
  */
public class DataAnalyzer implements Analyzer {

    public interface Backend {
        public Backend create();
        public void dispose();
        public void test();
        public String writeCover(Resource rc);
        public String writeCover(Resource rc, BufferedImage image);
        public String writeIndex(Resource rc, String url);
        public String writePart(Resource rc, Resource obj);
    }

    private Backend backend;

    public DataAnalyzer(Backend backend) {
        this.backend = backend;
    }

    @Override
    public Analyzer create() {
        backend.create();
        return this;
    }

    @Override
    public void dispose() {
        backend.dispose();
    }

    @Override
    public String probe() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Resource analyze(Resource rc) {
        Resource sub = null;
        String name = rc.getPropertyResourceValue(DCTerms.type).getLocalName();
        StmtIterator si = rc.listProperties(DCTerms.isPartOf);
        while(si.hasNext()) {
            Resource obj = si.nextStatement().getResource();
            name = obj.getPropertyResourceValue(DCTerms.type).getLocalName();
            if (name.equals("JournalIssue")) {
                sub = getPartOf(obj);
                sub.addProperty(DCTerms.hasPart, rc);
            }
        }
        boolean b = writeParts(rc); // write article
        // b = true ; // write issue anyway
        if (b && sub!=null) {
            b = writeParts(sub); // write issue
            b = !b;
        }
        return rc;
    }

    @Override
    public Resource test(Resource rc) {
        log("test # " + rc.getURI());
        backend.test();
        return analyze(rc);
    }

    // return a copy of isPartOf Resource 
    private Resource getPartOf(Resource rc) {
        Model model = ModelUtil.createModel();
        StmtIterator si = rc.listProperties();
        while( si.hasNext() ) {
            Statement stmt = si.nextStatement();
            model.add(stmt);
            if (stmt.getObject().isResource()) {
                Resource obj = stmt.getObject().asResource();
                StmtIterator sub = obj.listProperties();
                while( sub.hasNext() ) {
                    model.add(sub.nextStatement());
                }
            }
        }
        return model.getResource(rc.getURI());
    }

    /** write resource parts to storage backend */
    private boolean writeParts(Resource rc) {

        String result = null;
        if (rc.hasProperty(DCTerms.source)) {
            RDFNode obj = rc.getProperty(DCTerms.source).getObject();
            if (obj.isResource()) {
                result = backend.writeIndex(rc, obj.asResource().getURI());
            } else {
                // result = backend.writeIndex(rc, rc.getURI());
            }
        } else {
            // result = backend.writeIndex(rc, rc.getURI());
        }
        if (result==null) {
            // log("failed to write index " + rc.getURI());
        }

        String cover = backend.writeCover(rc);
        if (cover!=null) {
            rc.removeAll(FOAF.img);
            rc.addProperty(FOAF.img, cover);
		}

        HashMap<Resource,String> hash = new HashMap<Resource,String>();
        StmtIterator si = rc.listProperties(DCTerms.hasPart);
        while (si.hasNext()) {
            Resource obj = si.nextStatement().getResource();
            if (obj.getURI().endsWith("/All.pdf")) {
                // skip this
            } else {
                String uri = backend.writePart(rc, obj);
                if (uri==null) {
                    // nothing
                } else {
			        hash.put(obj, uri);
                }
            }
        }
        for(Map.Entry<Resource, String> entry : hash.entrySet()) {
            Resource obj = entry.getKey();
            //log("rename " + obj.getURI() + " to [" + entry.getValue() + "]");
            if (entry.getValue().length()>0) {
                obj = ResourceUtils.renameResource(obj, entry.getValue()); 
            } else {
                log("failed to rename " + obj.getURI());
            }
        }
        hash.clear();

        // there are no parts but this may be a simple resource description
        if (!rc.hasProperty(DCTerms.hasPart)) {
            String uri = backend.writePart(rc, rc);
            ResourceUtils.renameResource(rc, uri);
        }
        return true;
    }

    private void log(Exception e) {
        logger.info(e.toString());
    }

    private void log(String msg) {
        logger.info(msg);
    }

    private static final Logger logger =
                         Logger.getLogger(DataAnalyzer.class.getName());

}

