package org.shanghai.store;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.query.QueryExecution;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title General Triple Store Interface
   @date 2013-09-19
*/
public class TripleStore {

    private Virtuoso virt;
    private JenaTDB  jtdb;
    private int  store;
    private String graph;

    public TripleStore() {
        virt = new Virtuoso();
        store = 1;
    }

    public TripleStore(String uri) {
        if (uri.startsWith("jdbc:virtuoso")) {
            virt = new Virtuoso(uri);
            store = 1;
        } else {
            jtdb = new JenaTDB(uri);
            store = 2;
        }
    }

    public TripleStore(String uri, String graph) {
        if (uri.startsWith("jdbc:virtuoso")) {
            virt = new Virtuoso(uri, graph);
            store = 1;
        } else {
            jtdb = new JenaTDB(uri, graph);
            store = 2;
        }
        this.graph = graph;
    }

    public TripleStore(String uri, String graph, String dbuser, String dbpass) {
        if (uri.startsWith("jdbc:virtuoso")) {
            virt = new Virtuoso(uri, graph, dbuser, dbpass);
            store = 1;
        } else {
            jtdb = new JenaTDB(uri, graph);
            store = 2;
        }
        this.graph = graph;
    }

    public void create() {
        if (store==1) {
            virt.create();
        } else if (store==2) {
            jtdb.create();
        }
    }

    public void dispose() {
        if (store==1)
            virt.dispose();
        else if (store==2)
            jtdb.dispose();
    }

    public void clean() {
        switch(store) {
            case 1: virt.clean();
                    break;
            case 2: jtdb.clean();
                    break;
        }
    }

    public QueryExecution getExecutor(String q) {
        switch(store) {
            case 1: return virt.getExecutor(q);
            case 2: return jtdb.getExecutor(q);
        }
        return null;
    }

    public boolean delete(String about) {
        boolean b=false;
        switch(store) {
            case 1: b=virt.delete(about);
                    break;
            case 2: b=jtdb.delete(about);
                    break;
        }
        return b;
    }

    public boolean delete(Model m) {
        return delete(m,graph);
    }

    private boolean delete(Model m, String path) {
        boolean b=false;
        switch(store) {
            case 1: b=virt.remove(m, path);
                    break;
            case 2: b=jtdb.remove(m, path);
                    break;
        }
        return b;
    }

    public boolean write(Model m) {
        boolean b = false;
        switch(store) {
            case 1: b=virt.save(m);
                    break;
            case 2: b=jtdb.save(m);
                    break;
        }
        return b;
    }

    public boolean update(Model m) {
        boolean b = false;
        switch(store) {
            case 1: b=virt.update(m);
                    break;
            case 2: b=jtdb.update(m);
                    break;
        }
        return b;
    }

}
