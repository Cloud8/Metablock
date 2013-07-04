package org.shanghai.oai;

import org.shanghai.rdf.RDFTransporter;

import java.util.logging.Logger;
import java.util.Properties;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A Jena Crawler to get BiblioRecords from Jena
   @date 2013-04-14
*/
public class JenaCrawl {

    private RDFTransporter rdfTransporter;
    private static final Logger logger =
                         Logger.getLogger(JenaCrawl.class.getName());
    private int chunkSize = 12;
    private int count = 0;

    public JenaCrawl(Properties prop) {
        rdfTransporter = new RDFTransporter(prop);
    }

    private void log(String msg) {
        // logger.info(msg);
        System.out.println(msg);
    }

    private void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    public void create() {
        rdfTransporter.create();
    }

    public void dispose() {
        rdfTransporter.dispose(); 
    }

    public void crawl() {
        int i = 0;
        for (boolean b=true; b; b=crawl((i-1)*chunkSize, chunkSize)) {
             i++;
        }
    }

    public boolean crawl(int offset, int limit) {
        count = offset;
        boolean result = false;
        String[] identifiers = rdfTransporter.getIdentifiers(offset,limit);
        for (String resource : identifiers) {
             if (resource==null) {
                 return false;
             }
             String xml = rdfTransporter.getDescription(resource);
             count++;
             log(resource);
             if (resource.endsWith("948"))
                 log(xml);
        }
        return true;
    }

}

