package org.shanghai.crawl;

import org.shanghai.bones.BiblioRecord;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @license http://www.apache.org/licenses/LICENSE-2.0
 * @author Goetz Hatop <fb.com/goetz.hatop>
 * @title Simple Scanner able to deliver some file Metadata. 
 * @date 2012-10-11
 */
public class FileScanner implements TDBTransporter.Scanner {

    private TextFileScanner scanner;
    private String directory;

    private static final Logger logger =
                         Logger.getLogger(FileScanner.class.getName());

    public void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception e ) {
        log(e.toString());
    }

    @Override
    public void create() {
        scanner = new TextFileScanner();
        scanner.create();
    }

    @Override
    public void dispose() {
        if (scanner!=null) 
            scanner.dispose();
        scanner = null;
    }

    @Override
    public void setStartDirectory(String d) {
        this.directory = d;
    }

    @Override
    public BiblioRecord getRecord(File file) {
        BiblioRecord b = RecordFactory.getRecord(directory, file);
        try {
		    scanner.scanFile(file, b);
        } catch(FileNotFoundException e) { log(e); }
          catch(IOException e) { log(e); }
        return b;
    }

    @Override
    public String getDescription(File file) {
        return getRecord(file).toString();
    }

    /**
    @Override
    public boolean canTalk(File file) {
	    if (file.getName().endsWith(".php")) {
           return true;
        } else if (file.getName().endsWith(".java")) {
           return true;
        }
        return false;
    }

    @Override
    public void update(File file, BiblioRecord b) {
        try {
		    scanner.scanFile(file, b);
        } catch(FileNotFoundException e) { log(e); }
          catch(IOException e) { log(e); }
    }
    **/
 
}
