package org.seaview.pdf;

import org.seaview.data.AbstractAnalyzer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.rdf.model.Seq;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

import java.net.URL;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URISyntaxException;

import java.io.UnsupportedEncodingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.logging.Logger;
import java.util.List;
import java.net.MalformedURLException;

/*
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Reference extractor for pdf files
    @date 2015-05-08
 */
public class PDFAnalyzer extends AbstractAnalyzer {

    protected static final int MONOSIZE = 33; // pages considered mono
    protected static final int MONOTHRESHOLD = 29; // minimum # references
    protected static final String dct = DCTerms.NS;
    protected static final String about /* will become rdf:about */
                                        = "http://localhost/refs/";

    private Property references;

    private boolean meta;
    private boolean refs;
    private int threshold;
    private PDFLoader pl;
    private AbstractExtractor extractor;
    public int count;

    public PDFAnalyzer(String engine, boolean meta, boolean refs, String base) {
        if (engine.equals("cermine")) {
            extractor = new Cermine(meta, refs);
        } else if (engine.equals("grobid")) {
            extractor = new Grobid(meta, refs, base);
        } else {
            extractor = new AbstractExtractor(meta, refs);
        }
        this.meta = meta;
        this.refs = refs;
    }

    @Override
    public void dispose() {
        extractor.dispose();
    }

    @Override
    public AbstractAnalyzer create() {
        //log("create metadata " + title + " references " + refs);
        count = 0;
        extractor.create();
        pl = new PDFLoader();
        return this;
    }

    @Override
    public void test(Model model, Resource rc, String id) {
        log("test: " + id); // dumped to crawl.test
        pl.create();
        String fname = create(model, rc, id);
        if (fname==null) {
            log("test: no pdf file found for " + rc.getURI());
            return;
        }
        extractor.test = true;
        extractor.create(fname);
        if (meta) {
            log("scratch " + fname + " metadata " + rc.getURI());
            extractMetadata(model, rc, fname);
        }
        if (refs&&(!rc.hasProperty(references))) {
            log("scratch " + fname + " references " + rc.getURI());
            extractReferences(model, rc, fname);
        }
        pl.dispose();
    }

    @Override
    public void analyze(Model model, Resource rc, String fname) {
        references = model.createProperty(dct, "references");

        if (refs && (rc.hasProperty(references))) {
            log("skipped references " + fname);
            //rc.removeAll(references); // does not work
            return;
        }
        pl.create();
        fname = create(model, rc, fname);
        if (fname==null) {
            log("fatal: no pdf file found for " + rc.getURI());
            return;
        }
        extractor.create(fname);
        if (meta) {
            extractMetadata(model, rc, fname);
        }

        if (refs && (!rc.hasProperty(references))) {
            extractReferences(model, rc, fname);
        }
        pl.dispose();
    }

    @Override
    public void dump(Model model, String id, String fname) {
        pl.create();
        pl.dump(model, id, fname);
        pl.dispose();
    }

    private String create(Model model, Resource rc, String fname) {
        pl.analyze(model, rc, fname);
        if (pl.failed()) {
            return null;
        }
        threshold = 0;
        if (pl.size > MONOSIZE) {
            threshold = MONOTHRESHOLD;
        }
        fname = pl.maltreat(3, 0.70); // pages 0-3 and 30 % from the tail 
        return fname;
    }

    private void extractMetadata(Model model, Resource rc, String fname) {
        extractor.extractMetadata(model, rc, fname);

        Property extend = model.createProperty(dct, "extend");
        if (!rc.hasProperty(extend)) {
            rc.addProperty(extend, "" + pl.size);
        }

        Property format = model.createProperty(dct, "format");
        if (!rc.hasProperty(format)) {
			if (pl.size<30) {
			    rc.addProperty(format, "Article");
			} else {
			    rc.addProperty(format, "Monograph");
			}
		}

        Property title = model.createProperty(dct, "title");
        if (!rc.hasProperty(title)) {
            log("setting title from pdf catalog");
		    if (pl.getTitle()!=null) {
                rc.addProperty(title, pl.getTitle());
            }
        }

        Property creator = model.createProperty(dct, "creator");
        if (!rc.hasProperty(creator)) {
            log("setting creator from pdf catalog");
		    if (pl.getAuthor()!=null) {
                extractor.injectAuthors(model,rc,new String[]{pl.getAuthor()});
                //rc.addProperty(creator, pl.getAuthor());
            }
        }
    }

    private void extractReferences(Model model, Resource rc, String fname) {
        extractor.extractReferences(model, rc, fname, threshold);
    }

    private static final Logger logger =
                         Logger.getLogger(PDFAnalyzer.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        logger.info(e.toString());
    }

    protected void log(Model mod) {
        mod.write(System.out, "RDF/XML");
    }

    protected void log(Element[] elements) {
        XMLOutputter outp = new XMLOutputter(Format.getPrettyFormat());
        for (Element el : elements) {
            String str = outp.outputString(el);
            System.out.println(str);
        }
    }

    protected void log(Element el) {
        XMLOutputter outp = new XMLOutputter(Format.getPrettyFormat());
        String str = outp.outputString(el);
        System.out.println(str);
    }
}
