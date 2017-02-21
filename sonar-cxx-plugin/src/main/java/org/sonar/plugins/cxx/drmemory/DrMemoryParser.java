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
package org.sonar.plugins.cxx.drmemory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.plugins.cxx.drmemory.DrMemoryParser.DrMemoryError.Location;

public class DrMemoryParser {	
	
	public static enum DrMemoryErrorType {
		UNADRESSABLE_ACCESS("UnadressableAccess", "UNADDRESSABLE ACCESS"),
		UNINITIALIZE_READ("UninitializedRead", "UNINITIALIZED READ"),
		INVALID_HEAP_ARGUMENT("InvalidHeapArgument", "INVALID HEAP ARGUMENT"),
		GDI_USAGE_ERROR("GdiUsageError", "GDI Usage Error"),
		HANDLE_LEAK("HandleLeak", "HANDLE LEAK"),
		WARNING("DrMemoryWarning", "WARNING"),
		POSSIBLE_LEAK("PossibleMemoryLeak", "POSSIBLE LEAK"),
		LEAK("MemoryLeak", "LEAK"),
		UNRECOGNIZED("Dr Memory unrecognized error", "");

		private String id;
		private String title;
		
		DrMemoryErrorType(String id, String title) {
			this.id = id;
			this.title = title;
		}

		public String getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}
	}
	
	
	
	public static class DrMemoryError {
		
		public static class Location {
			public String file = "";
			public Integer line;
		}
		
		public DrMemoryErrorType type = DrMemoryErrorType.UNRECOGNIZED;
		public List<Location> stackTrace = new ArrayList<Location>();
		public String message = "";
	}
	
	
	private DrMemoryParser() {
	}

	public static final Pattern rx_message_finder = Pattern.compile( "^Error #\\d+:(.*)");
	public static final Pattern rx_file_finder = Pattern.compile( "^.*\\[(.*):(\\d+)\\]$");

	public static final int __TOP_COUNT = 4;
	
	public static List<DrMemoryError> parse( File file ) throws IOException {
		
		List<DrMemoryError> result = new ArrayList<DrMemoryError>();
		
		List<String> elements = getElements(file);
		
		for (String element : elements) {
			Matcher m = rx_message_finder.matcher( element );
			
			if( m.find() ) {
				DrMemoryError error = new DrMemoryError();
				error.type =  extractErrorType(m.group(1));
				String elementSplitted[] = element.split("\\r?\\n");
				error.message  = elementSplitted[0];
				for (String elementPart : elementSplitted) {
					Matcher locationMatcher = rx_file_finder.matcher( elementPart );
					if (locationMatcher.find()) {
						Location location = new Location(); 
						location.file =  locationMatcher.group( 1 );
						location.line = Integer.valueOf(locationMatcher.group( 2 ));
						error.stackTrace.add(location);
					}
				}
				result.add(error);
				
			}
		}
		
		return result;
	}
	
	
	private static DrMemoryErrorType extractErrorType(String title) {
		String cleanedTitle = clean(title);
		for (DrMemoryErrorType drMemoryErrorType : DrMemoryErrorType.values()) {
			if (cleanedTitle.startsWith(drMemoryErrorType.getTitle())) {
				return drMemoryErrorType;
		    }
		}
		return DrMemoryErrorType.UNRECOGNIZED;
	}


	private static String clean(String title) {
		return title.trim();
	}


	public static List<String> getElements( File file ) throws IOException {
		FileReader fr = new FileReader( file );
		BufferedReader br = new BufferedReader( fr );
		List<String> list = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		String line;
		int cnt = 0;
		while( ( line = br.readLine() ) != null ) {
			if( cnt > ( __TOP_COUNT ) ) {
				
				if( line.matches( "^\\s*$" ) ) {
					list.add( sb.toString() );
					sb.setLength( 0 );
					
				} else {
					sb.append( line + '\n' );
				}
			}			
			
			cnt++;
		}
		
		if( sb.length() > 0 ) {
			list.add( sb.toString() );
		}
		
		br.close();
		
		return list;
	}
}
