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
 */
public class ResolveCPool extends MyVisitor {


	public ResolveCPool(JOPizer jz) {
		super(jz);
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);
	}
	
	public void visitConstantPool(ConstantPool cp) {
		cli.resolveCPool(cp);
	}
	

}
