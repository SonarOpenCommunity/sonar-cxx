/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2024 SonarOpenCommunity
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
/**
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge.indexer;

import java.util.Collection;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.squidbridge.api.SourceClass;
import org.sonar.cxx.squidbridge.api.SourceCode;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.api.SourcePackage;
import org.sonar.cxx.squidbridge.api.SourceProject;

class SquidIndexTest {

  private SquidIndex indexer;
  private SourceProject project;
  private SourcePackage packSquid;
  private SourceFile fileSquid;
  private SourceFile file2Squid;
  private SourceCode classSquid;

  @BeforeEach
  public void setup() {
    indexer = new SquidIndex();
    project = new SourceProject("Squid Project");
    indexer.index(project);
    packSquid = new SourcePackage("org.sonar.squid");
    project.addChild(packSquid);
    fileSquid = new SourceFile("org.sonar.squid.Squid.java", "Squid.java");
    packSquid.addChild(fileSquid);
    file2Squid = new SourceFile("org.sonar.squid.SquidConfiguration.java", "SquidConfiguration.java");
    packSquid.addChild(file2Squid);
    classSquid = new SourceClass("org.sonar.squid.Squid", "Squid");
    fileSquid.addChild(classSquid);
  }

  @Test
  void searchSingleResource() {
    SourceCode squidClass = indexer.search("org.sonar.squid.Squid");
    assertThat(squidClass).isEqualTo(new SourceClass("org.sonar.squid.Squid", "Squid"));
    SourceCode javaNCSSClass = indexer.search("org.sonar.squid.JavaNCSS");
    assertThat(javaNCSSClass).isNull();
  }

  @Test
  void searchByType() {
    Collection<SourceCode> resources = indexer.search(new QueryByType(SourceFile.class));
    assertThat(resources).hasSize(2);
    resources = indexer.search(new QueryByType(SourceClass.class));
    assertThat(resources)
      .hasSize(1)
      .contains(classSquid);
  }

}
