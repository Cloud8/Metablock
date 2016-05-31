package org.seaview.opus;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;
import org.shanghai.util.ModelUtil;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.ArrayList;
import java.util.logging.Logger;

public class FedoraTransporter implements MetaCrawl.Transporter {

    private static final String LDP = "http://www.w3.org/ns/ldp#";
    private String user;
    private String pass;
    private String base;
    private String graph;
    private String testFile;
    private boolean indexed = false;
    private boolean test = false;

    private List<String> parts;

    public FedoraTransporter(String user, String pass, String base, 
        String graph, String file) {
        this.user= user;
        this.pass= pass;
        this.base = base;
        this.graph = graph;
        this.testFile = file;
    }

    @Override
    public void create() {
        parts = new ArrayList<String>();
        // log("create " + base);
    }

    @Override
    public void dispose() {
        parts.clear();
    }

    @Override
    public String probe() {
        if (parts.size()==0) {
            index(graph);
        }
        return "" + parts.size();
    }

    @Override
    public Resource read(String uri) {
        //String container = uri.substring(uri.indexOf("/",7));
        String resource = base + FedoraTransporter.base(uri);
        if (REST.head(resource, user, pass)==200) {
            Model model = ModelUtil.createModel().read(resource);
            return model.getResource(uri);
        }
        return null;
    }

    @Override
    public Resource test(String uri) {
        test = true;
        String resource = base + base(uri, ".rdf");
        log("read [" + resource + "] " + REST.head(resource, user, pass));
        return read(uri);
    }

    @Override
    public List<String> getIdentifiers(int off, int limit) {
        if (parts.size()==0 && !indexed) {
            index(graph);
        }
        if (parts.size()<off+limit) {
            List<String> subList = parts.subList(off, parts.size());
            subList.add((String)null);
            return subList;
        }
        List<String> subList = parts.subList(off, off + limit);
        return subList;
    }

    @Override
    public int index(String uri) {
        indexed = true;
        if (test) log("index " + uri);
        Model model = ModelUtil.createModel().read(base);
        Resource subject = model.getResource(base);
        //FileUtil.write(testFile, ModelUtil.asString(subject));
        Property contains = model.createProperty(LDP, "contains");
        StmtIterator si = subject.listProperties(contains);
        while(si.hasNext()) {
            Statement st = si.nextStatement();
            if (st.getObject().isResource()) {
                Resource obj = st.getResource();
                String muri = map(st.getResource().getURI());
                if (muri==null) {
                    // skip this
                } else if (muri.startsWith(uri)) {
                    //log("index " + muri);
                    parts.add(muri);
                } else {
                    if (test) log("add " + muri);
                    parts.add(muri);
                }
            }
        }
        return parts.size();
    }

    private String base(String url, String suffix) {
        String resource = url;
        if (resource.endsWith(".pdf")) {
            resource = base + resource.substring(0,resource.length()-4)+suffix;
        } else {
            resource = base + resource + "/about.rdf";
        }
        return resource;
    }

    static String base(String url) {
        if (url.startsWith("file://")) {
            return url.substring(7);
        } else if (url.startsWith("http://")) {
            return url.substring(url.indexOf("/",7));
        } else if (url.startsWith("https://")) {
            return url.substring(url.indexOf("/",8));
        }
        return url;
    }

    /* map from fedora uri to graph based uri */
    private String map(String uri) {
        if (uri.endsWith("/about.rdf")) {
            int x = uri.indexOf("rest/")+4;
            return graph + uri.substring(x,uri.length()-10);
        } else if (uri.endsWith(".rdf")) {
            int x = uri.indexOf("rest/")+4;
            return graph + uri.substring(x);
        } else {
            // log("zero " + uri);
        }
        return null;
    }

    private static Logger logger = Logger.getLogger(FedoraTransporter.class.getName());

    private static void log(String msg) {
        logger.info(msg);
    }

    private static void log(Exception e) {
        log(e.toString());
        try {
             throw(e);
        } catch(Exception ex) {}
    }

}
