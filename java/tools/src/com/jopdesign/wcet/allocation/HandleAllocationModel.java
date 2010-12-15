package com.jopdesign.wcet.allocation;

import com.jopdesign.wcet.Project;

public class HandleAllocationModel extends AllocationModel {

	public HandleAllocationModel(Project p) {
		super(p);
	}
	
	public long computeObjectSize(long raw) {
		return raw;
	}

	public long computeArraySize(long raw) {
		return raw;
	}
	
}

