package org.seaview.opus;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.data.Database;
import org.shanghai.data.DBTransporter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.riot.system.IRIResolver;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.net.URLEncoder;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @title Opus Database Transporter
    @date 2013-07-07
*/
public class OpusTransporter implements MetaCrawl.Transporter {

    protected DBTransporter opus;
    protected DBTransporter series = null;
    private int count;
    private int mark;
	private boolean switched;
	private boolean dump;
	private int seriesOff;
    private List<String> results = new ArrayList<String>();
    private XMLTransformer transformer;
    private String docbase;
    private boolean refs;

    public OpusTransporter(String[] db, String[] idx, String docbase, 
           int days, String idxSeries, String dumpSeries, boolean dump) 
    {
        //log(idxSeries + " " + dumpSeries + " " + days + " " + dump);
        this.docbase = docbase;
        this.dump = dump;
        opus = new DBTransporter(db, idx, days);
        if (idxSeries !=null && dumpSeries!=null /* && days==0 */ ) {
            String xslt = idx[2];
            String[] idxs = {idxSeries, dumpSeries, xslt};
            series = new DBTransporter(db, idxs, 0);
        }
    }

    @Override
    public void create() {
        count = 0;
        mark = 0;
		switched = false;
		transformer = null;
        results = new ArrayList<String>();
        opus.create();
        String test = opus.getSingleText("show tables like 'sv_references'");
        refs = (test!=null);
        if (series!=null) {
            series.create();
        }
    }

    @Override
    public void dispose() {
        opus.dispose();
        if (series!=null) {
            series.dispose();
        }
        results.clear();
		switched = false;
    }

    @Override
    public int index(String search) {
        if (Files.isRegularFile(Paths.get(search))) {
            String index = FileUtil.read(search);
            if (index.contains("select")) { //not mistaken
                series = null;
                opus.index = index;
                log("index # " + search);
                return 1;
            }
        } else if (search.matches("[0-9]+")) {
            opus.index = "select " + search;
            series = null;
            return 1;
        } else if (search.matches("[0-9\\-]+")) {
            opus.index = "select source_opus, date from statistics"
                + " where uri is not null and date> '" + search + "'";
            series = null;
            log("search date # [" + search + "]");
            return 1;
        } else if (search.matches("[0-9a-zA-Z]+")) {
            opus.index = "select source_opus from opus"
                + " where title like '%" + search.replace("[\\s]+","%") + "%'";
            series = null;
            log("search title # [" + search + "]");
            return 1;
        } else if (search.startsWith("urn:")) {
            opus.index = "select source_opus from statistics"
                + " where urn like '" + search + "%'";
            series = null;
            log("search urn # [" + search + "]");
            return 1;
        }
        return 0;
    }

    @Override
    public String probe() {
        if (series==null) return opus.probe();
        return opus.probe() + " # " + series.probe();
    }

    @Override
    public Resource read(String oid) {
	    count++;
        Resource rc = null;
        if (switched && count <= mark) {
            rc = readOpus(oid);
            // log(oid + " " + rc.getURI());
        } else if (switched) {
            rc = series.read(oid);
            if (rc==null) {
                log("zero series " + oid);
            } else if (dump) {
                dump(series.document, rc.getURI(), "c"+oid);
            } else {
                // log(rc.getURI());
            }
        } else {
            rc =  readOpus(oid);
        }
        return rc;
    }

    private Resource readOpus(String oid) {
        Resource rc = opus.read(oid);
        if (rc==null) {
            return rc;
        }
        addReferences(rc, oid);
        addCitations(rc, oid);
        if (dump && opus.document!=null) {
            dump(opus.document, rc.getURI(), oid);
        }
        return rc;
    }

    /* -- slower than extract uri from text.
    private Resource readOpusWithURI(String oid) {
        Resource rc = null;
        String uri = opus.getSingleText(
                     "select uri from statistics where source_opus=" + oid);
        if (uri!=null) {
            rc = opus.read(oid, uri);
            addReferences(rc, oid);
        }
        if (dump) {
            dump(opus.document, rc.getURI(), oid);
        }
        return rc;
    }
    */

    @Override
    public List<String> getIdentifiers(int off, int limit) {
        results.clear();
	    count = 0;
        if (switched) {
		    mark = 0;
            results.addAll(series.getIdentifiers(off - seriesOff, limit));
        } else {
            results.addAll(opus.getIdentifiers(off, limit));
            if (results.size() < limit && series!=null) {
			    switched = true;
                mark = results.size();
                results.remove(mark-1); // remove zero marker
                results.addAll(series.getIdentifiers(0, limit));
                log("switch to series " + mark + " # " + results.size());
                seriesOff = off;
            }
        }
        if (results.size()==0) {
            log("getIdentifiers : " + off + " " + limit);
        } else if (results.size()==1) {
            log("getIdentifiers " + off + " " + limit + " " + results.size());
            results.add((String)null); // stop crawler
        } else if (results.size() < limit) {
            log("getIdentifiers " + off + " " + limit + " " + results.size());
            results.add((String)null); // stop crawler
        }
        return results;
    }

    @Override
    public Resource test(String oid) {
        if (oid.startsWith("c")) {
            oid = oid.substring(1);
            log("series test mode " + oid);
            Resource rc = series.test(oid);
            if (docbase!=null) {
                dump(series.document, rc.getURI(), "c"+oid);
            }
            return rc;
        } else {
            log("opus test mode " + oid);
            Resource rc = opus.test(oid);
            if (rc==null) {
                dump(opus.document, null, oid);
                throw new AssertionError("zero resource");
            }
            addReferences(rc, oid);
            addCitations(rc, oid);
            if (docbase!=null) {
                dump(opus.document, rc.getURI(), oid);
                log("write " + oid);
            }
            return rc;
        }
    }

    private void dump(Document doc, String uri, String oid) {
        if (doc==null) {
            return;
        }
        if (transformer==null) {
            transformer = new XMLTransformer();
			transformer.create();
		}
        if (docbase==null) {
            //FileUtil.write("opus-" + oid + ".xml", transformer.asString(doc));
        } else if (uri==null) {
            FileUtil.write("data/opus-"+oid+".xml", transformer.asString(doc));
        } else if (uri.indexOf("/",7)<0) {
            log("No uri for " + oid + " [" + uri + "]");
        } else if (uri.startsWith("file://")) {
            Path path = Paths.get(uri.substring(7)).resolve("opus-"+oid+".xml");
            boolean b = FileUtil.write(path, transformer.asString(doc));
        } else if (Files.isDirectory(Paths.get(docbase))) {
            Path path = Paths.get(docbase + uri.substring(uri.indexOf("/",7)));
            FileUtil.mkdir(path);
            Path out = path.resolve("opus-" + oid + ".xml");
            if (Files.isWritable(path)) {
                boolean b = FileUtil.write(out, transformer.asString(doc));
                //if (b) log("wrote " + out.toString());
            }
        }
    }

    private void addReferences(Resource rc, String oid) {
        if (!refs) {
            return; // table does not exist
        }
        ResultSet rs = opus.database.getResult("select uri, identifier, title,"
                     + " date, cite, authors, flag "
                     + " from sv_references where source_opus=" + oid);
        try {
            if (rs.isBeforeFirst() ) {
                Seq seq = rc.getModel().createSeq(rc.getURI() + "/References");
                while (rs.next()) {
                    String uri = rs.getString(1).trim();
                    if (uri.startsWith( // truth maintenance
                        rc.getURI().substring(0, rc.getURI().indexOf("/",8)))) {
                        seq.add(rc.getModel().createResource(uri));
                    } else {
                        if (uri.contains("[")) {
                            log("skip " + uri);
                            continue;
                        }
                        //if (!uri.startsWith("http://")) {
                        //    log("skip " + uri);
                        //    continue;
                        //}
                        //try (IRIResolver.validateIRI(uri)) {
                        //    continue;
                        //} catch(IRIException e) {
                        //    log("bad iri " + uri);
                        //}
                        Resource ref = rc.getModel()
                            .createResource(uri, DCTerms.BibliographicResource);
                        //if (rs.getString(2)!=null) {
                        //    ref.addProperty(DCTerms.identifier, "ref:" + rs.getString(2));
                        //}
                        if (rs.getString(3)!=null) {
                            ref.addProperty(DCTerms.title, rs.getString(3));
                        }
                        if (rs.getString(4)!=null) {
                            ref.addProperty(DCTerms.date, rs.getString(4));
                        }
                        if (rs.getString(5)!=null) {
                            ref.addProperty(DCTerms.bibliographicCitation, rs.getString(5));
                        }
                        if (rs.getString(6)!=null) {
                            String[] authors = rs.getString(6).split(" ; ");
                            //log(authors.toString());
                            ref = injectAuthors(ref, authors);
                        }
                        //if (rs.getString(7)!=null) { //GH2015
                        //    ref.addProperty(DCTerms.instructionalMethod, 
                        //                    "flag: " + rs.getString(7));
                        //}
                        seq.add(ref);
                    }
                }
                rc.addProperty(DCTerms.references, seq);
            }
        } catch(SQLException e) { log(e); }
    }

    private void addCitations(Resource rc, String oid) {
        if (!refs) {
            return; // table does not exist
        }
        ResultSet rs = opus.database.getResult("select isReferencedBy, title"
                       + " from sv_citations where source_opus=" + oid);
        try {
            if (rs.isBeforeFirst() ) {
                while (rs.next()) {
                      String uri = rs.getString(1);
                      rc.addProperty(DCTerms.isReferencedBy, 
                                     rc.getModel().createResource(uri));
                }
            }
        } catch(SQLException e) { log(e); }
    }

    private Resource injectAuthors(Resource rc, String[] authors) {
        if (authors.length==0 || rc.hasProperty(DCTerms.creator)) {
            return rc;
        }
        // Model model = ModelFactory.createDefaultModel();
        Seq seq = null;
        int index = 1;
        List<String> list = new ArrayList<String>();
        for (String aut : authors) {
            if (aut==null || aut.length()==0) continue;
            String uri = "http://localhost/aut/"
                       + aut.replaceAll("[^a-zA-Z0-9\\:\\.]","");
            if (list.contains(uri)) {
                //no duplicates
            } else {
                list.add(uri);
                Resource prs = rc.getModel().createResource(uri, FOAF.Person);
                prs.addProperty(FOAF.name, aut);
                if (seq==null) {
                    seq = rc.getModel().createSeq();
                }
                seq.add(index++, prs);
            }
        }
        if (seq!=null && seq.size()>0) {
            rc.addProperty(DCTerms.creator, seq);
        }
        return rc;
    }

    private static final Logger logger =
                         Logger.getLogger(OpusTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception ex) {
        logger.info(ex.toString());
        ex.printStackTrace();
    }
}
