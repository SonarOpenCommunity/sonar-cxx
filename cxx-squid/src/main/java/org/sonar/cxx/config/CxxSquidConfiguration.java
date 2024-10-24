/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Verifier;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.PathUtils;
import org.sonar.cxx.squidbridge.api.SquidConfiguration;

/**
 * Database for compile options.
 *
 * To analyze source code additional information like defines and includes are needed. Only then it is possible for the
 * preprocessor and parser to generate an complete abstract syntax tree.
 *
 * The class allows to store information as key/value pairs. The information is also arranged hierarchically. If an
 * information is not found on one level, the next higher level is searched. Additional information can be e.g. on file
 * level (translation unit), global or from sonar-project.properties.
 *
 * Pre-defined hierarchy (levels): PredefinedMacros, SonarProjectProperties, Global, Units
 *
 * With {@code add} the key/value pairs are added to the database. The level parameter defines the level on which the
 * data should be inserted. For level a predefined name can be used or a new one can be defined. If level is an
 * identifier, the information is created in an element with the level-name directly under root. If level is a path, the
 * information is stored on Units level.
 *
 * With {@code get} and {@code getValues} the information is read out again afterwards. {@code get} returns the first
 * found value for key, whereby the search starts on level. {@code getValues} collects all found values over all levels.
 * It starts with the given level and further found values are added to the end of the list.
 *
 * <code>
 * CompilationDatabase
 * |-- PredefinedMacros
 * |-- SonarProjectProperties
 * |-- Global
 * |   |-- Defines
 * |   |   |-- Value
 * |   |   |-- ...
 * |   |-- IncludeDirectories
 * |       |-- Value
 * |       |-- ...
 * |-- Units
 *     |-- File [path=...]
 *     |    |-- Defines
 *     |    |   |-- Value
 *     |    |   |-- ...
 *     |    |-- IncludeDirectories
 *     |        |-- Value
 *     |        |-- ...
 *     | -- File [path=...]
 *     | -- ...
 * </code>
 */
public class CxxSquidConfiguration extends SquidConfiguration {

  // Root
  public static final String ROOT = "CompilationDatabase";

  // Levels
  public static final String PREDEFINED_MACROS = "PredefinedMacros";
  public static final String SONAR_PROJECT_PROPERTIES = "SonarProjectProperties";
  public static final String GLOBAL = "Global";
  public static final String UNITS = "Units";

  // name of 'File' elements
  public static final String FILE = "File";
  public static final String ATTR_PATH = "path";

  // name of 'Value' elements
  public static final String VALUE = "Value";

  // SonarProjectProperties
  public static final String ERROR_RECOVERY_ENABLED = "ErrorRecoveryEnabled";
  public static final String CPD_IGNORE_LITERALS = "CpdIgnoreLiterals";
  public static final String CPD_IGNORE_IDENTIFIERS = "CpdIgnoreIdentifiers";
  public static final String FUNCTION_COMPLEXITY_THRESHOLD = "FunctionComplexityThreshold";
  public static final String FUNCTION_SIZE_THRESHOLD = "FunctionSizeThreshold";
  public static final String API_FILE_SUFFIXES = "ApiFileSuffixes";
  public static final String JSON_COMPILATION_DATABASE = "JsonCompilationDatabase";

  // Global/File Properties
  public static final String DEFINES = "Defines";
  public static final String INCLUDE_DIRECTORIES = "IncludeDirectories";
  public static final String FORCE_INCLUDES = "ForceIncludes";

  private static final Logger LOG = LoggerFactory.getLogger(CxxSquidConfiguration.class);

  // case-sensitive filesystem or not
  private static boolean isCaseSensitive = true;

  private final XPathFactory xFactory = XPathFactory.instance();
  private Document document;

  // index to speed up element access
  private final HashMap<String, Element> index = new HashMap<>();

  // defines order to search for key/value pairs: Units => Global => SonarProjectProperties => PredefinedMacros
  private final LinkedList<Element> parentList = new LinkedList<>();

  // base directory to resolve relative paths
  private String baseDir = "";

  public CxxSquidConfiguration() {
    this("", Charset.defaultCharset());
  }

  public CxxSquidConfiguration(String baseDir) {
    this(baseDir, Charset.defaultCharset());
  }

  /**
   * Ctor.
   *
   * Creates the initial hierarchy for the data storage.
   */
  public CxxSquidConfiguration(String baseDir, Charset encoding) {
    super(encoding);
    this.baseDir = baseDir;

    try {
      isCaseSensitive = fileSystemIsCaseSensitive();
    } catch (IOException e) {
      isCaseSensitive = true;
    }

    var root = new Element(ROOT);
    root.setAttribute(new Attribute("version", "1.0"));
    document = new Document(root);

    addLevelElement(root, PREDEFINED_MACROS);
    addLevelElement(root, SONAR_PROJECT_PROPERTIES);
    addLevelElement(root, GLOBAL);
    // UNITS must be first one in the parentList
    addLevelElement(root, UNITS);
  }

  /**
   * Reads the Squid configuration from a file.
   *
   * @param fileName The system-dependent filename.
   * @return true, if data could be read from the file.
   */
  public boolean readFromFile(String fileName) {
    SAXBuilder builder = new SAXBuilder(XMLReaders.NONVALIDATING);
    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    try {
      document = builder.build(fileName);
    } catch (JDOMException | IOException e) {
      LOG.debug("Can't read Squid configuration from file '{}': {}", fileName, e.getMessage(), e);
      return false;
    }

    // clear old element caches
    parentList.clear();
    index.clear();

    var root = document.getRootElement();
    addLevelElement(root, PREDEFINED_MACROS);
    addLevelElement(root, SONAR_PROJECT_PROPERTIES);
    addLevelElement(root, GLOBAL);
    // UNITS must be first one in the parentList
    addLevelElement(root, UNITS);

    return true;
  }

  /**
   * Writes the Squid configuration to a file.
   *
   * @param fileName The system-dependent filename.
   * @return true, if the data could be written to a file
   */
  public boolean writeToFile(String fileName) {
    try {
      try (FileWriter writer = new FileWriter(fileName)) {
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        outputter.output(document, writer);
      }
    } catch (IOException e) {
      LOG.debug("Can't write Squid configuration to file '{}': {}", fileName, e.getMessage(), e);
      return false;
    }
    return true;
  }

  /**
   * Does items on Units level exist?
   *
   * @return false if empty
   */
  public boolean isUnitsEmpty() {
    return parentList.getFirst().getContentSize() == 0;
  }

  /**
   * Add a single key/value pair (property) to the database.
   *
   * @param level The level parameter defines the level on which the data should be inserted. For level a predefined
   * name can be used or a new one can be defined. <br>
   * - If level is an identifier, the information is created in an element with the level-name directly under root.<br>
   * - If level is a path, the information is stored on Units level. In that case the level-string is normalized to
   * simplify the following search.
   * @param key the key to be placed into the database.
   * @param value the value corresponding to key. Several values can be assigned to one key. Internally a value-list for
   * key is created. The method can be called several times for this, but more effective is the method
   * {@code add(String, String, List<String>)}.
   */
  public void add(String level, String key, @Nullable String value) {
    if (value != null && !value.isEmpty()) {
      Element eKey = getKey(level, key);
      setValue(eKey, value);
    }
  }

  /**
   * Add a single key/value pair (property) to the database.
   *
   * Same as {@code add(String, String, String)} for an {@code Optional<String>}.
   *
   * @param level defines the level on which the data should be inserted
   * @param key the key to be placed into the database
   * @param value the value corresponding to key
   */
  public void add(String level, String key, Optional<String> value) {
    if (value.isPresent()) {
      Element eKey = getKey(level, key);
      setValue(eKey, value.get());
    }
  }

  /**
   * Add key/value pairs (properties) from an array to the database.
   *
   * Same as {@code add(String, String, String)} for an array of values.
   *
   * @param level defines the level on which the data should be inserted
   * @param key the key to be placed into the database
   * @param values the values corresponding to key
   */
  public void add(String level, String key, @Nullable String[] values) {
    if (values != null) {
      Element eKey = getKey(level, key);
      for (var value : values) {
        setValue(eKey, value);
      }
    }
  }

  /**
   * Add key/value pairs (properties) from a list to the database.
   *
   * Same as {@code add(String, String, String)} for a list of values.
   *
   * @param level defines the level on which the data should be inserted
   * @param key the key to be placed into the database
   * @param values the values corresponding to key
   */
  public void add(String level, String key, List<String> values) {
    if (!values.isEmpty()) {
      Element eKey = getKey(level, key);
      for (var value : values) {
        setValue(eKey, value);
      }
    }
  }

  /**
   * Searches for the property with the specified key.
   *
   * The first occurrence of a single value is searched for. The search is started at the specified level and if no
   * entry is found, it is continued to the higher level. The method can return {@code Optional#empty()} if the property
   * is not set.
   *
   * @param level level at which the search is started
   * @param key the property key
   * @return The value in this property list with the specified key value. Can return {@code Optional#empty()} if the
   * property is not set.
   */
  public Optional<String> get(String level, String key) {
    Element eLevel = findLevel(level, parentList.getFirst());
    do {
      if (eLevel != null) {
        Element eKey = eLevel.getChild(key);
        if (eKey != null) {
          return Optional.of(eKey.getChildText(VALUE));
        }
      }
      eLevel = getParentElement(eLevel);
    } while (eLevel != null);
    return Optional.empty();
  }

  /**
   * Used to read multi-valued properties from one level.
   *
   * The method can return an empty list if the property is not set.
   *
   * @param level level to read
   * @param key key that is searched for
   * @return the values with the specified key value
   */
  public List<String> getLevelValues(String level, String key) {
    List<String> result = new ArrayList<>();
    Element eLevel = findLevel(level, null);
    if (eLevel != null) {
      Element eKey = eLevel.getChild(key);
      if (eKey != null) {
        for (var value : eKey.getChildren(VALUE)) {
          result.add(value.getText());
        }
      }
    }

    return result;
  }

  /**
   * Used to read multi-valued properties.
   *
   * Collects all found values over all levels. It starts with the given level and further found values in parent levels
   * are added to the end of the list. The method can return an empty list if the property is not set.
   *
   * @param level level at which the search is started
   * @param key key that is searched for
   * @return the values with the specified key value
   */
  public List<String> getValues(String level, String key) {
    List<String> result = new ArrayList<>();
    Element eLevel = findLevel(level, parentList.getFirst());
    do {
      if (eLevel != null) {
        Element eKey = eLevel.getChild(key);
        if (eKey != null) {
          for (var value : eKey.getChildren(VALUE)) {
            result.add(value.getText());
          }
        }
      }
      eLevel = getParentElement(eLevel);
    } while (eLevel != null);
    return result;
  }

  /**
   * Read all file items from the database.
   *
   * @return list of file items
   */
  public List<Path> getFiles() {
    List<Path> result = new ArrayList<>();
    Element eLevel = findLevel(UNITS, null);
    if (eLevel != null) {
      for (var file : eLevel.getChildren(FILE)) {
        result.add(Path.of(file.getAttributeValue(ATTR_PATH)));
      }
    }
    return result;
  }

  /**
   * Used to read multi-valued properties.
   *
   * Collects all found values over all children. Further found values in parent levels are added to the end of the
   * list. The method can return an empty list if the property is not set.
   *
   * @param level start level from which the values of all children are returned
   * @param key property key that is searched for in all children
   * @return the values with the specified key value
   */
  public List<String> getChildrenValues(String level, String key) {
    List<String> result = new ArrayList<>();
    Element eLevel = findLevel(level, parentList.getFirst());
    if (eLevel != null) {
      for (var child : eLevel.getChildren()) {
        Element eKey = child.getChild(key);
        if (eKey != null) {
          for (var value : eKey.getChildren(VALUE)) {
            result.add(value.getText());
          }
        }
      }
    }
    // add content of shared parents only once at the end
    eLevel = getParentElement(eLevel);
    if (eLevel != null) {
      result.addAll(getValues(eLevel.getName(), key));
    }
    return result;
  }

  /**
   * Effective value as boolean.
   *
   * @return {@code true} if the effective value is {@code "true"}, {@code false} for any other non empty value. If the
   * property does not have value nor default value, then {@code empty} is returned.
   */
  public Optional<Boolean> getBoolean(String level, String key) {
    return get(level, key).map(String::trim).map(Boolean::parseBoolean);
  }

  /**
   * Effective value as {@code int}.
   *
   * @return the value as {@code int}. If the property does not have value nor default value, then {@code empty} is
   * returned.
   * @throws IllegalStateException if value is not empty and is not a parsable integer
   */
  public Optional<Integer> getInt(String level, String key) {
    try {
      return get(level, key).map(String::trim).map(Integer::parseInt);
    } catch (NumberFormatException e) {
      throw new IllegalStateException(
        String.format("The property '%s' is not an int value: %s", key, e.getMessage()), e
      );
    }
  }

  /**
   * Effective value as {@code long}.
   *
   * @return the value as {@code long}. If the property does not have value nor default value, then {@code empty} is
   * returned.
   * @throws IllegalStateException if value is not empty and is not a parsable {@code long}
   */
  public Optional<Long> getLong(String level, String key) {
    try {
      return get(level, key).map(String::trim).map(Long::parseLong);
    } catch (NumberFormatException e) {
      throw new IllegalStateException(
        String.format("The property '%s' is not an long value: %s", key, e.getMessage()), e
      );
    }
  }

  /**
   * Effective value as {@code Float}.
   *
   * @return the value as {@code Float}. If the property does not have value nor default value, then {@code empty} is
   * returned.
   * @throws IllegalStateException if value is not empty and is not a parsable number
   */
  public Optional<Float> getFloat(String level, String key) {
    try {
      return get(level, key).map(String::trim).map(Float::valueOf);
    } catch (NumberFormatException e) {
      throw new IllegalStateException(
        String.format("The property '%s' is not an float value: %s", key, e.getMessage()), e
      );
    }
  }

  /**
   * Effective value as {@code Double}.
   *
   * @return the value as {@code Double}. If the property does not have value nor default value, then {@code empty} is
   * returned.
   * @throws IllegalStateException if value is not empty and is not a parsable number
   */
  public Optional<Double> getDouble(String level, String key) {
    try {
      return get(level, key).map(String::trim).map(Double::valueOf);
    } catch (NumberFormatException e) {
      throw new IllegalStateException(
        String.format("The property '%s' is not an double value: %s", key, e.getMessage()), e
      );
    }
  }

  /**
   * Returns a string representation of the object: XML/UTF-8 encoded.
   *
   * @return object XML encoded
   */
  @Override
  public String toString() {
    var stream = new ByteArrayOutputStream();
    try {
      var outputter = new XMLOutputter();
      outputter.setFormat(Format.getPrettyFormat());
      outputter.output(document, stream);
    } catch (IOException e) {
      throw new IllegalStateException("Can't create XML data", e);
    }
    return stream.toString(StandardCharsets.UTF_8);
  }

  public String getBaseDir() {
    return baseDir;
  }

  public void readMsBuildFiles(List<File> logFiles, String charsetName) {
    MsBuild msBuild = new MsBuild(this);
    for (var logFile : logFiles) {
      if (logFile.exists()) {
        msBuild.parse(logFile, baseDir, charsetName);
      } else {
        LOG.error("MsBuild log file not found: '{}'", logFile.getAbsolutePath());
      }
    }
  }

  public void readJsonCompilationDb() {
    var jsonDbFile = get(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES,
      CxxSquidConfiguration.JSON_COMPILATION_DATABASE);
    if (jsonDbFile.isPresent()) {
      try {
        var jsonDb = new JsonCompilationDatabase(this);
        jsonDb.parse(new File(jsonDbFile.get()));
      } catch (IOException e) {
        LOG.error("Cannot access Json DB File: {}", e.getMessage(), e);
      }
    }
  }

  /**
   * Check if file system is case sensitive
   *
   * @return true if running on a case sensitive filesystem
   * @throws IOException
   */
  public static boolean fileSystemIsCaseSensitive() throws IOException {
    Path a = null;
    Path b = null;
    try {
      Path tempDir = Files.createTempDirectory("test");
      a = Files.createFile(tempDir.resolve(Paths.get("test.test")));
      b = Files.createFile(tempDir.resolve(Paths.get("TEST.TEST")));
    } catch (FileAlreadyExistsException e) {
      return false;
    } finally {
      Files.deleteIfExists(a);
      if (b != null) {
        Files.deleteIfExists(b);
      }
    }
    return true;
  }

  /**
   * Create uniform notation of path names.
   *
   * Normalize path and replace file separators by forward slash. Use lowercase path on case insensitive file systems.
   *
   * @param path to unify
   * @return unified path
   */
  private static String unifyPath(String path) {
    String result = PathUtils.sanitize(path);
    if (result == null) {
      result = "unknown";
    }
    if (isCaseSensitive) {
      return result;
    } else {
      return result.toLowerCase();
    }
  }

  /**
   * Method that returns any parent {@code Element} for this Element, or null if the Element is unattached or is a root
   * Element.
   *
   * The method first searches in {@code parentList}. If the own Element is found here, the next entry (parent) is
   * returned. If nothing is found here, the method returns parent of Element.
   *
   * @param element to be searched for parent
   * @return the containing Element or null if unattached or a root Element
   */
  @CheckForNull
  private Element getParentElement(@Nullable Element element) {
    var parentIterator = parentList.iterator();
    while (parentIterator.hasNext()) {
      var next = parentIterator.next();
      if (next.equals(element)) {
        if (parentIterator.hasNext()) {
          return parentIterator.next();
        } else {
          break;
        }
      }
    }
    return element != null ? element.getParentElement() : null;
  }

  /**
   * Searches for Element associated with level.
   *
   * If level is an identifier, level element is searched for. Otherwise it is searched for a File element with path
   * attribute level.
   *
   * @param level to search for
   * @param defaultElement Element to return if no item was found
   * @return found Element or defaultElement
   */
  @CheckForNull
  private Element findLevel(String level, @Nullable Element defaultElement) {
    Element element = index.getOrDefault(level, null);
    if (element != null) {
      return element;
    }
    String xpath;
    if (Verifier.checkElementName(level) == null) {
      xpath = "/" + ROOT + "/" + level;
    } else {
      // handle special case 'UNITS empty' no need to search in tree
      if (isUnitsEmpty()) {
        return defaultElement;
      }
      xpath = "/" + ROOT + "/" + UNITS + "/" + FILE + "[@" + ATTR_PATH + "='" + unifyPath(level) + "']";
    }
    XPathExpression<Element> expr = xFactory.compile(xpath, Filters.element());
    element = expr.evaluateFirst(document);
    if (element == null) {
      element = defaultElement;
    }
    return element;
  }

  /**
   * Add or reuse an key Element.
   *
   * @param level for key
   * @param key identifier of key
   * @return existing or new Element for key
   */
  private Element getKey(String level, String key) {
    Element eLevel = findLevel(level, null);
    if (eLevel == null) {
      if (Verifier.checkElementName(level) == null) {
        eLevel = new Element(level);
        document.getRootElement().addContent(eLevel);
      } else {
        eLevel = new Element(FILE);
        level = unifyPath(level);
        eLevel.setAttribute(new Attribute(ATTR_PATH, level));
        parentList.getFirst().addContent(eLevel);
      }
      index.put(level, eLevel);
    }
    Element eKey = eLevel.getChild(key);
    if (eKey == null) {
      eKey = new Element(key);
      eLevel.addContent(eKey);
    }
    return eKey;
  }

  /**
   * Add level element to Squid structure.
   *
   * If level already exists only the index and parentList will be updated, otherwise new element will be created and
   * inserted.
   *
   * @param root root element to add new level below
   * @param level level string
   */
  private void addLevelElement(Element root, String level) {
    var element = findLevel(level, null);
    if (element == null) {
      element = new Element(level);
      root.addContent(element);
    }
    index.put(level, element);
    parentList.addFirst(element);
  }

  /**
   * Add a value to a key.
   *
   * @param key to add the value
   * @param value to add
   */
  private static void setValue(Element key, String value) {
    var eValue = new Element(VALUE);
    eValue.setText(value);
    key.addContent(eValue);
  }

}
