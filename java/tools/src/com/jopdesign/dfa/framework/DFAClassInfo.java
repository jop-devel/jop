/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Wolfgang Puffitsch

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

package com.jopdesign.dfa.framework;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.UnconditionalBranch;

import com.jopdesign.build.MethodInfo;

public class DFAClassInfo extends com.jopdesign.build.ClassInfo {

	private static final long serialVersionUID = 1L;

	public class CliVisitor extends com.jopdesign.build.ClassInfo.CliVisitor {

		private static final long serialVersionUID = 1L;

		private Map<String, MethodInfo> methods;
		private Set<String> fields;

		public CliVisitor(Map<String, com.jopdesign.build.ClassInfo> classes) {			
			super(classes);
		}

		public void visitJavaClass(JavaClass clazz) {

			this.cli = map.get(clazz.getClassName());
			DFAClassInfo cli = (DFAClassInfo)this.cli;

			synchronized(cli) {
				if (cli.visited) {
					return;
				} else {
					cli.visited = true;
				}
			}
			
			// visit superclass first
			try {
				if (clazz.getSuperClass() != null) {
					visitJavaClass(clazz.getSuperClass());
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new Error();
			}
			
			// do basic stuff
			super.visitJavaClass(clazz);

			methods = cli.getMethodInfoMap();
			fields = cli.getFields();

			// add inherited members
			JavaClass[] superClazz;
			try {
				superClazz = clazz.getSuperClasses();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new Error();
			}
			for (int i = superClazz.length - 1; i >= 0; --i) {
				Method[] m = superClazz[i].getMethods();
				for (int k = 0; k < m.length; k++) {
					if (!m[k].isPrivate() && !m[k].getName().equals("<clinit>") && !m[k].getName().equals("<init>")) {
						String methodId = m[k].getName() + m[k].getSignature();
						methods.put(methodId, map.get(superClazz[i].getClassName()).getMethodInfo(methodId));
					}
				}

				Field[] f = superClazz[i].getFields();
				for (int k = 0; k < f.length; k++) {
					String fieldId = f[k].getName();
					fields.add(fieldId);
				}
			}

			// add dummy method infos
			Method[] m = clazz.getMethods();
			for (int k = 0; k < m.length; k++) {
				String methodId = m[k].getName() + m[k].getSignature();
				MethodInfo mi = cli.newMethodInfo(methodId);
				methods.put(methodId, mi);
			}

			Field[] f = clazz.getFields();
			for (int k = 0; k < f.length; k++) {
				String fieldId = f[k].getName();
				fields.add(fieldId);
			}
		}

		public void visitMethod(Method method) {
			
			DFAClassInfo cli = (DFAClassInfo) this.cli;
			
			String methodId = method.getName() + method.getSignature();
	        MethodGen mg = new MethodGen(method, cli.clazz.getClassName(), new ConstantPoolGen(method.getConstantPool()));
	        MethodInfo mi = cli.getMethodInfo(methodId);
			mi.setMethodGen(mg);
			
			processMethod(mg);
			cli.list.add(mi);
		}

		private void processMethod(MethodGen method) {

			if (method.getInstructionList() != null) {

				InstructionList exit = new InstructionList(new NOP());
				((DFAAppInfo)appInfo).getStatements().add(exit.getStart());

				for (Iterator l = method.getInstructionList().iterator(); l.hasNext();) {
					InstructionHandle handle = (InstructionHandle) l.next();
					((DFAAppInfo)appInfo).getStatements().add(handle);

					Instruction instr = handle.getInstruction();
					if (instr instanceof BranchInstruction) {
						if (instr instanceof Select) {
							Select s = (Select) instr;
							InstructionHandle[] target = s.getTargets();
							for (int j = 0; j < target.length; j++) {
								((DFAAppInfo)appInfo).getFlow().addEdge(new FlowEdge(handle, target[j],
										FlowEdge.TRUE_EDGE));
							}
							((DFAAppInfo)appInfo).getFlow().addEdge(new FlowEdge(handle, s.getTarget(),
									FlowEdge.FALSE_EDGE));
						} else {
							BranchInstruction b = (BranchInstruction) instr;
							((DFAAppInfo)appInfo).getFlow().addEdge(new FlowEdge(handle, b.getTarget(),
									FlowEdge.TRUE_EDGE));
						}
					}
					if (handle.getNext() != null
							&& !(instr instanceof UnconditionalBranch
									|| instr instanceof Select || instr instanceof ReturnInstruction)) {
						if (instr instanceof BranchInstruction) {
							((DFAAppInfo)appInfo).getFlow().addEdge(new FlowEdge(handle, handle.getNext(),
									FlowEdge.FALSE_EDGE));
						} else {
							((DFAAppInfo)appInfo).getFlow().addEdge(new FlowEdge(handle, handle.getNext(),
									FlowEdge.NORMAL_EDGE));
						}
					}
					if (instr instanceof ReturnInstruction) {
						((DFAAppInfo)appInfo).getFlow().addEdge(new FlowEdge(handle, exit.getStart(),
								FlowEdge.NORMAL_EDGE));
					}
				}

				InstructionList list = method.getInstructionList();
				list.append(exit);
				list.setPositions();
			}
		}
	}

	protected DFAClassInfo(JavaClass jc, DFAAppInfo ai) {
		super(jc, ai);

		visited = false;
		fields = new HashSet<String>();
	}
	
	private boolean visited;
	private Set<String> fields;
	
	/**
	 * A dummy instance for the dispatch of newClassInfo() that
	 * creates the real ClassInfo sub type
	 */
	public static DFAClassInfo getTemplate() {
		return new DFAClassInfo(null, null);
	}

	/**
	 * A dummy instance for the dispatch of newClassInfo() that creates the real
	 * ClassInfo sub type
	 */
	public DFAClassInfo() {
		super(null, null);
	}

	public Set<String> getFields() {
		return fields;
	}

	/**
	 * A funny version of a factory method to create ClassInfo types. Has to be
	 * overwritten by each sub-type.
	 */
	@Override
	public com.jopdesign.build.ClassInfo newClassInfo(JavaClass jc, com.jopdesign.build.AppInfo ai) {
		return new DFAClassInfo(jc, (DFAAppInfo)ai);
	}

	/**
	 * Another funny factory method.
	 */
	@Override
	public com.jopdesign.build.ClassInfo.CliVisitor newCliVisitor(Map<String, com.jopdesign.build.ClassInfo> map) {
		return new CliVisitor(map);
	}

}
