/*
 */
package com.jopdesign.build;


import org.apache.bcel.classfile.*;

/**
 * @author martin
 *
 * Just dump all methods to the debug text file
 */
public class Dump extends MyVisitor {

	public Dump(JOPizer jz) {
		super(jz);
	}
	
	public void visitMethod(Method method) {

		jz.outTxt.println(clazz.getClassName()+":"+method.getName()+method.getSignature());
		jz.outTxt.println(method.getCode());
	}
}
