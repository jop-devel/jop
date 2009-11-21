package com.jopdesign.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Pattern;

public class MatchPattern {
	
	protected static final String headerPattern =
		"^[0-9]{1,6} words of Java bytecode \\([0-9]{1,4} KB\\)\\s+" +
		"[0-9]{1,6} words external RAM \\([0-9]{1,4} KB\\)\\s+" +
		"download complete\\s+" +
		"JOP start V [0-9]{8} \\s+" +
		"[0-9]{1,4} MHz, [0-9]{3,6} KB RAM, [0-9]{3,6} Byte on-chip RAM, [0-9]{1,2} CPUs\\s+" +
		"multianewarray \\- GC issue\\?\\s+";

	protected static final String trailerPattern = "\\s*JVM exit!$";  

	/**
	 * @param args File containing regex pattern to be matched.
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
		
		System.exit(p.matcher(new String(input)).matches() ? 0 : 1);
	}
}
