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
package org.sonar.plugins.cxx.checks;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.cxx.CxxSourceCode;

/**
 *
 * @author Jorge Costa
 */
public class CxxMaximumComplexityTest {
        
    @Test
    public void shouldReportViolation() throws URISyntaxException {      
        Resource resource = mock(Resource.class);
        File testFileLongName = new File(getClass().getResource("/org/sonar/plugins/cxx/SampleProject/sources/tests/SAMPLE-test.cpp").toURI());
        CxxSourceCode cxxSourceCode = new CxxSourceCode(resource, testFileLongName);
      
        CxxMaximumComplexity checkComplexity = new CxxMaximumComplexity();
        checkComplexity.setLine(8);
        checkComplexity.setFunctionComplexity(100);
        checkComplexity.setFunctionName("testCase1");       
        checkComplexity.validate(CxxMaximumComplexity.getMyself(), cxxSourceCode);
                
        List<Violation> violations = cxxSourceCode.getViolations();
        assertEquals(1, violations.size());                  
        assertEquals(Integer.valueOf(8), violations.get(0).getLineId());        
    }
          
    @Test
    public void shouldReportNoViolationOnMoreThanSuite() throws URISyntaxException {      
        Resource resource = mock(Resource.class);
        File testFileLongName = new File(getClass().getResource("/org/sonar/plugins/cxx/SampleProject/sources/tests/SAMPLE-test.cpp").toURI());
        CxxSourceCode cxxSourceCode = new CxxSourceCode(resource, testFileLongName);
      
        CxxMaximumComplexity checkComplexity = new CxxMaximumComplexity();   
        checkComplexity.validate(CxxMaximumComplexity.getMyself(), cxxSourceCode);
                
        List<Violation> violations = cxxSourceCode.getViolations();
        assertEquals(0, violations.size());
    }    
   
}
