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

     xmlns:xMetaDiss="http://www.d-nb.de/standards/xmetadissplus/"
     xmlns:cc="http://www.d-nb.de/standards/cc/"
     xmlns:thesis="http://www.ndltd.org/standards/metadata/etdms/1.0/"
     xmlns:ddb="http://www.d-nb.de/standards/ddb/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:pc="http://www.d-nb.de/standards/pc/"

     version="1.0" >

<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />

<xsl:template match="rdf:RDF">
  <xMetaDiss:xMetaDiss
     xmlns="http://www.d-nb.de/standards/xmetadissplus/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:cc="http://www.d-nb.de/standards/cc/"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:pc="http://www.d-nb.de/standards/pc/"
     xmlns:urn="http://www.d-nb.de/standards/urn/"
     xmlns:thesis="http://www.ndltd.org/standards/metadata/etdms/1.0/"
     xmlns:ddb="http://www.d-nb.de/standards/ddb/"
     xmlns:dini="http://www.d-nb.de/standards/xmetadissplus/type/"
     xsi:schemaLocation="http://www.d-nb.de/standards/xmetadissplus/ http://files.dnb.de/standards/xmetadissplus/xmetadissplus.xsd">

    <xsl:apply-templates select="fabio:*" />
  </xMetaDiss:xMetaDiss>
</xsl:template>

<xsl:template match="fabio:*">
  <xsl:comment> xMetaDissPlus Transformer UB Marburg 2016 </xsl:comment>
  <xsl:apply-templates select="dcterms:title[not(@xml:lang)]"/>
  <xsl:apply-templates select="dcterms:title[@xml:lang]"/>
  <xsl:apply-templates select="dcterms:creator"/>

  <!-- 4. subject --> 
  <xsl:apply-templates select="dcterms:subject"/>
  <!-- 5. tableOfContents -->
  <!-- 6. abstract -->
  <xsl:apply-templates select="dcterms:abstract"/>

  <!-- 7. publisher -->
  <xsl:apply-templates select="dcterms:publisher"/>
  <!-- 7. publisher journals -->
  <xsl:if test="count(dcterms:publisher)=0">
  <xsl:apply-templates select="dcterms:isPartOf/fabio:*/dcterms:publisher"/>
  </xsl:if>
  <xsl:apply-templates select="dcterms:contributor"/>

  <!-- 9. dates -->
  <xsl:apply-templates select="dcterms:created"/>
  <xsl:apply-templates select="dcterms:dateAccepted"/>
  <xsl:apply-templates select="dcterms:issued"/>
  <xsl:apply-templates select="dcterms:modified"/>

  <!-- <xsl:apply-templates select="." mode="type"/> -->
  <xsl:apply-templates select="dcterms:type"/>
  <xsl:apply-templates select="dcterms:identifier[starts-with(text(),'urn:')]"/>
  <!-- 21 Sprache -->
  <xsl:choose>
  <xsl:when test="dcterms:language">
    <xsl:apply-templates select="dcterms:language"/> 
  </xsl:when>
  <xsl:when test="dcterms:isPartOf/fabio:JournalIssue/dcterms:language">
    <xsl:apply-templates select="dcterms:isPartOf/fabio:JournalIssue/dcterms:language"/> 
  </xsl:when>
  </xsl:choose>

  <!-- 29 Hochschulschrift ist Teil von -->
  <xsl:apply-templates select="dcterms:isPartOf/fabio:JournalIssue/fabio:hasIdentifier"/>
  <xsl:apply-templates select="dcterms:isPartOf/fabio:JournalIssue/dcterms:title"/>
  <xsl:apply-templates select="dcterms:isPartOf/fabio:Periodical"/>
  <!-- 39 Recht -->
  <xsl:apply-templates select="dcterms:rights"/> 
  <!-- 41 Akademischer Grad -->
  <xsl:apply-templates select="." mode="degree"/>
  <!-- 46 Transfer -->
  <xsl:apply-templates select="dcterms:hasPart"/> 
  <!-- 47 Weitere Identifier-->
  <xsl:apply-templates select="dcterms:identifier[starts-with(text(),'http://dx.doi.org/')]"/>
  <!-- URL LICENSE -->
  <xsl:apply-templates select="." mode="about" />
</xsl:template>

<xsl:template match="dcterms:title[not(@xml:lang)]">
  <xsl:variable name="lang">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="../dcterms:language"/>
   </xsl:call-template>
  </xsl:variable>
  <dc:title xsi:type="ddb:titleISO639-2" lang="{$lang}">
     <xsl:value-of select="."/>
  </dc:title>
</xsl:template>

<xsl:template match="dcterms:title[@xml:lang]">
  <xsl:variable name="lang1">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="../dcterms:language"/>
   </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="lang2">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="@xml:lang"/>
   </xsl:call-template>
  </xsl:variable>
  <xsl:if test="$lang1!=$lang2">
  <dc:title xsi:type="ddb:titleISO639-2" lang="{$lang2}" ddb:type="translated">
     <xsl:value-of select="."/>
   </dc:title>
  </xsl:if>
</xsl:template>

<xsl:template match="dcterms:creator">
  <xsl:apply-templates select="foaf:Person"/>
  <xsl:apply-templates select="rdf:Seq"/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq">
  <xsl:apply-templates select="rdf:li"/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li">
  <xsl:apply-templates select="foaf:Person"/>
</xsl:template>

<xsl:template match="dcterms:creator/foaf:Person|dcterms:creator/rdf:Seq/rdf:li/foaf:Person">
  <dc:creator xsi:type="pc:MetaPers">
   <pc:person>
     <pc:name type="nameUsedByThePerson">
     <xsl:choose>
     <xsl:when test="foaf:familyName">
       <xsl:apply-templates select="foaf:givenName"/>
       <xsl:apply-templates select="foaf:familyName"/>
     </xsl:when>
       <xsl:otherwise><xsl:apply-templates select="foaf:name"/></xsl:otherwise>
     </xsl:choose>
    </pc:name>
    <xsl:apply-templates select="foaf:title"/>
   </pc:person>
  </dc:creator>
</xsl:template>

<xsl:template match="foaf:Person/foaf:givenName">
  <pc:foreName><xsl:value-of select="."/></pc:foreName>
</xsl:template>

<xsl:template match="foaf:Person/foaf:familyName">
  <pc:surName><xsl:value-of select="."/></pc:surName>
</xsl:template>

<xsl:template match="foaf:Person/foaf:title">
  <pc:academicTitle><xsl:value-of select="."/></pc:academicTitle>
</xsl:template>

<xsl:template match="foaf:Person/foaf:name">
    <pc:personEnteredUnderGivenName>
     <xsl:value-of select="."/>
    </pc:personEnteredUnderGivenName>
</xsl:template>

<xsl:template match="dcterms:contributor">
  <xsl:apply-templates select="foaf:Person"/>
</xsl:template>

<xsl:template match="dcterms:contributor/foaf:Person[foaf:role]">
  <dc:contributor xsi:type="pc:Contributor" thesis:role="{foaf:role}">
   <pc:person>
    <pc:name type="nameUsedByThePerson">
     <xsl:choose>
     <xsl:when test="foaf:familyName">
       <xsl:apply-templates select="foaf:givenName"/>
       <xsl:apply-templates select="foaf:familyName"/>
     </xsl:when>
       <xsl:otherwise><xsl:apply-templates select="foaf:name"/></xsl:otherwise>
     </xsl:choose>
    </pc:name>
    <xsl:apply-templates select="foaf:title"/>
   </pc:person>
  </dc:contributor>
</xsl:template>

<xsl:template match="dcterms:publisher[@rdf:resource]">
  <xsl:apply-templates select="../dcterms:isPartOf/fabio:*/dcterms:publisher"/>
</xsl:template>

<xsl:template match="dcterms:publisher">
    <xsl:apply-templates select="foaf:Organization"/>
</xsl:template>

<xsl:template match="dcterms:publisher/foaf:Organization">
 <dc:publisher xsi:type="cc:Publisher">
    <cc:universityOrInstitution>
      <cc:name><xsl:value-of select="foaf:name"/></cc:name>
      <cc:place>Marburg</cc:place>
    </cc:universityOrInstitution>
    <cc:address cc:Scheme="DIN5008">
        <xsl:value-of select="'Wilhelm-Röpke-Straße 4, 35039 Marburg'"/>
    </cc:address>
 </dc:publisher>
</xsl:template>

<xsl:template match="dcterms:subject">
  <xsl:apply-templates select="skos:Concept"/>
</xsl:template>

<!-- SWD TOPICS -->
<xsl:template match="dcterms:subject/skos:Concept[contains(@rdf:about,'gnd')]">
 <xsl:if test="normalize-space(rdfs:label)!=''">
  <dc:subject xsi:type="xMetaDiss:SWD">
      <xsl:value-of select="rdfs:label"/>
  </dc:subject>
 </xsl:if>
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept[not(@rdf:about)]">
 <xsl:if test="normalize-space(rdfs:label)!=''">
  <dc:subject xsi:type="xMetaDiss:noScheme">
      <xsl:value-of select="rdfs:label"/>
  </dc:subject>
 </xsl:if>
</xsl:template>


<xsl:template match="dcterms:subject/skos:Concept[contains(@rdf:about,'dewey')]">
  <!-- <xsl:comment> DDC </xsl:comment> -->
  <xsl:apply-templates select="skos:prefLabel" />
</xsl:template>

<xsl:template match="skos:Concept/skos:prefLabel">
  <dc:subject xsi:type="dcterms:DDC">
    <xsl:value-of select="."/>
  </dc:subject>
</xsl:template>

<xsl:template match="dcterms:abstract[@xml:lang]">
  <xsl:variable name="lang">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="@xml:lang"/>
   </xsl:call-template>
  </xsl:variable>
 <dcterms:abstract xsi:type="ddb:contentISO639-2" lang="{$lang}">
    <xsl:value-of select="."/>
 </dcterms:abstract>
</xsl:template>

<xsl:template match="dcterms:abstract[not(@xml:lang)]">
  <xsl:variable name="lang">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="../dcterms:language"/>
   </xsl:call-template>
  </xsl:variable>
 <dcterms:abstract xsi:type="ddb:contentISO639-2" lang="{$lang}">
    <xsl:value-of select="."/>
 </dcterms:abstract>
</xsl:template>

<xsl:template name="getlang">
 <xsl:param name="input"/>
  <xsl:choose>
   <xsl:when test="$input='de'">ger</xsl:when>
   <xsl:when test="$input='DE'">ger</xsl:when>
   <xsl:when test="$input='en'">eng</xsl:when>
   <xsl:when test="$input='EN'">eng</xsl:when>
   <xsl:when test="$input='fr'">fre</xsl:when>
   <xsl:when test="$input='la'">lat</xsl:when>
   <xsl:when test="$input='es'">spa</xsl:when>
   <xsl:when test="$input='it'">ita</xsl:when>
   <xsl:when test="$input='ja'">jpn</xsl:when>
   <xsl:when test="$input='nl'">ndl</xsl:when>
   <xsl:when test="$input='ru'">rus</xsl:when>
   <xsl:when test="$input='na'">paa</xsl:when>
   <xsl:when test="$input='bi'">mul</xsl:when>
   <xsl:when test="$input='ar'">ara</xsl:when>
   <xsl:when test="$input='el'">grc</xsl:when>
   <xsl:when test="$input='he'">heb</xsl:when>
   <xsl:when test="$input='zu'">und</xsl:when>
   <xsl:otherwise><xsl:value-of select="$input"/></xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="dcterms:dateAccepted">
  <dcterms:dateAccepted xsi:type="dcterms:W3CDTF">
      <xsl:value-of select="."/>
  </dcterms:dateAccepted>
</xsl:template>

<xsl:template match="dcterms:created">
  <dcterms:created><xsl:value-of select="."/></dcterms:created>
</xsl:template>

<xsl:template match="dcterms:issued">
  <dcterms:issued xsi:type="dcterms:W3CDTF">
      <xsl:value-of select="."/>
  </dcterms:issued>
</xsl:template>

<xsl:template match="dcterms:modified">
  <dcterms:modified><xsl:value-of select="."/></dcterms:modified>
</xsl:template>

<!-- preprint, workingPaper, article, contributionToPeriodical, 
     PeriodicalPart, Periodical, Manuscript, book, bookPart, 
     StudyThesis, bachelorThesis, masterThesis, doctoralThesis,
     conferenceObject, lecture, review, annotation, patent, report, 
     Sound, Image, MovingImage, StillImage, MusicalNotation, 
     CourseMaterial, Website, Software, CarthographicMaterial, ResearchData,
-->
<!--
<xsl:template match="fabio:Book|fabio:Biography" mode="type">
    <dc:type xsi:type="dini:PublType">book</dc:type>
    <dc:type xsi:type="dcterms:DCMIType">Text</dc:type>
</xsl:template>
<xsl:template match="fabio:PeriodicalIssue" mode="type">
    <dc:type xsi:type="dini:PublType">PeriodicalPart</dc:type>
    <dc:type xsi:type="dcterms:DCMIType">Text</dc:type>
</xsl:template>
<xsl:template match="fabio:DoctoralThesis" mode="type">
    <dc:type xsi:type="dini:PublType">doctoralThesis</dc:type>
    <dc:type xsi:type="dcterms:DCMIType">Text</dc:type>
</xsl:template>
<xsl:template match="fabio:JournalArticle" mode="type">
    <dc:type xsi:type="dini:PublType">article</dc:type>
    <dc:type xsi:type="dcterms:DCMIType">Text</dc:type>
</xsl:template>
<xsl:template match="fabio:Article" mode="type">
    <dc:type xsi:type="dini:PublType">article</dc:type>
    <dc:type xsi:type="dcterms:DCMIType">Text</dc:type>
</xsl:template>
<xsl:template match="fabio:InBook" mode="type">
    <dc:type xsi:type="dini:PublType">bookPart</dc:type>
    <dc:type xsi:type="dcterms:DCMIType">Text</dc:type>
</xsl:template>
<xsl:template match="fabio:BookChapter" mode="type">
    <dc:type xsi:type="dini:PublType">bookPart</dc:type>
    <dc:type xsi:type="dcterms:DCMIType">Text</dc:type>
</xsl:template>
<xsl:template match="fabio:Paper" mode="type">
    <dc:type xsi:type="dini:PublType">workingPaper</dc:type>
    <dc:type xsi:type="dcterms:DCMIType">Text</dc:type>
</xsl:template>
<xsl:template match="fabio:CollectedWorks" mode="type">
    <dc:type xsi:type="dini:PublType">Website</dc:type>
    <dc:type xsi:type="dcterms:DCMIType">Text</dc:type>
</xsl:template>
<xsl:template match="fabio:*" mode="type">
    <dc:type xsi:type="dini:PublType">report</dc:type>
    <dc:type xsi:type="dcterms:DCMIType">Text</dc:type>
</xsl:template>
-->

<xsl:template match="dcterms:type">
    <dc:type xsi:type="dini:PublType"><xsl:value-of select="."/></dc:type>
    <dc:type xsi:type="dcterms:DCMIType">Text</dc:type>
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(text(),'urn:')]">
  <dc:identifier xsi:type="urn:nbn"><xsl:value-of select="."/></dc:identifier>
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(text(),'http://dx.doi.org/')]">
  <ddb:identifier ddb:type="DOI"><xsl:value-of select="substring-after(.,'http://dx.doi.org/')"/></ddb:identifier>
</xsl:template>

<!-- Old -->
<xsl:template match="fabio:hasDOI">
  <ddb:identifier ddb:type="DOI"><xsl:value-of select="."/></ddb:identifier>
</xsl:template>

<xsl:template match="dcterms:language[@rdf:resource]">
 <xsl:variable name="lang">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="substring-after(@rdf:resource,'http://www.lexvo.org/id/iso639-1/')"/>
   </xsl:call-template>
 </xsl:variable>
 <dc:language xsi:type="dcterms:ISO639-2">
    <xsl:value-of select="$lang"/>
 </dc:language>
</xsl:template>

<xsl:template match="dcterms:language[not(@rdf:resource)]">
 <xsl:variable name="lang">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="."/>
   </xsl:call-template>
 </xsl:variable>
 <dc:language xsi:type="dcterms:ISO639-2">
    <xsl:value-of select="$lang"/>
 </dc:language>
</xsl:template>

<!-- 
 dcterms:isPartOf: mit Hilfe diese Elementes wird die Verknüpfung von Lieferung
 zum periodischen Titel angegeben; dazu kann ein systeminterner Identifier 
 (Attibut xsi:type="ddb:ZSTitelID") oder die ZDB-ID 
 (Attribut xsi:type="ddb:Erstkat_ID) benutzt werden; 
 zusätzlich ist die Angabe zur Ausgabe der Lieferung erforderlich 
 (Wiederholung des Elements dcterms:isPartOf mit 
  Attribut xsi:type="ddb:ZS-Ausgabe")

 META : ZDB-Idn 2714728-9
 MEDREZ : ZDB-Idn 1465812-4
-->

<xsl:template match="dcterms:isPartOf/fabio:JournalIssue/fabio:hasIdentifier">
 <dcterms:isPartOf xsi:type="ddb:Erstkat-ID">
     <xsl:value-of select="."/>
 </dcterms:isPartOf>
</xsl:template>

<xsl:template match="dcterms:isPartOf/fabio:JournalIssue/dcterms:title">
 <dcterms:isPartOf xsi:type="ddb:ZS-Ausgabe">
     <xsl:value-of select="."/>
 </dcterms:isPartOf>
</xsl:template>

<xsl:template match="dcterms:isPartOf/fabio:Periodical">
 <xsl:if test="../../fabio:hasSequenceIdentifier and dcterms:title">
   <dcterms:isPartOf xsi:type="ddb:noScheme">
     <xsl:value-of select="concat(dcterms:title,' ; ',
                                  ../../fabio:hasSequenceIdentifier)"/>
   </dcterms:isPartOf>
 </xsl:if>
 <xsl:apply-templates select="fabio:hasISSN"/>
</xsl:template>

<xsl:template match="dcterms:isPartOf/fabio:Periodical/fabio:hasISSN">
 <dcterms:isPartOf xsi:type="ddb:ISSN">
    <xsl:value-of select="."/>
 </dcterms:isPartOf>
</xsl:template>

<xsl:template match="dcterms:rights[@rdf:resource]">
  <dc:rights><xsl:value-of select="@rdf:resource"/></dc:rights>
</xsl:template>

<xsl:template match="dcterms:rights[not(@rdf:resource)]">
  <dc:rights><xsl:value-of select="."/></dc:rights>
</xsl:template>

<xsl:template match="fabio:*" mode="degree">
</xsl:template>

<xsl:template match="fabio:DoctoralThesis" mode="degree">
  <thesis:degree>
    <thesis:level>thesis.doctoral</thesis:level>
    <thesis:grantor xsi:type="cc:Corporate">
      <cc:universityOrInstitution>
        <cc:name>Philipps-Universität Marburg</cc:name>
        <cc:place>Marburg</cc:place>
        <cc:department>
          <cc:name><xsl:value-of select="normalize-space(dcterms:publisher/aiiso:Faculty)"/></cc:name>
        </cc:department>
      </cc:universityOrInstitution>
    </thesis:grantor>
  </thesis:degree>
</xsl:template>

<!-- since 20151101 -->
<xsl:template match="dcterms:hasPart[not(@rdf:resource)][1]">
  <ddb:fileNumber><xsl:value-of select="'1'"/></ddb:fileNumber>
  <xsl:choose>
  <xsl:when test="count(../dcterms:hasPart)>1">
      <ddb:transfer ddb:type="dcterms:URI">
            <xsl:value-of select="concat(../@rdf:about, '/container.zip')"/>
      </ddb:transfer>
  </xsl:when>
  <xsl:when test="count(dctypes:Text)=1">
      <xsl:apply-templates select="dctypes:Text"/>
  </xsl:when>
  <xsl:when test="count(dctypes:Collection)=1">
      <xsl:apply-templates select="dctypes:Collection"/>
  </xsl:when>
 </xsl:choose>
  <!--
  <xsl:apply-templates select="dctypes:Image"/>
  <xsl:apply-templates select="dctypes:MovingImage"/>
  <xsl:apply-templates select="dctypes:Collection"/>
  <xsl:apply-templates select="dctypes:Dataset"/>
  -->
</xsl:template>

<xsl:template match="dcterms:hasPart/dctypes:Text">
  <ddb:transfer ddb:type="dcterms:URI">
    <xsl:value-of select="@rdf:about"/>
  </ddb:transfer>
</xsl:template>

<xsl:template match="dcterms:hasPart/dctypes:Collection">
  <ddb:transfer ddb:type="dcterms:URI">
    <xsl:value-of select="@rdf:about"/>
  </ddb:transfer>
</xsl:template>

<!-- URL LICENSE -->
<xsl:template match="fabio:*" mode="about">
  <ddb:identifier ddb:type="URL">
       <xsl:value-of select="@rdf:about"/>
  </ddb:identifier>
  <ddb:rights ddb:kind="free"/>
</xsl:template>

<xsl:template match="fabio:DoctoralThesis" mode="about">
  <ddb:identifier ddb:type="URL">
       <xsl:value-of select="@rdf:about"/>
  </ddb:identifier>
  <xsl:choose>
  <xsl:when test="contains(dcterms:license/@rdf:resource,'creativecommons')">
    <ddb:rights ddb:kind="free"/>
  </xsl:when>
  <xsl:otherwise><ddb:rights ddb:kind="domain"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="fabio:JournalArticle" mode="about">
  <ddb:identifier ddb:type="URL">
   <xsl:choose>
     <xsl:when test="fabio:hasURL">
       <xsl:value-of select="fabio:hasURL"/>
     </xsl:when>
     <xsl:otherwise>
       <xsl:value-of select="@rdf:about"/>
     </xsl:otherwise>
   </xsl:choose>
  </ddb:identifier>
  <xsl:choose>
  <xsl:when test="contains(dcterms:license/@rdf:resource,'creativecommons')">
      <ddb:rights ddb:kind="free"/>
  </xsl:when>
  <!-- NLM -->
  <xsl:when test="contains(dcterms:isPartOf/fabio:JournalIssue/dcterms:license/@rdf:resource,'creativecommons')">
      <ddb:rights ddb:kind="free"/>
  </xsl:when>
  <xsl:when test="contains(@rdf:about,'ep/0002/')">
      <ddb:rights ddb:kind="free"/>
  </xsl:when>
  <xsl:otherwise>
      <ddb:rights ddb:kind="domain"/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- suppress emptyness -->
<xsl:template match="text()"/>

<xsl:template match="*" priority="-1"/>

</xsl:stylesheet>
