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
package org.sonar.cxx.visitors;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.squidbridge.api.SourceFile;

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
  public static final String API_FILE_SUFFIXES_KEY = "sonar.cxx.metric.api.file.suffixes";

  /**
   * Default API files knows suffixes
   */
  public static final String API_DEFAULT_FILE_SUFFIXES = ".hxx,.hpp,.hh,.h";

  private static final Logger LOG = LoggerFactory.getLogger(CxxPublicApiVisitor.class);

  private int publicApiCounter;
  private int undocumentedApiCounter;

  public CxxPublicApiVisitor(CxxSquidConfiguration squidConfig) {
    super();
    String[] suffixes = squidConfig.getValues(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES,
      CxxSquidConfiguration.API_FILE_SUFFIXES)
      .stream()
      .filter(s -> s != null && !s.trim().isEmpty()).toArray(String[]::new);
    if (suffixes.length == 0) {
      suffixes = Iterables.toArray(Splitter.on(',').split(API_DEFAULT_FILE_SUFFIXES), String.class);
    }
    withHeaderFileSuffixes(suffixes);
    LOG.debug(API_FILE_SUFFIXES_KEY + ": {}", Arrays.toString(suffixes));
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    publicApiCounter = 0;
    undocumentedApiCounter = 0;
    super.visitFile(astNode);
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    super.leaveFile(astNode);

    if (!skipFile) {
      var sourceFile = (SourceFile) getContext().peekSourceCode();
      sourceFile.setMeasure(CxxMetric.PUBLIC_API, publicApiCounter);
      sourceFile.setMeasure(CxxMetric.PUBLIC_UNDOCUMENTED_API, undocumentedApiCounter);
      LOG.debug("'Public API' metric for '{}': total={}, undocumented={}",
        sourceFile.getName(), publicApiCounter, undocumentedApiCounter);
    }
  }

  @Override
  protected void onPublicApi(AstNode node, String id, List<Token> comments) {
    if (comments.isEmpty()) {
      undocumentedApiCounter++;
    }

    publicApiCounter++;
  }

}
