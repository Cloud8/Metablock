package org.shanghai.oai;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.HashMap;
import java.util.Map;
import java.lang.StringBuffer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Transformer;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.shanghai.util.FileUtil;
import org.shanghai.rdf.Config;
import org.shanghai.rdf.XMLTransformer;

/**
   ✪ (c) reserved.
   @license http://www.apache.org/licenses/LICENSE-2.0
   @author Goetz Hatop <fb.com/goetz.hatop>
   @title Another URN implementation because all other ones are garbage
   @date 2013-04-14
*/
public class URN {

  String journal;
  String year;
  String issue;
  String article;
  String urn; 
  String url; 

  private DocumentBuilder dBuilder;
  private String prefix;

  public URN(String prefix) {
      this.prefix = prefix;
  }

  public void create() {
	  DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	  try {
	      dBuilder = dbFactory.newDocumentBuilder();
	  } catch(ParserConfigurationException e) { log(e); }
  }

  public void dispose() {
  }

  /** add a urn to xml nlm document and return as xml */
  public String talk(String xml) {
      Document doc = makeDoc(xml);
      return asString(doc);
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

  private void log(Document doc) {
      String xml = asString(doc);
      System.out.println(xml);
  }

  private String asString(Document doc) {
      String xml = null;
      try {
          Transformer transformer = 
	              TransformerFactory.newInstance().newTransformer();
          transformer.setOutputProperty(OutputKeys.INDENT, "yes");
          StreamResult result = new StreamResult(new StringWriter());
          DOMSource source = new DOMSource(doc);
          transformer.transform(source, result);
          xml = result.getWriter().toString();
	  } catch(TransformerConfigurationException e) { log(e); }
	    catch(TransformerException e) { log(e); }
      finally {
          return xml;
      }
  }

  private Document makeDoc(String xml) {
      Document doc = null;
	  try {
	      InputSource is = new InputSource(new StringReader(xml));
	      doc = dBuilder.parse(is);
	  } catch(SAXException e) { log(e); }
	    catch(IOException e) { log(e); }
	  if (doc==null) 
          return null;

	  //optional, but recommended
	  //read this - http://stackoverflow.com/questions/13786607/
	  //            normalization-in-dom-parsing-with-java-how-does-it-work
	  doc.getDocumentElement().normalize();

      NodeList jList = doc.getElementsByTagName("journal-meta");
      for (int temp = 0; temp < jList.getLength(); temp++) {
		   Node nNode = jList.item(temp);
		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
               Element el = (Element) nNode;
			   journal = el.getElementsByTagName("journal-id")
			                  .item(0).getTextContent();
           }
      }

      NodeList nList = doc.getElementsByTagName("article-meta");
      for (int temp = 0; temp < nList.getLength(); temp++) {
		   Node nNode = nList.item(temp);
		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
               Element el = (Element) nNode;
			   article = el.getElementsByTagName("article-id")
			                  .item(0).getTextContent();
			   issue = el.getElementsByTagName("issue-id")
			                  .item(0).getTextContent();
               NodeList pubs = el.getElementsByTagName("pub-date");
               for (int i=0; i<pubs.getLength(); i++) {
                    Element sub = (Element)pubs.item(i);
                    if (sub.getAttribute("pub-type")!=null
                        && sub.getAttribute("pub-type")
                               .equals("collection"))
			            year = sub.getElementsByTagName("year")
			                  .item(0).getTextContent();
               }
	           urn = getUrn(journal, year, issue, article);
               NodeList subs = el.getElementsByTagName("self-uri");
               for (int i=0; i<subs.getLength(); i++) {
                    Element sub = (Element)subs.item(i);
                    if (sub.getAttribute("content-type")!=null
                        && sub.getAttribute("content-type")
                               .equals("application/pdf"))
                        url = sub.getAttribute("xlink:href");
                }
		   }
      }
	  if (urn!=null) {
          deleteElement(doc, "urn");
          deleteElement(doc, "article-urn");
          addElement(doc, "article-urn", urn, nList.item(0));
	  }

	  String issueUrn = getUrn(journal, year, issue);
	  if (issueUrn!=null) {
          deleteElement(doc, "issue-urn");
          addElement(doc, "issue-urn", issueUrn, nList.item(0));
	  }

	  String journalUrn = getUrn(journal);
	  if (journalUrn!=null) {
          deleteElement(doc, "journal-urn");
          addElement(doc, "journal-urn", journalUrn, nList.item(0));
	  }
      return doc;
  }

  private void deleteElement(Document doc, String tag) {
      Element element = (Element) doc.getElementsByTagName(tag).item(0);
      if (element!=null)
          element.getParentNode().removeChild(element);
  }

  private void addElement(Document doc, String tag, String strg, Node node) {
	  Text text = doc.createTextNode(strg);
	  Element element = doc.createElement(tag);
	  element.appendChild(text);
	  node.appendChild(element);
  }

  private String getUrn(String journal, String year, String issue, String aid) {
      String raw = prefix + journal + "-" + year + "-" + issue + "-" + aid;
      return getUrnCheck(raw);
  }

  private String getUrn(String journal, String year, String issue) {
      String raw = prefix + journal + "-" + year + "-" + issue;
      return getUrnCheck(raw);
  }

  private String getUrn(String journal, String year) {
      String raw = prefix + journal + "-" + year;
      return getUrnCheck(raw);
  }

  private String getUrn(String journal) {
      String raw = prefix + journal;
      return getUrnCheck(raw);
  }

  Map<String, String> map = null;
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
      if (str.startsWith("urn:")) {
          String urn = getUrnCheck(str);
          System.out.println(urn);
      } else if (str.startsWith("http://")) {
          String src = str.substring(str.indexOf("//")+2);
          src = src.substring(src.indexOf("/")+1);
          src = src.replace("diss/","");
          src = src.replaceFirst("/","");
          src = src.replace("/","-");
          String urn = getUrn(src);
          log(urn);
      }
  }

  public void make(String in, String outfile) {
      String xml = FileUtil.read(in);
      Document doc = this.makeDoc(xml);
      if (outfile==null)
          log(asString(doc));
      FileUtil.write(outfile, this.asString(doc));
  }

  /***
  public static void main(String... args) {
      URN myself = new URN("urn:nbn:de:hebis:04-ep");
      myself.create();
      if (args.length >0 && args[0].equals("-urn")) {
          if (args[1]!=null) {
              String urn = myself.getUrnCheck(args[1]);
              //myself.log("urn: " + args[1] + " " + urn);
              System.out.println(urn);
          }
      } else if (args.length >0) {
          String xml = FileUtil.read(args[0]);
          Document doc = myself.makeDoc(xml);
          if (args.length >1)
              FileUtil.write(args[1], myself.asString(doc));
          else myself.log(myself.asString(doc));
          myself.log("article: " + myself.article);
          myself.log("year: " + myself.year);
          myself.log("issue: " + myself.issue);
          myself.log("urn: " + myself.urn);
          myself.log("url: " + myself.url);
      } else {
          myself.test();
      }
      myself.dispose();
  }
  **/
}
