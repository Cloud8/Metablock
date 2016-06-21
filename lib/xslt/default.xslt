<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="html" encoding="UTF-8" 
            standalone="no"
	        indent="yes"
	        doctype-public = "-//W3C//DTD HTML 4.0 Transitional//EN"
            doctype-system = "http://www.w3.org/TR/REC-html40/loose.dtd" />


<xsl:template match="/">
  <h4>Illegal URL</h4>
</xsl:template>

<xsl:template match="@*|node()">
  <xsl:apply-templates />
</xsl:template>

<xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
	<xsl:copy-of select="@*"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
</xsl:template>

</xsl:stylesheet>
