package org.shanghai.bones;

import org.shanghai.bones.BiblioRecord;

import java.io.StringWriter;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;

import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.DC;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.arp.JenaReader;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

import java.util.logging.Logger;
import java.util.Iterator;
import java.util.Iterator;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.net.MalformedURLException;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title Bean writer to safe bean as RDF data
   @date 2013-02-21
*/
public class BiblioModel {

    private static final Logger logger =
                         Logger.getLogger(BiblioModel.class.getName());

    public String SHG; // view base
    private IRIFactory factory;
    private IRI cwd;
    private Property cover;

    public BiblioModel(String s) {
        SHG = s;
    }

    private BiblioRecord bib;

    public void create() {
        factory = IRIFactory.uriImplementation();
        cwd = factory.construct(SHG);
    }

    public void dispose() {
    }

    public void log(String msg) {
        logger.info(msg);
    }

    public void log(Exception e) {
        log(e.toString());
        e.printStackTrace();
    }

    public Model getModel(BiblioRecord bib) { 
        Model model = ModelFactory.createDefaultModel();
      //Model model = ModelFactory.createModel("dcterms:BibliographicResource");
        model.setNsPrefix("dct", DCTerms.NS);
        // model.setNsPrefix("dc", DC.NS);
        model.setNsPrefix("shg", SHG);
        String about = null;
        try {
            //GH2013-05 : this gets out of control
            //about = cwd.resolve(bib.id).toURL().toString();
            about = cwd.resolve(bib.id.replace(":","/")).toURL().toString();
        } catch(MalformedURLException e) { 
            log("bib.id: " + bib.id); 
            log(e); 
        }

        String coverStr = null;
        if (bib.thumbnail!=null) {
            try { 
              coverStr = cwd.resolve(bib.thumbnail).toURL().toString();
              cover = model.createProperty(SHG, "hasCoverImage");
              // log("cover: " + cover.toString() + " " + coverStr);
            } catch(MalformedURLException e) { log(e); }
        }
        Resource r = model.createResource(about);
        if (bib.fulltext!=null)
            r.addProperty(DCTerms.abstract_, bib.fulltext);
        if (bib.getUrl()!=null) {
            String u = bib.getUrl();
            // log("url: " + u);
            if (u.startsWith("/") && u.endsWith(".pdf")) {
                r.addProperty(DCTerms.relation, SHG + "/page" + u);
            } else if (u.startsWith("/") && u.endsWith(".epub")) {
                r.addProperty(DCTerms.relation, SHG + "/book" + u);
            } else {
                r.addProperty(DCTerms.relation, bib.getUrl());
            }
        }
        if (bib.getTopic()!=null)
            r.addProperty(DCTerms.subject, bib.getTopic());
        if (bib.recordtype!=null)
            r.addProperty(DCTerms.type, bib.recordtype);

        if (bib.id!=null)
            r.addProperty(DCTerms.identifier, bib.id);
        if (bib.getLanguage()!=null)
            r.addProperty(DCTerms.language, bib.getLanguage());
        if (bib.publisher!=null)
            r.addProperty(DCTerms.publisher, bib.publisher);
        if (bib.getFormat()!=null)
            r.addProperty(DCTerms.format, bib.getFormat());
        if (bib.author!=null)
            r.addProperty(DCTerms.creator, bib.author);
        if (bib.title!=null)
            r.addProperty(DCTerms.title, bib.title);
        if (bib.getPublishDate()!=null)
            r.addProperty(DCTerms.dateSubmitted, bib.getPublishDate());
        if (bib.getIssued()!=null)
            r.addProperty(DCTerms.issued, bib.getIssued());
        r.addProperty(DCTerms.modified, 
            new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        if (bib.description!=null)
            r.addProperty(DCTerms.abstract_, bib.description);
        if (cover!=null) { // this is ugly.
            r.addProperty(cover, coverStr);
        }
        return model;
    }

    public BiblioRecord read(Model model) {
        BiblioRecord bib = new BiblioRecord();
        bib.title		= getObject(model, DCTerms.title);
        bib.title_short	= getObject(model, DCTerms.title);
        bib.title_full	= getObject(model, DCTerms.title);
        bib.id 			= getObject(model, DCTerms.identifier);
        bib.setFormat   ( getObject(model, DCTerms.format) );
        bib.recordtype	= getObject(model, DCTerms.type);
        bib.modified	= getObject(model, DCTerms.modified);
        bib.author		= getObject(model, DCTerms.creator);
        bib.issued		= getObject(model, DCTerms.issued);
        bib.setLanguage ( getObject(model, DCTerms.language) );
        bib.setPublishDate ( getObject(model, DCTerms.dateSubmitted) );
        bib.description = getObject(model, DCTerms.abstract_);
        bib.setUrl 		( getObject(model, DCTerms.relation) );
        bib.publisher 	= getObject(model, DCTerms.publisher);
        bib.thumbnail   = getObject(model, 
                          model.createProperty(SHG, "hasCoverImage"));
        bib.upd_date    = new Date();
        return bib;
    }

    private String getObject(Model model, Property p) {
      Selector selector = new SimpleSelector((Resource)null, p, (RDFNode)null);
        StmtIterator iter = model.listStatements(selector);
        String result = null;
        if (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            result = stmt.getObject().toString();
        }
        return result;
    }

    private String getObject(Model model, String resource, Property p) {
            Resource s = model.getResource(resource);
            Statement st = model.getProperty(s, p); 
            if (st==null) {
                //log("no " + p.toString());
                return null;
            }
            RDFNode rdf = st.getObject();
            if (rdf==null) {
                //log("no property " + p.toString());
                return null;
            }
            return st.getObject().toString();
    }

    private BiblioRecord read(Model model, String resource) {
        BiblioRecord bib = new BiblioRecord();
        bib.title		= getObject(model, resource, DCTerms.title);
        bib.id 			= getObject(model, resource, DCTerms.identifier);
        bib.setFormat   ( getObject(model, resource, DCTerms.format) );
        bib.recordtype	= getObject(model, resource, DCTerms.type);
        bib.modified	= getObject(model, resource, DCTerms.modified);
        bib.author		= getObject(model, resource, DCTerms.creator);
        bib.issued		= getObject(model, resource, DCTerms.issued);
        bib.setLanguage ( getObject(model, resource, DCTerms.language) );
        bib.description = getObject(model, resource, DCTerms.abstract_);
        bib.setUrl 		( getObject(model, resource, DCTerms.relation) );
        bib.publisher 	= getObject(model, resource, DCTerms.publisher);
        return bib;
    }

}
