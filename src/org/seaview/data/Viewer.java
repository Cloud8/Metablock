package org.seaview.data;

import org.shanghai.rdf.Config;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.util.FileUtil;
import org.shanghai.crawl.TrivialScanner;
import org.shanghai.oai.URN;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
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

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Build data structures for DFG image viewer
    @date 2014-07-01
*/
public class Viewer {

    private static final String dct = DCTerms.NS;
    SimpleDateFormat fdin = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat fdout = new SimpleDateFormat("dd.MM.yyyy");

    XMLTransformer transformer;

    Property hasPart;
	Property hasFormat;
	Property identifier;
	Property title;
    Resource Concept;

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
        this.outfile = outfile;
        this.xsltFile = xsltFile;
        this.debug = debug;
    }

    public void dispose() {
        transformer.dispose();
    }

    public void create() {
        if (debug) log("create " + outfile + " from " + fname);
        model = getModel(fname);
		createProperties(model);
		String xslt = FileUtil.read(xsltFile); // rdf2mets.xslt
		transformer = new XMLTransformer(xslt);
        transformer.create();
	}

    public void transform() {
        StringWriter writer = new StringWriter();

		Resource rc = getResource(model, fname);
		rc.removeAll(hasFormat);
        if (debug) log("analyze " + rc.getURI());
		model = analyze(model, rc, fname);

		if (rc.hasProperty(hasFormat)) {
            if (debug) {
                model.write(writer, "RDF/XML-ABBREV");
				String out = FileUtil.removeExtension(outfile) + ".abd";
                FileUtil.write(out, writer.toString());
			}
			String mets = transformer.transform(model);
            FileUtil.write(outfile, mets);
        }
    }

    private Resource checkIdentifier(Resource rc) {
        String id = rc.getProperty(identifier).getLiteral().getString();
        if (id.length()==0) {
            URN urn = new URN("urn:nbn:de:hebis:04-");
            id = urn.getUrn(rc.getURI());
			rc.removeAll(identifier);
		    rc.addProperty(identifier, id);	
        }
        return rc;
    }

    private void createProperties(Model model) {
	    hasFormat = model.createProperty(dct, "hasFormat");
        hasPart = model.createProperty(dct, "hasPart");
		identifier = model.createProperty(dct, "identifier");
	    title = model.createProperty(dct, "title");
        Concept = model.createResource(dct + "FileFormat");
	}

    private Model getModel(String fname) {
        TrivialScanner ts = new TrivialScanner();
        ts.create();
        Model model = ts.read(fname);
        ts.dispose();
        return model;
    }

    private Resource getResource(Model model, String fname) {
		Resource rc = null;
        File file = new File(fname);
        if (file.exists()) {
            String pwd = new File(fname).getAbsolutePath();
            pwd = pwd.substring(0,pwd.lastIndexOf("/"));
            pwd = pwd.substring(pwd.lastIndexOf("/")+1);
            ResIterator ri = model.listResourcesWithProperty(identifier);
            while(ri.hasNext()) {
			    rc = ri.nextResource();
                if (rc.getURI().endsWith(pwd)) {
                    break;
                }
            }
        }
        rc = checkIdentifier(rc);
		return rc;
    }

    private Model analyze(Model model, Resource rc, String fname) {
		model = analyzeDir(model, rc, ".");
	    return model;
	}

    /* analyze subdirectories named tif or made of regexp */
    private Model analyzeDir(Model model, Resource rc, String directory) {
        Seq dseq = model.createSeq();
        String regex = "^[0-9][0-9a-z]+$";
		File dir = new File(directory);
        if (dir.isDirectory()) {
            for (File sub : dir.listFiles()) {
			    if (sub.getName().equals("tif")) {
                    //log("found " + sub.getName());
		            Resource obj = sequence(model, rc, sub, ".tif");
                    rc.addProperty(hasFormat, obj);
				} else if (sub.getName().matches(regex)) {
                    Resource obj = sequence(model, rc, sub, ".tif");
                    dseq.add(obj);
                }
			}
	    }
        if (dseq.size()>0) {
            rc.addProperty(hasFormat, dseq);
        }
	    return model;
	}

    private Resource sequence(Model model, Resource rc, File dir, String suff) {
        String regex = "^[0-9][0-9a-z]+$";
        String uri = rc.getURI() + "/" + dir.getName();
        Seq seq = model.createSeq();
        boolean hasFiles = false;
		Resource obj = model.createResource(uri, Concept);

        File[] files = dir.listFiles();
        Arrays.sort(files);
        for (File sub : files) {
            if (sub.isDirectory() && sub.getName().matches(regex)) {
				Resource dobj = sequence(model, obj, sub, suff);
                seq.add(dobj);
            } else if (sub.isFile()) {
                int x = sub.getName().lastIndexOf(".");
                if (x>0) {
                    String suffix = sub.getName().substring(x);
                    if (suff.contains(suffix)) {
                        hasFiles = true;
				        seq.add( dir.getName() + "/" + sub.getName() );	
                    }
                }
            }
        }

        if (!hasFiles) {
            if (debug) log("sequence " + seq.size() + " : " + uri);
            return seq;
        }

		if (seq.size()>0) {
            if (debug) log("sequence " + seq.size() + " : " + uri);
            String tit = dir.getName();
            try {
                File meta = new File(dir, "meta.txt");
                if (meta.exists()) {
                    String check = FileUtil.read(meta);
                    int x = check.indexOf("title:")+6;
                    if (x>5) {
                        int y = check.indexOf(System.lineSeparator());
                        tit = check.substring(x,y);
                    }
                } else if (tit.length()==8) {
                    tit = fdout.format(fdin.parse(dir.getName()));
                } 
            } catch(ParseException e) { log(e); }
			if (tit.equals("tif")) {
            } else {
                obj.addProperty(title, tit);
            }
			obj.addProperty(hasPart, seq);
        }
        return obj;
    }

    private static final Logger logger =
                         Logger.getLogger(Viewer.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception e) {
        log(e.toString());
    }

}

