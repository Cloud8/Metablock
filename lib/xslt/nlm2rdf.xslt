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

<!-- /** @license http://www.apache.org/licenses/LICENSE-2.0
       * @author Goetz Hatop <fb.com/goetz.hatop>
       * @title An XSLT Transformer for NLM to RDF
       * @date 2014-06-05
      **/ -->

<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/">
  <xsl:apply-templates select="*" />
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
    <dct:type>JournalArticle</dct:type>
    <dct:title><xsl:value-of select="nlm:title-group/nlm:article-title"/>
    </dct:title>

    <xsl:apply-templates select="nlm:contrib-group"/>
    <xsl:apply-templates select="nlm:pub-date[@pub-type='collection']" />
    <xsl:apply-templates select="nlm:pub-date[@pub-type='epub']" />
    <xsl:apply-templates select="nlm:article-categories"/>
    <xsl:apply-templates select="nlm:kwd-group" />
    <xsl:apply-templates select="nlm:permissions" />
    <xsl:apply-templates select="../../nlm:body" />

    <!-- republished document: -->
    <dct:relation>
      <xsl:value-of select="concat($docbase,'/',nlm:article-id,'.pdf')"/> 
    </dct:relation>

    <!-- original OJS : -->
    <xsl:variable name="doc">
    <xsl:value-of 
         select="nlm:self-uri[@content-type='application/pdf']/@xlink:href"/>
    </xsl:variable>
    <dct:alternative>
      <xsl:value-of select="concat(substring-before($doc,'view'),'download',substring-after($doc,'view'))"/>
    </dct:alternative>

    <foaf:img>
     <xsl:value-of select="concat($docbase,'/',nlm:article-id,'.png')"/>
    </foaf:img>
    <xsl:apply-templates select="nlm:article-id" />
    <xsl:apply-templates select="nlm:issue-id" />
    <xsl:apply-templates select="nlm:volume" />

    <xsl:apply-templates select="../nlm:journal-meta" />
 </fabio:JournalArticle>
</xsl:template>

<xsl:template match="nlm:article-categories">
  <xsl:apply-templates select="nlm:subj-group"/>
</xsl:template>

<xsl:template match="nlm:article-categories/nlm:subj-group">
  <xsl:apply-templates select="nlm:subject"/>
</xsl:template>

<xsl:template match="nlm:article-categories/nlm:subj-group/nlm:subject">
  <dct:subject><xsl:value-of select="."/></dct:subject>
</xsl:template>

<xsl:template match="nlm:contrib-group">
  <xsl:apply-templates select="nlm:contrib[@contrib-type='author']"/>
</xsl:template>

<xsl:template match="nlm:contrib-group/nlm:contrib[@contrib-type='author']">
   <dct:creator>
    <foaf:Person rdf:about="http://archiv.ub.uni-marburg.de/au/{translate(concat(nlm:name/nlm:given-names,'_',nlm:name/nlm:surname),' ,[].','_')}">
      <xsl:apply-templates select="nlm:name"/>
    </foaf:Person>
   </dct:creator>
</xsl:template>

<xsl:template match="nlm:contrib-group/nlm:contrib/nlm:name">
  <xsl:apply-templates select="nlm:given-names"/>
  <xsl:apply-templates select="nlm:surname"/>
  <foaf:name>
    <xsl:value-of select="nlm:given-names"/><xsl:text> </xsl:text>
    <xsl:value-of select="nlm:surname"/>
  </foaf:name>
  <xsl:apply-templates select="nlm:aff"/>
</xsl:template>

<xsl:template match="nlm:contrib-group/nlm:contrib/nlm:name/nlm:given-names">
  <foaf:givenName><xsl:value-of select="."/></foaf:givenName>
</xsl:template>

<xsl:template match="nlm:contrib-group/nlm:contrib/nlm:name/nlm:surname">
  <foaf:familyName><xsl:value-of select="."/></foaf:familyName>
</xsl:template>

<xsl:template match="nlm:contrib-group/nlm:contrib/nlm:name/nlm:aff">
  <!-- unfortunately, foaf has no affiliation plan -->
  <foaf:plan><xsl:value-of select="."/></foaf:plan>
</xsl:template>

<xsl:template match="nlm:permissions">
  <xsl:apply-templates select="nlm:copyright-statement"/>
</xsl:template>

<xsl:template match="nlm:body">
 <xsl:if test="string-length(.)>5">
  <dct:abstract>
    <xsl:value-of select="substring(string(.),0,1999)" />
  </dct:abstract>
 </xsl:if>
</xsl:template>

<!-- http://creativecommons.org/licenses/by/3.0/ -->
<xsl:template match="nlm:permissions/nlm:copyright-statement">
  <dct:licence>
    <xsl:value-of select="substring-before(substring-after(.,'href='),' ')"/>
  </dct:licence>
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
      <xsl:choose>
      <xsl:when test="../nlm:article-meta/nlm:issue-title">
        <dct:title>
          <xsl:value-of select="concat(nlm:journal-title,'; Vol ',
              ../nlm:article-meta/nlm:volume, ' (',
              ../nlm:article-meta/nlm:pub-date[@pub-type='collection']/nlm:year
              ,'): ')"/>
          
          <xsl:value-of select="../nlm:article-meta/nlm:issue-title"/>
        </dct:title>
      </xsl:when>
      <xsl:otherwise>
        <dct:title>
          <xsl:value-of select="concat(nlm:journal-title,' ',
              ../nlm:article-meta/nlm:pub-date[@pub-type='collection']/nlm:year
              ,'/',../nlm:article-meta/nlm:issue)"/>
        </dct:title>
      </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="../nlm:article-meta/nlm:issue-id"/>
      <xsl:apply-templates select="../nlm:article-meta/nlm:pub-date"/>
      <foaf:img><xsl:value-of select="concat($url,'/',../nlm:article-meta/nlm:pub-date[@pub-type='collection']/nlm:year,'/',../nlm:article-meta/nlm:issue-id,'/cover.png')"/></foaf:img>

   <xsl:choose>
     <xsl:when test="nlm:journal-id='0002'">
      <dct:publisher>
         <aiiso:Faculty rdf:about="http://www.uni-marburg.de/fb09">
         <foaf:name>Fachbereich  Germanistik und Kunstwissenschaften</foaf:name>
         </aiiso:Faculty>
      </dct:publisher>
      <!-- NLM data has print-issn 1431-5262. This is the e-issn: -->
      <prism:issn><xsl:value-of select="'2196-4270'"/></prism:issn>
      <!-- ZDB-Idn MEDREZ : -->
      <fabio:hasIdentifier>
          <xsl:value-of select="'1465812-4'"/>
      </fabio:hasIdentifier>
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
      <!-- <prism:issn>2196-629X</prism:issn> -->
      <xsl:apply-templates select="nlm:issn"/>
      <!-- ZDB-Idn META : -->
      <fabio:hasIdentifier>
          <xsl:value-of select="'2714728-9'"/>
      </fabio:hasIdentifier>
     </xsl:when>
   </xsl:choose>

   <dct:isPartOf>
    <fabio:Journal rdf:about="{$url}">
     <dct:title><xsl:value-of select="nlm:journal-title"/></dct:title>
     <dct:publisher>
        <foaf:Organization rdf:about="http://d-nb.info/gnd/2001630-X">
        <foaf:name>Philipps-Universität Marburg</foaf:name></foaf:Organization>
     </dct:publisher>
    </fabio:Journal>
   </dct:isPartOf>
  </fabio:JournalIssue>
 </dct:isPartOf>
</xsl:template>

<xsl:template match="nlm:journal-meta/nlm:issn">
  <prism:issn><xsl:value-of select="."/></prism:issn>
</xsl:template>

<xsl:template match="nlm:article-meta/nlm:volume">
  <prism:volume><xsl:value-of select="."/></prism:volume>
</xsl:template>

<xsl:template match="nlm:article-meta/nlm:issue-id">
  <prism:issueIdentifier><xsl:value-of select="."/></prism:issueIdentifier>
</xsl:template>

<xsl:template match="nlm:article-meta/nlm:article-id">
  <prism:number><xsl:value-of select="."/></prism:number>
</xsl:template>

<xsl:template match="nlm:pub-date[@pub-type='collection']">
  <xsl:apply-templates select="nlm:year"/>
</xsl:template>

<xsl:template match="nlm:pub-date[@pub-type='collection']/nlm:year">
  <dct:created><xsl:value-of select="."/></dct:created>
</xsl:template>

<xsl:template match="nlm:pub-date[@pub-type='epub']">
  <dct:modified><xsl:value-of select="nlm:year"/>-<xsl:value-of select="nlm:month"/>-<xsl:value-of select="nlm:day"/></dct:modified>
  <dct:issued><xsl:value-of select="../nlm:pub-date[@pub-type='collection']/nlm:year"/>-<xsl:value-of select="nlm:month"/>-<xsl:value-of select="nlm:day"/></dct:issued>
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
