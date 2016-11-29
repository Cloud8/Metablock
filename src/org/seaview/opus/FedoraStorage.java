package org.seaview.opus;

import org.seaview.opus.REST;

import org.shanghai.rdf.XMLTransformer;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.ModelUtil;
import org.shanghai.util.FileUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @title Write RDF Model to Fedora Commons Repository
  @date 2016-05-35
*/
public class FedoraStorage implements MetaCrawl.Storage {

    private String user;
    private String pass;
    private String base;

    private boolean test = false;
    private static final String LDP = "http://www.w3.org/ns/ldp#";

    public FedoraStorage(String user, String pass, String base) {
        this.user= user;
        this.pass= pass;
        this.base = base;
    }

    @Override
    public void create() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean test(Resource rc) {
        String resource = rc.getURI().substring(rc.getURI().indexOf("/",7));
        String container = base + resource;
        int status = REST.head(container, user, pass);
        boolean b = false;
        if (status==404) {
            // result = REST.put(container, user, pass);
            // log("put " + result + " # " + status);
        } else if (status==200) { 
            b = true; // ok.
        } else if (status==410) {
            // result = REST.put(container, user, pass);
            log("gone " + container + " # " + status);
        } else {
            log("head " + container + " # " + status);
        }
        return b;
    }

    @Override
    public boolean delete(String resource) {
        String result = REST.delete(resource, user, pass);
        System.out.println("delete: " + resource + " [" + result + "]"); 
        // remove tombstone too
        result = REST.delete(resource + "/fcr:tombstone", user, pass);
        return true;
    }

    @Override
    public boolean write(Resource rc) {
        //String resource = rc.getURI().substring(rc.getURI().indexOf("/",7));
        String resource = FedoraTransporter.base(base, rc.getURI());
        if (resource.charAt(resource.length()-4)=='.') {
            resource = resource.substring(0,resource.length()-4)+".rdf";
        } else {
            resource = resource + "/about.rdf";
        }
        //String format = "application/rdf+xml";
        String format = "application/xml";
        //log("write " + rc.getURI() + " # " + resource);
        ByteArrayOutputStream baos = ModelUtil.getBaos(rc);
        String result = REST.put(resource, baos, format, user, pass);
        if (result.equals(resource)) {
            return true;
        } else if (result.length()==0) {
            //result = REST.post(resource, baos, format, user, pass);
            log("result [" + result + "] " + resource);
            return true;
        } else {
            log("result [" + result + "] " + resource);
            return false;
        }
    }

    @Override
    public void destroy() {
        Model model = ModelUtil.createModel().read(base);
        Resource subject = model.getResource(base);
        Property contains = model.createProperty(LDP, "contains");
        StmtIterator si = subject.listProperties(contains);
        while(si.hasNext()) {
            Statement st = si.nextStatement();
            if (st.getObject().isResource()) {
                delete(st.getResource().getURI());
            }
        }
    }

    private void log(Exception e) {
        logger.info(e.toString());
        e.printStackTrace();
    }

    private void log(String msg) {
        logger.info(msg);
    }

    private static final Logger logger =
                         Logger.getLogger(FedoraStorage.class.getName());

}
