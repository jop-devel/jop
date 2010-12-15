package com.jopdesign.wcet.allocation;

import com.jopdesign.wcet.Project;

public class ObjectAllocationModel extends AllocationModel {

	public ObjectAllocationModel(Project p) {
		super(p);
	}
	
	public long computeObjectSize(long raw) {
		return 1;
	}

	public long computeArraySize(long raw) {
		return 1;
	}
	
}

