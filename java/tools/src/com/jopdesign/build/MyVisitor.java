/*
 * Created on 04.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.jopdesign.build;


import org.apache.bcel.classfile.*;

/**
 * @author martin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MyVisitor extends EmptyVisitor {

	protected JOPizer jz;
	protected ClassInfo cli;
	protected JavaClass clazz;

	public MyVisitor(JOPizer jz) {
		this.jz = jz;
	}
	
	public void visitJavaClass(JavaClass clazz) {
		this.clazz = clazz;
		cli = ClassInfo.getClassInfo(clazz.getClassName());
	}

}
