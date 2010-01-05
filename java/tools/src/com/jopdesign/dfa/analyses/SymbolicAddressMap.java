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
	@SuppressWarnings("unchecked")
	public static SymbolicAddressMap TOP = new SymbolicAddressMap(new BoundedSetFactory(0), (HashMap)null);

	/* Invariant: obj.map == null ==> obj == BOTTOM || obj == TOP */
	private Map<Location, BoundedSet<SymbolicAddress>> map;
	private BoundedSetFactory<SymbolicAddress> bsFactory;

	private int topOfStack;

	/** empty constructor */
	public SymbolicAddressMap(BoundedSetFactory<SymbolicAddress> bsFactory) {
		this(bsFactory, new HashMap<Location,BoundedSet<SymbolicAddress>>());
		this.topOfStack = -1;
	}
	/** copy constructor */
	public SymbolicAddressMap(SymbolicAddressMap a) {
		this(a.bsFactory, new HashMap<Location,BoundedSet<SymbolicAddress>>(a.map));
		this.topOfStack = a.topOfStack;
	}
	/* full, private constructor */
	private SymbolicAddressMap(BoundedSetFactory<SymbolicAddress> bsFactory,
							   HashMap<Location,BoundedSet<SymbolicAddress>> initMap) {
		this.bsFactory = bsFactory;
		this.map = initMap;
	}
	@Override
	public int hashCode() {
		if(this == TOP) return 1;
		else return 2 + map.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SymbolicAddressMap other = (SymbolicAddressMap) obj;
		if (map == null || other.map == null) return false; // TOP and BOTTOM use object equality
		return map.equals(other.map);
	}
	public boolean isSubset(SymbolicAddressMap other) {
		if(other == TOP)       return true;
		else if(this == TOP)   return false;
		else if(other == null) return false;
		/* Neither is bottom or top -> pointwise subseteq */
		for(Location l : this.map.keySet()) {
			BoundedSet<SymbolicAddress> thisEntry = map.get(l);
			BoundedSet<SymbolicAddress> otherEntry = other.map.get(l);
			if(otherEntry == null) return false;
			if(! thisEntry.isSubset(otherEntry)) return false;
		}
		return true;
	}
	@Override
	protected SymbolicAddressMap clone() throws CloneNotSupportedException {
		if(this == TOP) return this;
		return new SymbolicAddressMap(this);
	}
	
	/** Clone address map, but only those stack variables below {@code bound} */
	public SymbolicAddressMap cloneFilterStack(int bound) {
		if(this == TOP) return this;
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
		if(this == TOP) return this;
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
		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : map.entrySet()) {
			Location loc = entry.getKey();
			if(! loc.isHeapLoc() && loc.stackLoc < bound) {
				map.put(loc, in.getStack(loc.stackLoc));
			}
		}		
	}
	
	public void join(SymbolicAddressMap b) {
		joinReturned(b,0);
	}
	
	/** Merge in df info from returned method */
	public void joinReturned(SymbolicAddressMap returned, int framePtr) {
		if(this == TOP) return;

		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : returned.map.entrySet()) {
			Location loc = entry.getKey();
			BoundedSet<SymbolicAddress> currentSet = map.get(loc);
			BoundedSet<SymbolicAddress> returnedSet = returned.map.get(loc);
			if (loc.isHeapLoc()) {
				put(loc, returnedSet.join(currentSet));
			} else {
				putStack(loc.stackLoc + framePtr, returnedSet.join(currentSet));
			}
		}		
	}
	
	public BoundedSet<SymbolicAddress> getStack(int index) {
		if(this == TOP) return bsFactory.top();
		Location stackLoc = new Location(index);
		BoundedSet<SymbolicAddress> val = map.get(stackLoc);
		if(val == null) throw new AssertionError("Undefined stack loc: "+index);
		return val;
	}
	
	public BoundedSet<SymbolicAddress> getTopOfStack() {
		if(this == TOP) return bsFactory.top();
		return map.get(new Location(topOfStack));
	}

	public void put(Location l, BoundedSet<SymbolicAddress> bs) {
		if(this == TOP) return;
		if(! l.isHeapLoc() && l.stackLoc > this.topOfStack) {
			this.topOfStack = l.stackLoc;
		}
		this.map.put(l, bs);
	}
	
	public void putStack(int index, BoundedSet<SymbolicAddress> bs) {
		if(this == TOP) return;
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
		if(this==TOP) {
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
