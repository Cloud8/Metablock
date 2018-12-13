<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:fabio="http://purl.org/spar/fabio/"
     version="1.0">

<xsl:output method="html" encoding="UTF-8" indent="yes"
     doctype-public = "-//W3C//DTD HTML 4.0 Transitional//EN"
     doctype-system = "http://www.w3.org/TR/REC-html40/loose.dtd" />

<xsl:template match="rdf:RDF">
<HTML>
  <xsl:apply-templates select="dcterms:BibliographicResource" />
</HTML>
</xsl:template>

<xsl:template match="dcterms:BibliographicResource">
<HEAD>
<xsl:apply-templates select="dcterms:creator" mode="head"/>
<META NAME="DC.Title" LANG="{dcterms:language}"
      CONTENT="{dcterms:title[not(@xml:lang)]}"/>
<META NAME="DC.Identifier" SCHEME="URL" CONTENT="{@rdf:about}"/>
<TITLE>_INDEX.HTM</TITLE>
</HEAD>
<BODY>

<H2>Indexdatei</H2>
<TABLE CELLPADDING="2" CELLSPACING="4">
<TR>
  <TD VALIGN="top" ALIGN="right">Autor:</TD>
  <TD><xsl:apply-templates select="dcterms:creator" /></TD>
</TR>
<TR>
  <TD VALIGN="top" ALIGN="right">Titel:</TD>
  <TD><xsl:value-of select="dcterms:title[not(@xml:lang)]" /></TD>
</TR>
</TABLE>

<hr/>
<xsl:apply-templates select="dcterms:hasPart" />
</BODY>
</xsl:template>

<xsl:template match="dcterms:creator" mode="head">
  <xsl:apply-templates select="rdf:Seq/rdf:li" mode="head"/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li" mode="head">
<META NAME="DC.Creator.PersonalName" CONTENT="{foaf:Person/foaf:name}" />
</xsl:template>

<xsl:template match="dcterms:creator">
  <xsl:apply-templates select="rdf:Seq/rdf:li" />
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li">
  <xsl:value-of select="concat(', ',foaf:Person/foaf:name)"/>
</xsl:template>

<xsl:template match="dcterms:creator/rdf:Seq/rdf:li[1]">
  <xsl:value-of select="foaf:Person/foaf:name"/>
</xsl:template>


<xsl:template match="dcterms:hasPart">
  <xsl:apply-templates select="dctypes:Text[dcterms:format/dcterms:MediaTypeOrExtent/rdfs:label='application/pdf']"/>
  <xsl:apply-templates select="dctypes:Text[dcterms:format/dcterms:MediaTypeOrExtent/rdfs:label='text/html']"/>
  <xsl:apply-templates select="dctypes:Dataset"/>
</xsl:template>

<xsl:template match="dcterms:hasPart[dctypes:Text[dcterms:format/dcterms:MediaTypeOrExtent/rdfs:label='application/pdf']][1]">
  <a name="pdf"> Dokumente (PDF): </a>
  <table border="1" cellspacing="5px" cellpadding="2px">
  <tr> <th>Inhalt</th><th>Datei</th><th>Format</th><th>Kommentar</th> </tr>
  <xsl:for-each select="../dcterms:hasPart/dctypes:Text/dcterms:format/dcterms:MediaTypeOrExtent[rdfs:label='application/pdf']">
  <tr>
    <td><a href="{substring-after(../../@rdf:about,concat(../../../../@rdf:about,'/'))}"><xsl:value-of select="substring-after(../../@rdf:about,concat(../../../../@rdf:about,'/'))"/></a></td>
    <td> <xsl:value-of select="substring-after(../../@rdf:about,concat(../../../../@rdf:about,'/'))"/></td>
    <td><xsl:value-of select="rdfs:label"/></td>
    <td><xsl:value-of select="'Dokument'"/></td>
  </tr>
  </xsl:for-each>
  </table><hr/>
</xsl:template>

<xsl:template match="dcterms:hasPart[dctypes:Text[dcterms:format/dcterms:MediaTypeOrExtent/rdfs:label='text/html']][1]">
  <a name="data"> Dokumente (HTML): </a>
  <table border="1" cellspacing="5px" cellpadding="2px">
  <tr> <th>Inhalt</th><th>Datei</th><th>Format</th><th>Kommentar</th> </tr>
  <tr>
    <td><a href="{substring-after(dctypes:Text/@rdf:about,concat(../@rdf:about,'/'))}"><xsl:value-of select="substring-after(dctypes:Text/@rdf:about,concat(../@rdf:about,'/'))"/></a></td>
    <td><xsl:value-of select="substring-after(dctypes:Text/@rdf:about,concat(../@rdf:about,'/'))"/></td>
    <td><xsl:value-of select="dctypes:Text/dcterms:format/dcterms:MediaTypeOrExtent/rdfs:label"/></td>
    <td><xsl:value-of select="'Verzeichnis'"/></td>
  </tr>
  </table>
  <hr />
</xsl:template>

<xsl:template match="dctypes:Dataset[contains(@rdf:about,'data/data.zip')]">
  <a name="data"> Forschungsdaten: </a>
  <table border="1" cellspacing="5px" cellpadding="2px">
  <tr> <th>Inhalt</th><th>Datei</th><th>Format</th><th>Kommentar</th> </tr>
  <tr>
    <td><a href="{substring-after(@rdf:about,concat(../../@rdf:about,'/'))}"><xsl:value-of select="substring-after(@rdf:about,concat(../../@rdf:about,'/'))"/></a></td>
    <td><xsl:value-of select="substring-after(@rdf:about,concat(../../@rdf:about,'/'))"/></td>
    <td><xsl:value-of select="dcterms:format/dcterms:MediaTypeOrExtent/rdfs:label"/></td>
    <td><xsl:value-of select="'Forschungsdaten'"/></td>
  </tr>
  </table>
  <hr />
</xsl:template>

<xsl:template match="@*|node()" priority="-1">
</xsl:template>

</xsl:stylesheet>


