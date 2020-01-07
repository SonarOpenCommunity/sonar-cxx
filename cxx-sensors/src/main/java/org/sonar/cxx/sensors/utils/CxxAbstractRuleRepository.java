/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2019 SonarOpenCommunity
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
import org.sonar.cxx.CxxLanguage;

/**
 * {@inheritDoc}
 */
public abstract class CxxAbstractRuleRepository implements RulesDefinition {

  private static final Logger LOG = Loggers.get(CxxAbstractRuleRepository.class);

  private final ServerFileSystem fileSystem;
  private final RulesDefinitionXmlLoader xmlRuleLoader;
  private final CxxLanguage language;
  protected final String repositoryKey;
  protected final String repositoryName;

  /**
   * {@inheritDoc}
   */
  public CxxAbstractRuleRepository(
    ServerFileSystem fileSystem,
    RulesDefinitionXmlLoader xmlRuleLoader,
    String key,
    String name,
    CxxLanguage language) {
    this.fileSystem = fileSystem;
    this.xmlRuleLoader = xmlRuleLoader;
    this.repositoryKey = getRepositoryKey(key, language);
    this.repositoryName = name;
    this.language = language;
  }

  public static String getRepositoryKey(String key, CxxLanguage lang) {
    return key + lang.getRepositorySuffix();
  }

  @Override
  public void define(Context context) {
    Charset charset = StandardCharsets.UTF_8;
    NewRepository repository = context.createRepository(repositoryKey, this.language.getKey()).setName(repositoryName);

    RulesDefinitionXmlLoader xmlLoader = new RulesDefinitionXmlLoader();
    if (!"".equals(fileName())) {
      InputStream xmlStream = getClass().getResourceAsStream(fileName());
      xmlLoader.load(repository, xmlStream, charset);

      for (File userExtensionXml : getExtensions(repositoryKey, "xml")) {
        try (InputStream input = java.nio.file.Files.newInputStream(userExtensionXml.toPath())) {
          xmlRuleLoader.load(repository, input, charset);
        } catch (IOException | IllegalStateException ex) {
          LOG.info("Cannot Load XML '{}'", ex);
        }
      }
    }

    repository.done();
  }

  public List<File> getExtensions(String dirName, @Nullable String... suffixes) {
    File dir = new File(fileSystem.getHomeDir(), "extensions/rules/" + dirName);
    List<File> files = new ArrayList<>();
    if (dir.exists() && dir.isDirectory()) {
      if (suffixes != null && suffixes.length > 0) {
        files.addAll(FileUtils.listFiles(dir, suffixes, false));
      } else {
        files.addAll(FileUtils.listFiles(dir, null, false));
      }
    }
    return files;
  }

  protected abstract String fileName();

}
