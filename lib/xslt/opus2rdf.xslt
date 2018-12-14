<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:void="http://rdfs.org/ns/void#"
     xmlns:sco="http://schema.org/"
     version="1.0" >

<!-- 
    @license http://www.apache.org/licenses/LICENSE-2.0
    @title Opus to RDF Transformer 
    @date 2015-11-01 2018-11-11
-->

<xsl:output method="xml" indent="yes" encoding="UTF-8" />
<xsl:strip-space elements="*"/>

<xsl:param name="graph" 
    select="document/resultset[@table='domain']/row[1]/field[@name='url']"/>

<xsl:template match="/">
  <xsl:apply-templates select="document" />
</xsl:template>

<xsl:template match="document">
 <rdf:RDF>
  <xsl:comment> Opus2RDF Transformer (2018) </xsl:comment>
  <xsl:apply-templates select="resultset[@table='opus']" />
  <xsl:apply-templates select="resultset[@table='schriftenreihen']" />
 </rdf:RDF>
</xsl:template>

<!-- COLLECTION -->
<xsl:template match="resultset[@table='schriftenreihen']">
  <xsl:param name="uri" select="row/field[@name='url']"/>
  <dcterms:BibliographicResource rdf:about="{$uri}">
  <dcterms:title>
      <xsl:value-of select="row/field[@name='name']"/>
  </dcterms:title>
  <dcterms:identifier>
      <xsl:value-of select="row/field[@name='urn']"/>
  </dcterms:identifier>
  <xsl:apply-templates select="row/field[@name='doi']"/>
  <sco:serialNumber>
      <xsl:value-of select="concat('opus:c',row/field[@name='sr_id'])"/>
  </sco:serialNumber>
  <dcterms:publisher>
      <foaf:Organization rdf:about="{row/field[@name='uni_gnd']}">
          <foaf:name><xsl:value-of select="row/field[@name='universitaet']"/>
          </foaf:name>
    </foaf:Organization>
  </dcterms:publisher>
  <xsl:if test="count(row/field[@name='organization'])=0">
    <dcterms:creator>
      <aiiso:Division rdf:about="{row/field[@name='inst_gnd']}">
          <foaf:name>
                <xsl:value-of select="row/field[@name='instname']"/>
          </foaf:name>
      </aiiso:Division>
    </dcterms:creator>
  </xsl:if>
  <xsl:choose>
    <xsl:when test="row[1]/field[@name='type']='BookChapter'">
      <dcterms:type rdf:resource="http://purl.org/spar/fabio/Book"/>
    </xsl:when>
    <xsl:otherwise>
      <dcterms:type rdf:resource="http://purl.org/spar/fabio/Periodical"/>
    </xsl:otherwise>
  </xsl:choose>
  <foaf:img><xsl:value-of select="concat($uri,'/cover.png')"/></foaf:img>
  <xsl:apply-templates select="row/field[@name='organization']"/>
  <xsl:apply-templates select="row/field[@name='year']"/>
  <xsl:apply-templates select="../resultset[@table='opus_schriftenreihe']" 
       mode="collection"/>
 </dcterms:BibliographicResource>
</xsl:template>

<xsl:template match="resultset[@table='opus_schriftenreihe']" mode="collection">
  <xsl:comment><xsl:value-of select="' Collection '"/></xsl:comment>
  <xsl:apply-templates select="row" mode="collection"/>
</xsl:template>

<xsl:template match="resultset[@table='opus_schriftenreihe']/row" mode="collection">
  <dcterms:hasPart rdf:resource="{field[@name='uri']}"/>
</xsl:template>

<xsl:template match="field[@name='opus_schriftenreihe']/row/field" mode="collection">
</xsl:template>

<xsl:template match="resultset[@table='schriftenreihen']/row/field[@name='year']">
  <dcterms:created><xsl:value-of select="."/></dcterms:created>
</xsl:template>

<xsl:template match="resultset[@table='schriftenreihen']/row/field[@name='organization']">
  <dcterms:creator><foaf:Organization>
      <foaf:name><xsl:value-of select="."/></foaf:name>
  </foaf:Organization></dcterms:creator>
</xsl:template>

<!-- DOCUMENT -->
<xsl:template match="resultset[@table='opus']">
  <xsl:variable name="uri">
    <xsl:choose>
    <xsl:when test="../resultset[@table='statistics']">
        <xsl:value-of select="normalize-space(
             ../resultset[@table='statistics']/row/field[@name='uri'])"/>
    </xsl:when>
    <xsl:otherwise> <!-- RDF breaks with bad URIs -->
    <xsl:value-of select="concat('http://localhost/metacard/card/opus/about/',
         row/field[@name='source_opus'])"/>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <dcterms:BibliographicResource rdf:about="{$uri}">
    <xsl:apply-templates select="row">
      <xsl:with-param name="uri" select="$uri"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="../resultset[@table='opus_autor']">
      <xsl:with-param name="uri" select="$uri"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="../resultset[@table='domain']/row"/>
  </dcterms:BibliographicResource>
</xsl:template>

<xsl:template match="resultset[@table='domain']/row">
    <dcterms:mediator>
        <dcterms:Agent rdf:about="{field[@name='url']}">
        <foaf:name><xsl:value-of select="field[@name='instname']"/></foaf:name>
        <rdfs:label><xsl:value-of select="field[@name='longname']"/></rdfs:label>
        </dcterms:Agent>
    </dcterms:mediator>
</xsl:template>

<xsl:template match="resultset[@table='domain']/row/field[@name='url']">
    <void:inDataset rdf:resource="{.}"/>
</xsl:template>

<xsl:template match="resultset[@table='opus']/row">
  <xsl:param name="uri"/>
  <!-- 1 TITLE -->
  <xsl:apply-templates select="field[@name='title']" />
  <xsl:apply-templates select="field[@name='title_en']" />
  <xsl:apply-templates select="field[@name='title_de']" />
  <!-- 3 AUTHOR -->
  <xsl:if test="field[@name='creator_name']!='' and count(../../resultset[@table='opus_autor']/row)=1">
  <dcterms:creator>
    <xsl:apply-templates select="field[@name='creator_name']" />
  </dcterms:creator>
  </xsl:if>
  <!-- 4 TOPIC DDC -->
  <xsl:apply-templates select="field[@name='sachgruppe_ddc']" />
  <!-- 4 TOPIC SWD -->
  <xsl:apply-templates select="field[@name='subject_swd']" />
  <!-- 4 TOPIC noScheme -->
  <xsl:apply-templates select="field[@name='subject_uncontrolled_german']" />
  <xsl:apply-templates select="field[@name='subject_uncontrolled_english']" />
  <!-- 4 TOPIC Klassifikationen -->
  <xsl:apply-templates select="../../resultset[@table='opus_ccs']"/>
  <xsl:apply-templates select="../../resultset[@table='opus_pacs']"/>
  <xsl:apply-templates select="../../resultset[@table='opus_msc']"/>
  <!-- 6 DESCRIPTION -->
  <xsl:apply-templates select="field[@name='description']" />
  <xsl:apply-templates select="field[@name='description2']" />
  <!-- 7 PUBLISHER -->
  <xsl:apply-templates select="field[@name='publisher_university']" />
  <xsl:apply-templates select="field[@name='publisher_faculty']" />
  <xsl:apply-templates select="field[@name='inst_name']" />
  <!-- 8 CONTRIBUTOR -->
  <xsl:apply-templates select="field[@name='advisor']"/>
  <xsl:apply-templates select="field[@name='contributors_name']"/>
  <xsl:apply-templates select="field[@name='contributors_corporate']"/>
  <!-- 9 CREATOR -->
  <xsl:apply-templates select="field[@name='creator_corporate']"/>

  <!-- 11 DATE ACCEPTED : Datum der Promotion -->
  <xsl:apply-templates select="field[@name='date_accepted_esc']"/>
  <!-- 12 DATE CREATED : Datum der Erstveroeffentlichung -->
  <xsl:apply-templates select="field[@name='date_creation_esc']"/>
  <!-- 12 DATE CREATED : Änderungsdatum des Dokuments -->
  <xsl:apply-templates select="field[@name='date_modified_esc']"/>
  <!-- DATE YEAR -->
  <xsl:apply-templates select="field[@name='date_year']"/>
  <!-- 14 TYPE -->
  <xsl:apply-templates select="field[@name='type']"/>
  <!-- <xsl:apply-templates select="field[@name='dini_publtype']"/> -->
  <!-- 18 MEDIUM -->
  <!-- <xsl:apply-templates select="field[@name='medium']"/> -->
  <!-- 20 SOURCE -->
  <xsl:apply-templates select="field[@name='isbn']"/>
  <!-- 21 LANGUAGE -->
  <xsl:apply-templates select="field[@name='language']"/>
  <!-- 29 PART OF -->
  <xsl:apply-templates select="field[@name='source_title']"/>
  <!-- 29 isPartOf -->
  <xsl:apply-templates select="../../resultset[@table='opus_schriftenreihe']"/>
  <!-- 40 RIGHTS -->
  <xsl:apply-templates select="field[@name='license']" />
  <xsl:apply-templates select="field[@name='accessRights']" />
  <!-- subtitles Kirche und Welt -->
  <xsl:apply-templates select="field[@name='subtitle']"/>
  <xsl:apply-templates select="../../resultset[@table='statistics']" />
  <xsl:apply-templates select="../../resultset[@table='files']">
      <xsl:with-param name="uri" select="$uri"/>
  </xsl:apply-templates>
  <!-- Opus identifier : only index records with files attached ?? -->
  <xsl:if test="count(../../resultset[@table='statistics'])=0">
      <xsl:apply-templates select="field[@name='source_opus']"/>
  </xsl:if>
  <xsl:apply-templates select="../../resultset[@table='opus_toc']" />
  <!-- see OpusTransporter
  <xsl:apply-templates select="../../resultset[@table='dct_references']" />
  -->
</xsl:template>

<!-- Opus identifier resultset[@table='statistics']/row/ -->
<xsl:template match="field[@name='source_opus']">
<sco:serialNumber><xsl:value-of select="concat('opus:',.)"/></sco:serialNumber>
</xsl:template>

<!-- 1. TITLE -->
<xsl:template match="field[@name='title_de'][text()!='']">
  <xsl:if test="../field[@name='language']!='ger'">
  <dcterms:title xml:lang="de">
    <xsl:value-of select="normalize-space(.)"/>
  </dcterms:title>
 </xsl:if>
</xsl:template>

<xsl:template match="field[@name='title_en'][text()!='']">
  <xsl:if test="../field[@name='language']!='eng'">
  <dcterms:title xml:lang="en">
    <xsl:value-of select="normalize-space(.)"/>
  </dcterms:title>
 </xsl:if>
</xsl:template>

<xsl:template match="field[@name='title'][text()!='']">
  <xsl:variable name="lang">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="../field[@name='language']"/>
   </xsl:call-template>
  </xsl:variable>
  <dcterms:title xml:lang="{$lang}">
      <xsl:value-of select="normalize-space(.)"/>
  </dcterms:title>
</xsl:template>

<!-- not used by opus
<xsl:template match="field[@name='subtitle']">
  <dcterms:alternative>
    <xsl:value-of select="normalize-space(.)"/>
  </dcterms:alternative>
</xsl:template>
-->

<!-- 3. AUTHOR -->
<xsl:template match="field[@name='creator_name']">
 <xsl:variable name="gnd" select="../field[@name='gnd']"/>
 <xsl:variable name="orcid" select="../field[@name='orcid']"/>
 <xsl:variable name="aut">
       <xsl:call-template name="string-generic"/>
 </xsl:variable>
 <xsl:variable name="auid">
  <xsl:choose>
   <xsl:when test="$gnd!=''">
       <xsl:value-of select="concat('http://d-nb.info/gnd/',$gnd)"/>
   </xsl:when>
   <xsl:when test="$orcid!=''">
       <xsl:value-of select="concat('http://orcid.org/',$orcid)"/>
   </xsl:when>
   <xsl:otherwise>
   <xsl:value-of select="concat($graph, '/aut/',$aut)"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:variable>

  <foaf:Person rdf:about="{$auid}">
      <xsl:call-template name="creator-names">
        <xsl:with-param name="text"><xsl:value-of select="."/></xsl:with-param>
      </xsl:call-template>
  </foaf:Person>
</xsl:template>

<xsl:template name="string-generic">
 <xsl:param name="text" select="."/>
 <xsl:value-of select="translate($text,
  translate($text, 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzüäö', ''),
  '')"/>
</xsl:template>

<xsl:template name="creator-names">
 <xsl:param name="text"/>
 <xsl:choose>
  <xsl:when test="contains($text,'(add)')">
      <xsl:element name="foaf:name">
          <xsl:value-of select="substring-before($text,'(add)')"/>
      </xsl:element>
      <xsl:element name="foaf:role"><!-- author_additional -->
          <xsl:value-of select="'add'"/>
      </xsl:element>
  </xsl:when>
  <xsl:when test="contains($text,' ; ')">
     <foaf:name><xsl:value-of select="normalize-space($text)"/></foaf:name>
  </xsl:when>
  <xsl:when test="contains($text,', ')">
      <foaf:givenName>
       <xsl:value-of select="normalize-space(substring-after($text,','))"/>
      </foaf:givenName>
      <foaf:familyName>
        <xsl:value-of select="normalize-space(substring-before($text,','))"/>
      </foaf:familyName>
      <foaf:name><xsl:value-of select="normalize-space($text)"/></foaf:name>
      <!-- <xsl:comment>
           <xsl:value-of select="../field[@name='reihenfolge']"/>
      </xsl:comment>
      -->
  </xsl:when>
  <xsl:otherwise>
     <foaf:name><xsl:value-of select="normalize-space($text)"/></foaf:name>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<!-- 4. DDC TOPIC -->
<xsl:template match="field[@name='sachgruppe_ddc']">
 <dcterms:subject>
  <skos:Concept rdf:about="http://dewey.info/class/{.}">
   <skos:prefLabel xml:lang="de">
     <xsl:value-of select="normalize-space(../field[@name='sachgruppe'])"/>
   </skos:prefLabel>
   <skos:prefLabel xml:lang="en">
     <xsl:value-of select="normalize-space(../field[@name='sachgruppe_en'])"/>
   </skos:prefLabel>
  </skos:Concept>
 </dcterms:subject>
</xsl:template>

<!-- 4. SWD TOPIC 2016 -->
<xsl:template match="field[@name='subject_swd'][text()!='']">
  <xsl:call-template name="swd2skos">
     <xsl:with-param name="text"><xsl:value-of select="."/></xsl:with-param>
  </xsl:call-template>
</xsl:template>

<!-- 4. TOPIC UNCONTROLLED 2016 -->
<xsl:template match="field[@name='subject_uncontrolled_german'][text()!='']">
  <xsl:call-template name="unc2skos">
     <xsl:with-param name="text"><xsl:value-of select="."/></xsl:with-param>
     <xsl:with-param name="lang"><xsl:value-of select="'de'"/></xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template match="field[@name='subject_uncontrolled_english'][text()!='']">
  <xsl:call-template name="unc2skos">
     <xsl:with-param name="text"><xsl:value-of select="."/></xsl:with-param>
     <xsl:with-param name="lang"><xsl:value-of select="'en'"/></xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template match="resultset[@table='opus_msc']">
  <xsl:apply-templates select="row"/>
</xsl:template>

<xsl:template match="resultset[@table='opus_msc']/row">
  <dcterms:subject>
   <skos:Concept rdf:about="http://www.iwi-iuk.org/material/RDF/Schema/Class/Extern/msc2000.rdf#{substring(field[@name='class'],0,5)}">
    <skos:altLabel>
     <xsl:value-of select="field[@name='class']"/></skos:altLabel>
    <skos:prefLabel>
     <xsl:value-of select="field[@name='bez']"/></skos:prefLabel>
   </skos:Concept>
  </dcterms:subject>
</xsl:template>

<xsl:template match="resultset[@table='opus_ccs']">
  <xsl:apply-templates select="row"/>
</xsl:template>

<xsl:template match="resultset[@table='opus_ccs']/row">
  <dcterms:subject>
   <skos:Concept rdf:about="{concat('http://www.acm.org/about/class/1998/', normalize-space(substring(field[@name='class'],0,5)))}">
    <skos:altLabel>
     <xsl:value-of select="field[@name='class']"/></skos:altLabel>
    <skos:prefLabel>
     <xsl:value-of select="field[@name='bez']"/></skos:prefLabel>
   </skos:Concept>
  </dcterms:subject>
</xsl:template>

<xsl:template match="resultset[@table='opus_pacs']">
  <xsl:apply-templates select="row"/>
</xsl:template>

<xsl:template match="resultset[@table='opus_pacs']/row">
  <dcterms:subject>
   <skos:Concept rdf:about="http://publish.aps.org/PACS/{substring(field[@name='class'],0,5)}">
    <skos:altLabel>
     <xsl:value-of select="field[@name='class']"/></skos:altLabel>
    <skos:prefLabel>
     <xsl:value-of select="field[@name='bez']"/></skos:prefLabel>
   </skos:Concept>
  </dcterms:subject>
</xsl:template>

<!-- 6 DESCRIPTION ABSTRACT -->
<xsl:template match="resultset[@table='opus']/row/field[@name='description']">
 <xsl:if test="normalize-space(.)!=''">
  <xsl:variable name="lang">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="../field[@name='language']"/>
   </xsl:call-template>
  </xsl:variable>
  <dcterms:abstract>
    <xsl:if test="$lang!=''">
     <xsl:attribute name="xml:lang">
       <xsl:value-of select="$lang"/>
     </xsl:attribute>
    </xsl:if>
    <xsl:value-of select="."/>
  </dcterms:abstract>
 </xsl:if>
</xsl:template>

<xsl:template match="field[@name='description2']">
 <xsl:if test="normalize-space(.)!=''">
  <xsl:variable name="lang">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="../field[@name='description2_lang']"/>
   </xsl:call-template>
  </xsl:variable>
  <dcterms:abstract>
    <xsl:if test="$lang!=''">
     <xsl:attribute name="xml:lang">
       <xsl:value-of select="$lang"/>
     </xsl:attribute>
    </xsl:if>
    <xsl:value-of select="."/>
  </dcterms:abstract>
 </xsl:if>
</xsl:template>

<!-- 7 PUBLISHER -->
<xsl:template match="field[@name='publisher_university']">
 <dcterms:publisher>
 <xsl:choose>
     <xsl:when test="../../../resultset[@table='domain']/row/field[@name='uni_gnd']">
     <foaf:Organization rdf:about="{../../../resultset[@table='domain']/row/field[@name='uni_gnd']}">
         <foaf:name><xsl:value-of select="normalize-space(.)"/></foaf:name>
         </foaf:Organization>
     </xsl:when>
     <xsl:otherwise>
         <foaf:Organization>
             <foaf:name><xsl:value-of select="."/></foaf:name>
         </foaf:Organization>
     </xsl:otherwise>
 </xsl:choose>
 </dcterms:publisher>
</xsl:template>

<!-- FAKULTAET -->
<xsl:template match="field[@name='publisher_faculty']">
 <xsl:if test="normalize-space(../field[@name='faculty_name'])!=''">
 <dcterms:provenance>
   <aiiso:Faculty rdf:about="http://www.example.org/fb{.}">
     <foaf:name>
       <xsl:value-of select="normalize-space(../field[@name='faculty_name'])"/>
     </foaf:name>
   </aiiso:Faculty>
 </dcterms:provenance>
 </xsl:if>
</xsl:template>

<xsl:template match="field[@name='inst_name']">
 <xsl:variable name="iid">
  <xsl:call-template name="string-generic"/>
 </xsl:variable>
 <xsl:if test="normalize-space(.)!=''">
  <dcterms:contributor>
   <xsl:choose>
    <xsl:when test="contains(.,'Universitätsbibliothek')">
     <aiiso:Division rdf:about="http://d-nb.info/gnd/11210-0">
      <foaf:name><xsl:value-of select="'Universitätsbibliothek'" /></foaf:name>
     </aiiso:Division>
    </xsl:when>
    <xsl:when test="starts-with(.,'Center')">
     <aiiso:Center rdf:about="{concat($graph, '/aut/', $iid)}">
      <foaf:name><xsl:value-of select="normalize-space(.)" /></foaf:name>
     </aiiso:Center>
    </xsl:when>
    <xsl:otherwise>
     <aiiso:Institute rdf:about="{concat($graph, '/aut/', $iid)}">
      <foaf:name><xsl:value-of select="normalize-space(.)" /></foaf:name>
     </aiiso:Institute>
    </xsl:otherwise>
   </xsl:choose>
  </dcterms:contributor>
 </xsl:if>
</xsl:template>

<!-- 8 CONTRIBUTOR -->
<xsl:template match="field[@name='advisor']">
 <xsl:if test="normalize-space(.)!=''">
  <xsl:variable name="aut">
      <xsl:call-template name="string-generic"/>
  </xsl:variable>
  <dcterms:contributor>
    <foaf:Person 
        rdf:about="{concat($graph, '/aut/', $aut)}">
        <foaf:name><xsl:value-of select="normalize-space(.)"/></foaf:name>
        <xsl:choose>
          <xsl:when test="contains(.,',')">
            <foaf:givenName><xsl:value-of select="normalize-space(substring-before(substring-after(.,','),'('))"/></foaf:givenName>
            <foaf:familyName><xsl:value-of select="normalize-space(substring-before(.,','))"/></foaf:familyName>
            <foaf:title><xsl:value-of select="normalize-space(substring-before(substring-after(.,'('),')'))"/></foaf:title>
          </xsl:when>
        </xsl:choose>
        <!--<pro:withRole rdf:resource="http://purl.org/spar/pro/reviewer"/>-->
        <foaf:role><xsl:value-of select="'ths'"/></foaf:role>
    </foaf:Person>
  </dcterms:contributor>
 </xsl:if>
</xsl:template>

<!-- may contain semicolon as delimiter -->
<xsl:template match="field[@name='contributors_name']">
 <xsl:if test="normalize-space(.)!=''">
  <dcterms:contributor>
   <rdf:Seq>
    <xsl:call-template name="tokenize">
     <xsl:with-param name="text" select="normalize-space(.)"/>
    </xsl:call-template>
   </rdf:Seq>
  </dcterms:contributor>
 </xsl:if>
</xsl:template>

<xsl:template match="field[@name='contributors_corporate']">
 <xsl:if test="normalize-space(.)!=''">
  <xsl:variable name="aut">
      <xsl:call-template name="string-generic"/>
  </xsl:variable>
  <dcterms:contributor>
    <foaf:Organization rdf:about="{concat($graph, '/aut/', $aut)}">
        <foaf:name><xsl:value-of select="normalize-space(.)"/></foaf:name>
    </foaf:Organization>
  </dcterms:contributor>
 </xsl:if>
</xsl:template>

<xsl:template match="field[@name='creator_corporate']">
 <xsl:if test="normalize-space(.)!=''">
  <xsl:variable name="aut">
      <xsl:call-template name="string-generic"/>
  </xsl:variable>
  <dcterms:creator>
    <foaf:Organization rdf:about="{concat($graph, '/aut/', $aut)}">
        <foaf:name><xsl:value-of select="normalize-space(.)"/></foaf:name>
    </foaf:Organization> 
  </dcterms:creator>
 </xsl:if>
</xsl:template>

<!-- DINI Publication types : elsewhere -->
<xsl:template match="field[@name='dini_publtype']">
</xsl:template>

<xsl:template match="field[@name='type']">
<xsl:if test=".!=''">
  <dcterms:type rdf:resource="{concat('http://purl.org/spar/fabio/',normalize-space(.))}"/>
</xsl:if>
</xsl:template>

<!-- DATE YEAR : Erstellungsdatum -->
<xsl:template match="field[@name='date_year']">
<xsl:if test=".!=''">
  <dcterms:created><xsl:value-of select="."/></dcterms:created>
</xsl:if>
</xsl:template>

<!-- 11 DATE ACCEPTED : Datum der Promotion -->
<xsl:template match="field[@name='date_accepted_esc']">
<xsl:if test=".!=''">
  <dcterms:dateAccepted><xsl:value-of select="."/></dcterms:dateAccepted>
</xsl:if>
</xsl:template>

<!-- 12 DATE ISSUED : Datum der Erstveroeffentlichung -->
<xsl:template match="field[@name='date_creation_esc']">
<xsl:if test=".!=''">
  <dcterms:issued><xsl:value-of select="."/></dcterms:issued>
</xsl:if>
</xsl:template>

<!-- 13 DATE MODIFIED : Änderungsdatum des Dokuments -->
<xsl:template match="field[@name='date_modified_esc']">
<xsl:if test=".!=''">
   <dcterms:modified><xsl:value-of select="."/></dcterms:modified>
</xsl:if>
</xsl:template>

<!-- 20 SOURCE -->
<xsl:template match="field[@name='isbn']">
  <xsl:choose>
  <xsl:when test="normalize-space(.)=''"></xsl:when>
  <xsl:when test="../../../resultset[@table='opus_schriftenreihe']/row">
  </xsl:when>
  <xsl:when test="string-length(.)=9 and substring(.,5,1)='-'">
    <sco:issn><xsl:value-of select="."/></sco:issn>
  </xsl:when>
  <xsl:otherwise>
    <sco:isbn><xsl:value-of select="."/></sco:isbn>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- 21 LANGUAGE -->
<xsl:template match="field[@name='language'][text()!='']">
  <xsl:variable name="lang">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="."/>
   </xsl:call-template>
  </xsl:variable>
  <xsl:choose>
      <xsl:when test="$lang!=''">
        <dcterms:language 
         rdf:resource="{concat('http://id.loc.gov/vocabulary/iso639-1/',$lang)}"/>
      </xsl:when>
      <xsl:when test="$lang!=''"><!-- Old -->
          <dcterms:language><xsl:value-of select="$lang"/></dcterms:language>
      </xsl:when>
      <xsl:otherwise>
          <dcterms:language><xsl:value-of select="."/></dcterms:language>
      </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- 29 PART OF : introduced URIs, blank nodes failed -->
<xsl:template match="field[@name='source_title']">
 <xsl:if test="normalize-space(.)!=''">
  <dcterms:source><xsl:value-of select="normalize-space(.)"/></dcterms:source>
 </xsl:if>
</xsl:template>

<!-- 40 RIGHTS -->
<xsl:template match="field[@name='license']">
  <dcterms:license rdf:resource="{normalize-space(.)}"/>
</xsl:template>

<xsl:template match="field[@name='accessRights']">
 <xsl:choose> 
 <xsl:when test="starts-with(.,'/srv/archiv/')"></xsl:when>
 <xsl:when test="normalize-space(.)!=''">
  <dcterms:accessRights><xsl:value-of select="normalize-space(.)"/>
  </dcterms:accessRights>
 </xsl:when>
 </xsl:choose>
</xsl:template>

<!-- 16 IDENTIFIER -->
<xsl:template match="resultset[@table='statistics']">
  <xsl:apply-templates select="row/field[@name='source_opus']"/>
  <xsl:apply-templates select="row/field[@name='doi']"/>
  <xsl:apply-templates select="row/field[@name='urn']"/>
  <xsl:apply-templates select="row/field[@name='ppn']"/>
  <!-- <xsl:apply-templates select="row/field[@name='stat']"/> -->
</xsl:template>

<xsl:template match="row/field[@name='doi'][text()!='']">
  <dcterms:identifier>
    <xsl:value-of select="concat('https://doi.org/',.)"/>
  </dcterms:identifier>
</xsl:template>

<xsl:template match="resultset[@table='statistics']/row/field[@name='urn']">
  <dcterms:identifier><xsl:value-of select="."/></dcterms:identifier>
</xsl:template>

<xsl:template match="resultset[@table='statistics']/row/field[@name='ppn'][text()!='']">
<sco:orderNumber><xsl:value-of select="concat('ppn:',.)"/></sco:orderNumber>
</xsl:template>

<xsl:template match="resultset[@table='files']">
  <xsl:param name="uri"/>
  <!--
       select="normalize-space(../resultset[@table='statistics']/row/field[@name='uri'])"/>
  -->
  <xsl:apply-templates select="row">
      <xsl:with-param name="uri" select="$uri"/>
  </xsl:apply-templates>
  <!-- default cover if no png file -->
  <xsl:if test="count(row/field[@name='file'][contains(text(),'.png')])=0">
    <foaf:img><xsl:value-of select="concat($uri,'/cover.png')"/></foaf:img>
  </xsl:if>
</xsl:template>

<xsl:template match="resultset[@table='files']/row">
  <xsl:param name="uri"/>
  <xsl:apply-templates select="field[@name='file']">
     <xsl:with-param name="uri" select="normalize-space($uri)"/>
  </xsl:apply-templates>
  <xsl:apply-templates select="field[@name='extent']">
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="resultset[@table='files']/row/field[@name='file']">
  <xsl:param name="uri"/>
  <xsl:choose>
   <xsl:when test="contains(.,'mets-')"><!-- viewer -->
     <dcterms:hasPart><dctypes:Text rdf:about="{concat($uri,'/',.)}">
         <dcterms:format><dcterms:MediaTypeOrExtent>
         <rdfs:label><xsl:value-of select="'application/xml'"/></rdfs:label>
         </dcterms:MediaTypeOrExtent></dcterms:format>
     </dctypes:Text></dcterms:hasPart>
   </xsl:when>
   <xsl:when test="contains(.,'container.zip')">
     <dcterms:hasPart><dctypes:Collection rdf:about="{concat($uri,'/',.)}">
         <dcterms:format><dcterms:MediaTypeOrExtent>
         <rdfs:label><xsl:value-of select="'application/zip'"/></rdfs:label>
         </dcterms:MediaTypeOrExtent></dcterms:format>
     </dctypes:Collection></dcterms:hasPart>
   </xsl:when>
   <xsl:when test="contains(.,'.zip')">
     <dcterms:hasPart><dctypes:Dataset rdf:about="{concat($uri,'/',.)}">
         <dcterms:format><dcterms:MediaTypeOrExtent>
         <rdfs:label><xsl:value-of select="'application/zip'"/></rdfs:label>
         </dcterms:MediaTypeOrExtent></dcterms:format>
     </dctypes:Dataset></dcterms:hasPart>
   </xsl:when>
   <xsl:when test="contains(.,'.pdf')">
     <dcterms:hasPart><dctypes:Text rdf:about="{concat($uri,'/',.)}">
         <dcterms:format><dcterms:MediaTypeOrExtent>
         <rdfs:label><xsl:value-of select="'application/pdf'"/></rdfs:label>
         </dcterms:MediaTypeOrExtent></dcterms:format>
     </dctypes:Text></dcterms:hasPart>
   </xsl:when>
   <xsl:when test="contains(.,'html')">
     <dcterms:hasPart><dctypes:Text rdf:about="{concat($uri,'/',.)}">
         <dcterms:format><dcterms:MediaTypeOrExtent>
         <rdfs:label><xsl:value-of select="'text/html'"/></rdfs:label>
         </dcterms:MediaTypeOrExtent></dcterms:format>
     </dctypes:Text></dcterms:hasPart>
   </xsl:when>
   <xsl:when test="contains(.,'.jpg')">
     <dcterms:hasPart><dctypes:Image rdf:about="{concat($uri,'/',.)}">
         <dcterms:format><dcterms:MediaTypeOrExtent>
         <rdfs:label><xsl:value-of select="'image/jpeg'"/></rdfs:label>
         </dcterms:MediaTypeOrExtent></dcterms:format>
     </dctypes:Image></dcterms:hasPart>
   </xsl:when>
   <xsl:when test="contains(.,'.mp4')">
     <dcterms:hasPart><dctypes:MovingImage rdf:about="{concat($uri,'/',.)}">
         <dcterms:format><dcterms:MediaTypeOrExtent>
         <rdfs:label><xsl:value-of select="'video/mp4'"/></rdfs:label>
         </dcterms:MediaTypeOrExtent></dcterms:format>
     </dctypes:MovingImage></dcterms:hasPart>
   </xsl:when>
   <xsl:when test="contains(.,'.png')">
     <foaf:img><xsl:value-of select="concat($uri,'/',.)"/></foaf:img>
     <dcterms:hasPart><dctypes:Image rdf:about="{concat($uri,'/',.)}">
         <dcterms:format><dcterms:MediaTypeOrExtent>
         <rdfs:label><xsl:value-of select="'image/png'"/></rdfs:label>
         </dcterms:MediaTypeOrExtent></dcterms:format>
     </dctypes:Image></dcterms:hasPart>
   </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template match="resultset[@table='files']/row/field[@name='extent'][text()!='']">
  <xsl:param name="uri" select="normalize-space(../../../resultset[@table='statistics']/row/field[@name='uri'])"/>
  <dcterms:extent>
    <dcterms:SizeOrDuration rdf:about="{concat($uri, '/Extent')}">
      <rdf:value><xsl:value-of select="concat(.,' pages.')"/></rdf:value>
    </dcterms:SizeOrDuration>
  </dcterms:extent>
</xsl:template>

<xsl:template match="resultset[@table='opus_schriftenreihe']">
  <xsl:apply-templates select="row" />
</xsl:template>

<!-- 28 Hochschulschrift ist Teil von URL -->
<xsl:template match="resultset[@table='opus_schriftenreihe']/row">
 <dcterms:isPartOf>
  <xsl:choose>
   <xsl:when test="../../../resultset/row/field[@name='type']='BookChapter'">
    <dcterms:BibliographicResource rdf:about="{normalize-space(field[@name='url'])}">
      <dcterms:title>
         <xsl:value-of select="normalize-space(field[@name='name'])"/>
      </dcterms:title>
      <dcterms:identifier>
        <xsl:value-of select="normalize-space(field[@name='urn'])"/>
       </dcterms:identifier>
      <dcterms:type rdf:resource="http://purl.org/spar/fabio/Book"/>
    </dcterms:BibliographicResource> 
   </xsl:when>
   <xsl:otherwise>
    <dcterms:BibliographicResource rdf:about="{normalize-space(field[@name='url'])}">
      <dcterms:title>
          <xsl:value-of select="normalize-space(field[@name='name'])"/>
      </dcterms:title>
      <dcterms:identifier>
      <xsl:value-of select="normalize-space(field[@name='urn'])"/>
      </dcterms:identifier>
        <dcterms:type rdf:resource="http://purl.org/spar/fabio/Periodical"/>
      <xsl:choose>
      <xsl:when test="string-length(../../../resultset/row/field[@name='isbn'])=9">
       <sco:issn>
         <xsl:value-of select="../../../resultset/row/field[@name='isbn']"/>
       </sco:issn>
      </xsl:when>
     </xsl:choose>
    </dcterms:BibliographicResource> 
   </xsl:otherwise>
  </xsl:choose>
 </dcterms:isPartOf>
 
 <xsl:apply-templates select="field[@name='sequence_nr']"/>
</xsl:template>

<xsl:template match="isPartOf">
  <dcterms:isPartOf>
   <xsl:choose>
    <xsl:when test="../resultset/row/field[@name='type']='PeriodicalItem'">
    <dcterms:BibliographicResource rdf:about="{field[@name='url']}">
     <dcterms:title>
         <xsl:value-of select="normalize-space(field[@name='title'])"/>
     </dcterms:title>
     <dcterms:identifier>
         <xsl:value-of select="normalize-space(field[@name='urn'])"/>
     </dcterms:identifier>
     <dcterms:type rdf:resource="http://purl.org/spar/fabio/PeriodicalIssue"/>
   </dcterms:BibliographicResource>
   </xsl:when>
   <xsl:otherwise>
    <dcterms:BibliographicResource rdf:about="{field[@name='url']}">
     <dcterms:title>
         <xsl:value-of select="normalize-space(field[@name='title'])"/>
     </dcterms:title>
     <dcterms:identifier>
         <xsl:value-of select="normalize-space(field[@name='urn'])"/>
     </dcterms:identifier>
     <dcterms:type rdf:resource="http://purl.org/spar/fabio/Periodical"/>
   </dcterms:BibliographicResource>
   </xsl:otherwise>
   </xsl:choose>
  </dcterms:isPartOf>
</xsl:template>

<xsl:template match="document/resultset[@table='opus_autor']">
  <xsl:choose>
  <xsl:when test="count(row)=0"></xsl:when>
  <xsl:when test="count(row)>1">
  <dcterms:creator>
   <rdf:Seq rdf:about="{concat(normalize-space(../resultset[@table='statistics']/row/field[@name='uri']),'/Authors')}">
     <xsl:apply-templates select="row"/>
   </rdf:Seq>
  </dcterms:creator>
  </xsl:when>
  <xsl:otherwise>
  <dcterms:creator>
     <xsl:apply-templates select="row/field[@name='creator_name']"/>
  </dcterms:creator>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="document/resultset[@table='opus_autor']/row">
  <rdf:li>
  <xsl:apply-templates select="field[@name='creator_name']"/>
  </rdf:li>
</xsl:template>

<xsl:template match="field[@name='sequence_nr'][text()!='']">
 <sco:volumeNumber><xsl:value-of select="."/></sco:volumeNumber>
</xsl:template>

<xsl:template match="resultset[@table='opus_toc']">
  <dcterms:tableOfContents>
    <rdf:Seq>
      <xsl:apply-templates select="row"/>
    </rdf:Seq>
  </dcterms:tableOfContents>
</xsl:template>

<xsl:template match="resultset[@table='opus_toc']/row">
 <rdf:li>
   <xsl:value-of select="concat(field[@name='label'],' ',field[@name='page'])"/>
 </rdf:li>
</xsl:template>

<!-- https://www.loc.gov/standards/iso639-2/php/code_list.php -->
<xsl:template name="getlang">
 <xsl:param name="input"/>
  <xsl:choose>
   <xsl:when test="$input='ger'">de</xsl:when>
   <xsl:when test="$input='eng'">en</xsl:when>
   <xsl:when test="$input='fre'">fr</xsl:when>
   <xsl:when test="$input='jpn'">ja</xsl:when>
   <xsl:when test="$input='lat'">la</xsl:when>
   <xsl:when test="$input='spa'">es</xsl:when>
   <xsl:when test="$input='ita'">it</xsl:when>
   <xsl:when test="$input='nld'">nl</xsl:when>
   <xsl:when test="$input='rus'">ru</xsl:when>
   <xsl:when test="$input='paa'">na</xsl:when>
   <xsl:when test="$input='mul'">bi</xsl:when><!-- multiple / bi -->
   <xsl:when test="$input='ara'">ar</xsl:when>
   <xsl:when test="$input='grc'">el</xsl:when><!-- greek -->
   <xsl:when test="$input='heb'">he</xsl:when><!-- hebrew -->
   <xsl:when test="$input='und'">zu</xsl:when><!-- undetermined -->
   <xsl:otherwise><xsl:value-of select="normalize-space($input)"/>
   </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template name="tokenize">
    <xsl:param name="text"/>
    <xsl:param name="delimiter" select="' ; '"/>
    <xsl:choose>
      <xsl:when test="contains($text,$delimiter)">
        <xsl:call-template name="person">
            <xsl:with-param name="text" 
                 select="substring-before($text,$delimiter)"/>
        </xsl:call-template>
        <xsl:call-template name="tokenize">
          <xsl:with-param name="text" select="substring-after($text,$delimiter)"/>
          <xsl:with-param name="delimiter" select="$delimiter"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$text">
        <xsl:call-template name="person">
            <xsl:with-param name="text" select="$text"/>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
</xsl:template>

<xsl:template name="person">
    <xsl:param name="text"/>
    <xsl:variable name="aut">
        <xsl:call-template name="string-generic">
            <xsl:with-param name="text" select="$text"/>
        </xsl:call-template>
    </xsl:variable>
    <xsl:element name="rdf:li">
      <xsl:element name="foaf:Person">
        <xsl:attribute name="rdf:about">
            <xsl:value-of select="concat($graph, '/aut/',$aut)"/>
        </xsl:attribute>
        <xsl:choose>
        <xsl:when test="contains($text,'(Hrsg')">
            <xsl:element name="foaf:name">
              <xsl:value-of select="normalize-space(substring-before($text,'(Hrsg'))"/>
            </xsl:element>
            <xsl:element name="foaf:role">
              <xsl:value-of select="'edt'"/>
            </xsl:element>
        </xsl:when>
        <xsl:when test="contains($text,'(Übers')">
            <xsl:element name="foaf:name">
              <xsl:value-of select="normalize-space(substring-before($text,'(Übers'))"/>
            </xsl:element>
            <xsl:element name="foaf:role">
              <xsl:value-of select="'trl'"/>
            </xsl:element>
        </xsl:when>
        <xsl:when test="contains($text,'[')">
            <xsl:element name="foaf:name">
              <xsl:value-of select="normalize-space(substring-before($text,'['))"/>
            </xsl:element>
            <xsl:element name="foaf:role">
              <xsl:value-of select="substring-after(substring-before($text,']'),'[')"/>
            </xsl:element>
        </xsl:when>
        <xsl:otherwise>
            <xsl:element name="foaf:name">
              <xsl:value-of select="$text"/>
            </xsl:element>
        </xsl:otherwise>
        </xsl:choose>
        </xsl:element>
    </xsl:element>
</xsl:template>

<!-- <xsl:comment><xsl:value-of select="$text"/></xsl:comment> -->
<xsl:template name="swd2skos">
  <xsl:param name="text"/>
  <xsl:param name="sep" select="', '"/>
  <xsl:choose>
    <xsl:when test="contains($text,$sep)">
      <xsl:call-template name="swd2skos">
        <xsl:with-param name="text" select="substring-before($text,$sep)"/>
      </xsl:call-template>
      <xsl:call-template name="swd2skos">
        <xsl:with-param name="text" select="substring-after($text,$sep)"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
    <!-- SWD tentatively : http://d-nb.info/gnd/4331361-9 -->
    <xsl:variable name="cls">
    <xsl:call-template name="string-generic">
        <xsl:with-param name="text" select="$text"/>
    </xsl:call-template>
    </xsl:variable>
    <dcterms:subject>
     <skos:Concept rdf:about="{concat('http://example.org/swd/',$cls)}">
      <rdfs:label><xsl:value-of select="normalize-space($text)"/></rdfs:label>
     </skos:Concept>
    </dcterms:subject>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="unc2skos">
  <xsl:param name="text"/>
  <xsl:param name="lang"/>
  <xsl:param name="sep" select="', '"/>
  <xsl:choose>
    <xsl:when test="contains($text,$sep)">
      <xsl:call-template name="unc2skos">
        <xsl:with-param name="text" select="substring-before($text,$sep)"/>
        <xsl:with-param name="lang" select="$lang"/>
      </xsl:call-template>
      <xsl:call-template name="unc2skos">
        <xsl:with-param name="text" select="substring-after($text,$sep)"/>
        <xsl:with-param name="lang" select="$lang"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
    <dcterms:subject>
     <skos:Concept>
      <rdfs:label xml:lang="{$lang}"><xsl:value-of select="normalize-space($text)"/></rdfs:label>
     </skos:Concept>
    </dcterms:subject>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="text()"/>
<xsl:template mode="collection" match="text()"/>
</xsl:stylesheet>

