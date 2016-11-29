<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:daia="http://purl.org/ontology/daia#"
     xmlns:pica="http://localhost/metacard/card/ppn/"
     version="1.0" >

<xsl:output method="html" indent="yes"/>

<xsl:template match="/">
  <p>
  <table width="100%">
    <xsl:apply-templates select="rdf:RDF" />
  </table>
  </p>
</xsl:template>

<xsl:template match="rdf:RDF">
  <xsl:apply-templates select="dcterms:BibliographicResource" />
</xsl:template>


<xsl:template match="dcterms:BibliographicResource[pica:context]">
  <xsl:variable name="abt" 
    select="substring-after(pica:context[starts-with(text(),'abt:')],'abt:')"/>
  <xsl:variable name="lss" 
    select="substring-after(pica:context[starts-with(text(),'lss:')],'lss:')"/>
    <xsl:comment> abt <xsl:value-of select="$abt"/> lss <xsl:value-of select="$lss"/> </xsl:comment>
    <xsl:apply-templates select="dcterms:medium/rdf:Seq/rdf:li/dcterms:PhysicalMedium[contains(dcterms:spatial/dcterms:Location/@rdf:about,$abt)][pica:f209B=$lss]"/>
</xsl:template>

<xsl:template match="dcterms:BibliographicResource[not(pica:context)]">
  <xsl:apply-templates select="dcterms:medium" />
  <xsl:apply-templates select="dcterms:hasPart" />
</xsl:template>

<xsl:template match="dcterms:medium">
    <xsl:apply-templates select="rdf:Seq/rdf:li/dcterms:PhysicalMedium"/>
</xsl:template>

<!-- container -->
<xsl:template match="dcterms:hasPart[@rdf:resource]">
</xsl:template>

<xsl:template match="dcterms:hasPart[dctypes:*]">
    <tr><td></td><td></td></tr>
    <tr><td>
         <xsl:value-of select="position()"/>
         </td><td align="right">
    <a href="{concat('http://nbn-resolving.de/urn/resolver.pl?urn=',
              ../dcterms:identifier[starts-with(text(),'urn:')])}">
    <xsl:value-of select="../dcterms:identifier[starts-with(text(),'urn:')]"/>
    </a></td></tr>

    <xsl:apply-templates select="../dcterms:creator"/>
    <xsl:apply-templates select="../dcterms:title"/>
    <xsl:apply-templates select="../dcterms:publisher"/>
    <xsl:apply-templates select="../dcterms:extent"/>
    <xsl:apply-templates select="../dcterms:created"/>
    <xsl:apply-templates select="../dcterms:identifier[starts-with(text(),'isbn:')]"/>
    <xsl:apply-templates select="../dcterms:isPartOf"/>
    <xsl:apply-templates select="../dcterms:coverage"/>
    <xsl:apply-templates select="dctypes:Text"/>
    <tr class="border"><td></td><td></td></tr>
</xsl:template>

<xsl:template match="dcterms:hasPart[contains(dctypes:Text/@rdf:about,'All.pdf')]">
</xsl:template>

<xsl:template match="dctypes:Text">
  <tr><td></td><td align="right">
    <a href="{@rdf:about}"><xsl:value-of select="@rdf:about"/></a>
  </td></tr>
</xsl:template>

<xsl:template match="dcterms:PhysicalMedium">

    <!-- Signatur -->
    <tr><td><xsl:value-of select="count(../../../../preceding-sibling::*) + 1"/>.<xsl:value-of select="pica:number"/>
    </td><td align="right"><xsl:value-of select="rdfs:label"/></td></tr>

    <!-- Metadata -->
    <xsl:apply-templates select="../../../../dcterms:creator"/>
    <xsl:apply-templates select="../../../../dcterms:title[1]"/>
    <xsl:apply-templates select="../../../../dcterms:publisher"/>
    <xsl:apply-templates select="../../../../dcterms:extent"/>
    <xsl:apply-templates select="../../../../dcterms:created"/>
    <xsl:apply-templates select="../../../../dcterms:identifier[starts-with(text(),'isbn:')]"/>
    <xsl:apply-templates select="../../../../dcterms:isPartOf"/>
    <xsl:apply-templates select="../../../../dcterms:coverage"/>
    <xsl:apply-templates select="dcterms:identifier[starts-with(text(),'inv:')]"/>
    <tr><td></td><td align="right"><code>
        <xsl:apply-templates select="../../../../dcterms:identifier[starts-with(text(),'num:')]"/>
        <xsl:value-of select="concat(' ',translate(../../../../dcterms:identifier[starts-with(text(),'ppn:')],':',' '))"/>
    </code></td></tr>
    <tr><td></td><td><hr/></td></tr>
</xsl:template>

<xsl:template match="dcterms:creator">
  <tr><td></td><td>
    <xsl:apply-templates select="rdf:Seq/rdf:li"/>
  </td></tr>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li[1]">
  <xsl:value-of select="foaf:*/foaf:name"/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li[position()>1]">
  <xsl:value-of select="concat(' ; ', foaf:*/foaf:name)"/>
</xsl:template>

<xsl:template match="dcterms:title">
    <tr><td></td><td align="left">
    <b>
         <xsl:value-of select="."/>
         <xsl:apply-templates select="../dcterms:alternative"/>
    </b>
    </td></tr>
</xsl:template>

<xsl:template match="dcterms:created">
    <tr><td></td><td align="left">
         <xsl:value-of select="."/>
    </td></tr>
</xsl:template>

<xsl:template match="dcterms:alternative">
    <xsl:value-of select="concat(' ',.)"/>
</xsl:template>

<xsl:template match="dcterms:isPartOf[count(*)=0]">
    <tr><td></td><td>
         <xsl:value-of select="."/>
    </td></tr>
</xsl:template>

<xsl:template match="dcterms:publisher">
    <tr><td></td><td>
         <xsl:value-of select="foaf:*/foaf:name"/>
         <xsl:value-of select="concat(' ',../dcterms:created)"/>
    </td></tr>
</xsl:template>

<xsl:template match="dcterms:extent">
    <tr><td></td><td>
         <xsl:value-of select="dcterms:SizeOrDuration/rdf:value"/>
    </td></tr>
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(.,'isbn:')][1]">
    <tr><td></td><td>
         <xsl:value-of select="'ISBN '"/>
         <xsl:for-each select="../dcterms:identifier[starts-with(.,'isbn:')]">
            <xsl:value-of select="concat(substring(.,6),' ')"/>
         </xsl:for-each>
    </td></tr>
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(.,'num:')][1]">
    <xsl:value-of select="concat(substring(.,5),' ')"/>
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(.,'inv:')][1]">
  <tr><td></td><td>
    <xsl:value-of select="substring(.,5)"/>
  </td></tr>
</xsl:template>

<!-- Dissertationen Vermerk -->
<xsl:template match="dcterms:coverage">
    <tr><td></td><td><xsl:value-of select="."/></td></tr>
</xsl:template>

<!-- Schriftenreihen -->
<!-- 028C : kann mehrfach besetzt sein; erster Autor reicht hier
<xsl:template match="dcterms:Author">
    <tr><td></td><td>
          <xsl:choose>
            <xsl:when test="../field[@key='028A']">
              <xsl:apply-templates select="../field[@key='028A']" />
            </xsl:when>
            <xsl:when test="../field[@key='028C']">
              <xsl:apply-templates select="../field[@key='028C'][1]" />
            </xsl:when>
            <xsl:when test="../field[@key='021A']/b">
                 <xsl:apply-templates select="../field[@key='021A']/b" />
            </xsl:when>
            <xsl:when test="../field[@key='029A']">
              <xsl:apply-templates select="../field[@key='029A']" />
            </xsl:when>
            <xsl:when test="../field[@key='029F']">
              <xsl:apply-templates select="../field[@key='029F']" />
            </xsl:when>
            <xsl:when test="../field[@key='028F']">
              <xsl:apply-templates select="../field[@key='028F']" />
            </xsl:when>
          </xsl:choose>
    </td></tr>
</xsl:template>
-->

<!-- 036C : Mehrteiliges Werk: Grosser F-Satz + e-Satz
     021B : Mehrteiliges Werk: Grosser F-Satz -->
<!--
<xsl:template match="dcterms:Title">
    <tr><td></td><td align="left">
            <xsl:apply-templates select="../field[@key='036C']" />
            <xsl:apply-templates select="../field[@key='036C/01']" />
            <xsl:apply-templates select="../field[@key='036C/02']" />
    </td></tr>
    <tr><td></td><td align="left">
            <xsl:apply-templates select="../field[@key='021A']" />
    </td></tr>
    <tr><td></td><td align="left">
            <xsl:apply-templates select="../field[@key='021B']" />
    </td></tr>
    <tr><td></td><td align="left">
            <xsl:apply-templates select="../field[@key='036F']" />
    </td></tr>
</xsl:template>
-->

<!--
    <tr><td></td><td>
        <xsl:apply-templates select="../field[@key='036E']" />
    </td></tr>
-->

<!-- Inventarnummer -->
<!--
    <tr><td></td><td>
        <xsl:apply-templates select="field[@key='209C']" />
    </td></tr>
-->

<!-- Zeitschriften -->
<!--
    <tr><td></td><td>
        <xsl:apply-templates select="field[@key='209E'][position()=1]" />
    </td></tr>
-->

<!-- Sonderstandorte -->
<!--
    <tr><td></td><td>
        <xsl:value-of select="field[@key='209G']" />
    </td></tr>
-->

<!-- ppn bar -->
<!--
    <tr><td></td><td>
            <tt>
            ppn <xsl:apply-templates select="../field[@key='003@']" />
            bar <xsl:apply-templates select="field[@key='201@']" />
            </tt>
    </td></tr>
-->

<!--
<xsl:template match="field[@key='036E']">
  <xsl:value-of select="concat(a,' ; ',l)"/>
</xsl:template>
-->

<xsl:template match="*" priority="-1"/>
</xsl:stylesheet>
