<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:ore="http://www.openarchives.org/ore/terms/"
     version="1.0">

<xsl:output encoding="UTF-8" indent="yes"/>

<xsl:template match="rdf:RDF">
 <rdf:RDF>
  <xsl:apply-templates select="dcterms:BibliographicResource" />
 </rdf:RDF>
</xsl:template>

<xsl:template match="@*|node()" priority="-1">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
</xsl:template>

</xsl:stylesheet>


