/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

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
package com.jopdesign.wcet08.analysis;

import com.jopdesign.wcet08.Config;

public class CacheConfig {
	public final int INVOKE_STATIC_HIDE_LOAD_CYCLES = 37;
	public final int MIN_RETURN_HIDE_LOAD_CYCLES = 9;
	public final int MIN_HIDE_LOAD_CYCLES = 9;
	private Config config;

	public CacheConfig(Config c) { 
		this.config = c; 
	}	
	public static final String CACHE_BLOCKS = "cache-blocks";	
	public static final String BLOCK_SIZE = "cache-block-size";
	public final static String[][] optionDescrs = {
		{ CACHE_BLOCKS, "number of cache blocks [default: 16]" },
		{ BLOCK_SIZE,   "size of cache blocks in bytes [default: 256]"}
	};
	
	public int cacheBlocks() {
		return config.getIntProperty(CACHE_BLOCKS,16);
	}
	public int blockSize() {
		return config.getIntProperty(BLOCK_SIZE,256);
	}
}
