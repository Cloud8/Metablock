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
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import com.hp.hpl.jena.rdf.model.Model;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A XML Transformer 
   @date 2013-01-22
*/
public class XMLTransformer {

    private static final Logger logger =
                         Logger.getLogger(XMLTransformer.class.getName());

    Transformer transformer;
    private StringWriter stringWriter;

    public XMLTransformer() {
        stringWriter = new StringWriter();
    }

    public XMLTransformer(String xslt) {
        this();
        createTransformer(xslt);
    }

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

    public void create() {
    }

    public void dispose() {
        try { stringWriter.close(); }
        catch(IOException e) {log(e);}
    }

    //dirty
    public String transform( Model mod ) {
        return transform( asString(mod) );
    }

    public String transform( Document doc ) {
		transformer.reset();
	    String result = null;
	    try {
		  StringWriter writer = new StringWriter();
		  transformer.transform( new DOMSource(doc), 
			                     new StreamResult(writer));
          result = writer.toString();
        } catch(TransformerException e) { log(e); }
          finally {
          return result;
        }
    }

    public String transform( String xmlString ) {
		transformer.reset();
	    String result = null;
	    try {
          StringReader reader = new StringReader(xmlString);
		  StringWriter writer = new StringWriter();
		  transformer.transform( new StreamSource(reader), 
			                     new StreamResult(writer));
          result = writer.toString();
        } catch(TransformerException e) { log(e); }
          finally {
          return result;
        }
    }

    /** Add a parameter for the transformation */
    public void setParameter(String name, String value) {
        transformer.setParameter(name, value);
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

    private void createTransformer(String text) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            InputStream in = new ByteArrayInputStream(text.getBytes("UTF-8"));
            if (in==null) {
                log ("xslt dirty.");
                return;
            }
            Source xslt = new StreamSource(in);
            transformer = factory.newTransformer(xslt);
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            if (in!=null) in.close();
        } catch(TransformerConfigurationException e) { log(e); }
          catch(IOException e) { log(e); }
    }

}
