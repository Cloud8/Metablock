<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:gnd="http://d-nb.info/standards/elementset/gnd#"
     version="1.0" >

<xsl:output method="xml" encoding="UTF-8" 
     indent="yes" omit-xml-declaration="no"/>

<xsl:template match="rdf:RDF">
  <xsl:apply-templates select="gnd:DifferentiatedPerson" />
</xsl:template>

<xsl:template match="gnd:DifferentiatedPerson">
 <add>
  <xsl:comment> RDF Transformer UB Marburg 2013 </xsl:comment>
   <doc>
      <!-- RECORDTYPE -->
      <!-- <field name="recordtype">gnd</field> -->

      <!-- IDENTIFIER -->
      <xsl:apply-templates select="gnd:gndIdentifier" />

      <!-- FULLRECORD -->
      <field name="fullrecord"><xsl:value-of select="." /></field>

      <!-- ALLFIELDS -->
      <field name="allfields"><xsl:value-of select="." /></field>

      <!-- SOURCE -->
      <field name="source"><xsl:value-of select="@rdf:about" /></field>
  </doc>
 </add>
</xsl:template>

<xsl:template match="gnd:gndIdentifier">
  <field name="id"><xsl:value-of select="." /></field>
</xsl:template>

<!-- identity template -->
<xsl:template match="@*|node()">
    <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
</xsl:template>

</xsl:stylesheet>
