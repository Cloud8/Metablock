package org.shanghai.main;

/**
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title A Command Line Interface for the Shanghai Indexer
   @date 2013-06-05
*/
public class Main {

    public static void main(String[] args) {
      JarClassLoader jcl = new JarClassLoader();
      try {
            jcl.invokeMain("org.shanghai.oai.Main", args);
      } catch (Throwable e) {
            e.printStackTrace();
      }
    }

}
