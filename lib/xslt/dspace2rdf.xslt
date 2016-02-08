<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:nlm="http://dtd.nlm.nih.gov/publishing/2.3"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     version="1.0" >

<xsl:output method="xml" indent="yes"/>

<xsl:template match="/">
 <rdf:RDF>
  <xsl:apply-templates/>
 </rdf:RDF>
</xsl:template>

<xsl:template match="document">
  <xsl:element name="fabio:{array[key/text()='dc.type']/value}">
    <xsl:attribute name="rdf:about">
       <xsl:value-of select="array[key/text()='dc.identifier.uri']/value"/>
    </xsl:attribute>
    <xsl:apply-templates/>
    <xsl:choose>
       <xsl:when test="count(array[key='dc.identifier'])=1"></xsl:when>
       <xsl:otherwise>
	   <dct:identifier>
       <xsl:value-of select="translate(substring-after(substring-after(
            substring-after(array[key/text()='dc.identifier.uri']/value,'//'),
                            '/'),'/'),'/',':')"/>
	   </dct:identifier>
       </xsl:otherwise>
    </xsl:choose>
  </xsl:element>
</xsl:template>

<xsl:template match="array">
  <xsl:apply-templates select="key"/>
</xsl:template>

<xsl:template match="array[starts-with(key,'dc.')]">
  <xsl:element name="dct:{substring-after(key,'dc.')}">
    <xsl:value-of select="value"/>
  </xsl:element>
</xsl:template>

<xsl:template match="array[key='dc.identifier.urn']">
    <dct:identifier><xsl:value-of select="value"/></dct:identifier>
</xsl:template>

<xsl:template match="array[key='dc.identifier.uri']">
  <xsl:variable name="uri" select="value"/>
  <xsl:choose>
    <xsl:when test="starts-with($uri, 'http://localhost')"></xsl:when>
    <xsl:when test="starts-with($uri, 'http://dx.doi.org/')">
       <fabio:hasDOI><xsl:value-of select="substring-after($uri,'doi.org/')"/>
       </fabio:hasDOI>
    </xsl:when>
    <xsl:otherwise>
       <!-- <dct:identifier><xsl:value-of select="$uri"/></dct:identifier> -->
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="array[key='dc.title.alternative']">
  <dct:title xml:lang="en"><xsl:value-of select="value"/></dct:title>
</xsl:template>

<xsl:template match="array[key='dc.contributor.author']">
 <xsl:variable name="aut">
     <xsl:call-template name="string-generic"/>
 </xsl:variable>
  <dct:creator>
    <foaf:Person rdf:about="{concat('http://localhost/aut/',$aut)}">
      <foaf:name><xsl:value-of select="value"/></foaf:name>
    </foaf:Person>
  </dct:creator>
</xsl:template>

<xsl:template match="array[key='dc.description.abstract']">
  <dct:abstract xml:lang="{substring(language,0,3)}">
    <xsl:value-of select="value"/>
  </dct:abstract>
</xsl:template>

<xsl:template match="array[key='dc.date.accessioned']">
</xsl:template>

<xsl:template match="array[key='dc.language.iso']">
  <dct:language>
    <xsl:value-of select="substring(language,0,3)"/>
  </dct:language>
</xsl:template>

<xsl:template match="array[key='dc.date.created']">
  <dct:created><xsl:value-of select="value"/></dct:created>
</xsl:template>

<xsl:template match="array[key='dc.date.issued']">
 <dct:issued>
 <xsl:choose><xsl:when test="contains(value,'T')">
     <xsl:value-of select="substring-before(value,'T')"/></xsl:when>
 <xsl:otherwise><xsl:value-of select="value"/></xsl:otherwise></xsl:choose>
 </dct:issued>
</xsl:template>

<xsl:template match="array[key='dc.date.available']">
 <xsl:choose>
 <xsl:when test="count(../array[key='dc.date.issued'])=0">
  <dct:issued><xsl:value-of select="substring-before(value,'T')"/></dct:issued>
 </xsl:when>
 <xsl:otherwise>
 <dct:dateSubmitted><xsl:value-of select="substring-before(value,'T')"/>
 </dct:dateSubmitted></xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="array[key='dc.date.updated']">
 <dct:modified>
     <xsl:value-of select="substring-before(value,'T')"/>
  </dct:modified>
</xsl:template>

<!-- map to dini publ type -->
<xsl:template match="array[key='dc.type']">
 <dct:type>
   <xsl:choose>
   <xsl:when test="value='JournalArticle'">
     <xsl:value-of select="'article'"/>
   </xsl:when>
     <xsl:otherwise><xsl:value-of select="value"/></xsl:otherwise>
   </xsl:choose>
 </dct:type>
</xsl:template>

<xsl:template name="string-generic">
 <xsl:param name="text" select="value"/>
 <xsl:value-of select="translate($text, translate($text, 
      'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzüäö', ''),'')"/>
</xsl:template>

<xsl:template match="@*|node()" priority="-1">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
</xsl:template>

</xsl:stylesheet>
