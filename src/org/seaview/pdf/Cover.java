package org.seaview.pdf;

import org.seaview.opus.DataAnalyzer.Backend;
import org.seaview.pdf.PDFLoader;
import org.shanghai.crawl.MetaCrawl.Analyzer;
import org.shanghai.crawl.TrivialScanner;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.sparql.vocabulary.FOAF;

import java.awt.image.BufferedImage;
//import java.awt.geom.AffineTransform;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import java.io.File;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.util.logging.Logger;
import java.io.IOException;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @title PDF Cover Page Creator
   @date 2016-04-20
 */
public class Cover implements Analyzer {

    private Backend backend;
	private PDFLoader loader;
    private Logger logger = Logger.getLogger(Cover.class.getName());

    /** a backend to write to and possibly a docbase to help the pdf loader */
    public Cover(Backend backend, String docbase) {
        this.backend = backend;
        this.loader = new PDFLoader(docbase);
    }

    @Override
    public Analyzer create() {
        loader.create();
        return this;
    }

    @Override
    public void dispose() {
        loader.dispose();
    }

    @Override
    public String probe() {
        return " " + this.getClass().getName();
    }

    @Override
    public Resource analyze(Resource rc) {
        if (rc==null) {
            log("zero resource");
            return null;
        } 
        String cover = backend.writeCover(rc);
        if (cover==null) {
            loader.analyze(rc);
            PDPage page = loader.getPageOne();
            if (page==null) {
                log("No document no cover");
            } else {
                BufferedImage image = createCover(rc, page);
                if (image!=null) {
                    cover = backend.writeCover(rc, image);
                }
            }
        }
        if (cover==null) {
            log("cover failed " + rc.getURI());
        } else {
            rc.removeAll(FOAF.img);
            rc.addProperty(FOAF.img, cover);
            log("cover " + cover);
        }
        return rc;
    }

    @Override
    public Resource test(Resource rc) {
        backend.test();
        return analyze(rc);
    }
   
    private BufferedImage createCover(Resource rc, PDPage page) {
        BufferedImage image = null;
        PDDocument doc = new PDDocument();
        doc.addPage(page);
        PDFRenderer renderer = new PDFRenderer(doc);
        try {
            image = renderer.renderImageWithDPI(0,96);
            //ImageIO.write(image, "PNG", os);
            //cover = backend.writeCover(rc, image);
            doc.close();
        } catch(IOException e) { log(e); }
        return image;
    }

	protected void log(String msg) {
	    logger.info(msg);
	}

    protected void log(Exception e) {
        log(e.toString());
    }

}
