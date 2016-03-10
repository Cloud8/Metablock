<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:ore="http://www.openarchives.org/ore/terms/"
     version="1.0">

<!-- GH2015-02 : create ORE Map -->
<!-- http://www.openarchives.org/ore/1.0/rdfxml -->
<xsl:output encoding="UTF-8" indent="yes"/>

<xsl:template match="rdf:RDF">
 <rdf:RDF xsi:schemaLocation="http://www.w3.org/1999/02/22-rdf-syntax-ns# http://www.openarchives.org/OAI/2.0/rdf.xsd">
  <xsl:apply-templates select="dcterms:BibliographicResource" mode="rem"/>
  <xsl:apply-templates select="dcterms:BibliographicResource" />
 </rdf:RDF>
</xsl:template>

<xsl:template match="dcterms:BibliographicResource" mode="rem">
  <ore:ResourceMap rdf:about="{concat(@rdf:about,'/about.rdf')}">
    <ore:describes rdf:resource="{@rdf:about}" />
    <dcterms:creator>
      <foaf:Organization rdf:about="http://d-nb.info/gnd/11210-0">
        <foaf:name>Universitätsbibliothek Marburg</foaf:name>
      </foaf:Organization>
    </dcterms:creator>
    <xsl:choose>
    <xsl:when test="dcterms:modified">
    <dcterms:modified><xsl:value-of select="dcterms:modified"/></dcterms:modified>
    </xsl:when>
    <xsl:when test="dcterms:issued">
    <dcterms:modified><xsl:value-of select="dcterms:issued"/></dcterms:modified>
    </xsl:when>
    </xsl:choose>
    <dcterms:rights rdf:resource="http://creativecommons.org/licenses/by-nc/2.5/"/>
  </ore:ResourceMap>
</xsl:template>

<xsl:template match="dcterms:abstract">
</xsl:template>

<xsl:template match="dcterms:references">
</xsl:template>

<xsl:template match="@*|node()">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
</xsl:template>

</xsl:stylesheet>


