package com.jopdesign.dfa.analyses;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import com.jopdesign.dfa.framework.BoundedSetFactory;
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
		new SymbolicAddressMap(new BoundedSetFactory(0), (HashMap)null, (HashMap) null);

	private BoundedSetFactory<SymbolicAddress> bsFactory;

	/* Invariant: obj.map == null iff obj == TOP */
	private Map<Location, BoundedSet<SymbolicAddress>> mapPointsTo;
	private HashMap<String, BoundedSet<SymbolicAddress>> mapFieldAliases;

	public boolean isTop()
	{
		return(this.mapPointsTo == null);
	}
	private void setTop()
	{
		this.mapPointsTo = null;
		this.mapFieldAliases = null;
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
							   HashMap<Location,BoundedSet<SymbolicAddress>> initP,
							   HashMap<String,BoundedSet<SymbolicAddress>> initA) {
		this.bsFactory = bsFactory;
		this.mapPointsTo = initP;
		this.mapFieldAliases = initA;
	}

	@Override
	public int hashCode() {
		if(isTop()) return 1;
		else return 2 + 31 * mapPointsTo.hashCode() + mapFieldAliases.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SymbolicAddressMap other = (SymbolicAddressMap) obj;
		if(this.isTop() || other.isTop()) return (this.isTop() && other.isTop());
		return mapPointsTo.equals(other.mapPointsTo) && mapFieldAliases.equals(other.mapFieldAliases);
	}

	public boolean isSubset(SymbolicAddressMap other) {
		if(other.isTop())       return true;
		else if(this.isTop())   return false;
		else if(other == null)  return false;
		/* Neither is \bot or \top -> pointwise subseteq */
		for(Location l : this.mapPointsTo.keySet()) {
			BoundedSet<SymbolicAddress> thisEntry = mapPointsTo.get(l);
			BoundedSet<SymbolicAddress> otherEntry = other.mapPointsTo.get(l);
			if(otherEntry == null) return false;
			if(! thisEntry.isSubset(otherEntry)) return false;
		}
		for(String l : this.mapFieldAliases.keySet()) {
			BoundedSet<SymbolicAddress> thisEntry = mapFieldAliases.get(l);
			BoundedSet<SymbolicAddress> otherEntry = other.mapFieldAliases.get(l);
			if(otherEntry == null) return false;
			if(! thisEntry.isSubset(otherEntry)) return false;
		}
		return true;
	}
	
	@Override
	protected SymbolicAddressMap clone() {
		if(this.isTop()) return this;
		return new SymbolicAddressMap(this.bsFactory,
				new HashMap<Location, BoundedSet<SymbolicAddress>>(this.mapPointsTo),
				new HashMap<String, BoundedSet<SymbolicAddress>>(this.mapFieldAliases));
	}
	
	/** Clone address map, but only those stack variables below {@code bound} */
	public SymbolicAddressMap cloneWithStackPtr(int bound) {
		if(this.isTop()) return this;
		SymbolicAddressMap copy = new SymbolicAddressMap(this.bsFactory);
		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : mapPointsTo.entrySet()) {
			Location loc = entry.getKey();
			if(loc.isHeapLoc() || loc.stackLoc < bound) {
				copy.put(loc, entry.getValue());
			}
		}
		copy.mapFieldAliases = new HashMap<String, BoundedSet<SymbolicAddress>>(this.mapFieldAliases);
		return copy;
	}
	
	/** Clone address map, but only those stack variables with index greater than or equal to
	 *  {@code framePtr}. The stack variables are moved down to the beginning of the stack. */
	public SymbolicAddressMap cloneInvoke(int framePtr) {
		if(this.isTop()) return this;
		SymbolicAddressMap copy = new SymbolicAddressMap(this.bsFactory);
		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : mapPointsTo.entrySet()) {
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
		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : in.mapPointsTo.entrySet()) {
			Location loc = entry.getKey();
			if(! loc.isHeapLoc() && loc.stackLoc < bound) {
				mapPointsTo.put(loc, in.getStack(loc.stackLoc));
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

		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : returned.mapPointsTo.entrySet()) {
			Location locReturnedFrame = entry.getKey();
			Location locCallerFrame; 
			if(locReturnedFrame.isHeapLoc()) {
				locCallerFrame = locReturnedFrame;
			} else {
				locCallerFrame = new Location(locReturnedFrame.stackLoc + framePtr); 
			}
			BoundedSet<SymbolicAddress> callerSet = mapPointsTo.get(locCallerFrame);
			BoundedSet<SymbolicAddress> returnedSet = returned.mapPointsTo.get(locReturnedFrame);
			put(locCallerFrame, returnedSet.join(callerSet));
		}		
	}
	
	public void copyStack(SymbolicAddressMap in, int dst, int src) {
		if(in.isTop()) return;
		if(this.isTop()) return;
		Location srcLoc = new Location(src);
		BoundedSet<SymbolicAddress> val = in.mapPointsTo.get(srcLoc);
		if(val == null) return;
		putStack(dst, val);
	}

	public BoundedSet<SymbolicAddress> getStack(int index) {
		if(this.isTop()) return bsFactory.top();
		Location loc = new Location(index);
		BoundedSet<SymbolicAddress> val = mapPointsTo.get(loc);
		if(val == null) {
			Logger.getLogger(this.getClass()).error("Undefined stack location: "+loc);
			for(Entry<Location, BoundedSet<SymbolicAddress>> entry : this.mapPointsTo.entrySet()) {
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
		BoundedSet<SymbolicAddress> val = mapPointsTo.get(loc);
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
		this.mapPointsTo.put(l, bs);
	}

	public void addFieldAlias(String fieldName, BoundedSet<SymbolicAddress> newRefs) {
		if(this.isTop()) return;
		BoundedSet<SymbolicAddress> oldAlias = this.mapFieldAliases.get(fieldName);
		if(oldAlias == null) oldAlias = bsFactory.empty();
		// FIXME: Debugging
		if(newRefs == null) {
			Logger.getLogger("Object Cache Analysis").error("Undefined alias set for "+fieldName);
			return;
		}
		oldAlias.addAll(newRefs);
		mapFieldAliases.put(fieldName, oldAlias);
	}
	
	public void addArrayAlias(Type elementType, BoundedSet<SymbolicAddress> newRefs) {
		addFieldAlias(elementType.getSignature()+"[]", newRefs);
	}

	private BoundedSet<SymbolicAddress> getAliases(String fieldType) {
		if(this.isTop()) return bsFactory.top();
		BoundedSet<SymbolicAddress> aliases = this.mapFieldAliases.get(fieldType);
		if(aliases == null) return bsFactory.empty();
		return aliases;
	}
	
	public BoundedSet<SymbolicAddress> getArrayAliases(Type type) {
		return getAliases(type.getSignature() + "[]");
	}

	public BoundedSet<SymbolicAddress> getFieldAliases(String fieldName) {
		return getAliases(fieldName);
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
		out.println("SymbolicAddressMap ("+mapPointsTo.size()+")");
		indentstr.append(' ');
		for(Entry<Location, BoundedSet<SymbolicAddress>> entry : mapPointsTo.entrySet()) {
			out.print(indentstr.toString());
			out.print(entry.getKey());
			out.print(": ");
			out.print(entry.getValue().getSize()+"<="+entry.getValue().getLimit());
			out.print(entry.getValue());
			out.print("\n");
		}
		for(Entry<String, BoundedSet<SymbolicAddress>> entry : mapFieldAliases.entrySet()) {
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
			sb.append(mapPointsTo);
			sb.append(", ");
			sb.append(mapFieldAliases);
			sb.append(" }");
		}
		return sb.toString();
	}
}
