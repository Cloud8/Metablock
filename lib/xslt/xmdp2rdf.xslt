<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dc="http://purl.org/dc/elements/1.1/" 
     xmlns:dct="http://purl.org/dc/terms/" 
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:skos="http://www.w3.org/2008/05/skos#"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:xMetaDiss="http://www.d-nb.de/standards/xmetadissplus/" 
     xmlns:cc="http://www.d-nb.de/standards/cc/"
     xmlns:pc="http://www.d-nb.de/standards/pc/" 
     xmlns:urn="http://www.d-nb.de/standards/urn/" 
     xmlns:thesis="http://www.ndltd.org/standards/metadata/etdms/1.0/" 
     xmlns:ddb="http://www.d-nb.de/standards/ddb/" 
     xmlns:dini="http://www.d-nb.de/standards/xmetadissplus/type/" 
     xmlns:gnd="http://d-nb.info/gnd/" 
     xsi:schemaLocation="http://www.d-nb.de/standards/xmetadissplus/ http://www.d-nb.de/standards/xmetadissplus/xmetadissplus.xsd"
     version="1.0" >

<!-- /**
      * @license http://www.apache.org/licenses/LICENSE-2.0
      * @author Goetz Hatop <fb.com/goetz.hatop>
      * @title A XSLT Transformer for XMetadissPlus to RDF 
      * @date 2014-06-03
      **/ -->
<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/">
  <xsl:apply-templates select="*" />
</xsl:template>

<xsl:template match="metadata">
  <xsl:apply-templates select="*" />
</xsl:template>


<xsl:template match="xMetaDiss:xMetaDiss">
  <rdf:RDF>
    <xsl:choose>
     <xsl:when test="dc:type[@xsi:type='dini:PublType']='doctoralThesis'">
      <fabio:DoctoralThesis rdf:about="{ddb:identifier}">
         <xsl:call-template name="mad" />
         <dct:type>DoctoralThesis</dct:type>
      </fabio:DoctoralThesis>
     </xsl:when>
     <xsl:when test="dc:type[@xsi:type='dini:PublType']='book'">
      <fabio:Book rdf:about="{ddb:identifier}">
         <xsl:call-template name="mad" />
         <dct:type>Book</dct:type>
      </fabio:Book>
     </xsl:when>
     <xsl:when test="dc:type[@xsi:type='dini:PublType']='Periodical'">
      <fabio:Periodical rdf:about="{ddb:identifier}">
         <xsl:call-template name="mad" />
         <dct:type>Periodical</dct:type>
      </fabio:Periodical>
     </xsl:when>
     <!-- Volume : Band einer Serie -->
     <xsl:when test="dc:type[@xsi:type='dini:PublType']='bookPart'">
      <fabio:PeriodicalIssue rdf:about="{ddb:identifier}">
         <xsl:call-template name="mad" />
         <dct:type>PeriodicalIssue</dct:type>
      </fabio:PeriodicalIssue>
     </xsl:when>
     <xsl:when test="dc:type[@xsi:type='dini:PublType']='MusicalNotation'">
      <fabio:MusicalComposition rdf:about="{ddb:identifier}">
         <xsl:call-template name="mad" />
         <dct:type>MusicalNotation</dct:type>
      </fabio:MusicalComposition>
     </xsl:when>
     <xsl:when test="dc:type[@xsi:type='dini:PublType']='Image'">
      <fabio:StillImage rdf:about="{ddb:identifier}">
         <xsl:call-template name="mad" />
         <dct:type>Image</dct:type>
      </fabio:StillImage>
     </xsl:when>
     <xsl:when test="dc:type[@xsi:type='dini:PublType']='article'">
      <fabio:Article rdf:about="{ddb:identifier}">
         <xsl:call-template name="mad" />
         <dct:type>Article</dct:type>
      </fabio:Article>
     </xsl:when>
     <xsl:when test="dc:type[@xsi:type='dini:PublType']='bookPart'">
      <fabio:BookChapter rdf:about="{ddb:identifier}">
         <xsl:call-template name="mad" />
         <dct:type>BookChapter</dct:type>
      </fabio:BookChapter>
     </xsl:when>
     <xsl:when 
          test="dc:type[@xsi:type='dini:PublType']='contributionToPeriodical'">
      <fabio:PeriodicalVolume rdf:about="{ddb:identifier}">
         <xsl:call-template name="mad" />
         <dct:type>PeriodicalVolume</dct:type>
      </fabio:PeriodicalVolume>
     </xsl:when>
     <xsl:when test="dc:type[@xsi:type='dini:PublType']='workingPaper'">
      <fabio:WorkingPaper rdf:about="{ddb:identifier}">
         <xsl:call-template name="mad" />
         <dct:type>WorkingPaper</dct:type>
      </fabio:WorkingPaper>
     </xsl:when>
     <xsl:otherwise>
      <fabio:BibliographicMetadata rdf:about="{ddb:identifier}">
         <xsl:call-template name="mad" />
         <dct:type>BibliographicResource</dct:type>
      </fabio:BibliographicMetadata> 
     </xsl:otherwise>
    </xsl:choose>
  </rdf:RDF>
</xsl:template>

<xsl:template name="mad">
      <xsl:apply-templates select="dc:title" />
      <xsl:apply-templates select="dc:creator" />
      <xsl:apply-templates select="dc:subject" />
      <xsl:apply-templates select="dct:abstract" />
      <xsl:apply-templates select="dc:publisher" />
      <xsl:apply-templates select="dc:contributor" />
      <xsl:apply-templates select="dct:dateAccepted" />
      <xsl:apply-templates select="dct:issued" />
      <xsl:apply-templates select="dct:modified" />
      <xsl:apply-templates select="dct:created" />
      <xsl:apply-templates select="dc:identifier" />
      <xsl:apply-templates select="dc:language" />
      <xsl:apply-templates select="dct:isPartOf" />
      <xsl:apply-templates select="dc:rights" />
      <xsl:apply-templates select="dc:type" />
      <xsl:apply-templates select="ddb:transfer" />
</xsl:template>

<xsl:template match="dc:title">
  <dct:title><!--<xsl:copy-of select="@xsi:type"/>-->
     <xsl:attribute name="xml:lang">
      <xsl:choose>
        <xsl:when test="@lang='ger'">de</xsl:when>
        <xsl:when test="@lang='eng'">en</xsl:when>
        <xsl:when test="@lang='fre'">fr</xsl:when>
        <xsl:otherwise><xsl:value-of select="@lang"/></xsl:otherwise>
      </xsl:choose>
     </xsl:attribute>
     <xsl:value-of select="."/>
  </dct:title>
</xsl:template>

<xsl:template match="dc:creator">
  <dct:creator>
     <foaf:Person>
       <xsl:apply-templates select="pc:person" />
     </foaf:Person>
  </dct:creator>
</xsl:template>

<xsl:template match="pc:person">
   <xsl:apply-templates select="pc:name" />
</xsl:template>

<xsl:template match="pc:name">
  <xsl:apply-templates select="pc:foreName" />
  <xsl:apply-templates select="pc:surName" />
  <xsl:apply-templates select="pc:personEnteredUnderGivenName" />
</xsl:template>

<!-- most general first -->
<xsl:template match="dc:subject[@xsi:type]">
   <dct:subject><xsl:value-of select="."/></dct:subject>
</xsl:template>

<xsl:template match="dc:subject[@xsi:type='xMetaDiss:DDC-SG']">
   <dct:subject><xsl:value-of select="."/></dct:subject>
</xsl:template>

<!-- 2012 ist die SWD in der Gemeinsamen Normdatei (GND) aufgegangen -->
<xsl:template match="dc:subject[@xsi:type='xMetaDiss:SWD']">
   <dct:subject><xsl:value-of select="."/></dct:subject>
</xsl:template>

<xsl:template match="dc:subject[@xsi:type='xMetaDiss:noScheme']">
  <dct:subject><xsl:value-of select="."/></dct:subject>
</xsl:template>

<xsl:template match="dct:abstract">
  <dct:abstract>
     <xsl:attribute name="xml:lang">
      <xsl:choose>
        <xsl:when test="@lang='ger'">de</xsl:when>
        <xsl:when test="@lang='eng'">en</xsl:when>
      </xsl:choose>
     </xsl:attribute>
     <xsl:value-of select="."/>
  </dct:abstract>
</xsl:template>

<xsl:template match="dc:publisher">
    <xsl:apply-templates select="cc:universityOrInstitution" />
</xsl:template>

<xsl:template match="cc:universityOrInstitution">
  <dct:publisher>
   <xsl:choose>
   <xsl:when test="contains(.,'Universität Marburg')">
    <xsl:value-of select="'http://d-nb.info/gnd/2001630-X'"/>
    </xsl:when>
    <xsl:otherwise>
    <foaf:Organization>
     <foaf:name><xsl:value-of select="cc:name" /></foaf:name>
    </foaf:Organization>
    </xsl:otherwise>
   </xsl:choose>
  </dct:publisher>
  <dct:publisher>
    <foaf:Organization>
     <foaf:name><xsl:value-of select="cc:department/cc:name" /></foaf:name>
    </foaf:Organization>
  </dct:publisher>
</xsl:template>

<xsl:template match="dc:contributor">
  <dct:contributor>
     <xsl:value-of select="pc:person/pc:academicTitle"/>
     <xsl:text> </xsl:text>
     <xsl:value-of select="pc:person/pc:name/pc:foreName"/>
     <xsl:text> </xsl:text>
     <xsl:value-of select="pc:person/pc:name/pc:surName"/>
  </dct:contributor>
</xsl:template>

<xsl:template match="dct:dateAccepted">
  <dct:dateAccepted><xsl:value-of select="."/></dct:dateAccepted>
</xsl:template>

<xsl:template match="dct:issued">
  <dct:issued><xsl:value-of select="."/></dct:issued>
</xsl:template>

<xsl:template match="dct:modified">
  <dct:modified><xsl:value-of select="."/></dct:modified>
</xsl:template>

<xsl:template match="dct:created">
  <dct:created><xsl:value-of select="."/></dct:created>
</xsl:template>

<xsl:template match="dc:type[@xsi:type='dcterms:DCMIType']">
  <dct:format><xsl:value-of select="."/></dct:format>
</xsl:template>

<xsl:template match="dc:type[@xsi:type='dini:PublType']">
  <dct:type><xsl:value-of select="."/></dct:type>
</xsl:template>

<xsl:template match="dc:identifier">
  <dct:identifier><xsl:value-of select="."/></dct:identifier>
</xsl:template>

<xsl:template match="dc:language">
  <dct:language>
  <xsl:choose>
    <xsl:when test=".='ger'">de</xsl:when>
    <xsl:when test=".='eng'">en</xsl:when>
    <xsl:when test=".='fre'">fr</xsl:when>
    <xsl:otherwise>
     <xsl:value-of select="text()"/>
    </xsl:otherwise>
  </xsl:choose>
  </dct:language>
</xsl:template>

<xsl:template match="dct:isPartOf[not(@xsi:type)]">
 <xsl:comment>
    <xsl:value-of select="@*" />: <xsl:value-of select="." />
 </xsl:comment>
</xsl:template>

<!-- hierarchies ?? -->
<xsl:template match="dct:isPartOf[@xsi:type='ddb:noScheme']">
  <dct:hasPart><xsl:value-of select="." /></dct:hasPart>
</xsl:template>

<xsl:template match="dct:isPartOf[@xsi:type='dcterms:URI']">
  <dct:isPartOf rdf:resource="{.}" />
</xsl:template>

<xsl:template match="dct:isPartOf[@xsi:type='dct:URI']">
  <dct:isPartOf rdf:resource="{.}" />
</xsl:template>

<xsl:template match="thesis:degree">
  <xsl:copy><xsl:value-of select="thesis:level/text()"/></xsl:copy>
</xsl:template>

<xsl:template match="cc:name">
  <xsl:copy>
     <xsl:apply-templates select="text()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="cc:department">
  <xsl:copy>
     <xsl:value-of select="cc:name/text()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="dc:rights">
  <dct:rights><xsl:value-of select="."/></dct:rights>
</xsl:template>

<xsl:template match="ddb:transfer">
  <dct:relation><xsl:value-of select="."/></dct:relation>
</xsl:template>

<xsl:template match="pc:foreName">
  <foaf:givenName><xsl:value-of select="."/></foaf:givenName>
</xsl:template>

<xsl:template match="pc:surName">
  <foaf:familyName><xsl:value-of select="."/></foaf:familyName>
  <foaf:name>
     <xsl:value-of select="../pc:foreName"/>
     <xsl:text> </xsl:text>
     <xsl:value-of select="."/>
  </foaf:name>
</xsl:template>

<xsl:template match="pc:personEnteredUnderGivenName">
  <foaf:name><xsl:value-of select="."/></foaf:name>
</xsl:template>

<!-- garbage protection -->
<xsl:template match="text()"/>

</xsl:stylesheet>

