package org.shanghai.solr;

import org.shanghai.rdf.XMLTransformer;
import org.shanghai.solr.SolrClient;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;
import org.shanghai.util.PrefixModel;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrDocument;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.arp.JenaReader;
import com.hp.hpl.jena.rdf.model.RDFReader;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.StringReader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import java.io.StringWriter;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @title Solr Store Transporter
  @date 2013-12-18
*/
public class SolrTransporter implements MetaCrawl.Transporter {

    protected SolrClient solrClient;
    private StringWriter writer;
    private XMLTransformer transformer;
    private int count;
    private String iri;
    private String nsp;

    public SolrTransporter(String solr) {
        this.solrClient = new SolrClient(solr);
    }

    public SolrTransporter(String solr, String iri) {
        this.solrClient = new SolrClient(solr);
        this.iri = iri;
        this.nsp = "nlp";
    }

    public SolrTransporter(String solr, String iri, String xsltFile) {
        this.solrClient = new SolrClient(solr);
        this.transformer = new XMLTransformer(FileUtil.read(xsltFile));
        this.iri = iri;
        this.nsp = "nlp";
    }

    @Override
    public void create() {
        log("create " + iri);
        solrClient.create();
        writer = new StringWriter();
        if (transformer!=null)
            transformer.create();
        count = 0;
    }

    @Override
    public void dispose() {
        solrClient.dispose();
        try { writer.close(); } catch(IOException e) {}
        if (transformer!=null)
            transformer.dispose();
    }

    @Override
    public String probe() {
        return "" + solrClient.probe();
    }

    @Override
    public Model read(String oid) { 
        if (iri!=null && nsp!=null) {
            return readFromIndex(oid);
        }
        return PrefixModel.retrieve(oid);
    }

    public Model readFromIndex(String oid) { 
        log("read iri [" + iri + "]" + oid);
        SolrDocument sdoc = solrClient.read(oid);
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix(nsp,iri);
        Resource resource = model.createResource(iri + oid);
        if (sdoc!=null) {
            for(Map.Entry<String,Object> m : sdoc.entrySet()) {
                String k = m.getKey();
                Object v = m.getValue();
                if ( v instanceof String) {
                    Property p = model.createProperty(iri,k);
                    resource.addProperty(p,v.toString());
                } else if (v instanceof Long) {
                    Property p = model.createProperty(iri,k);
                    resource.addProperty(p,v.toString());
                } else if (v instanceof Collection) {
                    for (Object o: ((Collection)v).toArray()) {
                        Property p = model.createProperty(iri,k);
                        resource.addProperty(p,o.toString());
                    }
                }
            }
        }
        if (transformer==null) 
            return model;
        String str = transformer.transform(model);
        Model mod = ModelFactory.createDefaultModel();
        try {
            InputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"));
            RDFReader reader = new JenaReader();
            reader.read(mod, is, null);
            is.close();
        } catch(UnsupportedEncodingException e) { log(e); }
          catch(IOException e) { log(e); }
        return mod;
    } 

    @Override
    public int crawl(String query) {
        return (int)solrClient.probe(query);
    } 

    @Override
    public String[] getIdentifiers(int off, int limit) {
        return solrClient.getIdentifiers(off, limit);
    } 

    private static final Logger logger =
                         Logger.getLogger(SolrTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());    
    }

    private void log(Model mod) {
        mod.write(System.out, "RDF/XML");
    }

}
