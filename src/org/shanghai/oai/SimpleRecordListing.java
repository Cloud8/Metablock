package org.shanghai.oai;

import com.lyncode.xoai.serviceprovider.HarvesterManager;
import com.lyncode.xoai.serviceprovider.configuration.Configuration;
import com.lyncode.xoai.serviceprovider.data.Record;
import com.lyncode.xoai.serviceprovider.exceptions.HarvestException;
import com.lyncode.xoai.serviceprovider.iterators.RecordIterator;

public class SimpleRecordListing {

  public static void main(String... args) {
      Configuration config = new Configuration();
      config.setResumptionInterval(1000); // 1 second

      //String baseUrl = "http://localhost:8080/xoai/request";
      //String metadataPrefix = "oai_dc";
      String metadataPrefix = "nlm";

      //HarvesterManager harvester = new HarvesterManager(config, baseUrl);
      String baseUrl = "http://archiv.ub.uni-marburg.de/ep/0002/oai";
      int interval = 1000; 

      //HarvesterManager harvester = new HarvesterManager(config, baseUrl);
      HarvesterManager harvester = new HarvesterManager(baseUrl, interval);

    RecordIterator it = harvester.listRecords(metadataPrefix).iterator();
    try {
        while (it.hasNext()) {
            Record r = it.next();
            System.out.println(r.getHeader().getIdentifier());
        }
    } catch (Exception e) {
        System.out.println(e.getClass().getName());
        System.out.println(e.getMessage());
        }
    }
}
