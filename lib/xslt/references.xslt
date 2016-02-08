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
     xmlns:frbr="http://purl.org/vocab/frbr/core#"
     xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
     xmlns:ore="http://www.openarchives.org/ore/terms/"
     xmlns:c4o="http://purl.org/spar/c4o/"
     version="1.0" >

<!-- 
  <xsl:import href="seaview.xslt"/>
  <xsl:include 
     href="file:///usr/local/vufind/local/autobib/lib/xslt/seaview.xslt"/>
-->
  <xsl:import href="lib/xslt/seaview.xslt"/> 

<xsl:template match="rdf:RDF">
 <add>
   <!--<xsl:comment>Seaview RDF Transformer (2015)</xsl:comment>-->
   <xsl:apply-templates select="dct:BibliographicResource" />
   <xsl:apply-templates select="dct:BibliographicResource/dct:references" mode="refs"/>
   <xsl:apply-templates select="fabio:*" />
   <xsl:apply-templates select="fabio:*/dct:references" mode="refs"/>
 </add>
</xsl:template>

<!-- REFERENCES : core extension GH20150529 -->
<xsl:template match="/rdf:RDF/dct:BibliographicResource[dct:identifier]">
 <xsl:comment>Seaview Bibliographic Reference Transformer (2016)</xsl:comment>
 <doc>
    <field name="allfields"><xsl:for-each select="dct:*">
        <xsl:value-of select="concat(' ', normalize-space(text()))"/>
    </xsl:for-each></field>
    <xsl:apply-templates select="dct:*"/>
    <xsl:apply-templates select="foaf:img"/>
    <xsl:apply-templates select="fabio:hasDOI"/>
    <xsl:apply-templates select="fabio:hasISSN"/>
    <xsl:apply-templates select="." mode="spec"/>
    <field name="series"><xsl:value-of select="dct:identifier"/></field>
 </doc>
</xsl:template>

<xsl:template match="dct:BibliographicResource/dct:references" mode="refs">
  <xsl:comment> References </xsl:comment>
  <xsl:apply-templates select="rdf:Seq/rdf:li/dct:BibliographicResource" mode="refs"/>
</xsl:template>

<!-- Make a record if c4o:hasContext -->
<xsl:template match="rdf:Seq/rdf:li/dct:BibliographicResource[c4o:hasContext]" mode="refs">
 <xsl:param name="text" 
      select="substring-after(substring-after(@rdf:about,'//'),'/')"/>
 <doc>
  <field name="id"><xsl:value-of select="translate($text, translate($text, 
         'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzüäö', ''), '')"/>
  </field>
  <field name="recordtype">opus</field>
  <field name="series"><xsl:value-of select="../../../../dct:identifier"/></field>
  <field name="format"><xsl:value-of select="'Reference'"/></field>
  <xsl:apply-templates select="dct:title" mode="refs"/>
  <xsl:apply-templates select="dct:date" mode="refs"/>
  <xsl:apply-templates select="dct:creator" mode="refs"/>
  <xsl:apply-templates select="c4o:hasContext" mode="refs"/>
  <xsl:apply-templates select="dct:bibliographicCitation" mode="refs"/>
  <xsl:apply-templates select="dct:isReferencedBy" mode="refs"/>
 </doc>
</xsl:template>

<!-- no record without context -->
<xsl:template match="rdf:Seq/rdf:li/dct:BibliographicResource[not(c4o:hasContext)]" mode="refs">
</xsl:template>

<xsl:template match="dct:BibliographicResource/dct:bibliographicCitation[1]" mode="refs">
  <field name="description"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dct:BibliographicResource/dct:title[1]" mode="refs">
  <field name="title"><xsl:value-of select="." /></field>
  <field name="title_short"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="dct:creator" mode="refs">
  <xsl:apply-templates select="foaf:Person/foaf:name"/>
  <xsl:apply-templates select="rdf:Seq/rdf:li/foaf:Person/foaf:name"/>
  <xsl:apply-templates select="rdf:Seq/rdf:li[1][@rdf:resource]"/>
</xsl:template>

<xsl:template match="dct:creator/rdf:Seq/rdf:li[@rdf:resource]">
  <xsl:variable name="resource">
    <xsl:value-of select="@rdf:resource"/>
  </xsl:variable>
  <xsl:apply-templates select="//*/foaf:Person[@rdf:about=$resource]"/>
</xsl:template>

<xsl:template match="dct:BibliographicResource/dct:creator[position()>1]" mode="refs">
  <field name="author2"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dct:BibliographicResource/dct:date[1]" mode="refs">
  <field name="publishDate"><xsl:value-of select="." /></field>
  <field name="publishDateSort"><xsl:value-of select="." /></field>
</xsl:template>

<xsl:template match="dct:BibliographicResource/c4o:hasContext" mode="refs">
  <field name="contents"><xsl:value-of select="."/></field>
</xsl:template>

<xsl:template match="dct:BibliographicResource/c4o:hasInTextCitationFrequency" mode="refs">
</xsl:template>

<xsl:template match="dct:BibliographicResource/c4o:hasSentiment" mode="refs">
    <field name="genre"><xsl:value-of select="." /></field>
    <field name="genre_facet"><xsl:value-of select="." /></field>
</xsl:template>

</xsl:stylesheet>
