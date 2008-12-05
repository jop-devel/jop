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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.Type;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.framework.Analysis;
import com.jopdesign.dfa.framework.Context;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.FlowEdge;
import com.jopdesign.dfa.framework.HashedString;
import com.jopdesign.dfa.framework.Interpreter;
import com.jopdesign.dfa.framework.MethodHelper;
import com.jopdesign.dfa.framework.AppInfo;

public class LoopBounds implements Analysis<List<HashedString>, Map<Location, LoopBounds.ValueMapping>> {

	private static final int CALLSTRING_LENGTH = 0;
	private static final int ASSIGN_LIMIT = 64;
	private static final int CONSTRAINT_LIMIT = 1024;

	public static class ValueMapping {

		public Interval assigned;
		public Interval constrained;
		public Interval increment;
		public Location source;
		public int cnt;
		
		public ValueMapping() {
			assigned = new Interval();
			constrained = new Interval();
			increment = null;
			source = null;
			cnt = 0;
		}

		public ValueMapping(int val) {
			assigned = new Interval(val, val);
			constrained = new Interval();
			increment = null;
			source = null;
			cnt = 0;
		}
		
		public ValueMapping(ValueMapping val, boolean full) {
			assigned = new Interval(val.assigned);
			constrained = new Interval(val.constrained);
			
			if (full) {
				if (val.increment != null) {
					increment = new Interval(val.increment);
				} else {
					increment = null;
				}
				source = val.source;
				cnt = val.cnt;
			} else {
				increment = null;
				source = null;
				cnt = 0;
			}
		}		

		public void join(ValueMapping val) {

			//System.out.print("join: "+this+", "+val+" = ");
			if (val != null) {
				final Interval old = new Interval(assigned);

				// merge assigned values
				if (cnt > ASSIGN_LIMIT) {
					assigned = new Interval();
				} else {
					assigned.join(val.assigned);
				}
				// merge constraints
				if (cnt > CONSTRAINT_LIMIT) {
					constrained = new Interval();
				} else {
					constrained.join(val.constrained);
				}
				// apply new constraints
				assigned.constrain(constrained);
				// widen if possible
				assigned.widen(constrained);

				// merge increments
				if (increment == null) {
					increment = val.increment;
				} else if (val.increment != null) {
					increment.join(val.increment);
				}

				if (!old.equals(assigned)) {
					cnt++;
				}
			}
			
			//System.out.println(this);
		}
		
		public boolean equals(Object o) {
			ValueMapping m = (ValueMapping)o;

			boolean inceq = false;
			if (increment == null && m.increment == null) {
				inceq = true;
			} else if (increment == null || m.increment == null) {
				inceq = false;
			} else {
				inceq = increment.equals(m.increment);
			}
			return assigned.equals(m.assigned)
				&& constrained.equals(m.constrained)
				&& inceq;
		}

		public int hashCode() {
			return assigned.hashCode()+31*constrained.hashCode()+31*31*increment.hashCode();
		}
		
		public String toString() {
			return "<"+assigned+", "+constrained+", ="+source+", #"+cnt+", +"+increment+">";				
		}
		
		public static int computeBound(ValueMapping first, ValueMapping second) {
			// basic checks
			if (//first == null ||
					first.increment == null
					// || second == null
					|| second.increment == null) {
				// System.out.println("no valid increment");
 				return -1;
			}
			// check for boundedness
			if (!first.assigned.hasLb()
					|| !first.assigned.hasUb()
					|| !second.assigned.hasLb()
					|| !second.assigned.hasUb()) {
				// System.out.println("unbounded");
				return -1;
			}
			// monotone increments?
			if (first.increment.getLb()*first.increment.getUb() <= 0
					|| second.increment.getLb()*second.increment.getUb() <= 0) {
				// System.out.println("invalid increments");
				return -1;
			}

			int firstRange = first.assigned.getUb() - first.assigned.getLb() + 1;
			int secondRange = second.assigned.getUb() - second.assigned.getLb() + 1;

			int firstBound;
			if (first.assigned.getUb() < first.assigned.getLb()) {
				firstBound = 0;
			} else {
				firstBound = (int)Math.ceil((double)firstRange / Math.min(Math.abs(first.increment.getUb()), Math.abs(first.increment.getLb())));
			}
			int secondBound;
			if (second.assigned.getUb() < second.assigned.getLb()) {
				secondBound = 0;
			} else {
				secondBound = (int)Math.ceil((double)secondRange / Math.min(Math.abs(second.increment.getUb()), Math.abs(second.increment.getLb())));
			}

			return Math.max(firstBound, secondBound);
		}
	}
	
	public ContextMap<List<HashedString>, Map<Location, ValueMapping>> bottom() {
		return null;
	}

	public ContextMap<List<HashedString>, Map<Location, ValueMapping>> initial(InstructionHandle stmt) {
		ContextMap<List<HashedString>, Map<Location, ValueMapping>> retval = new ContextMap<List<HashedString>, Map<Location, ValueMapping>>(new Context(), new HashMap<List<HashedString>, Map<Location, ValueMapping>>());
		
		List<HashedString> l = new LinkedList<HashedString>();
		Map<Location, ValueMapping> init = new HashMap<Location, ValueMapping>();
		
		ValueMapping value;
		
		value = new ValueMapping();
		value.assigned.setLb(0);
		value.assigned.setUb(16);		
		init.put(new Location("com.jopdesign.io.SysDevice.nrCpu"), value);
		
		retval.put(l, init);
		return retval;
	}

	private Map<InstructionHandle, ContextMap<List<HashedString>, Pair<ValueMapping>>> bounds = new HashMap<InstructionHandle, ContextMap<List<HashedString>, Pair<ValueMapping>>>();

	public void initialize(String sig, Context context) {
	}
	
	public ContextMap<List<HashedString>, Map<Location, ValueMapping>> join(
			ContextMap<List<HashedString>, Map<Location, ValueMapping>> s1,
			ContextMap<List<HashedString>, Map<Location, ValueMapping>> s2) {

		if (s1 == null) {
			return new ContextMap<List<HashedString>, Map<Location, ValueMapping>>(s2);
		}
		
		if (s2 == null) {
			return new ContextMap<List<HashedString>, Map<Location, ValueMapping>>(s1);
		}

		ContextMap<List<HashedString>, Map<Location, ValueMapping>> result = new ContextMap<List<HashedString>, Map<Location, ValueMapping>>(new Context(s1.getContext()), new HashMap<List<HashedString>, Map<Location, ValueMapping>>());
		result.putAll(s1);
		result.putAll(s2);
		
		Map<Location, ValueMapping> a = s1.get(s1.getContext().callString);
		Map<Location, ValueMapping> b = s2.get(s2.getContext().callString);

//		System.out.println("A: "+s1);
//		System.out.println("B: "+s2);
		
		Map<Location, ValueMapping> merged = new HashMap<Location, ValueMapping>(a);
		
		for (Iterator<Location> i = b.keySet().iterator(); i.hasNext(); ) {
			Location l = i.next();
			ValueMapping x = a.get(l);
			ValueMapping y = b.get(l);
			if (x != null) {
				if (!x.equals(y)) {
					ValueMapping r = new ValueMapping(x, true);
					r.join(y);
					merged.put(l, r);
				} else {
					merged.put(l, x);
				}
			} else {
				merged.put(l, y);
			}
		}
		
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

	public boolean compare(ContextMap<List<HashedString>, Map<Location, ValueMapping>> s1, ContextMap<List<HashedString>, Map<Location, ValueMapping>> s2) {

		if (s1 == null || s2 == null) {
			return false;
		}

		if (!s1.getContext().equals(s2.getContext())) {
			
			return false;
			
		} else {
			
			Map<Location, ValueMapping> a = s1.get(s1.getContext().callString);
			Map<Location, ValueMapping> b = s2.get(s1.getContext().callString);
			
			if (a == null || b == null) {
				return false;
			}

			for (Iterator<Location> i = a.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (!b.containsKey(l) || !a.get(l).equals(b.get(l))) {
					return false;
				}
			}
			
			return true;
		}
	}

	public ContextMap<List<HashedString>, Map<Location, ValueMapping>> transfer(
			InstructionHandle stmt, FlowEdge edge,
			ContextMap<List<HashedString>, Map<Location, ValueMapping>> input,
			Interpreter<List<HashedString>, Map<Location, ValueMapping>> interpreter,
			Map<InstructionHandle, ContextMap<List<HashedString>, Map<Location, ValueMapping>>> state) {

		Context context = new Context(input.getContext());
		HashMap<Location, ValueMapping> in = (HashMap<Location, ValueMapping>)input.get(context.callString);
		ContextMap<List<HashedString>, Map<Location, ValueMapping>> retval = new ContextMap<List<HashedString>, Map<Location, ValueMapping>>(context, input);

		HashMap<Location, ValueMapping> result = new HashMap<Location, ValueMapping>();
		retval.put(context.callString, result);		

		Instruction instruction = stmt.getInstruction();
		
//		System.out.println(context.method+": "+stmt);
//		System.out.println(stmt+" "+(edge.getType() == FlowEdge.TRUE_EDGE ? "TRUE" : (edge.getType() == FlowEdge.FALSE_EDGE) ? "FALSE" : "NORMAL")+" "+edge);
//		System.out.println(context.callString+"/"+context.method);
//		System.out.print(stmt.getInstruction()+":\t{ ");
//		System.out.print(input.get(context.callString));
//		System.out.println("}");

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
			ConstantPushInstruction instr = (ConstantPushInstruction)instruction;
			result = new HashMap<Location, ValueMapping>(in);
			retval.put(context.callString, result);
			int value = instr.getValue().intValue();
			result.put(new Location(context.stackPtr), new ValueMapping(value));
		}
		break;
		
		case Constants.ACONST_NULL:
			result = in;
			retval.put(context.callString, result);
			break;
			
		case Constants.LDC:
		case Constants.LDC_W: {
			LDC instr = (LDC)instruction;
 			result = new HashMap<Location, ValueMapping>(in);
			retval.put(context.callString, result);
			Type type = instr.getType(context.constPool);
			if (type.equals(Type.INT)) {
				Integer value = (Integer)instr.getValue(context.constPool);
				result.put(new Location(context.stackPtr), new ValueMapping(value.intValue()));
			} else if (type.equals(Type.STRING)) {
				String value = (String)instr.getValue(context.constPool);
				String name = "char[]";
				name += "@"+context.method+":"+stmt.getPosition();	
				result.put(new Location(name+".length"), new ValueMapping(value.length()));
//				System.out.println(name+": \""+value+"\"");				
			}
		}
		break;
		
		case Constants.LDC2_W:
			result = in;
			retval.put(context.callString, result);
			break;
 
		case Constants.ISTORE_0:
		case Constants.ISTORE_1:
		case Constants.ISTORE_2:
		case Constants.ISTORE_3:
		case Constants.ISTORE: {	
			StoreInstruction instr = (StoreInstruction)instruction; 
			int index = instr.getIndex();
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-1	&& l.stackLoc != index) {
					result.put(l, in.get(l));
				}
				if (l.stackLoc == context.stackPtr-1) {
					result.put(new Location(index), new ValueMapping(in.get(l), true));
				}				
			}
		}
		break;
			
		case Constants.ASTORE_0:
		case Constants.ASTORE_1:
		case Constants.ASTORE_2:
		case Constants.ASTORE_3:
		case Constants.ASTORE:
			result = in;
			retval.put(context.callString, result);
			break;	

		case Constants.ILOAD_0:
		case Constants.ILOAD_1:
		case Constants.ILOAD_2:
		case Constants.ILOAD_3:
		case Constants.ILOAD: {	
			LoadInstruction instr = (LoadInstruction)instruction; 
			int index = instr.getIndex();
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr) {
					result.put(l, in.get(l));
				}
				if (l.stackLoc == index) {
					ValueMapping m = new ValueMapping(in.get(l), true);
					m.source = l;
					result.put(new Location(context.stackPtr), m);
				}				
			}
		}
		break;

		case Constants.ALOAD_0:
		case Constants.ALOAD_1:
		case Constants.ALOAD_2:
		case Constants.ALOAD_3:
		case Constants.ALOAD:
			result = in;
			retval.put(context.callString, result);
			break;	

		case Constants.ARRAYLENGTH: {	
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-1) {
					result.put(l, in.get(l));
				}
			}

			AppInfo p = interpreter.getProgram();
			ContextMap<String, String> receivers = p.getReceivers().get(stmt);
			Location location = new Location(context.stackPtr-1); 
			boolean valid = false;
			if (receivers != null) {
				for (Iterator<String> i = receivers.keySet().iterator(); i.hasNext(); ) {
					String arrayName = i.next();
					ValueMapping m = in.get(new Location(arrayName+".length"));
					// System.out.println("ARRAY LENGTH: "+arrayName+": "+m);
					if (m != null) {
						ValueMapping value = new ValueMapping(m, false);
						value.join(result.get(location));
						result.put(location, value);
						valid = true;
					}
				}
			}
			if (!valid) {	
				result.put(new Location(context.stackPtr-1), new ValueMapping());
			}
		}
		break;

		case Constants.PUTFIELD: {			
			PUTFIELD instr = (PUTFIELD)instruction;
			int fieldSize = instr.getFieldType(context.constPool).getSize();
			
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc >= 0 && l.stackLoc < context.stackPtr-1-fieldSize) {
					result.put(l, in.get(l));
				}
			}
			
//			System.out.println(context.stackPtr+","+fieldSize+": "+result);

			AppInfo p = interpreter.getProgram();
			ContextMap<String, String> receivers = p.getReceivers().get(stmt);
			for (Iterator<String> i = receivers.keySet().iterator(); i.hasNext(); ) {
				
				String fieldName = i.next();
				
				String f = fieldName.substring(fieldName.lastIndexOf("."), fieldName.length());
				String strippedName;
				if (fieldName.indexOf("@") >= 0) {
					strippedName = fieldName.split("@")[0] + f;
				} else {
					strippedName = fieldName;
				}
				
//				System.out.println(fieldName+" vs "+strippedName);
				
				if (p.containsField(strippedName)) {
					for (Iterator<Location> k = in.keySet().iterator(); k.hasNext(); ) {
						Location l = k.next();
						if (!receivers.containsKey(l.heapLoc)) {
							result.put(l, in.get(l));
						}
						if (l.stackLoc == context.stackPtr-1) {
							result.put(new Location(fieldName), new ValueMapping(in.get(l), false));
						}
					}
				}
			}
		}
		break;
		
		case Constants.GETFIELD: {			
			GETFIELD instr = (GETFIELD)instruction;
			
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-1) {
					result.put(l, in.get(l));
				}
			}

			AppInfo p = interpreter.getProgram();
			ContextMap<String, String> receivers = p.getReceivers().get(stmt);
			Location location = new Location(context.stackPtr-1);
			boolean valid = false;
			for (Iterator<String> i = receivers.keySet().iterator(); i.hasNext(); ) {
				String fieldName = i.next();
				
				String f = fieldName.substring(fieldName.lastIndexOf("."), fieldName.length());
				String strippedName;
				if (fieldName.indexOf("@") >= 0) {
					strippedName = fieldName.split("@")[0] + f;
				} else {
					strippedName = fieldName;
				}
				
//				System.out.println(fieldName+" vs "+strippedName);

				if (p.containsField(strippedName)) {
					for (Iterator<Location> k = in.keySet().iterator(); k.hasNext(); ) {
						Location l = k.next();
						if (l.heapLoc.equals(fieldName)) {
							ValueMapping value = new ValueMapping(in.get(l), false);
							value.join(result.get(location));
							result.put(location, value);
							valid = true;
						}
					}
				}
			}
			if (!valid && !(instr.getFieldType(context.constPool) instanceof ReferenceType)) {
				result.put(new Location(context.stackPtr-1), new ValueMapping(0));				
			}
		}
		break;
		
		case Constants.PUTSTATIC: {			
			PUTSTATIC instr = (PUTSTATIC)instruction;
			int fieldSize = instr.getFieldType(context.constPool).getSize();
			
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc >= 0 && l.stackLoc < context.stackPtr-fieldSize) {
					result.put(l, in.get(l));
				}
			}

			AppInfo p = interpreter.getProgram();
			ContextMap<String, String> receivers = p.getReceivers().get(stmt);
			for (Iterator<String> i = receivers.keySet().iterator(); i.hasNext(); ) {
				String fieldName = i.next();
				if (p.containsField(fieldName)) {
					for (Iterator<Location> k = in.keySet().iterator(); k.hasNext(); ) {
						Location l = k.next();
						if (!receivers.containsKey(l.heapLoc)) {
							result.put(l, in.get(l));
						}
						if (l.stackLoc == context.stackPtr-1) {
							result.put(new Location(fieldName), new ValueMapping(in.get(l), false));
						}
					}
				}
			}
		}
		break;

		case Constants.GETSTATIC: {			
			GETSTATIC instr = (GETSTATIC)instruction;
			
			result = new HashMap<Location, ValueMapping>(in);
			retval.put(context.callString, result);

			AppInfo p = interpreter.getProgram();
			ContextMap<String, String> receivers = p.getReceivers().get(stmt);
			Location location = new Location(context.stackPtr);
			boolean valid = false;
			for (Iterator<String> i = receivers.keySet().iterator(); i.hasNext(); ) {
				String fieldName = i.next();
				if (p.containsField(fieldName)) {
					for (Iterator<Location> k = in.keySet().iterator(); k.hasNext(); ) {
						Location l = k.next();
						if (l.heapLoc.equals(fieldName)) {
							ValueMapping value = new ValueMapping(in.get(l), false);
							value.join(result.get(location));
							result.put(location, value);
							valid = true;
						}
					}
				}
			}
			if (!valid && !(instr.getFieldType(context.constPool) instanceof ReferenceType)) {
				result.put(new Location(context.stackPtr), new ValueMapping());				
			}
		}
		break;

		case Constants.IASTORE:
		case Constants.CASTORE:
		case Constants.SASTORE:
		case Constants.BASTORE: {
			
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc >= 0 && l.stackLoc < context.stackPtr-3) {
					result.put(l, in.get(l));
				}
			}

			AppInfo p = interpreter.getProgram();
			ContextMap<String, String> receivers = p.getReceivers().get(stmt);
			for (Iterator<String> i = receivers.keySet().iterator(); i.hasNext(); ) {
				String name = i.next();
				for (Iterator<Location> k = in.keySet().iterator(); k.hasNext(); ) {
					Location l = k.next();
					if (!receivers.containsKey(l.heapLoc)) {
						result.put(l, in.get(l));
					}
					if (l.stackLoc == context.stackPtr-1) {
						// TODO: join intervals!
						result.put(new Location(name), new ValueMapping(in.get(l), false));
					}
				}
			}
		}
		break;

		case Constants.AASTORE: {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-3) {
					result.put(l, in.get(l));
				}
			}
		}
		break;
			
		case Constants.IALOAD:
		case Constants.CALOAD:
		case Constants.SALOAD:			
		case Constants.BALOAD: {
			
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-2) {
					result.put(l, in.get(l));
				}
			}

			AppInfo p = interpreter.getProgram();
			ContextMap<String, String> receivers = p.getReceivers().get(stmt);
			Location location = new Location(context.stackPtr-2);
			boolean valid = false;
			if (receivers != null) {
				for (Iterator<String> i = receivers.keySet().iterator(); i.hasNext(); ) {
					String name = i.next();

					for (Iterator<Location> k = in.keySet().iterator(); k.hasNext(); ) {
						Location l = k.next();
						if (l.heapLoc.equals(name)) {
							ValueMapping value = new ValueMapping(in.get(l), false);
							value.join(result.get(location));
							result.put(location, value);
							valid = true;
						}
					}
				}
			}
			if (!valid) {
				result.put(new Location(context.stackPtr-2), new ValueMapping(0));				
			}
		}
		break;
		
		case Constants.AALOAD: {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-2) {
					result.put(l, in.get(l));
				}
			}
		}
		break;
					
		case Constants.DUP: {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				result.put(l, in.get(l));
				if (l.stackLoc == context.stackPtr-1) {
					result.put(new Location(context.stackPtr), new ValueMapping(in.get(l), true));
				}
			}
		}
		break;
		case Constants.DUP_X1: {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-2) {
					result.put(l, in.get(l));
				}
				if (l.stackLoc == context.stackPtr-1) {
					result.put(new Location(context.stackPtr-2), new ValueMapping(in.get(l), true));
					result.put(new Location(context.stackPtr), new ValueMapping(in.get(l), true));
				}
				if (l.stackLoc == context.stackPtr-2) {
					result.put(new Location(context.stackPtr-1), new ValueMapping(in.get(l), true));
				}
			}
		}
		break;
		
		case Constants.DUP2: {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				result.put(l, in.get(l));
				if (l.stackLoc == context.stackPtr-2) {
					result.put(new Location(context.stackPtr), new ValueMapping(in.get(l), true));
				}
				if (l.stackLoc == context.stackPtr-1) {
					result.put(new Location(context.stackPtr+1), new ValueMapping(in.get(l), true));
				}
			}
		}
		break;
		
		case Constants.POP: {	
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-1) {
					result.put(l, in.get(l));
				}
			}
		}
		break;
		
		case Constants.POP2: {	
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-2) {
					result.put(l, in.get(l));
				}
			}
		}
		break;

		case Constants.IINC: {	
			IINC instr = (IINC)instruction; 
			int index = instr.getIndex();
			int increment = instr.getIncrement();
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr) {
					result.put(l, in.get(l));
				}
				if (l.stackLoc == index) {
					ValueMapping m = new ValueMapping(in.get(l), true);
					m.assigned.add(increment);
					m.constrained.add(increment);
					if (m.increment != null) {
						m.increment.join(new Interval(increment, increment));
					} else {
						m.increment = new Interval(increment, increment);
					}
					result.put(l, m);
				}				
			}
		}
		break;
		
		case Constants.IADD: {
			Interval operand = new Interval();
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc == context.stackPtr-1) {
					operand = in.get(l).assigned;
				}
			}			
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-2) {
					result.put(l, in.get(l));	
				} else if (l.stackLoc == context.stackPtr-2) {
					ValueMapping m = new ValueMapping(in.get(l), true);
					m.assigned.add(operand);
					m.constrained.add(operand);
					if (m.increment != null) {
						m.increment.join(operand);
					} else {
						m.increment = operand;
					}
					result.put(l, m);
				}
			}			

		}
		break;
		
		case Constants.ISUB: {
			Interval operand = new Interval();
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc == context.stackPtr-1) {
					operand = in.get(l).assigned;
				}
			}			
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-2) {
					result.put(l, in.get(l));	
				} else if (l.stackLoc == context.stackPtr-2) {
					ValueMapping m = new ValueMapping(in.get(l), true);
					m.assigned.sub(operand);
					m.constrained.sub(operand);
					m.increment = new Interval();
					result.put(l, m);
				}
			}			

		}
		break;

		case Constants.INEG: {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-1) {
					result.put(l, in.get(l));
				} else if (l.stackLoc == context.stackPtr-1) {
					ValueMapping m = new ValueMapping(in.get(l), true);
					m.assigned.neg();
					m.constrained.neg();
					m.increment = new Interval();
					result.put(l, m);
				}
			}			
		}
		break;

		case Constants.IUSHR: {
			Interval operand = new Interval();
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc == context.stackPtr-1) {
					operand = in.get(l).assigned;
				}
			}			
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-2) {
					result.put(l, in.get(l));	
				} else if (l.stackLoc == context.stackPtr-2) {
					ValueMapping m = new ValueMapping(in.get(l), true);
					m.assigned.ushr(operand);
					m.constrained.ushr(operand);
					m.increment = new Interval();
					result.put(l, m);
				}
			}			
		}
		break;
			
		case Constants.ISHR: {
			Interval operand = new Interval();
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc == context.stackPtr-1) {
					operand = in.get(l).assigned;
				}
			}			
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-2) {
					result.put(l, in.get(l));	
				} else if (l.stackLoc == context.stackPtr-2) {
					ValueMapping m = new ValueMapping(in.get(l), true);
					m.assigned.shr(operand);
					m.constrained.shr(operand);
					m.increment = new Interval();
					result.put(l, m);
				}
			}			
		}
		break;

		case Constants.IAND:
		case Constants.IOR:
		case Constants.IXOR:
		case Constants.IMUL: 
		case Constants.IDIV: 
		case Constants.IREM:
		case Constants.ISHL: {
			// TODO: we could be more clever for some operations 
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-2) {
					result.put(l, in.get(l));
				} else if (l.stackLoc == context.stackPtr-2) {
					ValueMapping m = new ValueMapping();
					result.put(l, m);
				}
			}
		}
		break;

		case Constants.I2B:
		case Constants.I2C:
		case Constants.I2S:
			// TODO: is this really correct?
			result = in;
			retval.put(context.callString, result);
			break;

		case Constants.MONITORENTER:
			result = in;
			retval.put(context.callString, result);
			context.syncLevel++;
			break;

		case Constants.MONITOREXIT:
			result = in;
			retval.put(context.callString, result);
			context.syncLevel--;
			if (context.syncLevel < 0) {
				System.err.println("Synchronization level mismatch.");
				System.exit(-1);
			}
			break;

		case Constants.CHECKCAST:
			result = in;
			retval.put(context.callString, result);
			break;
			
		case Constants.INSTANCEOF: {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-1) {
					result.put(l, in.get(l));
				}
			}
			ValueMapping bool = new ValueMapping();
			bool.assigned.setLb(0);
			bool.assigned.setUb(1);
			result.put(new Location(context.stackPtr-1), bool);
		}
		break;

		case Constants.NEW:
			result = in;
			retval.put(context.callString, result);
			break;
			
		case Constants.NEWARRAY: {
			NEWARRAY instr = (NEWARRAY)instruction;

			String name = instr.getType().toString();
			name += "@"+context.method+":"+stmt.getPosition();
			//System.out.println("NEW ARRAY: "+name);

			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-1) {
					result.put(l, in.get(l));
				}
				if (l.stackLoc == context.stackPtr-1) {
					result.put(new Location(name+".length"), in.get(l));
				}
			}
		}
		break;
		
		case Constants.ANEWARRAY: {	
			ANEWARRAY instr = (ANEWARRAY)instruction;

			String name = instr.getType(context.constPool).toString()+"[]";
			name += "@"+context.method+":"+stmt.getPosition();
			//System.out.println("NEW ARRAY: "+name);

			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-1) {
					result.put(l, in.get(l));
				}
				if (l.stackLoc == context.stackPtr-1) {
					result.put(new Location(name+".length"), in.get(l));
				}
			}
		}
		break;
		
		case Constants.MULTIANEWARRAY: {
			MULTIANEWARRAY instr = (MULTIANEWARRAY)instruction;
			int dim = instr.getDimensions();

			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-dim) {
					result.put(l, in.get(l));
				}
			}
			
			String type = instr.getType(context.constPool).toString();
			type = type.substring(0, type.indexOf("["));
			
			for (int i = 1; i <= dim; i++) {
				String name = type;
				for (int k = 0; k < i; k++) {
					name += "[]";
				}
				name += "@"+context.method+":"+stmt.getPosition();

				for (Iterator<Location> k = in.keySet().iterator(); k.hasNext(); ) {
					Location l = k.next();
					if (l.stackLoc == context.stackPtr-i) {
						result.put(new Location(name+".length"), in.get(l));
					}
				}
			}
		}
		break;

		case Constants.GOTO:
			result = in;
			retval.put(context.callString, result);
			break;
		
		case Constants.IFNULL:
		case Constants.IFNONNULL: {	
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-1) {
					result.put(l, in.get(l));
				}
			}
		}
		break;
		
		case Constants.IF_ACMPEQ:
		case Constants.IF_ACMPNE: {	
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-2) {
					result.put(l, in.get(l));
				}
			}
		}
		break;			
			
		case Constants.IFEQ:
		case Constants.IFNE:
		case Constants.IFLT:
		case Constants.IFGE:
		case Constants.IFLE:
		case Constants.IFGT:
			doIf(stmt, edge, context, in, result);
			break;

		case Constants.IF_ICMPEQ:
		case Constants.IF_ICMPNE:
		case Constants.IF_ICMPLT:
		case Constants.IF_ICMPGE:
		case Constants.IF_ICMPGT:
		case Constants.IF_ICMPLE:
			doIfIcmp(stmt, edge, context, in, result);
			break;

		case Constants.LOOKUPSWITCH:
		case Constants.TABLESWITCH:
			result = in;
			retval.put(context.callString, result);
			break;
			
		case Constants.INVOKEVIRTUAL:
		case Constants.INVOKEINTERFACE:
		case Constants.INVOKESTATIC:
		case Constants.INVOKESPECIAL: {
			AppInfo p = interpreter.getProgram();
			ContextMap<String, String> receivers = p.getReceivers().get(stmt);
			if (receivers == null) {
				System.out.println(context.method + ": invoke "	+ instruction.toString(context.constPool.getConstantPool()) + " unknown receivers");
				break;
			}
			for (Iterator<String> i = receivers.keySet().iterator(); i.hasNext(); ) {
				String methodName = i.next();
				doInvoke(methodName, stmt, context, input, interpreter, state, retval);
			}
		}
		break;
		
		case Constants.ARETURN:
		case Constants.RETURN: {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < 0) {
					result.put(l, in.get(l));
				}
			}
		}
		break;						

		case Constants.IRETURN: {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < 0) {
					result.put(l, in.get(l));
				}
				if (l.stackLoc == context.stackPtr-1) {
					result.put(new Location(0), new ValueMapping(in.get(l), false));
				}
			}
		}
		break;						

		default:
//			System.out.println("unknown instruction: "+stmt);
			result = in;
			retval.put(context.callString, result);
			break;
		}
		
//		System.out.println(stmt);
//		System.out.print(stmt.getInstruction()+":\t{ ");
//		if (retval != null) {
//			for (Iterator<Map<Location, ValueMapping>> k = retval.values().iterator(); k.hasNext(); ) {
//				Map<Location, ValueMapping> m = k.next();
//				System.out.print(m+", ");
//			}
//		}
//		System.out.println("}");
		
		context.stackPtr += instruction.produceStack(context.constPool) - instruction.consumeStack(context.constPool);
		return retval;
	}
	
	private void doIf(InstructionHandle stmt, FlowEdge edge, Context context,
			Map<Location, ValueMapping> in,	Map<Location, ValueMapping> result) {
		
		// copy input values
		for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
			Location l = i.next(); 
			if (l.stackLoc < context.stackPtr-1) {
				result.put(l, in.get(l));
			}
		}
		// apply constraint
		for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
			Location l = i.next(); 
			if (l.stackLoc == context.stackPtr-1 && in.get(l).source != null) {
			
				ValueMapping m = new ValueMapping(in.get(l), true);
				
				switch(stmt.getInstruction().getOpcode()) {
				case Constants.IFEQ:				
					if (edge.getType() == FlowEdge.FALSE_EDGE) {
						// != 0 cannot be expressed as interval
						// TODO: mark paths infeasible if appropriate
					} else if (edge.getType() == FlowEdge.TRUE_EDGE) {
						m.constrained.setLb(0);
						m.constrained.setUb(0);
					}
					break;
				case Constants.IFNE:
					if (edge.getType() == FlowEdge.FALSE_EDGE) {
						m.constrained.setLb(0);
						m.constrained.setUb(0);
					} else if (edge.getType() == FlowEdge.TRUE_EDGE) {
						// != 0 cannot be expressed as interval
						// TODO: mark paths infeasible if appropriate
					}
					break;
				case Constants.IFLT:
					if (edge.getType() == FlowEdge.FALSE_EDGE) {
						m.constrained.setLb(0);
					} else if (edge.getType() == FlowEdge.TRUE_EDGE) {
						m.constrained.setUb(-1);
					}
					break;
				case Constants.IFGE:
					if (edge.getType() == FlowEdge.FALSE_EDGE) {
						m.constrained.setUb(-1);
					} else if (edge.getType() == FlowEdge.TRUE_EDGE) {
						m.constrained.setLb(0);
					}
					break;
				case Constants.IFLE:
					if (edge.getType() == FlowEdge.FALSE_EDGE) {
						m.constrained.setLb(1);
					} else if (edge.getType() == FlowEdge.TRUE_EDGE) {
						m.constrained.setUb(0);
					}
					break;
				case Constants.IFGT:
					if (edge.getType() == FlowEdge.FALSE_EDGE) {
						m.constrained.setUb(0);
					} else if (edge.getType() == FlowEdge.TRUE_EDGE) {
						m.constrained.setLb(1);
					}
					break;

				}

				if (m.assigned.getLb() > m.constrained.getUb()
						|| m.assigned.getUb() < m.constrained.getLb()) {
					//System.out.println("infeasible condition: "+m);
					//retval.clear();
					//break;
				}
				
				m.assigned.constrain(m.constrained);
				
				recordBound(stmt, context, edge, m);
									
				// TODO: is this really correct for all cases?
				result.put(in.get(l).source, m);
			}
		}
	}
	
	private void doIfIcmp(InstructionHandle stmt, FlowEdge edge, Context context, Map<Location, ValueMapping> in, Map<Location, ValueMapping> result) {
		// search for constraining value
		Interval constraint = null;
		for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
			Location l = i.next(); 
			if (l.stackLoc == context.stackPtr-1) {
				constraint = in.get(l).assigned;
			}
		}
		// copy input values
		for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
			Location l = i.next(); 
			if (l.stackLoc < context.stackPtr-2) {
				result.put(l, in.get(l));
			}
		}
		// apply constraint
		for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
			Location l = i.next(); 
			if (l.stackLoc == context.stackPtr-2 && in.get(l).source != null) {
			
				ValueMapping m = new ValueMapping(in.get(l), true);
				
				switch(stmt.getInstruction().getOpcode()) {
				case Constants.IF_ICMPEQ:				
					if (edge.getType() == FlowEdge.FALSE_EDGE) {
						// != Interval not expressable as Interval
						// TODO: mark paths infeasible if appropriate
					} else if (edge.getType() == FlowEdge.TRUE_EDGE) {
						if (constraint.hasLb()) {
							m.constrained.setLb(constraint.getLb());
						}
						if (constraint.hasUb()) {
							m.constrained.setUb(constraint.getUb());
						}
					}
					break;
				case Constants.IF_ICMPNE:				
					if (edge.getType() == FlowEdge.FALSE_EDGE) {
						if (constraint.hasLb()) {
							m.constrained.setLb(constraint.getLb());
						}
						if (constraint.hasUb()) {
							m.constrained.setUb(constraint.getUb());
						}
					} else if (edge.getType() == FlowEdge.TRUE_EDGE) {
						// != Interval not expressable as Interval
						// TODO: mark paths infeasible if appropriate
					}
					break;
				case Constants.IF_ICMPLT:
					if (edge.getType() == FlowEdge.FALSE_EDGE) {
						if (constraint.hasLb()) {
							m.constrained.setLb(constraint.getLb());
						}
					} else if (edge.getType() == FlowEdge.TRUE_EDGE) {
						if (constraint.hasUb()) {
							m.constrained.setUb(constraint.getUb()-1);
						}
					}
					break;
				case Constants.IF_ICMPGE:
					if (edge.getType() == FlowEdge.FALSE_EDGE) {
						if (constraint.hasUb()) {
							m.constrained.setUb(constraint.getUb()-1);
						}
					} else if (edge.getType() == FlowEdge.TRUE_EDGE) {
						if (constraint.hasLb()) {
							m.constrained.setLb(constraint.getLb());
						}
					}
					break;
				case Constants.IF_ICMPGT:
					if (edge.getType() == FlowEdge.FALSE_EDGE) {
						if (constraint.hasUb()) {
							m.constrained.setUb(constraint.getUb());
						}
					} else if (edge.getType() == FlowEdge.TRUE_EDGE) {
						if (constraint.hasLb()) {
							m.constrained.setLb(constraint.getLb()+1);
						}
					}
					break;
				case Constants.IF_ICMPLE:
					if (edge.getType() == FlowEdge.FALSE_EDGE) {
						if (constraint.hasLb()) {
							m.constrained.setLb(constraint.getLb()-1);
						}
					} else if (edge.getType() == FlowEdge.TRUE_EDGE) {
						if (constraint.hasUb()) {
							m.constrained.setUb(constraint.getUb());
						}
					}
					break;
				}

				if (m.assigned.getLb() > m.constrained.getUb()
						|| m.assigned.getUb() < m.constrained.getLb()) {
					//System.out.println("infeasible condition: "+m);
					//retval.clear();
					//break;
				}
				
				m.assigned.constrain(m.constrained);
				
				recordBound(stmt, context, edge, m);

				// TODO: is this really correct for all cases?
				result.put(in.get(l).source, m);
			}
		}
	}

	private void doInvoke(String methodName,
			InstructionHandle stmt,
			Context context,
			Map<List<HashedString>, Map<Location, ValueMapping>> input,
			Interpreter<List<HashedString>, Map<Location, ValueMapping>> interpreter,
			Map<InstructionHandle, ContextMap<List<HashedString>, Map<Location, ValueMapping>>> state,
			Map<List<HashedString>, Map<Location, ValueMapping>> result) {

		AppInfo p = interpreter.getProgram();
		MethodInfo mi = p.getMethod(methodName);
		MethodGen method = mi.getMethodGen();
		methodName = method.getClassName()+"."+method.getName()+method.getSignature();

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
			c.callString = new LinkedList<HashedString>(context.callString);
			c.callString.add(new HashedString(context.method + ":" + stmt.getPosition()));
			while (c.callString.size() > CALLSTRING_LENGTH) {
				c.callString.removeFirst();
			}

			// carry only minimal information with call
			Map<Location, ValueMapping> in = input.get(context.callString);
			Map<Location, ValueMapping> out = new HashMap<Location, ValueMapping>();
			for (Iterator<Location> k = in.keySet().iterator(); k.hasNext(); ) {
				Location l = k.next();
				if (l.stackLoc < 0) {
					out.put(l, in.get(l));
				}
				if (l.stackLoc >= varPtr) {
					out.put(new Location(l.stackLoc-varPtr), new ValueMapping(in.get(l), false));
				}
			}
			
			ContextMap<List<HashedString>, Map<Location, ValueMapping>> tmpresult = new ContextMap<List<HashedString>, Map<Location, ValueMapping>>(c, new HashMap<List<HashedString>, Map<Location, ValueMapping>>());
			tmpresult.put(c.callString, out);
					
			InstructionHandle entry = mi.getMethodGen().getInstructionList().getStart();
			state.put(entry, join(tmpresult, state.get(entry)));

			// interpret method
			Map<InstructionHandle, ContextMap<List<HashedString>, Map<Location, ValueMapping>>> r = interpreter.interpret(c, entry, state, false);

			// pull out relevant information from call
			InstructionHandle exit = mi.getMethodGen().getInstructionList().getEnd();
			if (r.get(exit) != null) {
				Map<Location, ValueMapping> returned = r.get(exit).get(c.callString);
				if (returned != null) {
					for (Iterator<Location> i = returned.keySet().iterator(); i.hasNext(); ) {
						Location l = i.next();
						if (l.stackLoc < 0) {
							ValueMapping m = new ValueMapping(returned.get(l), true);
							m.join(result.get(context.callString).get(l));
							result.get(context.callString).put(l, m);
						}
						if (l.stackLoc >= 0) {
							ValueMapping m = new ValueMapping(returned.get(l), false);
							Location loc = new Location(l.stackLoc+varPtr);
							m.join(result.get(context.callString).get(loc));
							result.get(context.callString).put(loc, m);						
						}
					}
				}
			}

			// add relevant information to result
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc >= 0 && l.stackLoc < context.stackPtr-MethodHelper.getArgSize(method)) {
					result.get(context.callString).put(l, new ValueMapping(in.get(l), true));
				}				
			}
		}
	}
	
	private Map<List<HashedString>, Map<Location, ValueMapping>> handleNative(MethodGen method, Context context,
			Map<List<HashedString>, Map<Location, ValueMapping>> input,
			Map<List<HashedString>, Map<Location, ValueMapping>> result) {
		
		String methodId = method.getClassName()+"."+method.getName()+method.getSignature();

		Map<Location, ValueMapping> in = input.get(context.callString);
		Map<Location, ValueMapping> out = new HashMap<Location, ValueMapping>();
	
		if (methodId.equals("com.jopdesign.sys.Native.rd(I)I")
				|| methodId.equals("com.jopdesign.sys.Native.rdMem(I)I")
				|| methodId.equals("com.jopdesign.sys.Native.rdIntMem(I)I")) {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-1) {
					out.put(l, in.get(l));
				}
			}
			out.put(new Location(context.stackPtr-1), new ValueMapping());
		} else if (methodId.equals("com.jopdesign.sys.Native.wr(II)V")
				|| methodId.equals("com.jopdesign.sys.Native.wrMem(II)V")
				|| methodId.equals("com.jopdesign.sys.Native.wrIntMem(II)V")) {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-2) {
					out.put(l, in.get(l));
				}
			}
		} else if (methodId.equals("com.jopdesign.sys.Native.toInt(Ljava/lang/Object;)I")) {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-1) {
					out.put(l, in.get(l));
				}
			}
			out.put(new Location(context.stackPtr-1), new ValueMapping());
		} else if (methodId.equals("com.jopdesign.sys.Native.toObject(I)Ljava/lang/Object;")
				|| methodId.equals("com.jopdesign.sys.Native.toIntArray(I)[I")) {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-1) {
					out.put(l, in.get(l));
				}
			}
		} else if (methodId.equals("com.jopdesign.sys.Native.getSP()I")) {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr) {
					out.put(l, in.get(l));
				}
			}
			out.put(new Location(context.stackPtr), new ValueMapping());
		} else if (methodId.equals("com.jopdesign.sys.Native.toInt(Ljava/lang/Object;)I")) {
			for (Iterator<Location> i = in.keySet().iterator(); i.hasNext(); ) {
				Location l = i.next();
				if (l.stackLoc < context.stackPtr-1) {
					out.put(l, in.get(l));
				}
			}
			out.put(new Location(context.stackPtr-1), new ValueMapping());
		} else {
			System.err.println("Unknown native method: "+methodId);
			System.exit(-1);
		}
		
		result.put(context.callString, out);
		
		return result;
	}
	
	private void recordBound(InstructionHandle stmt, Context context, FlowEdge edge, ValueMapping bound) {
		ContextMap<List<HashedString>, Pair<ValueMapping>> map = bounds.get(stmt);
		if (map == null) {
			map = new ContextMap<List<HashedString>, Pair<ValueMapping>>(context, new HashMap<List<HashedString>, Pair<ValueMapping>>());
			bounds.put(stmt, map);
		}
		Pair<ValueMapping> b = map.get(context.callString);
		if (b == null) {
			b = new Pair<ValueMapping>();
			map.put(context.callString, b);
		}
//		System.out.println("CONDITION BOUND: "+bound);
//		System.out.println("\tin "+context.callString+"/"+context.method);
		if (edge.getType() == FlowEdge.FALSE_EDGE) {
			map.put(context.callString, new Pair<ValueMapping>(b.getFirst(), bound));
		} else if (edge.getType() == FlowEdge.TRUE_EDGE) {
			map.put(context.callString, new Pair<ValueMapping>(bound, b.getSecond()));						
		}
		
	}

	public Map getResult() {
		return bounds;
	}

	public int getBound(AppInfo program, InstructionHandle instr) {
				
		ContextMap<List<HashedString>, Pair<ValueMapping>> r = bounds.get(instr);
		if (r == null) {
			// no bound at this point
			return -1;
		}
		
		// merge bound for all contexts
		int maxValue = -1;
		for (Iterator<List<HashedString>> k = r.keySet().iterator(); k.hasNext(); ) {
			List<HashedString> callString = k.next();
			Pair<ValueMapping> bounds = r.get(callString);

			LoopBounds.ValueMapping first = bounds.getFirst();
			LoopBounds.ValueMapping second = bounds.getSecond();

			int val = ValueMapping.computeBound(first, second);
			if (val < 0) {
				// no bound for some context
				return -1; 
			} else {
				// compute the maximum
				maxValue = Math.max(maxValue, val);
			}
		}			

		return maxValue;
	}
	
	public void printResult(AppInfo program) {
		
		for (Iterator<InstructionHandle> i = bounds.keySet().iterator(); i.hasNext(); ) {
			InstructionHandle instr = i.next();

			ContextMap<List<HashedString>, Pair<ValueMapping>> r = bounds.get(instr);
			Context c = r.getContext();

			LineNumberTable lines = program.getMethod(c.method).getMethod().getLineNumberTable();
			int sourceLine = lines.getSourceLine(instr.getPosition());			

			for (Iterator<List<HashedString>> k = r.keySet().iterator(); k.hasNext(); ) {
				List<HashedString> callString = k.next();
				Pair<ValueMapping> bounds = r.get(callString);

				LoopBounds.ValueMapping first = bounds.getFirst();
				LoopBounds.ValueMapping second = bounds.getSecond();

				System.out.println(c.method+":"+sourceLine+":\t"+callString+": ");

				System.out.print("\t\ttrue:\t");
				System.out.println(first);
				System.out.print("\t\tfalse:\t");
				System.out.println(second);
				System.out.print("\t\tbound:\t");

				int val = ValueMapping.computeBound(first, second);
				if (val >= 0) {
					System.out.println(val);
				} else {
					System.out.println("invalid");
				}
			}			
		}
	}

}
