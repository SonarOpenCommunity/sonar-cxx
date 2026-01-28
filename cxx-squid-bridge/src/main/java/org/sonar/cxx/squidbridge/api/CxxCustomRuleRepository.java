/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2025 SonarOpenCommunity
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

/**
 * Interface for external plugins to register custom C++ checks.
 *
 * <p>This interface allows external plugins (such as sonar-cryptography) to provide
 * custom rule implementations that integrate with the sonar-cxx analysis framework.
 *
 * <p>This interface provides a lightweight contract that external plugins can implement
 * without depending on the full sonar-cxx-plugin module.
 *
 * <p>Usage example in an external plugin:
 * <pre>
 * public class MyCryptoRuleRepository implements CxxCustomRuleRepository {
 *     &#64;Override
 *     public String repositoryKey() {
 *         return "my-crypto-rules";
 *     }
 *
 *     &#64;Override
 *     public List&lt;Class&lt;?&gt;&gt; checkClasses() {
 *         return List.of(
 *             WeakCipherCheck.class,
 *             InsecureHashCheck.class
 *         );
 *     }
 * }
 * </pre>
 */
public interface CxxCustomRuleRepository {

  /**
   * The unique key of the custom rule repository.
   *
   * <p>This key is used to identify the rule repository in SonarQube.
   *
   * @return the repository key
   */
  String repositoryKey();

  /**
   * The check classes provided by this custom rule repository.
   *
   * <p>Each class should extend {@code SquidCheck<Grammar>} and be annotated
   * with {@code @Rule} to define its metadata.
   *
   * @return list of check classes
   */
  List<Class<?>> checkClasses();
}
