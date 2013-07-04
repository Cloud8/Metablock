package org.shanghai.tika;

import org.shanghai.bones.BiblioRecord;
import org.shanghai.bones.BiblioModel;
import org.shanghai.bones.RecordFactory;

import org.shanghai.crawl.TDBTransporter;

import com.hp.hpl.jena.rdf.model.Model;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.ArrayList;

import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
 
/**
 * @license http://www.apache.org/licenses/LICENSE-2.0
 * @author Goetz Hatop <fb.com/goetz.hatop>
 * @title Scanner Collection using Tika to select Subscanner
 * @date 2012-10-11
 */
public abstract class Scanner implements TDBTransporter.Scanner {

    private String directory;
    private PDFScanner pdfScanner;
    //private MboxScanner mboxScanner;

    private static final Logger logger =
                         Logger.getLogger(Scanner.class.getName());

    private Tika tika;
	boolean mbox; //may be multiple records in a file (mbox)
    protected BiblioModel bibModel;
 
    public void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception e ) {
        log(e.toString());
    }

    // @Override
    // public TDBTransporter.Scanner create() {
    //     bibModel = new BiblioModel();
    //     bibModel.create();
    //     return this;
    // }

    @Override
    public void dispose() {
        bibModel.dispose();
    }

    @Override
    public void setStartDirectory(String d) {
        this.directory = d;
    }

    @Override
    public Model getModel(File file) {
        //log("getModel: " + directory);
        BiblioRecord b = RecordFactory.getRecord(directory, file);
        scanFile(file, b);
        //log(b.toString());
        log(b.getFileUrl());
        Model m = bibModel.getModel(b);
        return m;
    }

    abstract BiblioRecord scanFile(File file, BiblioRecord b);

    /**
     * Detect and return MIME Type of the given file.
     * @param file java.io.File to detect MIME Type
     * @return MIME Type of the file as String
     */
    public String getMimeType(File file) {
        String mimeType = null;
        try {
            mimeType = tika.detect(file);
        } catch (FileNotFoundException e) { log(e);} 
          catch (IOException e) { log(e); }
        finally {
          return mimeType;
        }
    }
 
    private boolean isMbox(File file) {
        mbox = false;
	    try {
	        FileInputStream is = new FileInputStream(file);
		    byte[] b = new byte[5];
		    is.read(b);
		    String from = new String(b);
            if ("From ".equals(from)) 
                mbox = true;
		} catch(IOException e) { log(e); }
		return mbox;
	}
}
