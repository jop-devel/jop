/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)
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

import java.util.Random;


/**
 * Simulation (including hitrate and timing) of a configurable
 * split cache architecture.
 * 
 * Intended to replace DCacheSim
 * 
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class SplitCacheSim {
	
	public static void main(String argv[])
	{
		final int size = 2048;
		final int testCount = size*size;
		final int seed = 424534;
		Random r = new Random(seed);

		/* Memory with random content */
		int[] raw_mem = new int[size];
		for(int i = 0; i < size; i++) {
			raw_mem[i] = r.nextInt();
		}
		Memory mem = new Memory(raw_mem);
		SetAssociativeCache cache1 = new SetAssociativeCacheLRU(4, 16, 16, mem);
		SetAssociativeCache cache  = new SetAssociativeCacheFIFO(16, 4, 4, cache1);
		
		/* Random access (no locality) */
		for(int i = 0; i < testCount; i++) {
			cache.read(r.nextInt(size));
		}
		System.out.println(cache.stats.toString());
		System.out.println(cache1.stats.toString());
		cache.invalidate();
		cache.stats.reset();
		/* Random test (simple spatial locality) */
		int pos = r.nextInt(size);
		for(int i = 0; i < testCount; i++) {
			cache.read(pos);
			pos = Math.abs(pos + (int)(r.nextGaussian() / 16.0 * size)) % size;
		}
		System.out.println(cache.stats.toString());
		System.out.println(cache1.stats.toString());
	}
}
