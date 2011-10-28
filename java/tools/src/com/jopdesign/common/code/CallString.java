/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.common.code;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.common.type.MemberID;
import org.apache.bcel.generic.InstructionHandle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Callstrings for a context sensitive analysis.
 * Note that callstrings are immutable objects.
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 * @author Stefan Hepp <stefan@stefant.org>
 */
public class CallString implements CallStringProvider, Iterable<InvokeSite> {

    public static class CallStringSerialization implements Serializable {
		private static final long serialVersionUID = 1L;
		private List<Pair<String,Integer>> sites = new ArrayList<Pair<String,Integer>>();
		public CallStringSerialization(CallString cs) {
			for(InvokeSite site : cs.getInvokeSiteList()) {
				String method = site.getInvoker().getFQMethodName();
				int pos = site.getInstructionHandle().getPosition();
				this.sites.add(new Pair<String,Integer>(method,pos));
			}
		}
		public CallString getCallString(AppInfo appInfo) throws MethodNotFoundException {
			List<InvokeSite> invokeSiteList = new ArrayList<InvokeSite>();
			for(Pair<String,Integer> invokeSiteSpec : sites) {
				String methodID   = invokeSiteSpec.first();
				Integer pos       = invokeSiteSpec.second();
				MethodInfo method = appInfo.getMethodInfo(MemberID.parse(methodID));
				InstructionHandle ih = method.getCode().getInstructionList().findHandle(pos);
				InvokeSite site   = method.getCode().getInvokeSite(ih);
				invokeSiteList.add(site);
			}
			return CallString.fromInvokeSiteList(invokeSiteList);
		}
		public String toString() {
			return this.sites.toString();
		}
	}

    private final InvokeSite[] callString;
    private final int hash;

    public static final CallString EMPTY = new CallString() {
        @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
        public boolean equals(Object obj) { return obj == this; }
        public int hashCode() { return 1; }
    };

    /**
     * Create a new callstring with one entry.
     * If you want a new, empty CallString, use {@link #EMPTY}.
     *
     * @param site the invokesite to push to the callstring.
     */
    public CallString(InvokeSite site) {
        callString = new InvokeSite[]{site};
        hash = site.hashCode(); 
    }

    public CallString(List<InvokeSite> sites) {
        this(sites.toArray(new InvokeSite[sites.size()]));
    }

    private CallString() {
        callString = new InvokeSite[]{};
        hash = 1;
    }

    private CallString(InvokeSite[] cs) {
        assert cs != null;
        callString = cs;
        int hash = 0;
        for (InvokeSite invokeSite : callString) {
            hash = hash * 31 + invokeSite.hashCode();
        }
        this.hash = hash;
    }

    public CallString getCallString() {
        return this;
    }

    @Override
    public Iterator<InvokeSite> iterator() {
        return new Iterator<InvokeSite>() {
            private int pos = 0;
            @Override
            public boolean hasNext() {
                return pos < callString.length;
            }
            @Override
            public InvokeSite next() {
                return callString[pos++];
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean contains(MethodInfo invoker) {
        for (InvokeSite site : callString) {
            if (site.getInvoker().equals(invoker)) return true;
        }
        return false;
    }

    public boolean contains(InvokeSite invokeSite) {
        for (InvokeSite site : callString) {
            if (site.equals(invokeSite)) return true;
        }
        return false;
    }

    public CallString push(InvokeSite invokeSite) {
        return push(invokeSite, length()+1);
    }

    /**
     * Return a new callstring, extended by the given invoke site.
     * <p>Let {@code n(1)} be the id of the given invoke site, and
     *    {@code n(2),...,n(k)} be the callstring represented by {@code this}
     * <ol><li/>If k &lt;= maxDepth, the resulting callstring is {@code n(1),n(2),...,n(k)}
     *     <li/>If k &gt;= maxDepth, the resulting callstring is {@code n(1),n(2),...,n(maxDepth)}
     * </ol>
     *
     * @param invokeNode a CFG InvokeNode representing the invocation
     * @param maxLen the maximum length of the callstring
     * @return a new callstring with the given invocation as the last element.
     */
    public CallString push(ControlFlowGraph.InvokeNode invokeNode , int maxLen) {
        return push(invokeNode.getBasicBlock().getMethodInfo(),invokeNode.getInstructionHandle(),maxLen);
    }

    /**
     * Return a new callstring, extended by the given invoke site.
     * <p>Let {@code n(1)} be the id of the given invoke site, and
     *    {@code n(2),...,n(k)} be the callstring represented by {@code this}
     * <ol><li/>If k &lt;= maxDepth, the resulting callstring is {@code n(1),n(2),...,n(k)}
     *     <li/>If k &gt;= maxDepth, the resulting callstring is {@code n(1),n(2),...,n(maxDepth)}
     *  </ol>
     *
     * TODO: Code duplication with DFA/LoopBounds.java ?
     *
     * @param method the method containing the invocation
     * @param invoke the invocation instruction
     * @param maxLen the maximum length of the callstring
     * @return a new callstring with the given invocation as the last element.
     */
    public CallString push(MethodInfo method, InstructionHandle invoke, int maxLen) {
        return push(method.getCode().getInvokeSite(invoke), maxLen);
    }

    /**
     * Return a new callstring, extended by the given invoke site.
     * <p>Let {@code n(1)} be the id of the given invoke site, and
     *    {@code n(2),...,n(k)} be the callstring represented by {@code this}
     * <ol><li/>If k &lt;= maxDepth, the resulting callstring is {@code n(1),n(2),...,n(k)}
     *     <li/>If k &gt;=   maxDepth, the resulting callstring is {@code n(1),n(2),...,n(maxDepth)}
     *  </ol>
     *
     * @see MethodCode#getInvokeSite(InstructionHandle)
     * @param is the invokesite to be pushed at the end of the string.
     * @param maxLen the maximum length of the callstring
     * @return a new callstring with the given invocation as the last element.
     */
    public CallString push(InvokeSite is, int maxLen) {

        if (maxLen <= 0) return EMPTY;

        // Workaround for hanging copyOfRange() if this is the empty callstring
        if (callString.length == 0) {
            // We know that maxLen is > 0 here
            return new CallString(is);
        }

        // shallow clone, new length is k
        int k = Math.min(callString.length + 1, maxLen);
        int end = callString.length + 1;
        InvokeSite[] cs = Arrays.copyOfRange(callString, end - k, end);
        cs[k - 1] = is;

        return new CallString(cs);
    }

    public CallString pop() {
        if (callString.length < 2) {
            return EMPTY;
        }
        InvokeSite[] cs = Arrays.copyOfRange(callString, 0, callString.length-1);
        return new CallString(cs);
    }

    /**
     * @return the execution context containing the top invoke
     */
    public ExecutionContext getExecutionContext() {
        if (callString.length == 0) return null;
        return new ExecutionContext(top().getInvoker(), pop());
    }

    public int length() {
        return callString.length;
    }

    /**
     * @return the first invokesite in this callstring.
     */
    public InvokeSite first() {
        if (callString.length > 0) {
            return callString[0];
        } else {
            return null;
        }
    }

    /**
     * Return the most previously pushed InvokeSite.
     * @return the last InvokeSite in this string, or null if this is the empty callstring.
     */
    public InvokeSite top() {
        if (callString.length > 0) {
            return callString[callString.length-1];
        } else {
            return null;
        }
    }

    public InvokeSite get(int pos) {
        return callString[pos];
    }

    /**
     * @param length must be between 0 and {@link #length()} inclusive.
     * @return return a callstring with the given length containing the {@code length} most recently pushed items.
     */
    public CallString getSuffix(int length) {
        if (length == 0) return EMPTY;
        if (length > callString.length) {
            throw new IllegalArgumentException("Trying to get suffix with length "+length+
                    " greater than callstring length "+callString.length);
        }
        if (length == callString.length) return this;

        InvokeSite[] cs = Arrays.copyOfRange(callString, callString.length - length, callString.length);
        return new CallString(cs);
    }

    /**
     * Check if either this callstring or the given callstring is a suffix of the other one.
     * @param cs callstring to compare to.
     * @return true if they are equal or one of them is a suffix of the other.
     */
    public boolean matches(CallString cs) {
        // empty callstring always matches
        if (length() == 0 || cs.length() == 0) return true;
        
        return hasSuffix(cs) || cs.hasSuffix(this);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        InvokeSite[] other = ((CallString) obj).callString;

        // This is slightly faster than Arrays.equals()
        int len = callString.length;
        if (len != other.length) return false;
        
        for (int i = 0; i < len; i++) {
            if (!callString[i].equals(other[i])) return false;
        }
        return true;
    }

    /**
     * Return true if {@code cs} is a suffix of callstring, that is,
     * {@code this = prefix + cs} for some {@code prefix}.
     *
     * @param cs the suffix to check
     * @return true if this callstring ends with the given callstring
     */
    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    public boolean hasSuffix(CallString cs) {
        if (this.equals(cs) || cs.isEmpty()) {
            return true;
        }
        if (callString.length < cs.callString.length) {
            return false;
        }
        int suffixStart = callString.length - cs.callString.length;
        for (int i = 0; i < cs.callString.length; i++) {
            if (!callString[suffixStart + i].equals(cs.callString[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        return callString.length == 0;
    }

    @Override
    public String toString() {
        if (this.isEmpty()) return "CallString.EMPTY";
        long hash = hashCode();
        if (hash < 0) hash += Integer.MAX_VALUE;
        //if (callString.length == 1) {
        //	return String.format("CallString[%s|%x]", callString[0].toString().substring(arg0, arg1), hash);        	
        //} else {
        	return String.format("CallString[|%d|%x]", callString.length, hash);
        //}
    }

    public String toStringVerbose(boolean newlines) {
        if (this.isEmpty()) return "CallString.Empty";
        StringBuffer sb = new StringBuffer("CallString[");
        boolean first = true;
        for (int i = callString.length-1; i >= 0; i--) {
            if (first) first = false;
            else if (newlines) sb.append("\n        ");
            else sb.append(";");
            sb.append(callString[i].toString());
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Create a list of all invoke sites as strings.
     * Note that this representation can change, or it might even be incorrect!
     *
     * @return a list of invoke site string representations.
     * @see #toString()
     */
    public List<String> toStringList() {
        LinkedList<String> cs = new LinkedList<String>();
        for (InvokeSite cse : callString) {
            cs.add(cse.toString());
        }
        return cs;
    }

    /* for serialization */
    private InvokeSite[] getInvokeSiteList() {
        return this.callString;
    }

    public List<InvokeSite> getInvokeSites() {
        return Collections.unmodifiableList(Arrays.asList(callString));
    }

    public static CallString fromInvokeSiteList(List<InvokeSite> invokeSiteList) {
        InvokeSite[] sites = new InvokeSite[invokeSiteList.size()];
        invokeSiteList.toArray(sites);
        return new CallString(Arrays.copyOf(sites, sites.length));
    }

}
