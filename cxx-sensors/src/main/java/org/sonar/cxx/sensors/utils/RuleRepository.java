/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * {@inheritDoc}
 */
public class RuleRepository implements RulesDefinition {

  protected static final Logger LOG = Loggers.get(RuleRepository.class);

  protected final ServerFileSystem fileSystem;
  protected final RulesDefinitionXmlLoader xmlRuleLoader;
  protected final String repositoryKey;
  protected final String repositoryName;
  protected final String repositoryFile;

  /**
   * {@inheritDoc}
   */
  protected RuleRepository(
    ServerFileSystem fileSystem,
    RulesDefinitionXmlLoader xmlRuleLoader,
    String key,
    String name,
    String file) {
    this.fileSystem = fileSystem;
    this.xmlRuleLoader = xmlRuleLoader;
    this.repositoryKey = key;
    this.repositoryName = name;
    this.repositoryFile = file;
  }

  @Override
  public void define(Context context) {
    Charset charset = StandardCharsets.UTF_8;
    NewRepository repository = context.createRepository(repositoryKey, "cxx").setName(repositoryName);

    var xmlLoader = new RulesDefinitionXmlLoader();
    if (!"".equals(repositoryFile)) {
      InputStream xmlStream = getClass().getResourceAsStream(repositoryFile);
      xmlLoader.load(repository, xmlStream, charset);

      for (var userExtensionXml : getExtensions(repositoryKey, "xml")) {
        try ( InputStream input = java.nio.file.Files.newInputStream(userExtensionXml.toPath())) {
          xmlRuleLoader.load(repository, input, charset);
        } catch (IOException | IllegalStateException e) {
          LOG.info("Cannot Load XML '{}'", e);
        }
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

}
