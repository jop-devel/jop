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
public class ClassAddress extends MyVisitor {

	private int addr;
	
	public ClassAddress(JOPizer jz, int addr) {
		super(jz);
		this.addr = addr;
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);
		addr = cli.setAddress(addr);
	}
	
	public int getAddress() {
		return addr;
	}

}
