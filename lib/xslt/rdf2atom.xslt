<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:atom="http://www.w3.org/2005/Atom"
     xmlns:ore="http://www.openarchives.org/ore/atom/"
     version="1.0">

<!-- GH201502 GH201710 Atom version for DSpace convenience
     original atom http://www.w3.org/2005/Atom
     http://www.openarchives.org/ore/1.0/atom 
-->

<xsl:output encoding="UTF-8" indent="yes"/>

<xsl:template match="rdf:RDF">
  <xsl:apply-templates select="dcterms:BibliographicResource"/>
</xsl:template>

<xsl:template match="dcterms:BibliographicResource">
  <atom:entry xsi:schemaLocation="http://www.w3.org/2005/Atom http://www.kbcafe.com/rss/atom.xsd.xml">
    <xsl:apply-templates select="dcterms:*"/>
    <xsl:if test="count(dcterms:identifier[starts-with(.,'http')])=0">
    <atom:id><xsl:value-of select="@rdf:about"/></atom:id>
    <atom:link rel="alternate" href="{@rdf:about}"/>
    </xsl:if>
  </atom:entry>
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(.,'urn:')]">
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(.,'http')]">
  <atom:id><xsl:value-of select="."/></atom:id>
  <atom:link rel="alternate" href="{.}"/>
</xsl:template>

<xsl:template match="dcterms:abstract">
  <atom:summary><xsl:value-of select="."/></atom:summary>
</xsl:template>

<xsl:template match="dcterms:references">
</xsl:template>

<xsl:template match="dcterms:language">
</xsl:template>

<xsl:template match="dcterms:title">
  <atom:title><xsl:value-of select="."/></atom:title>
</xsl:template>

<xsl:template match="dcterms:modified">
  <atom:updated><xsl:value-of select="."/></atom:updated>
</xsl:template>

<xsl:template match="dcterms:published">
  <atom:published><xsl:value-of select="."/></atom:published>
</xsl:template>

<xsl:template match="dcterms:creator">
  <atom:author>
    <xsl:apply-templates select="rdf:Seq/rdf:li/foaf:Person/foaf:name"/>
  </atom:author>
</xsl:template>

<xsl:template match="rdf:Seq/rdf:li/foaf:Person/foaf:name">
  <atom:name><xsl:value-of select="."/></atom:name>
</xsl:template>

<xsl:template match="foaf:img">
</xsl:template>

<xsl:template match="dcterms:hasPart[dctypes:Text]">
  <xsl:variable name="title"><xsl:call-template name="substring-after-last">
  <xsl:with-param name="input" select="dctypes:Text/@rdf:about"/>
  </xsl:call-template></xsl:variable>
  <atom:link rel="http://www.openarchives.org/ore/terms/aggregates" 
   type="{dctypes:Text/dcterms:format/dcterms:MediaTypeOrExtent/rdfs:label}"
   href="{dctypes:Text/@rdf:about}" title="{$title}" />
  <ore:triples>
    <xsl:apply-templates select="dctypes:Text"/>
  </ore:triples>
</xsl:template>

<xsl:template match="dcterms:hasPart/dctypes:Text">
  <rdf:Description rdf:about="{@rdf:about}">
      <rdf:type rdf:resource="http://purl.org/dc/dcmitype/Text"/>
      <dcterms:description>ORIGINAL</dcterms:description>
  </rdf:Description>
</xsl:template>

<xsl:template name="substring-after-last">
  <xsl:param name="input"/>
  <xsl:variable name="temp" select="substring-after($input,'/')"/>
  <xsl:choose>
     <xsl:when test="contains($temp,'/')">
          <xsl:call-template name="substring-after-last">
               <xsl:with-param name="input" select="$temp"/>
          </xsl:call-template>
     </xsl:when>
     <xsl:otherwise><xsl:value-of select="$temp"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="@*|node()" prioriry="-1">
  <!-- <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy> -->
</xsl:template>

</xsl:stylesheet>


