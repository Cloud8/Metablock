package org.shanghai.crawl;

import org.shanghai.util.FileUtil;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.data.FileTransporter;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop
   @title Scanner for RDF data files
   @date 2013-02-21
*/
public class TrivialScanner implements FileTransporter.Delegate {

    private XMLTransformer tr;
    private String suffix;

    public TrivialScanner(String suffix) {
        this.suffix = suffix;
    }

    public FileTransporter.Delegate create() {
        tr = new XMLTransformer();
        tr.create();
        return this;
    }

    public void dispose() {
        tr.dispose();
    }

    public boolean canRead(String fname) {
        for(String suffix : this.suffix.split(" ")) {
            // if (fname.endsWith(".rdf") || fname.endsWith(".xml")) {
            if (fname.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public Resource read(String file) {
        String rdf = FileUtil.read(file);
        return tr.asResource(rdf); 
    }
}
