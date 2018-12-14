<?xml version="1.0"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:fabio="http://purl.org/spar/fabio/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     xmlns:aiiso="http://purl.org/vocab/aiiso/schema#"
     xmlns:skos="http://www.w3.org/2004/02/skos/core#"
     xmlns:void="http://rdfs.org/ns/void#"
     xmlns:sco="http://schema.org/"
     version="1.0" >

<xsl:output method="html" indent="yes"/>

<xsl:param name="lang" select="substring-after(rdf:RDF//dcterms:language/@rdf:resource,'iso639-1/')"/>

<xsl:template match="rdf:RDF">
  <xsl:apply-templates select="dcterms:BibliographicResource" />
</xsl:template>

<xsl:template match="dcterms:BibliographicResource">
 <html>
  <head>
    <xsl:call-template name="head"/>
    <xsl:call-template name="style"/>
    <title><xsl:value-of select="dcterms:title[@xml:lang=$lang]"/></title>
  </head>

  <body>
    <xsl:call-template name="body"/>
  </body>
 </html>
</xsl:template>

<xsl:template name="body">
   <div class="header">
    <a href="{../@rdf:about}">
        <xsl:value-of select="dcterms:mediator/dcterms:Agent/rdfs:label"/>
    </a>
   </div>
   <hr/>

    <table summary="Metadata" class="metadata">
    <xsl:apply-templates select="dcterms:title[@xml:lang=$lang]"/>

    <xsl:choose>
    <xsl:when test="count(dcterms:creator//foaf:Person)=0">
        <xsl:comment><xsl:value-of select="'foaf:Organization'"/></xsl:comment>
        <tr><td><i><b>Urheber:</b></i></td><td>
            <xsl:value-of select="dcterms:creator/foaf:Organization/foaf:name"/>
        </td></tr>
    </xsl:when>
    <xsl:otherwise>
        <tr><td><i><b>Autor:</b></i></td><td>
            <xsl:value-of select="dcterms:creator/foaf:Person/foaf:name"/>
            <xsl:value-of 
             select="dcterms:creator/rdf:Seq/rdf:li[1]/foaf:Person/foaf:name"/>
        </td></tr>
    </xsl:otherwise>
    </xsl:choose>

    <xsl:if test="count(dcterms:creator/rdf:Seq/rdf:li)>1">
        <tr><td width="20px"><em>Weitere Verfasser:</em></td><td>
        <xsl:for-each select="dcterms:creator/rdf:Seq/rdf:li[position()>1]">
            <xsl:value-of select="foaf:Person/foaf:name"/>
            <xsl:if test="position()!=last()"><xsl:value-of select="'; '"/>
            </xsl:if>
        </xsl:for-each>
        </td></tr>
    </xsl:if>

    <xsl:if test="count(dcterms:contributor//foaf:*/foaf:name)>0">
        <tr><td><em>Weitere Beteiligte:</em></td><td>
        <xsl:for-each select="dcterms:contributor//foaf:*/foaf:name">
            <xsl:comment><xsl:value-of select="position()"/></xsl:comment>
            <xsl:if test="position()!=1"><xsl:value-of select="'; '"/></xsl:if>
            <xsl:value-of select="."/>
        </xsl:for-each>
        </td></tr>
    </xsl:if>

    <xsl:apply-templates select="dcterms:issued"/>
    <xsl:apply-templates select="dcterms:publisher"/>

    <tr><td><i><b>URI:</b></i></td><td>
      <a href="{@rdf:about}">
      <xsl:value-of select="@rdf:about"/>
      </a>
    </td></tr>

    <xsl:apply-templates select="dcterms:identifier"/>
    <xsl:apply-templates select="sco:issn"/>
    <xsl:apply-templates select="sco:isbn"/>

    <xsl:if test="dcterms:subject/skos:Concept/skos:prefLabel[@xml:lang='de']">
    <TR><TD VALIGN="TOP"><I><B>DDC:</B></I></TD>
    <TD VALIGN="TOP"><B><I>
         <xsl:value-of select="substring-after(dcterms:subject/skos:Concept/@rdf:about,'http://dewey.info/class/')"/>
         <xsl:value-of select="'  '" />
       </I></B>
         <xsl:value-of select="string(dcterms:subject/skos:Concept/skos:prefLabel[@xml:lang='de'])"/>
    </TD></TR>
    </xsl:if>

    <xsl:if test="count(dcterms:isPartOf//sco:issn)>0">
    <TR><TD VALIGN="TOP"><I><B>Zeitschrift:</B></I></TD>
    <TD VALIGN="TOP">
         <xsl:value-of select="dcterms:isPartOf//dcterms:title"/>
    </TD></TR>
    </xsl:if>

    <xsl:apply-templates select="dcterms:title[@xml:lang!=$lang]"/>
    </table>
    <hr/>

    <p>
    <div class="wrap">
        <div class="left">
            <xsl:apply-templates select="foaf:img"/>
        </div>
        <div class="main">
            <xsl:apply-templates select="dcterms:hasPart[not(@rdf:resource)]"/>
        </div>
    </div>
    </p>

    <table summary="keywords" class="metadata">
    <xsl:if test="dcterms:subject">
    <tr><th>Schlagwörter:</th></tr>
    <tr><td>
         <xsl:apply-templates select="dcterms:subject"/>
    </td></tr>
    </xsl:if>
    <xsl:apply-templates select="dcterms:hasPart[@rdf:resource]"/>
    <xsl:apply-templates select="dcterms:isReferencedBy"/>
    </table>

    <xsl:apply-templates select="dcterms:abstract[@xml:lang=$lang]"/>
    <xsl:apply-templates select="dcterms:abstract[@xml:lang!=$lang]"/>

    <hr/>
    <xsl:choose>
    <xsl:when test="dcterms:accessRights"></xsl:when>
    <xsl:otherwise>
        <xsl:apply-templates select="dcterms:mediator"/>
    </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template match="dcterms:mediator">
    <table summary="publisher info">
    <tr>
        <td>
            <img src="https://archiv.ub.example.org/img/free.png" alt="*"/>
        </td>
        <td valign="center">
            <a href="https://archiv.ub.example.org/recht_1.html">
            Das Dokument ist im Internet frei zugänglich -
            Hinweise zu den Nutzungsrechten</a>
        </td>
    </tr>
    </table>
    <div class="footer"> &#169; 2018 
        <a href="https://www.example.org/bis">
            <xsl:value-of select="dcterms:Agent/foaf:name"/>
        </a>
    </div>
</xsl:template>

<xsl:template match="dcterms:title">
    <TR><TD VALIGN="TOP"><I><B>Titel<xsl:if test="@xml:lang!=$lang"> 
        <em>(trans.)</em></xsl:if>:</B></I></TD>
    <TD VALIGN="TOP"><xsl:value-of select="." />
    </TD></TR>
</xsl:template>

<xsl:template match="dcterms:title" mode="header">
    <meta name="DC.title" content="{.}" xml:lang="{@xml:lang}"/>
    <xsl:if test="@xml:lang=$lang or count(../dcterms:title)=1">
    <meta name="citation_title" content="{.}"/>
    </xsl:if>
</xsl:template>

<xsl:template match="dcterms:issued" mode="header">
    <meta name="DC.issued" content="{.}"/> 
    <meta name="citation_online_date" content="{.}"/>
</xsl:template>

<xsl:template match="fabio:hasISBN" mode="header">
    <meta name="citation_isbn" content="{.}"/>
</xsl:template>

<xsl:template match="fabio:hasISSN" mode="header">
    <meta name="citation_issn" content="{.}"/>
</xsl:template>

<xsl:template match="dcterms:created" mode="header">
    <meta name="citation_date" content="{.}"/>
    <meta name="citation_publication_date" content="{.}"/>
</xsl:template>

<xsl:template match="dcterms:publisher" mode="header">
  <xsl:variable name="publisher">
    <xsl:value-of select="concat(foaf:Organization/foaf:name,' ',
                          ../dcterms:publisher/aiiso:Faculty/foaf:name)"/>
  </xsl:variable>
  <xsl:if test="foaf:Organization">
    <meta name="DC.publisher" content="{$publisher}" />
  </xsl:if>
  <xsl:if test="../fabio:DoctoralThesis">
    <meta name="citation_dissertation_institution" content="{$publisher}" />
  </xsl:if>
</xsl:template>

<xsl:template match="dcterms:creator" mode="header">
  <xsl:apply-templates select=".//foaf:Person/foaf:name"/>
</xsl:template>

<xsl:template match="dcterms:type" mode="header">
    <xsl:if test="contains(@rdf:resource,'JournalArticle')">
    <xsl:if test="../dcterms:isPartOf/fabio:JournalIssue/dcterms:title">
    <meta name="citation_journal_title" 
          content="{../dcterms:isPartOf/fabio:JournalIssue/dcterms:title}"/>
    </xsl:if>
    </xsl:if>
</xsl:template>

<xsl:template match="dcterms:publisher" mode="header">
    <xsl:for-each select="aiiso:*">
    <meta name="DC.publisher" content="{foaf:name}" />
    </xsl:for-each>
</xsl:template>

<xsl:template match="foaf:Person/foaf:name">
  <meta name="DC.creator" content="{.}"/> 
  <meta name="citation_author" content="{.}"/>
</xsl:template>

<xsl:template match="foaf:Person/foaf:plan">
  <meta name="citation_author_institution" content="{.}"/>
</xsl:template>

<xsl:template match="sco:serialNumber|sco:volumeNumber|sco:issueNumber">
</xsl:template>

<xsl:template match="dcterms:issued">
  <tr><td><i><b>Erscheinungsjahr:</b></i></td><td>
  <xsl:value-of select="substring(.,1,4)" />
  <xsl:if test="../sco:volumeNumber">
      <xsl:value-of select="concat(' (',../sco:volumeNumber,')')"/>
  </xsl:if>
  </td></tr>
</xsl:template>

<xsl:template match="dcterms:identifier[starts-with(text(),'urn:')]">
    <TR><TD VALIGN="TOP"><I><B>URN: </B></I></TD>
        <TD VALIGN="TOP">
            <a href="{concat('https://nbn-resolving.org/',.)}">
        <xsl:value-of select="." /></a>
    </TD></TR>
</xsl:template>

<xsl:template match="dcterms:identifier[contains(text(),'doi.org')]">
   <TR><TD VALIGN="TOP"><I><B>DOI: </B></I></TD>
       <TD VALIGN="TOP"><a href="{.}"><xsl:value-of select="."/></a>
   </TD></TR>
</xsl:template>

<xsl:template match="dcterms:identifier">
</xsl:template>

<xsl:template match="dcterms:publisher">
    <xsl:apply-templates select="aiiso:Faculty"/>
    <xsl:apply-templates select="aiiso:Institute"/>
    <xsl:apply-templates select="aiiso:Center"/>
</xsl:template>

<xsl:template match="dcterms:publisher/aiiso:Faculty">
    <tr><td><i><b>Fachbereich:</b></i></td><td>
        <xsl:value-of select="foaf:name"/>
        <xsl:value-of select="', '" />
        <xsl:value-of select="dcterms:publisher/foaf:Organization/foaf:name"/>
    </td></tr>
</xsl:template>

<xsl:template match="dcterms:publisher/aiiso:Institute">
    <tr><td><i><b>Institut:</b></i></td><td>
        <xsl:value-of select="foaf:name"/>
    </td></tr>
</xsl:template>

<xsl:template match="dcterms:publisher/aiiso:Center">
    <tr><td><i><b>Zentrum:</b></i></td><td>
        <xsl:value-of select="foaf:name"/>
    </td></tr>
</xsl:template>

<xsl:template match="sco:isbn">
   <TR><TD VALIGN="TOP"><I><B>ISBN: </B></I></TD>
       <TD VALIGN="TOP"><xsl:value-of select="."/>
   </TD></TR>
</xsl:template>

<xsl:template match="sco:issn">
   <TR><TD VALIGN="TOP"><I><B>ISSN: </B></I></TD>
       <TD VALIGN="TOP"><xsl:value-of select="."/>
   </TD></TR>
</xsl:template>

<xsl:template match="foaf:img">
    <img height="110px" border="1" src="{.}"/>
</xsl:template>

<xsl:template match="dcterms:subject">
  <xsl:apply-templates select="skos:Concept"/>
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept">
  <xsl:apply-templates select="rdfs:label"/>
</xsl:template>

<xsl:template match="dcterms:subject/skos:Concept/rdfs:label">
  <xsl:value-of select="."/>
  <xsl:choose>
  <xsl:when test="count(../../following-sibling::dcterms:subject[skos:Concept/rdfs:label])>0">
    <xsl:value-of select="', '"/>
  </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template match="dcterms:abstract[@xml:lang='en']">
    <p><b><a name="abstract">Summary:</a></b><br/>
       <xsl:value-of select="." />
    </p>
</xsl:template>

<xsl:template match="dcterms:abstract[@xml:lang='de']">
    <p><b><a name="abstract">Zusammenfassung:</a></b><br/>
       <xsl:value-of select="." />
    </p>
</xsl:template>

<xsl:template match="dcterms:isPartOf" mode="header">
    <meta name="DC.relation.ispartof" content="{*/dcterms:title}"/> 
</xsl:template>

<xsl:template match="dcterms:isReferencedBy">
    <TR><TD VALIGN="TOP"><I><B>Referenziert von: </B></I></TD>
    <TD VALIGN="TOP"><a href="{.}"><xsl:value-of select="substring-after(.,'pdf/')" /></a></TD></TR>
</xsl:template>

<xsl:template match="dcterms:hasPart[@rdf:resource][1]">
  <table>
   <xsl:for-each select="following-sibling::dcterms:hasPart[@rdf:resource]">
   <xsl:sort select="@rdf:resource"/>
    <tr><td><i><b>Band <xsl:value-of select="position()"/>: </b></i></td><td>
    <a href="{concat('https:',substring-after(@rdf:resource,':'))}">
    <xsl:value-of select="concat('https:',substring-after(@rdf:resource,':'))"/>
    </a>
    </td></tr>
   </xsl:for-each>
  </table>
</xsl:template>

<xsl:template match="dcterms:hasPart[not(@rdf:resource)]">
  <xsl:apply-templates select="dctypes:Text"/>
  <xsl:apply-templates select="dctypes:MovingImage"/>
  <xsl:apply-templates select="dctypes:Image"/>
  <xsl:apply-templates select="dctypes:Collection"/>
  <xsl:apply-templates select="dctypes:Dataset"/>
</xsl:template>

<xsl:template match="dctypes:Text[contains(@rdf:about, '/html')]">
  <a href="{concat('https:',substring-after(@rdf:about,':'))}">
  <xsl:value-of select="'Dokument'"/></a><br/>
</xsl:template>

<xsl:template match="dctypes:Text[contains(@rdf:about, '/All.pdf')]">
  <!-- skip -->
</xsl:template>

<xsl:template match="dctypes:Text[dcterms:format/dcterms:MediaTypeOrExtent/rdfs:label='application/pdf']">
  <a href="{concat('https:',substring-after(@rdf:about,':'))}">
  <xsl:value-of select="'Dokument'"/></a><br/>
</xsl:template>

<xsl:template match="dctypes:Text[contains(@rdf:about, '/mets-')]">
  <xsl:variable name="urlenc">
    <xsl:call-template name="url-encode">
      <xsl:with-param name="str" select="@rdf:about"/>
    </xsl:call-template>
  </xsl:variable>
  <a href="{concat('http://dfg-viewer.de/show/?set[mets]=',$urlenc)}">
      <xsl:value-of select="'DFG-Viewer'"/>
  </a><br/>
</xsl:template>

<xsl:template match="dctypes:Text[substring(@rdf:about,string-length(@rdf:about)-3)='.xml']">
  <xsl:variable name="urlenc">
    <xsl:call-template name="url-encode">
      <xsl:with-param name="str" select="@rdf:about"/>
    </xsl:call-template>
  </xsl:variable>
  <a href="{concat('http://dfg-viewer.de/show/?set[mets]=',$urlenc)}">
      <xsl:value-of select="'DFG-Viewer'"/>
  </a><br/>
</xsl:template>

<xsl:template match="dctypes:MovingImage">
  <a href="{@rdf:about}">
  <xsl:value-of select="'Video'"/></a><br/>
</xsl:template>

<xsl:template match="dctypes:Image">
  <a href="{@rdf:about}">
  <xsl:value-of select="'Bild'"/></a><br/>
</xsl:template>

<xsl:template match="dctypes:Dataset">
  <xsl:value-of select="count(../preceding-sibling::dcterms:hasPart)+1"/>
  <xsl:value-of select="'. '"/>
  <a href="{@rdf:about}">
  <xsl:value-of select="'Daten'"/></a><br/>
</xsl:template>

<xsl:template match="dctypes:Collection[contains(@rdf:about,'container.zip')]">
  <a href="{@rdf:about}">
  <xsl:value-of select="'Container'"/></a>
  <br/>
</xsl:template>

<xsl:template match="void:Dataset">
  <html><body><ul>
  <xsl:apply-templates select="dcterms:hasPart"/>
  </ul></body></html>
</xsl:template>

<xsl:template match="void:Dataset/dcterms:hasPart[@rdf:resource]">
  <li><a href="{@rdf:resource}"><xsl:value-of select="@rdf:resource"/></a></li>
</xsl:template>

<xsl:template name="url-encode">
    <xsl:param name="str"/>
  <xsl:variable name="ascii"> !"#$%&amp;'()*+,-./0123456789:;&lt;=&gt;?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~</xsl:variable>
  <xsl:variable name="latin1">&#160;&#161;&#162;&#163;&#164;&#165;&#166;&#167;&#168;&#169;&#170;&#171;&#172;&#173;&#174;&#175;&#176;&#177;&#178;&#179;&#180;&#181;&#182;&#183;&#184;&#185;&#186;&#187;&#188;&#189;&#190;&#191;&#192;&#193;&#194;&#195;&#196;&#197;&#198;&#199;&#200;&#201;&#202;&#203;&#204;&#205;&#206;&#207;&#208;&#209;&#210;&#211;&#212;&#213;&#214;&#215;&#216;&#217;&#218;&#219;&#220;&#221;&#222;&#223;&#224;&#225;&#226;&#227;&#228;&#229;&#230;&#231;&#232;&#233;&#234;&#235;&#236;&#237;&#238;&#239;&#240;&#241;&#242;&#243;&#244;&#245;&#246;&#247;&#248;&#249;&#250;&#251;&#252;&#253;&#254;&#255;</xsl:variable>

  <xsl:variable name="safe">!'()*-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~</xsl:variable>
  <xsl:variable name="hex" >0123456789ABCDEF</xsl:variable>

    <xsl:if test="$str">
      <xsl:variable name="first-char" select="substring($str,1,1)"/>
      <xsl:choose>
        <xsl:when test="contains($safe,$first-char)">
          <xsl:value-of select="$first-char"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="codepoint">
            <xsl:choose>
              <xsl:when test="contains($ascii,$first-char)">
                <xsl:value-of select="string-length(substring-before($ascii,$first-char)) + 32"/>
              </xsl:when>
              <xsl:when test="contains($latin1,$first-char)">
                <xsl:value-of select="string-length(substring-before($latin1,$first-char)) + 160"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:message terminate="no">Warning: string contains a character that is out of range! Substituting "?".</xsl:message>
                <xsl:text>63</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
        <xsl:variable name="hex-digit1" select="substring($hex,floor($codepoint div 16) + 1,1)"/>
        <xsl:variable name="hex-digit2" select="substring($hex,$codepoint mod 16 + 1,1)"/>
        <xsl:value-of select="concat('%',$hex-digit1,$hex-digit2)"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="string-length($str) &gt; 1">
        <xsl:call-template name="url-encode">
          <xsl:with-param name="str" select="substring($str,2)"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
</xsl:template>

<xsl:template name="head">
    <meta name="ROBOTS" CONTENT="INDEX"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

    <link rel="meta" type="application/rdf+xml" 
          href="{concat(@rdf:about,'/about.rdf')}" />
    <meta charset="UTF-8"/>
    <meta name="DC.identifier" content="{@rdf:about}"/>

    <xsl:apply-templates select="dcterms:title" mode="header"/>
    <xsl:apply-templates select="dcterms:creator" mode="header"/>
    <xsl:apply-templates select="dcterms:created" mode="header"/>
    <xsl:apply-templates select="dcterms:issued" mode="header"/>

    <xsl:apply-templates select="dcterms:isPartOf" mode="header"/>
    <xsl:apply-templates select="dcterms:publisher" mode="header"/>
    <xsl:apply-templates select="sco:issn" mode="header"/>
    <xsl:apply-templates select="sco:isbn" mode="header"/>

    <xsl:apply-templates select="dcterms:type" mode="header"/>
    <xsl:apply-templates select="dcterms:publisher" mode="header"/>

    <xsl:choose>
       <xsl:when test="contains(dcterms:hasPart/dctypes:Text/@rdf:about,'All.pdf')">
       </xsl:when>
       <xsl:when test="contains(dcterms:hasPart/dctypes:Text/dcterms:format,'pdf')">
       <meta name="citation_pdf_url" content="{dcterms:hasPart/dctypes:Text/@rdf:about}"/>
       </xsl:when>
    </xsl:choose>

    <xsl:for-each select="dcterms:abstract">
      <meta name="DC.abstract" content="{.}" />
    </xsl:for-each>
</xsl:template>

<xsl:template name="style">
  <style type="text/css" media="screen">
  /*<![CDATA[*/
  html, body {
    background: #eee;
    color: #333;
    height: 100%;
    margin: 9px 33px 4px 33px;
  }
  div.header {
    text-align: center;
    position: relative;
    margin-bottom: 1.8em;
    padding: 0.7em;
  }
  div.content {
    position: relative; min-height: 100%;
  }
  div.footer {
    font-size: small;
    padding: .8em;
    text-align: right;
    bottom:0;
  }
  div.right {
    float: right;
  }
  div.left {
    float: left;
    width: 33%;
    margin: 9px 33px 4px 33px;
    overflow: hidden;
  }
  div.wrap {
    height: 170px;
    overflow: hidden;
    position: relative;
  }
  div.footer {
    font-size: small;
    padding: .8em;
    text-align: right;
    bottom:0;
  }
  div.main {
    margin-top: 33px;
    margin-left: 33px;
  }
  table.metadata {
    padding: .8em;
  }
  table.metadata tr th {
    text-align:left;
  }
  /*]]>*/
  </style>
</xsl:template>

</xsl:stylesheet>
