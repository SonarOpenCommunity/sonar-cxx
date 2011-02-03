/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits réservés.
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
package org.sonar.plugins.cxx.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.FileSet;
import org.slf4j.Logger;
import org.sonar.api.batch.maven.MavenPlugin;
import org.sonar.api.resources.Project;

/**
 * @todo VH : prefer CXXSENSOR because it's the common code between all CCX sensor
 * @author fbonin
 */
public abstract class ReportsHelper {

	protected abstract Logger getLogger();

	protected abstract String getGroupId();
	
	protected abstract String getSensorId();

	protected abstract String getArtifactId();

	protected abstract String getDefaultReportsDir();

	protected abstract String getDefaultReportsFilePattern();

	public File getReportsDirectory(Project project) {

		File report = getReportDirectoryFromPluginConfiguration(project);
		if (report == null) {
			report = getReportDirectoryFromDefaultPath(project);
		}

		if (report == null || !report.exists()) {
			getLogger().warn("Reports directory not found at {}", report);
			report = null;
		}
		return report;
	}

	private File getReportDirectoryFromPluginConfiguration(Project project) {
		MavenPlugin mavenPlugin = MavenPlugin.getPlugin(project.getPom(),
				getGroupId(), getArtifactId());
		if (mavenPlugin != null) {
			String path = mavenPlugin.getParameter(getSensorId() + "/directory");
			if (path != null) {
				return project.getFileSystem().resolvePath(path);
			}
		}
		return null;
	}

	private File getReportDirectoryFromDefaultPath(Project project) {
		return new File(project.getFileSystem().getBasedir(),//$FB For Cpp project getReportOutputDir(), make no sense
				getDefaultReportsDir());
	}

	public File[] getReports(Project project, File dir) {

		FileSet afileSet = new FileSet();
		afileSet.setDirectory(dir.getAbsolutePath());
		MavenPlugin plugin = MavenPlugin.getPlugin(project.getPom(),
				getGroupId(), getArtifactId());
		String includes[] = null;
		String excludes[] = null;
		if (plugin != null) {
			includes = plugin.getParameters(getSensorId() + "/includes/include");
			excludes = plugin.getParameters(getSensorId() + "/excludes/exclude");
		}
		if (null == includes || includes.length == 0) {
			includes = new String[1];
			includes[0] = getDefaultReportsFilePattern();
		}
		if (null == excludes) {
			excludes = new String[0];
		}
		getLogger()
				.info(
						getGroupId() + " " + getArtifactId()
								+ " includes value = {}", includes);
		getLogger()
				.info(
						getGroupId() + " " + getArtifactId()
								+ " excludes value = {}", excludes);
		afileSet.setIncludes(Arrays.asList(includes));
		afileSet.setExcludes(Arrays.asList(excludes));

		List<File> aListFile = new ArrayList<File>();
		FileSetManager aFileSetManager = new FileSetManager();
		String[] found = aFileSetManager.getIncludedFiles(afileSet);
		for (String aTmp : found) {
			getLogger().info("reportsfile found  = {}", aTmp);
			aListFile.add(new File(afileSet.getDirectory() + "/" + aTmp));
		}
		return aListFile.toArray(new File[0]);
	}
	
	public String[] getReportsIncludeSourcePath(Project project)
	{
		MavenPlugin plugin = MavenPlugin.getPlugin(project.getPom(),
				getGroupId(), getArtifactId());
		String includes[] = null;
		if (plugin != null) {
			includes = plugin.getParameters(getSensorId() + "/reportsIncludeSourcePath/include");
		} else {
	        // VH Workaround to run without include source path
	        // This exists because I don't understand IncludeSourcePath
	        includes = new String[]{"."};
			// FB : IncludeSourcePath is use when there is a mismatch
			// between report relative resource path (see gcovr reports for example)
			// end pom.xml relative source path (used by sensor to locate resources)
			// anyway returning "." may not be a bad things
		}
		return includes;
	}
}
