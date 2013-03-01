<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:dcmitype="http://purl.org/dc/dcmitype/"
     xmlns:dcq="http://purl.org/dc/qualifier/1.0/"

     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:bibo="http://purl.org/ontology/bibo/"
     xmlns:oai="http://www.openarchives.org/OAI/2.0/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:xMetaDiss="http://www.d-nb.de/standards/xmetadissplus/"
     xmlns:cc="http://www.d-nb.de/standards/cc/"
     xmlns:pc="http://www.d-nb.de/standards/pc/"
     xmlns:urn="http://www.d-nb.de/standards/urn/"
     xmlns:hdl="http://www.d-nb.de/standards/hdl/"
     xmlns:doi="http://www.d-nb.de/standards/doi/"
     xmlns:thesis="http://www.ndltd.org/standards/metadata/etdms/1.0/"
     xmlns:ddb="http://www.d-nb.de/standards/ddb/"
     xmlns:dini="http://www.d-nb.de/standards/xmetadissplus/type/"
     xmlns:gnd="http://d-nb.info/gnd/"
     version="1.0" >

<xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>

<xsl:template match="rdf:RDF">
  <xsl:apply-templates select="fabio:*" />
  <xsl:apply-templates select="dcmitype:Image" />
  <xsl:apply-templates select="dcmitype:Collection" />
</xsl:template>

<xsl:template match="dcmitype:Collection">
 <add>
  <doc>
    <field name="recordtype">opus</field>
    <field name="id"><xsl:value-of select="dc:identifier" /></field>
    <field name="url"><xsl:value-of select="@rdf:about" /></field>
    <field name="title"><xsl:value-of select="dc:title[@xml:lang='de']"/></field>
    <xsl:if test="normalize-space(dc:creator//foaf:name)!=''">
    <field name="author"><xsl:value-of select="dc:creator//foaf:name"/></field>
    </xsl:if>
    <field name="publisher"><xsl:value-of select="dc:publisher//foaf:name"/></field>
  </doc>
 </add>
</xsl:template>

<xsl:template match="fabio:*|dcmitype:Image">
 <add>
  <xsl:comment> RDF Transformer UB Marburg 2013 </xsl:comment>
   <doc>
    <!-- RECORDTYPE -->
    <field name="recordtype">opus</field>

    <!-- IDENTIFIER -->
    <field name="id"><xsl:value-of select="dc:identifier" /></field>

    <!-- TITLE -->
    <xsl:apply-templates select="dc:title" />

    <!-- AUTHOR -->
    <xsl:apply-templates select="dc:creator" />
    <xsl:apply-templates select="dc:contributor" />

    <!-- PUBLISHER -->
    <xsl:apply-templates select="dc:publisher" />

    <!-- DATE -->
    <xsl:apply-templates select="dct:dateAccepted" />
    <xsl:apply-templates select="dct:issued" />

    <!-- URL -->
    <field name="url"><xsl:value-of select="@rdf:about" /></field>
    <field name="thumbnail">
    <xsl:value-of select="@rdf:about" />/cover.png</field>

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
    <xsl:apply-templates select="dct:abstract" />

    <!-- CONTAINER -->
    <xsl:apply-templates select="dct:isPartOf" />
    <xsl:apply-templates select="dct:hasPart" />

    <!-- ALLFIELDS -->
    <!-- <xsl:apply-templates select="*" mode="all" />-->
    <field name="fullrecord"><xsl:value-of select="." /></field>
  </doc>
 </add>
</xsl:template>

<!-- TITLE @lang : no multilanguage support for now : position()=1 -->
<xsl:template match="dc:title[@xml:lang=../dc:language]">
  <field name="title"><xsl:value-of select="." /></field>
  <field name="title_short"><xsl:value-of select="." /></field>
  <field name="title_full"><xsl:value-of select="." /></field>
  <field name="title_sort"><xsl:value-of select="." /></field>
  <field name="title_fullStr"><xsl:value-of select="." /></field>
  <field name="allfields"><xsl:value-of select="." /></field>
  <field name="allfields_unstemmed"><xsl:value-of select="." /></field>
</xsl:template>

<!-- TITLE @lang : no multilanguage support for now -->
<xsl:template match="dc:title">
  <field name="allfields"><xsl:value-of select="." /></field>
</xsl:template>

<!-- AUTHOR -->
<xsl:template match="dc:creator[position()=1]">
   <field name="author">
     <xsl:if test="normalize-space(.)=''">Unbekannt</xsl:if>
     <xsl:value-of select="foaf:Person/foaf:name"/>
   </field>
</xsl:template>

<xsl:template match="dc:creator[position()!=1]">
 <xsl:if test="normalize-space(.)!=''">
  <field name="author_additional">
   <xsl:value-of select="foaf:Person/foaf:name"/>
  </field>
 </xsl:if>
</xsl:template>

<!-- CONTRIBUTOR -->
<xsl:template match="dc:contributor">
 <xsl:if test="normalize-space(.)!=''">
  <field name="author_additional"><xsl:value-of select="." /></field>
 </xsl:if>
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
  <!-- series -->
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
<xsl:template match="dct:dateAccepted">
</xsl:template>

<!-- DATE -->
<xsl:template match="dct:modified">
</xsl:template>

<xsl:template match="dct:issued">
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
<xsl:template match="ddb:transfer[@ddb:type='dct:URI']">
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
<xsl:template match="dc:subject[@xsi:type='dct:DDC']">
  <xsl:if test="normalize-space(.)!=''">
  <field name="topic" boost="44"><xsl:value-of select="." /></field>
  <field name="topic_facet"><xsl:value-of select="." /></field>
  <field name="topic_browse"><xsl:value-of select="." /></field>
  </xsl:if>
</xsl:template>

<!-- DESCRIPTION -->
<xsl:template match="dct:abstract">
  <xsl:if test="position()=1">
     <field name="description"><xsl:value-of select="." /></field>
  </xsl:if>
  <field name="contents"><xsl:value-of select="." /></field>
</xsl:template>

<!-- CONTAINER : Collection identifier = "Top" -->
<xsl:template match="dct:isPartOf">
  <!-- blank causes VuFind to default to the driver 
       specified in the [Hierarchy] section of config.ini -->
  <field name="hierarchytype"></field>

  <field name="hierarchy_top_id">
     <xsl:value-of select="dcmitype:Collection/dc:identifier" />
  </field>
  <field name="hierarchy_top_title">
     <xsl:value-of select="dcmitype:Collection/dc:title" />
  </field>
  <field name="hierarchy_parent_id">
     <xsl:value-of select="dcmitype:Collection/dc:identifier" />
  </field>
  <field name="hierarchy_parent_title">
     <xsl:value-of select="dcmitype:Collection/dc:title" />
  </field>

  <field name="container_title">
     <xsl:value-of select="dcmitype:Collection/dc:title" />
  </field>
</xsl:template>

<!-- this record has a part : is_hierarchy_id can only be populated once -->
<xsl:template match="dct:hasPart[position()=1]">
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
<xsl:template match="dct:hasPart">
</xsl:template>

<!-- suppress emptyness -->
<xsl:template match="text()"/>

</xsl:stylesheet>
