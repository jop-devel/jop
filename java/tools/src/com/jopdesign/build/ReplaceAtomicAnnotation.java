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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;

import com.jopdesign.sys.Const;

import boxpeeking.instrument.bcel.AnnotationsAttribute;

/**
 * Known limitations: can't load string constants because they are already 
 * indexed in jz.load().
 * 
 * @author Peter Hilber (peter@hilber.name)
 *
 */
public class ReplaceAtomicAnnotation extends JOPizerVisitor {

	public ReplaceAtomicAnnotation(AppInfo jz) {
		super(jz);
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);
		
		Method[] methods = clazz.getMethods();
		
		for(int i = 0; i < methods.length; i++) {
			for (Attribute a: methods[i].getAttributes()) {
				if (a instanceof AnnotationsAttribute) {
					if (((AnnotationsAttribute)a).hasAtomicAnnotation()) {
						ConstantPoolGen cpoolgen = new ConstantPoolGen(clazz.getConstantPool()); 
						
						Method nm = transform(methods[i], clazz, cpoolgen);						
				        MethodInfo mi = getCli().getMethodInfo(nm.getName()+nm.getSignature());
				        // set new method also in MethodInfo
				        mi.setMethod(nm);
						
						methods[i] = nm;
						
						clazz.setConstantPool(cpoolgen.getFinalConstantPool());
						
						// TMTODO remove annotation
					}
				}
			}
		}
	}
	
	public static Method transform(Method m, JavaClass clazz, 
			  ConstantPoolGen _cp) {
		MethodGen method = new MethodGen(m, clazz.getClassName(), _cp);
		InstructionList oldIl = method.getInstructionList();
		
		Type returnType = m.getReturnType();
		
		if (returnType.equals(Type.LONG) || returnType.equals(Type.DOUBLE)) {
			throw new UnsupportedOperationException();
		}
		
		int argsCount;
		{
            int max = method.isStatic() ? 0 : 1;
            Type[] arg_types = method.getArgumentTypes();
            if (arg_types != null) {
                for (int i = 0; i < arg_types.length; i++) {
                    max += arg_types[i].getSize();
                }
            }
            
            argsCount = max;
		}
		
		final int transactionLocals = 4; // TMTODO 
		
		final int maxLocals = method.getMaxLocals();
		
		final int transactionLocalsBaseIndex = maxLocals; 
		final int copyBaseIndex = transactionLocalsBaseIndex + transactionLocals;
		
		
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
			    BranchInstruction ifne_12 = _factory.createBranchInstruction(Constants.IFNE, null);
			il.append(ifne_12);
			il.append(new PUSH(_cp, 1));
			    BranchInstruction goto_16 = _factory.createBranchInstruction(Constants.GOTO, null);
			il.append(goto_16);
			InstructionHandle ih_19 = il.append(new PUSH(_cp, 0));
			InstructionHandle ih_20 = il.append(_factory.createStore(Type.INT, transactionLocalsBaseIndex-2+2));
			InstructionHandle ih_21 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex-2+2));
			    BranchInstruction ifeq_22 = _factory.createBranchInstruction(Constants.IFEQ, null);
			il.append(ifeq_22);
//			InstructionHandle ih_25 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex-2+0));
//			il.append(_factory.createStore(Type.INT, transactionLocalsBaseIndex-2+1));
			
		    {
		    	// TMTODO only for (possibly two-word) variables which are written to
		    	for (int i = 0; i < argsCount; i++) {
		    		il.append(_factory.createLoad(Type.INT, i));
		    		il.append(_factory.createStore(Type.INT, copyBaseIndex+i));
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
			// TMTODO optimize jumps
			// TMTODO get tm constants using reflection
			
			InstructionHandle ih_43 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex-2+2));
			    BranchInstruction ifeq_44 = _factory.createBranchInstruction(Constants.IFEQ, null);
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
			    		nl.append(_factory.createStore(
			    			method.getReturnType(), transactionLocalsBaseIndex-2+3));
		    		}
			    	
		    		BranchInstruction goto_transactionCommit = 
		    			_factory.createBranchInstruction(Constants.GOTO, null);
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
			
			InstructionHandle ih_58 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex-2+2));
			    BranchInstruction ifeq_59 = _factory.createBranchInstruction(Constants.IFEQ, null);
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
				    ih_84 = il.append(_factory.createLoad(method.getReturnType(), 
				    		transactionLocalsBaseIndex-2+3));
				    il.append(_factory.createReturn(method.getReturnType()));
		    	} else {
		    		ih_84 = il.append(_factory.createReturn(method.getReturnType()));
		    	}
			    
		    }
		    
		    // catch block
			
			InstructionHandle ih_86 = il.append(_factory.createStore(Type.OBJECT, transactionLocalsBaseIndex-2+3));
			InstructionHandle ih_87 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex-2+2));
			    BranchInstruction ifeq_88 = _factory.createBranchInstruction(Constants.IFEQ, null);
			il.append(ifeq_88);
			InstructionHandle ih_91 = il.append(new PUSH(_cp, 0));
			il.append(new PUSH(_cp, -116));
			il.append(_factory.createInvoke("com.jopdesign.sys.Native", "wr", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKESTATIC));
			InstructionHandle ih_97 = il.append(_factory.createLoad(Type.OBJECT, transactionLocalsBaseIndex-2+3));
			il.append(_factory.createFieldAccess("rttm.internal.Utils", "abortException", new ObjectType("rttm.AbortException"), Constants.GETSTATIC));
			    BranchInstruction if_acmpne_101 = _factory.createBranchInstruction(Constants.IF_ACMPNE, null);
			il.append(if_acmpne_101);
			InstructionHandle ih_104 = il.append(_factory.createFieldAccess("rttm.internal.Utils", "abortException", new ObjectType("rttm.AbortException"), Constants.GETSTATIC));
			il.append(InstructionConstants.ATHROW);
//			InstructionHandle ih_108 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex-2+1));
//			il.append(_factory.createStore(Type.INT, transactionLocalsBaseIndex-2+0));
			
			InstructionHandle ih_108 = null;
		    {
		    	// TMTODO only for (possibly two-word) variables which are written to
		    	for (int i = 0; i < argsCount; i++) {
		    		InstructionHandle ih = il.append(_factory.createLoad(Type.INT, copyBaseIndex+i));
		    		if (i == 0) {
		    			ih_108 = ih;
		    		}
		    		il.append(_factory.createStore(Type.INT, i));
		    	}
		    }
			
			    BranchInstruction goto_110 = _factory.createBranchInstruction(Constants.GOTO, null);
		    InstructionHandle ih_110 = il.append(goto_110);
			
			{
				if (ih_108 == null) {
					ih_108 = ih_110; // TMTODO
				}
			}
			
			InstructionHandle ih_113 = il.append(_factory.createLoad(Type.OBJECT, transactionLocalsBaseIndex-2+3));
			il.append(_factory.createFieldAccess("rttm.internal.Utils", "abortException", new ObjectType("rttm.AbortException"), Constants.GETSTATIC));
			    BranchInstruction if_acmpne_117 = _factory.createBranchInstruction(Constants.IF_ACMPNE, null);
			il.append(if_acmpne_117);
			InstructionHandle ih_120 = il.append(_factory.createFieldAccess("rttm.internal.Utils", "abortException", new ObjectType("rttm.AbortException"), Constants.GETSTATIC));
			il.append(InstructionConstants.ATHROW);
			InstructionHandle ih_124 = il.append(_factory.createFieldAccess("rttm.internal.Utils", "retryException", new ObjectType("com.jopdesign.sys.RetryException"), Constants.GETSTATIC));
			il.append(InstructionConstants.ATHROW);
			InstructionHandle ih_128;
			BranchInstruction goto_128 = _factory.createBranchInstruction(Constants.GOTO, ih_43);
			ih_128 = il.append(goto_128);
			
			// set branch targets			
			
			ifne_12.setTarget(ih_19);
			goto_16.setTarget(ih_20);
			ifeq_22.setTarget(ih_43);
			ifeq_44.setTarget(ih_53);
			ifeq_59.setTarget(ih_84);
			ifeq_88.setTarget(ih_113);
			if_acmpne_101.setTarget(ih_108);
			goto_110.setTarget(ih_128);
			if_acmpne_117.setTarget(ih_124);
			
			{
			    for (BranchInstruction b: gotos_transactionCommit) {
			    	b.setTarget(ih_58);
			    }
			}
			
			// set exception handlers 
			
			// TMTODO restrict exception handler?
			method.addExceptionHandler(ih_53, ih_84, ih_86, new ObjectType("java.lang.Throwable"));
			method.setMaxStack();
			method.setMaxLocals();
		}
		
		m = method.getMethod();
		oldIl.dispose();
		il.dispose();
		  
		return m;
	}
}
