<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/TestLog">

    <xsl:element name="testsuite">
      <xsl:attribute name="tests">
        <xsl:value-of select="count(//TestCase)"/>
      </xsl:attribute>

      <xsl:attribute name="errors">
        <xsl:value-of select="count(//TestCase/FatalError)+count(//TestCase/Exception)"/>
      </xsl:attribute>

      <xsl:attribute name="failures">
        <xsl:value-of select="count(//TestCase/Error)"/>
      </xsl:attribute>

      <xsl:attribute name="name">MergedTestSuite</xsl:attribute>

      <xsl:attribute name="skipped">0</xsl:attribute>

      <xsl:for-each select="//TestCase">
        <xsl:call-template name="testCase"/>
      </xsl:for-each>
    </xsl:element>

  </xsl:template>


  <xsl:template name="testCaseContent">
    <xsl:for-each select="child::*">
      <xsl:variable name="currElt" select="."/>
      <xsl:variable name="currEltName" select="name(.)"/>
      <xsl:choose>
        <xsl:when test="$currEltName='Error'">
          <xsl:text>+</xsl:text>
          <xsl:text>[Error] - </xsl:text>
          <xsl:value-of select="($currElt)"/>
          <xsl:text>+</xsl:text>
          <xsl:text> == [File] - </xsl:text><xsl:value-of select="($currElt)/@file"/>
          <xsl:text>+</xsl:text>
          <xsl:text> == [Line] - </xsl:text><xsl:value-of select="($currElt)/@line"/>
          <xsl:text>+</xsl:text>
        </xsl:when>

        <xsl:when test="$currEltName='FatalError' or $currEltName='Exception'">
          <xsl:text>+</xsl:text>
          <xsl:text>[Exception] - </xsl:text>
          <xsl:value-of select="($currElt)"/>
          <xsl:text>+</xsl:text>
          <xsl:text> == [File] - </xsl:text><xsl:value-of select="($currElt)/@file"/>
          <xsl:text>+</xsl:text>
          <xsl:text> == [Line] -</xsl:text><xsl:value-of select="($currElt)/@line"/>
          <xsl:text>+</xsl:text>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>


  <xsl:template name="testCase">

    <xsl:variable name="curElt" select="."/>
    <xsl:variable name="suiteName">
      <xsl:for-each select="($curElt/ancestor::TestSuite)"><xsl:value-of select="./@name"/>.</xsl:for-each>
    </xsl:variable>
    <xsl:variable name="packageName" select="($suiteName)"/>


    <xsl:element name="testcase">
      <xsl:variable name="elt" select="(child::*[position()=1])"/>
      <xsl:variable name="time" select="TestingTime"/>

      <xsl:attribute name="classname">
        <xsl:value-of select="concat($packageName, substring-before(($elt)/@file, '.'))"/>
      </xsl:attribute>

      <xsl:attribute name="name">
        <xsl:value-of select="@name"/>
      </xsl:attribute>


      <xsl:attribute name="time">
        <xsl:value-of select="$time div 1000000"/>
      </xsl:attribute>

      <xsl:variable name="nbErrors" select="count(Error)"/>
      <xsl:variable name="nbFatalErrors" select="count(FatalError)+count(Exception)"/>

      <xsl:choose>
        <xsl:when test="$nbFatalErrors&gt;0">
          <xsl:element name="error">
            <xsl:call-template name="testCaseContent"/>
          </xsl:element>
        </xsl:when>

        <xsl:when test="$nbErrors&gt;0">
          <xsl:element name="failure">
            <xsl:call-template name="testCaseContent"/>
          </xsl:element>
        </xsl:when>
      </xsl:choose>


      <xsl:if test="(count(child::Info)+ count(child::Warning) + count(child::Message))&gt;0">
        <xsl:element name="system-out">
          <xsl:for-each select="child::Info">
            <xsl:variable name="currElt" select="."/>
            <xsl:text>+</xsl:text>
            <xsl:text>[Info] - </xsl:text>
            <xsl:value-of select="($currElt)"/>
            <xsl:text>+</xsl:text>
            <xsl:text> == [File] - </xsl:text><xsl:value-of select="($currElt)/@file"/>
            <xsl:text>+</xsl:text>
            <xsl:text> == [Line] - </xsl:text><xsl:value-of select="($currElt)/@line"/>
            <xsl:text>+</xsl:text>
          </xsl:for-each>

          <xsl:for-each select="child::Warning">
            <xsl:variable name="currElt" select="."/>
            <xsl:text>+</xsl:text>
            <xsl:text>[Warning] - </xsl:text>
            <xsl:value-of select="($currElt)"/>
            <xsl:text>+</xsl:text>
            <xsl:text> == [File] - </xsl:text><xsl:value-of select="($currElt)/@file"/>
            <xsl:text>+</xsl:text>
            <xsl:text> == [Line] - </xsl:text><xsl:value-of select="($currElt)/@line"/>
            <xsl:text>+</xsl:text>
          </xsl:for-each>

          <xsl:for-each select="child::Message">
            <xsl:variable name="currElt" select="."/>
            <xsl:text>+</xsl:text>
            <xsl:text>[Message] - </xsl:text>
            <xsl:value-of select="($currElt)"/>
            <xsl:text>+</xsl:text>
            <xsl:text> == [File] - </xsl:text><xsl:value-of select="($currElt)/@file"/>
            <xsl:text>+</xsl:text>
            <xsl:text> == [Line] - </xsl:text><xsl:value-of select="($currElt)/@line"/>
            <xsl:text>+</xsl:text>
          </xsl:for-each>

        </xsl:element>
      </xsl:if>

      <xsl:if test="count(child::Exception)&gt;0">
        <xsl:element name="system-err">
          <xsl:for-each select="child::Exception">
            <xsl:variable name="currElt" select="."/>
            <xsl:text>+</xsl:text>
            <xsl:text>[Exception] - </xsl:text>
            <xsl:value-of select="($currElt)"/>
            <xsl:text>+</xsl:text>
            <xsl:text> == [File] - </xsl:text><xsl:value-of select="($currElt)/@file"/>
            <xsl:text>+</xsl:text>
            <xsl:text> == [Line] - </xsl:text><xsl:value-of select="($currElt)/@line"/>
            <xsl:text>+</xsl:text>
          </xsl:for-each>
        </xsl:element>
      </xsl:if>

    </xsl:element>

  </xsl:template>

  <xsl:template match="text()|@*"/>
</xsl:stylesheet>
