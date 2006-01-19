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

		// Added by Rasmus
		// Replace GETFIELD with GETFIELD_REF_JOP for reference
		// references:-)
		f = new InstructionFinder(il);
		int cnt = 0;

		// replace the field instructions with the new reference/value type
		// instructions
		String fs = "FieldInstruction";
		for (Iterator i = f.search(fs); i.hasNext();) {
			InstructionHandle[] match = (InstructionHandle[]) i.next();
			for (int j = 0; j < match.length; j++) {
				InstructionHandle ih = match[j];
				if ((ih.getInstruction()) instanceof GETFIELD) {
					GETFIELD gf = (GETFIELD) ih.getInstruction();
					if (gf.getFieldType(cpool) instanceof ReferenceType) {
//						System.out.println("GETFIELD ReferenceType found:"
//								+ gf.getFieldName(cpool));
						int index = gf.getIndex();
						// ih.setInstruction(new
						// GETFIELD_REF_JOP((short)0xB4,(short)index)); //use
						// the GETFIELD
						ih.setInstruction(new GETFIELD_REF((short) 0xE0,
								(short) index)); // use the new GETFIELD_REF
						// System.exit(1);
						cnt++;
					}
				}
			}
		}

	    if (cnt > 0)
		System.out.println("GETFIELD found " + cnt + " matches in "
				+ clazz.getClassName() + "." + method.getName());

		
		
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
	class GETFIELD_REF extends FieldInstruction {
		public GETFIELD_REF(short arg0, short arg1) {
			super(arg0, arg1);
		}

		public void accept(org.apache.bcel.generic.Visitor v) {
		}
	}
}
