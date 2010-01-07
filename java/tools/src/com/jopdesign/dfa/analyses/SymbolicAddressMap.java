package com.jopdesign.dfa.analyses;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.jopdesign.dfa.analyses.LoopBounds.ValueMapping;
import com.jopdesign.dfa.framework.BoundedSetFactory;
import com.jopdesign.dfa.framework.MethodHelper;
import com.jopdesign.dfa.framework.BoundedSetFactory.BoundedSet;


/**
 * Map from locations to symbolic addresses. The special value TOP represents the upper bound
 * of the lattice.
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class SymbolicAddressMap {
	// Attention:
	// a.isTop() and b.isTop() does not imply a==b, but top() always returns _top
	@SuppressWarnings("unchecked")
	private static SymbolicAddressMap _top =
		new SymbolicAddressMap(new BoundedSetFactory(0), (HashMap)null);

	private BoundedSetFactory<SymbolicAddress> bsFactory;

	/* Invariant: obj.map == null iff obj == TOP */
	private Map<Location, BoundedSet<SymbolicAddress>> map;

	public boolean isTop()
	{
		return(this.map == null);
	}
	private void setTop()
	{
		this.map = null;
	}
	
	private int maxStackIndex;

	/** empty constructor */
	public SymbolicAddressMap(BoundedSetFactory<SymbolicAddress> bsFactory) {
		this(bsFactory, new HashMap<Location,BoundedSet<SymbolicAddress>>());
		this.maxStackIndex = -1;
	}

	/** top element */
	public static SymbolicAddressMap top() {
		return _top;
	}
	
	/* full, private constructor */
	private SymbolicAddressMap(BoundedSetFactory<SymbolicAddress> bsFactory,
							   HashMap<Location,BoundedSet<SymbolicAddress>> initMap) {
		this.bsFactory = bsFactory;
		this.map = initMap;
	}

	@Override
	public int hashCode() {
		if(isTop()) return 1;
		else return 2 + map.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SymbolicAddressMap other = (SymbolicAddressMap) obj;
		if(this.isTop() || other.isTop()) return (this.isTop() && other.isTop());
		return map.equals(other.map);
	}

	public boolean isSubset(SymbolicAddressMap other) {
		if(other.isTop())       return true;
		else if(this.isTop())   return false;
		else if(other == null)  return false;
		/* Neither is \bot or \top -> pointwise subseteq */
		for(Location l : this.map.keySet()) {
			BoundedSet<SymbolicAddress> thisEntry = map.get(l);
			BoundedSet<SymbolicAddress> otherEntry = other.map.get(l);
			if(otherEntry == null) return false;
			if(! thisEntry.isSubset(otherEntry)) return false;
		}
		return true;
	}
	
	@Override
	protected SymbolicAddressMap clone() {
		if(this.isTop()) return this;
		return new SymbolicAddressMap(this.bsFactory,
				new HashMap<Location, BoundedSet<SymbolicAddress>>(this.map));
	}
	
	/** Clone address map, but only those stack variables below {@code bound} */
	public SymbolicAddressMap cloneFilterStack(int bound) {
		if(this.isTop()) return this;
		SymbolicAddressMap copy = new SymbolicAddressMap(this.bsFactory);
		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : map.entrySet()) {
			Location loc = entry.getKey();
			if(loc.isHeapLoc() || loc.stackLoc < bound) {
				copy.put(loc, entry.getValue());
			}
		}
		return copy;
	}
	
	/** Clone address map, but only those stack variables with index greater than or equal to
	 *  {@code framePtr}. The stack variables are move down to the beginning of the stack. */
	public SymbolicAddressMap cloneInvoke(int framePtr) {
		if(this.isTop()) return this;
		SymbolicAddressMap copy = new SymbolicAddressMap(this.bsFactory);
		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : map.entrySet()) {
			Location loc = entry.getKey();
			if(loc.isHeapLoc()) {
				copy.put(loc, entry.getValue());
			} else if(loc.stackLoc >= framePtr) {
				copy.putStack(loc.stackLoc - framePtr, entry.getValue());
			}
		}
		return copy;
	}

	/** Set stack info from other other map, upto bound.
	 *  Used to restore stack frames when returning from a method */
	public void addStackUpto(SymbolicAddressMap in, int bound) {
		if(in == null) {
			throw new AssertionError("addStackUpto: caller map is undefined ?");
		}
		if(this.isTop()) return;
		if(in.isTop()) {
			setTop();
			return;
		}
		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : in.map.entrySet()) {
			Location loc = entry.getKey();
			if(! loc.isHeapLoc() && loc.stackLoc < bound) {
				map.put(loc, in.getStack(loc.stackLoc));
			}
		}		
	}
	
	public void join(SymbolicAddressMap b) {
		if(b == null) return;
		joinReturned(b,0);
	}
	
	/** Merge in df info from returned method. */
	public void joinReturned(SymbolicAddressMap returned, int framePtr) {
		if(returned == null) {
			throw new AssertionError("joinReturned: returned map is undefined ?");
		}
		if(this.isTop()) return;
		else if(returned.isTop()) {
			setTop();
			return;
		}
//		System.out.println("JOIN RETURNED [returned]");
//		returned.print(System.out, 2);
//		System.out.println("JOIN RETURNED [before]");
//		this.print(System.out, 2);
		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : returned.map.entrySet()) {
			Location locReturnedFrame = entry.getKey();
			Location locCallerFrame; 
			if(locReturnedFrame.isHeapLoc()) {
				locCallerFrame = locReturnedFrame;
			} else {
				locCallerFrame = new Location(locReturnedFrame.stackLoc + framePtr); 
			}
			BoundedSet<SymbolicAddress> callerSet = map.get(locCallerFrame);
			BoundedSet<SymbolicAddress> returnedSet = returned.map.get(locReturnedFrame);
			put(locCallerFrame, returnedSet.join(callerSet));
		}		
//		System.out.println("JOIN RETURNED [after]");
//		this.print(System.out, 2);
	}
	
	public BoundedSet<SymbolicAddress> getStack(int index) {
		if(this.isTop()) return bsFactory.top();
		Location stackLoc = new Location(index);
		BoundedSet<SymbolicAddress> val = map.get(stackLoc);
		return val;
	}
	
	public int getMaxStackIndex() {
		return maxStackIndex;
	}

	public void put(Location l, BoundedSet<SymbolicAddress> bs) {
		if(bs == null) return;
		if(this.isTop()) return;
		if(! l.isHeapLoc() && l.stackLoc > this.maxStackIndex) {
			this.maxStackIndex = l.stackLoc;
		}
		this.map.put(l, bs);
	}
	
	public void putStack(int index, BoundedSet<SymbolicAddress> bs) {
		this.put(new Location(index), bs);
	}
	
	/** Print results
	 * 
	 * @param indent Indentation (amount of leading whitespace)
	 */
	public void print(PrintStream out, int indent) {
		StringBuffer indentstr = new StringBuffer();
		for(int i = 0; i < indent; i++) indentstr.append(' ');
		out.print(indentstr.toString());
		if(this.isTop()) {
			out.println("TOP"); return;
		}
		out.println("SymbolicAddressMap ("+map.size()+")");
		indentstr.append(' ');
		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : map.entrySet()) {
			out.print(indentstr.toString());
			out.print(entry.getKey());
			out.print(": ");
			out.print(entry.getValue());
			out.print("\n");
		}
	}
}
