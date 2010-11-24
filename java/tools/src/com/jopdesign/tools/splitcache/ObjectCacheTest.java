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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.jopdesign.tools.MainDataMemory;
import com.jopdesign.tools.Cache.ReplacementStrategy;
import com.jopdesign.tools.DataMemory.Access;
import com.jopdesign.tools.splitcache.ObjectCache.BlockAccessResult;
import com.jopdesign.tools.splitcache.ObjectCache.FieldIndexMode;
import com.jopdesign.tools.splitcache.ObjectCache.ObjectCacheLookupResult;

/**
 * Purpose:
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class ObjectCacheTest {

	private MainDataMemory mainMemory;
	private MainDataMemory handleMemory;
	private ObjectCache[] objectCaches;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		mainMemory = new MainDataMemory(new int[4096]);
		handleMemory = new MainDataMemory(new int[1024]);
		objectCaches = new ObjectCache[2];
		Access[] handledTypes = { Access.FIELD, Access.MVB };
		objectCaches[0] =  new ObjectCache(8,4,4,FieldIndexMode.Bypass, ReplacementStrategy.LRU,
				                           false, handleMemory, mainMemory, handledTypes);
		objectCaches [1] = new ObjectCache(8,4,4,FieldIndexMode.Bypass, ReplacementStrategy.FIFO,
				                           false, handleMemory, mainMemory, handledTypes);
		initRAM();
	}
	
	private void initRAM() {
		// Initialize 4 handles:
		// 0,1,2,3 -> 3*32, 5*32, 7*32, 11*32
		handleMemory.write(1, 3 << 5, Access.INTERN);
		handleMemory.write(2, 5 << 5, Access.INTERN);
		handleMemory.write(3, 7 << 5, Access.INTERN);
		handleMemory.write(4, 11 << 5, Access.INTERN);
		// Initialize 4 objects
		for(int i = 1; i <= 4; i++) {
			int oid = i << 5;
			for(int j = 0; j < 32; j++) {
				mainMemory.write(handleMemory.read(i,Access.HANDLE) + j, oid+j, Access.FIELD);
			}
		}
		// Initialize copy of object 1 at base address (12<<5)
		int oid = 1 << 5;
		int base = 12 << 5;
		for(int j = 0; j < 32; j++) {
			mainMemory.write(base + j, oid+j, Access.FIELD);
		}
	}

	/**
	 * Test method for {@link com.jopdesign.tools.splitcache.ObjectCache#getSize()}.
	 */
	@Test
	public void testGetSize() {
		for(ObjectCache cache: objectCaches) {
			assertEquals(8*4*4, cache.getSize());
		}
	}	

	/**
	 * Test method for {@link com.jopdesign.tools.splitcache.ObjectCache#read(int, int)}.
	 */
	@Test
	public void testRead() {
		for(ObjectCache cache: objectCaches) {
			int oid = 2, oid2 = 3, field1a = 14, field1b = 15, field2 = 11, fieldBypass = 16;
			ObjectCacheLookupResult r = cache.new ObjectCacheLookupResult();
			// read first time (should be uncached)
			cache.readFieldInto(oid, field1a, r);
			assertEquals(oid, r.getData() >> 5);
			assertEquals(field1a, r.getData() & 31);
			assertEquals(false, r.isObjectHit());
			assertEquals(BlockAccessResult.MISS, r.getBlockAccessStatus());
			// read another block (object hit, field miss)
			cache.readFieldInto(oid, field2, r);
			assertEquals(oid, r.getData() >> 5);
			assertEquals(field2, r.getData() & 31);
			assertEquals(true, r.isObjectHit());
			assertEquals(BlockAccessResult.MISS, r.getBlockAccessStatus());
			// read another object (object miss, field miss)
			cache.readFieldInto(oid2, field1a, r);
			assertEquals(oid2, r.getData() >> 5);
			assertEquals(field1a, r.getData() & 31);
			assertEquals(false, r.isObjectHit());
			assertEquals(BlockAccessResult.MISS, r.getBlockAccessStatus());			
			// read again first block of first object (object hit, field hit)
			cache.readFieldInto(oid, field1b, r);
			assertEquals(oid, r.getData() >> 5);
			assertEquals(field1b, r.getData() & 31);
			assertEquals(true, r.isObjectHit());
			assertEquals(BlockAccessResult.HIT, r.getBlockAccessStatus());
			// read bypassed field
			cache.readFieldInto(oid, fieldBypass, r);
			assertEquals(oid, r.getData() >> 5);
			assertEquals(fieldBypass, r.getData() & 31);
			assertEquals(true, r.isObjectHit());
			assertEquals(BlockAccessResult.BYPASS, r.getBlockAccessStatus());
		}
	}


	/**
	 * Test method for {@link com.jopdesign.tools.splitcache.ObjectCache#invalidateObjectCache()}.
	 */
	@Test
	public void testInvalidateObjectCache() {
		for(ObjectCache cache: objectCaches) {
			initRAM();

			int oid = 2, field = 14;
			ObjectCacheLookupResult r = cache.new ObjectCacheLookupResult();
			// read first time (should be uncached)
			cache.readFieldInto(oid, field, r);
			assertEquals(oid, r.getData() >> 5);
			assertEquals(field, r.getData() & 31);
			assertEquals(false, r.isObjectHit());
			assertEquals(BlockAccessResult.MISS, r.getBlockAccessStatus());
			// write to next level memory and invalidate cache
			int addr = cache.getAddress(oid, field);
			mainMemory.write(addr, 0xcafebabe, Access.INTERN);
			cache.invalidateData();
			// read same block again (after invalidate)
			cache.readFieldInto(oid, field, r);
			assertEquals(0xcafebabe, r.getData());
			assertEquals(false, r.isObjectHit());
			assertEquals(BlockAccessResult.MISS, r.getBlockAccessStatus());
		}
	}

	/**
	 * Test method for {@link com.jopdesign.tools.splitcache.ObjectCache#invalidateHandleCache()}.
	 */
	@Test
	public void testInvalidateHandleCache() {
		for(ObjectCache cache: objectCaches) {
			initRAM();

			int oid = 1, newBase = 12 << 5; // there is a copy of object 1 and base (12 << 5)
			int field = 11, field2 = 12;
			ObjectCacheLookupResult r = cache.new ObjectCacheLookupResult();
			// read first time (should be uncached)
			cache.readFieldInto(oid, field, r);
			assertEquals(oid, r.getData() >> 5);
			assertEquals(field, r.getData() & 31);
			assertEquals(false, r.isObjectHit());
			assertEquals(BlockAccessResult.MISS, r.getBlockAccessStatus());
			// write to handle cache, invalidate handle cache and overwrite old memory;
			int oldAddr = this.handleMemory.read(oid, Access.HANDLE);
			this.handleMemory.write(oid, newBase, Access.INTERN);
			cache.invalidateHandles();
			for(int p = oldAddr; p < oldAddr+32; p++) {
				mainMemory.write(p, 0xdeadbeef, Access.INTERN);
			}
			// read same block again (should be a cache hit, as data was not invalidated)
			cache.readFieldInto(oid, field, r);
			assertEquals(oid, r.getData() >> 5);
			assertEquals(field, r.getData() & 31);
			assertEquals(true, r.isObjectHit());
			assertEquals(false, r.isReloadAddress()); // we did not reload the address, as it was unneccessary
			assertEquals(BlockAccessResult.HIT, r.getBlockAccessStatus());
			// read a different block (should be handle and data miss, reading from new data)
			cache.readFieldInto(oid, field2, r);
			assertEquals(oid, r.getData() >> 5);
			assertEquals(field2, r.getData() & 31);
			assertEquals(true, r.isObjectHit());
			assertEquals(true, r.isReloadAddress());
			assertEquals(BlockAccessResult.MISS, r.getBlockAccessStatus());
		}
	}


	/**
	 * Test method for {@link com.jopdesign.tools.splitcache.ObjectCache#write(int, int, int)}.
	 */
	@Test
	public void testWrite() {
		for(ObjectCache cache: objectCaches) {
			int oid = 2, oid2 = 3, field1a = 14, field1b = 15, field2 = 11, fieldBypass = 16;
			ObjectCacheLookupResult r = cache.new ObjectCacheLookupResult();
			// read first time (uncached)
			cache.readFieldInto(oid, field1a, r);
			assertEquals(oid, r.getData() >> 5);
			assertEquals(field1a, r.getData() & 31);
			assertEquals(BlockAccessResult.MISS, r.getBlockAccessStatus());
			// read again first block of first object (object hit, field hit)
			cache.readFieldInto(oid, field1b, r);
			assertEquals(oid, r.getData() >> 5);
			assertEquals(field1b, r.getData() & 31);
			assertEquals(BlockAccessResult.HIT, r.getBlockAccessStatus());
			// write 3rd field of this block and read (other value, still cached)
			cache.writeField(oid, field1a, 0xcafebabe, Access.FIELD);
			cache.readFieldInto(oid, field1a, r);
			assertEquals(0xcafebabe, r.getData());
			assertEquals(true, r.isObjectHit());
			assertEquals(BlockAccessResult.HIT, r.getBlockAccessStatus());
			// write another block, read (object hit, field miss)
			// read again (on write allocate - hit, otherwise - miss)
		}
	}



}
