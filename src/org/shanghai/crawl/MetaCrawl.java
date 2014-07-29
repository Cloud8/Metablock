package org.shanghai.crawl;

import org.shanghai.util.FileUtil;
import org.shanghai.rdf.Config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop 
    @title The Meta Crawler
    @date 2013-02-23
*/
public class MetaCrawl {

    /** read from here */
    public interface Transporter {
        public void create();
        public void dispose();
        public String probe();
        public Model read(String resource);
        public String[] getIdentifiers(int off, int limit);
        public int crawl(String directory);
    }

    /** write to there */
    public interface Storage {
        public void create();
        public void dispose();
        public boolean test(String resource); // existance check
        public boolean delete(String resource);
        public boolean write(Model mod);
        public boolean update(Model mod); 
        public void destroy();
    }

    /** model analyzer */
    public interface Analyzer {
        public Analyzer create();
        public void dispose();
        public void analyze(Model model);
        public void analyze(Model model, Resource rc);
    }

    protected Transporter transporter;
    protected Storage storage;
    private List<Analyzer> analyzers;

    private String testFile;
    private int logC;
    private int count = 0;
    private boolean create;// (no update, just write)
    private boolean test = false;
    private boolean analyze = false;
    private long start,end;

    public MetaCrawl(Transporter t, Storage s, 
                     String testFile, int logC, boolean create) {
        this.transporter = t;
        this.storage = s;
        this.testFile = testFile;
        this.logC = logC;
        this.create = create;
    }

    public MetaCrawl(Transporter t, Storage s, String testFile, int logC) {
        this(t,s,testFile,logC,true);//just write, no model update.
    } 

    public MetaCrawl(Transporter t, Storage s, String testFile) {
        this(t,s,testFile,0,true);//no log messages required
    } 

    public MetaCrawl(Transporter t, Storage s) {
        this(t,s,null,0,true);//dump problems to sys.out
    } 

    public MetaCrawl(Transporter t,String testFile) {
        this.transporter = t;
        this.testFile = testFile;
        test=true;
    } 

    public MetaCrawl(Transporter t) {
        this(t,null,null,0,true);//test case
        test=true;
    } 

    public void inject(Analyzer a) {
        log("injected " + a.getClass().getName());
        if (!analyze) {
            analyzers = new ArrayList<Analyzer>();
        }
        analyze = true;
        analyzers.add(a);
    }

    public void dispose() {
        if (analyze) {
            for (Analyzer a : analyzers) {
                 a.dispose();
            }
        }
    }

    public void create() {
        start=0L;
        end = System.currentTimeMillis();
    }

    public void dump() {
        dump(transporter.getIdentifiers(0,1)[0]);
    }

    public void dump(String resource) {
        Model mod = transporter.read(resource);
        dump(mod);
    }

    public String probe() {
        log("probe " + transporter.getClass().getName());
        return transporter.probe();
    }

    public void test(String resource) {
        int count=transporter.crawl(resource);
        for (String id : transporter.getIdentifiers(0,7))
             if (id!=null)
                 log("test " + resource + " : " + id);
        log("found " + count + " items in [" + resource + "]"); 
    }

    public void test(String from, String until) {
        int x = Integer.parseInt(from);
        int y = Integer.parseInt(until);
        String[] ids = transporter.getIdentifiers(x,y);
        log("found " + ids.length  + " items "); 
    }

    public int crawl(String source) {
        start = System.currentTimeMillis();
        log("index " + source);
        int found = transporter.crawl(source);
        end = System.currentTimeMillis();
        log("crawl " + source + " " + found + " items in " 
                     + (end - start)/1000 + " sec.");
        return found;
    }

    public int crawl() {
        //if (1==1) {
        //    log(transporter.getClass().getName());
        //    log(storage.getClass().getName());
        //    return 0;
        //}
        if (start==0L) {
            start = System.currentTimeMillis();
        }
        int chunkSize = 200;
        crawl(chunkSize);
        end = System.currentTimeMillis();
        double rs = ((end - start)/1000);
        if ( rs>0.0 ) //prevent zero div
             rs = count / rs ;
        String msg = "crawled " + count + " records in " 
          + ((double)Math.round(end - start)/1000) + " sec [" + rs +" rec/s]";
        if (test)
            log(msg + " in test mode.");
        else log(msg);
        return count;
    }

    public int crawl(String offset, String limit) {
        int off = Integer.parseInt(offset);
        int count = Integer.parseInt(limit);
        if ( crawl(off,count) )
            return count;
        return 0;
    }

    protected Model analyze(Model model, String resource) {
        if (model==null) {
            return model;
        }
        if (analyze) {
            for (Analyzer a : analyzers) {
                 //Resource rc = model.getResource(resource);
                 //a.analyze(model, rc);
                 a.analyze(model);
            }
        }
        return model;
    }

    private int crawl(int chunkSize) {
        int i = 0;
        for (boolean b=true; b; b=crawl((i-1)*chunkSize, chunkSize)) {
             i++;
        }
        return (i-1)*chunkSize;
    }

    private boolean crawl(int offset, int limit) {
        //log("crawl " + offset);
        boolean b = true;
        String[] identifiers = transporter.getIdentifiers(offset,limit);
        if (identifiers==null)
            return false;
        for (String id : identifiers) {
             if (id==null)
                 return false;
             if (logC!=0&&count%logC==0)
                 log(id + " crawl " + count);
             //if (sync&&storage.exists(transporter.getIdentifier(id))) {
             //    continue;
             //} 
             Model mod = transporter.read(id);
             mod = analyze(mod, id);
             if(test&&count==0) dump(id,mod);
             if (mod==null) { //garbage, dont care.
                 log("failed " + id);
                 continue;
             }
             if(test) ; // log(id);
             else if (create) b=storage.write(mod);
                 else b=storage.update(mod);
             if(b) count++; //count storage success
             else {
                 log("storage problem: " + id);
                 dump(id,mod);
                 b=true;//dont stop the show
             }
        }
        return b;
    }

    public void post(String resource) {
        if (test) {
            log("post no storage " + resource);
            return;
        }
        Model mod = transporter.read(resource);
        if (create) storage.write(mod);
        else storage.update(mod);
    }

    private void dump(Model model) {
        StringWriter writer = new StringWriter();
        try {
           model.write(writer, "RDF/XML-ABBREV");
        } catch(Exception e) {
           model.write(System.out,"TTL");
        } finally {
            if (testFile==null) {
                System.out.println(writer.toString());
            } else {
                FileUtil.write(testFile, writer.toString());
                log("wrote " + testFile);
            }
        }
    }

    private void dump(String id, Model model) {
        StringWriter writer = new StringWriter();
        try {
           model.write(writer, "RDF/XML-ABBREV");
        } catch(Exception e) {
           model.write(System.out,"TTL");
        } finally {
           dumpOut(id,writer.toString());
        }
    }

    private void dumpOut(String id, String data) {
        if (testFile==null) {
            System.out.println(data);
        } else {
            FileUtil.write(testFile, data);
            log("dump " + id + " to " + testFile);
        }
    }

    public void dump(String id, String fn) {
        Model mod = transporter.read(id);
        StringWriter writer = new StringWriter();
        try {
           mod.write(writer, "RDF/XML-ABBREV");
        } catch(Exception e) {
           mod.write(System.out,"TTL");
        } finally {
            FileUtil.write(fn, writer.toString());
        }
    }

    public void destroy() {
        if(test) {
           log("destroy test " + test);
           return;
        } else {
           log("destroy storage");
           storage.destroy();
        }
    }

    public boolean delete(String resource) {
        if(test) return false;
        return storage.delete(resource);
    }

    public boolean update(String resource) {
        boolean b = false;
        Model mod = transporter.read(resource);
        mod = analyze(mod, resource);
        if (test) {
            dump(resource,mod);
        } 
        else b = storage.update(mod); 
        return b;
    }

    private static final Logger logger =
                         Logger.getLogger(MetaCrawl.class.getName());

    public static void log(String msg) {
        logger.info(msg);
    }

    protected void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

}
