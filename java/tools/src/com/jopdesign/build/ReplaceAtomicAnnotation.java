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

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MONITORENTER;
import org.apache.bcel.generic.MONITOREXIT;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.InstructionFinder;

import com.jopdesign.build.ReplaceNativeAndCPIdx.GETFIELD_LONG;
import com.jopdesign.build.ReplaceNativeAndCPIdx.GETFIELD_REF;
import com.jopdesign.build.ReplaceNativeAndCPIdx.GETSTATIC_LONG;
import com.jopdesign.build.ReplaceNativeAndCPIdx.GETSTATIC_REF;
import com.jopdesign.build.ReplaceNativeAndCPIdx.InvalidateInstruction;
import com.jopdesign.build.ReplaceNativeAndCPIdx.JOPSYS_INVOKESUPER;
import com.jopdesign.build.ReplaceNativeAndCPIdx.NativeInstruction;
import com.jopdesign.build.ReplaceNativeAndCPIdx.PUTFIELD_LONG;
import com.jopdesign.build.ReplaceNativeAndCPIdx.PUTFIELD_REF;
import com.jopdesign.build.ReplaceNativeAndCPIdx.PUTSTATIC_LONG;
import com.jopdesign.build.ReplaceNativeAndCPIdx.PUTSTATIC_REF;
import com.jopdesign.tools.JopInstr;

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
		
		final int transactionLocals = 7; // TMTODO 
		
		final int maxLocals = method.getMaxLocals();
		
		final int transactionLocalsBaseIndex = maxLocals; 
		final int copyBaseIndex = transactionLocalsBaseIndex + transactionLocals;
		
		
		InstructionList il = new InstructionList();
		
		InstructionFactory _factory = new InstructionFactory(_cp);
		
		method.setInstructionList(il);
		
		{
		    InstructionHandle ih_0 = il.append(new PUSH(_cp, -559038737));
		    il.append(_factory.createStore(Type.INT, transactionLocalsBaseIndex+1));
		    InstructionHandle ih_3 = il.append(new PUSH(_cp, -559038737));
		    il.append(_factory.createStore(Type.INT, transactionLocalsBaseIndex+2));
		    InstructionHandle ih_6 = il.append(_factory.createFieldAccess("rttm.internal.Utils", "inTransaction", new ArrayType(Type.BOOLEAN, 1), Constants.GETSTATIC));
		    il.append(new PUSH(_cp, -122));
		    il.append(_factory.createInvoke("com.jopdesign.sys.Native", "rd", Type.INT, new Type[] { Type.INT }, Constants.INVOKESTATIC));
		    il.append(InstructionConstants.BALOAD);
		        BranchInstruction ifne_15 = _factory.createBranchInstruction(Constants.IFNE, null);
		    il.append(ifne_15);
		    il.append(new PUSH(_cp, 1));
		        BranchInstruction goto_19 = _factory.createBranchInstruction(Constants.GOTO, null);
		    il.append(goto_19);
		    InstructionHandle ih_22 = il.append(new PUSH(_cp, 0));
		    InstructionHandle ih_23 = il.append(_factory.createStore(Type.INT, transactionLocalsBaseIndex+3));
		    InstructionHandle ih_24 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex+3));
		        BranchInstruction ifeq_25 = _factory.createBranchInstruction(Constants.IFEQ, null);
		    il.append(ifeq_25);
		    InstructionHandle ih_28 = il.append(_factory.createFieldAccess("rttm.internal.Utils", "inTransaction", new ArrayType(Type.BOOLEAN, 1), Constants.GETSTATIC));
		    il.append(new PUSH(_cp, -122));
		    il.append(_factory.createInvoke("com.jopdesign.sys.Native", "rd", Type.INT, new Type[] { Type.INT }, Constants.INVOKESTATIC));
		    il.append(new PUSH(_cp, 1));
		    il.append(InstructionConstants.BASTORE);
		    
		    {
		    	// TMTODO only for (possibly two-word) variables which are written to
		    	for (int i = 0; i < argsCount; i++) {
		    		il.append(_factory.createLoad(Type.INT, i));
		    		il.append(_factory.createStore(Type.INT, copyBaseIndex+i));
		    	}
		    }
		    
		    InstructionHandle ih_40 = il.append(new PUSH(_cp, 0));
		    il.append(_factory.createStore(Type.INT, transactionLocalsBaseIndex+4));
		    InstructionHandle ih_43 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex+3));
		        BranchInstruction ifeq_44 = _factory.createBranchInstruction(Constants.IFEQ, null);
		    il.append(ifeq_44);
		    InstructionHandle ih_47 = il.append(new PUSH(_cp, 1));
		    il.append(new PUSH(_cp, 524287));
		    il.append(_factory.createInvoke("com.jopdesign.sys.Native", "wrMem", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKESTATIC));
		        BranchInstruction goto_53 = _factory.createBranchInstruction(Constants.GOTO, null);
		    il.append(goto_53);
//		    InstructionHandle ih_56 = il.append(_factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
//		    il.append(new PUSH(_cp, "Entered inner transaction."));
//		    il.append(_factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));

		    InstructionHandle ih_64 = oldIl.getStart();
		    Collection<BranchInstruction> gotos_transactionCommit = new ArrayList<BranchInstruction>();
		    {
		    	// TMTODO redirect returns
		    	
		    	InstructionFinder f = new InstructionFinder(oldIl);
		    	
		    	String returnInstructionsPattern = "ARETURN|IRETURN|FRETURN|RETURN";
		    	
		    	for (Iterator i = f.search(returnInstructionsPattern); i.hasNext(); ) {
		    		InstructionHandle oldIh = ((InstructionHandle[])i.next())[0];
		    		
		    		InstructionList nl = new InstructionList();
		    				    		
		    		if (!method.getReturnType().equals(Type.VOID)) {		    		
			    		nl.append(_factory.createStore(
			    			method.getReturnType(), transactionLocalsBaseIndex+2));
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
//		    il.append(_factory.createStore(Type.INT, transactionLocalsBaseIndex+2));
		    
		    InstructionHandle ih_69 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex+3));
		        BranchInstruction ifeq_70 = _factory.createBranchInstruction(Constants.IFEQ, null);
		    il.append(ifeq_70);
		    InstructionHandle ih_73 = il.append(new PUSH(_cp, 0));
		    il.append(new PUSH(_cp, 524287));
		    InstructionHandle ih_76 = il.append(_factory.createInvoke("com.jopdesign.sys.Native", "wrMem", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKESTATIC));
		    InstructionHandle ih_79 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex+3));
		        BranchInstruction ifeq_80 = _factory.createBranchInstruction(Constants.IFEQ, null);
		    il.append(ifeq_80);
		    InstructionHandle ih_83 = il.append(_factory.createFieldAccess("rttm.internal.Utils", "inTransaction", new ArrayType(Type.BOOLEAN, 1), Constants.GETSTATIC));
		    il.append(new PUSH(_cp, -122));
		    il.append(_factory.createInvoke("com.jopdesign.sys.Native", "rd", Type.INT, new Type[] { Type.INT }, Constants.INVOKESTATIC));
		    il.append(new PUSH(_cp, 0));
		    il.append(InstructionConstants.BASTORE);
		        BranchInstruction goto_93 = _factory.createBranchInstruction(Constants.GOTO, null);
		    il.append(goto_93);
		    InstructionHandle ih_96 = il.append(_factory.createStore(Type.OBJECT, transactionLocalsBaseIndex+5));
		    InstructionHandle ih_98 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex+3));
		        BranchInstruction ifne_99 = _factory.createBranchInstruction(Constants.IFNE, null);
		    il.append(ifne_99);
		    InstructionHandle ih_102 = il.append(_factory.createFieldAccess("rttm.internal.Utils", "rollbackException", new ObjectType("com.jopdesign.sys.RollbackException"), Constants.GETSTATIC));
		    il.append(InstructionConstants.ATHROW);
		    InstructionHandle ih_106 = il.append(new PUSH(_cp, 0));
		    il.append(new PUSH(_cp, -116));
		    il.append(_factory.createInvoke("com.jopdesign.sys.Native", "wr", Type.VOID, new Type[] { Type.INT, Type.INT }, Constants.INVOKESTATIC));
		    InstructionHandle ih_112 = il.append(new PUSH(_cp, 1));
		    il.append(_factory.createStore(Type.INT, transactionLocalsBaseIndex+4));
//		    InstructionHandle ih_115 = il.append(_factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
//		    il.append(new PUSH(_cp, "Transaction aborted."));
//		    il.append(_factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
		    
		    InstructionHandle ih_finally = ih_112;
		    {
		    	// TMTODO only for (possibly two-word) variables which are written to
		    	for (int i = 0; i < argsCount; i++) {
		    		il.append(_factory.createLoad(Type.INT, copyBaseIndex+i));
		    		ih_finally = il.append(_factory.createStore(Type.INT, i));
		    	}
		    }
//		    InstructionHandle ih_123 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex+1));
//		    InstructionHandle ih_124 = il.append(_factory.createStore(Type.INT, 0));
		    
		    InstructionHandle ih_125 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex+3));
		        BranchInstruction ifeq_126 = _factory.createBranchInstruction(Constants.IFEQ, null);
		    il.append(ifeq_126);
		    InstructionHandle ih_129 = il.append(_factory.createFieldAccess("rttm.internal.Utils", "inTransaction", new ArrayType(Type.BOOLEAN, 1), Constants.GETSTATIC));
		    il.append(new PUSH(_cp, -122));
		    il.append(_factory.createInvoke("com.jopdesign.sys.Native", "rd", Type.INT, new Type[] { Type.INT }, Constants.INVOKESTATIC));
		    il.append(new PUSH(_cp, 0));
		    il.append(InstructionConstants.BASTORE);
		        BranchInstruction goto_139 = _factory.createBranchInstruction(Constants.GOTO, null);
		    il.append(goto_139);
		    InstructionHandle ih_142 = il.append(_factory.createStore(Type.OBJECT, transactionLocalsBaseIndex+6));
		    il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex+3));
		        BranchInstruction ifeq_145 = _factory.createBranchInstruction(Constants.IFEQ, null);
		    il.append(ifeq_145);
		    InstructionHandle ih_148 = il.append(_factory.createFieldAccess("rttm.internal.Utils", "inTransaction", new ArrayType(Type.BOOLEAN, 1), Constants.GETSTATIC));
		    il.append(new PUSH(_cp, -122));
		    il.append(_factory.createInvoke("com.jopdesign.sys.Native", "rd", Type.INT, new Type[] { Type.INT }, Constants.INVOKESTATIC));
		    il.append(new PUSH(_cp, 0));
		    il.append(InstructionConstants.BASTORE);
		    InstructionHandle ih_158 = il.append(_factory.createLoad(Type.OBJECT, transactionLocalsBaseIndex+6));
		    il.append(InstructionConstants.ATHROW);
		    InstructionHandle ih_161 = il.append(_factory.createLoad(Type.INT, transactionLocalsBaseIndex+4));
		        BranchInstruction ifne_163 = _factory.createBranchInstruction(Constants.IFNE, ih_40);
		    il.append(ifne_163);
		    
		    {
		    	if (!method.getReturnType().equals(Type.VOID)) {
				    InstructionHandle ih_ret1 = il.append(_factory.createLoad(method.getReturnType(), 
				    		transactionLocalsBaseIndex+2));
		    	}
			    InstructionHandle ih_ret2 = il.append(_factory.createReturn(method.getReturnType()));
		    }
		    
		    ifne_15.setTarget(ih_22);
		    goto_19.setTarget(ih_23);
		    ifeq_25.setTarget(ih_40);
		    ifeq_44.setTarget(ih_64); // TMTODO edited
		    goto_53.setTarget(ih_64);
		    ifeq_70.setTarget(ih_79);
		    ifeq_80.setTarget(ih_161);
		    goto_93.setTarget(ih_161);
		    ifne_99.setTarget(ih_106);
		    ifeq_126.setTarget(ih_161);
		    goto_139.setTarget(ih_161);
		    ifeq_145.setTarget(ih_158);
		    
		    for (BranchInstruction b: gotos_transactionCommit) {
		    	b.setTarget(ih_69);
		    }
		    
		    method.addExceptionHandler(ih_64, ih_76, ih_96, new ObjectType("java.lang.Throwable"));
		    method.addExceptionHandler(ih_64, ih_76, ih_142, null);
		    method.addExceptionHandler(ih_96, ih_finally, ih_142, null);
		    method.addExceptionHandler(ih_142, ih_142, ih_142, null);
		    method.setMaxStack();
		    method.setMaxLocals();
		}
		
		m = method.getMethod();
		oldIl.dispose();
		il.dispose();
		  
		return m;
	}
}
