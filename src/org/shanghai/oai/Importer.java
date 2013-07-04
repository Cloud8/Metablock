package org.shanghai.oai;

import org.shanghai.util.FileUtil;
import org.shanghai.rdf.Config;
import org.shanghai.rdf.XMLTransformer;
import org.shanghai.jena.TDBWriter;

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
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.List;

public class Importer {

  private Config.OAI shanghai; //our settings

  private Configuration config;
  private HarvesterManager harvester;
  private String metadataPrefix;
  private String baseUrl;
  private Parameters param;
  private int interval;
  private String rawtest = "oai-test.xml";
  private String rdftest = "oai-test.rdf";
  private XMLTransformer transformer;
  private TDBWriter tdbWriter;
  private URN urn;

  public Importer(Config.OAI s, TDBWriter t) {
      this.shanghai = s;
      this.tdbWriter = t;
      urn = new URN(s.urnPrefix);
  }

  public void create() {
      config = new Configuration();
      config.setResumptionInterval(1000); // 1 second
      //metadataPrefix = "nlm";
      metadataPrefix = shanghai.prefix;
      //metadataPrefix = "oai_dc";
      baseUrl = shanghai.harvest;
      interval = 1000; 
      //interval = 10; 
      //harvester = new HarvesterManager(config, baseUrl);
      harvester = new HarvesterManager(baseUrl, interval);
      //SimpleDateFormat formatter = new SimpleDateFormat("dd MMM HH:mm");
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      param = new Parameters();
      try {
	      param.setFrom( formatter.parse(shanghai.from + " 00:00:01") );
	      param.setUntil( formatter.parse(shanghai.until + " 23:59:59") );
      } catch(ParseException e) { log(e); }
      String xslt = FileUtil.read(shanghai.transformer); 
      if (xslt==null) {
          log("No transformer file " + shanghai.transformer);
      } else {
          transformer = new XMLTransformer( xslt );
          transformer.create();
          //transformer.setParameter("date", shanghai.from);
          //if (shanghai.docbase!=null)
          //transformer.setParameter("docbase", shanghai.docbase);
      }
      if (shanghai.rdftest!=null)
          rdftest = shanghai.rdftest;
      if (shanghai.rawtest!=null)
          rawtest = shanghai.rawtest;
      urn.create();
  }

  public void dispose() {
      if (transformer!=null) {
          transformer.dispose();
          transformer = null;
      }
      urn.dispose();
  }

  private void log(String msg) {
      System.out.println(msg);
  }

  private void log(Exception e) {
      // e.printStackTrace();
      log(e.toString());
      try {
           throw(e);
      } catch(Exception ex) {}
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
      File f = new File(shanghai.republish);
      if (!f.isDirectory())
          return null;
      String path = shanghai.republish 
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

  /** this may go wrong if no path exists, but avoid paranoia */
  /** does not work, pdf streaming needs more efforts */
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
              //log(r.getHeader().getIdentifier());
              String record = getRecord(r.getHeader().getIdentifier());
              String urnRec = urn.talk(record);
              rePublish("ojs-"+urn.article+".xml", urnRec);
              String result = transformer.transform(urnRec);
              rePublish("about.rdf", result);
              rePublish();
              tdbWriter.add( result );
              count++;
          }
          log("harvested " + count + " records.");
      } catch (Exception e) {
          log(e);
      }
  }

  public void test() {
      log("test harvest " + baseUrl); 
      log("prefix " + metadataPrefix); 
      log("transformer " + shanghai.transformer); 
      log(" from: "  + param.getFrom());
      log(" until: " + param.getUntil());
      int count = 0;
      try {
          Identify id = harvester.identify();
          log(id.getRepositoryName());
      } catch (HarvestException e) { log(e);
      } catch (InternalHarvestException e) { log(e); }

      RecordIterator it = 
	                harvester.listRecords(metadataPrefix, param).iterator();
      String ri = null;
      try { 
          while (it.hasNext()) {
              // System.out.println(count);
              Record r = it.next();
              ri = r.getHeader().getIdentifier();
              log(ri);
              if (count++==3) 
                  break; //its a test.
          }

          String record = getRecord(ri);
          String urnRec = urn.talk(record);
          FileUtil.write(rawtest, urnRec);
		  log("wrote " + rawtest);
          rePublish("ojs-"+urn.article+".xml", urnRec);

		  if (transformer!=null) {
              String result = transformer.transform(urnRec);
              FileUtil.write(rdftest, result);
			  log("wrote " + rdftest);
              rePublish("about.rdf", result);
          }
          // rePublish();
      } catch (Exception e) { log(e.getMessage()); }
  }

  public void show() {
      log("test harvest " + baseUrl); 
      log("prefix " + metadataPrefix); 
      log("transformer " + shanghai.transformer); 
      log("from " + shanghai.from); 
      log("until " + shanghai.until); 
      log(" from: "  + param.getFrom());
      log(" until: " + param.getUntil());
      log(" republish " + shanghai.republish); 
  }

  public static void main(String... args) {
      Config c = new Config("/shanghai.ttl").create();
      TDBWriter tdbWriter = new TDBWriter(
                c.getProperties().getProperty("store.tdb"),
                c.getProperties().getProperty("store.graph"));
      List<Config.OAI> list = c.getOAIList();
      if (list.size()>1) {
          list.get(0).show();
      }

      if (true) {
          Config.OAI oai = list.get(0);
          Importer test = new Importer(oai, tdbWriter);
          test.create();
          test.test();
          test.dispose();
      }
  }
}
