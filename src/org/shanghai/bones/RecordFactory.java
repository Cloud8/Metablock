package org.shanghai.bones;

import org.shanghai.bones.BiblioRecord;

import java.io.File;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop <fb.com/goetz.hatop>
  @title A RecordFactory for BiblioRecords.
  @date 2012-11-13
*/
public class RecordFactory {

    public static BiblioRecord getRecord(String directory, File file) {
        BiblioRecord b = new BiblioRecord();
	    b.recordtype = "rdf";
	    String id = file.getPath();
        b.setUrl(id);
	    id = directory==null ? 
	    id:id.substring(id.lastIndexOf(directory)+directory.length());
	    b.id = id.replace("/",":"); 
        if (b.id.startsWith(":"))
            b.id = b.id.substring(1);
	    Date date = new Date(file.lastModified());
	    b.modified = new SimpleDateFormat("yyyy-MM-dd").format(date);
		return b;
	}

}
