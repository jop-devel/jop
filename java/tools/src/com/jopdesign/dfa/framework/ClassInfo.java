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

public class ClassInfo extends com.jopdesign.build.ClassInfo {

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
			ClassInfo cli = (ClassInfo)this.cli;

			synchronized(cli) {
				if (cli.visited) {
					return;
				} else {
					cli.visited = true;
				}
			}
			
			// visit superclass first
			if (clazz.getSuperClass() != null) {
				visitJavaClass(clazz.getSuperClass());
			}
			
			// do basic stuff
			super.visitJavaClass(clazz);

			methods = cli.getMethodInfoMap();
			fields = cli.getFields();

			// add inherited members
			JavaClass[] superClazz = clazz.getSuperClasses();
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
			
			ClassInfo cli = (ClassInfo) this.cli;
			
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
				((AppInfo)appInfo).getStatements().add(exit.getStart());

				for (Iterator l = method.getInstructionList().iterator(); l.hasNext();) {
					InstructionHandle handle = (InstructionHandle) l.next();
					((AppInfo)appInfo).getStatements().add(handle);

					Instruction instr = handle.getInstruction();
					if (instr instanceof BranchInstruction) {
						if (instr instanceof Select) {
							Select s = (Select) instr;
							InstructionHandle[] target = s.getTargets();
							for (int j = 0; j < target.length; j++) {
								((AppInfo)appInfo).getFlow().addEdge(new FlowEdge(handle, target[j],
										FlowEdge.TRUE_EDGE));
							}
							((AppInfo)appInfo).getFlow().addEdge(new FlowEdge(handle, s.getTarget(),
									FlowEdge.FALSE_EDGE));
						} else {
							BranchInstruction b = (BranchInstruction) instr;
							((AppInfo)appInfo).getFlow().addEdge(new FlowEdge(handle, b.getTarget(),
									FlowEdge.TRUE_EDGE));
						}
					}
					if (handle.getNext() != null
							&& !(instr instanceof UnconditionalBranch
									|| instr instanceof Select || instr instanceof ReturnInstruction)) {
						if (instr instanceof BranchInstruction) {
							((AppInfo)appInfo).getFlow().addEdge(new FlowEdge(handle, handle.getNext(),
									FlowEdge.FALSE_EDGE));
						} else {
							((AppInfo)appInfo).getFlow().addEdge(new FlowEdge(handle, handle.getNext(),
									FlowEdge.NORMAL_EDGE));
						}
					}
					if (instr instanceof ReturnInstruction) {
						((AppInfo)appInfo).getFlow().addEdge(new FlowEdge(handle, exit.getStart(),
								FlowEdge.NORMAL_EDGE));
					}
				}

				InstructionList list = method.getInstructionList();
				list.append(exit);
				list.setPositions();
			}
		}
	}

	protected ClassInfo(JavaClass jc, AppInfo ai) {
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
	public static ClassInfo getTemplate() {
		return new ClassInfo(null, null);
	}

	/**
	 * A dummy instance for the dispatch of newClassInfo() that creates the real
	 * ClassInfo sub type
	 */
	public ClassInfo() {
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
		return new ClassInfo(jc, (AppInfo)ai);
	}

	/**
	 * Another funny factory method.
	 */
	@Override
	public com.jopdesign.build.ClassInfo.CliVisitor newCliVisitor(Map<String, com.jopdesign.build.ClassInfo> map) {
		return new CliVisitor(map);
	}

}
