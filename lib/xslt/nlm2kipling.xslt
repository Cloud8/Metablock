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
     xmlns:prism="http://prismstandard.org/namespaces/basic/2.0/"
     xmlns:jats="http://jats.nlm.nih.gov/publishing/1.1d1/"
     version="1.0" >

<!-- /** @license http://www.apache.org/licenses/LICENSE-2.0
       * @author Goetz Hatop <fb.com/goetz.hatop>
       * @title An XSLT Transformer for NLM to RDF
       * @date 2014-11-26
      **/ -->

<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/">
  <xsl:apply-templates select="*" />
</xsl:template>

<!--
         xsi:schemaLocation="http://jats.nlm.nih.gov/publishing/1.1d1/ http://jats.nlm.nih.gov/publishing/1.1d1/xsd/JATS-journalpublishing1.xsd"
-->

<xsl:template match="nlm:article">
<xsl:text disable-output-escaping="yes">&lt;!DOCTYPE article PUBLIC "-//NLM//DTD Journal Publishing DTD v3.0 20080202//EN" "journalpublishing3.dtd"&gt;
</xsl:text>
<article article-type="research-article" xml:lang="en" 
         xmlns:mml="http://www.w3.org/1998/Math/MathML" 
         xmlns:xlink="http://www.w3.org/1999/xlink" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns:jats="http://jats.nlm.nih.gov/publishing/1.1d1/">
    <xsl:apply-templates select="*" />
</article>
</xsl:template>

<xsl:template match="nlm:trans-title">
    <xsl:apply-templates select="*" />
</xsl:template>

<xsl:template match="nlm:journal-title">
    <xsl:apply-templates select="*" />
</xsl:template>

<xsl:template match="nlm:issue-id">
    <xsl:apply-templates select="*" />
</xsl:template>

<xsl:template match="nlm:email">
    <xsl:apply-templates select="*" />
</xsl:template>

<xsl:template match="nlm:aff">
    <xsl:apply-templates select="*" />
</xsl:template>

<xsl:template match="nlm:kwd-group[@xml:lang]">
 <kwd-group>
    <xsl:apply-templates select="nlm:kwd" />
 </kwd-group>
</xsl:template>

<xsl:template match="nlm:abstract[@xml:lang]">
 <abstract>
    <xsl:apply-templates select="*" />
 </abstract>
</xsl:template>

<xsl:template match="nlm:self-uri">
    <xsl:apply-templates select="*" />
</xsl:template>

<xsl:template match="nlm:*">
  <xsl:element name="{name()}">
    <xsl:apply-templates select="@*|node()" />
  </xsl:element>
</xsl:template>

<xsl:template match="@*|node()" priority="-1">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
</xsl:template>

</xsl:stylesheet>

