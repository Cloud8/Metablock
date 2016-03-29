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

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.util.logging.Logger;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Never Mind
    @title OAI Multiplex Transporter 
    @date 2016-01-17
*/
public class OAITransporterList implements MetaCrawl.Transporter {

    private List<Config.OAI> config;
    private List<MetaCrawl.Transporter> oais;
    private int index;
    private int mark;
    private int count;
    private String urn_schema;
    private Map<String,Integer> map = new HashMap<String,Integer>();

    public OAITransporterList(List<Config.OAI> config, String urn_schema) {
        this.config =  config;
        this.urn_schema = urn_schema;
    }

    @Override
    public void create() {
        index = 0;
	    oais = new ArrayList<MetaCrawl.Transporter>();
        for (Config.OAI conf : config) {
            conf.urn_prefix=conf.urn_prefix==null?urn_schema:conf.urn_prefix;
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
        StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append(":");
        for (MetaCrawl.Transporter oai : oais) {
		    sb.append("\n    ");
		    sb.append(oai.probe());
        }
        return sb.toString();
    }

    @Override
    public Resource read(String oid) {
        log(map.get(oid) + " " + oid);
        return oais.get(map.get(oid)).read(oid);
    }

    @Override
    public List<String> getIdentifiers(int off, int limit) {
        map.clear();
        for (String id : oais.get(index).getIdentifiers(off, limit)) {
            map.put(id, index);
        }
        if (map.size() < limit && index < oais.size()-1) {
            index++;
            log("switch " + index + "/" + oais.size() 
                          + " " + oais.get(index).probe());
            mark = map.size();
            map.remove(mark-1); // remove zero marker
            for (String id : oais.get(index).getIdentifiers(off, limit)) {
                map.put(id, index);
            }
        }
        List<String> list = new ArrayList<String>(map.keySet());
        return list; // map.keySet();
    }

    @Override
    public Resource test(String oid) {
        if (index==0) {
            return oais.get(index).test(oid);
        } else {
            log(oid + " " + map.get(oid));
            return null;
        }
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
