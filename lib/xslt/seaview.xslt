<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
     version="1.0" >

<xsl:output method="xml" encoding="UTF-8" indent="yes" />
<xsl:strip-space elements="*"/> 

<xsl:template match="rdf:RDF">
 <add>
   <xsl:apply-templates select="fabio:*" />
   <xsl:apply-templates select="dct:BibliographicResource"/>
 </add>
</xsl:template>

<xsl:template match="dct:BibliographicResource[dct:identifier]">
 <xsl:comment>Seaview Bibliographic Resource Transformer (2016)</xsl:comment>
 <doc>
    <field name="allfields"><xsl:for-each select="dct:*">
        <xsl:value-of select="concat(' ', normalize-space(text()))"/>
    </xsl:for-each></field>
    <xsl:apply-templates select="dct:*"/>
    <xsl:apply-templates select="foaf:img"/>
    <xsl:apply-templates select="." mode="spec"/>
 </doc>
</xsl:template>

<xsl:template match="fabio:*[dct:identifier]">
 <doc>
    <xsl:apply-templates select="dct:*"/>
    <xsl:apply-templates select="foaf:img" />
    <xsl:apply-templates select="fabio:hasDOI" />
    <xsl:apply-templates select="fabio:hasISBN" />
    <xsl:apply-templates select="fabio:hasISSN" />
    <xsl:apply-templates select="fabio:hasURL" />
    <field name="allfields">
        <xsl:value-of 
            select="(substring-after(substring-after(@rdf:about,'//'),'/'))"/>
        <xsl:for-each select="dct:*">
            <xsl:value-of select="concat(' ', normalize-space(text()))"/>
        </xsl:for-each>
        <xsl:for-each select="fabio:*">
            <xsl:value-of select="concat(' ', normalize-space(text()))"/>
        </xsl:for-each>
    </field>
    <xsl:apply-templates select="." mode="type"/>
    <xsl:apply-templates select="." mode="spec"/>
    <xsl:apply-templates select="." mode="call"/>
 </doc>
</xsl:template>

<xsl:template match="dct:identifier[starts-with(text(),'urn:')]">
  <field name="recordtype">opus</field>
  <field name="id"><xsl:call-template name="identity">
    <xsl:with-param name="id" select="."/></xsl:call-template>
  </field>
  <field name="urn_str"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template name="identity">
  <xsl:param name="id"/>
  <xsl:value-of select="substring($id,0,string-length($id))" />
</xsl:template>

<xsl:template match="dct:identifier[starts-with(text(),'isbn:')]">
  <field name="isbn"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dct:identifier[starts-with(text(),'issn:')]">
  <field name="issn"><xsl:value-of select="."/></field>
  <field name="oai_set_str_mv">
      <xsl:value-of select="concat('issn:',.)"/></field>
</xsl:template>

<xsl:template match="dct:identifier[starts-with(text(),'oclc:')]">
  <field name="oclc_num"><xsl:value-of select="substring(.,7)"/></field>
</xsl:template>

<!-- Soon to be used by OJS -->
<xsl:template match="dct:identifier[starts-with(text(),'http:')]">
  <field name="url"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dct:identifier[starts-with(text(),'http://dx.doi.org/')]">
  <field name="doi_str"><xsl:value-of select="."/></field>
</xsl:template>

<!-- TITLE -->
<xsl:template match="dct:title[@xml:lang]">
 <xsl:variable name="lang"><xsl:call-template name="getlang"/></xsl:variable>
 <xsl:choose>
  <xsl:when test="@xml:lang!=$lang">
   <field name="title_alt"><xsl:value-of select="." /></field>
  </xsl:when>
 </xsl:choose>
</xsl:template>

<xsl:template name="title" match="dct:title[not(@xml:lang)][1]">
  <field name="title"><xsl:value-of select="." /></field>
  <field name="title_short"><xsl:value-of select="." /></field>
  <field name="title_full"><xsl:value-of select="." /></field>
  <field name="title_sort"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="dct:alternative">
  <field name="title_sub"><xsl:value-of select="." /></field>
</xsl:template>

<!-- AUTHOR -->
<xsl:template match="dct:creator[1]">
  <xsl:apply-templates select="foaf:Person"/>
  <xsl:apply-templates select="rdf:Seq"/>
</xsl:template>

<xsl:template match="dct:creator/foaf:Person">
  <xsl:apply-templates select="foaf:name"/>
</xsl:template>

<xsl:template match="dct:creator/rdf:Seq">
  <xsl:apply-templates select="rdf:li/foaf:Person"/>
</xsl:template>

<xsl:template match="dct:creator/rdf:Seq/rdf:li/foaf:Person">
    <xsl:apply-templates select="foaf:name"/>
</xsl:template>

<xsl:template match="dct:creator/foaf:Person[1]/foaf:name[1]">
  <field name="author"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dct:creator/rdf:Seq/rdf:li[1][not(@rdf:resource)]/foaf:Person[1]/foaf:name[1]">
  <field name="author"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dct:creator/rdf:Seq/rdf:li[@rdf:resource]">
  <xsl:variable name="rc" select="@rdf:resource"/>
  <!--<xsl:comment><xsl:value-of select="$rc"/></xsl:comment>-->
  <field name="author2">
  <xsl:value-of select="//*/dct:creator/rdf:Seq/rdf:li/foaf:Person[@rdf:about=$rc]/foaf:name"/>
  </field>
</xsl:template>

<xsl:template match="dct:creator/rdf:Seq/rdf:li[position()>1]/foaf:Person/foaf:name">
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

<xsl:template match="dct:provenance">
   <xsl:apply-templates select="aiiso:Faculty" />
   <xsl:apply-templates select="aiiso:Center" />
   <xsl:apply-templates select="aiiso:Division" />
   <xsl:apply-templates select="aiiso:Institute" />
</xsl:template>

<xsl:template match="dct:publisher">
   <xsl:apply-templates select="foaf:Organization" />
</xsl:template>

<xsl:template match="dct:publisher/foaf:Organization">
  <field name="publisher"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dct:provenance/aiiso:Faculty">
  <field name="institution"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dct:provenance/aiiso:Center">
  <field name="institution"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dct:provenance/aiiso:Division">
  <field name="institution"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dct:provenance/aiiso:Institute">
  <field name="building"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<!-- GH201507 : source information loaded from driver
<xsl:template match="dct:source">
</xsl:template>
-->

<!-- ISBN Old -->
<xsl:template match="fabio:hasISBN">
  <field name="isbn"><xsl:value-of select="."/></field>
</xsl:template>

<!-- ISSN Old -->
<xsl:template match="fabio:hasISSN">
   <field name="issn"><xsl:value-of select="." /></field>
   <field name="oai_set_str_mv">
       <xsl:value-of select="concat('issn:',.)"/></field>
</xsl:template>

<!-- OJS Old -->
<xsl:template match="fabio:hasDOI">
  <field name="doi_str">
    <xsl:value-of select="concat('http://dx.doi.org/',.)"/>
  </field>
</xsl:template>

<xsl:template match="fabio:hasURL">
  <field name="edition"><xsl:value-of select="."/></field>
</xsl:template>

<!-- FORMAT -->
<xsl:template match="dct:format">
  <!-- <field name="format"><xsl:value-of select="."/></field> -->
</xsl:template>

<!-- LANGUAGE -->
<xsl:template match="dct:language[@rdf:resource]">
 <xsl:variable name="lang"><xsl:call-template name="getlang"/></xsl:variable>
 <field name="language">
  <xsl:choose>
   <xsl:when test="$lang='de'">German</xsl:when>
   <xsl:when test="$lang='en'">English</xsl:when>
   <xsl:when test="$lang='fr'">French</xsl:when>
   <xsl:when test="$lang='lt'">Latin</xsl:when>
   <xsl:when test="$lang='es'">Spanish</xsl:when>
   <xsl:when test="$lang='it'">Italian</xsl:when>
   <xsl:when test="$lang='ja'">Japanese</xsl:when>
   <xsl:when test="$lang='nl'">Dutch</xsl:when>
   <xsl:when test="$lang='la'">Latin</xsl:when>
   <xsl:when test="$lang='ru'">Russia</xsl:when>
   <xsl:when test="$lang='na'">Papua</xsl:when>
   <xsl:when test="$lang='bi'">Multiple</xsl:when>
   <xsl:when test="$lang='ar'">Arabic</xsl:when>
   <xsl:when test="$lang='el'">Greek</xsl:when>
   <xsl:when test="$lang='he'">Hebrew</xsl:when>
   <xsl:when test="$lang='zu'">Undetermined</xsl:when>
   <xsl:when test="$lang='java'">Java</xsl:when>
   <xsl:when test="$lang='php'">PHP</xsl:when>
   <xsl:otherwise><xsl:value-of select="$lang"/></xsl:otherwise>
  </xsl:choose>
 </field>
</xsl:template>

<xsl:template name="getlang">
  <xsl:value-of select="substring-after(../dct:language/@rdf:resource,
                       'http://www.lexvo.org/id/iso639-1/')"/>
</xsl:template>

<!-- LANGUAGE old -->
<xsl:template match="dct:language[not(@rdf:resource)]">
 <field name="language">
  <xsl:choose>
   <xsl:when test=".='de'">German</xsl:when>
   <xsl:when test=".='en'">English</xsl:when>
   <xsl:when test=".='fr'">French</xsl:when>
   <xsl:when test=".='lt'">Latin</xsl:when>
   <xsl:when test=".='es'">Spanish</xsl:when>
   <xsl:when test=".='it'">Italian</xsl:when>
   <xsl:when test=".='ja'">Japanese</xsl:when>
   <xsl:when test=".='nl'">Dutch</xsl:when>
   <xsl:when test=".='la'">Latin</xsl:when>
   <xsl:when test=".='ru'">Russia</xsl:when>
   <xsl:when test=".='na'">Papua</xsl:when>
   <xsl:when test=".='bi'">Multiple</xsl:when>
   <xsl:when test=".='ar'">Arabic</xsl:when>
   <xsl:when test=".='el'">Greek</xsl:when>
   <xsl:when test=".='he'">Hebrew</xsl:when>
   <xsl:when test=".='zu'">Undetermined</xsl:when>
   <xsl:when test=".='java'">Java</xsl:when>
   <xsl:when test=".='php'">PHP</xsl:when>
   <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
  </xsl:choose>
 </field>
</xsl:template>

<xsl:template match="dct:modified[1]">
  <field name="last_indexed">
      <xsl:value-of select="concat(.,'T23:59:59Z')"/>
  </field>
</xsl:template>

<xsl:template match="dct:issued[1]">
  <!--
  <field name="publishDate"><xsl:value-of select="." /></field>
  <field name="publishDate"><xsl:value-of select="substring(.,1,4)"/></field>
  -->
  <field name="publishDateSort"><xsl:value-of select="." /></field>
  <field name="first_indexed">
      <xsl:value-of select="concat(.,'T00:00:00Z')" />
  </field>
  <xsl:if test="not(../dct:modified)">
  <field name="last_indexed">
      <xsl:value-of select="concat(.,'T23:59:59Z')" />
  </field>
  </xsl:if>
</xsl:template>

<xsl:template match="dct:created">
  <field name="publishDate"><xsl:value-of select="." /></field>
  <field name="era"><xsl:value-of select="substring(.,1,4)" /></field>
  <field name="era_facet"><xsl:value-of select="substring(.,1,4)" /></field>
</xsl:template>

<xsl:template match="dct:extent">
  <xsl:apply-templates select="dct:SizeOrDuration"/>
</xsl:template>

<xsl:template match="dct:extent/dct:SizeOrDuration">
  <field name="physical"><xsl:value-of select="rdf:value"/></field>
</xsl:template>

<xsl:template match="foaf:img[1]">
  <field name="thumbnail"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="dct:BibliographicResource/dct:type">
  <field name="format"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dct:BibliographicResource/dct:type[@rdf:resource]">
  <field name="format">
    <xsl:value-of select="substring-after(@rdf:resource,'/fabio/')"/>
  </field>
</xsl:template>

<!-- dini publication types -->
<xsl:template match="fabio:*/dct:type">
  <field name="oai_set_str_mv">
      <xsl:value-of select="concat('doc-type:',.)"/>
  </field>
</xsl:template>

<xsl:template match="fabio:*" mode="type">
  <xsl:choose>
  <xsl:when test="local-name(.)='DoctoralThesis'">
    <field name="format"><xsl:value-of select="'Dissertation'"/></field>
  </xsl:when>
  <xsl:when test="local-name(.)='JournalArticle'">
    <field name="format"><xsl:value-of select="'Journal Articles'"/></field>
  </xsl:when>
  <xsl:when test="local-name(.)='JournalIssue'">
    <field name="format"><xsl:value-of select="'Issue'"/></field>
  </xsl:when>
  <xsl:when test="local-name(.)='BookChapter'">
    <field name="format"><xsl:value-of select="'Book Chapter'"/></field>
  </xsl:when>
  <xsl:when test="local-name(.)='Periodical'">
    <field name="format"><xsl:value-of select="'Volume Holdings'"/></field>
  </xsl:when>
  <xsl:when test="local-name(.)='PeriodicalIssue'">
    <field name="format"><xsl:value-of select="'Volume'"/></field>
  </xsl:when>
  <!--
  <xsl:when test="local-name(.)='PeriodicalItem'">
    <field name="format"><xsl:value-of select="'Volume'"/></field>
  </xsl:when>
  -->
  <xsl:when test="local-name(.)='MusicalComposition'">
    <field name="format"><xsl:value-of select="'Musical Score'"/></field>
  </xsl:when>
  <xsl:when test="local-name(.)='MastersThesis'">
    <field name="format"><xsl:value-of select="'Book'"/></field>
  </xsl:when>
  <xsl:when test="local-name(.)='BachelorsThesis'">
    <field name="format"><xsl:value-of select="'Book'"/></field>
  </xsl:when>
  <xsl:when test="local-name(.)='Biography'">
    <field name="format"><xsl:value-of select="'Book'"/></field>
  </xsl:when>
  <xsl:when test="local-name(.)='CollectedWorks'">
    <field name="format"><xsl:value-of select="'Collected Works'"/></field>
  </xsl:when>
  <xsl:otherwise>
    <field name="format"><xsl:value-of select="local-name(.)"/></field>
  </xsl:otherwise>
  </xsl:choose>

  <xsl:choose>
  <xsl:when test="contains(@rdf:about,'/es/')">
    <field name="collection"><xsl:value-of select="'Monograph'"/></field>
  </xsl:when>
  <xsl:when test="contains(@rdf:about,'/diss/')">
    <field name="collection"><xsl:value-of select="'Monograph'"/></field>
  </xsl:when>
  <xsl:when test="contains(@rdf:about,'/eb/')">
    <field name="collection"><xsl:value-of select="'Digitalisat'"/></field>
  </xsl:when>
  <xsl:when test="contains(@rdf:about,'/ep/')">
    <field name="collection"><xsl:value-of select="'Article'"/></field>
  </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template match="dct:subject">
  <xsl:apply-templates select="skos:Concept"/>
</xsl:template>

<!-- SWD TOPICS -->
<xsl:template match="skos:Concept[contains(@rdf:about,'swd')]">
   <field name="topic"><xsl:value-of select="rdfs:label"/></field>
</xsl:template>

<!-- DDC Topic -->
<xsl:template match="skos:Concept[contains(@rdf:about,'dewey')]">
  <xsl:param name="class" 
       select="normalize-space(substring-after(@rdf:about,'class/'))"/>
  <xsl:if test="$class!=''">
  <field name="oai_set_str_mv">
   <xsl:value-of select="concat('ddc:',translate($class,translate($class,'0123456789',''),''))"/>
  </field>
  <field name="dewey-raw"><xsl:value-of select="$class"/></field>
  </xsl:if>
  <xsl:apply-templates select="skos:prefLabel" />
</xsl:template>

<!-- DDC has skos:prefLabel -->
<xsl:template match="skos:Concept[@rdf:about]/skos:prefLabel[@xml:lang='de']">
    <field name="topic_facet"><xsl:value-of select="." /></field>
    <field name="topic"><xsl:value-of select="." /></field>
</xsl:template>

<!-- DDC has skos:prefLabel -->
<xsl:template match="skos:Concept[@rdf:about]/skos:prefLabel[@xml:lang='en']">
    <field name="genre"><xsl:value-of select="." /></field>
    <field name="genre_facet"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="dct:subject/skos:Concept[not(@rdf:about)]">
  <xsl:apply-templates select="rdfs:label"/>
  <xsl:apply-templates select="skos:prefLabel"/>
</xsl:template>

<!-- ccs pacs msc metablock -->
<xsl:template match="skos:Concept[not(@rdf:about)]/skos:prefLabel">
    <field name="topic_facet"><xsl:value-of select="." /></field>
    <field name="topic"><xsl:value-of select="." /></field>
</xsl:template>

<!-- TOPICS UNCONTROLLED -->
<xsl:template match="skos:Concept[not(@rdf:about)]/rdfs:label">
   <field name="topic"><xsl:value-of select="."/></field>
</xsl:template>

<!-- contents can be multivalued -->
<xsl:template match="dct:abstract">
 <xsl:variable name="lang"><xsl:call-template name="getlang"/></xsl:variable>
 <!--<xsl:comment>description <xsl:value-of select="$lang"/></xsl:comment>-->
 <xsl:choose>
  <xsl:when test="@xml:lang=$lang">
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

<xsl:template match="dct:hasPart[not(@rdf:resource)]">
  <xsl:apply-templates select="dctypes:Text"/>
  <xsl:apply-templates select="dctypes:Image"/>
  <xsl:apply-templates select="dctypes:MovingImage"/>
  <xsl:apply-templates select="dctypes:Collection"/>
  <xsl:apply-templates select="dctypes:Dataset"/>
</xsl:template>

<xsl:template match="dct:hasPart/dctypes:Text">
 <xsl:choose>
  <xsl:when test="contains(@rdf:about, '/mets-')">
   <field name="url">
      <xsl:value-of select="concat(../../@rdf:about,'/view.html')"/>
   </field>
  </xsl:when>
  <xsl:when test="contains(@rdf:about, '/All.pdf')"></xsl:when>
  <xsl:otherwise>
   <field name="url"><xsl:value-of select="@rdf:about"/></field>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="dct:hasPart/dctypes:Image">
  <field name="url"><xsl:value-of select="@rdf:about"/></field>
</xsl:template>

<!-- TODO: to be handled by record driver -->
<xsl:template match="dct:hasPart/dctypes:MovingImage">
    <field name="url"><xsl:value-of select="@rdf:about"/></field>
 <!-- record driver can handles this if extension is mp4
 <field name="url">
    <xsl:value-of select="concat('video:',substring(@rdf:about,6))"/>
 </field>
 -->
</xsl:template>

<!-- container.zip -->
<xsl:template match="dct:hasPart/dctypes:Collection">
  <field name="url"><xsl:value-of select="@rdf:about"/></field>
</xsl:template>

<!-- data/data.zip -->
<xsl:template match="dct:hasPart/dctypes:Dataset">
  <field name="url"><xsl:value-of select="@rdf:about"/></field>
  <field name="format"><xsl:value-of select="'Dataset'"/></field>
</xsl:template>

<!-- LICENSE : core extension -->
<xsl:template match="dct:license[1]">
  <xsl:choose>
   <xsl:when test="../dct:accessRights"><!--restricted-->
     <field name="oai_set_str_mv">
    <xsl:value-of select="'restricted_access'"/></field>
   </xsl:when>
   <xsl:when test="@rdf:resource">
     <field name="license_str"><xsl:value-of select="@rdf:resource"/></field>
     <field name="oai_set_str_mv"><xsl:value-of select="'open_access'"/></field>
   </xsl:when>
   <xsl:otherwise>
     <field name="oai_set_str_mv"><xsl:value-of select="'open_access'"/></field>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- RIGHTS : not evalueated -->
<xsl:template match="dct:rights[@rdf:resource]">
</xsl:template>

<!-- ACCESS RIGHTS : core extension : IP address list -->
<xsl:template match="dct:accessRights">
   <field name="rights_str_mv"><xsl:value-of select="."/></field>
</xsl:template>

<!-- Top Hierarchies -->
<xsl:template match="fabio:Periodical/dct:hasPart[@rdf:resource][1]|fabio:Journal/dct:hasPart[@rdf:resource][1]|fabio:Book/dct:hasPart[@rdf:resource][1]|fabio:Catalog/dct:hasPart[@rdf:resource][1]"> 
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
  <field name="url"><xsl:value-of select="@rdf:resource" /></field>
</xsl:template>

<xsl:template match="fabio:*/dct:hasPart[@rdf:resource][position()>1]">
  <field name="url"><xsl:value-of select="@rdf:resource" /></field>
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

<!-- FULLTEXT : extension dct:tableOfContents dct:description -->
<xsl:template match="dct:description">
  <field name="fulltext"><xsl:value-of select="."/></field>
</xsl:template>

<!-- REFERENCES : core extension -->
<xsl:template match="dct:references">
  <xsl:apply-templates select="rdf:Seq"/>
</xsl:template>

<xsl:template match="dct:references/rdf:Seq">
  <xsl:apply-templates select="rdf:li"/>
</xsl:template>

<!-- References fetched from somewhere else for display (GH20151105) -->
<xsl:template match="fabio:*/dct:references/rdf:Seq/rdf:li[1]">
 <field name="ref_str_mv"><xsl:value-of select="'references'"/></field>
</xsl:template>

<!-- References written to solr -->
<xsl:template match="dct:BibliographicResource/dct:references/rdf:Seq/rdf:li">
  <xsl:apply-templates select="dct:BibliographicResource"/>
</xsl:template>

<xsl:template match="dct:BibliographicResource/dct:references/rdf:Seq/rdf:li/dct:BibliographicResource">
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
<xsl:template match="dct:isReferencedBy[@rdf:resource]">
 <field name="cites_str_mv">
     <xsl:value-of select="@rdf:resource"/>
 </field>
</xsl:template>

<!-- old : sequenced -->
<xsl:template match="dct:isReferencedBy[1][not(@rdf:resource)]">
  <xsl:apply-templates select="rdf:Seq"/>
</xsl:template>

<xsl:template match="dct:isReferencedBy/rdf:Seq">
  <xsl:apply-templates select="rdf:li"/>
</xsl:template>

<xsl:template match="dct:isReferencedBy/rdf:Seq/rdf:li">
 <field name="cites_str_mv">
     <xsl:value-of select="@rdf:resource"/>
 </field>
</xsl:template>

<!-- DOWNLOADS : core extension -->
<xsl:template match="dct:downloads">
 <field name="downloads_str"><xsl:value-of select="."/></field>
</xsl:template>

<!-- CALLNUMBER : uri part -->
<xsl:template match="fabio:*[starts-with(@rdf:about,'http')]" mode="call">
  <field name="uri_str"><xsl:value-of select="@rdf:about"/></field>
  <xsl:variable name="callnumber"><xsl:value-of 
       select="(substring-after(substring-after(@rdf:about,'//'),'/'))"/>
  </xsl:variable>
  <field name="callnumber-raw"><xsl:value-of select="$callnumber"/></field>
  <field name="callnumber-sort"><xsl:value-of select="$callnumber"/></field>
  <field name="callnumber-label">
         <xsl:value-of select="translate($callnumber,'/',' ')"/></field>
  <field name="callnumber-first">
      <xsl:value-of select="substring-before($callnumber,'/')"/>
  </field>
  <field name="callnumber-subject">
      <xsl:value-of select="concat(substring-before($callnumber,'/'),' ',
                    substring-before(substring-after($callnumber,'/'),'/'))"/>
  </field>
</xsl:template>

<xsl:template match="fabio:*[starts-with(@rdf:about,'file')]" mode="call">
</xsl:template>

<!-- SERIES : spec extension -->
<xsl:template match="fabio:*" mode="spec">
 <xsl:choose>
  <xsl:when test="contains(@rdf:about,'/eb/2014/10')">
   <field name="series2">
    <xsl:value-of select="'Semesterapparat Bohde WS 2014/15 Fotobücher'"/>
   </field>
  </xsl:when>
  <xsl:when test="contains(@rdf:about,'/eb/')"></xsl:when>
  <xsl:when test="contains(@rdf:about,'/diss/')">
  <field name="oai_set_str_mv"><xsl:value-of select="'xMetaDissPlus'"/></field>
  </xsl:when>
  <xsl:when test="contains(@rdf:about,'/es/')">
  <field name="oai_set_str_mv"><xsl:value-of select="'xMetaDissPlus'"/></field>
  </xsl:when>
  <xsl:when test="local-name(.)='JournalArticle'">
  <field name="oai_set_str_mv"><xsl:value-of select="'xMetaDissPlus'"/></field>
  </xsl:when>
  <xsl:otherwise></xsl:otherwise>
 </xsl:choose>
</xsl:template>

<!-- make sure that bibliographic resources have an identifier and an URL -->
<xsl:template match="dct:BibliographicResource" mode="spec">
  <xsl:choose>
   <xsl:when test="count(dct:identifier[starts-with(text(),'ppn:')])=1">
      <field name="recordtype">opac</field>
      <field name="id"><xsl:value-of select="dct:identifier"/></field>
   </xsl:when>
   <xsl:when test="count(dct:identifier)=1">
      <field name="recordtype">opus</field>
      <field name="id"><xsl:value-of select="dct:identifier"/></field>
   </xsl:when>
  </xsl:choose>
  <xsl:choose>
   <xsl:when test="dct:hasPart"><!-- parts have their own urls --></xsl:when>
   <xsl:otherwise>
      <field name="url"><xsl:value-of select="@rdf:about"/></field>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- suppress emptyness -->
<!-- <xsl:template match="text()"/> -->

<xsl:template match="*" priority="-1"/>

</xsl:stylesheet>
