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
    <xsl:apply-templates select="nlm:front/nlm:article-meta" />
 </rdf:RDF>
</xsl:template>

<!-- republished to archiv -->
<xsl:template match="nlm:article-meta">
  <xsl:param name="uri"><!-- urn calculation base -->
      <xsl:value-of 
          select="concat('http://archiv.ub.uni-marburg.de/ep/',
                  ../nlm:journal-meta/nlm:journal-id,
                  '/',nlm:pub-date[@pub-type='collection']/nlm:year,
                  '/',nlm:issue-id,'/',nlm:article-id[@pub-id-type='other'])" />
  </xsl:param>

  <fabio:JournalArticle rdf:about="{$uri}">
    <dct:type><xsl:value-of select="'article'"/></dct:type>
    <dct:title><xsl:value-of select="nlm:title-group/nlm:article-title"/>
    </dct:title>

    <xsl:apply-templates select="nlm:contrib-group"/>
    <xsl:apply-templates select="nlm:pub-date[@pub-type='collection']" />
    <xsl:apply-templates select="nlm:pub-date[@pub-type='epub']" />
    <xsl:apply-templates select="nlm:article-categories"/>
    <xsl:apply-templates select="nlm:kwd-group" />
    <xsl:apply-templates select="nlm:permissions" />
    <xsl:apply-templates select="nlm:abstract" />

    <!-- original OJS : -->
    <xsl:variable name="doc">
        <xsl:value-of 
            select="nlm:self-uri[@content-type='application/pdf']/@xlink:href"/>
    </xsl:variable>

    <fabio:hasURL>
        <xsl:value-of select="nlm:self-uri[1]/@xlink:href"/>
    </fabio:hasURL>
  
    <ore:aggregates rdf:resource="{concat(substring-before($doc,'view'),
                                'download',substring-after($doc,'view'))}"/>
    <!--
    <xsl:choose>
    <xsl:when test="../nlm:journal-meta/nlm:journal-id='0003'">
    </xsl:when>
    <xsl:otherwise>
      <ore:aggregates rdf:resource="{concat($uri,'/',nlm:article-id[@pub-id-type='other'],'.pdf')}"/>
    </xsl:otherwise>
    </xsl:choose>
    -->

    <foaf:img>
     <xsl:value-of select="concat($uri,'/',nlm:article-id[@pub-id-type='other'],'.png')"/>
    </foaf:img>
    <xsl:apply-templates select="nlm:article-id" />
    <xsl:apply-templates select="nlm:issue-id" />
    <xsl:apply-templates select="nlm:issue" />
    <xsl:apply-templates select="nlm:volume" />
    <dct:publisher>
      <foaf:Organization rdf:about="http://d-nb.info/gnd/2001630-X">
      <foaf:name>Philipps-Universität Marburg</foaf:name></foaf:Organization>
    </dct:publisher>

    <xsl:choose>
      <xsl:when test="../nlm:journal-meta/nlm:journal-id='0002'">
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
      <dct:language>de</dct:language>
      <dct:subject><!-- DDC 302.23 -->
         <skos:Concept rdf:about="http://dewey.info/class/302">
           <skos:prefLabel xml:lang="en">Social Interaction</skos:prefLabel>
         </skos:Concept>
      </dct:subject>
      <dct:rights rdf:resource="https://creativecommons.org/licenses/by/4.0"/>
      </xsl:when>

      <xsl:when test="../nlm:journal-meta/nlm:journal-id='0003'">
      <dct:publisher>
        <aiiso:Center rdf:about="http://www.uni-marburg.de/cnms">
        <foaf:name>Center for Near and Middle Eastern Studies (CNMS)</foaf:name>
       </aiiso:Center>
      </dct:publisher>
      <dct:language>en</dct:language>
      <dct:subject> <!-- 320 Politics ? -->
         <skos:Concept rdf:about="http://dewey.info/class/956">
           <skos:prefLabel xml:lang="en">General history of Asia; Middle East</skos:prefLabel>
         </skos:Concept>
      </dct:subject>
      <dct:rights rdf:resource="https://creativecommons.org/licenses/by/4.0"/>
      </xsl:when>

      <xsl:when test="../nlm:journal-meta/nlm:journal-id='0004'">
      <dct:publisher>
         <aiiso:Faculty rdf:about="http://www.uni-marburg.de/fb03">
         <foaf:name>Gesellschaftswissenschaften und Philosophie</foaf:name>
         </aiiso:Faculty>
      </dct:publisher>
      <dct:subject>
         <skos:Concept rdf:about="http://dewey.info/class/200">
           <skos:prefLabel xml:lang="en">Philosophy and theory of religion</skos:prefLabel>
           <skos:prefLabel xml:lang="de">Religion</skos:prefLabel>
         </skos:Concept>
      </dct:subject>
      </xsl:when>
    </xsl:choose>
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
 <dct:creator>
  <rdf:Seq>
    <xsl:apply-templates select="nlm:contrib[@contrib-type='author']"/>
  </rdf:Seq>
 </dct:creator>
</xsl:template>

<xsl:template match="nlm:contrib-group/nlm:contrib[@contrib-type='author']">
  <rdf:li>
    <foaf:Person rdf:about="http://archiv.ub.uni-marburg.de/aut/{translate(concat(nlm:name/nlm:given-names,'_',nlm:name/nlm:surname),' ,[].','_')}">
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
  <!-- foaf has no affiliation plan -->
  <foaf:plan><xsl:value-of select="."/></foaf:plan>
</xsl:template>

<xsl:template match="nlm:permissions">
  <xsl:apply-templates select="nlm:copyright-statement"/>
</xsl:template>

<xsl:template match="nlm:abstract">
  <dct:abstract><xsl:value-of select="." /></dct:abstract>
</xsl:template>

<xsl:template match="nlm:body">
  <dct:fulltext><xsl:value-of select="." /></dct:fulltext>
</xsl:template>

<!-- http://creativecommons.org/licenses/by/3.0/ -->
<xsl:template match="nlm:permissions/nlm:copyright-statement">
 <xsl:if test="substring-before(substring-after(.,'href='),' ')!=''">
  <dct:licence>
    <xsl:value-of select="substring-before(substring-after(.,'href='),' ')"/>
  </dct:licence>
 </xsl:if>
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
      <!-- GH201503: DNB moechte Angabe zur Ausgabe ohne ZS-Titel -->
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
      <foaf:img><xsl:value-of select="concat($url,'/',../nlm:article-meta/nlm:pub-date[@pub-type='collection']/nlm:year,'/',../nlm:article-meta/nlm:issue-id,'/cover.png')"/></foaf:img>
      <dct:publisher>
        <foaf:Organization rdf:about="http://d-nb.info/gnd/2001630-X">
        <foaf:name>Philipps-Universität Marburg</foaf:name></foaf:Organization>
      </dct:publisher>
      <dct:type><xsl:value-of select="'PeriodicalPart'"/></dct:type>

   <xsl:choose>
     <xsl:when test="nlm:journal-id='0002'">
      <!-- NLM data has print-issn 1431-5262. This is the e-issn: -->
      <fabio:hasISSN><xsl:value-of select="'2196-4270'"/></fabio:hasISSN>
      <!-- ZDB-Idn MEDREZ : -->
      <fabio:hasIdentifier>
          <xsl:value-of select="'1465812-4'"/>
      </fabio:hasIdentifier>
      <!-- this would link to external ojs content:
      <ore:aggregates rdf:resource="{concat('http://archiv.ub.uni-marburg.de/ep/0002/issue/view/',../nlm:article-meta/nlm:issue-id)}"/>
      -->
      <dct:subject><!-- DDC 302.23 -->
         <skos:Concept rdf:about="http://dewey.info/class/302">
           <skos:prefLabel xml:lang="en">Social Interaction</skos:prefLabel>
         </skos:Concept>
      </dct:subject>
     </xsl:when>

     <xsl:when test="nlm:journal-id='0003'">
      <!-- <fabio:hasISSN>2196-629X</fabio:hasISSN> -->
      <xsl:apply-templates select="nlm:issn"/>
      <!-- ZDB-Idn META : -->
      <fabio:hasIdentifier>
          <xsl:value-of select="'2714728-9'"/>
      </fabio:hasIdentifier>
      <!-- this would link to external ojs issue:
        <ore:aggregates 
         rdf:resource="{concat('http://meta-journal.net/issue/view/',
                                      ../nlm:article-meta/nlm:issue-id)}"/>
       -->
      <dct:subject>
         <skos:Concept rdf:about="http://dewey.info/class/956">
           <skos:prefLabel xml:lang="en">General history of Asia; Middle East</skos:prefLabel>
         </skos:Concept>
      </dct:subject>
     </xsl:when>

     <xsl:when test="nlm:journal-id='0004'">
      <!-- <fabio:hasISSN>1612-2941</fabio:hasISSN> -->
      <xsl:apply-templates select="nlm:issn"/>
      <!-- ZDB-Idn MJR : -->
      <fabio:hasIdentifier>
          <xsl:value-of select="'1418722-x'"/>
      </fabio:hasIdentifier>
      <dct:subject>
         <skos:Concept rdf:about="http://dewey.info/class/200">
           <skos:prefLabel xml:lang="en">Philosophy and theory of religion</skos:prefLabel>
           <skos:prefLabel xml:lang="de">Religion</skos:prefLabel>
         </skos:Concept>
      </dct:subject>
     </xsl:when>
   </xsl:choose>

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
