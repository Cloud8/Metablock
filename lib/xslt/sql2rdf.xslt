<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:nlp="http://localhost/"
     xmlns:dct="http://purl.org/dc/terms/"
     version="1.0" >

<xsl:output method="xml" indent="yes"/>

<xsl:template match="nlp:oid">
    <dct:identifier><xsl:value-of select="."/></dct:identifier>
</xsl:template>

<xsl:template match="@*|node()">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
</xsl:template>

</xsl:stylesheet>
