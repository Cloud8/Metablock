package org.shanghai.ojs;

import java.lang.StringBuffer;
import java.util.HashMap;
import java.util.Map;

/**
    (c) reserved.
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Goetz Hatop 
    @title Another URN implementation
    @date 2013-04-14
*/
public class URN {

    private String prefix;
  
    public URN(String prefix) {
        this.prefix = prefix;
    }
  
    public void create() {
        createMap();
    }
  
    public void dispose() {
        map = null;
    }
  
    /** return urn prefixed as configured and and suffixed by control number */
    public String getUrn(String raw) {
        String urn = null;
        if (prefix==null) {
            return null;
        } else if (raw.startsWith("urn:")) {
            urn = getUrnCheck(raw);
        } else if (raw.startsWith("http://")) {
            String src = raw.substring(raw.indexOf("//")+2);
            src = src.substring(src.indexOf("/")+1);
            src = src.replace("diss/","");
            src = src.replace("ep/","ep");
            src = src.replace("eb/","eb");
            src = src.replace("es/","es");
            src = src.replace("ed/","ed");
            src = src.replace("/","-");
            src = src.replace(" ","");
            //log(prefix + src);
            urn = getUrnCheck(prefix + src);
        }
        //log(raw + " # " + urn);
        return urn;
    }
  
    private Map<String, String> map = null;
    private void createMap() {
        map = new HashMap<String, String>();
        map.put("9", "41");
        map.put("8", "9");
        map.put("7", "8");
        map.put("6", "7");
        map.put("5", "6");
        map.put("4", "5");
        map.put("3", "4");
        map.put("2", "3");
        map.put("1", "2");
        map.put("0", "1");
        map.put("a", "18");
        map.put("b", "14");
        map.put("c", "19");
        map.put("d", "15");
        map.put("e", "16");
        map.put("f", "21");
        map.put("g", "22");
        map.put("h", "23");
        map.put("i", "24");
        map.put("j", "25");
        map.put("k", "42");
        map.put("l", "26");
        map.put("m", "27");
        map.put("n", "13");
        map.put("o", "28");
        map.put("p", "29");
        map.put("q", "31");
        map.put("r", "12");
        map.put("s", "32");
        map.put("t", "33");
        map.put("u", "11");
        map.put("v", "34");
        map.put("w", "35");
        map.put("x", "36");
        map.put("y", "37");
        map.put("z", "38");
        map.put("-", "39");
        map.put(":", "17");
        map.put("_", "43");
        map.put("/", "45");
        map.put(".", "47");
        map.put("+", "49");
    }
  
    private String getUrnCheck(String raw) {
        if (map==null)
            createMap();
  	    String result = null;
  	    StringBuffer buf = new StringBuffer();
        String newURN = "";
        int sum = 0;
        for (int i = 0; i < raw.length(); i++) {
            String ch = raw.substring(i,i+1).toLowerCase();
  			buf.append(map.get(ch));
        }
        for (int i = 0; i < buf.length(); i++) {
            int x = Integer.parseInt(buf.substring(i,i+1));
  			int prod = x * (i+1);
            sum += prod;
        }
        int last = Integer.parseInt(buf.substring(buf.length()-1));
        float quot = sum / last;
        int quotRound = (int)Math.floor(quot)%10;
        String quotString = "" + quotRound;
        result = raw + quotString;
  	  return result;
    }
  
    private boolean test(String urnRaw, String suffix) {
        if (getUrnCheck(urnRaw).endsWith(suffix)) {
            // log("pass: " + getUrnCheck(urnRaw));
            return true;
        } else {
            // log("failed: " + getUrnCheck(urnRaw));
            return false;
        }
    }
  
    private void test(String urn) {
        // test("urn:nbn:de:hebis:04-eb2013-0056", "4");
        String urnRaw = urn.substring(0,urn.length()-1);
        String suffix = urn.substring(urn.length()-1);
        if (test(urnRaw, suffix)) {
            log("OK: " + urn);
        } else {
            log(" FAILED: " + urn);
        }
    }
  
    public void test() {
        test("urn:nbn:de:hebis:04-eb2013-00564");
        test("urn:nbn:de:hebis:04-z2013-01252");
        test("urn:nbn:de:hebis:04-z2012-11075");
        test("urn:nbn:de:hebis:34-2006092214718");
        test("urn:nbn:de:hebis:04-ep0002-2013-31-9947");
        test("urn:nbn:de:hebis:04-ep0002-2013-31-10022");
        test("urn:nbn:de:hebis:04-ep0002-2013-31-10118");
        test("urn:nbn:de:hebis:04-ep0002-2012-30-9834");
    }
  
    public void make(String str) {
        String urn = getUrn(str);
        System.out.println(urn);
        /*
        if (str.startsWith("urn:")) {
            String urn = getUrnCheck(str);
            System.out.println(urn);
        } else if (str.startsWith("http://")) {
            String src = str.substring(str.indexOf("//")+2);
            src = src.substring(src.indexOf("/")+1);
            src = src.replace("diss/","");
            src = src.replace("/","-");
            String urn = getUrnCheck(prefix + src);
            System.out.println(urn);
        }
        */
    }

    public String toString() {
        return prefix;
    }
  
    private void log(String msg) {
        System.out.println(msg);
    }
  
    private void log(Exception e) {
        log(e.toString());
        try {
             throw(e);
        } catch(Exception ex) {}
    }
}
