package org.seaview.opus;

import org.seaview.opus.REST;

import org.shanghai.rdf.XMLTransformer;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.ModelUtil;
import org.shanghai.util.FileUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

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
        return true;
    }

    @Override
    public boolean write(Resource rc) {
        //String resource = rc.getURI().substring(rc.getURI().indexOf("/",7));
        String resource = FedoraTransporter.base(rc.getURI());
        if (resource.endsWith(".pdf")) {
            resource = base + resource.substring(0,resource.length()-4)+".rdf";
        } else {
            resource = base + resource + "/about.rdf";
        }
        //String format = "application/rdf+xml";
        String format = "application/xml";
        // log("write " + rc.getURI() + " # " + resource);
        ByteArrayOutputStream baos = ModelUtil.getBaos(rc);
        String result = REST.put(resource, baos, format, user, pass);
        if (result.equals(resource)) {
            return true;
        } else if (result.length()==0) {
            log("result [" + result + "] " + resource);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void destroy() {
        FedoraTransporter fed = new FedoraTransporter(user, pass, base, base, null);
        fed.create();
        int size = Integer.parseInt(fed.probe());
        for (String uri : fed.getIdentifiers(0, size)) {
            delete(uri);
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
