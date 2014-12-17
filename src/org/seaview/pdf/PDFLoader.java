package org.seaview.pdf;

import org.seaview.nlp.AbstractAnalyzer;
import org.shanghai.util.FileUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.DCTerms;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.Splitter;
import org.apache.pdfbox.pdfwriter.COSWriter;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

public class PDFLoader extends AbstractAnalyzer {

    private static final String dct = DCTerms.getURI();
    private PDDocument document;
    private String url;
    private static final String scratchFile = "data/temp.pdf";
    public int size;

    public PDFLoader create() {
        size = 0;
        document = new PDDocument();
        return this;
    }

    public void dispose() {
        if (document==null) {
            return ;
        }
        try { document.close(); }
        catch (IOException e) { log(e); }
        if (scratchFile!=null) {
            //new File(scratchFile).delete(); 
        }
        document = null;
    }

    public void analyze(Model model, Resource rc, String id) {
        //log("analyze " + rc.getURI() + " [" + id + "]");
        Property relation = model.createProperty(DCTerms.getURI(), "relation");
        if (rc.hasProperty(relation)) {
            url = rc.getProperty(relation).getString();
        }
    }

    public String maltreat(int start, double tail) {
        try {
            //url = "/srv/archiv/"+url.substring(url.indexOf("/", 8));
            //document = PDDocument.load(url);
            document = PDDocument.load(new URL(url));

            size = document.getNumberOfPages();
            if (tail < 1.0 && size>28) {
                int count = (int) (tail * size);
                log("removing " + (size-start) + " pages");
                for (int i=start; i<count; i++) {
                    document.removePage(start);
                }
            }
            if (scratchFile!=null) {
                write(scratchFile, document);
            }
        } catch (FileNotFoundException e) { log(e); }
          catch (IOException e) { log(e); }
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
}
