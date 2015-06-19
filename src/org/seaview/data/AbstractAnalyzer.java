package org.seaview.data;

import org.shanghai.crawl.MetaCrawl.Analyzer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.DCTerms;

import java.io.File;
import java.util.logging.Logger;

/*
 * (Really simple-dumb) abstract analyzer
 *
 */
public abstract class AbstractAnalyzer implements Analyzer {

    public static final String ore = "http://www.openarchives.org/ore/terms/";
    protected static final String c4o = "http://purl.org/spar/c4o/";
    protected static final String voidNS = "http://rdfs.org/ns/void#";

    @Override
    public Analyzer create() {
        return this;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Resource analyze(Model model, String id) {
        //log("analyze " + rc.getURI() + " [" + id + "]");
        Resource rc = findResource(model, id);
        analyze(model, rc, id);
        return rc;
    }

    @Override
    public boolean test() {
        log("test: " + this.getClass().getName());
        return true;
    }

    @Override
    public Resource test(Model model, String id) {
        Resource rc = findResource(model, id);
        test(model, rc, id);
        return rc;
    }

    @Override
    public void dump(Model model, String id, String fname) {
        Resource rc = findResource(model, id);
        dump(model, rc, id, fname);
    }

    public abstract void analyze(Model model, Resource rc, String id);

    public void test(Model model, Resource rc, String id) {
        log("test " + rc.getURI() + " " + id);
    }

    public void dump(Model model, Resource rc, String id, String outfile) {
        log("dump not available " + rc.getURI() + " " + id);
    }

    protected Resource findResource(Model model, String id) {
        Resource rc = null;
        Property type = model.createProperty(rdf, "type");
        Property agg = model.createProperty(ore, "aggregates");
        if (id.startsWith("http://")) {
            rc = model.getResource(id);
            return rc;
        }
		ResIterator ri = model.listResourcesWithProperty(type);
        boolean isPart = false;
		while( ri.hasNext() ) {
            Resource rcx = ri.nextResource();
		    String ns = rcx.getPropertyResourceValue(type).getNameSpace();
		    String name = rcx.getPropertyResourceValue(type).getLocalName();
            if (ns.equals(fabio)) {
			    // log("findResource " + name + " [" + ns + "]");
                if (rc==null) {
                    rc = rcx;
                } else if (name.equals("JournalIssue") && 
                           rc.getPropertyResourceValue(type)
                             .getLocalName().equals("Journal")) { // isPartOf
                    rc = rcx;
                } 
                if (name.equals("JournalArticle")) { // isPartOf
                    rc = rcx;
                } 
                if (rcx.hasProperty(agg) && !rc.hasProperty(agg)) {
                    rc = rcx;
                }
            } else if (ns.equals(dct) && name.equals("BibliographicResource")) {
                if (rcx.hasProperty(agg)) {
                    rc = rcx;
                }
            }
		}
        return rc;
    }

    /**
    private Resource findOldResource(Model model, String id) {
        Resource rc = null;
        if (id.startsWith("http://")) {
            rc = model.getResource(id);
        } else {
            ResIterator ri = model.listSubjectsWithProperty(
                     model.createProperty(ore, "aggregates"));
            if (ri.hasNext()) {
                rc = ri.nextResource();
            } else {
                ri = model.listSubjectsWithProperty(
                     model.createProperty(DCTerms.getURI(), "identifier"));
                if (ri.hasNext()) {
                    rc = ri.nextResource();
                } else {
                    log("no resource for " + id);
                }
            }
        }
        return rc;
    }
    **/

    public static String getPath(Model mod, Resource rc, 
                                 String id, String suffix) {
        int x = id.lastIndexOf(".");
        if (x>0) {
            String fname = id.substring(0, x) + suffix;
            if ( (new File(fname)).exists()) {
                return fname;
            }
        }
        return getPath(mod, rc, suffix);
    }

    public static String getPath(Model mod, Resource rc, String suffix) {
        Property rel = mod.createProperty(ore, "aggregates");
        StmtIterator si = rc.listProperties(rel);

        String path = null;
	    while( si.hasNext() ) {
	    	RDFNode node = si.nextStatement().getObject();
	    	if (node==null) { 
	    	    //log("zero " + path);
	    	} else if (node.isLiteral() && node.toString().equals("")) {
	    	    //log("empty " + path);
	    	} else if (node.isResource()) {
	    		path = node.asResource().getURI();
	    	} else {
	    		path = node.asLiteral().toString();
	    	}
            if (path!=null && path.endsWith(suffix)) {
                break;
            }
	    }
        if (path==null) {
            return null;
        }
        if (path.startsWith("http://localhost/")) {
            path = path.substring(17);
        }
        if (path.startsWith("http://archiv.ub.uni-marburg.de/")) {
            String test = "/srv/archiv/" + path.substring(32);
            logger.info("test " + test);
            if (new File(test).canRead()) {
                return test;
            }
        }
        if (path.startsWith("http://")) {
            return path;
        } 
        if (new File(path).canRead()) {
            return path;
        }
        path = System.getProperty("user.home") + "/" + path;
        if (new File(path).canRead()) {
            return path;
        }
        return null;
    }

    protected static Logger logger =
                         Logger.getLogger(AbstractAnalyzer.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        logger.info(e.toString());
    }

    protected void log(Model mod) {
        mod.write(System.out, "RDF/XML-ABBREV");
    }

}
