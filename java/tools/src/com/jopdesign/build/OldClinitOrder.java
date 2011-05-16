/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

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

/*
 * Created on 04.06.2005
 *
 */
package com.jopdesign.build;


import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantInterfaceMethodref;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.util.InstructionFinder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Find a correct order of static class initializers (<clinit>)
 * @author martin
 * @deprecated Use ClinitOrder from common.tools with new *Info classes instead
 */
public class OldClinitOrder extends AppVisitor {


	Map clinit = new HashMap();
	
	public OldClinitOrder(OldAppInfo jz) {
		super(jz);
	}
		
	public void visitJavaClass(JavaClass clazz) {
		super.visitJavaClass(clazz);
		OldMethodInfo mi = getCli().getMethodInfo(OldAppInfo.clinitSig);
		if (mi!=null) {
			Set depends = findDependencies(getCli(), mi, false);
			clinit.put(getCli(), depends);
		}
	}	
	
	
	private Set findDependencies(OldClassInfo cli, OldMethodInfo mi, boolean inRec) {

//		System.out.println("find dep. in "+cli.clazz.getClassName()+":"+mi.getMethod().getName());
		Method method = mi.getMethod();
		Set depends = new HashSet();
		if (method.isNative() || method.isAbstract()) {
			// nothing to do
			// or should we look for all possible subclasses on
			// abstract.... or in general also all possible
			// subclasses???? :-(
			return depends;
		}

		ConstantPool cpool = cli.clazz.getConstantPool();
		ConstantPoolGen cpoolgen = new ConstantPoolGen(cpool);
		
		MethodGen mg  = new MethodGen(method, cli.clazz.getClassName(), cpoolgen);
		InstructionList il  = mg.getInstructionList();
		InstructionFinder f = new InstructionFinder(il);
		
		// find instructions that access the constant pool
		// collect all indices to constants in ClassInfo
		String cpInstr = "CPInstruction";		
		for(Iterator it = f.search(cpInstr); it.hasNext(); ) {
			InstructionHandle[] match = (InstructionHandle[])it.next();
			InstructionHandle   first = match[0];
			
			CPInstruction ii = (CPInstruction)first.getInstruction();
			int idx = ii.getIndex();
			Constant co = cpool.getConstant(idx);
			ConstantClass cocl = null;
			Set addDepends = null;
			String clname;
			OldClassInfo clinfo;
			OldMethodInfo minfo;
			switch(co.getTag()) {
			case Constants.CONSTANT_Class:
				cocl = (ConstantClass) co;
				clname = cocl.getBytes(cpool).replace('/','.');
				clinfo = (OldClassInfo) ai.cliMap.get(clname);
				
				if (clinfo!=null) {
					minfo = clinfo.getMethodInfo("<init>()V");
					if (minfo!=null) {
						addDepends = findDependencies(clinfo, minfo, true);
					}
				}
				// check for all sub classes when no going up the hierarchy
//				if (!inRec) {
//					Object[] y = clinfo.getSubClasses().toArray();
//					for (int i=0; i<y.length; ++i) {
//						clinfo = (ClassInfo) y[i];
//						minfo = clinfo.getMethodInfo("<init>()V");
//						if (minfo!=null) {
//							System.out.println("known sub classes with this method");
//							System.out.println(((ClassInfo) y[i]).clazz.getClassName());						
//						}
//					}					
//				}

				break;
			case Constants.CONSTANT_InterfaceMethodref:
				cocl = (ConstantClass) cpool.getConstant(((ConstantInterfaceMethodref) co).getClassIndex());
				break;
			case Constants.CONSTANT_Methodref:
				cocl = (ConstantClass) cpool.getConstant(((ConstantMethodref) co).getClassIndex());
				clname = cocl.getBytes(cpool).replace('/','.');
				clinfo = (OldClassInfo) ai.cliMap.get(clname);
				int sigidx = ((ConstantMethodref) co).getNameAndTypeIndex();
				ConstantNameAndType signt = (ConstantNameAndType) cpool.getConstant(sigidx);
				String sigstr = signt.getName(cpool)+signt.getSignature(cpool);
				if (clinfo!=null) {
					minfo = clinfo.getMethodInfo(sigstr);
					if (minfo!=null) {
						addDepends = findDependencies(clinfo, minfo, true);					
					}
				}
				// check for all sub classes when no going up the hierarchy
//				if (!inRec) {
//					Object[] x = clinfo.getSubClasses().toArray();
//					for (int i=0; i<x.length; ++i) {
//						clinfo = (ClassInfo) x[i];
//						minfo = clinfo.getMethodInfo(sigstr);
//						if (minfo!=null) {
//							System.out.println("known sub classes with this method");
//							System.out.println(((ClassInfo) x[i]).clazz.getClassName());						
//						}
//					}					
//				}
				
				break;
			case Constants.CONSTANT_Fieldref:
				cocl = (ConstantClass) cpool.getConstant(((ConstantFieldref) co).getClassIndex());
				break;
			}
			if (cocl!=null) {
				clname = cocl.getBytes(cpool).replace('/','.');
				OldClassInfo clinf = (OldClassInfo) ai.cliMap.get(clname);
				if (clinf!=null) {
					if (clinf.getMethodInfo(OldAppInfo.clinitSig)!=null) {
						// don't add myself as dependency
						if (clinf!=cli) {
							depends.add(clinf);
						}
					}					
				}
			}
			
			if (addDepends!=null) {
				Iterator itAddDep = addDepends.iterator();
				while (itAddDep.hasNext()) {
					OldClassInfo addCli = (OldClassInfo) itAddDep.next();
					if (addCli==cli) {
						throw new Error("cyclic indirect <clinit> dependency");
					}
					depends.add(addCli);
				}
			}
			
			
		}
		
		il.dispose();
		
		return depends;
	}
	/**
	 * Print the dependency for debugging. Not used at the moment.
	 *
	 */
	private void printDependency() {

		Set cliSet = clinit.keySet();
		Iterator itCliSet = cliSet.iterator();
		while (itCliSet.hasNext()) {
		
			OldClassInfo clinf = (OldClassInfo) itCliSet.next();
			System.out.println("Class "+clinf.clazz.getClassName());
			Set depends = (Set) clinit.get(clinf);
				
			Iterator it = depends.iterator();
			while(it.hasNext()) {
				OldClassInfo clf = (OldClassInfo) it.next();
				System.out.println("\tdepends "+clf.clazz.getClassName());
			}
		}
	}
	
	/**
	 * Find a 'correct' oder for the static <clinit>.
	 * Throws an error on cyclic dependencies.
	 * 
	 * @return the ordered list of classes
	 */
	public List findOrder() {

		printDependency();
		
		Set cliSet = clinit.keySet();
		List order = new LinkedList();
		int maxIter = cliSet.size();

		// maximum loop bound detects cyclic dependency
		for (int i=0; i<maxIter && cliSet.size()!=0; ++i) {

			Iterator itCliSet = cliSet.iterator();
			while (itCliSet.hasNext()) {			
				OldClassInfo clinf = (OldClassInfo) itCliSet.next();
				Set depends = (Set) clinit.get(clinf);
				if (depends.size()==0) {
					order.add(clinf);
					// check all depends sets and remove the added
					// element (a leave in the dependent tree
					Iterator itCliSetInner = clinit.keySet().iterator();
					while (itCliSetInner.hasNext()) {
						OldClassInfo clinfInner = (OldClassInfo) itCliSetInner.next();
						Set dep = (Set) clinit.get(clinfInner);
						dep.remove(clinf);
					}
					itCliSet.remove();
				}
			}
		}
		
		if (cliSet.size()!=0) {
			printDependency();
			throw new Error("Cyclic dependency in <clinit>");
		}
		
		return order;
	}

}
