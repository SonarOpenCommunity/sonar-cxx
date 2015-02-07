<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/TestLog">
    <xsl:element name="testsuite">

      <xsl:choose>

        <xsl:when test="boolean(//TestCase)">
          <xsl:attribute name="tests">
            <xsl:value-of select="count(//TestCase)"/>
          </xsl:attribute>

          <xsl:attribute name="errors">
            <xsl:value-of select="count(//TestCase/FatalError)+count(//TestCase/Exception)"/>
          </xsl:attribute>

          <xsl:attribute name="failures">
            <xsl:value-of select="count(//TestCase/Error)"/>
          </xsl:attribute>

          <xsl:attribute name="skipped">0</xsl:attribute>

          <xsl:attribute name="name">
            <xsl:value-of select="//TestSuite/@name"/>
          </xsl:attribute>

          <xsl:for-each select="//TestCase">
            <xsl:call-template name="testCase"/>
          </xsl:for-each>
        </xsl:when>

        <xsl:otherwise>
          <xsl:variable name="countErrors" select="count(//FatalError)+count(//Exception)"/>
          <xsl:variable name="countFailures" select="count(//Error)"/>

          <xsl:attribute name="tests">
            <xsl:value-of select="$countErrors+$countFailures"/>
          </xsl:attribute>

          <xsl:attribute name="errors">
            <xsl:value-of select="$countErrors"/>
          </xsl:attribute>

          <xsl:attribute name="failures">
            <xsl:value-of select="$countFailures"/>
          </xsl:attribute>

          <xsl:attribute name="skipped">0</xsl:attribute>

          <xsl:attribute name="name">Master Test Suite</xsl:attribute>

          <xsl:for-each select="./*">
            <xsl:call-template name="testCase"/>
          </xsl:for-each>
        </xsl:otherwise>

      </xsl:choose>

    </xsl:element>
  </xsl:template>

  <xsl:template name="testCaseContent">

    <xsl:variable name="currElt" select="."/>
    <xsl:variable name="currEltName" select="name(.)"/>

    <xsl:choose>

      <xsl:when test="$currEltName='Error'">
        <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
        <xsl:value-of select="($currElt)/@file"/>
        <xsl:text>(</xsl:text>
        <xsl:value-of select="($currElt)/@line"/>
        <xsl:text>) Error: </xsl:text>
        <xsl:value-of select="($currElt)"/>
        <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
      </xsl:when>

      <xsl:when test="$currEltName='FatalError'">
        <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
        <xsl:value-of select="($currElt)/@file"/>
        <xsl:text>(</xsl:text>
        <xsl:value-of select="($currElt)/@line"/>
        <xsl:text>) FatalError: </xsl:text>
        <xsl:value-of select="($currElt)"/>
        <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
      </xsl:when>

      <xsl:when test="$currEltName='Exception'">
        <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
        <xsl:value-of select="($currElt)/@file"/>
        <xsl:text>(</xsl:text>
        <xsl:value-of select="($currElt)/@line"/>
        <xsl:text>) Exception: </xsl:text>
        <xsl:value-of select="($currElt)"/>
        <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
      </xsl:when>

    </xsl:choose>

  </xsl:template>

  <xsl:template name="testCase">

    <xsl:variable name="curElt" select="."/>
    <xsl:variable name="suiteName">
      <xsl:for-each select="($curElt/ancestor::TestSuite/TestSuite)">
        <xsl:text>::</xsl:text>
        <xsl:value-of select="./@name"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="namespace" select="substring($suiteName,3)"/>

    <xsl:element name="testcase">
      <xsl:variable name="elt" select="(child::*[position()=1])"/>
      <xsl:variable name="time" select="TestingTime"/>

      <xsl:variable name="name">
        <xsl:choose>
          <xsl:when test="string-length(@name)">
            <xsl:value-of select="@name"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat(name(.),string(position()))"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:attribute name="classname">
        <xsl:choose>
          <xsl:when test="string-length($namespace)">
            <xsl:value-of select="concat($namespace,'::',$name)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$name"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>

      <xsl:attribute name="name">
        <xsl:value-of select="$name"/>
      </xsl:attribute>

      <xsl:for-each select="descendant-or-self::*[@file]">
        <xsl:if test="(@file)!='unknown location'">
          <xsl:attribute name="filename">
            <xsl:value-of select="@file"/>
          </xsl:attribute>
        </xsl:if>
      </xsl:for-each>

      <xsl:if test="$time&gt;0">
        <xsl:attribute name="time">
          <xsl:value-of select="$time div 1000000"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:variable name="countErrors" select="count(Error)"/>
      <xsl:variable name="countFailures" select="count(FatalError)+count(Exception)"/>

      <xsl:choose>
        <xsl:when test="$countFailures&gt;0">
          <xsl:for-each select="./FatalError | ./Exception">
            <xsl:element name="error">
              <xsl:call-template name="testCaseContent"/>
            </xsl:element>
          </xsl:for-each>
        </xsl:when>
        <xsl:when test="$countErrors&gt;0">
          <xsl:for-each select="./Error">
            <xsl:element name="failure">
              <xsl:call-template name="testCaseContent"/>
            </xsl:element>
          </xsl:for-each>
        </xsl:when>
        <xsl:when test="name()='Error'">
          <xsl:element name="failure">
            <xsl:call-template name="testCaseContent"/>
          </xsl:element>
        </xsl:when>
        <xsl:when test="name()='FatalError' or name()='Exception'">
          <xsl:element name="error">
            <xsl:call-template name="testCaseContent"/>
          </xsl:element>
        </xsl:when>
      </xsl:choose>

    </xsl:element>

  </xsl:template>

  <xsl:template match="text()|@*"/>

</xsl:stylesheet>
