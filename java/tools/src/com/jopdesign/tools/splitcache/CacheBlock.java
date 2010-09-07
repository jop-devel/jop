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

public class CacheBlock {
	private boolean valid;
	private int tag;
	private int[] data;
	
	public CacheBlock(int blockSize) {
		valid = false;
		tag = 0;
		data = new int[blockSize];		
	}
	
	public void load(int tag, int[] rawData, int rawDataOffset) {
		for(int i = 0; i < data.length; i++) {
			this.data[i] = rawData[i+rawDataOffset];
		}
		this.tag = tag;
		this.valid = true;
	}

	public void load(int tag, int baseAddr, Cache nextLevel) {
		for(int i = 0; i < data.length; i++) {
			this.data[i] = nextLevel.read(baseAddr + i);
		}
		this.tag = tag;
		this.valid = true;
	}

	public void invalidate() {
		valid = false;
	}

	public boolean isValid() {
		return valid;
	}

	public int getTag() {
		return tag;
	}
	
	public int getData(int offset) {
		return data[offset];
	}
}