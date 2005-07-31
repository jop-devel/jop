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
public class SetClassInfo extends MyVisitor {

	private ConstantPool cpool;
	
	public SetClassInfo(JOPizer jz) {
		super(jz);
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);
// System.err.println("visit "+clazz.getClassName()+" getSuper");
		cli.superClass = ClassInfo.getClassInfo(clazz.getSuperclassName());
		if (clazz.getClassName().equals("java.lang.Object")) {
			// Object has no super class
			cli.superClass = null;
		}
		// this one searches in the application CLASSPATH!!!
		/*
		JavaClass suClazz = clazz.getSuperClass();
		if (suClazz!=null) {
			cli.superClass = ClassInfo.getClassInfo(suClazz.getClassName());
		} else {
			cli.superClass = null;
		}
		*/
// System.err.println("after visit getSuper");
		cpool = clazz.getConstantPool();
	}

	public void visitMethod(Method method) {
		
		String methodId = method.getName()+method.getSignature();
        cli.addMethodOnce(methodId);
        MethodInfo mi = cli.getMethodInfo(methodId);
	}

	public void visitConstantString(ConstantString S) {
		// identifying constant strings
		StringInfo.addString(S.getBytes(cpool));
	}

}
