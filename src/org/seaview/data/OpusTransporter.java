package org.seaview.data;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;
import org.shanghai.util.PrefixModel;
import org.shanghai.rdf.XMLTransformer;
import org.seaview.data.Database;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;

import java.io.InputStream;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Never Mind
    @title Simple Database Transporter
    @date 2013-07-07
*/
public class OpusTransporter implements MetaCrawl.Transporter {

    protected DBTransporter opus;
    protected DBTransporter series;
    private int count;
    private int mark;
	private boolean switched;
	private boolean test;
	private int seriesOff;
    private List<String> results = new ArrayList<String>();
    private XMLTransformer transformer;
    private String docbase;

    public OpusTransporter(String host, String db, String user, String pass,
                           String idxFile, String dumpFile, 
                           String idxSeries, String dumpSeries, String xslt) {
        opus = new DBTransporter(host, db, user, pass, idxFile, dumpFile, xslt);
        series = new DBTransporter(host, db, user, pass, idxSeries, dumpSeries, xslt);
    }

    public OpusTransporter(String host, String db, String user, String pass,
        String idx, String dump, String xslt, String base, int days, boolean t) 
    {
        opus = new DBTransporter(host, db, user, pass, idx, dump, xslt, days,t);
        series = null;
        docbase = base;
        this.test = t;
    }

    @Override
    public void create() {
        count = 0;
        mark = 0;
		switched = false;
		transformer = null;
        results = new ArrayList<String>();
        opus.create();
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
    public String probe() {
        if (series==null) return opus.probe();
        return opus.probe() + " # " + series.probe();
    }

    @Override
    public Model read(String oid) {
	    count++;
        if (switched && count < mark) {
            return opus.read(oid);
        } else if (switched) {
            return series.read(oid);
        } else if (test) {
            return test(oid);
        } else {
            return opus.read(oid);
        }
    }

    @Override
    public String[] getIdentifiers(int off, int limit) {
        results.clear();
	    count = 0;
        if (switched) {
		    mark = 0;
            results.addAll(series.identifiers(off - seriesOff, limit));
            if (results.size() < limit) {
                results.add((String)null);
            }
        } else {
            results.addAll(opus.identifiers(off, limit));
            //log("identifiers: " + results.size());
            if (results.size() < limit && series!=null) {
			    switched = true;
                mark = results.size();
                log("switch to series " + mark);
                results.addAll(series.identifiers(0, limit));
                seriesOff = off;
            }
        }
        return results.toArray(new String[results.size()]);
    }

    @Override
    public int crawl(String str) {
        log("crawl " + str);
        return series.crawl(str);
    }

    @Override
    public Model test(String resource) {
        //log("test mode " + resource);
        if (series!=null && count==1) {
            return series.test(resource);
        } else {
            Model model = opus.test(resource);
            Document doc = opus.document;
            if (transformer==null) {
                transformer = new XMLTransformer();
				transformer.create();
		    }
            if (docbase==null) {
                FileUtil.write("opus-" + resource + ".xml", 
                    transformer.asString(doc));
            } else {
                NodeList nodes = doc.getElementsByTagName("field");
                for(int x=0,size=nodes.getLength(); x<size; x++) {
			        String value = nodes.item(x).getAttributes()
				                    .getNamedItem("name").getNodeValue();
		 	        if (value.equals("uri")) {
					    String uri = nodes.item(x).getTextContent();
					    String target = docbase 
                                      + uri.substring(uri.indexOf("/",7)) 
						              + "/opus-" + resource + ".xml";
                        FileUtil.write(target, transformer.asString(doc));
				        log("wrote " + target);
					}
			    }
            }
            return model;
        }
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
