package org.shanghai.util;

import org.shanghai.util.ModelUtil;
import org.shanghai.util.FileUtil;

import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.SAXParser;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFErrorHandler;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfxml.xmlinput.JenaReader;
import org.apache.jena.rdfxml.xmlinput.DOM2Model;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @title Document Utilities
   @date 2016-05-18
*/
public class DocUtil {

    private static SAXTransformerFactory factory; 
    private static DocumentBuilderFactory dbf;

    public DocUtil create() {
        factory = ((SAXTransformerFactory) TransformerFactory.newInstance());
        dbf = DocumentBuilderFactory.newInstance();
        return this;
    }

    public void dispose() {
    }

    private Model asModel(Document document) {
        Model m = ModelUtil.createModel();
        RDFReader reader = new JenaReader();
        String rdf = asString(document);
        try {
            InputStream in = new ByteArrayInputStream(rdf.getBytes("UTF-8"));
            reader.read(m, in, null);
            in.close();
        } catch(UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        } catch(IOException e) {
            log(e);
        } finally {
            return m;
        }
    }

    public String asString(Document doc) {
        String text = null;
        try {
            DOMSource domSource = new DOMSource(doc);
            Transformer tr = factory.newTransformer();
			tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tr.setOutputProperty(
			            "{http://xml.apache.org/xslt}indent-amount", "2");
            java.io.StringWriter sw = new java.io.StringWriter();
            StreamResult sr = new StreamResult(sw);
            tr.transform(domSource, sr);
            text = sw.toString();
        } catch(javax.xml.transform.TransformerException e) { log(e); }
        return text;
    }

    public String asString(Resource rc) {
        StringWriter sw = new StringWriter();
        Model model = ModelUtil.prefix(rc.getModel());
        try {
            model.write(sw, "RDF/XML-ABBREV");
        } catch(Exception e) {
            model.write(System.out,"RDF/XML-ABBREV");
            e.printStackTrace();
        } finally {
            return sw.toString();
        }
    }

    public Document asDocument(Resource rc) {
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.newDocument();
            String xml = asString(rc);
            Source source = new StreamSource(new StringReader(xml));
            Result result = new DOMResult(document); 
            Transformer tr = factory.newTransformer();
            tr.transform(source, result);
            return document;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public Document append(Document parent, Resource rc ) {
        return append(parent, asDocument(rc));
    }

    public Document append(Document parent, Document child) {
        if (parent==null) {
            return child;
        }
        NodeList nodes = child.getDocumentElement().getChildNodes();
        for (int i=0; i<nodes.getLength(); i++) {
            Node node = parent.importNode(nodes.item(i), true);
            parent.getDocumentElement().appendChild(node);
        }
        return parent;
    }

    private static final Logger logger =
                         Logger.getLogger(DocUtil.class.getName());

    private static void log(String msg) {
        logger.info(msg);    
    }

    private static void log(Exception e) {
        //e.printStackTrace(System.out);
        logger.log(Level.SEVERE, e.toString());
    }

}
