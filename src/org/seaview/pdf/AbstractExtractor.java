package org.seaview.pdf;

import org.shanghai.util.TextUtil;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.sparql.vocabulary.FOAF;

import java.net.URL;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.net.MalformedURLException;

/*
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title AbstractExtractor for pdf files
    @date 2015-05-08
 */
public class AbstractExtractor {

    protected static final String fabio = "http://purl.org/spar/fabio/";

    protected boolean title;
    protected boolean refs;
    public boolean test = false;
    public int count;

    public AbstractExtractor(boolean title, boolean refs) {
        this.title = title;
        this.refs = refs;
    }

    public void dispose() {
    }

    public void create() {
        log("create metadata " + title + " references " + refs);
        count = 0;
    }

    public void create(String fname) {
        //log("scratch " + fname);
    }

    public void extractMetadata(Resource rc, String fname) {
        log("scratch " + fname + " metadata " + rc.getURI());
    }

    public void extractReferences(Resource rc, String fname, int threshold) {
        log("scratch " + fname + " references " + rc.getURI());
    }

    public String getTEI(String file) {
        return null;
    }

    /** reject a reference to limit false positives */
    protected boolean reject(String raw) {
        if (raw.replaceAll("\\s","").contains("....")) {
            return true;
        }
        if (raw.contains(" danke")) {
            return true;
        }
        return false;
    }

    protected String symbol(String symbol) {
        if (symbol==null) {
            // no identifier
        } else if (symbol.contains("Dank")) {
            return null;
        } else if (symbol.length()>12) {
            return symbol.substring(0,11);
        }
        return symbol;
    }

    public Resource injectAuthors(Resource rc, String[] authors) {
        if (authors.length==0 || rc.hasProperty(DCTerms.creator)) {
            return rc;
        }
        Seq seq = rc.getModel().createSeq();
		int index = 1;
        List<String> list = new ArrayList<String>();
        for (String aut : authors) {
            if (aut==null) continue;
            String uri = TextUtil.createURI(aut);
            if (list.contains(uri)) {
                //no duplicates
            } else {
                list.add(uri);
                Resource prs = rc.getModel().createResource(uri, FOAF.Person);
                prs.addProperty(FOAF.name, aut);
                seq.add(index++, prs);
            }
        }
		rc.addProperty(DCTerms.creator, seq);
        return rc;
    }

    /** inject property only, if it does not exist already */
    protected Resource inject(Resource rc, String term, String val) {
       if (val==null) {
           return rc;
       } else try {
           Property prop;
           if (term.startsWith("has")) {
               prop = rc.getModel().createProperty(fabio, term);
           } else {
               prop = rc.getModel().createProperty(DCTerms.NS, term);
           }
           if (!rc.hasProperty(prop)) {
               val = val.replaceAll("\\s+", " ");
               rc.addProperty(prop, val);
           }
       } finally {
           return rc;
       }
    }

    private String[] getAuthors(String raw) {
        String[] list;
        if (raw.contains(", ")) {
            list = raw.split(", ");
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

    protected ArrayList<String> pullLinks(String text) {
        ArrayList<String> links = new ArrayList<String>();
        text = text.replace("\n","").replace("\r","");

        //String regex = "\\(?\\b(https?://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
        String regex = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        while(m.find()) {
            String url = m.group();
            if (url.startsWith("(") && url.endsWith(")")) {
                url = url.substring(1, url.length() - 1);
            }
            if (url.endsWith(";")) {
                url = url.substring(0, url.length()-1);
            }
            if (url.endsWith("/")) {
                url = url.substring(0, url.length()-1);
            }
            if (url.endsWith("/)")) {
                url = url.substring(0, url.length()-2);
            }
            if (url.endsWith(".pdf") && url.contains("/pdf/")) {
                url = url.substring(0, url.indexOf("/pdf/"));
            }
            if (url.endsWith(")")) {
                url = url.substring(0, url.length()-1);
            }
            if (url.endsWith(";")) {
                url = url.substring(0, url.length()-1);
            }
            links.add(url);
        }
    return links;
    }

    protected String getArxivId(String raw) {
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

    /** no stinking parser */
    protected String getDoi(String raw) {
	    int x = raw.indexOf("doi:");
        x=x<0?raw.indexOf("DOI:"):x;
        int a=0;
        int b=0;
        for (int i=x; i<raw.length(); i++) {
            char ch = raw.charAt(i);
            if (a==0 && ch>='0' && ch<='9') {
			    a = i;
            }
			if (a>0 && b==0 && Character.isSpaceChar(ch)) {
			    b = i;
			}
        }
		if (b==0) {
		    b=raw.length();
		}
		if (a>0) {
		    String doi = raw.substring(a,b);
            if (doi.endsWith(".")) {
                doi = doi.substring(0,doi.length()-1);
            }
		    String url = "http://doi.org/" + doi;
			return url;
		}
		return null;
    }

    protected String getUri(String raw, String title) {
        String uri = null;
        if (raw.contains("http")) {
            List<String> links = pullLinks(raw.substring(raw.indexOf("http")));
            if (links.size()>0) {
                uri = links.get(0);
            }
        } else if (raw.contains("arXiv:")) {
            uri = getArxivId(raw);
        } else if (raw.contains("doi:") || raw.contains("DOI:")) {
            uri = getDoi(raw);
        }
        if (uri==null || !validate(uri)) {
            uri = TextUtil.createURI(title);
        } else {
            //log("uri: " + uri);
        }
        return uri;
    }

    protected boolean validate(String url) {
        boolean b = url.contains("."); // -- dont want http://doi
        b = url.endsWith("-")?false:b;
        if (b) try {
            final URI uri = new URL(url).toURI();
            if (b) {
                // b = IRIResolver.checkIRI(url); -- rejects everything
                IRIResolver.validateIRI(url);
            }
        } catch (MalformedURLException e) {
            b = false;
        } catch (URISyntaxException e) {
            b = false;
        } catch (Exception e) {
            log("iri rejected " + e.toString());
            b = false;
        } finally {
            if (!b) log("iri rejected " + url);
            //else log("iri accepted " + url);
        }
        return b;
    }

    private static final Logger logger =
                         Logger.getLogger(AbstractExtractor.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        logger.info(e.toString());
    }

}
