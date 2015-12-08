package org.shanghai.crawl;

import org.shanghai.rdf.Config;
import org.shanghai.rdf.Indexer;
import org.shanghai.ojs.URN;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Command Line Interface for the Main RDF Crawler
    @date 2013-03-01
*/
public class Main {

    protected Config config;
    protected Crawl crawl;
    protected static String configFile = "seaview.ttl";

    public void dispose() {
	    if (crawl!=null)
		    crawl.dispose();
        crawl=null;
	    if (config!=null)
            config.dispose();
        config=null;
    }

    public void create() {
        config = new Config(configFile).create();
        crawl = new Crawl(config);
        crawl.create();
    }

    protected static int help() {
        //org.shanghai.rdf.Main.help();
        String usage = "\n" 
               + "   -crawl -s source -t target -e engine\n"
               // [directories|oai] to storage\n"
               + "          -probe check setup\n"
               + "          -test [resource] test resource\n"
               // + "          -dump [resource] out of store\n"
               + "          -post [resource] to storage\n"
               + "          -del [resource] from storage\n"
               + "          -destroy : destroy storage.\n"
			   + "\n";
        System.out.print(usage);
        return 0;
    }

    public void make(String[] args) {
        //for (String s : args) System.out.print(s + " ");
        //System.out.println(" ["+args.length+"]");
        if (args.length==0) {
            help();
            return;
        }

        if (args[0].startsWith("-help")) {
            help();
            return;
        }

        if (args[0].startsWith("-index")) {
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
                    indexer.dump();
                } else if (args[1].equals("-clean")) {
                    indexer.clean();
                } else if (args[1].equals("-destroy")) {
                    indexer.clean();
                } else if (args[1].equals("-help")) {
                    org.shanghai.rdf.Main.help();
                }
            } else if (args.length==3) {
                if (args[1].equals("-test")) {
                    indexer.test(args[2]);
                } else if (args[1].equals("-dump")) {
                    indexer.dump(args[2]);
                } else if (args[1].equals("-post")) {
                    indexer.post(args[2]);
                } else if (args[1].equals("-del")) {
                    indexer.remove(args[2]);
                } else { // index from to
                    indexer.index(args[1],args[2]);
                }
            } else if (args.length==4) {
                if (args[1].equals("-dump")) {
                    indexer.dump(args[2],args[3]);
                }
            }
            indexer.dispose();
            return;
        }

        if (args[0].startsWith("-urn")) {
            Config config = new Config(configFile).create();
            URN engine = new URN(config.get("schema.urn"));
            if (args.length==2) {
                engine.create();
                engine.make(args[1]);
                engine.dispose();
            }
            return;
        }

        if (args[0].startsWith("-show")) {
            Config config = new Config(configFile).create();
            config.test();
        }

        if (args[0].startsWith("-crawl")) {
            if (args.length==1) {
                this.create();
                this.crawl.crawl();
                this.dispose();
            } else if (args.length==2 && args[1].startsWith("-")) {
                this.create();
                if (args[1].equals("-probe")) {
                    this.crawl.probe();
                } else if (args[1].equals("-test")) {
                    this.crawl.test();
                } else if (args[1].equals("-dump")) {
                    this.crawl.dump();
                } else if (args[1].equals("-clean")) {
                    this.crawl.clean();
                } else if (args[1].equals("-destroy")) {
                    this.crawl.clean();
                } else if (args[1].equals("-help")) {
                    help();
                }
                this.dispose();
            } else if (args.length==3 && args[1].startsWith("-")) {
                this.create();
                if (args[1].equals("-dump")) {
                    this.crawl.dump(args[2]);
                } else if (args[1].equals("-test")) {
                    this.crawl.test(args[2]);
                } else if (args[1].equals("-post")) {
                    this.crawl.post(args[2]);
                } else if (args[1].equals("-del")) {
                    this.crawl.delete(args[2]);
                } else {
                    this.crawl.crawl(args);
                }
                this.dispose();
            } else if (args.length==4 && args[1].startsWith("-")) {
                this.create();
                if (args[1].equals("-dump")) {
                    this.crawl.dump(args[2], args[3]);
                } else if (args[1].equals("-test")) {
                    this.crawl.test(args[2], args[3]);
                } else {
                    this.crawl.crawl(args);
                }
                this.dispose();
            } else {
                this.create();
                this.crawl.crawl(args);
                this.dispose();
            }
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.make(args);
    }
}
