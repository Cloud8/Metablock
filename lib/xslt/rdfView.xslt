<?xml version="1.0"?>
<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:dctypes="http://purl.org/dc/dcmitype/"
     xmlns:foaf="http://xmlns.com/foaf/0.1/"
     version="1.0" >

<xsl:output method="html" indent="yes"/>

<xsl:template match="rdf:RDF">
  <xsl:apply-templates select="dcterms:BibliographicResource" />
</xsl:template>

<xsl:template match="dcterms:BibliographicResource">
  <xsl:variable name="apos">'</xsl:variable>
  <html>
  <head>
  <script type="text/javascript">
  <xsl:value-of select="concat(
    'window.onload = function() { history.replaceState(null,',$apos,$apos,
    ',',$apos,'/',substring-after(substring-after(@rdf:about,'://'),'/'),
    $apos,'); }')"/>
  </script>
  </head>

  <body>
    <xsl:comment>DFG viewer</xsl:comment>
    <xsl:apply-templates select="dcterms:hasPart"/>
  </body>
 </html>
</xsl:template>

<xsl:template match="dcterms:hasPart">
   <xsl:apply-templates select="dctypes:Text"/>
</xsl:template>

<xsl:template match="dcterms:hasPart/dctypes:Text[dcterms:format/dcterms:MediaTypeOrExtent/rdfs:label='application/xml']">
    <xsl:variable name="urlenc">
      <xsl:call-template name="url-encode">
       <xsl:with-param name="str" select="@rdf:about"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:text>
    </xsl:text>
    <iframe src="http://dfg-viewer.de/show/?set[mets]={$urlenc}" 
             frameborder="0" width="100%" height="100%" id="view">
             Your Browser does not support iframes.<br/>
             Please try 
             <a href="http://dfg-viewer.de/show/?set[mets]={$urlenc}">
             this link</a>
    </iframe>
    <xsl:text>
    </xsl:text>
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

<xsl:template match="@*|node()" priority="-1">
</xsl:template>

</xsl:stylesheet>
