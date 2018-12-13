<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:nlm="http://dtd.nlm.nih.gov/publishing/2.3"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     version="1.0" >

<xsl:output method="xml" indent="yes"/>

<xsl:template match="/">
  <xsl:apply-templates select="rdf:RDF/dcterms:BibliographicResource"/>
</xsl:template>

<xsl:template match="/rdf:RDF/dcterms:BibliographicResource">
  <item>
    <metadata>
      <value><xsl:value-of select="@rdf:about"/></value>
      <key>dcterms.identifier</key>
    </metadata>
    <xsl:apply-templates select="dcterms:*"/>
    <xsl:apply-templates select="dcterms:*" mode="dcterms" />
  </item>
</xsl:template>

<xsl:template match="dcterms:creator">
  <xsl:apply-templates select=".//foaf:Person/foaf:name"/>
</xsl:template>

<xsl:template match="dcterms:contributor">
  <xsl:apply-templates select=".//foaf:Person/foaf:name"/>
</xsl:template>

<xsl:template match="dcterms:creator//foaf:Person/foaf:name">
  <metadata>
    <value><xsl:value-of select="."/></value>
    <key>dc.contributor.author</key>
  </metadata>
  <metadata>
    <value><xsl:value-of select="."/></value>
    <key>dc.creator</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:contributor//foaf:Person/foaf:name">
  <metadata>
    <value><xsl:value-of select="."/></value>
    <key><xsl:value-of select="'dc.contributor.advisor'"/></key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:created">
  <metadata>
    <value><xsl:value-of select="."/></value>
    <key>dc.date.created</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:issued">
  <metadata>
    <value><xsl:value-of select="concat(.,'T08:18:27Z')"/></value>
    <key>dc.date.issued</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:dateAccepted">
  <metadata>
    <value><xsl:value-of select="concat(.,'T08:18:27Z')"/></value>
    <key>dc.date.accessioned</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:title">
  <metadata>
    <xsl:choose>
      <xsl:when test="@xml:lang='de'"><language>de_DE</language></xsl:when>
      <xsl:when test="@xml:lang='en'"><language>en_US</language></xsl:when>
      <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
    <value><xsl:value-of select="."/></value>
    <key>dc.title</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:type">
  <metadata>
    <value><xsl:value-of select="substring-after(@rdf:resource,'fabio/')"/>
    </value>
    <key>dc.type</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:subject">
  <xsl:apply-templates select="skos:Concept"/>
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept">
  <xsl:apply-templates select="skos:Concept"/>
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept/rdfs:label">
  <metadata>
    <value><xsl:value-of select="."/></value>
    <key>dc.subject</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:format">
  <metadata>
    <language>en_US</language>
    <value><xsl:value-of select="."/></value>
    <key>dc.format</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:language">
  <metadata>
    <value><xsl:value-of select="."/></value>
    <key>dc.language</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:publisher">
 <xsl:apply-templates select="foaf:Organization"/>
</xsl:template>

<xsl:template match="dcterms:publisher/foaf:Organization">
  <metadata>
    <language>de_DE</language>
    <value><xsl:value-of select="foaf:name"/></value>
    <key>dc.publisher</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:publisher">
 <xsl:apply-templates select="foaf:Organization"/>
</xsl:template>

<xsl:template match="dcterms:language[@rdf:resource]">
  <metadata>
    <value><xsl:value-of select="substring-after(@rdf:resource,'1/')"/></value>
    <key>dc.language</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:hasPart[@rdf:resource]">
  <metadata>
    <value><xsl:value-of select="@rdf:resource"/></value>
    <key>dcterms.hasPart</key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:*[count(*)>0]" priority="0">
  <xsl:comment><xsl:value-of select="concat('dc.',local-name())"/>
  </xsl:comment>
</xsl:template>

<!-- dcterms mode : copy if no child nodes -->
<xsl:template match="dcterms:*[count(*)=0]" mode="dcterms">
  <metadata>
    <!--<xsl:comment><xsl:value-of select="'copy no child'"/></xsl:comment>-->
    <xsl:if test="@xml:lang">
    <language><xsl:value-of select="@xml:lang"/></language>
    </xsl:if>
    <value><xsl:value-of select="."/></value>
    <key><xsl:value-of select="concat('dcterms.',local-name())"/></key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:*[@rdf:resource]" mode="dcterms">
  <metadata>
    <!--<xsl:comment><xsl:value-of select="'copy resource'"/></xsl:comment>-->
    <value><xsl:value-of select="@rdf:resource"/></value>
    <key><xsl:value-of select="concat('dcterms.',local-name())"/></key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:*[count(*)>0]" mode="dcterms">
  <xsl:apply-templates select="skos:Concept" mode="dcterms"/>
  <xsl:apply-templates select=".//foaf:Person/foaf:name" mode="dcterms"/>
</xsl:template>

<xsl:template match="dcterms:subjcet/skos:Concept" mode="dcterms">
  <xsl:comment><xsl:value-of select="'skos concept'"/></xsl:comment>
  <xsl:apply-templates select="rdfs:label" mode="dcterms"/>
  <xsl:apply-templates select="skos:prefLabel" mode="dcterms"/>
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept/rdfs:label" mode="dcterms">
  <metadata>
    <value><xsl:value-of select="."/></value>
    <key><xsl:value-of select="'dcterms.subject'"/></key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept/skos:prefLabel" mode="dcterms">
  <metadata>
    <value><xsl:value-of select="."/></value>
    <key><xsl:value-of select="'dcterms.subject'"/></key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:creator//foaf:Person/foaf:name" mode="dcterms">
  <!--<xsl:comment><xsl:value-of select="'dcterms creator'"/></xsl:comment>-->
  <metadata>
    <value><xsl:value-of select="."/></value>
    <key><xsl:value-of select="'dcterms.creator'"/></key>
  </metadata>
</xsl:template>

<xsl:template match="dcterms:contributor//foaf:Person/foaf:name" mode="dcterms">
  <metadata>
    <value><xsl:value-of select="."/></value>
    <key><xsl:value-of select="'dcterms.contributor'"/></key>
  </metadata>
</xsl:template>

<xsl:template match="@*|node()" priority="-1" mode="dcterms">
  <xsl:comment><xsl:value-of select="concat('dcterms.',local-name(),': ',.)"/>
  </xsl:comment>
  <!-- <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy> -->
</xsl:template>

<xsl:template match="@*|node()" priority="-1">
  <xsl:comment><xsl:value-of select="concat('zero.',local-name())"/>
  </xsl:comment>
</xsl:template>

</xsl:stylesheet>
