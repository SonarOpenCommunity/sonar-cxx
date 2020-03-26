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
import java.util.List;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxSquidConfiguration;
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

  private static final Logger LOG = Loggers.get(CxxPublicApiVisitor.class);

  private int publicApiCounter;
  private int undocumentedApiCounter;

  public CxxPublicApiVisitor(CxxSquidConfiguration squidConfig) {
    super();
    withHeaderFileSuffixes(squidConfig.getPublicApiFileSuffixes());
  }

  @Override
  public void visitFile(AstNode astNode) {
    publicApiCounter = 0;
    undocumentedApiCounter = 0;
    super.visitFile(astNode);
  }

  @Override
  public void leaveFile(AstNode astNode) {
    super.leaveFile(astNode);

    SourceFile sourceFile = (SourceFile) getContext().peekSourceCode();
    sourceFile.setMeasure(CxxMetric.PUBLIC_API, publicApiCounter);
    sourceFile.setMeasure(CxxMetric.PUBLIC_UNDOCUMENTED_API, undocumentedApiCounter);
  }

  @Override
  protected void onPublicApi(AstNode node, String id, List<Token> comments) {
    final boolean commented = !comments.isEmpty();
    LOG.debug("node: {} line: {} id: '{}' documented: {}", node.getType(), node.getTokenLine(), id, commented);

    if (!commented) {
      undocumentedApiCounter++;
    }

    publicApiCounter++;
  }

}
