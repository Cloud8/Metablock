package org.metablock.rest;

import org.seaview.bones.Crawl;
import org.seaview.bones.OpacTransporter;
import org.seaview.opus.OpusStorage;

import org.shanghai.rdf.Config;
import org.shanghai.solr.SolrStorage;
import org.shanghai.store.StreamStorage;
import org.shanghai.crawl.MetaCrawl;

import org.shanghai.data.DumpStorage;
import org.shanghai.util.ModelUtil;

import org.shanghai.store.FileStorage;
import org.shanghai.data.FileTransporter;
import org.shanghai.data.FileScanner;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;

import javax.servlet.ServletException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *  @version 0.55
 *  @title Rest Servlet to sync RDF
 *  @date 2018-03-01
 */
public class Metablock extends HttpServlet {

    private final static String myself = "Metablock";
    private final static String version = "0.55";
    private final static int LIMIT = 10; // Thread limit
    private static final Object lock = new Object();

    private String title;
    private Logger logger;

    private Crawl crawl;
    private MetaCrawl.Transporter transporter;
    private MetaCrawl.Storage storage;
    private Config metablock;
    private static int count;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        title = config.getInitParameter("title");
        String mblock = config.getInitParameter("metablock");
        InputStream is = getServletContext().getResourceAsStream(mblock);
        metablock = new Config(mblock).create(is);
        try {
            is.close();
        } catch(IOException e) { log(e); }
        crawl = new Crawl(metablock);
        count = 0;
    }

    @Override
    public void destroy() {
        crawl.dispose();
        super.destroy();
    }

    @Override
    public void log(String message) {
        if (logger==null) {
            logger = Logger.getLogger(Metablock.class.getName());
            logger.info(myself + " " + version + " " + title);
        }
        logger.info(message);
    }

    public void doPost (HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        doGet(req, res);
    }

    public void doGet (HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException { 

        long start = System.currentTimeMillis();
        String response = null;

        String path = req.getPathInfo();
        if (path==null) {
            log("rest alive.");
            response = "OK.";
        } else if (path.startsWith("/test")) {
            response = getEnvironment(req);
        } else if (path.contains("/daia/")) {
            // legacy support
            if (req.getParameter("id")!=null) {
                path = path + "/" + req.getParameter("id");
            }
            response = rest(start, path, res);
        } else { 
            response = rest(start, path, res);
        }

        if (response==null) {
            // Nothing
        } else { 
            res.setContentType("text/plain; charset=UTF-8");
            res.getWriter().println(response);
        }
    }

    private String rest(long start, final String path, HttpServletResponse res)
        throws ServletException, IOException {
        String response = null;
        String source = null;
        String target = null;
        String search = null;

        String[] args = path.split("/");
        int argc = args.length;

        if (argc<3) {
            response = "/";
        } else if (argc==3) { // stream out from any source
            source = args[1];
            search = args[2];
            rest5(start, source, "rdf", search, res); // own response
        } else { 
            source = args[1];
            target = args[2];
            StringBuilder sb = new StringBuilder(args[3]);
            for (int i=4; i<argc; i++) {
                sb.append("/");
                sb.append(args[i]);
            }
            // preserve file oid like /../rdf//var/tmp/test.pdf
            search = argc>4?"/"+sb.toString():sb.toString();

            if (target.equals("daia")) { // DAIA may have a double slash
                daia(start, source, search, res);
            } else if (target.matches("rdf|json")) {
                rest5(start, source, target, search, res); // own response
            } else {
                response = rest4(start, source, target, search);
            }
        }
        long time = (System.currentTimeMillis()-start);
        log("REST " + argc + " -- " + time + " µsec. ");
        return response;
    }

    private String delete(long start, String source, String target, String oid)
    {
        String response = null;
        crawl.create(source, target);
        if (oid!=null && oid.matches("[a-zA-Z0-9:-]+")) {
            storage = crawl.createStorage(target);
            boolean b = storage.delete(oid);
            storage.dispose();
            long time = (System.currentTimeMillis()-start);
            response = "Delete " + " " + oid + " " + b  +" "+time+" µs";
        } else {
            response = "Unwilling to delete " + oid;
        }
        log(response);
        return response;
    }

    private String rest4(long start, String source, String target, String oid) {
        String response = null;
        if (source.equals("del")) { // shorthand request
            response = delete(start, "empty", target, oid);
        } else if (oid.equals("*")) {
            response = crawl(source, target); 
        } else if (source.matches("(op[ua][sc]|temp):?[0-9]?|files|ojs") 
            && target.matches("[ctso][eop][mlu][dprs][:0-9]*|empty")) {
            log("rest4 " + source + " - " + target + " " + oid);
            if (count>LIMIT || target.equals("temp")) {
                response = restSingle(start, source, target, oid);
            } else {
                response = restThread(start, source, target, oid);
            }
        } else {
            long time = (System.currentTimeMillis()-start);
            response = "Failed " + source + " " + time + " µsec.";
            log("rest4 failed " + source + " - " + target + " - " + oid);
        }
        return response;
    }

    private String restSingle(final long start, final String source, 
        final String target, final String search) {
        String response = null;
        crawl.create(source, target);
        Resource rc = null;
        storage = crawl.createStorage(target);
        if (storage==null) {
            response = ".";
        } else {
            transporter = crawl.createTransporter(source);
            crawl.crawl(search);
            transporter.dispose();
            storage.dispose();
            long time = (System.currentTimeMillis()-start);
            response = "OK " + time + " µs ";
        }
        crawl.dispose();
        log(response);
        return response;
    }

    private String restThread(final long start, 
        final String source, final String target, final String search) {
        String response = null;
        new Thread() { public void run() { 
            count++;
            Crawl crawl = new Crawl(metablock);
            crawl.create(source, target);
            MetaCrawl.Storage storage = crawl.createStorage(target); 
            String response = null;
            Resource rc = null;
            MetaCrawl.Transporter transporter = null;
            if (storage==null) {
                log(".");
            } else {
                transporter = crawl.createTransporter(source);
                crawl.crawl(search);
                transporter.dispose();
                storage.dispose();
                long time = (System.currentTimeMillis()-start);
                log("OK thread " + count + " " + time + " µs ");
            }
            crawl.dispose();
            count--;
        }}.start();
        long time = (System.currentTimeMillis()-start);
        response = "OK " + time + " µsec.";
        return response;
    }

    private String crawl(final String source, final String target) {
	    log("Index from " + source + " to " + target);
	    String response = null;
	    if (count>0) {
            response = "Index already running.";
	    } else {
	        count++;
            new Thread() { public void run() { 
                Crawl crawl = new Crawl(metablock);
                crawl.create(source, target);
                transporter = crawl.createTransporter(source);
                storage = crawl.createStorage(target);
	            crawl.crawl();
	            crawl.dispose();
		        count--;
            }}.start();
            response = "Full index started.";
        }
        return response;
    }

    private void rest5(long start, String source, String format, String oid, 
        HttpServletResponse res) throws IOException {

        Resource rc = null;
        String mime = null;

        if (source.equals("ppn")) {
            source = "opac";
        }

        if (source.matches("op[au][sc]|temp") && oid.matches("[0-9Xc]*")) {
            log("rest5 " + source + " - " + format + ": " + oid);
            crawl.create(source, "empty");
            transporter = crawl.createTransporter(source);
            rc = transporter.read(oid);
            transporter.dispose();
        } else if (source.equals("ojs") && oid.matches("[0-9ij]*")) {
            log("rest5 " + source + " - " + format + ": " + oid);
            crawl.create(source, "empty");
            transporter = crawl.createTransporter(source);
            rc = transporter.read(oid);
            transporter.dispose();
        } else if (source.equals("files:pdf")) {
            log("rest5 " + source + " - " + format + " # " + oid);
            metablock.set("pdf.extractor", "cermine");
            metablock.set("pdf.fulltext", "false");
            crawl.create(source, "empty");
            transporter = crawl.createTransporter(source);
            rc = crawl.analyze(oid);
            transporter.dispose();
        } else {
            log("Bad rest5 " + source + " - " + format + " # " + oid);
        }

        if (format.equals("rdf")) {
            mime = "application/rdf+xml";
        } else if (format.equals("json")) {
            mime = "application/ld+json";
        } 

        if (rc!=null && mime!=null) {
            res.setContentType(mime);
            storage = new StreamStorage(res.getOutputStream(), mime);
            storage.write(rc);
            storage.dispose();
            res.getOutputStream().flush();
        } else {
            res.getOutputStream().println(".");
        }
    }

    private void daia(long start, String source, String oid, 
        HttpServletResponse res) throws IOException {

        if (source.equals("ppn")) {
            source = "opac";
        }

        if (oid.startsWith("HEB")) { // DAIA
            oid=oid.substring(3).trim();
        }

        res.setContentType("application/xml");
        if (oid.startsWith("ppn:") && oid.contains("|")) {
            // multi query test : has to be cleaned up
            transporter = crawl.createTransporter("opac");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            storage = crawl.createStorage(baos, "daia");
            int found = transporter.index(oid);
            List<String> ids = transporter.getIdentifiers(0,found);
            for (String id : ids) {
                storage.write(transporter.read(id));
            }
            storage.dispose();
            byte[] bytes = baos.toByteArray();
            log(" multiquery " + baos.size() + " bytes daia");
            res.setContentLength(bytes.length);
            res.getOutputStream().write(bytes);
            baos.close();
            //res.getOutputStream().flush();
        } else {
            if (oid.startsWith("//ppn:")) {
                oid=oid.substring(6).trim();
            } else if (oid.startsWith("ppn:")) {
                oid=oid.substring(4).trim();
            }
            transporter = crawl.createTransporter("opac");
            Resource rc = transporter.read(oid);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            storage = crawl.createStorage(baos, "daia");
            storage.write(rc);
            storage.dispose();
            byte[] bytes = baos.toByteArray();
            baos.close();
            log( baos.size() + " bytes daia [" + oid + "] XML");
            res.setContentLength(bytes.length);
            res.getOutputStream().write(bytes);
            res.getOutputStream().flush();
        }
    }

    private String getEnvironment(HttpServletRequest req) {
        String ret;
        ret  = "Request method: " + req.getMethod() + "\n";
        ret += "Request URI: " + req.getRequestURI() + "\n";
        ret += "Request protocol: " + req.getProtocol() + "\n";
        ret += "Real Path: " + this.getServletContext().getRealPath("/")+"\n";
        ret += "Servlet path: " + req.getServletPath() + "\n";
        ret += "Path info: " + req.getPathInfo() + "\n";
        ret += "Path translated: " + req.getPathTranslated() + "\n";
        ret += "Query string: " + req.getQueryString() + "\n";
        ret += "Content length: " + req.getContentLength() + "\n";
        ret += "Content type: " + req.getContentType() + "\n";
        ret += "Server name: " + req.getServerName() + "\n";
        ret += "Server port: " + req.getServerPort() + "\n";
        ret += "Remote user: " + req.getRemoteUser() + "\n";
        ret += "Remote address: " + req.getRemoteAddr() + "\n";
        ret += "Remote host: " + req.getRemoteHost() + "\n";
        ret += "Authorization scheme: " + req.getAuthType();
        return ret;
    }

    private void log(Exception ex) {
        logger.info(ex.toString());
    }

}
