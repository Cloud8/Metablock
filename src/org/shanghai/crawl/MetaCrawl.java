package org.shanghai.crawl;

import org.shanghai.util.FileUtil;
import org.shanghai.rdf.Config;

import java.io.StringWriter;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.rdf.model.Property;

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
        public Resource read(String resource);
        public Resource test(String resource);
        public List<String> getIdentifiers(int off, int limit);
        public int index(String directory);
    }

    /** write to there */
    public interface Storage {
        public void create();
        public void dispose();
        public boolean delete(String resource);
        public boolean write(Resource rc);
        public boolean test(Resource rc);
        public void destroy();
    }

    /** model analyzer */
    public interface Analyzer {
        public Analyzer create();
        public void dispose();
        public String probe();
        public Resource analyze(Resource rc);
        public Resource test(Resource rc);
    }

    protected Transporter transporter;
    protected Storage storage;
    private List<Analyzer> analyzers;

    private String testFile;
    private int logC;
    private int count = 0;
    private boolean create; // no update
    private boolean test = false;
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
        this(t,s,testFile,logC,true);//write without model update.
    } 

    public MetaCrawl(Transporter t, Storage s, String testFile) {
        this(t,s,testFile,0,true);//no log messages required
    } 

    public MetaCrawl(Transporter t, Storage s) {
        this(t,s,null,0,true);//dump problems to sys.out
    } 

    public MetaCrawl(Transporter t, String testFile) {
        this.transporter = t;
        this.testFile = testFile;
        test = true;
    } 

    public MetaCrawl(Transporter t) {
        this(t,null,null,0,true);//test case
        test = true;
    } 

    public void dispose() {
        for (Analyzer a : analyzers) {
            a.dispose();
        }
    }

    public void create() {
        start=0L;
        end = System.currentTimeMillis();
        analyzers = new ArrayList<Analyzer>();
    }

    public void inject(Analyzer a) {
        analyzers.add(a);
        log("injected " + a.getClass().getName());
    }

    public String probe() {
        StringBuilder sb = new StringBuilder();
        sb.append(transporter.probe());
        for (Analyzer a : analyzers) {
            sb.append(" # ").append(a.probe());
        }
        return(sb.toString());
    }

    public void test() {
        for (String id : transporter.getIdentifiers(0,5)) {
            if (id!=null) log("test " + id);
            for (Analyzer a : analyzers) a.test(transporter.read(id));
        }
    }

    public boolean test(String resource) {
        log("test # " + resource);
        Resource rc = transporter.test(resource);
        if (rc==null) {
            logger.severe("No model found " + resource);
            return false;
        }
        for (Analyzer a : analyzers) {
            rc = a.test(rc);
        }
        boolean b = storage==null?false:storage.test(rc);
        // if (!b) dump(rc);
        return b;
    }

    private Resource test(Resource rc) {
        log("test ## " + rc.getURI());
        if (analyzers.size()>0) {
            for (Analyzer a : analyzers) {
                 rc = a.test(rc);
            }
        } else {
            log("no analyzer to test.");
            boolean b = storage.test(rc);
        }
        return rc;
    }

    public void test(String from, String until) {
        int x = Integer.parseInt(from);
        int y = Integer.parseInt(until);
        List<String> ids = transporter.getIdentifiers(x,y);
        log("found " + ids.size()  + " items "); 
        for (String id : ids) {
            // test(id);
            log(id);
        }
    }

    public int index(String source) {
        start = System.currentTimeMillis();
        int found = transporter.index(source);
        end = System.currentTimeMillis();
        if (found>1) log("index [" + source + "] " + found + " items in " 
                     + (end - start)/1000 + " sec.");
        return found;
    }

    /** crawl this resource 
        @param id the resource identifier
        @return the number of successfully written resources
     */
    public int crawl(String id) {
        //log("crawl (" + id + ") " + create);
        boolean b = true;
        Resource rc = transporter.read(id);
        if (rc==null) { //garbage, dont care.
            log("zero model transporter " + id);
            return count;
        }
        rc = test?test(rc):analyze(rc);
        if(test&&count==0) dump(rc);
        if(test || storage==null) ; // log(id);
        else {
            if (!create && rc!=null) {
                log("delete " + rc.getURI());
                storage.delete(id);
            }
            b=storage.write(rc);
        }
        if(b) count++; //count storage success
        else {
            log("storage problem: " + id);
            dump(rc);
            b=true;//dont stop
        }
        return count;
    }

    public int crawl() {
        if (start==0L) {
            start = System.currentTimeMillis();
            count = 0;
        }
        int chunkSize = 200;
        crawl(chunkSize);
        end = System.currentTimeMillis();
        double rs = ((end - start)/1000);
        if ( rs>0.0 ) //prevent invalid div
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
        int lim = Integer.parseInt(limit);
        if ( crawl(off, lim) )
            return lim;
        return 0;
    }

    private int crawl(int chunkSize) {
        int i = 0;
        // log("crawl chunkSize " + chunkSize);
        for (boolean b=true; b; b=crawl((i-1)*chunkSize, chunkSize)) {
             i++;
        }
        return (i-1)*chunkSize;
    }

    private boolean crawl(int offset, int limit) {
        int x = 0;
        List<String> identifiers = transporter.getIdentifiers(offset,limit);
        if (identifiers==null) {
            return false;
        }
        for (String id : identifiers) {
             if (id==null)
                 return false;
             if (logC!=0&&count%logC==0) {
                 end = System.currentTimeMillis();
                 log(id + " #" + count + " " + (end - start)/1000 + " sec.");
             }
             x += crawl(id);
        }
        return (x>0); 
    }

    public void post(String resource) {
        if (test) {
            log("post no storage " + resource);
            return;
        }
        Resource rc = transporter.read(resource);
        if (!create) storage.delete(resource);
        storage.write(rc);
    }

    public void dump() {
        dump(transporter.getIdentifiers(0,1).get(0));
    }

    public void dump(String resource) {
        //log("should dump " + resource);
        Resource rc = transporter.read(resource);
        dump(rc);
    }

    private void dump(Resource rc) {
        StringWriter writer = new StringWriter();
        Model model = rc.getModel();
        try {
            model.write(writer, "RDF/XML-ABBREV");
        } catch(Exception e) {
            //model.write(System.out,"TTL");
            log("broken model " + rc.getURI());
        } finally {
            if (testFile==null) {
                //System.out.println(writer.toString());
            } else {
                boolean b = FileUtil.write(testFile, writer.toString());
                if (b) log("wrote " + rc.getURI() + " to " + testFile);
            }
        }
    }

    public void dump(String id, String fn) {
        log("dump " + id + " : " + fn);
        Resource rc = null;
        if (test) {
            rc = transporter.test(id);
        } else {
            rc = transporter.read(id);
        }
        if (test) {
            for (Analyzer a : analyzers) {
                 a.test(rc);
            }
        } 
        if (fn.endsWith(".rdf")) {
            StringWriter writer = new StringWriter();
            try {
                rc.getModel().write(writer, "RDF/XML-ABBREV");
            } catch(Exception e) {
                rc.getModel().write(System.out,"TTL");
            } finally {
                FileUtil.write(fn, writer.toString());
            }
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

    private Resource analyze(Resource rc) {
        if (rc==null) {
            throw new AssertionError("zero resource.");
            //log("zero resource.");
            //return rc;
        }
        for (Analyzer a : analyzers) {
            rc = a.analyze(rc);
        }
        return rc;
    }

    private final Logger logger =
                         Logger.getLogger(MetaCrawl.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception e) {
        log(e.toString());
        e.printStackTrace(System.out);
    }

}
