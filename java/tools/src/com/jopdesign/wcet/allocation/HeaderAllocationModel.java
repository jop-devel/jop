package com.jopdesign.wcet.allocation;

import com.jopdesign.wcet.Project;

public class HeaderAllocationModel extends AllocationModel {

	public static final int HEADER_SIZE = 4;

	public HeaderAllocationModel(Project p) {
		super(p);
	}
	
	public long computeObjectSize(long raw) {
		return HEADER_SIZE+raw;
	}

	public long computeArraySize(long raw) {
		return HEADER_SIZE+raw;
	}
	
}

