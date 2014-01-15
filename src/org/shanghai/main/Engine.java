package org.shanghai.main;

import org.shanghai.rdf.Config;
import org.shanghai.crawl.MetaCrawl;
import org.shanghai.data.Crawl;
import org.shanghai.data.WebCrawl;
import org.shanghai.oai.OAICrawl;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop
    @title Shanghai Crawler API
    @date 2014-01-12
*/
public class Engine {

    private String configFile;
    private Config config;
    private Crawl crawl;

    public Engine() {
        configFile = "lib/shanghai.ttl";
    }

    public Engine(String configFile) {
        this.configFile = configFile;
    }

    public void create() {
        config = new Config(configFile);
        config.create();
        crawl = new Crawl(config);
        crawl.create();
    }

    public void dispose() {
        crawl.dispose();
        config.dispose();
    }

    public void probe() {
        crawl.probe();
    }

    public void test() {
        crawl.test();
    }

    public void test(String off) {
        crawl.test(off);
    }

    public void dump() {
        crawl.dump();
    }

    public void dump(String rc) {
        crawl.dump(rc);
    }

    public void delete(String rc) {
        crawl.delete(rc);
    }

    public void post(String rc) {
        crawl.post(rc);
    }

    public void clean() {
        crawl.clean();
    }

    public void crawl(String directory) {
        crawl.createCrawler();
        crawl.crawl(directory); // does not copy 
        crawl.crawl();
    }

    public void crawl(String source, String target) {
        System.out.println("Engine " + source + " to " + target);
        if (source.equals("web")) {
            System.out.println("web crawl");
            String iri = config.get("crawl.iri");
            String site = config.get("web.crawl");
            String cache = config.get("web.cache");
            int robots = config.getInt("web.robots");
            MetaCrawl.Storage storage = crawl.createStorage(target);
            WebCrawl transporter = new WebCrawl(cache,robots,storage,iri);
            transporter.create();
            transporter.crawl(site);
            transporter.dispose();
        } else if (source.equals("oai")) {
            System.out.println("oai crawl");
            new OAICrawl(config).crawl(target); // -probe -test
        } else {
            crawl(new String[]{source,target});
        }
    }

    public void crawl(String[] directories) {
        crawl.createCrawler();
        crawl.crawl(directories); // copy included; repair ?
        //crawl.crawl();
    }

    public void sync() {
        crawl.sync();
    }

    public void sync(String source) {
        crawl.sync(source);
    }

    public void sync(String source, String target) {
        crawl.sync(source, target);
    }

    public void sync(String source, String target, String directory) {
        crawl.sync(source, target, directory);
    }

}
