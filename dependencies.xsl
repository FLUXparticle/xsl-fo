<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text" encoding="UTF-8"/>

    <xsl:template match="/xsl:stylesheet">
        <xsl:apply-templates select="//xsl:import"/>
    </xsl:template>

    <xsl:template match="xsl:import">
        <xsl:value-of select="@href"/>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>

</xsl:stylesheet>
