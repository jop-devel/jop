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

import org.junit.Test;

import com.jopdesign.tools.MainDataMemory;
import com.jopdesign.tools.Cache.ReplacementStrategy;
import com.jopdesign.tools.DataMemory.Access;

/**
 * Purpose:
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class SetAssociativeCacheTest {

	@Test
	public void testInsertLine() {
		final int MEM_SIZE = 4096;
		final int WAYS = 2, LINES = 8, BLOCKWORDS = 8, BLOCKBITS = 3; 
		MainDataMemory mem = new MainDataMemory(new int[MEM_SIZE]);
		SetAssociativeCache cache = new SetAssociativeCache(WAYS,LINES,BLOCKWORDS,ReplacementStrategy.LRU,
				                                            true, false, mem, Access.values());

		for(int line = 0; line < LINES; line ++)
		for(int word = 0; word < BLOCKWORDS; word++) {
			int addr = (line << BLOCKBITS) | word;
			assertEquals(cache.wordOfAddress(addr), word);
			assertEquals(cache.lineOfAddress(addr), line);

			int value = addr;
			mem.write(addr, value, Access.STATIC);
			cache.invalidateCache();
			int v1 = cache.read(addr, Access.STATIC);
			assertEquals(v1, value);
			int v2 = cache.getCacheBlock(0, line).getData(word);
			assertEquals(v2, value);
		}
	}
	
	@Test
	public void testReplaceLRU() {
		MainDataMemory mem = new MainDataMemory(new int[4096]);
		SetAssociativeCache cache = new SetAssociativeCache(8,2,2,ReplacementStrategy.LRU,
				                        true,false,mem,Access.values());
		for(int i = 0; i < 8; i++) {
			mem.write(i * cache.getSizeInBytes(), i, Access.STATIC);
		}
		for(int i = 0; i < 17; i++) {
			for(int j = 0; j <= i; j++) {
				cache.read(j * cache.getSizeInBytes(), Access.STATIC);
			}
			for(int j = 0; i < Math.min(i,8); j++) {
				assertEquals(cache.getCacheBlock(i, 0).getData(0), i-j);
			}
		}		
	}

	@Test
	public void testReplaceFIFO() {
		MainDataMemory mem = new MainDataMemory(new int[4096]);
		SetAssociativeCache cache = new SetAssociativeCache(8,2,2,ReplacementStrategy.FIFO,true,false,mem,Access.values());
		for(int i = 0; i < 8; i++) {
			mem.write(i * cache.getSizeInBytes(), i, Access.STATIC);
		}
		for(int i = 0; i < 17; i++) {
			for(int j = 0; j <= i; j++) {
				cache.read(j * cache.getSizeInBytes(), Access.STATIC);
			}
			for(int j = 0; i < Math.min(i,8); j++) {
				assertEquals(cache.getCacheBlock(i, 0).getData(0), i-j);
			}
		}		
	}

}
