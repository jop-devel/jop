package com.jopdesign.build;

/**
 * Constant definitions for JOPizer
 * 
 * @author Martin
 *
 */
public class ClassStructConstants {

	/**
	 * Size of the class header.
	 * Difference between class pointer and mtab pointer.
	 * 
	 * If changed than also change in GC.java and JVM.java
	 * (checkcast, instanceof). 
	 */
	static final int CLS_HEAD = 5;
	/**
	 * Size of a method table entry.
	 */
	static final int METH_STR = 2;

}
