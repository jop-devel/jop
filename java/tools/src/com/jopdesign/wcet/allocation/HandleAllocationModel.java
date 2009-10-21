package com.jopdesign.wcet.allocation;

import com.jopdesign.wcet.Project;

public class HandleAllocationModel extends AllocationModel {

	// TODO: make configurable through command-line
	public static final int HANDLE_SIZE = 0;
	
	public HandleAllocationModel(Project p) {
		super(p);
	}
	
	public int computeObjectSize(int raw) {
		return HANDLE_SIZE+raw;
	}

	public int computeArraySize(int raw) {
		return HANDLE_SIZE+raw;
	}
	
}

