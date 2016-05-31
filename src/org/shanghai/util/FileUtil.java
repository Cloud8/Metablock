package org.shanghai.util;

import java.net.URL;
import java.net.HttpURLConnection;
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

import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.ByteArrayOutputStream;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.nio.file.StandardCopyOption;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileVisitResult;
import java.nio.file.DirectoryStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.text.Normalizer;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop 
   @title Utility Class to wrap File access
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
		    // log("readResource " + path);
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
        if (path==null) {
            return false;
        }
        return write(Paths.get(path), text);
    }

    public static boolean write(String path, byte[] bytes) {
        boolean b = true;
        try {
            //FileOutputStream fos = new FileOutputStream(path);
            //fos.write(bytes);
            //fos.close();
            Files.write(Paths.get(path), bytes);
        } catch(IOException e) {
            log(e.getMessage());
        } finally {
            return b;
        }
    }

    public static boolean write(Path path, String text) {
        boolean b = false;
        try {
            text = Normalizer.normalize(text, Normalizer.Form.NFC);
			Files.write(path, text.getBytes());
            b = true;
		} catch(AccessDeniedException e) {
          log(e);
		} catch(NoSuchFileException e) {
          log(e);
		} catch(IOException e) {
		  e.printStackTrace();
		} 
        return b;
    }

    /** copy from URL to Filesystem : should make directories */
    public static void copy(String from, String to) {
        copy(from, Paths.get(to));
    }   

    public static void copyIfExists(String url, Path path) {
        if (url.startsWith("file:")) {
            copy(getFilePath(url), path);
            return;
        } else if (url.startsWith("http")) try {
            URL u = new URL(url);
            HttpURLConnection huc = (HttpURLConnection)u.openConnection();
			huc.setRequestMethod("HEAD");
            huc.connect();
			if (huc.getResponseCode() == HttpURLConnection.HTTP_OK) {
			    copy(url, path);
			}
        } catch(IOException e) { e.printStackTrace(); }
    }

    public static void copy(String url, Path path) {
        if (url.startsWith("file:")) {
            copy(getFilePath(url), path);
            return;
        }
        try {
            URL oracle = new URL(url);
            InputStream is = oracle.openStream();
			Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
            is.close();
        } catch(MalformedURLException e) { e.printStackTrace(); }
          catch(AccessDeniedException e) { log(e.toString()); }
          catch(FileNotFoundException e) { e.printStackTrace(); }
          catch(IOException e) { log(e.toString()); }
          //catch(IOException e) { e.printStackTrace(); }
    }   

    public static void copy(Path from, Path to) {
        try {
			Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        } catch(IOException e) { e.printStackTrace(); }
    }   

    public static boolean exists(String path) {
        return Files.exists(Paths.get(path));
    }

    public static void mkdir(Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch(IOException e) { log(e); }
        }
    }

    public static String dirname(String name) {
        int slash = name.lastIndexOf('/');
        return (slash == -1) ? name : name.substring(0, slash);
    }

    public static String basename(String name) {
        int slash = name.lastIndexOf('/');
        return (slash == -1) ? name : name.substring(slash+1);
    }

    public static String basename(String name, String suffix) {
        int dot = name.lastIndexOf(suffix);
        name = (dot == -1) ? name : name.substring(0, dot);
        return basename(name);
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

    public static void copyFiles(String source, String target) {
        final Path targetPath = Paths.get(target);
        copyFiles(source, targetPath);
    }

    public static void copyFiles(String source, final Path targetPath) {
        final Path sourcePath = Paths.get(source);
        if (!Files.isDirectory(sourcePath)) {
		    log("no source directory " + source);
            return;
        }
        try {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir,
                    final BasicFileAttributes attrs) throws IOException {
                    Files.createDirectories(targetPath.resolve(sourcePath
                        .relativize(dir)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file,
                    final BasicFileAttributes attrs) throws IOException {
                    Files.copy(file,
                        targetPath.resolve(sourcePath.relativize(file)));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch(IOException e) { log(e); }
    }

    public static List<String> listFiles(Path path, String glob) {
	    List<String> result = new ArrayList<String>();
        try {
		    DirectoryStream<Path> paths = Files.newDirectoryStream(path, glob);
			for(Path sub: paths) {
			    if (Files.isRegularFile(sub)) {
			        result.add(path.getFileName() + "/" + sub.getFileName());
				}
			}
			paths.close();
        } catch(IOException e) { log(e); }
		return result;
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

    private static boolean writeFile(String path, String text) 
        throws IOException {
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

    public static boolean touch(Path path, String date) {
        boolean b = false;
        if (date==null) {
            return b;
        }
        //log("date [" + date + "]");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedDate = LocalDate.parse(date, df);
        LocalDateTime ldt = parsedDate.atStartOfDay();
        ZonedDateTime zdt = ldt.atZone(ZoneId.of("Europe/Berlin"));
        long millis = zdt.toInstant().toEpochMilli();
        FileTime time = FileTime.fromMillis(millis);
        try {
            Files.setLastModifiedTime(path, time);
            //log(path.toString());
            b = true;
        } catch(IOException e) { log(e); }
        return b;
    }

    public static synchronized ByteArrayOutputStream load(String url) {
        if (url.startsWith("file:")) {
            return readFile(url);
        } else if (url.matches("[A-Za-z].*")) {
            Path path = Paths.get(System.getProperty("user.home") + "/" + url);
            return readFile(path);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            URL oracle = new URL(url);
            is = oracle.openStream();
            byte[] chunk = new byte[4096];
            int n;
            while ( (n = is.read(chunk)) > 0 ) {
                baos.write(chunk, 0, n);
            }
        } catch(MalformedURLException e) { log(e); }
          catch(IOException e) { log(e); }
        finally {
          if (is != null) {
              try { is.close(); } catch(IOException e) { log(e); }
          }
        }
        return baos;
    }
  
    private static synchronized ByteArrayOutputStream readFile(String url) {
        return readFile(getFilePath(url));
    }

    private static synchronized ByteArrayOutputStream readFile(Path path) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            InputStream is = Files.newInputStream(path);
            for (int readNum; (readNum = is.read(buf)) != -1;) {
                //Writes to this byte array output stream
                baos.write(buf, 0, readNum); 
            }
        } catch (IOException ex) {
            log(ex);
        }
        return baos;
    }

    private static Path getFilePath(String uri) {
        Path path = null;
        if (uri.startsWith("file:///")) {
            path = Paths.get(uri.substring(7));
        } else if (uri.startsWith("file://")) {
            path = Paths.get(System.getProperty("user.home")
                             + "/" + uri.substring(7));
        }
        return path; 
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
