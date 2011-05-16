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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Visitor;
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.SyntheticRepository;

/**
 * Helper class for BCEL based class analysis and manipulation.
 * Used by JOPizer and should be used by WCETAnalyser and
 * merged with libgraph
 * 
 * TODO: split JOP specific and general MethodInfo
 * TODO: make all general usable visitors AppVisitor instead of JOPizerVisitor
 * TODO: How do we handle the update when manipulating methods and classes?
 * 	There is this gen* in BCEL, but don't we also need to update the info
 * 	in XClassInfo and XMethodInfo?
 * 
 * @author Martin
 * @deprecated
 */
public class OldAppInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected String outFile;
	protected String srcPath;
	/**
	 * The class that contains the main method.
	 */
	protected String mainClass = "unknown";
	protected String mainMethodName = "main";
	
	// we usually provide the classpath, so this is redundant.
	public ClassPath classpath = new ClassPath(".");
	/**
	 * class list from the arguments
	 */
	private Set<String> clsArgs = new HashSet<String>();
	/**
	 * Excluded classes (e.g. JOP Native.java)
	 */
	private Set<String> exclude = new HashSet<String>();
	
	/**
	 * A template of the specific cli type to create
	 * the specific cli map (some form of factory pattern)
	 */
	private OldClassInfo template;

	/**
	 * Map of ClassInfo that represents the application.
	 * 
	 * It's public for the debugger.
	 */
	public Map<String, ? extends OldClassInfo> cliMap;
//	protected Map<String, ClassInfo> cliMap;

	public final static String clinitSig = "<clinit>()V";
	

	/**
	 * 
	 * @param cliTemplate a template to create the correct ClassInfo type
	 */
	public OldAppInfo(OldClassInfo cliTemplate) {
		template = cliTemplate;
		template.appInfo = this;
	}
	
	public String[] parseOptions(String args[]) {
		
		List<String> retList = new LinkedList<String>();

		System.out.println(args);

//		if(args.length == 0) {
//			System.err.println("arguments: [-cp classpath] [-o file] class [class]*");

		try {
			for (int i = 0; i<args.length; i++) {
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
				
				if (args[i].charAt(0)=='-') {
					// an option we don't know
					retList.add(args[i]);
					i++;
					while (i<args.length) {
						if (args[i].charAt(0)!='-') {
							retList.add(args[i]);							
						} else {
							break;
						}
					}
				} else {
					// it's a class
					clsArgs.add(args[i]);					
				}
				
				// The last class contains the main method
				// We also allow / notation as used with JCC
				mainClass = args[i].replace('/', '.');
			}
			
		} catch(Exception e) { e.printStackTrace();}

		String[] sa = new String[retList.size()];
		return retList.toArray(sa);
	}
	
	/**
	 * Add an application known class (e.g. JOP system classes)
	 * @param name
	 */
	public void addClass(String name) {
		clsArgs.add(name);
	}
	
	/**
	 * Exclude that class.
	 * @param name
	 */
	public void excludeClass(String name) {
		exclude.add(name);
	}
	
	/**
	 * Load all classes that belong to the application.
	 * @throws ClassNotFoundException 
	 */
	public void load() throws IOException, ClassNotFoundException {
		
		JavaClass[] jcl = new JavaClass[clsArgs.size()];
		Repository.setRepository(SyntheticRepository.getInstance(classpath));
		Iterator<String> i = clsArgs.iterator();
		for (int nr=0; i.hasNext(); ++nr) {
			String clname = i.next();
			jcl[nr] = Repository.lookupClass(clname);
			if(jcl[nr] == null) throw new IOException("lookupClass("+clname+") failed");
			System.out.println(jcl[nr].getClassName());
		}
		TransitiveHull hull = new TransitiveHull(classpath, jcl);
		String[] excl = new String[exclude.size()];
		hull.setExcluded(exclude.toArray(excl));
		hull.start();
		System.out.println(Arrays.asList(hull.getClassNames()));
		JavaClass[] jclazz = hull.getClasses();
		// jclazz now contains the closure of the application
		cliMap = template.genClassInfoMap(jclazz, this);
	}

	public String toString() {
		
		return cliMap.toString();
	}

	/**
	 * Iterate over all classes and run the visitor.
	 * 
	 * @param v
	 */
	public void iterate(Visitor v) {
	
		Iterator<? extends OldClassInfo> it = cliMap.values().iterator();
		while (it.hasNext()) {
			JavaClass clz = it.next().clazz;
			new DescendingVisitor(clz, v).visit();
		}
	}

	/**
	 * A simple example main that prints the Map of ClassInfo
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		OldAppInfo ai = new OldAppInfo(OldClassInfo.getTemplate());
		ai.parseOptions(args);
		System.out.println("CLASSPATH="+ai.classpath+"\tmain class="+ai.mainClass);
		try {
			ai.load();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("Provide your own main!");
		System.out.println("Ok here comes a BCEL dump of the loaded classes:");
		System.out.println(ai);
	}
	
	/** Configure the application (classpath, sourcepath, entry point) */
	public void configure(String classpath, String sourcepath,String entryClass) {
		this.classpath = new ClassPath(classpath);
		this.srcPath = sourcepath;
		addClass(entryClass);
		this.mainClass = entryClass;
	}
}
