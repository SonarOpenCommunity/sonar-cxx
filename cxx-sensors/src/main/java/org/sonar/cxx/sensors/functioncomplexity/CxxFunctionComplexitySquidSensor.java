/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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
package org.sonar.cxx.sensors.functioncomplexity;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import java.util.Hashtable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputModule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.parser.CxxGrammarImpl;
import static org.sonar.cxx.sensors.clangtidy.CxxClangTidySensor.REPORT_PATH_KEY;
import org.sonar.cxx.sensors.squid.SquidSensor;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceFunction;
import org.sonar.squidbridge.checks.ChecksHelper;

public class CxxFunctionComplexitySquidSensor extends SquidAstVisitor<Grammar> implements SquidSensor {
  
  private static final Logger LOG = Loggers.get(CxxFunctionComplexitySquidSensor.class);
  
  public static final String FUNCTION_COMPLEXITY_THRESHOLD_KEY = "funccomplexity.threshold";
  
  private int cyclomaticComplexityThreshold;
  
  private int functionsBelowThreshold;
  
  public int getFunctionsBelowThreshold(){
    return this.functionsBelowThreshold;
  }
  
  private int functionsOverThreshold;
  
  public int getFunctionsOverThreshold(){
    return this.functionsOverThreshold;
  }   
  
  private Hashtable<SourceFile, FunctionCount> complexFunctionsPerFile = new Hashtable<>();      
  
  public CxxFunctionComplexitySquidSensor(CxxLanguage language){    
    this.cyclomaticComplexityThreshold = language.getIntegerOption(FUNCTION_COMPLEXITY_THRESHOLD_KEY).orElse(10);
    LOG.debug("Cyclomatic complexity threshold: " + this.cyclomaticComplexityThreshold);   
  }

  @Override
  public SquidAstVisitor<Grammar> getVisitor() {
    return this;
  }
  
  @Override
  public void init() {
        subscribeTo(CxxGrammarImpl.functionDefinition);
  }     
  
  @Override
  public void leaveNode(AstNode node) {              
      SourceFunction sourceFunction = (SourceFunction) getContext().peekSourceCode();
      SourceFile sourceFile = (SourceFile)sourceFunction.getAncestor(SourceFile.class);

      int complexity = ChecksHelper.getRecursiveMeasureInt(sourceFunction, CxxMetric.COMPLEXITY);            

      incrementFunctionByThresholdForAllFiles(complexity);           
      incrementFunctionByThresholdForFile(sourceFile, complexity);
  }      
  
  private void incrementFunctionByThresholdForAllFiles(int complexity){
      if (complexity > this.cyclomaticComplexityThreshold)
          this.functionsOverThreshold++;
      else
          this.functionsBelowThreshold++;                              
  }  
  
  private void incrementFunctionByThresholdForFile(SourceFile sourceFile, int complexity){
    if (!complexFunctionsPerFile.containsKey(sourceFile))
      complexFunctionsPerFile.put(sourceFile, new FunctionCount());
        
    FunctionCount count = complexFunctionsPerFile.get(sourceFile);
    if (complexity > this.cyclomaticComplexityThreshold)
        count.functionsOverThreshold++;
    else
        count.functionsBelowThreshold++;        
  }

  @Override
  public void publishMeasureForFile(InputFile inputFile, SourceFile squidFile, SensorContext context) {
    FunctionCount c = complexFunctionsPerFile.get(squidFile);
    if (c == null) 
      return;
    
    context.<Integer>newMeasure()
      .forMetric(FunctionComplexityMetrics.COMPLEX_FUNCTIONS)
      .on(inputFile)
      .withValue(c.functionsOverThreshold)
      .save();

    context.<Double>newMeasure()
      .forMetric(FunctionComplexityMetrics.PERC_COMPLEX_FUNCTIONS)
      .on(inputFile)
      .withValue(calculatePercComplexFunctions(c.functionsOverThreshold, c.functionsBelowThreshold))
      .save();    
  }

  @Override
  public void publishMeasureForProject(InputModule module, SensorContext context) {
    context.<Integer>newMeasure()
      .forMetric(FunctionComplexityMetrics.COMPLEX_FUNCTIONS)
      .on(module)
      .withValue(functionsOverThreshold)
      .save();
    
    context.<Double>newMeasure()
      .forMetric(FunctionComplexityMetrics.PERC_COMPLEX_FUNCTIONS)
      .on(module)
      .withValue(calculatePercComplexFunctions(functionsOverThreshold, functionsBelowThreshold))
      .save();    
  }
  
  private double calculatePercComplexFunctions(int functionsOverThreshold, int functionsBelowThreshold){
    return ((float)functionsOverThreshold * 100.0) / ((float)functionsOverThreshold + (float)functionsBelowThreshold);
  }
  
}
