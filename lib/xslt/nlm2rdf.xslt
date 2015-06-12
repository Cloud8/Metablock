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
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:ore="http://www.openarchives.org/ore/terms/"
     version="1.0" >

<!--
  /** @license http://www.apache.org/licenses/LICENSE-2.0
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
 <fabio:JournalArticle rdf:about="nlm:article-meta/nlm:self-uri[1]/@xlink:href">
    <xsl:apply-templates select="nlm:front/nlm:article-meta" />
    <xsl:apply-templates select="nlm:journal-meta" />
 </fabio:JournalArticle>
 </rdf:RDF>
</xsl:template>

<xsl:template match="nlm:article-meta">
    <dct:type><xsl:value-of select="'article'"/></dct:type>
    <dct:title>
       <xsl:value-of select="nlm:title-group/nlm:article-title"/>
    </dct:title>
    <xsl:apply-templates select="nlm:publisher" />

    <xsl:apply-templates select="nlm:contrib-group"/>
    <xsl:apply-templates select="nlm:pub-date[@pub-type='collection']" />
    <xsl:apply-templates select="nlm:pub-date[@pub-type='epub']" />
    <xsl:apply-templates select="nlm:article-categories"/>
    <xsl:apply-templates select="nlm:kwd-group" />
    <xsl:apply-templates select="nlm:permissions" />
    <xsl:apply-templates select="nlm:abstract" />
    <fabio:hasURL>
        <xsl:value-of select="nlm:self-uri[1]/@xlink:href"/>
    </fabio:hasURL>
    <xsl:variable name="doc">
        <xsl:value-of 
            select="nlm:self-uri[@content-type='application/pdf']/@xlink:href"/>
    </xsl:variable>
    <ore:aggregates rdf:resource="{concat(substring-before($doc,'view'),
                                'download',substring-after($doc,'view'))}"/>
    <xsl:apply-templates select="nlm:article-id" />
    <xsl:apply-templates select="nlm:issue-id" />
    <xsl:apply-templates select="nlm:issue" />
    <xsl:apply-templates select="nlm:volume" />
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
 <dct:creator>
  <rdf:Seq>
    <xsl:apply-templates select="nlm:contrib[@contrib-type='author']"/>
  </rdf:Seq>
 </dct:creator>
</xsl:template>

<xsl:template match="nlm:contrib-group/nlm:contrib[@contrib-type='author']">
  <rdf:li>
    <foaf:Person rdf:about="http://localhost/aut/{translate(concat(nlm:name/nlm:given-names,'_',nlm:name/nlm:surname),' ,[].','_')}">
      <xsl:apply-templates select="nlm:name"/>
    </foaf:Person>
  </rdf:li>
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
  <!-- foaf term ? -->
  <foaf:affiliation><xsl:value-of select="."/></foaf:affiliation>
</xsl:template>

<xsl:template match="nlm:permissions">
  <xsl:apply-templates select="nlm:copyright-statement"/>
</xsl:template>

<xsl:template match="nlm:abstract">
  <dct:abstract><xsl:value-of select="." /></dct:abstract>
</xsl:template>

<xsl:template match="nlm:publisher">
 <dct:publisher>
   <foaf:Organization rdf:about="http://localhost/org/ojs">
      <foaf:name><xsl:value-of select="." /></foaf:name>
   </foaf:Organization>
 </dct:publisher>
</xsl:template>

<xsl:template match="nlm:body">
  <dct:fulltext><xsl:value-of select="." /></dct:fulltext>
</xsl:template>

<xsl:template match="nlm:permissions/nlm:copyright-statement">
 <xsl:if test="substring-before(substring-after(.,'href='),' ')!=''">
  <dct:licence>
    <xsl:value-of select="substring-before(substring-after(.,'href='),' ')"/>
  </dct:licence>
 </xsl:if>
</xsl:template>

<xsl:template match="nlm:journal-meta">
  <xsl:param name="url">
     <xsl:value-of select="substring-before(../nlm:article-meta/nlm:self-uri/@xlink:href,'/article')"/>
 </xsl:param>

  <dct:isPartOf>
    <fabio:JournalIssue rdf:about="{concat($url,'/',../nlm:article-meta/nlm:pub-date[@pub-type='collection']/nlm:year,'/',../nlm:article-meta/nlm:issue-id)}">
      <xsl:choose>
      <xsl:when test="../nlm:article-meta/nlm:volume">
        <dct:title>
          <xsl:value-of select="concat('Vol. ',
              ../nlm:article-meta/nlm:volume, ' (',
              ../nlm:article-meta/nlm:pub-date[@pub-type='collection']/nlm:year
              ,')')"/>
        </dct:title>
      </xsl:when>
      <xsl:otherwise>
        <dct:title>
          <xsl:value-of select="concat(
              ../nlm:article-meta/nlm:pub-date[@pub-type='collection']/nlm:year
              ,',',../nlm:article-meta/nlm:issue)"/>
        </dct:title>
      </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="../nlm:article-meta/nlm:issue-title"/>
      <xsl:apply-templates select="../nlm:article-meta/nlm:issue-id"/>
      <xsl:apply-templates select="../nlm:article-meta/nlm:pub-date"/>
      <dct:type><xsl:value-of select="'PeriodicalPart'"/></dct:type>
      <xsl:apply-templates select="nlm:issn"/>

      <dct:isPartOf>
       <fabio:Journal rdf:about="{$url}">
        <dct:title><xsl:value-of select="nlm:journal-title"/></dct:title>
       </fabio:Journal>
      </dct:isPartOf>
  </fabio:JournalIssue>
 </dct:isPartOf>
</xsl:template>

<xsl:template match="nlm:journal-meta/nlm:issn">
  <fabio:hasISSN><xsl:value-of select="."/></fabio:hasISSN>
</xsl:template>

<xsl:template match="nlm:article-meta/nlm:volume">
  <fabio:hasVolumeIdentifier>
    <xsl:value-of select="."/>
  </fabio:hasVolumeIdentifier>
</xsl:template>

<!-- the internal ojs issue identifier -->
<xsl:template match="nlm:article-meta/nlm:issue-id">
  <fabio:hasIssueIdentifier>
    <xsl:value-of select="."/>
  </fabio:hasIssueIdentifier>
</xsl:template>

<!-- the issue sequence number -->
<xsl:template match="nlm:article-meta/nlm:issue[@seq]">
  <fabio:hasSequenceIdentifier>
    <xsl:value-of select="."/>
  </fabio:hasSequenceIdentifier>
</xsl:template>

<!-- the internal article identifier -->
<xsl:template match="nlm:article-meta/nlm:article-id[@pub-id-type='other']">
  <fabio:hasElectronicArticleIdentifier>
    <xsl:value-of select="."/>
  </fabio:hasElectronicArticleIdentifier>
</xsl:template>

<xsl:template match="nlm:article-meta/nlm:issue-title">
  <fabio:hasSubtitle><xsl:value-of select="."/></fabio:hasSubtitle>
</xsl:template>

<!-- the doi set from ojs -->
<xsl:template match="nlm:article-meta/nlm:article-id[@pub-id-type='doi']">
  <fabio:hasDOI><xsl:value-of select="."/></fabio:hasDOI>
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
