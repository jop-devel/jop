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
		
//		System.out.println("Replace: "+method.getName());
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

		// Added field instruction replacement
		// by Rasmus and extended by Martin
		// Replace reference and long/double field bytecodes
		// with 'special' bytecodes.
		// TODO: also replace index by the offset into the object
		// on method fields
		
		
		f = new InstructionFinder(il);
		// find instructions that access the constant pool
		// and replace the index by the new value from ClassInfo
		String cpInstr = "CPInstruction";		
		for(Iterator it = f.search(cpInstr); it.hasNext(); ) {
			InstructionHandle[] match = (InstructionHandle[])it.next();
			InstructionHandle   ih = match[0];
			
			CPInstruction cpii = (CPInstruction)ih.getInstruction();
			int index = cpii.getIndex();
			
			// we have to grab the information before we change
			// the CP index.
			FieldInstruction fi = null;
			Type ft = null;
			if (cpii instanceof FieldInstruction) {
				fi = (FieldInstruction) ih.getInstruction();
				ft = fi.getFieldType(cpool);
			}

			Integer idx = new Integer(index);
			// pos is the new position in the reduced constant pool
			// idx is the position in the 'original' unresolved cpool
			int pos = cli.cpoolUsed.indexOf(idx);
			int new_index = pos+1;
			if (pos==-1) {
				System.out.println("Error: constant "+index+" "+cpool.getConstant(index)+
						" not found");
				System.out.println("new cpool: "+cli.cpoolUsed);
				System.out.println("original cpool: "+cpool);
				
				System.exit(-1);
			} else {
//				if (cpii instanceof GETFIELD || cpii instanceof GETFIELD) {
//					System.out.println("CPI get/putfield");
//				} else if (cpii instanceof GETFIELD_REF){
//					System.out.println("CPI getfield_ref");					
//				}
				// set new index, position starts at
				// 1 as cp points to the length of the pool
// System.out.println(cli.clazz.getClassName()+"."+method.getName()+" "+ii+" -> "+(pos+1));
				cpii.setIndex(new_index);
			}
			
			if (cpii instanceof FieldInstruction) {
//				System.out.println("Field instruction");
				
				boolean isRef = ft instanceof ReferenceType;
				boolean	isLong = ft==BasicType.LONG || ft==BasicType.DOUBLE;

				if (fi instanceof GETSTATIC) {
					if (isRef) {
						ih.setInstruction(new GETSTATIC_REF((short) new_index));
					} else if (isLong) {
						ih.setInstruction(new GETSTATIC_LONG((short) new_index));
					}
				} else if (fi instanceof PUTSTATIC) {
					if (isRef) {
						ih.setInstruction(new PUTSTATIC_REF((short) new_index));
					} else if (isLong) {
						ih.setInstruction(new PUTSTATIC_LONG((short) new_index));
					}
				} else if (fi instanceof GETFIELD) {
					if (isRef) {
						ih.setInstruction(new GETFIELD_REF((short) new_index));
//						System.out.println("get ref");
					} else if (isLong) {
						ih.setInstruction(new GETFIELD_LONG((short) new_index));
//						System.out.println("get long");
					} else {
//						System.out.println("get word");						
					}
				} else if (fi instanceof PUTFIELD) {
					if (isRef) {
						ih.setInstruction(new PUTFIELD_REF((short) new_index));
					} else if (isLong) {
						ih.setInstruction(new PUTFIELD_LONG((short) new_index));
					}
				}
			}
		}
		

		Method m = mg.getMethod();
		il.dispose();
		return m;

	}
	class GETSTATIC_REF extends FieldInstruction {
		public GETSTATIC_REF(short index) {
			super((short) JopInstr.get("getstatic_ref"), index);
		}
		public void accept(org.apache.bcel.generic.Visitor v) {}
	}
	class PUTSTATIC_REF extends FieldInstruction {
		public PUTSTATIC_REF(short index) {
			super((short) JopInstr.get("putstatic_ref"), index);
		}
		public void accept(org.apache.bcel.generic.Visitor v) {}
	}
	class GETFIELD_REF extends FieldInstruction {
		public GETFIELD_REF(short index) {
			super((short) JopInstr.get("getfield_ref"), index);
		}
		public void accept(org.apache.bcel.generic.Visitor v) {}
	}
	class PUTFIELD_REF extends FieldInstruction {
		public PUTFIELD_REF(short index) {
			super((short) JopInstr.get("putfield_ref"), index);
		}
		public void accept(org.apache.bcel.generic.Visitor v) {}
	}
	class GETSTATIC_LONG extends FieldInstruction {
		public GETSTATIC_LONG(short index) {
			super((short) JopInstr.get("getstatic_long"), index);
		}
		public void accept(org.apache.bcel.generic.Visitor v) {}
	}
	class PUTSTATIC_LONG extends FieldInstruction {
		public PUTSTATIC_LONG(short index) {
			super((short) JopInstr.get("putstatic_long"), index);
		}
		public void accept(org.apache.bcel.generic.Visitor v) {}
	}
	class GETFIELD_LONG extends FieldInstruction {
		public GETFIELD_LONG(short index) {
			super((short) JopInstr.get("getfield_long"), index);
		}
		public void accept(org.apache.bcel.generic.Visitor v) {}
	}
	class PUTFIELD_LONG extends FieldInstruction {
		public PUTFIELD_LONG(short index) {
			super((short) JopInstr.get("putfield_long"), index);
		}
		public void accept(org.apache.bcel.generic.Visitor v) {}
	}
	class NativeInstruction extends Instruction {
		public NativeInstruction(short arg0, short arg1) {
			super(arg0, arg1);
		}
		public void accept(org.apache.bcel.generic.Visitor v) {}
	}
}
