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
     xmlns:prism="http://prismstandard.org/namespaces/basic/2.0/"
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

<!-- public repository -->
<xsl:template match="fabio:*[dct:identifier]">
 <doc>
    <field name="recordtype">opus</field>
    <field name="edition"><xsl:value-of select="@rdf:about"/></field>
    <xsl:apply-templates select="dct:*"/>
    <xsl:apply-templates select="foaf:img" />
    <xsl:apply-templates select="pro:editor" />
    <xsl:apply-templates select="prism:issn" />
    <field name="allfields">
        <xsl:value-of select="dct:identifier"/>
        <xsl:value-of select="' '"/>
        <xsl:value-of select="normalize-space(.)"/>
    </field>
    <xsl:apply-templates select="." mode="type"/>
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
   <field name="author2"><xsl:value-of select="."/></field>
   <field name="author2-role"><xsl:value-of select="'Editor'"/></field>
</xsl:template>

<xsl:template match="prism:issn">
   <field name="issn"><xsl:value-of select="." /></field>
</xsl:template>

<!-- PUBLISHER -->
<xsl:template match="dct:publisher">
   <xsl:apply-templates select="aiiso:Faculty" />
   <xsl:apply-templates select="aiiso:Center" />
   <xsl:apply-templates select="aiiso:Division" />
   <xsl:apply-templates select="aiiso:Institute" />
   <xsl:apply-templates select="foaf:Organization" />
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
  <field name="isbn"><xsl:value-of select="."/></field>
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

<xsl:template match="dct:modified">
  <field name="last_indexed">
  <xsl:value-of select="concat(.,'T00:00:01Z')" /></field>
</xsl:template>

<xsl:template match="dct:issued[position()=1]">
  <field name="publishDate"><xsl:value-of select="." /></field>
  <field name="publishDateSort"><xsl:value-of select="." /></field>
  <field name="first_indexed">
  <xsl:value-of select="concat(.,'T00:00:01Z')" /></field>
</xsl:template>

<xsl:template match="dct:created">
  <field name="era"><xsl:value-of select="substring(.,1,4)" /></field>
  <field name="era_facet"><xsl:value-of select="substring(.,1,4)" /></field>
</xsl:template>

<xsl:template match="dct:extend">
  <field name="physical"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="foaf:img">
 <field name="thumbnail"><xsl:value-of select="." /></field>
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
  <field name="contents"><xsl:value-of select="."/></field>
</xsl:template>

<!-- annotations etc. -->
<xsl:template match="dct:isReferencedBy">
  <field name="url"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dct:relation">
  <field name="url"><xsl:value-of select="." /></field>
</xsl:template>

<!-- Top Hierarchies -->
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
</xsl:template>

<xsl:template match="dct:hasPart[position()>1]">
</xsl:template>

<xsl:template match="dct:isPartOf">
  <xsl:apply-templates select="fabio:*[dct:identifier]"/>
</xsl:template>

<!-- level one : issue -->
<xsl:template match="dct:isPartOf/fabio:Periodical|dct:isPartOf/fabio:Journal">
  <field name="hierarchytype"></field>
  <field name="is_hierarchy_id">
    <xsl:call-template name="identity">
      <xsl:with-param name="id" select="../../dct:identifier"/>
    </xsl:call-template>
  </field>
  <field name="is_hierarchy_title">
     <xsl:value-of select="../../dct:title" />
  </field>

  <field name="hierarchy_parent_id">
    <xsl:call-template name="identity">
        <xsl:with-param name="id" select="dct:identifier"/>
    </xsl:call-template>
  </field>
  <field name="hierarchy_parent_title">
     <xsl:value-of select="dct:title"/>
  </field>

  <xsl:apply-templates mode="hierarchy" select="."/>
</xsl:template>

<!-- level two : item -->
<xsl:template match="dct:isPartOf/fabio:PeriodicalIssue|dct:isPartOf/fabio:JournalIssue">
  <xsl:apply-templates select="dct:publisher"/>
  <xsl:apply-templates select="dct:isPartOf/fabio:Journal/dct:publisher"/>
  <xsl:apply-templates select="dct:isPartOf/fabio:Periodical/dct:publisher"/>
  <field name="hierarchytype"></field>

  <field name="is_hierarchy_id">
    <xsl:call-template name="identity">
      <xsl:with-param name="id" select="../../dct:identifier"/>
    </xsl:call-template>
  </field>
  <field name="is_hierarchy_title">
     <xsl:value-of select="../../dct:title" />
  </field>

  <field name="hierarchy_parent_id">
    <xsl:call-template name="identity">
        <xsl:with-param name="id" select="dct:identifier"/>
    </xsl:call-template>
  </field>
  <field name="hierarchy_parent_title">
     <xsl:value-of select="dct:title"/>
  </field>

  <xsl:apply-templates mode="hierarchy" select="dct:isPartOf/fabio:Periodical"/>
  <xsl:apply-templates mode="hierarchy" select="dct:isPartOf/fabio:Journal"/>
</xsl:template>

<!-- top level mode -->
<xsl:template match="dct:isPartOf/fabio:Periodical|dct:isPartOf/fabio:Journal"
              mode="hierarchy">
  <field name="hierarchy_top_id">
     <xsl:call-template name="identity">
          <xsl:with-param name="id" select="dct:identifier"/>
     </xsl:call-template>
  </field>
  <field name="hierarchy_top_title">
     <xsl:value-of select="dct:title"/>
  </field>
  <field name="hierarchy_browse">
     <xsl:value-of select="concat(dct:title,'{{{_ID_}}}',substring(dct:identifier,0,string-length(dct:identifier)))" />
  </field>
</xsl:template>

<xsl:template match="text()"/>
<xsl:template match="*" priority="-1"/>

</xsl:stylesheet>
