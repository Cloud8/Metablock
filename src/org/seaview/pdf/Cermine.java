package org.seaview.pdf;

import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.metadata.model.DocumentMetadata;
import pl.edu.icm.cermine.metadata.model.DocumentAuthor;
import pl.edu.icm.cermine.metadata.model.DocumentAffiliation;

import pl.edu.icm.cermine.exception.AnalysisException;
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
import org.apache.jena.vocabulary.VCARD;
import org.apache.jena.sparql.vocabulary.FOAF;

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

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @title Wrapper to use CERMINE for metadata and reference extraction
    @date 2016-05-13
    see : cermine-impl/src/main/java/pl/edu/icm/cermine/RDFGenerator.java
 */
public class Cermine extends AbstractExtractor {

	private ContentExtractor cermine;
    private BxDocument bxdoc;

    public Cermine(boolean title, boolean refs) {
        super(title, refs);
    }

    @Override
    public void dispose() {
        cermine = null;
        bxdoc = null;
    }

    @Override
    public void create() {
        count = 0;
        try {
		    cermine = new ContentExtractor();
        } catch(AnalysisException e) { log(e); }
    }

    @Override
    public void create(String fname) {
        try {
            InputStream is = null;
            if (fname.startsWith("http")) {
                is = new URL(fname).openStream();
            } else {
                is = new FileInputStream(fname);
            }
            //cermine.setPDF(new FileInputStream(fname));
            cermine.setPDF(is);
            bxdoc = cermine.getBxDocument();
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
    public void extractMetadata(Resource rc) {
        try {
            DocumentMetadata dm = cermine.getMetadata();
			readCermine(dm, rc);
        } catch(AnalysisException e) { e.printStackTrace(); log(e);}
    }

    @Override
    public void extractReferences(Resource rc, int threshold) {
        try {
            List<BibEntry> references = cermine.getReferences();
            count += references.size();
		    readReferences(references, rc, threshold);
        } catch(AnalysisException e) { e.printStackTrace(); log(e);}
        // GH20151231 bonus track : test cermines citation sentiment analysis
        // if (test) try {
        //     List<CitationSentiment> sentiments = getCitationSentiments();
        //     for (CitationSentiment sentiment : sentiments) {
        //         Set<CiTOProperty> properties = sentiment.getProperties();
        //         log(properties.toString());
        //     }
        // } catch(AnalysisException e) { e.printStackTrace(); log(e);}
    }

    protected Resource readCermine(DocumentMetadata dm, Resource rc) {
        if (dm.getTitle()==null || rc.hasProperty(DCTerms.title)) {
            // conservative
        } else {
            rc.addProperty(DCTerms.title, dm.getTitle());
        }
        List<DocumentAuthor> authors = dm.getAuthors();
        String[] aut = new String[authors.size()];
        for (int i=0; i<authors.size(); i++) {
            DocumentAuthor author = authors.get(i);
            author = readAffiliations(author, rc, i); // add provenance
            aut[i++] = author.getName(); //
        }
        rc = injectAuthors(rc, aut); // non-invasive

        String abstract_ = dm.getAbstrakt();
        if (abstract_==null || rc.hasProperty(DCTerms.abstract_)) {
            //
        } else {
            rc.addProperty(DCTerms.abstract_, abstract_);
        }

        if (dm.getPublisher()==null || rc.hasProperty(DCTerms.publisher)) {
            // nothing
        } else {
            Resource publisher = rc.getModel().createResource(FOAF.Organization);
            publisher.addProperty(FOAF.name, dm.getPublisher());
            rc.addProperty(DCTerms.publisher, publisher);
        }

        List<String> kws = dm.getKeywords();
        if (kws!=null) {
            for (String kw : kws) {
                Resource skos = rc.getModel().createResource(SKOS.Concept);
                skos.addProperty(SKOS.prefLabel, kw.trim());
                rc.addProperty(DCTerms.subject, skos);
            }
        }

        // GH201605 : getJournal() getVolume() getIssue() getJournalISSN()
        String str = dm.getJournal();
        String source = str==null?"":str;
        str = dm.getVolume();
        source = str==null?source:source + " vol. " + str;
        str = dm.getIssue();
        source = str==null?source:source + " no. " + str;
        str = dm.getJournalISSN();
        source = str==null?source:source + " issn " + str;
        if (source.trim().length()>0 || rc.hasProperty(DCTerms.source)) {
            rc.addProperty(DCTerms.source, source);
        }
        return rc;
    }

    // GH20160619 : TBD -- for now, affiliations as provenance
    protected DocumentAuthor readAffiliations(DocumentAuthor author, 
        Resource rc, int autId) {
        String prefix = "http://example.org/";
        Model model = rc.getModel();
        String docURI = rc.getURI() + "/affiliations";
        // Resource docRes = model.createResource(docURI);
        int affId = 0;
        for (DocumentAffiliation docAffiliation : author.getAffiliations()) {
            //String affURI = prefix + "affiliation/" + volDoc + "_" + affId;
            String affURI = prefix + "affiliation#" + affId;
            String autURI = prefix + "author/" + author.getName().replaceAll("\\s", "-");
            String countryName = docAffiliation.getCountry();
            if (countryName == null) {
                countryName = "";
            }
            String countryURI = prefix + "country/" + countryName.replaceAll("\\s", "-");
            String org = docAffiliation.getOrganization();
            if (org == null) {
                // org = "";
            } else {
                log("found affilliation " + org);
                Resource affiliation = model.createResource(affURI);
                affiliation.addProperty(VCARD.Orgname, org);
                Resource docAuthor = model.createResource(autURI);
                docAuthor.addProperty(VCARD.FN, author.getName());
                affiliation.addProperty(DCTerms.contributor, docAuthor);
                if (!countryName.isEmpty()) {
                    Resource country = model.createResource(countryURI);
                    country.addProperty(VCARD.NAME, countryName);
                    affiliation.addProperty(VCARD.Country, country);
                }
                // docRes.addProperty(DCTerms.creator, affiliation);
                rc.addProperty(DCTerms.provenance, affiliation);
                affId++;
            }
        }
        return author;
    }

    private void readReferences(List<BibEntry> refs, Resource rc, int thresh) {
        log("References: Cermine.");
        int found = 0;
        if (refs==null || refs.size()==0) {
            log("No references found.");
            return;
        }
        Model mod = ModelFactory.createDefaultModel();
        Seq seq = mod.createSeq(rc.getURI() + "#References");
        for (BibEntry ref : refs) {
            String raw = ref.getText();
            String title = ref.getFirstFieldValue(BibEntry.FIELD_TITLE);
            if (title==null) continue;
            String uri = getUri(raw, title);
			Resource rf = mod.createResource(uri,DCTerms.BibliographicResource);
			rf = inject(rf, DCTerms.bibliographicCitation, raw);
            rf = inject(rf, DCTerms.title, title);
            String year = ref.getFirstFieldValue(BibEntry.FIELD_YEAR);
            rf = inject(rf, DCTerms.date, year);
            log("title " + title);
            List<String> aut = ref.getAllFieldValues(BibEntry.FIELD_AUTHOR);
            if (aut.size()>0) {
                rf = injectAuthors(rf, aut.toArray(new String[aut.size()]));
            }
			seq.add(rf);
            found++;
        }
        if (found>thresh && !test) {
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

    /**
     * Extracts raw text.
     * 
     * @return raw text
     * @throws AnalysisException 
     */
    private String getRawFullText() throws AnalysisException {
        if (rawFullText == null) {
            rawFullText = cermine.getRawFullText();
        }
        return rawFullText;
    }

    /**
     * Extracts the locations of the document's citations.
     * 
     * @return the locations
     * @throws AnalysisException 
     */
    /*
    private List<List<CitationPosition>> getCitationPositions() throws AnalysisException {
        if (citationPositions == null) {
            getRawFullText();
            if (references==null) {
                // references = Lists.newArrayList(ExtractionUtils.extractRefStrings(conf, bxdoc));
                references = Lists.newArrayList(ExtractionUtils.extractRefStrings(cermine.getConf(), bxdoc));
            }
            citationPositions = ExtractionUtils.findCitationPositions(cermine.getConf(), rawFullText, references);
        }
        return citationPositions;
    }
    */

    /**
     * Extractes the sentiments of the document's citations.
     * 
     * @return the citation sentiments
     * @throws AnalysisException 
     */
    /*
    private List<CitationSentiment> getCitationSentiments() throws AnalysisException {
        if (citationSentiments == null) {
            getCitationPositions();
            // citationSentiments = ExtractionUtils.analyzeSentimentFromPositions(conf, rawFullText, citationPositions);
        }
        return citationSentiments;
    }
    */

    private static final Logger logger =
                         Logger.getLogger(Cermine.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        logger.severe(e.toString());
        e.printStackTrace();
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
