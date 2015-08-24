package org.seaview.pdf;

import org.seaview.data.AbstractAnalyzer;
import org.seaview.util.TextUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileUtils;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.net.URLEncoder;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.naming.NameAlreadyBoundException;

import org.grobid.core.*;
import org.grobid.core.data.*;
import org.grobid.core.factory.*;
import org.grobid.core.mock.*;
import org.grobid.core.utilities.*;
import org.grobid.core.engines.Engine;
import org.grobid.core.lang.Language;
import org.grobid.core.exceptions.GrobidException;

public class Grobid extends AbstractExtractor {

    //private static final String dct = DCTerms.NS;
    private boolean doTitle;
    private boolean doRefs;
    private int bad;                      //Pattern.compile("[^\\x00-\\x7f]");
    private static final Pattern nonASCII = Pattern.compile("[^a-zA-Z]");
    private Property title;
    private String home;

    private Engine engine;
    public  int count; 

    private static final String about /* will become rdf:about */
                                = "http://localhost/refs/";
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
    public void extractMetadata(Model model, Resource rc, String fname) {
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
                   } else if (prs.getLastName()!=null) {
                       authors.add(prs.getLastName());
                   }
                }
                if (authors.size()>0) {
                    injectAuthors(model, rc, 
                        authors.toArray(new String[authors.size()]));
                }
            }
            rc = inject(rc, model, "title", bi.getTitle());
            List<String> subjects = bi.getSubjects();
            if (subjects!=null)
                for (String s : subjects)
                     rc = inject(rc, model, "subject", s);
            if (bi.getYear()!=null) {
                String year = bi.getYear();
                String month = bi.getMonth();
                month=month==null?"11":month;
                String day = bi.getDay();
                day=day==null?"11":day;
                String issued = year + "-" + month + "-" + day;
                rc = inject(rc, model, "issued", issued);
            } else if (bi.getE_Year()!=null) {
                String issued = bi.getE_Year();
                String month = bi.getE_Month();
                String day = bi.getE_Day();
                issued=month==null?issued:issued+"-"+month;
                issued=day==null?issued:issued+"-"+day;
                rc = inject(rc, model, "issued", issued);
            }
            rc = inject(rc, model, "abstract", bi.getAbstract());
            if (bi.getKeywords()!=null) {
                for (Keyword keyword : bi.getKeywords()) {
                    String topic = keyword.getKeyword();
                    rc = inject(rc, model, "subject", topic);
                }
            }
            rc = inject(rc, model, "hasDOI", bi.getDOI());
            //String label = bi.getNumber();
            //if (label!=null) {
            //    log("label " + label);
            //    rc.addProperty(model.createProperty(fabio, "label"), label);
            //}
        } catch (Exception e) {
            log(e); 
        } finally {
        }
    }

    @Override
    public void extractReferences(Model model, Resource rc, String fname, int threshold) {
        try {
                List<BibDataSet> bdsl = engine.processReferences(fname, false);
                readReferences(bdsl, rc, model, threshold);
            } catch (Exception e) {
                log(e); 
                // e.printStackTrace();
            } finally { }
    }

    private void readReferences(List<BibDataSet> bdsl, Resource rc, Model org, int threshold) {
        //rc.removeAll(DCTerms.references); 
        int found = 0;
        Model mod = ModelFactory.createDefaultModel();
        Seq seq = mod.createSeq(rc.getURI() + "#References");
        if (bdsl==null || bdsl.size()==0) {
            log("No references found.");
        } else {
            count += bdsl.size();
            //log(rc.getURI() + " : " + bdsl.size() + " refs.");

            //String concept = dct + "BibliographicResource";
            //Resource rcConcept = mod.createResource(concept);
            for (BibDataSet bds : bdsl) {
                BiblioItem bd = bds.getResBib();
                if (bd==null) {
                    continue;
                }
                //log(bd.toString());
                String raw = TextUtil.cleanUTF(bds.getRawBib());
                String title = bd.getTitle();
                         //+ bd.getYear()+"-"+bd.getMonth()+"-"+bd.getDay();
                if (title==null) {
                    continue;
                }

                String uri = bd.getURI();
                String url = bd.getURL();
                if (uri==null) {
                     uri = getUri(raw, title);
                }
                if (url==null) {
                     url = uri;
                }
                //Resource ref = mod.createResource(uri, rcConcept);
                Resource ref = mod.createResource(uri, DCTerms.BibliographicResource);
                ref = inject(ref, mod, "bibliographicCitation", raw);
                ref = inject(ref, mod, "title", title);
                if (uri.startsWith("http://localhost")) {
                } else {
                    ref = inject(ref, mod, "hasURL", url);
                }
                //if (bd.getAuthors()!=null) {
                //  String aut = bd.getAuthors().replaceAll("\\s+", " ").trim();
                //    ref = inject(ref, mod, "creator", aut);
                //}
                List<String> authors = new ArrayList<String>();
                if (bd.getFullAuthors()!=null) {
                    int index = 1;
                    for(Person prs: bd.getFullAuthors()) {
                       authors.add(prs.getLastName()+", "+prs.getFirstName());
                    }
                    if (authors.size()>0) {
                        ref = injectAuthors(mod, ref, 
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
                    ref = inject(ref, mod, "date", issued);
                }
                seq.add(ref);
                found++;
            }
        }
        if (found>threshold && !test) {
            log("added " + found + " references [" + threshold + "]");
            rc.addProperty(DCTerms.references, seq);
            org.add(mod);
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
        e.printStackTrace();
        logger.severe(e.toString());
    }

    protected void log(String msg) {
        logger.info(msg);
    }

}
