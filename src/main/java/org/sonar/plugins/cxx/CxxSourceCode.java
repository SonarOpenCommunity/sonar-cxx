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
package org.sonar.plugins.cxx;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.cxx.utils.CxxUtils;


/**
 * Checks and analyzes report measurements, violations and other findings in WebSourceCode.
 * 
 * @author Matthijs Galesloot
 */
public class CxxSourceCode {

  private final List<String> codelines;
  private final File file;

  private final Resource<?> resource;
  private final List<Violation> violations = new ArrayList<Violation>();
  private final String code;
 
  public CxxSourceCode(Resource<?> resource, File file) {
    this.resource = resource;
    this.file = file;
    this.codelines = initSourceCode(file);
    this.code = initCode(file);
  }  

  @Override
  public String toString() {
    return resource.getLongName();
  }
  
  public void addViolation(Violation violation) {
    this.violations.add(violation);
  }

  InputStream createInputStream() {
    return new ByteArrayInputStream(code.getBytes());
  }

  public Resource<?> getResource() {
    return resource;
  }

  public List<Violation> getViolations() {
    return violations;
  }

  public List<String> getCode() {
    return codelines;    
  }
  
  public String getCodeAsString() {
    return code;    
  }  

  public File getFile() {
    return file;
  } 
  
  private List<String> initSourceCode(File file) {
    List<String> codeinit = new ArrayList<String>();
    try {
      codeinit = FileUtils.readLines(file);      
    } catch (IOException ex) {
      CxxUtils.LOG.debug("Cannot Access File: '{}'  : ", file.getName(), ex.getMessage());      
    }
    return codeinit;
  }
  
  private String initCode(File file) {
    String codeinit = "";
    try {
      codeinit = FileUtils.readFileToString(file);      
    } catch (IOException ex) {
      CxxUtils.LOG.debug("Cannot Access File: '{}'  : ", file.getName(), ex.getMessage());      
    }
    return codeinit;
  }

  public void saveViolations(SensorContext sensorContext) {
    for (Violation violation : getViolations()) {
      sensorContext.saveViolation(violation);
    }
  }
}
