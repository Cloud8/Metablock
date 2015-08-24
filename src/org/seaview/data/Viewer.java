package org.seaview.data;

import org.shanghai.rdf.Config;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.util.FileUtil;
import org.shanghai.crawl.TrivialScanner;
import org.shanghai.oai.URN;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.DirectoryStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Logger;
import java.util.Arrays;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Build data structures for DFG image viewer
    @date 2015-07-01
*/
public class Viewer {

    private static final String dct = DCTerms.NS;
    SimpleDateFormat fdin = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat fdout = new SimpleDateFormat("dd.MM.yyyy");

    XMLTransformer transformer;

    private boolean debug = false;

    private String fname;
    private String outfile;
    private String xsltFile;
    private Model  model;
    private int count=0;

    /* directory structure :
       tif / Images.tif
       tif / 12345 / Images.tif
    */
    public Viewer(String fname, String outfile, String xsltFile, boolean debug) 
    {
        this.fname = fname;
        model = getModel(fname);
        this.outfile = outfile;
        this.xsltFile = xsltFile;
        this.debug = debug;
    }

    /** used by OpusAnalyzer */
    public Viewer(String xsltFile, boolean debug) 
    {
        this.xsltFile = xsltFile;
        this.debug = debug;
    }

    public void dispose() {
        transformer.dispose();
    }

    public void create() {
		String xslt = FileUtil.readResource(xsltFile); // rdf2mets.xslt
		transformer = new XMLTransformer(xslt);
        transformer.create();
	}

    public void transform() {
        StringWriter writer = new StringWriter();

		Resource rc = findResource(model, fname);
		rc.removeAll(DCTerms.hasFormat);
        log("analyze " + rc.getURI());
		model = analyze(model, rc);

		if (rc.hasProperty(DCTerms.hasFormat)) {
            if (debug) {
                model.write(writer, "RDF/XML-ABBREV");
				String out = FileUtil.removeExtension(outfile) + ".abd";
                FileUtil.write(out, writer.toString());
			}
			String mets = transformer.transform(model);
            FileUtil.write(outfile, mets);
        }
    }
  
    public void analyze(Model model, Resource rc, Path path, Path out) {
        log("analyze " + path.toString());
		rc.removeAll(DCTerms.hasFormat);
		Resource obj = sequence(model, rc, path, ".tif");
		if (obj.hasProperty(DCTerms.hasPart)) {
            rc.addProperty(DCTerms.hasFormat, obj);
            if (debug) {
                StringWriter writer = new StringWriter();
                model.write(writer, "RDF/XML-ABBREV");
                Path check = path.getParent().resolve("about.abd");
                log("wrote # " + check.toString());
                FileUtil.write(check, writer.toString());
			}
		    String mets = transformer.transform(model);
            log("wrote ## " + out.toString());
            FileUtil.write(out, mets);
        }
    }

    private Model getModel(String fname) {
        TrivialScanner ts = new TrivialScanner();
        ts.create();
        Model model = ts.read(fname);
        ts.dispose();
        return model;
    }

    private Resource findResource(Model model, String fname) {
		Resource rc = null;
        Path path = Paths.get(fname);
        if (Files.isReadable(path)) {
            String pwd = path.toAbsolutePath()
                             .getParent().getFileName().toString();
            log(fname + " : " + pwd);
            ResIterator ri = model.listResourcesWithProperty(RDF.type);
            while(ri.hasNext()) {
			    rc = ri.nextResource();
                if (rc.getURI().endsWith(pwd)) {
                    break;
                }
            }
        }
		return rc;
    }

    private Model analyze(Model model, Resource rc) {
        Path path = Paths.get(".");
		model = analyze(model, rc, path);
	    return model;
	}

    /* analyze subdirectories named tif or made of regexp */
    private Model analyze(Model model, Resource rc, Path path) {
		try {
            if (Files.isDirectory(path)) {
                for (Path sub : Files.newDirectoryStream(path)) {
			        if (sub.getFileName().toString().equals("tif")) {
                        log("found " + sub.getFileName().toString());
		                Resource obj = sequence(model, rc, sub, ".tif");
                        rc.addProperty(DCTerms.hasFormat, obj);
				    } 
			    }
	        }
		} catch(IOException e) { log(e); }
	    return model;
	}

    private Resource sequence(Model model, Resource rc, Path dir, String suff) {
        String regex = "^[0-9][0-9a-z]+$";
        String uri = rc.getURI() + "/" + dir.getFileName();
        Seq seq = model.createSeq();
        boolean hasFiles = false;
		Resource obj = model.createResource(uri, DCTerms.FileFormat);

		try {
		    DirectoryStream<Path> paths = Files.newDirectoryStream(dir);
            List<Path> dirs = new ArrayList<>();
            for (Path sub : paths) {
			    dirs.add(sub);
			}
            Collections.sort(dirs);
            for (Path sub : dirs) {
                if (Files.isDirectory(sub) && sub.getFileName().toString().matches(regex)) {
				    Resource dobj = sequence(model, obj, sub, suff);
                    seq.add(dobj);
                } else if (Files.isRegularFile(sub)) {
                    int x = sub.getFileName().toString().lastIndexOf(".");
                    if (x>0) {
                        String suffix = sub.getFileName().toString().substring(x);
                        if (suff.contains(suffix)) {
                            hasFiles = true;
				            seq.add( dir.getFileName() + "/" + sub.getFileName() );	
                        }
                    }
                }
            }
		} catch(IOException e) { log(e); }

        if (!hasFiles) {
            log("sequence " + seq.size() + " : " + uri);
            return seq;
        }

		if (seq.size()>0) {
            log("sequence " + seq.size() + " : " + uri);
            String title = dir.getFileName().toString();
            try {
                Path meta = dir.resolve("meta.txt");
                if (Files.isRegularFile(meta)) {
                    String check = FileUtil.read(meta);
                    int x = check.indexOf("title:")+6;
                    if (x>5) {
                        int y = check.indexOf(System.lineSeparator());
                        title = check.substring(x,y);
                    }
                } else if (title.length()==8) {
                    title = fdout.format(fdin.parse(dir.getFileName().toString()));
                } 
            } catch(ParseException e) { log(e); }
			if (title.equals("tif")) {
            } else {
                obj.addProperty(DCTerms.title, title);
            }
			obj.addProperty(DCTerms.hasPart, seq);
        }
        return obj;
    }

    private static final Logger logger =
                         Logger.getLogger(Viewer.class.getName());

    private void log(String msg) {
        if (debug) logger.info(msg);
    }

    private void log(Exception e) {
        logger.severe(e.toString());
    }

}

