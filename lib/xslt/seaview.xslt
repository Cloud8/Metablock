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

<xsl:template match="rdf:RDF">
 <add>
   <xsl:comment>Shanghai RDF Transformer (2014)</xsl:comment>
   <xsl:apply-templates select="fabio:*" />
   <xsl:apply-templates select="dct:BibliographicResource" />
 </add>
</xsl:template>

<xsl:template match="dct:BibliographicResource[dct:identifier]">
 <doc>
    <field name="recordtype">opus</field>
    <field name="edition"><xsl:value-of select="@rdf:about"/></field>
    <field name="alternative_str">
       <xsl:value-of select="substring-after(@rdf:about,'http://localhost/')"/>
    </field>
    <xsl:apply-templates select="dct:*"/>
    <xsl:apply-templates select="foaf:img" />
    <field name="allfields">
        <xsl:value-of select="dct:identifier"/>
        <xsl:value-of select="' '"/>
        <xsl:if test="count(dct:references)>0">
          <xsl:value-of select="'seaview '"/>
        </xsl:if>
        <xsl:value-of select="normalize-space(dct:*)"/>
    </field>
 </doc>
</xsl:template>

<xsl:template match="fabio:*[dct:identifier]">
 <doc>
    <field name="recordtype">opus</field>
    <field name="edition"><xsl:value-of select="@rdf:about"/></field>
    <xsl:apply-templates select="dct:*"/>
    <xsl:apply-templates select="foaf:img" />
    <xsl:apply-templates select="pro:editor" />
    <xsl:apply-templates select="prism:issn" />
    <field name="allfields">
        <xsl:value-of 
         select="substring(dct:identifier,0,string-length(dct:identifier))" />
        <xsl:value-of select="' '"/>
        <xsl:value-of select="substring(@rdf:about,32)"/>
        <xsl:value-of select="' '"/>
        <xsl:if test="count(dct:references)>0">
          <xsl:value-of select="'seaview '"/>
        </xsl:if>
        <xsl:for-each select="dct:*[not(@xml:lang)]">
            <xsl:value-of select="concat(normalize-space(.),' ')"/>
        </xsl:for-each>
    </field>
    <xsl:apply-templates select="." mode="type"/>
 </doc>
</xsl:template>

<xsl:template match="dct:identifier">
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
<xsl:template match="dct:title[@xml:lang]">
  <field name="title_alt"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template name="title" match="dct:title[not(@xml:lang)][1]">
  <field name="title"><xsl:value-of select="." /></field>
  <field name="title_short"><xsl:value-of select="." /></field>
  <field name="title_full"><xsl:value-of select="." /></field>
  <field name="title_sort"><xsl:value-of select="." /></field>
  <!-- <field name="title_fullStr"><xsl:value-of select="." /></field> -->
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
   <field name="author_additional"><xsl:value-of select="." /></field>
</xsl:template>

<!-- EDITOR -->
<xsl:template match="pro:editor">
   <field name="author2"><xsl:value-of select="."/></field>
   <field name="author2-role"><xsl:value-of select="'Editor'"/></field>
</xsl:template>

<xsl:template match="prism:issn">
   <field name="issn"><xsl:value-of select="." /></field>
   <field name="oai_set_str_mv">
       <xsl:value-of select="concat('issn:',.)"/></field>
</xsl:template>

<!-- PUBLISHER -->
<xsl:template match="dct:publisher">
   <xsl:apply-templates select="aiiso:Faculty" />
   <xsl:apply-templates select="aiiso:Center" />
   <xsl:apply-templates select="aiiso:Division" />
   <xsl:apply-templates select="aiiso:Institute" />
   <xsl:apply-templates select="foaf:Organization" />
</xsl:template>

<xsl:template match="dct:publisher/foaf:Organization">
  <field name="publisher"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dct:publisher/aiiso:Faculty">
  <field name="institution"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dct:publisher/aiiso:Center">
  <field name="institution"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dct:publisher/aiiso:Division">
  <field name="institution"><xsl:value-of select="foaf:name"/></field>
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
  <field name="series"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="fabio:hasISBN">
  <field name="isbn"><xsl:value-of select="."/></field>
</xsl:template>

<!-- FORMAT -->
<xsl:template match="dct:format">
  <field name="format"><xsl:value-of select="."/></field>
</xsl:template>

<!-- LANGUAGE -->
<xsl:template match="dct:language">
 <field name="language">
  <xsl:choose>
   <xsl:when test=".='de'">German</xsl:when>
   <xsl:when test=".='en'">English</xsl:when>
   <xsl:when test=".='fr'">French</xsl:when>
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

<xsl:template match="dct:issued">
  <field name="publishDate"><xsl:value-of select="." /></field>
  <field name="publishDateSort"><xsl:value-of select="." /></field>
  <field name="first_indexed">
      <xsl:value-of select="concat(.,'T00:00:00Z')" />
  </field>
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

<xsl:template match="dct:BibliographicResource" mode="type">
</xsl:template>

<!-- https://www.coar-repositories.org/activities/repository-interoperability/ig-controlled-vocabularies-for-repository-assets/wiki/info-eu-repo/ -->
<xsl:template match="fabio:Book|fabio:Biography" mode="type">
  <field name="format"><xsl:value-of select="'Book'"/></field>
  <field name="oai_set_str_mv">
         <xsl:value-of select="'doc-type:book'"/></field>
  <xsl:choose>
  <xsl:when test="contains(@rdf:about,'/eb/')">
    <field name="collection"><xsl:value-of select="'Digitalisat'"/></field>
  </xsl:when>
  <xsl:when test="contains(@rdf:about,'/es/')">
    <field name="collection"><xsl:value-of select="'Monograph'"/></field>
    <field name="oai_set_str_mv">
         <xsl:value-of select="'doc-type:report'"/>
    </field>
    <field name="oai_set_str_mv"><xsl:value-of select="'xMetaDissPlus'"/>
    </field>
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
  <field name="collection"><xsl:value-of select="'Monograph'"/></field>
  <field name="oai_set_str_mv"><xsl:value-of select="'xMetaDissPlus'"/></field>
  <field name="oai_set_str_mv">
      <xsl:value-of select="'doc-type:doctoralThesis'"/>
  </field>
</xsl:template>

<xsl:template match="fabio:Journal" mode="type">
  <field name="format"><xsl:value-of select="'Journal'"/></field>
  <field name="oai_set_str_mv"><xsl:value-of select="'doc-type:Periodical'"/></field>
</xsl:template>

<xsl:template match="fabio:JournalIssue" mode="type">
  <field name="format"><xsl:value-of select="'Issue'"/></field>
  <field name="oai_set_str_mv"><xsl:value-of select="'doc-type:Periodical'"/></field>
</xsl:template>

<xsl:template match="fabio:JournalArticle" mode="type">
  <field name="format"><xsl:value-of select="'Journal Articles'"/></field>
  <field name="oai_set_str_mv">
         <xsl:value-of select="'doc-type:article'"/>
  </field>
  <xsl:if test="contains(@rdf:about,'/ep/')">
  <field name="oai_set_str_mv"><xsl:value-of select="'xMetaDissPlus'"/></field>
  </xsl:if>
</xsl:template>

<xsl:template match="fabio:Periodical" mode="type">
  <field name="format"><xsl:value-of select="'Volume Holdings'"/></field>
</xsl:template>

<xsl:template match="fabio:PeriodicalIssue" mode="type">
  <field name="format"><xsl:value-of select="'Volume'"/></field>
</xsl:template>

<xsl:template match="fabio:PeriodicalItem" mode="type">
  <field name="format"><xsl:value-of select="'Issue'"/></field>
  <xsl:if test="contains(@rdf:about,'/eb/')">
    <field name="collection"><xsl:value-of select="'Digitalisat'"/></field>
  </xsl:if>
</xsl:template>

<xsl:template match="fabio:Article" mode="type">
  <field name="format"><xsl:value-of select="'Article'"/></field>
  <xsl:if test="contains(@rdf:about,'/eb/')">
    <field name="collection"><xsl:value-of select="'Digitalisat'"/></field>
  </xsl:if>
</xsl:template>

<xsl:template match="fabio:Image" mode="type">
  <field name="format"><xsl:value-of select="'Image'"/></field>
</xsl:template>

<xsl:template match="fabio:MastersThesis" mode="type">
  <field name="collection"><xsl:value-of select="'Monograph'"/></field>
  <field name="oai_set_str_mv">
         <xsl:value-of select="'doc-type:masterThesis'"/>
  </field>
</xsl:template>

<xsl:template match="fabio:MusicalComposition" mode="type">
  <field name="format"><xsl:value-of select="'Musical Score'"/></field>
</xsl:template>

<xsl:template match="fabio:*" mode="type">
  <field name="format"><xsl:value-of select="'Work'"/></field>
</xsl:template>

<xsl:template match="dct:subject">
  <xsl:apply-templates select="skos:Concept"/>
  <xsl:choose>
    <xsl:when test="count(*)>0"></xsl:when>
    <xsl:when test="normalize-space(.)=''"></xsl:when>
    <xsl:when test="contains(.,',')">
      <field name="topic"><xsl:value-of select="." /></field>
    </xsl:when>
    <xsl:otherwise>
      <field name="topic"><xsl:value-of select="." /></field>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- SWD TOPICS as blank nodes, DDC Topic qualified -->
<xsl:template match="dct:subject/skos:Concept">
  <xsl:if test="substring-after(@rdf:about,'class/')!=''">
  <field name="oai_set_str_mv">
   <xsl:value-of select="concat('ddc:',substring-after(@rdf:about,'class/'))"/>
  </field>
  <field name="dewey-hundreds">
      <xsl:value-of select="substring-after(@rdf:about,'class/')"/>
  </field>
  </xsl:if>
  <xsl:apply-templates select="skos:prefLabel" />
</xsl:template>

<xsl:template match="skos:Concept/skos:prefLabel">
    <field name="topic_facet"><xsl:value-of select="." /></field>
    <field name="topic_browse"><xsl:value-of select="." /></field>
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

<!-- annotations -->
<xsl:template match="dct:isReferencedBy">
  <field name="url"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dct:hasFormat">
  <xsl:choose>
   <xsl:when test="contains(substring-before(.,'.xml'),'/mets')">
     <field name="url">
      <xsl:value-of select="concat(../@rdf:about,'/view.html')" />
     </field>
    <field name="collection"><xsl:value-of select="'Digitalisat'"/></field>
    <field name="oai_set_str_mv"><xsl:value-of select="'Digitalisat'"/></field>
   </xsl:when>
   <xsl:otherwise>
    <field name="url"><xsl:value-of select="." /></field>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- core extension : alternative edition -->
<xsl:template match="dct:alternative">
  <field name="alternative_str">
    <xsl:choose>
    <xsl:when test="contains(.,'download')">
    <xsl:value-of select="concat(substring-before(.,'download'),'view',substring-after(.,'download'))"/>
    </xsl:when>
    <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
    </xsl:choose>
  </field>
  <xsl:choose>
   <xsl:otherwise>
     <field name="url"><xsl:value-of select="../dct:alternative" /></field>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="dct:relation">
  <xsl:choose>
   <xsl:when test="contains(.,'All.pdf')"><!-- viewer --></xsl:when>
   <xsl:when test="count(../dct:alternative)>0"></xsl:when>
   <xsl:otherwise>
     <field name="url"><xsl:value-of select="." /></field>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- RIGHTS : core extension -->
<xsl:template match="dct:rights">
 <xsl:choose>
  <xsl:when test="../dct:accessRights"><!--Magazin-->
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
</xsl:template>

<!-- RIGHTS : core extension : IP address list -->
<xsl:template match="dct:accessRights">
   <field name="rights_str_mv"><xsl:value-of select="."/></field>
   <!--
   <field name="rights_str_mv"><xsl:value-of select="'127.0.0.1'"/></field>
   -->
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
  <field name="hierarchytype"></field>
  <field name="is_hierarchy_id">
    <xsl:call-template name="identity">
      <xsl:with-param name="id" select="../dct:identifier"/>
    </xsl:call-template>
  </field>
  <field name="is_hierarchy_title"><xsl:value-of select="../dct:title"/></field>

  <field name="hierarchy_parent_id">
    <xsl:call-template name="identity">
        <xsl:with-param name="id" select="fabio:*/dct:identifier"/>
    </xsl:call-template>
  </field>
  <field name="hierarchy_parent_title">
     <xsl:value-of select="fabio:*/dct:title"/>
  </field>

  <xsl:apply-templates select="fabio:Book[dct:identifier]" mode="hierarchy"/>
  <xsl:apply-templates select="fabio:PeriodicalIssue[dct:identifier]" mode="hierarchy"/>
  <xsl:apply-templates select="fabio:JournalIssue[dct:identifier]" mode="hierarchy"/>
  <xsl:apply-templates select="fabio:Journal[dct:identifier]" mode="hierarchy"/>
  <xsl:apply-templates select="fabio:Periodical[dct:identifier]" mode="hierarchy"/>
  <xsl:apply-templates select="fabio:JournalIssue/dct:subject" />
  <xsl:apply-templates select="fabio:JournalIssue/prism:issn" />
</xsl:template>

<!-- level two : issue -->
<xsl:template match="dct:isPartOf/fabio:PeriodicalIssue|dct:isPartOf/fabio:JournalIssue" mode="hierarchy">
  <xsl:apply-templates select="dct:publisher"/>
  <xsl:apply-templates select="dct:isPartOf/fabio:Journal/dct:publisher"/>
  <xsl:apply-templates select="dct:isPartOf/fabio:Periodical/dct:publisher"/>

  <xsl:apply-templates mode="hierarchy" select="dct:isPartOf/fabio:Periodical"/>
  <xsl:apply-templates mode="hierarchy" select="dct:isPartOf/fabio:Journal"/>
</xsl:template>

<!-- is_hierarchy = hierarchy_top : Collection -->
<!-- level one : issue -->
<xsl:template match="dct:isPartOf/fabio:Journal" mode="hierarchy">
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

<!-- level one : issue -->
<xsl:template match="dct:isPartOf/fabio:Periodical|dct:isPartOf/fabio:Book" mode="hierarchy">
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

<!-- FULLTEXT : ojs extension -->
<xsl:template match="dct:fulltex">
  <field name="fulltext"><xsl:value-of select="."/></field>
</xsl:template>

<!-- REFERENCES : core extension -->
<xsl:template match="dct:references">
  <xsl:apply-templates select="rdf:Seq"/>
</xsl:template>

<xsl:template match="dct:references/rdf:Seq">
  <xsl:apply-templates select="rdf:li/dct:BibliographicResource"/>
</xsl:template>

<xsl:template match="dct:references/rdf:Seq/rdf:li">
  <xsl:apply-templates select="dct:BibliographicResource"/>
</xsl:template>

<xsl:template match="dct:references/rdf:Seq/rdf:li/dct:BibliographicResource">
 <field name="ref_str_mv">
  <xsl:choose>
  <xsl:when test="starts-with(@rdf:about,'http://localhost')">
   <xsl:value-of select="concat(dct:bibliographicCitation,' :: ',dct:title)"/>
  </xsl:when>
  <xsl:otherwise>
   <xsl:value-of select="concat(dct:bibliographicCitation,' :: ',@rdf:about)"/>
  </xsl:otherwise>
  </xsl:choose>
 </field>
</xsl:template>

<!-- CITATIONS : core extension -->
<xsl:template match="dct:isReferencedBy">
 <field name="cites_str"><xsl:value-of select="."/></field>
</xsl:template>

<!-- DOWNLOADS : core extension -->
<xsl:template match="dct:downloads">
 <field name="downloads_str"><xsl:value-of select="."/></field>
</xsl:template>

<!-- suppress emptyness -->
<!--
<xsl:template match="text()"/>
-->

<xsl:template match="*" priority="-1"/>

</xsl:stylesheet>
