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
public class SetMethodInfo extends MyVisitor {

	private int addr;

	public SetMethodInfo(JOPizer jz) {
		super(jz);
		addr = jz.codeStart;
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);


		Method[] methods = clazz.getMethods();
		
		for(int i=0; i < methods.length; i++) {
			Method m = methods[i];
			String methodId = m.getName()+m.getSignature();
	        MethodInfo mi = cli.getMethodInfo(methodId);
	        mi.setMethod(m, addr);
	        addr += mi.getLength();
		}
		
		jz.pointerAddr = addr;
	}


}
