package com.jopdesign.dfa.analyses;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.generic.AALOAD;
import org.apache.bcel.generic.ARRAYLENGTH;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.StoreInstruction;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.framework.Analysis;
import com.jopdesign.dfa.framework.BoundedSetFactory;
import com.jopdesign.dfa.framework.CallString;
import com.jopdesign.dfa.framework.Context;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.DFAAppInfo;
import com.jopdesign.dfa.framework.FlowEdge;
import com.jopdesign.dfa.framework.Interpreter;
import com.jopdesign.dfa.framework.MethodHelper;
import com.jopdesign.dfa.framework.BoundedSetFactory.BoundedSet;

public class SymbolicPointsTo implements Analysis<CallString, SymbolicAddressMap> {

	private static final int CALLSTRING_LENGTH = 0;
	private static final boolean DEBUG_PRINT = false;
	private BoundedSetFactory<SymbolicAddress> bsFactory;
	private MethodInfo entryMethod;
	private HashMap<InstructionHandle, ContextMap<CallString, BoundedSet<SymbolicAddress>>> usedRefs =
		new HashMap<InstructionHandle, ContextMap<CallString,BoundedSet<SymbolicAddress>>>();

	public SymbolicPointsTo(int maxSetSize) {
		bsFactory = new BoundedSetFactory<SymbolicAddress>(maxSetSize);
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
		
		CallString l = new CallString();
		SymbolicAddressMap init = new SymbolicAddressMap(bsFactory);
		// Add symbolic stack names
		int i = 0;
		if(! entryMethod.getMethod().isStatic()) {
			init.putStack(i++, bsFactory.singleton(new SymbolicAddress("$this")));
		}
		String[] args = entryMethod.getMethodGen().getArgumentNames();
		for(int j = 0; j < args.length; j++)
		{
			init.putStack(i++, bsFactory.singleton(new SymbolicAddress("$"+args[j])));
		}
		retval.put(l, init);
		return retval;
	}
	
	public void initialize(MethodInfo mi, Context context) {
		entryMethod = mi;
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

//		System.out.println("A: "+s1);
//		System.out.println("B: "+s2);
		
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
		SymbolicAddressMap in = input.get(context.callString);
		ContextMap<CallString, SymbolicAddressMap> retval = new ContextMap<CallString, SymbolicAddressMap>(context, input);

		Instruction instruction = stmt.getInstruction();
		int newStackPtr =
			  context.stackPtr 
			+ instruction.produceStack(context.constPool) 
			- instruction.consumeStack(context.constPool);

//		if (context.method.startsWith("wcet.LBAnalysisTest.measure")) {
//			System.out.println(context.method+": "+stmt);
//			System.out.println("###"+context.stackPtr+" + "+instruction.produceStack(context.constPool)+" - "+instruction.consumeStack(context.constPool));		
//			System.out.println(stmt+" "+(edge.getType() == FlowEdge.TRUE_EDGE ? "TRUE" : (edge.getType() == FlowEdge.FALSE_EDGE) ? "FALSE" : "NORMAL")+" "+edge);
//			System.out.println(context.callString+"/"+context.method);
//			System.out.print(stmt.getInstruction()+":\t{ ");
//			System.out.print(input.get(context.callString));
//			System.out.println("}");
//		}		
		
		//		if (stmt.hasTargeters() && context.method.startsWith("wcet.LBAnalysisTest.measure")) {
//			for (int i = 0; i < stmt.getTargeters().length; i++) {
//				InstructionTargeter targeter = stmt.getTargeters()[i];
//				if (targeter instanceof BranchInstruction) {
//					checkScope(context, stmt);
//					break;
//				}
//			}
//		}
//		
		switch (instruction.getOpcode()) {

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
			// We do not consider constants for now
		}
		break;
		
		case Constants.ACONST_NULL: {
			// Null -> No reference
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			result.putStack(newStackPtr-1, bsFactory.empty());
			retval.put(context.callString, result);		
		}
		break;
			
		case Constants.LDC:
		case Constants.LDC_W: {
			// We do not consider constants for now
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
		}
		break;
		
		case Constants.LDC2_W:
			// We do not consider constants for now
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
			break;
 
		case Constants.ISTORE_0:
		case Constants.ISTORE_1:
		case Constants.ISTORE_2:
		case Constants.ISTORE_3:
		case Constants.ISTORE: {	
			// We do not consider istore for now
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
			result.putStack(instr.getIndex(), in.getStack(context.stackPtr-1));
			retval.put(context.callString, result);
		}
		break;	

		case Constants.ILOAD_0:
		case Constants.ILOAD_1:
		case Constants.ILOAD_2:
		case Constants.ILOAD_3:
		case Constants.ILOAD: {	
			// We do not consider iload
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
			result.putStack(context.stackPtr, in.getStack(instr.getIndex()));
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
			putResult(stmt, context, input.get(context.callString).getStack(context.stackPtr-2));
			PUTFIELD instr = (PUTFIELD)instruction;
			// If PUTFIELD has object type, set result to TOP
			if(instr.getFieldType(context.constPool) instanceof ReferenceType) {
				retval.put(context.callString, SymbolicAddressMap.top());						
			} else {
				retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
			}
		}
		break;
		
		
		// Access Object Handle, top of stack
		// if value has reference type:
		//   stackptr ^ objectref | ... ==> stackptr ^ value | ... 
		//   value <- getfield objectref F ==> value |-> map (.F) M(objectref)
		case Constants.GETFIELD: {			
			putResult(stmt, context, input.get(context.callString).getStack(context.stackPtr-1));
			GETFIELD instr = (GETFIELD)instruction;
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			if(instr.getFieldType(context.constPool) instanceof ReferenceType) {
				BoundedSet<SymbolicAddress> objectMapping = in.getStack(context.stackPtr-1);
				BoundedSet<SymbolicAddress> newMapping;
				if(objectMapping.isSaturated()) {
					newMapping = bsFactory.top();
				} else {
					newMapping = bsFactory.empty();
					for(SymbolicAddress addr: objectMapping.getSet()) {
						newMapping.add(addr.access(instr.getFieldName(context.constPool)));
					}
				}
				result.putStack(context.stackPtr-1, newMapping);
			}
			retval.put(context.callString, result);
			// We are interested in the references here
		}
		break;
		
		case Constants.PUTSTATIC: {			
			PUTSTATIC instr = (PUTSTATIC)instruction;
			// If PUTSTATIC has object type, set result to TOP
			if(instr.getFieldType(context.constPool) instanceof ReferenceType) {
				retval.put(context.callString, SymbolicAddressMap.top());						
			} else {
				retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
			}
		}
		break;
		// Assign TOS the symbolic name
		case Constants.GETSTATIC: {			
			GETSTATIC instr = (GETSTATIC)instruction;
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			if(instr.getFieldType(context.constPool) instanceof ReferenceType) {
				BoundedSet<SymbolicAddress> newMapping = bsFactory.empty();
				newMapping.add(SymbolicAddress.staticField(instr.getFieldName(context.constPool)));
				result.putStack(context.stackPtr, newMapping);				
			}
			retval.put(context.callString, result);		
		}
		break;

		case Constants.IASTORE:
		case Constants.CASTORE:
		case Constants.SASTORE:
		case Constants.BASTORE: {
			putResult(stmt, context, input.get(context.callString).getStack(context.stackPtr-3));
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
		}
		break;
		// changing the heap -> TOP
		case Constants.AASTORE: {
			putResult(stmt, context, input.get(context.callString).getStack(context.stackPtr-3));
			retval.put(context.callString, SymbolicAddressMap.top());						
		}
		break;
			
		case Constants.IALOAD:
		case Constants.CALOAD:
		case Constants.SALOAD:			
		case Constants.BALOAD: {
			putResult(stmt, context, input.get(context.callString).getStack(context.stackPtr-2));
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));		
		}
		break;
		// AALOAD objectref, index -> objectref
		case Constants.AALOAD: {
			putResult(stmt, context, input.get(context.callString).getStack(context.stackPtr-2));
			AALOAD instr = (AALOAD)instruction;
			SymbolicAddressMap result = in.cloneFilterStack(newStackPtr);
			BoundedSet<SymbolicAddress> objectMapping = in.getStack(context.stackPtr-2);
			BoundedSet<SymbolicAddress> newMapping;
			if(objectMapping.isSaturated()) {
				newMapping = bsFactory.top();
			} else {
				newMapping = bsFactory.empty();
				for(SymbolicAddress addr: objectMapping.getSet()) {
					newMapping.add(addr.accessArrayAny());
				}
			}					
				
//				Doesn't work, but is probably stupid anyway :(
//  			LoopBounds bounds = interpreter.getProgram().getLoopBounds();
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
			result.putStack(context.stackPtr, in.getStack(context.stackPtr-1));
			retval.put(context.callString, result);		
		}
		break;
		case Constants.DUP_X1: {
			// copy value on stack
			SymbolicAddressMap result = in.cloneFilterStack(context.stackPtr-2);
			result.putStack(context.stackPtr-2, in.getStack(context.stackPtr-1));
			result.putStack(context.stackPtr-1, in.getStack(context.stackPtr-2));
			result.putStack(context.stackPtr, in.getStack(context.stackPtr-1));
			retval.put(context.callString, result);		
		}
		break;
		
		case Constants.DUP2: {
			// v1,v2 -> v1,v2,v1,v2
			SymbolicAddressMap result = in.cloneFilterStack(context.stackPtr);
			result.putStack(context.stackPtr, in.getStack(context.stackPtr-2));
			result.putStack(context.stackPtr+1, in.getStack(context.stackPtr-1));
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

		case Constants.I2B:
		case Constants.I2C:
		case Constants.I2S: {
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
		}
		break;

		case Constants.MONITORENTER:
			// not supported yet
			retval.put(context.callString, SymbolicAddressMap.top());						
			break;

		case Constants.MONITOREXIT:
			// not supported yet
			retval.put(context.callString, SymbolicAddressMap.top());						
			break;

		case Constants.CHECKCAST:
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
			break;
			
		case Constants.INSTANCEOF: {
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
		}
		break;

		case Constants.NEW: {
			// not supported yet
			retval.put(context.callString, SymbolicAddressMap.top());						
		}
		break;
			
		case Constants.NEWARRAY: {
			// not supported yet
			retval.put(context.callString, SymbolicAddressMap.top());						
		}
		break;
		
		case Constants.ANEWARRAY: {	
			// not supported yet
			retval.put(context.callString, SymbolicAddressMap.top());						
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
			DFAAppInfo p = interpreter.getProgram();
			ContextMap<String, String> receivers = p.getReceivers().get(stmt);
			retval.put(context.callString, new SymbolicAddressMap(bsFactory));
			if (receivers == null) {
				String errMsg = context.method + ": invoke "	+ instruction.toString(context.constPool.getConstantPool()) + " unknown receivers";
				throw new AssertionError(errMsg);
			}
			for (Iterator<String> i = receivers.keySet().iterator(); i.hasNext(); ) {
				String methodName = i.next();
				doInvoke(methodName, stmt, context, input, interpreter, state, retval);
			}
		}
		break;
		
		case Constants.ARETURN: {
			SymbolicAddressMap result = in.cloneFilterStack(0);
			// store results
			result.putStack(0, in.getStack(context.stackPtr-1));
			retval.put(context.callString, result);
		}
		break;
		
		case Constants.RETURN: {
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
			// We are interested in the references here
		}
		break;						

		case Constants.IRETURN: {
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
			// We are interested in the references here
		}
		break;						

		default:
			System.err.println("unknown instruction: "+stmt);
			retval.put(context.callString, in.cloneFilterStack(newStackPtr));						
			break;
		}
		
		
		// DEBUGGING
		if(DEBUG_PRINT) {
			System.out.println("[D] "+context+" / "+stmt);
			System.out.println("  Stackptr: "+context.stackPtr + " -> "+newStackPtr);
			System.out.println("  Before: ");
			input.get(context.callString).print(System.out, 4);		
			System.out.println("  After: ");
			retval.get(context.callString).print(System.out, 4);			
		}
		
		context.stackPtr = newStackPtr;
		return retval;
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

		DFAAppInfo p = interpreter.getProgram();
		MethodInfo mi = p.getMethod(methodName);
		MethodGen method = mi.getMethodGen();
		methodName = method.getClassName()+"."+method.getName()+method.getSignature();

//		System.out.println(stmt+" invokes method: "+methodName);				

		if (method.isNative()) {

			handleNative(method, context, input, retval);

		} else {

			// set up new context
			int varPtr = context.stackPtr - MethodHelper.getArgSize(method);
			Context c = new Context(context);
			c.stackPtr = method.getMaxLocals();
			c.constPool = method.getConstantPool();
			if (method.isSynchronized()) {
				c.syncLevel = context.syncLevel+1;
			}
			c.method = methodName;
			c.callString = c.callString.push(p.getMethod(context.method), stmt.getPosition(), CALLSTRING_LENGTH);
			
			// carry only minimal information with call
			SymbolicAddressMap in = input.get(context.callString);
			SymbolicAddressMap out = in.cloneInvoke(varPtr);
			
			HashMap<CallString, SymbolicAddressMap> initialMap =
				new HashMap<CallString, SymbolicAddressMap>(); 
			ContextMap<CallString, SymbolicAddressMap> tmpresult = 
				new ContextMap<CallString, SymbolicAddressMap>(c, initialMap); 
			tmpresult.put(c.callString, out);
					
			InstructionHandle entry = mi.getMethodGen().getInstructionList().getStart();
			state.put(entry, join(state.get(entry), tmpresult));
			
			// interpret method
			Map<InstructionHandle, ContextMap<CallString, SymbolicAddressMap>> r = 
				interpreter.interpret(c, entry, state, false);

			SymbolicAddressMap ctxInfo = retval.get(context.callString);

			// pull out relevant information from call
			InstructionHandle exit = mi.getMethodGen().getInstructionList().getEnd();
			if(r.get(exit) != null) {
				SymbolicAddressMap returned = r.get(exit).get(c.callString);
				if (returned != null) {
					ctxInfo.joinReturned(returned, varPtr);
				} else {
					System.err.println("doInvoke(): No exit information for callstring ?");					
				}
			} else {
				System.err.println("doInvoke(): No exit information ?");				
			}

			// add relevant information to result
			ctxInfo.addStackUpto(in, context.stackPtr-MethodHelper.getArgSize(method));
		}
	}
	
	private Map<CallString, SymbolicAddressMap> handleNative(MethodGen method, Context context,
			ContextMap<CallString,SymbolicAddressMap> input,
			ContextMap<CallString,SymbolicAddressMap> retval) {
		
		String methodId = method.getClassName()+"."+method.getName()+method.getSignature();

		SymbolicAddressMap in = input.get(context.callString);
		SymbolicAddressMap out;
	
		if (methodId.equals("com.jopdesign.sys.Native.rd(I)I")
				|| methodId.equals("com.jopdesign.sys.Native.rdMem(I)I")
				|| methodId.equals("com.jopdesign.sys.Native.rdIntMem(I)I")) {
			out = in.cloneFilterStack(context.stackPtr - 1);
		} else if (methodId.equals("com.jopdesign.sys.Native.wr(II)V")
				|| methodId.equals("com.jopdesign.sys.Native.wrMem(II)V")
				|| methodId.equals("com.jopdesign.sys.Native.wrIntMem(II)V")) {
			out = in.cloneFilterStack(context.stackPtr - 2);
		} else if (methodId.equals("com.jopdesign.sys.Native.toInt(Ljava/lang/Object;)I")) {
			out = in.cloneFilterStack(context.stackPtr - 1);
		} else if (methodId.equals("com.jopdesign.sys.Native.toObject(I)Ljava/lang/Object;")
				|| methodId.equals("com.jopdesign.sys.Native.toIntArray(I)[I")) {
			out = in.cloneFilterStack(context.stackPtr - 1);
		} else if (methodId.equals("com.jopdesign.sys.Native.getSP()I")) {
			out = in.cloneFilterStack(context.stackPtr);
		} else if (methodId.equals("com.jopdesign.sys.Native.toInt(Ljava/lang/Object;)I")) {
			out = in.cloneFilterStack(context.stackPtr - 1);
		} else if (methodId.equals("com.jopdesign.sys.Native.condMove(IIZ)I")
				|| methodId.equals("com.jopdesign.sys.Native.condMoveRef(Ljava/lang/Object;Ljava/lang/Object;Z)Ljava/lang/Object;")) {
			out = in.cloneFilterStack(context.stackPtr - 3);
		} else {
			out = null;
			System.err.println("Unknown native method: "+methodId);
			System.exit(-1);
		}
		
		retval.put(context.callString, out);
		
		return retval;
	}
	
	public void printResult(DFAAppInfo program) {
		Map<String,String> getFields = new TreeMap<String,String>();
		for(InstructionHandle instr : usedRefs.keySet()) {
			
			ContextMap<CallString, BoundedSet<SymbolicAddress>> r = usedRefs.get(instr);
			Context c = r.getContext();
			MethodInfo method = program.getMethod(c.method);
			if(method == null) {
				throw new AssertionError("Internal Error: No method '"+c.method+"'");
			}
			LineNumberTable lines = method.getMethod().getLineNumberTable();
			int sourceLine = lines.getSourceLine(instr.getPosition());			

			for (Iterator<CallString> k = r.keySet().iterator(); k.hasNext(); ) {
				CallString callString = k.next();

				System.out.println(c.method+":"+sourceLine+":"+callString+": "+instr);
				BoundedSet<SymbolicAddress> symAddr = r.get(callString);
				
				String infoStr;
				if(instr.getInstruction() instanceof GETFIELD) {
					GETFIELD gfInstr = (GETFIELD) instr.getInstruction();
					infoStr = String.format("GETFIELD %s %s %s",
							symAddr.toString(),
							gfInstr.getFieldName(c.constPool),
							gfInstr.getFieldType(c.constPool));
				} else if(instr.getInstruction() instanceof ARRAYLENGTH) {
					infoStr = String.format("ARRAYLENGTH %s",
							symAddr.toString());
				} else if(instr.getInstruction() instanceof ArrayInstruction) {
					ArrayInstruction aInstr = (ArrayInstruction) instr.getInstruction();
					infoStr = String.format("%s %s %s[]",
							aInstr.getName().toUpperCase(),
							symAddr.toString(),										
							aInstr.getType(c.constPool));
				} else {
					infoStr = String.format("%s %s",instr.getInstruction().getName().toUpperCase(),
							symAddr.toString());
				}
				if(infoStr != null) {
					String infoKey = String.format("%s:%04d:%s",c.method,sourceLine,callString);
					while(getFields.containsKey(infoKey)) infoKey = infoKey + "'";
					getFields.put(infoKey,infoStr);					
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
	
}