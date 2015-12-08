<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:nlm="http://dtd.nlm.nih.gov/publishing/2.3"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     version="1.0" >

<!--
  /** @license http://www.apache.org/licenses/LICENSE-2.0
    * @author Goetz Hatop
    * @title NLM to RDF Transformer
    * @date 2014-06-05
   **/ -->

<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:param name="server" select="'http://example.com/'"/>
<xsl:param name="base" select="nlm:article/nlm:front/nlm:article-meta"/>
<xsl:param name="year" 
     select="$base/nlm:pub-date[@pub-type='collection']/nlm:year"/>
<xsl:param name="seq" select="$base/nlm:issue-id[@pub-id-type='other']"/>
<xsl:param name="aid" select="$base/nlm:article-id[@pub-id-type='other']"/>

<xsl:template match="/">
  <xsl:apply-templates select="*" />
</xsl:template>

<xsl:template match="nlm:article">
 <xsl:param name="uri" 
      select="concat($server, nlm:front/nlm:journal-meta/nlm:journal-id)"/>
 <rdf:RDF>
   <fabio:JournalArticle rdf:about="{concat($uri,'/',$year,'/',$seq,'/',$aid)}">
    <xsl:apply-templates select="nlm:front/nlm:journal-meta">
        <xsl:with-param name="uri" select="$uri"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="nlm:front/nlm:article-meta">
        <xsl:with-param name="uri" select="$uri"/>
    </xsl:apply-templates>
   </fabio:JournalArticle>
 </rdf:RDF>
</xsl:template>

<xsl:template match="nlm:article-meta">
    <xsl:param name="uri"/>
    <dcterms:type><xsl:value-of select="'article'"/></dcterms:type>
    <xsl:apply-templates select="nlm:title-group/nlm:article-title"/>
    <xsl:apply-templates select="nlm:title-group/nlm:trans-title"/>
    <xsl:apply-templates select="nlm:contrib-group">
        <xsl:with-param name="uri" select="$uri"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="nlm:pub-date[@pub-type='collection']" />
    <xsl:apply-templates select="nlm:pub-date[@pub-type='epub']" />
    <xsl:apply-templates select="nlm:article-categories"/>
    <xsl:apply-templates select="nlm:kwd-group" />
    <xsl:apply-templates select="nlm:permissions" />
    <xsl:apply-templates select="nlm:abstract" />
    <xsl:apply-templates select="nlm:self-uri">
        <xsl:with-param name="uri" select="$uri"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="nlm:article-id" />
    <xsl:apply-templates select="nlm:issue-id" />
    <xsl:apply-templates select="nlm:issue" />
    <xsl:apply-templates select="nlm:volume" />
</xsl:template>

<xsl:template match="nlm:self-uri[not(@content-type)]">
  <xsl:param name="uri"/>
</xsl:template>

<xsl:template match="nlm:self-uri[@content-type='text/html']">
  <xsl:param name="uri"/>
</xsl:template>

<xsl:template match="nlm:self-uri[@content-type='application/pdf'][@xlink:href]">
  <fabio:hasURL><xsl:value-of select="@xlink:href"/></fabio:hasURL>
  <dcterms:hasPart>
    <dctypes:Text rdf:about="{concat(substring-before(@xlink:href,
                   'view'), 'download',substring-after(@xlink:href,'view'))}">
       <dcterms:format><dcterms:MediaTypeOrExtent>
         <rdfs:label><xsl:value-of select="@content-type"/></rdfs:label>
       </dcterms:MediaTypeOrExtent></dcterms:format>
    </dctypes:Text>
  </dcterms:hasPart>
</xsl:template>

<xsl:template match="nlm:title-group/nlm:article-title">
  <dcterms:title><xsl:value-of select="."/></dcterms:title>
</xsl:template>

<xsl:template match="nlm:title-group/nlm:trans-title">
  <dcterms:language><xsl:value-of select="@xml:lang"/></dcterms:language>
</xsl:template>

<xsl:template match="nlm:article-categories">
  <xsl:apply-templates select="nlm:subj-group"/>
</xsl:template>

<xsl:template match="nlm:article-categories/nlm:subj-group">
  <xsl:apply-templates select="nlm:subject"/>
</xsl:template>

<xsl:template match="nlm:article-categories/nlm:subj-group/nlm:subject">
  <dcterms:subject><skos:Concept>
    <rdfs:label><xsl:value-of select="."/></rdfs:label>
   </skos:Concept></dcterms:subject>
</xsl:template>

<xsl:template match="nlm:contrib-group">
 <xsl:param name="uri"/>
 <dcterms:creator>
  <rdf:Seq>
    <xsl:apply-templates select="nlm:contrib[@contrib-type='author']">
        <xsl:with-param name="uri" select="$uri"/>
    </xsl:apply-templates>
  </rdf:Seq>
 </dcterms:creator>
</xsl:template>

<xsl:template match="nlm:contrib-group/nlm:contrib[@contrib-type='author']">
 <xsl:param name="uri"/>
  <rdf:li>
    <foaf:Person rdf:about="{concat($uri,'/aut/',translate(concat(nlm:name/nlm:given-names,'_',nlm:name/nlm:surname),' ,[].','_'))}">
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
  <!-- better term ? -->
  <foaf:plan><xsl:value-of select="."/></foaf:plan>
</xsl:template>

<xsl:template match="nlm:abstract">
  <dcterms:abstract><xsl:value-of select="." /></dcterms:abstract>
</xsl:template>

<xsl:template match="nlm:publisher">
 <xsl:param name="uri"/>
 <dcterms:publisher>
   <foaf:Organization 
     rdf:about="{concat($uri,'/aut/',translate(.,' ,[].','_'))}">
      <foaf:name><xsl:value-of select="." /></foaf:name>
   </foaf:Organization>
 </dcterms:publisher>
</xsl:template>

<xsl:template match="nlm:body">
</xsl:template>

<xsl:template match="nlm:permissions">
  <xsl:apply-templates select="nlm:copyright-statement"/>
  <xsl:apply-templates select="nlm:license"/>
</xsl:template>

<xsl:template match="nlm:permissions/nlm:copyright-statement">
   <dcterms:rights><xsl:value-of select="."/></dcterms:rights>
</xsl:template>

<xsl:template match="nlm:permissions/nlm:license">
  <xsl:if test="@href!=''">
   <dcterms:license rdf:resource="{@href}"></dcterms:license>
  </xsl:if>
</xsl:template>

<xsl:template match="nlm:journal-meta">
 <xsl:param name="uri"/>
  <dcterms:isPartOf>
    <fabio:JournalIssue rdf:about="{concat($uri,'/',$year,'/',$seq)}">
      <xsl:choose>
      <xsl:when 
        test="../nlm:article-meta/nlm:volume and ../nlm:article-meta/nlm:issue">
        <dcterms:title>
          <xsl:value-of select="concat('Vol. ',../nlm:article-meta/nlm:volume, 
          ' No. ',../nlm:article-meta/nlm:issue, ' (', $year ,')')"/>
        </dcterms:title>
      </xsl:when>
      <xsl:when test="../nlm:article-meta/nlm:volume">
        <dcterms:title>
          <xsl:value-of select="concat('Vol. ',
              ../nlm:article-meta/nlm:volume, ' (', $year ,')')"/>
        </dcterms:title>
      </xsl:when>
      <xsl:otherwise>
        <dcterms:title><xsl:value-of select="concat($year, ', ', ../nlm:article-meta/nlm:issue)"/></dcterms:title>
      </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="nlm:journal-id"/>
      <xsl:apply-templates select="../nlm:article-meta/nlm:issue-title"/>
      <xsl:apply-templates select="../nlm:article-meta/nlm:issue-id"/>
      <xsl:apply-templates select="../nlm:article-meta/nlm:pub-date"/>
      <dcterms:type><xsl:value-of select="'PeriodicalPart'"/></dcterms:type>
      <xsl:apply-templates select="nlm:issn"/>
      <xsl:apply-templates select="nlm:publisher">
        <xsl:with-param name="uri" select="$uri"/>
      </xsl:apply-templates>
      <dcterms:isPartOf>
        <fabio:Journal rdf:about="{$uri}">
          <xsl:apply-templates select="nlm:journal-title"/>
        </fabio:Journal>
      </dcterms:isPartOf>
  </fabio:JournalIssue>
 </dcterms:isPartOf>
</xsl:template>

<xsl:template match="nlm:journal-meta/nlm:journal-id">
  <xsl:comment><xsl:value-of select="."/></xsl:comment>
</xsl:template>

<xsl:template match="nlm:journal-meta/nlm:journal-title">
  <dcterms:title><xsl:value-of select="."/></dcterms:title>
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
  <fabio:hasIdentifier><xsl:value-of select="concat('ojs:',.)"/>
  </fabio:hasIdentifier>
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
  <dcterms:created><xsl:value-of select="."/></dcterms:created>
</xsl:template>

<xsl:template match="nlm:pub-date[@pub-type='epub']">
  <dcterms:modified><xsl:value-of select="nlm:year"/>-<xsl:value-of select="nlm:month"/>-<xsl:value-of select="nlm:day"/></dcterms:modified>
  <dcterms:issued><xsl:value-of select="../nlm:pub-date[@pub-type='collection']/nlm:year"/>-<xsl:value-of select="nlm:month"/>-<xsl:value-of select="nlm:day"/></dcterms:issued>
</xsl:template>

<xsl:template match="nlm:kwd-group">
  <xsl:apply-templates select="nlm:kwd" />
</xsl:template>

<xsl:template match="nlm:kwd[text()!='']">
  <dcterms:subject><skos:Concept>
     <rdfs:label><xsl:value-of select="."/></rdfs:label>
  </skos:Concept></dcterms:subject>
</xsl:template>

<xsl:template match="text()"/>
</xsl:stylesheet>
