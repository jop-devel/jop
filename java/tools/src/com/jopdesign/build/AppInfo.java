/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2005-2008, Martin Schoeberl (martin@jopdesign.com)

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

package com.jopdesign.build;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassPath;

/**
 * Helper class for BCEL class analyzation and manipulation.
 * Should be used by JOPizer and WCETAnalyser
 * 
 * @author martin
 *
 */
public class AppInfo {

	
	protected String outFile;
	protected String srcPath;
	protected String mainClass = "unknown";
	protected String mainMethodName = "main";
	
	/**
	 * Array of the application classes - should be a form of ClassInfo
	 * with the fields super/sub set.
	 */
	protected JavaClass[] jclazz;

	public AppInfo(String args[]) {
		
		// we usually provide the classpath, so this is redundant.
		ClassPath classpath = new ClassPath(".");
		// class list from the arguments
		Set<String> clsArgs = new HashSet<String>();

		try {
			if(args.length == 0) {
				System.err.println("arguments: [-cp classpath] [-o file] class [class]*");
			} else {
				for (int i = 0; i < args.length; i++) {
					if (args[i].equals("-cp")) {
						i++;
						classpath = new ClassPath(args[i]);
						continue;
					}
					if (args[i].equals("-o")) {
						i++;
						outFile = args[i];
						continue;
					}
					if (args[i].equals("-sp")) {
						i++;
						srcPath = args[i];
						continue;
					}
					if (args[i].equals("-mm")) {
						i++;
						mainMethodName = args[i];
						continue;
					}
					
					clsArgs.add(args[i]);
					
					// The last class contains the main method
					// We also allow / notation as used with JCC
					mainClass = args[i].replace('/', '.');
				}
				
				System.out.println("CLASSPATH="+classpath+"\tmain class="+mainClass);
				
			}
			load(classpath, clsArgs);
		} catch(Exception e) { e.printStackTrace();}
	}
	
	
	/**
	 * Load all classes that belong to the application.
	 */
	private void load(ClassPath classpath, Set<String> clsArgs) throws IOException {
		
		JavaClass[] jcl = new JavaClass[clsArgs.size()];
		
		Iterator<String> i = clsArgs.iterator();
		for (int nr=0; i.hasNext(); ++nr) {
			String clname = (String) i.next();
			InputStream is = classpath.getInputStream(clname);
			jcl[nr] = new ClassParser(is, clname).parse();
			System.out.println(jcl[nr].getClassName());

		}
		TransitiveHull hull = new TransitiveHull(classpath, jcl);
		hull.start();
		System.out.println(Arrays.asList(hull.getClassNames()));
		jclazz = hull.getClasses();
		// jclazz now contains the closure of the application
	}

	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<jclazz.length; ++i) {
			sb.append(jclazz[i]);
		}
		return sb.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		AppInfo ai = new AppInfo(args);
		System.out.println("Provide your own main!");
		System.out.println("Ok here comes a BCEL dump of the loaded classes:");
		System.out.println(ai);
	}

}
