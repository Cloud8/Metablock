<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:nlp="http://localhost/view/nlp/"
     xmlns:dct="http://purl.org/dc/terms/" 
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:skos="http://www.w3.org/2008/05/skos#"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     version="1.0" >

<!-- /**
      * @license http://www.apache.org/licenses/LICENSE-2.0
      * @author Goetz Hatop <fb.com/goetz.hatop>
      * @title A XSLT Transformer for XMetadissPlus to RDF 
      * @date 2011-01-02
      **/ -->
<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="rdf:RDF">
 <rdf:RDF>
   <xsl:apply-templates select="rdf:Description" />
 </rdf:RDF>
</xsl:template>

<xsl:template match="rdf:Description">
 <xsl:copy>
 <xsl:copy-of select="@rdf:about"/>
   <xsl:apply-templates select="nlp:*" />
 </xsl:copy>
</xsl:template>

<xsl:template match="nlp:id">
  <dct:identifier><xsl:value-of select="."/></dct:identifier>
</xsl:template>

<xsl:template match="nlp:title">
  <dct:title><xsl:value-of select="."/></dct:title>
</xsl:template>

<xsl:template match="nlp:author">
  <dct:creator><xsl:value-of select="."/></dct:creator>
</xsl:template>

<xsl:template match="nlp:publishDate">
  <dct:publishDate><xsl:value-of select="."/></dct:publishDate>
</xsl:template>

<xsl:template match="nlp:description">
  <dct:abstract><xsl:value-of select="."/></dct:abstract>
</xsl:template>

<xsl:template match="nlp:fulltext">
  <dct:fulltext><xsl:value-of select="."/></dct:fulltext>
</xsl:template>

<xsl:template match="nlp:topic">
  <dct:subject><xsl:value-of select="."/></dct:subject>
</xsl:template>

<xsl:template match="nlp:contents">
  <dct:references><xsl:value-of select="."/></dct:references>
</xsl:template>

<xsl:template match="nlp:url">
  <dct:relation><xsl:value-of select="."/></dct:relation>
</xsl:template>

<!-- garbage protection -->
<xsl:template match="text()"/>

</xsl:stylesheet>

