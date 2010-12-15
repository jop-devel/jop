/**
 * 
 */
package com.jopdesign.wcet.analysis.cache;

public class ObjectCacheCost {
	private long missCost;
	private long bypassCost;
	private long fieldAccesses;
	private long bypassAccesses;
	private long missCount;

	/**
	 * @param missCost2
	 * @param bypassCost2
	 * @param fieldAccesses2
	 */
	public ObjectCacheCost(long missCount, long missCost, long bypassAccesses, long bypassCost, long fieldAccesses) {
		this.missCost = missCost;
		this.bypassCost = bypassCost;
		this.fieldAccesses = fieldAccesses;
		this.missCount = missCount;
		this.bypassAccesses = bypassAccesses;
	}

	public ObjectCacheCost() {
		this(0,0,0,0,0);
	}

	public long getCost()
	{
		return missCost + bypassCost;
	}
	
	public long getBypassCost() { return bypassCost; }
	public long getBypassAccesses() { return this.bypassAccesses; }
	
	public void addBypassCost(long bypassCost, int accesses) {
		this.bypassCost += bypassCost;
		this.bypassAccesses += accesses;			
	}

	public ObjectCacheCost addMissCost(long missCost, int missCount) {
		this.missCost += missCost;
		this.missCount += missCount;
		return this;
	}
	
	/* addition field accesses either hit or miss (but not bypass) */
	public void addAccessToCachedField(long additionalFAs) {
		fieldAccesses += additionalFAs;
	}

	public long getTotalFieldAccesses()
	{
		return bypassAccesses + fieldAccesses;
	}
	
	public long getFieldAccessesWithoutBypass()
	{
		return fieldAccesses;
	}
	/* cache miss count */
	public long getCacheMissCount() {
		return missCount;
	}

	public void addCost(ObjectCacheCost occ) {
		this.missCount += occ.missCount;
		this.missCost += occ.missCost;
		this.bypassAccesses += occ.bypassAccesses;
		this.bypassCost += occ.bypassCost;
		addAccessToCachedField(occ.fieldAccesses);
	}
	
	public String toString() {
		return String.format("%d [miss=%d,bypass=%d,accesses=%d]",getCost(),this.missCost,this.bypassCost,this.fieldAccesses);
	}

	public ObjectCacheCost times(Long value) {
		return new ObjectCacheCost(missCount * value, missCost * value,
				                   bypassAccesses * value, bypassCost * value,
				                   fieldAccesses * value);
	}

}