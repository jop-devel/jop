/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, 2010, Peter Hilber (peter@hilber.name)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Pattern;

public class MatchPattern {
	
	protected static final String headerPattern =
		// TODO fix pattern
		/* "^.*" +
		"JOP start V [0-9]{8} \\s+" +
		"[0-9]{1,4} MHz, [0-9]{3,6} KB RAM, [0-9]{3,6} Byte on-chip RAM, [0-9]{1,2} CPUs\\s+" +
		"multianewarray \\- GC issue\\?\\s+"; */
		"^.*";

	protected static final String trailerPattern = "\\s*JVM exit!$";  

	/**
	 * @param args File containing regex pattern to be matched by System.in.
	 */
	public static void main(String[] args) throws IOException {
		byte[] input;
		
		{
			byte[] buf = new byte[1<<20];
			
			int read;
			int offset = 0;
			int capacity = buf.length;
			
			while ((read = System.in.read(buf, offset, capacity)) != -1) {
				System.out.write(buf, offset, read);
				
				offset += read;
				capacity -= read;
				
				if (capacity == 0) {
					throw new IOException();
				}
			}
			
			input = new byte[offset];
			System.arraycopy(buf, 0, input, 0, offset);			
		}
		
		File patternFile = new File(args[0]);
		long patternFileLen = patternFile.length();
		
		if (patternFileLen > Integer.MAX_VALUE) {
			throw new IOException();
		}
		
		byte[] pattern = new byte[(int)patternFileLen];
		new FileInputStream(patternFile).read(pattern);
		
		Pattern p = 
			Pattern.compile(
				headerPattern + new String(pattern) + trailerPattern, 
				Pattern.DOTALL);
		
		System.exit(p.matcher(new String(input)).matches() ? 0 : 2);
	}
}
