package org.shanghai.rdf;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;

import java.io.InputStream;
import java.io.StringWriter;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.Iterator;
import java.util.Iterator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.lang.reflect.Field;
import java.lang.NoSuchFieldException;
import java.lang.IllegalAccessException;
import java.lang.ClassLoader;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop 
    @title Turtle Adventures: First Blood. 
    @date 2013-03-09
*/
public class Config {

    private static final String SH = "http://localhost/terms/";
    private static final Logger log = Logger.getLogger(Config.class.getName());
    private Model model;
    private ArrayList<String> verbs;
    private String turtle;
    private boolean test = false;

    public Config(String file) {
        turtle = file;
    }

    public void dispose() {
        model.close();
    }

    public Config create() {
        InputStream in = Config.class.getResourceAsStream("/"+turtle);
        if (in==null) {
            in = Config.class.getResourceAsStream("lib" + turtle);
            if (in!=null) log("load lib" + turtle);
        }
        if (in==null) {
            in = getClass().getClassLoader().getResourceAsStream(turtle);
            if (in!=null) log("class load " + turtle);
        }
        if (in==null) {
            in = getClass().getClassLoader().getResourceAsStream("lib"+turtle);
            if (in!=null) log("class load lib" + turtle);
        }
        if (in==null && turtle.startsWith("lib/")) {
            in = Config.class.getResourceAsStream("/"+turtle.substring(4));
        }
        if (in==null) {
            log(turtle + " not found!");
            return null;
        }
        return create(in);
    }

    public Config create(InputStream in) {
		try {
            model = ModelFactory.createDefaultModel();
		} catch(java.lang.ExceptionInInitializerError e) {
		    log("Config: " + e.getCause());
            e.printStackTrace();
		}
        model.read(in, SH, "TURTLE");
        return this;
    }

    /** little helper to make array shorter */
    public static String[] shorter(String[] dirs) {
        String[] result = new String[dirs.length-1];
        for (int i=0; i<dirs.length-1; i++)
             result[i] = dirs[i+1];
        return result;
    }

    /** little helper to get some value out of the model */
    public String get(String name) {
        //log("get " + name);
        RDFNode node = getNode(name);
        return get(node);
    }

    private String get(RDFNode node) {
        if (node==null) 
            return null;
        if (node.isLiteral()) {
            return node.asLiteral().getString();
        }
        return node.asResource().getLocalName();
    }

    public int getInt(String name) {
        RDFNode node = getNode(name);
        return getInt(node);
    }

    private int getInt(RDFNode node) {
        if (node==null)
            log("No integer " + node.toString());
        if (node.isLiteral()) 
            return node.asLiteral().getInt();
        return -1;
    }

    public boolean getBoolean(String name) {
        RDFNode o = getNode(name);
        if (o.isLiteral()) 
            return o.asLiteral().getBoolean();
        return false;
    }

    private RDFNode getNode(String name) {
        String[] x = name.split("\\.", 2);
        Resource r = model.getResource(SH + x[0]);
        Property p = model.createProperty(SH + x[1]);
        NodeIterator ni = model.listObjectsOfProperty(r,p);
        if (ni.hasNext()) {
            RDFNode o = model.listObjectsOfProperty(r,p).next();
            return o;
        }
        return null;
    }

    /** return properties from store */
    public Properties getProperties() {
        return getIndexList().get(0).getProperties();
    }

    /** simple flat parameter list */
    private void createVerbs() {
        verbs = new ArrayList<String>();
        verbs.add("crawl.depth");
        verbs.add("crawl.suffix");
        verbs.add("crawl.count");
        verbs.add("crawl.create");
        verbs.add("crawl.store");
    }

    private Properties getSimpleProperties() {
        createVerbs();
        Properties prop = new Properties();
        for(String verb : verbs) {
            String val = get(verb);
            if ( val!=null )
                 prop.setProperty(verb, val);
            else {
                 log("not found: " + verb);
            }
        }
        changeProp(prop);
        return prop;
    }

    private void changeProp(Properties prop) {
        String days = prop.getProperty("index.days");
        if (days!=null) {
            int n = Integer.parseInt(days);
            String date = getBack(n);
            log(date + " " + n);
            prop.setProperty("index.days", date);
        }
    }

    private String getNow() {
        Date today = new java.util.Date();
        SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd");
        String date = form.format( today.getTime() );
        return date;
    }

    private String getBack(int n) {
        //Date x = new Date(new Date().getTime() - n * 24 * 3600 * 1000 );
        Date today = new java.util.Date();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add( GregorianCalendar.DATE, -n );
        Date before = cal.getTime();
        SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd");
        String date = form.format( before.getTime() );
        return date;
    }

    /** retrieve something like index.solr */
    //private String getData(String what) {
    //    String[] x = what.split("\\.", 2);
    //    return getData(x[0], x[1]);
    //}

    public class Index {
	    public String name;
	    public String sparql;
	    public String probe;
	    public String enum_;
	    public String dump;
	    public String transformer;
	    public String test;
        public String date;
        public String days;
        public int count;
	    public String store;
        public void show() {
           log("Index: " + name  + "\n"
		    + "      : " + sparql + "\n"
		    + "      : " + probe + "\n"
		    + "      : " + enum_ + "\n"
		    + "      : " + dump + "\n"
		    + "      : " + transformer + "\n"
		    + " test : " + test + "\n"
		    + " days : " + days + "\n"
		    + " store: " + store + "\n"
		    + "      : " + count);
		}
        void calcule() {
            if (date!=null)
                date = date.substring(0,date.indexOf("^^"));
            if (days!=null) {
                days = days.substring(0, days.indexOf("^^"));
                date = getBack(Integer.parseInt(days));
            }
        }
        private Properties prop;
        public Properties getProperties() {
            if (prop!=null)
                return prop;
            prop = getSimpleProperties();
            prop.setProperty("index.sparql", sparql);
            prop.setProperty("index.probe", probe);
            prop.setProperty("index.enum", enum_);
            prop.setProperty("index.dump", dump);
            prop.setProperty("index.transformer", transformer);
            prop.setProperty("index.count", "" + count);
            prop.setProperty("index.test", test);
            prop.setProperty("index.store", store);
            prop.setProperty("index.sparql", sparql);
            prop.setProperty("index.name", name);
            prop.setProperty("index.date", date);
            return prop;
        }
    }

    /** get index by name */
    public Index getIndex(String name) {
        for (Index idx : getList("index")) {
            if (name.equals(idx.name)) {
                log(idx.name);
                return idx;
            }
        }
        return null;
    }
 
    /** get default index */
    public Index getIndex() {
        return getIndexList().get(0);
    }

    public List<Index> getIndexList() {
        return getList("index");
    }

    private List<Index> getList(String what) {
        boolean isStore = false;
        ArrayList<Index> list = new ArrayList<Index>();
        Resource s = model.getResource(SH + what);
        Property p = model.createProperty(SH + "hasPart");
        Selector selector = new SimpleSelector(s, p, (RDFNode)null );
        StmtIterator iter = model.listStatements(selector);
        while (iter.hasNext()) {
            Index idx = new Index();
            Statement stmt = iter.nextStatement();
            Resource r = stmt.getResource();
            Property part = stmt.getPredicate();
            idx.name = stmt.getResource().getLocalName();
            Resource o = stmt.getObject().asResource();
            Selector sel2 = new SimpleSelector(o, (Property)null, (String)null);
            StmtIterator iter2 = model.listStatements(sel2);
            while (iter2.hasNext()) {
                Statement stmt2 = iter2.nextStatement();
                Resource r2 = stmt.getResource();
                Property p2 = stmt2.getPredicate();
                try {
                    String pn = p2.getLocalName();
                    if ("enum".equals(pn))
                         pn = "enum_";
                    Field f = Index.class.getDeclaredField(pn);
                    f.setAccessible(true);
                    if (pn.equals("count")) {
                        int val = getInt(stmt2.getObject());
                        f.set(idx, val);
                    } else {
                        String  val = get(stmt2.getObject());
                        f.set(idx, val);
                    }
                } catch(NoSuchFieldException e) { log(e); }
                  catch(IllegalAccessException e) { log(e); }
            }
            idx.calcule();
            list.add(0,idx);
            //log(idx.name);
        }
        return list;//.toArray(new Class[list.size()]);
	}

    private List<OAI> getOAIList(String what) {
        ArrayList<OAI> list = new ArrayList<OAI>();
        Resource s = model.getResource(SH + what);
        Property p = model.createProperty(SH + "hasPart");
        Selector selector = new SimpleSelector(s, p, (RDFNode)null );
        StmtIterator iter = model.listStatements(selector);
        while (iter.hasNext()) {
            OAI idx = new OAI();
            Statement stmt = iter.nextStatement();
            Resource r = stmt.getResource();
            Property part = stmt.getPredicate();
            Resource o = stmt.getObject().asResource();
            Selector sel2 = new SimpleSelector(o, (Property)null, (String)null);
            StmtIterator iter2 = model.listStatements(sel2);
            while (iter2.hasNext()) {
                Statement stmt2 = iter2.nextStatement();
                Resource r2 = stmt.getResource();
                Property p2 = stmt2.getPredicate();
                String  val = stmt2.getObject().toString();
                try {
                    String pn = p2.getLocalName();
                    if ("enum".equals(pn))
                         pn = "enum_";
                    Field f = OAI.class.getDeclaredField(pn);
                    f.setAccessible(true);
                    f.set(idx, val);
                } catch(NoSuchFieldException e) { log(e); }
                  catch(IllegalAccessException e) { log(e); }
            }
            idx.calcule();
            list.add(idx);
        }
        return list;//.toArray(new Class[list.size()]);
	}

    public class OAI {
        public String transformer;
        public String harvest;
        public String prefix;
        public String from;
        public String until;
        public String rawtest;
        public String rdftest;
        public String republish;
        public String urnPrefix;
        public String docbase;
        public String days;
        public String interval;
        public String maxRequest;
        public void show() {
           log("OAI: " + prefix 
             + "   : " + days + ": " + transformer + "\n"
             + "   : " + harvest + "\n" 
             + "   : " + rawtest + ": " + rdftest + "\n"
             + "   : " + "republish to " + republish + "\n"
             + "   : from " + from + " until " + until); 
        }
        void calcule() {
            if (from==null) {
                days = days.substring(0, days.indexOf("^^"));
                from = getBack(Integer.parseInt(days));
            } else {
                from = from.substring(0, from.indexOf("^^"));
            }
            if (until==null) {
                until = getNow();
            } else {
                until = until.substring(0, until.indexOf("^^"));
            }
        }
    }

    public List<OAI> getOAIList() {
        return getOAIList("oai");
    }

    /** something like index.solr */
    //private String getData(String what, String prop) {
    //    Resource r = model.getResource(SH + what);
    //    Property p = model.createProperty(SH + prop);
    //    String res = getObject(model, r, p);
    //    if (res!=null && res.indexOf("^^")>0) {
    //        if (res.endsWith("integer"))
    //            return "" + getInteger(r, p);
    //        return res.substring(0, res.indexOf("^^"));
    //    }
    //    return res;
    //}

    public void test() {
        //getSimpleProperties();
        //getSimpleProperties().list(System.out);
        //List<OAI> oaiList = getOAIList();
        //for (OAI oai: getOAIList()) {
        //     oai.show();
        //}
        //List<Index> idxList = getIndexList();
        //for (Index idx: idxList) {
        //     idx.show();
        //}
        //Index idx = getIndexList().get(1);
        getIndex().show();
        //idx.getProperties().list(System.out);
        //getProperties().list(System.out);
        //getIndex("index05").show();
        log("configured by " + turtle);
    }

    private void show() {
        StringWriter out = new StringWriter();
        //model.write(out, "RDF/XML-ABBREV");
        model.write(out, "TTL");
        String result = out.toString();
        log(result);
    }

    private int getInteger(Resource r, Property p) {
        Statement st = model.getProperty(r, p);
        return st.getInt();
    }

    private String getString(Resource r, Property p) {
        String res = getObject(r, p);
        if (res.indexOf("^^")>0)
            return res.substring(0, res.indexOf("^^"));
        return res;
    }

    private String getObject(Resource r, Property p) {
        return getObject(model, r, p);
    }

    private String getObject(Model model, String resource, Property p) {
        Resource res = model.getResource(resource);
        return getObject(model, res, p);
    }

    private String getObject(Model model, Resource res, Property p) {
        Statement st = model.getProperty(res, p);
        if (st==null) {
            //log("No statement about " + p.toString());
            return null;
        }
        RDFNode rdf = st.getObject();
        if (rdf==null) {
            log("no property " + p.toString());
            return null;
        }
        return st.getObject().toString();
    }

    private void log(Model m) {
        m.write(System.out, "TTL");
    }

    private void log(String msg) {
        log.info(msg);
    }

    private void log(Exception e) {
        log(e.toString());
    }

    public static void main(String... args) {
        Config c = new Config("shanghai.ttl");
        c.create();
        int argc = 0;
        if (args.length>argc && args[argc].equals("-show")) {
            c.show();
        } else if (args.length>argc && args[argc].equals("-test")) {
            c.test();
        }
        c.dispose();
    }

}
