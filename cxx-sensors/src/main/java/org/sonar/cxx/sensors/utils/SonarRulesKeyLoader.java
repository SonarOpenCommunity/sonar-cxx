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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class SonarRulesKeyLoader {

  private SonarRulesKeyLoader() {
  }

  static ObjectMapper objectMapper = new ObjectMapper();

  public static List<String> getRulesFor(String sonarUrl, String sonarLogin, String language, String tag)
    throws IOException, InterruptedException {

    int p = 0;
    int total = 0;
    Response response = null;
    String requestURL = getUrl(sonarUrl, language, tag);
    do {
      p++;
      Response res = objectMapper.readValue(get(requestURL + p, sonarLogin), Response.class);
      if (response == null || response.getRules() == null) {
        response = res;
      } else {
        response.getRules().addAll(res.getRules());
      }
      total = res.getTotal();
    } while (total - p * 500 > 0);

    return response.getRules().stream().map(Rule::getKey).map(k -> k.replace(tag + ":", ""))
      .collect(Collectors.toList());
  }

  private static String getUrl(String sonarUrl, String language, String tag) {
    StringBuilder builder = new StringBuilder(1024);
    builder.append(sonarUrl);
    if (!sonarUrl.endsWith("/")) {
      builder.append("/");
    }
    builder.append("api/rules/search?f=internalKey&ps=500");
    builder.append("&language=").append(language);
    builder.append("&tags=").append(tag);
    builder.append("&p=");
    return builder.toString();
  }

  public static String get(String uri, String sonarLogin) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(uri))
      .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((sonarLogin + ":").getBytes()))
      .build();

    HttpResponse<String> response
      = client.send(request, BodyHandlers.ofString());
    return response.body();
  }

}
