<?xml version="1.0"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:fabio="http://purl.org/spar/fabio/"
     version="1.0" >

<xsl:output method="text" encoding="UTF-8" />

<xsl:template match="rdf:RDF">
  <xsl:apply-templates select="fabio:*" />
  <xsl:apply-templates select="dcterms:BibliographicResource" />
</xsl:template>

<xsl:template match="fabio:*|dcterms:BibliographicResource">
    <xsl:apply-templates select="dcterms:accessRights" />
</xsl:template>

<xsl:template match="dcterms:accessRights">
# Kunstgeschichte 137.248.8.122
# Vor- und Fruehgeschichte 137.248.8.172
# Mitarbeiter 137.248.216.
Require ip <xsl:value-of select="."/>
## Require ip 137.248.216. 127.0.0.
&lt;Files view.html>
  Require ip <xsl:value-of select="."/>
  Require ip 137.248.216. 127.0.0.
&lt;/Files>
&lt;Files mets*.xml>
  # dfg-viewer
  Require ip 194.95.145.62
&lt;/Files>
&lt;Files index.html>
  Require all granted
&lt;/Files>
&lt;Files cover.png>
  Require all granted
&lt;/Files>
&lt;Files &apos;&apos;>
  Require all granted
&lt;/Files>
</xsl:template>

</xsl:stylesheet>
