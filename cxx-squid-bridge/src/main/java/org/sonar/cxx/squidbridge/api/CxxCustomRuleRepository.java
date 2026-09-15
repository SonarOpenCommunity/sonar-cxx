/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) SonarOpenCommunity
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
package org.sonar.cxx.squidbridge.api;

import java.util.List;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.scanner.ScannerSide;

/**
 * Interface for external plugins to register custom C++ checks, without depending on the full
 * sonar-cxx-plugin module.
 *
 * <p>Usage example in an external plugin:
 * <pre>
 * public class MyCustomRuleRepository implements CxxCustomRuleRepository {
 *     &#64;Override
 *     public String repositoryKey() {
 *         return "my-custom-rules";
 *     }
 *
 *     &#64;Override
 *     public List&lt;Class&lt;?&gt;&gt; checkClasses() {
 *         return List.of(
 *             MyFirstCheck.class,
 *             MySecondCheck.class
 *         );
 *     }
 * }
 * </pre>
 */
@ScannerSide
@ExtensionPoint
public interface CxxCustomRuleRepository {

  /**
   * @return the unique key identifying this rule repository in SonarQube
   */
  String repositoryKey();

  /**
   * @return the check classes provided by this repository, each extending
   *     {@code SquidCheck<Grammar>} and annotated with {@code @Rule}
   */
  List<Class<?>> checkClasses();
}
