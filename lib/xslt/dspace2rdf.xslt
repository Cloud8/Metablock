<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     version="1.0" >

<xsl:output method="xml" indent="yes"/>

<xsl:template match="/">
 <rdf:RDF>
  <xsl:apply-templates select="document"/>
 </rdf:RDF>
</xsl:template>

<xsl:template match="document">
  <dcterms:BibliographicResource>
    <xsl:attribute name="rdf:about">
       <xsl:value-of select="array[key/text()='dc.identifier.uri']/value"/>
    </xsl:attribute>
    <xsl:apply-templates/>
    <xsl:choose>
       <xsl:when test="count(array[key='dc.identifier'])=1"></xsl:when>
       <xsl:otherwise>
	   <dcterms:identifier>
       <xsl:value-of select="translate(substring-after(substring-after(
            substring-after(array[key/text()='dc.identifier.uri']/value,'//'),
                            '/'),'/'),'/',':')"/>
	   </dcterms:identifier>
       </xsl:otherwise>
    </xsl:choose>
  </dcterms:BibliographicResource>
</xsl:template>

<xsl:template match="array[starts-with(key,'dc.')]">
  <xsl:element name="dcterms:{substring-after(key,'dc.')}">
    <xsl:value-of select="value"/>
  </xsl:element>
</xsl:template>

<xsl:template match="array[key='dc.identifier.urn']">
    <dcterms:identifier><xsl:value-of select="value"/></dcterms:identifier>
</xsl:template>

<xsl:template match="array[key='dc.identifier.uri']">
  <xsl:variable name="uri" select="value"/>
  <xsl:choose>
    <xsl:when test="starts-with($uri, 'http://localhost')"></xsl:when>
    <xsl:otherwise>
       <dcterms:identifier><xsl:value-of select="$uri"/></dcterms:identifier>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="array[key='dc.title.alternative']">
  <dcterms:title xml:lang="en"><xsl:value-of select="value"/></dcterms:title>
</xsl:template>

<xsl:template match="array[key='dc.contributor.author']">
 <xsl:variable name="aut">
     <xsl:call-template name="string-generic"/>
 </xsl:variable>
  <dcterms:creator>
    <foaf:Person rdf:about="{concat('http://localhost/aut/',$aut)}">
      <foaf:name><xsl:value-of select="value"/></foaf:name>
    </foaf:Person>
  </dcterms:creator>
</xsl:template>

<xsl:template match="array[key='dc.description.abstract']">
  <dcterms:abstract xml:lang="{substring(language,0,3)}">
    <xsl:value-of select="value"/>
  </dcterms:abstract>
</xsl:template>

<xsl:template match="array[key='dc.date.accessioned']">
</xsl:template>

<xsl:template match="array[key='dc.language.iso']">
  <dcterms:language>
    <xsl:value-of select="substring(language,0,3)"/>
  </dcterms:language>
</xsl:template>

<xsl:template match="array[key='dc.date.created']">
  <dcterms:created><xsl:value-of select="value"/></dcterms:created>
</xsl:template>

<xsl:template match="array[key='dc.date.issued']">
 <dcterms:issued>
 <xsl:choose><xsl:when test="contains(value,'T')">
     <xsl:value-of select="substring-before(value,'T')"/></xsl:when>
 <xsl:otherwise><xsl:value-of select="value"/></xsl:otherwise></xsl:choose>
 </dcterms:issued>
</xsl:template>

<xsl:template match="array[key='dc.date.available']">
 <xsl:choose>
 <xsl:when test="count(../array[key='dc.date.issued'])=0">
  <dcterms:issued><xsl:value-of select="substring-before(value,'T')"/></dcterms:issued>
 </xsl:when>
 <xsl:otherwise>
 <dcterms:dateSubmitted><xsl:value-of select="substring-before(value,'T')"/>
 </dcterms:dateSubmitted></xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="array[key='dc.date.updated']">
 <dcterms:modified>
     <xsl:value-of select="substring-before(value,'T')"/>
  </dcterms:modified>
</xsl:template>

<xsl:template match="array[key='dc.type']">
 <dcterms:type rdf:resource="{concat('http://purl.org/spar/fabio/',value)}"/>
</xsl:template>

<xsl:template match="array">
  <xsl:apply-templates select="key"/>
</xsl:template>

<xsl:template match="array[starts-with(key,'dcterms.')]">
  <xsl:element name="{translate(key,'.',':')}">
    <xsl:choose>
    <xsl:when test="language='null'"/>
    <xsl:when test="language">
    <xsl:attribute name="xml:lang">
          <xsl:value-of select="language"/>
      </xsl:attribute>
    </xsl:when>
    <xsl:otherwise>
    </xsl:otherwise>
    </xsl:choose>
    <xsl:value-of select="value"/>
  </xsl:element>
</xsl:template>

<xsl:template match="array/key" priority="-1">
  <xsl:comment><xsl:value-of select="."/>
  </xsl:comment>
</xsl:template>

<xsl:template name="string-generic">
 <xsl:param name="text" select="value"/>
 <xsl:value-of select="translate($text, translate($text, 
      'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzüäö', ''),'')"/>
</xsl:template>

<xsl:template match="@*|node()" priority="-2">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
</xsl:template>

</xsl:stylesheet>
