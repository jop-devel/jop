package com.jopdesign.wcet.uppaal.translator.cache;

import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.uppaal.model.NTASystem;

public abstract class CacheSimBuilder {
	public abstract void appendDeclarations(NTASystem system, String NUM_METHODS);
	public abstract boolean isDynamic();
	public abstract boolean isAlwaysMiss();
	public abstract long getWaitTime(ProcessorModel proc, ControlFlowGraph cfg, boolean isInvoke);
}
