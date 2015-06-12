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
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.DCTerms;

import java.io.InputStream;
import java.util.List;
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
    //private Model model;
    Property hasPart;

    public VoidTransporter() {
    }

    @Override
    public void create() {
        parts = new ArrayList<String>();
        //model = null;
    }

    @Override
    public void dispose() {
        parts.clear();
        //model = null;
    }

    @Override
    public String probe() {
        return "VoidTransporter probed.";
    }

    @Override
    public synchronized Model read(String resource) {
        //log("read " + resource);
        Model model = PrefixModel.retrieve(resource);
        getParts(model);
        return model;
    }

    @Override
    public String[] getIdentifiers(int off, int limit) {
        log("identifiers : " + off + " " + limit + " size " + parts.size());
        if (parts.size()<=off) {
            return null;
        }
        if (parts.size()<off+limit) {
            List<String> subList = parts.subList(off, parts.size());
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
        hasPart = model.createProperty(dct, "hasPart");
        getParts(model);
        log("index " + resource + " size " + parts.size());
        return parts.size();
    }

    private void getParts(Model model) {
        NodeIterator ni = model.listObjectsOfProperty(hasPart);
        while(ni.hasNext()) {
            RDFNode node = ni.next();
            if (node.isResource()) {
                parts.add(node.asResource().getURI());
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
