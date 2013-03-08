package org.shanghai.bones;

import org.apache.solr.client.solrj.beans.Field;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop <fb.com/goetz.hatop>
  @title A Bibliographic Record
  @date 2012-10-22
*/
public class BiblioRecord {

    @Field
    public String id;

    @Field
    public String recordtype;

    @Field
    public String author;
    public void setAuthor(String a) {
	    author = a;
    }

    @Field
    public List<String> author_additional = new ArrayList<String>();
    public void setAuthor_additional(String a) {
        author_additional = getList(author_additional, a);
    }

    @Field
    public String title;
    public void setTitle(String t) {
        title = t;
    }

    @Field
    public String title_short;

    @Field
    public String title_full;

    @Field
    private List<String> isbn = new ArrayList<String>();
    public void setIsbn(String i) {
        isbn.clear();
        if (i==null) return;
        isbn.add(i);
    }
    public List<String> getIsbn() {
        return isbn;
    }

    @Field
    private List<String> oclc_num = new ArrayList<String>();
    public void setOclc_num(String i) {
        oclc_num.clear();
        if (i==null) return;
        oclc_num.add(i);
    }
    public List<String> getOclc_num() {
        return oclc_num;
    }

    @Field
    public List<String> format;
    public void setFormat(String f) {
        if (f==null) return;
        if (format==null)
            format = new ArrayList<String>();
        format.add(f);
    }
    public String getFormat() {
        if (format==null)
            return null;
        return format.get(0);
    }

    @Field
    public List<String> url = new ArrayList<String>();
    public void setUrl(String u) {
        url.add(u);
    }
    public String getUrl() {
        return url.get(0);
    }

    @Field
    public List<String> institution = new ArrayList<String>();
    public void setInstitution(String i) {
        institution.add(i);
    }

    @Field
    public List<String> language;
    public void setLanguage(String l) {
        if (language==null) 
            language = new ArrayList<String>();
        language.add(l);
    }
    public String getLanguage() {
        if (language==null) 
            return null;
        return language.get(0);
    }

    @Field
    private List<String> topic;
    public void setTopic(List<String> t) {
        topic = t;
    }
    public void delTopic() {
        topic = new ArrayList<String>();
    }

    @Field
    private List<String> topic_facet = new ArrayList<String>();
    public void setTopic_facet(List<String> t) {
        topic_facet = t;
    }

    /** einzelne topics werden hinzugefuegt,
        listen vom topics setzen die topics dieses beans neu. */
    public void setTopic(String t) {
        if (t==null || t.trim().length()==0) return;
        if (topic==null) 
            topic = new ArrayList<String>();
        if (t.indexOf(",")>0) {
            topic = getList(topic, t);
            topic_facet = getList(topic_facet, t);
        } else {
            topic.add(t);
            topic_facet.add(t);
        }
    }

    public String getTopic() {
        return getListAsString(topic);
    }

    @Field
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

    @Field
    public String fulltext;

    //@Field
    //String Abstract;

    @Field
    public String publisher;

    @Field
    public List<String> publishDate;
    public void setPublishDate(String p) {
        if (publishDate==null)
            publishDate = new ArrayList<String>();
        publishDate.add(p);
    }
    public String getPublishDate() {
        if (publishDate==null)
            return null;
        if (publishDate.size()==0)
            return "";
        return publishDate.get(0);
    }

    @Field /** Date of formal issuance (e.g., publication) of the resource. */
    public String issued;
    public String getIssued() {
        return issued;
    }

    //@Field
    public String modified;

    //@Field
    //public String publishDateSort;

    @Field
    public String description;
    public String getDescription() {
        return description;
    }
    //may be later
    //public void setDescription(String d) {
    //    description = d;
    //}

    @Field
    public String contents;

    @Field /** thumbnail image url */
    public String thumbnail;

    @Field
    public Date upd_date;

    private String getListAsString(List<String> list) {
        if (list==null)
            return null;
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
               + "Url: " + url.toString() + "\n"
               + "Titel: " + title + "\n" 
               + "Autor: " + author
               + "]";
    }
}
