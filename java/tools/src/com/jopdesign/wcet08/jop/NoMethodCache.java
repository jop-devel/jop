package com.jopdesign.wcet08.jop;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.ProcessorModel;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.frontend.ControlFlowGraph;
import com.jopdesign.wcet08.jop.CacheConfig.CacheImplementation;

public class NoMethodCache extends MethodCache {

	public NoMethodCache(Project p) {
		super(p,0);
	}

	@Override
	public boolean allFit(MethodInfo m) {
		return false;
	}

	@Override
	public boolean fitsInCache(int sizeInWords) {
		return true;
	}

	@Override
	public boolean isLRU() {
		return false;
	}

	@Override
	public int requiredNumberOfBlocks(int sizeInWords) {
		return 0;
	}
	@Override 
	public long getMissOnInvokeCost(ProcessorModel proc, ControlFlowGraph invoked) {
		return 0;
	}
	@Override 
	public long getMissOnReturnCost(ProcessorModel proc, ControlFlowGraph invoker) {
		return 0;		
	}
	@Override 
	public long getInvokeReturnMissCost(ProcessorModel proc, ControlFlowGraph invoker, ControlFlowGraph invoked) {
		return 0;		
	}

	@Override
	public CacheImplementation getName() {
		return CacheImplementation.NO_METHOD_CACHE;
	}
}
