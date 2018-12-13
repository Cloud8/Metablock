<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:urn="http://www.d-nb.de/standards/urn/"
     version="1.0">

<!-- UB Marburg 2013 / 2016 http://www.persistent-identifier.de/?link=210 -->
<xsl:output method="xml" encoding="UTF-8" indent="yes" />

<xsl:template match="rdf:RDF">
 <xsl:apply-templates select="dcterms:BibliographicResource[dcterms:hasPart]"/>
</xsl:template>

<!-- xmlns:epicur="urn:nbn:de:1111-2004033116" -->
<xsl:template match="dcterms:BibliographicResource">
 <epicur xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="urn:nbn:de:1111-2004033116"
         xsi:schemaLocation="urn:nbn:de:1111-2004033116 http://www.persistent-identifier.de/xepicur/version1.0/xepicur.xsd">

  <administrative_data>
    <delivery>
      <update_status type="urn_new"/>
    </delivery>
  </administrative_data>

  <record>
   <identifier scheme="urn:nbn:de">
      <xsl:value-of select="dcterms:identifier[starts-with(text(),'urn:')]" />
   </identifier>
   <resource>
    <identifier scheme="url" type="frontpage" role="primary">
     <xsl:choose><!-- meta journal -->
      <xsl:when test="contains(dcterms:source/@rdf:resource,'meta')">
       <xsl:value-of select="dcterms:source/@rdf:resource"/>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="@rdf:about" /></xsl:otherwise>
     </xsl:choose>
    </identifier>
    <format scheme="imt">text/html</format>
   </resource>
  </record>
 </epicur>
</xsl:template>

</xsl:stylesheet>
