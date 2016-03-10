package org.seaview.opus;

import org.shanghai.data.FileTransporter;
import org.shanghai.data.FileScanner;

import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Guide;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.sparql.vocabulary.FOAF;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.io.StringWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.util.Date;
import java.util.logging.Logger;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop 
  @title An Epub Document Metadata extractor
  @date 2014-01-05
*/
public class EpubScanner extends FileScanner
    implements FileTransporter.Delegate {

    private EpubReader epubReader;

    public EpubScanner() {
    }

    @Override
    public FileTransporter.Delegate create() {
        epubReader = new EpubReader();
        return this;
    }

    @Override
    public void dispose() {
        epubReader = null;
	}

    @Override
    public Resource read(String fname) {
        Resource rc = super.read(fname);
        //log("read [" + docbase + "] " + fname + " " + rc.getURI());
        try {
            rc = read(rc, fname);
        }//catch (FileNotFoundException e) { log(e); }
        // catch (IOException e) { log(e); }
        finally {
           return rc;
        }
    }

    private Resource read(Resource rc, String fname) {
        rc.addProperty(DCTerms.format, "Epub");
        rc.addProperty(DCTerms.type, "Book");
        Book book = readBook(Paths.get(fname));

        String title = book.getMetadata().getFirstTitle();
        rc.addProperty(DCTerms.title, title);
        String author = book.getMetadata().getAuthors().get(0).toString();
        if (author.length()>0) {
            String uri = iri + "/" + author.replaceAll("[^A-Za-z]", "");
            Resource person = rc.getModel().createResource(uri, FOAF.Person);
            person.addProperty(FOAF.name, author);
            rc.addProperty(DCTerms.creator, person);
        }
        String cover = fname.substring(0, fname.lastIndexOf(".")) + ".png";
        if (Files.isRegularFile(Paths.get(cover))) {
            // no cover
        } else {
            String img = book.getCoverImage().getHref();
            if (img!=null) try {
                InputStream is = book.getCoverImage().getInputStream();
                BufferedImage image = ImageIO.read(is);
                is.close();
                OutputStream os = Files.newOutputStream(Paths.get(cover));
                ImageIO.write(image, "PNG", os);
                int x = rc.getURI().lastIndexOf(".");
                img = rc.getURI().substring(0, x) + ".png";
                rc = rc.addProperty(FOAF.img, img);
            } catch(IOException e) { log(e); }
        }
        if (Files.isRegularFile(Paths.get(cover))) {
            int x = rc.getURI().lastIndexOf(".");
            String img = rc.getURI().substring(0, x) + ".png";
            if (img.startsWith("http://localhost/")) {
                img = img.substring(17);
            }
            rc = rc.addProperty(FOAF.img, img);
        }
        if (book.getMetadata().getDescriptions().size()>0) {
            String abstract_ = book.getMetadata().getDescriptions().get(0);
            rc.addProperty(DCTerms.abstract_, abstract_);
        }
        if (book.getMetadata().getDates().size()>0) {
            String issued = book.getMetadata().getDates().get(0).getValue();
            rc.addProperty(DCTerms.issued, issued.substring(0,10));
        }
        String language = book.getMetadata().getLanguage();
        rc.addProperty(DCTerms.language, language);
        return rc;
    }

    private Book readBook(Path path) {
        Book book = null;
        try {
            InputStream is = Files.newInputStream(path);
            book = epubReader.readEpub(is);
            is.close();
        } catch(java.lang.RuntimeException e) { log(e); }
          catch(FileNotFoundException e) { log(e); }
          catch(IOException e) { log(e); }
        return book;
    }

    @Override
    public boolean canRead(String file) {
        if (file.endsWith(".epub")) {
            return true;
        }
        return false;
    }

    private static final Logger logger =
                         Logger.getLogger(EpubScanner.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e ) {
        log(e.toString());
        //e.printStackTrace();
    }
}
