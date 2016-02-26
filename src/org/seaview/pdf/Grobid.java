package org.seaview.pdf;

import org.shanghai.util.TextUtil;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.RDFS;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.naming.NameAlreadyBoundException;
import java.io.File;

import org.grobid.core.*;
import org.grobid.core.data.*;
import org.grobid.core.factory.*;
import org.grobid.core.mock.*;
import org.grobid.core.utilities.*;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.lang.Language;
import org.grobid.core.exceptions.GrobidException;

public class Grobid extends AbstractExtractor {

    private boolean doTitle;
    private boolean doRefs;
    private int bad;                      //Pattern.compile("[^\\x00-\\x7f]");
    private static final Pattern nonASCII = Pattern.compile("[^a-zA-Z]");
    private Property title;
    private String home;

    private Engine engine;
    public  int count; 

    private static final Pattern urlPattern = Pattern.compile(
        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public Grobid(boolean title, boolean refs, String home) {
        super(title, refs);
        this.home = home;
    }

    @Override
    public void create() {
        count = 0;
        try {
            String props = home + "/config/grobid.properties";
            MockContext.setInitialContext(home, props);
            GrobidProperties.getInstance();
            engine = GrobidFactory.getInstance().createEngine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        try {
            MockContext.destroyInitialContext();
        } catch (NameAlreadyBoundException e) {
            e.printStackTrace();
        }
        finally {
            return;
        }
    }

    // http://grobid.github.io/grobid-core/org/grobid/core/data/BiblioItem.html
    /** inject property title, creator, issued, abstract
        inject property references */
    @Override
    public void extractMetadata(Resource rc, String fname) {
        try {
            log("grobid title detection " + fname);
            BiblioItem bi = new BiblioItem();
            String tei = engine.processHeader(fname, false, bi);
            //rc = inject(rc, model, "creator", bi.getFirstAuthorSurname());
            //rc = inject(rc, model, "creator", bi.getAuthors());
            List<String> authors = new ArrayList<String>();
            if (bi.getFullAuthors()!=null) {
                int index = 1;
                for(Person prs: bi.getFullAuthors()) {
                   if (prs.getLastName()!=null && prs.getFirstName()!=null) {
                       authors.add(prs.getLastName()+", "+prs.getFirstName());
                   } else if (prs.getLastName()!=null && prs.getLastName().length()>0) {
                       authors.add(prs.getLastName());
                   }
                }
                if (authors.size()>0) {
                    injectAuthors(rc, authors.toArray(new String[authors.size()]));
                }
            }
            rc = inject(rc, DCTerms.title, bi.getTitle());
            List<String> subjects = bi.getSubjects();
            if (subjects!=null)
                for (String s : subjects) {
                    Resource skos = rc.getModel().createResource(SKOS.Concept);
                    //skos.addProperty(RDFS.label, s);
                    skos.addProperty(SKOS.prefLabel, s);
                    rc.addProperty(DCTerms.subject, skos);
                    log("subject " + s);
                    // rc = inject(rc, "subject", s);
                }
            if (bi.getYear()!=null) {
                String year = bi.getYear();
                rc = inject(rc, DCTerms.created, year);
                String month = bi.getMonth();
                month=month==null?"11":month;
                String day = bi.getDay();
                day=day==null?"11":day;
                String issued = year + "-" + month + "-" + day;
                rc = inject(rc, DCTerms.issued, issued);
            } else if (bi.getE_Year()!=null) {
                String year = bi.getE_Year();
                rc = inject(rc, DCTerms.created, year);
                String month = bi.getE_Month();
                String day = bi.getE_Day();
                String issued=month==null?year:year+"-"+month;
                issued=day==null?issued:issued+"-"+day;
                rc = inject(rc, DCTerms.issued, issued);
            }
            rc = inject(rc, DCTerms.abstract_, bi.getAbstract());
            if (bi.getKeywords()!=null) {
                for (Keyword keyword : bi.getKeywords()) {
                    String topic = keyword.getKeyword();
                    Resource skos = rc.getModel().createResource(SKOS.Concept);
                    //skos.addProperty(RDFS.label, topic);
                    skos.addProperty(SKOS.prefLabel, topic);
                    rc.addProperty(DCTerms.subject, skos);
                }
            }
            if (bi.getDOI()!=null && bi.getDOI().length()!=0) {
                String doi = bi.getDOI();
                doi = doi.startsWith("http://")?doi:"http://dx.doi.org/" + doi;
                rc = inject(rc, DCTerms.identifier, doi);
            }
        } catch (Exception e) {
            log(e); 
        } finally {
        }
    }

    @Override
    public void extractReferences(Resource rc, String fname, int threshold) {
        try {
                File file = new File(fname);
                List<BibDataSet> bdsl = engine.processReferences(file, false);
                readReferences(bdsl, rc, threshold);
            } catch (Exception e) {
                log(e); 
                // e.printStackTrace();
        } finally { }
    }

    @Override
    public String getTEI(String fname) {
        String tei = null;
        try {
            File file = new File(fname);
            //tei = engine.fullTextToTEI(file, false, false, null);
            GrobidAnalysisConfig cfg = GrobidAnalysisConfig.defaultInstance();
            tei = engine.fullTextToTEI(file, cfg);
            // make tei header:
            //BiblioItem resHeader = new BiblioItem();
            //tei = engine.processHeader(fname, false, resHeader);
        } catch (Exception e) {
            //e.printStackTrace();
            log(e); 
        } finally { 
            return tei;
        }
    }

    private void readReferences(List<BibDataSet> bdsl, Resource rc, int threshold) {
        int found = 0;
        // prevent garbage and add statements later to recource
        Model mod = ModelFactory.createDefaultModel();
        Seq seq = mod.createSeq(rc.getURI() + "/References");
        if (bdsl==null || bdsl.size()==0) {
            log("No references found.");
        } else {
            count += bdsl.size();
            //log(rc.getURI() + " : " + bdsl.size() + " refs.");
            for (BibDataSet bds : bdsl) {
                BiblioItem bd = bds.getResBib();
                if (bd==null) {
                    continue;
                }
                String symbol = symbol(bds.getRefSymbol());
                symbol = symbol==null?null:symbol.replaceAll("\\s","");
                String raw = TextUtil.clean(bds.getRawBib());
                String title = bd.getTitle();
                if (reject(raw)) {
                    continue;
                }
                if (title==null) {
                    continue;
                }

                String uri = bd.getURI();
                if (uri==null) {
                    uri = bd.getURL();
                }
                if (uri==null) {
                    uri = bd.getDOI();
                    if (uri!=null&&!uri.startsWith("http")) {
                        uri = "http://doi.org/" + bd.getDOI();
                    }
                }
                if (uri==null) {
                    uri = getUri(raw, title);
                } else {
                    log("grobid [" + uri + "]");
                }
                Resource ref = mod.createResource(uri, DCTerms.BibliographicResource);
                ref = inject(ref, DCTerms.bibliographicCitation, raw);
                ref = inject(ref, DCTerms.title, title);
                ref = inject(ref, DCTerms.identifier, "ref:" + symbol);
                List<String> authors = new ArrayList<String>();
                if (bd.getFullAuthors()!=null) {
                    int index = 1;
                    for(Person prs: bd.getFullAuthors()) {
                       authors.add(prs.getLastName()+", "+prs.getFirstName());
                    }
                    if (authors.size()>0) {
                        ref = injectAuthors(ref, 
                            authors.toArray(new String[authors.size()]));
                    }
                }
                if (bd.getPublicationDate()!=null) {
                    Date date = bd.getNormalizedPublicationDate();
                    String issued = date.getYearString();
                    if (issued==null) {
                        break;
                    }
                    int mon = date.getMonth();
                    if (mon>0) {
                        //String month = date.getMonthString();
                        String month = mon>9?""+mon:"0"+mon;
                        issued=mon==0?issued:issued+"-"+month;
                        String day = date.getDayString();
                        if (day!=null) {
                            day = day.length()==1?"0"+day:day;
                            issued = issued + "-" + day;
                        }
                    }
                    ref = inject(ref, DCTerms.date, issued);
                }
                seq.add(ref);
                found++;
            }
        }
        if (found>threshold && !test) {
            log("added " + found + " references [" + threshold + "]");
            rc.getModel().add(mod);
            rc.addProperty(DCTerms.references, seq);
        } else {
            if (test) {
                log("test: " + found + " references found.");
            } else {
                rc.addProperty(DCTerms.references, "");
                log("skipped " + found + " references, set to empty.");
            }
        }
    }

    private String[] getAuthors(String raw) {
        String[] list;
        if (raw.contains(",")) {
            list = raw.split(",");
        } else if (raw.contains("·")) {
            list = raw.split("·");
        } else {
            list = new String[1];
            list[0] = raw;
        }
        for (String str : list) {
            str = str.trim();
        }
        return list;
    }

    private static final Logger logger =
                         Logger.getLogger(Grobid.class.getName());

    protected void log(Exception e) {
        //e.printStackTrace();
        logger.severe(e.toString());
    }

    protected void log(String msg) {
        logger.info(msg);
    }

}
