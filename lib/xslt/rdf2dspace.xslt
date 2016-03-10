<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:nlm="http://dtd.nlm.nih.gov/publishing/2.3"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     version="1.0" >

<xsl:output method="xml" indent="yes"/>

<!-- see dspace config/crosswalks/QDC.properties -->
<xsl:template match="/">
  <xsl:apply-templates select="rdf:RDF/dcterms:BibliographicResource"/>
</xsl:template>

<xsl:template match="/rdf:RDF/dcterms:BibliographicResource[dcterms:identifier]">
  <item>
    <xsl:apply-templates select="dcterms:*"/>
    <metadata>
      <language>de_DE</language>
      <value><xsl:value-of select="@rdf:about"/></value>
      <key>dc.identifier.uri</key>
    </metadata>
  </item>
</xsl:template>

<xsl:template match="dcterms:creator">
  <xsl:apply-templates select="foaf:Person"/>
  <xsl:apply-templates select="rdf:Seq/rdf:li/foaf:Person"/>
</xsl:template>

<xsl:template match="dcterms:creator/foaf:Person">
  <metadata>
    <language>de_DE</language>
    <value><xsl:value-of select="foaf:name"/></value>
    <key>dc.contributor.author</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li/foaf:Person">
  <metadata>
    <language>de_DE</language>
    <value><xsl:value-of select="foaf:name"/></value>
    <key>dc.contributor.author</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:identifier">
  <metadata>
    <language>de_DE</language>
    <value><xsl:value-of select="."/></value>
    <key>dc.identifier</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:abstract[@xml:lang='de']">
  <metadata>
    <language>de_DE</language>
    <value><xsl:value-of select="."/></value>
    <key>dc.description.abstract</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:abstract[@xml:lang='en']">
  <metadata>
    <language>en_US</language>
    <value><xsl:value-of select="."/></value>
    <key>dc.description.abstract</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:created">
  <metadata>
    <language>en_US</language>
    <value><xsl:value-of select="."/></value>
    <key>dc.date.created</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:issued">
  <metadata>
    <language>en_US</language>
    <value><xsl:value-of select="concat(.,'T08:18:27Z')"/></value>
    <key>dc.date.issued</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:dateAccepted">
  <metadata>
    <language>en_US</language>
    <value><xsl:value-of select="concat(.,'T08:18:27Z')"/></value>
    <key>dc.date.accessioned</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:title[@xml:lang='de']">
  <metadata>
    <language>de_DE</language>
    <value><xsl:value-of select="."/></value>
    <key>dc.title</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:title[@xml:lang='en']">
  <metadata>
    <language>en_US</language>
    <value><xsl:value-of select="."/></value>
    <key>dc.title</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:title[not(@xml:lang)]">
  <metadata>
    <value><xsl:value-of select="."/></value>
    <key>dc.title</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:type">
  <metadata>
    <language>en_US</language>
    <value><xsl:value-of select="."/></value>
    <key>dc.type</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:contributor">
  <xsl:apply-templates select="foaf:Person"/>
</xsl:template>

<xsl:template match="dcterms:contributor/foaf:Person">
  <metadata>
    <language>de_DE</language>
    <value><xsl:value-of select="foaf:name"/></value>
    <key>dc.contributor.advisor</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:format">
  <metadata>
    <language>en_US</language>
    <value><xsl:value-of select="."/></value>
    <key>dc.format</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:language">
  <metadata>
    <language>en_US</language>
    <value><xsl:value-of select="."/></value>
    <key>dc.language</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:publisher">
 <xsl:apply-templates select="foaf:Organization"/>
</xsl:template>

<xsl:template match="dcterms:publisher/foaf:Organization">
  <metadata>
    <language>de_DE</language>
    <value><xsl:value-of select="foaf:name"/></value>
    <key>dc.publisher</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:*">
</xsl:template>

<xsl:template match="@*|node()" priority="-1">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
</xsl:template>

</xsl:stylesheet>
