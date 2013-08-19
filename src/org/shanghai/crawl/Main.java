package org.shanghai.crawl;

import org.shanghai.crawl.Crawl;
//import org.shanghai.rdf.Indexer;

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
public class Main extends org.shanghai.rdf.Main {

    protected Crawl crawl;
    //protected Indexer indexer;

    public Crawl getCrawl(Properties prop) {
        if (crawl==null)
            crawl = new Crawl(prop).create();
        return crawl;
    }

    //public Indexer getIndexer(Properties prop) {
    //    if (indexer==null)
    //        indexer = new Indexer(prop).create();
    //    return indexer;
    //}
   
    public void dispose() {
	    if (crawl!=null)
		    crawl.dispose();
	    if (indexer!=null)
		    indexer.dispose();
    }

    public int make(String[] args) {
        int argc = super.make(args);
        // System.out.println("crawl make # " + argc + " # " + args.length);
        if (argc==args.length)
            return argc;

        if (args.length>argc && args[argc].endsWith("-crawl")) {
            argc++;
            if (args.length==argc) {
                help();
            } else {
                getCrawl(prop).crawl(args);
            }
        } else if (args.length>argc && args[argc].endsWith("-del")) {
            argc++;
            getCrawl(prop).delete(args[argc++]);
        } else if (args.length>argc && args[argc].endsWith("-upd")) {
            argc++;
            getCrawl(prop).update(args[argc++]);
        } else if (args.length>argc && args[argc].endsWith("-put")) {
            argc++;
            getCrawl(prop).add(args[argc++]);
        } else if (args.length>argc && args[argc].endsWith("-get")) {
            argc++;
            getCrawl(prop).read(args[argc++]);
        } else if (args.length>argc && args[argc].endsWith("-destroy")) {
            argc++;
            getCrawl(prop).clean();
        } else if (args.length>argc && args[argc].endsWith("-show")) {
            show();
        } else {
        //    help();
        }
        //System.out.println("crawl Main # " + argc);
        return argc;
    }

    protected void show() {
        if (config!=null)
            config.test();
        else prop.list(System.out);
    }

    protected void help() {
        super.help();
        String usage = "\n" // "java org.shanghai.crawl.Main"
                            // + " -prop [file.properties] \n"
                     + "   -show : show configuration.\n"
                     + "   -crawl [directories]\n"
                 //  + "   -put   [resource] : put a rdf file to store\n"
                 //  + "   -get   [resource] [file]: get resource from store\n"
                 //  + "   -upd   [resource] : update rdf file to the store\n"
                     + "   -del   [resource] : delete resource from store.\n"
                     + "   -destroy : destroy store.\n"
                 //  + "options with brackets are sometimes optional.\n"
					 + "";
        System.out.print(usage);
    }

    public static void main(String[] args) {
	    Main main = new Main();
		main.make(args);
		main.dispose();
    }

}
