/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
package org.sonar.plugins.cxx.vc;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

//import java.nio.Files; 
import java.io.*;
import java.util.regex.*;
import java.util.*;


/**
 * VC is a compiler for C++ with advanced analysis features for commericial editions (team edition or premium)
 *
 * @author Bert
 */

public class CxxVCSensor extends CxxReportSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.vc.reportPath";
  private static final String DEFAULT_REPORT_PATH = "vc-reports/BuildLog.htm";
  private RulesProfile profile;
  // look for single line with compiler warning message
  private String pattern4groups = "^.*[\\\\,/](.*)\\(([0-9]+)\\)\\x20:\\x20warning\\x20(C\\d\\d\\d\\d):(.*)$";
  //private String pattern4groups = "(C\\d\\d\\d\\d):(.*)";
  /**
   * {@inheritDoc}
   */
  public CxxVCSensor(RuleFinder ruleFinder, Settings conf, RulesProfile profile) {
    super(ruleFinder, conf);
    this.profile = profile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return super.shouldExecuteOnProject(project)
      && !profile.getActiveRulesByRepository(CxxVCRuleRepository.KEY).isEmpty();
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String defaultReportPath() {
    return DEFAULT_REPORT_PATH;
  }

  @Override
  protected void processReport(final Project project, final SensorContext context, File report)
      throws javax.xml.stream.XMLStreamException
  {
	    int countViolations = 0;
	    // Iterate through the lines of the input file:
	    try {
	    Scanner scanner = new Scanner(report, "UTF-16");
	    CxxUtils.LOG.info("Scanner initialized with report '{}'", report);
	    Pattern p = Pattern.compile(pattern4groups, Pattern.MULTILINE);
	    CxxUtils.LOG.info("Using pattern : '" + p.toString() +"'");
	    //CxxUtils.LOG.info("read first line : '" + scanner.nextLine()+"'");
	    while (scanner.findWithinHorizon(p, 0) != null)
	    {
	          String file = scanner.match().group(1);
	          String line = scanner.match().group(2);
	          String id = scanner.match().group(3);
	          String msg = scanner.match().group(4);
//	  	      CxxUtils.LOG.info("Scanner match file='" +file+"' line='"+line+"' id='"+id+"' msg="+msg);          
	          // get real name for file - VC writes case insensitive file name to html
        	  file = GetRealFileName (file, project.getFileSystem().getSourceDirs());
	          
              if (isInputValid(file, line, id, msg)) {
                saveViolation(project, context, CxxVCRuleRepository.KEY, file, Integer.parseInt(line), id, msg);
                countViolations++;
              } else {
                CxxUtils.LOG.warn("VC warning: {}", msg);
              }
	      }
	    scanner.close();
	    CxxUtils.LOG.info("VC warnings processed = " + countViolations);
	    } catch (java.io.FileNotFoundException e){
            CxxUtils.LOG.error("processReport Exception: " + "report.getName" + " - not processed '{}'", e.toString());
	    } catch (java.lang.IllegalArgumentException e1) {
            CxxUtils.LOG.error("processReport Exception: " + "report.getName" + " - not processed '{}'", e1.toString());
	    }
  }  
	  private boolean isInputValid(String file, String line, String id, String msg) {
	    return !StringUtils.isEmpty(file) && !StringUtils.isEmpty(line) 
	      && !StringUtils.isEmpty(id) && !StringUtils.isEmpty(msg);
	  }	    
	    
	  private static String GetRealFileName(String file, List<java.io.File> sourceDirs) {

		  Iterator<java.io.File> iterator = sourceDirs.iterator();
		  while(iterator.hasNext()){
			  File targetfile = new java.io.File(iterator.next().getPath()+"\\"+file);
		          if (targetfile.exists()) {
		          try {
		        		  return targetfile.getCanonicalFile().getName();
		          } catch (java.io.IOException e) {
		        		  CxxUtils.LOG.error("processReport GetRealFileName getName failed '{}'", e.toString());
		          }
		        	  
		       }
	 	   }
		  return file;
	  }
	  
  }

