<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:fabio="http://purl.org/spar/fabio/"

     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:bibo="http://purl.org/ontology/bibo/"
     xmlns:oai="http://www.openarchives.org/OAI/2.0/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:xMetaDiss="http://www.d-nb.de/standards/xmetadissplus/"
     xmlns:cc="http://www.d-nb.de/standards/cc/"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dcmitype="http://purl.org/dc/dcmitype/"
     xmlns:pc="http://www.d-nb.de/standards/pc/"
     xmlns:urn="http://www.d-nb.de/standards/urn/"
     xmlns:hdl="http://www.d-nb.de/standards/hdl/"
     xmlns:doi="http://www.d-nb.de/standards/doi/"
     xmlns:thesis="http://www.ndltd.org/standards/metadata/etdms/1.0/"
     xmlns:ddb="http://www.d-nb.de/standards/ddb/"
     xmlns:dini="http://www.d-nb.de/standards/xmetadissplus/type/"
     xmlns:gnd="http://d-nb.info/gnd/"
     xmlns:dcq="http://purl.org/dc/qualifier/1.0/"
     version="1.0" >

<xsl:output method="xml" encoding="UTF-8" 
     indent="yes" omit-xml-declaration="no"/>

<xsl:template match="rdf:RDF">
  <!-- rdf:Description fabio:DoctoralThesis -->
  <xsl:apply-templates select="fabio:*|rdf:Description" />
</xsl:template>

<xsl:template match="rdf:Description|fabio:*">
 <add>
  <xsl:comment> RDF Transformer UB Marburg 2013 </xsl:comment>
   <doc>
      <!-- RECORDTYPE -->
      <field name="recordtype">opus</field>

      <!-- IDENTIFIER -->
      <field name="id">
        <xsl:value-of select="dc:identifier" />
      </field>

      <!-- TITLE -->
      <xsl:apply-templates select="dc:title" />

      <!-- AUTHOR -->
      <xsl:apply-templates select="dc:creator" />
      <xsl:apply-templates select="dc:contributor" />

      <!-- PUBLISHER -->
      <xsl:apply-templates select="dc:publisher" />

      <!-- DATE -->
      <xsl:apply-templates select="dcterms:dateAccepted" />
      <xsl:apply-templates select="dcterms:issued" />

      <!-- URL -->
      <field name="url">
        <xsl:value-of select="@rdf:about" />
      </field>
      <field name="thumbnail">
        <xsl:value-of select="@rdf:about" />/cover.png</field>
      <!--
      <xsl:apply-templates select="ddb:identifier[@ddb:type='URL']" />
      <xsl:apply-templates select="ddb:transfer[@ddb:type='dcterms:URI']" />
      -->

      <!-- LANGUAGE -->
      <xsl:apply-templates select="dc:language" />

      <!-- FORMAT -->
      <!-- <xsl:apply-templates select="dc:type" /> -->
      <xsl:apply-templates select="dini:PublType" />

      <!-- ISBN -->
      <xsl:apply-templates select="dc:source" />

      <!-- TOPIC -->
      <xsl:apply-templates select="dc:subject" />
      <xsl:apply-templates select="gnd:preferredNameForTheSubjectHeading" />

      <!-- DESCRIPTION -->
      <xsl:apply-templates select="dcterms:abstract" />

      <!-- CONTAINER -->
      <xsl:apply-templates select="dcterms:isPartOf" />
      <xsl:apply-templates select="dcterms:hasPart" />

      <!-- ALLFIELDS -->
      <!-- <xsl:apply-templates select="*" mode="all" />-->
      <field name="fullrecord">
        <xsl:value-of select="." />
      </field>
  </doc>
 </add>
</xsl:template>

<!-- TITLE @lang : no multilanguage support for now -->
<xsl:template match="dc:title[position()=1]">
  <field name="title"><xsl:value-of select="." /></field>
  <field name="title_short"><xsl:value-of select="." />
  </field>
  <field name="title_full"><xsl:value-of select="." /></field>
  <field name="title_sort"><xsl:value-of select="." /></field>
</xsl:template>

<!-- TITLE @lang : no multilanguage support for now -->
<xsl:template match="dc:title[position()=2]">
   <field name="title_fullStr"><xsl:value-of select="." /></field>
</xsl:template>

<!-- AUTHOR -->
<xsl:template match="dc:creator">
   <field name="author">
     <xsl:if test="normalize-space(.)=''">Unbekannt</xsl:if>
     <xsl:value-of select="foaf:Person/foaf:name"/>
     <!--
     <xsl:value-of select="*/pc:foreName" />
     <xsl:if test="*/pc:surName">
       <xsl:text> </xsl:text>
       <xsl:value-of select="*/pc:surName" />
     </xsl:if>
     <xsl:value-of select="normalize-space(*/pc:personEnteredUnderGivenName)"/>
     -->
   </field>
</xsl:template>

<!-- CONTRIBUTOR -->
<xsl:template match="dc:contributor">
  <field name="author_additional">
   <xsl:value-of select="." />
  </field>
</xsl:template>

<!-- PUBLISHER -->
<xsl:template match="dc:publisher">
  <xsl:apply-templates select="*" />
</xsl:template>

<xsl:template match="dc:publisher/foaf:Organization">
  <field name="publisher">
    <xsl:value-of select="foaf:name"/>
  </field>
</xsl:template>

<xsl:template match="dc:publisher/foaf:Group">
  <field name="institution">
    <xsl:value-of select="foaf:name"/>
  </field>
</xsl:template>

<!-- ISBN -->
<xsl:template match="dc:source[@xsi:type='ddb:ISBN']">
 <xsl:if test="normalize-space(.)!=''">
  <field name="isbn"><xsl:value-of select="."/></field>
 </xsl:if>
</xsl:template>

<!-- TYPE ?? FORMAT ?? : publication type mapping -->
<xsl:template match="dini:PublType">
  <xsl:choose>
  <xsl:when test=".='doctoralThesis'">
     <field name="format">Dissertation</field>
  </xsl:when>
  <xsl:when test=".='Periodical'">
     <field name="format">Journal</field>
  </xsl:when>
  <xsl:when test=".='book'">
     <field name="format">eBook</field>
  </xsl:when>
  <!-- series like Professionalisierung und Diagnosekompetenz etc. -->
  <xsl:when test=".='bookPart'">
     <field name="format">Article</field>
  </xsl:when>
  <xsl:when test=".='article'">
     <field name="format">Article</field>
  </xsl:when>
  <!-- 2012: we only have one, so no extra -->
  <xsl:when test=".='workingPaper'">
     <field name="format">Article</field>
  </xsl:when>
  <xsl:when test=".='contributionToPeriodical'">
     <field name="format">Article</field>
  </xsl:when>
  <!-- 2012: we only have one -->
  <xsl:when test=".='masterThesis'">
     <field name="format">Article</field>
  </xsl:when>
  <xsl:otherwise>
     <field name="format"><xsl:value-of select="." /></field>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- LANGUAGE -->
<xsl:template match="dc:language">
  <field name="language">
   <xsl:choose>
     <xsl:when test=".='de'">German</xsl:when>
     <xsl:when test=".='en'">English</xsl:when>
     <xsl:when test=".='ger'">German</xsl:when>
     <xsl:when test=".='eng'">English</xsl:when>
     <xsl:when test=".='fre'">French</xsl:when>
     <xsl:when test=".='mul'">Multiple</xsl:when>
     <xsl:otherwise>Other</xsl:otherwise> -->
     </xsl:choose>
  </field>
</xsl:template>

<!-- DATE -->
<xsl:template match="dcterms:dateAccepted">
</xsl:template>

<xsl:template match="dcterms:issued">
  <field name="publishDate"><xsl:value-of select="." /></field>
  <field name="publishDateSort"><xsl:value-of select="." /></field>
  <field name="era"><xsl:value-of select="substring(.,0,5)" /></field>
  <field name="era_facet"><xsl:value-of select="substring(.,0,5)" /></field>
</xsl:template>

<!-- URL COVER -->
<xsl:template match="ddb:identifier[@ddb:type='URL']">
  <field name="url"><xsl:value-of select="." /></field>
  <!-- we have a generated cover image for every url we publish -->
  <field name="thumbnail"><xsl:value-of select="." />/cover.png</field>
</xsl:template>

<!-- URL DOCUMENT -->
<xsl:template match="ddb:transfer[@ddb:type='dcterms:URI']">
  <!-- <xsl:comment>
       <xsl:value-of select="substring(., (string-length(.) - 3))" />
       </xsl:comment> -->
  <xsl:if test="substring(., (string-length(.) - 3)) = '.pdf'">
  <field name="url"><xsl:value-of select="." /></field>
  </xsl:if>
</xsl:template>

<!-- TOPIC -->
<xsl:template match="dc:subject">
  <xsl:choose>
    <xsl:when test=".//dcq:SubjectScheme">
        <field name="topic" boost="44">
           <xsl:value-of select=".//rdf:Value" /></field>
    </xsl:when>
    <!-- links do not work 
    <xsl:when test="contains(.,'http://')">
      <field name="topic"><a href="{.}"><xsl:value-of select="." /></a></field>
    </xsl:when>
    -->
    <xsl:otherwise>
    <field name="topic"><xsl:value-of select="." /></field>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- SWD TOPICS -->
<xsl:template match="gnd:preferredNameForTheSubjectHeading">
  <field name="topic" boost="44"><xsl:value-of select="." /></field>
  <field name="topic_facet"><xsl:value-of select="." /></field>
  <field name="topic_browse"><xsl:value-of select="." /></field>
</xsl:template>

<!-- TOPIC -->
<xsl:template match="dc:subject[@xsi:type='xMetaDiss:noScheme']">
  <xsl:if test="normalize-space(.)!=''">
  <field name="topic"><xsl:value-of select="." /></field>
  </xsl:if>
</xsl:template>

<!-- DDC -->
<xsl:template match="dc:subject[@xsi:type='xMetaDiss:DDC-SG']">
  <xsl:if test="normalize-space(.)!=''">
  <field name="dewey-hundreds"><xsl:value-of select="." /></field>
  </xsl:if>
</xsl:template>

<!-- DDC TOPIC -->
<xsl:template match="dc:subject[@xsi:type='dcterms:DDC']">
  <xsl:if test="normalize-space(.)!=''">
  <field name="topic" boost="44"><xsl:value-of select="." /></field>
  <field name="topic_facet"><xsl:value-of select="." /></field>
  <field name="topic_browse"><xsl:value-of select="." /></field>
  </xsl:if>
</xsl:template>

<!-- DESCRIPTION -->
<xsl:template match="dcterms:abstract">
  <!--
  <xsl:if test="@xml:lang=../dc:language">
  -->
  <xsl:if test="position()=1">
     <field name="description"><xsl:value-of select="." /></field>
  </xsl:if>
  <field name="contents"><xsl:value-of select="." /></field>
</xsl:template>

<!-- CONTAINER -->
<xsl:template match="dcterms:isPartOf">
  <!-- blank causes VuFind to default to the driver 
       specified in the [Hierarchy] section of config.ini -->
  <field name="hierarchytype"></field>

  <field name="hierarchy_top_id">
     <xsl:value-of select="rdf:Description/dc:identifier" />
  </field>
  <field name="hierarchy_top_title">
     <xsl:value-of select="rdf:Description/dc:title" />
  </field>
  <field name="hierarchy_parent_id">
     <xsl:value-of select="rdf:Description/dc:identifier" />
  </field>
  <field name="hierarchy_parent_title">
     <xsl:value-of select="rdf:Description/dc:title" />
  </field>

  <field name="container_title">
     <xsl:value-of select="rdf:Description/dc:title" />
  </field>
</xsl:template>

<!-- this record has a part : is_hierarchy_id can only be poulated once -->
<xsl:template match="dcterms:hasPart[position()=1]">
  <field name="is_hierarchy_id">
     <xsl:value-of select="../dc:identifier" />
  </field>
  <field name="is_hierarchy_title">
     <xsl:value-of select="../dc:title" />
  </field>
  <!-- GH2013-01 : suppress container for now
     <field name="container_volume"><xsl:value-of select="." /></field>
  -->
</xsl:template>

<!-- suppress al other positions -->
<xsl:template match="dcterms:hasPart">
</xsl:template>

<!--
<xsl:template match="@*|text()">
  <xsl:copy>
      <xsl:apply-templates select="@*|text()"/>
  </xsl:copy>
</xsl:template>
-->

</xsl:stylesheet>
