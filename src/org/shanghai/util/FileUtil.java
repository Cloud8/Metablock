package org.shanghai.util;

import java.net.URL;
import java.net.MalformedURLException;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.BufferedOutputStream;

import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.ByteBuffer;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title Simple Utility Class to wrap Filesystem access
   @date 2013-01-18
*/
public class FileUtil {

    private static final Logger logger =
                         Logger.getLogger(FileUtil.class.getName());

    public static String read(final String what) {
        if (what==null)
            return what;
        if (what.startsWith("/")) {
            InputStream stream = FileUtil.class.getResourceAsStream(what);
            String result = read(stream);
            try {
                stream.close();
            } finally {
                if (result==null)
                    logger.info("resource " + what + " not found.");
                return result;
            }
        } else {
            File check = new File(what);
            if (check.exists())
                return read(check);
        }
        return null;
    }

    public static String read(final File f) {
        String result = null;
        try {
            FileInputStream stream = new FileInputStream(f);
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(
                              FileChannel.MapMode.READ_ONLY, 0, fc.size());
            result = Charset.defaultCharset().decode(bb).toString();
            stream.close();
        } finally {
            if (result==null)
                logger.info("File " + f.getName() + " not readable.");
            return result;
        }
	}

    public static String read(final InputStream is) { 
        String result = null;
        ByteBuffer bb = ByteBuffer.allocate(256);
        StringBuilder sb = new StringBuilder();
        try {
            ReadableByteChannel bc = Channels.newChannel(is);
            while(bc.read(bb) != -1) {
                  bb.flip();
                  result = Charset.defaultCharset().decode(bb).toString();
                  sb.append(result);
                  bb.clear();
            }
        } finally {
            return sb.toString();
        }
    }

    public static void write(String path, String text) {
        try {
		    writeFile(path, text);
		} catch(IOException e) {
		  e.printStackTrace();
		}
    }

    /** copy from URL to Filesystem */
    public static void copy(String from, String to) {
        try {
            URL oracle = new URL(from);
            InputStream is = oracle.openStream();
            DataOutputStream out = new DataOutputStream(
                                   new BufferedOutputStream(
                                   new FileOutputStream(
                                   new File(to))));
            int c;
            while((c = is.read()) != -1) {
                out.writeByte(c);
            }
            out.close();
            is.close();
        } catch(MalformedURLException e) { e.printStackTrace(); }
          catch(IOException e) { e.printStackTrace(); }
    }   

    //public static String readFile(String path) throws IOException {
	//    return readFile(new File(path));
    //}

    private static void writeFile(String path, String text) throws IOException {
        Writer out = new BufferedWriter(new OutputStreamWriter(
                     new FileOutputStream(path), "UTF-8"));
        //PrintStream out = null;
        try {
            //out = new PrintStream(new FileOutputStream(path));
            //out.print(text);
            out.write(text);
        } catch(FileNotFoundException e) { 
            throw new IOException(e.toString()); 
        } finally {
            if (out != null) out.close();
        }
    }

    /** Cannot read UTF8. Seems channels work better */
    private static String readX(final InputStream is, final int bufferSize)
    {
      final char[] buffer = new char[bufferSize];
      final StringBuilder out = new StringBuilder();
      try {
        final Reader in = new InputStreamReader(is, "UTF-8");
        try {
          for (;;) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
              break;
            out.append(buffer, 0, rsz);
          }
        }
        finally {
          in.close();
        }
      } catch (UnsupportedEncodingException ex) { /* ... */ }
        catch (IOException ex) { /* ... */ }
      return out.toString();
    }

    public static void main (String... args) {
        int argc = args.length;
        String in = args[0];
        File infile = new File(in);

        try {
            InputStream is = new FileInputStream(infile);
            String content = read(is);
            System.out.println("[" + content + "]");
        } catch(IOException e) { e.printStackTrace(); }

        String content = read(in);
        System.out.println("[" + content + "]");
    }
}
