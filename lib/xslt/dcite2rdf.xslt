<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:dcite="http://datacite.org/schema/kernel-4"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:sco="http://schema.org/"
     version="1.0" >

<!-- 
    @title XSLT Transformer for DataCite to RDF 
    @date 2018-02-28
-->
<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

<xsl:template match="/">
  <xsl:apply-templates select="dcite:resource"/>
</xsl:template>

<xsl:template match="dcite:resource">
 <rdf:RDF>
  <dcterms:BibliographicResource rdf:about="{dcite:alternateIdentifiers/dcite:alternateIdentifier[@alternateIdentifierType='URL']}">
    <xsl:apply-templates select="dcite:identifier"/>
    <xsl:apply-templates select="dcite:creators"/>
    <xsl:apply-templates select="dcite:titles"/>
    <xsl:apply-templates select="dcite:publisher"/>
    <xsl:apply-templates select="dcite:publicationYear"/>
    <xsl:apply-templates select="dcite:subjects"/>
    <xsl:apply-templates select="dcite:contributors"/>
    <xsl:apply-templates select="dcite:dates"/>
    <xsl:apply-templates select="dcite:language"/>
    <xsl:apply-templates select="dcite:resourceType"/>
    <xsl:apply-templates select="dcite:alternateIdentifiers"/>
    <xsl:apply-templates select="dcite:relatedIdentifiers"/>
    <xsl:apply-templates select="dcite:formats"/>
    <xsl:apply-templates select="dcite:rightsList"/>
    <xsl:apply-templates select="dcite:descriptions"/>

    <!-- TODO : contributors relatedIdentifiers formats rightsList -->
  </dcterms:BibliographicResource>
 </rdf:RDF>
</xsl:template>

<xsl:template match="dcite:titles">
  <xsl:apply-templates select="dcite:title" />
</xsl:template>

<xsl:template match="dcite:title">
  <dcterms:title><xsl:copy-of select="@xml:lang"/>
     <xsl:value-of select="."/>
  </dcterms:title>
</xsl:template>

<xsl:template match="dcite:title[@titleType='TranslatedTitle']">
  <!-- other option:
  <dcterms:alternative><xsl:value-of select="."/></dcterms:alternative>
  -->
  <dcterms:title><xsl:copy-of select="@xml:lang"/>
     <xsl:value-of select="."/>
  </dcterms:title>
</xsl:template>

<xsl:template match="dcite:creators">
  <dcterms:creator>
     <rdf:Seq>
       <xsl:apply-templates select="dcite:creator" />
     </rdf:Seq>
  </dcterms:creator>
</xsl:template>

<xsl:template match="dcite:creator">
  <rdf:li><foaf:Person><foaf:name>
      <xsl:value-of select="dcite:creatorName"/>
  </foaf:name></foaf:Person></rdf:li>
</xsl:template>

<xsl:template match="dcite:publisher">
  <dcterms:publisher><foaf:Organization><foaf:name>
    <xsl:value-of select="."/></foaf:name></foaf:Organization>
  </dcterms:publisher>
</xsl:template>

<xsl:template match="dcite:language">
  <dcterms:language 
       rdf:resource="{concat('http://id.loc.gov/vocabulary/iso639-1/',.)}"/>
</xsl:template>

<xsl:template match="dcite:publicationYear">
  <dcterms:created><xsl:value-of select="."/></dcterms:created>
</xsl:template>

<xsl:template match="dcite:resourceType">
  <dcterms:type rdf:resource="{concat('http://purl.org/spar/fabio/',.)}"/>
</xsl:template>

<xsl:template match="dcite:alternateIdentifiers">
  <xsl:apply-templates select="dcite:alternateIdentifier"/>
</xsl:template>

<xsl:template match="dcite:alternateIdentifier[@alternateIdentifierType='URL']">
  <xsl:comment> <xsl:value-of select="."/> </xsl:comment>
</xsl:template>

<xsl:template match="dcite:alternateIdentifier[@alternateIdentifierType='URN']">
  <dcterms:identifier><xsl:value-of select="."/></dcterms:identifier>
</xsl:template>

<xsl:template match="dcite:descriptions">
  <xsl:apply-templates select="dcite:description"/>
</xsl:template>

<xsl:template match="dcite:description">
  <dcterms:abstract>
    <xsl:copy-of select="@xml:lang"/>
    <xsl:value-of select="."/>
  </dcterms:abstract>
</xsl:template>

<xsl:template match="dcite:subjects">
  <xsl:apply-templates select="dcite:subject" />
</xsl:template>

<xsl:template match="dcite:subject">
 <dcterms:subject><skos:Concept><rdfs:label>
   <xsl:value-of select="."/>
 </rdfs:label></skos:Concept></dcterms:subject>
</xsl:template>

<xsl:template match="dcite:contributors">
  <xsl:apply-templates select="dcite:contributor"/>
</xsl:template>

<!-- GH201801 TODO -->
<xsl:template match="dcite:contributor">
  <dcterms:contributor>
    <xsl:apply-templates select="dcite:contributorName"/>
    <xsl:apply-templates select="@dcite:contributorType"/>
  </dcterms:contributor>
</xsl:template>

<xsl:template match="dcite:contributor/dcite:contributorName">
</xsl:template>

<xsl:template match="dcite:contributor/@dcite:contributorType">
</xsl:template>

<xsl:template match="dcite:dates">
  <xsl:apply-templates select="dcite:date"/>
</xsl:template>

<xsl:template match="dcite:date[@dateType='Issued']">
  <dcterms:issued><xsl:value-of select="."/></dcterms:issued>
</xsl:template>

<xsl:template match="dcite:date[@dateType='Accepted']">
  <dcterms:dateAccepted><xsl:value-of select="."/></dcterms:dateAccepted>
</xsl:template>

<xsl:template match="dcite:date[@dateType='Updated']">
  <dcterms:modified><xsl:value-of select="."/></dcterms:modified>
</xsl:template>

<xsl:template match="dcite:identifier[@identifierType='DOI']">
  <dcterms:identifier><xsl:value-of select="concat('https://doi.org/',.)"/></dcterms:identifier>
</xsl:template>

<xsl:template match="dcite:relatedIdentifiers">
  <xsl:apply-templates select="dcite:relatedIdentifier"/>
</xsl:template>

<xsl:template match="dcite:relatedIdentifier[@relationType='HasPart'][@relatedIdentifierType='URL'][@resourceTypeGeneral]">
  <xsl:variable name="pos"><xsl:value-of select="position()"/></xsl:variable>
  <dcterms:hasPart>
    <xsl:comment><xsl:value-of select="concat(' pos ',$pos,' ')"/></xsl:comment>
    <xsl:element name="{concat('dctypes:',@resourceTypeGeneral)}">
      <xsl:attribute name="rdf:about">
         <xsl:value-of select="."/></xsl:attribute>
        <dcterms:format>
          <dcterms:MediaTypeOrExtent>
            <rdfs:label><xsl:value-of 
             select="../../dcite:formats/dcite:format[position()=$pos]"/>
            </rdfs:label>
          </dcterms:MediaTypeOrExtent>
        </dcterms:format>
    </xsl:element>
  </dcterms:hasPart>
</xsl:template>

<xsl:template match="dcite:relatedIdentifier[@relationType='IsDescribedBy'][@relatedIdentifierType='URL'][@resourceTypeGeneral='Image']">
  <foaf:img><xsl:value-of select="."/></foaf:img>
</xsl:template>

<xsl:template match="dcite:relatedIdentifier[@relationType='IsPartOf'][@relatedIdentifierType='ISSN']">
  <sco:issn><xsl:value-of select="."/></sco:issn>
</xsl:template>

<!-- garbage protection -->
<xsl:template match="*|text()"/>

</xsl:stylesheet>

