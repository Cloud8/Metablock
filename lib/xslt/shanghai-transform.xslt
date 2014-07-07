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

<!-- private repository -->
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

<!-- public repository -->
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
    <field name="edition"><xsl:value-of select="@rdf:about"/></field>
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

<!-- TITLE -->
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
       <!-- gnd ?
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

<!-- PUBLISHER -->
<xsl:template match="dct:publisher">
   <xsl:apply-templates select="@rdf:resource" />
   <xsl:apply-templates select="aiiso:Faculty" />
   <xsl:apply-templates select="aiiso:Center" />
   <xsl:apply-templates select="aiiso:Division" />
   <xsl:apply-templates select="aiiso:Institute" />
   <xsl:apply-templates select="foaf:Organization" />
</xsl:template>

<xsl:template match="dct:publisher/@rdf:resource">
  <xsl:variable name="res" select="." />
  <xsl:apply-templates select="/rdf:RDF/foaf:Organization[@rdf:about=$res]"/>
</xsl:template>

<xsl:template match="dct:publisher/foaf:Organization|rdf:RDF/foaf:Organization">
  <field name="publisher"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dct:publisher/aiiso:Faculty">
  <field name="institution"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dct:publisher/aiiso:Center">
  <field name="institution"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dct:publisher/aiiso:Division">
  <xsl:choose>
  <xsl:when test="starts-with(foaf:name,'Universitätsbibliothek')">
     <field name="institution">
         <xsl:value-of select="'Universitätsbibliothek'"/>
     </field>
  </xsl:when>
  <xsl:otherwise>
  <field name="institution"><xsl:value-of select="foaf:name"/></field>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="dct:publisher/aiiso:Institute">
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

<!-- FORMAT -->
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

<xsl:template match="dct:extend">
  <field name="physical"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="foaf:img[1]">
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

<xsl:template match="dct:BibliographicResource" mode="type">
</xsl:template>

<xsl:template match="fabio:Book" mode="type">
    <field name="format"><xsl:value-of select="'Book'"/></field>
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

<!-- Drive OAI core extension -->
<xsl:template match="fabio:*[contains(@rdf:about,'/eb/')]" mode="about">
</xsl:template>

<xsl:template match="fabio:*[contains(@rdf:about,'/diss/')]" mode="about">
    <field name="collection">Monographie</field>
    <field name="oai_str_mv"><xsl:value-of select="'xMetaDissPlus'"/></field>
</xsl:template>

<xsl:template match="fabio:*[contains(@rdf:about,'/es/')]" mode="about">
    <field name="collection">Monographie</field>
    <field name="oai_str_mv"><xsl:value-of select="'xMetaDissPlus'"/></field>
</xsl:template>

<xsl:template match="fabio:*[contains(@rdf:about,'/ed/')]" mode="about">
    <field name="collection">Monographie</field>
    <field name="oai_str_mv"><xsl:value-of select="'xMetaDissPlus'"/></field>
</xsl:template>

<xsl:template match="fabio:*[contains(@rdf:about,'/ep/')]" mode="about">
    <field name="oai_str_mv"><xsl:value-of select="'xMetaDissPlus'"/></field>
</xsl:template>

<xsl:template match="fabio:Catalog" mode="about">
</xsl:template>

<xsl:template match="fabio:*" mode="about">
    <field name="collection"><xsl:value-of select="'Schrott'"/></field>
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
<xsl:template match="dct:subject/skos:Concept">
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

<!-- annotations etc. -->
<xsl:template match="dct:isReferencedBy">
  <field name="url"><xsl:value-of select="."/></field>
</xsl:template>

<!-- GH2014-07-02 mets.xml -->
<xsl:template match="dct:hasFormat">
  <xsl:choose>
   <xsl:when test="contains(substring-before(.,'.xml'),'/mets')">
     <field name="url">
      <xsl:value-of select="concat(../@rdf:about,'/view.html')" />
     </field>
    <field name="collection"><xsl:value-of select="'Digitalisat'"/></field>
    <field name="oai_str_mv"><xsl:value-of select="'Digitalisat'"/></field>
   </xsl:when>
   <xsl:otherwise>
    <field name="url"><xsl:value-of select="." /></field>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="dct:relation">
  <xsl:choose>
   <xsl:when test="contains(.,'All.pdf')"></xsl:when>
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

<xsl:template match="dct:relation/rdf:Seq">
  <xsl:choose>
  <xsl:when test="count(rdf:li/dct:FileFormat)>0">
     <field name="url">
         <xsl:value-of select="concat(../../@rdf:about,'/view.html')" />
     </field>
   </xsl:when>
   <xsl:otherwise>
       <xsl:apply-templates select="rdf:li"/>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="dct:relation/rdf:Seq/rdf:li">
    <field name="url"><xsl:value-of select="." /></field>
</xsl:template>

<!-- Hierarchies -->
<xsl:template match="fabio:Periodical/dct:hasPart[1]|fabio:Journal/dct:hasPart[1]|fabio:Book/dct:hasPart[1]|fabio:Catalog/dct:hasPart[1]"> 
  <field name="hierarchytype"></field>
  <field name="hierarchy_top_id">
    <xsl:call-template name="identity">
      <xsl:with-param name="id" select="../dct:identifier"/>
    </xsl:call-template>
  </field>
  <field name="hierarchy_top_title">
     <xsl:value-of select="../dct:title" />
  </field>
  <field name="is_hierarchy_id">
    <xsl:call-template name="identity">
      <xsl:with-param name="id" select="../dct:identifier"/>
    </xsl:call-template>
  </field>
  <field name="is_hierarchy_title">
     <xsl:value-of select="../dct:title" />
  </field>
  <field name="container_title"><xsl:value-of select="../dct:title"/></field>
  <field name="container_start_page">
     <xsl:value-of select="../@rdf:about" />
  </field>
</xsl:template>

<xsl:template match="dct:hasPart[position()>1]">
</xsl:template>

<xsl:template match="dct:isPartOf">
  <xsl:apply-templates select="*"/>
</xsl:template>

<xsl:template match="fabio:PeriodicalIssue/dct:isPartOf/fabio:Periodical|fabio:JournalIssue/dct:isPartOf/fabio:Journal|fabio:BookChapter/dct:isPartOf/fabio:Book|dct:isPartOf/rdf:Description[dct:identifier]">
  <field name="hierarchytype"></field>
  <field name="hierarchy_browse">
     <xsl:value-of select="concat(dct:title,'{{{_ID_}}}',substring(dct:identifier,0,string-length(dct:identifier)))" />
  </field>
  <field name="hierarchy_top_id">
     <xsl:call-template name="identity">
          <xsl:with-param name="id" select="dct:identifier"/>
     </xsl:call-template>
  </field>
  <field name="hierarchy_parent_id">
    <xsl:call-template name="identity">
        <xsl:with-param name="id" select="dct:identifier"/>
    </xsl:call-template>
  </field>
  <field name="hierarchy_parent_title">
     <xsl:value-of select="dct:title"/>
  </field>
</xsl:template>

<xsl:template match="fabio:PeriodicalItem/dct:isPartOf/fabio:PeriodicalIssue|fabio:JournalArticle/dct:isPartOf/fabio:JournalIssue">
  <field name="hierarchytype"></field>
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
</xsl:template>

<!--
<xsl:template match="fabio:DoctoralThesis/dct:isPartOf">
</xsl:template>
<xsl:template match="fabio:Article/dct:isPartOf">
</xsl:template>
-->

<!-- REFERENCES : core extension -->
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
</xsl:template>

<!-- bad verb -->
<xsl:template match="dct:fulltext">
  <field name="fulltext"><xsl:value-of select="normalize-space(.)"/></field>
</xsl:template>

<!-- suppress emptyness -->
<xsl:template match="text()"/>

<xsl:template match="*" priority="-1"/>

</xsl:stylesheet>
