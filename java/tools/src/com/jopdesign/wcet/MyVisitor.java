package com.jopdesign.wcet;

import org.apache.bcel.classfile.*;

/**
 * @author martin, rup
 *
 */
public class MyVisitor extends EmptyVisitor {

	protected WCETAnalyser jz;
	protected JavaClass clazz;

	public MyVisitor(WCETAnalyser jz) {
		this.jz = jz;
	}
	
	public void visitJavaClass(JavaClass clazz) {
		this.clazz = clazz;
	}

}
