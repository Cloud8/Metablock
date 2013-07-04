package org.shanghai.tika;

import org.shanghai.bones.BiblioRecord;
import org.shanghai.bones.BiblioModel;
import org.shanghai.crawl.TDBTransporter;
//import org.shanghai.util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.CharConversionException;
import java.lang.ArrayIndexOutOfBoundsException;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.SimpleDateFormat;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.mbox.MboxParser;
import org.apache.tika.metadata.PropertyTypeException;
import org.xml.sax.SAXException;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ToTextContentHandler;

import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.epub.EpubParser;
import org.xml.sax.ContentHandler;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop <fb.com/goetz.hatop>
  @title A Metadata Scanner for epub Books using Apache Tika
  @date 2012-11-12
*/
public class TEpubScanner extends Scanner implements TDBTransporter.Scanner {

	ByteArrayOutputStream bos;
    Parser parser;
    Metadata meta;
    ContentHandler handler;
    ParseContext context;

    public TEpubScanner(String base) {
        bibModel = new BiblioModel(base);
    }

    public TDBTransporter.Scanner create() {
        bibModel.create();
        parser = new EpubParser();
        meta = new Metadata();
	    bos = new ByteArrayOutputStream(1024);
        // handler = new BodyContentHandler(bos);
        try {
        handler = new ToTextContentHandler(bos, "UTF-8");
        } catch(UnsupportedEncodingException e) { log(e); }
        context = new ParseContext();
        return this;
    }

    @Override
    public void dispose() {
	    if (bos!=null) 
		    try { bos.close(); } catch(IOException e) { log(e); }
	}

    private void reset() {
        dispose();
        create();
    }

    private void log(Exception e) {
        log(e.toString());
    }

    @Override
    public boolean canTalk(File file) {
        if (file.getName().endsWith(".epub")) {
            return true;
        }
        return false;
    }

    @Override
    public BiblioRecord scanFile(File file, BiblioRecord b) {
	    try {
		    // InputStream is = new BufferedInputStream(
            //                  new FileInputStream(file));
            FileInputStream is = new FileInputStream(file);
            parser.parse(is, handler, meta, context);
			is.close();
            talk(b);
		} catch(SAXException e) {log(e);}
		  catch(TikaException e) {log(e);}
		  catch(IOException e) {log(e);}
		  catch(PropertyTypeException e) {}
		  catch(ArrayIndexOutOfBoundsException e) {}
        finally {
          reset();
          return b;
        }
    }

    int x;
    private void talk(BiblioRecord b) {
        b.setFormat("Text (EPub)");
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
	    b.title = meta.get("dc:title");
	    // b.setTopic( meta.get("meta:keyword") );
	    // b.setGenre( meta.get("subject") );
	    String p = meta.get("date");
        if (p!=null && (x=p.indexOf('T'))>0) 
            p= p.substring(0,x);
        b.issued = p;
	    String content = getContent();
        if (content.length()>5000)
	        b.description = content.substring(0,4999);
        else
	        b.description = content;
    }

    private String getContent() {
	    String content = null;
	    //try {
	        // content = new String(bos.toByteArray(), "UTF-8");
            // content = bos.toString("UTF-8");
            content = bos.toString();
            content.trim();
	    //} catch (java.io.UnsupportedEncodingException e) { log(e); }
	    return content;
    }

}
