<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     exclude-result-prefixes="xsl rdf xsi foaf"
     version="1.0" >

<xsl:output method="xml" encoding="UTF-8" indent="yes" />

<xsl:template match="rdf:RDF">
   <xsl:apply-templates select="foaf:Person"/>
</xsl:template>

<xsl:template match="foaf:Person">
 <add>
   <doc>
   <field name="id">
     <xsl:value-of select="translate(
                           substring-after(@rdf:about,'/au/')
                           , '&lt;&gt;','')
                           " disable-output-escaping="yes"/>
   </field>
   <field name="source"><xsl:value-of select="'local'"/></field>
   <field name="record_type"><xsl:value-of select="'rdf'"/></field>
   <field name="allfields"><xsl:value-of select="."/></field>
   <!-- <field name="name"><xsl:value-of select="foaf:name"/></field> -->
   </doc>
 </add>
</xsl:template>

<!-- suppress emptyness -->
<xsl:template match="text()"/>

</xsl:stylesheet>

