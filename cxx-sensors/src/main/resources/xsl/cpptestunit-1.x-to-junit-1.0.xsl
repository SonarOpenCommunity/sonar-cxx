<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:template match="/">
    <xsl:apply-templates select="ResultsSession/Exec"/>
  </xsl:template>

  <xsl:template match="Exec">
    <testsuite name="{Summary/Projects/Project/@name}" time="0" tests="{Summary/Projects/Project/@testCases}" failures="{Summary/Projects/Project/@fail}">
      <xsl:apply-templates select="*"/>
    </testsuite>
  </xsl:template>

  <xsl:template match="Goals">
    <properties>
      <xsl:apply-templates select="Goal"/>
    </properties>
  </xsl:template>

  <xsl:template match="Goal">
    <property name="{@name}" value="{@type}"/>
  </xsl:template>

  <xsl:template match="ExecViols">
    <xsl:apply-templates select="ExecViol"/>
  </xsl:template>

  <xsl:template match="ExecViol">
    <testcase classname="{@locFile}" name="{@testName}" time="0">
      <xsl:apply-templates select="Thr"/>
    </testcase>
  </xsl:template>

  <xsl:template match="Thr">
    <xsl:apply-templates select="ThrPart"/>
  </xsl:template>

  <xsl:template match="ThrPart">
    <failure type="{@clName}" message="{@detMsg}"/>
    <system-err>
      <xsl:text>Trace </xsl:text>
      <xsl:apply-templates select="Trace"/>
    </system-err>
  </xsl:template>

  <xsl:template match="Trace">
    <xsl:text>Line :</xsl:text>
    <xsl:value-of select="@ln"/>
    <xsl:text>    File :</xsl:text>
    <xsl:value-of select="@fileName"/>
  </xsl:template>

</xsl:stylesheet>
