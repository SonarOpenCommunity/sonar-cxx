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
package org.sonar.cxx.visitors;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.internal.google.common.base.Splitter;
import org.sonar.api.internal.google.common.collect.Iterables;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.squidbridge.api.SourceFile;

/**
 * Visitor that counts documented and undocumented API items.<br>
 * Following items are counted as public API:
 * <ul>
 * <li>classes/structures</li>
 * <li>class members (public and protected)</li>
 * <li>structure members</li>
 * <li>enumerations</li>
 * <li>enumeration values</li>
 * <li>typedefs</li>
 * <li>functions</li>
 * <li>variables</li>
 * </ul>
 * <p>
 * Public API items are considered documented if they have Doxygen comments.<br>
 * Function arguments are not counted since they can be documented in function documentation and this visitor does not
 * parse Doxygen comments.<br>
 * This visitor should be applied only on header files.<br>
 * Currently, no filtering is applied using preprocessing directive.<br>
 * <p>
 * Limitation: only "in front of the declaration" comments and inline comments (for members) are considered. Documenting
 * public API by name (\struct Foo for instance) in other files is not supported.
 *
 * @see <a href="http://www.stack.nl/~dimitri/doxygen/manual/docblocks.html">
 * Doxygen Manual: Documenting the code</a>
 *
 * @author Ludovic Cintrat
 *
 * @param <G>
 */
public class CxxPublicApiVisitor<G extends Grammar> extends AbstractCxxPublicApiVisitor<G> {

  /**
   * Key of the file suffix parameter
   */
  public static final String FILE_SUFFIXES_KEY = "sonar.cxx.api.file.suffixes";

  /**
   * Default API files knows suffixes
   */
  public static final String DEFAULT_FILE_SUFFIXES = ".hxx,.hpp,.hh,.h";

  private static final Logger LOG = Loggers.get(CxxPublicApiVisitor.class);

  private int totalAPINr;
  private int undocumentedAPINr;

  public CxxPublicApiVisitor(Configuration settings) {
    super();
    String[] suffixes = Arrays.stream(settings.getStringArray(FILE_SUFFIXES_KEY))
      .filter(s -> s != null && !s.trim().isEmpty()).toArray(String[]::new);
    if (suffixes.length == 0) {
      suffixes = Iterables.toArray(Splitter.on(',').split(DEFAULT_FILE_SUFFIXES), String.class);
    }
    withHeaderFileSuffixes(Arrays.asList(suffixes));
  }

  public static List<PropertyDefinition> properties() {
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(FILE_SUFFIXES_KEY)
        .defaultValue(DEFAULT_FILE_SUFFIXES)
        .name("Header file suffixes")
        .multiValues(true)
        .description(
          "Comma-separated list of suffixes for files to analyze API. To not filter, leave the list empty.")
        .subCategory("Public API")
        .onQualifiers(Qualifiers.PROJECT)
        .build()
    ));
  }

  @Override
  public void visitFile(AstNode astNode) {
    totalAPINr = 0;
    undocumentedAPINr = 0;
    super.visitFile(astNode);
  }

  @Override
  public void leaveFile(AstNode astNode) {
    super.leaveFile(astNode);

    SourceFile sourceFile = (SourceFile) getContext().peekSourceCode();
    sourceFile.setMeasure(CxxMetric.PUBLIC_API, totalAPINr);
    sourceFile.setMeasure(CxxMetric.PUBLIC_UNDOCUMENTED_API, undocumentedAPINr);
  }

  @Override
  protected void onPublicApi(AstNode node, String id, List<Token> comments) {
    final boolean commented = !comments.isEmpty();
    LOG.debug("node: {} line: {} id: '{}' documented: {}", node.getType(), node.getTokenLine(), id, commented);

    if (!commented) {
      undocumentedAPINr++;
    }

    totalAPINr++;
  }

}
