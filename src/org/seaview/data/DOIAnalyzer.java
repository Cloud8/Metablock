package org.seaview.data;

import org.seaview.data.AbstractAnalyzer;

import org.shanghai.util.FileUtil;
import org.shanghai.util.PrefixModel;
import org.shanghai.crawl.TrivialScanner;
import org.shanghai.rdf.XMLTransformer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import java.util.logging.Logger;

/**
    (c) reserved.
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop 
    @title Register DOI from fabio:hasDOI property
    @date 2015-05-04
*/
public class DOIAnalyzer extends AbstractAnalyzer {

    private static Logger log = Logger.getLogger(DOIAnalyzer.class.getName());

	private Property hasDOI;
	private Property hasURL;

    private String volume = null;
    private String number = null;
    private String year = null;
    private String articleId = null;

    private String prefix;
    private String server;
    private String user;
    private String pass;

    private String xsltFile;
    private DOI doiGenerator;
    private String dbase = null;
    private XMLTransformer transformer;
    private Database db = null;

    private String uri = null;
    private String doi = null;

    private boolean quiet = true; // no talking
    private boolean delete = false;
    private boolean submit = false; // submit metadata
    private boolean register = false; // register metadata
  
    public DOIAnalyzer(String prefix, String store, String user, String pass, 
                       String xsltFile, boolean quiet) {
        this.prefix = prefix;
        this.server = store;
        this.user = user;
        this.pass = pass;
        this.xsltFile = xsltFile;
        this.quiet = quiet;
        //this.delete = true;
    }
  
    @Override
    public AbstractAnalyzer create() {
        doiGenerator = new DOI(prefix, server, user, pass);
        String xslt = FileUtil.readResource(xsltFile);
        transformer = new XMLTransformer(xslt);
        transformer.create();
        return this;
    }

    public void createDb(String dbhost, String dbase, 
                         String dbuser, String dbpass) {
	    db = new Database(dbhost, dbase, dbuser, dbpass);
        this.dbase = dbase;
		db.create();
    }
  
    public void submit() {
        this.submit = true;
    }
  
    public void register() {
        this.register = true;
    }
  
    public void delete() {
        this.delete = true;
    }
  
    @Override
    public void dispose() {
        if (transformer!=null) {
            transformer.create();
            transformer = null;
        }
        if (db!=null) {
		    db.dispose();
            db = null;
        }
    }

    @Override
    public void analyze(Model model, Resource rc, String id) {
        if (rc==null) {
            log("zero resource " + id);
            return;
        }
        createProperties(model, rc, id);

        if (!rc.hasProperty(DCTerms.creator)) {
            log("model has no creator, skip.");
            return ;
        }

        if (delete) {
            String res = this.doiGenerator.delete(doi);
            log(uri + " delete " + doi + " : " + res);
            rc.removeAll(hasDOI);
            return ;
        }

        if (rc.hasProperty(hasDOI)) {
            if (submit) {
                resubmit(model, id, doi, uri);
            } else if (register) {
                register(model, id, doi, uri);
            } else {
                log("doi exists: " + doi + " [" + uri + "]");
            }
        } else {
            log("doi: " + doi + " " + uri);
		    rc.addProperty(hasDOI, doi);
            register(model, uri, id, doi);
        }
    }

    @Override
    public Resource test(Model model, String id) {
        Resource rc = findResource(model, id);
        if (!rc.hasProperty(DCTerms.creator)) {
            log("model has no creator, skip " + id);
            return rc;
        }

        log("test " + id);
        createProperties(model, rc, id);
        if (rc.hasProperty(hasDOI)) {
	        String test = rc.getProperty(hasDOI).getString();
            if (test.equals(doi)) {
                log("resource has doi: " + test + " [" + doi + "]");
            } else {
                log("WARNING doi: " + test + " vs. " + doi + "");
            }
            //String test = test(doi, uri, true); // doi, uri, quiet
            //if (test!=null) log("test mode: [" + test + "]\n");
            //if (register) {
            //    String durl = "http://doi.org/" + doi;
            //    Model mod = PrefixModel.retrieve(durl);
            //    log(mod);
            //}
        } else {            
            log("test mode add: " + doi + " " + uri); // + " " + rc.getURI());
        }
        return rc;
    }

    @Override
    public void dump(Model model, String id, String fname) {
        Resource rc = findResource(model, id);
        createProperties(model, rc, id);
        if (rc.hasProperty(hasDOI)) {
            log("doi exists: " + doi + " [" + uri + "]");
        } else {
            log("doi: " + doi + " " + uri);
    		rc.addProperty(hasDOI, doi);
        }
        String xml = transformer.transform(model);
        FileUtil.write(fname, xml);
        log("wrote " + fname);
    }

    private void createProperties(Model model, Resource rc, String id) {
		hasDOI = model.createProperty(fabio, "hasDOI");
		hasURL = model.createProperty(fabio, "hasURL");
        Property hasVolume = model.createProperty(fabio, "hasVolumeIdentifier");
        Property hasNumber = model.createProperty(fabio, "hasSequenceIdentifier");
        Property articleIdentifier = model.createProperty(fabio, 
                                            "hasElectronicArticleIdentifier");
        //Property dctYear = model.createProperty(dct, "created");

        uri = rc.getURI();
        if (rc.hasProperty(hasURL)) {
            uri = rc.getProperty(hasURL).getString();
        } 
        if (rc.hasProperty(DCTerms.created)) {
		    year = rc.getProperty(DCTerms.created).getString();
        } else {
            year = null;
        }
        if (rc.hasProperty(hasVolume)) {
		    volume = rc.getProperty(hasVolume).getString();
        } else {
            volume = null;
        }
        if (rc.hasProperty(hasNumber)) {
		    number = rc.getProperty(hasNumber).getString();
        } else {
            number = null;
        }
        if (rc.hasProperty(articleIdentifier)) {
		    articleId = rc.getProperty(articleIdentifier).getString();
        } else {
            articleId = null;
        }
        if (articleId!=null) {
            doi = doiGenerator.createDoi(uri, year, volume, number, articleId);
        } else {
            doi = doiGenerator.createDoi(uri);
        }
    }
  
    /*
    private void opusOid(String oid) {
		String   q = "select url from opus where source_opus="+oid;
		String uri = db.getSingleText(q);
        String doi = doiGenerator.createDoi(uri);
		String   u = "update opus set doi=\""+doi+"\" where source_opus="+oid;
		int    res = db.update(u);
		         u = "update statistics set doi=\""+doi+"\" "
                     + "where source_opus="+oid+" and uri=\""+uri+"\"";
		       res = db.update(u);
		log("opus " + doi + " " + uri);
    }
    */

    private void opusUri(String doi, String uri) {
		String u = "update opus set doi=\""+doi+"\" where url=\""+uri+"\"";
		int res = db.update(u);
		u = "update statistics set doi=\""+doi+"\" where uri=\""+uri+"\"";
		res = db.update(u);
    }

    private String ojsOid(String aid) {
	    String q = "select setting_value from article_settings where "
                 + "setting_name='pub-id::doi' and "
                 + "article_id='" + aid + "'";
		return db.getSingleText(q);
    }

    private void ojsUri(String doi, String aid) {
		String ojs_doi = ojsOid(aid);
        if (ojs_doi==null) {
		    String u = "insert into article_settings "
                 + "(article_id, setting_name, setting_value, setting_type)"
                 + " values ('"+aid+"','pub-id::doi','" + doi + "', 'string')";
		    int res = db.update(u);
        } else if (ojs_doi.equals(doi)) {
		    log("ojs dois match: " + ojs_doi + " vs. " + doi);
        }
    }

    private boolean syncDB(String doi, String uri, String aid) {
        if (dbase==null) {
		    return false;
        }
        if (dbase.equals("opus3")) {
            opusUri(doi, uri);
        } else if (dbase.equals("ojs")) {
            ojsUri(doi, aid);
        }
		return true;
    }

    private void register(Model model, String uri, String fname, String doi) {
        String xml = transformer.transform(model);
        if (xml!=null && xml.trim().length()>11) {
            String result = this.doiGenerator.postData(xml); // set metadata
            if (result.contains("OK")) {
                result += ":" + this.doiGenerator.register(doi, uri); 
            }
            log(" [" + result + "] " + fname);
            if (result.contains("OK")) {
			    syncDB(doi, uri, articleId); // update database
            } else {
                log(xml);
                log("failed to register " + fname);
            }
        } else {
            log("no xml, no registration.");
        }
    }

    private void resubmit(Model model, String fname, String doi, String uri) {
        String xml = transformer.transform(model);
        if (xml!=null && xml.trim().length()>11) {
            String result = this.doiGenerator.postData(xml); // set metadata
            if (result.contains("OK")) {
                String tib = this.doiGenerator.getDoi(doi);
                if (tib==null) {
                    result += ":" + this.doiGenerator.register(doi, uri); 
                }
                log("data submit OK: " + result);
            } else {
                log("failed to register " + fname);
            }
        }
    }

    private String test(String doi, String uri, boolean quiet) {
	    String test = doi + " " + uri;
        if (db==null) {
	        test = "doi: " + this.doiGenerator.getDoi(doi);
        } else if (dbase.equals("opus3")) {
		    String q = "select doi from opus where url='"+uri+"'";
            String opus = db.getSingleText(q);
		    test = "opus: " + opus;
	        test += " tib: " + this.doiGenerator.getDoi(doi);
            if (opus==null) {
                test += " uri: " + uri;
            } else if (quiet && uri.equals(this.doiGenerator.getDoi(doi))) {
                test=null; // success
            }
        } else if (dbase.equals("ojs") && articleId!=null) {
		    String ojs_doi = ojsOid(articleId);
            String tib = this.doiGenerator.getDoi(doi);
            if (ojs_doi==null) {
			    syncDB(doi, uri, articleId); // update database
            } else if (ojs_doi.equals(doi) && tib!=null) { 
		        test = tib;
            } else {
		        test = "failed: " + doi + " vs. " + ojs_doi + " [" + tib + "]";
            }
        }
        return test;
    }

    public void log(String msg) {
        log.info(msg);
    }
  
    public void log(Exception e) {
        log.severe(e.toString());
        try {
             throw(e);
        } catch(Exception ex) {}
    }
}
