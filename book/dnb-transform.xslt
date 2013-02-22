<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:gnd="http://d-nb.info/standards/elementset/gnd#"
     xmlns:bibo="http://purl.org/ontology/bibo/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:dnb="http://d-nb.info/gnd/"
     xmlns:ifla="http://iflastandards.info/ns/isbd/elements/"
     version="1.0" >

<xsl:output method="xml" encoding="UTF-8" 
     indent="yes" omit-xml-declaration="no"/>

<xsl:template match="rdf:RDF">
  <xsl:apply-templates select="bibo:periodical" />
  <xsl:apply-templates select="bibo:series" />
  <xsl:apply-templates select="bibo:document" />
  <xsl:apply-templates select="bibo:collection" />
  <xsl:apply-templates select="bibo:issue" />
  <xsl:apply-templates select="bibo:article" />
</xsl:template>

<xsl:template match="bibo:*">
 <add>
  <xsl:comment> RDF Transformer UB Marburg 2013 </xsl:comment>
   <doc>
      <!-- RECORDTYPE -->
      <field name="recordtype">rdf</field>

      <!-- IDENTIFIER -->
      <xsl:apply-templates select="dcterms:identifier" />

      <!-- TITLE -->
      <xsl:apply-templates select="dcterms:title" />

      <!-- AUTHOR -->
      <xsl:apply-templates select="dcterms:creator" />

      <!-- PUBLISHER -->
      <xsl:apply-templates select="dc:publisher" />

      <!-- DATE -->
      <xsl:apply-templates select="ifla:P1018" />

      <!-- URL -->
      <field name="url"><xsl:value-of select="@rdf:about" /></field>

      <!-- LANGUAGE -->
      <xsl:apply-templates select="dcterms:language" />

      <!-- FORMAT -->
      <xsl:apply-templates select="dcterms:format" />

      <!-- ISBN -->
      <xsl:apply-templates select="bibo:isbn13" />
      <xsl:apply-templates select="bibo:isbn10" />

      <!-- TOPIC -->
      <xsl:apply-templates select="dcterms:subject" />

      <!-- EXTEND -->
      <xsl:apply-templates select="dcterms:extend"/>

      <!-- DESCRIPTION -->
      <xsl:apply-templates select="dcterms:abstract" />

      <!-- CONTAINER -->
      <xsl:apply-templates select="dcterms:isPartOf" />
      <xsl:apply-templates select="dcterms:hasPart" />

      <!-- ALLFIELDS -->
      <!--
      <field name="fullrecord"><xsl:value-of select="." /></field>
      -->
  </doc>
 </add>
</xsl:template>

<!-- may be later : authority data -->
<xsl:template match="gnd:NEVER-MATCH-DifferentiatedPerson">
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

<!-- Hope this fits into an url -->
<xsl:template match="dcterms:identifier[position()=1]">
  <field name="id"><xsl:value-of select="." /></field>
</xsl:template>

<!-- TODO: <dcterms:identifier>(OColc)180723415</dcterms:identifier> -->
<xsl:template match="dcterms:identifier">
</xsl:template>

<!-- TITLE : single valued -->
<xsl:template match="dcterms:title[position()=1]"">
  <field name="title"><xsl:value-of select="." /></field>
  <field name="title_short"><xsl:value-of select="substring(.,0,128)" /></field>
  <field name="title_full"><xsl:value-of select="." /></field>
  <field name="title_sort"><xsl:value-of select="." /></field>
</xsl:template>

<!-- TODO : other title fields should go somewhere -->
<xsl:template match="dcterms:title">
</xsl:template>

<!-- AUTHOR -->
<xsl:template match="dcterms:creator">
  <field name="author">
    <xsl:choose>
     <xsl:when test="gnd:DifferentiatedPerson">
      <xsl:value-of 
           select="gnd:DifferentiatedPerson/gnd:preferredNameForThePerson" />
     </xsl:when>
     <xsl:otherwise>
      <xsl:value-of select="@rdf:about" />
     </xsl:otherwise>
    </xsl:choose>
  </field>
</xsl:template>

<!-- PUBLISHER -->
<xsl:template match="dc:publisher">
  <field name="publisher"><xsl:value-of select="."/></field>
</xsl:template>

<!-- DATE -->
<xsl:template match="ifla:P1018">
  <field name="publishDate"><xsl:value-of select="." /></field>
  <field name="era"><xsl:value-of select="." /></field>
  <field name="era_facet"><xsl:value-of select="." /></field>
</xsl:template>

<!-- FORMAT -->
<xsl:template match="dcterms:format">
  <field name="format"><xsl:value-of select="."/></field>
</xsl:template>

<!-- LANGUAGE -->
<xsl:template match="dcterms:language">
  <!-- looks like http://id.loc.gov/vocabulary/iso639-2/ger -->
  <xsl:param name="lang" select="substring(@rdf:resource,39)" />
  <field name="language">
   <xsl:choose>
     <xsl:when test="$lang='ger'">German</xsl:when>
     <xsl:when test="$lang='eng'">English</xsl:when>
     <xsl:when test="$lang='fre'">French</xsl:when>
     <xsl:otherwise><xsl:value-of select="$lang"/></xsl:otherwise> -->
   </xsl:choose>
  </field>
</xsl:template>

<!-- ISBN -->
<xsl:template match="bibo:isbn13|bibo:isbn10">
  <field name="isbn"><xsl:value-of select="."/></field>
</xsl:template>

<!-- TOPIC linked to DDC or the like -->
<xsl:template match="dcterms:subject[@rdf:resource]">
  <field name="topic_facet"><xsl:value-of select="@rdf:resource"/></field>
</xsl:template>

<!-- TOPIC <dcterms:subject>(DDC)338.768176109431550904</dcterms:subject> -->
<xsl:template match="dcterms:subject">
  <field name="topic"><xsl:value-of select="."/></field>
</xsl:template>

<!-- EXTEND : suppressed, is something like '217 S.' -->
<xsl:template match="dcterms:extend">
  <!-- <field name="format"><xsl:value-of select="."/></field> -->
</xsl:template>

<!-- ABSTRACT -->

<!-- identity template : suppress garbage-->
<xsl:template match="@*|node()">
  <!--
    <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  -->
</xsl:template>

</xsl:stylesheet>
