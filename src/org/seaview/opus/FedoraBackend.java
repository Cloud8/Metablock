package org.seaview.opus;

import org.seaview.opus.DataAnalyzer.Backend;

import org.shanghai.util.FileUtil;
import org.shanghai.data.FileStorage;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.DCTypes;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.rdf.model.Resource;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.File;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.util.logging.Logger;

/** 
  * @title Fedora Backend for object storage
  * @date 2016-05-35 
  */
public class FedoraBackend implements Backend {

    private String user;
    private String pass;
    private String base;

    private boolean test;

    public FedoraBackend(String user, String pass, String base) {
        this.user = user;
        this.pass = pass;
        this.base = base;
    }

    @Override
    public Backend create() {
        return this;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void test() {
        test = true;
    }

    @Override
    public String writeIndex(Resource rc, String url) {
        log("writeIndex " + rc.getURI() + " # " + url);
        String container = base + FedoraTransporter.base(url) + "/index.html";
        ByteArrayOutputStream baos = FileUtil.load(url);
        String format = "text/html";
        String result = REST.put(container, baos, format, user, pass);
        return result;
    }

    @Override
    public String writeCover(Resource rc) {
        String result = null;
		if (rc.hasProperty(FOAF.img)) {
		    String source = rc.getProperty(FOAF.img).getString();
            if (test) {
                log(" writeCover " + source);
            } else {
                result = writeObject(source, "image/png");
                log("writeCover " + source + " # " + result);
		    }
		}
        return result;
    }

    @Override
    public String writeCover(Resource rc, BufferedImage image) {
        String url = rc.getURI() + "/cover.png";
        if (test) {
            log("writeCover " + url);
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "PNG", baos);
            } catch(IOException e) { log(e); }
            String container = base + FedoraTransporter.base(url) + "/cover.png";
            String format = "image/png";
            url = REST.put(container, baos, format, user, pass);
        }
        return url;
    }

    /** write DCTypes part */
    @Override
    public String writePart(Resource rc, Resource obj) {
        log("writePart " + rc.getURI() + " # " + obj.getURI());
        String result = null;
        String uri = obj.getURI();
        if (obj.hasProperty(RDF.type) && obj.getProperty(RDF.type)
               .getResource().getNameSpace().equals(DCTypes.NS)) {
            // should write this part
        } else if (obj.getURI().endsWith(".pdf")) {
            log("write " + obj.getURI());
            result = writeObject(obj.getURI(), "application/pdf");
            return result;
        } else {
            log("no write for " + obj.getURI());
            return null;
        }

        if (test) {
            String t = obj.getProperty(RDF.type).getResource().getLocalName();
            log(" should write " + obj.getURI() + " " + t);
        }
        if (obj.hasProperty(DCTerms.format)) {
            String format = obj.getProperty(DCTerms.format).getResource()
                               .getProperty(RDFS.label).getString();
            log("format " + format);
            switch(format) {
                case "application/pdf":
                    result = writeObject(obj.getURI(), format);
                    break;
                case "video/mp4":
                    result = writeObject(obj.getURI(), format);
                    break;
                case "application/xml":
                    result = writeObject(obj.getURI(), format);
                    break;
                case "application/zip":
                    result = writeObject(obj.getURI(), format);
                    break;
                default:
                    log("unknown format " + format);
            }
        }
        return result;
    }

    private String writeObject(String url, String format) {
        // log("writeObject [" + url + "]");
        ByteArrayOutputStream baos = FileUtil.load(url);
        String container = base + FedoraTransporter.base(url);
        String result = REST.put(container, baos, format, user, pass);
        return container;
    }

    /*
    private String base(String url) {
        if (url.startsWith("file://")) {
            return url.substring(7);
        } else if (url.startsWith("http://")) {
            return url.substring(url.indexOf("/",7));
        } else if (url.startsWith("https://")) {
            return url.substring(url.indexOf("/",8));
        }
        return url;
    }
    */

    private void log(Exception e) {
        logger.info(e.toString());
        e.printStackTrace();
    }

    private void log(String msg) {
        logger.info(msg);
    }

    private static final Logger logger =
                         Logger.getLogger(FedoraBackend.class.getName());

}

