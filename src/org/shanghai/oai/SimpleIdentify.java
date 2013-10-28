package org.shanghai.oai;

import com.lyncode.xoai.serviceprovider.HarvesterManager;
import com.lyncode.xoai.serviceprovider.configuration.Configuration;
import com.lyncode.xoai.serviceprovider.exceptions.HarvestException;
import com.lyncode.xoai.serviceprovider.exceptions.InternalHarvestException;
import com.lyncode.xoai.serviceprovider.verbs.Identify;

/** test class */
public class SimpleIdentify {

  public static void main(String... args) {
      Configuration config = new Configuration();
      config.setResumptionInterval(1000); // 1 second

      String baseUrl = "http://localhost:8080/xoai/request";
      int interval = 1000; 

      HarvesterManager harvester = new HarvesterManager(baseUrl, interval);

      try {
          Identify id = harvester.identify();
          System.out.println(id.getRepositoryName());
      } catch (HarvestException e) {
          System.out.println(e.getClass().getName());
          System.out.println(e.getMessage());
      } catch (InternalHarvestException e) {
          e.printStackTrace();
      }
    }
}
