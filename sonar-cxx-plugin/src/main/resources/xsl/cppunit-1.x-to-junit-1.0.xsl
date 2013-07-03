<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/">
    <xsl:element name="testsuite">
      <xsl:attribute name="errors">
        <xsl:value-of select="TestRun/Statistics/Errors"/>
      </xsl:attribute>

      <xsl:attribute name="failures">
        <xsl:value-of select="TestRun/Statistics/Failures"/>
      </xsl:attribute>

      <xsl:attribute name="tests">
        <xsl:value-of select="TestRun/Statistics/Tests"/>
      </xsl:attribute>

      <xsl:attribute name="name">cppunit</xsl:attribute>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="/TestRun/SuccessfulTests/Test">
    <xsl:call-template name="successTestCase"/>
  </xsl:template>

  <xsl:template match="/TestRun/FailedTests/FailedTest">
    <xsl:call-template name="failureOrErrorTestCase"/>
  </xsl:template>

  <xsl:template match="/TestRun/FailedTests/Test">
    <xsl:call-template name="failureOrErrorTestCase"/>
  </xsl:template>


  <xsl:template name="successTestCase">

    <xsl:element name="testcase">
      <xsl:choose>
        <xsl:when test="contains(Name, '::')">
          <xsl:attribute name="classname">
            <xsl:value-of select="substring-before(Name, '::')"/>
          </xsl:attribute>
          <xsl:attribute name="name">
            <xsl:value-of select="substring-after(Name, '::')"/>
          </xsl:attribute>
          <xsl:attribute name="time">0</xsl:attribute>
        </xsl:when>

        <xsl:when test="contains(Name, '.')">
          <xsl:attribute name="classname">
            <xsl:value-of select="substring-before(Name, '.')"/>
          </xsl:attribute>
          <xsl:attribute name="name">
            <xsl:value-of select="substring-after(Name, '.')"/>
          </xsl:attribute>
          <xsl:attribute name="time">0</xsl:attribute>
        </xsl:when>

        <xsl:otherwise>
          <xsl:attribute name="classname">TestClass</xsl:attribute>
          <xsl:attribute name="name">
            <xsl:value-of select="Name"/>
          </xsl:attribute>
          <xsl:attribute name="time">0</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>


  <xsl:template name="failureOrErrorTestCase">

    <xsl:element name="testcase">
      <xsl:choose>
        <xsl:when test="contains(Name, '::')">
          <xsl:attribute name="classname">
            <xsl:value-of select="substring-before(Name, '::')"/>
          </xsl:attribute>
          <xsl:attribute name="name">
            <xsl:value-of select="substring-after(Name, '::')"/>
          </xsl:attribute>
          <xsl:attribute name="time">0</xsl:attribute>
        </xsl:when>

        <xsl:when test="contains(Name, '.')">
          <xsl:attribute name="classname">
            <xsl:value-of select="substring-before(Name, '.')"/>
          </xsl:attribute>
          <xsl:attribute name="name">
            <xsl:value-of select="substring-after(Name, '.')"/>
          </xsl:attribute>
          <xsl:attribute name="time">0</xsl:attribute>
        </xsl:when>

        <xsl:otherwise>
          <xsl:attribute name="classname">TestClass</xsl:attribute>
          <xsl:attribute name="name">
            <xsl:value-of select="Name"/>
          </xsl:attribute>
          <xsl:attribute name="time">0</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:choose>
        <xsl:when test="FailureType='Error'">
          <xsl:element name="error">
            <xsl:attribute name="message">
              <xsl:value-of select=" normalize-space(Message)"/>
            </xsl:attribute>
            <xsl:attribute name="type">
              <xsl:value-of select="FailureType"/>
            </xsl:attribute>
            <xsl:value-of select="normalize-space(Message)"/>
          </xsl:element>

          <xsl:element name="system-err">
            <xsl:text>+</xsl:text>
            <xsl:text>[File] - </xsl:text><xsl:value-of select="Location/File"/>
            <xsl:text>+</xsl:text>
            <xsl:text>[Line] - </xsl:text><xsl:value-of select="Location/Line"/>
            <xsl:text>+</xsl:text>
          </xsl:element>
        </xsl:when>

        <xsl:otherwise>
          <xsl:element name="failure">
            <xsl:attribute name="message">
              <xsl:value-of select=" normalize-space(Message)"/>
            </xsl:attribute>
            <xsl:attribute name="type">
              <xsl:value-of select="FailureType"/>
            </xsl:attribute>
          </xsl:element>
          <xsl:element name="system-err">
            <xsl:text>+</xsl:text>
            <xsl:text>[File] - </xsl:text><xsl:value-of select="Location/File"/>
            <xsl:text>+</xsl:text>
            <xsl:text>[Line] - </xsl:text><xsl:value-of select="Location/Line"/>
            <xsl:text>+</xsl:text>
          </xsl:element>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>

  <xsl:template match="text()|@*"/>
</xsl:stylesheet>
