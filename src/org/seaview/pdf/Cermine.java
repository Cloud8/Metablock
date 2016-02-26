package org.seaview.pdf;

import pl.edu.icm.cermine.PdfBxStructureExtractor;
import pl.edu.icm.cermine.metadata.model.DocumentMetadata;
import pl.edu.icm.cermine.PdfBxStructureExtractor;
import pl.edu.icm.cermine.PdfNLMMetadataExtractor;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.PdfNLMReferencesExtractor;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.ComponentConfiguration;
import pl.edu.icm.cermine.ExtractionUtils;

import pl.edu.icm.cermine.bibref.BibReferenceExtractor;
import pl.edu.icm.cermine.bibref.sentiment.model.CitationPosition;
import pl.edu.icm.cermine.bibref.sentiment.model.CitationSentiment;
import pl.edu.icm.cermine.bibref.sentiment.model.CiTOProperty;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.SKOS;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.logging.Logger;
import java.util.List;
import java.util.Set;
import java.net.MalformedURLException;
import java.lang.IllegalArgumentException;
import com.google.common.collect.Lists;

/*
  see : cermine-impl/src/main/java/pl/edu/icm/cermine/RDFGenerator.java
 */
public class Cermine extends AbstractExtractor {

	private PdfBxStructureExtractor cermine;
    private PdfNLMMetadataExtractor   bibExtractor;
    private PdfNLMReferencesExtractor refExtractor;
    private BxDocument bxdoc;

    public Cermine(boolean title, boolean refs) {
        super(title, refs);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void create() {
        count = 0;
        try {
		    cermine = new PdfBxStructureExtractor();
            if (title) {
                bibExtractor = new PdfNLMMetadataExtractor();
            }
            if (refs) {
                refExtractor = new PdfNLMReferencesExtractor();
            }
        } catch(AnalysisException e) { log(e); }
    }

    public void create(String fname) {
        try {
            InputStream is = new FileInputStream(fname);
            bxdoc = cermine.extractStructure(is);
            is.close();
        } catch(FileNotFoundException e) {log(e);}
           catch(MalformedURLException e) {log(e);}
           catch(IOException e) {log(e);}
           catch(AnalysisException e) {log(e);}
           catch(IllegalArgumentException e) {log(e);}
        finally {
            return;
        }
    }

    @Override
    public void extractMetadata(Resource rc, String fname) {
        try {
            Element metadata = bibExtractor.extractMetadataAsNLM(bxdoc);
			readCermine(metadata, rc);
        } catch(AnalysisException e) { e.printStackTrace(); log(e);}
    }

    @Override
    public void extractReferences(Resource rc, String fname, int threshold) {
        try {
            Element[] refArray = refExtractor.extractReferencesAsNLM(bxdoc); 
            count += refArray.length;
		    readReferences(refArray, rc, threshold);
        } catch(AnalysisException e) { e.printStackTrace(); log(e);}
        // GH20151231 bonus : test cermines citation sentiment analysis
        if (test) try {
            List<CitationSentiment> sentiments = getCitationSentiments();
            for (CitationSentiment sentiment : sentiments) {
                Set<CiTOProperty> properties = sentiment.getProperties();
                log(properties.toString());
            }
        } catch(AnalysisException e) { e.printStackTrace(); log(e);}
    }

    @SuppressWarnings("unchecked")
    protected Resource readCermine(Element metadata, Resource rc) 
        throws AnalysisException {
        if (metadata==null) {
            return rc;
        }
        Element meta = metadata;
        meta = meta==null?null:meta.getChild("front");
        meta = meta==null?null:meta.getChild("article-meta");
        meta = meta==null?null:meta.getChild("title-group");
        meta = meta==null?null:meta.getChild("article-title");
        String title = meta==null?null:meta.getText();
        if (title==null || rc.hasProperty(DCTerms.title)) {
            //
        } else {
            rc.addProperty(DCTerms.title, title);
        }

        meta = metadata;
        meta = meta==null?null:meta.getChild("front");
        meta = meta==null?null:meta.getChild("article-meta");
        meta = meta==null?null:meta.getChild("contrib-group");
        if (meta!=null && !rc.hasProperty(DCTerms.creator)) {
            String[] authors = new String[meta.getContentSize()];
            int i=0;
            for (Element el : (List<Element>)meta.getChildren()) {
                authors[i++] = el.getChildText("string-name");
            }
            rc = injectAuthors(rc, authors);
        }

        meta = metadata;
        // log(meta);
        meta = meta==null?null:meta.getChild("front");
        meta = meta==null?null:meta.getChild("article-meta");
        meta = meta==null?null:meta.getChild("abstract");
        meta = meta==null?null:meta.getChild("p");
        String abstract_ = meta==null?null:meta.getText();
        if (abstract_==null || rc.hasProperty(DCTerms.abstract_)) {
            //
        } else {
            rc.addProperty(DCTerms.abstract_, abstract_);
        }

        meta = metadata;
        meta = meta==null?null:meta.getChild("front");
        meta = meta==null?null:meta.getChild("article-meta");
        meta = meta==null?null:meta.getChild("pub-date");
        String year = meta==null?null:meta.getChild("year").getText();
        String month = null;
        if (meta!=null && meta.getChild("month")!=null) {
            month = meta.getChild("month").getText();
        }
        String day = null;
        if (meta!=null && meta.getChild("day")!=null) {
            day = meta.getChild("day").getText();
        }
        if (test) log("year: " + year);
        if (year==null || rc.hasProperty(DCTerms.created)) {
            //
        } else {
            rc.addProperty(DCTerms.created, year);
            if (month!=null && day !=null) {
                String issued = year + "-" + month + "-" + day;
                rc.addProperty(DCTerms.issued, issued);
            }
        }

        meta = metadata;
        meta = meta==null?null:meta.getChild("front");
        meta = meta==null?null:meta.getChild("article-meta");
        meta = meta==null?null:meta.getChild("kwd-group");
        if (meta==null || rc.hasProperty(DCTerms.subject)) {
            //
        } else {
            List<Element> childs = meta.getChildren("kwd");
            for(Element kw : childs) {
                Resource skos = rc.getModel().createResource(SKOS.Concept);
                skos.addProperty(SKOS.prefLabel, kw.getText().trim());
                rc.addProperty(DCTerms.subject, skos);
            }
        }
        if (test) log(metadata);
        return rc;
    }

    @SuppressWarnings({"unchecked"})
    private void readReferences(Element[] refs, Resource rc, int threshold)
        throws AnalysisException {
        // log("References: Cermine.");
        int found = 0;
        Model mod = ModelFactory.createDefaultModel();
        Seq seq = mod.createSeq(rc.getURI() + "#References");
        if (refs==null || refs.length==0) {
            log("No references found.");
        } else {
            //log(refs);
            for (Element element : refs) {
                String raw = element.getValue();
                if ( reject(raw) ) {
                    continue ;
                }
                Element titleEl = element.getChild("article-title"); 
                if (titleEl!=null) {
				    String title = titleEl.getValue();
                    if (title==null) {
                        continue;
                    }
                    String uri = getUri(raw, title);
				    Resource ref = mod.createResource(uri, DCTerms.BibliographicResource);
				    ref = inject(ref, DCTerms.bibliographicCitation, raw);
                    ref = inject(ref, DCTerms.title, title);
				    Element year = element.getChild("year");
                    if (year!=null) {
                        ref = inject(ref, DCTerms.date, year.getValue());
                    }
                    List<String> authors = new ArrayList<String>();
                    for (Element el : 
                          (List<Element>)element.getChildren("string-name")) {
                        authors.add(el.getValue());
                    }
                    if (authors.size()>0) {
                        ref = injectAuthors(ref,
                            authors.toArray(new String[authors.size()]));
                    }
				    seq.add(ref);
                    found++;
                }
            }
        }
        if (found>threshold && !test) {
            log("added " + found + " references");
			rc.removeAll(DCTerms.references);
			rc.addProperty(DCTerms.references, seq);
            rc.getModel().add(mod);
        } else {
            if (test) {
                log("test: " + found + " references found.");
            } else {
                rc.addProperty(DCTerms.references, "");
                log("skipped " + found + " references, set to empty.");
            }
        }
    }

    /** citation positions */
    private List<List<CitationPosition>> citationPositions;

    /** document's list of references */
    private List<BibEntry> references;
    
    /** citation sentiments */
    private List<CitationSentiment> citationSentiments;

    /** raw full text */
    private String rawFullText;

    private ComponentConfiguration conf;

    private void createConf() throws AnalysisException {
        if (conf==null) {
            conf = new ComponentConfiguration();
        }
    }

    /**
     * Extracts raw text.
     * 
     * @return raw text
     * @throws AnalysisException 
     */
    private String getRawFullText() throws AnalysisException {
        createConf();
        if (rawFullText == null) {
            rawFullText = ExtractionUtils.extractRawText(conf, bxdoc);
        }
        return rawFullText;
    }

    /**
     * Extracts the locations of the document's citations.
     * 
     * @return the locations
     * @throws AnalysisException 
     */
    private List<List<CitationPosition>> getCitationPositions() throws AnalysisException {
        if (citationPositions == null) {
            getRawFullText();
            if (references==null) {
                references = Lists.newArrayList(ExtractionUtils.extractReferences(conf, bxdoc));
            }
            citationPositions = ExtractionUtils.findCitationPositions(conf, rawFullText, references);
        }
        return citationPositions;
    }

    /**
     * Extractes the sentiments of the document's citations.
     * 
     * @return the citation sentiments
     * @throws AnalysisException 
     */
    private List<CitationSentiment> getCitationSentiments() throws AnalysisException {
        if (citationSentiments == null) {
            getCitationPositions();
            citationSentiments = ExtractionUtils.analyzeSentimentFromPositions(conf, rawFullText, citationPositions);
        }
        return citationSentiments;
    }

    private static final Logger logger =
                         Logger.getLogger(Cermine.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        logger.severe(e.toString());
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
