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
package org.sonar.plugins.cxx;

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.ExtensionProvider;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.ServerExtension;
import org.sonar.api.config.Settings;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.plugins.cxx.compiler.*;
import org.sonar.plugins.cxx.cppcheck.*;
import org.sonar.plugins.cxx.externalrules.CxxExternalRuleRepository;
import org.sonar.plugins.cxx.pclint.CxxPCLintRuleRepository;
import org.sonar.plugins.cxx.rats.CxxRatsRuleRepository;
import org.sonar.plugins.cxx.utils.CxxAbstractRuleRepository;
import org.sonar.plugins.cxx.valgrind.CxxValgrindRuleRepository;
import org.sonar.plugins.cxx.veraxx.CxxVeraxxRuleRepository;

/**
 * Creates FXCop rule repositories for every language supported by FxCop.
 */
@Properties({
  @Property(key = CxxCompilerRuleRepository.CUSTOM_RULES_KEY,
    defaultValue = "", name = "Compiler custom rules",
    description = "XML description of Compiler custom rules", type = PropertyType.TEXT,
    global = true, project = false),
  @Property(key = CxxCppCheckRuleRepository.CUSTOM_RULES_KEY,
    defaultValue = "", name = "CppCheck custom rules",
    description = "XML description of CppCheck custom rules", type = PropertyType.TEXT,
    global = true, project = false),
  @Property(key = CxxPCLintRuleRepository.CUSTOM_RULES_KEY,
    defaultValue = "", name = "PCLint custom rules",
    description = "XML description of PCLint custom rules", type = PropertyType.TEXT,
    global = true, project = false),
  @Property(key = CxxRatsRuleRepository.CUSTOM_RULES_KEY,
    defaultValue = "", name = "Rats custom rules",
    description = "XML description of Rats custom rules", type = PropertyType.TEXT,
    global = true, project = false),
  @Property(key = CxxValgrindRuleRepository.CUSTOM_RULES_KEY,
    defaultValue = "", name = "Valgrind custom rules",
    description = "XML description of Valgrind custom rules", type = PropertyType.TEXT,
    global = true, project = false),
  @Property(key = CxxVeraxxRuleRepository.CUSTOM_RULES_KEY,
    defaultValue = "", name = "Vera++ custom rules",
    description = "XML description of Vera++ custom rules", type = PropertyType.TEXT,
    global = true, project = false),
  @Property(key = CxxExternalRuleRepository.CUSTOM_RULES_KEY,
    defaultValue = "", name = "External custom rules",
    description = "XML description of External custom rules", type = PropertyType.TEXT,
    global = true, project = false)      
})
public class CxxRuleRepositoryProvider extends ExtensionProvider implements ServerExtension {
  private final ServerFileSystem fileSystem;
  private final XMLRuleParser xmlRuleParser;
  private final Settings settings;

  public CxxRuleRepositoryProvider(ServerFileSystem fileSystem, Settings settings) {
    this.fileSystem = fileSystem;
    this.xmlRuleParser = new XMLRuleParser();
    this.settings = settings;
  }

  @Override
  public Object provide() {
    List<CxxAbstractRuleRepository> extensions = new ArrayList<CxxAbstractRuleRepository>();
    
    extensions.add(new CxxCompilerRuleRepository(this.fileSystem, this.xmlRuleParser, this.settings));
    extensions.add(new CxxCppCheckRuleRepository(this.fileSystem, this.xmlRuleParser, this.settings));
    extensions.add(new CxxPCLintRuleRepository(this.fileSystem, this.xmlRuleParser, this.settings));
    extensions.add(new CxxRatsRuleRepository(this.fileSystem, this.xmlRuleParser, this.settings));
    extensions.add(new CxxValgrindRuleRepository(this.fileSystem, this.xmlRuleParser, this.settings));
    extensions.add(new CxxVeraxxRuleRepository(this.fileSystem, this.xmlRuleParser, this.settings));
    extensions.add(new CxxExternalRuleRepository(this.fileSystem, this.xmlRuleParser, this.settings));

    return extensions;
  }
}
