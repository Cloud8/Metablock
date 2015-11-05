<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:marc="http://www.loc.gov/MARC21/slim"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:frbr="http://purl.org/vocab/frbr/core#"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:ore="http://www.openarchives.org/ore/terms/"
     xmlns:pica="http://www.ub.uni-marburg.de/webcat/card/ppn/"
     version="1.0">

<!--
 /**
  * @license http://www.apache.org/licenses/LICENSE-2.0
  * @author Goetz Hatop 
  * @title RDF Transformer for MARC XML format
  * @date 2015-10-28
 **/
 -->

<xsl:output method="xml" indent="yes" encoding="UTF-8" />
<xsl:strip-space elements="*"/>

<xsl:template match="/">
  <xsl:apply-templates select="marc:record" />
</xsl:template>

<xsl:template match="marc:record">
 <rdf:RDF>
  <xsl:comment> marc2rdf Transformer UB Marburg (2015) </xsl:comment>
  <dct:BibliograpohicResource rdf:about="{marc:datafield[@tag='856']/marc:subfield[@code='u']}">
   <xsl:apply-templates select="marc:datafield" />
  </dct:BibliograpohicResource>
 </rdf:RDF>
</xsl:template>

<xsl:template match="marc:datafield[@tag='245']">
  <xsl:apply-templates select="marc:subfield"/>
</xsl:template>

<xsl:template match="marc:datafield[@tag='245']/marc:subfield[@code='a']">
  <dct:title><xsl:value-of select="."/></dct:title>
</xsl:template>

<xsl:template match="marc:datafield[@tag='100']">
  <xsl:apply-templates select="marc:subfield"/>
</xsl:template>

<xsl:template match="marc:datafield[@tag='100']/marc:subfield[@code='a']">
  <dct:creator><xsl:value-of select="."/></dct:creator>
</xsl:template>

<xsl:template match="@*|node()">
</xsl:template>

</xsl:stylesheet>
