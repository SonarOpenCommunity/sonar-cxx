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
package org.sonar.cxx.sensors.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.server.rule.RulesDefinition;

/**
 * {@inheritDoc}
 */
public class RulesDefinitionXml implements RulesDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(RulesDefinitionXml.class);

  private final ServerFileSystem fileSystem;
  private final RulesDefinitionXmlLoader xmlRuleLoader;
  private final String repositoryLanguage;
  private final String repositoryKey;
  private final String repositoryName;
  private final String repositoryFile;

  /**
   * {@inheritDoc}
   */
  protected RulesDefinitionXml(
    ServerFileSystem fileSystem,
    RulesDefinitionXmlLoader xmlRuleLoader,
    String repositoryLanguage,
    String repositoryKey,
    String repositoryName,
    String repositoryFile) {
    this.fileSystem = fileSystem;
    this.xmlRuleLoader = xmlRuleLoader;
    this.repositoryLanguage = repositoryLanguage;
    this.repositoryKey = repositoryKey;
    this.repositoryName = repositoryName;
    this.repositoryFile = repositoryFile;
  }

  @Override
  public void define(Context context) {
    Charset encoding = StandardCharsets.UTF_8;
    var repository = context.createRepository(repositoryKey, repositoryLanguage)
      .setName(repositoryName);

    var xmlLoader = new RulesDefinitionXmlLoader();
    if (!"".equals(repositoryFile)) {
      var xmlStream = getClass().getResourceAsStream(repositoryFile);
      xmlLoader.load(repository, xmlStream, encoding);

      for (var userExtensionXml : getExtensions(repositoryKey, "xml")) {
        try (var input = java.nio.file.Files.newInputStream(userExtensionXml.toPath())) {
          xmlRuleLoader.load(repository, input, encoding);
        } catch (IOException | IllegalStateException e) {
          LOG.error("Cannot load Rules Definions '{}'", e.getMessage(), e);
        }
      }

      // add repository key as tag to make it possible to filter in issues by tool (tag must be a-z,0-9,-,+)
      String tag = repositoryKey.toLowerCase().replaceAll("[^a-z0-9-+]", "-");
      for (var rule : repository.rules()) {
        prepareRule(rule);
        rule.addTags(tag);
      }
    }

    repository.done();
  }

  public List<File> getExtensions(String dirName, @Nullable String... suffixes) {
    var dir = new File(fileSystem.getHomeDir(), "extensions/rules/" + dirName);
    var files = new ArrayList<File>();
    if (dir.exists() && dir.isDirectory()) {
      if (suffixes != null && suffixes.length > 0) {
        files.addAll(FileUtils.listFiles(dir, suffixes, false));
      } else {
        files.addAll(FileUtils.listFiles(dir, null, false));
      }
    }
    return files;
  }

  public void prepareRule(NewRule rule) {
    // can be overridden in derived repositories to set rule properties
  }

}
