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
     version="1.0" >

<xsl:output method="xml" encoding="UTF-8" indent="yes" />

<xsl:template match="rdf:RDF">
 <add>
   <xsl:comment>Seaview RDF Transformer (2015)</xsl:comment>
   <xsl:apply-templates select="fabio:*" />
   <xsl:apply-templates select="dct:BibliographicResource" />
 </add>
</xsl:template>

<xsl:template match="dct:BibliographicResource[dct:identifier]">
 <doc>
    <field name="recordtype">opus</field>
    <xsl:apply-templates select="dct:*"/>
    <xsl:apply-templates select="foaf:img" />
    <xsl:apply-templates select="ore:aggregates" />
    <xsl:apply-templates select="fabio:hasDOI" />
    <field name="allfields">
        <xsl:value-of select="normalize-space(.)"/>
        <xsl:if test="dct:isReferencedBy">
        <xsl:value-of select="' isReferencedBy '"/>
        </xsl:if>
    </field>
 </doc>
</xsl:template>

<xsl:template match="fabio:*[dct:identifier]">
 <doc>
    <field name="recordtype">opus</field>
    <field name="uri_str"><xsl:value-of select="@rdf:about"/></field>
    <xsl:apply-templates select="dct:*"/>
    <xsl:apply-templates select="foaf:img" />
    <xsl:apply-templates select="fabio:hasISSN" />
    <xsl:apply-templates select="ore:aggregates" />
    <xsl:apply-templates select="fabio:hasDOI" />
    <xsl:apply-templates select="fabio:hasISBN" />
    <xsl:apply-templates select="fabio:hasISSN" />
    <xsl:apply-templates select="fabio:hasURL" />
    <field name="allfields"><xsl:value-of select="normalize-space(.)"/></field>
    <xsl:apply-templates select="." mode="type"/>
    <xsl:apply-templates select="." mode="spec"/>
    <xsl:apply-templates select="." mode="call"/>
 </doc>
</xsl:template>

<xsl:template match="dct:identifier">
  <field name="id">
   <xsl:call-template name="identity">
    <xsl:with-param name="id" select="."/>
   </xsl:call-template>
  </field>
  <field name="urn_str"><xsl:value-of select="."/></field>
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
</xsl:template>

<!-- AUTHOR -->
<xsl:template match="dct:creator[1]">
  <xsl:apply-templates select="foaf:Person"/>
  <xsl:apply-templates select="rdf:Seq"/>
</xsl:template>

<xsl:template match="dct:creator/foaf:Person">
  <xsl:apply-templates select="foaf:name"/>
</xsl:template>

<xsl:template match="dct:creator/foaf:Person[1]/foaf:name[1]">
  <field name="author"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dct:creator/rdf:Seq">
    <xsl:apply-templates select="rdf:li/foaf:Person"/>
</xsl:template>

<xsl:template match="dct:creator/rdf:Seq/rdf:li">
    <xsl:apply-templates select="foaf:Person"/>
</xsl:template>

<xsl:template match="dct:creator/rdf:Seq/rdf:li/foaf:Person">
    <xsl:apply-templates select="foaf:name"/>
</xsl:template>

<xsl:template match="dct:creator/rdf:Seq/rdf:li[1]/foaf:Person/foaf:name">
  <field name="author"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template 
     match="dct:creator/rdf:Seq/rdf:li[position()>1]/foaf:Person/foaf:name">
  <field name="author2"><xsl:value-of select="."/></field>
</xsl:template>

<!-- searchable, but not displayed -->
<xsl:template match="dct:creator/rdf:Seq/rdf:li/foaf:Person[foaf:role='add']/foaf:name">
  <field name="author_additional"><xsl:value-of select="."/></field>
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

<!-- ISBN ISSN -->
<xsl:template match="dct:source">
  <xsl:choose>
  <xsl:when test="string-length(.)=9 and substring(.,4,1)='-'">
    <field name="issn"><xsl:value-of select="."/></field>
  </xsl:when>
  <xsl:when test="string-length(.)=17 and substring(.,3,1)='-'">
    <field name="isbn"><xsl:value-of select="."/></field>
  </xsl:when>
  <xsl:otherwise>
    <field name="series"><xsl:value-of select="."/></field>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="fabio:hasISBN">
  <field name="isbn"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="fabio:hasISSN">
  <field name="issn"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="fabio:hasDOI">
  <field name="doi_str"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="fabio:hasURL">
  <field name="edition"><xsl:value-of select="."/></field>
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

<xsl:template match="dct:issued[1]">
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

<!-- dini publication types -->
<xsl:template match="dct:type">
  <field name="oai_set_str_mv">
      <xsl:value-of select="concat('doc-type:',.)"/>
  </field>
</xsl:template>

<xsl:template match="foaf:img[1]">
  <field name="thumbnail"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="dct:BibliographicResource" mode="type">
</xsl:template>

<xsl:template match="fabio:*" mode="type">
  <xsl:choose>
  <xsl:when test="local-name()='DoctoralThesis'">
    <field name="format"><xsl:value-of select="'Dissertation'"/></field>
  </xsl:when>
  <xsl:when test="local-name()='JournalArticle'">
    <field name="format"><xsl:value-of select="'Journal Articles'"/></field>
  </xsl:when>
  <xsl:when test="local-name()='JournalIssue'">
    <field name="format"><xsl:value-of select="'Issue'"/></field>
  </xsl:when>
  <xsl:when test="local-name()='BookChapter'">
    <field name="format"><xsl:value-of select="'Book Chapter'"/></field>
  </xsl:when>
  <xsl:when test="local-name()='Periodical'">
    <field name="format"><xsl:value-of select="'Volume Holdings'"/></field>
  </xsl:when>
  <xsl:when test="local-name()='PeriodicalIssue'">
    <field name="format"><xsl:value-of select="'Volume'"/></field>
  </xsl:when>
  <!--
  <xsl:when test="local-name()='PeriodicalItem'">
    <field name="format"><xsl:value-of select="'Volume'"/></field>
  </xsl:when>
  -->
  <xsl:when test="local-name()='MusicalComposition'">
    <field name="format"><xsl:value-of select="'Musical Score'"/></field>
  </xsl:when>
  <xsl:when test="local-name()='MastersThesis'">
    <field name="format"><xsl:value-of select="'Book'"/></field>
  </xsl:when>
  <xsl:when test="local-name()='BachelorsThesis'">
    <field name="format"><xsl:value-of select="'Book'"/></field>
  </xsl:when>
  <xsl:when test="local-name()='Biography'">
    <field name="format"><xsl:value-of select="'Book'"/></field>
  </xsl:when>
  <xsl:when test="local-name()='CollectedWorks'">
    <field name="format"><xsl:value-of select="'Collected Works'"/></field>
  </xsl:when>
  <xsl:otherwise>
    <field name="format"><xsl:value-of select="local-name()"/></field>
  </xsl:otherwise>
  </xsl:choose>

  <xsl:choose>
  <xsl:when test="contains(@rdf:about,'/eb/')">
    <field name="collection"><xsl:value-of select="'Digitalisat'"/></field>
  </xsl:when>
  <xsl:when test="contains(@rdf:about,'/es/')">
    <field name="collection"><xsl:value-of select="'Monograph'"/></field>
  </xsl:when>
  <xsl:when test="contains(@rdf:about,'/diss/')">
    <field name="collection"><xsl:value-of select="'Monograph'"/></field>
  </xsl:when>
  </xsl:choose>
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
      <field name="topic_facet"><xsl:value-of select="." /></field>
    </xsl:otherwise>
  </xsl:choose>
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

<xsl:template match="ore:aggregates[@rdf:resource]">
  <xsl:choose>
   <xsl:when test="contains(@rdf:resource,'All.pdf')"><!-- viewer --></xsl:when>
   <xsl:when test="contains(@rdf:resource,'/mets-')">
     <field name="url">
         <xsl:value-of select="concat(../@rdf:about,'/view.html')"/>
     </field>
     <field name="collection"><xsl:value-of select="'Digitalisat'"/></field>
     <field name="oai_set_str_mv"><xsl:value-of select="'Digitalisat'"/></field>
   </xsl:when>
   <xsl:when test="substring(@rdf:resource,string-length(@rdf:resource)-3,string-length(@rdf:resource))='.txt'"><!-- skip .txt files --></xsl:when>
   <xsl:when test="starts-with(@rdf:resource, 'http://localhost/')">
     <field name="url"><xsl:value-of select="substring(@rdf:resource,18)"/>
     </field>
   </xsl:when>
   <xsl:otherwise>
     <field name="url"><xsl:value-of select="@rdf:resource" /></field>
   </xsl:otherwise>
  </xsl:choose>
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
  <!--
  <field name="container_title"><xsl:value-of select="fabio:*/dct:title"/>
  </field>
  <field name="container_reference"><xsl:value-of select="fabio:*/dct:title"/>
  </field>
  -->

  <xsl:apply-templates select="fabio:Book[dct:identifier]" mode="hierarchy"/>
  <xsl:apply-templates select="fabio:PeriodicalIssue[dct:identifier]" mode="hierarchy"/>
  <xsl:apply-templates select="fabio:JournalIssue[dct:identifier]" mode="hierarchy"/>
  <xsl:apply-templates select="fabio:Journal[dct:identifier]" mode="hierarchy"/>
  <xsl:apply-templates select="fabio:Periodical[dct:identifier]" mode="hierarchy"/>
  <xsl:apply-templates select="fabio:JournalIssue/dct:subject" />
  <xsl:apply-templates select="fabio:JournalIssue/fabio:hasISSN" />
</xsl:template>

<!-- level two : issue -->
<xsl:template match="dct:isPartOf/fabio:PeriodicalIssue|dct:isPartOf/fabio:JournalIssue" mode="hierarchy">
  <xsl:apply-templates select="dct:publisher"/>
  <xsl:apply-templates select="dct:isPartOf/fabio:Journal/dct:publisher"/>
  <xsl:apply-templates select="dct:isPartOf/fabio:Periodical/dct:publisher"/>

  <xsl:apply-templates select="dct:isPartOf/fabio:Periodical" mode="hierarchy"/>
  <xsl:apply-templates select="dct:isPartOf/fabio:Journal" mode="hierarchy"/>
</xsl:template>

<!-- is_hierarchy = hierarchy_top : Collection -->
<!-- level one : issue -->
<xsl:template match="dct:isPartOf/fabio:Journal" mode="hierarchy">
  <!-- journal hierarchy -->
  <field name="hierarchy_top_id">
     <xsl:call-template name="identity">
          <xsl:with-param name="id" select="dct:identifier"/>
     </xsl:call-template>
  </field>
  <field name="hierarchy_top_title"><xsl:value-of select="dct:title"/></field>
  <!-- journal issue hierarchy -->
  <field name="hierarchy_top_id">
     <xsl:call-template name="identity">
          <xsl:with-param name="id" select="../../dct:identifier"/>
     </xsl:call-template>
  </field>
  <field name="hierarchy_top_title">
     <xsl:value-of select="../../dct:title"/>
  </field>
  <field name="hierarchy_browse">
     <xsl:value-of select="concat(../../dct:title,'{{{_ID_}}}',substring(../../dct:identifier,0,string-length(../../dct:identifier)))" />
  </field>
</xsl:template>

<!-- level one : issue -->
<xsl:template match="dct:isPartOf/fabio:Periodical|dct:isPartOf/fabio:Book" mode="hierarchy">
  <field name="hierarchy_top_id">
     <xsl:call-template name="identity">
          <xsl:with-param name="id" select="dct:identifier"/>
     </xsl:call-template>
  </field>
  <field name="hierarchy_top_title"><xsl:value-of select="dct:title"/></field>
  <field name="hierarchy_browse">
     <xsl:value-of select="concat(dct:title,'{{{_ID_}}}',substring(dct:identifier,0,string-length(dct:identifier)))" />
  </field>
</xsl:template>

<!-- FULLTEXT : fulltext extension -->
<xsl:template match="dct:fulltext">
  <field name="fulltext"><xsl:value-of select="."/></field>
</xsl:template>

<!-- REFERENCES : core extension -->
<xsl:template match="dct:references">
  <xsl:apply-templates select="rdf:Seq"/>
  <xsl:apply-templates select="rdf:Description"/>
</xsl:template>

<xsl:template match="dct:references/rdf:Seq">
  <xsl:apply-templates select="rdf:li/dct:BibliographicResource"/>
</xsl:template>

<xsl:template match="dct:references/rdf:Description">
  <xsl:apply-templates select="rdf:li/dct:BibliographicResource"/>
</xsl:template>

<xsl:template match="rdf:li/dct:BibliographicResource">
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
 <field name="cites_str_mv"><xsl:value-of select="@rdf:resource"/></field>
</xsl:template>

<!-- DOWNLOADS : core extension -->
<xsl:template match="dct:downloads">
 <field name="downloads_str"><xsl:value-of select="."/></field>
</xsl:template>

<!-- SERIES : spec extension -->
<xsl:template match="fabio:*" mode="spec">
 <xsl:choose>
  <xsl:when test="contains(@rdf:about,'/eb/2014/10')">
   <field name="series2">
    <xsl:value-of select="'Semesterapparat Bohde WS 2014/15 Fotobücher'"/>
   </field>
  </xsl:when>
  <xsl:when test="contains(@rdf:about,'/ep/')">
    <field name="oai_set_str_mv">
        <xsl:value-of select="'xMetaDissPlus'"/></field>
  </xsl:when>
 </xsl:choose>
</xsl:template>

<xsl:template match="fabio:*" mode="call">
  <xsl:variable name="callnumber">
      <xsl:value-of select="translate(
           substring-after(substring-after(@rdf:about,'//'),'/'),'/',' ')"/>
  </xsl:variable>
  <field name="callnumber-raw"><xsl:value-of select="$callnumber"/></field>
  <field name="callnumber-sort"><xsl:value-of select="$callnumber"/></field>
  <field name="callnumber-label"><xsl:value-of select="$callnumber"/></field>
  <field name="callnumber-first">
      <xsl:value-of select="substring-before($callnumber,' ')"/>
  </field>
  <field name="callnumber-subject">
      <xsl:value-of select="concat(substring-before($callnumber,' '),'/',
                    substring-before(substring-after($callnumber,' '),' '))"/>
  </field>
</xsl:template>

<!-- suppress emptyness -->
<!-- <xsl:template match="text()"/> -->

<xsl:template match="*" priority="-1"/>

</xsl:stylesheet>
