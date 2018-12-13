<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:frbr="http://purl.org/vocab/frbr/core#"
     xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
     xmlns:ore="http://www.openarchives.org/ore/terms/"
     xmlns:swrc="http://swrc.ontoware.org/ontology#"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     version="1.0" >

<xsl:output method="xml" encoding="UTF-8" indent="yes" />

<xsl:template match="rdf:RDF">
 <add>
   <xsl:comment>Seaview RDF Transformer (2015)</xsl:comment>
   <xsl:apply-templates select="foaf:*" />
 </add>
</xsl:template>

<xsl:template match="foaf:Document">
 <doc>
    <field name="recordtype">opus</field>
    <field name="id"><xsl:call-template name="substring-after-last">
        <xsl:with-param name="string" select="@rdf:about" />
        <xsl:with-param name="delimiter" select="'/'" />
        </xsl:call-template>
    </field>
    <xsl:apply-templates select="dct:*"/>
    <xsl:apply-templates select="dc:*"/>
    <xsl:apply-templates select="foaf:*"/>
 </doc>
</xsl:template>

<!-- TITLE -->
<xsl:template match="dc:title[@xml:lang]">
  <field name="title_alt"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template name="title" match="dc:title[not(@xml:lang)][1]">
  <field name="title"><xsl:value-of select="." /></field>
  <field name="title_short"><xsl:value-of select="." /></field>
  <field name="title_full"><xsl:value-of select="." /></field>
  <field name="title_sort"><xsl:value-of select="." /></field>
</xsl:template>

<!-- AUTHOR -->
<xsl:template match="dc:creator">
  <xsl:apply-templates select="foaf:person"/>
</xsl:template>

<xsl:template match="dc:creator/foaf:person">
  <xsl:apply-templates select="foaf:name"/>
</xsl:template>

<xsl:template match="dc:creator[1]/foaf:person/foaf:name[1]">
  <field name="author"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dc:creator[position()>1]/foaf:person/foaf:name[1]">
  <field name="author2"><xsl:value-of select="."/></field>
</xsl:template>

<!-- CONTRIBUTOR -->
<xsl:template match="dct:contributor">
    <xsl:apply-templates select="foaf:Person"/>
    <xsl:apply-templates select="foaf:Organization"/>
</xsl:template>

<xsl:template match="dct:contributor/foaf:Person">
    <xsl:choose>
      <xsl:when test="foaf:role='editor'">
        <field name="author2">
            <xsl:value-of select="concat(foaf:name,' (Hrsg)')"/>
        </field>
      </xsl:when>
      <xsl:when test="foaf:role='advisor'">
        <field name="author_additional">
            <!-- <xsl:value-of select="concat(foaf:name,' (Gutachter)')"/> -->
            <xsl:value-of select="foaf:name"/>
        </field>
      </xsl:when>
      <xsl:when test="foaf:role='translator'">
        <field name="author2">
            <xsl:value-of select="concat(foaf:name,' [Übers.]')"/>
        </field>
      </xsl:when>
      <xsl:otherwise>
        <field name="author2">
            <xsl:value-of select="foaf:name"/>
        </field>
      </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template match="dct:contributor/foaf:Organization">
    <xsl:apply-templates select="foaf:name"/>
</xsl:template>

<xsl:template match="dct:contributor/foaf:Person/foaf:name">
   <field name="author2"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dct:contributor/foaf:Organization/foaf:name">
   <field name="author2"><xsl:value-of select="."/></field>
</xsl:template>

<!-- ISSN -->
<xsl:template match="fabio:hasISSN">
   <field name="issn"><xsl:value-of select="." /></field>
   <field name="oai_set_str_mv">
       <xsl:value-of select="concat('issn:',.)"/></field>
</xsl:template>

<!-- PUBLISHER -->
<xsl:template match="dc:publisher">
  <field name="publisher"><xsl:value-of select="."/></field>
</xsl:template>

<!-- FORMAT -->
<xsl:template match="dc:type">
  <field name="format"><xsl:value-of select="."/></field>
</xsl:template>

<!-- LANGUAGE -->
<xsl:template match="dc:language">
 <field name="language">
  <xsl:choose>
   <xsl:when test=".='de'">German</xsl:when>
   <xsl:when test=".='en'">English</xsl:when>
   <xsl:when test=".='fr'">French</xsl:when>
   <xsl:when test=".='lt'">Latin</xsl:when>
   <xsl:when test=".='es'">Spanish</xsl:when>
   <xsl:when test=".='it'">Italian</xsl:when>
   <xsl:when test=".='it'">Italian</xsl:when>
   <xsl:when test=".='na'">Papua</xsl:when>
   <xsl:when test=".='java'">Java</xsl:when>
   <xsl:when test=".='php'">PHP</xsl:when>
   <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
  </xsl:choose>
 </field>
</xsl:template>

<xsl:template match="dct:modified[1]">
  <field name="last_indexed">
      <xsl:value-of select="concat(.,'T23:59:59Z')" />
  </field>
</xsl:template>

<xsl:template match="dc:issued[1]">
  <field name="publishDate"><xsl:value-of select="." /></field>
  <field name="publishDateSort"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="dct:created">
  <field name="era"><xsl:value-of select="substring(.,1,4)" /></field>
  <field name="era_facet"><xsl:value-of select="substring(.,1,4)" /></field>
</xsl:template>

<xsl:template match="dct:extend">
  <field name="physical"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dc:keyword">
  <field name="topic"><xsl:value-of select="." /></field>
  <field name="topic_facet"><xsl:value-of select="." /></field>
</xsl:template>

<!-- SWD TOPICS as blank nodes, DDC Topic qualified -->
<xsl:template match="dct:subject/skos:Concept">
  <xsl:if test="substring-after(@rdf:about,'class/')!=''">
  <field name="oai_set_str_mv">
   <xsl:value-of select="concat('ddc:',substring-after(@rdf:about,'class/'))"/>
  </field>
  <!--
  <field name="dewey-sort">
      <xsl:value-of select="substring-after(@rdf:about,'class/')"/>
  </field>
  -->
  <field name="dewey-raw">
      <xsl:value-of select="substring-after(@rdf:about,'class/')"/>
  </field>
  </xsl:if>
  <xsl:apply-templates select="skos:prefLabel" />
</xsl:template>

<xsl:template match="skos:Concept/skos:prefLabel[@xml:lang='de']">
    <field name="topic_facet"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="skos:Concept/skos:prefLabel[@xml:lang='en']">
    <field name="genre"><xsl:value-of select="." /></field>
    <field name="genre_facet"><xsl:value-of select="." /></field>
</xsl:template>

<!-- contents can be multivalued -->
<xsl:template match="dct:abstract">
 <xsl:choose>
   <xsl:when test="@xml:lang=../dct:language">
     <field name="description"><xsl:value-of select="."/></field>
   </xsl:when>
   <xsl:when test="count(../dct:language)=0 and @xml:lang='en'">
     <field name="description"><xsl:value-of select="."/></field>
   </xsl:when>
   <xsl:when test="count(../dct:abstract)=1">
     <field name="description"><xsl:value-of select="."/></field>
   </xsl:when>
  <xsl:otherwise>
     <field name="contents"><xsl:value-of select="."/></field>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="foaf:page[@rdf:resource]">
   <field name="url"><xsl:value-of select="@rdf:resource" /></field>
</xsl:template>

<!-- RIGHTS : core extension -->
<xsl:template match="dct:rights[1]">
 <xsl:choose>
  <xsl:when test="../dct:accessRights"><!--restricted-->
   <field name="oai_set_str_mv">
       <xsl:value-of select="'restricted_access'"/></field>
  </xsl:when>
  <xsl:when test=".='restricted'"><!--Paper-->
   <field name="oai_set_str_mv"><xsl:value-of select="'restricted'"/></field>
  </xsl:when>
  <xsl:otherwise>
   <field name="oai_set_str_mv"><xsl:value-of select="'open_access'"/></field>
  </xsl:otherwise>
 </xsl:choose>
 <field name="license_str"><xsl:value-of select="@rdf:resource"/></field>
</xsl:template>

<!-- RIGHTS : core extension : IP address list -->
<xsl:template match="dct:accessRights">
   <field name="rights_str_mv"><xsl:value-of select="."/></field>
   <field name="rights_str_mv"><xsl:value-of select="'127.0.0.1'"/></field>
</xsl:template>

<!-- suppress emptyness -->
<!-- <xsl:template match="text()"/> -->

<xsl:template match="*" priority="-1"/>

<xsl:template name="substring-after-last">
    <xsl:param name="string" />
    <xsl:param name="delimiter" />
    <xsl:choose>
      <xsl:when test="contains($string, $delimiter)">
        <xsl:call-template name="substring-after-last">
          <xsl:with-param name="string"
            select="substring-after($string, $delimiter)" />
          <xsl:with-param name="delimiter" select="$delimiter" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise><xsl:value-of 
                  select="$string" /></xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>
