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
     xmlns:prism="http://prismstandard.org/namespaces/basic/2.0/"
     version="1.0" >

<!-- 
   /**
     * @license http://www.apache.org/licenses/LICENSE-2.0
     * @author Goetz Hatop <fb.com/goetz.hatop>
     * @title A XSLT Transformer for NLM to RDF
     * @date 2013-05-29
     **/ -->

<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/">
  <xsl:apply-templates select="nlm:article" />
</xsl:template>

<xsl:template match="nlm:article">
 <rdf:RDF>
    <xsl:apply-templates select="nlm:front/nlm:article-meta" />
 </rdf:RDF>
</xsl:template>

<!-- republished to archiv -->
<xsl:template match="nlm:article-meta">
  <xsl:param name="docbase">
  <xsl:choose>
  <xsl:when test="../nlm:journal-meta/nlm:journal-id='0003'">
     <xsl:value-of select="concat('http://archiv.ub.uni-marburg.de/ep/0003',
          '/',nlm:pub-date[@pub-type='collection']/nlm:year,
          '/',nlm:issue-id,'/',nlm:article-id)"/>
  </xsl:when>
  <xsl:otherwise>
     <xsl:value-of 
          select="concat(substring-before(nlm:self-uri/@xlink:href,'article'),
          nlm:pub-date[@pub-type='collection']/nlm:year,
          '/',nlm:issue-id,'/',nlm:article-id)" />
  </xsl:otherwise>
  </xsl:choose>
 </xsl:param>

 <fabio:JournalArticle rdf:about="{$docbase}">
  <!-- urn build by org.shanghai.oai.URN -->
  <dct:identifier><xsl:value-of select="nlm:article-urn"/></dct:identifier>
  <dct:type>JournalArticle</dct:type>
  <xsl:if test="./nlm:journal-meta/nlm:issn">
  <prism:issn><xsl:value-of select="../nlm:journal-meta/nlm:issn"/></prism:issn>
  </xsl:if>
  <xsl:if test="nlm:issue">
  <prism:number><xsl:value-of select="nlm:issue"/></prism:number>
  </xsl:if>
  <xsl:if test="nlm:volume">
  <prism:volume><xsl:value-of select="nlm:volume"/></prism:volume>
  </xsl:if>
  <fabio:hasPublicationYear>
     <xsl:value-of select="nlm:pub-date[@pub-type='collection']/nlm:year"/>
  </fabio:hasPublicationYear>
  <dct:title><xsl:value-of select="nlm:title-group/nlm:article-title"/>
  </dct:title>
  <xsl:for-each select="nlm:contrib-group/nlm:contrib[@contrib-type='author']">
   <dct:creator>
    <foaf:Person rdf:about="http://archiv.ub.uni-marburg.de/au/{translate(concat(nlm:name/nlm:given-names,'_',nlm:name/nlm:surname),' ,[].','_')}">
      <foaf:givenName><xsl:value-of select="nlm:name/nlm:given-names"/>
        </foaf:givenName>
      <foaf:familyName><xsl:value-of select="nlm:name/nlm:surname"/>
        </foaf:familyName>
      <foaf:name>
         <xsl:value-of select="nlm:name/nlm:given-names"/><xsl:text> </xsl:text>
         <xsl:value-of select="nlm:name/nlm:surname"/>
      </foaf:name>
      <xsl:if test="nlm:aff">
      <!-- unfortunately, foaf has no affiliation plan -->
      <foaf:plan><xsl:value-of select="nlm:aff"/></foaf:plan>
      </xsl:if>
    </foaf:Person>
   </dct:creator>
  </xsl:for-each>
  <xsl:apply-templates select="nlm:pub-date[@pub-type='collection']" />
  <xsl:apply-templates select="nlm:pub-date[@pub-type='epub']" />
  <dct:subject>
   <xsl:value-of select="nlm:article-categories/nlm:subj-group/nlm:subject"/>
  </dct:subject>
  <xsl:apply-templates select="nlm:kwd-group" />
  <dct:licence>http://creativecommons.org/licenses/by/3.0/</dct:licence>

  <dct:abstract>
    <xsl:value-of select="substring(string(../../nlm:body),0,1999)" />
  </dct:abstract>
  <xsl:apply-templates select="../nlm:journal-meta" />

  <dct:relation>
    <!-- republished document: -->
    <!-- <xsl:value-of select="concat($docbase,'/',nlm:article-id,'.pdf')"/> -->
    <!-- original OJS version: -->
    <xsl:variable name="doc">
    <xsl:value-of select="nlm:self-uri[@content-type='application/pdf']/@xlink:href"/>
    </xsl:variable>
    <xsl:value-of select="concat(substring-before($doc,'view'),'viewFile',substring-after($doc,'view'))"/>
  </dct:relation>
  <foaf:img><xsl:value-of select="concat($docbase,'/cover.png')"/>
  </foaf:img>
 </fabio:JournalArticle>
</xsl:template>

<xsl:template match="nlm:journal-meta">
  <xsl:param name="url">
  <xsl:choose>
  <xsl:when test="nlm:journal-id='0003'">
     <xsl:value-of select="'http://archiv.ub.uni-marburg.de/ep/0003'"/>
  </xsl:when>
  <xsl:otherwise>
     <xsl:value-of select="substring-before(../nlm:article-meta/nlm:self-uri/@xlink:href,'/article')"/>
  </xsl:otherwise>
  </xsl:choose>
 </xsl:param>

   <dct:isPartOf>
    <fabio:JournalIssue rdf:about="{concat($url,'/',../nlm:article-meta/nlm:pub-date[@pub-type='collection']/nlm:year,'/',../nlm:article-meta/nlm:issue-id)}">
      <xsl:if test="../nlm:article-meta/nlm:issue-title">
      <fabio:hasSubtitle>
        <xsl:value-of select="../nlm:article-meta/nlm:issue-title"/>
      </fabio:hasSubtitle>
      </xsl:if>
      <dct:title>
         <xsl:value-of select="../nlm:journal-meta/nlm:journal-title"/>
      </dct:title>
      <dct:identifier>
        <xsl:value-of select="../nlm:article-meta/nlm:issue-urn"/>
      </dct:identifier>
      <foaf:img><xsl:value-of select="concat($url,'/',../nlm:article-meta/nlm:pub-date[@pub-type='collection']/nlm:year,'/',../nlm:article-meta/nlm:issue-id,'/cover.png')"/></foaf:img>
    </fabio:JournalIssue>
   </dct:isPartOf>

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
</xsl:template>

<xsl:template match="nlm:pub-date[@pub-type='collection']">
 <dct:issued>
      <xsl:value-of select="nlm:year"/>-<xsl:value-of select="../nlm:pub-date[@pub-type='epub']/nlm:month"/>-<xsl:value-of select="../nlm:pub-date[@pub-type='epub']/nlm:day"/>
 </dct:issued>
</xsl:template>

<xsl:template match="nlm:pub-date[@pub-type='epub']">
  <dct:modified><xsl:value-of select="nlm:year"/>-<xsl:value-of select="nlm:month"/>-<xsl:value-of select="nlm:day"/></dct:modified>
</xsl:template>

<xsl:template match="nlm:kwd-group">
  <xsl:apply-templates select="nlm:kwd" />
</xsl:template>

<xsl:template match="nlm:kwd">
  <xsl:if test=".!=''">
  <dct:subject><xsl:value-of select="."/></dct:subject>
  </xsl:if>
</xsl:template>

<xsl:template match="text()"/>
</xsl:stylesheet>
