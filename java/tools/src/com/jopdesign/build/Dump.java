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
public class Dump extends EmptyVisitor {

	private JOPizer jz;
	private ClassInfo cli;
	private JavaClass clazz;

	public Dump(JOPizer jz) {
		this.jz = jz;
	}
	
	public void visitJavaClass(JavaClass clazz) {

		this.clazz = clazz;
		cli = ClassInfo.getClassInfo(clazz.getClassName());
	}

	public void visitMethod(Method method) {

		jz.out.println(clazz.getClassName()+":"+method.getName()+method.getSignature());
		jz.out.println(method.getCode());
	}
}
