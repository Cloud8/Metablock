package org.shanghai.oai;

import org.shanghai.rdf.Config;
import org.shanghai.crawl.FileStorage;
import org.shanghai.oai.OAITransporter;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.hp.hpl.jena.rdf.model.Model;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @title Test OAI Transporter
   @date 2014-05-31
*/
public class Test {

    private Config config;
    private String configFile = "seaview.ttl";
    private OAITransporter transporter;

    public void create() {
        config = new Config(configFile).create();
        transporter = new OAITransporter(config.getOAIList().get(0), false);
        transporter.create();
    }

    public void dispose() {
        transporter.dispose();
    }

    public void test() {
        String probe = transporter.probe();
        System.out.println("OAI probe: " + probe);

        String identifiers[] = transporter.getIdentifiers(0,4);
        String test = null;
        for (String id : identifiers) {
            if (test==null)
                test = id;
            System.out.println("OAI: " + id);
        }
        System.out.println();

        transporter.test();
        //if (test!=null) {
        //    Model model = transporter.read(test);
        //    if (model!=null) {
        //        model.write(System.out);
        //    }
        //}
        // transporter.make();
    }

    protected int help() {
        String usage = "\t  -oai -probe : check setup\n"
                     + "\t       -test : test some records\n"
                     + "\t       -crawl : start harvesting\n"
                     + "\t  -urn [urn:http:infile] [outfile] : make urns\n"
                     + "\n";
        System.out.print(usage);
        return 0;
    }

    public static void main(String[] args) {
        Test myself = new Test();
        myself.create();
        myself.test();
        myself.dispose();
    }

}
