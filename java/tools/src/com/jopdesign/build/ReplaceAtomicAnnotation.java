/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Peter Hilber (peter@hilber.name)

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

import com.jopdesign.common.bcel.AnnotationAttribute;
import com.jopdesign.sys.Const;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.DSTORE;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.IndexedInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.LSTORE;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.InstructionFinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Known limitations: 
 * - can't load string constants because they are already indexed in jz.load()
 * - return type double and long not supported
 * 
 * TODO: check method signature
 * 
 * @author Peter Hilber (peter@hilber.name)
 *
 */
public class ReplaceAtomicAnnotation extends JOPizerVisitor {

	public ReplaceAtomicAnnotation(OldAppInfo jz) {
		super(jz);
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);
		
		Method[] methods = clazz.getMethods();
		
		for(int i = 0; i < methods.length; i++) {
			for (Attribute a: methods[i].getAttributes()) {
				if (a instanceof AnnotationAttribute) {
					if (((AnnotationAttribute)a).hasAtomicAnnotation()) {
						ConstantPoolGen cpoolgen = new ConstantPoolGen(clazz.getConstantPool()); 
						
						Method nm = transform(methods[i], clazz, cpoolgen);						
				        OldMethodInfo mi = getCli().getMethodInfo(nm.getName()+nm.getSignature());
				        // set new method also in MethodInfo
				        mi.setMethod(nm);
						
						methods[i] = nm;
						
						clazz.setConstantPool(cpoolgen.getFinalConstantPool());
						
						System.out.println(
								"RTTM: transformed atomic method " + 
								clazz.getClassName() + "." + nm.getName() + 
								nm.getSignature());
					}
				}
			}
		}
	}
	
	protected static int getArgsCount(MethodGen method) {
        int max = method.isStatic() ? 0 : 1;
        Type[] arg_types = method.getArgumentTypes();
        if (arg_types != null) {
            for (int i = 0; i < arg_types.length; i++) {
                max += arg_types[i].getSize();
            }
        }
        
        return max;
	}
	
	protected static SortedSet<Integer> getModifiedArguments(MethodGen method) {
		SortedSet<Integer> result = new TreeSet<Integer>();
		
		int arguments = getArgsCount(method);
		
		/*
		 * local variables are modified only by the bytecodes
		 * astore, astore_<n>, dstore, dstore_<n>, fstore, fstore_<n>, iinc, 
		 * istore, istore_<n>, lstore, lstore_<n>
		 */

		for (Instruction in: method.getInstructionList().getInstructions()) {
			if (in instanceof IndexedInstruction) {
				IndexedInstruction i = (IndexedInstruction)in;
				
				if (i.getIndex() < arguments) {
					if (i instanceof DSTORE || i instanceof LSTORE) {
						result.add(i.getIndex());
						result.add(i.getIndex()+1);
					} else if (i instanceof StoreInstruction || i instanceof IINC) {
						result.add(i.getIndex());
					}
				}
			}
		}
		
		return result;
	}
	
	public static Method transform(Method m, JavaClass clazz, 
			  ConstantPoolGen _cp) {
		MethodGen method = new MethodGen(m, clazz.getClassName(), _cp);
		InstructionList oldIl = method.getInstructionList();
		
		Type returnType = m.getReturnType();
		
		if (returnType.equals(Type.LONG) || returnType.equals(Type.DOUBLE)) {
			throw new UnsupportedOperationException();
		}
		
		final int transactionLocals = 2;
		
		/*
		 * local variable indices:
		 * isNotNestedTransaction is -2+2
		 * result is -2+3
		 * Throwable e is -2+3 
		 */
		
		final int maxLocals = method.getMaxLocals();
		
		final int transactionLocalsBaseIndex = maxLocals; 
		final int copyBaseIndex = transactionLocalsBaseIndex + transactionLocals;
		
		SortedSet<Integer> modifiedArguments = getModifiedArguments(method);
		
		// maps modified arguments indices to copies
		Map<Integer, Integer> modifiedArgumentsCopies = 
			new TreeMap<Integer, Integer>();
		
		{
			int copyIndex = copyBaseIndex;
			for (Integer i: modifiedArguments) {
				System.out.println("RTTM: method " + 
						method.getClassName() + "." + method.getName() + 
						method.getSignature() + 
						": saving argument " + i + " to variable " + 
						copyIndex);							
				
				modifiedArgumentsCopies.put(i, copyIndex++);
			}
		}
		
		InstructionList il = new InstructionList();
		InstructionFactory _factory = new InstructionFactory(_cp);
		method.setInstructionList(il);
		
		{
//			InstructionHandle ih_0 = il.append(new PUSH(_cp, -559038737));
//			il.append(_factory.createStore(Type.INT, transactionLocalsBaseIndex-2+1));
			InstructionHandle ih_3 = il.append(_factory.createFieldAccess("rttm.internal.Utils", "inTransaction", new ArrayType(Type.BOOLEAN, 1), Constants.GETSTATIC));
			il.append(new PUSH(_cp, -122));
			il.append(_factory.createInvoke("com.jopdesign.sys.Native", "rd", Type.INT, new Type[] { Type.INT }, Constants.INVOKESTATIC));
			il.append(InstructionConstants.BALOAD);
			    BranchInstruction ifne_12 = InstructionFactory.createBranchInstruction(Constants.IFNE, null);
			il.append(ifne_12);
			il.append(new PUSH(_cp, 1));
			    BranchInstruction goto_16 = InstructionFactory.createBranchInstruction(Constants.GOTO, null);
			il.append(goto_16);
			InstructionHandle ih_19 = il.append(new PUSH(_cp, 0));
			InstructionHandle ih_20 = il.append(InstructionFactory.createStore(Type.INT, transactionLocalsBaseIndex-2+2));
			InstructionHandle ih_21 = il.append(InstructionFactory.createLoad(Type.INT, transactionLocalsBaseIndex-2+2));
			    BranchInstruction ifeq_22 = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
			il.append(ifeq_22);
//			InstructionHandle ih_25 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex-2+0));
//			il.append(_factory.createStore(Type.INT, transactionLocalsBaseIndex-2+1));
			
		    {
		    	// only save arguments which might be modified
		    	for (int i: modifiedArguments) {
		    		il.append(InstructionFactory.createLoad(Type.INT, i));
		    		il.append(InstructionFactory.createStore(Type.INT, 
		    				modifiedArgumentsCopies.get(i)));
		    	}
		    }		    			
			
			InstructionHandle ih_27 = il.append(new PUSH(_cp, 0));
			il.append(new PUSH(_cp, -128));
			il.append(_factory.createInvoke("com.jopdesign.sys.Native", "wr", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKESTATIC));
			InstructionHandle ih_33 = il.append(_factory.createFieldAccess("rttm.internal.Utils", "inTransaction", new ArrayType(Type.BOOLEAN, 1), Constants.GETSTATIC));
			il.append(new PUSH(_cp, -122));
			il.append(_factory.createInvoke("com.jopdesign.sys.Native", "rd", Type.INT, new Type[] { Type.INT }, Constants.INVOKESTATIC));
			il.append(new PUSH(_cp, 1));
			il.append(InstructionConstants.BASTORE);
			
			// transaction loop
			
			InstructionHandle ih_43 = il.append(InstructionFactory.createLoad(Type.INT, transactionLocalsBaseIndex-2+2));
			    BranchInstruction ifeq_44 = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
			il.append(ifeq_44);
			InstructionHandle ih_47 = il.append(new PUSH(_cp, 1));
			il.append(new PUSH(_cp, Const.MEM_TM_MAGIC));
			il.append(_factory.createInvoke("com.jopdesign.sys.Native", "wrMem", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKESTATIC));
//			InstructionHandle ih_53 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex-2+0));
//			il.append(_factory.createInvoke("rttm.swtest.Transaction", "atomicSection", Type.INT, new Type[] { Type.INT }, Constants.INVOKESTATIC));
//			il.append(_factory.createStore(Type.INT, transactionLocalsBaseIndex-2+3));
			
			InstructionHandle ih_53 = oldIl.getStart();
		    Collection<BranchInstruction> gotos_transactionCommit = new ArrayList<BranchInstruction>();
		    {
		    	// redirect returns
		    	
		    	InstructionFinder f = new InstructionFinder(oldIl);
		    	
		    	String returnInstructionsPattern = "ARETURN|IRETURN|FRETURN|RETURN";
		    	
		    	for (Iterator i = f.search(returnInstructionsPattern); i.hasNext(); ) {
		    		InstructionHandle oldIh = ((InstructionHandle[])i.next())[0];
		    		
		    		InstructionList nl = new InstructionList();
		    				    		
		    		if (!method.getReturnType().equals(Type.VOID)) {		    		
			    		nl.append(InstructionFactory.createStore(
			    			method.getReturnType(), transactionLocalsBaseIndex-2+3));
		    		}
			    	
		    		BranchInstruction goto_transactionCommit = 
		    			InstructionFactory.createBranchInstruction(Constants.GOTO, null);
			    	nl.append(goto_transactionCommit);
			    	gotos_transactionCommit.add(goto_transactionCommit);
			    	
			    	InstructionHandle newTarget = nl.getStart();
			    	oldIl.append(oldIh, nl);
		    		
		    		try {
		    			oldIl.delete(oldIh);
		    		} catch(TargetLostException e) {
		    			InstructionHandle[] targets = e.getTargets();
		    			for(int k=0; k < targets.length; k++) {
		    				InstructionTargeter[] targeters = targets[k].getTargeters();
		    				for(int j=0; j < targeters.length; j++) {
		    					targeters[j].updateTarget(targets[k], newTarget);
		    				}
		    		    }
		    		}
		    	}
		    	
		    	il.append(oldIl);
		    }
			
			InstructionHandle ih_58 = il.append(InstructionFactory.createLoad(Type.INT, transactionLocalsBaseIndex-2+2));
			    BranchInstruction ifeq_59 = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
			il.append(ifeq_59);
			InstructionHandle ih_62 = il.append(new PUSH(_cp, 0));
			il.append(new PUSH(_cp, Const.MEM_TM_MAGIC));
			il.append(_factory.createInvoke("com.jopdesign.sys.Native", "wrMem", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKESTATIC));
			InstructionHandle ih_68 = il.append(_factory.createFieldAccess("rttm.internal.Utils", "inTransaction", new ArrayType(Type.BOOLEAN, 1), Constants.GETSTATIC));
			il.append(new PUSH(_cp, -122));
			il.append(_factory.createInvoke("com.jopdesign.sys.Native", "rd", Type.INT, new Type[] { Type.INT }, Constants.INVOKESTATIC));
			il.append(new PUSH(_cp, 0));
			il.append(InstructionConstants.BASTORE);
			InstructionHandle ih_78 = il.append(new PUSH(_cp, 1));
			il.append(new PUSH(_cp, -128));
			il.append(_factory.createInvoke("com.jopdesign.sys.Native", "wr", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKESTATIC));
//			InstructionHandle ih_84 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex-2+3));
//			il.append(_factory.createReturn(Type.INT));
			
			InstructionHandle ih_84;
		    {
		    	// return
		    	if (!method.getReturnType().equals(Type.VOID)) {
				    ih_84 = il.append(InstructionFactory.createLoad(method.getReturnType(), 
				    		transactionLocalsBaseIndex-2+3));
				    il.append(InstructionFactory.createReturn(method.getReturnType()));
		    	} else {
		    		ih_84 = il.append(InstructionFactory.createReturn(method.getReturnType()));
		    	}
			    
		    }
		    
		    // catch block
			// variable e has index 3
			// } catch (Throwable e) {
			InstructionHandle nih_86 = il.append(InstructionFactory
					.createStore(Type.OBJECT, transactionLocalsBaseIndex-2+3));
			// if (isNotNestedTransaction) {
			InstructionHandle nih_87 = il.append(InstructionFactory.createLoad(Type.INT, transactionLocalsBaseIndex-2+2));
			BranchInstruction ifeq_88 = InstructionFactory.createBranchInstruction(
					Constants.IFEQ, null);
			il.append(ifeq_88);

			InstructionHandle nih_91 = il
					.append(InstructionFactory.createLoad(Type.OBJECT, transactionLocalsBaseIndex-2+3));
		    il.append(_factory.createFieldAccess("com.jopdesign.sys.RetryException", 
		    		"instance", new ObjectType(
		    				"com.jopdesign.sys.RetryException"), 
		    		Constants.GETSTATIC));
			BranchInstruction if_acmpne_95 = InstructionFactory.createBranchInstruction(
					Constants.IF_ACMPNE, null);
			il.append(if_acmpne_95);
			// InstructionHandle nih_98 = il.append(_factory.createLoad(Type.INT,
			// 1));
			// il.append(_factory.createStore(Type.INT, 0));

			
		    {
		    	for (int i: modifiedArguments) {
		    		il.append(InstructionFactory.createLoad(
		    				Type.INT, modifiedArgumentsCopies.get(i)));
		    		il.append(InstructionFactory.createStore(Type.INT, i));
		    	}
		    }
			
			    BranchInstruction goto_110 = InstructionFactory.createBranchInstruction(Constants.GOTO, ih_43);
		    InstructionHandle ih_110 = il.append(goto_110);
			
			// exception was manually aborted or a bug triggered
			InstructionHandle nih_103 = il.append(_factory.createFieldAccess(
					"rttm.internal.Utils", "inTransaction", new ArrayType(
							Type.BOOLEAN, 1), Constants.GETSTATIC));
			il.append(new PUSH(_cp, -122));
			il.append(_factory.createInvoke("com.jopdesign.sys.Native", "rd",
					Type.INT, new Type[] { Type.INT }, Constants.INVOKESTATIC));
			il.append(new PUSH(_cp, 0));
			il.append(InstructionConstants.BASTORE);
			InstructionHandle nih_113 = il.append(new PUSH(_cp, 1));
			il.append(new PUSH(_cp, -128));
			il.append(_factory.createInvoke("com.jopdesign.sys.Native", "wrMem",
					Type.VOID, new Type[] { Type.INT, Type.INT },
					Constants.INVOKESTATIC));
			InstructionHandle nih_119 = il.append(InstructionFactory
					.createLoad(Type.OBJECT, transactionLocalsBaseIndex-2+3));
			// il.append(_factory.createCheckCast(new
			// ObjectType("java.lang.RuntimeException")));
			il.append(InstructionConstants.ATHROW);
			

			
			// set branch targets			
			
			ifne_12.setTarget(ih_19);
			goto_16.setTarget(ih_20);
			ifeq_22.setTarget(ih_43);
			ifeq_44.setTarget(ih_53);
			ifeq_59.setTarget(ih_84);
			
			ifeq_88.setTarget(nih_119);
			if_acmpne_95.setTarget(nih_103);
			
			{
			    for (BranchInstruction b: gotos_transactionCommit) {
			    	b.setTarget(ih_58);
			    }
			}
			
			// set exception handlers 
			
			// TODO restrict exception handler
			method.addExceptionHandler(ih_53, ih_84, nih_86, null);
			method.setMaxStack();
			method.setMaxLocals();
		}
		
		m = method.getMethod();
		oldIl.dispose();
		il.dispose();
		  
		return m;
	}
}
