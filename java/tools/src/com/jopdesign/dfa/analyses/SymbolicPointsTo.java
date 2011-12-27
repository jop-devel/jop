/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber <benedikt.huber@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.dfa.analyses;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.common.misc.MiscUtils.Query;
import com.jopdesign.dfa.DFATool;
import com.jopdesign.dfa.framework.Analysis;
import com.jopdesign.dfa.framework.AnalysisResultSerialization;
import com.jopdesign.dfa.framework.BoundedSetFactory;
import com.jopdesign.dfa.framework.BoundedSetFactory.BoundedSet;
import com.jopdesign.dfa.framework.Context;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.FlowEdge;
import com.jopdesign.dfa.framework.Interpreter;
import com.jopdesign.dfa.framework.MethodHelper;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.generic.AASTORE;
import org.apache.bcel.generic.ARRAYLENGTH;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class SymbolicPointsTo implements Analysis<CallString, SymbolicAddressMap> {

	private static final boolean DEBUG_PRINT = false;
	
	// Set this to true to see how good one could get
	//private static final boolean ASSUME_NO_ALIASING = true;
	// Set this to true to see how good one could get
	private static final boolean ASSUME_NO_CONC = true;

    public static final String NAME = "SymbolicPointsTo";
    private static final Logger logger = Logger.getLogger(DFATool.LOG_DFA_ANALYSES+"."+NAME);

	private BoundedSetFactory<SymbolicAddress> bsFactory;
	private final int callStringLength;
	private MethodInfo entryMethod;
	
	private HashMap<InstructionHandle, ContextMap<CallString, BoundedSet<SymbolicAddress>>> usedRefs =
		new HashMap<InstructionHandle, ContextMap<CallString,BoundedSet<SymbolicAddress>>>();
	private Query<InstructionHandle> executedOnce;

	private CallString initialCallString;

	// optional extra info: Max flow for each instruction handle
	
	public SymbolicPointsTo(int maxSetSize, int callStringLength) {		
		bsFactory = new BoundedSetFactory<SymbolicAddress>(maxSetSize);
		this.callStringLength = callStringLength;
		executedOnce = new Query<InstructionHandle>() {
			public boolean query(InstructionHandle a) { return false; }			
		};
	}

	public String getId() {
		return NAME + "-" + callStringLength;
	}

	public SymbolicPointsTo(int maxSetSize, int callStringLength, Query<InstructionHandle> eoAna) {
		bsFactory = new BoundedSetFactory<SymbolicAddress>(maxSetSize);
		this.callStringLength = callStringLength;
		executedOnce = eoAna;
	}

	public ContextMap<CallString, SymbolicAddressMap> bottom() {
		return null;
	}
	
	/* A `compare` B <=> contexts are equals and A subseteq B */
	public boolean compare(ContextMap<CallString, SymbolicAddressMap> s1, 
			               ContextMap<CallString, SymbolicAddressMap> s2) {
		/* If either is undefined, order is not defined */
		if (s1 == null || s2 == null)  return false;
		/* Get context */
		Context context = s1.getContext();
		/* If context do not match, partial order is not defined */
		if (!context.equals(s2.getContext())) return false;
		
		SymbolicAddressMap a = s1.get(context.callString);
		SymbolicAddressMap b = s2.get(context.callString);
		/* If either is undefined, partial order is not defined */
		if (a == null || b == null) return false;

		/* The actual comparison */
		return a.isSubset(b);
	}

	public ContextMap<CallString, SymbolicAddressMap> initial(InstructionHandle stmt) {
		ContextMap<CallString, SymbolicAddressMap> retval = 
			new ContextMap<CallString, SymbolicAddressMap>(
					new Context(),
					new HashMap<CallString, SymbolicAddressMap>());
		
		SymbolicAddressMap init = new SymbolicAddressMap(bsFactory);
		// Add symbolic stack names
		int stackPtr = 0;
		if(! entryMethod.isStatic()) {
			init.putStack(stackPtr++, bsFactory.singleton(SymbolicAddress.rootAddress("$this")));
		}
		String[] args = entryMethod.getArgumentNames();
		for(int i = 0; i < args.length; i++)
		{	
			Type ty = entryMethod.getArgumentType(i);
			if(ty instanceof ReferenceType) {
				init.putStack(stackPtr, bsFactory.singleton(SymbolicAddress.rootAddress("$"+args[i])));
			}
			stackPtr += ty.getSize();
		}
		retval.put(initialCallString, init);
		return retval;
	}
	
	public void initialize(MethodInfo mi, Context context) {
		entryMethod = mi;
		initialCallString = context.callString;
	}
	
	public ContextMap<CallString, SymbolicAddressMap> join(
			ContextMap<CallString, SymbolicAddressMap> s1,
			ContextMap<CallString, SymbolicAddressMap> s2) {
		if (s1 == null) {
			return new ContextMap<CallString, SymbolicAddressMap>(s2);
		}
		
		if (s2 == null) {
			return new ContextMap<CallString, SymbolicAddressMap>(s1);
		}
		// create empty context map, with s1's context
		ContextMap<CallString, SymbolicAddressMap> result = 
			new ContextMap<CallString, SymbolicAddressMap>(new Context(s1.getContext()), new HashMap<CallString, SymbolicAddressMap>());
		// add both context maps. Note that entries from s2 overwrite those from the s1
		result.putAll(s1);
		result.putAll(s2);
		// a and b are the DF results for the respectively active contexts
		SymbolicAddressMap a = s1.get(s1.getContext().callString);
		SymbolicAddressMap b = s2.get(s2.getContext().callString);
		
		SymbolicAddressMap merged = a.clone();
		merged.join(b);
		
		result.put(s2.getContext().callString, merged);		
		
		if (result.getContext().stackPtr < 0) {
			result.getContext().stackPtr = s2.getContext().stackPtr;
		}
		if (result.getContext().syncLevel < 0) {
			result.getContext().syncLevel = s2.getContext().syncLevel;
		}
		result.getContext().threaded = Context.isThreaded();
		
//		System.out.println("R: "+result);
		
		return result;
	}

	public ContextMap<CallString, SymbolicAddressMap> transfer(
			InstructionHandle stmt,
			FlowEdge edge,
			ContextMap<CallString, SymbolicAddressMap> input,
			Interpreter<CallString, SymbolicAddressMap> interpreter,
			Map<InstructionHandle, ContextMap<CallString, SymbolicAddressMap>> state) {
				
		Context context = new Context(input.getContext());
		
		if(DEBUG_PRINT) {
			System.out.println("[S] "+context.callString.toStringList()+": "+context.method()+" / "+stmt);
		}
		
		SymbolicAddressMap in = input.get(context.callString);
		ContextMap<CallString, SymbolicAddressMap> retval = new ContextMap<CallString, SymbolicAddressMap>(context, input);

		Instruction instruction = stmt.getInstruction();
		int newStackPtr =
			  context.stackPtr 
			+ instruction.produceStack(context.constPool())
			- instruction.consumeStack(context.constPool());
		int opcode = instruction.getOpcode();
		switch (opcode) {

		/* Constants (boring) */
		case Constants.DCONST_0:
		case Constants.DCONST_1:
		case Constants.LCONST_0:
		case Constants.LCONST_1 :
		/* Instructions above need two stack slots */
		case Constants.FCONST_0:
		case Constants.FCONST_1:
		case Constants.FCONST_2:			
		case Constants.ICONST_M1:
		case Constants.ICONST_0:
		case Constants.ICONST_1:
		case Constants.ICONST_2:
		case Constants.ICONST_3:
		case Constants.ICONST_4:
		case Constants.ICONST_5:
			
		case Constants.BIPUSH:
		case Constants.SIPUSH: {
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
		}
		break;

		case Constants.ACONST_NULL: {
			// Null -> No reference
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			result.putStack(newStackPtr-1, bsFactory.empty());
			retval.put(context.callString, result);		
		}
		break;
			
		/* Long/Double Constants */
		case Constants.LDC2_W: {
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
		}
		break;
		/* Int/Float/String Constants */
		case Constants.LDC:
		case Constants.LDC_W: {
			LDC ldc = (LDC) instruction;
			Type t = ldc.getType(context.constPool());
			if(t instanceof ReferenceType) {
				SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
				/* FIXME: This is overly conservative, but class pointer not available here */
				String classContext = context.getMethodInfo().getMemberID().toString();
				SymbolicAddress addr = SymbolicAddress.stringLiteral(classContext,ldc.getIndex());
				result.putStack(newStackPtr-1, bsFactory.singleton(addr ));
				retval.put(context.callString, result);		
			} else {
				retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
			}
		}
		break;
		
		case Constants.DSTORE:
		case Constants.DSTORE_0:
		case Constants.DSTORE_1:
		case Constants.DSTORE_2:
		case Constants.DSTORE_3:
		case Constants.LSTORE:
		case Constants.LSTORE_0:
		case Constants.LSTORE_1:
		case Constants.LSTORE_2:
		case Constants.LSTORE_3:
		case Constants.FSTORE:
		case Constants.FSTORE_0:
		case Constants.FSTORE_1:
		case Constants.FSTORE_2:
		case Constants.FSTORE_3:
		case Constants.ISTORE_0:
		case Constants.ISTORE_1:
		case Constants.ISTORE_2:
		case Constants.ISTORE_3:
		case Constants.ISTORE: {	
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
		}
		break;
		// 
		case Constants.ASTORE_0:
		case Constants.ASTORE_1:
		case Constants.ASTORE_2:
		case Constants.ASTORE_3:
		case Constants.ASTORE: {
			// copy value to local variable
			StoreInstruction instr = (StoreInstruction)instruction; 
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			if(DEBUG_PRINT) {
				System.out.println(String.format("[DD] Copy: stack[%d] <- stack[%d]",
						instr.getIndex(), context.stackPtr-1));
			}
			result.copyStack(in, instr.getIndex(), context.stackPtr-1);
			retval.put(context.callString, result);
		}
		break;	

		/* Load variables (boring) */
		case Constants.DLOAD_0:
		case Constants.DLOAD_1:
		case Constants.DLOAD_2:
		case Constants.DLOAD_3:
		case Constants.DLOAD: 
		case Constants.LLOAD_0:
		case Constants.LLOAD_1:
		case Constants.LLOAD_2:
		case Constants.LLOAD_3:
		case Constants.LLOAD:
			/* Instructions above need two stack slots */
		case Constants.FLOAD_0:
		case Constants.FLOAD_1:
		case Constants.FLOAD_2:
		case Constants.FLOAD_3:
		case Constants.FLOAD: 
		case Constants.ILOAD_0:
		case Constants.ILOAD_1:
		case Constants.ILOAD_2:
		case Constants.ILOAD_3:
		case Constants.ILOAD: {	
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
		}
		break;

		/* Floating Point Comparison (boring) */
		case Constants.DCMPG:
		case Constants.DCMPL:
		case Constants.LCMP: 
			/* Instructions above need two stack slots */
		case Constants.FCMPG:
		case Constants.FCMPL:{
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
		}
		break;


		case Constants.ALOAD_0:
		case Constants.ALOAD_1:
		case Constants.ALOAD_2:
		case Constants.ALOAD_3:
		case Constants.ALOAD: {
			LoadInstruction instr = (LoadInstruction)instruction; 
			// copy value from local variable
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			result.copyStack(in, context.stackPtr, instr.getIndex());
			retval.put(context.callString, result);		
		}
		break;	
		// Access Object Handle (area), top of stack
		case Constants.ARRAYLENGTH: {
			putResult(stmt, context, input.get(context.callString).getStack(context.stackPtr-1));
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
		}
		break;
		// Access Object Handle, second on stack
		case Constants.PUTFIELD: {
			PUTFIELD instr = (PUTFIELD)instruction;
			putResult(stmt, context, input.get(context.callString).getStack(context.stackPtr-1-instr.getType(context.constPool()).getSize()));
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			// Change alias information
			if(instr.getFieldType(context.constPool()) instanceof ReferenceType) {
				String ty = instr.getFieldType(context.constPool()).getSignature();
				result.addAlias(ty, in.getStack(context.stackPtr-1));
			}
			retval.put(context.callString, result);		
		}
		break;
		
		
		// Access Object Handle, top of stack
		case Constants.GETFIELD: {			
			putResult(stmt, context, input.get(context.callString).getStack(context.stackPtr-1));
			GETFIELD instr = (GETFIELD)instruction;
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			if(instr.getFieldType(context.constPool()) instanceof ReferenceType) {
				BoundedSet<SymbolicAddress> newMapping =
					SymbolicAddress.fieldAccess(bsFactory,
												in.getStack(context.stackPtr-1),
							                    instr.getFieldName(context.constPool()));
				newMapping.addAll(in.getAliases(instr.getFieldType(context.constPool()).getSignature()));
				result.putStack(context.stackPtr-1, newMapping);
			}
			retval.put(context.callString, result);
		}
		break;
		// Put object is on top of stack
		// Handles the same way as MOV
		case Constants.PUTSTATIC: {			
			PUTSTATIC instr = (PUTSTATIC)instruction;
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			if(instr.getFieldType(context.constPool()) instanceof ReferenceType) {
				BoundedSet<SymbolicAddress> pointers = in.getStack(context.stackPtr - 1);
				result.putHeap(fieldSignature(context, instr), pointers);
			}
			retval.put(context.callString, result);		
		}
		break;
		// Assign TOS the field value
		// Handled the same as MOV
		case Constants.GETSTATIC: {			
			GETSTATIC instr = (GETSTATIC)instruction;
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			if(instr.getFieldType(context.constPool()) instanceof ReferenceType) {
				result.putStack(context.stackPtr, in.getHeap(fieldSignature(context, instr)));
			}
			retval.put(context.callString, result);		
		}
		break;

		case Constants.LASTORE:
		case Constants.DASTORE:
		case Constants.IASTORE:
		case Constants.FASTORE:
		case Constants.CASTORE:
		case Constants.SASTORE:
		case Constants.BASTORE: {
			int offset = 3;
			if(opcode == Constants.LASTORE || opcode == Constants.DASTORE) offset=4;
			putResult(stmt, context, input.get(context.callString).getStack(context.stackPtr-offset));
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
		}
		break;
		// changing the heap -> TOP
		case Constants.AASTORE: {
			putResult(stmt, context, input.get(context.callString).getStack(context.stackPtr-3));
			
			AASTORE instr = (AASTORE) stmt.getInstruction();
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			// Change alias information
			if(instr.getType(context.constPool()) instanceof ReferenceType) {
				String ty = instr.getType(context.constPool()).getSignature();
				result.addAlias(ty, in.getStack(context.stackPtr-1));
			}
			retval.put(context.callString, result);		
		}
		break;
			
		case Constants.DALOAD:
		case Constants.LALOAD: 
		case Constants.IALOAD:
		case Constants.FALOAD:
		case Constants.CALOAD:
		case Constants.SALOAD:
		case Constants.BALOAD: {
			putResult(stmt, context, input.get(context.callString).getStack(context.stackPtr-2));
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
		}
		break;
		// AALOAD objectref, index -> objectref
		// TODO: Use index info
		case Constants.AALOAD: {
			putResult(stmt, context, input.get(context.callString).getStack(context.stackPtr-2));
			//AALOAD instr = (AALOAD)instruction;
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			BoundedSet<SymbolicAddress> objectMapping = in.getStack(context.stackPtr-2);
			BoundedSet<SymbolicAddress> newMapping;
			LoopBounds bounds = interpreter.getDFATool().getLoopBounds();
			if(executedOnce.query(stmt)) {
				newMapping = bsFactory.singleton(SymbolicAddress.newName());
			} else if(objectMapping.isSaturated() || bounds == null) {
				newMapping = bsFactory.top();
			} else {
				
				Interval interval = bounds.getArrayIndices(stmt, context.callString);
				if(interval.hasLb() && interval.hasUb()) {
					newMapping = bsFactory.empty();
					for(SymbolicAddress addr: objectMapping.getSet()) {
						for(int i = interval.getLb(); i <= interval.getUb(); i++) {
							newMapping.add(addr.accessArray(i));
						}
					}					
				} else {
					newMapping = bsFactory.top();					
				}
			}
				
//				Doesn't work, but is probably stupid anyway :(
//  			LoopBounds bounds = interpreter.getDFATool().getLoopBounds();
//              bounds.getArraySizes().get(stmt).get(context.callString);
//			    newMapping = bsFactory.empty();
//				Interval[] sizeBounds = { new Interval(2,3) }; 
//				for(Interval i : sizeBounds) {
//					if(! i.hasUb()) {
//						newMapping = bsFactory.top();
//					}
//					int ub = i.getUb();
//					for(int j = 0; j <= ub; j++) {
//						for(SymbolicAddress addr: objectMapping.getSet()) {
//							newMapping.add(addr.accessArray(j));
//						}					
//					}
//				}
//			}
			result.putStack(context.stackPtr-2, newMapping);
			retval.put(context.callString, result);
		}
		break;
					
		case Constants.DUP: {
			// copy value on stack
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			result.copyStack(in, context.stackPtr, context.stackPtr-1);
			retval.put(context.callString, result);		
		}
		break;
		case Constants.DUP_X1: {
			// copy value on stack
			SymbolicAddressMap result = in.cloneFilterStack(context.stackPtr-2);
			result.copyStack(in, context.stackPtr-2, context.stackPtr-1);
			result.copyStack(in, context.stackPtr-1, context.stackPtr-2);
			result.copyStack(in, context.stackPtr, context.stackPtr-1);
			retval.put(context.callString, result);		
		}
		break;
		case Constants.DUP_X2: {
			// copy value on stack
			SymbolicAddressMap result = in.cloneFilterStack(context.stackPtr-3);
			result.copyStack(in, context.stackPtr-3, context.stackPtr-1);
			result.copyStack(in, context.stackPtr-2, context.stackPtr-3);
			result.copyStack(in, context.stackPtr-1, context.stackPtr-2);
			result.copyStack(in, context.stackPtr, context.stackPtr-1);
			retval.put(context.callString, result);		
		}
		break;
		
		case Constants.DUP2: {
			// v1,v2 -> v1,v2,v1,v2
			SymbolicAddressMap result = in.cloneFilterStack(context.stackPtr);
			result.copyStack(in, context.stackPtr, context.stackPtr-2);
			result.copyStack(in, context.stackPtr+1, context.stackPtr-1);
			retval.put(context.callString, result);		
		}
		break;
		
		case Constants.POP: 
		case Constants.POP2: {	
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
		}
		break;

		case Constants.IINC: {	
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
		}
		break;
		
		case Constants.IADD: 
		case Constants.ISUB:
		case Constants.INEG:
		case Constants.IUSHR:
		case Constants.ISHR: 
		case Constants.IAND:
		case Constants.IOR:
		case Constants.IXOR:
		case Constants.IMUL: 
		case Constants.IDIV: 
		case Constants.IREM:
		case Constants.ISHL: {
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));			
		}
		break;

		/* Long,Float and Double operations (boring) */
		case Constants.DADD: 
		case Constants.DSUB:
		case Constants.DMUL: 
		case Constants.DDIV: 
		case Constants.DREM:
		case Constants.LADD: 
		case Constants.LSUB:
		case Constants.LUSHR:
		case Constants.LSHR: 
		case Constants.LAND:
		case Constants.LOR:
		case Constants.LXOR:
		case Constants.LMUL: 
		case Constants.LDIV: 
		case Constants.LREM:
		case Constants.LSHL: 
			/* Instructions above need two stack slots */
		case Constants.DNEG:
		case Constants.LNEG:
		case Constants.FADD: 
		case Constants.FSUB:
		case Constants.FNEG:
		case Constants.FMUL: 
		case Constants.FDIV: 
		case Constants.FREM: {
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));			
		}
		break;
			
		/* Conversion of primitives (boring) */
		case Constants.D2F:
		case Constants.D2I:
			/* Instructions above need two stack slots */
		case Constants.D2L: {
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
		}
		break;
		 
		case Constants.L2F:
		case Constants.L2I: 
			/* Instructions above need two stack slots */
		case Constants.L2D: {
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
		}
		break;
			
		case Constants.F2D:
		case Constants.I2D:
		case Constants.F2L:
		case Constants.I2L:
			/* Instructions above need one stack slot less */
		case Constants.F2I:
		case Constants.I2B:
		case Constants.I2C:
		case Constants.I2F:
		case Constants.I2S:{
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
		}
		break;

		case Constants.MONITORENTER:
			// not supported yet
			if(ASSUME_NO_CONC) retval.put(context.callString, in.cloneFilterStack(newStackPtr));
			else               retval.put(context.callString, SymbolicAddressMap.top());						
			break;

		case Constants.MONITOREXIT:
			// not supported yet
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
			break;

		case Constants.CHECKCAST:
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
			break;
			
		case Constants.INSTANCEOF: {
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
		}
		break;

		case Constants.NEW: 
		case Constants.NEWARRAY:
		case Constants.ANEWARRAY:
		{
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			BoundedSet<SymbolicAddress> newMapping;
			if(executedOnce.query(stmt)) {
				newMapping = bsFactory.singleton(SymbolicAddress.newName());
			} else {
				newMapping = bsFactory.top();					
			}
			int objPtr = (instruction.getOpcode() == Constants.NEW) ? 
					context.stackPtr: (context.stackPtr-1);
			result.putStack(objPtr, newMapping);
			retval.put(context.callString, result);
		}
		break;
				
		case Constants.MULTIANEWARRAY: {
			// not supported yet
			retval.put(context.callString, SymbolicAddressMap.top());						
		}
		break;

		case Constants.GOTO:
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
			break;
		
		case Constants.IFNULL:
		case Constants.IFNONNULL: {	
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
		}
		break;
		
		case Constants.IF_ACMPEQ:
		case Constants.IF_ACMPNE: {	
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
		}
		break;			
			
		case Constants.IFEQ:
		case Constants.IFNE:
		case Constants.IFLT:
		case Constants.IFGE:
		case Constants.IFLE:
		case Constants.IFGT:
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
			break;

		case Constants.IF_ICMPEQ:
		case Constants.IF_ICMPNE:
		case Constants.IF_ICMPLT:
		case Constants.IF_ICMPGE:
		case Constants.IF_ICMPGT:
		case Constants.IF_ICMPLE:
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
			break;

		case Constants.LOOKUPSWITCH:
		case Constants.TABLESWITCH:
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
			break;
			
		case Constants.INVOKEVIRTUAL:
		case Constants.INVOKEINTERFACE:
		case Constants.INVOKESTATIC:
		case Constants.INVOKESPECIAL: {
			
			DFATool p = interpreter.getDFATool();
			Set<String> receivers = p.getReceivers(stmt, context.callString);
			retval.put(context.callString, new SymbolicAddressMap(bsFactory));
			
			if (receivers == null || receivers.size() == 0) {
				String errMsg = String.format("%s : invoke %s: %s receivers",
				  context.method(), instruction.toString(context.constPool().getConstantPool()),
				  (receivers == null ? "Unknown" : "No"));
				// Get static receivers (FIXME: this just workarounds DFA bugs)
				if(opcode == Constants.INVOKESTATIC) {
					receivers = new HashSet<String>();
					InvokeInstruction invInstruction = (InvokeInstruction) instruction;					
					String klass = invInstruction.getClassName(context.constPool());
					String name = invInstruction.getMethodName(context.constPool());
					String sig = invInstruction.getSignature(context.constPool());
					String recv = klass+"."+name+sig;
					Logger.getLogger(this.getClass()).info("Using static receiver: "+recv);
					receivers.add(recv);
				} else {
					Logger.getLogger(this.getClass()).error(errMsg);
					throw new AssertionError(errMsg);
				}
			}

			if( instruction.getOpcode() == Constants.INVOKEVIRTUAL
			 || instruction.getOpcode() == Constants.INVOKEINTERFACE) {
				MethodInfo mi = p.getMethod(receivers.iterator().next());
				int refPos = MethodHelper.getArgSize(mi);
//				System.out.println(String.format("%s: args+1: %d; stack[%d] %s",
//						mi.methodId,refPos,context.stackPtr,input.get(context.callString)));
				try {
					putResult(stmt, context, input.get(context.callString).getStack(context.stackPtr-refPos));
				} catch(Error e) {
					System.err.println("We have problems with method "+mi);
					System.err.println(e.getMessage());
				}
			}

			for (String methodName : receivers) {
				doInvoke(methodName, stmt, context, input, interpreter, state, retval);
			}
		}
		break;
		
		case Constants.ARETURN: {
			SymbolicAddressMap result = in.cloneFilterStack(0);
			// store results
			result.copyStack(in, 0, context.stackPtr-1);
			retval.put(context.callString, result);
		}
		break;
		
		/* The values of other return statements are not of interest here */
		case Constants.DRETURN:
		case Constants.LRETURN:
		case Constants.FRETURN:
		case Constants.IRETURN:
		case Constants.RETURN: {
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
		}
		break;						

		default:
			System.err.println("unknown instruction: "+stmt+" in method "+context.method());
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
			break;
		}
		
		
		// DEBUGGING
		if(DEBUG_PRINT) {
			System.out.println("[F] "+context.method()+" / "+stmt);
			System.out.println("  Stackptr: "+context.stackPtr + " -> "+newStackPtr);
			System.out.println("  Before: ");
			input.get(context.callString).print(System.out, 4);		
			System.out.println("  After: ");
			retval.get(context.callString).print(System.out, 4);			
		}
		
		context.stackPtr = newStackPtr;
		return retval;
	}
	
	private String fieldSignature(Context context, FieldInstruction instr) {
		return instr.getClassName(context.constPool()) + instr.getFieldName(context.constPool());
	}

	/** Save used object handle */
	private void putResult(InstructionHandle stmt,
						   Context ctx,
		     	           BoundedSet<SymbolicAddress> stackEntry) {
		if(! usedRefs.containsKey(stmt)) {
			usedRefs.put(stmt, new ContextMap<CallString, BoundedSet<SymbolicAddress>>(ctx,
					new HashMap<CallString, BoundedSet<SymbolicAddress>>()));
		}
		usedRefs.get(stmt).put(ctx.callString,stackEntry);
	}

	private void doInvoke(String methodName,
			InstructionHandle stmt,
			Context context,
			ContextMap<CallString,SymbolicAddressMap> input,
			Interpreter<CallString,SymbolicAddressMap> interpreter,
			Map<InstructionHandle, ContextMap<CallString, SymbolicAddressMap>> state,
			ContextMap<CallString, SymbolicAddressMap> retval) {

		DFATool p = interpreter.getDFATool();
		MethodInfo method = p.getMethod(methodName);
		//methodName = method.getMemberID().toString();

//		System.out.println(stmt+" invokes method: "+methodName);				

		if (method.isNative()) {

			handleNative(method, context, input, retval);

		} else {

			// set up new context
			int varPtr = context.stackPtr - MethodHelper.getArgSize(method);
			Context c = new Context(context);
			c.stackPtr = method.getCode().getMaxLocals();
			if (method.isSynchronized()) {
				c.syncLevel = context.syncLevel+1;
			}
			c.setMethodInfo(method);
			c.callString = c.callString.push(method, stmt, callStringLength);
			
			// carry only minimal information with call
			SymbolicAddressMap in = input.get(context.callString);
			SymbolicAddressMap out = in.cloneInvoke(varPtr);
			
			HashMap<CallString, SymbolicAddressMap> initialMap =
				new HashMap<CallString, SymbolicAddressMap>(); 
			ContextMap<CallString, SymbolicAddressMap> tmpresult = 
				new ContextMap<CallString, SymbolicAddressMap>(c, initialMap);
			tmpresult.put(c.callString, out);
					
			InstructionHandle entry = p.getEntryHandle(method);
			state.put(entry, join(state.get(entry), tmpresult));
	
			if(DEBUG_PRINT) {
				System.out.println("[I] Invoke: "+method.getMemberID());
				System.out.println(String.format("  StackPtr: %d, framePtr: %d, args: %d",
						context.stackPtr, varPtr, MethodHelper.getArgSize(method)));
			}
			// interpret method
			Map<InstructionHandle, ContextMap<CallString, SymbolicAddressMap>> r = 
				interpreter.interpret(c, entry, state, false);

			SymbolicAddressMap ctxInfo = retval.get(context.callString);

			// pull out relevant information from call
			InstructionHandle exit = p.getExitHandle(method);
			if(r.get(exit) != null) {
				SymbolicAddressMap returned = r.get(exit).get(c.callString);
				if (returned != null) {
					ctxInfo.joinReturned(returned, varPtr);
				} else {
					System.err.println("doInvoke(): No exit information for callstring ?");					
				}
			} else {
				System.err.println("Symbolic Points To[doInvoke()]: No exit information from "+methodName+"?");				
			}
			
			// add relevant information to result
			ctxInfo.addStackUpto(in, context.stackPtr - MethodHelper.getArgSize(method));
			if(DEBUG_PRINT) {
				System.out.println("[R] Invoke: "+method.getMemberID());
				System.out.println(String.format("  StackPtr: %d, framePtr: %d, args: %d",
						context.stackPtr, varPtr, MethodHelper.getArgSize(method)));
			}
		}
	}
	
	@SuppressWarnings({"LiteralAsArgToStringEquals"})
    private Map<CallString, SymbolicAddressMap> handleNative(MethodInfo method, Context context,
			ContextMap<CallString,SymbolicAddressMap> input,
			ContextMap<CallString,SymbolicAddressMap> retval) {
		
		String methodId = method.getMemberID().toString(false);

		SymbolicAddressMap in = input.get(context.callString);
		SymbolicAddressMap out;
		int nextStackPtr = context.stackPtr-1;
		if (methodId.equals("com.jopdesign.sys.Native.rd(I)I")
				|| methodId.equals("com.jopdesign.sys.Native.rdMem(I)I")
				|| methodId.equals("com.jopdesign.sys.Native.rdIntMem(I)I")) {
			out = in.cloneFilterStack(nextStackPtr);
		} else if (methodId.equals("com.jopdesign.sys.Native.wr(II)V")
				|| methodId.equals("com.jopdesign.sys.Native.wrMem(II)V")
				|| methodId.equals("com.jopdesign.sys.Native.wrIntMem(II)V")) {
			out = in.cloneFilterStack(nextStackPtr-2);
		} else if (methodId.equals("com.jopdesign.sys.Native.toInt(Ljava/lang/Object;)I")) {
			out = in.cloneFilterStack(nextStackPtr);
		} else if (methodId.equals("com.jopdesign.sys.Native.toInt(F)I")) {
			out = in.cloneFilterStack(nextStackPtr);
		} else if (methodId.equals("com.jopdesign.sys.Native.toInt(Ljava/lang/Object;)I")) {
			out = in.cloneFilterStack(nextStackPtr);
		} else if (methodId.equals("com.jopdesign.sys.Native.toInt(Ljava/lang/Object;)I")) {
			out = in.cloneFilterStack(nextStackPtr);
		} else if (methodId.equals("com.jopdesign.sys.Native.toLong(D)J")) {
			out = in.cloneFilterStack(nextStackPtr);			
		} else if (methodId.equals("com.jopdesign.sys.Native.toDouble(J)D")) {
			out = in.cloneFilterStack(nextStackPtr);			
		} else if (methodId.equals("com.jopdesign.sys.Native.toObject(I)Ljava/lang/Object;")
				|| methodId.equals("com.jopdesign.sys.Native.toIntArray(I)[I")) {
			out = in.cloneFilterStack(nextStackPtr);
			out.putStack(context.stackPtr - 1, bsFactory.top());
		} else if (methodId.equals("com.jopdesign.sys.Native.getSP()I")) {
			out = in.cloneFilterStack(nextStackPtr+1);
		} else if (methodId.equals("com.jopdesign.sys.Native.getField(II)I")) {
			out = in.cloneFilterStack(nextStackPtr-1);
	    } else if(methodId.equals("com.jopdesign.sys.Native.putField(III)V")) {
			out = in.cloneFilterStack(nextStackPtr-3);
	    } else if (methodId.equals("com.jopdesign.sys.Native.condMove(IIZ)I")) {
			out = in.cloneFilterStack(nextStackPtr-2);
		} else if(methodId.equals("com.jopdesign.sys.Native.arrayLoad(II)I")) {
			out = in.cloneFilterStack(nextStackPtr-1);
		} else if(methodId.equals("com.jopdesign.sys.Native.arrayStore(III)V")) {
			out = in.cloneFilterStack(nextStackPtr-3);			
		} else if(methodId.equals("com.jopdesign.sys.Native.condMoveRef(Ljava/lang/Object;Ljava/lang/Object;Z)Ljava/lang/Object;")) { 
			out = in.cloneFilterStack(nextStackPtr-2);
			BoundedSet<SymbolicAddress> joined =
				in.getStack(context.stackPtr-3).join(in.getStack(context.stackPtr-2));
			out.putStack(context.stackPtr-3, joined);
		} else {
			out = null;
        	RuntimeException ex = new RuntimeException("Unknown native method: " + methodId);
        	Logger.getLogger(this.getClass()).error(ex);
        	throw ex;
		}
		
		retval.put(context.callString, out);
		
		return retval;
	}
	
	public void printResult(DFATool program) {
		Map<String,String> getFields = new TreeMap<String,String>();
		for(InstructionHandle instr : usedRefs.keySet()) {
			
			ContextMap<CallString, BoundedSet<SymbolicAddress>> r = usedRefs.get(instr);
			Context c = r.getContext();
			MethodInfo method = c.getMethodInfo();
			if(method == null) {
				throw new AssertionError("Internal Error: No method '"+c.method()+"'");
			}
			LineNumberTable lines = method.getCode().getLineNumberTable();
			int sourceLine = lines.getSourceLine(instr.getPosition());

            for (CallString callString : r.keySet()) {
                System.out.println(c.method() + ":" + sourceLine + ":" + callString + ": " + instr);
                BoundedSet<SymbolicAddress> symAddr = r.get(callString);

                String infoStr;
                if (instr.getInstruction() instanceof GETFIELD) {
                    GETFIELD gfInstr = (GETFIELD) instr.getInstruction();
                    infoStr = String.format("GETFIELD %s %s %s",
                            symAddr.toString(),
                            gfInstr.getFieldName(c.constPool()),
                            gfInstr.getFieldType(c.constPool()));
                } else if (instr.getInstruction() instanceof ARRAYLENGTH) {
                    infoStr = String.format("ARRAYLENGTH %s",
                            symAddr.toString());
                } else if (instr.getInstruction() instanceof ArrayInstruction) {
                    ArrayInstruction aInstr = (ArrayInstruction) instr.getInstruction();
                    infoStr = String.format("%s %s %s[]",
                            aInstr.getName().toUpperCase(),
                            symAddr.toString(),
                            aInstr.getType(c.constPool()));
                } else {
                    infoStr = String.format("%s %s", instr.getInstruction().getName().toUpperCase(),
                            symAddr.toString());
                }
                if (infoStr != null) {
                    String infoKey = String.format("%s:%04d:%s", c.method(), sourceLine, callString);
                    while (getFields.containsKey(infoKey)) infoKey += "'";
                    getFields.put(infoKey, infoStr);
                }
            }
		}
		for(Entry<String, String> entry : getFields.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println("  "+entry.getValue());
		}
	}
	
	/** Return symbolic object names used at instructions which use a handle 
	  *  <ul>
	  *   <li/> getfield (top of stack)
	  *   <li/> putfield (second on stack)
	  *   <li/> arraylen (top of stack, handle area)
	  *   <li/> a*load (second on stack)
	  *   <li/> a*store (third on stack)
	  * </ul>
	 */
	public HashMap<InstructionHandle, ContextMap<CallString, BoundedSet<SymbolicAddress>>> getResult() {
		return usedRefs;
	}
	
    @Override
	public void serializeResult(File cacheFile) throws IOException {
    	AnalysisResultSerialization.fromContextMapResult(getResult()).serialize(cacheFile);
	}

    @Override
    public Map deSerializeResult(AppInfo appInfo, File cacheFile) throws IOException,
			ClassNotFoundException, MethodNotFoundException {
    	return AnalysisResultSerialization.fromSerialization(cacheFile).toContextMapResult(appInfo, null);
	}

    @Override
    public void copyResults(MethodInfo newContainer, Map<InstructionHandle, InstructionHandle> newHandles) {
        throw new AppInfoError("Not yet implemented!");
    }
}