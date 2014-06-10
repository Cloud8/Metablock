package org.shanghai.crawl;

import com.hp.hpl.jena.rdf.model.Model;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop
   @title Small wrapper for RDF data 
   @date 2013-12-01
*/
public class RDFTransporter extends org.shanghai.rdf.RDFTransporter
                            implements MetaCrawl.Transporter {

    public RDFTransporter(String sparql,
            String probe, String enum_, String dump, String date) {
        super(sparql, probe, enum_, dump, date);
    }

    @Override 
    public int crawl(String directory) {
        return Integer.parseInt(super.probe());
    }

}
