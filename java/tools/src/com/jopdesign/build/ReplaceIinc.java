/*
 * Created on 04.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.jopdesign.build;


import java.util.Iterator;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;
import org.apache.bcel.verifier.statics.DOUBLE_Upper;
import org.apache.bcel.verifier.statics.LONG_Upper;

import com.jopdesign.tools.JopInstr;

/**
 * @author Martin
 *
 * replaces IINC by ILOAD, push the constant, IADD, and ISTORE
 * 
 * avoids issues with the Java 1.5 compiler (produces WIDE IINC) and
 * generates faster code on JOP.
 * 
 */
public class ReplaceIinc extends MyVisitor {

	// Why do we use a ConstantPoolGen and a ConstantPool?
	private ConstantPoolGen cpoolgen;
	private ConstantPool cp;
	
	public ReplaceIinc(JOPizer jz) {
		super(jz);
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);
		
		Method[] methods = clazz.getMethods();
		cp = clazz.getConstantPool();
		cpoolgen = new ConstantPoolGen(cp);
		
		for(int i=0; i < methods.length; i++) {
			if(!(methods[i].isAbstract() || methods[i].isNative())) {
				Method m = replace(methods[i]);
		        MethodInfo mi = cli.getMethodInfo(m.getName()+m.getSignature());
		        // set new method also in MethodInfo
		        mi.setMethod(m);
				if (m!=null) {
					methods[i] = m;
				}
			}
		}
	}


	private Method replace(Method method) {
		
		MethodGen mg  = new MethodGen(method, clazz.getClassName(), cpoolgen);
		InstructionList il  = mg.getInstructionList();
		InstructionFinder f = new InstructionFinder(il);
    
		for(Iterator i = f.search("IINC"); i.hasNext(); ) {
			InstructionHandle[] match = (InstructionHandle[])i.next();
			InstructionHandle   ih = match[0];
			IINC ii = (IINC) ih.getInstruction();
			int idx = ii.getIndex();
			int inc = ii.getIncrement();
//			IINC rep = new IINC(idx, inc);
			ih.setInstruction(new ILOAD(idx));
			if (inc>=-1 && inc<=5) {
				ih = il.append(ih, new ICONST(inc));				
			} else if (inc>=-128 && inc<127){
				ih = il.append(ih, new BIPUSH((byte) inc));								
			} else if (inc>=-32768 && inc<32767){
				ih = il.append(ih, new SIPUSH((short) inc));								
			} else {
				System.out.println("IINC constant too big");
				System.exit(-1);
			}
			ih = il.append(ih, new IADD());
			ih = il.append(ih, new ISTORE(idx));
		}
		

		Method m = mg.getMethod();
		il.dispose();
		return m;

	}

}
