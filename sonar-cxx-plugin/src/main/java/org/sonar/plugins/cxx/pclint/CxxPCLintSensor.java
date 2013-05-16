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
package org.sonar.plugins.cxx.pclint;

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

import javax.xml.stream.XMLStreamException;

import java.io.File;

/**
 * PCLint is an equivalent to pmd but for C++
 *
 * @author Bert
 */
public class CxxPCLintSensor extends CxxReportSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.pclint.reportPath";
  public static final String MISRA_UNIQUE_ID_KEY = "false";
  private static final String DEFAULT_REPORT_PATH = "pclint-reports/pclint-result-*.xml";
  
  private RulesProfile profile;
  private Settings conf = null;

  /**
   * {@inheritDoc}
   */
  public CxxPCLintSensor(RuleFinder ruleFinder, Settings conf, RulesProfile profile) {
    super(ruleFinder, conf);
    this.profile = profile;    
    this.conf = conf;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return super.shouldExecuteOnProject(project)
      && !profile.getActiveRulesByRepository(CxxPCLintRuleRepository.KEY).isEmpty();
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
        StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
          /**
          * {@inheritDoc}
          */

            public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
            rootCursor.advance(); // results

                SMInputCursor errorCursor = rootCursor.childElementCursor("issue"); // error

                String remapMisra = conf.getString(MISRA_UNIQUE_ID_KEY); 
                while (errorCursor.getNext() != null) {

                    String file = errorCursor.getAttrValue("file");
                    String line = errorCursor.getAttrValue("line");
                    String id = errorCursor.getAttrValue("number");
                    String msg = errorCursor.getAttrValue("desc");

                    if (isInputValid(file, line, id, msg)) {
                        if(remapMisra.equals("true")) {
                            if(id.equals("960") || id.equals("961")) { //remap only MISRA 2004 IDs
                                String newId = MapMisraRulesToUniqueSonarRules(msg);

                                String debugText="File: "+file+", Line: "+line+", ID: "+newId+", msg: " +msg;
                                CxxUtils.LOG.debug(debugText);

                                saveViolation(project, context, CxxPCLintRuleRepository.KEY,
                                file, Integer.parseInt(line), newId, msg);
                            } else {
                                saveViolation(project, context, CxxPCLintRuleRepository.KEY,
                                file, Integer.parseInt(line), id, msg);
                            }
                        } else {
                            saveViolation(project, context, CxxPCLintRuleRepository.KEY,
                            file, Integer.parseInt(line), id, msg);
                        }
                    } else {
                        CxxUtils.LOG.warn("PC-Lint warning ignored: {}", msg);
                        
                        String debugText="File: "+file+", Line: "+line+", ID: "+id+", msg: " +msg;
                        CxxUtils.LOG.debug(debugText);
                    }
                }
            }

            private boolean isInputValid(String file, String line, String id, String msg) {
            return !StringUtils.isEmpty(file) && !StringUtils.isEmpty(line) 
             && !StringUtils.isEmpty(id) && !StringUtils.isEmpty(msg);
            }

            private String MapMisraRulesToUniqueSonarRules(String msg)
            {    
                return CalculateNewRuleId(msg); 
            }      

            /**
            Removes the dot in the rule number and adds a big offset
            to make sure the key ID is not already used by other Pc-lint rules.
            **/
            private String CalculateNewRuleId(String msg) {      
                String rule = ExtractMisraRuleNumberFromDescription(msg);

                String ruleWithOutDot = rule.replace(".", "");   
                int key = Integer.parseInt(ruleWithOutDot) + 10000;
                String newKey = String.valueOf(key);

                String infoText="Remap MISRA rule "+rule+" to key " +newKey;
                CxxUtils.LOG.info(infoText);

                return newKey;
            }

            /**
            Get the MISRA rule number from the PC-lint message
            **/
            private String ExtractMisraRuleNumberFromDescription(String msg) {
                String[] splitDescription = msg.split(",", 2)[0].split(" ");
                String rule = splitDescription[splitDescription.length-1];
                return rule;
            }        

        });

        parser.parse(report);
    }
}
