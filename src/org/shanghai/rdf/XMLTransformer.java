package org.shanghai.rdf;

import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
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
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.SAXParser;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Result;
import javax.xml.stream.XMLStreamReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLReaderFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.util.SAXInputSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.rdf.model.RDFReader;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.arp.JenaReader;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop 
   @title A XML Transformer 
   @date 2013-01-22
*/
public class XMLTransformer {

    private Templates templates;
    private StringWriter stringWriter;

    private String xslt; 
    //private static TransformerFactory factory;
    private static SAXTransformerFactory factory; 
    //private MyErrorHandler myErrorHandler;
    private XMLReader xr;

    public XMLTransformer(String xslt) {
        //if (xslt==null)
        //    log("bad xslt");
        this.xslt = xslt;
        stringWriter = new StringWriter();
    }

    public void create() {
        //factory = TransformerFactory.newInstance();
        //factory = ((SAXTransformerFactory) factory);
        factory = ((SAXTransformerFactory) TransformerFactory.newInstance());
        factory.setErrorListener(new MyErrorListener());
        try {
            InputStream in = new ByteArrayInputStream(xslt.getBytes("UTF-8"));
            //Source src = new StreamSource(in);
            //templates = factory.newTemplates(src);
            //TransformerHandler th = factory.newTransformerHandler();
            TemplatesHandler th = factory.newTemplatesHandler();
            xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(th);
            xr.setErrorHandler(new MyErrorHandler());
            //SAXSource src = new SAXSource(xr, new InputSource(in));
            SAXSource src = new SAXSource(xr, new InputSource(in));
            //factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            //following line throws warning
            templates = factory.newTemplates(src);
            if (in!=null) in.close();
        } catch(SAXNotRecognizedException e) { 
            log("catched: " + e.toString());
        } catch(SAXException e) { 
            log("create: " + e.toString());
        } catch(TransformerConfigurationException e) { 
            log("create: " + e); 
        } catch(IOException e) { 
            log("create: " + e);
        }
    }

    public void dispose() {
        try { stringWriter.close(); }
        catch(IOException e) {log(e);}
    }

    public String transform( Model mod ) {
        //cheap transformer
        return transform( asString(mod) );
    }

    public Model transform( Document doc ) {
        Model model = ModelFactory.createDefaultModel();
        RDFReader reader = new JenaReader();
        try {
            Transformer transformer = templates.newTransformer();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Result result = new StreamResult(bos);
            transformer.transform( new DOMSource(doc), result);
            //InputStream in = new ByteArrayInputStream(rdf.getBytes("UTF-8"));
            InputStream in = new ByteArrayInputStream(bos.toByteArray());
            reader.read(model, in, null);
            bos.close();
        } catch(UnsupportedEncodingException e) {
            log(e);
        } catch(IOException e) {
            log(e);
        } finally {
            return model;
        }
    }

    public String transform(String xmlString) {
	    String result = null;
	    try {
            StringReader reader = new StringReader(xmlString);
		    StringWriter writer = new StringWriter();
            Transformer transformer = templates.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            SAXSource src = new SAXSource(new InputSource(reader));
            src.setXMLReader(xr);
		    transformer.transform( src, // new StreamSource(reader), 
			                       new StreamResult(writer));
            result = writer.toString();
        } catch(TransformerException e) { 
            log(e); 
        } finally {
            return result;
        }
    }

    public String asString(Model model) {
        String result = null;
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        //model.setNsPrefix("npg", "http://ns.nature.com/terms/");
        model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        model.setNsPrefix("bibo", "http://purl.org/ontology/bibo/");
        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        model.setNsPrefix("fabio", "http://purl.org/spar/fabio/");
        model.setNsPrefix("pro", "http://purl.org/spar/pro/");
        model.setNsPrefix("aiiso", "http://purl.org/vocab/aiiso/schema#");
        model.setNsPrefix("dct", "http://purl.org/dc/terms/");
        model.setNsPrefix("prism", "http://prismstandard.org/namespaces/basic/2.0/");
        stringWriter.getBuffer().setLength(0);
        try {
           //does use ontological information:
           model.write(stringWriter, "RDF/XML-ABBREV");
           //only writes rdf description: bad logic.
           //model.write(stringWriter, "RDF/XML");
        } catch(Exception e) {
           //model.write(System.out,"TTL");
           model.write(System.out,"RDF/XML");
           e.printStackTrace();
        } finally {
           return stringWriter.toString();
        }
    }

    public static Model asModel(String rdf) {
        Model m = ModelFactory.createDefaultModel();
        RDFReader reader = new JenaReader();
        try {
            InputStream in = new ByteArrayInputStream(rdf.getBytes("UTF-8"));
            reader.read(m, in, null);
            in.close();
        } catch(UnsupportedEncodingException e) {
            log(e);
        } catch(IOException e) {
            log(e);
        } finally {
            return m;
        }
    }

    String asString(Document doc) {
        String text = null;
        try {
            DOMSource domSource = new DOMSource(doc);
            Transformer transformer = factory.newTransformer();
            //transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            //transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            //transformer.setOutputProperty
            //    ("{http://xml.apache.org/xslt}indent-amount", "4");
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            java.io.StringWriter sw = new java.io.StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(domSource, sr);
            text = sw.toString();
        } //catch(javax.xml.parsers.ParserConfigurationException e) { log(e); }
          catch(javax.xml.transform.TransformerException e) { log(e); }
        return text;
    }

    private Document asDocument(String xml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    class MyErrorListener implements ErrorListener {
        public void warning(TransformerException e)
                throws TransformerException {
            //show("Warning",e);
        }
        public void error(TransformerException e)
                throws TransformerException {
            show("Error",e);
            throw(e);
        }
        public void fatalError(TransformerException e)
                throws TransformerException {
            show("Fatal Error",e);
            throw(e);
        }
        private void show(String type,TransformerException e) {
            log(type + "## " + e.getMessage());
            if(e.getLocationAsString() != null)
                log(e.getLocationAsString());
            e.printStackTrace();
        }
    }

    class MyErrorHandler implements ErrorHandler {
        public void warning(SAXParseException e)
                throws SAXParseException {
            //show("Warning",e);
        }
        public void error(SAXParseException e)
                throws SAXParseException {
            show("Error",e);
            throw(e);
        }
        public void fatalError(SAXParseException e)
                throws SAXParseException {
            show("Fatal Error",e);
            e.printStackTrace(System.out);
            throw(e);
        }
        private void show(String type,SAXParseException e) {
            log(type + ":: [" + e.getMessage() + "]");
        }
    }

	private static boolean isEmpty(String s) {
	    return s==null || s.isEmpty();
	}

	private static boolean isURI(String s) {
	    return s.startsWith("http://");
	}

    private static final Logger logger =
                         Logger.getLogger(XMLTransformer.class.getName());

    private static void log(String msg) {
        logger.info("XMLTransformer :: " + msg);    
    }

    private static void log(Exception e) {
        logger.log(Level.SEVERE, e.toString());
        e.printStackTrace(System.out);
    }

}
