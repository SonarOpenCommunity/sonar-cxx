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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SonarServerWebApi {

  private static final Logger LOG = LoggerFactory.getLogger(SonarServerWebApi.class);
  static ObjectMapper objectMapper = new ObjectMapper();

  private SonarServerWebApi() {
  }

  /**
   * Get list with rule keys from server.
   *
   * @param serverUrl URL of the SonarQube server
   * @param authenticationToken authentication token to use for API access
   * @param language language filter for result
   * @param tag repository key
   * @return list of all keys of the rules matching the filter criteria
   *
   * @throws IOException if an I/O error occurs when sending or receiving
   */
  public static List<Rule> getRules(String serverUrl, String authenticationToken, String language, String tag)
    throws IOException {

    int p = 0;
    int total;
    List<Rule> rules = new ArrayList<>();
    String requestURL = createUrl(serverUrl, "api/rules/search?f=deprecatedKeys&ps=500", language, tag);
    do {
      p++;
      ApiRulesSearchResponse res = objectMapper.readValue(
        get(requestURL + p, authenticationToken),
        ApiRulesSearchResponse.class
      );
      rules.addAll(res.rules());
      total = res.total();
    } while (total - p * 500 > 0);

    return rules;
  }

  private static String createUrl(String sonarUrl, String api, String language, String tag) {
    StringBuilder builder = new StringBuilder(1024);
    builder.append(sonarUrl);
    if (!sonarUrl.endsWith("/")) {
      builder.append("/");
    }
    builder.append(api);
    builder.append("&language=").append(language);
    builder.append("&tags=").append(tag);
    builder.append("&p=");

    return builder.toString();
  }

  /**
   * HTTP method GET.
   *
   * @param uri URI to use for the GET method
   * @param authenticationToken authentication token to use for the GET method
   * @return response of the server
   *
   * @throws IOException if an I/O error occurs when sending or receiving
   */
  public static String get(String uri, String authenticationToken) throws IOException {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(uri))
      .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((authenticationToken + ":").getBytes()))
      .build();

    try {
      long start = System.currentTimeMillis();
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      long finish = System.currentTimeMillis();
      LOG.debug("{} {} {} | time={}ms", response.request().method(), response.statusCode(), response.request().uri(),
        finish - start);
      return response.body();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException(e);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static record ApiRulesSearchResponse(int total, int p, int ps, List<Rule> rules) {

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static record Rule(String key, DeprecatedKeys deprecatedKeys) {

    public Rule  {
      if (deprecatedKeys == null) {
        deprecatedKeys = new DeprecatedKeys(new ArrayList<>());
      }
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static record DeprecatedKeys(List<String> deprecatedKey) {

  }

}
