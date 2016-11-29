package org.seaview.pdf;

import org.seaview.pdf.Cover;
import org.seaview.opus.FileBackend;
import org.shanghai.util.Language;
import org.shanghai.util.FileUtil;
import org.shanghai.util.ModelUtil;
import org.shanghai.crawl.MetaCrawl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PropertyNotFoundException;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import java.util.logging.Logger;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @title Reference and Bibliographic Metadata extractor for PDF files
    @date 2015-05-08
 */
public class PDFAnalyzer implements MetaCrawl.Analyzer {

    protected static final int MONOSIZE = 99; // pages considered mono
    protected static final int MONOTHRESHOLD = 29; // minimum # references

    private boolean meta;
    private boolean fulltext;
    private boolean refs;
    private int threshold;
    private PDFLoader pl;
    private Cover cover;
    private AbstractExtractor extractor;
    private Language language;
    public int count;

    public PDFAnalyzer(String engine, boolean meta, boolean fulltext,
                       boolean refs, String ghome) {
        if (engine.equals("cermine")) {
            extractor = new Cermine(meta, refs);
        } else if (engine.equals("grobid")) {
            extractor = new Grobid(meta, refs, ghome);
        } else {
            // extractor = new AbstractExtractor(meta, refs);
        }
        this.meta = meta;
        this.fulltext = fulltext;
        this.refs = refs;
        language = new Language();
        pl = null;
    }

    public PDFAnalyzer(String engine, boolean meta, boolean fulltext,
                       boolean refs, String ghome, String docbase) {
        this(engine, meta, fulltext, refs, ghome);
        pl = new PDFLoader(docbase);
        cover = new Cover(new FileBackend(docbase), docbase);
    }

    @Override
    public void dispose() {
        extractor.dispose();
        cover.dispose();
        language.dispose();
        pl.dispose();
    }

    @Override
    public String probe() {
        return extractor.getClass().getSimpleName();
    }

    @Override
    public MetaCrawl.Analyzer create() {
        //log("create metadata " + title + " references " + refs);
        count = 0;
        extractor.create();
        if (pl==null) {
            pl = new PDFLoader();
        }
        cover.create();
        language.create();
        pl.create();
        return this;
    }

    @Override
    public Resource test(Resource rc) {
        String fname = create(rc);
        log("test: " + rc.getURI() + " # " + fname); 
        if (fname==null) {
            log("test: no pdf file found for " + rc.getURI());
            return rc;
        }
        extractor.test = true;
        extractor.create(fname);
        if (meta) {
            log("scratch metadata " + rc.getURI());
            extractMetadata(rc);
        }
        if (refs) {
            log("scratch references " + rc.getURI());
            extractReferences(rc);
        }
        if (fulltext) {
            log("scratch fulltext " + rc.getURI());
            extractFulltext(rc);
        }
        return rc;
    }

    @Override
    public Resource analyze(Resource rc) {
        //if (refs && (rc.hasProperty(DCTerms.references))) {
        //    ModelUtil.remove(rc, DCTerms.references);
        //    log("removed references " + rc.getURI());
        //}
        // TODO : test wether agent has already worked on this
        if (rc.hasProperty(DCTerms.provenance) 
            && rc.getProperty(DCTerms.provenance).getObject().isResource()) {
            Resource prov = rc.getProperty(DCTerms.provenance).getResource();
            // if (prov.hasProperty(DCTerms.creator)
            //     && prov.getProperty(DCTerms.creator).getObject()
            // 
        }
        String fname = create(rc);
        if (fname==null) {
            log("fatal: no file for " + rc.getURI());
            return rc;
        }

        extractor.create(fname);
        if (meta) {
            extractMetadata(rc);
        }

        if (refs) {
            extractReferences(rc);
        }

        if (fulltext) {
            extractFulltext(rc);
        }

        cover.analyze(rc);
        language.analyze(rc);
        // makeTEI(rc); 

        return rc;
    }

    private String create(Resource rc) {
        pl.analyze(rc);
        if (pl.failed()) {
            return null;
        }
        threshold = 0;
        if (pl.size > MONOSIZE) {
            threshold = MONOTHRESHOLD;
            return pl.maltreat(3, 0.70); // pages 0-3 and 30 % from the tail 
        } else {
            return pl.getPath(rc, ".pdf");
        }
    }

    private void extractMetadata(Resource rc) {
        extractor.extractMetadata(rc);

        if (!rc.hasProperty(DCTerms.type) 
            && rc.hasProperty(RDF.type, DCTerms.BibliographicResource)) {
			if (pl.size<30) {
                String type = "http://purl.org/spar/fabio/Article";
			    rc.addProperty(DCTerms.type, rc.getModel().createResource(type));
			} else {
                String type = "http://purl.org/spar/fabio/Book";
			    rc.addProperty(DCTerms.type, rc.getModel().createResource(type));
			}
		}

        if (!rc.hasProperty(DCTerms.extent)) {
            Resource ex = rc.getModel().createResource(DCTerms.SizeOrDuration);
            ex.addProperty(RDF.value, pl.size + " pages.");
            rc.addProperty(DCTerms.extent, ex);
        }

        if (!rc.hasProperty(DCTerms.title)) {
            log("setting title from pdf catalog");
		    if (pl.getTitle()!=null && pl.getTitle().length()!=0) {
                rc.addProperty(DCTerms.title, pl.getTitle());
            }
        }

        if (!rc.hasProperty(DCTerms.creator) && pl.getAuthor()!=null) {
            log("setting creator from pdf catalog");
            extractor.injectAuthors(rc, new String[]{pl.getAuthor()});
        }

        if (!rc.hasProperty(DCTerms.issued) && pl.getDate()!=null) {
            log("setting issue date from pdf catalog");
            rc.addProperty(DCTerms.issued, pl.getDate());
        }
        if (!rc.hasProperty(DCTerms.created) && pl.getDate()!=null) {
            rc.addProperty(DCTerms.created, pl.getDate().substring(0,4));
        }
    }

    private void extractReferences(Resource rc) {
        if (rc.hasProperty(DCTerms.references)) {
		    return;
        }

        extractor.extractReferences(rc, threshold);
        if (rc.hasProperty(DCTerms.references)) {
            RDFNode node = rc.getProperty(DCTerms.references).getObject();
            if (node.isResource()) {
                Resource provenance = rc.getModel().createResource(DCTerms.ProvenanceStatement);
                provenance.addProperty(RDFS.label, extractor.getClass().getSimpleName());
                rc.getProperty(DCTerms.references).getResource().addProperty(DCTerms.provenance, provenance);
            }
        }
    }

    private void extractFulltext(Resource rc) {
        rc.addProperty(DCTerms.description, pl.fulltext(rc));
    }

    private void makeTEI(Resource rc) {
        String tei = pl.getPath(rc, ".tei");
        if (tei==null) {
            //
        } else if (FileUtil.exists(tei)) {
            //
        } else {
            String pdf = pl.getPath(rc, ".pdf");
            String content = extractor.getTEI(pdf);
            if (content==null) {
                log("no TEI from: " + extractor.getClass().getSimpleName());
            } else {
                FileUtil.write(tei, content);
                log("wrote tei: " + tei);
            }
        }
    }

    private static final Logger logger =
                         Logger.getLogger(PDFAnalyzer.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        logger.info(e.toString());
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
