<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" 
     xmlns:dc="http://purl.org/dc/elements/1.1/" 
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     version="1.0" >

<!-- 
 /**
  * @license http://www.apache.org/licenses/LICENSE-2.0
  * @title An XSLT Transformer for OAI DC to RDF
  * @date 2014-12-14
 **/ 
 -->

<xsl:output method="xml" indent="yes" encoding="UTF-8" />

<xsl:template match="/">
  <xsl:apply-templates select="oai_dc:dc" />
</xsl:template>

<xsl:template match="oai_dc:dc">
 <rdf:RDF
     xmlns:oai="http://www.openarchives.org/OAI/2.0/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:dcterms="http://purl.org/dc/terms/">
  <dcterms:BibliographicResource rdf:about="{dc:identifier[starts-with(., 'http')]}">
    <xsl:apply-templates select="*" />
  </dcterms:BibliographicResource>
 </rdf:RDF>
</xsl:template>

<xsl:template match="dc:title">
  <dcterms:title><xsl:value-of select="."/></dcterms:title>
</xsl:template>

<xsl:template match="dc:creator[1]">
 <dcterms:creator>
 <rdf:Seq>
   <rdf:li>
       <foaf:Person><foaf:name><xsl:value-of select="."/></foaf:name>
       </foaf:Person>
   </rdf:li>
   <xsl:for-each select="following-sibling::dc:creator">
       <rdf:li>
       <foaf:Person><foaf:name><xsl:value-of select="."/></foaf:name>
       </foaf:Person>
       </rdf:li>
  </xsl:for-each>
 </rdf:Seq>
 </dcterms:creator>
</xsl:template>

<xsl:template match="dc:language">
 <xsl:param name="ns" select="'http://id.loc.gov/vocabulary/iso639-1/'"/>
  <xsl:choose>
   <xsl:when test=".='ger'">
     <dcterms:language rdf:resource="{concat($ns,'de')}"/>
   </xsl:when>
   <xsl:when test=".='eng'">
     <dcterms:language rdf:resource="{concat($ns,'en')}"/>
   </xsl:when>
   <xsl:when test=".='fre'">
     <dcterms:language rdf:resource="{concat($ns,'fr')}"/>
   </xsl:when>
   <xsl:otherwise>
     <dcterms:language 
          rdf:resource="{concat('http://id.loc.gov/vocabulary/iso639-2/',.)}"/>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="dc:publisher">
 <dcterms:publisher><xsl:value-of select="."/></dcterms:publisher>
</xsl:template>

<xsl:template match="dc:subject">
 <dcterms:subject>
   <skos:Concept>
      <skos:prefLabel><xsl:value-of select="."/></skos:prefLabel>
   </skos:Concept>
 </dcterms:subject>
</xsl:template>

<xsl:template match="dc:source">
 <dcterms:source><xsl:value-of select="."/></dcterms:source>
</xsl:template>

<xsl:template match="dc:description">
 <dcterms:abstract><xsl:value-of select="."/></dcterms:abstract>
</xsl:template>

<xsl:template match="dc:type[text()='doc-type:doctoralThesis']">
 <dcterms:type rdf:resource="http://purl.org/spar/fabio/Journal"/>
</xsl:template>

<xsl:template match="dc:type[text()='doc-type:book']">
 <dcterms:type rdf:resource="http://purl.org/spar/fabio/Book"/>
</xsl:template>

<xsl:template match="dc:type[text()='doc-type:article']">
 <dcterms:type rdf:resource="http://purl.org/spar/fabio/JournalArticle"/>
</xsl:template>

<xsl:template match="dc:type[text()='doc-type:PeriodicalPart']">
 <dcterms:type rdf:resource="http://purl.org/spar/fabio/JournalIssue"/>
</xsl:template>

<xsl:template match="dc:date[1]">
  <xsl:choose>
  <xsl:when test="contains(.,'T')">
    <dcterms:issued><xsl:value-of select="substring-before(.,'T')"/></dcterms:issued>
  </xsl:when>
  <xsl:when test="string-length(.)=4">
    <dcterms:created><xsl:value-of select="."/></dcterms:created>
  </xsl:when>
  <xsl:otherwise>
    <dcterms:issued><xsl:value-of select="."/></dcterms:issued>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="dc:identifier[1]">
  <dcterms:identifier><xsl:value-of select="."/></dcterms:identifier>
</xsl:template>

<xsl:template match="text()"/>
</xsl:stylesheet>

