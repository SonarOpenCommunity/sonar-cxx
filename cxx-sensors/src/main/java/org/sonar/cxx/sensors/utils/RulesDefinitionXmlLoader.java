/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
// copy of https://github.com/SonarSource/sonar-plugin-api/blob/master/plugin-api/src/main/java/org/sonar/api/server/rule/RulesDefinitionXmlLoader.java
// Don't remove copyright below!

/*
 * Sonar Plugin API
 * Copyright (C) 2009-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import static java.lang.String.format;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.StringUtils;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trim;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Cardinality;
import org.sonarsource.api.sonarlint.SonarLintSide;

/**
 * Loads definitions of rules from a XML file.
 *
 * <h3>Usage</h3>
 * <pre>
 * public class MyJsRulesDefinition implements RulesDefinition {
 *
 *   private static final String PATH = "my-js-rules.xml";
 *   private final RulesDefinitionXmlLoader xmlLoader;
 *
 *   public MyJsRulesDefinition(RulesDefinitionXmlLoader xmlLoader) {
 *     this.xmlLoader = xmlLoader;
 *   }
 *
 *   {@literal @}Override
 *   public void define(Context context) {
 *     try (Reader reader = new InputStreamReader(getClass().getResourceAsStream(PATH), StandardCharsets.UTF_8)) {
 *       NewRepository repository = context.createRepository("my_js", "js").setName("My Javascript Analyzer");
 *       xmlLoader.load(repository, reader);
 *       repository.done();
 *     } catch (IOException e) {
 *       throw new IllegalStateException(String.format("Fail to read file %s", PATH), e);
 *     }
 *   }
 * }
 * </pre>
 *
 * <h3>XML Format</h3>
 * <pre>
 * &lt;rules&gt;
 *   &lt;rule&gt;
 *     &lt;!-- Required key. Max length is 200 characters. --&gt;
 *     &lt;key&gt;the-rule-key&lt;/key&gt;
 *
 *     &lt;!-- Required name. Max length is 200 characters. --&gt;
 *     &lt;name&gt;The purpose of the rule&lt;/name&gt;
 *
 *     &lt;!-- Required description. No max length. --&gt;
 *     &lt;description&gt;
 *       &lt;![CDATA[The description]]&gt;
 *     &lt;/description&gt;
 *     &lt;!-- Optional format of description. Supported values are HTML (default). --&gt;
 *     &lt;descriptionFormat&gt;HTML&lt;/descriptionFormat&gt;
 *
 *     &lt;!-- Optional key for configuration of some rule engines --&gt;
 *     &lt;internalKey&gt;Checker/TreeWalker/LocalVariableName&lt;/internalKey&gt;
 *
 *     &lt;!-- Default severity when enabling the rule in a Quality profile.  --&gt;
 *     &lt;!-- Possible values are INFO, MINOR, MAJOR (default), CRITICAL, BLOCKER. --&gt;
 *     &lt;severity&gt;BLOCKER&lt;/severity&gt;
 *
 *     &lt;!-- Possible values are SINGLE (default) and MULTIPLE for template rules --&gt;
 *     &lt;cardinality&gt;SINGLE&lt;/cardinality&gt;
 *
 *     &lt;!-- Status displayed in rules console. Possible values are BETA, READY (default), DEPRECATED. --&gt;
 *     &lt;status&gt;BETA&lt;/status&gt;
 *
 *     &lt;!-- Type as defined by the SonarQube Quality Model. Possible values are CODE_SMELL (default),
 *             BUG and VULNERABILITY.--&gt;
 *     &lt;type&gt;BUG&lt;/type&gt;
 *
 *     &lt;!-- Optional tags. See org.sonar.api.server.rule.RuleTagFormat.
 *             The maximal length of all tags is 4000 characters. --&gt;
 *     &lt;tag&gt;misra&lt;/tag&gt;
 *     &lt;tag&gt;multi-threading&lt;/tag&gt;
 *
 *     &lt;!-- Optional deprecated rules keys for this rule.
 *             See org.sonar.api.server.rule.RulesDefinition.Rule.deprecatedRuleKeys
 *             If you want to rename the key of a rule register the rule's previous key. This will allow SonarQube to
 *             support "issue re-keying" for this rule. Several deprecated keys can be provided to allow SonarQube to
 *             support several key changes across multiple versions of a plugin. --&gt;
 *     &lt;deprecatedKey&gt;deprecatedKey1&lt;/deprecatedKey&gt;
 *     &lt;deprecatedKey&gt;deprecatedKey2&lt;/deprecatedKey&gt;
 *
 *     &lt;!-- Optional parameters --&gt;
 *     &lt;param&gt;
 *       &lt;!-- Required key. Max length is 128 characters. --&gt;
 *       &lt;key&gt;the-param-key&lt;/key&gt;
 *       &lt;description&gt;
 *         &lt;![CDATA[the optional description, in HTML format. Max length is 4000 characters.]]&gt;
 *       &lt;/description&gt;
 *       &lt;!-- Optional default value, used when enabling the rule in a Quality profile.
 *               Max length is 4000 characters. --&gt;
 *       &lt;defaultValue&gt;42&lt;/defaultValue&gt;
 *     &lt;/param&gt;
 *     &lt;param&gt;
 *       &lt;key&gt;another-param&lt;/key&gt;
 *     &lt;/param&gt;
 *
 *     &lt;!-- Quality Model - type of debt remediation function --&gt;
 *     &lt;!-- See enum {@link org.sonar.api.server.debt.DebtRemediationFunction.Type} for supported values --&gt;
 *     &lt;!-- Default value is 'CONSTANT_ISSUE' for all severities except 'INFO' --&gt;
 *     &lt;!-- It was previously named 'debtRemediationFunction'. --&gt;
 *     &lt;!-- Since 5.5 --&gt;
 *     &lt;remediationFunction&gt;LINEAR_OFFSET&lt;/remediationFunction&gt;
 *
 *     &lt;!-- Quality Model - raw description of the "gap", used for some types of remediation functions. --&gt;
 *     &lt;!-- See {@link org.sonar.api.server.rule.RulesDefinition.NewRule#setGapDescription(String)} --&gt;
 *     &lt;!-- It was previously named 'effortToFixDescription'. --&gt;
 *     &lt;!-- Since 5.5 --&gt;
 *     &lt;gapDescription&gt;Effort to test one uncovered condition&lt;/gapFixDescription&gt;
 *
 *     &lt;!-- Quality Model - gap multiplier of debt remediation function.
 *             Must be defined only for some function types. --&gt;
 *     &lt;!-- See {@link org.sonar.api.server.rule.RulesDefinition.DebtRemediationFunctions} --&gt;
 *     &lt;!-- It was previously named 'debtRemediationFunctionCoefficient'. --&gt;
 *     &lt;!-- Since 5.5 --&gt;
 *     &lt;remediationFunctionGapMultiplier&gt;10min&lt;/remediationFunctionGapMultiplier&gt;
 *
 *     &lt;!-- Quality Model - base effort of debt remediation function.
 *             Must be defined only for some function types. --&gt;
 *     &lt;!-- See {@link org.sonar.api.server.rule.RulesDefinition.DebtRemediationFunctions} --&gt;
 *     &lt;!-- Default value is '5min' for 'CONSTANT_ISSUE' --&gt;
 *     &lt;!-- It was previously named 'debtRemediationFunctionOffset'. --&gt;
 *     &lt;!-- Since 5.5 --&gt;
 *     &lt;remediationFunctionBaseEffort&gt;2min&lt;/remediationFunctionBaseEffort&gt;
 *
 *     &lt;!-- Deprecated field, replaced by "internalKey" --&gt;
 *     &lt;configKey&gt;Checker/TreeWalker/LocalVariableName&lt;/configKey&gt;
 *
 *     &lt;!-- Deprecated field, replaced by "severity" --&gt;
 *     &lt;priority&gt;BLOCKER&lt;/priority&gt;
 *
 *   &lt;/rule&gt;
 * &lt;/rules&gt;
 * </pre>
 *
 * <h3>XML Example</h3>
 * <pre>
 * &lt;rules&gt;
 *   &lt;rule&gt;
 *     &lt;key&gt;S1442&lt;/key&gt;
 *     &lt;name&gt;"alert(...)" should not be used&lt;/name&gt;
 *     &lt;description&gt;alert(...) can be useful for debugging during development, but ...&lt;/description&gt;
 *     &lt;tag&gt;cwe&lt;/tag&gt;
 *     &lt;tag&gt;security&lt;/tag&gt;
 *     &lt;tag&gt;user-experience&lt;/tag&gt;
 *     &lt;debtRemediationFunction&gt;CONSTANT_ISSUE&lt;/debtRemediationFunction&gt;
 *     &lt;remediationFunctionBaseEffort&gt;5min&lt;/remediationFunctionBaseEffort&gt;
 *   &lt;/rule&gt;
 *
 *   &lt;!-- another rules... --&gt;
 * &lt;/rules&gt;
 * </pre>
 *
 * @see org.sonar.api.server.rule.RulesDefinition
 * @since 4.3
 */
@ServerSide
@ComputeEngineSide
@SonarLintSide
public class RulesDefinitionXmlLoader {

  private static final String ELEMENT_RULES = "rules";
  private static final String ELEMENT_RULE = "rule";
  private static final String ELEMENT_PARAM = "param";

  private enum DescriptionFormat {
    HTML
  }

  /**
   * Loads rules by reading the XML input stream. The input stream is not always closed by the method, so it should be
   * handled by the caller.
   *
   * @since 4.3
   */
  public void load(RulesDefinition.NewRepository repo, InputStream input, String encoding) {
    load(repo, input, Charset.forName(encoding));
  }

  /**
   * @since 5.1
   */
  public void load(RulesDefinition.NewRepository repo, InputStream input, Charset charset) {
    try (Reader reader = new InputStreamReader(
      BOMInputStream.builder()
        .setInputStream(input)
        .setInclude(false)
        .setByteOrderMarks(
          ByteOrderMark.UTF_8,
          ByteOrderMark.UTF_16LE,
          ByteOrderMark.UTF_16BE,
          ByteOrderMark.UTF_32LE,
          ByteOrderMark.UTF_32BE
        ).get(),
      charset)) {
      load(repo, reader);
    } catch (IOException e) {
      throw new IllegalStateException("Error while reading XML rules definition for repository " + repo.key(), e);
    }
  }

  /**
   * Loads rules by reading the XML input stream. The reader is not closed by the method, so it should be handled by the
   * caller.
   *
   * @since 4.3
   */
  public void load(RulesDefinition.NewRepository repo, Reader inputReader) {
    XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
    xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    // just so it won't try to load DTD in if there's DOCTYPE
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    try {
      final XMLEventReader reader = xmlFactory.createXMLEventReader(inputReader);
      while (reader.hasNext()) {
        final XMLEvent event = reader.nextEvent();
        if (event.isStartElement() && event.asStartElement().getName()
          .getLocalPart().equals(ELEMENT_RULES)) {
          parseRules(repo, reader);
        }
      }
    } catch (XMLStreamException e) {
      throw new IllegalStateException("XML is not valid", e);
    }
  }

  private static void parseRules(RulesDefinition.NewRepository repo, XMLEventReader reader) throws XMLStreamException {
    while (reader.hasNext()) {
      final XMLEvent event = reader.nextEvent();
      if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(ELEMENT_RULES)) {
        return;
      }
      if (event.isStartElement()) {
        final StartElement element = event.asStartElement();
        final String elementName = element.getName().getLocalPart();
        if (ELEMENT_RULE.equals(elementName)) {
          processRule(repo, element, reader);
        }
      }
    }
  }

  private static void processRule(RulesDefinition.NewRepository repo, StartElement ruleElement, XMLEventReader reader)
    throws XMLStreamException {
    String key = null;
    String name = null;
    String description = null;
    // enum is not used as variable type as we want to raise an exception with the rule key when format is not supported
    String descriptionFormat = DescriptionFormat.HTML.name();
    String internalKey = null;
    String severity = Severity.defaultSeverity();
    String type = null;
    RuleStatus status = RuleStatus.defaultStatus();
    boolean template = false;
    String gapDescription = null;
    String debtRemediationFunction = null;
    String remediationFunctionBaseEffort = null;
    String remediationFunctionGapMultiplier = null;
    List<ParamStruct> params = new ArrayList<>();
    List<String> tags = new ArrayList<>();
    List<String> deprecatedKeys = new ArrayList<>();

    /* BACKWARD COMPATIBILITY WITH VERY OLD FORMAT */
    Attribute keyAttribute = ruleElement.getAttributeByName(new QName("key"));
    if (keyAttribute != null && StringUtils.isNotBlank(keyAttribute.getValue())) {
      key = trim(keyAttribute.getValue());
    }
    Attribute priorityAttribute = ruleElement.getAttributeByName(new QName("priority"));
    if (priorityAttribute != null && StringUtils.isNotBlank(priorityAttribute.getValue())) {
      severity = trim(priorityAttribute.getValue());
    }

    while (reader.hasNext()) {
      final XMLEvent event = reader.nextEvent();
      if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(ELEMENT_RULE)) {
        if ((!"INFO".equals(severity)) && (debtRemediationFunction == null)) {
          debtRemediationFunction = "CONSTANT_ISSUE";
          if (remediationFunctionBaseEffort == null) {
            remediationFunctionBaseEffort = "5min";
          }
        }
        buildRule(repo, key, name, description, descriptionFormat, internalKey, severity, type, status, template,
          gapDescription, debtRemediationFunction, remediationFunctionBaseEffort,
          remediationFunctionGapMultiplier, params, tags, deprecatedKeys);
        return;
      }
      if (event.isStartElement()) {
        final StartElement element = event.asStartElement();
        final String elementName = element.getName().getLocalPart();
        if ("name".equalsIgnoreCase(elementName)) {
          name = StringUtils.trim(reader.getElementText());
        } else if ("type".equalsIgnoreCase(elementName)) {
          type = StringUtils.trim(reader.getElementText());
        } else if ("description".equalsIgnoreCase(elementName)) {
          description = StringUtils.trim(reader.getElementText());
        } else if ("descriptionFormat".equalsIgnoreCase(elementName)) {
          descriptionFormat = StringUtils.trim(reader.getElementText());
        } else if ("key".equalsIgnoreCase(elementName)) {
          key = StringUtils.trim(reader.getElementText());
        } else if ("configKey".equalsIgnoreCase(elementName)) {
          // deprecated field, replaced by internalKey
          internalKey = StringUtils.trim(reader.getElementText());
        } else if ("internalKey".equalsIgnoreCase(elementName)) {
          internalKey = StringUtils.trim(reader.getElementText());
        } else if ("priority".equalsIgnoreCase(elementName) || "severity".equalsIgnoreCase(elementName)) {
          // "priority" is deprecated field and has been replaced by "severity"
          severity = StringUtils.trim(reader.getElementText());
        } else if ("cardinality".equalsIgnoreCase(elementName)) {
          template = Cardinality.MULTIPLE == Cardinality.valueOf(StringUtils.trim(reader.getElementText()));
        } else if ("gapDescription".equalsIgnoreCase(elementName) || "effortToFixDescription".equalsIgnoreCase(
          elementName)) {
          gapDescription = StringUtils.trim(reader.getElementText());
        } else if ("remediationFunction".equalsIgnoreCase(elementName) || "debtRemediationFunction".equalsIgnoreCase(
          elementName)) {
          debtRemediationFunction = StringUtils.trim(reader.getElementText());
        } else if ("remediationFunctionBaseEffort".equalsIgnoreCase(elementName) || "debtRemediationFunctionOffset"
          .equalsIgnoreCase(elementName)) {
          remediationFunctionBaseEffort = StringUtils.trim(reader.getElementText());
        } else if ("remediationFunctionGapMultiplier".equalsIgnoreCase(elementName)
          || "debtRemediationFunctionCoefficient".equalsIgnoreCase(elementName)) {
          remediationFunctionGapMultiplier = StringUtils.trim(reader.getElementText());
        } else if ("status".equalsIgnoreCase(elementName)) {
          String s = StringUtils.trim(reader.getElementText());
          if (s != null) {
            status = RuleStatus.valueOf(s);
          }
        } else if (ELEMENT_PARAM.equalsIgnoreCase(elementName)) {
          params.add(processParameter(element, reader));
        } else if ("tag".equalsIgnoreCase(elementName)) {
          tags.add(StringUtils.trim(reader.getElementText()));
        } else if ("deprecatedKey".equalsIgnoreCase(elementName)) {
          deprecatedKeys.add(StringUtils.trim(reader.getElementText()));
        }
      }
    }
  }

  private static void buildRule(RulesDefinition.NewRepository repo, String key, String name,
    @Nullable String description,
    String descriptionFormat, @Nullable String internalKey, String severity,
    @Nullable String type, RuleStatus status,
    boolean template, @Nullable String gapDescription,
    @Nullable String debtRemediationFunction,
    @Nullable String debtRemediationFunctionBaseEffort,
    @Nullable String debtRemediationFunctionGapMultiplier, List<ParamStruct> params,
    List<String> tags,
    List<String> deprecatedKeys) {
    try {
      RulesDefinition.NewRule rule = repo.createRule(key)
        .setSeverity(severity)
        .setName(name)
        .setInternalKey(internalKey)
        .setTags(tags.toArray(String[]::new))
        .setTemplate(template)
        .setStatus(status)
        .setGapDescription(gapDescription);
      if (type != null) {
        rule.setType(RuleType.valueOf(type));
      }
      fillDescription(rule, descriptionFormat, description);
      fillRemediationFunction(rule, debtRemediationFunction, debtRemediationFunctionGapMultiplier,
        debtRemediationFunctionBaseEffort);
      fillParams(rule, params);
      fillDeprecatedKeys(repo, rule, deprecatedKeys);
    } catch (Exception e) {
      throw new IllegalStateException(format("Fail to load the rule with key [%s:%s]", repo.key(), key), e);
    }
  }

  private static void fillDescription(RulesDefinition.NewRule rule, String descriptionFormat,
    @Nullable String description) {
    if (isNotBlank(description)) {
      if (DescriptionFormat.valueOf(descriptionFormat) == DescriptionFormat.HTML) {
        rule.setHtmlDescription(description);
      } else {
        throw new IllegalArgumentException("Value of descriptionFormat is not supported: " + descriptionFormat);
      }
    }
  }

  private static void fillRemediationFunction(RulesDefinition.NewRule rule, @Nullable String debtRemediationFunction,
    @Nullable String functionCoeff, @Nullable String functionOffset) {
    if (isNotBlank(debtRemediationFunction)) {
      DebtRemediationFunction.Type functionType = DebtRemediationFunction.Type.valueOf(debtRemediationFunction);
      rule.setDebtRemediationFunction(rule.debtRemediationFunctions()
        .create(functionType, functionCoeff, functionOffset));
    }
  }

  private static void fillParams(RulesDefinition.NewRule rule, List<ParamStruct> params) {
    for (var param : params) {
      rule.createParam(param.key)
        .setDefaultValue(param.defaultValue)
        .setType(param.type)
        .setDescription(param.description);
    }
  }

  /**
   * @since cxx plugin 2.2.0
   */
  private static void fillDeprecatedKeys(RulesDefinition.NewRepository repo,
    RulesDefinition.NewRule rule, List<String> deprecatedKeys) {
    for (var deprecatedKey : deprecatedKeys) {
      rule.addDeprecatedRuleKey(repo.key(), deprecatedKey);
    }
  }

  private static class ParamStruct {

    private String key = "";
    private String description = "";
    private String defaultValue = "";
    private RuleParamType type = RuleParamType.STRING;
  }

  private static ParamStruct processParameter(StartElement paramElement, XMLEventReader reader) throws
    XMLStreamException {
    ParamStruct param = new ParamStruct();

    // BACKWARD COMPATIBILITY WITH DEPRECATED FORMAT
    Attribute keyAttribute = paramElement.getAttributeByName(new QName("key"));
    if (keyAttribute != null && StringUtils.isNotBlank(keyAttribute.getValue())) {
      param.key = StringUtils.trim(keyAttribute.getValue());
    }

    // BACKWARD COMPATIBILITY WITH DEPRECATED FORMAT
    Attribute typeAttribute = paramElement.getAttributeByName(new QName("type"));
    if (typeAttribute != null && StringUtils.isNotBlank(typeAttribute.getValue())) {
      param.type = RuleParamType.parse(StringUtils.trim(typeAttribute.getValue()));
    }

    while (reader.hasNext()) {
      final XMLEvent event = reader.nextEvent();
      if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(ELEMENT_PARAM)) {
        return param;
      }
      if (event.isStartElement()) {
        final StartElement element = event.asStartElement();
        final String elementName = element.getName().getLocalPart();
        if ("key".equalsIgnoreCase(elementName)) {
          param.key = StringUtils.trim(reader.getElementText());
        } else if ("description".equalsIgnoreCase(elementName)) {
          param.description = StringUtils.trim(reader.getElementText());
        } else if ("type".equalsIgnoreCase(elementName)) {
          param.type = RuleParamType.parse(StringUtils.trim(reader.getElementText()));
        } else if ("defaultValue".equalsIgnoreCase(elementName)) {
          param.defaultValue = StringUtils.trim(reader.getElementText());
        }
      }
    }
    return param;
  }
}
