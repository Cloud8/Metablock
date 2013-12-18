<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:nlm="http://dtd.nlm.nih.gov/publishing/2.3"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     version="1.0" >

<!-- 
   /**
     * @license http://www.apache.org/licenses/LICENSE-2.0
     * @author Goetz Hatop <fb.com/goetz.hatop>
     * @title A XSLT Transformer for NLM to RDF
     * @date 2013-09-24
     **/ -->

<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/">
  <xsl:apply-templates select="nlm:article" />
</xsl:template>

<xsl:template match="nlm:article">
 <rdf:RDF>
    <xsl:apply-templates select="nlm:front/nlm:journal-meta" />
 </rdf:RDF>
</xsl:template>

<xsl:template match="nlm:journal-meta">
  <xsl:param name="iid">
      <xsl:value-of select="concat(../nlm:article-meta/nlm:pub-date[@pub-type='collection']/nlm:year,'/',../nlm:article-meta/nlm:issue-id)"/>
  </xsl:param>
  <xsl:param name="url">
      <xsl:value-of 
       select="concat('http://archiv.ub.uni-marburg.de/ep/'
                       , nlm:journal-id)"/>
  </xsl:param>
  <fabio:JournalIssue rdf:about="{concat($url,'/',$iid)}">
      <xsl:choose>
      <xsl:when test="../nlm:article-meta/nlm:issue-title">
        <dct:title><xsl:value-of select="../nlm:article-meta/nlm:issue-title"/>
        </dct:title>
      </xsl:when>
      <xsl:otherwise>
        <dct:title><xsl:value-of select="nlm:journal-title"/></dct:title>
      </xsl:otherwise>
      </xsl:choose>
      <dct:identifier>
        <xsl:value-of select="../nlm:article-meta/nlm:issue-urn"/>
      </dct:identifier>
    <foaf:img><xsl:value-of select="concat($url,'/',$iid,'/cover.png')"/></foaf:img>

   <dct:publisher>
     <foaf:Organization rdf:about="http://d-nb.info/gnd/2001630-X">
     <foaf:name>Philipps-Universität Marburg</foaf:name></foaf:Organization>
   </dct:publisher>
   <xsl:choose>
     <xsl:when test="nlm:journal-id='0002'">
       <dct:publisher>
         <aiiso:Faculty rdf:about="http://www.uni-marburg.de/fb09">
         <foaf:name>Fachbereich  Germanistik und Kunstwissenschaften</foaf:name>
         </aiiso:Faculty>
       </dct:publisher>
       <dct:publisher>
          <aiiso:Institute rdf:about="http://www.uni-marburg.de/fb09/medienwissenschaft">
          <foaf:name>Medienwissenschaft</foaf:name>
         </aiiso:Institute>
       </dct:publisher>
     </xsl:when>
     <xsl:when test="nlm:journal-id='0003'">
      <dct:publisher>
       <aiiso:Center rdf:about="http://www.uni-marburg.de/cnms">
        <foaf:name>Center for Near and Middle Eastern Studies (CNMS)</foaf:name>
       </aiiso:Center>
      </dct:publisher>
     </xsl:when>
   </xsl:choose>

  <dct:isPartOf>
   <fabio:Journal rdf:about="{$url}">
      <dct:title><xsl:value-of select="nlm:journal-title"/></dct:title>
      <dct:identifier>
        <xsl:value-of select="../nlm:article-meta/nlm:journal-urn"/>
      </dct:identifier>
   </fabio:Journal>
  </dct:isPartOf>

  <dct:hasPart>
   <fabio:JournalArticle rdf:about="{concat($url,'/',$iid,'/',../nlm:article-meta/nlm:article-id)}">
    <dct:identifier><xsl:value-of select="../nlm:article-meta/nlm:article-urn"/>
    </dct:identifier>
    <dct:title>
        <xsl:value-of 
         select="../nlm:article-meta/nlm:title-group/nlm:article-title"/>
    </dct:title>
   </fabio:JournalArticle>
  </dct:hasPart>
 </fabio:JournalIssue>
</xsl:template>

<xsl:template match="text()"/>
</xsl:stylesheet>
