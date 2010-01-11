package com.jopdesign.dfa.framework;

import java.util.LinkedList;
import java.util.List;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;

/**
 * Callstrings for a context sensitive analysis.
 * Note that callstrings are immutable objects.
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class CallString {
	public class CallStringEntry {
		public MethodInfo invokedMethod;
		public int invokeSite;
		private HashedString _key;

		/**
		 * @param invokedMethod The invoked method, must not be null
		 * @param invokeSite (optional) an id of the invoke instruction
		 */
		public CallStringEntry(MethodInfo invokedMethod, int invokeSite) {
			assert invokedMethod != null;

			this.invokedMethod = invokedMethod;
			this.invokeSite = invokeSite;
			this._key = new HashedString(buildStringId());
		}
		public MethodInfo getInvokedMethod() {
			return this.invokedMethod;
		}
		public int getInvokeSite() {
			return this.invokeSite;
		}
		public HashedString getStringId() {
			assert _key != null;

			return _key;
		}
		private String buildStringId() {
			StringBuilder sb = new StringBuilder();
			sb.append(invokedMethod.getFQMethodName());
			sb.append(':');
			sb.append(invokeSite);
			return sb.toString();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			CallStringEntry other = (CallStringEntry) obj;
			if (!invokedMethod.equals(other.invokedMethod)) return false;
			return (invokeSite == other.invokeSite);
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + invokedMethod.hashCode();
			result = prime * result + invokeSite;
			return result;
		}
		@Override
		public String toString() {
			return this._key.toString();
		}
	}

	public static final CallString EMPTY = new CallString() {
		public boolean equals(Object obj) { return obj == this; }
		public int hashCode() { return 1; }
	};

	private LinkedList<CallStringEntry> callString;

	public CallString() {
		callString = new LinkedList<CallStringEntry>();
	}

	protected CallString(LinkedList<CallStringEntry> cs) {
		assert cs != null;

		callString = cs;
	}

	/**
	 * Extend the callstring by the given method
	 */
	public CallString push(InvokeNode n , int maxDepth) {
		return push(n.getBasicBlock().getMethodInfo(),n.getInstructionHandle().getPosition(),maxDepth);
	}

	/**
	 * Extend the callstring by the given method
	 * FIXME: Code duplication with DFA/LoopBounds.java
	 */
	@SuppressWarnings("unchecked")
	public CallString push(MethodInfo m, int siteId, int maxLen) {
		assert callString.size() <= maxLen;
		// shallow clone
		LinkedList<CallStringEntry> cs = (LinkedList<CallStringEntry>) callString.clone();

		if(maxLen == 0) return this;
		if(cs.size() == maxLen) {
			cs.removeFirst();
		}
		cs.add(new CallStringEntry(m,siteId));
		return new CallString(cs);
	}

	@Override
	public int hashCode() {
		return callString.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CallString other = (CallString) obj;
		return this.callString.equals(other.callString);

	}

	public List<HashedString> asList() {
		LinkedList<HashedString> cs = new LinkedList<HashedString>();
		for(CallStringEntry cse : callString) {
			cs.add(cse.getStringId());
		}
		return cs;
	}

	public boolean isEmpty() {
		return callString.isEmpty();
	}

	public String toString() {
		return "callctx["+hashCode()+"]";
	}
}
