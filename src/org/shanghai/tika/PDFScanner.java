package org.shanghai.tika;

import org.shanghai.bones.BiblioRecord;
import org.shanghai.bones.BiblioModel;
import org.shanghai.crawl.TDBTransporter;
//import org.shanghai.pdf.CoverPdf;
//import org.shanghai.nlp.Summariser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.CharConversionException;
import java.lang.ArrayIndexOutOfBoundsException;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import java.util.Date;
import java.util.logging.Logger;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.mbox.MboxParser;
import org.apache.tika.metadata.PropertyTypeException;
import org.xml.sax.SAXException;
import org.apache.tika.sax.BodyContentHandler;

import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.xml.sax.ContentHandler;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop <fb.com/goetz.hatop>
  @title A PDF Document Scanner
  @date 2012-10-23
*/
public class PDFScanner extends Scanner implements TDBTransporter.Scanner {

    private final static CharsetEncoder ENCODER = 
                         Charset.forName("UTF-8").newEncoder();

	ByteArrayOutputStream bos;
    Parser parser;
    Metadata meta;
    ContentHandler handler;
    ParseContext context;
    private String cache;
    //private CoverPdf coverPdf;
    //private Summariser summarizer;
    private String base;

    public PDFScanner(String b, String c) {
        this.base = b;
        this.cache = c;
    }

    @Override
    public TDBTransporter.Scanner create() {
        bibModel = new BiblioModel(base);
        bibModel.create();
        parser = new PDFParser();
        meta = new Metadata();
        //handler = new DefaultHandler();
	    bos = new ByteArrayOutputStream();
        handler = new BodyContentHandler(bos);
        context = new ParseContext();
        //coverPdf = new CoverPdf(cache);
        //coverPdf.create();
        //summarizer = new Summariser();
        //summarizer.create();
        return this;
    }

    public void log(Exception e) {
        log(e.toString());
    }

    @Override
    public void dispose() {
	    if (bos!=null) 
		    try { bos.close(); } catch(IOException e) { log(e); }
        //coverPdf.dispose();
        //summarizer.dispose();
	}

    private void reset() {
        dispose();
        create();
    }

    @Override
    public BiblioRecord scanFile(File file, BiblioRecord b) {
        // System.out.println("scanFile " + file.getPath());
		try {
		    InputStream is = new BufferedInputStream(new FileInputStream(file));
            parser.parse(is, handler, meta, context);
            talk(b);
		} catch(SAXException e) {log(e);}
		  catch(IOException e) {log(e);}
		  catch(TikaException e) {log(e);}
		  catch(PropertyTypeException e) {}
		  catch(ArrayIndexOutOfBoundsException e) {}
        finally {
          reset();
          return b;
        }
	}

    /** id url : set by parent */
    private void talk(BiblioRecord b) {
        int x;
        b.setFormat("Text (PDF)");
		b.author = meta.get("dc:creator");
		b.setAuthor_additional(meta.get("dc:creator"));
        if (b.author==null) 
	        b.author = meta.get("creator");
        if (b.author==null) 
	        b.author = meta.get("Author");
        if (b.author!=null && (x=b.author.indexOf("<"))>0 )
            b.author = b.author.substring(0, x).trim();
        if (b.author!= null)
            b.author = b.author.replace('\"',' ');
        if (b.author!=null && (x=b.author.lastIndexOf("="))>0 )
            b.author = b.author.substring(x+1).trim();
        if (b.author!=null && b.author.trim().length()<1)
            b.author=null;
	    b.title = meta.get("dc:title");
	    b.setTopic( meta.get("meta:keyword") );
	    b.setTopic( meta.get("subject") );
        b.spelling = getContent();
        b.description = b.spelling.substring(0,600);
        //b = summarizer.talk(b);
	    String p = meta.get("date");
        if (p!=null && (x=p.indexOf('T'))>0) 
            p= p.substring(0,x);
        b.issued = p;
        //b.thumbnail = base + "/page" + coverPdf.getCover(b.getFileUrl());
        // log("url: " + b.getUrl());
        if (b.modified!=null) {
            b.setPublishDate( b.modified );
        }
    }

    private String getContent() {
	    String content = null;
	    try {
	        content = new String(bos.toByteArray(), "UTF-8");
            content.trim();
	    } catch (java.io.UnsupportedEncodingException e) { log(e); }
	    return content;
    }

    /** GH2013-03-03 : TODO : Tika detects nothing */
    public boolean canTalk(File file) {
        if (file.getName().endsWith(".pdf"))
            return true;
        /****
	    String type = super.getMimeType(file);
        if (type==null) {
            // log("zero type: " + file.getName());
            return false;
        }
		if ("application/pdf".equals( type )) {
            // log("type pdf: " + file.getName());
            return true;
        }
        // log("strange type: " + file.getName());
        ***/
        return false;
    }

    private String getCover() {
        return "May be later";
    }
}
