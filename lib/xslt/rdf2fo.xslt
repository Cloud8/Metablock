<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
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
     xmlns:daia="http://purl.org/ontology/daia#"
     xmlns:fo="http://www.w3.org/1999/XSL/Format">

<xsl:output method="xml" indent="yes"/>

<xsl:template match="/">
  <xsl:apply-templates select="rdf:RDF" />
</xsl:template>

<xsl:template match="rdf:RDF">
 <fo:root>
   <fo:layout-master-set>
     <fo:simple-page-master master-name="A4-titlecard"
              page-height="312mm" page-width="21.0cm" 
			  margin-top="0mm" margin-bottom="0mm"
			  margin-left="4cm" margin-right="4cm">
          <fo:region-body/>
          <fo:region-after region-name="footer" extent="14mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="A4-titlecard">
        <fo:static-content flow-name="footer">
            <fo:block text-align="right">
                <fo:page-number />
            </fo:block>
        </fo:static-content>
        <fo:flow flow-name="xsl-region-body">
            <fo:block font-family="DroidSerif" font-size="10pt">
                <xsl:apply-templates select="dcterms:BibliographicResource"/>
            </fo:block>
        </fo:flow>
  </fo:page-sequence>
 </fo:root>
</xsl:template>


<xsl:template match="dcterms:BibliographicResource">
    <xsl:apply-templates select="dcterms:medium" />
    <xsl:apply-templates select="dcterms:hasPart" />
</xsl:template>

<xsl:template match="dcterms:medium">
    <xsl:apply-templates select="rdf:Seq/rdf:li/dcterms:PhysicalMedium"/>
</xsl:template>

<xsl:template match="dcterms:hasPart|dcterms:PhysicalMedium">
 <!-- Titelkarten sind 12.5 cm breit und 7.4 cm hoch -->
 <!-- border-style="dashed" border-width="0.8pt" border-color="orange" -->
 <fo:block-container overflow="hidden" 
     space-before="0mm" space-after="0mm"
     top="0cm" left="0cm" width="125mm" height="76mm">

 <fo:table table-layout="fixed" width="100%">

    <fo:table-column column-width="0.8cm"/>
    <fo:table-column column-width="11.1cm"/>
    <fo:table-column column-width="0.6cm"/>

    <fo:table-body>

    <fo:table-row height="1.3cm">
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
    </fo:table-row>

    <!-- Signatur -->
   <fo:table-row space-before="0mm">
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
        <fo:table-cell><fo:block text-align="right">
           <xsl:apply-templates select="daia:label" />
           <xsl:apply-templates select="../dcterms:identifier[starts-with(text(),'urn')]" />
           </fo:block></fo:table-cell>
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
    </fo:table-row>

    <!-- Autor -->
    <fo:table-row height="1.0cm">
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
        <fo:table-cell display-align="center">
        <fo:block>
          <xsl:apply-templates select="../dcterms:creator" />
          <xsl:apply-templates select="../../../../dcterms:creator"/>
        </fo:block>
        </fo:table-cell>
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
    </fo:table-row>

    <!-- Titel -->
    <fo:table-row>
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
        <fo:table-cell>
        <fo:block font-family="DroidSerif-BoldItalic" font-weight="bold">
            <fo:inline>
                <xsl:apply-templates select="../dcterms:title"/>
                <xsl:apply-templates select="../../../../dcterms:title"/>
            </fo:inline>
        </fo:block>
        </fo:table-cell>
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
    </fo:table-row>

    <!-- Verlagsangaben -->
    <fo:table-row space-before="0.4em">
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
        <fo:table-cell>
        <fo:block>
           <xsl:apply-templates select="./dcterms:publisher"/>
           <xsl:apply-templates select="../../../../dcterms:publisher"/>
        </fo:block>
        </fo:table-cell>
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
    </fo:table-row>

    <!-- Material -->
    <fo:table-row space-before="0.4em">
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
        <fo:table-cell>
        <fo:block>
            <xsl:apply-templates select="../../../../dcterms:extent"/>
            <xsl:apply-templates select="../dcterms:extent"/>
        </fo:block>
        </fo:table-cell>
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
    </fo:table-row>

    <!-- ISBN -->
    <fo:table-row space-before="0.4em">
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
        <fo:table-cell>
        <fo:block>
            <xsl:apply-templates select="../../../../dcterms:identifier[starts-with(text(),'isbn:')]"/>
            <xsl:apply-templates select="../dcterms:identifier[starts-with(text(),'isbn:')]"/>
        </fo:block>
        </fo:table-cell>
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
    </fo:table-row>

    <!-- Dissertationen Vermerk : TO DO -->
    <fo:table-row space-before="0.4em">
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
        <fo:table-cell>
        <fo:block>
            <xsl:apply-templates select="../../../../dcterms:alternative"/>
        </fo:block>
        </fo:table-cell>
        <fo:table-cell><fo:block>
        </fo:block></fo:table-cell>
    </fo:table-row>

    <!-- Schriftenreihen -->
    <!--
    <fo:table-row space-before="0.4em">
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
        <fo:table-cell>
        <fo:block>
          <xsl:apply-templates select="../field[@key='036E']" />
        </fo:block>
        </fo:table-cell>
        <fo:table-cell><fo:block>
        </fo:block></fo:table-cell>
    </fo:table-row>
    -->

    <!-- Inventarnummer -->
    <!--
    <fo:table-row space-before="0.4em">
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
        <fo:table-cell>
        <fo:block>
          <xsl:apply-templates select="field[@key='209C']" />
        </fo:block>
        </fo:table-cell>
        <fo:table-cell><fo:block>
        </fo:block></fo:table-cell>
    </fo:table-row>
    -->

    <!-- Zeitschriften Bandzaehlung -->
    <!--
    <fo:table-row space-before="0.4em">
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
        <fo:table-cell>
        <fo:block>
          <xsl:apply-templates select="field[@key='209E'][position()=1]" />
        </fo:block>
        </fo:table-cell>
        <fo:table-cell><fo:block>
        </fo:block></fo:table-cell>
    </fo:table-row>
    -->

    <!-- Sonderstandorte -->
    <!--
    <fo:table-row space-before="0.4em">
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
        <fo:table-cell>
        <fo:block text-align="right">
          <xsl:apply-templates select="field[@key='209G']" />
        </fo:block>
        </fo:table-cell>
        <fo:table-cell><fo:block>
        </fo:block></fo:table-cell>
    </fo:table-row>
    -->

    <!-- ppn bar -->
    <!--
    <fo:table-row height="0.65cm">
        <fo:table-cell><fo:block></fo:block></fo:table-cell>
        <fo:table-cell>
        <fo:block>
            bar <xsl:apply-templates select="field[@key='201@']" />
            ppn <xsl:apply-templates select="../field[@key='003@']" />
        </fo:block>
        </fo:table-cell>
        <fo:table-cell><fo:block>
      <xsl:value-of select="count(../preceding-sibling::*/item) + position()"/>.
        </fo:block></fo:table-cell>
    </fo:table-row>
    -->
</fo:table-body>
</fo:table>
</fo:block-container>
</xsl:template>

<xsl:template match="dcterms:creator">
  <xsl:apply-templates select="rdf:Seq/rdf:li/foaf:Person/foaf:name"/>
  <xsl:apply-templates select="rdf:Seq/rdf:li/foaf:Organization/foaf:name"/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li/foaf:Person/foaf:name">
    <xsl:value-of select="concat('; ',.)"/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li[1]/foaf:Person/foaf:name">
    <xsl:value-of select="."/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li[1]/foaf:Organization">
    <xsl:value-of select="foaf:name"/>
</xsl:template>

<xsl:template match="dcterms:title">
    <xsl:value-of select="."/>
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(text(),'isbn:')]">
    <xsl:value-of select="substring(.,6)"/>
</xsl:template>

<xsl:template match="dcterms:publisher">
    <xsl:value-of select="foaf:*/foaf:name"/>
    <xsl:value-of select="concat(' ',../dcterms:created)"/>
</xsl:template>

<xsl:template match="dcterms:extent">
    <xsl:value-of select="normalize-space(../../../../dcterms:extent/dcterms:SizeOrDuration)"/>
</xsl:template>

<xsl:template match="daia:label">
    <xsl:value-of select="."/>
</xsl:template>

<xsl:template match="*" priority="-1"/>
</xsl:stylesheet>
