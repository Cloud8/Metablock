package org.shanghai.jena;

import org.shanghai.rdf.RDFReader.Interface;
import java.io.InputStream;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title Defined SPARQL Query evaluation against a storage layer
   @date 2013-02-21
*/

public class Spinner implements Interface {

    ModelTalk modelTalk;

    public Spinner(String service) {
        modelTalk = new ModelTalk(service);
    }

    public Spinner(String service, String graph) {
        modelTalk = new ModelTalk(service, graph);
    }

    public void create() {
        modelTalk.create();
    }

    public void dispose() {
        modelTalk.dispose();
    }

    public String[] getSubjects(String query, int limit) {
        return modelTalk.getSubjects(query, limit);
    }

    public String getDescription(String query) {
        return modelTalk.getDescription(query);
    }

    public boolean delete(String what) {
        return modelTalk.delete(what);
    }

    public boolean update(InputStream in) {
        return modelTalk.update(in);
    }

    public String query(String what) {
        return modelTalk.query(what);
    }

}

