package org.shanghai.oai;

import org.shanghai.util.FileUtil;
import org.shanghai.rdf.Config;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.jena.TDBWriter;

import com.lyncode.xoai.serviceprovider.HarvesterManager;
import com.lyncode.xoai.serviceprovider.configuration.Configuration;
import com.lyncode.xoai.serviceprovider.data.Record;
import com.lyncode.xoai.serviceprovider.exceptions.HarvestException;
import com.lyncode.xoai.serviceprovider.iterators.RecordIterator;
import com.lyncode.xoai.serviceprovider.iterators.RecordIterator;
import com.lyncode.xoai.serviceprovider.verbs.GetRecord;
import com.lyncode.xoai.serviceprovider.verbs.Parameters;
import com.lyncode.xoai.serviceprovider.exceptions.InternalHarvestException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.List;

public class OAIImporter {

    private Config config;
    private TDBWriter tdbWriter;
    private List<Config.OAI> oais;

    public OAIImporter(Config config) {
        this.config = config;
    }

    public OAIImporter create() {
        tdbWriter = new TDBWriter(
                    config.getProperties().getProperty("crawl.store"),
                    config.getProperties().getProperty("crawl.graph"));
        oais = config.getOAIList();
        tdbWriter.create();
        return this;
    }

    public void dispose() {
       tdbWriter.dispose();
    }

    public void test() {
        show();
        for (int i=0; i<oais.size(); i++) {
            Config.OAI oai = oais.get(i);
            Importer imp = new Importer(oai, tdbWriter);
            imp.create();
            imp.test();
            imp.dispose();
        }
    }

    public void show() {
        for (int i=0; i<oais.size(); i++) {
             System.out.print("OAI " + i + ": ");
             oais.get(i).show();
        }
        System.out.println();
    }

    public void make() {
        for (int i=0; i<oais.size(); i++) {
            Config.OAI oai = oais.get(i);
            Importer imp = new Importer(oai, tdbWriter);
            imp.create();
            imp.make();
            imp.dispose();
        }
    }
}

