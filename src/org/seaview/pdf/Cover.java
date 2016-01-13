package org.seaview.pdf;

import org.shanghai.crawl.MetaCrawl.Analyzer;
import org.seaview.pdf.PDFLoader;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.vocabulary.DCTerms;

import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

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
   @author Goetz Hatop 
   @title PDF Cover Page
   @date 2013-03-25
 */
public class Cover implements Analyzer {

	public static final long serialVersionUID = 50L;
    private static final String foaf = "http://xmlns.com/foaf/0.1/";
    private Logger logger = Logger.getLogger(Cover.class.getName());
    private String cache;
    private String path;
    private Property img;
	private PDFLoader loader;

    public Cover(PDFLoader loader) {
        this.loader = loader;
    }

    @Override
    public Analyzer create() {
        //loader.create();
        return this;
    }

    @Override
    public void dispose() {
        //loader.dispose();
    }

    @Override
    public String probe() {
        return " " + this.getClass().getName();
    }

    @Override
    public Resource analyze(Resource rc) {
        loader.analyze(rc);
        String path = loader.getPath(rc, ".pdf");
        if (path!=null && path.endsWith(".pdf")) {
            if (rc.hasProperty(DCTerms.hasPart)) {
				path = path.substring(0, path.lastIndexOf("/"));
				path = path.substring(0, path.lastIndexOf("/")+1)+"cover.png";
            } else {
                path = path.substring(0, path.lastIndexOf(".pdf")) + ".png";
            }
        }
        makeCover(rc, path);
        return rc;
    }

    @Override
    public Resource test(Resource rc) {
        return analyze(rc);
    }
   
    private void makeCover(Resource rc, String fname) {
	    makeCover(fname);
        img = rc.getModel().createProperty(foaf, "img");
        if (rc.hasProperty(img)) {
            return;
        } else if (fname.startsWith(System.getProperty("user.home"))) {
            fname = fname.substring(System.getProperty("user.home").length()+1);
            rc.addProperty(img, fname);
        } else {
            rc.addProperty(img, fname);
        }
	}

    private void makeCover(String fname) {
        if (Files.isRegularFile(Paths.get(fname))) {
            //log("cover exists " + fname);
        } else {
            //log("make cover " + fname);
            PDPage page = loader.getPageOne();
            PDDocument doc = new PDDocument();
            doc.addPage(page);
            PDFRenderer renderer = new PDFRenderer(doc);
            try {
                BufferedImage image = renderer.renderImageWithDPI(0,96);
                ImageIO.write(image, "PNG", new File(fname));
                doc.close();
            } catch(IOException e) { log(e); }
        }
    }

    public static void main(String[] args) {
        if (args.length==2) {
            PDFLoader pl = new PDFLoader();
            pl.load(args[0]);
            Cover cover = new Cover(pl);
			cover.makeCover(args[1]);
            pl.dispose();
        } else {
		    System.out.println("no argument ?");
        }
    }

	protected void log(String msg) {
	    logger.info(msg);
	}

    protected void log(Exception e) {
        log(e.toString());
    }

}
