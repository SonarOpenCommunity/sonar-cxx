<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output method="xml" version="1.0" encoding="UTF-8"/>
 <xsl:template match="results">
  <results>
   <xsl:apply-templates/>
  </results>
 </xsl:template>
 <xsl:template match="warning">
  <error id="{@identifier}" msg="{@message}" file="{@filename}" line="{@line}" />
 </xsl:template>
</xsl:stylesheet>
