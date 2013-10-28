package org.shanghai.rdf;

import java.util.logging.Logger;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title A Command Line Interface for the Shanghai RDF Indexer
    @date 2013-03-01
*/
public class Main {
 
    protected Config config;
    private String configFile;
    private Indexer indexer;

    private Main() {
        configFile = "lib/shanghai.ttl";
    }
    
    public Main(String s) {
        configFile = s;
    }

    public void dispose() {
		if (config!=null)
		    config.dispose();
    }

    public void create() {
        config = new Config(configFile).create();
    }

    public int make(String[] args) {
        int argc=0;
        if (args.length==0) {
            return help();
        } 

        create();
        if (args[0].startsWith("-show")) {
            config.test();
        } if (args[0].startsWith("-index")) {
            String [] cmd = shorter(args);
            if (args.length>1 && !args[1].startsWith("-")) {
                cmd = shorter(cmd);
                indexer = new Indexer(config, args[1]);
                indexer.create();
                indexer.index(cmd);
		        indexer.dispose();
            } else if (args.length>1 && args[1].startsWith("-all")) {
                cmd = shorter(cmd);
                for (Config.Index idx : config.getIndexList()) {
                    indexer = new Indexer(config,idx.name);
                    indexer.create();
                    indexer.index(cmd);
		            indexer.dispose();
                }
            } else {
                indexer = new Indexer(config);
                indexer.create();
                indexer.index(cmd);
		        indexer.dispose();
            }
        } else {
            indexer = new Indexer(config);
            indexer.create();
            indexer.index(args);
		    indexer.dispose();
        }
        dispose();
        return 0;
    }

    protected int help() {
        String usage = "java org.shanghai.rdf.Main\n"
                     + "   -config file.ttl (default is lib/shanghai.ttl)\n"
                     + "   -index [index name] [command]\n"
                     + "          -probe test setup\n"
                     + "          -test  [offset] [limit] record identifiers\n"
                     + "          -dump  [resource] [file] resource dump\n"
                     + "          -post  [resource] to index\n"
                     + "          -del   [resource] remove record from index\n"
                     + "          -destroy : destroy index\n"
                     + "           [offset limit] limited build\n"
                     + "";
        System.out.print(usage);
        return 0;
    }

    private static final Logger logger = Logger.getLogger(Main.class.getName());
    protected void log() {
        log("Hi!");
    }

    protected void log(String msg) {
        logger.info(msg);
    }

    private static String[] shorter(String[] dirs) {
        String[] result = new String[dirs.length-1];
        for (int i=0; i<dirs.length-1; i++)
             result[i] = dirs[i+1];
        return result;
    }

    public static void log(String[] msg) {
        String result = new String();
        for (String str : msg) 
             result += str + " ";
        logger.info(result);
    }

    public static void main(String[] args) {
        Main myself = new Main();
        myself.make(args);
    }
}
