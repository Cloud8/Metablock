package org.shanghai.util;

import java.net.URL;
import java.net.MalformedURLException;

import java.io.DataOutputStream;
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

import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop 
   @title Simple Utility Class to wrap Filesystem access
   @date 2013-01-18
*/
public class FileUtil {

    public static String read(final String path) {
        //log("read: " + path);
        String content = null;
        if (Files.isRegularFile(Paths.get(path))) {
            try {
                content = new String(Files.readAllBytes(Paths.get(path)));
		    } catch(IOException e) { log(e); log(path); }
        } else {
		    log("readResource " + path);
            content = readResource(path);
        }
        return content;
    }

    public static String read(final Path path) {
        try {
            InputStream in = path.getFileSystem()
                                 .provider().newInputStream(path);
            return read(in);
		} catch(IOException e) { log(e); log(path.toString()); }
        return null;
    }

    public static String readResource(final String file) {
	    String result = null;
        try {
            InputStream in = FileUtil.class.getResourceAsStream(file);
            result = read(in);
            in.close();
		} catch(IOException e) { log(e.toString()); }
          finally {
            if (result==null || result.length()==0) {
                logger.info("FileUtil: resource " + file + " not found.");
                //return read(new FileUtil(), file);
                //return read(Paths.get(file));
            }
            return result;
        }
        //if (file.startsWith("/")) {
        //}
        //return read(Paths.get(file));
        //return null;
    }

    /*
    private static String oldRead(final String file) {
        if (file==null)
            return file;
        if (file.startsWith("/")) {
		    String result = null;
            try {
                InputStream in = FileUtil.class.getResourceAsStream(file);
                if (in==null) {
                    Path path = Paths.get(file);
				    in = path.getFileSystem().provider().newInputStream(path);
                }
                result = read(in);
                in.close();
			} catch(IOException e) { log(e.toString()); }
              finally {
                if (result==null || result.length()==0) {
                    logger.info("FileUtil: resource " + file + " not found.");
                    return read(new FileUtil(), file);
                }
                return result;
            }
        } else {
            File check = new File(file);
            if (check.exists())
                return read(check);
        }
        return null;
    }
    */

    /* 
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
    */

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

    public static List<String> readAllLines(String fname) {
        Path path = Paths.get(fname);
        Charset utf8 = StandardCharsets.UTF_8;
        try {
            List<String> result = Files.readAllLines(path, utf8); 
	    	return result;
        } catch(IOException e) {
            log(e.getMessage());
        }
        return null;
    }

    public static boolean write(String path, String text) {
        boolean b = false;
        try {
		    b = writeFile(path, text);
		} catch(IOException e) {
		  e.printStackTrace();
		} 
        return b;
    }

    public static boolean write(Path path, String text) {
        boolean b = false;
        try {
			Files.write(path, text.getBytes());
		} catch(IOException e) {
		  e.printStackTrace();
		} 
        return b;
    }

    /** copy from URL to Filesystem : should make directories */
    public static void copy(String from, String to) {
        try {
            URL oracle = new URL(from);
            InputStream is = oracle.openStream();
			Files.copy(is, Paths.get(to));
        } catch(MalformedURLException e) { e.printStackTrace(); }
          catch(IOException e) { e.printStackTrace(); }
    }   

    /*
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
    */

    //public boolean isDirectory(String path) {
    //    return new File(path).isDirectory();
    //}

    public static String base(String name) {
        int slash = name.lastIndexOf('/');
        return (slash == -1) ? name : name.substring(0, slash);
        //String extension = (dot == -1) ? "" : name.substring(dot+1);
    }

    public static String removeExtension(String s) {

        String separator = System.getProperty("file.separator");
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1)
            return filename;

        return filename.substring(0, extensionIndex);
    }

    private static String read(Object who, String what) {
        InputStream is = FileUtil.class.getResourceAsStream(what);
        if (is==null) {
            is = FileUtil.class.getResourceAsStream("lib" + what);
            if (is!=null) log("load lib " + what);        }
        if (is==null) {
            is = who.getClass().getClassLoader().getResourceAsStream(what);
            if (is!=null) log("class load " + what);
        }
        if (is==null) {
            is = who.getClass().getClassLoader().getResourceAsStream("lib"+what);
            if (is!=null) log("class load lib " + what);
        }
        if (is==null) {
            return null;
        }
        //stupid scanner tricks
        //java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
        //return s.hasNext() ? s.next() : "";
        return FileUtil.read(is);
    }

    private static boolean writeFile(String path, String text) throws IOException {
        Writer out = null;
        boolean b = false;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                     new FileOutputStream(path), "UTF-8"));
            out.write(text);
            b = true;
        } catch(FileNotFoundException e) { 
            //throw new IOException(e.toString()); 
        } finally {
            if (out != null) out.close();
            return b;
        }
    }

    private static final Logger logger =
                         Logger.getLogger(FileUtil.class.getName());

    private static void log(String msg) {
        logger.info(msg);
    }

    private static void log(Exception e) {
        e.printStackTrace(); 
        log(e.toString());
    }

}
