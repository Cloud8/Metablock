<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:mets="http://www.loc.gov/METS/"
     xmlns:mods="http://www.loc.gov/mods/v3"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:dv="http://dfg-viewer.de/"
     version="1.0" >

<!-- 
 /**
  * @license http://www.apache.org/licenses/LICENSE-2.0
  * @title An XSLT Transformer for MODS to RDF for HLGL
  * @date 2017-04-12 2018-02-01
 **/ 
 -->

<xsl:param name="url" select="'http://www.example.com/1963'"/>
<xsl:param name="ppn" select="'1963'"/>
<xsl:output method="xml" indent="yes"/>

<xsl:template match="mets:mets">
  <xsl:apply-templates select="mets:dmdSec" />
</xsl:template>

<xsl:template match="mets:dmdSec">
 <rdf:RDF
     xmlns:oai="http://www.openarchives.org/OAI/2.0/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:dcterms="http://purl.org/dc/terms/">
  <dcterms:BibliographicResource rdf:about="{$url}">
    <xsl:apply-templates select="mets:mdWrap/mets:xmlData/mods:mods" />
    <xsl:apply-templates select="../mets:amdSec" />
    <xsl:apply-templates select="../mets:fileSec" />
    <dcterms:identifier>
        <xsl:value-of select="concat('rdf:',$ppn)"/>
    </dcterms:identifier>
    <dcterms:license rdf:resource="https://creativecommons.org/licenses/by-nc-nd/4.0/legalcode.de"/>
    <dcterms:type rdf:resource="http://purl.org/spar/fabio/Book"/>
    <dcterms:language rdf:resource="http://id.loc.gov/vocabulary/iso639-1/de"/>
    <dcterms:issued>2018-02-01</dcterms:issued>
    <foaf:img><xsl:value-of select="concat($url,'/cover.png')"/></foaf:img>
    <dcterms:hasPart>
     <dctypes:Text rdf:about="{concat($url,'/data/',$ppn,'.xml')}">
       <dcterms:format>
          <dcterms:MediaTypeOrExtent>
            <rdfs:label>application/xml</rdfs:label>
          </dcterms:MediaTypeOrExtent>
        </dcterms:format>
      </dctypes:Text>
    </dcterms:hasPart>
  </dcterms:BibliographicResource>
 </rdf:RDF>
</xsl:template>

<xsl:template match="mods:mods">
    <xsl:apply-templates select="mods:titleInfo" />
    <xsl:apply-templates select="mods:name" />
    <xsl:apply-templates select="mods:originInfo" />
</xsl:template>

<xsl:template match="mods:title">
  <dcterms:title><xsl:value-of select="."/></dcterms:title>
</xsl:template>

<xsl:template match="mods:name">
  <xsl:apply-templates select="mods:displayForm" />
</xsl:template>

<xsl:template match="mods:name/mods:displayForm">
  <dcterms:contributor>
     <foaf:Organization>
       <foaf:name><xsl:value-of select="."/></foaf:name>
     </foaf:Organization>
  </dcterms:contributor>
</xsl:template>

<xsl:template match="mods:originInfo">
  <xsl:apply-templates select="mods:place" />
  <xsl:apply-templates select="mods:dateIssued" />
</xsl:template>

<xsl:template match="mods:place">
  <xsl:apply-templates select="mods:placeTerm" />
</xsl:template>

<xsl:template match="mods:placeTerm">
  <dcterms:spatial><xsl:value-of select="."/></dcterms:spatial>
</xsl:template>

<xsl:template match="mods:dateIssued">
  <dcterms:created><xsl:value-of select="."/></dcterms:created>
</xsl:template>

<xsl:template match="mets:amdSec">
  <xsl:apply-templates select="mets:rightsMD/mets:mdWrap/mets:xmlData" />
</xsl:template>

<xsl:template match="mets:amdSec/mets:rightsMD/mets:mdWrap/mets:xmlData">
  <xsl:comment><xsl:value-of select="'amdSec'"/></xsl:comment>
  <xsl:apply-templates select="dv:rights" />
</xsl:template>

<xsl:template match="dv:rights">
  <dcterms:publisher>
    <foaf:Organization rdf:about="{dv:ownerSiteURL}">
      <foaf:name><xsl:value-of select="dv:owner"/></foaf:name>
      <foaf:mbox><xsl:value-of select="concat('mailto:',dv:ownerContact)"/>
      </foaf:mbox>
    </foaf:Organization>
  </dcterms:publisher>
</xsl:template>

<xsl:template match="mets:fileSec">
  <xsl:comment><xsl:value-of select="'fileSec'"/></xsl:comment>
  <dcterms:extent>
    <dcterms:SizeOrDuration rdf:about="{concat($url,'/Extent')}">
        <rdf:value>
        <xsl:value-of select="count(mets:fileGrp[1]/mets:file)"/>
        </rdf:value>
    </dcterms:SizeOrDuration>
  </dcterms:extent>
</xsl:template>

<xsl:template match="text()"/>
</xsl:stylesheet>

