<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:nlm="http://dtd.nlm.nih.gov/publishing/2.3"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     version="1.0" >

<!--
  /** @license http://www.apache.org/licenses/LICENSE-2.0
    * @author Goetz Hatop <fb.com/goetz.hatop>
    * @title An XSLT Transformer for NLM to RDF
    * @date 2015-06-17
   **/ -->

<!-- <xsl:import href="nlm2rdf.xslt"/> -->
<!-- <xsl:import href="lib/xslt/nlm2rdf.xslt"/> -->
<xsl:include 
     href="file:///usr/local/vufind/local/autobib/lib/xslt/nlm2rdf.xslt"/>

<xsl:template match="nlm:article">
  <xsl:param name="uri">
  <xsl:choose>
   <xsl:when test="nlm:front/nlm:journal-meta/nlm:journal-id='0003'">
       <xsl:value-of select="'http://archiv.ub.uni-marburg.de/ep/0003'"/>
   </xsl:when>
   <xsl:otherwise>
       <xsl:value-of
      select="substring-before($base/nlm:self-uri[1]/@xlink:href,'/article')"/>
   </xsl:otherwise>
  </xsl:choose>
  </xsl:param>
 
 <rdf:RDF>
   <fabio:JournalArticle rdf:about="{concat($uri,'/',$year,'/',$seq,'/',$aid)}">
    <xsl:apply-templates select="nlm:front/nlm:article-meta">
        <xsl:with-param name="uri" select="$uri"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="nlm:front/nlm:journal-meta">
        <xsl:with-param name="uri" select="$uri"/>
    </xsl:apply-templates>
    <!-- <xsl:apply-templates select="nlm:body" /> -->
   </fabio:JournalArticle>
 </rdf:RDF>
</xsl:template>

<xsl:template match="nlm:self-uri[not(@content-type)]">
</xsl:template>

<xsl:template match="nlm:self-uri[@content-type='application/pdf'][@xlink:href]">
 <xsl:param name="uri"/>
  <xsl:choose>
  <xsl:when test="../../nlm:journal-meta/nlm:journal-id='0003'">
    <fabio:hasURL><xsl:value-of select="@xlink:href"/></fabio:hasURL>
    <dcterms:hasPart>
    <dctypes:Text rdf:about="{concat(substring-before(@xlink:href,
                   'view'), 'download',substring-after(@xlink:href,'view'))}">
      <dcterms:format><xsl:value-of select="@content-type"/></dcterms:format>
     </dctypes:Text>
    </dcterms:hasPart>
  </xsl:when>
  <xsl:otherwise>
    <dcterms:hasPart>
     <dctypes:Text rdf:about="{concat($uri,'/',$year,'/',$seq,'/',$aid,'/',$aid,'.pdf')}">
      <dcterms:format><xsl:value-of select="@content-type"/></dcterms:format>
     </dctypes:Text>
    </dcterms:hasPart>
 </xsl:otherwise>
 </xsl:choose>
 <foaf:img><xsl:value-of 
       select="concat($uri,'/',$year,'/',$seq,'/',$aid,'/',$aid,'.png')"/>
 </foaf:img>
</xsl:template>

<xsl:template match="nlm:publisher">
 <xsl:param name="uri"/>
 <xsl:choose>
 <xsl:when test="../nlm:journal-id='0002'"><!--suppress schueren--></xsl:when>
 <xsl:when test="../nlm:journal-id='0003'"><!--suppress CNMS --></xsl:when>
 <xsl:otherwise>
   <!--
   <dcterms:publisher>
     <foaf:Organization 
       rdf:about="{concat($uri,'/aut/',translate(.,' ,[].','_'))}">
      <foaf:name><xsl:value-of select="." /></foaf:name>
    </foaf:Organization>
   </dcterms:publisher>
   -->
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="nlm:journal-meta/nlm:journal-id">
  <xsl:comment><xsl:value-of select="."/></xsl:comment>
  <dcterms:publisher>
     <foaf:Organization rdf:about="http://d-nb.info/gnd/2001630-X">
     <foaf:name>Philipps-Universität Marburg</foaf:name></foaf:Organization>
  </dcterms:publisher>
  <dcterms:type><xsl:value-of select="'PeriodicalPart'"/></dcterms:type>
  <xsl:choose>

     <xsl:when test=".='0002'">
      <!-- NLM data has print-issn 1431-5262. This is the e-issn: -->
      <fabio:hasISSN><xsl:value-of select="'2196-4270'"/></fabio:hasISSN>
      <!-- ZDB-Idn MEDREZ : -->
      <fabio:hasIdentifier>
          <xsl:value-of select="'1465812-4'"/>
      </fabio:hasIdentifier>
      <dcterms:subject><!-- DDC 302.23 -->
         <skos:Concept rdf:about="http://dewey.info/class/302">
           <skos:prefLabel xml:lang="en">Social Interaction</skos:prefLabel>
         </skos:Concept>
      </dcterms:subject>
      <dcterms:publisher>
       <aiiso:Institute rdf:about="http://www.uni-marburg.de/fb09/medienwissenschaft">
        <foaf:name>Medienwissenschaft</foaf:name>
      </aiiso:Institute>
      </dcterms:publisher>
      <dcterms:publisher>
       <aiiso:Faculty rdf:about="http://www.uni-marburg.de/fb09">
        <foaf:name>Fachbereich Germanistik und Kunstwissenschaften</foaf:name>
       </aiiso:Faculty>
      </dcterms:publisher>
      <dcterms:language>de</dcterms:language>
      <dcterms:subject><!-- DDC 302.23 -->
         <skos:Concept rdf:about="http://dewey.info/class/302">
           <skos:prefLabel xml:lang="en">Social Interaction</skos:prefLabel>
         </skos:Concept>
      </dcterms:subject>
      <!-- rights ?
      <dcterms:license rdf:resource="https://creativecommons.org/licenses/by/4.0"/>
      -->
     </xsl:when>

     <xsl:when test=".='0003'">
      <!-- <fabio:hasISSN>2196-629X</fabio:hasISSN> -->
      <xsl:apply-templates select="nlm:issn"/>
      <!-- ZDB-Idn META : -->
      <fabio:hasIdentifier>
          <xsl:value-of select="'2714728-9'"/>
      </fabio:hasIdentifier>
      <dcterms:publisher>
        <aiiso:Center rdf:about="http://www.uni-marburg.de/cnms">
        <foaf:name>Center for Near and Middle Eastern Studies (CNMS)</foaf:name>
       </aiiso:Center>
      </dcterms:publisher>
      <dcterms:language>en</dcterms:language>
      <dcterms:subject>
         <skos:Concept rdf:about="http://dewey.info/class/956">
           <skos:prefLabel xml:lang="en">General history of Asia; Middle East</skos:prefLabel>
         </skos:Concept>
      </dcterms:subject>
      <dcterms:license rdf:resource="https://creativecommons.org/licenses/by/3.0"/>
     </xsl:when>

     <xsl:when test=".='0004'">
      <!-- <fabio:hasISSN>1612-2941</fabio:hasISSN> -->
      <xsl:apply-templates select="nlm:issn"/>
      <!-- ZDB-Idn MJR : -->
      <fabio:hasIdentifier>
          <xsl:value-of select="'1418722-x'"/>
      </fabio:hasIdentifier>
      <dcterms:publisher>
       <aiiso:Faculty rdf:about="http://www.uni-marburg.de/fb03">
        <foaf:name>Fachbereich Gesellschaftswissenschaften und Philosophie </foaf:name>
       </aiiso:Faculty>
      </dcterms:publisher>
      <dcterms:language>en</dcterms:language>
      <dcterms:subject>
         <skos:Concept rdf:about="http://dewey.info/class/200">
           <skos:prefLabel xml:lang="en">Philosophy and theory of religion</skos:prefLabel>
           <skos:prefLabel xml:lang="de">Religion</skos:prefLabel>
         </skos:Concept>
      </dcterms:subject>
     </xsl:when>

   </xsl:choose>
</xsl:template>

</xsl:stylesheet>
