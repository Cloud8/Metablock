package org.shanghai.solr;

import org.shanghai.bones.BiblioRecord;
import org.shanghai.bones.BiblioModel;
import org.shanghai.bones.RecordFactory;
import org.shanghai.bones.FileScanner;

import org.shanghai.solr.SolrClient;
import org.shanghai.crawl.FileCrawl;
import org.shanghai.crawl.TDBTransporter;
import org.shanghai.util.FileUtil;

import com.hp.hpl.jena.rdf.model.Model;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop <fb.com/goetz.hatop>
  @title File Transporter
  @date 2012-10-07
  @abstract Makes a Bibliorecord from a file and indexes the
            record to solr. Needs a Scanner.
*/
public class SolrTransporter implements FileCrawl.Transporter {

    private SolrClient solrClient;
    private List<TDBTransporter.Scanner> scanner;
    private BiblioModel bibModel;
    private String base;

    private static final Logger logger =
                         Logger.getLogger(SolrTransporter.class.getName());

    public SolrTransporter(String url, String base) {
        bibModel = new BiblioModel(base);
        solrClient = new SolrClient(url);
        scanner = new ArrayList<TDBTransporter.Scanner>();
        this.base = base;
    }

    private void log(String msg) {
        logger.info(msg);    
    }

    private void log(Exception e) {
        log(e.toString());    
    }

    @Override
    public void create() {
        bibModel.create();
        solrClient.create();
        addScanner(new FileScanner(base).create());
    }

    @Override
    public void dispose() {
        bibModel.dispose();
        solrClient.dispose();
        for (TDBTransporter.Scanner s: scanner)
             s.dispose();
    }
 
    @Override
	public void addScanner(TDBTransporter.Scanner s) {
	    scanner.add(s);
	}

    @Override
    public void setStartDirectory(String dir) {
        for (TDBTransporter.Scanner s: scanner)
             s.setStartDirectory(dir);
    }

    public void clean() {
        solrClient.clean();
    }

    public String readAsString(String what) { 
        return readFile(new File(what));
    } 

    private String readFile(File f) {
        return FileUtil.read(f);
    }

    @Override
    public void delete(String what) {
        log("/*Unwilling*/");
    } 

    @Override /** solr can do that */
    public boolean create(File file) {
        return update(file);
    }

    @Override
    public boolean update(File file) {
        for (TDBTransporter.Scanner s: scanner) {
		     if (s.canTalk(file)) {
                 Model m = s.getModel(file);
                 BiblioRecord b = bibModel.read(m);
                 // BiblioRecord b = s.getRecord(file);
                 //log("Transporter::update " + b.id);
                 solrClient.add(b);
                 return true;
			 }
        }
        // } else if (scanner.mbox) {
        //     BiblioRecord[] records = scanner.getRecords(file);
        //     for (BiblioRecord b : records)
        //         solrClient.add(b);
        //     return true;
        // }
        return false;
    }

}
