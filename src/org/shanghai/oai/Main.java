package org.shanghai.oai;

import org.shanghai.rdf.Config;
import org.shanghai.oai.Importer;
import org.shanghai.oai.URN;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A Command Line Interface for the Shanghai OAI Importer
   @date 2013-03-15
*/
public class Main extends org.shanghai.crawl.Main {

    @Override
    public void create() {
        super.create();
    }

    public void createThis() {
        //System.out.println("oai create");
        config = new Config(configFile).create();
        crawl = new Crawl(config);
        crawl.create();
    }

    private Main() {
        configFile = "lib/shanghai.ttl";
    }
    
    public Main(String s) {
        configFile = s;
    }

    @Override
    public int make(String[] args) {
        if (args.length==0) 
            return help();
        if (args.length<2) 
            return super.make(args);

        //System.out.println("# oai " + args[0] + " " + args[1]);
        if (args[1].startsWith("-urn")) {
            config = new Config(configFile).create();
            String prefix = config.get("oai.urnPrefix");
            URN urn = new URN(prefix);
            urn.create();
            if(args.length==4)
                urn.make(args[2], args[3]);
            else if(args.length==3)
                urn.make(args[2]);
            urn.dispose();
        } else if (args[1].startsWith("-oai")) {
            System.out.println("OAI module");
            if (args.length==2) {
                help();
            } else {
                createThis(); 
                crawl.crawl(shorter(args));
                dispose(); 
            }
        } else {
            super.make(args);
        }
        return 0;
    }

    protected int help() {
        super.help();
        String usage = "\t  -oai -probe : check setup\n"
                     + "\t       -test : test some records\n"
                     + "\t       -crawl : start harvesting\n"
                     + "\t  -urn [urn:http:infile] [outfile] : make urns\n"
                     + "\n";
        System.out.print(usage);
        return 0;
    }

    public static String[] shorter(String[] dirs) {
        String[] result = new String[dirs.length-1];
        for (int i=0; i<dirs.length-1; i++)
             result[i] = dirs[i+1];
        return result;
    }

    public static void main(String[] args) {
        Main myself = new Main();
        myself.make(args);
    }

}
