/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.squid;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.squid.measures.Metric;
import org.sonar.squid.recognizer.CodeRecognizer;
import org.sonar.squid.text.Source;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.utils.CxxSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * {@inheritDoc}
 */
public final class CxxSquidSensor extends CxxSensor {
  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    final List<InputFile> sources = project.getFileSystem().mainFiles(CxxLanguage.KEY);
    final CodeRecognizer codeRecognizer = new CodeRecognizer(0.9, new CxxLanguageFootprint());
    
    for (InputFile inputFile : sources) {
      File file = inputFile.getFile();
      org.sonar.api.resources.File cxxFile = org.sonar.api.resources.File.fromIOFile(file, project);
      
      CxxUtils.LOG.debug("Saving size measures for file '{}'", file.getPath());
      
      Reader reader = null;
      try {
        reader = new FileReader(file);
        Source result = new Source(reader, codeRecognizer, "//");
        
        context.saveMeasure(cxxFile, CoreMetrics.LINES, (double) result.getMeasure(Metric.LINES));
        context.saveMeasure(cxxFile, CoreMetrics.COMMENT_LINES, (double) result.getMeasure(Metric.COMMENT_LINES));
        context.saveMeasure(cxxFile, CoreMetrics.COMMENT_BLANK_LINES, (double) result.getMeasure(Metric.COMMENT_BLANK_LINES));
        context.saveMeasure(cxxFile, CoreMetrics.COMMENTED_OUT_CODE_LINES, (double) result.getMeasure(Metric.COMMENTED_OUT_CODE_LINES));
        context.saveMeasure(cxxFile, CoreMetrics.NCLOC, (double) result.getMeasure(Metric.LINES_OF_CODE));
      } catch (IOException exc) {
        String msg = new StringBuilder()
          .append("Cannot analyse the file: '")
          .append(file.getPath())
          .append("'")
          .toString();
        throw new SonarException(msg, exc);
      } finally {
        IOUtils.closeQuietly(reader);
      }
    } 
  }
}
