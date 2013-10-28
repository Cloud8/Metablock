package org.shanghai.main;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop
   @title Main Command Line Interface for the Shanghai Indexer
   @date 2013-09-05
*/
public class Main {

    public static String[] shorter(String[] dirs) {
        String[] result = new String[dirs.length-1];
        for (int i=0; i<dirs.length-1; i++)
             result[i] = dirs[i+1];
        return result;
    }

    //decide, which module to start
    public static void main(String[] args) {

        String configFile = "lib/shanghai.ttl";
        if (args.length>1 && args[0].startsWith("-conf")) {
            if (1<args.length) {
                configFile = args[1];
                args = shorter(shorter(args));
            } 
            System.out.println("configured by " + configFile);
        } 

        if (args.length>0&&"-crawl".equals(args[0])) {
            org.shanghai.oai.Main main = new org.shanghai.oai.Main(configFile);
            main.make(args);
        } else {
            org.shanghai.rdf.Main main = new org.shanghai.rdf.Main(configFile);
            main.make(args);
        }
    }
}
