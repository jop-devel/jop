/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2004,2005, Flavius Gruian
  Copyright (C) 2005-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.build;

import com.jopdesign.tools.JopInstr;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MONITORENTER;
import org.apache.bcel.generic.MONITOREXIT;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.InstructionFinder;

import java.util.Iterator;

/**
 * @author Flavius, Martin
 * 
 */
public class ReplaceNativeAndCPIdx extends JOPizerVisitor {

	// Why do we use a ConstantPoolGen and a ConstantPool?
	private ConstantPoolGen cpoolgen;

	private ConstantPool cp;

	public ReplaceNativeAndCPIdx(OldAppInfo jz) {
		super(jz);
	}

	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);

		Method[] methods = clazz.getMethods();
		cp = clazz.getConstantPool();
		cpoolgen = new ConstantPoolGen(cp);

		for (int i = 0; i < methods.length; i++) {
			if (!(methods[i].isAbstract() || methods[i].isNative())) {

				Method m = replace(methods[i]);
		        OldMethodInfo mi = getCli().getMethodInfo(m.getName()+m.getSignature());
		        // set new method also in MethodInfo
		        mi.setMethod(m);
				if (m != null) {
					methods[i] = m;
				}
			}
		}
	}

	private Method replace(Method method) {

		MethodGen mg = new MethodGen(method, clazz.getClassName(), cpoolgen);
		InstructionList il = mg.getInstructionList();
		InstructionFinder f = new InstructionFinder(il);

		String methodId = method.getName() + method.getSignature();

		OldMethodInfo mi = getCli().getMethodInfo(methodId);

		// find invokes first and replace call to Native by
		// JOP native instructions.
		String invokeStr = "InvokeInstruction";
		for (Iterator i = f.search(invokeStr); i.hasNext();) {
			InstructionHandle[] match = (InstructionHandle[]) i.next();
			InstructionHandle first = match[0];
			InvokeInstruction ii = (InvokeInstruction) first.getInstruction();
			if (ii.getClassName(cpoolgen).equals(JOPizer.nativeClass)) {
				short opid = (short) JopInstr.getNative(ii
						.getMethodName(cpoolgen));
				if (opid == -1) {
					System.err.println(method.getName() + ": cannot locate "
							+ ii.getMethodName(cpoolgen)
							+ ". Replacing with NOP.");
					first.setInstruction(new NOP());
				} else {
					first.setInstruction(new NativeInstruction(opid, (short) 1));
					((JOPizer) ai).outTxt.println("\t"+first.getPosition());
					// since the new instruction is of length 1 and
					// the replaced invokespecial was of length 3
					// then we remove pc+2 and pc+1 from the MGCI info
					if (JOPizer.dumpMgci) {
						il.setPositions();
						int pc = first.getPosition();
						// important: take the high one first
						GCRTMethodInfo.removePC(pc + 2, mi);
						GCRTMethodInfo.removePC(pc + 1, mi);
					}
				}
			}

 			if (ii instanceof INVOKESPECIAL) {			    
 				// not an initializer
 				if (!ii.getMethodName(cpoolgen).equals("<init>")) {
                                     // check if this is a super invoke
                                     // TODO this is just a hack, use InvokeSite.isInvokeSuper() when this is ported to the new framework!
                                     boolean isSuper = false;

                                     String declaredType = ii.getClassName(cpoolgen);
                                     JopClassInfo cls = getCli();
                                     OldClassInfo superClass = cls.superClass;
                                     while (superClass != null) {
                                         if (superClass.clazz.getClassName().equals(declaredType)) {
                                             isSuper = true;
                                             break;
                                         }
                                         if ("java.lang.Object".equals(superClass.clazz.getClassName())) {
                                             break;
                                         }
                                         superClass = superClass.superClass;
                                     }

                                     if (isSuper) {
                                            Integer idx = ii.getIndex();
                                            int new_index = getCli().cpoolUsed.indexOf(idx) + 1;
                                            first.setInstruction(new JOPSYS_INVOKESUPER((short)new_index));
                                            // System.err.println("invokesuper "+ii.getClassName(cpoolgen)+"."+ii.getMethodName(cpoolgen));
                                     }
				}
 			}

		}

		if (JOPizer.CACHE_INVAL) {
			f = new InstructionFinder(il);
			// find volatile reads and insert cache invalidation bytecode
			String fieldInstr = "GETFIELD|GETSTATIC|PUTFIELD|PUTSTATIC";
			for(Iterator i = f.search(fieldInstr); i.hasNext(); ) {
				InstructionHandle[] match = (InstructionHandle[])i.next();
				InstructionHandle   ih = match[0];
				FieldInstruction fi = (FieldInstruction) ih.getInstruction();

				JavaClass jc = JOPizer.jz.cliMap.get(fi.getClassName(cpoolgen)).clazz;
				Field field = null;
				while (field == null) {
					Field [] fields = jc.getFields();
					for (int k = 0; k < fields.length; k++) {
						if (fields[k].getName().equals(fi.getFieldName(cpoolgen))) {
							field = fields[k];
							break;
						}
					}
					if (field == null) {
						try {
							jc = jc.getSuperClass();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
							throw new Error();
						}
					}
				}

				if (field.isVolatile()) {

					if (field.getType().getSize() < 2) {
						if (fi instanceof GETFIELD ||
							fi instanceof GETSTATIC) {
							ih.setInstruction(new InvalidateInstruction());
							ih = il.append(ih, fi);
						}
					} else {
						// this only works because we do not throw a
						// NullPointerException for monitorenter/-exit!
						ih.setInstruction(new ACONST_NULL());
						ih = il.append(ih, new MONITORENTER());
						ih = il.append(ih, fi);
						ih = il.append(ih, new ACONST_NULL());
						ih = il.append(ih, new MONITOREXIT());
					}

				}
			}
		}		

		f = new InstructionFinder(il);
		// find instructions that access the constant pool
		// and replace the index by the new value from ClassInfo
		String cpInstr = "CPInstruction";
		for (Iterator it = f.search(cpInstr); it.hasNext();) {
			InstructionHandle[] match = (InstructionHandle[]) it.next();
			InstructionHandle ih = match[0];

			CPInstruction cpii = (CPInstruction) ih.getInstruction();
			int index = cpii.getIndex();

			// we have to grab the information before we change
			// the CP index.
			FieldInstruction fi = null;
			Type ft = null;
			if (cpii instanceof FieldInstruction) {
				fi = (FieldInstruction) ih.getInstruction();
				ft = fi.getFieldType(cpoolgen);
			}

			Integer idx = new Integer(index);
			// pos is the new position in the reduced constant pool
			// idx is the position in the 'original' unresolved cpool
			int pos = getCli().cpoolUsed.indexOf(idx);
			int new_index = pos + 1;
			// replace index by the offset for getfield
			// and putfield and by address for getstatic and putstatic
			if (cpii instanceof GETFIELD || cpii instanceof PUTFIELD ||
					cpii instanceof GETSTATIC || cpii instanceof PUTSTATIC) {
				// we use the offset instead of the CP index
				new_index = getFieldOffset(cp, index);
			} else {
				if (pos == -1) {
					System.out.println("Error: constant " + index + " "
							+ cpoolgen.getConstant(index) + " not found");
					System.out.println("new cpool: " + getCli().cpoolUsed);
					System.out.println("original cpool: " + cpoolgen);

					System.exit(-1);	
				}
			}
			// set new index, position starts at
			// 1 as cp points to the length of the pool
			cpii.setIndex(new_index);

			// Added field instruction replacement
			// by Rasmus and extended by Martin
			// Replace reference and long/double field bytecodes
			// with 'special' bytecodes.

			if (cpii instanceof FieldInstruction) {

				boolean isRef = ft instanceof ReferenceType;
				boolean isLong = ft == BasicType.LONG || ft == BasicType.DOUBLE;

				if (fi instanceof GETSTATIC) {
					if (isRef) {
						ih.setInstruction(new GETSTATIC_REF((short) new_index));
					} else if (isLong) {
						ih.setInstruction(new GETSTATIC_LONG((short) new_index));
					}
				} else if (fi instanceof PUTSTATIC) {
					if (isRef) {
						if (!com.jopdesign.build.JOPizer.USE_RTTM) {
							ih.setInstruction(new PUTSTATIC_REF((short) new_index));
						}
					} else if (isLong) {
						ih.setInstruction(new PUTSTATIC_LONG((short) new_index));
					}
				} else if (fi instanceof GETFIELD) {
					if (isRef) {
						ih.setInstruction(new GETFIELD_REF((short) new_index));
					} else if (isLong) {
						ih.setInstruction(new GETFIELD_LONG((short) new_index));
					}
				} else if (fi instanceof PUTFIELD) {
					if (isRef) {
						if (!com.jopdesign.build.JOPizer.USE_RTTM) {
							ih.setInstruction(new PUTFIELD_REF((short) new_index));
						}
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

	/**
	 * Get field offset: relative affset for object fields, absolute
	 * addresses for static fields.
	 * 
	 * TODO: remove the dead code in constant pool replacement
	 * TODO: remove not used constants
	 * @param cp
	 * @param index
	 * @return
	 */
	private int getFieldOffset(ConstantPool cp, int index) {

		// from ClassInfo.resolveCPool

		Constant co = cp.getConstant(index);

		int fidx = ((ConstantFieldref) co).getClassIndex();
		ConstantClass fcl = (ConstantClass) cp.getConstant(fidx);
		String fclname = fcl.getBytes(cp).replace('/', '.');
		// got the class name
		int sigidx = ((ConstantFieldref) co).getNameAndTypeIndex();
		ConstantNameAndType signt = (ConstantNameAndType) cp
				.getConstant(sigidx);
		String sigstr = signt.getName(cp) + signt.getSignature(cp);
		JopClassInfo clinf = (JopClassInfo) ai.cliMap.get(fclname);
		int j;
		boolean found = false;
		while (!found) {
			for (j = 0; j < clinf.clft.len; ++j) {
				if (clinf.clft.key[j].equals(sigstr)) {
					found = true;
					return clinf.clft.idx[j];
				}
			}
			if (!found) {
				clinf = (JopClassInfo) clinf.superClass;
				if (clinf == null) {
					System.out.println("Error: field " + fclname + "." + sigstr
							+ " not found!");
					break;
				}
			}
		}
		System.out.println("Error in getFieldOffset()");
		System.exit(-1);
		return 0;
	}
	

	class GETSTATIC_REF extends FieldInstruction {
		public GETSTATIC_REF(short index) {
			super((short) JopInstr.get("getstatic_ref"), index);
		}

		public void accept(org.apache.bcel.generic.Visitor v) {
		}
	}

	class PUTSTATIC_REF extends FieldInstruction {
		public PUTSTATIC_REF(short index) {
			super((short) JopInstr.get("putstatic_ref"), index);
		}

		public void accept(org.apache.bcel.generic.Visitor v) {
		}
	}

	class GETFIELD_REF extends FieldInstruction {
		public GETFIELD_REF(short index) {
			super((short) JopInstr.get("getfield_ref"), index);
		}

		public void accept(org.apache.bcel.generic.Visitor v) {
		}
	}

	class PUTFIELD_REF extends FieldInstruction {
		public PUTFIELD_REF(short index) {
			super((short) JopInstr.get("putfield_ref"), index);
		}

		public void accept(org.apache.bcel.generic.Visitor v) {
		}
	}

	class GETSTATIC_LONG extends FieldInstruction {
		public GETSTATIC_LONG(short index) {
			super((short) JopInstr.get("getstatic_long"), index);
		}

		public void accept(org.apache.bcel.generic.Visitor v) {
		}
	}

	class PUTSTATIC_LONG extends FieldInstruction {
		public PUTSTATIC_LONG(short index) {
			super((short) JopInstr.get("putstatic_long"), index);
		}

		public void accept(org.apache.bcel.generic.Visitor v) {
		}
	}

	class GETFIELD_LONG extends FieldInstruction {
		public GETFIELD_LONG(short index) {
			super((short) JopInstr.get("getfield_long"), index);
		}

		public void accept(org.apache.bcel.generic.Visitor v) {
		}
	}

	class PUTFIELD_LONG extends FieldInstruction {
		public PUTFIELD_LONG(short index) {
			super((short) JopInstr.get("putfield_long"), index);
		}

		public void accept(org.apache.bcel.generic.Visitor v) {
		}
	}

	class NativeInstruction extends Instruction {
		public NativeInstruction(short arg0, short arg1) {
			super(arg0, arg1);
		}

		public void accept(org.apache.bcel.generic.Visitor v) {
		}
	}

	class JOPSYS_INVOKESUPER extends InvokeInstruction {
		public JOPSYS_INVOKESUPER(short index) {
			super((short) JopInstr.get("invokesuper"), index);
		}

		public void accept(org.apache.bcel.generic.Visitor v) {
		}

		// could be copied from INVOKESPECIAL
		public Class[] getExceptions() {
		       return null;
		}
	}

	class InvalidateInstruction extends Instruction {
		public InvalidateInstruction() {
			super((short)JopInstr.get("jopsys_inval"), (short)1);
		}

		public void accept(org.apache.bcel.generic.Visitor v) {
		}
	}

}
