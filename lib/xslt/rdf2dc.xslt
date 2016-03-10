<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:ore="http://www.openarchives.org/ore/terms/"

     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
     exclude-result-prefixes="rdf dcterms foaf aiiso skos ore"
     version="1.0" >

<xsl:output method="xml" standalone="yes" encoding="UTF-8" indent="yes" />

<xsl:template match="rdf:RDF">
 <oai_dc:dc xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
    <xsl:apply-templates select="dcterms:BibliographicResource" />
 </oai_dc:dc>
</xsl:template>

<xsl:template match="dcterms:BibliographicResource">
  <xsl:comment> Dublin Core Transformer UB Marburg 2015 </xsl:comment>
  <xsl:apply-templates select="dcterms:title[not(@xml:lang)]"/>
  <xsl:apply-templates select="dcterms:title[@xml:lang]"/>
  <xsl:apply-templates select="dcterms:creator"/>
  <xsl:apply-templates select="dcterms:subject"/>
  <xsl:apply-templates select="dcterms:abstract"/>

  <xsl:apply-templates select="dcterms:publisher"/>
  <xsl:if test="count(dcterms:publisher)=0">
  <xsl:apply-templates select="dcterms:isPartOf/*/dcterms:publisher"/>
  </xsl:if>
  <xsl:apply-templates select="dcterms:contributor"/>

  <!--
  <xsl:apply-templates select="dcterms:created"/>
  <xsl:apply-templates select="dcterms:dateAccepted"/>
  <xsl:apply-templates select="dcterms:modified"/>
  -->
  <xsl:apply-templates select="dcterms:issued"/>
  <xsl:apply-templates select="dcterms:type"/>
  <!--
  <xsl:apply-templates select="dcterms:format"/>
  -->

  <xsl:apply-templates select="dcterms:source"/>
  <xsl:apply-templates select="dcterms:language"/> 
  <xsl:apply-templates select="dcterms:rights"/> 

  <dc:identifier><xsl:value-of select="@rdf:about"/></dc:identifier>
</xsl:template>

<xsl:template match="dcterms:title[not(@xml:lang)]">
  <dc:title><xsl:value-of select="."/></dc:title>
</xsl:template>

<xsl:template match="dcterms:title[@xml:lang]">
  <dc:title><xsl:value-of select="."/></dc:title>
</xsl:template>

<xsl:template match="dcterms:creator">
  <xsl:apply-templates select="foaf:Person"/>
  <xsl:apply-templates select="rdf:Seq"/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq">
  <xsl:apply-templates select="rdf:li"/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li">
  <xsl:apply-templates select="foaf:Person"/>
</xsl:template>

<xsl:template match="dcterms:creator/foaf:Person|dcterms:creator/rdf:Seq/rdf:li/foaf:Person">
  <dc:creator>
     <xsl:choose>
     <xsl:when test="foaf:familyName">
       <xsl:apply-templates select="foaf:familyName"/>
       <xsl:apply-templates select="foaf:givenName"/>
     </xsl:when>
       <xsl:otherwise><xsl:apply-templates select="foaf:name"/></xsl:otherwise>
     </xsl:choose>
    <xsl:apply-templates select="foaf:title"/>
  </dc:creator>
</xsl:template>

<xsl:template match="foaf:Person/foaf:familyName">
  <xsl:value-of select="."/>
</xsl:template>

<xsl:template match="foaf:Person/foaf:givenName">
  <xsl:value-of select="concat(', ',.)"/>
</xsl:template>

<xsl:template match="foaf:Person/foaf:title">
  <xsl:value-of select="concat(' (',.,')')"/>
</xsl:template>

<xsl:template match="foaf:Person/foaf:name">
     <xsl:value-of select="."/>
</xsl:template>

<xsl:template match="dcterms:contributor">
  <xsl:apply-templates select="foaf:Person"/>
</xsl:template>

<xsl:template match="dcterms:contributor/foaf:Person[foaf:role]">
  <dc:contributor>
     <xsl:choose>
     <xsl:when test="foaf:familyName">
       <xsl:apply-templates select="foaf:familyName"/>
       <xsl:apply-templates select="foaf:givenName"/>
     </xsl:when>
       <xsl:otherwise><xsl:apply-templates select="foaf:name"/></xsl:otherwise>
     </xsl:choose>
    <xsl:apply-templates select="foaf:title"/>
  </dc:contributor>
</xsl:template>

<xsl:template match="dcterms:publisher[@rdf:resource]">
  <xsl:apply-templates select="../dcterms:isPartOf/*/dcterms:publisher"/>
</xsl:template>

<xsl:template match="dcterms:publisher">
    <xsl:apply-templates select="foaf:Organization"/>
</xsl:template>

<xsl:template match="dcterms:publisher/foaf:Organization">
 <dc:publisher><xsl:value-of select="foaf:name"/></dc:publisher>
</xsl:template>

<xsl:template match="dcterms:subject">
  <xsl:apply-templates select="skos:Concept" />
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept">
  <xsl:apply-templates select="skos:prefLabel" />
  <xsl:apply-templates select="rdfs:label" />
</xsl:template>

<xsl:template match="skos:Concept/skos:prefLabel">
  <dc:subject><xsl:value-of select="."/></dc:subject>
</xsl:template>

<xsl:template match="skos:Concept/rdfs:label">
  <dc:subject><xsl:value-of select="."/></dc:subject>
</xsl:template>

<xsl:template match="dcterms:abstract[@xml:lang]">
 <dc:description><xsl:value-of select="."/></dc:description>
</xsl:template>

<xsl:template match="dcterms:dateAccepted">
  <dc:dateAccepted><xsl:value-of select="."/></dc:dateAccepted>
</xsl:template>

<xsl:template match="dcterms:created">
  <dc:created><xsl:value-of select="."/></dc:created>
</xsl:template>

<xsl:template match="dcterms:issued">
  <dc:date><xsl:value-of select="."/></dc:date>
</xsl:template>

<xsl:template match="dcterms:modified">
  <dc:modified><xsl:value-of select="."/></dc:modified>
</xsl:template>

<xsl:template match="dcterms:type">
    <dc:type><xsl:value-of select="concat('doc-type:',.)"/></dc:type>
</xsl:template>

<xsl:template match="dcterms:identifier">
  <dc:identifier><xsl:value-of select="."/></dc:identifier>
</xsl:template>

<xsl:template match="dcterms:language">
 <xsl:variable name="lang">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="."/>
   </xsl:call-template>
 </xsl:variable>
 <dc:language>
    <xsl:value-of select="$lang"/>
 </dc:language>
</xsl:template>

<xsl:template match="dcterms:isPartOf/*/dcterms:title">
 <dc:isPartOf><xsl:value-of select="."/></dc:isPartOf>
</xsl:template>

<xsl:template match="dcterms:rights[@rdf:resource]">
  <dc:rights><xsl:value-of select="@rdf:resource"/></dc:rights>
</xsl:template>

<xsl:template match="dcterms:rights[not(@rdf:resource)]">
  <dc:rights><xsl:value-of select="."/></dc:rights>
</xsl:template>

<!-- suppress emptyness -->
<xsl:template match="text()"/>
<xsl:template match="*" priority="-1"/>

<xsl:template name="getlang">
 <xsl:param name="input"/>
  <xsl:choose>
   <xsl:when test="$input='de'">ger</xsl:when>
   <xsl:when test="$input='en'">eng</xsl:when>
   <xsl:when test="$input='fr'">fre</xsl:when>
   <xsl:when test="$input='nl'">ndl</xsl:when>
   <xsl:when test="$input='es'">spa</xsl:when>
   <xsl:when test="$input='it'">ita</xsl:when>
   <xsl:when test="$input='lt'">lat</xsl:when>
   <xsl:otherwise><xsl:value-of select="$input"/></xsl:otherwise>
 </xsl:choose>
</xsl:template>

</xsl:stylesheet>
