<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" 
     xmlns:dc="http://purl.org/dc/elements/1.1/" 
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:opus="http://localhost/"
     version="1.0" >

<!-- 
 /**
  * @license http://www.apache.org/licenses/LICENSE-2.0
  * @author Goetz Hatop
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
  <xsl:element name="fabio:{dc:type}">
    <xsl:attribute name="rdf:about"><xsl:value-of select="dc:identifier"/>
    </xsl:attribute>
    <xsl:apply-templates select="*" />
  </xsl:element>
 </rdf:RDF>
</xsl:template>

<xsl:template match="dc:title">
  <dct:title><xsl:value-of select="."/></dct:title>
</xsl:template>

<xsl:template match="dc:creator">
 <dct:creator><xsl:value-of select="."/></dct:creator>
</xsl:template>

<xsl:template match="dc:language">
  <dct:language><xsl:value-of select="."/></dct:language>
</xsl:template>

<xsl:template match="dc:publisher">
 <dct:publisher><xsl:value-of select="."/></dct:publisher>
</xsl:template>

<xsl:template match="dc:subject">
 <dct:subject><xsl:value-of select="."/></dct:subject>
</xsl:template>

<xsl:template match="dc:source">
 <dct:source><xsl:value-of select="."/></dct:source>
</xsl:template>

<xsl:template match="dc:description">
 <dct:description><xsl:value-of select="."/></dct:description>
</xsl:template>

<xsl:template match="dc:relation">
  <xsl:choose>
  <xsl:when test="starts-with(., 'http://') and contains(., 'article/view')">
   <dct:relation>
      <xsl:value-of select="concat(substring-before(.,'view'),'download',substring-after(.,'view'))"/>
   </dct:relation>
  </xsl:when>
  <xsl:when test="starts-with(. , 'http://')">
    <dct:relation><xsl:value-of select="."/></dct:relation>
  </xsl:when>
  <xsl:when test="starts-with(. , 'urn:')">
    <dct:identifier><xsl:value-of select="."/></dct:identifier>
  </xsl:when>
  <xsl:otherwise>
    <dct:relation><xsl:value-of select="."/></dct:relation>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="text()"/>
</xsl:stylesheet>

