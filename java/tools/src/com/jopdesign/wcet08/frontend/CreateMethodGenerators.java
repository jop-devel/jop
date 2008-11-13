package com.jopdesign.wcet08.frontend;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import com.jopdesign.build.AppInfo;
import com.jopdesign.build.AppVisitor;
import com.jopdesign.build.MethodInfo;

/**
 * Set {@link MethodGen} in all reachable classes 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class CreateMethodGenerators extends AppVisitor {
	public CreateMethodGenerators(AppInfo ai) {
		super(ai);
	}
	public void visitJavaClass(JavaClass clazz) {
		super.visitJavaClass(clazz);
		ConstantPoolGen cpg = new ConstantPoolGen(clazz.getConstantPool());
		Method[] methods = clazz.getMethods();
		for(int i=0; i < methods.length; i++) {
			if(!(methods[i].isAbstract() || methods[i].isNative())) {
				Method m = methods[i];
		        MethodInfo mi = getCli().getMethodInfo(m.getName()+m.getSignature());
		        mi.setMethodGen(new MethodGen(m,
		        							  mi.getCli().clazz.getClassName(),
		        							  cpg));
			}
		}
	}
}
