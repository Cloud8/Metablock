<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:bibo="http://purl.org/ontology/bibo/"

     xmlns:dcmitype="http://purl.org/dc/dcmitype/"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:xMetaDiss="http://www.d-nb.de/standards/xmetadissplus/"
     xmlns:urn="http://www.d-nb.de/standards/urn/"
     xmlns:ddb="http://www.d-nb.de/standards/ddb/"
     xmlns:gnd="http://d-nb.info/gnd/"
     xmlns:dcq="http://purl.org/dc/qualifier/1.0/"
     xmlns:shg="http://localhost/view/"
     version="1.0" >

<xsl:output method="xml" encoding="UTF-8" indent="yes" />

<!-- TODO: transform a nature article -->

<xsl:template match="rdf:RDF">
  <xsl:apply-templates select="bibo:Article" />
</xsl:template>

<xsl:template match="bibo:*">
 <add>
  <xsl:comment>The Shanghai RDF Transformer (2013)</xsl:comment>
   <doc>
      <field name="recordtype">rdf</field>
      <field name="about_str"><xsl:value-of select="@rdf:about" /></field>

      <field name="id">
         <xsl:apply-templates select="dct:identifier[position()=1]"/>
      </field>

      <xsl:apply-templates select="dct:title" />
      <xsl:apply-templates select="dct:creator" />
      <xsl:apply-templates select="dct:contributor" />
      <xsl:apply-templates select="dct:publisher" />
      <xsl:apply-templates select="dct:dateAccepted" />
      <xsl:apply-templates select="dct:issued" />
      <xsl:apply-templates select="dct:language" />
      <xsl:apply-templates select="dct:type" />
      <xsl:apply-templates select="dct:format" />
      <xsl:apply-templates select="dct:source" />
      <xsl:apply-templates select="dct:subject" />
      <xsl:apply-templates select="gnd:preferredNameForTheSubjectHeading" />
      <xsl:apply-templates select="dct:abstract" />
      <xsl:apply-templates select="dct:isPartOf" />
      <xsl:apply-templates select="dct:hasPart" />
      <xsl:apply-templates select="dct:relation" />

   <!-- <xsl:apply-templates select="*" mode="all" />-->
   <!-- <field name="fullrecord"><xsl:value-of select="." /></field> -->
   <xsl:apply-templates select="shg:hasCoverImage" />

   <!-- ALLFIELDS -->
   <!-- <xsl:value-of select="normalize-space(string(dct:*))"/> -->
   <field name="allfields">
      <xsl:value-of select="normalize-space(.)"/>
   </field>
  </doc>
 </add>
</xsl:template>

<xsl:template match="dct:identifier[position()=1]">
 <xsl:choose>
  <xsl:when test="starts-with(.,'urn:nbn')">
        <xsl:value-of select="substring(.,0,string-length(.))" />
  </xsl:when>
  <xsl:otherwise>
        <xsl:value-of select="." />
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<!-- TITLE : we can only have one, and no multilanguage support for now -->
<xsl:template match="dct:title">
  <xsl:choose>
   <xsl:when test="@xml:lang=../dct:language">
    <xsl:call-template name="title" />
   </xsl:when>
   <xsl:when test="position()=1 and not(@xml:lang)">
    <xsl:call-template name="title" />
   </xsl:when>
   <xsl:otherwise>
     <!-- <xsl:call-template name="title" /> -->
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="title">
  <field name="title"><xsl:value-of select="." /></field>
  <field name="title_short"><xsl:value-of select="." /></field>
  <field name="title_full"><xsl:value-of select="." /></field>
  <field name="title_sort"><xsl:value-of select="." /></field>
  <field name="title_fullStr"><xsl:value-of select="." /></field>
</xsl:template>

<!-- AUTHOR -->
<xsl:template match="dct:creator[position()=1]">
   <field name="author">
     <xsl:choose>
     <xsl:when test="foaf:Person/foaf:name">
       <xsl:value-of select="foaf:Person/foaf:name"/>
     </xsl:when>
     <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
     </xsl:choose>
   </field>
</xsl:template>

<xsl:template match="dct:creator[position()!=1]">
 <xsl:if test="normalize-space(.)!=''">
  <field name="author_additional">
   <xsl:value-of select="foaf:Person/foaf:name"/>
  </field>
 </xsl:if>
</xsl:template>

<!-- CONTRIBUTOR -->
<xsl:template match="dct:contributor">
 <xsl:choose>
  <xsl:when test="normalize-space(../dct:creator)=''">
   <field name="author"><xsl:value-of select="." /></field>
  </xsl:when>
  <xsl:when test="normalize-space(.)!=''">
   <field name="author_additional"><xsl:value-of select="." /></field>
  </xsl:when>
 </xsl:choose>
</xsl:template>

<!-- PUBLISHER -->
<xsl:template match="dct:publisher">
  <xsl:apply-templates select="aiiso:Faculty" />
  <xsl:apply-templates select="aiiso:Center" />
  <xsl:apply-templates select="aiiso:Division" />
</xsl:template>

<xsl:template match="aiiso:Faculty|aiiso:Center">
  <field name="institution"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="aiiso:Division">
  <field name="institution"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<!-- ISBN -->
<xsl:template match="dct:source[@xsi:type='ddb:ISBN']">
 <xsl:if test="normalize-space(.)!=''">
  <field name="isbn"><xsl:value-of select="."/></field>
 </xsl:if>
</xsl:template>

<!-- TYPE -->
<xsl:template match="dct:type">
 <xsl:choose>
  <xsl:when test=".='DoctoralThesis'">
     <field name="format">Dissertation</field>
  </xsl:when>
  <xsl:when test="../dct:publisher/aiiso:Division">
     <field name="format">Digitalisat</field>
  </xsl:when>
  <xsl:when test=".='Book'">
     <field name="format">eBook</field>
  </xsl:when>
  <xsl:when test=".='PeriodicalIssue'">
     <field name="format">Periodical</field>
  </xsl:when>
  <xsl:when test=".='JournalArticle'">
     <field name="format">Article</field>
  </xsl:when>
  <xsl:otherwise>
     <!-- <field name="format"><xsl:value-of select="." /></field> -->
     <field name="format">Work</field>
  </xsl:otherwise>
 </xsl:choose>

 <xsl:choose>
  <xsl:when test=".='DoctoralThesis'">
     <field name="genre">Dissertation</field>
     <field name="genre_facet">Monographie</field>
  </xsl:when>
  <xsl:when test=".='Book'">
     <field name="genre">eBook</field>
     <field name="genre_facet">eBook</field>
  </xsl:when>
  <xsl:when test=".='Image'">
     <field name="genre">Image</field>
     <field name="genre_facet">Image</field>
  </xsl:when>
  <xsl:when test=".='Periodical'">
     <field name="genre">Periodical</field>
     <field name="genre_facet">Periodical</field>
  </xsl:when>
  <xsl:when test=".='PeriodicalIssue'">
     <field name="genre">Periodical</field>
     <field name="genre_facet">Periodical</field>
  </xsl:when>
  <xsl:when test=".='Article'">
     <field name="genre">Article</field>
     <field name="genre_facet">Article</field>
  </xsl:when>
  <xsl:when test=".='workingPaper'">
     <field name="genre">Work</field>
     <field name="genre_facet">Work</field>
  </xsl:when>
  <xsl:when test=".='contributionToPeriodical'">
     <field name="genre">Periodical</field>
     <field name="genre_facet">Periodical</field>
  </xsl:when>
  <!-- we only have one -->
  <xsl:when test=".='MastersThesis' or .='Thesis.Bachelor'">
     <field name="genre">Work</field>
     <field name="genre_facet">Work</field>
  </xsl:when>
  <xsl:when test=".='JournalArticle'">
     <field name="genre">Article</field>
     <field name="genre_facet">Article</field>
  </xsl:when>
  <xsl:when test=".='BibliographicResource'">
     <field name="genre">Work</field>
     <field name="genre_facet">Work</field>
  </xsl:when>
  <xsl:otherwise>
     <field name="genre">Work</field>
     <field name="genre_facet">Work</field>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="dct:format">
 <xsl:choose>
  <xsl:when test=".='Sound'">
        <field name="format">Noten</field>
    </xsl:when>
    <xsl:when test=".='StillImage'">
        <field name="format">Bild</field>
    </xsl:when>
    <xsl:when test=".='Text (EPUB)'">
        <field name="format">Text (EPUB)</field>
    </xsl:when>
   <xsl:otherwise>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<!-- LANGUAGE -->
<xsl:template match="dct:language">
  <field name="language">
   <xsl:choose>
     <xsl:when test=".='de'">German</xsl:when>
     <xsl:when test=".='en'">English</xsl:when>
     <xsl:when test=".='fr'">French</xsl:when>
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

<!-- COVER -->
<xsl:template match="shg:hasCoverImage">
  <field name="thumbnail"><xsl:value-of select="." /></field>
</xsl:template>

<!-- TOPIC -->
<xsl:template match="dct:subject">
  <xsl:choose>
    <xsl:when test="rdf:Description">
      <xsl:for-each select="rdf:Description/rdf:value[@xml:lang='de']">
        <field name="topic" boost="44">
           <xsl:value-of select="." /></field>
      </xsl:for-each>
    </xsl:when>
    <xsl:otherwise>
    <field name="topic"><xsl:value-of select="." /></field>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- SWD TOPICS -->
<xsl:template match="gnd:preferredNameForTheSubjectHeading">
  <field name="topic" boost="49"><xsl:value-of select="." /></field>
  <field name="topic_facet"><xsl:value-of select="." /></field>
  <field name="topic_browse"><xsl:value-of select="." /></field>
</xsl:template>

<!-- TOPIC -->
<xsl:template match="dct:subject[@xsi:type='xMetaDiss:noScheme']">
  <xsl:if test="normalize-space(.)!=''">
  <field name="topic"><xsl:value-of select="." /></field>
  </xsl:if>
</xsl:template>

<!-- DESCRIPTION -->
<xsl:template match="dct:abstract">
  <!-- <xsl:if test="@xml:lang=../dct:language"> -->
  <xsl:if test="position()=1 and normalize-space(.)!=''">
     <field name="description">
        <xsl:value-of select="substring(.,0,899)" />
     </field>
  </xsl:if>
  <xsl:if test="position()>1 and normalize-space(.)!=''">
     <field name="contents">
       <xsl:value-of select="substring(.,0,899)" />
     </field>
  </xsl:if>
</xsl:template>

<!-- CONTAINER : Collection identifier = "Top" -->
<xsl:template match="dct:isPartOf">
  <!-- blank causes VuFind to default to the driver 
       specified in the [Hierarchy] section of config.ini -->
  <xsl:choose>
   <xsl:when test="dcmitype:Collection">
     <xsl:apply-templates select="dcmitype:Collection" mode="hierarchy" />
   </xsl:when>
   <xsl:when test="fabio:Journal">
     <xsl:apply-templates select="fabio:Journal" mode="hierarchy" />
   </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template match="fabio:Journal" mode="hierarchy" >
  <field name="container_title"><xsl:value-of select="dct:title"/></field>
  <field name="format"><xsl:value-of select="'Journal'"/></field>
</xsl:template>

<xsl:template match="dcmitype:Collection" mode="hierarchy" >
  <field name="hierarchytype"></field>
  <field name="hierarchy_top_id">
    <xsl:apply-templates select="dct:identifier[position()=1]"/>
  </field>
  <field name="hierarchy_top_title">
     <xsl:value-of select="dct:title" />
  </field>
  <field name="hierarchy_parent_id">
    <xsl:apply-templates select="dct:identifier[position()=1]"/>
  </field>
  <field name="hierarchy_parent_title">
     <xsl:value-of select="dct:title" />
  </field>
  <field name="container_title"><xsl:value-of select="dct:title" /></field>
  <field name="format">Journal</field>
  <field name="container_start_page">
     <xsl:value-of select="@rdf:about" />
  </field>
</xsl:template>

<!-- this record has a part : is_hierarchy_id can only be populated once -->
<!--
<xsl:template match="dct:hasPart[position()=1]">
  <field name="hierarchy_sequence"><xsl:value-of select="."/></field>
</xsl:template>
-->
 
<xsl:template match="dct:relation">
  <xsl:choose>
   <xsl:when test="../dct:publisher/aiiso:Division">
     <field name="url"><xsl:value-of select="../@rdf:about" /></field>
   </xsl:when>
   <xsl:otherwise>
    <field name="url"><xsl:value-of select="." /></field>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- suppress other positions -->
<xsl:template match="dct:hasPart">
</xsl:template>

<!-- suppress emptyness -->
<xsl:template match="text()"/>

</xsl:stylesheet>
