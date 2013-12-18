<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:frbr="http://purl.org/vocab/frbr/core#"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:daia="http://purl.org/ontology/daia/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:pica="http://www.ub.uni-marburg.de/webcat/card/ppn/"
     version="1.0">

<!--
 /**
  * @license http://www.apache.org/licenses/LICENSE-2.0
  * @author Goetz Hatop 
  * @title RDF Transformer for Pica format
  * @date 2013-10-20
 **/
 -->

<xsl:output method="xml" indent="yes" encoding="UTF-8" />
<xsl:strip-space elements="*"/>

<xsl:param name="webcat" select="'http://www.ub.uni-marburg.de/webcat/card/'"/>

<xsl:template match="/">
  <xsl:apply-templates select="records" />
</xsl:template>

<xsl:template match="records">
 <rdf:RDF>
  <xsl:comment> Pica2RDF Transformer UB Marburg (2013) </xsl:comment>
  <xsl:apply-templates select="record" />
 </rdf:RDF>
</xsl:template>

<xsl:template match="record">
  <xsl:param name="uri" select="concat($webcat,'ppn/',field[@key='003@']/x0)"/>
  <rdf:Description rdf:about="{$uri}">
    <xsl:apply-templates select="field">
      <xsl:with-param name="uri" select="$uri" />
    </xsl:apply-templates>

    <xsl:apply-templates select="item">
      <xsl:with-param name="uri" select="$uri" />
    </xsl:apply-templates>
  </rdf:Description>
</xsl:template>

<xsl:template match="field[@key]">
  <xsl:param name="uri" />
  <xsl:param name="tag" select="translate(translate(@key,'@','Y'),'/','_')"/>

  <xsl:element name="pica:f{$tag}">
  <xsl:choose>
  <xsl:when test="count(*)=1">
    <xsl:value-of select="*"/>
  </xsl:when>
  <xsl:otherwise>
     <pica:Sub rdf:about="{concat($uri,'#',$tag)}">
      <xsl:for-each select="*">
       <xsl:element name="pica:{name(.)}"><xsl:value-of select="."/>
       </xsl:element>
      </xsl:for-each>
     </pica:Sub>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:element>
</xsl:template>

<xsl:template match="item">
  <xsl:param name="uri" />
  <frbr:exemplar>
  <rdf:Description rdf:about="{concat($uri,'#',@num)}">
    <xsl:apply-templates select="field">
      <xsl:with-param name="uri" select="$uri" />
    </xsl:apply-templates>
  </rdf:Description>
  </frbr:exemplar>
</xsl:template>

<xsl:template match="@*|node()">
</xsl:template>

</xsl:stylesheet>
