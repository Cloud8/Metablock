package org.seaview.data;

import org.seaview.data.AbstractAnalyzer;
import org.seaview.data.Database;

import org.shanghai.rdf.XMLTransformer;
import org.shanghai.oai.URN;
import org.seaview.pdf.PDFLoader;
import org.shanghai.util.FileUtil;
import org.shanghai.rdf.XMLTransformer;
import org.seaview.data.DOI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.DCTerms;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop
  @title Opus Data Analyzer
  @date 2015-07-22
*/
public class OpusAnalyzer extends AbstractAnalyzer {

    private String logbase;
    private String dbhost;
    private String dbase;
    private String dbuser;
    private String dbpass;
    private Database db;
    private Document doc;
    private boolean test = false;
    private boolean write = false;
	private int count;
    private URN urn;
    private String myday;
    private XMLTransformer transformer;
    private boolean hasView;
    private String docbase = "/srv/archiv";

    public OpusAnalyzer(String s, String p, String u, String v, 
        String urnprefix, boolean write /* , String doiprefix */ ) {
	    this.dbhost = s;
	    this.dbase = p;
	    this.dbuser = u;
	    this.dbpass = v;
        urn = new URN(urnprefix);
        this.write = write;
        // doi = new DOI(doiprefix);
    }

    @Override
    public AbstractAnalyzer create() {
        db = new Database(dbhost, dbase, dbuser, dbpass);
        db.create();
		count = 0;
        urn.create();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        myday = sdf.format(Calendar.getInstance().getTime());
        transformer = new XMLTransformer(
                          FileUtil.readResource("/xslt/rdfIndex.xslt"));
        transformer.create();
        hasView = false;
        return this;
    }

    @Override
    public void dispose() {
        urn.dispose();
        transformer.dispose();
    }

    @Override
    public Resource test(Model model, String id) {
        Resource rc = findResource(model, id);
        log("dumb test " + rc.getURI() + " " + id);
        return rc;
    }

    @Override
    public void analyze(Model model, Resource rc, String id) {
        if (rc==null) {
            log("bad model " + id);
            return;
        }
        //log("analyze " + rc.getURI() + " " + id);
        hasDOI = model.createProperty(fabio, "hasDOI");
        if (!rc.hasProperty(hasDOI)) {
            String identifier = DOI.createDoi(rc.getURI());
            //log("should make doi " + identifier);
            //db.update("update statistics set doi='" + identifier
            //           + "', date='" + myday + "'"
            //           + " where uri='" + rc.getURI() + "'");
        }
        if (rc.hasProperty(DCTerms.identifier)) {
            String curn = urn.getUrn(rc.getURI());
            String identifier = rc.getProperty(DCTerms.identifier).getString();
            if (curn.equals(identifier)) {
            } else {
			    log(curn + " # " + identifier);
			}
        } else {
            String identifier = urn.getUrn(rc.getURI());
            log("make urn " + rc.getURI() + " [" + identifier + "] " + id);
            rc.addProperty(DCTerms.identifier, identifier);
            db.update("update statistics set urn='" + identifier
                       + "' where uri='" + rc.getURI() + "'");
        }
        if (write) {
            makeFiles(model, rc, id);
        } else {
	        String uri = rc.getURI();
            Path path = Paths.get(docbase + uri.substring(uri.indexOf("/",7)));
		    try {
		        findFiles(model, rc, id, path);
		    } catch(IOException e) { log(e); }
        }
    }

    @Override
    public void dump(Model model, String resource, String fname) {
        Resource rc = findResource(model, resource);
        if (rc==null) {
            log("dumb dump " + resource + " " + fname);
        } else {
            analyze(model, rc, resource);
        }
    }

    /** copy pdf directory if no pdf exists
        write mets file if not exists and if tif exists
        write cover.png file if not exists 
        write about.rdf index.html
    */
    private void makeFiles(Model model, Resource rc, String id) {
	    String uri = rc.getURI();
        String year = db.getSingleText("select date_year from opus"
		                  + " where source_opus=" + id);
        //log("mk_files " + uri + " " + id + " " + year);
        Path path = Paths.get(docbase + uri.substring(uri.indexOf("/",7)));

        //create pdf directory
        if (!Files.isDirectory(path.resolve("pdf"))) {
            String source = docbase + "/adm/pub/opus/" + year + "/" + id;
            log("cp " + source + " " + path.toString());
            FileUtil.copyFiles(source, path);
        }

        //create viewer file
        if (Files.isDirectory(path.resolve("tif"))) {
			Path mets = path.resolve("mets-" + id + ".xml");
            hasView = true;
            if (!Files.isRegularFile(mets)) {
				log("write to " + mets.toString()); // debug flag
				Viewer viewer = new Viewer("/xslt/rdf2mets.xslt", false);
				viewer.create();
				viewer.analyze(model, rc, path.resolve("tif"), mets);
				viewer.dispose();
            }
        }

        //write cover file
        Path cover = path.resolve("cover.png");
        if (!Files.isRegularFile(cover)) {
            Path test1 = path.resolve("jpg/pre/Image00000.jpg");
            Path test2 = path.resolve("jpg/pre/Image00001.jpg");
            if (Files.isRegularFile(test1)) {
                //FileUtil.copy("file://" + test, target + "/cover.png");
                FileUtil.copy(test1, cover);
            } else if (Files.isRegularFile(test2)) {
                FileUtil.copy(test2, cover);
            } else {
		        try {
                    PDFLoader loader = new PDFLoader(docbase).create();
                    loader.analyze(model, rc, id);
                    if (loader.size>0) {
                        PDPage page = loader.getPageOne();
			            PDDocument doc = new PDDocument();
			            doc.addPage(page);
			            PDFRenderer renderer = new PDFRenderer(doc);
			            BufferedImage image = renderer.renderImageWithDPI(0,96);
			            ImageIO.write(image, "PNG", cover.toFile());
                    }
                    loader.dispose();
			    } catch(IOException e) { log(e); }
            }
        }

		try {
		    findFiles(model, rc, id, path);
		} catch(IOException e) { log(e); }

        FileUtil.write(path.resolve("about.rdf"), transformer.asString(model));
        FileUtil.write(path.resolve("index.html"),transformer.transform(model));
        if (hasView) {
            XMLTransformer view = new XMLTransformer(
                FileUtil.readResource("/xslt/rdfView.xslt"));
            view.create();
            FileUtil.write(path.resolve("view.html"), view.transform(model));
            view.dispose();
            log("wrote " + path.toString() + " about.rdf index.html view.html");
        } else {
		    // crawl from oai to files instead
            Path xmdp = Paths.get(docbase 
                    + "/adm/oai/xmdp/" + year + "/xmdp-" + id + ".xml");
            if (!Files.isRegularFile(xmdp)) {
                XMLTransformer transformer = new XMLTransformer(
                    FileUtil.readResource("/xslt/rdf2epicur.xslt"));
                transformer.create();
                FileUtil.write(xmdp, transformer.transform(model));
                transformer.dispose();
                log("wrote " + xmdp.toString());
            }
        }
        Path epicur = Paths.get(docbase 
                    + "/adm/oai/epicur/" + year + "/epicur-" + id + ".xml");
        if (!Files.isRegularFile(epicur)) {
            XMLTransformer transformer = new XMLTransformer(
                FileUtil.readResource("/xslt/rdf2epicur.xslt"));
            transformer.create();
            FileUtil.write(epicur, transformer.transform(model));
            transformer.dispose();
            log("wrote " + epicur.toString());
        }
    }

    synchronized void findFiles(Model model, Resource rc, String id, Path path) 
	    throws IOException {
		Property prop = model.createProperty(ore, "aggregates");
	    String uri = rc.getURI();
		db.update("delete from files where source_opus="+id);
        DirectoryStream<Path> paths = Files.newDirectoryStream(path);
        for (Path sub : paths) {
            if (Files.isDirectory(sub)) {
			    String name = sub.getFileName().toString();
				if (name.equals("tif")) {
				    String file = "mets-" + id + ".xml";
				    db.update("insert into files (source_opus, file) "
				        + " values (" + id + ",'" + file + "')");
					inject(model, rc, prop, file);
				} else if (name.equals("data")) {
                    for (String file : FileUtil.listFiles(sub, "*.zip")) {
		                db.update("insert into files (source_opus, file) "
			                + " values (" + id + ",'" + file + "')");
						inject(model, rc, prop, file);
                    }
				} else if (name.equals("pdf")) {
                    //log("findFiles " + uri + " " + name);
                    for (String file : FileUtil.listFiles(sub, "*.pdf")) {
                        //log("inject " + file);
		                db.update("insert into files (source_opus, file) "
			                + " values (" + id + ",'" + file + "')");
						inject(model, rc, prop, file);
                    }
				} else if (name.equals("jpg")) {
                    Path test = sub.resolveSibling(Paths.get("tif"));
                    if (Files.isDirectory(test)) {
                        // log("needs viewer");
                    } else {
				        for(String file: FileUtil.listFiles(sub, "*.jpg")) {
				            db.update("insert into files (source_opus, file) "
				            + " values (" + id + ",'" + file + "')");
						    inject(model, rc, prop, file);
                            //log("image " + file);
					    }
					}
				}
            }
        }
        paths.close();
	}

    private void inject(Model model, Resource rc, Property prop, String file) {
	    Resource obj = model.createResource(rc.getURI() +"/" + file);
	    Statement stmt = model.createStatement(rc, prop, obj);
	    model.add(stmt);
	}

    public void statistics(String logbase, String urlbase) {
        File file = new File(logbase);
        try {
            doc = Jsoup.parse(file, "UTF-8", "");
        } catch(IOException e) { log(e); }

        /*
        db.update("create table if not exists statistics ("
        + "source_opus int unique key, stat int, uri varchar(200) primary key,"
        + " doi varchar(40), urn varchar(40), date date)");
        db.update("create table if not exists files ("
          + "source_opus int, file varchar(100), index oid (source_opus))");
        */

        Elements list = doc.select("td[class=aws]");
        if (list==null || list.size()==0) {
            log("Strange : no data found.");
            return;
        }
        for (Element element : list) {
            String url = element.childNode(0).outerHtml();
            //System.out.println(" : " + url);
            Element sib = element.nextElementSibling();
            if (sib==null) {
                continue;
            }
            sib = sib.nextElementSibling();
            if (sib==null) {
                continue;
            }
            if (sib.hasText()) {
                String uri = null;
                String value = sib.text();
                if (value.length()>3) { // TODO believe downloads > 999 ??
                    value = value.substring(0,3);
                }
                if (url.startsWith("/diss/")) {
                    uri = urlbase + url.substring(0,16);
                } else if (url.startsWith("/eb/")) {
                    uri = urlbase + url.substring(0,13);
                }
                if (uri!=null) {
                    if (test) {
                        log(uri + " : [" + value + "]");
                    } else {
					    count++;
						if (count % 100 == 0) {
                            log(uri + " : [" + value + "]");
						}
                        db.update("INSERT INTO statistics (uri, stat) "
                            + "VALUES('" + uri + "'," + value + ") "
                            + "ON DUPLICATE KEY UPDATE stat=VALUES(stat)");
                    }
                }
            }
        }
    }

    private static final Logger logger =
                         Logger.getLogger(OpusAnalyzer.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        logger.info(e.toString());
    }

}
