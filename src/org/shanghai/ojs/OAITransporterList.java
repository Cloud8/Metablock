package org.shanghai.ojs;

import org.shanghai.rdf.Config;
import org.shanghai.ojs.OAITransporter;
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
    @author Never Mind
    @title OAI Multiplex Transporter 
    @date 2016-01-17
*/
public class OAITransporterList implements MetaCrawl.Transporter {

    private List<Config.OAI> config;
    private List<MetaCrawl.Transporter> oais;

    public OAITransporterList(List<Config.OAI> config) {
        this.config =  config;
    }

    @Override
    public void create() {
	    oais = new ArrayList<MetaCrawl.Transporter>();
        for (Config.OAI conf : config) {
		    MetaCrawl.Transporter oai = new OAITransporter(conf);
			oai.create();
		    oais.add(oai);
        }
    }

    @Override
    public void dispose() {
        for (MetaCrawl.Transporter oai : oais) {
		    oai.dispose();
        }
		oais.clear();
    }

    @Override
    public int index(String fname) {
        return 0;
    }

    @Override
    public String probe() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Resource read(String oid) {
        Resource rc = null;
        return rc;
    }

    @Override
    public List<String> getIdentifiers(int off, int limit) {
        return new ArrayList<String>();
    }

    @Override
    public Resource test(String oid) {
        return null;
    }

    private static final Logger logger =
                         Logger.getLogger(OAITransporterList.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception ex) {
        logger.info(ex.toString());
        ex.printStackTrace();
    }
}
