<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:sco="http://schema.org/"
     version="1.0" >

<xsl:output method="text" indent="yes"/>

<!-- 26 columns -->
<xsl:param name="columns" select="normalize-space(
            'title=?, subject_swd=?,description=?, 
            publisher_university=?, contributors_name=?,
            contributors_corporate=?, date_year=?, 
            date_creation=UNIX_TIMESTAMP(STR_TO_DATE(?,&quot;%Y-%m-%d&quot;)),
            date_modified=unix_timestamp(now()),
            bem_extern=?, source_title=?, 
            language=?, verification=?, subject_uncontrolled_german=?, 
            subject_uncontrolled_english=?, title_en=?, description2=?, 
            subject_type=?, date_valid=?, description_lang=?, 
            description2_lang=?, sachgruppe_ddc=?,
            bereich_id=?, lic=?, isbn=?, bem_intern=?')"/>

<xsl:template match="/">
  <xsl:apply-templates select="rdf:RDF/dcterms:BibliographicResource"/>
</xsl:template>

<xsl:template match="/rdf:RDF/dcterms:BibliographicResource">
  <xsl:variable name="oid" select="substring-after(sco:serialNumber,'opus:')"/>
  <xsl:variable name="type" select="'17'"/>
  <xsl:variable name="license" select="'4'"/>
  <!--
    '&quot;', dcterms:subject/skos:Concept[@rdf:about]/rdfs:label[not(@xml:lang)],'&quot;,',
  -->
  <xsl:variable name="params" select="concat(
    '&quot;', dcterms:title,       '&quot;,',
    '&quot;', dcterms:subject/skos:Concept/rdfs:label,'&quot;,',
    '&quot;', dcterms:abstract[@xml:language=../dcterms:language], '&quot;,',

    '&quot;', dcterms:publisher/foaf:Organization/foaf:name,   '&quot;,',
    '&quot;', dcterms:contributor/foaf:Person/foaf:name, '&quot;,',
    '&quot;', dcterms:contributor/foaf:Organization/foaf:name, '&quot;,',
    '&quot;', dcterms:created,'&quot;,',

    '&quot;', dcterms:issued,'&quot;,',
    '&quot;', $type,'&quot;,',
    '&quot;', dcterms:source,'&quot;,',

    '&quot;', dcterms:language,'&quot;,',
    '&quot;&quot;,',
    '&quot;', dcterms:subject/skos:Concept[not(@rdf:about)]/rdfs:label[@xml:lang='de'],'&quot;,',
    '&quot;', dcterms:subject/skos:Concept[not(@rdf:about)]/rdfs:label[@xml:lang='en'],'&quot;,',
    '&quot;', dcterms:alternative,'&quot;,',
    '&quot;', dcterms:abstract[not(@xml:language=dcterms:language)],'&quot;,',
    '&quot;&quot;,',
    '&quot;2024&quot;,',
    '&quot;ger&quot;,',
    '&quot;eng&quot;,',
    '&quot;ddc&quot;,',
    '&quot;6&quot;,',
    '&quot;',$license,'&quot;,',
    '&quot;&quot;,',
    '&quot;test&quot;'
    )"/>
    insert into opus set source_opus=<xsl:value-of select="$oid"/>,
    <xsl:value-of select="$columns"/>
    <xsl:value-of select="concat('[',$params,']')"/>
</xsl:template>

<xsl:template match="@*|node()" priority="-1">
</xsl:template>

</xsl:stylesheet>
