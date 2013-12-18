package org.shanghai.rdf;

import java.util.Iterator;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A XML Transformer 
   @date 2013-01-22
*/
public class XMLTransformer {

    private static final Logger logger =
                         Logger.getLogger(XMLTransformer.class.getName());

    private Templates templates;
    private StringWriter stringWriter;

    private String xslt; 
    private static TransformerFactory factory;

    public XMLTransformer(String xslt) {
        if (xslt==null)
            log("bad xslt");
        this.xslt = xslt;
    }

    private static void log(String msg) {
        logger.info(msg);    
    }

    private static void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    public void create() {
        stringWriter = new StringWriter();
        factory = TransformerFactory.newInstance();
        try {
            InputStream in = new ByteArrayInputStream(xslt.getBytes("UTF-8"));
            Source src = new StreamSource(in);
            templates = factory.newTemplates(src);
            //transformer.setOutputProperty("omit-xml-declaration", "yes");
            if (in!=null) in.close();
        } catch(TransformerConfigurationException e) { log(e); }
          catch(IOException e) { log(e); }
    }

    public void dispose() {
        try { stringWriter.close(); }
        catch(IOException e) {log(e);}
    }

    public String transform( Model mod ) {
        return transform( asString(mod) );
        //return transform( asDocument(mod) );
    }

    public String transform( Document doc ) {
	    String result = null;
	    try {
          Transformer transformer = templates.newTransformer();
		  StringWriter writer = new StringWriter();
		  transformer.transform( new DOMSource(doc), 
			                     new StreamResult(writer));
          result = writer.toString();
          //transformer.reset();
        } catch(TransformerException e) { log(e); }
          finally {
              return result;
        }
    }

    public String transform( String xmlString ) {
	    String result = null;
	    try {
            StringReader reader = new StringReader(xmlString);
		    StringWriter writer = new StringWriter();
            Transformer transformer = templates.newTransformer();
		    transformer.transform( new StreamSource(reader), 
			                     new StreamResult(writer));
            result = writer.toString();
            //transformer.reset();
        } catch(TransformerException e) { 
            result = null;
            log(e); 
        } finally {
            return result;
        }
    }

    String asString(Model model) {
        String result = null;
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("npg", "http://ns.nature.com/terms/");
        model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        model.setNsPrefix("bibo", "http://purl.org/ontology/bibo/");
        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        model.setNsPrefix("fabio", "http://purl.org/spar/fabio/");
        model.setNsPrefix("aiiso", "http://purl.org/vocab/aiiso/schema#");
        model.setNsPrefix("dct", "http://purl.org/dc/terms/");
        stringWriter.getBuffer().setLength(0);
        try {
           model.write(stringWriter, "RDF/XML-ABBREV");
        } catch(Exception e) {
           model.write(System.out,"TTL");
        } finally {
           return stringWriter.toString();
        }
    }

    String asString(Document doc) {
        String text = null;
        try {
            DOMSource domSource = new DOMSource(doc);
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            transformer.setOutputProperty
                ("{http://xml.apache.org/xslt}indent-amount", "4");
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            java.io.StringWriter sw = new java.io.StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(domSource, sr);
            text = sw.toString();
        } //catch(javax.xml.parsers.ParserConfigurationException e) { log(e); }
          catch(javax.xml.transform.TransformerException e) { log(e); }
        return text;
    }

    private Document asDocument(Model model) {
        String subject = null;
        String property = null;
        String object = null;

	    Document doc=null;	
		try {
		    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    DocumentBuilder db = dbf.newDocumentBuilder();
		    doc = db.newDocument();
		} catch(ParserConfigurationException pce) {
		    //throw new org.apache.xml.utils.WrappedRuntimeException(pce);
            log(pce);
		}	
		 
		Element root= doc.createElementNS(RDF.getURI(),"rdf:RDF");
		root.setAttributeNS("http://www.w3.org/2000/xmlns/", 
                            "xmlns:rdf",RDF.getURI());
		StmtIterator iter=model.listStatements(
		    isEmpty(subject)?null:ResourceFactory.createResource(subject),
		    isEmpty(property)?null:ResourceFactory.createProperty(property),
		    isEmpty(object)?null: (isURI(object)?
            ResourceFactory.createResource(object):model.createLiteral(object))
		);

		while(iter.hasNext()) {
		    Statement stmt= iter.nextStatement();
		    Element S=doc.createElementNS(RDF.getURI(),"rdf:Statement");
		    root.appendChild(S);
		    Element f=doc.createElementNS(RDF.getURI(),"rdf:subject");
		    S.appendChild(f);
		    f.setAttributeNS(RDF.getURI(),
                             "rdf:resource",stmt.getSubject().getURI());
		    f=doc.createElementNS(RDF.getURI(),"rdf:predicate");
		    S.appendChild(f);
		    f.setAttributeNS(RDF.getURI(),
                              "rdf:resource",stmt.getPredicate().getURI());
		    f=doc.createElementNS(RDF.getURI(),"rdf:object");
		    S.appendChild(f);
		    if(stmt.getObject().isLiteral()) {
		        f.appendChild(
                    doc.createTextNode(""+stmt.getLiteral().getValue()));
		    } else {
		        f.setAttributeNS(RDF.getURI(),
                   "rdf:resource",stmt.getResource().getURI());
		    }
		}
		iter.close();
		return doc;
	}

	private static boolean isEmpty(String s) {
	    return s==null || s.isEmpty();
	}

	private static boolean isURI(String s) {
	    return s.startsWith("http://");
	}

}
