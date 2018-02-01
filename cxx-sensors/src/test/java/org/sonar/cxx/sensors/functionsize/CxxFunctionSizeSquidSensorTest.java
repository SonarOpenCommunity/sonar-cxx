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
package org.sonar.cxx.sensors.functionsize;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;
import org.sonar.squidbridge.api.SourceFile;

public class CxxFunctionSizeSquidSensorTest {
    private FileLinesContextFactory fileLinesContextFactory;
    private FileLinesContext fileLinesContext;
    private CxxLanguage language;
    private SensorContextTester sensorContext;
    private CxxFunctionSizeSquidSensor sensor;
    private Configuration configuration;
    
    @Before
    public void setUp(){
        fileLinesContextFactory = mock(FileLinesContextFactory.class);
        fileLinesContext = mock(FileLinesContext.class);        
        
        language = TestUtils.mockCxxLanguage();        
        when(language.getIntegerOption(CxxFunctionSizeSquidSensor.FUNCTION_SIZE_THRESHOLD_KEY)).thenReturn(Optional.of(10));
        
        sensor = new CxxFunctionSizeSquidSensor(this.language);                        
    }    
    
    private DefaultInputFile getInputFile() throws IOException{
      File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors");
      File target = new File(baseDir, "FunctionComplexity.cc");

      String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
      DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target).setContents(content)
                                    .setCharset(Charset.forName("UTF-8")).setLanguage(language.getKey())
                                    .setType(InputFile.Type.MAIN).build();

      sensorContext = SensorContextTester.create(baseDir);
      sensorContext.fileSystem().add(inputFile);

      when(fileLinesContextFactory.createFor(inputFile)).thenReturn(fileLinesContext);        
      
      return inputFile;
    }       
    
    private DefaultInputFile getEmptyInputFile() throws IOException{
      File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors");
      File target = new File(baseDir, "EmptyFile.cc");

      String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
      DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target).setContents(content)
                                    .setCharset(Charset.forName("UTF-8")).setLanguage(language.getKey())
                                    .setType(InputFile.Type.MAIN).build();

      sensorContext = SensorContextTester.create(baseDir);
      sensorContext.fileSystem().add(inputFile);

      when(fileLinesContextFactory.createFor(inputFile)).thenReturn(fileLinesContext);        
      
      return inputFile;
    }        
    
    private <T extends Serializable> T getMeasureValue(SensorContextTester sensorContext, String componentKey, Metric<T> metric){
      Collection<Measure> measures = sensorContext.measures(componentKey);
      T value = null;
      for(Measure m : measures){
        if (m.metric() == metric)
          value = (T) m.value();
      }
      return value;
    }    
    
    @Test
    public void testPublishMeasuresForProject() throws IOException {            
        DefaultInputFile inputFile = getInputFile();              
                
        CxxAstScanner.scanSingleFile(inputFile, sensorContext, TestUtils.mockCxxLanguage(), sensor.getVisitor());
        sensor.publishMeasureForProject(sensorContext.module(), sensorContext);
                      
        assertThat(getMeasureValue(sensorContext, sensorContext.module().key(), FunctionSizeMetrics.BIG_FUNCTIONS)).isEqualTo(4);        
        assertThat(getMeasureValue(sensorContext, sensorContext.module().key(), FunctionSizeMetrics.LOC_IN_FUNCTIONS)).isEqualTo(55);      
        assertThat(getMeasureValue(sensorContext, sensorContext.module().key(), FunctionSizeMetrics.LOC_IN_BIG_FUNCTIONS)).isEqualTo(44);        
        assertThat(getMeasureValue(sensorContext, sensorContext.module().key(), FunctionSizeMetrics.PERC_BIG_FUNCTIONS)).isEqualTo(40.0);        
        assertThat(getMeasureValue(sensorContext, sensorContext.module().key(), FunctionSizeMetrics.PERC_LOC_IN_BIG_FUNCTIONS)).isEqualTo(80);        
    }        
    
    @Test
    public void testPublishMeasuresForEmptyProject() throws IOException {            
        DefaultInputFile inputFile = getEmptyInputFile();              
                
        CxxAstScanner.scanSingleFile(inputFile, sensorContext, TestUtils.mockCxxLanguage(), sensor.getVisitor());
        sensor.publishMeasureForProject(sensorContext.module(), sensorContext);
                      
        assertThat(getMeasureValue(sensorContext, sensorContext.module().key(), FunctionSizeMetrics.BIG_FUNCTIONS)).isEqualTo(0);        
        assertThat(getMeasureValue(sensorContext, sensorContext.module().key(), FunctionSizeMetrics.LOC_IN_FUNCTIONS)).isEqualTo(0);      
        assertThat(getMeasureValue(sensorContext, sensorContext.module().key(), FunctionSizeMetrics.LOC_IN_BIG_FUNCTIONS)).isEqualTo(0);        
        assertThat(getMeasureValue(sensorContext, sensorContext.module().key(), FunctionSizeMetrics.PERC_BIG_FUNCTIONS)).isEqualTo(0);        
        assertThat(getMeasureValue(sensorContext, sensorContext.module().key(), FunctionSizeMetrics.PERC_LOC_IN_BIG_FUNCTIONS)).isEqualTo(0);        
    }            
    
    @Test
    public void testPublishMeasuresForFile() throws IOException {            
        DefaultInputFile inputFile = getInputFile();                                    
                
        SourceFile squidFile = CxxAstScanner.scanSingleFile(inputFile, sensorContext, TestUtils.mockCxxLanguage(), sensor.getVisitor());
        sensor.publishMeasureForFile(inputFile, squidFile, sensorContext);
                      
        assertThat(getMeasureValue(sensorContext, inputFile.key(), FunctionSizeMetrics.BIG_FUNCTIONS)).isEqualTo(4);        
        assertThat(getMeasureValue(sensorContext, inputFile.key(), FunctionSizeMetrics.LOC_IN_FUNCTIONS)).isEqualTo(55);        
        assertThat(getMeasureValue(sensorContext, inputFile.key(), FunctionSizeMetrics.LOC_IN_BIG_FUNCTIONS)).isEqualTo(44);        
        assertThat(getMeasureValue(sensorContext, inputFile.key(), FunctionSizeMetrics.PERC_BIG_FUNCTIONS)).isEqualTo(40.0);        
        assertThat(getMeasureValue(sensorContext, inputFile.key(), FunctionSizeMetrics.PERC_LOC_IN_BIG_FUNCTIONS)).isEqualTo(80);        
    }  
    
    @Test
    public void testPublishMeasuresForEmptyFile() throws IOException {            
        DefaultInputFile inputFile = getEmptyInputFile();              
                
        SourceFile squidFile = CxxAstScanner.scanSingleFile(inputFile, sensorContext, TestUtils.mockCxxLanguage(), sensor.getVisitor());
        sensor.publishMeasureForFile(inputFile, squidFile, sensorContext);
                      
        assertThat(getMeasureValue(sensorContext, inputFile.key(), FunctionSizeMetrics.BIG_FUNCTIONS)).isEqualTo(0);        
        assertThat(getMeasureValue(sensorContext, inputFile.key(), FunctionSizeMetrics.LOC_IN_FUNCTIONS)).isEqualTo(0);        
        assertThat(getMeasureValue(sensorContext, inputFile.key(), FunctionSizeMetrics.LOC_IN_BIG_FUNCTIONS)).isEqualTo(0);        
        assertThat(getMeasureValue(sensorContext, inputFile.key(), FunctionSizeMetrics.PERC_BIG_FUNCTIONS)).isEqualTo(0);        
        assertThat(getMeasureValue(sensorContext, inputFile.key(), FunctionSizeMetrics.PERC_LOC_IN_BIG_FUNCTIONS)).isEqualTo(0);        
    }                
}
