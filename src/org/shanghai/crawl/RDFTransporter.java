package org.shanghai.crawl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop
   @title Tiny wrapper for RDF data 
   @date 2013-12-01
*/
public class RDFTransporter extends org.shanghai.rdf.RDFTransporter
                            implements MetaCrawl.Transporter {

    public RDFTransporter(String sparql,
            String probe, String enum_, String dump, String date) {
        super(sparql, probe, enum_, dump, date);
    }

    @Override 
    public int index(String directory) {
        return 0; // Integer.parseInt(super.probe());
    }

    @Override 
    public Resource test(String resource) {
        Resource rc = read(resource);
        return rc;
    }

}
