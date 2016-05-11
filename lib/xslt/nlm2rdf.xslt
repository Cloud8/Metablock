<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:nlm="http://dtd.nlm.nih.gov/publishing/2.3"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     version="1.0" >

<!--
 /** @license http://www.apache.org/licenses/LICENSE-2.0
   * @title NLM to RDF Transformer
   * @date 2014-06-05
 **/ 
 -->

<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

<xsl:template match="/">
  <xsl:apply-templates select="*" />
</xsl:template>

<xsl:template match="nlm:article">
  <xsl:apply-templates select="nlm:front" />
</xsl:template>

<xsl:template match="nlm:article/nlm:front">
  <xsl:param name="year" select="
       nlm:article-meta/nlm:pub-date[@pub-type='collection']/nlm:year"/>
  <xsl:param name="seq" select="
       nlm:article-meta/nlm:issue-id[@pub-id-type='other']"/>
  <xsl:param name="aid" select="
       nlm:article-meta/nlm:article-id[@pub-id-type='other']"/>
  <xsl:param name="uri" select="substring-before(
       nlm:article-meta/nlm:self-uri/@xlink:href,'/article')"/>

 <rdf:RDF>
   <dcterms:BibliographicResource rdf:about="{concat($uri,'/',$year,'/',$seq,'/',$aid)}">
    <xsl:apply-templates select="nlm:journal-meta">
        <xsl:with-param name="uri" select="$uri"/>
        <xsl:with-param name="seq" select="$seq"/>
        <xsl:with-param name="year" select="$year"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="nlm:article-meta">
        <xsl:with-param name="uri" select="$uri"/>
    </xsl:apply-templates>
   </dcterms:BibliographicResource>
 </rdf:RDF>
</xsl:template>

<xsl:template match="nlm:article-meta">
    <xsl:param name="uri"/>
    <dcterms:type rdf:resource="http://purl.org/spar/fabio/JournalArticle"/>
    <xsl:apply-templates select="nlm:title-group"/>
    <xsl:apply-templates select="nlm:contrib-group">
        <xsl:with-param name="uri" select="$uri"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="nlm:pub-date[@pub-type='collection']" />
    <xsl:apply-templates select="nlm:pub-date[@pub-type='epub']" />
    <xsl:apply-templates select="nlm:article-categories"/>
    <xsl:apply-templates select="nlm:kwd-group" />
    <xsl:apply-templates select="nlm:permissions" />
    <xsl:apply-templates select="nlm:abstract" />
    <xsl:apply-templates select="nlm:self-uri">
        <xsl:with-param name="uri" select="$uri"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="nlm:article-id" />
    <xsl:apply-templates select="nlm:issue-id" />
    <xsl:apply-templates select="nlm:issue" />
    <xsl:apply-templates select="nlm:volume" />
</xsl:template>

<xsl:template match="nlm:self-uri[not(@content-type)]">
  <xsl:param name="uri"/>
</xsl:template>

<xsl:template match="nlm:self-uri[@content-type='text/html']">
  <xsl:param name="uri"/>
</xsl:template>

<xsl:template match="nlm:self-uri[@content-type='application/pdf'][@xlink:href]">
  <dcterms:source rdf:resource="{@xlink:href}"/>
  <dcterms:hasPart>
    <dctypes:Text rdf:about="{concat(substring-before(@xlink:href,
                   'view'), 'download',substring-after(@xlink:href,'view'))}">
       <dcterms:format><dcterms:MediaTypeOrExtent>
         <rdfs:label><xsl:value-of select="@content-type"/></rdfs:label>
       </dcterms:MediaTypeOrExtent></dcterms:format>
    </dctypes:Text>
  </dcterms:hasPart>
</xsl:template>

<xsl:template match="nlm:article-meta/nlm:title-group">
  <xsl:apply-templates select="nlm:article-title"/>
  <xsl:apply-templates select="nlm:trans-title"/>
</xsl:template>

<xsl:template match="nlm:article-meta/nlm:title-group/nlm:article-title">
  <dcterms:title><xsl:value-of select="."/></dcterms:title>
</xsl:template>

<xsl:template match="nlm:article-meta/nlm:title-group/nlm:trans-title">
  <xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
  <dcterms:language rdf:resource="{concat('http://www.lexvo.org/id/iso639-1/',
                        translate(@xml:lang,$uppercase,$smallcase))}"/>
</xsl:template>

<xsl:template match="nlm:article-categories">
  <xsl:apply-templates select="nlm:subj-group"/>
</xsl:template>

<xsl:template match="nlm:article-categories/nlm:subj-group">
  <xsl:apply-templates select="nlm:subject"/>
</xsl:template>

<xsl:template match="nlm:article-categories/nlm:subj-group/nlm:subject">
  <dcterms:subject><skos:Concept>
    <rdfs:label><xsl:value-of select="."/></rdfs:label>
   </skos:Concept></dcterms:subject>
</xsl:template>

<xsl:template match="nlm:contrib-group">
 <xsl:param name="uri"/>
 <dcterms:creator>
  <rdf:Seq>
    <xsl:apply-templates select="nlm:contrib[@contrib-type='author']">
        <xsl:with-param name="uri" select="$uri"/>
    </xsl:apply-templates>
  </rdf:Seq>
 </dcterms:creator>
</xsl:template>

<xsl:template match="nlm:contrib-group/nlm:contrib[@contrib-type='author']">
 <xsl:param name="uri"/>
 <xsl:variable name="aut">
   <xsl:call-template name="string-generic">
     <xsl:with-param name="text" select="nlm:name"/>
   </xsl:call-template>
 </xsl:variable>
  <rdf:li>
    <foaf:Person rdf:about="{concat($uri,'/aut/',$aut)}">
      <xsl:apply-templates select="nlm:name"/>
    </foaf:Person>
  </rdf:li>
</xsl:template>

<xsl:template match="nlm:contrib-group/nlm:contrib/nlm:name">
  <xsl:apply-templates select="nlm:given-names"/>
  <xsl:apply-templates select="nlm:surname"/>
  <foaf:name>
    <xsl:value-of select="nlm:given-names"/><xsl:text> </xsl:text>
    <xsl:value-of select="nlm:surname"/>
  </foaf:name>
  <xsl:apply-templates select="nlm:aff"/>
</xsl:template>

<xsl:template match="nlm:contrib-group/nlm:contrib/nlm:name/nlm:given-names">
  <foaf:givenName><xsl:value-of select="."/></foaf:givenName>
</xsl:template>

<xsl:template match="nlm:contrib-group/nlm:contrib/nlm:name/nlm:surname">
  <foaf:familyName><xsl:value-of select="."/></foaf:familyName>
</xsl:template>

<xsl:template match="nlm:contrib-group/nlm:contrib/nlm:name/nlm:aff">
  <!-- better term ? -->
  <foaf:plan><xsl:value-of select="."/></foaf:plan>
</xsl:template>

<xsl:template match="nlm:abstract">
  <dcterms:abstract><xsl:value-of select="." /></dcterms:abstract>
</xsl:template>

<xsl:template match="nlm:publisher">
 <xsl:param name="uri"/>
 <xsl:variable name="aut">
   <xsl:call-template name="string-generic">
     <xsl:with-param name="text" select="."/>
   </xsl:call-template>
 </xsl:variable>
 <dcterms:publisher>
  <foaf:Organization rdf:about="{concat($uri,'/aut/',$aut)}">
      <foaf:name><xsl:value-of select="." /></foaf:name>
  </foaf:Organization>
 </dcterms:publisher>
</xsl:template>

<xsl:template match="nlm:body">
</xsl:template>

<xsl:template match="nlm:permissions">
  <xsl:apply-templates select="nlm:copyright-statement"/>
  <xsl:apply-templates select="nlm:license"/>
</xsl:template>

<xsl:template match="nlm:permissions/nlm:copyright-statement">
   <dcterms:rights><xsl:value-of select="."/></dcterms:rights>
</xsl:template>

<xsl:template match="nlm:permissions/nlm:license">
  <xsl:if test="@href!=''">
   <dcterms:license rdf:resource="{@href}"></dcterms:license>
  </xsl:if>
</xsl:template>

<xsl:template match="nlm:journal-meta">
 <xsl:param name="uri"/>
 <xsl:param name="seq"/>
 <xsl:param name="year"/>
  <dcterms:isPartOf>
    <dcterms:BibliographicResource rdf:about="{concat($uri,'/',$year,'/',$seq)}">
      <dcterms:type rdf:resource="http://purl.org/spar/fabio/JournalIssue"/>
      <xsl:choose>
      <xsl:when 
        test="../nlm:article-meta/nlm:volume and ../nlm:article-meta/nlm:issue">
        <dcterms:title>
          <xsl:value-of select="concat('Vol. ',../nlm:article-meta/nlm:volume, 
          ' No. ',../nlm:article-meta/nlm:issue, ' (', $year ,')')"/>
        </dcterms:title>
      </xsl:when>
      <xsl:when test="../nlm:article-meta/nlm:volume">
        <dcterms:title>
          <xsl:value-of select="concat('Vol. ',
              ../nlm:article-meta/nlm:volume, ' (', $year ,')')"/>
        </dcterms:title>
      </xsl:when>
      <xsl:when test="../nlm:article-meta/nlm:issue">
        <dcterms:title><xsl:value-of select="concat($year, ', ', ../nlm:article-meta/nlm:issue)"/></dcterms:title>
      </xsl:when>
      <xsl:otherwise>
      </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="../nlm:article-meta/nlm:issue-title"/>
      <xsl:apply-templates select="../nlm:article-meta/nlm:issue-id"/>
      <xsl:apply-templates select="../nlm:article-meta/nlm:pub-date"/>
      <dcterms:isPartOf>
        <dcterms:BibliographicResource rdf:about="{$uri}">
          <dcterms:type rdf:resource="http://purl.org/spar/fabio/Journal"/>
          <xsl:apply-templates select="nlm:journal-title"/>
          <xsl:apply-templates select="nlm:journal-id"/>
          <xsl:apply-templates select="nlm:publisher">
            <xsl:with-param name="uri" select="$uri"/>
          </xsl:apply-templates>
        </dcterms:BibliographicResource>
      </dcterms:isPartOf>
  </dcterms:BibliographicResource>
 </dcterms:isPartOf>
</xsl:template>

<xsl:template match="nlm:journal-meta/nlm:journal-id">
  <xsl:comment><xsl:value-of select="."/></xsl:comment>
  <xsl:apply-templates select="../nlm:issn"/>
</xsl:template>

<xsl:template match="nlm:journal-meta/nlm:journal-title">
  <dcterms:title><xsl:value-of select="."/></dcterms:title>
</xsl:template>

<xsl:template match="nlm:journal-meta/nlm:issn">
  <dcterms:identifier>
    <xsl:value-of select="concat('issn:',.)"/>
  </dcterms:identifier>
</xsl:template>

<xsl:template match="nlm:article-meta/nlm:volume">
  <dcterms:identifier>
    <xsl:value-of select="concat('vol:',.)"/>
  </dcterms:identifier>
</xsl:template>

<!-- issue sequence number -->
<xsl:template match="nlm:article-meta/nlm:issue">
  <xsl:if test="count(../nlm:volume)=0">
    <dcterms:identifier>
      <xsl:value-of select="concat('vol:',.)"/>
    </dcterms:identifier>
  </xsl:if>
</xsl:template>

<!-- internal article identifier -->
<xsl:template match="nlm:article-meta/nlm:article-id[@pub-id-type='other']">
  <dcterms:identifier>
    <xsl:value-of select="concat('ojs:',.)"/>
  </dcterms:identifier>
</xsl:template>

<xsl:template match="nlm:article-meta/nlm:issue-title">
  <dcterms:title><xsl:value-of select="."/></dcterms:title>
</xsl:template>

<xsl:template match="nlm:article-meta/nlm:article-id[@pub-id-type='doi']">
  <dcterms:identifier>
    <xsl:value-of select="concat('http://dx.doi.org/',.)"/>
  </dcterms:identifier>
</xsl:template>

<xsl:template match="nlm:pub-date[@pub-type='collection']">
  <xsl:apply-templates select="nlm:year"/>
</xsl:template>

<xsl:template match="nlm:pub-date[@pub-type='collection']/nlm:year">
  <dcterms:created><xsl:value-of select="."/></dcterms:created>
</xsl:template>

<xsl:template match="nlm:pub-date[@pub-type='epub']">
 <dcterms:issued>
    <xsl:value-of select="concat(nlm:year,'-',nlm:month,'-',nlm:day)"/>
 </dcterms:issued>
</xsl:template>

<xsl:template match="nlm:kwd-group">
  <xsl:apply-templates select="nlm:kwd" />
</xsl:template>

<xsl:template match="nlm:kwd[text()!='']">
  <dcterms:subject><skos:Concept>
     <rdfs:label><xsl:value-of select="."/></rdfs:label>
  </skos:Concept></dcterms:subject>
</xsl:template>

<xsl:template name="string-generic">
 <xsl:param name="text" select="."/>
 <xsl:value-of select="translate($text, translate($text, 
      'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzüäö', ''),'')"/>
</xsl:template>

<xsl:template match="text()"/>
</xsl:stylesheet>
