package org.seaview.opus;

import org.seaview.opus.DataAnalyzer.Backend;

import org.shanghai.util.FileUtil;
import org.shanghai.data.FileStorage;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.DCTypes;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.rdf.model.Resource;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.util.logging.Logger;

/** 
  * @title Filesystem Backend for object storage
  * @date 2016-04-10 
  */
public class FileBackend implements Backend {

    private String store; // storage path
    private Path path;  // storage file
    private boolean test;

    public FileBackend(String store) {
        this.store = store;
    }

    @Override
    public Backend create() {
        if (store==null) {
            throw new AssertionError("zero store");
        }
        path = null;
        return this;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void test() {
        test = true;
    }

    @Override
    public String writeIndex(Resource rc, String url) {
        create(rc);
        Path out = path.resolveSibling("index.html");
        boolean b = Files.isRegularFile(out); 
        if (test) {
            log(" file exists: index.html " + b);
        } else if (b) {
            return rc.getURI();
        } else {
            FileUtil.copy(url, out);
        }
        if (Files.isRegularFile(out)) {
            return rc.getURI();
        } else {
            return null;
        }
    }

    @Override
    public String writeCover(Resource rc) {
        create(rc);
        String cover = cover(rc);
        Path out = path.resolveSibling(cover);
		if (rc.hasProperty(FOAF.img)) {
		    String source = rc.getProperty(FOAF.img).getString();
            if (test) {
                log(" writeCover " + cover(rc, cover) + " " 
                      + out.toAbsolutePath() + " " + source + " " 
                      + Files.isRegularFile(out));
            } else if (Files.isRegularFile(out)) {
                // do not overwrite
			} else {
                FileUtil.copyIfExists(source, out);
			}
		}
        if (Files.isRegularFile(path.resolveSibling(cover))) {
            return cover(rc, cover); 
        }
        return null; // no cover 
    }

    @Override
    public String writeCover(Resource rc, BufferedImage image) {
        create(rc);
        String cover = cover(rc);
        Path out = path.resolveSibling(cover);
        if (test) {
            log("exists: " + out + " " + Files.isRegularFile(out));
        } else if (Files.isRegularFile(out)) {
            // log("exists: " + out);
        } else try {
            OutputStream os = new FileOutputStream(out.toFile());
            ImageIO.write(image, "PNG", os);
        } catch(IOException e) { log(e); }
        if (Files.isRegularFile(out)) {
            return cover(rc, cover); 
        }
        return null; // no cover 
    }

    /** write DCTypes part */
    @Override
    public String writePart(Resource rc, Resource obj) {
        create(rc);
        String uri = obj.getURI();
        if (obj.hasProperty(RDF.type) && obj.getProperty(RDF.type)
               .getResource().getNameSpace().equals(DCTypes.NS)) {
            // should write this part
        } else {
            return null;
        }

        if (test) {
            String t = obj.getProperty(RDF.type).getResource().getLocalName();
            log(" should write " + obj.getURI() + " " + path + " " + t);
        }
        if (obj.hasProperty(DCTerms.format)) {
            String format = obj.getProperty(DCTerms.format).getResource()
                               .getProperty(RDFS.label).getString();
            //log("format " + format);
            String base = null;
            boolean b = false;
            switch(format) {
                case "application/pdf":
                    base = create(uri, "pdf", "pdf");
                    b = writeObject(obj.getURI(), base);
                    break;
                case "video/mp4":
                    base = create(uri, "data", "mp4");
                    b = writeObject(obj.getURI(), base);
                    break;
                case "application/xml":
                    base = create(uri, ".", "xml");
                    b = writeObject(obj.getURI(), base);
                    break;
                case "application/zip":
                    base = create(uri, "data", "zip");
                    b = writeObject(obj.getURI(), base);
                    break;
                default:
                    log("unknown format " + format);
            }
            if (b) {
                uri = rc.getURI() + "/" + base;
            }
        }
        return uri;
    }

    private void create(Resource rc) {
        //log(rc.getURI() + " " + store);
        path = FileStorage.getPath(Paths.get(store), rc);
        if (path.getParent()!=null && !test) {
            FileUtil.mkdir(path.getParent());
        }
    }

    private String cover(Resource rc) {
        String name = rc.getPropertyResourceValue(RDF.type).getLocalName();
        if (name.equals("BibliographicResource")) {
            name = rc.getPropertyResourceValue(DCTerms.type).getLocalName();
        }
		String fname = path.getName(path.getNameCount()-1).toString();
		String cover = "cover.png";
        if (name.startsWith("Journal") && !fname.equals("about.rdf")) {
			cover = fname.substring(0,fname.indexOf(".rdf")) + ".png";
        }
        if (rc.getURI().endsWith(".pdf")) {
            cover = rc.getURI().substring(rc.getURI().lastIndexOf("/")+1);
            cover = cover.substring(0, cover.length()-3) + "png";
        }
        return cover;
    }

    private String cover(Resource rc, String cover) {
        if (rc.getURI().endsWith(".pdf")) {
            cover = rc.getURI().substring(0,rc.getURI().lastIndexOf("/")+1)
                  + cover;
        } else {
            cover = rc.getURI() + "/" + cover;
        }
        return cover;
    }

    /**
        @param uri object resource uri
        @param dir storage directory 
        @param media like pdf xml zip
    */
    private String create(String uri, String dir, String suffix) {
        // filename about.rdf or 1234.rdf
		String fname = path.getName(path.getNameCount()-1).toString();
        String base = uri.substring(uri.lastIndexOf("/")+1);

        if (fname.equals("about.rdf") && dir.equals(".")) {
            return base;
        } else if (fname.equals("about.rdf")) {
            return dir + "/" + base;
        } else if (fname.matches("[0-9]+.rdf")) {
            return fname.substring(0, fname.lastIndexOf(".")+1) + suffix;
        } else {
            return null;
        }
    }

    private boolean writeObject(String uri, String target) {
        Path out = path.resolveSibling(target);
        if (Files.isRegularFile(out)) {
            // log(" exists " + target);
        } else if (test) {
            log(" would write " + out);
        } else {
            FileUtil.mkdir(out.getParent());
            FileUtil.copy(uri, out);
        } 
        return Files.isRegularFile(out);
    }

    private void log(Exception e) {
        logger.info(e.toString());
    }

    private void log(String msg) {
        logger.info(msg);
    }

    private static final Logger logger =
                         Logger.getLogger(FileBackend.class.getName());

}

