package com.jopdesign.wcet.uppaal.translator.cache;

import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.uppaal.model.NTASystem;


public class StaticCacheBuilder extends CacheSimBuilder {

	private boolean alwaysMiss;
	public StaticCacheBuilder(boolean alwaysMiss) {
		this.alwaysMiss = alwaysMiss;
	}
	@Override
	public boolean isDynamic() {
		return false;
	}
	@Override
	public boolean isAlwaysMiss() {
		return alwaysMiss;
	}
	@Override
	public void appendDeclarations(NTASystem system, String NUM_METHODS) { }
	@Override
	public long getWaitTime(ProcessorModel proc, ControlFlowGraph cfg,boolean isInvoke) {
		return proc.getMethodCacheMissPenalty(cfg.getNumberOfWords(), isInvoke);
	}
}
