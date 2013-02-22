package org.shanghai.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;

import java.util.Properties;
import java.util.logging.Logger;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title Utility Class to wrap Filesystem access
   @date 2013-02-18
*/

public class FileUtil {

    private static final Logger logger =
                         Logger.getLogger(FileUtil.class.getName());

    private void log(String msg) {
        logger.info(msg);    
    }

    public static String readFile(File f) throws IOException {
        FileInputStream stream = new FileInputStream(f);
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(
                              FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        }
        finally {
            stream.close();
        }
	}

    public static String readFile(String path) throws IOException {
	    return readFile(new File(path));
    }

    public static void writeFile(String path, String text) throws IOException {
        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream(path));
            out.print(text);
        } catch(FileNotFoundException e) { 
            throw new IOException(e.toString()); 
        } finally {
            if (out != null) out.close();
        }
    }

}
