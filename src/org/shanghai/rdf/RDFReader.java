package org.shanghai.rdf;

import java.util.logging.Logger;
import java.io.InputStream;
import java.net.URL;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A RDF Reader as abstraction layer to some sparql service
   @date 2013-01-16
*/
public class RDFReader {

    public interface Interface {
        public void create();
        public void dispose();
        public String[] getSubjects(String query, int limit);
        public String getDescription(String query);
        public String query(String what);
    }

    private static final Logger logger =
                         Logger.getLogger(RDFReader.class.getName());

    private Interface reader;

    public RDFReader(String service) {
        this.reader = new ModelTalk(service);
    }

    /** Be prepared for graphs */
    public RDFReader(String service, String graph) {
        this.reader = new ModelTalk(service, graph);
    }

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    public void create() {
        reader.create();
    }

    public void dispose() {
        reader.dispose();
    }

    public String[] getSubjects(String q, int offset, int limit) {
        String query = q + " offset " + offset + " limit " + limit;
        return reader.getSubjects(query, limit);
    }

    /** return a concise bounded description for the subject */
    public String getDescription(String query, String subject) {
        // String desc = query.replace("%param%", "<" + subject + ">");
        if ( isValidURI(subject) ) {
            String desc = query.replace("<subject>", "<" + subject + ">");
            return reader.getDescription(desc);
        } 
        return null;
    }

    /** execute query and return result */
    public String query(String query) {
        return reader.query(query);
    }

    private boolean isValidURI(String uri) {
        final URL url;
    try {
        url = new URL(uri);
    } catch (Exception e1) {
        return false;
    }
    return "http".equals(url.getProtocol());
    }
}
