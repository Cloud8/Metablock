package org.seaview.pdf;

import org.seaview.data.AbstractAnalyzer;
import org.seaview.util.TextUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.rdf.model.Seq;
import org.apache.jena.riot.system.IRIResolver;

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

/*
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title AbstractExtractor for pdf files
    @date 2015-05-08
 */
public class AbstractExtractor {

    //protected static final int MONOSIZE = 33; // pages considered mono
    protected static final String dct = DCTerms.NS;
    protected static final String fabio = "http://purl.org/spar/fabio/";
    protected static final String foaf = "http://xmlns.com/foaf/0.1/";
    protected static final String about /* will become rdf:about */
                                = "http://localhost/";

    protected Property references;
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

    public void extractMetadata(Model model, Resource rc, String fname) {
        log("scratch " + fname + " metadata " + rc.getURI());
    }

    public void extractReferences(Model model, Resource rc, String fname, int threshold) {
        log("scratch " + fname + " references " + rc.getURI());
    }

    /** reject a reference to limit false positives */
    protected boolean reject(String raw) {
        if (raw.replaceAll("\\s","").contains("....")) {
            return true;
        }
        return false;
    }

    public Resource injectAuthors(Model mod, Resource rc, String[] authors) {
        Seq seq = mod.createSeq();
		//Resource prs = mod.createResource(
        Resource concept = mod.createResource(foaf + "Person");
		int index = 1;
        for (String aut : authors) {
            String uri = "http://localhost/aut/" 
                       + aut.replaceAll("[^a-zA-Z0-9\\:\\.]","");
            Resource prs = mod.createResource(uri, concept);
            prs.addProperty(mod.createProperty(foaf, "name"), aut);
            seq.add(index++, prs);
		    //seq.add(index++, aut);
        }
		Property creator = mod.createProperty(dct, "creator");
		rc.addProperty(creator, seq);
        return rc;
    }

    /** inject property only, if it does not exist already */
    protected Resource inject(Resource rc, Model mod, String term, String val) {
       if (val==null) {
           return rc;
       } else try {
           if (term.equals("creator")) {
               String[] authors = getAuthors(val);
               Property creator = mod.createProperty(dct, term);
               for (String str : authors) {
                   if (str.length()>2) {
                       rc.addProperty(creator, TextUtil.cleanUTF(str));
                   }
               }
               /**
               String foaf = "http://xmlns.com/foaf/0.1/";
               Resource concept = mod.createResource(foaf + "Person");
               String uri = rc.getURI();
               if (uri.contains("#")) {
                   uri = uri.substring(0, uri.indexOf("#"));
               }
               String uriStr = "http://localhost/aut/"
                             + val.replaceAll("[^a-zA-Z0-9\\:\\.]","");
               Seq seq = mod.createSeq(uriStr);
               for (String str : getAuthors(val)) {
                   str = str.replaceAll("[^a-zA-Z0-9\\:\\.]","");
                   if (str.length()>2) {
                       uri = "http://localhost/aut/" + str;
                       Resource prs = mod.createResource(uri, concept);
                       prs.addProperty(mod.createProperty(foaf, "name"), str);
                       seq.add(prs);
                   }
               }
               rc.addProperty(mod.createProperty(dct, term), seq);
               **/
           } else if (term.startsWith("has")) {
               Property prop = mod.createProperty(fabio,term);
               rc.addProperty(prop, val);
           } else {
               Property prop = mod.createProperty(dct,term);
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

    String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(text);
    while(m.find()) {
        String url = m.group();
        if (url.startsWith("(") && url.endsWith(")")) {
            url = url.substring(1, url.length() - 1);
        }
        if (url.contains("archiv.ub.unimarburg.de")) {
            url = url.replace("archiv.ub.unimarburg.de", 
                              "archiv.ub.uni-marburg.de");
        }
        if (url.contains("uni-marburg,de/diss/z2008/159")) {
            url = url.replace("uni-marburg,de/diss/z2008/159", 
                                    "uni-marburg.de/diss/z2008/0159");
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
			//if (a>0 && Character.isWhitespace(ch)) {
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
            //log("doi [" + doi + "]" + a + ":" + b);
            //log("raw [" + raw + "]" + raw.length() + "#" + x);
			//doi = doi.replaceAll("[^a-zA-Z0-9\\:\\.\\/]","");
		    String url = "http://doi.org/" + doi;
			return url;
		}
		return null;
    }

    protected String getUri(String raw, String title) {
        String uri = null;
        if (raw.contains("http://")) {
            List<String> links = pullLinks(raw);
            if (links.size()>0) {
                uri = links.get(0);
            }
        } else if (raw.contains("arXiv:")) {
            uri = getArxivId(raw);
        } else if (raw.contains("doi:") || raw.contains("DOI:")) {
            uri = getDoi(raw);
        }
        if (uri==null || !validate(uri)) {
            //try {
                uri = title.replaceAll("[^a-zA-Z0-9]","").toLowerCase();
                int x = uri.length()>30?30:uri.length();
                uri = about + uri.substring(0,x);
                //uri = about + URLEncoder.encode(title, "UTF-8");
                //log("article-title: " + title);
            //} catch(UnsupportedEncodingException e) { log(e); }
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
