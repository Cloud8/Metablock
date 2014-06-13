<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:pro="http://purl.org/spar/pro/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:frbr="http://purl.org/vocab/frbr/core#"
     version="1.0" >

<xsl:output method="xml" encoding="UTF-8" indent="yes" />
<xsl:param name="collection" select="'Seaview'"/>

<xsl:template match="rdf:RDF">
 <add>
  <xsl:comment>Shanghai RDF Transformer (2014)</xsl:comment>
  <xsl:apply-templates select="fabio:*" />
  <xsl:apply-templates select="dct:BibliographicResource" />
 </add>
</xsl:template>

<!-- paper repository -->
<xsl:template match="dct:BibliographicResource[dct:identifier]">
 <doc>
    <field name="recordtype">opus</field>
    <field name="collection"><xsl:value-of select="$collection" /></field>
    <xsl:apply-templates select="dct:*"/>
    <xsl:apply-templates select="foaf:img" />
    <field name="edition"><xsl:value-of select="@rdf:about"/></field>
    <field name="allfields">
        <xsl:value-of select="dct:identifier"/>
        <xsl:value-of select="' '"/>
        <xsl:value-of select="dct:abstract"/>
    </field>
 </doc>
</xsl:template>

<!-- general repository -->
<xsl:template match="fabio:*[dct:identifier]">
 <doc>
    <field name="recordtype">opus</field>
    <xsl:apply-templates select="dct:*"/>
    <xsl:apply-templates select="foaf:img" />
    <xsl:apply-templates select="pro:editor" />
    <field name="allfields">
        <xsl:value-of select="dct:identifier"/>
        <xsl:value-of select="' '"/>
        <xsl:value-of select="normalize-space(.)"/>
    </field>
    <xsl:apply-templates select="." mode="type"/>
    <xsl:apply-templates select="." mode="about"/>
 </doc>
</xsl:template>

<xsl:template match="dct:identifier[position()=1]">
  <field name="id">
   <xsl:call-template name="identity">
    <xsl:with-param name="id" select="."/>
   </xsl:call-template>
  </field>
  <field name="callnumber"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template name="identity">
  <xsl:param name="id"/>
  <xsl:choose>
   <xsl:when test="starts-with($id,'urn:nbn')">
        <xsl:value-of select="substring($id,0,string-length($id))" />
   </xsl:when>
   <xsl:otherwise><xsl:value-of select="$id" /></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- TITLE : no multilanguage support for now -->
<xsl:template match="dct:title[@xml:lang!=../dct:language]">
  <field name="title_alt"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="dct:title[@xml:lang=../dct:language]">
  <field name="title_alt"><xsl:value-of select="." /></field>
  <xsl:if test="count(../dct:title[not(@xml:lang)])=0">
    <xsl:call-template name="title"/>
  </xsl:if>
</xsl:template>

<xsl:template name="title" match="dct:title[not(@xml:lang)][1]">
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
       <!-- set gnd ?
       <xsl:if test="starts-with(foaf:Person/@rdf:about,'http://d-nb.info/gnd')">
         <field name="author_gnd_txt">
           <xsl:value-of select="foaf:Person/@rdf:about"/>
        </field>
       </xsl:if>
       -->
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
     <xsl:when test="@rdf:resource">
       <field name="author">
          <xsl:variable name="res" select="@rdf:resource" />
          <xsl:value-of select="normalize-space(/rdf:RDF/foaf:Person[@rdf:about=$res])"/>
       </field>
     </xsl:when>
     <xsl:otherwise></xsl:otherwise>
     </xsl:choose>
</xsl:template>

<xsl:template match="pro:editor">
 <xsl:choose>
  <xsl:when test="foaf:Person/foaf:name">
   <field name="author2">
      <xsl:value-of select="foaf:Person/foaf:name"/>
   </field>
   <field name="author2-role">
      <xsl:value-of select="'Herausgeber'"/>
   </field>
  </xsl:when>
  <xsl:when test="@rdf:resource">
   <field name="author2">
     <xsl:variable name="res" select="@rdf:resource" />
     <xsl:value-of select="normalize-space(/rdf:RDF/foaf:Person[@rdf:about=$res])"/>
    <xsl:value-of select="' (Herausgeber)'"/>
   </field>
  </xsl:when>
 </xsl:choose>
</xsl:template>

<xsl:template match="dct:creator[position()!=1]">
 <xsl:choose>
  <xsl:when test="foaf:Person/foaf:familyName">
      <field name="author_additional">
         <xsl:value-of select="foaf:Person/foaf:name"/>
      </field>
  </xsl:when>
  <xsl:when test="normalize-space(.)!=''">
      <field name="author_additional">
          <xsl:value-of select="normalize-space(.)"/>
      </field>
  </xsl:when>
  <xsl:otherwise></xsl:otherwise>
 </xsl:choose>
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
 <xsl:choose>
  <xsl:when test="@rdf:resource">
      <xsl:variable name="res" select="@rdf:resource" />
      <xsl:apply-templates select="/rdf:RDF/aiiso:*[@rdf:about=$res]"/>
  </xsl:when>
  <xsl:otherwise>
   <xsl:apply-templates select="aiiso:Faculty" />
   <xsl:apply-templates select="aiiso:Center" />
   <xsl:apply-templates select="aiiso:Division" />
   <xsl:apply-templates select="aiiso:Institute" />
  </xsl:otherwise>
 </xsl:choose>
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

<!-- PROVENANCE -->
<xsl:template match="dct:provenance">
  <field name="collection"><xsl:value-of select="."/></field>
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
<!-- Used by Paper Repository -->
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
     <xsl:when test=".='java'">Java</xsl:when>
     <xsl:when test=".='php'">PHP</xsl:when>
     <xsl:otherwise>Other</xsl:otherwise>
   </xsl:choose>
  </field>
</xsl:template>

<xsl:template match="dct:dateAccepted">
  <field name="first_indexed">
  <xsl:value-of select="concat(.,'T00:00:01Z')" /></field>
</xsl:template>

<xsl:template match="dct:modified">
  <field name="last_indexed">
  <xsl:value-of select="concat(.,'T00:00:01Z')" /></field>
</xsl:template>

<xsl:template match="dct:issued[position()=1]">
  <field name="publishDate"><xsl:value-of select="." /></field>
  <field name="publishDateSort"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="dct:BibliographicResource" mode="type">
</xsl:template>

<xsl:template match="fabio:Book" mode="type">
    <field name="format"><xsl:value-of select="'Book'"/></field>
    <xsl:choose> 
     <xsl:when test="contains(@rdf:about,'/es/')">
       <field name="collection">Monographie</field>
     </xsl:when>
    </xsl:choose>
</xsl:template>

<xsl:template match="fabio:BookChapter" mode="type">
    <field name="format"><xsl:value-of select="'Book Chapter'"/></field>
</xsl:template>

<xsl:template match="fabio:Catalog" mode="type">
    <field name="format"><xsl:value-of select="'Catalog'"/></field>
</xsl:template>

<xsl:template match="fabio:CollectedWorks" mode="type">
    <field name="format"><xsl:value-of select="'Collected Works'"/></field>
</xsl:template>

<xsl:template match="fabio:Database" mode="type">
    <field name="format"><xsl:value-of select="'Database'"/></field>
</xsl:template>

<xsl:template match="fabio:DoctoralThesis" mode="type">
    <field name="format"><xsl:value-of select="'Dissertation'"/></field>
    <field name="collection"><xsl:value-of select="'Monographie'"/></field>
</xsl:template>

<xsl:template match="fabio:Journal" mode="type">
    <field name="format"><xsl:value-of select="'Journal'"/></field>
</xsl:template>

<xsl:template match="fabio:JournalIssue" mode="type">
    <field name="format"><xsl:value-of select="'Issue'"/></field>
</xsl:template>

<xsl:template match="fabio:JournalArticle" mode="type">
    <field name="format"><xsl:value-of select="'Journal Articles'"/></field>
</xsl:template>

<xsl:template match="fabio:Periodical" mode="type">
    <field name="format"><xsl:value-of select="'Volume Holdings'"/></field>
</xsl:template>

<xsl:template match="fabio:PeriodicalIssue" mode="type">
    <field name="format"><xsl:value-of select="'Volume'"/></field>
    <xsl:choose> 
     <xsl:when test="contains(@rdf:about,'/es/')">
       <field name="collection">Monographie</field>
     </xsl:when>
    </xsl:choose>
</xsl:template>

<xsl:template match="fabio:PeriodicalItem" mode="type">
    <field name="format"><xsl:value-of select="'Issue'"/></field>
</xsl:template>

<xsl:template match="fabio:Article" mode="type">
    <field name="format"><xsl:value-of select="'Article'"/></field>
</xsl:template>

<xsl:template match="fabio:Image" mode="type">
    <field name="format"><xsl:value-of select="'Image'"/></field>
</xsl:template>

<xsl:template match="fabio:MusicalComposition" mode="type">
    <field name="format"><xsl:value-of select="'Musical Score'"/></field>
</xsl:template>

<xsl:template match="fabio:*" mode="type">
    <field name="format"><xsl:value-of select="'Work'"/></field>
</xsl:template>

<xsl:template match="fabio:JournalArticle" mode="about">
    <xsl:choose>
    <xsl:when test="starts-with(dct:relation, 'http://meta-journal.net')">
    <field name="edition">
       <xsl:value-of select="concat(substring-before(dct:relation,'File'),
                                    substring-after(dct:relation,'File'))"/>
    </field>
    </xsl:when>
    <xsl:otherwise>
    <field name="edition"><xsl:value-of select="@rdf:about"/></field>
    </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template match="fabio:*" mode="about">
    <field name="edition"><xsl:value-of select="@rdf:about"/></field>
    <xsl:choose>
     <xsl:when test="contains(@rdf:about,'/eb/') and dct:publisher/aiiso:Division">
     <field name="collection">Digitalisat</field>
     </xsl:when>
    </xsl:choose>
</xsl:template>

<!-- Used by Paper Repository -->
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
     <xsl:when test=".='java'">Java</xsl:when>
     <xsl:when test=".='php'">PHP</xsl:when>
     <xsl:otherwise>Other</xsl:otherwise>
   </xsl:choose>
  </field>
</xsl:template>

<xsl:template match="dct:dateAccepted">
  <field name="first_indexed">
  <xsl:value-of select="concat(.,'T00:00:01Z')" /></field>
</xsl:template>

<xsl:template match="dct:modified">
  <field name="last_indexed">
  <xsl:value-of select="concat(.,'T00:00:01Z')" /></field>
</xsl:template>

<xsl:template match="dct:issued[position()=1]">
  <field name="publishDate"><xsl:value-of select="." /></field>
  <field name="publishDateSort"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="dct:date">
  <field name="era"><xsl:value-of select="substring(.,1,4)" /></field>
  <field name="era_facet"><xsl:value-of select="substring(.,1,4)" /></field>
</xsl:template>

<xsl:template match="foaf:img[position()=1]">
 <xsl:choose>
  <xsl:when test="starts-with(.,'/')">
    <field name="thumbnail">
        <xsl:value-of select="concat('/view/page/',.)" />
    </field>
  </xsl:when>
  <xsl:otherwise>
    <field name="thumbnail"><xsl:value-of select="." /></field>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="dct:extend">
  <field name="physical"><xsl:value-of select="."/></field>
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
    <xsl:when test="@rdf:resource">
      <xsl:variable name="res" select="@rdf:resource" />
      <xsl:apply-templates select="/rdf:RDF/skos:Concept[@rdf:about=$res]"/>
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

<!--
<xsl:template match="dct:abstract[@xml:lang=../dct:language]">
</xsl:template>
-->

<!-- used for annotations e.g. Behring -->
<xsl:template match="dct:isReferencedBy">
  <field name="url"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dct:relation">
  <xsl:choose>
   <xsl:when test="../dct:publisher/aiiso:Division and name(..)='fabio:PeriodicalItem'">
     <field name="url">
         <xsl:value-of select="concat(../@rdf:about,'/view.html')" />
     </field>
   </xsl:when>
   <xsl:when test="../dct:publisher/aiiso:Division and substring(., string-length(.) - 7)='/All.pdf'">
     <field name="url">
         <xsl:value-of select="concat(../@rdf:about,'/view.html')"/>
     </field>
   </xsl:when>
   <xsl:when test="starts-with(.,'/')">
     <field name="url"><xsl:value-of select="concat('/view/page/',.)"/></field>
   </xsl:when>
   <xsl:when test="starts-with(.,'http://archiv.ub.uni-marburg.de/')">
     <field name="url"><xsl:value-of select="substring(.,32)" /></field>
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
  <field name="hierarchytype"></field>
  <field name="hierarchy_top_id">
    <xsl:call-template name="identity">
      <xsl:with-param name="id" select="dct:identifier"/>
    </xsl:call-template>
  </field>
  <field name="hierarchy_top_title">
     <xsl:value-of select="dct:title" />
  </field>
  <!-- if a record IS a hierarchy as opposed to something that belongs 
       within a hierarchy -->
  <field name="is_hierarchy_id">
    <xsl:call-template name="identity">
      <xsl:with-param name="id" select="dct:identifier"/>
    </xsl:call-template>
  </field>
  <field name="is_hierarchy_title">
     <xsl:value-of select="dct:title" />
  </field>
  <field name="container_title"><xsl:value-of select="dct:title"/></field>
  <field name="container_start_page">
     <xsl:value-of select="@rdf:about" />
  </field>
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
    <xsl:when test="name(.)='fabio:JournalIssue'">
     <field name="hierarchy_top_id">
       <xsl:value-of 
        select="substring(dct:identifier,0,string-length(dct:identifier)-5)"/>
     </field>
    </xsl:when>
    <xsl:otherwise>
     <field name="hierarchy_top_id">
        <!-- <xsl:value-of select="dct:identifier[position()=1]"/> -->
        <xsl:call-template name="identity">
          <xsl:with-param name="id" select="dct:identifier"/>
        </xsl:call-template>
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
    <xsl:call-template name="identity">
        <xsl:with-param name="id" select="dct:identifier"/>
    </xsl:call-template>
  </field>
  <field name="hierarchy_parent_title">
     <xsl:value-of select="dct:title"/>
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
</xsl:template>

<!-- paper repository -->
<xsl:template match="dct:references[@rdf:resource]">
  <xsl:variable name="res" select="@rdf:resource" />
  <xsl:value-of select="normalize-space(/rdf:RDF/foaf:Person[@rdf:about=$res])"/>
 <field name="ref_str_mv">
   <xsl:value-of select="normalize-space(/rdf:RDF/dct:BibliographicResource[@rdf:about=$res]/dct:bibliographicCitation)"/>
   <xsl:value-of select="concat('(',@rdf:resource,')')"/>
 </field>
</xsl:template>

<xsl:template match="dct:references[not(@rdf:resource)]">
 <field name="ref_str_mv">
   <xsl:value-of select="dct:BibliographicResource/dct:bibliographicCitation"/>
 </field>
 <!--
 <field name="contents">
   <xsl:value-of select="dct:BibliographicResource/dct:bibliographicCitation"/>
 </field>
 -->
</xsl:template>

<!-- bad verb -->
<xsl:template match="dct:fulltext">
  <field name="fulltext"><xsl:value-of select="normalize-space(.)"/></field>
</xsl:template>

<!-- suppress emptyness -->
<xsl:template match="text()"/>

</xsl:stylesheet>
