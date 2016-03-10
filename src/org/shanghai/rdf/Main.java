package org.shanghai.rdf;

import java.util.logging.Logger;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @title Command Line Interface for the Shanghai RDF Indexer
    @date 2013-03-01
*/
public class Main {
 
    protected Config config;
    private String configFile;
    private Indexer indexer;

    private Main() {
    }
    
    private Main(String s) {
        configFile = s;
    }

    public void dispose() {
		if (config!=null)
		    config.dispose();
    }

    public void create() {
        config = new Config(configFile).create();
    }

    public int index(String[] args) {
        int argc=0;
        if (args.length==0) {
            help();
            return 0;
        } 

        create();
        if (args[0].startsWith("-show")) {
            config.test();
        } else if (args[0].equals("-index")) {
            if (args.length==1) {
                Indexer indexer = new Indexer(config);
                indexer.create();
                indexer.index(); 
		        indexer.dispose();
            } else if (args.length==2) {
                if (args[1].equals("-probe")) {
                    Indexer indexer = new Indexer(config);
                    indexer.create();
                    indexer.probe(); 
		            indexer.dispose();
                } else if (args[1].equals("-test")) {
                    Indexer indexer = new Indexer(config);
                    indexer.create();
                    indexer.test(); 
		            indexer.dispose();
                } else if (args[1].equals("-dump")) {
                    Indexer indexer = new Indexer(config);
                    indexer.create();
                    indexer.dump(); 
		            indexer.dispose();
                } else if (args[1].equals("-clean")) {
                    Indexer indexer = new Indexer(config);
                    indexer.create();
                    indexer.clean(); 
		            indexer.dispose();
                } else if (args[1].equals("-all")) {
                    for (Config.Index idx : config.getIndexList()) {
                        indexer = new Indexer(config,idx.name);
                        indexer.create();
                        indexer.index();
		                indexer.dispose();
                    }
                }
            } else if (args.length==3) {
                if (args[1].equals("-test")) {
                    Indexer indexer = new Indexer(config);
                    indexer.create();
                    indexer.test(args[2]); 
		            indexer.dispose();
                } else if (args[1].equals("-dump")) {
                    Indexer indexer = new Indexer(config);
                    indexer.create();
                    indexer.dump(args[2]); 
		            indexer.dispose();
                } else if (args[1].equals("-index")) {
                    indexer = new Indexer(config, args[2]);
                    indexer.create();
                    indexer.index();
		            indexer.dispose();
                }
            }
        } 
        dispose();
        return 0;
    }

    public static void help() {
        String usage = "   -index [index name] [command]\n"
                     + "          -probe test setup\n"
                     + "          -test  [offset] [limit] record identifiers\n"
                     + "          -dump  [resource] [file] resource dump\n"
                     + "          -post  [resource] to index\n"
                     + "          -del   [resource] remove record from index\n"
                     + "          -destroy : destroy index\n"
                     //+ "           [offset limit] limited build\n"
                     + "";
        //usage += "   -config file.ttl (default is " + configFile + ")\n";
        System.out.print(usage);
    }

    private static final Logger logger = Logger.getLogger(Main.class.getName());
    protected void log() {
        log("Hi!");
    }

    protected void log(String msg) {
        logger.info(msg);
    }

    public static void log(String[] msg) {
        String result = new String();
        for (String str : msg) 
             result += str + " ";
        logger.info(result);
    }

    public static void main(String[] args) {
        Main myself = null;
        if (args.length>1 && args[0].startsWith("-conf")) {
            if (1<args.length) {
                myself = new Main(args[1]);
                args = Config.shorter(Config.shorter(args));
            } 
            System.out.println("configured by " + args[1]);
        } else {
            myself = new Main("lib/metablock.ttl");
        }
        myself.index(args);
    }

}
