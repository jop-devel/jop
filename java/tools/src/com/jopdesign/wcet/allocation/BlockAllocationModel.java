/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.wcet.allocation;

import com.jopdesign.wcet.WCETTool;

public class BlockAllocationModel extends AllocationWcetModel {

	public static final int HEADER_SIZE = 4;
	public static final int BLOCK_SIZE = 8;

	public BlockAllocationModel(WCETTool p) {
		super(p);
	}
	
	@Override
	public long computeObjectSize(long raw) {
		int blocks = (int)Math.ceil((float)(HEADER_SIZE+raw)/BLOCK_SIZE);
		return BLOCK_SIZE*blocks;
	}

	@Override
	public long computeArraySize(long raw) {
		int depth = (int)Math.ceil(Math.log(raw)/Math.log(BLOCK_SIZE));
		int blocks = (int)Math.ceil((float)HEADER_SIZE/BLOCK_SIZE);
		int blockPow = BLOCK_SIZE;
		for (int i = 0; i < depth; i++) {
			blocks += (int)Math.ceil((float)raw/blockPow);
			blockPow *= BLOCK_SIZE;
		}
		return BLOCK_SIZE*blocks;
	}
	
}

