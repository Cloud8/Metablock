package org.seaview.data;

import org.shanghai.util.FileUtil;
import org.shanghai.crawl.TrivialScanner;
import org.shanghai.rdf.Config;
import org.shanghai.util.PrefixModel;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.nio.charset.Charset;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.net.MalformedURLException;
import java.util.logging.Logger;
//import java.util.Base64;
import org.apache.commons.codec.binary.Base64;

/**
    ✪ (c) reserved.
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop 
    @title Datacite DOI registration
    @date 2015-02-22
*/
public class DOI {

    private static String prefix = "10.5072"; // test prefix
    private static String server = "https://test.datacite.org/mds"; // test
    private static String user;
    private static String pass;
    private static Logger logger = Logger.getLogger(DOI.class.getName());
  
    public DOI(String pref, String store, String user, String pass) {
        this.prefix = pref;
        this.server = store;
        this.user = user;
        this.pass = pass;
    }
  
    public void create() {
    }
  
    public void dispose() {
    }
  
    private void make(String cmd, String value) {
        if (cmd.startsWith("-test")) {
            String doi = getDoi(value);
            System.out.println(doi);
        } else if (cmd.equals("-post")) {
            if (value.endsWith(".xml") && new File(value).exists()) {
                log("sending " + value);
                String data = FileUtil.read(value);
                String result = postData(data);
                log(result);
            }
        } else if (cmd.equals("-register")) {
            String doi = getDoi(value);
            log("register " + doi + " " + value);
            String result = register(doi, value); // register doi to url mapping
            log(result);
        }
    }

    /** create doi based off an URI */
    static String createDoi(String uri) {
        String doi = null;
        String src = uri.substring(uri.indexOf("//")+2);
        src = src.substring(src.indexOf("/")+1);
        if (uri.startsWith("http://meta-journal.net")) {
            doi = prefix + "/meta" + src.replace("/",".");
        } else if (uri.startsWith("http://")) {
            //log("createDoi " + uri + " " + src);
            src = src.replace("diss/","");
            src = src.replace("ep/0002","medrez");
            src = src.replace("ep/0003","meta");
            src = src.replace("ep/0004","mjr");
            src = src.replace("/",".");
            doi = prefix + "/" + src;
        }
        return doi;
    }

    static String createDoi(String uri, 
            String year, String volume, String number, String articleId) {
        String doi = createDoi(uri);
        doi = doi.substring(0, doi.indexOf(".", doi.indexOf("/")));
        doi += "." + year;
        if (volume!=null) {
             doi += "." + volume;
        }
        if (number!=null) {
             doi += "." + number;
        }
        if (articleId!=null) {
             doi += "." + articleId;
        }
        return doi;
    }

    static String register(String doi, String url) {
        String body = "doi=" + doi + "\nurl=" + url;
        String permission = user+":"+pass;
        byte[] postData = body.getBytes( Charset.forName( "UTF-8" ));
        StringWriter writer = new StringWriter();
        try {
            URL curl = new URL (server + "/doi");
            //String encoding = new String(
            //       Base64.getEncoder().encode(permission.getBytes()));
            String encoding = new String(
                     Base64.encodeBase64(permission.getBytes()));
            HttpURLConnection conn = (HttpURLConnection) curl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Basic " + encoding);
            conn.setRequestProperty("Content-Type", "text/plain");
            conn.setRequestProperty("charset", "utf-8");
            try( DataOutputStream wr = 
                 new DataOutputStream( conn.getOutputStream())) {
                 wr.write( postData );
            }
            InputStream content = (InputStream)conn.getInputStream();
            BufferedReader in = 
                new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = in.readLine()) != null) {
                writer.append(line);
            }
        } catch(IOException e) {
            log(e);
        }
        return writer.toString();
    }
  
    static String postData(String data) {
        String permission = user+":"+pass;
        StringWriter writer = new StringWriter();
        try {
            byte[] postData = data.getBytes("UTF-8");
            URL curl = new URL (server + "/metadata");
            //String encoding = new String(
            //       Base64.getEncoder().encode(permission.getBytes()));
            String encoding = new String(
                     Base64.encodeBase64(permission.getBytes()));
            HttpURLConnection conn = (HttpURLConnection) curl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Basic " + encoding);
            conn.setRequestProperty("Content-Type", "application/xml");
            conn.setRequestProperty("charset", "utf-8");
            try( DataOutputStream wr = 
                 new DataOutputStream( conn.getOutputStream())) {
                 wr.write( postData );
            }
            InputStream content = (InputStream)conn.getInputStream();
            BufferedReader in = 
                new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = in.readLine()) != null) {
                writer.append(line);
            }
        } catch(UnsupportedEncodingException e) {
            log(e);
        } catch(IOException e) {
            log(e);
        }
        return writer.toString();
    }

    static String getDoi(String doi) {
        String perm = user+":"+pass;
        //String auth = new String(Base64.getEncoder().encode(perm.getBytes()));
        String auth = new String(Base64.encodeBase64(perm.getBytes()));
        StringWriter writer = new StringWriter();
        String result = null;
        try {
            URLConnection conn = new URL(server+"/doi/"+doi).openConnection();
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("Authorization", "Basic " + auth);
            InputStream is = conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = in.readLine()) != null) {
                writer.append(line);
            }
            result = writer.toString();
        } catch(UnsupportedEncodingException e) {
            log(e);
        } catch(IOException e) {
            log(doi + " " + e.toString());
        } finally {
            return result;
        }
    }
  
    static String delete(String doi) {
        String perm = user+":"+pass;
        //String auth = new String(Base64.getEncoder().encode(perm.getBytes()));
        String auth = new String(Base64.encodeBase64(perm.getBytes()));
        StringWriter writer = new StringWriter();
        try {
            HttpURLConnection conn = (HttpURLConnection)(new URL(server+"/metadata/"+doi)).openConnection();
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("Authorization", "Basic " + auth);
            conn.setRequestProperty("Content-Type", 
                                    "application/x-www-form-urlencoded" );
            conn.setRequestMethod("DELETE");
            InputStream is = conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = in.readLine()) != null) {
                writer.append(line);
            }
        } catch(UnsupportedEncodingException e) {
            log(e);
        } catch(IOException e) {
            log(e);
        } finally {
            return writer.toString();
        }
    }

    public static String resolve(String doi) {
        boolean b = false;
        if (doi.startsWith("http://")) {
        } else {
            //log("doi mapped: " + doi);
            doi += "http://dx.doi.org/" + doi;
        }
        String url = null;
        try {
            url = PrefixModel.resolveDOI(doi);
            b = true;
        } catch(MalformedURLException e) { log(e); }
          catch(FileNotFoundException e) { }
          catch(UnknownHostException e) { }
          catch(IOException e) { log(e); }
        finally {
            if (b) {
                return url;
            } else {
                return null;
            }
        }
    }

    private static void log(String msg) {
        logger.info(msg);
    }
  
    private static void log(Exception e) {
        log(e.toString());
        try {
             throw(e);
        } catch(Exception ex) {}
    }

    public static void main(String[] args) {
        Config config = new Config("seaview.ttl").create();
        String prefix = config.get("doi.prefix");
        String store = config.get("doi.store");
        String user = config.get("doi.user");
        String pass = config.get("doi.pass");
        DOI main = new DOI(prefix, store, user, pass);
        main.create();
        if (args.length==2) {
            main.make(args[0], args[1]);
        } else if (args.length==1 && args[0].startsWith("http://")) {
            System.out.println(createDoi(args[0]));
        } else {
            main.log("usage: DOI -test uri | -post dcite.xml | -register uri");
        }
        main.dispose();
    }
}
