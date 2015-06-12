package org.shanghai.util;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.IOException;

import java.util.logging.Logger;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Retrieve Model from remote, set beloved standard prefixes
    @date 2015-05-12
*/
public final class PrefixModel {


    static final Logger log = Logger.getLogger(PrefixModel.class.getName());

    public static Model create() {
        Model model = ModelFactory.createDefaultModel();
        model = prefix(model);
        return model;
    }

    public static Model prefix(Model model) {
        if (model==null) return model;
        for(int i=0; i<9; i++) model.removeNsPrefix("ns"+i);
        model.setNsPrefix("aiiso", "http://purl.org/vocab/aiiso/schema#");
        //model.setNsPrefix("bibo", "http://purl.org/ontology/bibo/");
        model.setNsPrefix("dct", "http://purl.org/dc/terms/");
        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        model.setNsPrefix("fabio", "http://purl.org/spar/fabio/");
        model.setNsPrefix("ore", "http://www.openarchives.org/ore/terms/");
        //model.setNsPrefix("pro", "http://purl.org/spar/pro/");
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        model.setNsPrefix("void", "http://rdfs.org/ns/void#");
        model.setNsPrefix("c4o", "http://purl.org/spar/c4o/");
        //model.withDefaultMappings(PrefixMapping.Standard);
        return model;
    }

    public static Model retrieve(String uri) {
        boolean b = false;
        Model model = ModelFactory.createDefaultModel();
        model = prefix(model);
        try {
            model = retrieveRaw(uri, model);
            b = true;
        } catch(MalformedURLException e) { log.info(e.toString()); }
          catch(IOException e) { log.info(e.toString()); }
          catch(Exception e) { e.printStackTrace(); }
        finally {
            if (b) return model;
            log.info("retrieve " + uri + " " + b);
            return null;
        }
    }

    public static Model retrieveRaw(String uri) 
        throws MalformedURLException, IOException {
        Model model = ModelFactory.createDefaultModel();
        return retrieveRaw(uri, model);
    }

    public static Model retrieveRaw(String uri, Model model) 
        throws MalformedURLException, IOException {
        try {
            URL url = new URL(uri);
            URLConnection urlc = url.openConnection();
            urlc.setRequestProperty("Accept", "application/rdf+xml");
            urlc.connect();
            String type = urlc.getContentType();
            if (type==null) {
            } else if (type.equals("application/rdf+xml")) {
                InputStream in = urlc.getInputStream();
                model.read(in, uri);
                in.close();
                model.setNsPrefix("dct", "http://purl.org/dc/terms/");
                return model;
            } else {
            }
        } catch(MalformedURLException e) { throw(e); }
          catch(IOException e) { throw(e); }
        return null;
    }

    public static String resolveDOI(String doiurl)
        throws MalformedURLException, IOException {
        try {
            URL url = new URL(doiurl);
            URLConnection urlc = url.openConnection();
            urlc.connect();
            InputStream in = urlc.getInputStream();
            String redirect = urlc.getURL().toString();
            in.close();
            if (redirect.endsWith("/")) {
                redirect = redirect.substring(0, redirect.length()-1);
            }
            return redirect;
        } catch(MalformedURLException e) { throw(e); }
          catch(IOException e) { throw(e); }
    }
}
