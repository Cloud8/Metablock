package org.shanghai.crawl;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.rdf.SolrPost;
import org.shanghai.util.FileUtil;
import com.hp.hpl.jena.rdf.model.Model;
import org.apache.solr.client.solrj.SolrQuery;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title Solr Store Transporter
   @date 2013-10-19
   @abstract Makes a XSLT transform and indexes the
             record to solr.
*/
public class SolrStorage implements MetaCrawl.Storage {

    protected SolrPost solrPost;
    private String xsltFile;
    private XMLTransformer transformer;
    private StringWriter writer;
    //private int count;

    public SolrStorage(String solr, String xsltFile) {
        this.xsltFile = xsltFile;
        this.solrPost = new SolrPost(solr);
    }

    @Override
    public void create() {
        String xslt = FileUtil.read(xsltFile);
        transformer = new XMLTransformer(xslt);
        transformer.create();
        solrPost.create();
        writer = new StringWriter();
        //count=0;
    }

    @Override
    public void dispose() {
        solrPost.dispose();
        transformer.dispose();
        try { writer.close(); } 
        catch(IOException e) {}
    }

    //private void reset() {
        //transformer.dispose();
        //transformer.create();
    //}

    @Override
    public boolean exists(String resource) {
        return solrPost.exists(resource);
    } 

    @Override
    public boolean delete(String resource) {
        return solrPost.delete(resource);
    } 

    @Override
    public synchronized boolean post(String rdf) {
        String xml = transformer.transform(rdf);
        boolean b = solrPost.post(xml);
        return b;
    } 

    @Override
    public synchronized boolean write(Model mod) {
        String xml = transformer.transform(mod);
        boolean b = solrPost.post(xml);
        //count++;
        //if (count>5)
        //    reset();
        return b;
    } 

    @Override
    public boolean update(Model mod) {
        return write(mod);
    } 

    @Override
    public void destroy() {
        solrPost.destroy();
    }

}
