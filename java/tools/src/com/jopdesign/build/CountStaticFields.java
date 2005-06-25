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
public class CountStaticFields extends MyVisitor {

	
	public CountStaticFields(JOPizer jz) {
		super(jz);
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);
		cli.cntStaticFields();
	}
	

}
