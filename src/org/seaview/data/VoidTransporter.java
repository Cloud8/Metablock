package org.seaview.data;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;
import org.shanghai.util.PrefixModel;
import org.shanghai.rdf.XMLTransformer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.DCTerms;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Never Mind
    @title Void Net Transporter
    @date 2015-06-10
*/
public class VoidTransporter implements MetaCrawl.Transporter {

    protected static final String dct = DCTerms.NS;
    private List<String> parts;

    public VoidTransporter() {
    }

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
    public synchronized Model read(String resource) {
        return PrefixModel.retrieve(resource);
    }

    @Override
    public String[] getIdentifiers(int off, int limit) {
        log("identifiers : " + off + " " + limit + " size " + parts.size());
        if (parts.size()<off+limit) {
            List<String> subList = parts.subList(off, parts.size());
            subList.add((String)null);
            return subList.toArray(new String[subList.size()]);
        }
        List<String> subList = parts.subList(off, off + limit);
        return subList.toArray(new String[subList.size()]);
    }

    @Override
    public int crawl(String resource) {
        Model model = PrefixModel.retrieve(resource);
        if (model==null) {
            log("crawl " + resource + " zero size " + parts.size());
            return 0;
        }
        parts.clear();
        parts.add(resource);
        getParts(model);
        Set<String> hs = new HashSet<>();
		hs.addAll(parts);
		parts.clear();
		parts.addAll(hs);
        Collections.sort(parts);
        //int ii = 0;
        //for (String str : parts) {
        //    log(str + " " + (ii++));
        //}
        log("index " + resource + " size " + parts.size());
        return parts.size();
    }

    @Override
    public Model test(String resource) {
        log("test # " + resource);
        Model model = PrefixModel.retrieve(resource);
        return model;
    }

    private void getParts(Model model) {
        NodeIterator ni = model.listObjectsOfProperty(DCTerms.hasPart);
        while(ni.hasNext()) {
            RDFNode node = ni.next();
            if (node.isResource()) {
                Resource rc = node.asResource();
                parts.add(rc.getURI());
                //Model next = PrefixModel.retrieve(rc.getURI());
                //if (next!=null) {
                //    getParts(next);
                //}
            }
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
