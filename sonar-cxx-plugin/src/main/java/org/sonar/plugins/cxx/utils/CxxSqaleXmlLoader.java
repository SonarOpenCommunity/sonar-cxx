/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.utils;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.rule.RulesDefinition.DebtRemediationFunctions;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Map;

/**
 *
 * This is a copy of
 * http://grepcode.com/file_/repo1.maven.org/maven2/org.codehaus.sonar.sslr-squid-bridge/sslr-squid-bridge/2.6/org/sonar/squidbridge/rules/SqaleXmlLoader.java/?v=source.
 * Currently it is not possible to derive and extend from SqaleXmlLoader because
 * most methods are private.
 *
 * @todo check in later version if direct usage of SqaleXmlLoader is possible.
 */
public class CxxSqaleXmlLoader {

  private NewRepository repository;

  public CxxSqaleXmlLoader(NewRepository repository) {
    this.repository = repository;
  }

  public static void load(NewRepository repository, String xmlResourcePath) {
    new CxxSqaleXmlLoader(repository).loadXmlResource(xmlResourcePath);
  }

  public static void load(NewRepository repository, Reader reader) {
    new CxxSqaleXmlLoader(repository).loadXmlStream(reader);
  }

  public void loadXmlStream(Reader reader) {
    XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
    xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    // just so it won't try to load DTD in if there's DOCTYPE
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    SMInputFactory inputFactory = new SMInputFactory(xmlFactory);
    try {
      processRoot(reader, inputFactory);
    } catch (XMLStreamException e) {
      throw new IllegalStateException("SQALE XML is not valid: ", e);
    }
  }

  public void loadXmlResource(String resourcePath) {
    loadXmlStream(reader(resourcePath));
  }

  private void processRoot(Reader reader, SMInputFactory inputFactory) throws XMLStreamException {
    SMHierarchicCursor rootCursor = inputFactory.rootElementCursor(reader);
    rootCursor.advance();
    SMInputCursor charCursor = rootCursor.childElementCursor("chc");
    while (charCursor.getNext() != null) {
      SMInputCursor subCharCursor = charCursor.childElementCursor("chc");
      while (subCharCursor.getNext() != null) {
        processSubChar(subCharCursor);
      }
    }
  }

  private void processSubChar(SMInputCursor subCharCursor) throws XMLStreamException {
    String subCharName = null;
    SMInputCursor subCharChildCursor = subCharCursor.childElementCursor();
    while (subCharChildCursor.getNext() != null) {
      String childName = subCharChildCursor.getLocalName();
      if ("key".equals(childName)) {
        subCharName = subCharChildCursor.getElemStringValue();
      }
      if ("chc".equals(childName)) {
        processRule(subCharName, subCharChildCursor);
      }
    }
  }

  private void processRule(String subCharName, SMInputCursor ruleCursor) throws XMLStreamException {
    SMInputCursor ruleChildCursor = ruleCursor.childElementCursor();
    String ruleKey = null;
    String remediationFunction = null;
    String remediationFactor = null;
    String offset = null;
    while (ruleChildCursor.getNext() != null) {
      String childName = ruleChildCursor.getLocalName();
      if ("rule-key".equals(childName)) {
        ruleKey = ruleChildCursor.getElemStringValue();
      }
      if ("prop".equals(childName)) {
        Map<String, String> propMap = childrenMap(ruleChildCursor.childElementCursor());
        String key = propMap.get("key");
        if ("remediationFunction".equals(key)) {
          remediationFunction = propMap.get("txt");
        }
        if ("offset".equals(key)) {
          offset = timeValue(propMap);
        }
        if ("remediationFactor".equals(key)) {
          remediationFactor = timeValue(propMap);
        }
      }
    }
    NewRule rule = repository.rule(ruleKey);
    if (rule != null) {
      rule.setDebtSubCharacteristic(subCharName);
      rule.setDebtRemediationFunction(remediationFunction(
        rule.debtRemediationFunctions(), remediationFunction, offset, remediationFactor));
    }
  }

  private String timeValue(Map<String, String> propMap) {
    String timeUnit = propMap.get("txt");
    if ("mn".equals(timeUnit)) {
      timeUnit = "min";
    }
    String value = propMap.get("val");
    if (value.indexOf('.') > -1) {
      value = value.substring(0, value.indexOf('.'));
    }
    return value + timeUnit;
  }

  private DebtRemediationFunction remediationFunction(DebtRemediationFunctions functions,
    String remediationFunction, String offset, String remediationFactor) {
    if ("CONSTANT_ISSUE".equalsIgnoreCase(remediationFunction)) {
      return functions.constantPerIssue(offset);
    } else if ("LINEAR".equalsIgnoreCase(remediationFunction)) {
      return functions.linear(remediationFactor);
    } else if ("LINEAR_OFFSET".equalsIgnoreCase(remediationFunction)) {
      return functions.linearWithOffset(remediationFactor, offset);
    }
    return null;
  }

  private Map<String, String> childrenMap(SMInputCursor cursor) throws XMLStreamException {
    Map<String, String> map = Maps.newHashMap();
    while (cursor.getNext() != null) {
      map.put(cursor.getLocalName(), cursor.getElemStringValue());
    }
    return map;
  }

  private Reader reader(String resourcePath) {
    URL url = Resources.getResource(CxxSqaleXmlLoader.class, resourcePath);
    try {
      return Resources.newReaderSupplier(url, Charsets.UTF_8).getInput();
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not read " + resourcePath, e);
    }
  }

}
