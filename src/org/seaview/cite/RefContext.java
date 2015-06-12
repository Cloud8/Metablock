package org.seaview.cite;

import org.shanghai.crawl.MetaCrawl.Storage;
import org.shanghai.crawl.MetaCrawl.Transporter;
import org.shanghai.util.FileUtil;
import org.seaview.data.AbstractAnalyzer;
import org.seaview.cite.RefAnalyzer;
import org.seaview.pdf.PDFLoader;
import org.seaview.util.HyphenRemover;
import org.seaview.util.TextUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.vocabulary.DCTerms;

import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.File;
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
*/
public class RefContext extends RefAnalyzer {

    protected Property hasContext;
    protected Property hasCount;
    protected Property name;
    private int size;
    private int count;
    private String text;
    private PDFLoader pl;
    private boolean debug = false;

    private class Context {
        int count;
        List<String> context = new ArrayList<String>();
        String aut;
        String year;
        String ref;
        public String toString() {
            return ref==null?aut+" "+year:ref;
        }
    }

    private Map<String, Context> hashTag;

    public RefContext() {
        this.size = 3;
    }

    public RefContext(int size) { 
        this.size = size;
    }

    @Override
    public AbstractAnalyzer create() {
        //super.create();
        text = null;
        count = 0;
        hashTag = new HashMap<String, Context>();
        pl = new PDFLoader();
        return this;
    }

    @Override
    public void dispose() {
        hashTag.clear();
        text = null;
    }

    @Override
    public void analyze(Model model, Resource rc, String id) {
        model.setNsPrefix("c4o", c4o);
        hasContext = model.createProperty(c4o, "hasContext");
        hasCount = model.createProperty(c4o, "hasInTextCitationFrequency");
        name = model.createProperty(foaf, "name");
        pl.create();
        text = pl.fulltext(model, rc, id);
        pl.dispose();
        super.analyze(model, rc, id); // fill up hashTag: analyzeReference
        analyzeText(model, rc, id); // set hasContext for references
    }


    @Override
    protected void analyzeReference(Model model, Resource rc, Resource obj) {
        if (!obj.hasProperty(bibliographicCitation)) {
            log("failed: " + " " + obj.getURI());
            return;
        }
        obj.removeAll(hasContext);
        obj.removeAll(hasCount);
        String ref = obj.getProperty(bibliographicCitation).getString();
        String uri = obj.getURI();
        Context ctx = new Context();
        if (ref.startsWith("[")) {
            ctx.ref = ref.substring(ref.indexOf("["),ref.indexOf("]")+1);
            hashTag.put(uri, ctx);
        } else if (obj.hasProperty(date) && obj.hasProperty(creator)) {
            ctx.year = obj.getProperty(date).getString();
            Resource prs = obj.getProperty(creator).getSeq().getResource(1);
            if (prs!=null && prs.hasProperty(name)) {
                ctx.aut = prs.getProperty(name).getString();
                if (ctx.aut.contains(",")) {
                    ctx.aut = ctx.aut.substring(0, ctx.aut.indexOf(","));
                }
                if (ctx.year!=null && ctx.aut!=null) {
                    hashTag.put(uri, ctx);
                }
            }
        } else {
            if (debug) log("no context for: " + rc.getURI());
        }
    }

    private void analyzeText(String text, String docId) {
        //text = HyphenRemover.dehyphenate(text, docId);
        //text = TextUtil.cleanUTF(text);
        //text = text.replaceAll("\\p{Co}", "");
        text = TextUtil.sentence(text);
        BufferedReader reader = new BufferedReader(new StringReader(text));
        String last=null;
        String context=null;
        String line=null;
        int count = 0;
        Context seen = null;
        try {
            while( (line=reader.readLine()) != null ) {
                for (Map.Entry<String, Context> cursor : hashTag.entrySet()) {
                    Context ctx = cursor.getValue();
                    if (seen!=null && seen==ctx && line.length()>0) 
                    { 
                       if (context.length()<512) {
                           context = size==2?context:context + " " + line;
                       } else {
                           context = context.substring(0,512);
                       }
                       ctx.context.add(context);
                       ctx.count++;
                       count++;
                       if (count==size) {
                           seen = null;
                           context = null;
                       }
                    }
                    line = line.trim();
                    if (ctx.ref==null) {
                    } else if (line.startsWith(ctx.ref)) {
					    //skip reference section itself
                    } else if (line.contains(ctx.ref)) {
                        context = last + " " + line;
                        seen = ctx;
                    } 
                    if (ctx.aut==null || ctx.year==null) {
                    } else if (line.contains(ctx.aut) 
                            && line.contains(ctx.year)) {
                        context = size==1?line:last + " " + line;
                        seen = ctx;
                        if (debug) log("context [" + line + "]");
                        count = size==1?1:2;
                    }
                }
                if (line.length()>0) last = line;
            }
        } catch(IOException e) { log(e.toString()); }
    }

    private void analyzeText(Model model, Resource rc, String id) {
        String lang = rc.hasProperty(language)?
                      rc.getProperty(language).getString():"en";
        if (debug) log("analyze text: " + lang + " " + rc.getURI());
        analyzeText(text, rc.getURI());
        if (rc.hasProperty(references)) {
            Seq seq = rc.getProperty(references).getSeq();
            if (seq!=null) {
                for (int i = 1; i< seq.size(); i++) {
                    Resource obj = seq.getResource(i);
                    if (hashTag.containsKey(obj.getURI())) {
                        Context ctx = hashTag.get(obj.getURI());
                        for (String context : ctx.context) {
                            RDFNode li = model.createTypedLiteral(context);
                            obj.addProperty(hasContext, li);
                        }
                        if (ctx.count>1) {
                            RDFNode li = model.createTypedLiteral(ctx.count);
                            obj.addProperty(hasCount, li);
                        }
                    }
                }
            }
        }
    }

    private static final Logger logger =
                         Logger.getLogger(RefContext.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

}
