<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output indent="no" media-type="application/xml" method="xml" omit-xml-declaration="yes" />

  <xsl:template match="TestSourceElement">
    <xsl:element name="TestResultElement">
      <xsl:value-of select="text()" />
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
