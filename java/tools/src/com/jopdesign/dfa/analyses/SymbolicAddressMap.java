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

import com.jopdesign.dfa.framework.BoundedSetFactory;
import com.jopdesign.dfa.framework.BoundedSetFactory.BoundedSet;
import org.apache.log4j.Logger;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


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
		new SymbolicAddressMap(new BoundedSetFactory(0), (HashMap)null, (HashMap) null);

	private BoundedSetFactory<SymbolicAddress> bsFactory;

	/* Invariant: obj.map == null iff obj == TOP */
	private Map<Location, BoundedSet<SymbolicAddress>> mapP;
	private HashMap<String, BoundedSet<SymbolicAddress>> mapA;

	public boolean isTop()
	{
		return(this.mapP == null);
	}
	private void setTop()
	{
		this.mapP = null;
		this.mapA = null;
	}
	

	/** empty constructor */
	public SymbolicAddressMap(BoundedSetFactory<SymbolicAddress> bsFactory) {
		this(bsFactory, new HashMap<Location,BoundedSet<SymbolicAddress>>(),
				        new HashMap<String,BoundedSet<SymbolicAddress>>());
	}

	/** top element */
	public static SymbolicAddressMap top() {
		return _top;
	}
	
	/* full, private constructor */
	private SymbolicAddressMap(BoundedSetFactory<SymbolicAddress> bsFactory,
                               HashMap<Location, BoundedSet<SymbolicAddress>> initP,
                               HashMap<String, BoundedSet<SymbolicAddress>> initA) {
		this.bsFactory = bsFactory;
		this.mapP = initP;
		this.mapA = initA;
	}

	@Override
	public int hashCode() {
		if(isTop()) return 1;
		else return 2 + 31 * mapP.hashCode() + mapA.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SymbolicAddressMap other = (SymbolicAddressMap) obj;
		if(this.isTop() || other.isTop()) return (this.isTop() && other.isTop());
		return mapP.equals(other.mapP) && mapA.equals(other.mapA);
	}

	public boolean isSubset(SymbolicAddressMap other) {
		if(other.isTop())       return true;
		else if(this.isTop())   return false;
		else if(other == null)  return false;
		/* Neither is \bot or \top -> pointwise subseteq */
		for(Location l : this.mapP.keySet()) {
			BoundedSet<SymbolicAddress> thisEntry = mapP.get(l);
			BoundedSet<SymbolicAddress> otherEntry = other.mapP.get(l);
			if(otherEntry == null) return false;
			if(! thisEntry.isSubset(otherEntry)) return false;
		}
		for(String l : this.mapA.keySet()) {
			BoundedSet<SymbolicAddress> thisEntry = mapP.get(l);
			BoundedSet<SymbolicAddress> otherEntry = other.mapP.get(l);
			if(otherEntry == null) return false;
			if(! thisEntry.isSubset(otherEntry)) return false;
		}
		return true;
	}
	
	@Override
	protected SymbolicAddressMap clone() {
		if(this.isTop()) return this;
		return new SymbolicAddressMap(this.bsFactory,
				new HashMap<Location, BoundedSet<SymbolicAddress>>(this.mapP),
				new HashMap<String, BoundedSet<SymbolicAddress>>(this.mapA));
	}
	
	/** Clone address map, but only those stack variables below {@code bound} */
	public SymbolicAddressMap cloneFilterStack(int bound) {
		if(this.isTop()) return this;
		SymbolicAddressMap copy = new SymbolicAddressMap(this.bsFactory);
		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : mapP.entrySet()) {
			Location loc = entry.getKey();
			if(loc.isHeapLoc() || loc.stackLoc < bound) {
				copy.put(loc, entry.getValue());
			}
		}
		return copy;
	}
	
	/** Clone address map, but only those stack variables with index greater than or equal to
	 *  {@code framePtr}. The stack variables are moved down to the beginning of the stack. */
	public SymbolicAddressMap cloneInvoke(int framePtr) {
		if(this.isTop()) return this;
		SymbolicAddressMap copy = new SymbolicAddressMap(this.bsFactory);
		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : mapP.entrySet()) {
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
		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : in.mapP.entrySet()) {
			Location loc = entry.getKey();
			if(! loc.isHeapLoc() && loc.stackLoc < bound) {
				mapP.put(loc, in.getStack(loc.stackLoc));
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

		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : returned.mapP.entrySet()) {
			Location locReturnedFrame = entry.getKey();
			Location locCallerFrame; 
			if(locReturnedFrame.isHeapLoc()) {
				locCallerFrame = locReturnedFrame;
			} else {
				locCallerFrame = new Location(locReturnedFrame.stackLoc + framePtr); 
			}
			BoundedSet<SymbolicAddress> callerSet = mapP.get(locCallerFrame);
			BoundedSet<SymbolicAddress> returnedSet = returned.mapP.get(locReturnedFrame);
			put(locCallerFrame, returnedSet.join(callerSet));
		}		
	}
	
	public void copyStack(SymbolicAddressMap in, int dst, int src) {
		if(in.isTop()) return;
		if(this.isTop()) return;
		Location srcLoc = new Location(src);
		BoundedSet<SymbolicAddress> val = in.mapP.get(srcLoc);
		if(val == null) return;
		putStack(dst, val);
	}

	public BoundedSet<SymbolicAddress> getStack(int index) {
		if(this.isTop()) return bsFactory.top();
		Location loc = new Location(index);
		BoundedSet<SymbolicAddress> val = mapP.get(loc);
		if(val == null) {
			Logger.getLogger(this.getClass()).error("Undefined stack location: "+loc);
			for(Entry<Location, BoundedSet<SymbolicAddress>> entry : this.mapP.entrySet()) {
				System.err.println("  "+entry.getKey()+ " --> "+entry.getValue());
			}
//			throw new AssertionError("Undefined stack Location");
		}
		return val;
	}
	public void putStack(int index, BoundedSet<SymbolicAddress> bs) {
		this.put(new Location(index), bs);
	}
	public BoundedSet<SymbolicAddress> getHeap(String staticfield) {
		if(this.isTop()) return bsFactory.top();
		Location loc = new Location(staticfield);
		BoundedSet<SymbolicAddress> val = mapP.get(loc);
		if(val == null) {
			val = bsFactory.singleton(SymbolicAddress.rootAddress(staticfield));
		}
		return val;
	}
	public void putHeap(String staticfield, BoundedSet<SymbolicAddress> pointers) {
		this.put(new Location(staticfield), pointers);		
	}

	public void put(Location l, BoundedSet<SymbolicAddress> bs) {
		if(bs == null) {
			throw new AssertionError("put "+l+": null");
		}
		if(this.isTop()) return;
		this.mapP.put(l, bs);
	}

	public void addAlias(String ty, BoundedSet<SymbolicAddress> newAliases) {
		if(this.isTop()) return;
		BoundedSet<SymbolicAddress> oldAlias = this.mapA.get(ty);
		if(oldAlias == null) oldAlias = bsFactory.empty();
		// FIXME: Debugging
		if(newAliases == null) {
			Logger.getLogger("Object Cache Analysis").error("Undefined alias set for "+ty);
			return;
		}
		oldAlias.addAll(newAliases);
		mapA.put(ty, newAliases);
	}
	
	public BoundedSet<SymbolicAddress> getAliases(String fieldType) {
		if(this.isTop()) return bsFactory.top();
		BoundedSet<SymbolicAddress> aliases = this.mapA.get(fieldType);
		if(aliases == null) return bsFactory.empty();
		return aliases;
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
		out.println("SymbolicAddressMap ("+mapP.size()+")");
		indentstr.append(' ');
		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : mapP.entrySet()) {
			out.print(indentstr.toString());
			out.print(entry.getKey());
			out.print(": ");
			out.print(entry.getValue().getSize()+"<="+entry.getValue().getLimit());
			out.print(entry.getValue());
			out.print("\n");
		}
		for(Entry<String, BoundedSet<SymbolicAddress>> entry : mapA.entrySet()) {
			out.print(indentstr.toString());
			out.print(entry.getKey());
			out.print("~~> ");
			out.print(entry.getValue().getSize()+"<="+entry.getValue().getLimit());
			out.print(entry.getValue());
			out.print("\n");
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("SymbolicAddressMap");
		if(this.isTop()) {
			sb.append(".TOP");
		} else {
			sb.append("{ ");
			sb.append(mapP);
			sb.append(", ");
			sb.append(mapA);
			sb.append(" }");
		}
		return sb.toString();
	}
}
