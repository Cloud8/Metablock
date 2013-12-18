package org.shanghai.crawl;

import org.shanghai.rdf.Config;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Command Line Interface for the Main RDF Crawler
    @date 2013-03-01
*/
public class Main {

    protected Config config;
    protected Crawl crawl;
    protected String configFile = "shanghai.ttl";

    public void dispose() {
	    if (crawl!=null)
		    crawl.dispose();
        crawl=null;
	    if (config!=null)
            config.dispose();
        config=null;
    }

    public void create() {
        //System.out.println(" # crawl create");
        config = new Config(configFile).create();
        crawl = new Crawl(config);
        crawl.create();
    }

    public int make(String[] args) {
        if (args.length==0)
            return help();

        if (args[0].endsWith("-crawl")) {
            if (1<args.length) {
                //System.out.println(" # crawl " + args[0] + " " + args[1]);
                create();
                crawl.crawl(args);
                dispose();
            } else help();
        }
        return 0;
    }

    protected int help() {
        String usage = "\n" 
               + "   -crawl [directories] to storage\n"
               + "          -test [directory] test files\n"
               + "          -dump [resource] out of store\n"
               + "          -post [resource] rdf file to storage\n"
               + "          -del [resource] delete from storage\n"
               + "          -destroy : destroy storage.\n"
			   + "\n";
        System.out.print(usage);
        return 0;
    }

    public static void main(String[] args) {
        Main myself = new Main();
        myself.make(args);
    }

}
