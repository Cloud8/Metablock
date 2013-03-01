package org.shanghai.crawl;

import org.shanghai.crawl.Crawl;
import org.shanghai.rdf.Indexer;

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
   @abstract Instruments the FileCrawl Class with a FileTransporter
             and starts it according to the command line settings.
             FileCrawl : FileTransporter : TDBTransporter : TDBReader
*/
public class Main {

    public static void main(String[] args) {
        int argc=0;

        Crawl crawl = null; // perform as crawler
        Indexer indexer = null; // be an indexer

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

        if (args.length>argc && args[argc].endsWith("-clean")) {
            //indexer.cleanSolr();
            new Indexer(prop).cleanSolr();
            return;
        }

        /** crawl commands */
        if (args.length>argc && args[argc].endsWith("-crawl")) {
            argc++;
            if (args.length==argc) 
                talkMore();
            else {
                crawl = new Crawl(prop).create();
                crawl.crawl(args);
            }
        } else if (args.length>argc && args[argc].endsWith("-del")) {
            crawl = new Crawl(prop).create();
            argc++;
            crawl.delete(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-upd")) {
            crawl = new Crawl(prop).create();
            argc++;
            crawl.update(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-get")) {
            crawl = new Crawl(prop).create();
            argc++;
            crawl.read(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-remove")) {
            crawl = new Crawl(prop).create();
            crawl.clean();
        } else

        /** index commands */
        if (args.length-1>argc && args[argc].endsWith("-test")) {
		    argc++;
            indexer = new Indexer(prop).create();
			indexer.test(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-test")) {
            silence();
            indexer = new Indexer(prop).create();
			indexer.test();
        } else if (args.length>argc && args[argc].endsWith("-dump")) {
            indexer = new Indexer(prop).create();
            if (args.length-2>argc) {
		        argc++;
                indexer.dump(args[argc], args[argc+1]);
            } else if (args.length-1>argc) {
		        argc++;
                silence();
                indexer.dump(args[argc]);
            } else {
                indexer.dump();
            }
        } else if (args.length-1>argc && args[argc].endsWith("-post")) {
		    argc++;
            indexer = new Indexer(prop).create();
            indexer.post(args[argc]);
        } else if (args.length-2>argc && args[argc].endsWith("-index")) {
		    argc++;
            indexer = new Indexer(prop).create();
            indexer.index(args[argc++], args[argc]);
        } else if (args.length-1>argc && args[argc].endsWith("-index")) {
		    argc++;
            indexer = new Indexer(prop).create();
            indexer.index(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-index")) {
            indexer = new Indexer(prop).create();
            indexer.index();
        } else {
            talk();
        }
        if (crawl!=null)
            crawl.dispose();
        if (indexer!=null)
            indexer.dispose();
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

    private static void talkMore() {
        String usage = "java org.shanghai.rdf.Main"
                     + " -prop [file.properties] \n"
                     + "   -crawl [directories]\n"
                     + "   -put [resource] : put a rdf file to store\n"
                     + "   -get [resource] [file]: get a resource from store\n"
                     + "   -upd [resource] : update a rdf file into the store\n"
                     + "   -del [resource] : delete resource from store.\n"
                     + "   -remove : destroy store.\n"
                     + "options with brackets area sometimes optional.\n";
        System.out.print(usage);
    }
}
