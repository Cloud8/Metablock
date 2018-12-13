<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dct="http://purl.org/dc/terms/" 
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:mods="http://www.loc.gov/mods/v3"
     xmlns:mets="http://www.loc.gov/METS/"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     version="1.0">

<!-- GH2015-03 : create DSpace MODS : see config/crosswalks/mods.properties -->
<xsl:output encoding="UTF-8" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="rdf:RDF">
  <xsl:apply-templates select="fabio:*" />
  <xsl:apply-templates select="dcterms:BibliographicResource" />
</xsl:template>

<!-- mods embedded in mets : DSpace MODS requires PROFILE attribute -->
<xsl:template match="fabio:*|dcterms:BibliographicResource">
 <mets xmlns="http://www.loc.gov/METS/"
     xmlns:mods="http://www.loc.gov/mods/v3"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     PROFILE="DSpace METS SIP Profile 1.0"
     xsi:schemaLocation="http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd">

  <dmdSec ID="dmdSec_1">
   <mdWrap MIMETYPE="text/xml" MDTYPE="MODS">
    <xmlData>
      <mods:mods>
      <xsl:apply-templates select="dct:title"/>
      <xsl:apply-templates select="dct:creator"/>
      <xsl:apply-templates select="dct:abstract"/>
      <xsl:apply-templates select="dct:issued"/>
      <xsl:apply-templates select="dct:created"/>
      <xsl:apply-templates select="dct:contributor"/>
      <xsl:apply-templates select="dct:publisher"/>
      <xsl:apply-templates select="dct:subject"/>
      <xsl:apply-templates select="dct:identifier"/>
      <xsl:apply-templates select="dct:language"/>
      <xsl:apply-templates select="dct:source"/>
      <xsl:apply-templates select="dct:type"/>
      </mods:mods>
     </xmlData>
    </mdWrap>
   </dmdSec>

   <!-- file section -->
   <mets:fileSec>
     <mets:fileGrp ID="group-1" USE="CONTENT">
       <xsl:apply-templates select="dct:hasPart" />
       <xsl:apply-templates select="foaf:img" />
       <!-- inject myself, see SwordStorage -->
       <mets:file ID="meta_id" MIMETYPE="application/rdf+xml" SEQ="1">
         <mets:FLocat LOCTYPE="URL" xlink:type="simple" xlink:href="meta.rdf"/>
       </mets:file>
     </mets:fileGrp>
   </mets:fileSec>
   <mets:structMap ID="struct_11" LABEL="DSpace Object" TYPE="LOGICAL">
       <xsl:apply-templates select="dct:hasPart" mode="struct"/>
       <xsl:apply-templates select="foaf:img" mode="struct"/>
       <!-- inject myself -->
       <mets:div ID="meta_div" TYPE="FILE"><mets:fptr FILEID="meta_id"/>
       </mets:div>
   </mets:structMap>
 </mets>
</xsl:template>

<xsl:template match="dct:title[not(@xml:lang)]">
 <mods:titleInfo><mods:title xml:lang="{../dct:language}">
   <xsl:value-of select="."/>
 </mods:title></mods:titleInfo>
</xsl:template>

<xsl:template match="dct:title[@xml:lang]">
 <mods:titleInfo type="alternative">
    <mods:title xml:lang="{@xml:lang}"><xsl:value-of select="."/></mods:title>
 </mods:titleInfo>
</xsl:template>

<xsl:template match="dct:creator">
  <xsl:apply-templates select="foaf:Person"/>
  <xsl:apply-templates select="rdf:Seq/rdf:li/foaf:Person"/>
</xsl:template>

<xsl:template match="dct:creator/foaf:Person|dct:creator/rdf:Seq/rdf:li/foaf:Person">
 <mods:name type="personal">
  <mods:role>
    <mods:roleTerm type="text">author</mods:roleTerm>
  </mods:role>
  <mods:namePart>
    <xsl:value-of select="foaf:name" />
  </mods:namePart>
 </mods:name>
</xsl:template>

<xsl:template match="dct:contributor">
  <xsl:apply-templates select="foaf:Person"/>
</xsl:template>

<xsl:template match="dct:contributor/foaf:Person">
 <mods:name type="personal">
  <mods:role><mods:roleTerm type="text">advisor</mods:roleTerm></mods:role>
  <mods:namePart><xsl:value-of select="foaf:name" /></mods:namePart>
 </mods:name>
</xsl:template>

<xsl:template match="dct:abstract">
 <mods:abstract><xsl:value-of select="." /></mods:abstract>
</xsl:template>

<xsl:template match="dct:abstract[@xml:lang]">
 <mods:abstract xml:lang="{@xml:lang}">
   <xsl:value-of select="." />
 </mods:abstract>
</xsl:template>

<xsl:template match="dct:identifier[starts-with(text(),'urn:')]">
 <mods:identifier type="urn"><xsl:value-of select="."/></mods:identifier>
 <mods:identifier type="uri">
    <xsl:value-of select="../@rdf:about"/>
 </mods:identifier>
</xsl:template>

<xsl:template match="dct:identifier[starts-with(text(),'https://doi.org/')]">
   <mods:identifier type="uri">
     <xsl:value-of select="concat('https://doi.org/',../fabio:hasDOI)"/>
   </mods:identifier>
</xsl:template>

<xsl:template match="dct:issued">
 <mods:originInfo>
  <mods:dateIssued encoding="iso8601">
    <xsl:value-of select="." />
  </mods:dateIssued>
 </mods:originInfo>
</xsl:template>

<xsl:template match="dct:created">
 <mods:originInfo>
  <mods:dateCreated keyDate="yes" encoding="iso8601">
    <xsl:value-of select="." />
  </mods:dateCreated>
 </mods:originInfo>
</xsl:template>

<xsl:template match="dct:publisher">
  <xsl:apply-templates select="foaf:Organization"/>
</xsl:template>

<xsl:template match="dct:publisher/foaf:Organization">
 <mods:originInfo>
   <mods:place>
     <mods:placeTerm type="text">
        <xsl:value-of select="foaf:name"/>
     </mods:placeTerm>
   </mods:place>
 </mods:originInfo>
</xsl:template>

<xsl:template match="dct:subject[not(skos:Concept)]">
 <mods:subject><mods:topic>
  <xsl:value-of select="."/>
 </mods:topic></mods:subject>
</xsl:template>

<xsl:template match="dct:source">
 <mods:relatedItem type="original">
  <xsl:value-of select="."/>
 </mods:relatedItem>
</xsl:template>

<!-- DSpace : see config/crosswalks/mods-submission.xsl -->
<xsl:template match="dct:language">
 <mods:language><mods:languageTerm>
  <xsl:value-of select="."/>
 </mods:languageTerm></mods:language>
</xsl:template>

<!-- DSpace : see config/crosswalks/mods-submission.xsl -->
<xsl:template match="dct:type">
 <!-- dini publ type:
 <mods:genre><xsl:value-of select="."/></mods:genre>
 -->
 <mods:genre><xsl:value-of select="local-name(..)"/></mods:genre>
</xsl:template>

<xsl:template match="dct:subject[skos:Concept]">
  <xsl:apply-templates select="skos:Concept"/>
</xsl:template>

<!-- DSpace DDC : not accepted ? -->
<xsl:template match="dct:subject/skos:Concept">
  <mods:classification authority="ddc">
    <!--
    <xsl:value-of select="skos:prefLabel[@xml:lang='de']" />
    -->
   <xsl:value-of 
        select="substring-after(@rdf:about,'http://dewey.info/class/')"/>
  </mods:classification>
</xsl:template>

<!--
  <mets:fileGrp USE="LICENSE">
   <mets:file ID="bitstream_2" MIMETYPE="text/plain; charset=utf-8" SEQ="2" SIZE="11" CHECKSUM="88da92e059fb13512a7846a2b487efe0" CHECKSUMTYPE="MD5" GROUPID="GROUP_bitstream_2">
    <mets:FLocat LOCTYPE="URL" xlink:type="simple" xlink:href="license.txt"/>
   </mets:file>
  </mets:fileGrp>
-->
<xsl:template match="dct:hasPart[1]">
      <xsl:comment><xsl:value-of select="../@rdf:about"/></xsl:comment>
      <xsl:for-each select="self::dct:hasPart|following-sibling::dct:hasPart">
      <xsl:choose>
      <xsl:when test="contains(dct:Text/@rdf:about, '/All.pdf')">
      <xsl:comment>
        <xsl:value-of select="concat('#',position(),' ',dct:Text/@rdf:about)"/>
      </xsl:comment>
      <mets:file ID="{concat('file_',position())}" MIMETYPE="text/html">
        <mets:FLocat LOCTYPE="URL" xlink:type="simple" xlink:href="view.html"/>
      </mets:file>
      </xsl:when>
      <xsl:when test="dctypes:Text">
      <xsl:comment><xsl:value-of select="concat('#',position())"/></xsl:comment>
      <mets:file ID="{concat('file_',position())}" 
            MIMETYPE="{dctypes:Text/dct:format}" SEQ="1">
        <mets:FLocat LOCTYPE="URL" xlink:type="simple" 
              xlink:href="{substring-after(substring-after(dctypes:Text/@rdf:about,../@rdf:about),'/')}"/>
      </mets:file>
      </xsl:when>
      <xsl:otherwise>
      <xsl:comment>
          <xsl:value-of select="concat('#',position(),' ',@rdf:resource)"/>
      </xsl:comment>
      </xsl:otherwise>
      </xsl:choose>
      </xsl:for-each>
</xsl:template>

<xsl:template match="foaf:img[1]">
  <mets:file ID="cover_id" MIMETYPE="image/png" SEQ="1">
    <mets:FLocat LOCTYPE="URL" xlink:type="simple" 
        xlink:href="{substring-after(substring-after(.,../@rdf:about),'/')}"/>
  </mets:file>
</xsl:template>

<xsl:template match="dct:hasPart[1]" mode="struct">
  <mets:div ID="div_12" DMDID="dmdSec_1">
  <xsl:for-each select="self::dct:hasPart|following-sibling::dct:hasPart">
     <xsl:choose>
      <xsl:when test="contains(@rdf:resource, '/All.pdf')">
         <mets:div ID="{concat('div_',position()+1)}" TYPE="FILE">
            <mets:fptr FILEID="{concat('file_',position())}"/>
         </mets:div>
      </xsl:when>
      <xsl:when test="contains(@rdf:resource, '.pdf')">
         <mets:div ID="{concat('div_',position()+1)}" TYPE="FILE">
            <mets:fptr FILEID="{concat('file_',position())}"/>
         </mets:div>
      </xsl:when>
      <xsl:when test="dctypes:Text">
         <mets:div ID="{concat('div_',position()+1)}" TYPE="FILE">
            <mets:fptr FILEID="{concat('file_',position())}"/>
         </mets:div>
      </xsl:when>
     </xsl:choose>
   </xsl:for-each>
  </mets:div>
</xsl:template>

<xsl:template match="foaf:img[1]" mode="struct">
  <mets:div ID="cover_div" TYPE="FILE">
    <mets:fptr FILEID="cover_id"/>
  </mets:div>
</xsl:template>

</xsl:stylesheet>

