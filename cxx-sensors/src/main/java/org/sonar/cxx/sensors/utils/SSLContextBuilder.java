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


// Some parts of the SSL Code are adapted from: 
// https://github.com/SonarSource/sonarqube/blob/bd7a1254715e0df950e61d05c9a07cb1ba42552b/sonar-scanner-engine/src/main/java/org/sonar/scanner/http/ScannerWsClientProvider.java#L152-L185
// Don't remove copyright below!

/*
 * SonarQube
 * Copyright (C) 2009-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;

import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.exception.GenericKeyStoreException;

public class SSLContextBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(SSLContextBuilder.class);

  public static final String SKIP_SYSTEM_TRUST_MATERIAL = "sonar.scanner.skipSystemTruststore";

  private SSLContextBuilder() {
  }

  /**
   * This is adapted from Sonar source...
   * 
   * @TODO: check if this can be grabbed from sonar
   * @return
   */
  protected static SSLContext createSSLContext(Configuration config) {
    var sslFactoryBuilder = SSLFactory.builder().withDefaultTrustMaterial();
    var skipSystemTrustMaterial = config.getBoolean(SKIP_SYSTEM_TRUST_MATERIAL).orElse(true);
    if (Boolean.FALSE.equals(skipSystemTrustMaterial)) {
      LOG.debug("Loading OS trusted SSL certificates...");
      LOG.debug(
          "This operation might be slow or even get stuck. You can skip it by passing the scanner property '{}=true'",
          SKIP_SYSTEM_TRUST_MATERIAL);
      try {
        sslFactoryBuilder.withSystemTrustMaterial();
      } catch (GenericKeyStoreException e) {
        LOG.info("System trust store not loadable: {}", e.getLocalizedMessage());
      }
    }
    try {
      sslFactoryBuilder.withSystemPropertyDerivedIdentityMaterial();
    } catch (GenericKeyStoreException e) {
      LOG.info("trust store based on javax.net.ssl not loadable: {}", e.getLocalizedMessage());
    }
    var keyStorePathString = config.get("sonar.scanner.keystorePath")
        .orElse(findSonarHome(config).resolve("ssl/keystore.p12").toString());
    if (keyStorePathString != null && Files.exists(Path.of(keyStorePathString))) {

      config.get("sonar.scanner.keystorePassword").ifPresentOrElse(
          password -> sslFactoryBuilder.withIdentityMaterial(keyStorePathString, password.toCharArray(), "PKCS12"),
          () -> sslFactoryBuilder.withIdentityMaterial(keyStorePathString, "changeit".toCharArray(), "PKCS12"));
    }
    var trustStorePathString = config.get("sonar.scanner.truststorePath")
        .orElse(findSonarHome(config).resolve("ssl/keystore.p12").toString());
    if (trustStorePathString != null && Files.exists(Path.of(trustStorePathString))) {
      config.get("sonar.scanner.truststorePassword").ifPresentOrElse(
          password -> sslFactoryBuilder.withTrustMaterial(trustStorePathString, password.toCharArray(), "PKCS12"),
          () -> sslFactoryBuilder.withTrustMaterial(trustStorePathString, "changeit".toCharArray(), "PKCS12"));
    }
    return sslFactoryBuilder.build().getSslContext();
  }

  private static Path findSonarHome(Configuration props) {
    var home = props.get("sonar.userHome").orElse(null);
    if (home != null) {
      return Paths.get(home).toAbsolutePath();
    }

    home = System.getenv("SONAR_USER_HOME");

    if (home != null) {
      return Paths.get(home).toAbsolutePath();
    }

    var userHome = Objects.requireNonNull(System.getProperty("user.home"),
        "The system property 'user.home' is expected to be non null");
    return Paths.get(userHome, ".sonar").toAbsolutePath();
  }

}
