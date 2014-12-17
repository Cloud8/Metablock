package org.seaview.pdf;

import org.seaview.nlp.AbstractAnalyzer;

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

public class Cermine extends AbstractAnalyzer {

    // for monographs : only add references if found more than this
    private static final int MONOTHRESHOLD = 89; 

    protected static final String dct = DCTerms.NS;
	private PdfBxStructureExtractor cermine;
    private PdfNLMMetadataExtractor   bibExtractor;
    private PdfNLMReferencesExtractor refExtractor;

    private BxDocument doc;
    private boolean doTitle;
    private boolean doRefs;

    private int bad = 0;
    public int count;

    private static final String about /* will become rdf:about */
                                = "http://localhost/refs/";

    public Cermine(boolean title, boolean refs) {
        this.doTitle = title;
        this.doRefs = refs;
    }

    @Override
    public void dispose() {
    }

    @Override
    public AbstractAnalyzer create() {
        count = 0;
        // log("doTitle " + doTitle + " doRefs " + doRefs);
        try {
		    cermine = new PdfBxStructureExtractor();
            if (doTitle) {
                bibExtractor = new PdfNLMMetadataExtractor();
            }
            if (doRefs) {
                refExtractor = new PdfNLMReferencesExtractor();
            }
        } catch(AnalysisException e) { log(e); }
        return this;
    }

    @Override
    public void analyze(Model model, Resource rc, String fname) {
        log("analyze " + rc.getURI() + " [" + fname + "]");
        PDFLoader pl = new PDFLoader().create();
        pl.analyze(model, rc, fname);
        int threshold = 0;
        if (pl.size > 33) {
            threshold = MONOTHRESHOLD;
        }
        fname = pl.maltreat(3, 0.70); // pages 0-3 and 30 % from the tail 
        try {
            InputStream is = pl.createInputStream();
            Element metadata;
            Element[] references;
            doc = cermine.extractStructure(is);
            if (doTitle) {
                // log("cermine metadata start");
                //metadata = bibExtractor.extractMetadata(doc);
                metadata = bibExtractor.extractMetadataAsNLM(doc);
                //log(metadata);
				readCermine(metadata, rc, model);
		    }
            if (doRefs) {
                // log("cermine reference start");
			    //references = refExtractor.extractReferences(doc); 
			    references = refExtractor.extractReferencesAsNLM(doc); 
                //log(references);
                int x = fname.indexOf("/data/");
                String logname = x>0?fname.substring(0,x):fname;
                log(logname + " " + references.length + " references");
                count += references.length;
				readReferences(references, rc, model, threshold);
			}
            is.close();
            //log("cermine : all done.");
        } catch(FileNotFoundException e) {log(e);}
           catch(MalformedURLException e) {log(e);}
           catch(IOException e) {log(e);}
           catch(AnalysisException e) { e.printStackTrace(); log(e);}
        finally {
            pl.dispose();
            return ;
        }
    }

    @SuppressWarnings("unchecked")
    protected Resource readCermine(Element metadata, Resource rc, Model mod) 
        throws AnalysisException {
        if (metadata==null) {
            return rc;
        }
        //log("readCermine");
        Element meta = metadata;
        meta = meta==null?null:meta.getChild("front");
        meta = meta==null?null:meta.getChild("article-meta");
        meta = meta==null?null:meta.getChild("title-group");
        meta = meta==null?null:meta.getChild("article-title");
        String title = meta==null?null:meta.getText();
        rc = inject(rc, mod, "title", title);
        //log("title: " + title);

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
            for (String aut : authors) {
                rc = inject(rc, mod, "creator", aut);
                // log("author: " + aut);
            }
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
        //log("year: " + year);
        if (year!=null) {
            String issued = year + "-12-01";
            rc = inject(rc, mod, "issued", issued);
        }

        //String aid = meta==null?null:meta.getChild("article-id").getValue();
        //rc = inject(rc, mod, "identifier", aid);
        return rc;
    }

    private void readReferences(Element[] refs, Resource rc, Model org, int threshold)
        throws AnalysisException {
        // log("References: Cermine.");
        if (refs==null || refs.length==0) {
            log("No references found.");
            return;
        }
        Model mod = ModelFactory.createDefaultModel();
        // log("references found");
        Seq seq = mod.createSeq(rc.getURI() + "#References");
        String concept = dct + "BibliographicResource";
        Resource rcConcept = mod.createResource(concept);
        int found = 0;
        for (Element ref : refs) {
            String raw = ref.getValue();
            Element titleEl = ref.getChild("article-title"); 
            if (titleEl!=null) {
                String uri = null;
				String title = titleEl.getValue();
                if (title==null) {
                    continue;
                }
                if (raw.contains("http://")) {
                    List<String> links = pullLinks(raw);
                    if (links.size()>0) {
                        uri = links.get(0);
                    }
                } else if (raw.contains("arXiv:")) {
                    uri = getArxivId(raw);
                }
                if (uri==null || !validate(uri)) {
                    try {
                        uri = about + URLEncoder.encode(title, "UTF-8");
                        //log("article-title: " + title);
                    } catch(UnsupportedEncodingException e) { log(e); }
                } else {
                    log("uri: " + uri);
                }
                //log(ref);
				Resource rcx = mod.createResource(uri, rcConcept);
				rcx = inject(rcx, mod, "bibliographicCitation", raw);
                rcx = inject(rcx, mod, "title", title);
				seq.add(rcx);
                found++;
            }
        }
        if (found>threshold) {
            log("added " + found + " references");
			rc.addProperty(mod.createProperty(dct, "references"), seq);
            org.add(mod);
        } else {
            log("skipped " + found + " references");
        }
    }

    /** inject property only, if it does not exist already */
    private Resource inject(Resource rc, Model mod, String term, String val) {
       if (val==null) {
           return rc;
       }
       try {
           Property prop = mod.createProperty(dct,term);
           if (!rc.hasProperty(prop)) {
               rc.addProperty(prop, val);
           }
       } finally {
           return rc;
       }
    }

    private ArrayList<String> pullLinks(String text) {
    ArrayList<String> links = new ArrayList<String>();

    String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(text);
    while(m.find()) {
        String urlStr = m.group();
        if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
            urlStr = urlStr.substring(1, urlStr.length() - 1);
        }
        links.add(urlStr);
    }
    return links;
    }

    private String getArxivId(String raw) {
	    int x = raw.indexOf("arXiv:");
	    int y = raw.indexOf(" ",x);
		if (y>x && x>0) {
		    String arxiv = raw.substring(x+6,y);
			arxiv = arxiv.replaceAll("[^a-zA-Z0-9\\:\\.]","");
		    String url = "http://arxiv.org/abs/" + arxiv;
			return url;
		}
		return null;
    }

    public boolean validate(String url) {
        boolean b = true;
        final URI uri;
        try {
            uri = new URL(url).toURI();
            if (url.endsWith("-")) {
                log("bad uri: " + url);
                b = false;
            }
        } catch (MalformedURLException e) {
            b = false;
        } catch (URISyntaxException e) {
            b = false;
        } finally {
            return b;
        }
    }

    private static final Logger logger =
                         Logger.getLogger(Cermine.class.getName());

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
