package org.shanghai.ojs;

import org.shanghai.rdf.Config;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.data.FileStorage;
import org.shanghai.ojs.URN;
import org.shanghai.util.FileUtil;
import org.shanghai.util.ModelUtil;

import ORG.oclc.oai.harvester2.verb.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.FileAlreadyExistsException;

import java.text.Normalizer;

import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLSession;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
  * @title OAI Transporter
  * @date 2015-11-21
  * @author Krystoff Nieszczęście
  */
public class OAITransporter implements MetaCrawl.Transporter {

    private Config.OAI settings; 
    private DocumentBuilderFactory docfactory;
    private static TransformerFactory factory;

    private XMLTransformer transformer;
    private URN urn;

    private boolean dump = false;
    private int count=0;
    private String from;
    private String until;
    private String testFile;

    public OAITransporter(Config.OAI settings) {
        this.settings = settings;
        urn = new URN(settings.urn_prefix);
        this.dump = settings.archive!=null;
    }

    @Override
    public void create() {
        docfactory = DocumentBuilderFactory.newInstance();
        docfactory.setNamespaceAware(true);
        factory = TransformerFactory.newInstance();

        if (settings.from.contains("T") && settings.from.contains("T")) {
            from = settings.from;
        } else {
            from = settings.from   + "T00:00:00Z";
        }
        if (settings.until.contains("T") && settings.until.contains("T")) {
            until = settings.until;
        } else {
            until = settings.until + "T23:59:59Z";
        }
        if (settings.transformer!=null) {
            String xslt = FileUtil.readResource(settings.transformer); 
            transformer = new XMLTransformer(xslt);
            transformer.create();
        }
        testFile = settings.test;
        urn.create();
        //log("harvest " + settings.harvest + " : " + from + " : " + until 
        //    + " : " + settings.prefix + " # " + settings.set);
    }

    @Override
    public void dispose() {
        transformer.dispose();
        urn.dispose();
    }

    @Override
    public String probe() {
        String result = "probe failed.";
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
    public List<String> getIdentifiers(int off, int limit) {
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
            //log("[" + listIdentifiers.getDocument().toString() + "]");
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
                //log("found " + nodes.getLength() + " identifiers " + limit);
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
                return Arrays.asList(result2);
            }
            return Arrays.asList(result);
        }
    }

    @Override 
    public int index(String source) {
        return 0;
    }

    @Override 
    public Resource test(String resource) {
        String raw = getRecord(resource);
        if (raw==null) {
            return null;
        }
        String xml = getMetadata(raw);
        if (xml==null) {
            log("test : no data for " + resource);
            FileUtil.write(testFile, raw);
            return null;
        } else {
            FileUtil.write(testFile, xml);
        }

        Resource rc = null;
        if ("rdf".equals(settings.prefix)) {
            rc = transformer.asResource(xml);
        } else {
            rc = transformer.transform(xml);    
        }
        // make DCTerms.identifier
        rc = NLMScanner.analyze(rc, urn);
        return rc;
    }

    @Override
    public Resource read(String identifier) {
        //log("read " + identifier);
        String raw = getRecord(identifier);
        if (raw==null) {
            log("empty result : " + identifier);
            return null;
        }
        String xml = getMetadata(raw);
        Resource rc = null;
        if (xml==null) {
		    return rc;
        }
        if ("rdf".equals(settings.prefix)) {
            rc = transformer.asResource(xml);
        } else {
            rc = transformer.transform(xml);    
        }
        // make DCTerms.identifier
        rc = NLMScanner.analyze(rc, urn);
        if (dump) { // write xml file
            dump(rc, xml);
        }
        return rc;
    }

    private String getRecord(String identifier) {
        String result = null;
        try {
            GetRecord record = new GetRecord(
                               settings.harvest, identifier, settings.prefix);
            result = new String(record.toString().getBytes("UTF-8"));
            result = Normalizer.normalize(result, Normalizer.Form.NFC);
        } catch (MalformedURLException e) { 
                log(identifier + " " + e.toString()); 
        } catch (IOException e) { 
            if (e.toString().contains("403")) {
                log(identifier + " " + e.toString()); 
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
        return XMLTransformer.asString(meta);
    }

    private void dump(Resource rc, String xml) {
        String date = rc.getProperty(DCTerms.issued).getString();
        String suffix = ".xml";
        if ("nlm".equals(settings.prefix)) {
            suffix = ".nlm";
        }
        Path path = FileStorage.getPath(settings.archive, rc, suffix);
        FileUtil.mkdir(path.getParent());
        FileUtil.write(path, xml);
        FileUtil.touch(path, date);
    }

    static {
        disableSslVerification();
    }

    private static void disableSslVerification() {
        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                        X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                        X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
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

}
