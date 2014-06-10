package org.shanghai.crawl;

public class SolrStorage extends org.shanghai.rdf.SolrStorage 
       implements MetaCrawl.Storage 
{

    public SolrStorage(String solr, String xslt) {
        super(solr, xslt);
    }

    @Override
    public boolean test(String resource) {
        return solrPost.test(resource);
    } 

}
