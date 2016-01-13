package org.seaview.cite;

import org.seaview.cite.RefAnalyzer;
import org.shanghai.crawl.MetaCrawl.Analyzer;
import org.shanghai.util.FileUtil;
import org.shanghai.util.TextUtil;
import org.seaview.pdf.PDFLoader;
import org.seaview.pdf.HyphenRemover;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.shared.PropertyNotFoundException;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Citation Context Finder
    @date 2015-06-02
    @abstract Simple citation context finder without learning machinery
*/
public class RefContext extends RefAnalyzer implements Analyzer {

    protected Property hasContext;
    protected Property hasCount;
    private int size;
    private int count;
    private PDFLoader pl;
    private Map<String, Citation> hashTag;

    // simple feature set 
    private class Citation {
        String aut;
        String aut2;
        String year;
        String symbol;
        String raw;
        int count;
        List<String> context = new ArrayList<String>();
        public String toString() {
            return "[" + symbol + " " + aut + "#" + aut2 + "#" + year + "]";
        }
    }

    public RefContext() {
        pl = new PDFLoader();
        this.size = 3;
    }

    public RefContext(int size) { 
        pl = new PDFLoader();
        this.size = size;
    }

    public RefContext(String base) { 
        this.size = 3;
        pl = new PDFLoader(base);
    }

    @Override
    public Analyzer create() {
        pl.create();
        count = 0;
        hashTag = new HashMap<String, Citation>();
        return this;
    }

    @Override
    public void dispose() {
        pl.dispose();
        hashTag.clear();
        log("disposed " + probe());
    }

    @Override
    public Resource analyze(Resource rc) {
        rc.getModel().setNsPrefix("c4o", c4o);
        hasContext = rc.getModel().createProperty(c4o, "hasContext");
        hasCount = rc.getModel().createProperty(c4o, "hasInTextCitationFrequency");
        pl.create();
        String text = pl.fulltext(rc);
        pl.dispose();
        if (test) log("analyze " + rc.getURI());
        if (text==null) {
            log("text extraction failed. [" + rc.getURI() + "]");
        } else {
            super.analyze(rc); // calls analyzeReference for each reference
            if (test) show();
            if (hashTag==null) {
			    // log("all bad references.");
            } else {     
                analyzeText(rc, text); // hasContext for rc references
            }
        }
        return rc;
    }

    @Override // rc dcterms:references reference fill hashTag
    protected void analyzeReference(Resource rc, Resource reference) {
        if (!reference.hasProperty(DCTerms.bibliographicCitation)) {
            log("failed: " + " " + reference.getURI());
            return;
        }
        reference.removeAll(hasContext);
        reference.removeAll(hasCount);
        String ref = reference.getProperty(DCTerms.bibliographicCitation).getString();
        String uri = reference.getURI();
        Citation ctx = new Citation();
        ctx.raw = ref;
        if (reference.hasProperty(DCTerms.temporal)) {
			ctx.symbol = reference.getProperty(DCTerms.temporal).getString();
        } else if (ref.startsWith("[")) {
            ctx.symbol = ref.substring(ref.indexOf("["),ref.indexOf("]")+1);
		} 
        if (reference.hasProperty(DCTerms.date) 
            && reference.hasProperty(DCTerms.creator)) {
            ctx.year = reference.getProperty(DCTerms.date).getString();
        }
        if (reference.hasProperty(DCTerms.creator)) {
            Resource prs = reference.getProperty(DCTerms.creator).getSeq().getResource(1);
            if (prs!=null && prs.hasProperty(FOAF.name)) {
                ctx.aut = prs.getProperty(FOAF.name).getString();
                if (ctx.aut.contains(",")) {
                    ctx.aut = ctx.aut.substring(0, ctx.aut.indexOf(","));
                } else if (ctx.aut.contains(" ")) {
                    ctx.aut = ctx.aut.substring(ctx.aut.lastIndexOf(" ")+1);
                }
            }
            if (reference.getProperty(DCTerms.creator).getSeq().size()>1) {
                prs = reference.getProperty(DCTerms.creator).getSeq().getResource(2);
                if (prs!=null && prs.hasProperty(FOAF.name)) {
                ctx.aut2 = prs.getProperty(FOAF.name).getString();
                    if (ctx.aut2.contains(",")) {
                        ctx.aut2 = ctx.aut2.substring(0, ctx.aut2.indexOf(","));
                    } else if (ctx.aut2.contains(" ")) {
                        ctx.aut2 = ctx.aut2.substring(ctx.aut2.lastIndexOf(" ")+1);
                    }
                }
            }
        }
        if (ctx.aut!=null) {
            hashTag.put(uri, ctx);
        }
    }

    // set hasContext property for each reference of rc where a 
    // context could be found
    private void analyzeText(Resource rc, String text) {
        String lang = rc.hasProperty(DCTerms.language)?
                      rc.getProperty(DCTerms.language).getString():"en";
        if (test) log("analyze text (" + lang + ") " + rc.getURI());
        String docId = rc.getURI().substring(rc.getURI().lastIndexOf("/")+1);
        //log("docId " + docId);
        if (rc.hasProperty(DCTerms.references)) {
            Seq seq = rc.getProperty(DCTerms.references).getSeq();
            if (seq!=null && seq.size()>0) {
                analyzeText(text, docId);
                for (int i = 1; i< seq.size(); i++) {
                    Resource reference = seq.getResource(i);
                    if (hashTag.containsKey(reference.getURI())) {
                        Citation ctx = hashTag.get(reference.getURI());
                        for (String context : ctx.context) {
                            context = TextUtil.clean(context);
                            reference.addProperty(hasContext, context);
                            if (test) log("context [" + context + "][" + ctx.raw + "]" + ctx.toString());
                        }
                        if (ctx.count>1) {
                            reference.addProperty(hasCount, "" + ctx.count);
                        }
                    }
                }
            }
        }
    }

    // for each sentence in text, look up every citation and add context
    // if that sentence can be classified as an explicit citation 
    private void analyzeText(String text, String docId) {
        BufferedReader reader = new BufferedReader(new StringReader(text));
        String last=null;
        String context=null;
        String line=null;
        int count = 0;
        Citation seen = null;
        boolean stop = false;
        int textCount = 0;
        int bodySize = text.length()/3*2;
        try { 
            out: while( (line=reader.readLine()) != null ) {
                line = line.trim();
                for (Map.Entry<String, Citation> cursor : hashTag.entrySet()) {
                    Citation ctx = cursor.getValue();
                    if (seen!=null && seen==ctx && line.length()>0) 
                    { 
                       if (context.length() < 1024) {
                           context = count<size?context + " " + line:context;
                       } else {
                           context = context.substring(0, 1024);
                       }
                       if (count == size) {
                           double score = TextUtil.similarity(ctx.raw, context);
                           context = HyphenRemover.dehyphenate(context, docId);
                           context = context + " # " + docId + " " + score;
                           ctx.context.add(context);
                           ctx.count++;
                           seen = null;
                           context = null;
                           count = 0;
                       } else if (count < size) {
                           count++;
                       } else {
                           seen = null;
                           context = null;
                           count = 0;
                       }
                    }
                    if (textCount > bodySize) {
                        if (   line.contains("Reference") 
                            || line.contains("REFERENCES")
                            || line.contains("Bibliography") ) {
                            //log(size + " " + bodySize + " # " + line);
                            stop = true; // stop here
                            break out; 
                        }
                    }
                    boolean b = classify(last, line, ctx);
                    if (b) {
                        context = size==1?line:last + " " + line;
                        seen = ctx;
                        count = size==1?1:2;
                        //log("found " + ctx + " " +line);
                    }
                }
                if (line.length()>0) last = line;
                textCount += line.length();
            }
        } catch(IOException e) { log(e.toString()); }
        if (stop) ; // log("analyze text " + docId + " stopped " + line);
        else log("analyze text " + docId + " never stopped");
    }

    private void show() {
        for (Map.Entry<String, Citation> cursor : hashTag.entrySet()) {
            Citation ctx = cursor.getValue();
            log(ctx.toString());
        }
    }

    private static final Logger logger =
                         Logger.getLogger(RefContext.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected boolean classify(String last, String line, Citation citation) {
        if (citation.symbol==null) {
            // feature not set
        } else if (line.contains(citation.symbol)) {
            // log("found " + citation.symbol + " " +line);
            return true;
        } 
        if (citation.aut==null || citation.year==null) {
            // feature not set
        } else if (line.contains(citation.aut) 
            && line.contains(citation.year)) {
            if (test) log(line);
            return true;
        } else if (line.contains(citation.aut) && citation.aut2!=null) { 
            if (line.contains(citation.aut2)) {
                //if (line.contains("Abu-Jbara")) 
                //log(citation + " # " + line);
                //return true;
            }
        }
        return false;
    }
}
