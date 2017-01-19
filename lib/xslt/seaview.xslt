<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
     xmlns:vcard="http://www.w3.org/2001/vcard-rdf/3.0#"
     version="1.0" >

<xsl:output method="xml" encoding="UTF-8" indent="yes" />
<xsl:strip-space elements="*"/> 

<xsl:template match="rdf:RDF">
 <add>
   <xsl:apply-templates select="dcterms:BibliographicResource"/>
 </add>
</xsl:template>

<xsl:template match="dcterms:BibliographicResource[dcterms:identifier]">
 <xsl:comment>Seaview Bibliographic Resource Transformer (2016)</xsl:comment>
 <doc>
    <xsl:apply-templates select="dcterms:*"/>
    <xsl:apply-templates select="foaf:img"/>
    <field name="allfields"><xsl:value-of 
           select="(substring-after(substring-after(@rdf:about,'//'),'/'))"/>
           <xsl:for-each select="//*/dcterms:*"><xsl:value-of 
           select="concat(' ', normalize-space(text()))"/></xsl:for-each>
           <xsl:for-each select="dcterms:*/foaf:*/*[text()]"><xsl:value-of 
           select="concat(' ', normalize-space(text()))"/></xsl:for-each>
    </field>
    <xsl:apply-templates select="." mode="spec"/>
 </doc>
</xsl:template>

<xsl:template name="identity">
  <xsl:param name="id"/>
  <xsl:value-of select="substring($id,0,string-length($id))" />
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(text(),'isbn:')]">
  <field name="isbn"><xsl:value-of select="substring(.,6)"/></field>
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(text(),'issn:')]">
  <field name="issn"><xsl:value-of select="substring(.,6)"/></field>
  <field name="oai_set_str_mv"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(text(),'oclc:')]">
  <field name="oclc_num"><xsl:value-of select="substring(.,6)"/></field>
</xsl:template>

<!-- not used:
<xsl:template match="dcterms:identifier[starts-with(text(),'opus:')]">
  <field name="opus_str"><xsl:value-of select="substring(.,6)"/></field>
</xsl:template>
-->

<xsl:template match="dcterms:identifier[starts-with(text(),'http://dx.doi.org/')]">
  <field name="doi_str_mv"><xsl:value-of select="."/></field>
  <field name="edition"><xsl:value-of select="."/></field>
</xsl:template>

<!-- opac -->
<xsl:template match="dcterms:hasVersion">
  <field name="edition"><xsl:value-of select="."/></field>
</xsl:template>

<!-- TITLE -->
<xsl:template match="dcterms:title[@xml:lang]">
 <xsl:variable name="lang"><xsl:call-template name="getlang"/></xsl:variable>
 <xsl:choose>
  <xsl:when test="@xml:lang!=$lang">
   <field name="title_alt"><xsl:value-of select="." /></field>
  </xsl:when>
 </xsl:choose>
</xsl:template>

<!-- MAIN TITLE -->
<xsl:template name="title" match="dcterms:title[not(@xml:lang)][1]">
  <field name="title"><xsl:value-of select="." /></field>
  <field name="title_short"><xsl:value-of select="." /></field>
  <field name="title_full"><xsl:value-of select="." /></field>
  <field name="title_sort"><xsl:value-of select="." /></field>
</xsl:template>

<!-- Opus subtitle -->
<xsl:template match="dcterms:alternative">
  <field name="title_sub"><xsl:value-of select="." /></field>
</xsl:template>

<!-- AUTHOR -->
<xsl:template match="dcterms:creator">
  <xsl:apply-templates select="foaf:Person"/>
  <xsl:apply-templates select="rdf:Seq"/>
</xsl:template>

<xsl:template match="dcterms:creator/foaf:Person">
  <xsl:apply-templates select="foaf:name"/>
  <xsl:apply-templates select="foaf:role"/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq">
  <xsl:apply-templates select="rdf:li/foaf:Person"/>
  <xsl:apply-templates select="rdf:li[@rdf:resource]"/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li/foaf:Person">
    <xsl:apply-templates select="foaf:name"/>
    <xsl:apply-templates select="foaf:role"/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li[@rdf:resource]">
  <xsl:param name="about" select="@rdf:resource"/>
  <xsl:apply-templates select="//foaf:Person[@rdf:about=$about]"/>
</xsl:template>

<xsl:template match="dcterms:creator/foaf:Person/foaf:name">
  <field name="author"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li/foaf:Person/foaf:name">
  <field name="author"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="foaf:Person/foaf:role">
  <field name="author_role"><xsl:value-of select="."/></field>
</xsl:template>

<!-- searchable, but not displayed -->
<xsl:template match="dcterms:creator/rdf:Seq/rdf:li/foaf:Person[foaf:role='add']/foaf:name">
  <field name="author_additional"><xsl:value-of select="."/></field>
</xsl:template>

<!-- CONTRIBUTOR -->
<xsl:template match="dcterms:contributor">
    <xsl:apply-templates select="foaf:Person"/>
    <xsl:apply-templates select="foaf:Organization"/>
</xsl:template>

<xsl:template match="dcterms:contributor/foaf:Person">
    <xsl:apply-templates select="foaf:name"/>
    <xsl:apply-templates select="foaf:role"/>
</xsl:template>

<xsl:template match="dcterms:contributor/foaf:Person/foaf:name[1]">
   <field name="author2"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:contributor/foaf:Person/foaf:role[1]">
   <field name="author2_role"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:contributor/foaf:Organization">
    <xsl:apply-templates select="foaf:name"/>
    <xsl:apply-templates select="foaf:role"/>
</xsl:template>

<xsl:template match="dcterms:contributor/foaf:Organization/foaf:name[1]">
   <field name="author_corporate"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:contributor/foaf:Organization/foaf:role[1]">
   <field name="author_corporate_role"><xsl:value-of select="."/></field>
</xsl:template>

<!-- opus -->
<xsl:template match="dcterms:provenance">
   <!-- <xsl:comment><xsl:value-of select="' provenance '"/></xsl:comment> -->
   <xsl:apply-templates select="aiiso:Faculty" />
   <xsl:apply-templates select="aiiso:Center" />
   <xsl:apply-templates select="aiiso:Division" />
   <xsl:apply-templates select="aiiso:Institute" />
   <xsl:apply-templates select="rdf:Description/vcard:Country" />
   <xsl:apply-templates select="rdf:Description/vcard:Orgname" />
</xsl:template>

<!-- opac -->
<xsl:template match="dcterms:medium">
   <xsl:apply-templates select="rdf:Seq/rdf:li/dcterms:PhysicalMedium" />
</xsl:template>

<xsl:template match="dcterms:PhysicalMedium">
   <xsl:apply-templates select="dcterms:spatial/dcterms:Location" />
   <xsl:apply-templates select="dcterms:spatial[@rdf:resource]" />
   <xsl:apply-templates select="rdfs:label" />
</xsl:template>

<xsl:template match="dcterms:spatial/dcterms:Location">
  <xsl:apply-templates select="foaf:name"/>
</xsl:template>

<xsl:template match="dcterms:spatial[@rdf:resource]">
  <xsl:variable name="about" select="@rdf:resource"/>
  <xsl:apply-templates select="//*/dcterms:Location[@rdf:about=$about]"/>
</xsl:template>

<xsl:template match="dcterms:spatial/dcterms:Location/foaf:name">
  <field name="institution"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:PhysicalResource/rdfs:label">
  <field name="callnumber-raw"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:publisher">
   <xsl:apply-templates select="foaf:Organization" />
</xsl:template>

<xsl:template match="dcterms:publisher/foaf:Organization">
  <field name="publisher"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dcterms:provenance/aiiso:Faculty">
  <field name="building"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dcterms:provenance/aiiso:Center">
  <field name="building"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dcterms:provenance/aiiso:Division">
  <field name="building"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dcterms:provenance/aiiso:Institute">
  <field name="institution"><xsl:value-of select="foaf:name"/></field>
</xsl:template>

<xsl:template match="dcterms:provenance/rdf:Description/vcard:Country">
  <xsl:apply-templates select="rdf:Description/vcard:NAME"/>
</xsl:template>

<xsl:template match="vcard:Country/rdf:Description/vcard:NAME">
  <field name="building"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:provenance/rdf:Description/vcard:Orgname">
  <field name="institution"><xsl:value-of select="substring(.,0,16)"/></field>
</xsl:template>

<!-- OJS : original article URL -->
<xsl:template match="dcterms:source[@rdf:resource]">
   <field name="url"><xsl:value-of select="@rdf:resource"/></field>
</xsl:template>

<!-- Opus : source_title loaded from driver -->
<xsl:template match="dcterms:source[not(@rdf:resource)]">
  <!-- <field name="title_old"><xsl:value-of select="."/></field> -->
</xsl:template>

<!-- FORMAT see type -->
<xsl:template match="dcterms:format">
  <!-- <field name="format"><xsl:value-of select="."/></field> -->
</xsl:template>

<!-- LANGUAGE -->
<xsl:template match="dcterms:language[@rdf:resource]">
 <xsl:call-template name="language">
   <xsl:with-param name="lang" select="substring-after(
    ../dcterms:language/@rdf:resource, 'iso639-1/')"/>
 </xsl:call-template>
</xsl:template>

<xsl:template name="language" match="dcterms:language[not(@rdf:resource)]">
 <xsl:param name="lang" select="."/>
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
  <xsl:value-of 
    select="substring-after(../dcterms:language/@rdf:resource, 'iso639-1/')"/>
</xsl:template>

<xsl:template match="dcterms:modified[1]">
  <field name="publishDateSort"><xsl:value-of select="." /></field>
  <field name="last_indexed">
      <xsl:value-of select="concat(.,'T23:59:59Z')"/>
  </field>
</xsl:template>

<xsl:template match="dcterms:issued[1]">
  <field name="first_indexed">
      <xsl:value-of select="concat(.,'T00:00:00Z')" />
  </field>
  <xsl:if test="not(../dcterms:modified)">
  <field name="publishDateSort"><xsl:value-of select="." /></field>
  <field name="last_indexed">
      <xsl:value-of select="concat(.,'T23:59:59Z')" />
  </field>
  </xsl:if>
</xsl:template>

<xsl:template match="dcterms:created">
  <field name="publishDate"><xsl:value-of select="." /></field>
  <field name="era_facet"><xsl:value-of select="substring(.,1,4)" /></field>
  <!-- displayed as topic if present
  <field name="era"><xsl:value-of select="substring(.,1,4)" /></field>
  -->
</xsl:template>

<xsl:template match="dcterms:extent">
  <xsl:apply-templates select="dcterms:SizeOrDuration"/>
</xsl:template>

<xsl:template match="dcterms:extent/dcterms:SizeOrDuration">
  <field name="physical"><xsl:value-of select="rdf:value"/></field>
</xsl:template>

<xsl:template match="foaf:img[1]">
  <field name="thumbnail"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="dcterms:type[@rdf:resource]">
  <xsl:variable name="type" select="substring-after(@rdf:resource,'/fabio/')"/>
  <xsl:choose>
  <xsl:when test="$type='DoctoralThesis'">
    <field name="format"><xsl:value-of select="'Dissertation'"/></field>
    <field name="oai_set_str_mv">
        <xsl:value-of select="concat('doc-type:','doctoralThesis')"/>
    </field>
  </xsl:when>
  <xsl:when test="$type='JournalArticle'">
    <field name="format"><xsl:value-of select="'Journal Articles'"/></field>
    <field name="oai_set_str_mv">
        <xsl:value-of select="concat('doc-type:','article')"/>
    </field>
  </xsl:when>
  <xsl:when test="$type='JournalIssue'">
    <field name="format"><xsl:value-of select="'Issue'"/></field>
    <field name="oai_set_str_mv">
        <xsl:value-of select="concat('doc-type:','PeriodicalPart')"/>
    </field>
  </xsl:when>
  <xsl:when test="$type='Journal'">
    <field name="format"><xsl:value-of select="'Journal'"/></field>
    <field name="oai_set_str_mv">
        <xsl:value-of select="concat('doc-type:','Periodical')"/>
    </field>
  </xsl:when>
  <xsl:when test="$type='Book'">
    <field name="format"><xsl:value-of select="'Book'"/></field>
    <field name="oai_set_str_mv">
        <xsl:value-of select="concat('doc-type:','book')"/>
    </field>
  </xsl:when>
  <xsl:when test="$type='BookChapter'">
    <field name="format"><xsl:value-of select="'Book Chapter'"/></field>
    <field name="oai_set_str_mv">
        <xsl:value-of select="concat('doc-type:','bookPart')"/>
    </field>
  </xsl:when>
  <xsl:when test="$type='Periodical'">
    <field name="format"><xsl:value-of select="'Volume Holdings'"/></field>
    <field name="oai_set_str_mv">
        <xsl:value-of select="concat('doc-type:','Periodical')"/>
    </field>
  </xsl:when>
  <xsl:when test="$type='PeriodicalIssue'">
    <field name="format"><xsl:value-of select="'Volume'"/></field>
    <field name="oai_set_str_mv">
        <xsl:value-of select="concat('doc-type:','PeriodicalPart')"/>
    </field>
  </xsl:when>
  <xsl:when test="$type='MusicalComposition'">
    <field name="format"><xsl:value-of select="'Musical Score'"/></field>
    <field name="oai_set_str_mv">
        <xsl:value-of select="concat('doc-type:','MusicalNotation')"/>
    </field>
  </xsl:when>
  <xsl:when test="$type='MastersThesis'">
    <field name="format"><xsl:value-of select="'Work'"/></field>
    <field name="oai_set_str_mv">
        <xsl:value-of select="concat('doc-type:','masterThesis')"/>
    </field>
  </xsl:when>
  <xsl:when test="$type='MovingImage'">
    <field name="format"><xsl:value-of select="'Video'"/></field>
  </xsl:when>
  <xsl:when test="$type='BachelorsThesis'">
    <field name="format"><xsl:value-of select="'Work'"/></field>
    <field name="oai_set_str_mv">
        <xsl:value-of select="concat('doc-type:','bachelorThesis')"/>
    </field>
  </xsl:when>
  <xsl:when test="$type='Biography'">
    <field name="format"><xsl:value-of select="'Book'"/></field>
    <field name="oai_set_str_mv">
        <xsl:value-of select="concat('doc-type:','book')"/>
    </field>
  </xsl:when>
  <xsl:when test="$type='CollectedWorks'">
    <field name="format"><xsl:value-of select="'Collected Works'"/></field>
    <field name="oai_set_str_mv">
        <xsl:value-of select="concat('doc-type:','book')"/>
    </field>
  </xsl:when>
  <xsl:when test="$type='Article'">
    <field name="format"><xsl:value-of select="'Article'"/></field>
  </xsl:when>
  <xsl:when test="$type='Volume'">
    <field name="format"><xsl:value-of select="'Volume'"/></field>
  </xsl:when>
  <xsl:when test="$type='Database'">
    <field name="format"><xsl:value-of select="'Database'"/></field>
  </xsl:when>
  <xsl:when test="$type='Film'">
    <field name="format"><xsl:value-of select="'Video'"/></field>
  </xsl:when>
  <xsl:when test="$type='AudioDocument'">
    <field name="format"><xsl:value-of select="'Audio'"/></field>
  </xsl:when>
  <xsl:when test="$type='Excerpt'">
    <field name="format"><xsl:value-of select="'Excerpt'"/></field>
  </xsl:when>
  <xsl:when test="$type='Image'">
    <field name="format"><xsl:value-of select="'Photo'"/></field>
  </xsl:when>
  <xsl:when test="$type='Dataset'"><!-- format from hasPart --></xsl:when>
  <xsl:otherwise>
    <field name="format"><xsl:value-of select="'Work'"/></field>
    <field name="oai_set_str_mv">
        <xsl:value-of select="concat('doc-type:','report')"/>
    </field>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- TODO : dini publication types -->
<!--
<xsl:template match="fabio:*/dcterms:type">
  <field name="oai_set_str_mv">
      <xsl:value-of select="concat('doc-type:',.)"/>
  </field>
</xsl:template>
-->

<xsl:template match="dcterms:subject">
  <xsl:apply-templates select="skos:Concept"/>
</xsl:template>

<!-- RVK -->
<xsl:template match="skos:Concept[contains(@rdf:about,'rvk')]">
  <field name="genre"><xsl:value-of select="rdfs:label"/></field>
  <field name="genre_facet"><xsl:value-of select="rdfs:label"/></field>
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

<xsl:template match="dcterms:subject/skos:Concept[not(@rdf:about)]">
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
<xsl:template match="dcterms:abstract">
 <xsl:variable name="lang"><xsl:call-template name="getlang"/></xsl:variable>
 <xsl:comment>description <xsl:value-of select="$lang"/></xsl:comment>
 <xsl:choose>
  <xsl:when test="@xml:lang=$lang">
     <field name="description"><xsl:value-of select="."/></field>
  </xsl:when>
  <xsl:when test="count(../dcterms:language)=0 and @xml:lang='en'">
     <field name="description"><xsl:value-of select="."/></field>
  </xsl:when>
  <xsl:when test="count(../dcterms:abstract)=1">
     <field name="description"><xsl:value-of select="."/></field>
  </xsl:when>
  <!-- need a solution for no abstract in document language
  <xsl:when test="count(../dcterms:abstract)=2 and @xml:lang='en'">
     <field name="description"><xsl:value-of select="."/></field>
  </xsl:when>
  -->
  <xsl:otherwise>
     <field name="contents"><xsl:value-of select="."/></field>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="dcterms:hasPart[not(@rdf:resource)]">
  <xsl:apply-templates select="dctypes:Text"/>
  <xsl:apply-templates select="dctypes:Image"/>
  <xsl:apply-templates select="dctypes:MovingImage"/>
  <xsl:apply-templates select="dctypes:Collection"/>
  <xsl:apply-templates select="dctypes:Dataset"/>
</xsl:template>

<xsl:template match="dcterms:hasPart/dctypes:Text">
 <xsl:choose>
  <xsl:when test="contains(@rdf:about, '/mets-')">
   <field name="url">
      <xsl:value-of select="concat(../../@rdf:about,'/view.html')"/>
   </field>
  </xsl:when>
  <xsl:when test="contains(@rdf:about, '/All.pdf')"></xsl:when>
  <xsl:when test="../../dcterms:source[@rdf:resource]"><!--OJS--></xsl:when>
  <xsl:otherwise>
   <field name="url"><xsl:value-of select="@rdf:about"/></field>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="dcterms:hasPart/dctypes:Image">
  <field name="url"><xsl:value-of select="@rdf:about"/></field>
</xsl:template>

<xsl:template match="dcterms:hasPart/dctypes:MovingImage">
    <field name="url"><xsl:value-of select="@rdf:about"/></field>
</xsl:template>

<!-- container.zip -->
<xsl:template match="dcterms:hasPart/dctypes:Collection">
  <field name="url"><xsl:value-of select="@rdf:about"/></field>
</xsl:template>

<!-- data/data.zip -->
<xsl:template match="dcterms:hasPart/dctypes:Dataset">
  <field name="url"><xsl:value-of select="@rdf:about"/></field>
  <field name="format"><xsl:value-of select="'Dataset'"/></field>
</xsl:template>

<!-- LICENSE : core extension -->
<xsl:template match="dcterms:license[@rdf:resource]">
  <field name="license_str"><xsl:value-of select="@rdf:resource"/></field>
  <xsl:choose>
   <xsl:when test="../dcterms:accessRights"><!--restricted-->
     <field name="oai_set_str_mv">
            <xsl:value-of select="'restricted_access'"/></field>
   </xsl:when>
   <xsl:otherwise>
     <field name="oai_set_str_mv"><xsl:value-of select="'open_access'"/></field>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- RIGHTS : not evaluated (TODO) -->
<xsl:template match="dcterms:rights[@rdf:resource]">
</xsl:template>

<!-- RESTRICTED ACCESS : core extension : IP address list -->
<xsl:template match="dcterms:accessRights">
   <field name="rights_str_mv"><xsl:value-of select="."/></field>
</xsl:template>

<!-- Top Hierarchies -->
<xsl:template match="dcterms:hasPart[@rdf:resource][1][count(../dcterms:isPartOf)=0]"> 
  <xsl:comment><xsl:value-of select="'hierarchy top'"/></xsl:comment>
  <field name="hierarchytype"></field>
  <field name="hierarchy_top_id">
    <xsl:call-template name="identity">
      <xsl:with-param name="id" select="../dcterms:identifier[starts-with(text(),'urn:')]"/>
    </xsl:call-template>
  </field>
  <field name="hierarchy_top_title">
     <xsl:value-of select="../dcterms:title" />
  </field>
  <field name="is_hierarchy_id">
    <xsl:call-template name="identity">
      <xsl:with-param name="id" select="../dcterms:identifier[starts-with(text(),'urn:')]"/>
    </xsl:call-template>
  </field>
  <field name="is_hierarchy_title">
     <xsl:value-of select="../dcterms:title" />
  </field>
</xsl:template>

<xsl:template match="dcterms:hasPart[@rdf:resource][position()>1]">
  <!-- <field name="url"><xsl:value-of select="@rdf:resource" /></field> -->
</xsl:template>

<!-- opac -->
<xsl:template match="dcterms:tableOfContents">
  <xsl:apply-templates select="dcterms:BibliographicResource"/>
</xsl:template>

<xsl:template match="dcterms:tableOfContents/dcterms:BibliographicResource">
  <field name="contents">
    <xsl:value-of select="concat('[',dcterms:title,'](',@rdf:about,')')"/>
  </field>
</xsl:template>

<xsl:template match="dcterms:isPartOf[not(dcterms:BibliographicResource)]">
  <field name="series"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dcterms:isPartOf[dcterms:BibliographicResource]">
  <xsl:comment><xsl:value-of select="'hierarchy level one'"/></xsl:comment>
  <field name="hierarchytype"></field>
  <field name="is_hierarchy_id">
    <xsl:call-template name="identity"><xsl:with-param name="id" 
         select="../dcterms:identifier[starts-with(text(),'urn:')]"/>
    </xsl:call-template>
  </field>
  <field name="is_hierarchy_title">
      <xsl:value-of select="../dcterms:title"/></field>
  <field name="hierarchy_parent_id">
    <xsl:call-template name="identity"><xsl:with-param name="id" 
         select="*/dcterms:identifier[starts-with(text(),'urn:')]"/>
    </xsl:call-template>
  </field>
  <field name="hierarchy_parent_title">
     <xsl:value-of select="*/dcterms:title[not(@xml:lang)]"/>
  </field>
  <xsl:apply-templates select="dcterms:BibliographicResource" mode="hierarchy"/>
</xsl:template>

<!-- TOP hierarchy -->
<xsl:template match="dcterms:isPartOf/dcterms:BibliographicResource[count(dcterms:isPartOf)=0]" mode="hierarchy">
  <xsl:variable name="oid">
     <xsl:call-template name="identity"><xsl:with-param name="id" 
          select="dcterms:identifier[starts-with(text(),'urn:')]"/>
     </xsl:call-template>
  </xsl:variable>
  <xsl:comment><xsl:value-of select="'hierarchy level top'"/></xsl:comment>
  <field name="hierarchy_top_id"><xsl:value-of select="$oid"/></field>
  <field name="hierarchy_top_title">
      <xsl:value-of select="dcterms:title"/>
  </field>
  <field name="hierarchy_browse">
     <xsl:value-of select="concat(dcterms:title,'{{{_ID_}}}',$oid)"/>
  </field>
  <xsl:apply-templates select="dcterms:provenance"/>
  <xsl:apply-templates select="dcterms:publisher"/>
  <xsl:apply-templates select="dcterms:identifier[starts-with(text(),'issn:')]"/>
</xsl:template>

<!-- hierarchy with more levels to follow-->
<xsl:template match="dcterms:isPartOf/dcterms:BibliographicResource[count(dcterms:isPartOf)=1]" mode="hierarchy">
  <xsl:comment><xsl:value-of select="'hierarchy level'"/></xsl:comment>
  <xsl:apply-templates select="dcterms:isPartOf/*" mode="hierarchy"/>
  <xsl:apply-templates select="dcterms:title" mode="container"/>
</xsl:template>

<!-- GH201603 : container test : multiple titles ? -->
<!--
<xsl:template match="dcterms:title[not(@xml:lang)]" mode="container">
  <field name="container_title"><xsl:value-of select="."/></field>
  <field name="container_issue"><xsl:value-of select="."/></field>
</xsl:template>
-->

<xsl:template match="*" mode="container">
</xsl:template>

<!-- FULLTEXT : extension dcterms:tableOfContents dcterms:description -->
<xsl:template match="dcterms:description">
  <field name="fulltext"><xsl:value-of select="."/></field>
</xsl:template>

<!-- REFERENCES : core extension -->
<xsl:template match="dcterms:references">
  <xsl:apply-templates select="rdf:Seq"/>
</xsl:template>

<xsl:template match="dcterms:BibliographicResource[starts-with(@rdf:about,'file')]/dcterms:references/rdf:Seq">
  <!-- References to solr -->
  <xsl:apply-templates select="rdf:li"/>
</xsl:template>

<xsl:template match="dcterms:BibliographicResource[starts-with(@rdf:about,'http')]/dcterms:references/rdf:Seq">
  <!-- References handled by RecordDriver -->
  <field name="ref_str_mv"><xsl:value-of select="'references'"/></field>
</xsl:template>

<!-- from sparql -->
<xsl:template match="dcterms:references[@rdf:resource]">
  <field name="ref_str_mv"><xsl:value-of select="'references'"/></field>
</xsl:template>

<xsl:template match="dcterms:references/rdf:Seq/rdf:li">
  <xsl:apply-templates select="dcterms:BibliographicResource"/>
</xsl:template>

<xsl:template match="dcterms:references/rdf:Seq/rdf:li/dcterms:BibliographicResource">
 <field name="ref_str_mv">
  <xsl:choose>
  <xsl:when test="starts-with(@rdf:about,'http://localhost')">
   <xsl:value-of select="concat(dcterms:bibliographicCitation,' :: ',dcterms:title)"/>
  </xsl:when>
  <xsl:otherwise>
   <xsl:value-of select="concat(dcterms:bibliographicCitation,' :: ',@rdf:about)"/>
  </xsl:otherwise>
  </xsl:choose>
 </field>
</xsl:template>

<!-- CITATIONS : core extension -->
<xsl:template match="dcterms:isReferencedBy[@rdf:resource]">
 <field name="cites_str_mv"><xsl:value-of select="@rdf:resource"/></field>
</xsl:template>

<xsl:template match="dcterms:isReferencedBy[not(@rdf:resource)]">
  <xsl:apply-templates select="dcterms:BibliographicResource"/>
</xsl:template>

<xsl:template match="dcterms:isReferencedBy/dcterms:BibliographicResource">
 <field name="cites_str_mv"><xsl:value-of select="@rdf:about"/></field>
</xsl:template>

<!-- CALLNUMBER : uri part -->
<xsl:template match="*[starts-with(@rdf:about,'http')]" mode="call">
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

<xsl:template match="*[starts-with(@rdf:about,'file')]" mode="call">
</xsl:template>

<!-- make sure that bibliographic resources are identified -->
<xsl:template match="dcterms:BibliographicResource[count(dcterms:identifier[starts-with(text(),'urn:')])=0][count(dcterms:identifier[starts-with(text(),'ppn:')])=0]" mode="spec">
  <field name="recordtype">opus</field>
  <field name="id"><xsl:value-of select="dcterms:identifier[1]"/></field>
  <xsl:if test="count(dcterms:hasPart)=0">
  <field name="url"><xsl:value-of select="@rdf:about"/></field>
  </xsl:if>
</xsl:template>

<xsl:template match="dcterms:BibliographicResource[count(dcterms:identifier[starts-with(text(),'urn:')])=1][count(dcterms:identifier[starts-with(text(),'ppn:')])=0]" mode="spec">
  <xsl:variable name="urn"><xsl:call-template name="identity">
      <xsl:with-param name="id" 
          select="dcterms:identifier[starts-with(text(),'urn:')]"/>
  </xsl:call-template></xsl:variable>
  <field name="recordtype">opus</field>
  <field name="id"><xsl:value-of select="$urn"/></field>
  <field name="urn_str">
      <xsl:value-of select="dcterms:identifier[starts-with(text(),'urn:')]"/>
  </field>
  <xsl:choose>
   <xsl:when test="dcterms:hasPart"><!--parts have their own urls--></xsl:when>
   <xsl:otherwise>
      <field name="url"><xsl:value-of select="@rdf:about"/></field>
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

  <xsl:choose>
  <xsl:when test="contains(@rdf:about,'/eb/2014/10')">
      <field name="series">
          <xsl:value-of select="'Semesterapparat Bohde WS 2014/15 Fotobücher'"/>
      </field>
   </xsl:when>
   <xsl:when test="contains(@rdf:about,'/eb/')"></xsl:when>
   <xsl:otherwise>
      <field name="oai_set_str_mv">
          <xsl:value-of select="'xMetaDissPlus'"/>
      </field>
   </xsl:otherwise>
  </xsl:choose>
  <xsl:apply-templates select="." mode="call"/>
</xsl:template>

<!-- opac -->
<xsl:template match="dcterms:BibliographicResource[count(dcterms:identifier[starts-with(text(),'ppn:')])=1]" mode="spec">
    <field name="recordtype">opac</field>
    <field name="id"><xsl:value-of select="substring-after(dcterms:identifier[starts-with(text(),'ppn:')],'ppn:')"/></field>
</xsl:template>

<!-- suppress emptyness -->
<xsl:template match="*" priority="-1"/>
<xsl:template match="*" mode="hierarchy" priority="-1"/>

</xsl:stylesheet>
