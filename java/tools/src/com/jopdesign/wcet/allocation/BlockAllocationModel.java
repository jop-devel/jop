package com.jopdesign.wcet.allocation;

import com.jopdesign.wcet.Project;

public class BlockAllocationModel extends AllocationModel {

	public static final int HEADER_SIZE = 4;
	public static final int BLOCK_SIZE = 8;

	public BlockAllocationModel(Project p) {
		super(p);
	}
	
	public long computeObjectSize(long raw) {
		int blocks = (int)Math.ceil((float)(HEADER_SIZE+raw)/BLOCK_SIZE);
		return BLOCK_SIZE*blocks;
	}

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

