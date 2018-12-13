<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:solr="http://localhost:8983/solr/biblio/" 
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:sco="http://schema.org/"
     version="1.0" >

<!-- 
 /**
  * @author Meta Block
  * @title An XSLT Transformer for solr biblio core to RDF
  * @date 2017-12-21
 **/ 
 -->

<xsl:output method="xml" indent="yes" encoding="UTF-8" />

<xsl:template match="/">
    <xsl:apply-templates select="*" />
</xsl:template>

<xsl:template match="rdf:RDF">
 <rdf:RDF>
    <xsl:apply-templates select="dcterms:BibliographicResource" />
 </rdf:RDF>
</xsl:template>

<xsl:template match="dcterms:BibliographicResource">
  <dcterms:BibliographicResource rdf:about="{@rdf:about}">
    <xsl:apply-templates select="*" />
  </dcterms:BibliographicResource>
</xsl:template>

<xsl:template match="solr:title">
  <dcterms:title><xsl:value-of select="."/></dcterms:title>
</xsl:template>

<xsl:template match="solr:author[1]">
 <dcterms:creator>
 <rdf:Seq>
   <rdf:li>
       <foaf:Person><foaf:name><xsl:value-of select="."/></foaf:name>
       </foaf:Person>
   </rdf:li>
   <xsl:for-each select="following-sibling::solr:creator">
       <rdf:li>
       <foaf:Person><foaf:name><xsl:value-of select="."/></foaf:name>
       </foaf:Person>
       </rdf:li>
  </xsl:for-each>
 </rdf:Seq>
 </dcterms:creator>
</xsl:template>

<xsl:template match="solr:language">
 <dcterms:language>
  <xsl:choose>
   <xsl:when test=".='ger'">de</xsl:when>
   <xsl:when test=".='eng'">en</xsl:when>
   <xsl:when test=".='fre'">fr</xsl:when>
   <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
  </xsl:choose>
 </dcterms:language>
</xsl:template>

<xsl:template match="solr:publisher">
 <dcterms:publisher><xsl:value-of select="."/></dcterms:publisher>
</xsl:template>

<xsl:template match="solr:subject">
 <dcterms:subject>
   <skos:Concept>
      <skos:prefLabel><xsl:value-of select="."/></skos:prefLabel>
   </skos:Concept>
 </dcterms:subject>
</xsl:template>

<xsl:template match="solr:source">
 <dcterms:source><xsl:value-of select="."/></dcterms:source>
</xsl:template>

<xsl:template match="solr:description">
 <dcterms:abstract><xsl:value-of select="."/></dcterms:abstract>
</xsl:template>

<xsl:template match="solr:type[text()='journal']">
 <dcterms:type rdf:resource="http://purl.org/spar/fabio/Journal"/>
</xsl:template>

<xsl:template match="solr:type[text()='info:eu-repo/semantics/article']">
 <dcterms:type rdf:resource="http://purl.org/spar/fabio/JournalArticle"/>
</xsl:template>

<xsl:template match="solr:date[1]">
  <xsl:choose>
  <xsl:when test="contains(.,'T')">
    <dcterms:issued><xsl:value-of select="substring-before(.,'T')"/></dcterms:issued>
  </xsl:when>
  <xsl:otherwise>
    <dcterms:issued><xsl:value-of select="."/></dcterms:issued>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="solr:identifier[1]">
  <dcterms:identifier><xsl:value-of select="."/></dcterms:identifier>
</xsl:template>

<xsl:template match="solr:publishDate">
  <dcterms:created><xsl:value-of select="."/></dcterms:created>
</xsl:template>

<xsl:template match="solr:issn">
  <sco:issn><xsl:value-of select="."/></sco:issn>
</xsl:template>

<xsl:template match="solr:url">
  <dcterms:hasPart>
    <dctypes:Text rdf:about="{.}">
      <dcterms:format><dcterms:MediaTypeOrExtent>
        <rdfs:label>application/pdf</rdfs:label>
      </dcterms:MediaTypeOrExtent></dcterms:format>
    </dctypes:Text>
  </dcterms:hasPart>
</xsl:template>

<xsl:template match="solr:format">
  <xsl:choose>
  <xsl:when test=".='Journal Articles'">
    <dcterms:type rdf:resource="http://purl.org/spar/fabio/JournalArticle"/>
  </xsl:when>
  <xsl:when test=".='Dissertation'">
    <dcterms:type rdf:resource="http://purl.org/spar/fabio/DoctoralThesis"/>
  </xsl:when>
  <xsl:when test=".='Issue'">
    <dcterms:type rdf:resource="http://purl.org/spar/fabio/JournalIssue"/>
  </xsl:when>
  <xsl:otherwise>
    <dcterms:type rdf:resource="{concat('http://purl.org/spar/fabio/',translate(.,' ',''))}"/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="solr:hierarchytype"></xsl:template>
<xsl:template match="solr:callnumber-sort"></xsl:template>
<xsl:template match="solr:callnumber-label"></xsl:template>
<xsl:template match="solr:title_full-sort"></xsl:template>
<xsl:template match="solr:is_hierarchy_title-sort"></xsl:template>
<xsl:template match="solr:is_hierarchy_title-sort"></xsl:template>

<xsl:template match="@*|node()" priority="-1">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
</xsl:template>

</xsl:stylesheet>

