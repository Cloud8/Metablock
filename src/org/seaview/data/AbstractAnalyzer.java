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
import com.hp.hpl.jena.vocabulary.RDF;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.util.logging.Logger;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop
  @title Abstract Data Analyzer
  @date 2015-07-01
*/
public abstract class AbstractAnalyzer implements Analyzer {

    protected static final String ore = "http://www.openarchives.org/ore/terms/";
    protected static final String c4o = "http://purl.org/spar/c4o/";
    protected static final String voidNS = "http://rdfs.org/ns/void#";
    protected String docbase;
    protected boolean test = false;
    protected Property hasDOI;

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
        hasDOI = model.createProperty(fabio, "hasDOI");
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
        test = true;
        return analyze(model, id);
    }

    @Override
    public void dump(Model model, String id, String fname) {
        Resource rc = findResource(model, id);
        log("dump not available " + rc.getURI() + " " + id);
    }

    public abstract void analyze(Model model, Resource rc, String id);

    //public void test(Model model, Resource rc, String id) {
    //    log("test " + rc.getURI() + " " + id);
    //}

    protected Resource findResource(Model model, String id) {
        Resource rc = null;
        //Property type = model.createProperty(rdf, "type");
        Property agg = model.createProperty(ore, "aggregates");
        if (id.startsWith("http://")) {
            rc = model.getResource(id);
            return rc;
        }
		ResIterator ri = model.listResourcesWithProperty(RDF.type);
        boolean isPart = false;
		while( ri.hasNext() ) {
            Resource rcx = ri.nextResource();
		    String ns = rcx.getPropertyResourceValue(RDF.type).getNameSpace();
		    String name = rcx.getPropertyResourceValue(RDF.type).getLocalName();
            if (ns==null) {
                log("ns " + ns + " name " + name);
            } else if (ns.equals(fabio)) {
			    // log("findResource " + name + " [" + ns + "]");
                if (rc==null) {
                    rc = rcx;
                } else if (name.equals("JournalIssue") && 
                           rc.getPropertyResourceValue(RDF.type)
                             .getLocalName().equals("Journal")) { // isPartOf
                    rc = rcx;
                } 
                if (name.equals("JournalArticle")) { // isPartOf
                    rc = rcx;
                } 
                if (rcx.hasProperty(agg) && !rc.hasProperty(agg)) {
                    rc = rcx;
                }
            } else if (ns.equals(DCTerms.NS) && name.equals("BibliographicResource")) {
                if (rcx.hasProperty(agg)) {
                    rc = rcx;
                }
            }
		}
        return rc;
    }

    public String getPath(Model mod, Resource rc, 
                                 String id, String suffix) {
        int x = id.lastIndexOf(".");
        if (x>0) {
            String fname = id.substring(0, x) + suffix;
            if (Files.isRegularFile(Paths.get(fname))) {
                return fname;
            }
        }
        return getPath(mod, rc, suffix);
    }

    public String getPath(Model mod, Resource rc, String suffix) {
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
        if (path.startsWith("files://")) {
            return path.substring(8);
        }
        if (path.startsWith("http://localhost/")) {
            path = path.substring(17);
        }
        if (docbase!=null && path.indexOf("/", 9)>0) {
            String test = docbase + path.substring(path.indexOf("/", 9));
            //logger.info("test " + test);
            if (Files.isReadable(Paths.get(test))) {
                return test;
            }
        }
        if (path.startsWith("http://")) {
            return path;
        } 
        if (Files.isReadable(Paths.get(path))) {
            return path;
        }
        path = System.getProperty("user.home") + "/" + path;
        if (Files.isReadable(Paths.get(path))) {
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
