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

public class SetAssociativeCacheLRU extends SetAssociativeCache {

	SetAssociativeCacheLRU(int ways, int linesPerWay, int wordsPerBlock, Cache nextLevel) {
		super(ways, linesPerWay, wordsPerBlock, nextLevel);
	}

	@Override
	public CacheLookupResult readCacheBlock(int addr, int tag, int line) {
		int word = addr & blockMask();
		int way = lookupTag(tag, line);

		CacheBlock cacheBlock = getOrLoadCacheBlock(tag, line, way);
		
		/* LRU move A B C X D -> X A B C D*/
		CacheBlock movedBlock = cacheBlock;
		for(int w = 0; w <= way && w < ways; w++) {
			CacheBlock activeBlock = cacheData[w][line];
			cacheData[w][line] = movedBlock;
			movedBlock = activeBlock;
		}
		
		/* return cache lookup result */
		int datum = cacheBlock.getData(word);
		if(datum != nextLevelCache.read(addr)) throw new AssertionError("Bad Cache implementation");
		return new CacheLookupResult(datum, validWay(way));
	}		
}