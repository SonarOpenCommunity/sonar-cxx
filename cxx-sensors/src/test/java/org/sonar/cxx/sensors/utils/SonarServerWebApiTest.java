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
package org.sonar.cxx.sensors.utils;

import java.io.IOException;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import org.mockito.invocation.InvocationOnMock;

public class SonarServerWebApiTest {

  @Test
  void getRulesTest() throws IOException {

    try (MockedStatic<SonarServerWebApi> sonarServerWebApi = mockStatic(
      SonarServerWebApi.class,
      InvocationOnMock::callRealMethod)) {

      sonarServerWebApi.when(() -> SonarServerWebApi.get(any(String.class), any(String.class)))
        .thenReturn("""
        {
          "total":1,
          "p":1,
          "ps":500,
          "rules":[
            {
              "key":"clangtidy:clang-diagnostic-c++11-narrowing-const-reference",
              "type":"CODE_SMELL",
              "deprecatedKeys":{
                "deprecatedKey":[
                  "ClangTidy:clang-diagnostic-c++11-narrowing-const-reference"
                ]
              },
              "impacts":[
                {
                  "softwareQuality":"MAINTAINABILITY",
                  "severity":"HIGH"
                }
              ]
            },
            {
              "key":"clangtidy:clang-diagnostic-c++20-compat",
              "type":"CODE_SMELL",
              "deprecatedKeys":{
                "deprecatedKey":[
                  "ClangTidy:clang-diagnostic-c++20-compat",
                  "clangtidy:clang-diagnostic-c++2a-compat"
                ]
              },
              "impacts":[
                {
                  "softwareQuality":"MAINTAINABILITY",
                  "severity":"LOW"
                }
              ]
            }
          ],
          "paging":{
            "pageIndex":1,
            "pageSize":500,
            "total":1
          }
        }
        """
        );

      List<SonarServerWebApi.Rule> rules = SonarServerWebApi.getRules(
        "http://localhost:9000", "token", "cxx", "clangtidy"
      );
      var rule1 = new SonarServerWebApi.Rule(
        "clangtidy:clang-diagnostic-c++11-narrowing-const-reference",
        new SonarServerWebApi.DeprecatedKeys(
          List.of(
            "ClangTidy:clang-diagnostic-c++11-narrowing-const-reference"
          )
        )
      );
      var rule2 = new SonarServerWebApi.Rule(
        "clangtidy:clang-diagnostic-c++20-compat",
        new SonarServerWebApi.DeprecatedKeys(
          List.of(
            "ClangTidy:clang-diagnostic-c++20-compat",
            "clangtidy:clang-diagnostic-c++2a-compat"
          )
        )
      );

      assertThat(rules)
        .hasSize(2)
        .contains(rule1)
        .contains(rule2);
    }
  }

}
