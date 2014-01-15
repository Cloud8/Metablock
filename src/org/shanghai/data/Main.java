package org.shanghai.data;

import org.shanghai.rdf.Config;
import org.shanghai.util.FileUtil;
import org.shanghai.bones.Helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import java.util.List;
import java.util.ArrayList;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title A data crawler, more general than parent.
    @date 2013-02-23
*/
public class Main extends org.shanghai.crawl.Main {

    public Main(String configFile) {
        this.configFile = configFile;
    }

    @Override
    public void create() {
        config = new Config(configFile).create();
        crawl = new Crawl(config);
        crawl.create();
    }

    @Override
    public void dispose() {
        crawl.dispose();
    }

    @Override
    public int make(String[] args) {
        System.out.println("# data " + args.length);
        create();
        crawl.crawl(args);
        dispose();
        return 0;
    }

    public static void main(String[] args) {
        String config = "lib/shanghai.ttl";
        if (args.length>1 && args[0].startsWith("-conf")) {
            if (1<args.length) {
                config = args[1];
                args = Config.shorter(args);
                args = Config.shorter(args);
            } 
            System.out.println("configured by " + config);
        } 

        if ("-crawl".equals(args[0])) {
            org.shanghai.data.Main main = new org.shanghai.data.Main(config);
            main.make(args);
        }
    }

}

