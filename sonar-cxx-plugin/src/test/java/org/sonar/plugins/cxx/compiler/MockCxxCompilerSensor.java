package org.sonar.plugins.cxx.compiler;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.compiler.CompilerParser.Warning;

public class MockCxxCompilerSensor extends CxxCompilerSensor {

  private List<CompilerParser.Warning> warnings;
  public List<CompilerParser.Warning> savedWarnings;

  @Override
  protected CompilerParser getCompilerParser() {

    CompilerParser compileParser = mock(CompilerParser.class);
  
    try {
      doAnswer(new Answer<List<CompilerParser.Warning>>() {

          public List<CompilerParser.Warning> answer(InvocationOnMock invocation)
                  throws Throwable {
              Object[] args = invocation.getArguments();
              if (args[5] instanceof List<?>) {
                List<CompilerParser.Warning> list = (List<CompilerParser.Warning>) args[5];
                list.addAll(warnings);
              }
              return null;
          }
        }).when(compileParser).processReport(any(Project.class), any(SensorContext.class), any(File.class), any(String.class),  any(String.class), any(List.class));
      } catch (FileNotFoundException e) {
        Assert.fail(e.getMessage());
      }
    
    return compileParser;
  }

  public MockCxxCompilerSensor(ResourcePerspectives perspectives, Settings settings, FileSystem fs, RulesProfile profile, List<CompilerParser.Warning> warningsToProcess) {
    super(perspectives, settings, fs, profile);

    warnings = warningsToProcess;
    savedWarnings = new LinkedList<CompilerParser.Warning>();
  }

  @Override
  public String getParserStringProperty(String name, String def) {
    // TODO Auto-generated method stub
    return super.getParserStringProperty(name, def);
  }

  @Override
  public void saveUniqueViolation(Project project, SensorContext context, String ruleRepoKey, String file,
      String line, String ruleId, String msg) {
    //       super.saveUniqueViolation(project, context, ruleRepoKey, file, line, ruleId, msg);
    
    CompilerParser.Warning w = new CompilerParser.Warning(file, line, ruleId, msg);
    savedWarnings.add(w);
  }
  
  
}