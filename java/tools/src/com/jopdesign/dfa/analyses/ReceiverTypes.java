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

package com.jopdesign.dfa.analyses;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.Type;

import com.jopdesign.dfa.framework.Analysis;
import com.jopdesign.dfa.framework.Context;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.FlowEdge;
import com.jopdesign.dfa.framework.Interpreter;
import com.jopdesign.dfa.framework.MethodHelper;
import com.jopdesign.dfa.framework.AppInfo;

public class ReceiverTypes implements Analysis<ReceiverTypes.TypeMapping, ReceiverTypes.TypeMapping> {

	public static class TypeMapping {
		public final int stackLoc;
		public final String heapLoc;
		public final String type;
		public final int hash;
		
		public TypeMapping(int l, String t) {
			stackLoc = l;
			heapLoc = "";
			type = t;
			hash = stackLoc+heapLoc.hashCode()+type.hashCode();
		}

		public TypeMapping(String l, String t) {
			stackLoc = -1;
			heapLoc = l;
			type = t;
			hash = stackLoc+heapLoc.hashCode()+type.hashCode();
		}

		public boolean equals(Object o) {
			TypeMapping m = (TypeMapping)o;
			return (stackLoc == m.stackLoc)
				&& heapLoc.equals(m.heapLoc)
				&& type.equals(m.type);
		}
				
		public int hashCode() {
			return hash;
		}
		
		public String toString() {
			if (stackLoc >= 0) {
				return "<stack["+stackLoc+"], "+type+">";
			} else {
				return "<"+heapLoc+", "+type+">";				
			}
		}
	}
	
	private Map<String, ContextMap<TypeMapping, TypeMapping>> threads = new LinkedHashMap<String, ContextMap<TypeMapping, TypeMapping>>();
	private Map<InstructionHandle, ContextMap<String, String>> targets = new LinkedHashMap<InstructionHandle, ContextMap<String, String>>();
	
	public ContextMap<TypeMapping, TypeMapping> bottom() {		
		return null;
	}

	public ContextMap<TypeMapping, TypeMapping> initial(InstructionHandle stmt) {
		ContextMap<TypeMapping, TypeMapping> init = new ContextMap<TypeMapping, TypeMapping>(new Context(), new HashMap<TypeMapping, TypeMapping>());
		
		init.add(new TypeMapping("com.jopdesign.io.IOFactory@com.jopdesign.io.IOFactory.<clinit>()V:0.sp", "com.jopdesign.io.SerialPort"));
		init.add(new TypeMapping("com.jopdesign.io.IOFactory@com.jopdesign.io.IOFactory.<clinit>()V:0.sys", "com.jopdesign.io.SysDevice"));

		return init;
	}

	public void initialize(String sig, Context context) {
		threads.put(sig, new ContextMap<TypeMapping, TypeMapping>(context, new HashMap<TypeMapping, TypeMapping>()));
	}

	public ContextMap<TypeMapping, TypeMapping> join(ContextMap<TypeMapping, TypeMapping> s1, ContextMap<TypeMapping, TypeMapping> s2) {
						
		if (s1 == null) {
			return new ContextMap<TypeMapping, TypeMapping>(s2);
		}
		
		if (s2 == null) {
			return new ContextMap<TypeMapping, TypeMapping>(s1);
		}

		ContextMap<TypeMapping, TypeMapping> result = new ContextMap<TypeMapping, TypeMapping>(new Context(s1.getContext()), new HashMap<TypeMapping, TypeMapping>(s1));
		result.putAll(s2);

		if (result.getContext().stackPtr < 0) {
			result.getContext().stackPtr = s2.getContext().stackPtr;
		}
		if (result.getContext().syncLevel < 0) {
			result.getContext().syncLevel = s2.getContext().syncLevel;
		}
		result.getContext().threaded = Context.isThreaded();
		
		return result;
	}

	public boolean compare(ContextMap<TypeMapping, TypeMapping> s1, ContextMap<TypeMapping, TypeMapping> s2) {

		if (s1 == null || s2 == null) {
			return false;
		}

		if (!s1.getContext().equals(s2.getContext())) {
			return false;
		} else {
			return s2.keySet().containsAll(s1.keySet());
		}
	}

	public ContextMap<TypeMapping, TypeMapping> transfer(
			InstructionHandle stmt, FlowEdge edge,
			ContextMap<TypeMapping, TypeMapping> input,
			Interpreter<TypeMapping, TypeMapping> interpreter,
			Map<InstructionHandle, ContextMap<TypeMapping, TypeMapping>> state) {

		Context context = new Context(input.getContext());
		ContextMap<TypeMapping, TypeMapping> result = new ContextMap<TypeMapping, TypeMapping>(context, new HashMap<TypeMapping, TypeMapping>());
		
		Instruction instruction = stmt.getInstruction();
		
//		System.out.println(context.method+": "+stmt);
//		System.out.print(stmt.getInstruction()+":\t{ ");
//		for (Iterator k = input.keySet().iterator(); k.hasNext(); ) {
//			ReceiverTypes.TypeMapping m = (ReceiverTypes.TypeMapping) k.next();
//			if (m.stackLoc >= 0) {
//				System.out.print("<stack[" + m.stackLoc + "], " + m.type +">, ");
//			} else {
//				System.out.print("<" + m.heapLoc + ", " + m.type +">, ");						
//			}
//		}
//		System.out.println("}");				

		switch (instruction.getOpcode()) {
		
		case Constants.NOP:
			result = input;
			break;

		case Constants.ACONST_NULL:
		case Constants.ICONST_M1:
		case Constants.ICONST_0:
		case Constants.ICONST_1:
		case Constants.ICONST_2:
		case Constants.ICONST_3:
		case Constants.ICONST_4:
		case Constants.ICONST_5:
		case Constants.BIPUSH:
		case Constants.SIPUSH:
		case Constants.FCONST_0:
		case Constants.FCONST_1:
		case Constants.FCONST_2:
		case Constants.LCONST_0:
		case Constants.LCONST_1:
		case Constants.DCONST_0:
		case Constants.DCONST_1:
		case Constants.LDC2_W:			
			result = input;
			break;

		case Constants.LDC: 
		case Constants.LDC_W: {
			LDC instr = (LDC)instruction;
			result = new ContextMap<TypeMapping, TypeMapping>(input);
			Type type = instr.getType(context.constPool);
			if (type.equals(Type.STRING)) {
				result.add(new TypeMapping(context.stackPtr, type.toString()));
				String value = type.toString()+".value";
				//value += "@"+context.method+":"+stmt.getPosition();	
				String name = "char[]";
				name += "@"+context.method+":"+stmt.getPosition();	
				result.add(new TypeMapping(value, name));
			}
		}
		break;
			
		case Constants.DUP: {
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				result.add(m);
				if (m.stackLoc == context.stackPtr-1) {
					result.add(new TypeMapping(context.stackPtr, m.type));
				}
			}
		}
		break;
		case Constants.DUP_X1: {
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr-2) {
					result.add(m);
				}
				if (m.stackLoc == context.stackPtr-1) {
					result.add(new TypeMapping(context.stackPtr-2, m.type));
					result.add(new TypeMapping(context.stackPtr, m.type));
				}
				if (m.stackLoc == context.stackPtr-2) {
					result.add(new TypeMapping(context.stackPtr-1, m.type));
				}
			}
		}
		break;
		case Constants.DUP_X2: {
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr-3) {
					result.add(m);
				}
				if (m.stackLoc == context.stackPtr-1) {
					result.add(new TypeMapping(context.stackPtr-3, m.type));
					result.add(new TypeMapping(context.stackPtr, m.type));
				}
				if (m.stackLoc == context.stackPtr-2) {
					result.add(new TypeMapping(context.stackPtr-1, m.type));
				}
				if (m.stackLoc == context.stackPtr-3) {
					result.add(new TypeMapping(context.stackPtr-2, m.type));
				}
			}
		}
		break;
		case Constants.DUP2: {
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				result.add(m);
				if (m.stackLoc == context.stackPtr-2) {
					result.add(new TypeMapping(context.stackPtr, m.type));
				}
				if (m.stackLoc == context.stackPtr-1) {
					result.add(new TypeMapping(context.stackPtr+1, m.type));
				}
			}
		}
		break;

		case Constants.DUP2_X1: {
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr-3) {
					result.add(m);
				}
				if (m.stackLoc == context.stackPtr-2) {
					result.add(new TypeMapping(context.stackPtr-3, m.type));
					result.add(new TypeMapping(context.stackPtr, m.type));
				}
				if (m.stackLoc == context.stackPtr-1) {
					result.add(new TypeMapping(context.stackPtr-2, m.type));
					result.add(new TypeMapping(context.stackPtr+1, m.type));
				}
				if (m.stackLoc == context.stackPtr-3) {
					result.add(new TypeMapping(context.stackPtr-1, m.type));
				}
			}
		}
		break;

		case Constants.DUP2_X2: {
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr-3) {
					result.add(m);
				}
				if (m.stackLoc == context.stackPtr-2) {
					result.add(new TypeMapping(context.stackPtr-4, m.type));
					result.add(new TypeMapping(context.stackPtr, m.type));
				}
				if (m.stackLoc == context.stackPtr-1) {
					result.add(new TypeMapping(context.stackPtr-3, m.type));
					result.add(new TypeMapping(context.stackPtr+1, m.type));
				}
				if (m.stackLoc == context.stackPtr-4) {
					result.add(new TypeMapping(context.stackPtr-2, m.type));
				}
				if (m.stackLoc == context.stackPtr-3) {
					result.add(new TypeMapping(context.stackPtr-1, m.type));
				}
			}
		}
		break;
		
		case Constants.SWAP: {
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr-2) {
					result.add(m);
				}
				if (m.stackLoc == context.stackPtr-2) {
					result.add(new TypeMapping(context.stackPtr-1, m.type));
				}
				if (m.stackLoc == context.stackPtr-1) {
					result.add(new TypeMapping(context.stackPtr-2, m.type));
				}
			}
		}
		break;

		case Constants.POP:
			filterSet(input, result, context.stackPtr-1);
			break;
		case Constants.POP2:
			filterSet(input, result, context.stackPtr-2);
			break;

		case Constants.GETFIELD: {
			
			GETFIELD instr = (GETFIELD)instruction;
			List<String> receivers = new LinkedList<String>();
			
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr-1) {
					result.add(m);
				} else if (m.stackLoc == context.stackPtr-1) {
					receivers.add(m.type);
				}
			}
			
//			System.out.println(receivers);
//			System.out.println(instr.getLoadClassType(context.constPool)+" . "+instr.getFieldName(context.constPool)+":"+instr.getFieldType(context.constPool));

			if (receivers.isEmpty()
				&& instr.getFieldType(context.constPool) != Type.INT) {
//				System.out.println("GETFIELD not found: "+context.method+"@"+stmt+": "+instr.getFieldName(context.constPool));
//				System.exit(1);
			}
			
			AppInfo p = interpreter.getProgram();

			for (Iterator<String> i = receivers.iterator(); i.hasNext(); ) {
				String receiver = i.next();
				String heapLoc = receiver+"."+instr.getFieldName(context.constPool);
				String namedLoc = receiver.split("@")[0]+"."+instr.getFieldName(context.constPool);
//				System.out.println(heapLoc+" vs "+namedLoc);
				if (p.containsField(namedLoc)) {
					
					recordReceiver(stmt, context, heapLoc);
					for (Iterator<TypeMapping> k = input.keySet().iterator(); k.hasNext(); ) {
						TypeMapping m = k.next();
						if (heapLoc.equals(m.heapLoc)) {
							result.add(new TypeMapping(context.stackPtr-1, m.type));
						}
					}
				}
			}
		}
		break;
		
		case Constants.PUTFIELD: {
			
			PUTFIELD instr = (PUTFIELD)instruction;
			List<String> receivers = new LinkedList<String>();
			
			int fieldSize = instr.getFieldType(context.constPool).getSize();
			
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc >= 0 && m.stackLoc < context.stackPtr-1-fieldSize) {
					result.add(m);
				} else if (m.stackLoc == context.stackPtr-1-fieldSize) {
					receivers.add(m.type);
				}
			}

			if (receivers.isEmpty()) {
//				System.out.println("PUTFIELD not found: "+context.method+"@"+stmt+": "+instr.getFieldName(context.constPool));
//				System.exit(-1);
			}

			AppInfo p = interpreter.getProgram();

			for (Iterator<String> i = receivers.iterator(); i.hasNext(); ) {
				String receiver = i.next();
				String heapLoc = receiver+"."+instr.getFieldName(context.constPool);
				String namedLoc = receiver.split("@")[0]+"."+instr.getFieldName(context.constPool);
				if (p.containsField(namedLoc)) { 
					recordReceiver(stmt, context, heapLoc);
					for (Iterator<TypeMapping> k = input.keySet().iterator(); k.hasNext(); ) {
						TypeMapping m = k.next();
						if (!heapLoc.equals(m.heapLoc) || context.threaded) {
							result.add(m);
						}
						if (m.stackLoc == context.stackPtr-1) {
							result.add(new TypeMapping(heapLoc, m.type));
						}
					}
				}
			}
			
			if (instr.getFieldType(context.constPool) instanceof ReferenceType) {
				doInvokeStatic("com.jopdesign.sys.JVM.f_putfield_ref(III)V", stmt, context, input, interpreter, state, result);
			}
		}
		break;

		case Constants.GETSTATIC: {
			
			GETSTATIC instr = (GETSTATIC)instruction;
			
			AppInfo p = interpreter.getProgram();
			String heapLoc = instr.getClassName(context.constPool)+"."+instr.getFieldName(context.constPool);

			if (p.containsField(heapLoc)) {
				recordReceiver(stmt, context, heapLoc);
				for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
					TypeMapping m = i.next();
					if (m.stackLoc < context.stackPtr) {
						result.add(m);
					}
					if (heapLoc.equals(m.heapLoc)) {
						result.add(new TypeMapping(context.stackPtr, m.type));
					}
				}
			} else {
//				System.out.println("GETSTATIC not found: "+heapLoc);
//				System.exit(1);
			}
			
		}
		break;
		
		case Constants.PUTSTATIC: {
			
			PUTSTATIC instr = (PUTSTATIC)instruction;
			
			AppInfo p = interpreter.getProgram();
			String heapLoc = instr.getClassName(context.constPool)+"."+instr.getFieldName(context.constPool);			
			
			if (p.containsField(heapLoc)) {			
				recordReceiver(stmt, context, heapLoc);
				for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
					TypeMapping m = i.next();
					if (m.stackLoc >= 0 && m.stackLoc < context.stackPtr-1) {
						result.add(m);
					} else if (m.stackLoc == -1 && (!heapLoc.equals(m.heapLoc) || context.threaded)) {
						result.add(m);					
					}
					if (m.stackLoc == context.stackPtr-1) {
						result.add(new TypeMapping(heapLoc, m.type));
					}
				}
			} else {
//				System.out.println("PUTSTATIC not found: "+heapLoc);
//				System.exit(1);
			}
			
			if (instr.getFieldType(context.constPool) instanceof ReferenceType) {
				doInvokeStatic("com.jopdesign.sys.JVM.f_putstatic_ref(II)V", stmt, context, input, interpreter, state, result);
			}
		}
		break;
		
		case Constants.ARRAYLENGTH:
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr-1) {
					result.add(m);
				}
				if (m.stackLoc == context.stackPtr-1) {
					recordReceiver(stmt, context, m.type);
				}
			}
			break;
		
		case Constants.IASTORE:
		case Constants.BASTORE:
		case Constants.CASTORE:
		case Constants.SASTORE:
		case Constants.FASTORE: {
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr-3) {
					result.add(m);
				}
				if (m.stackLoc == context.stackPtr-3) {
					recordReceiver(stmt, context, m.type);
				}
			}
		}
		break;
						
		case Constants.LASTORE:
		case Constants.DASTORE: {
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr-4) {
					result.add(m);
				}
				if (m.stackLoc == context.stackPtr-4) {
					recordReceiver(stmt, context, m.type);
				}
			}
		}
		break;

		case Constants.AASTORE: {
			
			List<String> receivers = new LinkedList<String>();
			
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr-3) {
					result.add(m);
				} else if (m.stackLoc == context.stackPtr-3) {
					receivers.add(m.type);
					recordReceiver(stmt, context, m.type);
				}
			}

			if (receivers.isEmpty()) {
//				System.out.println("AASTORE not found: "+context.method+"@"+stmt);
//				System.exit(-1);
			}

			for (Iterator<String> i = receivers.iterator(); i.hasNext(); ) {
				String receiver = i.next();
				String heapLoc = receiver;
				for (Iterator<TypeMapping> k = input.keySet().iterator(); k.hasNext(); ) {
					TypeMapping m = k.next();
					if (m.stackLoc == context.stackPtr-1) {
						result.add(new TypeMapping(heapLoc, m.type));
					}
				}
			}
			
			doInvokeStatic("com.jopdesign.sys.JVM.f_aastore(III)V", stmt, context, input, interpreter, state, result);
		}
		break;
				
		case Constants.IALOAD:
		case Constants.BALOAD:
		case Constants.CALOAD:
		case Constants.SALOAD:
		case Constants.FALOAD: {
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr-2) {
					result.add(m);
				}
				if (m.stackLoc == context.stackPtr-2) {
					recordReceiver(stmt, context, m.type);
				}
			}
		}
		break;
			
		case Constants.LALOAD:
		case Constants.DALOAD: {
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr-2) {
					result.add(m);
				}
				if (m.stackLoc == context.stackPtr-2) {
					recordReceiver(stmt, context, m.type);
				}
			}
		}
		break;

		case Constants.AALOAD: {
			
			List<String> receivers = new LinkedList<String>();
			
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr-2) {
					result.add(m);
				} else if (m.stackLoc == context.stackPtr-2) {
					receivers.add(m.type);
					recordReceiver(stmt, context, m.type);					
				}
			}
			
//			System.out.println(receivers);

			if (receivers.isEmpty()) {
//					System.out.println("AALOAD not found: "+context.method+"@"+stmt);
//					System.exit(1);
			}

			for (Iterator<String> i = receivers.iterator(); i.hasNext(); ) {
				String receiver = i.next();
				String heapLoc = receiver;
				for (Iterator<TypeMapping> k = input.keySet().iterator(); k.hasNext(); ) {
					TypeMapping m = k.next();
					if (heapLoc.equals(m.heapLoc)) {
						result.add(new TypeMapping(context.stackPtr-2, m.type));
					}
				}
			}			
		}
		break;

		case Constants.NEW: {
			NEW instr = (NEW)instruction;			
			filterSet(input, result, context.stackPtr);
			String name = instr.getType(context.constPool).toString();
			name += "@"+context.method+":"+stmt.getPosition();
			result.add(new TypeMapping(context.stackPtr, name));
			doInvokeStatic("com.jopdesign.sys.JVM.f_"+stmt.getInstruction().getName()+"(I)I", stmt, context, input, interpreter, state, result);
		}
		break;
		
		case Constants.ANEWARRAY: {
			ANEWARRAY instr = (ANEWARRAY)instruction;
			filterSet(input, result, context.stackPtr-1);
			String name = instr.getType(context.constPool).toString()+"[]";
			name += "@"+context.method+":"+stmt.getPosition();
			result.add(new TypeMapping(context.stackPtr-1, name));
			doInvokeStatic("com.jopdesign.sys.JVM.f_"+stmt.getInstruction().getName()+"(II)I", stmt, context, input, interpreter, state, result);
		}
		break;

		case Constants.NEWARRAY: {
			NEWARRAY instr = (NEWARRAY)instruction;
			filterSet(input, result, context.stackPtr-1);
			String name = instr.getType().toString();
			name += "@"+context.method+":"+stmt.getPosition();
			result.add(new TypeMapping(context.stackPtr-1, name));
			doInvokeStatic("com.jopdesign.sys.JVM.f_"+stmt.getInstruction().getName()+"(II)I", stmt, context, input, interpreter, state, result);
		}
		break;
		
		case Constants.MULTIANEWARRAY: {
			MULTIANEWARRAY instr = (MULTIANEWARRAY)instruction;
			int dim = instr.getDimensions();
			
			filterSet(input, result, context.stackPtr-dim);

			String type = instr.getType(context.constPool).toString();
			type = type.substring(0, type.indexOf("["));
			
			for (int i = 1; i <= dim; i++) {
				String type1 = type;
				String type2 = type;
				for (int k = 0; k < i; k++) {
					type1 += "[]";
				}
				for (int k = 0; k < i-1; k++) {
					type2 += "[]";
				}
				type1 += "@"+context.method+":"+stmt.getPosition();
				type2 += "@"+context.method+":"+stmt.getPosition();
				result.add(new TypeMapping(type1, type2));
			}

			String name = instr.getType(context.constPool).toString();
			name += "@"+context.method+":"+stmt.getPosition();
			result.add(new TypeMapping(context.stackPtr-dim, name));
		
			doInvokeStatic("com.jopdesign.sys.JVM.f_"+stmt.getInstruction().getName()+"()I", stmt, context, input, interpreter, state, result);
		}
		break;

		case Constants.ILOAD_0:
		case Constants.ILOAD_1:
		case Constants.ILOAD_2:
		case Constants.ILOAD_3:
		case Constants.ILOAD:
		case Constants.FLOAD_0:
		case Constants.FLOAD_1:
		case Constants.FLOAD_2:
		case Constants.FLOAD_3:		
		case Constants.FLOAD:
		case Constants.LLOAD_0:
		case Constants.LLOAD_1:
		case Constants.LLOAD_2:
		case Constants.LLOAD_3:
		case Constants.LLOAD:
		case Constants.DLOAD_0:
		case Constants.DLOAD_1:
		case Constants.DLOAD_2:
		case Constants.DLOAD_3:
		case Constants.DLOAD: 
			result = input;
			break;
			
		case Constants.ALOAD_0:
		case Constants.ALOAD_1:
		case Constants.ALOAD_2:
		case Constants.ALOAD_3:			
		case Constants.ALOAD: {
			
			LoadInstruction instr = (LoadInstruction)instruction;
			
			int index = instr.getIndex();
			
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr) {
					result.add(m);
				}
				if (m.stackLoc == index) {
					result.add(new TypeMapping(context.stackPtr, m.type));
				}
			}
		}
		break;

		case Constants.ISTORE_0:
		case Constants.ISTORE_1:
		case Constants.ISTORE_2:
		case Constants.ISTORE_3:
		case Constants.ISTORE:
		case Constants.FSTORE_0:
		case Constants.FSTORE_1:
		case Constants.FSTORE_2:
		case Constants.FSTORE_3:
		case Constants.FSTORE:
		case Constants.LSTORE_0:
		case Constants.LSTORE_1:
		case Constants.LSTORE_2:
		case Constants.LSTORE_3:
		case Constants.LSTORE:
		case Constants.DSTORE_0:
		case Constants.DSTORE_1:
		case Constants.DSTORE_2:
		case Constants.DSTORE_3:
		case Constants.DSTORE:
			result = input;
			break;
		
		case Constants.ASTORE_0:
		case Constants.ASTORE_1:
		case Constants.ASTORE_2:
		case Constants.ASTORE_3:
		case Constants.ASTORE: {
			
			StoreInstruction instr = (StoreInstruction)instruction; 

			int index = instr.getIndex();

			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < context.stackPtr-1
						&& m.stackLoc != index) {
					result.add(m);
				}
				if (m.stackLoc == context.stackPtr-1) {
					result.add(new TypeMapping(index, m.type));
				}
			}
		}
		break;

		case Constants.FCMPL:
		case Constants.FCMPG:
		case Constants.LCMP:
		case Constants.DCMPL:
		case Constants.DCMPG:
			result = input;
			break;						
		
		case Constants.IFEQ:
		case Constants.IFNE:
		case Constants.IFLT:
		case Constants.IFGE:
		case Constants.IFGT:
		case Constants.IFLE:
		case Constants.IFNULL:
		case Constants.IFNONNULL:
			filterSet(input, result, context.stackPtr-1);
			break;

		case Constants.IF_ICMPEQ:
		case Constants.IF_ICMPNE:
		case Constants.IF_ICMPLT:
		case Constants.IF_ICMPGE:
		case Constants.IF_ICMPGT:
		case Constants.IF_ICMPLE:
		case Constants.IF_ACMPEQ:
		case Constants.IF_ACMPNE:
			filterSet(input, result, context.stackPtr-2);
			break;
			
		case Constants.TABLESWITCH:
		case Constants.LOOKUPSWITCH:
			filterSet(input, result, context.stackPtr-1);
			break;

		case Constants.GOTO:
			result = input;
			break;			
			
		case Constants.IADD:
		case Constants.ISUB:
		case Constants.IMUL:
		case Constants.IDIV:
		case Constants.IREM:
		case Constants.ISHL:
		case Constants.ISHR:
		case Constants.IUSHR:
		case Constants.IAND:
		case Constants.IOR:
		case Constants.IXOR:
		case Constants.FADD:
		case Constants.FSUB:
		case Constants.FMUL:
		case Constants.FDIV:
		case Constants.FREM:
		case Constants.DADD:
		case Constants.DSUB:
		case Constants.DMUL:
		case Constants.DDIV:
		case Constants.DREM:
		case Constants.IINC:
		case Constants.INEG:
		case Constants.FNEG:
		case Constants.LNEG:
		case Constants.DNEG:
			result = input;
			break;

		case Constants.LADD:
		case Constants.LAND:
		case Constants.LOR:
		case Constants.LXOR:
			result = new ContextMap<TypeMapping, TypeMapping>(input);
			doInvokeStatic("com.jopdesign.sys.JVM.f_"+stmt.getInstruction().getName()+"(IIII)J", stmt, context, input, interpreter, state, result);
			break;

		case Constants.LSUB:
		case Constants.LMUL:
		case Constants.LDIV:
		case Constants.LREM:
			result = new ContextMap<TypeMapping, TypeMapping>(input);
			doInvokeStatic("com.jopdesign.sys.JVM.f_"+stmt.getInstruction().getName()+"(JJ)J", stmt, context, input, interpreter, state, result);
			break;			
			
		case Constants.LSHL:
		case Constants.LSHR:
		case Constants.LUSHR:
			result = new ContextMap<TypeMapping, TypeMapping>(input);
			doInvokeStatic("com.jopdesign.sys.JVM.f_"+stmt.getInstruction().getName()+"(III)J", stmt, context, input, interpreter, state, result);
			break;			

		case Constants.I2B:
		case Constants.I2C:
		case Constants.I2S:
		case Constants.I2F:
		case Constants.F2I:
		case Constants.I2L:
		case Constants.I2D:
		case Constants.F2L:
		case Constants.F2D:
		case Constants.L2I:
		case Constants.D2I:
		case Constants.L2F:
		case Constants.D2F:
		case Constants.L2D:
		case Constants.D2L:
			result = input;
			break;
			
		case Constants.INSTANCEOF:
			filterSet(input, result, context.stackPtr-1);
			break;			

		case Constants.CHECKCAST:
			result = input;
			break;			
			
		case Constants.MONITORENTER:
			filterSet(input, result, context.stackPtr-1);
			context.syncLevel++;
			break;

		case Constants.MONITOREXIT:
			filterSet(input, result, context.stackPtr-1);
			context.syncLevel--;
			if (context.syncLevel < 0) {
				System.err.println("Synchronization level mismatch.");
				System.exit(-1);
			}
			break;
			
		case Constants.INVOKEVIRTUAL:
		case Constants.INVOKEINTERFACE: {
			
			InvokeInstruction instr = (InvokeInstruction)instruction;
			int argSize = MethodHelper.getArgSize(instr, context.constPool);
			
			// find possible revceiver types
			List<String> receivers = new LinkedList<String>();
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc == context.stackPtr-argSize) {
					receivers.add(m.type.split("@")[0]);
				}
			}

			for (Iterator<String> i = receivers.iterator(); i.hasNext(); ) {
				// find receiving method
				String receiver = i.next();
				String signature = instr.getMethodName(context.constPool)+instr.getSignature(context.constPool);
				String methodName = receiver+"."+signature;
				
				// System.out.println("######## "+methodName+" ########");

				doInvokeVirtual(methodName, receiver, stmt, context, input, interpreter, state, result);
			}
			
			// add relevant information to result
			filterSet(input, result, context.stackPtr-argSize);
		}
		break;

		case Constants.INVOKESTATIC:
		case Constants.INVOKESPECIAL: {		

			InvokeInstruction instr = (InvokeInstruction)instruction;
			int argSize = MethodHelper.getArgSize(instr, context.constPool);

			String receiver = instr.getClassName(context.constPool);
			String signature = instr.getMethodName(context.constPool)+instr.getSignature(context.constPool);
			String methodName = receiver+"."+signature;
			
			// System.out.println("######## "+methodName+": "+interpreter.getProgram().getMethod(methodName)+" ########");
			
			if (interpreter.getProgram().getMethod(methodName).getMethodGen().isPrivate()
					&& !interpreter.getProgram().getMethod(methodName).getMethodGen().isStatic()) {
				doInvokeVirtual(methodName, receiver, stmt, context, input, interpreter, state, result);
			} else {
				doInvokeStatic(methodName, stmt, context, input, interpreter, state, result);
			}
			
			// add relevant information to result
			filterSet(input, result, context.stackPtr-argSize);
		}
		break;
		
		case Constants.RETURN:
		case Constants.IRETURN:
		case Constants.FRETURN:
		case Constants.LRETURN:
		case Constants.DRETURN:
			filterSet(input, result, 0);
			break;						

		case Constants.ARETURN: {
			for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
				TypeMapping m = i.next();
				if (m.stackLoc < 0) {
					result.add(m);
				}
				if (m.stackLoc == context.stackPtr-1) {
					result.add(new TypeMapping(0, m.type));
				}
			}
		}
		break;						

		default:
			System.out.println("Unknown opcode: "+instruction.toString(context.constPool.getConstantPool()));
			System.exit(-1);
		}
		
//		System.out.print(instruction+":\t{ ");
//		for (Iterator k = result.iterator(); k.hasNext(); ) {
//			ReceiverTypes.TypeMapping m = (ReceiverTypes.TypeMapping) k.next();
//			if (m.stackLoc >= 0) {
//				System.out.print("<stack[" + m.stackLoc + "], " + m.type +">, ");
//			} else {
//				System.out.print("<" + m.heapLoc + ", " + m.type +">, ");						
//			}
//		}
//		System.out.println("}");				

		context.stackPtr += instruction.produceStack(context.constPool) - instruction.consumeStack(context.constPool);
		
		return new ContextMap<TypeMapping, TypeMapping>(context, result);
	}

	private void filterSet(Map<TypeMapping, TypeMapping> input, Map<TypeMapping, TypeMapping> result, int bound) {
		for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
			TypeMapping m = i.next();
			if (m.stackLoc < bound) {
				result.put(m, m);
			}
		}		
	}
	
	private void filterReturnSet(Map<TypeMapping, TypeMapping> input, Map<TypeMapping, TypeMapping> result, int varPtr) {
		for (Iterator<TypeMapping> i = input.keySet().iterator(); i.hasNext(); ) {
			TypeMapping m = i.next();
			if (m.stackLoc < 0) {
				result.put(m, m);
			}
			if (m.stackLoc >= 0) {
				TypeMapping t = new TypeMapping(m.stackLoc+varPtr, m.type);
				result.put(t, t);
			}
		}		
	}
	
	private void doInvokeVirtual(String methodName, String receiver,
			InstructionHandle stmt, Context context,
			ContextMap<TypeMapping, TypeMapping> input,
			Interpreter<TypeMapping, TypeMapping> interpreter,
			Map<InstructionHandle, ContextMap<TypeMapping, TypeMapping>> state,
			ContextMap<TypeMapping, TypeMapping> result) {
		
		AppInfo p = interpreter.getProgram();
		if (p.getMethod(methodName) == null) {
			System.out.println(context.method+": "+stmt+" unknown method: "+methodName);
			return;					
		}
		MethodGen method = p.getMethod(methodName).getMethodGen();
		String signature = method.getName()+method.getSignature();
		methodName = method.getClassName()+"."+signature;
				
		recordReceiver(stmt, context, methodName);
		
//		System.out.println(stmt+" invokes method: "+methodName);
		
//		LineNumberTable lines = p.getMethods().get(context.method).getLineNumberTable(context.constPool);
//		int sourceLine = lines.getSourceLine(stmt.getPosition());
//		String invokeId = context.method+"\t"+":"+sourceLine+"."+stmt.getPosition();

		// set up new context
		int varPtr = context.stackPtr - MethodHelper.getArgSize(method);
		Context c = new Context(context);
		c.stackPtr = method.getMaxLocals();
		c.constPool = method.getConstantPool();
		if (method.isSynchronized()) {
			c.syncLevel = context.syncLevel+1;
		}
		c.method = methodName;

		boolean threaded = false;	
		
		if (p.cliMap.get(receiver).clazz.instanceOf(p.cliMap.get("joprt.RtThread").clazz) && signature.equals("run()V")) {
//					System.out.println("spawning thread: "+methodName);
			c.createThread();
			threaded = true;
		}

		// carry only minimal information with call
		ContextMap<TypeMapping, TypeMapping> tmpresult = new ContextMap<TypeMapping, TypeMapping>(c, new HashMap<TypeMapping, TypeMapping>());
		for (Iterator<TypeMapping> k = input.keySet().iterator(); k.hasNext(); ) {
			TypeMapping m = k.next();
			if (m.stackLoc < 0) {
				tmpresult.add(m);
			}
			if (m.stackLoc > varPtr) {
				tmpresult.add(new TypeMapping(m.stackLoc-varPtr, m.type));
			}
			if (m.stackLoc == varPtr) {
				// add "this"
				if (receiver.equals(m.type.split("@")[0])) {
					tmpresult.add(new TypeMapping(0, m.type));
				}
			}
		}

		InstructionHandle entry = method.getInstructionList().getStart();
		state.put(entry, join(tmpresult, state.get(entry)));
		
		// interpret method
		Map<InstructionHandle, ContextMap<TypeMapping, TypeMapping>> r = interpreter.interpret(c, entry, state, false);
							
		// pull out relevant information from call
		InstructionHandle exit = method.getInstructionList().getEnd();
		if (r.get(exit) != null) { 
			filterReturnSet(r.get(exit), result, varPtr);
		}

		// update all threads
		if (threaded) {
			threads.put(methodName, new ContextMap<TypeMapping, TypeMapping>(c, result));
			updateThreads(result, interpreter, state); 
		}
	}

	private void doInvokeStatic(String methodName,
			InstructionHandle stmt,
			Context context,
			Map<TypeMapping, TypeMapping> input,
			Interpreter<TypeMapping, TypeMapping> interpreter,
			Map<InstructionHandle, ContextMap<TypeMapping, TypeMapping>> state,
			Map<TypeMapping, TypeMapping> result) {

		AppInfo p = interpreter.getProgram();
		MethodGen method = p.getMethod(methodName).getMethodGen();
		methodName = method.getClassName()+"."+method.getName()+method.getSignature();

		recordReceiver(stmt, context, methodName);

//		System.out.println(stmt+" invokes method: "+methodName);				

		if (method.isNative()) {

			handleNative(method, context, input, result);

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

			// carry only minimal information with call
			ContextMap<TypeMapping, TypeMapping> tmpresult = new ContextMap<TypeMapping, TypeMapping>(c, new HashMap<TypeMapping, TypeMapping>());
			for (Iterator<TypeMapping> k = input.keySet().iterator(); k.hasNext(); ) {
				TypeMapping m = k.next();
				if (m.stackLoc < 0) {
					tmpresult.add(m);
				}
				if (m.stackLoc >= varPtr) {
					tmpresult.add(new TypeMapping(m.stackLoc-varPtr, m.type));
				}
			}

			InstructionHandle entry = method.getInstructionList().getStart();
			state.put(entry, join(tmpresult, state.get(entry)));

			// interpret method
			Map<InstructionHandle, ContextMap<TypeMapping, TypeMapping>> r = interpreter.interpret(c, entry, state, false);

			// pull out relevant information from call
			InstructionHandle exit = method.getInstructionList().getEnd();				
			if (r.get(exit) != null) {
				filterReturnSet(r.get(exit), result, varPtr);
			}
		}
	}
	
	private void updateThreads(Map<TypeMapping, TypeMapping> input,
			Interpreter<TypeMapping, TypeMapping> interpreter,
			Map<InstructionHandle, ContextMap<TypeMapping, TypeMapping>> state) {
		
		AppInfo p = interpreter.getProgram();
		
		boolean modified = true;
		while (modified) {
			modified = false;
			
			for (Iterator<String> k = threads.keySet().iterator(); k.hasNext(); ) {

				String methodName = k.next();

				MethodGen method = p.getMethod(methodName).getMethodGen();
				InstructionHandle entry = method.getInstructionList().getStart();
				Context c = state.get(entry).getContext();

				int varPtr = c.stackPtr - MethodHelper.getArgSize(method);

				// prepare input information
				ContextMap<TypeMapping, TypeMapping> threadInput = new ContextMap<TypeMapping, TypeMapping>(c, new HashMap<TypeMapping, TypeMapping>());
				filterSet(input, threadInput, 0);
				state.put(entry, join(state.get(entry), threadInput));

				// save information
				ContextMap<TypeMapping, TypeMapping> savedResult = threads.get(methodName);

				// interpret thread
				Map<InstructionHandle, ContextMap<TypeMapping, TypeMapping>> r = interpreter.interpret(c, entry, state, false);

				// pull out relevant information from thread
				InstructionHandle exit = method.getInstructionList().getEnd();
				ContextMap<TypeMapping, TypeMapping> threadResult;
				if (r.get(exit) != null) {
					threadResult = new ContextMap<TypeMapping, TypeMapping>(c, new HashMap<TypeMapping, TypeMapping>());
					filterReturnSet(r.get(exit), threadResult, varPtr);
				} else {
					threadResult = new ContextMap<TypeMapping, TypeMapping>(c, new HashMap<TypeMapping, TypeMapping>());
				}

				if (!threadResult.equals(savedResult)) {
					modified = true;
//					System.err.println("<changed>");
				}

				threads.put(methodName, threadResult);
			}
		}
	}
	
	private Map<TypeMapping, TypeMapping> handleNative(MethodGen method, Context context,
			Map<TypeMapping, TypeMapping> input, Map<TypeMapping, TypeMapping> result) {
		
		String methodId = method.getClassName()+"."+method.getName()+method.getSignature();
		
		if (methodId.equals("com.jopdesign.sys.Native.rd(I)I")
				|| methodId.equals("com.jopdesign.sys.Native.rdMem(I)I")
				|| methodId.equals("com.jopdesign.sys.Native.rdIntMem(I)I")) {
			filterSet(input, result, context.stackPtr-1);
		} else if (methodId.equals("com.jopdesign.sys.Native.wr(II)V")
				|| methodId.equals("com.jopdesign.sys.Native.wrMem(II)V")
				|| methodId.equals("com.jopdesign.sys.Native.wrIntMem(II)V")) {
			filterSet(input, result, context.stackPtr-2);
		} else if (methodId.equals("com.jopdesign.sys.Native.getSP()I")) {
			filterSet(input, result, context.stackPtr);
		} else if (methodId.equals("com.jopdesign.sys.Native.toInt(Ljava/lang/Object;)I")
				|| methodId.equals("com.jopdesign.sys.Native.toObject(I)Ljava/lang/Object;")) {
			filterSet(input, result, context.stackPtr-1);
		} else if (methodId.equals("com.jopdesign.sys.Native.toIntArray(I)[I")) {
			filterSet(input, result, context.stackPtr-1);
			String name = "int[]@"+context.method+":0";
			TypeMapping map = new TypeMapping(context.stackPtr-1, name);
			result.put(map, map);
		} else if (methodId.equals("com.jopdesign.sys.Native.makeLong(II)J")) {
			filterSet(input, result, context.stackPtr-2);
		} else if (methodId.equals("com.jopdesign.sys.Native.memCopy(III)V")) {
			filterSet(input, result, context.stackPtr-3);
		} else if (methodId.equals("com.jopdesign.sys.Native.getField(II)I")
				|| methodId.equals("com.jopdesign.sys.Native.arrayLoad(II)I")) {
			filterSet(input, result, context.stackPtr-2);
		} else if (methodId.equals("com.jopdesign.sys.Native.putField(III)V")
				|| methodId.equals("com.jopdesign.sys.Native.arrayStore(III)V")) {
			filterSet(input, result, context.stackPtr-3);
		} else {
			System.err.println("Unknown native method: "+methodId);
			System.exit(-1);
		}
		
		return result;
	}
	
	private void recordReceiver(InstructionHandle stmt, Context context, String target) {
		if (targets.get(stmt) == null) {
			targets.put(stmt, new ContextMap<String, String>(context, new HashMap<String, String>()));
		}
		targets.get(stmt).add(target);	
	}
	
	public Map<InstructionHandle, ContextMap<String, String>> getResult() {
		return targets;
	}
	
	public void printResult(AppInfo program) {
		
		for (Iterator<InstructionHandle> i = targets.keySet().iterator(); i.hasNext(); ) {
			InstructionHandle instr = i.next();

			ContextMap<String, String> r = targets.get(instr);
			Context c = r.getContext();

			System.out.println(c.method+":"+instr.getPosition());
			for (Iterator<String> k = r.keySet().iterator(); k.hasNext(); ) {
				String target = k.next();
				System.out.println("\t"+target);
			}
		}			
	}

}
