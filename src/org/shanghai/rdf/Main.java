package org.shanghai.rdf;

import org.shanghai.crawl.FileUtil;

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
*/
public class Main {

    public static void main(String[] args) {
        int argc=0;

        Properties prop = new Properties();
        boolean b = false;
        if (args.length>argc && args[argc].endsWith("-prop")) {
		    argc++;
            try {
              System.out.println("loading " + args[argc]);
              prop.load(new FileReader(args[argc]));
              b=true;
            } catch(IOException e) { e.printStackTrace(); }
		    argc++;
        }
        if (!b) try {
            prop.load(Main.class.getResourceAsStream("/shanghai.properties"));
        } catch(IOException e) { e.printStackTrace(); }

        Indexer myself = new Indexer(prop);

        if (args.length>argc && args[argc].endsWith("-clean")) {
            myself.cleanSolr();
            return;
        }

        myself.create();
        if (args.length-1>argc && args[argc].endsWith("-test")) {
		    argc++;
			myself.test(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-test")) {
            silence();
			myself.test();
        } else if (args.length>argc && args[argc].endsWith("-dump")) {
            if (args.length-2>argc) {
		        argc++;
                myself.dump(args[argc], args[argc+1]);
            } else if (args.length-1>argc) {
                silence();
		        argc++;
                myself.dump(args[argc]);
            } else {
                myself.dump();
            }
        } else if (args.length-1>argc && args[argc].endsWith("-post")) {
		    argc++;
            myself.post(args[argc]);
        } else if (args.length-2>argc && args[argc].endsWith("-index")) {
		    argc++;
            myself.index(args[argc++], args[argc]);
        } else if (args.length-1>argc && args[argc].endsWith("-index")) {
		    argc++;
            myself.index(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-index")) {
            myself.index();
        } else {
            talk();
        }
		myself.dispose();
    }

    private static void silence() {
        Logger.getLogger("org.shanghai.rdf.ModelTalk")
              .setLevel(Level.OFF);
        Logger.getLogger("org.shanghai.jena.TDBReader")
              .setLevel(Level.OFF);
    }

    private static void talk() {
        String usage = "java org.shanghai.rdf.Main"
                     + " -prop [file.properties] \n"
                     + "   -test [offset]\n"
                     + "   -dump [resource] [file]: resource dump\n"
                     + "   -post [resource] : post a resource to solr\n"
                     + "   -index [offset] [limit] : build index.\n"
                     + "   -clean : destroy index.\n"
                     + "options with brackets area optional.\n";
        System.out.print(usage);
    }
}
