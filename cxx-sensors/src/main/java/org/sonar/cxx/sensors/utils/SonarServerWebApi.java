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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SonarServerWebApi {

  private static final Logger LOG = LoggerFactory.getLogger(SonarServerWebApi.class);
  private final ObjectMapper objectMapper = new ObjectMapper();

  protected String serverUrl = "http://localhost:9000";
  protected String authorization = "";

  protected HttpClient client;
  
  protected SSLContext sslContext;
  
  /**
   * Get list with rule keys from server.
   *
   * @param language language filter for result
   * @param tag repository key
   * @return list of all keys of the rules matching the filter criteria
   *
   * @throws IOException if an I/O error occurs when sending or receiving
   */
  public List<Rule> getRules(String language, String tag)
    throws IOException {

    int p = 0;
    int total;
    List<Rule> rules = new ArrayList<>();
    String requestURL = createUrl(serverUrl, "api/rules/search?f=deprecatedKeys&ps=500", language, tag);
    do {
      p++;
      ApiRulesSearchResponse res = objectMapper.readValue(
        get(requestURL + p, authorization),
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
   * Only create new client if not exists or terminated
   * 
   * @return A correctly setup http client
   */
  private synchronized HttpClient getHttpClient() {
    if (client == null || client.isTerminated()) {
      if (sslContext != null) {
        client = HttpClient.newBuilder().sslContext(sslContext).build();
      } else {
        client = HttpClient.newHttpClient();
      }
    }
    return client;
  }
  
  /**
   * HTTP method GET.
   *
   * @param uri URI to use for the GET method
   * @param authorization authentication token to use for the GET method
   * @return response of the server
   *
   * @throws IOException if an I/O error occurs when sending or receiving
   */
  public String get(String uri, String authorization) throws IOException {
    HttpClient client = getHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(uri))
      .header("Authorization", authorization)
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

  /**
   * Set configuration of the SonarQube server.
   *
   * @param Configuration of the SonarQube server
   * @return return current object
   */
  public SonarServerWebApi setServerConfig(Configuration configuration) {

    var tempUrl = configuration.get("sonar.host.url").orElse("http://localhost:9000");
    if (!Objects.equals(tempUrl, this.serverUrl)) {
      LOG.info("Download url for server is '{}'", tempUrl);
      this.serverUrl = tempUrl;
      this.sslContext = SSLContextBuilder.createSSLContext(configuration);
    }
    // TODO: sonar.login is deprecated, remove in future version
    String authenticationToken = configuration.get("sonar.token").or(() -> configuration.get("sonar.login"))
        .orElse(System.getenv("SONAR_TOKEN"));
    var tempAuth = "Basic " + Base64.getEncoder().encodeToString((authenticationToken + ":").getBytes());
    if (!Objects.equals(tempAuth, this.authorization)) {
      this.authorization = tempAuth;
    }
    return this;
  }

}
