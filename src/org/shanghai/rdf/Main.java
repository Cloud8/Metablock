package org.shanghai.rdf;

import org.shanghai.util.FileUtil;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A Command Line Interface for the Shanghai RDF Indexer
   @date 2013-03-01
   @abstract Main : Indexer : RDFCrawl : RDFTransporter : RDFReader : ModelTalk
*/
public class Main {
 
    protected Properties prop;
    protected Indexer indexer;
    protected Config config;

    protected Indexer getIndexer() {
        if (config==null)
            return getIndexer(prop);
        return getIndexer(config);
    }

    protected Indexer getIndexer(Properties prop) {
        if (indexer==null)
            indexer = new Indexer(prop).create();
		return indexer;
    }

    protected Indexer getIndexer(Config config) {
        if (indexer==null)
            indexer = new Indexer(config).create();
		return indexer;
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.make(args);
        main.dispose();
    }

    private void create() {
    }

    protected int make(String[] args) {
        int argc=0;

        if (args.length>argc && args[argc].endsWith("-prop")) {
		    argc++;
            try {
                System.out.println("loading " + args[argc]);
                prop = new Properties();
                prop.load(new FileReader(args[argc++]));
            } catch(IOException e) { e.printStackTrace(); }
        } else {
             config = new Config("/shanghai.ttl").create();
             if (config==null) { //old school
                 if (prop==null) try {
                     prop = new Properties();
                     prop.load(
                     Main.class.getResourceAsStream("/shanghai.properties"));
                 } catch(IOException e) { e.printStackTrace(); }
             } else {
                 prop = config.getProperties();
             }
        }

        if (args.length>argc && args[argc].endsWith("-clean")) {
            argc++;
            getIndexer().cleanSolr();
            return args.length;
        }

        if (args.length>argc && args[argc].startsWith("-index")) {
            if (args[argc].equals("-index")) {
                indexer = getIndexer(config);
            } else {
                Config.Index idx = config.getIndex((args[argc].substring(1)));
                prop = idx.getProperties();
                indexer = getIndexer(prop);
		        argc++;
                System.out.println("index " + idx.name);
            }
        } 

        if (args.length-1>argc && args[argc].endsWith("-test")) {
		    argc++;
			getIndexer().test(args[argc++]);
        } else if (args.length>argc && args[argc].endsWith("-test")) {
		    argc++;
			getIndexer().test();
        } else if (args.length>argc && args[argc].endsWith("-probe")) {
		    argc++;
			getIndexer().probe();
        } else if (args.length>argc && args[argc].endsWith("-dump")) {
		    argc++;
            if (args.length-1>argc) {
                silence();
                getIndexer().dump(args[argc++], args[argc++]);
            } else if (args.length>argc) {
                silence();
                getIndexer().dump(args[argc++]);
            } else {
                getIndexer().dump();
            }
        } else if (args.length-1>argc && args[argc].endsWith("-post")) {
		    argc++;
            getIndexer().post(args[argc++]);
        } else if (args.length>argc && args[argc].startsWith("-h")) {
		    argc++;
            help();
        } else if (args.length>argc && args[argc].startsWith("-index")) {
            //System.out.println("# " + args[argc++] + " " + args[argc++]);
		    argc++;
            if (args.length-1>argc)
                indexer.index(args[argc++],args[argc++]);
            else if (args.length>argc)
                indexer.index(args[argc++]);
            else 
                indexer.index();
        }
        System.out.println("rdf make # " + args.length + " # " + argc);
        //if (args.length>argc && args[argc].startsWith("-")) 
        //    return argc;
        return argc;
    }

    private void dispose() {
		if (indexer!=null)
		    indexer.dispose();
    }

    protected void silence() {
        Logger.getLogger("org.shanghai.rdf.ModelTalk").setLevel(Level.OFF);
        Logger.getLogger("org.shanghai.jena.TDBReader").setLevel(Level.OFF);
    }

    protected void help() {
        String usage = "java org.shanghai.rdf.Main"
                     + " -prop [file.properties] \n"
                     + "   -probe \n"
                     + "   -test  [offset]\n"
                     + "   -dump  [resource] [file]: resource dump\n"
                 //  + "   -post  [resource] : post a resource to solr\n"
                     + "   -index [offset] [limit] : build index.\n"
                     + "   -clean : destroy index.\n"
                     //+ "options with brackets are optional.\n"
                     + "";
        System.out.print(usage);
    }
}
