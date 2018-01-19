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

public class FunctionScore {
  private int score;

  public int getScore(){
      return this.score;
  }

  public void setScore(int value){
      this.score = value;
  }    

  private String componentName;

  public String getComponentName(){
      return this.componentName;
  }

  public void setComponentName(String value){
      this.componentName = value;
  }

  private String functionId;

  public String getFunctionId(){
      return this.functionId;
  }

  public void setFunctionId(String value){
      this.functionId = value;
  }

  public FunctionScore(){

  }   

  public FunctionScore(int score, String componentName, String functionId){
      this.score = score;
      this.componentName = componentName;
      this.functionId = functionId;
  }
}
