<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:ore="http://www.openarchives.org/ore/terms/"
     version="1.0">

<!-- GH2015-02 : create ORE Map -->
<!-- http://www.openarchives.org/ore/1.0/rdfxml -->
<xsl:output encoding="UTF-8" indent="yes"/>

<xsl:template match="rdf:RDF">
 <rdf:RDF xsi:schemaLocation="http://www.w3.org/1999/02/22-rdf-syntax-ns# http://www.openarchives.org/OAI/2.0/rdf.xsd">
  <xsl:apply-templates select="fabio:*" mode="rem"/>
  <xsl:apply-templates select="fabio:*" />
 </rdf:RDF>
</xsl:template>

<xsl:template match="fabio:*" mode="rem">
  <ore:ResourceMap rdf:about="{concat(@rdf:about,'/about.rdf')}">
    <ore:describes rdf:resource="{@rdf:about}" />
    <dct:creator>
      <foaf:Organization rdf:about="http://d-nb.info/gnd/11210-0">
        <foaf:name>Universitätsbibliothek Marburg</foaf:name>
      </foaf:Organization>
    </dct:creator>
    <xsl:choose>
    <xsl:when test="dct:modified">
    <dct:modified><xsl:value-of select="dct:modified"/></dct:modified>
    </xsl:when>
    <xsl:when test="dct:issued">
    <dct:modified><xsl:value-of select="dct:issued"/></dct:modified>
    </xsl:when>
    </xsl:choose>
    <dct:rights rdf:resource="http://creativecommons.org/licenses/by-nc/2.5/"/>
  </ore:ResourceMap>
</xsl:template>

<xsl:template match="dct:abstract">
</xsl:template>

<xsl:template match="dct:references">
</xsl:template>

<xsl:template match="@*|node()">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
</xsl:template>

</xsl:stylesheet>


