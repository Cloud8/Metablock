package org.seaview.opus;

import org.seaview.opus.REST;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;
import org.shanghai.rdf.XMLTransformer;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.DCTypes;
import org.apache.jena.sparql.vocabulary.FOAF;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.XML;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.lang.StringBuilder;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Meta Block
    @title DSpace REST Transporter
    @date 2015-10-10
    @abstract see http://localhost:8080/opus/
*/
public class DSpaceTransporter implements MetaCrawl.Transporter {

    private List<String> items;
    private List<Record> streams;
    private XMLTransformer transformer = null;
    private String rest;
    private String test;

    public DSpaceTransporter(String rest, String xslt, String test) {
        this.rest = rest;
        this.transformer = new XMLTransformer(FileUtil.readResource(xslt));
        this.test = test;
    }

    @Override
    public void create() {
        items = new ArrayList<String>();
        streams = new ArrayList<Record>();
        transformer.create();
    }

    @Override
    public void dispose() {
        items.clear();
        streams.clear();
        transformer.dispose();
    }

    @Override
    public String probe() {
        return REST.get(rest + "/test");
    }

    @Override
    public Resource read(String resource) {
        String data = REST.get(rest + "/items/" + resource + "/metadata");
        JSONArray json = new JSONArray(data);
        String xml = "<document>" + XML.toString(json) + "</document>";
        Resource rc = transformer.transform(xml);
        addStreams(rc, resource);
        return rc;
    }

    @Override
    public List<String> getIdentifiers(int off, int limit) {
        if (items.size()==0) {
            readItems(rest + "/items");
        }
        if (off>items.size()) {
            return null;
        }
        int to = (off+limit)>items.size()?items.size():off+limit;
        return items.subList(off, to);
    }

    private void readItems(String url) {
        String id;
        String name;
        String json = REST.get(url);
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject row = array.getJSONObject(i);
            //log(row.toString());
            id = row.getString("uuid");
            //handle = row.getString("handle");
            name = row.getString("name");
            //id = handle.substring(handle.lastIndexOf("/")+1);
            //log(id + " " + name);
            items.add(id);
        }
    }

    @Override
    public int index(String resource) {
        readItems(rest + "/items");
        //items.add(resource);
        //log("index " + resource + " size " + items.size());
        return items.size();
    }

    @Override
    public Resource test(String resource) {
        log("test # " + resource);
        String data = REST.get(rest + "/items/" + resource + "/metadata");
        //JSONObject json = new JSONObject(data);
        JSONArray json = new JSONArray(data);
        log(json.toString());
        String xml = "<document>" + XML.toString(json) + "</document>";
        //log(xml);
        FileUtil.write(test, xml);
        log("wrote " + test);
        Resource rc = transformer.transform(xml);
        addStreams(rc, resource);
        return rc;
    }

    private void addStreams(Resource rc, String resource) {
        for (Record item : getStreams(resource)) {
            // "Adobe PDF" "image/png" "License"
            switch (item.format) {
            case "image/png":
                rc.addProperty(FOAF.img, item.uri);
                break;
            case "application/rdf+xml":
                log("skipped rdf metadata");
                break;
            case "application/pdf":
            case "Adobe PDF":
                Resource file = rc.getModel()
                                  .createResource(item.uri, DCTypes.Text);
                file = file.addProperty(DCTerms.format, "application/pdf");
                rc.addProperty(DCTerms.hasPart, file);
                break;
            case "MP4 Video":
                Resource video = rc.getModel()
                         .createResource(item.uri, DCTypes.MovingImage);
                video = video.addProperty(DCTerms.format, "video/mp4");
                rc.addProperty(DCTerms.hasPart, video);
                break;
            default: 
                log("skipped " + item.format);
                break;
            }
        }
    }

    class Record {
        public Record(String uri, String format) {
            this.uri = uri;
            this.format = format;
        }
        public String uri;
        public String format;
    }

    private synchronized List<Record> getStreams(String resource) {
        String id;
        String name;
        String link;
        String format;
        streams.clear();
        //log("getStreams # " + resource);
        String data = REST.get(rest + "/items/" + resource + "/bitstreams");
        JSONArray array = new JSONArray(data);
        //log(data);
        for (int i = 0; i < array.length(); i++) {
            JSONObject row = array.getJSONObject(i);
            id = row.getString("uuid");
            name = row.getString("name");
            link = row.getString("retrieveLink");
            format = row.getString("format");
            //log(id + " " + name + " " + link);
            //log(row.toString());
            if (format.equals("License")) {
                //suppress for now
            } else if (name.endsWith(".original")) {
                //suppress for now
            } else if (format.equals("epub")) {
                //streams.add(new Record("epub:" + rest + link);
            } else {
                //streams.add(rest + link);
                streams.add(new Record(rest + link, format));
            }
        }
        return streams;
    }

    private static final Logger logger =
                         Logger.getLogger(DSpaceTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception ex) {
        logger.info(ex.toString());
        ex.printStackTrace();
    }
}
