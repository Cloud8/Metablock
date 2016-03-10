package org.seaview.opus;

import org.shanghai.util.FileUtil;
import org.seaview.pdf.PDFLoader;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDFS;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;


/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Meta Block
  @title Provide acces to ByteArrayOutputStreams
  @date 2015-12-17
*/
public class BaosRecord {

    private PDFLoader pl;

    public BaosRecord(PDFLoader pl) {
        this.pl = pl;
    }

    public void create() {
        pl.create();
    }

    public void dispose() {
        pl.dispose();
    }

    public class Record {
        public Record(String name, String format, ByteArrayOutputStream baos) {
            this.name = name;
            this.format = format;
            this.baos = baos;
        }
        public String name;
        public ByteArrayOutputStream baos;
        public String format;
    }

    public List<Record> getData(Resource rc) {
        List<Record> records = new ArrayList<Record>();
        StmtIterator si = rc.listProperties(DCTerms.hasPart);
        while( si.hasNext() ) {
            Resource file = si.nextStatement().getResource();
            String fname = file.getURI();
            if (fname.endsWith("/All.pdf")) {
                //skip this
            } else if (file.hasProperty(DCTerms.format)) {
                String format = file.getProperty(DCTerms.format).getResource()
                                    .getProperty(RDFS.label).getString();
                if (format.contains("PDF") || format.contains("pdf")) {
                    pl.create();
                    pl.analyze(file);
                    if (fname.startsWith(rc.getURI())) {
                        fname = fname.substring(rc.getURI().length()+1);
                    } else {
                        fname = fname.substring(fname.lastIndexOf("/")+1);
                        //log("found " + rc.getURI() + " " + fname);
                    }
                    if (fname.endsWith(".pdf")) {
					    // bitstream format detection
					} else {
					    fname += ".pdf";
					}
                    if (pl.valid()) {
                        Record record = new Record(fname, format, pl.getBaos());
                        records.add(record);
                    } else {
                        log("invalid " + fname);
                        Record record = new Record(fname, null, null);
                        records.add(record);
                    }
                    pl.dispose();
                } else if (format.equals("application/xml")) {
                    ByteArrayOutputStream baos = FileUtil.load(fname);
                    Record record = new Record(fname, format, baos);
                    records.add(record);
                }
            }
        }
        RDFNode node = rc.getProperty(FOAF.img).getObject();
        if (node.isLiteral()) {
            String cover = node.toString();
            String name = cover.substring(cover.lastIndexOf("/")+1);
            ByteArrayOutputStream baos = FileUtil.load(cover);
            Record record = new Record(name, "image/png", baos);
            records.add(record);
        }
        // may be later:
        // Record meta = new Record("meta.rdf", ModelUtil.getBaos(rc));
        // records.add(meta);
        return records;
    }

    private static final Logger logger =
                         Logger.getLogger(BaosRecord.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception ex) {
        logger.info(ex.toString());
        ex.printStackTrace();
    }

}
 
