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

    protected Indexer getIndexer(Properties prop) {
        if (indexer==null)
            indexer = new Indexer(prop).create();
		return indexer;
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.make(args);
        main.dispose();
    }

    protected int make(String[] args) {
        int argc=0;

        prop = new Properties();
        boolean b = false;
        if (args.length>argc && args[argc].endsWith("-prop")) {
		    argc++;
            try {
              System.out.println("loading " + args[argc]);
              prop.load(new FileReader(args[argc++]));
              b=true;
            } catch(IOException e) { e.printStackTrace(); }
        }
        if (!b) try {
            prop.load(Main.class.getResourceAsStream("/shanghai.properties"));
        } catch(IOException e) { e.printStackTrace(); }

        if (args.length>argc && args[argc].endsWith("-clean")) {
            argc++;
            new Indexer(prop).cleanSolr();
            return args.length;
        }

        if (args.length-1>argc && args[argc].endsWith("-test")) {
		    argc++;
			getIndexer(prop).test(args[argc]);
        } else if (args.length>argc && args[argc].endsWith("-test")) {
		    argc++;
            silence();
			getIndexer(prop).test();
        } else if (args.length>argc && args[argc].endsWith("-dump")) {
		    argc++;
            if (args.length-1>argc) {
                silence();
                getIndexer(prop).dump(args[argc++], args[argc++]);
            } else if (args.length>argc) {
                silence();
                getIndexer(prop).dump(args[argc++]);
            } else {
                getIndexer(prop).dump();
            }
        } else if (args.length-1>argc && args[argc].endsWith("-post")) {
		    argc++;
            getIndexer(prop).post(args[argc++]);
        } else if (args.length-2>argc && args[argc].endsWith("-index")) {
		    argc++;
            getIndexer(prop).index(args[argc++], args[argc++]);
        } else if (args.length-1>argc && args[argc].endsWith("-index")) {
		    argc++;
            getIndexer(prop).index(args[argc++]);
        } else if (args.length>argc && args[argc].endsWith("-index")) {
		    argc++;
            getIndexer(prop).index();
        } else if (args.length>argc && args[argc].startsWith("-h")) {
            talk();
        } //else if (args.length==0) talk();
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

    protected void talk() {
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
