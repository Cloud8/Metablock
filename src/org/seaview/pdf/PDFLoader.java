package org.seaview.pdf;

import org.shanghai.crawl.MetaCrawl.Analyzer;
import org.seaview.pdf.HyphenRemover;

import org.shanghai.util.FileUtil;
import org.shanghai.util.TextUtil;
import org.shanghai.util.ModelUtil;
import org.shanghai.data.FileStorage;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.shared.PropertyNotFoundException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import javax.activation.FileDataSource;
import javax.xml.transform.TransformerException;
import org.apache.jempbox.xmp.XMPMetadata;
//import org.apache.jempbox.impl.XMLUtil;
import org.apache.jempbox.xmp.pdfa.XMPSchemaPDFAId;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.jempbox.xmp.XMPSchemaPDF;
import org.apache.jempbox.xmp.XMPSchemaBasic;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.net.URL;
import java.net.MalformedURLException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.logging.Logger;

public class PDFLoader implements Analyzer {

    private PDDocument document;
    private static final String scratchFile = 
                         System.getProperty("java.io.tmpdir") + "/seaview.pdf";
    public int size;
    private String docbase;
    private SimpleDateFormat sdf; 

    public PDFLoader() {
    }

    public PDFLoader(String docbase) {
        this.docbase = docbase;
    }

    @Override
    public Analyzer create() {
        size = 0;
        document = null;
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        return this;
    }

    @Override
    public void dispose() {
        if (document==null) {
            return ;
        }
        Path path = Paths.get(scratchFile);
        try { 
		    document.close(); 
            if (Files.exists(path)) {
                Files.delete(path);
            }
		} catch (IOException e) { log(e); }
        document = null;
    }

    @Override
    public String probe() {
        return " " + docbase;
    }

    @Override
    public Resource analyze(Resource rc) {
        dispose(); // close document
        Path path = getPath(docbase, rc, ".pdf");
        if (Files.isRegularFile(path) && path.toString().endsWith(".pdf")) {
            log("load path " + path);
            try {
                document = PDDocument.load(Files.newInputStream(path));
            } catch (IOException e) { log(e); }
        } else {
            String url = getPath(rc, ".pdf");
            log("load url " + url);
            try {
                URL curl = new URL(url);
                document = PDDocument.load(curl.openStream());
            } catch (IOException e) { log(e); }
        }
        size = document.getNumberOfPages();
        if (docbase==null) { 
            // nothing
        } else if (docbase.startsWith("files:") && docbase.endsWith(".pdf")) {
            log("Unsupported: write to [" + docbase + "] ?");
            // save(rc, docbase.substring(6));
        }
        return rc;
    }

    @Override
    public Resource test(Resource rc) {
        Path path = getPath(docbase, rc, ".pdf");
        if (Files.isRegularFile(path) && path.toString().endsWith(".pdf")) {
            log("would load " + path);
        } else {
            String url = getPath(rc, ".pdf");
            log("load url " + url);
        }
		return rc;
    }

    public void load(String filename) {
        Path path = Paths.get(filename);
        if (Files.isRegularFile(path)) {
            try {
                document = PDDocument.load(Files.newInputStream(path));
            } catch (IOException e) { log(e); }
        }
    }

    public boolean failed() {
        if (document==null) {
            return true;
        }
        return false;
    }

    public String maltreat(int start, double tail) {
        try {
            size = document.getNumberOfPages();
            if (tail < 1.0 && size>33) {
                int count = (int) (tail * size);
                //log("removing " + (size-start) + " pages");
                for (int i=start; i<count; i++) {
                    document.removePage(start);
                }
            }
            if (scratchFile!=null) {
                write(scratchFile, document);
            }
        } catch (IllegalArgumentException e) { log(e); }
        return scratchFile;
    }

    public InputStream createInputStream() {
        InputStream is = null;
        try {
            if (scratchFile!=null) {
                is = new FileInputStream(scratchFile);
            } else {
                is = new PDStream(document).createInputStream();
            }
        } catch (IOException e) { log(e); }
        finally {
            return is;
        }
    }
  
    public String fulltext(Resource rc) {
        return fulltext(rc, true);
    }

    private String fulltext(Resource rc, boolean cache) {
        int start = 0;
        String path = getPath(rc, ".pdf");
        if (path==null) {
            return null;
        }
        int x = path.lastIndexOf(".");
		path = x>0?path.substring(0, x) + ".txt" : path;

        if (cache && x>0 && Files.isRegularFile(Paths.get(path))) {
            //log("exists " + path);
            String text = FileUtil.read(path);
            if (text.length()==0) {
                return null;
            }
            return text;
        }

        String text = null;
        if (document==null) {
            analyze(rc);
        }
        try {
            int end = document.getNumberOfPages();
            log("extract " + end + " pages " + path);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setForceParsing( true );
            //stripper.setSortByPosition( true );
            stripper.setShouldSeparateByBeads( true );
            stripper.setStartPage(start);
            stripper.setEndPage(end);
            stripper.setAddMoreFormatting(true);
            StringWriter writer = new StringWriter();
            stripper.writeText(document, writer);
            text = writer.toString();
            text = TextUtil.clean(text);    
            text = HyphenRemover.dehyphenate(text, rc.getURI());
            text = TextUtil.sentence(text); // one sentence per line
            log("write " + path);
            FileUtil.write(path, text);
        } catch (MalformedURLException e) {
            log(e);
        } catch (IOException e) {
            log(e);
            e.printStackTrace();
        } finally { 
            return text;
        }
    }

    public PDPage getPageOne() {
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDPageTree root = catalog.getPages();
        if (root.getCount() == 0) {
            log("no pages found.");
            return null;
        }
        return root.get(0);
    }

    private void write(String filename, PDDocument document) {
        FileOutputStream output = null;
        COSWriter writer = null;
        try
        {
            output = new FileOutputStream(filename);
            writer = new COSWriter(output);
            writer.write(document);
        } catch ( IOException e ) { log(e); }
        finally
        {
            if( output != null )
            {
                try {
                    output.close();
                    output = null;
                } catch (IOException e) { log(e); }
            }
            if( writer != null )
            {
                try {
                    writer.close();
                    writer = null;
                } catch (IOException e) { log(e); }
            }
        }
    }

    private void decrypt() {
        if (document.isEncrypted()) {
            log("document.isEncrypted");
            //DecryptionMaterial dm = new StandardDecryptionMaterial("");
            //document.openProtection(dm); //GH201502 : gone ?
            document.setAllSecurityToBeRemoved(true);
        }
    }

    /** write resource description to document catalog */
    private void save(Resource rc, String outfile) {
        decrypt();
        PDDocumentInformation info = document.getDocumentInformation();

        String ctitle = info.getTitle();
        String dtitle = rc.getProperty(DCTerms.title).getString();

        double score = TextUtil.similarity(ctitle, dtitle);
        log("titel score " + score);
        if (score<0.9) {
            info.setTitle(dtitle);
        }

        String cauthor = info.getAuthor();
        if (cauthor == null ) {
            cauthor = info.getCreator();
        }
        Seq seq = rc.getProperty(DCTerms.creator).getSeq();

        StringBuilder builder = new StringBuilder();
        if (seq!=null) try {
            if (seq.size()==0) {
			    Resource rn = rc.getProperty(DCTerms.creator).getResource();
                String name = rn.getProperty(FOAF.name).getString();
                builder.append(name);
            }
            for (int i=1; i<=seq.size(); i++) {
                Resource rn = seq.getResource(i);
                Statement stmt = rn.getProperty(FOAF.name);
                if (stmt!=null) {
                    String name = seq.getResource(i)
                                     .getProperty(FOAF.name).getString();
                    builder.append(name);
                    if (i>1 && i<seq.size()-1) {
                        builder.append(" ; ");
                    }
                }
            }
        } catch (PropertyNotFoundException e) { log(e); }
        finally {}
        info.setAuthor(builder.toString());
        //log("title: " + dtitle);
        //log("authors: " + builder.toString());
        builder.setLength(0);

        if (rc.hasProperty(DCTerms.subject)) {
			StmtIterator si = rc.listProperties(DCTerms.subject);
            String subject = null;
            while (si.hasNext()) {
			    Statement stmt = si.next();
				if (stmt.getObject().isLiteral()) {
                    if (builder.length()>0) {
                        builder.append(" ; ");
                    }
                    builder.append(stmt.getString());
				} else if (stmt.getObject().isResource()) {
                    Resource obj = stmt.getResource();
                    if (obj.hasProperty(RDFS.label)) {
                        String term = obj.getProperty(RDFS.label).getString();
                        if (builder.length()>0) {
                            builder.append(" ; ");
                        }
                        builder.append(term);
                    } else if (obj.hasProperty(SKOS.prefLabel)) {
                        StmtIterator ssi = obj.listProperties(SKOS.prefLabel);
                        while (ssi.hasNext()) {
                            if (subject==null) {
                                subject = ssi.next().getString();
                            } else {
                                subject += " ; " + ssi.next().getString();
                            }
                        }
                    }
                }
            }
			if (builder.length()>0) {
                info.setKeywords(builder.toString());
			}
			if (subject!=null) {
                info.setSubject(subject);
			}
            builder.setLength(0);
        }

        if (rc.hasProperty(DCTerms.issued)) {
            String iss = rc.getProperty(DCTerms.issued).getString();
            Calendar date = Calendar.getInstance();
            try {
                date.setTime(sdf.parse(iss));
                info.setCreationDate(date);
            } catch (ParseException e) { log(e); }
        }

        info.setProducer("Seaview 1.1");
        info.setCreator((String)null);

        try { // PDF/A requires XMP Metadata
            PDDocumentCatalog catalog = document.getDocumentCatalog();
            PDMetadata metadata = new PDMetadata(document);
            //PDMetadata metadata = catalog.getMetadata();
            catalog.setMetadata(metadata);
            XMPMetadata xmp = new XMPMetadata();
            //XMPMetadata xmp = new XMPMetadata(
            //    XMLUtil.parse(metadata.createInputStream()));
            XMPSchemaPDFAId pdfaid = new XMPSchemaPDFAId(xmp);
            xmp.addSchema(pdfaid);
            pdfaid.setConformance("B");
            pdfaid.setPart(1);
            pdfaid.setAbout("");

            XMPSchemaDublinCore dc = xmp.addDublinCoreSchema();
            dc.setTitle(info.getTitle());
            dc.addCreator(info.getAuthor());
            dc.addSubject(info.getSubject());
            dc.setDescription(info.getSubject());
            //dc.addContributor("Contributor");
            //dc.setCoverage("coverage");
            //dc.addLanguage("language");
            //dc.setCoverage("coverage");
            //dc.setFormat("format");
            XMPSchemaPDF pdf = xmp.addPDFSchema();
            pdf.setKeywords(info.getKeywords());
            pdf.setProducer(info.getProducer());
            XMPSchemaBasic basic = xmp.addBasicSchema();
            basic.setCreateDate(info.getCreationDate());
            basic.setModifyDate(info.getModificationDate());

            metadata.importXMPMetadata(xmp.asByteArray());
        } catch (IOException e) { log(e); }
          catch (TransformerException e) { log(e); }
        save(outfile);
    }

    private void save(String outfile) {
        try {
            int size = document.getDocumentCatalog().getPages().getCount();
            //log("ctitle " + ctitle + " cauthor " + cauthor + " size " + size);
            log(" size " + size + " bytes write to " + outfile);
            document.save(outfile);
        } catch (IOException e) { log(e); }
    }

    public String getTitle() {
        return document.getDocumentInformation().getTitle();
    }

    public String getAuthor() {
        return document.getDocumentInformation().getAuthor();
    }

    public String getDate() {
        Calendar date = document.getDocumentInformation().getCreationDate();
        if (date==null) {
            return null;
        }
        return sdf.format(date.getTime());
    }

    public ByteArrayOutputStream getBaos() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            document.save(baos);
        } catch(IOException e) { log(e); }
        return baos;
    }

    public boolean valid() {
        //decrypt();
        if (document==null) {
            return false;
        }
        if (document.isEncrypted()) {
            return false;
        }
        return true;
    }

    private Path getPath(String docbase, Resource rc, String suffix) {
        String path = this.getPath(rc, suffix);
        if (path==null) {
            return null;
        }
        return Paths.get(path);
    }

    /** used by Cover */
    public String getPath(Resource rc, String suffix) {
        String path = getPath(rc);
        if (path==null) {
            log("no path " + rc.getURI());
        }
        int x = path.lastIndexOf(".");
        path = x>0?path.substring(0,x)+suffix:path;
        return path;
    }

    public String getPath(Resource rc) {
        StmtIterator si = rc.listProperties(DCTerms.hasPart);
        String path = null;
        while( si.hasNext() ) {
            RDFNode node = si.nextStatement().getObject();
            if (node==null) {
                // log("zero " + path);
            } else if (node.isLiteral() && node.toString().equals("")) {
                // log("empty " + path);
            } else if (node.isResource()) {
				return getPath(node.asResource());
            } else {
				throw new AssertionError("strange format");
            }
        }

        path = rc.getURI();
        if (path.startsWith("files://")) {
            return path.substring(8);
        }
        if (path.startsWith("http://localhost/")) {
            path = path.substring(17);
        }
        if (docbase!=null && path.indexOf("/", 9)>0) {
            String test = docbase + path.substring(path.indexOf("/", 9));
            if (Files.isReadable(Paths.get(test))) {
                return test;
            }
        }
        if (path.startsWith("http://")) {
            return path;
        }
        if (Files.isReadable(Paths.get(path))) {
            return path;
        }
        path = System.getProperty("user.home") + "/" + path;
        if (Files.isReadable(Paths.get(path))) {
            return path;
        }
        return null;
    }

    private boolean validate(String fname) throws IOException {
        ValidationResult result = null;
        FileDataSource fd = new FileDataSource(fname);
        PreflightParser parser = new PreflightParser(fd);
        try {
            parser.parse();
            /* Once the syntax validation is done, 
             * the parser can provide a PreflightDocument 
             * (that inherits from PDDocument) 
             * This document process the end of PDF/A validation.
             */
            PreflightDocument document = parser.getPreflightDocument();
            document.validate();
            // Get validation result
            result = document.getResult();
            document.close();
        } catch (SyntaxValidationException e) {
            /* the parse method can throw a SyntaxValidationException 
             * if the PDF file can't be parsed.
             * In this case, the exception contains a ValidationResult  
             */
            result = e.getResult();
        }
        if (result.isValid()) {
            // display validation result
            System.out.println("The file " + fname + " is valid PDF/A-1b");
            return true;
        } else {
            System.out.println("File " + fname + " is not valid, error(s) :");
            for (ValidationError error : result.getErrorsList()) {
                System.out.println(error.getErrorCode() + " : " + error.getDetails());
            }
            return false;
        }
    }

    public static void main(String[] args) {
        PDFLoader pl = new PDFLoader();
        pl.create();
        if (args.length==1 && args[0].endsWith(".pdf")) {
            try {
                pl.validate(args[0]);
            } catch (IOException e) { pl.log(e); }
        } else if (args.length==2 && args[0].endsWith(".pdf")) {
            pl.load(args[0]);
            pl.decrypt();
            pl.save(args[1]);
        } else if (args.length==2 && args[0].endsWith(".rdf")) {
            Resource rc = ModelUtil.read(args[0]);
            if (Files.exists(Paths.get(args[1])) && args[1].endsWith(".pdf")) {
                Path old = Paths.get(args[1]);
                String bak = FileUtil.basename(args[1], ".pdf") + ".old";
                FileUtil.copy(old, old.resolveSibling(bak));
                //try {
                //    Files.move(old, old.resolveSibling(bak));
                //} catch(IOException e) { pl.log(e); }
                pl.load(args[1]);
            } else {
                pl.analyze(rc);
            }
            pl.save(rc, args[1]);
        } else if (args.length==3 && args[0].endsWith(".rdf")) {
            Resource rc = ModelUtil.read(args[0]);
            pl.load(args[1]);
            pl.save(rc, args[2]);
        } else {
            System.out.println("no argument ?");
        }
        pl.dispose();
    }

    protected static Logger logger =
                         Logger.getLogger(PDFLoader.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        //e.printStackTrace();
        logger.info(e.toString());
    }

}
