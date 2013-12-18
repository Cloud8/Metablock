<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:frbr="http://purl.org/vocab/frbr/core#"
     xmlns:nlp="http://localhost/view/nlp/"
     version="1.0" >

<xsl:output method="xml" encoding="UTF-8" indent="yes" />

<xsl:template match="rdf:RDF">
 <add>
    <xsl:apply-templates select="rdf:Description"/>
 </add>
</xsl:template>

<xsl:template match="rdf:Description">
 <doc>
    <xsl:apply-templates select="nlp:*"/>
 </doc>
</xsl:template>

<!-- identifiers from solr -->
<xsl:template match="nlp:id">
  <field name="id"><xsl:value-of select="." /></field>
</xsl:template>

<!-- identifiers from mysql -->
<xsl:template match="nlp:oid">
  <field name="id"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="nlp:title">
  <field name="title"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="nlp:name">
  <xsl:if test=".!=''">
  <field name="keywords"><xsl:value-of select="."/></field>
  </xsl:if>
</xsl:template>

<xsl:template match="nlp:company">
  <xsl:if test=".!=''">
  <field name="subject"><xsl:value-of select="."/></field>
  </xsl:if>
</xsl:template>

<xsl:template match="nlp:location">
  <xsl:if test=".!=''">
  <field name="keywords"><xsl:value-of select="."/></field>
  </xsl:if>
</xsl:template>

<xsl:template match="nlp:changeRecommendation">
  <xsl:if test=".!=''">
  <field name="category"><xsl:value-of select="."/></field>
  </xsl:if>
</xsl:template>

<xsl:template match="nlp:pubDate">
 <field name="last_modified">
    <xsl:value-of select="concat(substring(.,7,4),'-',substring(.,4,2),
                          '-',substring(.,1,2),'T',substring(.,14,2),
                          ':',substring(.,17,2),':00Z')"/>
 </field>
</xsl:template>

<!-- from mysql -->
<xsl:template match="nlp:statement">
  <field name="fulltext"><xsl:value-of select="."/></field>
  <field name="description"><xsl:value-of select="."/></field>
</xsl:template>

<!-- from Webrawl -->
<xsl:template match="nlp:fulltext">
  <field name="fulltext"><xsl:value-of select="normalize-space(.)"/></field>
</xsl:template>

<!-- from Summarizer -->
<xsl:template match="nlp:summary">
  <field name="description"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="nlp:category">
  <field name="category"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="nlp:url">
  <field name="url"><xsl:value-of select="."/></field>
</xsl:template>

<!--
<xsl:template match="nlp:description">
  <field name="description"><xsl:value-of select="normalize-space(.)"/></field>
</xsl:template>
-->

<!-- suppress emptyness -->
<xsl:template match="text()"/>

</xsl:stylesheet>
