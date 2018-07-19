/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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

import java.util.Arrays;
import java.util.List;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.squidbridge.api.SourceFile;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;

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

  private static final Logger LOG = Loggers.get(CxxPublicApiVisitor.class);

  private int totalAPINr;
  private int undocumentedAPINr;

  public CxxPublicApiVisitor(CxxLanguage language) {
    super();
    withHeaderFileSuffixes(Arrays.asList(language.getHeaderFileSuffixes()));
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
