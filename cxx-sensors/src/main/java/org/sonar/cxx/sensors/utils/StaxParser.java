/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.cxx.sensors.utils;

import com.ctc.wstx.stax.WstxInputFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * helper class StaxParser
 */
public class StaxParser {

  private static final Logger LOG = Loggers.get(StaxParser.class);
  private SMInputFactory inf;
  private XmlStreamHandler streamHandler;
  private boolean isoControlCharsAwareParser;

  /**
   * Stax parser for a given stream handler and ISO control chars set awareness to off
   *
   * @param streamHandler the XML stream handler
   */
  public StaxParser(XmlStreamHandler streamHandler) {
    this(streamHandler, false);
  }

  /**
   * StaxParser for a given stream handler and ISO control chars set awareness to on. The ISO control chars in the XML
   * file will be replaced by simple spaces, useful for potentially bogus XML files to parse, this has a small perfs
   * overhead so use it only when necessary
   *
   * @param streamHandler the XML stream handler
   * @param isoControlCharsAwareParser true or false
   */
  public StaxParser(XmlStreamHandler streamHandler, boolean isoControlCharsAwareParser) {
    this.streamHandler = streamHandler;
    XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
    if (xmlFactory instanceof WstxInputFactory) {
      WstxInputFactory wstxInputfactory = (WstxInputFactory) xmlFactory;
      wstxInputfactory.configureForLowMemUsage();
      wstxInputfactory.getConfig().setUndeclaredEntityResolver(new UndeclaredEntitiesXMLResolver());
    }
    xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    this.isoControlCharsAwareParser = isoControlCharsAwareParser;
    inf = new SMInputFactory(xmlFactory);
  }

  /**
   * parse XML stream:
   *
   * @param xmlFile - java.io.File = input file
   * @exception XMLStreamException javax.xml.stream.XMLStreamException
   */
  public void parse(File xmlFile) throws XMLStreamException {
    try (InputStream input = java.nio.file.Files.newInputStream(xmlFile.toPath())) {
      parse(input);
    } catch (IOException e) {
      LOG.debug("Cannot access file", e);
    }
  }

  /**
   * parse XML stream:
   *
   * @param xmlInput - java.io.InputStream = input file
   * @exception XMLStreamException javax.xml.stream.XMLStreamException
   */
  public void parse(InputStream xmlInput) throws XMLStreamException {
    InputStream input = isoControlCharsAwareParser ? new ISOControlCharAwareInputStream(xmlInput) : xmlInput;
    parse(inf.rootElementCursor(input));
  }

  /**
   * parse XML stream:
   *
   * @param xmlReader - java.io.Reader = input file
   * @exception XMLStreamException javax.xml.stream.XMLStreamException
   */
  public void parse(Reader xmlReader) throws XMLStreamException {
    if (isoControlCharsAwareParser) {
      throw new XMLStreamException("Method call not supported when isoControlCharsAwareParser=true");
    }
    parse(inf.rootElementCursor(xmlReader));
  }

  /**
   * parse XML stream:
   *
   * @param xmlUrl - java.net.URL = input stream
   * @exception XMLStreamException javax.xml.stream.XMLStreamException
   */
  public void parse(URL xmlUrl) throws XMLStreamException {
    try {
      parse(xmlUrl.openStream());
    } catch (IOException e) {
      throw new XMLStreamException(e);
    }
  }

  private void parse(SMHierarchicCursor rootCursor) throws XMLStreamException {
    try {
      streamHandler.stream(rootCursor);
    } finally {
      rootCursor.getStreamReader().closeCompletely();
    }
  }

  private static class UndeclaredEntitiesXMLResolver implements XMLResolver {

    @Override
    public Object resolveEntity(String arg0, String arg1, String fileName, String undeclaredEntity)
      throws XMLStreamException {
      // avoid problems with XML docs containing undeclared entities.. 
      // return the entity under its raw form if not an unicode expression
      String undeclared = undeclaredEntity;
      if (StringUtils.startsWithIgnoreCase(undeclared, "u") && undeclared.length() == 5) {
        int unicodeCharHexValue = Integer.parseInt(undeclared.substring(1), 16);
        if (Character.isDefined(unicodeCharHexValue)) {
          undeclared = new String(new char[]{(char) unicodeCharHexValue});
        }
      }
      return undeclared;
    }
  }

  /**
   * XmlStreamHandler: Simple interface for handling XML stream to parse
   */
  public interface XmlStreamHandler {

    /**
     * stream:
     *
     * @param rootCursor - org.codehaus.staxmate.i.SMHierarchicCursor
     * @exception XMLStreamException javax.xml.stream.XMLStreamException
     */
    void stream(SMHierarchicCursor rootCursor) throws XMLStreamException;
  }

  private static class ISOControlCharAwareInputStream extends InputStream {

    private InputStream inputToCheck;

    public ISOControlCharAwareInputStream(InputStream inputToCheck) {
      super();
      this.inputToCheck = inputToCheck;
    }

    @Override
    public int read() throws IOException {
      return inputToCheck.read();
    }

    @Override
    public int available() throws IOException {
      return inputToCheck.available();
    }

    @Override
    public void close() throws IOException {
      inputToCheck.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
      inputToCheck.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
      return inputToCheck.markSupported();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      int readen = inputToCheck.read(b, off, len);
      checkBufferForISOControlChars(b, off, len);
      return readen;
    }

    @Override
    public int read(byte[] b) throws IOException {
      int readen = inputToCheck.read(b);
      checkBufferForISOControlChars(b, 0, readen);
      return readen;
    }

    @Override
    public synchronized void reset() throws IOException {
      inputToCheck.reset();
    }

    @Override
    public long skip(long n) throws IOException {
      return inputToCheck.skip(n);
    }

    private static void checkBufferForISOControlChars(byte[] buffer, int off, int len) {
      for (int i = off; i < len; i++) {
        char streamChar = (char) buffer[i];
        if (Character.isISOControl(streamChar) && streamChar != '\n') {
          // replace control chars by a simple space
          buffer[i] = ' ';
        }
      }
    }
  }
}
