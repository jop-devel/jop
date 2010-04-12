package com.jopdesign.wcet.allocation;

import com.jopdesign.wcet.Project;

public class ObjectAllocationModel extends AllocationModel {

	public ObjectAllocationModel(Project p) {
		super(p);
	}
	
	public int computeObjectSize(int raw) {
		return 1;
	}

	public int computeArraySize(int raw) {
		return 1;
	}
	
}

