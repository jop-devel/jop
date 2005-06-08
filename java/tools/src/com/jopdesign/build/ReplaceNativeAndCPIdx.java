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
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.util.InstructionFinder;

import com.jopdesign.tools.JopInstr;

/**
 * @author Flavius, Martin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ReplaceNativeAndCPIdx extends MyVisitor {

	private ConstantPoolGen cpool;
	
	public ReplaceNativeAndCPIdx(JOPizer jz) {
		super(jz);
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);
		
		Method[] methods = clazz.getMethods();
		cpool = new ConstantPoolGen(clazz.getConstantPool());
		
		for(int i=0; i < methods.length; i++) {
			if(!(methods[i].isAbstract() || methods[i].isNative())) {
				Method m = replace(methods[i]);
				if (m!=null) {
					methods[i] = m;
				}
			}
		}
	}


	private Method replace(Method method) {
		
		MethodGen mg  = new MethodGen(method, clazz.getClassName(), cpool);
		InstructionList il  = mg.getInstructionList();
		InstructionFinder f = new InstructionFinder(il);
		
		// find invokes first and replace call to Native by
		// JOP native instructions.
		String invokeStr = "InvokeInstruction";		
		for(Iterator i = f.search(invokeStr); i.hasNext(); ) {
			InstructionHandle[] match = (InstructionHandle[])i.next();
			InstructionHandle   first = match[0];
			InvokeInstruction ii = (InvokeInstruction)first.getInstruction();
			if(ii.getClassName(cpool).equals(JOPizer.nativeClass)) {
				short opid = (short) JopInstr.getNative(ii.getMethodName(cpool));
				if(opid == -1) {
					System.err.println(method.getName()+": cannot locate "+ii.getMethodName(cpool)+". Replacing with NOP.");
					first.setInstruction(new NOP());
				} else {
					first.setInstruction(new NativeInstruction(opid, (short)1));
				}
			}
		}

		f = new InstructionFinder(il);
		// find instructions that access the constant pool
		// and replace the index by the new value from ClassInfo
		String cpInstr = "CPInstruction";		
		for(Iterator it = f.search(cpInstr); it.hasNext(); ) {
			InstructionHandle[] match = (InstructionHandle[])it.next();
			InstructionHandle   first = match[0];
			
			CPInstruction ii = (CPInstruction)first.getInstruction();
			int i = ii.getIndex();
			Integer idx = new Integer(i);
			// pos is the new position in the reduced constant pool
			// idx is the position in the 'original' unresolved cpool
			int pos = cli.cpoolUsed.indexOf(idx);
			if (pos==-1) {
				System.out.println("Error: constant "+i+" "+cpool.getConstant(i)+
						" not found");
				System.out.println("new cpool: "+cli.cpoolUsed);
				System.out.println("original cpool: "+cpool);
				
				System.exit(-1);
			} else {
				// set new index, position starts at
				// 1 as cp points to the length of the pool
// System.out.println(cli.clazz.getClassName()+"."+method.getName()+" "+ii+" -> "+(pos+1));
				ii.setIndex(pos+1);
			}
		}

		Method m = mg.getMethod();
		il.dispose();
		return m;

	}
	
}
