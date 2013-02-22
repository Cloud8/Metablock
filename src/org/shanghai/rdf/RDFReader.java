package org.shanghai.rdf;

import org.shanghai.jena.Spinner;

import java.util.logging.Logger;
import java.io.InputStream;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A RDF Reader as abstraction layer to some real storage
   @date 2013-01-16
*/
public class RDFReader {

    public interface Interface {
        public void create();
        public void dispose();
        public String[] getSubjects(String query, int limit);
        public String getDescription(String query);
        public boolean delete(String what);
        public boolean update(InputStream in);
        public String query(String what);
    }

    private static final Logger logger =
                         Logger.getLogger(RDFReader.class.getName());

    private Interface reader;

    public RDFReader(String service) {
        this.reader = new Spinner(service);
    }

    /** just to be prepared to graphs */
    public RDFReader(String service, String graph) {
        this.reader = new Spinner(service, graph);
    }

    private void log(String msg) {
        logger.info(msg);    
    }

    /** shit happens */
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

    /** return a hopefully concise bounded description for the subject */
    public String getDescription(String query, String subject) {
        String desc = query.replace("%param%", "<" + subject + ">");
        return reader.getDescription(desc);
    }

    /** execute query and return result */
    public String query(String query) {
        return reader.query(query);
    }

    public boolean delete(String about) {
        return reader.delete(about);
    } 

    /** delete and add what is known about the record */
    public boolean update(InputStream in) {
        return reader.update(in);
    }

}
