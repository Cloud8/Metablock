package org.shanghai.oai;

import org.shanghai.rdf.Config;
import org.shanghai.crawl.MetaCrawl;
import java.util.logging.Logger;
import java.util.List;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop 
   @title Simple OAI Crawler 
   @date 2013-10-20
*/
public class OAICrawl extends org.shanghai.data.Crawl {

    private List<Config.OAI> oais;

    public OAICrawl(Config config) {
        super(config);
    }

    @Override
    public void create() {
        //oais = config.getOAIList();
        //Note : created below for faster testing
        //createStorage(config.get("oai.store"));
        //createTransporter("oai");
        //crawler = new Harvester(transporter,storage);
        //crawler.create();
    }

    /** Note: storage injected into importer, no MetaCrawl involved */
    public void crawl(String[] directories) {
        if (directories.length>1)
            crawl(directories[1]);
        else crawl(config.get("oai.store"));
    }

    public void crawl(String target) {
        oais = config.getOAIList();
        boolean probe = false;
        boolean test = false;
        if (target.startsWith("-probe"))
            probe = true;
        if (target.startsWith("-test"))
            test = true;
        if (!test&&!probe)
            createStorage(target);
        for (int i=0; i<oais.size(); i++) {
            Config.OAI oai = oais.get(i);
            Importer imp = new Importer(oai, storage);
            imp.create();
            if (test)
                imp.test();
            else if (probe)
                log(imp.probe());
            else imp.make();
            imp.dispose();
        }
    }

    private static final Logger logger =
                         Logger.getLogger(OAICrawl.class.getName());

    protected void log(String msg) {
        //System.out.println(msg);
        logger.info(msg);
    }

}

