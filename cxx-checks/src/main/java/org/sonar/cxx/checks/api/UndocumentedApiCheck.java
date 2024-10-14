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
package org.sonar.cxx.checks.api;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.squidbridge.annotations.ActivatedByDefault;
import org.sonar.cxx.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.cxx.tag.Tag;
import org.sonar.cxx.visitors.AbstractCxxPublicApiVisitor;

/**
 * Check that generates issue for undocumented API items.<br>
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
 * Limitation: only "in front of the declaration" comments are considered.
 *
 * @see <a href="http://www.stack.nl/~dimitri/doxygen/manual/docblocks.html">
 * Doxygen Manual: Documenting the code</a>
 *
 * @author Ludovic Cintrat
 *
 * @param <GRAMMAR>
 */
@Rule(
  key = "UndocumentedApi",
  name = "Public APIs should be documented",
  priority = Priority.MINOR,
  tags = {Tag.CONVENTION})
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class UndocumentedApiCheck extends AbstractCxxPublicApiVisitor<Grammar> {

  private static final Logger LOG = LoggerFactory.getLogger(UndocumentedApiCheck.class);

  private static final String[] DEFAULT_NAME_SUFFIX = new String[]{".h", ".hh", ".hpp", ".H"};

  /**
   * {@inheritDoc}
   */
  public UndocumentedApiCheck() {
    super();
    withHeaderFileSuffixes(DEFAULT_NAME_SUFFIX);
    LOG.debug("rule 'cxx:UndocumentedApi' file suffixes: {}", Arrays.toString(DEFAULT_NAME_SUFFIX));
  }

  @Override
  protected void onPublicApi(AstNode node, String id, List<Token> comments) {
    if (comments.isEmpty()) {
      getContext().createLineViolation(this, "Undocumented API: " + id, node);
    }
  }
}
