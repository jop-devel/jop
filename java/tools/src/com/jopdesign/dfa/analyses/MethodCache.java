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
import java.util.Map.Entry;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReferenceType;

import com.jopdesign.dfa.framework.Analysis;
import com.jopdesign.dfa.framework.Context;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.FlowEdge;
import com.jopdesign.dfa.framework.HashedString;
import com.jopdesign.dfa.framework.Interpreter;
import com.jopdesign.dfa.framework.AppInfo;

public class MethodCache implements	Analysis<List<HashedString>, MethodCache.CacheMapping> {

	public static final int CALLSTRING_LENGTH = 16;

	public static final int CACHE_BLOCKS = 8;

	public static final int CACHE_SIZE = 1024;

	public static class CacheMapping {

		public Map<HashedString, BlockSet> cache;

		public CacheMapping() {
			cache = new HashMap<HashedString, BlockSet>();
		}

		public CacheMapping(CacheMapping c) {
			cache = new HashMap<HashedString, BlockSet>();
			for (Iterator<Entry<HashedString, BlockSet>> i = c.cache.entrySet().iterator(); i.hasNext();) {
				Entry<HashedString, BlockSet> entry = i.next();
				cache.put(entry.getKey(), (BlockSet) entry.getValue().clone());
			}
		}

		public boolean fetch(String m, int size) {

			HashedString hashed = new HashedString(m);
			if (cache.get(hashed) != null && cache.get(hashed).isMust()) {
				// sure hit
				System.out.println("HIT: " + hashed);
				return true;
			}

			Map<HashedString, BlockSet> c = new HashMap<HashedString, BlockSet>();
			for (Iterator<HashedString> i = cache.keySet().iterator(); i.hasNext();) {
				HashedString h = i.next();
				BlockSet b = cache.get(h);
				if (b.get(CACHE_BLOCKS - 1)) {
					// potential spill
					b.setMust(false);
				}
				for (int k = CACHE_BLOCKS - 1; k >= 1; k--) {
					b.set(k, b.get(k - 1));
				}
				b.clear(0);
				if (!b.isEmpty()) {
					c.put(h, b);
				}
			}

			// add cache entry
			BlockSet b = new BlockSet(CACHE_BLOCKS);
			b.set(0);
			c.put(hashed, b);

			if (cache.get(hashed) != null) {
				for (Iterator<HashedString> i = cache.keySet().iterator(); i.hasNext();) {
					HashedString h = i.next();
					if (c.get(h) != null) {
						c.get(h).or(cache.get(h));
					} else {
						c.put(h, cache.get(h));
					}
				}
			}

			cache = c;
			return false;
		}

		public boolean equals(Object o) {
			// System.out.println("EQ");
			// try {
			// throw new Exception();
			// } catch (Exception exc) {
			// exc.printStackTrace();
			// }
			CacheMapping m = (CacheMapping) o;
			return cache.equals(m.cache);
		}

		public int hashCode() {
			return cache.hashCode();
		}

		public String toString() {
			return cache.toString();
		}
	}

	public ContextMap<List<HashedString>, CacheMapping> bottom() {
		return null;
	}

	public ContextMap<List<HashedString>, CacheMapping> initial(
			InstructionHandle stmt) {
		ContextMap<List<HashedString>, CacheMapping> init = new ContextMap<List<HashedString>, CacheMapping>(new Context(), new HashMap<List<HashedString>, CacheMapping>());
		List<HashedString> l = new LinkedList<HashedString>();
		CacheMapping m = new CacheMapping();
		m.fetch(signature, -1);
		init.put(l, m);
		return init;
	}

	private String signature;

	public void initialize(String sig, Context context) {
		signature = sig;
	}

	public boolean compare(ContextMap<List<HashedString>, CacheMapping> s1,
			ContextMap<List<HashedString>, CacheMapping> s2) {

		if (s1 == null || s2 == null) {
			return false;
		}

		if (s1 == s2) {
			return true;
		}

		if (!s1.getContext().equals(s2.getContext())) {
			return false;
		} else {
			boolean retval = true;
			for (Iterator<List<HashedString>> i = s1.keySet().iterator(); i.hasNext();) {
				List<HashedString> l = i.next();
				if (!s2.containsKey(l) || !s1.get(l).equals(s2.get(l))) {
					retval = false;
					break;
				}
			}
			return retval;
		}
	}

	public ContextMap<List<HashedString>, CacheMapping> join(
			ContextMap<List<HashedString>, CacheMapping> s1,
			ContextMap<List<HashedString>, CacheMapping> s2) {

		if (s1 == null) {
			return s2;
		}

		if (s2 == null) {
			return s1;
		}

		if (s1 == s2) {
			return s1;
		}

		// ContextSet<CacheMapping> result = new ContextSet<CacheMapping>(new
		// Context(s1.getContext()), s1);
		// result.addAll(s2);

		ContextMap<List<HashedString>, CacheMapping> result = new ContextMap<List<HashedString>, CacheMapping>(
				new Context(s1.getContext()),
				new HashMap<List<HashedString>, CacheMapping>());

		for (Iterator<List<HashedString>> i = s1.keySet().iterator(); i.hasNext();) {
			List<HashedString> l = i.next();

			CacheMapping a = s1.get(l);
			CacheMapping b = s2.get(l);
			CacheMapping merged;

			if (a != null && b != null) {
				merged = new CacheMapping();
				merged.cache = new HashMap<HashedString, BlockSet>();
				for (Iterator<HashedString> m = a.cache.keySet().iterator(); m.hasNext();) {
					HashedString h = m.next();
					BlockSet blocks = new BlockSet(CACHE_BLOCKS);
					blocks.or(a.cache.get(h));
					blocks.setMust(a.cache.get(h).isMust());
					if (b.cache.get(h) != null) {
						blocks.or(b.cache.get(h));
						blocks.setMust(blocks.isMust() && b.cache.get(h).isMust());
					}
					merged.cache.put(h, blocks);
				}
			} else if (a != null) {
				merged = new CacheMapping(a);
				// for (Iterator<HashedString> m =
				// merged.cache.keySet().iterator(); m.hasNext(); ) {
				// HashedString h = m.next();
				// merged.cache.get(h).setMust(false);
				// }
			} else {
				merged = new CacheMapping();
			}
			result.put(l, merged);
		}
		for (Iterator<List<HashedString>> i = s2.keySet().iterator(); i
				.hasNext();) {
			List<HashedString> l = i.next();

			CacheMapping a = s1.get(l);
			CacheMapping b = s2.get(l);

			if (a == null && b != null) {
				CacheMapping merged = new CacheMapping(b);
				// for (Iterator<HashedString> m =
				// merged.cache.keySet().iterator(); m.hasNext(); ) {
				// HashedString h = m.next();
				// merged.cache.get(h).setMust(false);
				// }
				result.put(l, merged);
			}
		}

		// System.out.println(" "+s1);
		// System.out.println(" "+s2);
		// System.out.println("///"+result);
		// System.out.println();

		return result;
	}

	public ContextMap<List<HashedString>, CacheMapping> transfer(
			InstructionHandle stmt,	FlowEdge edge,
			ContextMap<List<HashedString>, CacheMapping> input,
			Interpreter<List<HashedString>, CacheMapping> interpreter,
			Map<InstructionHandle, ContextMap<List<HashedString>, CacheMapping>> state) {

		Context context = new Context(input.getContext());
		ContextMap<List<HashedString>, CacheMapping> result;

		Instruction instruction = stmt.getInstruction();

		// System.out.println(stmt+" |"+input.size()+"|");
		// System.out.println("[ "+input.size());
		// System.out.println(input);
		// System.out.println("]");

		switch (instruction.getOpcode()) {

		case Constants.INVOKEVIRTUAL:
		case Constants.INVOKEINTERFACE:
		case Constants.INVOKESPECIAL:
		case Constants.INVOKESTATIC: {
			result = new ContextMap<List<HashedString>, CacheMapping>(context, new HashMap<List<HashedString>, CacheMapping>());

			AppInfo p = interpreter.getProgram();
			ContextMap<String, String> receivers = p.getReceivers().get(stmt);
			if (receivers == null) {
				System.out.println(context.method + ": invoke "	+ instruction.toString(context.constPool.getConstantPool()) + " unknown receivers");
				break;
			}
			for (Iterator<String> i = receivers.keySet().iterator(); i.hasNext(); ) {
				String methodName = i.next();
				doInvoke(methodName, stmt, input, interpreter, state, result);
			}
		}
		return result;

		case Constants.PUTFIELD: {
			FieldInstruction instr = (FieldInstruction) instruction;
			if (instr.getFieldType(context.constPool) instanceof ReferenceType) {
				result = new ContextMap<List<HashedString>, CacheMapping>(context, new HashMap<List<HashedString>, CacheMapping>());
				String methodName = "com.jopdesign.sys.JVM.f_putfield_ref(III)V";
				doInvoke(methodName, stmt, input, interpreter, state, result);
				return result;
			}
		}
		break;

		case Constants.PUTSTATIC: {
			FieldInstruction instr = (FieldInstruction) instruction;
			if (instr.getFieldType(context.constPool) instanceof ReferenceType) {
				result = new ContextMap<List<HashedString>, CacheMapping>(context, new HashMap<List<HashedString>, CacheMapping>());
				String methodName = "com.jopdesign.sys.JVM.f_putstatic_ref(II)V";
				doInvoke(methodName, stmt, input, interpreter, state, result);
				return result;
			}
		}
		break;

		case Constants.AASTORE: {
			result = new ContextMap<List<HashedString>, CacheMapping>(context, new HashMap<List<HashedString>, CacheMapping>());
			String methodName = "com.jopdesign.sys.JVM.f_aastore(III)V";
			doInvoke(methodName, stmt, input, interpreter, state, result);
		}
		return result;

		case Constants.NEW: {
			result = new ContextMap<List<HashedString>, CacheMapping>(context, new HashMap<List<HashedString>, CacheMapping>());
			String methodName = "com.jopdesign.sys.JVM.f_" + stmt.getInstruction().getName() + "(I)I";
			doInvoke(methodName, stmt, input, interpreter, state, result);
		}
		return result;

		case Constants.NEWARRAY:
		case Constants.ANEWARRAY: {
			result = new ContextMap<List<HashedString>, CacheMapping>(context, new HashMap<List<HashedString>, CacheMapping>());
			String methodName = "com.jopdesign.sys.JVM.f_" + stmt.getInstruction().getName() + "(II)I";
			doInvoke(methodName, stmt, input, interpreter, state, result);
		}
		return result;

		case Constants.LADD:
		case Constants.LAND:
		case Constants.LOR:
		case Constants.LXOR: {
			result = new ContextMap<List<HashedString>, CacheMapping>(context, new HashMap<List<HashedString>, CacheMapping>());
			String methodName = "com.jopdesign.sys.JVM.f_" + stmt.getInstruction().getName() + "(IIII)J";
			doInvoke(methodName, stmt, input, interpreter, state, result);
		}
		return result;

		case Constants.LSUB:
		case Constants.LMUL:
		case Constants.LDIV:
		case Constants.LREM: {
			result = new ContextMap<List<HashedString>, CacheMapping>(context, new HashMap<List<HashedString>, CacheMapping>());
			String methodName = "com.jopdesign.sys.JVM.f_" + stmt.getInstruction().getName() + "(JJ)J";
			doInvoke(methodName, stmt, input, interpreter, state, result);
		}
		return result;

		case Constants.LSHL:
		case Constants.LSHR:
		case Constants.LUSHR: {
			result = new ContextMap<List<HashedString>, CacheMapping>(context, new HashMap<List<HashedString>, CacheMapping>());
			String methodName = "com.jopdesign.sys.JVM.f_" + stmt.getInstruction().getName() + "(III)J";
			doInvoke(methodName, stmt, input, interpreter, state, result);
		}
		return result;
		}

		return input;
	}

	private void doInvoke(
			String methodName,
			InstructionHandle stmt,
			ContextMap<List<HashedString>, CacheMapping> input,
			Interpreter<List<HashedString>, CacheMapping> interpreter,
			Map<InstructionHandle, ContextMap<List<HashedString>, CacheMapping>> state,
			ContextMap<List<HashedString>, CacheMapping> result) {

		Context context = new Context(input.getContext());

		AppInfo p = interpreter.getProgram();
		MethodGen method = p.getMethod(methodName).getMethodGen();
		if (method == null) {
			System.out.println(context.method + ": " + stmt	+ " unknown method: " + methodName);
			return;
		}

		if (method.isNative()) {
			result.putAll(input);
			return;
		}

		// set up new context
		Context c = new Context(context);
		c.constPool = method.getConstantPool();
		c.method = methodName;
		c.callString = new LinkedList<HashedString>(context.callString);
		c.callString.add(new HashedString(context.method + ":" + stmt.getPosition()));

		// System.out.println("input callString: "+context.callString);

		// update cache entries
		ContextMap<List<HashedString>, CacheMapping> tmpresult = new ContextMap<List<HashedString>, CacheMapping>(c, new HashMap<List<HashedString>, CacheMapping>());
		CacheMapping mapping = new CacheMapping(input.get(context.callString));
		InstructionHandle last = method.getInstructionList().getEnd();
		mapping.fetch(methodName, last.getPosition());
		tmpresult.put(c.callString, mapping);

		while (c.callString.size() > CALLSTRING_LENGTH) {
			c.callString.removeFirst();
		}

		System.out.println("### " + stmt + ": " + context.callString + "/" + context.method + "->" + methodName);
		// System.out.println("calstr "+c.callString);

		InstructionHandle entry = method.getInstructionList().getStart();
		state.put(entry, join(tmpresult, state.get(entry)));

		// interpret method
		Map<InstructionHandle, ContextMap<List<HashedString>, CacheMapping>> r = interpreter.interpret(c, entry, state, false);

		// pull out relevant information from call
		InstructionHandle exit = method.getInstructionList().getEnd();
		if (r.get(exit) != null) {
			MethodGen returnMethod = p.getMethod(context.method).getMethodGen();			
			mapping = new CacheMapping(r.get(exit).get(c.callString));
			last = returnMethod.getInstructionList().getEnd();
			mapping.fetch(context.method, last.getPosition());
			result.put(context.callString, mapping);
		}

		if (result.isEmpty()) {
			System.out.println("empty result set!");
		}
	}

	public Map getResult() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void printResult(AppInfo program) {
		System.out.println("NYI");
	}

}
