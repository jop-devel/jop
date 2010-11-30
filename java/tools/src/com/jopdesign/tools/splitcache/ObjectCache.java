/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

package com.jopdesign.tools.splitcache;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.jopdesign.tools.DataMemory;
import com.jopdesign.tools.Cache.ReplacementStrategy;
import com.jopdesign.tools.splitcache.DataCacheStats.StatTy;

/**
 * A generalized object cache (fully associative cache for handle+offset), which supports</br>
 * <ul>
 * <li/> Configurable Associativity (N) and line size (L, in words)
 * <li/> Configurable Burst Load (B, in words)
 * <li/> (Optionally) caching the handle indirection (needs to be invalidated on GC)
 * <li/> Clamp (do not cache high offsets) and Wrap Around Mode (for caching arrays)
 * <li/> Allocate on write
 * </ul>
 * An object cache is quite similar to a set-associative cache, but the tag is only
 * used to select the way, not the cache line (which depends on the field index)
 * In Bypass mode we only need one tag per way (which in this simulation is saved in
 * the first cache cache block) 
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class ObjectCache extends DataMemory {
	
		public enum FieldIndexMode { Bypass, Wrap };
		public enum BlockAccessResult { INVALID, HIT, MISS, BYPASS };
				
		/** Result buffer for object cache accesses */
		public class ObjectCacheLookupResult {
			private int buf;
			private boolean hitObject;
			private boolean reloadAddress;
			private BlockAccessResult blockAccessStatus;
			ObjectCacheLookupResult() {
				reset();
			}
			public void reset() {
				hitObject = false;
				reloadAddress = false;
				blockAccessStatus = BlockAccessResult.INVALID;
			}

			void setData(int buf) {
				this.buf = buf;
			}
			public boolean isObjectHit() { return hitObject; }
			public boolean isReloadAddress() { return reloadAddress; }
			public BlockAccessResult getBlockAccessStatus() { return blockAccessStatus; }
			public int getData() { return buf; }

		}
		
		/** Depending on the configuration, the meta data block saves
		 *  <ul><li/> object valid bit and handle
		 *      <li/> handle indirection
		 *      <li/>array length ?
		 *  </ul>
		 */
		public class ObjectCacheEntry {
			private int handle;
			private int objectAddress;
			private int objectMVBorALen;
			private boolean valid, validMetaData;
			private CacheBlock[] cacheBlocks;
			private boolean isArray;

			public ObjectCacheEntry() {
				valid = false;
				validMetaData = false;
				cacheBlocks = new CacheBlock[blocksPerObject];
				for(int i = 0; i < blocksPerObject; i++)
					cacheBlocks[i] = new CacheBlock(wordsPerBlock);
			}
			
			public void initializeForObject(int handle) {
				this.handle = handle;
				this.valid = true;
				this.invalidateMetaData();
			}
			
			public void initializeForArray(int handle) {
				this.valid = true;
				this.handle = handle;
				this.isArray = true;
				this.invalidateMetaData();
			}

			public void invalidate() {
				this.valid = false;
			}

			public void invalidateMetaData() {
				this.validMetaData = false;
			}
			
			public boolean hasValidAddress() {
				return this.validMetaData;
			}
			/** load address and MVB */
			public void loadObjectMetaData() {
				this.objectAddress   = nextLevelMemory.read(handle, Access.HANDLE);
				this.objectMVBorALen = nextLevelMemory.read(handle+1, isArray ? Access.ALEN : Access.MVB);
			}
			
			public void loadBlock(int blockIndex) {
				assert(valid);
				assert(validMetaData);
				int offset = blockIndex * wordsPerBlock;
				cacheBlocks[blockIndex].load(blockIndex, objectAddress + offset, nextLevelMemory);
			}

			public CacheBlock getBlock(int blockIndex) {
				return cacheBlocks[blockIndex];
			}

			public int getMVB() {
				if(this.isArray) throw new AssertionError("getMVB(): Array, not Object");
				return this.objectMVBorALen;
			}

			public int getArrayLength() {
				if(! this.isArray) throw new AssertionError("getArrayLength(): Not an Array");
				return this.objectMVBorALen;
			}

			public void modifyData(int blockIndex, int blockOffset, int data) {
				cacheBlocks[blockIndex].modifyData(blockOffset, data);
			}

			@Override
			public String toString() {
				if(! this.valid) return "OCE#invalid";
				StringBuffer s = new StringBuffer("OCE{ h=");
				s.append(this.handle);
				s.append(", base=");
				s.append(this.objectAddress);
				s.append(" }");
				return s.toString();
			}
			/** Update the value at the given offset, if it is valid */
			public void updateIfValid(int offset, int data) {
				CacheBlock block = this.getBlock(getBlockIndex(offset));
				if(block.isValid()) {
					block.modifyData(getBlockOffset(offset), data);
				}
			}

		}
		
		protected int ways;
		protected int blocksPerObject;
		protected int wordsPerBlock;
		private int blockBits;

		private FieldIndexMode fieldIndexMode;
		private boolean allocateOnWrite;
		private ReplacementStrategy replacement;

		private DataMemory handleMemory;
		protected DataMemory nextLevelMemory;

		private ObjectCacheEntry[] objectEntry;

		private DataCacheStats stats, objectStats;
		private Set<Access> handledAccessTypes;
				
		/**
		 * Build a new object cache
		 * @param ways Associativity
		 * @param blocksPerObject cache blocks for each object
		 * @param wordsPerBlock block size
		 * @param nextLevelMemory
		 * @param handledTypes 
		 */
		public ObjectCache(int ways, int blocksPerObject, int wordsPerBlock, FieldIndexMode mode, ReplacementStrategy replacement,
						   boolean allocateOnWrite, DataMemory handleMemory, DataMemory nextLevelMemory, Access[] handledTypes) {
			
			this.ways = ways;
			this.blocksPerObject = blocksPerObject;
			this.wordsPerBlock = wordsPerBlock;
			this.fieldIndexMode = mode;
			this.replacement = replacement;
			this.allocateOnWrite = allocateOnWrite;
			this.handledAccessTypes = new HashSet<Access>(Arrays.asList(handledTypes));
			blockBits = 0;
			for(int w = wordsPerBlock - 1; w > 0; w>>=1) {
				blockBits++;
			}
			
			objectEntry = new ObjectCacheEntry[ways];
			for(int i = 0; i < ways; i++) {
				objectEntry[i] = new ObjectCacheEntry();
			}

			this.handleMemory = handleMemory; /* next level handle memory */
			this.nextLevelMemory = nextLevelMemory;
			objectStats = new DataCacheStats(getName()+"-objs");
			stats = new DataCacheStats(getName()+"-fields");
		}
						
		@Override
		public void  invalidateCache() {
			for(int i = 0; i < ways; i++) {
				objectEntry[i].invalidate();
			}
			this.handleMemory.invalidateHandles();
			for(ObjectCacheEntry entry: this.objectEntry) {
				entry.invalidateMetaData();
			}
			nextLevelMemory.invalidateData();

			objectStats.invalidate();
			stats.invalidate();
		}

		@Override
		public void  invalidateData() {			
			for(int i = 0; i < ways; i++) {
				objectEntry[i].invalidate();
			}
			objectStats.invalidate();
			stats.invalidate();
			nextLevelMemory.invalidateData();
		}
		
		@Override
		public void invalidateHandles() {
			this.handleMemory.invalidateHandles();
			objectStats.invalidate();
			for(ObjectCacheEntry entry: this.objectEntry) {
				entry.invalidateMetaData();
			}
		}

		@Override
		public int read(int addr, Access type) {
			if(type == Access.MVB || type == Access.ALEN) {
				return readMetaData(addr-1, 1, type);
			}
			throw new AssertionError("Attempt to to a plain " + type +
					                 " read from object cache at address " + type);
		}
		
		@Override
		public int readField(int handle, int fieldOffset, Access type) {			
			ObjectCacheLookupResult r = new ObjectCacheLookupResult();
			readFieldInto(handle,fieldOffset,r);
			objectStats.read(r.hitObject);
			return r.getData();
		}
		
		private int readMetaData(int handle, int metaOffset, Access type) {
			ObjectCacheLookupResult r = new ObjectCacheLookupResult();
			ObjectCacheEntry block = getOrLoadObjectEntry(handle, r);
			int rval = 0;
			switch(metaOffset) {
			case 0: resolveObjectAddress(block, r);
					rval = block.objectAddress;
					break;
			case 1: rval = block.objectMVBorALen;
					break;
			default: throw new AssertionError("Bad object metadata offset: " + metaOffset);
			}
			objectStats.read(r.hitObject);
			return rval;
		}
		
		public void readFieldInto(int handle, int offset, ObjectCacheLookupResult r) {
			r.reset();
			
			ObjectCacheEntry block = getOrLoadObjectEntry(handle, r);
			if(fieldIndexMode == FieldIndexMode.Bypass && offset >= wordsPerObject()) {
				r.blockAccessStatus = BlockAccessResult.BYPASS;
				readBypassed(block, offset, r);	
				stats.readBypassed();
			} else {
				readCacheBlock(block, offset, r);				
				stats.read(r.getBlockAccessStatus() == BlockAccessResult.HIT);
			}			
			// TODO: record more fine grained stats
		}

		@Override
		public void write(int address, int data, Access type) {
			throw new AssertionError("attempt to perform direct " + type 
					                 + " write in object cache at address "+address); 
		}

		// TODO: use ObjectCacheLookupResult
		@Override
		public void writeField(int handle, int offset, int data, Access type) {
			// Write through
			nextLevelMemory.writeField(handle, offset, data, type);
			if(allocateOnWrite) {
				getOrLoadObjectEntry(handle, null).modifyData(getBlockIndex(offset), getBlockOffset(offset), data);
			} else {
				if(fieldIndexMode == FieldIndexMode.Wrap || offset < wordsPerObject()) {
					// we need to update the entry if it is in the cache
					// FIXME: probably still broken for wrap-around, but works now for bypassing
					int way = lookupObject(handle);
					objectStats.read(isValidWay(way));
					if(! isValidWay(way)) return;
					getObjectCacheEntry(way).updateIfValid(offset, data);
					stats.write();
				}
			}
		}

		public int getAddress(int oid, int field) {
			return resolveHandle(oid) + field;
		}

		public int getSize() {
			return ways*blocksPerObject*wordsPerBlock;
		}
		
		/** Get entry for an object
		 *  If object is already in the cache, return the corresponding ObjectCacheEntry.
		 *  Otherwise, allocate a new ObjectCacheEntry (but do not resolve the handle indirection at this point)
		 * @return
		 */
		private ObjectCacheEntry getOrLoadObjectEntry(int handle, ObjectCacheLookupResult r) {
			ObjectCacheEntry ob;
			int way = lookupObject(handle);
			if(r != null) r.hitObject = isValidWay(way);
			if(! isValidWay(way)) {
				ob = new ObjectCacheEntry();
				ob.initializeForObject(handle);
				ob.loadObjectMetaData();
			} else {
				ob = getObjectCacheEntry(way);
			}
			updateCache(way, ob);
			return ob;
		}
		
		private int lookupObject(int handle) {
			for(int way = 0; way < ways; way++) {
				if(objectEntry[way].valid && objectEntry[way].handle == handle) return way;
			}
			return -1;
		}

		private int resolveHandle(int handle) {
			return this.handleMemory.read(handle,Access.HANDLE);
		}

		private void resolveObjectAddress(ObjectCacheEntry block, ObjectCacheLookupResult r) {
			if(! block.hasValidAddress()) {
				block.objectAddress = this.handleMemory.read(block.handle, Access.HANDLE);
				r.reloadAddress = true;
			}
		}

		/**
		 * Get the object cache entry at the given index
		 * @param way the index of the object in the cache
		 * @return
		 */
		private ObjectCacheEntry getObjectCacheEntry(int way) {
			if(! isValidWay(way)) throw new AssertionError("getObjectCacheEntry: invalid index");
			return this.objectEntry[way];
		}
		
		private void readBypassed(ObjectCacheEntry block, int offset, ObjectCacheLookupResult r) {
			// Need to resolve address in case handle cache has been invalidated
			resolveObjectAddress(block, r);
			r.buf = nextLevelMemory.read(block.objectAddress + offset, Access.FIELD); 
			r.blockAccessStatus = BlockAccessResult.BYPASS;
		}
		
		private void readCacheBlock(ObjectCacheEntry block, int fieldOffset, ObjectCacheLookupResult r) {			
			int blockIndex = getBlockIndex(fieldOffset);
			boolean valid = block.cacheBlocks[blockIndex].isValid();
			if(fieldIndexMode == FieldIndexMode.Wrap) {
				valid = valid && block.cacheBlocks[blockIndex].getTag() == blockIndex;
			}
			if(! valid) {
				r.blockAccessStatus = BlockAccessResult.MISS;
				resolveObjectAddress(block, r); // Need to resolve address in case handle cache has been invalidated
				block.loadBlock(blockIndex);
			} else {
				r.blockAccessStatus = BlockAccessResult.HIT;				
			}
			r.buf = block.getBlock(blockIndex).getData(getBlockOffset(fieldOffset));
		}

		
		private boolean isValidWay(int way) {
			return 0 <= way && way < ways;
		}

		private int wordsPerObject() {
			return blocksPerObject * wordsPerBlock;
		}

		private int getBlockIndex(int fieldOffset) {
			return (fieldOffset >> blockBits) % blocksPerObject;
		}

		private int getBlockOffset(int fieldOffset) {
			return fieldOffset % wordsPerBlock;
		}
		
		/** Simulate an access to the object cache entry in the specified 'way' table */
		private void updateCache(int way, ObjectCacheEntry ob) {
			switch(replacement) {
			case FIFO: 			replaceFifo(objectEntry,  ob, way, ways);
			case LRU:			replaceLRU(objectEntry,  ob, way, ways);
			}
		}		

		// stats + debug

		private Stack<DataCacheStats> recordedStats = new Stack<DataCacheStats>();
		private Stack<DataCacheStats> recordedObjStats = new Stack<DataCacheStats>();
		@Override
		public void resetStats() {
			this.stats.reset();
			this.objectStats.reset();
		}
		@Override 
		public void recordStats() {
			recordedStats.push(stats.clone());
			recordedObjStats.push(objectStats.clone());
		}

		@Override
		public String getName() {
			return String.format("O$-%d-%d-%d-%s%s%s",ways,blocksPerObject,wordsPerBlock,
					this.replacement.toString(),
					fieldIndexMode == FieldIndexMode.Wrap ? "-wrap" : "",
					allocateOnWrite ? "-aow" : "");
		}

		@Override
		public String toString() {
			return String.format("Object Cache{ ways=%d, wayblocks=%d, blockwords=%d, "+
					             "indexmode=%s, replacement=%s, allocateOnWrite=%s, handled=%s }",
					             this.ways, this.blocksPerObject, this.wordsPerBlock,
					             this.fieldIndexMode.toString(), this.replacement.toString(),
					             ""+ this.allocateOnWrite, this.handledAccessTypes.toString());
		}

		@Override
		public void dump(PrintStream out) {
			SplitCacheSim.printHeader(out,toString());
			DataCacheStats objStats = new DataCacheStats(getName()+"-objs").addAverage(this.recordedObjStats);
			DataCacheStats fieldStats = new DataCacheStats(getName()+"-field").addAverage(this.recordedStats);			
			objStats.dump(out);
			fieldStats.dump(out);

			long totalAccesses = objStats.get(StatTy.ReadCount);
			long cmcSRAM = cmc(objStats,fieldStats,0,2);
			long cmcSDRAM = cmc(objStats,fieldStats,10,2);
			out.println("Cache Miss Cycles (SRAM): "+cmcSRAM+" / "+totalAccesses+" = "+
					    (cmcSRAM / (double)objStats.get(StatTy.ReadCount)));
			out.println("Cache Miss Cycles (SDRAM): "+cmcSDRAM+" / "+totalAccesses+" = "+
				    (cmcSDRAM / (double)objStats.get(StatTy.ReadCount)));
		}
		
		// The beautiful power of arithmetic presents:
		// Cache Miss Cycles for the paper
		private long cmc(DataCacheStats objStats, DataCacheStats fieldStats, int delay, int cyclesPerWord) {
			long handleMisses = objStats.get(StatTy.ReadCount) - objStats.get(StatTy.HitCount);
			long fieldMissesOrBypasses = fieldStats.get(StatTy.ReadCount) - fieldStats.get(StatTy.HitCount); 
			//		let cmc or oh fr fh = ((or-oh) * 14.0 + (fr - fh) * 18) / or
			return (handleMisses * (delay + 2*cyclesPerWord) + 
				    fieldMissesOrBypasses * (delay + wordsPerBlock*cyclesPerWord));
		}
		
		public static <T> void replaceLRU(T[] data, T obj, int oldPosition, int ways) {
			if(oldPosition < 0 || oldPosition > ways) oldPosition = ways;

			T saved = obj;
			for(int i = 0; i < ways && i <= oldPosition; i++) {
				T next = data[i];
				data[i] = saved;
				saved = next;
			}
		}
		
		public static<T> void replaceFifo(T[] data, T obj, int oldPosition, int ways) {
			if(0 <= oldPosition && oldPosition < ways) return; /* No action with FIFO if object was in the cache */
			T saved = obj;
			for(int i = 0; i < ways; i++) {
				T next = data[i];
				data[i] = saved;
				saved = next;
			}
		}


}
