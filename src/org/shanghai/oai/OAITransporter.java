package org.shanghai.oai;

import org.shanghai.util.FileUtil;
import org.shanghai.rdf.Config;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.crawl.MetaCrawl;

import ORG.oclc.oai.harvester2.verb.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Model;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

import java.lang.NoSuchFieldException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/*
 see https://github.com/openpreserve/OAIHarvester2/blob/master/src/main/java/ORG/oclc/oai/harvester2/verb/HarvesterVerb.java
 */
public class OAITransporter implements MetaCrawl.Transporter {

    private Config.OAI settings; 
    private DocumentBuilderFactory docfactory;
    private static TransformerFactory factory;

    private XMLTransformer transformer;

    private boolean archive = false;
    private int count=0;
    private String from;
    private String until;

    public OAITransporter(Config.OAI settings, boolean archive) {
        this.settings = settings;
        this.archive = archive;
    }

    @Override
    public void create() {
        docfactory = DocumentBuilderFactory.newInstance();
        docfactory.setNamespaceAware(true);
        factory = TransformerFactory.newInstance();

        from = settings.from   + "T00:00:00Z";
        until = settings.until + "T23:59:59Z";
        if (settings.transformer!=null) {
            String xslt = FileUtil.read(settings.transformer); 
            if (xslt==null) {
                log("No transformer file " + settings.transformer);
            } else {
                transformer = new XMLTransformer( xslt );
                transformer.create();
                //if (settings.uri!=null) {
                //    transformer.setParameter("uri", settings.uri);
                //}
            }
        }
        log("harvest " + settings.harvest + " : " + from + " : " + until 
            + " : " + settings.prefix + " # " + settings.set);
    }

    @Override
    public void dispose() {
        if (transformer!=null) {
            transformer.dispose();
            transformer = null;
        }
    }

    @Override
    public String probe() {
        String result = "failed.";
        try {
            DocumentBuilder db = docfactory.newDocumentBuilder();
            result = new Identify(settings.harvest).toString();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(result));
            Document doc = db.parse(is);
            doc.getDocumentElement().normalize();
            result = doc.getElementsByTagName("repositoryName")
                        .item(0).getTextContent();
        } catch (IOException e) { log(e); }
        finally {
           return result;
        }
    }

    @Override
    public String[] getIdentifiers(int off, int limit) {
        String[] result = new String[limit];
        int found=0;
        int skip=0;
        try {
            DocumentBuilder db = docfactory.newDocumentBuilder();
            ListIdentifiers listIdentifiers = new ListIdentifiers(
                settings.harvest, from, until, settings.set, settings.prefix);
            //log("request " + listIdentifiers.getRequestURL());
            //listIdentifiers.harvest(req);
            //log("response " + listIdentifiers.toString());

            if (listIdentifiers.getDocument()==null) {
                log("no Identifiers");
                return null;
            }
          //log("response [" + listIdentifiers.getDocument().toString() + "]");
            //if (listIdentifiers.getDocument().getTextContent()==null) {
            //    log("empty response, no Identifiers");
            //    return null;
            //}

            while (listIdentifiers != null && found<limit) {
                String response = new String(listIdentifiers.toString()
                                             .getBytes("UTF-8"));
                //log("response [" + response + "]");
                String resumption = listIdentifiers.getResumptionToken();
                if (resumption == null || resumption.length() == 0) {
                    listIdentifiers = null;
                } else {
                    listIdentifiers = new ListIdentifiers(
                                          settings.harvest, resumption);
                }
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(response));
                Document doc = db.parse(is);
                doc.getDocumentElement().normalize();
                NodeList nodes = doc.getElementsByTagName("identifier");
                log("found " + nodes.getLength() + " identifiers " + limit);
                for (int i=0; i<nodes.getLength(); i++) {
                    if (skip<off) {
                        skip++;
                    } else {
                        String id = nodes.item(i).getTextContent();
                        //log("found " + found + " " + id);
                        if (found>=limit) {
                            break;
                        } else {
                            result[found++] = id;
                        }
                    }
                }
                //log(doc);
            }
            count += found;
        } catch (Exception e) { log(e); e.printStackTrace(); }
        finally {
            if (found<limit) {
                String[] result2 = new String[found];
                if (found>0) {
                    System.arraycopy(result,0,result2,0,found);
                } else {
                    //log("nothing found.");
                }
                return result2;
            }
            return result;
        }
    }

    @Override 
    public int crawl(String source) {
        //return getIdentifiers(0,99).length;
        return 0;
    }

    @Override
    public Model read(String identifier) {
        //log("read " + identifier);
        String raw = getRecord(identifier);
        if (raw==null) {
            log("empty result : " + identifier);
            return null;
        }
        String xml = getMetadata(raw);
        if (xml==null) {
            log("zero data : " + identifier);
            if (archive && count==0) {
                archive("raw-"+identifier, raw);
                count++;
            }
            return null;
        }
        if (archive) { // write xml pdf index.html
            archive(identifier, xml);
        }

        Model model = null;
        String rdf = null;
        if (transformer!=null) {
            rdf = transformer.transform(xml);
            model = transformer.asModel(rdf);
        } else if ("rdf".equals(settings.prefix)) {
            rdf = xml;
            model = XMLTransformer.asModel(xml);
        }
        //if (analyzer!=null) {
        //    analyzer.analyze(model, identifier);
        //}
        return model;
    }

    private String getRecord(String identifier) {
        String result = null;
        try {
            GetRecord record = new GetRecord(
                               settings.harvest, identifier, settings.prefix);
            result = new String(record.toString().getBytes("UTF-8"));
        } catch (IOException e) { 
            if (e.toString().contains("403")) {
                log(e.toString()); 
            } else {
                log(e); 
            }
        }
        finally {
           return result;
        }
    }

    /** throw away oai transport overhead and return record metadata only */
    private String getMetadata(String xml) {
        Document doc = null;
        DocumentBuilder db = null;
        try {
            db = docfactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            doc = db.parse(is);
        } catch(SAXException e) { log(e); }
          catch(ParserConfigurationException e) { log(e); }
          catch(IOException e) { log(e); }
        if (doc==null) {
            log("zero document");
            return null;
        }
        doc.getDocumentElement().normalize();

        Node metadata = doc.getElementsByTagName("metadata").item(0);
        if (metadata==null) {
            log("zero metadata");
            return null;
        }
        metadata.normalize();
        NodeList childs = metadata.getChildNodes();
        Element head = null;
        for (int i = 0; i < childs.getLength(); i++) {
            Node n = childs.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                head = (Element) n;
            }
        }
        Document meta = db.newDocument();
        Node root = meta.importNode(head, true);
        meta.appendChild(root);
        return toString(meta);
    }

    private String archive(String identifier, String xml) {
        String path = settings.archive;
        if ("nlm".equals(settings.prefix)) {
            writeData(path, xml);
        } else if (path!=null && new File(path).isDirectory()) {
            String outf = path + "/" + identifier.replaceAll("/",":") + ".xml";
            log("archive to " + outf);
            FileUtil.write(outf, xml);
        }
        return path;
    }

    private String writeData(String path, String xml) {
        if (path==null || ! new File(path).isDirectory()) {
            return null;
        }

        NLM nlm = new NLM(xml);
        //log(nlm.toString());
        if (nlm.year!=null)
            path += "/" + nlm.year;
        if (nlm.issueId!=null)
            path += "/" + nlm.issueId;
        if (nlm.articleId!=null)
            path += "/" + nlm.articleId;
        File check = new File(path);
        if (!check.exists())
            if (!new File(path).mkdirs())
                return path;
        if (nlm.url!=null) {
            FileUtil.copy(nlm.url, path + "/index.html");
            String from = nlm.url.replace("view", "viewFile"); // OJS
            //log("read [" + from + "]");
            FileUtil.copy(from, path + "/" + nlm.articleId + ".pdf");
        }
        if (nlm.articleId!=null) {
            FileUtil.write(path + "/" + nlm.articleId + ".nlm", xml);
            log("wrote " + path + "/" + nlm.articleId + ".nlm");
        } else {
            //log(nlm.toString());
            log("failed " + path + "/" + nlm.articleId + ".nlm");
        }
        return path;
    }

    public class NLM {
        String journal;
        String year;
        String issueId;
        String volume;
        String number;
        String articleId;
        String url;

        public NLM(String xml) {
            Document doc = null;
            DocumentBuilder db = null;
            try {
                db = docfactory.newDocumentBuilder();
                InputSource is = new InputSource(new StringReader(xml));
                doc = db.parse(is);
            } catch(SAXException e) { log(e); }
              catch(IOException e) { log(e); }
              catch(ParserConfigurationException e) { log(e); }
            NodeList nodes = doc.getElementsByTagName("journal-id");
            journal = nodes.getLength()==0?null:nodes.item(0).getTextContent();
            nodes = doc.getElementsByTagName("pub-date");
            for (int j = 0; j < nodes.getLength(); j++) {
                Element el = (Element)nodes.item(j);
				if (el.getAttribute("pub-type").equals("collection")) {
				    year = el.getElementsByTagName("year")
                             .item(0).getTextContent();
				}
            }
            //year = nodes.getLength()==0?null:nodes.item(0).getTextContent();
            nodes = doc.getElementsByTagName("volume");
            volume = nodes.getLength()==0?null:nodes.item(0).getTextContent();
            nodes = doc.getElementsByTagName("issue-id");
            issueId = nodes.getLength()==0?null:nodes.item(0).getTextContent();
            nodes = doc.getElementsByTagName("issue");
            number = nodes.getLength()==0?null:nodes.item(0).getTextContent();
            nodes = doc.getElementsByTagName("article-id");
            articleId=nodes.getLength()==0?null:nodes.item(0).getTextContent();
            if (articleId!=null) {
                nodes = doc.getElementsByTagName("self-uri");
                for (int j = 0; j < nodes.getLength(); j++) {
                    Element el = (Element)nodes.item(j);
                    if (el.hasAttribute("content-type")) {
                        String str = el.getAttribute("content-type");
                        if ( str.equals("application/pdf")) {
                             url = el.getAttribute("xlink:href");
                        }
                    }
                }
            }
        }

        public String toString() {
            return journal + " " + year + " " + volume + " " + issueId
                   + " " + articleId + " " + url;
        }
    }

    public void test() {
        log("test harvest " + settings.harvest); 
        log("prefix " + settings.prefix); 
        log("transformer " + settings.transformer); 
        log("from: "  + from);
        log("until: " + until);
        String[] ids = getIdentifiers(0,7);
        log("found " + ids.length + " identifiers");
        
        for (String s: ids) {
            log(s);
        }
    }

    public void show() {
        log("test harvest " + settings.harvest); 
        log("prefix " + settings.prefix); 
        log("transformer " + settings.transformer); 
        log("from " + settings.from); 
        log("until " + settings.until); 
        log("urnPrefix " + settings.urnPrefix); 
        log(" archive " + settings.archive); 
    }

    private static final Logger logger =
                         Logger.getLogger(OAITransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception e) {
        log(e.toString());
        try { throw(e); } catch(Exception ex) { ex.printStackTrace(); }
    }

    public static String toString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

}
