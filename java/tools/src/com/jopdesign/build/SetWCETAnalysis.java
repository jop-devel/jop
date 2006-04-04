package com.jopdesign.build;
import org.apache.bcel.classfile.*;

/*
 * It calls the methods and the stack walker. Adapted from SetMethodInfo. 
 * @author rup, ms
 */
public class SetWCETAnalysis extends MyVisitor {
  JOPizer jz;
	public SetWCETAnalysis(JOPizer jz) {
		super(jz);
		this.jz = jz; 
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);

		Method[] methods = clazz.getMethods();
		
		for(int i=0; i < methods.length; i++) {
			Method m = methods[i];
			String methodId = m.getName()+m.getSignature();
	    MethodInfo mi = cli.getMethodInfo(methodId);
	    // WCET: Walk the method
	    WCETAnalyser.controlFlowGraph(mi);
		}
	}
}
