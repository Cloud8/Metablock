<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:marc="http://www.loc.gov/MARC21/slim"
     xmlns:pica="http://localhost/metacat/card/ppn/"
     version="1.0" >

<!--
 /**
  * @license http://www.apache.org/licenses/LICENSE-2.0
  * @title Pica RDF Marc Transformer
  * @date 2016-06-02
  * @see http://www.rdaregistry.info
 **/
 -->

<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />

<xsl:template match="rdf:RDF">
 <marc:collection xmlns:marc="http://www.loc.gov/MARC21/slim"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.loc.gov/MARC21/slim
    http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd">
    <xsl:comment> RDF MARC Transformer UB Marburg (2016) </xsl:comment>
    <xsl:apply-templates select="dcterms:BibliographicResource" />
 </marc:collection>
</xsl:template>

<xsl:template match="dcterms:BibliographicResource">
 <marc:record>
  <marc:leader>00000nam a2200000 i 4500</marc:leader>
  <!-- <xsl:comment>Control Number</xsl:comment> -->
  <xsl:apply-templates select="dcterms:*"/>
 </marc:record>
</xsl:template>

<xsl:template match="dcterms:title">
  <datafield tag="500" ind1=" " ind2=" ">
     <subfield code="a"><xsl:value-of select="."/></subfield>
  </datafield>
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
  <datafield tag="100" ind1="1" ind2=" ">
      <subfield code="a"><xsl:value-of select="foaf:name"/></subfield>
  </datafield>
</xsl:template>

<xsl:template match="foaf:Person/foaf:givenName">
</xsl:template>

<xsl:template match="foaf:Person/foaf:familyName">
</xsl:template>

<xsl:template match="foaf:Person/foaf:title">
</xsl:template>

<xsl:template match="foaf:Person/foaf:name">
</xsl:template>

<xsl:template match="dcterms:contributor">
  <xsl:apply-templates select="foaf:Person"/>
</xsl:template>

<xsl:template match="dcterms:contributor/foaf:Person[foaf:role]">
</xsl:template>

<xsl:template match="dcterms:publisher[@rdf:resource]">
  <xsl:apply-templates select="../dcterms:isPartOf/*/dcterms:publisher"/>
</xsl:template>

<xsl:template match="dcterms:publisher">
    <xsl:apply-templates select="foaf:Organization"/>
</xsl:template>

<xsl:template match="dcterms:publisher/foaf:Organization">
 <marc:datafield tag="260" ind1="0" ind2="4">
  <marc:subfield code="c"><xsl:value-of select="foaf:name"/></marc:subfield>
 </marc:datafield>
</xsl:template>

<xsl:template match="dcterms:subject">
  <xsl:apply-templates select="skos:Concept"/>
</xsl:template>

<!-- SWD TOPICS -->
<xsl:template match="dcterms:subject/skos:Concept[contains(@rdf:about,'gnd')]">
 <xsl:if test="normalize-space(rdfs:label)!=''">
 </xsl:if>
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept[not(@rdf:about)]">
 <xsl:if test="normalize-space(rdfs:label)!=''">
 </xsl:if>
</xsl:template>


<xsl:template match="dcterms:subject/skos:Concept[contains(@rdf:about,'dewey')]">
  <!-- <xsl:comment> DDC </xsl:comment> -->
  <xsl:apply-templates select="skos:prefLabel" />
</xsl:template>

<!-- DDC -->
<xsl:template match="skos:Concept/skos:prefLabel">
</xsl:template>

<xsl:template match="dcterms:abstract[@xml:lang]">
  <xsl:variable name="lang">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="@xml:lang"/>
   </xsl:call-template>
  </xsl:variable>
  <datafield tag="520" ind1=" " ind2=" ">
      <subfield code="a"><xsl:value-of select="."/></subfield>
  </datafield>
</xsl:template>

<xsl:template match="dcterms:abstract[not(@xml:lang)]">
  <xsl:variable name="lang">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="substring-after(../dcterms:language/@rdf:resource,'iso639-1/')"/>
   </xsl:call-template>
  </xsl:variable>
  <datafield tag="520" ind1=" " ind2=" ">
      <subfield code="a"><xsl:value-of select="normalize-space(.)"/></subfield>
  </datafield>
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
   <xsl:otherwise><xsl:value-of select="'und'"/></xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="dcterms:dateAccepted">
</xsl:template>

<xsl:template match="dcterms:created">
</xsl:template>

<xsl:template match="dcterms:issued">
</xsl:template>

<xsl:template match="dcterms:modified">
</xsl:template>

<xsl:template match="dcterms:type[@rdf:resource]">
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(text(),'oclc:')]">
  <marc:controlfield tag="003"><xsl:value-of select="."/></marc:controlfield>
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(text(),'isbn:')]">
 <!-- <xsl:comment>ISBN</xsl:comment> -->
 <marc:datafield tag="020" ind1=" " ind2=" ">
   <marc:subfield code="a"><xsl:value-of select="substring(.,6)"/>
  </marc:subfield>
 </marc:datafield>
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(text(),'urn:')]">
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(text(),'http://dx.doi.org/')]">
</xsl:template>

<xsl:template match="dcterms:language[@rdf:resource]">
 <xsl:variable name="lang">
   <xsl:call-template name="getlang">
    <xsl:with-param name="input" select="substring-after(@rdf:resource,'iso639-1/')"/>
   </xsl:call-template>
 </xsl:variable>
</xsl:template>

<xsl:template match="dcterms:isPartOf/dcterms:BibliographicResource/dcterms:identifier[starts-with(text(),'zdb:')]">
</xsl:template>

<xsl:template match="dcterms:isPartOf/dcterms:BibliographicResource/dcterms:title[contains(text(),../../../dcterms:created)]">
</xsl:template>

<xsl:template match="dcterms:isPartOf/*/dcterms:identifier[starts-with(text(),'issn:')]">
</xsl:template>

<xsl:template match="dcterms:rights[@rdf:resource]">
</xsl:template>

<xsl:template match="dcterms:rights[not(@rdf:resource)]">
</xsl:template>

<xsl:template match="dcterms:BibliographicResource[contains(dcterms:type/@rdf:resource,'DoctoralThesis')]" mode="degree">
</xsl:template>

<xsl:template match="dcterms:hasPart[not(@rdf:resource)][1]">
</xsl:template>

<xsl:template match="dcterms:hasPart/dctypes:Text">
</xsl:template>

<xsl:template match="dcterms:hasPart/dctypes:Collection">
</xsl:template>

<xsl:template match="dcterms:BibliographicResource" mode="about">
</xsl:template>

<!-- suppress emptyness -->
<xsl:template match="text()"/>

<xsl:template match="*" priority="-1"/>
<xsl:template match="*" mode="degree" priority="-1"/>
<xsl:template match="*" mode="about" priority="-1"/>

</xsl:stylesheet>
