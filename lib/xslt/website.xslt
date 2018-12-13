<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:frbr="http://purl.org/vocab/frbr/core#"
     version="1.0" >

<xsl:output method="xml" encoding="UTF-8" indent="yes" />

<xsl:template match="rdf:RDF">
 <add>
    <xsl:apply-templates select="rdf:Description"/>
 </add>
</xsl:template>

<xsl:template match="rdf:Description">
 <doc>
    <field name="url"><xsl:value-of select="@rdf:about" /></field>
    <xsl:apply-templates select="*"/>
 </doc>
</xsl:template>

<xsl:template match="dcterms:identifier">
  <field name="id"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="dcterms:title">
  <field name="title"><xsl:value-of select="."/></field>
</xsl:template>

<!--
<xsl:template match="dcterms:description">
  <field name="fulltext"><xsl:value-of select="."/></field>
  <field name="description">
      <xsl:value-of select="substring(.,0,400)"/>
  </field>
</xsl:template>
-->

<xsl:template match="dcterms:abstract">
  <field name="fulltext"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:tableOfContents">
  <field name="description"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:subject">
    <xsl:apply-templates select="skos:Concept/skos:prefLabel"/>
    <xsl:apply-templates select="skos:Concept/skos:notation"/>
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept/skos:prefLabel">
  <field name="category"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept/skos:notation">
  <field name="subject"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:modified">
  <field name="last_indexed"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:issued">
  <field name="last_modified"><xsl:value-of select="."/></field>
</xsl:template>

<!-- missing
  <field name="first_indexed"><xsl:value-of select="."/></field>
  <field name="keywords"><xsl:value-of select="."/></field>
  <field name="use_count"><xsl:value-of select="."/></field>
-->

<!-- suppress emptyness -->
<xsl:template match="text()"/>

</xsl:stylesheet>
