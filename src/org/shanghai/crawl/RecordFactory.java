package org.shanghai.crawl;

import org.shanghai.bones.BiblioRecord;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop <fb.com/goetz.hatop>
  @title A Metadata Scanner for many Formats: The Parent.
  @date 2012-11-13
*/
public class RecordFactory {

    private static final Logger logger =
                         Logger.getLogger(RecordFactory.class.getName());

    static void log(String msg) {
        logger.info(msg);    
    }

    static void log(Exception e) {
        log(e.toString());    
    }

    public static BiblioRecord getRecord(String directory, File file) {
        BiblioRecord b = new BiblioRecord();
	    b.recordtype = "code";
	    String id = file.getPath();
        b.setUrl(id);
	    id = directory==null ? 
	    id:id.substring(id.lastIndexOf(directory)+directory.length()+1);
	    b.id = id.replace("/",":"); 
	    Date date = new Date(file.lastModified());
	    b.setPublishDate(new SimpleDateFormat("yyyy-MM-dd").format(date));
		return b;
	}

}
