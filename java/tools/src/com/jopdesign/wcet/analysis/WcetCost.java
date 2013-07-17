/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
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
package com.jopdesign.wcet.analysis;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.jopdesign.common.misc.Iterators;
import com.jopdesign.common.misc.MiscUtils;

public class WcetCost implements Serializable {
	private static final long serialVersionUID = 1L;
	private long localCost = 0;
	private Map<String, Long> cacheCost = new HashMap<String, Long>();
	private long totalCacheCost = 0;
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
		addCacheCost(cost.cacheCost);
		this.potCacheFlushes += cost.getPotentialCacheFlushes();
	}


	public long getCost() { return localCost+getCacheCost()+nonLocalCost; }
	
	public long getLocalCost() { return localCost; }
	public long getNonLocalCost() { return nonLocalCost; }
	public long getCacheCost() { 
		return Iterators.sum(cacheCost.values());
	}
	
	public void addLocalCost(long c) { this.localCost += c; }
	public void addNonLocalCost(long c) { this.nonLocalCost += c; }	
	public void addCacheCost(long unclassifiedCacheCost) { 
		addCacheCost("cache",unclassifiedCacheCost);
	}
	public void addCacheCost(String cache, long cost) {
		MiscUtils.incrementBy(cacheCost, cache, cost, 0);		
	}
	public void addCacheCost(Map<String,Long> cacheCost) { 
		for(Entry<String, Long> entry : cacheCost.entrySet()) {
			MiscUtils.incrementBy(this.cacheCost, entry.getKey(), entry.getValue(), 0);
		}
	}
	
	@Override public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(getCost());
		sb.append(" (");
		sb.append("local: ");
		sb.append(this.getLocalCost());
		for(Entry<String, Long> cacheEntry : this.cacheCost.entrySet()) {
			sb.append(", ");
			sb.append(cacheEntry.getKey());
			sb.append(": ");
			sb.append(cacheEntry.getValue());
		}
		if(this.getNonLocalCost() > 0) {
			sb.append(", non-local: ");
			sb.append(this.getNonLocalCost());
		}
		if(this.potCacheFlushes > 0) {
			sb.append(", potentialCacheFlushes: ");
			sb.append(potCacheFlushes);
		}
		sb.append(')');
		return sb.toString();
	}
	
	public WcetCost getFlowCost(Long flow) {
		WcetCost flowcost = new WcetCost();
		if(this.getCost() == 0 || flow == 0) return flowcost;
		flowcost.localCost = localCost*flow;
		for(Entry<String, Long> cacheEntry : cacheCost.entrySet()) {
			flowcost.addCacheCost(cacheEntry.getKey(), cacheEntry.getValue()*flow);			
		}
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