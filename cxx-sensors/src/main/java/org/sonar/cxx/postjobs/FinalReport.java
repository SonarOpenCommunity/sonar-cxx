/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.postjobs;

import org.sonar.api.batch.postjob.PostJob;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.visitors.CxxParseErrorLoggerVisitor;

public class FinalReport implements PostJob {

  private static final String DEBUG_INFO_MSG = "Turn debug info on to get more details (sonar-scanner -X -Dsonar.verbose=true ...).";
  private static final Logger LOG = Loggers.get(FinalReport.class);

  @Override
  public void describe(PostJobDescriptor descriptor) {
    descriptor.name("Final report");
  }

  @Override
  public void execute(PostJobContext context) {
    CxxParseErrorLoggerVisitor.finalReport();

    if (!LOG.isDebugEnabled()) {
      LOG.info(DEBUG_INFO_MSG);
    }
  }

}
