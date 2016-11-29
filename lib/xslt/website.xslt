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
    <xsl:apply-templates select="*"/>
    <field name="url"><xsl:value-of select="@rdf:about" /></field>
 </doc>
</xsl:template>

<xsl:template match="dcterms:identifier">
  <field name="id"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="dcterms:title">
  <field name="title"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:description">
  <field name="fulltext"><xsl:value-of select="normalize-space(.)"/></field>
  <field name="description">
      <xsl:value-of select="substring(normalize-space(.),0,256)"/>
  </field>
</xsl:template>

<xsl:template match="dcterms:subject">
    <xsl:apply-templates select="skos:Concept"/>
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept[contains(@rdf:about,'cat')]">
  <field name="category"><xsl:value-of select="normalize-space(.)"/></field>
</xsl:template>

<!-- suppress emptyness -->
<xsl:template match="text()"/>

</xsl:stylesheet>
