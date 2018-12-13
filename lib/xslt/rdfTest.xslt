<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
    xmlns:skos="http://www.w3.org/2004/02/skos/core#"
    xmlns:fabio="http://purl.org/spar/fabio/"
    xmlns:foaf="http://xmlns.com/foaf/0.1/"
    xmlns:void="http://rdfs.org/ns/void#"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    version="1.0" >

<xsl:output method="text" indent="yes"/>

<xsl:template match="/">
  <xsl:apply-templates select="rdf:RDF"/>
</xsl:template>

<xsl:template match="rdf:RDF">
 <xsl:choose>
  <xsl:when test="count(fabio:*)!=1">
   <!-- <xsl:value-of select="'fail'"/> -->
  </xsl:when>
  <xsl:otherwise>
   <xsl:apply-templates select="fabio:*"/>
   <xsl:apply-templates select="dcterms:BibliographicResource"/>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="fabio:*|dcterms:BibliographicResource">
 <xsl:variable name="lang" select="substring-after(dcterms:language/@rdf:resource,'iso639-1/')"/>
 <xsl:choose>
  <xsl:when test="count(dcterms:identifier[starts-with(text(),'urn:')])!=1">
      <xsl:value-of select="'fail: identifier '"/>
  </xsl:when>
  <xsl:when test="count(dcterms:language)!=1">
      <xsl:value-of select="'fail: language '"/>
  </xsl:when>
  <xsl:when test="count(dcterms:issued)!=1">
      <xsl:value-of select="'fail: issued '"/>
  </xsl:when>
  <xsl:when test="count(dcterms:modified)>1">
      <xsl:value-of select="'fail: modified '"/>
  </xsl:when>
  <xsl:when test="count(dcterms:created)!=1">
      <xsl:value-of select="'fail: created '"/>
  </xsl:when>
  <xsl:when test="count(dcterms:title[not(@xml:lang)])!=1">
      <xsl:value-of select="concat('fail: title #0 ',$lang)"/>
  </xsl:when>
  <!--
  <xsl:when test="count(dcterms:title[@xml:lang])=0">
     <xsl:value-of select="concat('fail: title #0 ',$lang)"/>
  </xsl:when>
  -->
  <!-- second title not in a different language
  <xsl:when test="count(dcterms:title[@xml:lang])=1">
   <xsl:if test="count(dcterms:title[@xml:lang=$lang])=1">
     <xsl:value-of select="concat('fail: title #1 ',$lang)"/>
   </xsl:if>
  </xsl:when>
  -->
  <xsl:when test="count(dcterms:title[@xml:lang])=2 and count(dcterms:title[@xml:lang=$lang])=0">
      <xsl:value-of select="concat('fail: title #2 ',$lang)"/>
  </xsl:when>
  <xsl:when test="count(dcterms:title[@xml:lang])&gt;2">
      <xsl:value-of select="'fail: title #3 '"/>
  </xsl:when>
  <xsl:when 
       test="count(dcterms:abstract[@xml:lang!=$lang])&gt;1 and $lang='bi'">
  </xsl:when>
  <xsl:when 
       test="count(dcterms:abstract[@xml:lang!=$lang])&gt;1 and $lang='zu'">
  </xsl:when>
  <xsl:when 
       test="count(dcterms:abstract[@xml:lang!=$lang])&gt;1 and $lang='fr'">
  </xsl:when>
  <xsl:when test="count(dcterms:abstract[@xml:lang!=$lang])&gt;1">
      <xsl:value-of select="concat('fail: abstract 2 ',dcterms:abstract/@xml:lang)"/>
  </xsl:when>
  <xsl:when test="count(dcterms:creator)!=1 and not(starts-with(local-name(.),'Journal')) and count(dcterms:contributor)=0">
      <xsl:value-of select="'fail: creator '"/>
  </xsl:when>
  <xsl:when test="count(dcterms:contributor)&gt;1">
      <!-- <xsl:value-of select="'fail: contributor '"/> -->
  </xsl:when>
  <!--if no abstract in documents language, then only one abstract except bi-->
  <xsl:when test="count(dcterms:abstract[@xml:lang=$lang])!=1 and count(dcterms:abstract[@xml:lang])&gt;1 and $lang!='bi'">
      <xsl:value-of select="concat('fail: abstract ',$lang)"/>
  </xsl:when>
  <xsl:when test="count(dcterms:publisher/foaf:Organization)!=1 and not(local-name(.)='JournalArticle')">
      <xsl:value-of select="concat('fail: publisher ',count(dcterms:publisher/foaf:Organization))"/>
  </xsl:when>
  <xsl:when test="count(dcterms:subject)=0 and local-name(.)!='Journal'">
      <xsl:value-of select="'fail: dcterms:subject '"/>
  </xsl:when>
  <xsl:when test="count(dcterms:type)!=1">
      <xsl:value-of select="'fail: dcterms:type '"/>
  </xsl:when>
  <xsl:when test="count(dcterms:hasPart)=0 and local-name(.)!='JournalIssue'">
      <xsl:value-of select="'fail: dcterms:hasPart '"/>
  </xsl:when>
  <xsl:when test="count(dcterms:license)!=1">
      <xsl:if test="count(dcterms:isPartOf/*/dcterms:license)!=1">
      <xsl:value-of select="concat('fail: license ',count(dcterms:license))"/>
      </xsl:if>
  </xsl:when>
  <xsl:when test="count(dcterms:format)&gt;0 and dcterms:format!='PDF'">
      <xsl:value-of select="'fail: format '"/>
  </xsl:when>
  <xsl:otherwise>
      <!--<xsl:value-of select="'success'"/>-->
<!--
      <xsl:value-of select="dcterms:identifier"/><xsl:text>
</xsl:text>
-->
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<!--
<xsl:template match="dcterms:modified">
  <dcterms:modified rdf:datatype="{@rdf:datatype}">2015-05-35</dcterms:modified>
</xsl:template>
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
-->

<xsl:template match="@*|node()" priority="-1">
</xsl:template>

</xsl:stylesheet>
