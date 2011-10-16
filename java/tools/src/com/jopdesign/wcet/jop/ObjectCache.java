/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

package com.jopdesign.wcet.jop;

import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.processormodel.JOPConfig;
import com.jopdesign.timing.jop.JOPTimingTable;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.cache.ObjectCacheAnalysis.ObjectCacheCostModel;

/**
 * Purpose:
 *
 */
public class ObjectCache implements CacheModel {

	public static class ObjectCacheCost {
		private long missCost;
		private long bypassCost;
		private long fieldAccesses;
		private long bypassCount;
		private long missCount;
	
		/**
		 * @param missCost
		 * @param bypassCost
		 * @param fieldAccesses
		 */
		public ObjectCacheCost(long missCount, long missCost, long bypassAccesses, long bypassCost, long fieldAccesses) {
			this.missCost = missCost;
			this.bypassCost = bypassCost;
			this.fieldAccesses = fieldAccesses;
			this.missCount = missCount;
			this.bypassCount = bypassAccesses;
		}
	
		public ObjectCacheCost() {
			this(0,0,0,0,0);
		}
	
		public long getCost()
		{
			return missCost + bypassCost;
		}
		
		public long getBypassCost() { return bypassCost; }
		public long getBypassCount() { return this.bypassCount; }
		
		public void addBypassCost(long bypassCost, int accesses) {
			this.bypassCost += bypassCost;
			this.bypassCount += accesses;			
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
			return bypassCount + fieldAccesses;
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
			this.bypassCount += occ.bypassCount;
			this.bypassCost += occ.bypassCost;
			addAccessToCachedField(occ.fieldAccesses);
		}
		
		@Override
		public String toString() {
			return String.format("missCycles = %d [miss-cost=%d, bypass-cost = %d, relevant-accesses=%d]",getCost(),this.missCost,this.bypassCost,this.fieldAccesses);
		}
	
		public ObjectCacheCost times(Long value) {
			return new ObjectCacheCost(missCount * value, missCost * value,
					                   bypassCount * value, bypassCost * value,
					                   fieldAccesses * value);
		}
	
	}

	private int associativity;
	private int blockSize;
	private boolean fieldAsTag;
	private int lineSize;
	private long hitCycles;
	private long loadFieldCycles;
	private long loadBlockCycles;

	/**
	 * @param p
	 * @param timing
	 */
	public ObjectCache(WCETTool p, JOPTimingTable timing) {
		
		OptionGroup options = JOPConfig.getOptions(p.getConfig());
        this.associativity = options.getOption(JOPConfig.OBJECT_CACHE_ASSOCIATIVITY).intValue();
        this.blockSize = options.getOption(JOPConfig.OBJECT_CACHE_BLOCK_SIZE).intValue();
        this.fieldAsTag = false;
        this.lineSize = options.getOption(JOPConfig.OBJECT_CACHE_WORDS_PER_LINE).intValue();

        this.hitCycles = options.getOption(JOPConfig.OBJECT_CACHE_HIT_CYCLES);
        this.loadFieldCycles = options.getOption(JOPConfig.OBJECT_CACHE_LOAD_FIELD_CYCLES);
        this.loadBlockCycles = options.getOption(JOPConfig.OBJECT_CACHE_LOAD_BLOCK_CYCLES);
	}

	public ObjectCache(WCETTool p, int associativity, int blockSize, int lineSize,
			long hitCycles, long loadFieldCycles, long loadBlockCycles) {
		
        this.associativity = associativity;
        this.blockSize = blockSize;
        this.fieldAsTag = false;
        this.lineSize = lineSize;

        this.hitCycles = hitCycles;
        this.loadFieldCycles = loadFieldCycles;
        this.loadBlockCycles = loadFieldCycles;
	}

	public static ObjectCache createFieldCache(WCETTool p, int ways,
			long hitCycles, long loadFieldCycles, long loadBlockCycles) {

		ObjectCache oc = new ObjectCache(p, ways, 1, 1, hitCycles, loadFieldCycles, loadBlockCycles);
		oc.fieldAsTag = true;
		return oc;
	}

	/* (non-Javadoc)
	 * @see com.jopdesign.wcet.jop.CacheModel#getSizeInWords()
	 */
	@Override
	public long getSizeInWords() {

		/* TODO: take metadata into account */
		if(fieldAsTag) return associativity;
		return lineSize * associativity;
	}

	/**
	 * @return
	 */
	public int getMaxCachedFieldIndex() {
		
		if(fieldAsTag) return Integer.MAX_VALUE;
		return lineSize - 1;
	}

    public boolean isFieldCache() {
        return fieldAsTag;
    }

    
    public int getAssociativity() {
        return this.associativity;
    }

    public int getLineSize() {
        return lineSize;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getBlocksPerLine() {
    	return lineSize / blockSize;
    }

    public long getHitCycles() {
        return hitCycles;
    }

    public long getLoadBlockCycles() {
        return this.loadBlockCycles;
    }

    public long getBypassTime() {
        return this.loadFieldCycles;
    }
    
    /* preliminary cost model */
	public ObjectCacheCostModel getCostModel() {

		return new ObjectCacheCostModel(
				Math.max(0, getLoadBlockCycles() - getHitCycles()), 0,  
				Math.max(0, getBypassTime() - getHitCycles()));
	}

 
	/* for evaluation purposes only */
    public void setHitCycles(long hitCycles) {
        this.hitCycles = hitCycles;
    }

    public void setLoadFieldCycles(long loadFieldCycles) {
        this.loadFieldCycles = loadFieldCycles;
    }

    public void setLoadBlockCycles(long loadBlockCycles) {
        this.loadBlockCycles = loadBlockCycles;
    }

    @Override
    public String toString() {
    	if(this.isFieldCache()) {
        	return "f$-N"+getAssociativity();
    	} else {
        	return "o$-N"+getAssociativity()+"-"+getBlocksPerLine()+"x"+getBlockSize();
    	}
    }
    /* Removed for now, as is not flexible enough */
//	public long getAccessTime(int words) {
//		int  burstLength = this.MaxBurst;
//		long delay       = this.AccessDelay;
//		long cyclesPerWord = this.CyclesPerWord;
//		int fullBursts = words / burstLength;
//		int lastBurst  = words % burstLength;
//		long accessTime = delay + cyclesPerWord * lastBurst;
//		for(int i = 0; i < fullBursts; i++) {
//			accessTime += delay + cyclesPerWord * burstLength;
//		}
//		return accessTime;
//	}


}
