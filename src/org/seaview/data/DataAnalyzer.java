package org.seaview.data;

import org.shanghai.crawl.MetaCrawl.Analyzer;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.util.FileUtil;
import org.seaview.data.AbstractAnalyzer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;

import java.util.logging.Logger;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop
  @title (Really simple-dumb) Data analysis
  @date 2015-07-01
*/
public class DataAnalyzer extends AbstractAnalyzer {

    private XMLTransformer transformer;

    public DataAnalyzer() {
    }

    public DataAnalyzer(String xsltFile) {
        String xslt = FileUtil.read(xsltFile);
        transformer = new XMLTransformer(xslt); 
    }

    public AbstractAnalyzer create() {
        if (transformer==null) {
            return this;
        }
        transformer.create();
        return this;
    }

    public void dispose() {
        //log("data analyze disposed.");
        if (transformer!=null) {
            transformer.dispose();
            transformer = null;
        }
    }

    /** model does not support transactions, so recreate all statements */
    public void analyze(Model model, Resource rc, String id) {
        if (rc==null) {
            log("data analyze with zero resource [" + id + "]");
            return;
        }
        //log("data analyze " + rc.getURI() + " [" + id + "]");
        //model.begin();
        //String rdf = transformer.transform(model);
        //Model newModel = transformer.asModel(rdf);
        //model.removeAll();
        //model = model.add(newModel);
        //model.commit();
        //log(model);
    }

    @Override
    public Resource test(Model model, String id) {
        Resource rc = findResource(model, id);
        log("test " + rc.getURI());
        //model.write(System.out, "RDF/JSON");
        return rc;
    }

    private static final Logger logger =
                         Logger.getLogger(DataAnalyzer.class.getName());

    protected void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        logger.info(e.toString());
    }

    protected void log(Model mod) {
        mod.write(System.out, "RDF/XML");
    }

}
