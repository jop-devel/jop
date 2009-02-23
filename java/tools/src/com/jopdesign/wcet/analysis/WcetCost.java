/**
 * 
 */
package com.jopdesign.wcet.analysis;

import java.io.Serializable;

public class WcetCost implements Serializable {
	private static final long serialVersionUID = 1L;
	private long localCost = 0;
	private long cacheCost = 0;
	private long nonLocalCost = 0;
	private int potCacheFlushes = 0;
	public WcetCost() {
		
	}
	public static WcetCost totalCost(long wcet) {
		WcetCost cost = new WcetCost();
		cost.addNonLocalCost(wcet);
		return cost;
	}
	@Override
	public WcetCost clone() {
		WcetCost cCost = new WcetCost();
		cCost.addLocalCost(localCost);
		cCost.addNonLocalCost(nonLocalCost);
		cCost.addCacheCost(cacheCost);
		cCost.addPotentialCacheFlushes(this.potCacheFlushes);
		return cCost;
	}
	
	public void addCost(WcetCost cost) {
		this.localCost += cost.getLocalCost();
		this.nonLocalCost += cost.getNonLocalCost();
		this.cacheCost += cost.getCacheCost();
		this.potCacheFlushes += cost.getPotentialCacheFlushes();
	}


	public long getCost() { return localCost+cacheCost+nonLocalCost; }
	
	public long getLocalCost() { return localCost; }
	public long getCacheCost() { return cacheCost; }
	public long getNonLocalCost() { return nonLocalCost; }
	
	public void addLocalCost(long c) { this.localCost += c; }
	public void addNonLocalCost(long c) { this.nonLocalCost += c; }
	public void addCacheCost(long c) { this.cacheCost += c; }
	
	@Override public String toString() {
		if(getCost() == 0) return "0";
		String s;
		if(localCost == 0) s= "(cost: "+getCost()+", execution: "+nonLocalCost+", cache: "+cacheCost;
		else s = ""+getCost()+" (local: "+localCost+",cache: "+cacheCost+",non-local: "+nonLocalCost;
		if(this.potCacheFlushes > 0) s+= ", potentialCacheFlushes: "+potCacheFlushes;
		s+=")";
		return s;
	}
	public WcetCost getFlowCost(Long flow) {
		WcetCost flowcost = new WcetCost();
		if(this.getCost() == 0 || flow == 0) return flowcost;
		flowcost.localCost = localCost*flow;
		flowcost.cacheCost = cacheCost*flow;
		flowcost.nonLocalCost = nonLocalCost*flow;
		if(flowcost.getCost() / flow != getCost()) {
			throw new ArithmeticException("getFlowCost: Arithmetic Error");
		}
		return flowcost;
	}
	public void moveLocalToGlobalCost() {
		this.nonLocalCost += this.localCost;
		this.localCost = 0;
	}
	public long getNonCacheCost() {
		return this.localCost + this.nonLocalCost;
	}
	public void addPotentialCacheFlushes(int i) {
		potCacheFlushes  = i;		
	}
	public int getPotentialCacheFlushes() {
		return potCacheFlushes;
	}
}