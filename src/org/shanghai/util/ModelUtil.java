package org.shanghai.util;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import java.util.logging.Logger;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Create nice prefixed Model 
    @date 2015-05-12
*/
public final class ModelUtil {


    public static Model createModel() {
        Model model = ModelFactory.createDefaultModel();
        return prefix(model);
    }

    public static Model prefix(Model model) {
        if (model==null) return model;
        for(int i=0; i<9; i++) model.removeNsPrefix("ns"+i);
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("dcterms", "http://purl.org/dc/terms/");
        model.setNsPrefix("dctypes", "http://purl.org/dc/dcmitype/");
        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        model.setNsPrefix("fabio", "http://purl.org/spar/fabio/");
        model.setNsPrefix("aiiso", "http://purl.org/vocab/aiiso/schema#");
        //model.setNsPrefix("pro", "http://purl.org/spar/pro/");
        model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        model.setNsPrefix("void", "http://rdfs.org/ns/void#");
        model.setNsPrefix("c4o", "http://purl.org/spar/c4o/");
        //model.setNsPrefix("swc", "http://data.semanticweb.org/ns/swc/ontology#");
        //model.setNsPrefix("swrc", "http://swrc.ontoware.org/ontology#");
        //model.setNsPrefix("sioc", "http://rdfs.org/sioc/ns#");
        return model;
    }

    /*
    private Model toModel( Document doc ) {
        Model model = ModelFactory.createDefaultModel();
        RDFReader reader = new JenaReader();
        try {
            Transformer transformer = templates.newTransformer();
            for (String name : params.keySet()) {
                transformer.setParameter(name, params.get(name));
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Result result = new StreamResult(baos);
            transformer.transform( new DOMSource(doc), result);
            InputStream in = new ByteArrayInputStream(baos.toByteArray());
            reader.read(model, in, null);
            baos.close();
        } catch(UnsupportedEncodingException e) {
            log(e);
        } catch(IOException e) {
            log(e);
        } finally {
            return model;
        }
    }
    */

    /*
    private String toString(Model model, String resource) {
        //log("toString " + resource);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stringWriter.getBuffer().setLength(0);
        model = ModelUtil.prefix(model);
        try {
           RDFWriter writer = model.getWriter("RDF/XML-ABBREV");
           writer.write(model, baos, null);
           //faster
           writer.setProperty("allowBadURIs","true");
           //writer.setProperty("relativeURIs","");
           writer.setProperty("tab","1");
           //writer.setProperty("blockRules","sectionReification");
           writer.setProperty("xmlbase", resource);
           //writer.setProperty("prettyTypes",
           //new Resource[] { model.createResource(fabio+":PeriodicalIssue")});
           //default writer does not sort topological
           //model.write(stringWriter, "RDF/XML-ABBREV");
           //only writes rdf description: bad logic.
           //model.write(stringWriter, "RDF/XML");
        } catch(Exception e) {
           //model.write(System.out,"RDF/XML-ABBREV");
           e.printStackTrace();
        } finally {
           String result = null;
           try {
               result = baos.toString("UTF-8");
           } catch(UnsupportedEncodingException e) { log(e); }
           return result;
        }
    }
    */

    /*
    private String format(String xml) {
        final Document document = asDocument(xml);
        document.getDocumentElement().normalize();
        try {
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            tf.setOutputProperty(OutputKeys.METHOD, "xml");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tf.setOutputProperty(
                        "{http://xml.apache.org/xslt}indent-amount", "2");
            Writer out = new StringWriter();
            tf.transform(new DOMSource(document), new StreamResult(out));
            return out.toString();
            //String result = out.toString();
            //System.out.println(result);
            //return result;
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
    */

    /*
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
    */

    private static final Logger log = Logger.getLogger(ModelUtil.class.getName());

    private static void log(String msg) {
        log.info(msg);
    }

    private static void log(Exception e) {
        //e.printStackTrace();
        log(e.toString());
    }

}
