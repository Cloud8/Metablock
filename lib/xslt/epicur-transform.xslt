<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:urn="http://www.d-nb.de/standards/urn/"
     version="1.0">

<!--
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns="urn:nbn:de:1111-2004033116"
-->

<!-- UB Marburg 2013 -->
<xsl:output method="xml" encoding="UTF-8" indent="yes" />

<xsl:template match="rdf:RDF">
 <xsl:apply-templates select="fabio:*" />
</xsl:template>

<xsl:template match="fabio:*">
 <epicur xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns:epicur="urn:nbn:de:1111-2004033116"
         xsi:schemaLocation="urn:nbn:de:1111-2004033116 http://www.persistent-identifier.de/xepicur/version1.0/xepicur.xsd">

  <administrative_data>
    <delivery>
      <update_status type="urn_new"/>
    </delivery>
  </administrative_data>

  <record>
   <identifier scheme="urn:nbn:de">
      <xsl:value-of select="dct:identifier" />
   </identifier>
   <resource>
    <identifier scheme="url" type="frontpage" role="primary">
      <xsl:value-of select="@rdf:about" />
    </identifier>
    <format scheme="imt">text/html</format>
   </resource>
  </record>
 </epicur>
</xsl:template>

</xsl:stylesheet>


