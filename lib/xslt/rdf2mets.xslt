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
     xmlns:dv="http://dfg-viewer.de/"
     version="1.0">

<xsl:output encoding="UTF-8" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="rdf:RDF">
  <xsl:apply-templates select="fabio:*" />
</xsl:template>

<xsl:template match="fabio:*">
 <mets:mets 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:mets="http://www.loc.gov/METS/"
    xmlns:mods="http://www.loc.gov/mods/v3"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:dv="http://dfg-viewer.de/"
    xsi:schemaLocation="http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/version18/mets.xsd">

  <mets:dmdSec ID="md91196">
   <mets:mdWrap MIMETYPE="text/xml" MDTYPE="MODS">
    <mets:xmlData>
      <mods:mods>
      <xsl:apply-templates select="dct:title" />
      <xsl:apply-templates select="dct:creator" />
      <xsl:apply-templates select="dct:abstract" />
      <xsl:apply-templates select="dct:issued" />
      <xsl:apply-templates select="dct:created" />
      <xsl:apply-templates select="dct:publisher" />
      <xsl:apply-templates select="dct:subject" />
      <!-- <xsl:apply-templates select="dct:identifier" /> -->
      <mods:identifier type="url"><xsl:value-of select="@rdf:about"/>
      </mods:identifier>
      </mods:mods>
     </mets:xmlData>
    </mets:mdWrap>
   </mets:dmdSec>

   <mets:amdSec ID="amd91196">
    <mets:rightsMD ID="rights91196">
     <mets:mdWrap MIMETYPE="text/xml" MDTYPE="OTHER" OTHERMDTYPE="DVRIGHTS">
      <mets:xmlData>
       <dv:rights xmlns:dv="http://dfg-viewer.de/">
        <dv:owner>Universitätsbibliothek Marburg</dv:owner>
         <dv:ownerContact>mailto:auskunft@ub.uni-marburg.de</dv:ownerContact>
          <dv:ownerLogo>http://archiv.ub.uni-marburg.de/adm/img/unilogo-dfg.gif</dv:ownerLogo>
           <dv:ownerSiteURL>http://www.uni-marburg.de/bis</dv:ownerSiteURL>
       </dv:rights>
      </mets:xmlData>
     </mets:mdWrap>
    </mets:rightsMD>
    <mets:digiprovMD ID="digiprov91196">
     <mets:mdWrap MIMETYPE="text/xml" MDTYPE="OTHER" OTHERMDTYPE="DVLINKS">
      <mets:xmlData>
       <dv:links xmlns:dv="http://dfg-viewer.de/">
        <dv:reference><xsl:value-of select="@rdf:about" /></dv:reference>
        <dv:presentation><xsl:value-of select="@rdf:about" />
            </dv:presentation>
       </dv:links>
      </mets:xmlData>
     </mets:mdWrap>
    </mets:digiprovMD>
   </mets:amdSec>

   <xsl:apply-templates select="dct:hasFormat" />
 </mets:mets>
</xsl:template>

<xsl:template match="dct:title">
 <mods:titleInfo>
  <mods:title>
   <xsl:value-of select="substring-before(concat(.,':'),':')"/>
  </mods:title>
 </mods:titleInfo>
</xsl:template>

<xsl:template match="dct:creator">
  <xsl:apply-templates select="foaf:Person"/>
</xsl:template>

<xsl:template match="dct:creator/foaf:Person">
 <mods:name type="personal">
  <mods:namePart>
    <xsl:value-of select="foaf:name" />
  </mods:namePart>
  <mods:role>
    <roleTerm type="text">creator</roleTerm>
    <!-- <roleTerm authority="marcrelator" type="code">asn</roleTerm> -->
    <!-- <roleTerm type="text">Verfasser</roleTerm> -->
  </mods:role>
 </mods:name>
</xsl:template>

<xsl:template match="dct:abstract">
 <mods:abstract><xsl:value-of select="." /></mods:abstract>
</xsl:template>

<xsl:template match="dct:identifier">
 <mods:identifier type="urn"><xsl:value-of select="."/></mods:identifier>
</xsl:template>

<xsl:template match="dct:issued">
 <mods:recordCreationDate encoding="iso8601">
    <xsl:value-of select="." />
 </mods:recordCreationDate>
</xsl:template>

<xsl:template match="dct:created">
 <mods:dateCreated keyDate="yes" encoding="w3cdtf">
    <xsl:value-of select="." />
 </mods:dateCreated>
 <!-- <mods:dateIssued>[1697]</mods:dateIssued> -->
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

<xsl:template match="dct:subject">
  <xsl:apply-templates select="skos:Concept"/>
</xsl:template>

<xsl:template match="dct:subject/skos:Concept[skos:prefLabel]">
 <mods:subject authority="ddc">
  <xsl:apply-templates select="skos:prefLabel"/>
 </mods:subject>
</xsl:template>

<xsl:template match="dct:subject/skos:Concept/skos:prefLabel">
   <mods:topic><xsl:value-of select="." /></mods:topic>
</xsl:template>

<xsl:template match="dct:hasFormat">
    <xsl:apply-templates select="dctypes:Text"/><!-- monographs -->
    <xsl:apply-templates select="rdf:Seq"/>       <!-- periodical -->
</xsl:template>

<xsl:template match="dct:hasPart">
 <xsl:if test="contains(dctypes:Text/dct:format,'application/pdf')">
  <mets:fileGrp USE="DOWNLOAD">
    <mets:file ID="All" MIMETYPE="'application/pdf'">
      <mets:FLocat xlink:href="{dctypes:Text/@rdf:about}" LOCTYPE="URL"/>
    </mets:file>
  </mets:fileGrp>
 </xsl:if>
</xsl:template>

<!-- periodical -->
<xsl:template match="dct:hasFormat/rdf:Seq">
  <mets:fileSec>
      <xsl:apply-templates select="." mode="files"/>
  </mets:fileSec>
  <mets:structMap TYPE="PHYSICAL">
    <mets:div ID="phys91196" TYPE="physSequence">
      <xsl:apply-templates select="rdf:li" mode="physical"/>
    </mets:div>
  </mets:structMap>
  <mets:structMap TYPE="LOGICAL">
    <mets:div ID="log91196" TYPE="periodical" DMDID="md91196" ADMID="amd91196"
              LABEL="{../../dct:issued}">
      <xsl:apply-templates select="rdf:li/dctypes:Text" mode="logical"/>
    </mets:div>
  </mets:structMap>
  <mets:structLink>
      <mets:smLink xlink:from="log91196" xlink:to="phys91196"/>
      <xsl:for-each select="rdf:li/dctypes:Text">
        <xsl:variable name="path" select="substring-after(@rdf:about,'tif/')"/>
        <mets:smLink xlink:from="log_{$path}_1" xlink:to="phys_{$path}_1"/>
      </xsl:for-each>
  </mets:structLink>
</xsl:template>

<xsl:template match="dct:hasFormat/rdf:Seq" mode="files">
 <mets:fileGrp USE="DEFAULT">
  <xsl:for-each select="rdf:li/dctypes:Text">
    <xsl:comment><xsl:value-of select="position()"/></xsl:comment>
    <xsl:apply-templates select="dct:hasPart/rdf:Seq/rdf:li" mode="files">
      <xsl:with-param name="use" select="'def'"/>
      <xsl:with-param name="mimetype" select="'image/jpeg'" />
    </xsl:apply-templates>
  </xsl:for-each>
 </mets:fileGrp>

 <mets:fileGrp USE="MAX">
  <xsl:for-each select="rdf:li/dctypes:Text">
    <xsl:apply-templates select="dct:hasPart/rdf:Seq/rdf:li" mode="files">
      <xsl:with-param name="use" select="'max'" />
      <xsl:with-param name="mimetype" select="'image/jpeg'" />
    </xsl:apply-templates>
  </xsl:for-each>
 </mets:fileGrp>

 <mets:fileGrp USE="MIN">
  <xsl:for-each select="rdf:li/dctypes:Text">
    <xsl:apply-templates select="dct:hasPart/rdf:Seq/rdf:li" mode="files">
      <xsl:with-param name="use" select="'min'" />
      <xsl:with-param name="mimetype" select="'image/jpeg'" />
    </xsl:apply-templates>
  </xsl:for-each>
 </mets:fileGrp>

 <mets:fileGrp USE="THUMBS">
  <xsl:for-each select="rdf:li/dctypes:Text">
    <xsl:apply-templates select="dct:hasPart/rdf:Seq/rdf:li" mode="files">
      <xsl:with-param name="use" select="'pre'" />
      <xsl:with-param name="mimetype" select="'image/jpeg'" />
    </xsl:apply-templates>
  </xsl:for-each>
 </mets:fileGrp>

 <mets:fileGrp USE="DOWNLOAD">
  <xsl:for-each select="rdf:li/dctypes:Text">
    <xsl:apply-templates select="dct:hasPart/rdf:Seq/rdf:li" mode="tif"/>
  </xsl:for-each>
 </mets:fileGrp>
</xsl:template>

<xsl:template match="dct:hasFormat/rdf:Seq/rdf:li" mode="physical">
    <xsl:apply-templates select="dctypes:Text/dct:hasPart/rdf:Seq/rdf:li" 
         mode="physical"/>
</xsl:template>

<!-- monograph -->
<xsl:template match="dct:hasFormat/dctypes:Text">
  <mets:fileSec>
      <xsl:apply-templates select="dct:hasPart/rdf:Seq" mode="files"/>
      <xsl:apply-templates select="../../dct:hasPart"/>
  </mets:fileSec>

  <mets:structMap TYPE="PHYSICAL">
    <mets:div ID="phys91196" TYPE="physSequence">
      <xsl:apply-templates select="dct:hasPart/rdf:Seq" mode="physical"/>
    </mets:div>
  </mets:structMap>

  <mets:structMap TYPE="LOGICAL">
    <mets:div ID="log91196" TYPE="monograph" DMDID="md91196" ADMID="amd91196">
     <mets:fptr FILEID="All"/>
    </mets:div>
  </mets:structMap>
  <mets:structLink>
      <mets:smLink xlink:from="log91196" xlink:to="phys91196"/>
  </mets:structLink>
</xsl:template>

<!-- monograph -->
<xsl:template match="dctypes:Text/dct:hasPart/rdf:Seq" mode="files">
 <mets:fileGrp USE="DEFAULT">
  <xsl:apply-templates select="rdf:li" mode="files">
      <xsl:with-param name="use" select="'def'"/>
      <xsl:with-param name="mimetype" select="'image/jpeg'" />
  </xsl:apply-templates>
 </mets:fileGrp>
 <mets:fileGrp USE="MAX">
  <xsl:apply-templates select="rdf:li" mode="files">
      <xsl:with-param name="use" select="'max'" />
      <xsl:with-param name="mimetype" select="'image/jpeg'" />
  </xsl:apply-templates>
 </mets:fileGrp>
 <mets:fileGrp USE="MIN">
  <xsl:apply-templates select="rdf:li" mode="files">
      <xsl:with-param name="use" select="'min'" />
      <xsl:with-param name="mimetype" select="'image/jpeg'" />
  </xsl:apply-templates>
 </mets:fileGrp>
 <mets:fileGrp USE="THUMBS">
  <xsl:apply-templates select="rdf:li" mode="files">
      <xsl:with-param name="use" select="'pre'" />
      <xsl:with-param name="mimetype" select="'image/jpeg'" />
  </xsl:apply-templates>
 </mets:fileGrp>
 <mets:fileGrp USE="DOWNLOAD">
  <xsl:apply-templates select="rdf:li" mode="tif"/>
 </mets:fileGrp>
</xsl:template>

<xsl:template match="dct:hasPart/rdf:Seq" mode="physical">
     <xsl:apply-templates select="rdf:li" mode="physical" />
</xsl:template>

<xsl:template match="rdf:li/dctypes:Text" mode="logical">
  <xsl:variable name="path" select="substring-before(.,'/')"/>
  <mets:div ID="log_{$path}_1" TYPE="Issue" LABEL="{dct:title}" >
     <mets:fptr FILEID="phys_{$path}_1"></mets:fptr>
  </mets:div>
</xsl:template>

<xsl:template match="rdf:li" mode="files">
   <xsl:param name="mimetype" />
   <xsl:param name="use" />
   <xsl:variable name="path" select="substring-before(., '/')"/>
   <!--
   <xsl:variable name="img" 
       select="concat(substring-before(substring-after(.,'/'),'.tif'),'.jpg')"/>
   -->
   <xsl:variable name="chk" select="substring-after(., 'tif/')"/>
   <xsl:variable name="img"> 
       <xsl:choose>
       <xsl:when test="starts-with(., 'tif/')">
            <xsl:value-of select="substring(concat(substring-before(.,'.tif'),'.jpg'),4)"/>
       </xsl:when>
       <xsl:otherwise>
            <xsl:value-of select="concat(substring-before(.,'.tif'),'.jpg')"/>
       </xsl:otherwise>
       </xsl:choose>
   </xsl:variable>
   <mets:file ID="{$use}_{$path}_{position()}" MIMETYPE="{$mimetype}">
      <mets:FLocat xlink:href="{substring-before(../../../@rdf:about,'/tif')}/jpg/{$use}/{$img}" LOCTYPE="URL"/>
   </mets:file>
</xsl:template>

<xsl:template match="dct:hasPart/rdf:Seq/rdf:li" mode="tif">
  <xsl:variable name="path" select="substring-before(.,'/')"/>
    <mets:file ID="tif_{$path}_{position()}" MIMETYPE="image/tiff">
    <mets:FLocat xlink:href="{concat(../../../@rdf:about,'/',substring-after(.,'/'))}" LOCTYPE="URL"/>
  </mets:file>
</xsl:template>

<xsl:template match="dct:hasPart/rdf:Seq/rdf:li" mode="physical">
  <xsl:variable name="order" select="count(../../../../preceding-sibling::rdf:li/dctypes:Text/dct:hasPart/rdf:Seq/rdf:li)"/>
  <xsl:variable name="path" select="substring-before(.,'/')"/>
  <mets:div ID="phys_{$path}_{position()}" ORDER="{count(preceding-sibling::rdf:li) + 1 + $order}" TYPE="page" >
         <mets:fptr FILEID="def_{$path}_{position()}"></mets:fptr>
         <mets:fptr FILEID="min_{$path}_{position()}"></mets:fptr>
         <mets:fptr FILEID="max_{$path}_{position()}"></mets:fptr>
         <mets:fptr FILEID="pre_{$path}_{position()}"></mets:fptr>
         <mets:fptr FILEID="tif_{$path}_{position()}"></mets:fptr>
  </mets:div>
</xsl:template>

<xsl:template match="*" priority="-1"/>
</xsl:stylesheet>

