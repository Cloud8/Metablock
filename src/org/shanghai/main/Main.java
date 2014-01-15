package org.shanghai.main;

import org.shanghai.rdf.Config;
import org.shanghai.rdf.Indexer;
import org.shanghai.data.Crawl;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop
   @title Command Line Interface for the Shanghai Indexer
   @date 2013-09-05
*/
public class Main {

    public static void main(String[] args) {

        String configFile = "lib/shanghai.ttl";
        if (args.length>1 && args[0].startsWith("-conf")) {
            if (1<args.length) {
                configFile = args[1];
                args = Config.shorter(Config.shorter(args));
            } 
            System.out.println("configured by " + configFile);
        } 

        if (args.length==0) {
            org.shanghai.rdf.Main.help();
        } else if (args[0].startsWith("-show")) {
            Config config = new Config(configFile).create();
            config.test();
        } else if (args[0].startsWith("-index")) {
            Config config = new Config(configFile).create();
            Indexer indexer;
            if (args.length>1&&args[1].startsWith("index")) {
                indexer = new Indexer(config,args[1]);
                args = Config.shorter(args);
            } else {
                indexer = new Indexer(config);
            }
            indexer.create();
            if (args.length==1) {
                indexer.index(); 
            } else if (args.length==2) {
                if (args[1].equals("-probe")) {
                    indexer.probe(); 
                } else if (args[1].equals("-test")) {
                    indexer.test(); 
                } else if (args[1].equals("-dump")) {
                    if (args.length==3)
                        indexer.dump(args[2]); 
                    else indexer.dump(); 
                } else if (args[1].equals("-clean")) {
                    indexer.clean(); 
                }
            }
		    indexer.dispose();
        } else if (args[0].startsWith("-crawl")) {
            if (args.length==1) {
                help();
            } else if (args.length==2) {
                Engine engine = new Engine(configFile);
                engine.create();
                if (args[1].equals("-probe")) {
                    engine.probe(); 
                } else if (args[1].equals("-test")) {
                    engine.test(); 
                } else if (args[1].equals("-dump")) {
                    engine.dump(); 
                } else if (args[1].equals("-clean")) {
                    engine.clean(); 
                } else {
                    engine.crawl(args[1]);
                }
                engine.dispose();
            } else if (args.length==3) {
                Engine engine = new Engine(configFile);
                engine.create();
                if (args[1].equals("-dump")) {
                    engine.dump(args[2]); 
                } else if (args[1].equals("-test")) {
                    engine.test(args[2]); 
                } else if (args[1].equals("-post")) {
                    engine.post(args[2]); 
                } else if (args[1].equals("-del")) {
                    engine.delete(args[2]); 
                } else {
                    engine.crawl(args[1], args[2]);
                }
                engine.dispose();
            } else { 
                Engine engine = new Engine(configFile);
                engine.create();
                engine.crawl(args);
                engine.dispose();
            }
        } else if (args[0].startsWith("-sync")) {
            Engine engine = new Engine(configFile);
            engine.create();
            if (args.length==1) {
                engine.sync();
            } else if (args.length==2) {
                engine.sync(args[1]);
            } else if (args.length==3) {
                engine.sync(args[1], args[2]);
            } else if (args.length==4) {
                engine.sync(args[1], args[2], args[3]);
            }
            engine.dispose();
        }
    }

    private static int help() {
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

}
