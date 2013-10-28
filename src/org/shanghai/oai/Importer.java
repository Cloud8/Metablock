package org.shanghai.oai;

import org.shanghai.util.FileUtil;
import org.shanghai.rdf.Config;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.crawl.MetaCrawl;

import com.lyncode.xoai.serviceprovider.HarvesterManager;
import com.lyncode.xoai.serviceprovider.configuration.Configuration;
import com.lyncode.xoai.serviceprovider.data.Record;
import com.lyncode.xoai.serviceprovider.exceptions.HarvestException;
import com.lyncode.xoai.serviceprovider.iterators.RecordIterator;
import com.lyncode.xoai.serviceprovider.iterators.RecordIterator;
import com.lyncode.xoai.serviceprovider.verbs.Identify;
import com.lyncode.xoai.serviceprovider.verbs.GetRecord;
import com.lyncode.xoai.serviceprovider.verbs.Parameters;
import com.lyncode.xoai.serviceprovider.exceptions.InternalHarvestException;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.List;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.arp.JenaReader;

public class Importer implements MetaCrawl.Transporter {

  /* TODO : this needs rework:
     settings.interval settings.maxRequest
     also, the MetaCrawl API usage should be clear.
  */

  private Config.OAI settings; 
  private Configuration config;
  private HarvesterManager harvester;
  private String metadataPrefix;
  private String baseUrl;
  private Parameters param;
  private int interval;
  private int count=0;
  private String rawtest = "oai-test.xml";
  private String rdftest = "oai-test.rdf";
  private XMLTransformer transformer;
  private MetaCrawl.Storage storage;
  private URN urn;

  public Importer(Config.OAI settings,MetaCrawl.Storage storage) {
      this.settings = settings;
      this.storage = storage;
      urn = new URN(settings.urnPrefix);
  }

  @Override
  public void create() {
      config = new Configuration();
      config.setResumptionInterval(1000); // 1 second
      metadataPrefix = settings.prefix;
      baseUrl = settings.harvest;
      //GH201310 : use settings interval ?
      //interval = 1000 * Integer.parseInt(settings.interval);  
      interval = 1000 ;
      harvester = new HarvesterManager(baseUrl, interval);
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      param = new Parameters();
      try {
	      param.setFrom( formatter.parse(settings.from + " 00:00:01") );
	      param.setUntil( formatter.parse(settings.until + " 23:59:59") );
      } catch(ParseException e) { log(e); }
      String xslt = FileUtil.read(settings.transformer); 
      if (xslt==null) {
          log("No transformer file " + settings.transformer);
      } else {
          transformer = new XMLTransformer( xslt );
          transformer.create();
      }
      if (settings.rdftest!=null)
          rdftest = settings.rdftest;
      if (settings.rawtest!=null)
          rawtest = settings.rawtest;
      urn.create();
  }

  @Override
  public void dispose() {
      if (transformer!=null) {
          transformer.dispose();
          transformer = null;
      }
      urn.dispose();
  }

    @Override
    public String probe() {
        String result = "failed.";
        try {
            Identify id = harvester.identify();
            result = id.getRepositoryName();
        } catch (HarvestException e) { log(e);
        } catch (InternalHarvestException e) { log(e); }
        finally {
           return result;
        }
    }

    @Override
    public Model read(String identifier) {
       //very cheap implementation : TODO : investigate on too much strings
       String xml = getRecord(identifier);
       String rdf = transformer.transform(xml);
       try {
            InputStream in = new ByteArrayInputStream(rdf.getBytes("UTF-8"));
            Model m = ModelFactory.createDefaultModel();
            RDFReader reader = new JenaReader(); 
            reader.read(m, in, null);
            in.close();
            return m;
        } catch(IOException e) { 
            log(identifier); log(e); 
        }
        return null;
    }

    @Override
    public String[] getIdentifiers(int off, int limit) {
        String[] result = new String[limit];
        int found=0;
        RecordIterator it = 
	                harvester.listRecords(metadataPrefix, param).iterator();
        try { 
            while (it.hasNext()) {
                Record rec = it.next();
                String ri = rec.getHeader().getIdentifier();
                if (off<count && count<off+limit)
                    result[found-off-1] = ri;
                found++;
            }
        } catch (Exception e) { log(e.getMessage()); }
        count += found;
        return result;
    }

    @Override 
    public int crawl(String source) {
        return count;
    }

    @Override
    public boolean canRead(String source) {
        return true;
    }

    private String getRecord(String identifier) {
        String result = null;
        try {
            GetRecord r = harvester.getRecord(identifier, metadataPrefix);
            if (r.hasMetadata()) {
                //log(identifier + " [" + (count++) + "]");
                //read from input stream
                result = FileUtil.read(r.getMetadata().getMetadata());
            }
        } catch(InternalHarvestException e) {
            log(e);
        } finally {
            return result;
        }
    }

    private String rePublishPath() {
        File f = new File(settings.republish);
        if (!f.isDirectory())
            return null;
        String path = settings.republish 
                  + "/" + urn.year + "/" + urn.issue + "/" + urn.article;
        File g = new File(path);
        if (!g.exists())
            if (!new File(path).mkdirs())
              return null;
        return path;
    }

  /* write metadata records out to directory */
  private void rePublish(String file, String xml) {
      String path = rePublishPath();
      if (path==null)
          return;
      FileUtil.write(path + "/" + file, xml);
      log("wrote " + path + "/" + file);
  }

  /** This may go wrong if no path exists, but try to avoid paranoia */
  private void rePublish() {
      String path = rePublishPath();
      String from = urn.url;
      if (from!=null) {
          FileUtil.copy(from, path + "/view.html");
          log("wrote " + path + "/view.html");
          from = from.replace("view", "viewFile");
          FileUtil.copy(from, path + "/" + urn.article + ".pdf");
          log("wrote " + path + "/" + urn.article + ".pdf");
      }
  }

  public void make() {
      int count = 0;
      RecordIterator it = 
	      harvester.listRecords(metadataPrefix, param).iterator();
      try {
          while (it.hasNext()) {
              Record r = it.next();
              boolean b = false;
              String record = getRecord(r.getHeader().getIdentifier());
              if (settings.urnPrefix!=null && settings.republish!=null) {
                  String urnRec = urn.talk(record);
                  rePublish("ojs-"+urn.article+".xml", urnRec);
                  String rdf = transformer.transform(urnRec);
                  rePublish("about.rdf", rdf);
                  rePublish();
                  b=storage.post(rdf);
              } else {
                  log(r.getHeader().getIdentifier());
                  String rdf = transformer.transform(record);
                  b=storage.post(rdf);
              }
              if (b) count++;
          }
          log("harvested " + count + " records.");
      } catch (Exception e) {
          log(e);
      }
  }

  public void test() {
      log("test harvest " + baseUrl); 
      log("prefix " + metadataPrefix); 
      log("transformer " + settings.transformer); 
      log(" from: "  + param.getFrom());
      log(" until: " + param.getUntil());
      int count = 0;
      probe();

      RecordIterator it = 
	                harvester.listRecords(metadataPrefix, param).iterator();
      String ri = null;
      try { 
          while (it.hasNext()) {
              // System.out.println(count);
              Record r = it.next();
              ri = r.getHeader().getIdentifier();
              if (count++==3 || ri==null) 
                  break; //its a test.
              log(ri);
          }

          String record = getRecord(ri);
		  log("test over.");
          if (settings.urnPrefix!=null && settings.republish!=null) {
              String urnRec = urn.talk(record);
              rePublish("ojs-"+urn.article+".xml", urnRec);
              FileUtil.write(rawtest, urnRec);
		      log("wrote " + rawtest);
              record = urnRec;
          } else {
              FileUtil.write(rawtest, record);
		      log("wrote " + rawtest);
          }

		  if (transformer!=null) {
              String result = transformer.transform(record);
              FileUtil.write(rdftest, result);
			  log("wrote " + rdftest);
              if (settings.republish!=null)
                  rePublish("about.rdf", result);
          }
      } catch (Exception e) { log(e.getMessage()); }
  }

  public void show() {
      log("test harvest " + baseUrl); 
      log("prefix " + metadataPrefix); 
      log("transformer " + settings.transformer); 
      log("from " + settings.from); 
      log("until " + settings.until); 
      log(" from: "  + param.getFrom());
      log(" until: " + param.getUntil());
      log(" republish " + settings.republish); 
  }

  private void log(String msg) {
      System.out.println(msg);
  }

  private void log(Exception e) {
      log(e.toString());
      try { throw(e); } catch(Exception ex) { ex.printStackTrace(); }
  }

}
