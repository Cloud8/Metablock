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
     xmlns:pica="http://localhost/view/"
     version="1.0" >

<xsl:output method="xml" encoding="UTF-8" indent="yes" />

<xsl:template match="rdf:RDF">
 <add>
  <!-- <xsl:comment>Shanghai RDF Transformer (2013)</xsl:comment> -->
  <xsl:apply-templates select="fabio:*" />
 </add>
</xsl:template>

<xsl:template match="fabio:*">
 <doc>
    <field name="recordtype">opus</field>
    <field name="callnumber"><xsl:value-of select="@rdf:about"/></field>

    <!--
    <field name="building">Online</field>
    <field name="callnumber-a">
      <xsl:value-of 
           select="substring-after(substring-after(@rdf:about,'//'),'/')"/>
    </field>
    -->

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
    <xsl:apply-templates select="dct:source" />
    <xsl:apply-templates select="dct:subject" />
    <xsl:apply-templates select="dct:abstract" />
    <xsl:apply-templates select="dct:isPartOf" />
    <xsl:apply-templates select="dct:hasPart" />
    <xsl:apply-templates select="dct:relation" />
    <xsl:apply-templates select="dct:isReferencedBy" />
    <xsl:apply-templates select="foaf:img" />
    <field name="allfields">
          <xsl:apply-templates select="dct:identifier[position()=1]"/>
          <xsl:value-of select="' '"/>
          <xsl:value-of select="normalize-space(.)"/>
    </field>
    <xsl:apply-templates select="." mode="type"/>

    <xsl:apply-templates select="dct:modified" />
    <!--
    <xsl:apply-templates select="dct:type" />
    <xsl:apply-templates select="dct:format" />
    -->
 </doc>
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

<!-- TITLE : only one, no multilanguage support -->
<xsl:template match="dct:title">
 <xsl:choose>
   <xsl:when test="@xml:lang='en' and count(following-sibling::dct:title[@xml:lang='en'])>0">
   </xsl:when>
   <xsl:when test="@xml:lang='en'">
        <xsl:call-template name="title" />
   </xsl:when>
   <xsl:when test="count(../dct:title[@xml:lang='en'])=0 and count(preceding-sibling::dct:title)=0">
      <xsl:call-template name="title" />
   </xsl:when>
   <xsl:otherwise>
     <field name="title_alt"><xsl:value-of select="." /></field>
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
     <xsl:choose>
     <xsl:when test="foaf:Person/foaf:familyName">
       <field name="author">
          <xsl:value-of select="concat(foaf:Person/foaf:familyName,', ',foaf:Person/foaf:givenName)"/>
       </field>
     </xsl:when>
     <xsl:when test="foaf:Person/foaf:name">
       <field name="author">
          <xsl:value-of select="foaf:Person/foaf:name"/>
       </field>
     </xsl:when>
     <xsl:when test="normalize-space(.)!=''">
       <field name="author">
          <xsl:value-of select="normalize-space(.)"/>
       </field>
     </xsl:when>
     <xsl:otherwise></xsl:otherwise>
     </xsl:choose>
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
  <xsl:apply-templates select="aiiso:Institute" />
</xsl:template>

<xsl:template match="aiiso:Faculty|aiiso:Center">
  <field name="institution"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="aiiso:Division">
  <field name="institution"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="aiiso:Institute">
  <field name="building"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<!-- ISBN -->
<xsl:template match="dct:source">
 <xsl:if test="normalize-space(.)!=''">
  <field name="isbn"><xsl:value-of select="."/></field>
 </xsl:if>
</xsl:template>
<xsl:template match="fabio:hasISBN">
  <field name="isbn"><xsl:value-of select="."/></field>
</xsl:template>


<!-- TYPE -->
<xsl:template match="fabio:*" mode="type">
 <xsl:choose>
  <!--
  <xsl:when test="name(.)!='fabio:Book' and dct:publisher/aiiso:Division">
     <field name="format">Digitalisat</field>
  </xsl:when>
  -->
  <xsl:when test="contains(@rdf:about,'/eb/') and dct:publisher/aiiso:Division">
     <field name="format">Digitalisat</field>
  </xsl:when>
 </xsl:choose>

 <xsl:choose>
  <xsl:when test="name(.)='fabio:DoctoralThesis'">
     <field name="format"><xsl:value-of select="'Dissertation'"/></field>
  </xsl:when>
  <xsl:when test="name(.)='fabio:Book'">
     <field name="format"><xsl:value-of select="'Book'"/></field>
  </xsl:when>
  <xsl:when test="name(.)='fabio:BookChapter'">
     <field name="format"><xsl:value-of select="'Book'"/></field>
  </xsl:when>
  <xsl:when test="name(.)='fabio:Catalog'">
     <field name="format"><xsl:value-of select="'Journal'"/></field>
  </xsl:when>
  <xsl:when test="name(.)='fabio:Periodical'">
     <field name="format"><xsl:value-of select="'Journal'"/></field>
  </xsl:when>
  <xsl:when test="name(.)='fabio:PeriodicalIssue'">
     <field name="format"><xsl:value-of select="'Journal'"/></field>
  </xsl:when>
  <xsl:when test="name(.)='fabio:PeriodicalItem'">
     <field name="format"><xsl:value-of select="'Journal'"/></field>
  </xsl:when>
  <xsl:when test="name(.)='fabio:Journal'">
     <field name="format"><xsl:value-of select="'Journal'"/></field>
  </xsl:when>
  <xsl:when test="name(.)='fabio:JournalIssue'">
     <field name="format">Journal</field>
  </xsl:when>
  <xsl:when test="name(.)='fabio:JournalArticle'">
     <field name="format"><xsl:value-of select="'Journal Articles'"/></field>
  </xsl:when>
  <xsl:when test="name(.)='fabio:Article'">
     <field name="format"><xsl:value-of select="'Journal Articles'"/></field>
  </xsl:when>
  <xsl:when test="name(.)='fabio:Image'">
     <field name="format"><xsl:value-of select="'Image'"/></field>
  </xsl:when>
  <xsl:when test="name(.)='fabio:MusicalComposition'">
     <field name="format"><xsl:value-of select="'Musical Score'"/></field>
  </xsl:when>
  <xsl:otherwise>
     <field name="format"><xsl:value-of select="'Work'"/></field>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<!-- Unused. May be later. -->
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
        <field name="format"><xsl:value-of select="."/></field>
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
  <field name="mod_date">
  <xsl:value-of select="concat(.,'T00:00:01Z')" /></field>
</xsl:template>

<xsl:template match="dct:issued[position()=1]">
  <field name="publishDate"><xsl:value-of select="." /></field>
  <field name="publishDateSort"><xsl:value-of select="." /></field>
  <field name="era"><xsl:value-of select="substring(.,0,5)" /></field>
  <field name="era_facet"><xsl:value-of select="substring(.,0,5)" /></field>
</xsl:template>

<!-- COVER -->
<xsl:template match="foaf:img[position()=1]">
  <field name="thumbnail"><xsl:value-of select="." /></field>
</xsl:template>

<!-- TOPIC : TODO: multiple languages -->
<xsl:template match="dct:subject">
  <xsl:choose>
    <xsl:when test="count(skos:Concept)>0">
      <xsl:apply-templates select="skos:Concept"/>
    </xsl:when>
    <xsl:when test="contains(.,',')">
      <field name="topic"><xsl:value-of select="." /></field>
    </xsl:when>
    <xsl:otherwise>
      <field name="topic"><xsl:value-of select="." /></field>
      <field name="topic_facet"><xsl:value-of select="." /></field>
      <field name="topic_browse"><xsl:value-of select="." /></field>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- SWD TOPICS as blank nodes, DDC Topic qualified -->
<xsl:template match="skos:Concept">
  <xsl:for-each select="skos:prefLabel[@xml:lang='de']">
    <field name="topic" boost="44"><xsl:value-of select="."/></field>
    <field name="topic_facet"><xsl:value-of select="." /></field>
    <field name="topic_browse"><xsl:value-of select="." /></field>
  </xsl:for-each>
</xsl:template>

<!--
    <field name="genre"><xsl:value-of select="."/></field>
    <field name="genre_facet"><xsl:value-of select="."/></field>
-->

<!-- DESCRIPTION -->
<xsl:template match="dct:abstract[@xml:lang='en']">
  <field name="description">
     <xsl:value-of select="normalize-space(substring(.,0,2999))"/>
  </field>
</xsl:template>

<!-- contents may be multivalued -->
<xsl:template match="dct:abstract[not(@xml:lang) or @xml:lang!='en']">
  <xsl:choose>
  <xsl:when test="count(../dct:abstract[@xml:lang='en'])>0">
  <field name="contents">
     <xsl:value-of select="normalize-space(substring(.,0,2999))"/>
  </field>
  </xsl:when>
  <xsl:when test="count(preceding-sibling::dct:abstract)>0">
  <field name="contents">
     <xsl:value-of select="normalize-space(substring(.,0,2999))"/>
  </field>
  </xsl:when>
  <xsl:otherwise>
  <field name="description">
     <xsl:value-of select="normalize-space(substring(.,0,2999))"/>
  </field>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="dct:isReferencedBy">
  <field name="url"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dct:relation">
  <xsl:choose>
   <xsl:when test="../dct:publisher/aiiso:Division and name(..)='fabio:PeriodicalItem'">
     <field name="url"><xsl:value-of select="concat(../@rdf:about,'/view.html')" />
     </field>
   </xsl:when>
   <xsl:when test="../dct:publisher/aiiso:Division and substring(., string-length(.) - 7)='/All.pdf'">
     <field name="url"> <!-- start dfg viewer -->
      <xsl:value-of select="concat(../@rdf:about,'/view.html')"/>
     </field>
   </xsl:when>
   <xsl:otherwise>
     <field name="url"><xsl:value-of select="." /></field>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Hierarchies TOP -->
<xsl:template match="dct:hasPart[position()=1]">
 <xsl:choose>
  <xsl:when test="name(..)='fabio:Periodical'"> 
   <xsl:apply-templates select=".." mode="hierarchy_top" />
  </xsl:when>
  <xsl:when test="name(..)='fabio:Catalog'"> 
   <xsl:apply-templates select=".." mode="hierarchy_top" />
  </xsl:when>
  <xsl:when test="name(..)='fabio:Book'"> 
   <xsl:apply-templates select=".." mode="hierarchy_top" />
  </xsl:when>
  <xsl:when test="name(..)='fabio:Journal'"> 
   <xsl:apply-templates select=".." mode="hierarchy_top" />
  </xsl:when>
  <xsl:otherwise></xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="dct:hasPart[position()>1]">
</xsl:template>

<!-- Hierarchies SUB -->
<xsl:template match="dct:isPartOf[position()=1]">
  <xsl:apply-templates select="fabio:*" mode="hierarchy" />
  <xsl:if test="rdf:Description/dct:identifier">
  <xsl:apply-templates select="rdf:Description" mode="hierarchy" />
  </xsl:if>
</xsl:template>

<xsl:template match="dct:isPartOf[position()>1]">
</xsl:template>

<!-- from hasPart -->
<xsl:template match="fabio:Periodical|fabio:Journal|fabio:Book|fabio:Catalog" 
              mode="hierarchy_top">
  <field name="format"><xsl:value-of select="'Volume Holdings'"/></field>
  <field name="hierarchytype"></field>
  <field name="hierarchy_top_id">
    <xsl:apply-templates select="dct:identifier[position()=1]"/>
  </field>
  <field name="hierarchy_top_title">
     <xsl:value-of select="dct:title" />
  </field>
  <field name="is_hierarchy_id">
    <xsl:apply-templates select="dct:identifier[position()=1]"/>
  </field>
  <field name="is_hierarchy_title">
     <xsl:value-of select="dct:title" />
  </field>
  <!--
  <field name="container_title"><xsl:value-of select="dct:title"/></field>
  <field name="container_start_page">
     <xsl:value-of select="@rdf:about" />
  </field>
  <field name="collection"><xsl:value-of select="dct:title"/></field>
  -->
</xsl:template>

<!-- from inside isPartOf : This is an Issue or Item -->
<xsl:template match="fabio:*|rdf:Description" mode="hierarchy">
  <field name="hierarchytype"></field>
   <xsl:choose>
    <xsl:when test="name(/rdf:RDF/*)='fabio:PeriodicalItem'">
     <field name="hierarchy_top_id">
       <xsl:value-of 
        select="substring(dct:identifier,0,string-length(dct:identifier)-5)"/>
     </field>
    </xsl:when>
    <xsl:when test="name(.)='fabio:JournalItem'">
     <field name="hierarchy_top_id">
       <xsl:value-of 
        select="substring(dct:identifier,0,string-length(dct:identifier)-5)"/>
     </field>
    </xsl:when>
    <xsl:otherwise>
     <field name="hierarchy_top_id">
        <xsl:apply-templates select="dct:identifier[position()=1]"/>
     </field>
    </xsl:otherwise>
   </xsl:choose>
  <field name="hierarchy_top_title">
     <xsl:value-of select="dct:title" />
  </field>
  <xsl:if test="../../dct:hasVersion">
  <field name="hierarchy_sequence">
     <xsl:value-of select="../../dct:hasVersion[position()=1]" />
  </field>
  </xsl:if>

  <field name="hierarchy_browse">
     <xsl:value-of select="concat(dct:title,'{{{_ID_}}}',substring(dct:identifier,0,string-length(dct:identifier)))" />
  </field>

  <field name="hierarchy_parent_id">
    <xsl:apply-templates select="dct:identifier[position()=1]"/>
  </field>
  <field name="hierarchy_parent_title">
     <xsl:value-of select="dct:title"/>
  </field>
  <field name="is_hierarchy_id">
     <xsl:apply-templates select="../../dct:identifier[position()=1]"/>
  </field>
  <field name="is_hierarchy_title">
     <xsl:value-of select="../../dct:title"/>
  </field>
  <!--
  <field name="container_title"><xsl:value-of select="dct:title"/></field>
  <field name="container_start_page">
     <xsl:value-of select="@rdf:about"/>
  </field>
  -->
  <!--
  <xsl:if test="../../dct:hasVersion">
  <field name="container_volume">
     <xsl:value-of select="../../dct:hasVersion[position()=1]"/>
  </field>
  <field name="container_issue">
     <xsl:value-of select="../../dct:hasVersion[position()=1]"/>
  </field>
  </xsl:if>
  -->
  <!--
  <field name="container_reference">
     <xsl:value-of select="@rdf:about"/>
  </field>
  -->
  <!--
  <field name="collection"><xsl:value-of select="dct:title"/></field>
  -->
</xsl:template>

<!-- suppress emptyness -->
<xsl:template match="text()"/>

</xsl:stylesheet>
