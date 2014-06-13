package org.shanghai.store;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.query.QueryExecution;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop
   @title General Triple Store Interface
   @date 2013-09-19
*/
public class TripleStore {

    private Virtuoso virt;
    private JenaTDB  jtdb;
    private FourStore fstore;
    private int  store;
    private String graph;

    public TripleStore(String uri) {
        if (uri.startsWith("jdbc:virtuoso")) {
            virt = new Virtuoso(uri);
            store = 1;
        } else if (uri.startsWith("http://")) {
            fstore = new FourStore(uri);
            store = 3;
        } else {
            jtdb = new JenaTDB(uri);
            store = 2;
        }
    }

    public TripleStore(String uri, String graph) {
        if (uri.startsWith("jdbc:virtuoso")) {
            virt = new Virtuoso(uri, graph);
            store = 1;
        } else if (uri.startsWith("http://")) {
            fstore = new FourStore(uri, graph);
            store = 3;
        } else if (uri.startsWith("/")) {
            jtdb = new JenaTDB(uri, graph);
            store = 2;
        } else {
            store = 0;
        }
        this.graph = graph;
    }

    public TripleStore(String uri, String graph, String dbuser, String dbpass) {
        if (uri.startsWith("jdbc:virtuoso")) {
            virt = new Virtuoso(uri, graph, dbuser, dbpass);
            store = 1;
        } else if (uri.startsWith("http://")) {
            fstore = new FourStore(uri, graph);
            store = 3;
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
        } else if (store==3) {
            fstore.create();
        }
    }

    public void dispose() {
        if (store==1)
            virt.dispose();
        else if (store==2)
            jtdb.dispose();
        else if (store==3)
            fstore.dispose();
    }

    public void clean() {
        switch(store) {
            case 1: virt.clean();
                    break;
            case 2: jtdb.clean();
                    break;
            case 3: fstore.clean();
                    break;
        }
    }

    public QueryExecution getExecutor(String q) {
        switch(store) {
            case 1: return virt.getExecutor(q);
            case 2: return jtdb.getExecutor(q);
            case 3: return fstore.getExecutor(q);
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
            case 3: b=fstore.delete(about);
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
            case 3: b=fstore.remove(m, path);
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
            case 3: b=fstore.save(m);
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
            case 3: b=fstore.update(m);
                    break;
        }
        return b;
    }

}
