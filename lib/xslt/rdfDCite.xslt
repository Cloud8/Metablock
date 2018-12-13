<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/" 
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:dcite="http://datacite.org/schema/kernel-4"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:sco="http://schema.org/"
     exclude-result-prefixes="rdf rdfs dcterms dctypes foaf skos aiiso sco"
     version="1.0">

<!-- GH2015-02 : Datacite Metadata Schema v3 -->
<!-- GH2017-04 : Datacite Metadata Schema v4 -->
<!-- GH2018-01 : DataCite Metadata Schema v4.1 -->

<xsl:output encoding="UTF-8" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/">
  <xsl:apply-templates select="rdf:RDF" />
</xsl:template>

<xsl:template match="rdf:RDF">
  <!--[count(dcterms:identifier[contains(text(),'doi.org/')])=1]-->
  <xsl:apply-templates select="dcterms:BibliographicResource[count(dcterms:identifier[contains(text(),'doi.org/')])=1]"/>
</xsl:template>

<xsl:template match="dcterms:BibliographicResource">
   <dcite:resource xmlns="http://datacite.org/schema/kernel-4" 
      xsi:schemaLocation="http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.1/metadata.xsd" 
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <!-- 1. DOI Identifier -->
  <xsl:apply-templates select="dcterms:identifier[contains(text(),'doi.org/')]"/>

  <!-- 2. Creator : mandatory -->
  <dcite:creators>
     <xsl:choose>
     <xsl:when test="count(dcterms:creator)>0">
        <xsl:apply-templates select="dcterms:creator"/>
     </xsl:when>
     <xsl:when test="count(dcterms:isPartOf//dcterms:contributor)>0">
        <xsl:apply-templates select="dcterms:isPartOf//dcterms:contributor" mode="construct"/>
     </xsl:when>
     <xsl:otherwise> <!-- construct one -->
      <dcite:creator>
       <dcite:creatorName nameType="Organizational">
        <xsl:value-of select="dcterms:publisher//foaf:name"/>
       </dcite:creatorName>
      </dcite:creator>
     </xsl:otherwise>
     </xsl:choose>
  </dcite:creators>

   <!-- 3. Title -->
   <dcite:titles>
     <xsl:apply-templates select="dcterms:title"/>
     <xsl:apply-templates select="dcterms:alternative"/>
     <!-- container ?
     <xsl:apply-templates select="dcterms:isPartOf/dcterms:BibliographicResource/dcterms:title"/>
     -->
   </dcite:titles>

   <!-- 4. Publisher -->
   <xsl:apply-templates select="dcterms:publisher"/>
   <xsl:apply-templates select="dcterms:isPartOf//dcterms:publisher"/>

   <!-- 5. PublicationYear -->
   <xsl:apply-templates select="dcterms:created"/>

   <!-- 6. Subject -->
   <xsl:if test="count(dcterms:subject)>0">
     <dcite:subjects>
         <xsl:apply-templates select="dcterms:subject"/>
     </dcite:subjects>
   </xsl:if>

   <!-- 7. Contributor -->
   <xsl:if test="count(dcterms:contributor) + count(dcterms:isPartOf//dcterms:contributor)>0">
     <dcite:contributors>
       <xsl:apply-templates select="dcterms:contributor"/>
       <xsl:apply-templates select="dcterms:isPartOf//dcterms:contributor"/>
     </dcite:contributors>
   </xsl:if>

   <!-- 8. Date -->
   <xsl:if test="count(dcterms:modified)+count(dcterms:issued)>0">
     <dcite:dates>
         <xsl:apply-templates select="dcterms:modified"/>
         <xsl:apply-templates select="dcterms:issued"/>
     </dcite:dates>
   </xsl:if>

   <!-- 9. Language -->
   <xsl:apply-templates select="dcterms:language"/>
   
   <!-- 10. ResourceType -->
   <xsl:apply-templates select="dcterms:type"/>

   <!-- 11. AlternateIdentifier -->
   <dcite:alternateIdentifiers>
     <dcite:alternateIdentifier alternateIdentifierType="URL">
       <xsl:value-of select="concat('https:',substring-after(@rdf:about,':'))"/>
     </dcite:alternateIdentifier>
     <xsl:apply-templates select="dcterms:identifier[starts-with(text(),'urn:')]"/>
   </dcite:alternateIdentifiers>

   <!-- 12. RelatedIdentifier -->
   <xsl:if test="(count(dcterms:hasPart) + count(dcterms:isPartOf)>0)">
   <dcite:relatedIdentifiers>
     <xsl:apply-templates select="dcterms:hasPart[@rdf:resource]"/>
     <xsl:apply-templates select="dcterms:hasPart/dctypes:*"/>
     <xsl:apply-templates select="dcterms:isPartOf/dcterms:BibliographicResource[@rdf:about]/dcterms:identifier[contains(text(),'doi.org')]"/>
     <!-- No Cover ? --> <xsl:apply-templates select="foaf:img"/>
     <xsl:apply-templates select="dcterms:isPartOf//sco:issn"/>
   </dcite:relatedIdentifiers>
   </xsl:if>

   <!-- 13. Size -->

   <!-- 14. Format -->
   <xsl:if test="count(dcterms:hasPart//dcterms:format)>0">
   <dcite:formats>
     <xsl:apply-templates select="dcterms:hasPart//dcterms:format"/>
   </dcite:formats>
   </xsl:if>

   <!-- 15. Version -->

   <!-- 16. Rights -->
   <xsl:if test="count(dcterms:rights)+count(dcterms:license)>0">
      <dcite:rightsList>
          <xsl:apply-templates select="dcterms:rights"/>
          <xsl:apply-templates select="dcterms:license"/>
      </dcite:rightsList>
   </xsl:if>

   <!-- 17. Description -->
   <xsl:if test="count(dcterms:abstract)>0">
     <dcite:descriptions>
         <xsl:apply-templates select="dcterms:abstract"/>
         <xsl:apply-templates select="dcterms:tableOfContents"/>
     </dcite:descriptions>
   </xsl:if>

   <!-- 18. GeoLocation -->
   <!-- 19. FundingReference -->
</dcite:resource>
</xsl:template>

<xsl:template match="dcterms:identifier[contains(text(),'doi.org/')]">
  <dcite:identifier identifierType="DOI">
     <xsl:value-of select="substring-after(.,'doi.org/')"/>
  </dcite:identifier>
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(text(),'urn:')]">
  <dcite:alternateIdentifier alternateIdentifierType="URN">
    <xsl:value-of select="."/>
  </dcite:alternateIdentifier>
</xsl:template>

<xsl:template match="dcterms:creator">
  <xsl:apply-templates select="foaf:Person"/>
  <xsl:apply-templates select="rdf:Seq"/>
  <xsl:apply-templates select="foaf:Organization/foaf:name"/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq">
    <xsl:apply-templates select="rdf:li"/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li">
    <xsl:apply-templates select="foaf:Person"/>
</xsl:template>

<xsl:template match="dcterms:creator//foaf:Person">
  <dcite:creator>
    <xsl:apply-templates select="foaf:name"/>
    <!-- more specific data would require nameIdentifier like orcid or such
    <xsl:apply-templates select="foaf:familyName"/>
    <xsl:apply-templates select="foaf:givenName"/>
    -->
  </dcite:creator>
</xsl:template>

<xsl:template match="dcterms:creator/foaf:Organization/foaf:name">
  <dcite:creator>
   <dcite:creatorName nameType="Organizational">
    <xsl:value-of select="."/></dcite:creatorName>
  </dcite:creator>
</xsl:template>

<xsl:template match="dcterms:creator//foaf:Person/foaf:name">
   <dcite:creatorName nameType="Personal">
    <xsl:value-of select="."/></dcite:creatorName>
</xsl:template>

<xsl:template match="dcterms:creator//foaf:Person/foaf:familyName">
   <dcite:familyName><xsl:value-of select="."/></dcite:familyName>
</xsl:template>
<xsl:template match="dcterms:creator//foaf:Person/foaf:givenName">
   <dcite:givenName><xsl:value-of select="."/></dcite:givenName>
</xsl:template>

<xsl:template match="dcterms:contributor">
  <xsl:apply-templates select="foaf:Person"/>
  <xsl:apply-templates select="foaf:Organization/foaf:name"/>
  <xsl:apply-templates select="aiiso:Institute/foaf:name"/>
  <xsl:apply-templates select="aiiso:Center/foaf:name"/>
  <xsl:apply-templates select="aiiso:Division/foaf:name"/>
</xsl:template>

<xsl:template match="dcterms:contributor" mode="construct">
  <xsl:apply-templates select="*" mode="construct"/>
</xsl:template>

<xsl:template match="dcterms:contributor//foaf:Person">
  <dcite:contributor>
  <xsl:attribute name="contributorType">
   <xsl:choose>
    <xsl:when test="foaf:role='edt'">
        <xsl:value-of select="'Editor'"/>
    </xsl:when>
    <xsl:when test="foaf:role='trl'">
        <xsl:value-of select="'RelatedPerson'"/>
    </xsl:when>
    <xsl:when test="foaf:role='ths'">
        <xsl:value-of select="'Supervisor'"/>
    </xsl:when>
    <xsl:otherwise><xsl:value-of select="'Other'"/></xsl:otherwise>
   </xsl:choose>
   </xsl:attribute>
   <dcite:contributorName><xsl:value-of select="foaf:name"/>
   </dcite:contributorName>
  </dcite:contributor>
</xsl:template>

<xsl:template match="dcterms:contributor/foaf:Organization/foaf:name">
  <dcite:contributor contributorType="HostingInstitution">
   <dcite:contributorName><xsl:value-of select="."/>
   </dcite:contributorName>
  </dcite:contributor>
</xsl:template>

<xsl:template match="dcterms:contributor/aiiso:Center/foaf:name">
 <dcite:contributor contributorType="ResearchGroup">
   <dcite:contributorName><xsl:value-of select="."/></dcite:contributorName>
 </dcite:contributor>
</xsl:template>

<xsl:template match="dcterms:contributor/aiiso:Institute/foaf:name">
 <dcite:contributor contributorType="ResearchGroup">
   <dcite:contributorName><xsl:value-of select="."/></dcite:contributorName>
 </dcite:contributor>
</xsl:template>

<xsl:template match="dcterms:contributor/aiiso:Division/foaf:name">
 <dcite:contributor contributorType="Distributor">
   <dcite:contributorName><xsl:value-of select="."/></dcite:contributorName>
 </dcite:contributor>
</xsl:template>

<xsl:template match="dcterms:title">
  <xsl:choose>
  <xsl:when test="contains(../dcterms:type/@rdf:resource,'JournalIssue')">
    <dcite:title>
    <xsl:value-of select="concat(../dcterms:isPartOf//dcterms:title,' : ',.)"/>
    </dcite:title>
  </xsl:when>
  <xsl:when test="@xml:lang!=substring-after(../dcterms:language/@rdf:resource,'iso639-1/')">
    <dcite:title titleType="TranslatedTitle"><xsl:copy-of select="@xml:lang"/>
      <xsl:value-of select="."/>
    </dcite:title>
  </xsl:when>
  <xsl:when test="not(@xml:lang) and count(../dcterms:title[@xml:lang])>1">
  </xsl:when>
  <xsl:otherwise>
    <dcite:title><xsl:copy-of select="@xml:lang"/>
      <xsl:value-of select="."/>
    </dcite:title>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="dcterms:alternative">
  <dcite:title titleType="AlternativeTitle">
      <xsl:value-of select="."/>
  </dcite:title>
</xsl:template>

<!--
<xsl:template match="dcterms:isPartOf/dcterms:BibliographicResource/dcterms:title">
</xsl:template>
-->

<xsl:template match="dcterms:created">
  <dcite:publicationYear><xsl:value-of select="."/></dcite:publicationYear>
</xsl:template>

<xsl:template match="dcterms:modified">
 <dcite:date dateType="Updated"><xsl:value-of select="."/></dcite:date>
</xsl:template>

<xsl:template match="dcterms:issued">
 <dcite:date dateType="Issued"><xsl:value-of select="."/></dcite:date>
</xsl:template>

<xsl:template match="dcterms:dateAccepted">
 <dcite:date dateType="Accepted"><xsl:value-of select="."/></dcite:date>
</xsl:template>

<xsl:template match="dcterms:publisher[@rdf:resource]">
  <xsl:apply-templates select="../dcterms:isPartOf//dcterms:publisher"/>
</xsl:template>

<xsl:template match="dcterms:publisher">
  <xsl:apply-templates select="foaf:Organization"/>
</xsl:template>

<xsl:template match="dcterms:publisher/foaf:Organization">
 <dcite:publisher><xsl:value-of select="foaf:name"/></dcite:publisher>
</xsl:template>

<xsl:template match="dcterms:subject">
  <xsl:apply-templates select="skos:Concept"/>
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept">
  <xsl:apply-templates select="skos:prefLabel"/>
  <xsl:apply-templates select="rdfs:label"/>
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept/skos:prefLabel">
  <dcite:subject><xsl:value-of select="." /></dcite:subject>
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept/rdfs:label">
  <dcite:subject><xsl:value-of select="." /></dcite:subject>
</xsl:template>

<xsl:template match="dcterms:subject[@rdf:resource]">
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept/skos:prefLabel[@xml:lang='en']">
  <dcite:subject><xsl:value-of select="." /></dcite:subject>
</xsl:template>

<!--
  Allowed values are taken from IETF BCP 47, ISO 639‐1 language codes.
  Examples: en, de, fr 
  see http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
-->
<xsl:template match="dcterms:language[@rdf:resource]">
  <dcite:language><xsl:value-of select="substring-after(@rdf:resource,'iso639-1/')"/></dcite:language>
</xsl:template>

<!--
<xsl:template match="dcterms:language[not(@rdf:resource)]">
  <dcite:language><xsl:value-of select="." /></dcite:language>
</xsl:template>
-->

<xsl:template match="dcterms:type[@rdf:resource]">
  <xsl:variable name="general">
    <xsl:choose>
       <xsl:when test="contains(@rdf:resource, 'Issue')">
           <xsl:value-of select="'Collection'"/></xsl:when>
       <xsl:when test="contains(@rdf:resource, 'Image')">
           <xsl:value-of select="'Image'"/></xsl:when>
       <xsl:otherwise><xsl:value-of select="'Text'"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <dcite:resourceType resourceTypeGeneral="{$general}">
      <xsl:value-of select="substring-after(@rdf:resource,'/fabio/')"/>
  </dcite:resourceType>
</xsl:template>

<xsl:template match="dcterms:hasPart//dcterms:format">
  <xsl:apply-templates select="dcterms:MediaTypeOrExtent/rdfs:label"/>
</xsl:template>

<xsl:template match="dcterms:hasPart/dctypes:*[contains(@rdf:about,'All.pdf')]/dcterms:format">
</xsl:template>

<xsl:template match="dcterms:format/dcterms:MediaTypeOrExtent/rdfs:label">
  <dcite:format><xsl:value-of select="."/></dcite:format>
</xsl:template>

<xsl:template match="dcterms:hasPart[@rdf:resource]">
  <dcite:relatedIdentifier relatedIdentifierType="URL" relationType="HasPart">
    <xsl:value-of select="concat('https:',substring-after(@rdf:resource,':'))"/>
  </dcite:relatedIdentifier>
</xsl:template>

<xsl:template match="dcterms:hasPart[contains(@rdf:resource,'doi.org')]">
  <dcite:relatedIdentifier relatedIdentifierType="DOI" relationType="HasPart">
    <xsl:value-of select="@rdf:resource"/>
  </dcite:relatedIdentifier>
</xsl:template>

<!-- DCMI Type: Collection Dataset Event Image InteractiveResource MovingImage 
                PhysicalObject Service Software Sound StillImage Text
   DCite Types: Audiovisual Collection DataPaper Dataset Event Image 
                InteractiveResource Model PhysicalObject Service Software
                Sound Text Workflow Other
-->
<xsl:template match="dcterms:hasPart/dctypes:*[not(contains(@rdf:about,'All.pdf'))]">
  <dcite:relatedIdentifier relatedIdentifierType="URL" relationType="HasPart"
         resourceTypeGeneral="{local-name(.)}">
    <xsl:choose>
    <xsl:when test="dcterms:source[@rdf:resource]">
       <xsl:value-of select="dcterms:source/@rdf:resource"/>
    </xsl:when>
    <xsl:otherwise>
       <xsl:value-of select="@rdf:about"/>
    </xsl:otherwise>
    </xsl:choose>
  </dcite:relatedIdentifier>
</xsl:template>

<xsl:template match="dcterms:hasPart/dctypes:*[dcterms:format/dcterms:MediaTypeOrExtent/rdfs:label='application/xml']">
  <dcite:relatedIdentifier relatedIdentifierType="URL" relationType="HasPart"
         resourceTypeGeneral="{local-name(.)}">
    <xsl:value-of select="concat(../../@rdf:about,'/view.html')"/>
  </dcite:relatedIdentifier>
</xsl:template>

<xsl:template match="sco:issn">
  <dcite:relatedIdentifier relatedIdentifierType="ISSN" relationType="IsPartOf">
    <xsl:value-of select="."/>
  </dcite:relatedIdentifier>
</xsl:template>

<xsl:template match="dcterms:isPartOf">
  <xsl:apply-templates select="dcterms:BibliographicResource"/>
</xsl:template>

<xsl:template match="dcterms:isPartOf/dcterms:BibliographicResource/dcterms:identifier[contains(text(),'doi.org')]">
  <dcite:relatedIdentifier relatedIdentifierType="DOI" relationType="IsPartOf">
    <xsl:value-of select="."/>
  </dcite:relatedIdentifier>
</xsl:template>

<xsl:template match="dcterms:rights[@rdf:resource]">
  <dcite:rights rightsURI="{@rdf:resource}">
      <xsl:value-of select="@rdf:resource"/>
  </dcite:rights>
</xsl:template>

<xsl:template match="dcterms:rights[not(@rdf:resource)]">
  <dcite:rights><xsl:value-of select="."/></dcite:rights>
  <xsl:apply-templates select="../dcterms:isPartOf//dcterms:license"/>
</xsl:template>

<xsl:template match="dcterms:license[@rdf:resource]">
  <dcite:rights rightsURI="{@rdf:resource}">
      <xsl:value-of select="@rdf:resource"/>
  </dcite:rights>
</xsl:template>

<xsl:template match="dcterms:abstract[@xml:lang]">
 <dcite:description xml:lang="{@xml:lang}" descriptionType="Abstract">
    <xsl:value-of select="." />
 </dcite:description>
</xsl:template>

<xsl:template match="dcterms:abstract">
 <dcite:description descriptionType="Abstract">
    <xsl:value-of select="." />
 </dcite:description>
</xsl:template>

<xsl:template match="dcterms:tableOfContents">
  <xsl:apply-templates select="rdf:Seq"/>
</xsl:template>

<xsl:template match="dcterms:tableOfContents/rdf:Seq">
 <dcite:description descriptionType="TableOfContents">
  <xsl:text>&#xa;</xsl:text>
   <xsl:for-each select="rdf:li">
      <xsl:value-of select="concat(.,'&#xa;')" />
   </xsl:for-each>
 </dcite:description>
</xsl:template>

<xsl:template match="foaf:img">
  <dcite:relatedIdentifier relatedIdentifierType="URL" 
         resourceTypeGeneral="Image" relationType="IsDescribedBy">
    <xsl:value-of select="concat('https:',substring-after(.,':'))" />
  </dcite:relatedIdentifier>
</xsl:template>

<xsl:template match="dcterms:contributor/aiiso:Institute" mode="construct">
  <dcite:creator><dcite:creatorName nameType="Organizational">
    <xsl:value-of select="concat(../../dcterms:publisher/foaf:Organization/foaf:name, ', ', foaf:name)"/>
  </dcite:creatorName></dcite:creator>
</xsl:template>

<xsl:template match="dcterms:contributor/aiiso:Center" mode="construct">
  <dcite:creator><dcite:creatorName nameType="Organizational">
    <xsl:value-of select="concat(../../dcterms:publisher/foaf:Organization/foaf:name, ', ', foaf:name)"/>
  </dcite:creatorName></dcite:creator>
</xsl:template>

<xsl:template match="@*|node()" priority="-1">
    <!--<xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>-->
</xsl:template>

</xsl:stylesheet>

