package org.shanghai.rdf;

import org.shanghai.rdf.MetaCrawl;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.rdf.SolrPost;
import org.shanghai.util.FileUtil;
import com.hp.hpl.jena.rdf.model.Model;

import java.io.IOException;
import java.io.StringWriter;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Solr Storage
    @date 2013-10-19
*/
public class SolrStorage implements MetaCrawl.Storage {

    protected SolrPost solrPost;
    private String xsltFile;
    private XMLTransformer transformer;
    private StringWriter writer;
    private int count;

    public SolrStorage(String solr, String xsltFile) {
        this.xsltFile = xsltFile;
        this.solrPost = new SolrPost(solr);
    }

    @Override
    public void create() {
        String xslt = FileUtil.readResource(xsltFile);
        transformer = new XMLTransformer(xslt);
        transformer.create();
        solrPost.create();
        writer = new StringWriter();
        count=0;
    }

    @Override
    public void dispose() {
        solrPost.dispose();
        transformer.dispose();
        try { writer.close(); } 
        catch(IOException e) {}
    }

    @Override
    public boolean delete(String resource) {
        return solrPost.delete(resource);
    } 

    //@Override
    //public synchronized boolean post(String rdf) {
    //    String xml = transformer.transform(rdf);
    //    boolean b = solrPost.post(xml);
    //    return b;
    //} 

    @Override
    public synchronized boolean write(Model mod, String resource) {
        String xml = transformer.transform(mod, resource);
        //writer.getBuffer().setLength(0);
        //mod.write(writer, "RDF/JSON");
        //String a = xml.substring(0, xml.indexOf("</doc>"));
        //String b = "<field name=\"json_str\">" +writer.toString()+"</field>";
        //String c = xml.substring(xml.indexOf("</doc>"));
        //return solrPost.post(a + b + c);
        return solrPost.post(xml);
    } 

    @Override
    public boolean update(String id, String field, String value) {
        return solrPost.update(id, field, value);
    }

    @Override
    public void destroy() {
        solrPost.destroy();
    }

}
