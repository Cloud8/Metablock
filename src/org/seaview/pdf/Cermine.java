package org.seaview.pdf;

import org.seaview.data.AbstractAnalyzer;

import pl.edu.icm.cermine.PdfBxStructureExtractor;
import pl.edu.icm.cermine.metadata.model.DocumentMetadata;
import pl.edu.icm.cermine.PdfBxStructureExtractor;
import pl.edu.icm.cermine.PdfNLMMetadataExtractor;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.PdfNLMReferencesExtractor;
import pl.edu.icm.cermine.bibref.model.BibEntry;

import pl.edu.icm.cermine.bibref.BibReferenceExtractor;

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
import java.lang.IllegalArgumentException;

/*
  nice : http://archiv.ub.uni-marburg.de/diss/z2015/0047 ref. 66
  see : cermine-impl/src/main/java/pl/edu/icm/cermine/RDFGenerator.java
 */
public class Cermine extends AbstractExtractor {

	private PdfBxStructureExtractor cermine;
    private PdfNLMMetadataExtractor   bibExtractor;
    private PdfNLMReferencesExtractor refExtractor;
    private BxDocument doc;

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
            doc = cermine.extractStructure(is);
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
    public void extractMetadata(Model model, Resource rc, String fname) {
        try {
            Element metadata = bibExtractor.extractMetadataAsNLM(doc);
			readCermine(metadata, rc, model);
        } catch(AnalysisException e) { e.printStackTrace(); log(e);}
    }

    @Override
    public void extractReferences(Model model, Resource rc, String fname, int threshold) {
        references = model.createProperty(dct, "references");
        try {
            Element[] refArray = refExtractor.extractReferencesAsNLM(doc); 
            count += refArray.length;
		    readReferences(refArray, rc, model, threshold);
        } catch(AnalysisException e) { e.printStackTrace(); log(e);}
    }

    @SuppressWarnings("unchecked")
    protected Resource readCermine(Element metadata, Resource rc, Model mod) 
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
        rc = inject(rc, mod, "title", title);

        meta = metadata;
        meta = meta==null?null:meta.getChild("front");
        meta = meta==null?null:meta.getChild("article-meta");
        meta = meta==null?null:meta.getChild("contrib-group");
        if (meta!=null) {
            String[] authors = new String[meta.getContentSize()];
            int i=0;
            for (Element el : (List<Element>)meta.getChildren()) {
                authors[i++] = el.getChildText("string-name");
            }
            rc = injectAuthors(mod, rc, authors);
        }

        meta = metadata;
        meta = meta==null?null:meta.getChild("front");
        meta = meta==null?null:meta.getChild("article-meta");
        meta = meta==null?null:meta.getChild("abstract");
        meta = meta==null?null:meta.getChild("p");
        String abstract_ = meta==null?null:meta.getText();
        //log("abstract: " + abstract_);
        rc = inject(rc, mod, "abstract", abstract_);

        meta = metadata;
        meta = meta==null?null:meta.getChild("front");
        meta = meta==null?null:meta.getChild("article-meta");
        meta = meta==null?null:meta.getChild("pub-date");
        meta = meta==null?null:meta.getChild("year");
        String year = meta==null?null:meta.getText();
        if (test) log("year: " + year);
        if (year!=null) {
            String issued = year + "-12-01";
            rc = inject(rc, mod, "issued", issued);
        }

        //String aid = meta==null?null:meta.getChild("article-id").getValue();
        //rc = inject(rc, mod, "identifier", aid);
        return rc;
    }

    @SuppressWarnings({"unchecked"})
    private void readReferences(Element[] refs, Resource rc, Model org, int threshold)
        throws AnalysisException {
        // log("References: Cermine.");
        int found = 0;
        Model mod = ModelFactory.createDefaultModel();
        Seq seq = mod.createSeq(rc.getURI() + "#References");
        if (refs==null || refs.length==0) {
            log("No references found.");
        } else {
            //log(refs);
            String concept = dct + "BibliographicResource";
            Resource rcConcept = mod.createResource(concept);
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

				    Resource ref = mod.createResource(uri, rcConcept);
				    ref = inject(ref, mod, "bibliographicCitation", raw);
                    ref = inject(ref, mod, "title", title);
				    Element year = element.getChild("year");
                    if (year!=null) {
                        ref = inject(ref, mod, "date", year.getValue());
                    }
                    List<String> authors = new ArrayList<String>();
                    for (Element el : 
                          (List<Element>)element.getChildren("string-name")) {
                        //rc = inject(rc, mod, "creator", el.getValue());
                        authors.add(el.getValue());
                    }
                    if (authors.size()>0) {
                        ref = injectAuthors(mod, ref,
                            authors.toArray(new String[authors.size()]));
                    }
				    //Element creator = ref.getChild("string-name");
                    //if (creator!=null) {
                    //    rcx = inject(rcx, mod, "creator", creator.getValue());
                    //}
				    seq.add(ref);
                    found++;
                }
            }
        }
        if (found>threshold && !test) {
            log("added " + found + " references");
			rc.removeAll(references);
			rc.addProperty(references, seq);
            org.add(mod);
        } else {
            if (test) {
                log("test: " + found + " references found.");
            } else {
                rc.addProperty(references, "");
                log("skipped " + found + " references, set to empty.");
            }
        }
    }

    private static final Logger logger =
                         Logger.getLogger(Cermine.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        logger.severe(e.toString());
    }

    protected void log(Element el) {
        XMLOutputter outp = new XMLOutputter(Format.getPrettyFormat());
        String str = outp.outputString(el);
        System.out.println(str);
    }
}
