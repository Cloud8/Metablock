package org.shanghai.solr;

import org.shanghai.crawl.MetaCrawl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
//import org.json.XMLTokener;
//import org.json.JSONML;
import java.util.logging.Logger;

public class SolrStorage extends org.shanghai.rdf.SolrStorage 
       implements MetaCrawl.Storage 
{

    public SolrStorage(String solr, String xslt) {
        super(solr, xslt);
    }

    @Override
    public boolean test(Resource rc, String resource) {
        log("test # " + rc.getURI());
        return super.write(rc, resource);
        //String xml = transformer.transform(rc.getModel(), resource);
        //String rdf = transformer.asString(model);
        //String json = JSONML.toJSONArray(new XMLTokener(rdf)).toString();
        //String a = xml.substring(0, xml.indexOf("</doc>"));
        //String b = "<field name=\"json_str\">" + json +"</field>";
        //String c = xml.substring(xml.indexOf("</doc>"));
        //return solrPost.post(a + b + c);
        //System.out.println(xml);
        //return solrPost.test(resource);
        //return solrPost.post(xml);
    } 

    private static final Logger logger =
                         Logger.getLogger(SolrStorage.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

}
