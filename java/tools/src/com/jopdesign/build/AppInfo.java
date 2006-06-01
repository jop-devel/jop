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

/**
 * Helper class for BCEL class analyzation and manipulation.
 * Should be used by JOPizer and WCETAnalyser
 * 
 * @author martin
 *
 */
public class AppInfo {

	org.apache.bcel.util.ClassPath classpath;
	
	/**
	 * class list from the arguments
	 */
	HashSet clsArgs = new HashSet();

	protected String outFile;
	protected String srcPath;
	protected String mainClass = "unknown";
	protected String mainMethodName = "main";
	
	/**
	 * Array of the application classes
	 */
	protected JavaClass[] jclazz;

	public AppInfo(String args[]) {
		// we usually provide the classpath, so this is
		// redundant.
		classpath = new org.apache.bcel.util.ClassPath(".");
		try {
			if(args.length == 0) {
				System.err.println("arguments: [-cp classpath] [-o file] class [class]*");
			} else {
				for (int i = 0; i < args.length; i++) {
					if (args[i].equals("-cp")) {
						i++;
						classpath = new org.apache.bcel.util.ClassPath(args[i]);
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
//					AppInfo.mainClass = args[i];
				}
				
				System.out.println("CLASSPATH="+classpath+"\tmain class="+mainClass);
				
			}
			load();
		} catch(Exception e) { e.printStackTrace();}
	}
	
	
	/**
	 * Load all classes that belong to the application.
	 */
	private void load() throws IOException {
		
		JavaClass[] jcl = new JavaClass[clsArgs.size()];
		
		Iterator i = clsArgs.iterator();
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
		// jcalzz contains now the closure of the application
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		AppInfo la = new AppInfo(args);
		System.out.println("provide your own main");
	}

}
