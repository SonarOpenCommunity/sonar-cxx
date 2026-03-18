/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedConstruction;
import org.sonar.api.config.Configuration;

class SonarServerWebApiTest {

    private static String jsonString = """
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
    """;

    @Test
    void ruleTest() {
	var rule = new SonarServerWebApi.Rule("key", null);

	assertThat(rule.key()).isEqualTo("key");
	assertThat(rule.deprecatedKeys()).isNotNull();
    }

    @Test
    void getRulesTest() throws IOException {

	SonarServerWebApi realApi = new SonarServerWebApi();

	var config = new Configuration() {

	    @Override
	    public boolean hasKey(String key) {
		return switch (key) {
		case "sonar.host.url" -> true;
		case "sonar.token" -> true;
		default -> false;
		};
	    }

	    @Override
	    public String[] getStringArray(String key) {
		return null;
	    }

	    @Override
	    public Optional<String> get(String key) {
		return switch (key) {
		case "sonar.host.url" -> Optional.of("http://localhost:9000");
		case "sonar.token" -> Optional.of("token");
		default -> Optional.empty();
		};
	    }
	};
	realApi.setServerConfig(config);
	var spyApi = spy(realApi);
	doReturn(jsonString).when(spyApi).get(anyString(), anyString());
	List<SonarServerWebApi.Rule> rules = spyApi.getRules("cxx", "clangtidy");
	var rule1 = new SonarServerWebApi.Rule("clangtidy:clang-diagnostic-c++11-narrowing-const-reference",
		new SonarServerWebApi.DeprecatedKeys(
			List.of("ClangTidy:clang-diagnostic-c++11-narrowing-const-reference")));
	var rule2 = new SonarServerWebApi.Rule("clangtidy:clang-diagnostic-c++20-compat",
		new SonarServerWebApi.DeprecatedKeys(
			List.of("ClangTidy:clang-diagnostic-c++20-compat", "clangtidy:clang-diagnostic-c++2a-compat")));

	assertThat(rules).hasSize(2).contains(rule1).contains(rule2);

    }

}
