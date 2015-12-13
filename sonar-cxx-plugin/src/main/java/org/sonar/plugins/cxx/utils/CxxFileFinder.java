/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * sonarqube@googlegroups.com
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
package org.sonar.plugins.cxx.utils;
 
import static java.nio.file.FileVisitResult.CONTINUE;
 
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
 
public class CxxFileFinder extends SimpleFileVisitor<Path> {
 
    private final PathMatcher matcher;
    private final boolean recursive;
    private final String baseDir;
    private List<Path> matchedPaths = new ArrayList<Path>();
    
    CxxFileFinder(String pattern, String baseDir, boolean recursive) {
        matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        this.recursive = recursive;
        this.baseDir = baseDir.toLowerCase();        
    }
 
    void match(Path file) {
        Path name = file.getFileName();
 
        if (name != null && matcher.matches(name)) {
            matchedPaths.add(file);
        }
    }
 
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (recursive) {
          match(file);
        } else {
          String parentPath = file.getParent().toString().toLowerCase();
          if (parentPath.equals(this.baseDir)) {
            match(file);  
          }
        }
        
        return CONTINUE;
    }
  
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        CxxUtils.LOG.warn("File access Failed '{}' : ", file, exc.getMessage());
        return CONTINUE;
    }

    public Collection<Path> getMatchedPaths() {
        return matchedPaths;
    }
    
    public static Collection<Path> FindFiles(String baseDir, String pattern, boolean recursive) throws IOException {
      CxxFileFinder finder = new CxxFileFinder(pattern, baseDir, recursive);
      Files.walkFileTree(Paths.get(baseDir), finder);
      return finder.getMatchedPaths();
    }
}