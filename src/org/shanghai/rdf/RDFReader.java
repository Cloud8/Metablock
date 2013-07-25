package org.shanghai.rdf;

import org.shanghai.rdf.RDFTransporter;

import java.util.logging.Logger;
import java.io.InputStream;
import java.net.URL;
import com.hp.hpl.jena.rdf.model.Model;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A RDF Reader as abstraction layer to some sparql service
   @date 2013-01-16
*/
public class RDFReader implements RDFTransporter.Reader {

    public interface Interface {
        public void create();
        public void dispose();
        public String[] getSubjects(String query, int limit);
        public String getDescription(String query);
        public String query(String what);
        public Model getModel(String desc);
        public void save(Model model);
    }

    private static final Logger logger =
                         Logger.getLogger(RDFReader.class.getName());

    private Interface reader;

    public RDFReader(Interface reader) {
        this.reader = reader;
    }

    public RDFReader(String service) {
        this.reader = new ModelTalk(service);
    }

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

    @Override
    public void create() {
        reader.create();
    }

    @Override
    public void dispose() {
        reader.dispose();
    }

    @Override
    public String[] getSubjects(String q, int offset, int limit) {
        String query = q + " offset " + offset + " limit " + limit;
        return reader.getSubjects(query, limit);
    }

    /** return a concise bounded description for the subject */
    @Override
    public String getDescription(String query, String subject) {
        if (subject==null) //support probe queries
            return reader.getDescription(query);
        if ( isValidURI(subject) ) {
            String desc;
            // if (query==null)
            //      desc = defaultQuery(subject);
            // else 
            desc = query.replace("<subject>", "<" + subject + ">");
            return reader.getDescription(desc);
        } 
        log("zero: " + subject);
        return null;
    }

    public Model getModel(String query, String subject) {
        log("getModel " + subject);
        String desc = query.replace("<subject>", "<" + subject + ">");
        return reader.getModel(desc);
    }

    /** execute query and return result. 
        Used for probe query, may be deprecated sometimes. */
    @Override
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

    public void save(Model model) {
        reader.save(model);
    }

    //only a simple rather flat model
    public Model getModel(String subject) {
        return reader.getModel( defaultQuery(subject) );
    }

    private String defaultQuery(String resource) {
        String query = "PREFIX  dct: <http://purl.org/dc/terms/>\n" 
                     + "PREFIX  dcq: <http://purl.org/dc/qualifier/1.0/>\n" 
                     + "PREFIX  dcam: <http://purl.org/dc/dcam/>\n" 
                     + "PREFIX  dcmitype: <http://purl.org/dc/dcmitype/>\n" 
                     + "PREFIX  cito:    <http://purl.org/spar/cito/>\n"
                     + "PREFIX  gnd: <http://d-nb.info/gnd/>\n" 
                     + "PREFIX  foaf: <http://xmlns.com/foaf/0.1/>\n" 
                     + "PREFIX  fabio: <http://purl.org/spar/fabio/>\n" 
                     + "PREFIX  aiiso: <http://purl.org/vocab/aiiso/schema#>\n" 
                     + "PREFIX  urn: <http://www.d-nb.de/standards/urn/>\n" 
                     + "PREFIX  shg: <http://localhost/view/>\n" 
                     + "CONSTRUCT { " + "<" + resource + ">" + " ?p ?o }\n"
                     + " where { " + "<" + resource + ">" + " ?p ?o "
					 // + "optional { "
					 // + "  ?o ?x ?y "
					 // + "  FILTER (isBlank(?o) && !isBlank(?y)) "
					 // + "}\n"
					 + "}";
        return query;
    }
}
