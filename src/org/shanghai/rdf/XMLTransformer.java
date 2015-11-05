package org.shanghai.rdf;

import org.shanghai.util.ModelUtil;

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
import java.nio.charset.StandardCharsets;

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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFErrorHandler;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfxml.xmlinput.JenaReader;
import org.apache.jena.rdfxml.xmlinput.DOM2Model;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop 
   @title A XML Transformer 
   @date 2013-01-22
*/
public class XMLTransformer {

    private Templates templates;
    private StringWriter stringWriter;

    private String xslt = null; 
    private static SAXTransformerFactory factory; 
    private XMLReader xr;
    private Map<String,String> params;

    public XMLTransformer() {
        stringWriter = new StringWriter();
    }

    public XMLTransformer(String xslt) {
        this();
        this.xslt = xslt;
    }

    public void create() {
        factory = ((SAXTransformerFactory) TransformerFactory.newInstance());
        factory.setErrorListener(new MyErrorListener());
        params = new HashMap<String,String>();
        if (xslt==null) {
            return;
        }
        try {
            InputStream in = new ByteArrayInputStream(xslt.getBytes("UTF-8"));
            TemplatesHandler th = factory.newTemplatesHandler();
            xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(th);
            xr.setErrorHandler(new MyErrorHandler());
            SAXSource src = new SAXSource(xr, new InputSource(in));
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
        if (params!=null) {
            params.clear();
        }
    }

    public void setParameter(String name, String value) {
        params.put(name, value);
    }

    // public Resource transform(Resource rc) {
    //     Model model = rc.getModel();
    //     String data = asString(model);
    //     String rdf = _transform(data);
    //     model = asModel(rdf);
    //     return model.getResource(rc.getURI());
    // }

    //cheap transformer
    //public String transform( Model model ) {
    //    return _transform( asString(model) );
    //}

    //cheap transformer
    public String transform( Resource rc ) {
        return _transform( asString(rc) );
    }

    // public Resource transform(Document doc, String uri) {
    //     Document rdf = _transformDoc(doc);
    //     Model model = ModelUtil.createModel();
    //     try {
    //         DOM2Model.createD2M(uri, model).load(rdf);
    //     } catch( SAXParseException e ) { log(e); }
    //     return model.getResource(uri);
    // }

    // public Resource transform(Document doc, String uri) {
    //     String rdf = _transform(doc);
    //     Model model = asModel(rdf);
    //     return model.getResource(uri);
    // }

    public Resource transform(Document doc) {
        String rdf = _transform(doc);
        return asResource(rdf);
    }

    public Resource transform(String xml) {
        String rdf = _transform(xml);
        return asResource(rdf);
        //if (rdf==null) {
        //    log("transformed to zero.");
        //}
        //Resource rc = asResource(rdf);
        //if (rc==null) {
        //    log("[" + rdf + "]");
        //    log("resource is zero.");
        //}
        //return rc;
    }

    public Resource asResource(String rdf) {
        int x = rdf.indexOf("rdf:about") + 11;
        int y = rdf.indexOf("\"", x);
        if (x<0 || y<0 || y<x) {
            return null;
        }
        String uri = rdf.substring(x, y);
        //log("about [" + uri + "]");
        Model model = asModel(rdf);
        return model.getResource(uri);
    }

    private String _transform( Document doc ) {
        String rdf = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Transformer transformer = templates.newTransformer();
            for (String name : params.keySet()) {
                transformer.setParameter(name, params.get(name));
            }
            Result result = new StreamResult(baos);
            transformer.transform( new DOMSource(doc), result);
            rdf = new String( baos.toByteArray(), StandardCharsets.UTF_8 );
        } finally {
            return rdf;
        }
    }

    // private Document _transformDoc( Document doc ) {
    //     Document out = null;
    //     try {
    //         Transformer transformer = templates.newTransformer();
    //         for (String name : params.keySet()) {
    //             transformer.setParameter(name, params.get(name));
    //         }
    //         DOMResult result = new DOMResult();
    //         transformer.transform( new DOMSource(doc), result);
    //         out = (Document) result.getNode();
    //     } finally {
    //         return out;
    //     }
    // }

    private String _transform(String xml) {
	    String result = null;
	    try {
            StringReader reader = new StringReader(xml);
		    StringWriter writer = new StringWriter();
            Transformer tr= templates.newTransformer();
            for (String name : params.keySet()) {
                tr.setParameter(name, params.get(name));
            }
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            SAXSource src = new SAXSource(new InputSource(reader));
            src.setXMLReader(xr);
		    tr.transform( src, new StreamResult(writer));
            result = writer.toString();
        } catch(TransformerException e) { 
            log(e); 
            result = null;
        } finally {
            return result;
        }
    }

    /* OpusAnalyzer */
    public String asString(Resource rc) {
        stringWriter.getBuffer().setLength(0);
        Model model = ModelUtil.prefix(rc.getModel());
        try {
            model.write(stringWriter, "RDF/XML-ABBREV");
        } catch(Exception e) {
            model.write(System.out,"RDF/XML-ABBREV");
            e.printStackTrace();
        } finally {
            return stringWriter.toString();
        }
    }

    private static Model asModel(Document doc) {
        Model m = ModelUtil.createModel();
        RDFReader reader = new JenaReader();
        String rdf = asString(doc);
        //reader.setErrorHandler(new MyRDFErrorHandler(rdf));
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

    private static Model asModel(String rdf) {
        Model m = ModelUtil.createModel();
        RDFReader reader = new JenaReader();
        //reader.setErrorHandler(new MyRDFErrorHandler(rdf));
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

    /* OAITransporter */
    public static String asString(Document doc) {
        String text = null;
        try {
            DOMSource domSource = new DOMSource(doc);
            Transformer tr= factory.newTransformer();
            //for (String name : params.keySet()) {
            //    tr.setParameter(name, params.get(name));
            //}
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

    class MyErrorListener implements ErrorListener {
        public void warning(TransformerException e)
                throws TransformerException {
            show("Warning from Listener ",e);
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
            show("Warning from ErrorHandler ",e);
        }
        public void error(SAXParseException e)
                throws SAXParseException {
            show("Error",e);
            //throw(e);
        }
        public void fatalError(SAXParseException e)
                throws SAXParseException {
            show("Fatal Error",e);
            //throw(e);
        }
        private void show(String type, SAXParseException e) {
            log(type + ":: [" + e.getMessage() + "]");
            //e.printStackTrace();
        }
    }

    static class MyRDFErrorHandler implements RDFErrorHandler {
        public MyRDFErrorHandler(String rdf) {
            super();
            int x = rdf.indexOf("rdf:about");
                x = x>0?x:0;
            int y = rdf.length()>x+100?x+100:rdf.length();
            //log("x " + x + " y " + y); log(rdf);
            resource = rdf.substring(x, y);
        }

        static String resource;
        public void warning(Exception e) {
            show("Warning from RDFErrorHandler ",e);
        }
        public void error(Exception e) {
            show("Error", e);
        }
        public void fatalError(Exception e) {
            show("Fatal", e);
        }
        private void show(String type, Exception e) {
            log(type + ": [" + resource + "]" + e.getMessage());
            //e.printStackTrace();
        }
    }

    private static final Logger logger =
                         Logger.getLogger(XMLTransformer.class.getName());

    private static void log(String msg) {
        logger.info(msg);    
    }

    private static void log(Exception e) {
        //e.printStackTrace(System.out);
        logger.log(Level.SEVERE, e.toString());
    }

}
