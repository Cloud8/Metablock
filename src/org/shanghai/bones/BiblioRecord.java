package org.shanghai.bones;

import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;
import thewebsemantic.RdfType;

//import thewebsemantic.Uri;
import thewebsemantic.Id;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop <fb.com/goetz.hatop>
  @title A Bibliographic Record
  @date 2012-10-22
*/
@Namespace("http://purl.org/dc/terms/")
@RdfType("BibliographicResource")
public class BiblioRecord {

    public static final String DCT = "http://purl.org/dc/terms/";
    public static final String DC  = "http://purl.org/dc/elements/1.1/";

    @Id public String id;
    public String getId() {
        return id;
    }

    public String recordtype;

    @RdfProperty(DCT + "coverage")
    public String getRecordtype() {
        return recordtype;
    }

    @RdfProperty(DC + "creator") 
    public String author;
    public void setAuthor(String a) {
	    author = a;
    }
    public String getAuthor() {
        return author;
    }

    public String title;
    public void setTitle(String t) {
        title = t;
    }
    @RdfProperty(DC + "title")
    public String getTitle() {
        return title;
    }

    public List<String> author_additional = new ArrayList<String>();
    public void setAuthor_additional(String a) {
        author_additional = getList(author_additional, a);
    }

    public String title_short;

    public String title_full;

    private List<String> isbn = new ArrayList<String>();
    public void setIsbn(String i) {
        isbn.clear();
        if (i==null) return;
        isbn.add(i);
    }
    public List<String> getIsbn() {
        return isbn;
    }

    private List<String> oclc_num = new ArrayList<String>();
    public void setOclc_num(String i) {
        oclc_num.clear();
        if (i==null) return;
        oclc_num.add(i);
    }
    public List<String> getOclc_num() {
        return oclc_num;
    }

    public List<String> format = new ArrayList<String>();
    public void setFormat(String f) {
        if (f==null) return;
        format.add(f);
    }

    @RdfProperty
    public String getFormat() {
        return format.get(0);
    }

    public List<String> url = new ArrayList<String>();
    public void setUrl(String u) {
        url.add(u);
    }

    public String getUrl() {
        return url.get(0);
    }

    //@Uri
    //public String getUri() {
    //    return url.get(0);
    //}

    public List<String> publishDate = new ArrayList<String>();
    public void setPublishDate(String p) {
        publishDate.add(p);
    }
    @RdfProperty(DCT + "publishdate") 
    public String getPublishDate() {
        if (publishDate.size()==0)
            return "";
        return publishDate.get(0);
    }

    public List<String> language = new ArrayList<String>();
    public void setLanguage(String l) {
        language.add(l);
    }

    @RdfProperty(DCT + "language")
    public String getLanguage() {
        return language.get(0);
    }

    public String publisher;

    public String fulltext;

    public List<String> institution = new ArrayList<String>();
    public void setInstitution(String i) {
        institution.add(i);
    }

    private List<String> topic = new ArrayList<String>();
    public void setTopic(List<String> t) {
        topic = t;
    }
    
    public void delTopic() {
        topic = new ArrayList<String>();
    }

    private List<String> topic_facet = new ArrayList<String>();
    public void setTopic_facet(List<String> t) {
        topic_facet = t;
    }

    public void setTopic(String t) {
        if (t==null || t.trim().length()==0) return;
        if (t.indexOf(",")>0) {
            topic = getList(topic, t);
            topic_facet = getList(topic_facet, t);
        } else {
            topic.add(t);
            topic_facet.add(t);
        }
    }

    @RdfProperty("subject")
    public String getTopic() {
        return getListAsString(topic);
    }

    public List<String> genre = new ArrayList<String>();

    public void setGenre(List<String> g) {
        genre = g;
    }

    public void setGenre(String g) {
        genre = getList(genre, g);
    }

    public String getGenre() {
        return getListAsString(genre);
    }

    String Abstract;

    Date date;

    public String publishDateSort;

    public String description = "";
    public String getDescription() {
        return description;
    }
    //may be later
    //public void setDescription(String d) {
    //    description = d;
    //}

    public String contents;

    public String thumbnail;

    public Date upd_date;

    private String getListAsString(List<String> list) {
        String s = new String("");
        for (int i=0; i<list.size(); i++) {
            s+=list.get(i);
            if (i<list.size()-1)
            s+=", ";
        }
        return s;
    }

    private List<String> getList(List<String> list, String s) {
       if (s==null) return list;
       list.clear();
       String[] split = s.split(",");
       for (int i=0; i<split.length; i++) {
           if (split[i].trim().length()>0)
           list.add(split[i].trim());
       }
       return list;
    }

    public String toString() {
        return "[" 
               + "id: " + id + "\n"
               + "Url: " + url + "\n"
               + "Titel: " + title + "\n" 
               + "Autor: " + author
               + "]";
    }
}
