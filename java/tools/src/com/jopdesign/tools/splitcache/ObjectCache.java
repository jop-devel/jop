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


/**
 * A generalized object cache (fully associative cache for handle+offset), which supports</br>
 * <ul>
 * <li/> Configurable Associativity (N) and line size (L, in words)
 * <li/> Configurable Burst Load (B, in words)
 * <li/> (Optionally) caching the handle indirection (needs to be invalidated on GC)
 * <li/> Clamp (do not cache high offsets) and Wrap Around Mode (for caching arrays)
 * </ul>
 * An object cache is quite similar to a set-associative cache, but the tag is only
 * used to select the way, not the cache line (which depends on the field index)
 * In Bypass mode we only need one tag per way (which in this simulation is saved in
 * the first cache cache block) 
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class ObjectCache {
	
		public enum FieldIndexMode { Bypass, Wrap };
		
		protected int ways;
		protected int blocksPerObject;
		protected int wordsPerBlock;

		private ObjectCacheEntry[] cacheData;

		protected CacheStats stats;
		private Cache nextLevelHandleCache;
		protected Cache nextLevelCache;
		private int blockBits;
		private FieldIndexMode fieldIndexMode;
		private boolean cacheHandleIndirection;
		
		public int getSize() {
			return ways*blocksPerObject*wordsPerBlock;
		}
		
		/** Depending on the configuration, the meta data block saves
		 * - object valid bit and handle
		 * - handle indirection
		 * - array length ?
		 */
		public class ObjectCacheEntry {
			private int arrayLength;
			private int objectAddress;
			private int handle;
			private boolean valid;
			private CacheBlock[] cacheBlocks;

			public ObjectCacheEntry() {
				valid = false;
				cacheBlocks = new CacheBlock[blocksPerObject];
				for(int i = 0; i < blocksPerObject; i++)
					cacheBlocks[i] = new CacheBlock(wordsPerBlock);
			}
			
			public void loadObject(int handle, int objectAddress) {
				this.valid = true;
				this.handle = handle;
				this.objectAddress = objectAddress;
			}
			
			public void loadArray(int handle, int objectAddress, int arrayLen) {
				this.valid = true;
				this.handle = handle;
				this.objectAddress = objectAddress;
				this.arrayLength = arrayLen;
			}

			public void invalidate() {
				this.valid = false;
			}

			public void loadBlock(int blockIndex) {
				int offset = blockIndex * wordsPerBlock;
				cacheBlocks[blockIndex].load(blockIndex, objectAddress + offset, nextLevelCache);
			}

			public CacheBlock getBlock(int blockIndex) {
				return cacheBlocks[blockIndex];
			}

			public int getArrayLength() {
				return this.arrayLength;
			}
		}
		/**
		 * Build a new object cache
		 * @param ways Associativity
		 * @param blocksPerObject cache blocks for each object
		 * @param wordsPerBlock block size
		 * @param nextLevelCache
		 */
		public ObjectCache(int ways, int blocksPerObject, int wordsPerBlock, FieldIndexMode mode, boolean cacheHandleIndirection,
				           Cache handleCache, Cache nextLevelCache) {
			
			this.ways = ways;
			this.blocksPerObject = blocksPerObject;
			this.wordsPerBlock = wordsPerBlock;
			this.fieldIndexMode = mode;
			
			blockBits = 0;
			for(int w = wordsPerBlock - 1; w > 0; w>>=1) {
				blockBits++;
			}
			
			cacheData = new ObjectCacheEntry[ways];
			for(int i = 0; i < ways; i++) {
				cacheData[i] = new ObjectCacheEntry();
			}
			this.cacheHandleIndirection = cacheHandleIndirection;
			this.nextLevelHandleCache = handleCache; /* handle cache or handle memory */
			this.nextLevelCache = nextLevelCache;
			stats = new CacheStats();
		}
				
		public void invalidate() {			
			for(int i = 0; i < ways; i++) {
				cacheData[i].invalidate();
			}
			stats.invalidate();
		}
					
		/** Read field from the given handle */
		public int read(int handle, int offset) {
			ObjectCacheEntry block = getOrLoadObject(handle);
			if(fieldIndexMode == FieldIndexMode.Bypass && offset >= wordsPerObject()) {
				return readBypassed(block, offset);				
			} else {
				return readCacheBlock(block, offset);				
			}
		}
		
		private ObjectCacheEntry getOrLoadObject(int handle) {
			ObjectCacheEntry ob;
			int way = lookupObject(handle);
			if(! isValidWay(way)) {
				ob = new ObjectCacheEntry();
				ob.loadObject(handle, resolveHandle(handle));
			} else {
				ob = getObjectBlock(way);
			}
			updateCache(way, ob);
			return ob;
		}
		
		private int lookupObject(int handle) {
			for(int way = 0; way < ways; way++) {
				if(cacheData[way].valid && cacheData[way].handle == handle) return way;
			}
			return ways;
		}

		private int resolveHandle(int handle) {
			return this.nextLevelHandleCache.read(handle);
		}

		private ObjectCacheEntry getObjectBlock(int way) {
			return this.cacheData[way];
		}
		
		public int readBypassed(ObjectCacheEntry block, int offset) {
			return nextLevelCache.read(block.objectAddress + offset);
		}
		
		public int readCacheBlock(ObjectCacheEntry block, int offset) {
			int blockIndex = (offset >> blockBits) % blocksPerObject;
			boolean valid = block.cacheBlocks[blockIndex].isValid();
			if(fieldIndexMode == FieldIndexMode.Wrap) {
				valid = valid && block.cacheBlocks[blockIndex].getTag() == blockIndex;
			}
			stats.read(valid);
			if(! valid) {
				block.loadBlock(blockIndex);
			}
			return block.getBlock(blockIndex).getData(offset % wordsPerBlock);
		}

		private boolean isValidWay(int way) {
			return 0 <= way && way < ways;
		}

		private int wordsPerObject() {
			return blocksPerObject * wordsPerBlock;
		}
		
		private void updateCache(int way, ObjectCacheEntry ob) {
			replaceFifo(cacheData,  ob, way, ways);
		}		

		public static <T> void replaceLRU(T[] data, T obj, int oldPosition, int ways) {
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
