package com.jopdesign.wcet.allocation;

import com.jopdesign.wcet.Project;

public class HandleAllocationModel extends AllocationModel {

	public HandleAllocationModel(Project p) {
		super(p);
	}
	
	public int computeObjectSize(int raw) {
		return raw;
	}

	public int computeArraySize(int raw) {
		return raw;
	}
	
}

