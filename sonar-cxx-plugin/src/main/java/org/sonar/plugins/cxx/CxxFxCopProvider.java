/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx;

import org.sonar.api.Extension;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.utils.CxxUtils;
import org.sonar.plugins.fxcop.FxCopConfiguration;
import org.sonar.plugins.fxcop.FxCopRulesDefinition;
import org.sonar.plugins.fxcop.FxCopSensor;
import org.sonar.squidbridge.rules.SqaleXmlLoader;

import com.google.common.collect.ImmutableList;

public class CxxFxCopProvider {
  private static final String FXCOP_REPORT_PATH_PROPERTY_KEY = "sonar.cxx.fxcop.reportPath";

  private static final FxCopConfiguration FXCOP_CONF = new FxCopConfiguration(CxxLanguage.KEY, "fxcop", null, null, null,
      null, null, null, FXCOP_REPORT_PATH_PROPERTY_KEY);

  private CxxFxCopProvider() {
  }

  public static ImmutableList<Class<? extends Extension>> extensions() {
    return ImmutableList.of(CxxFxCopRulesDefinition.class, CxxFxCopSensor.class);
  }

  public static class CxxFxCopRulesDefinition extends FxCopRulesDefinition {
    private static final String LANGUAGE_KEY =  "cs";

    private static final FxCopConfiguration SPECIAL_FXCOP_CONF = new FxCopConfiguration(LANGUAGE_KEY, FXCOP_CONF.repositoryKey(), null, null, null,
        null, null, null, FXCOP_CONF.reportPathPropertyKey());

    
    public CxxFxCopRulesDefinition() {
      super(SPECIAL_FXCOP_CONF, new FxCopRulesDefinitionSqaleLoader() {
        @Override
        public void loadSqale(NewRepository repository) {
          SqaleXmlLoader.load(repository, "/com/sonar/sqale/fxcop.xml");
        }
      });
    }

  }

  public static class CxxFxCopSensor extends FxCopSensor {

    private final FxCopConfiguration fxCopConf;
    private final Settings settings;

    public CxxFxCopSensor(Settings settings, RulesProfile profile, FileSystem fs, ResourcePerspectives perspectives) {
      super(FXCOP_CONF, settings, profile, fs, perspectives);
      this.fxCopConf = FXCOP_CONF;
      this.settings = settings;
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
      String reportPath = settings.getString(fxCopConf.reportPathPropertyKey());

      boolean shouldExecute = reportPath != null;
      
      CxxUtils.LOG.info("Parsing managed C++ FxCop logfile ({})", reportPath);
      return shouldExecute;
    }

  }

}
