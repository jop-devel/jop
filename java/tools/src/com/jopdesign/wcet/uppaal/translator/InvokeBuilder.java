package com.jopdesign.wcet.uppaal.translator;

import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.uppaal.model.Location;
import com.jopdesign.wcet.uppaal.model.Transition;
import com.jopdesign.wcet.uppaal.translator.cache.CacheSimBuilder;

/** Translate invokes nodes */
public abstract class InvokeBuilder {
	protected JavaTranslator javaTranslator;
	protected TemplateBuilder tBuilder;
	private CacheSimBuilder cacheSim;

	public InvokeBuilder(JavaTranslator mt, TemplateBuilder tBuilder, CacheSimBuilder cacheSim) {
		this.javaTranslator = mt;
		this.tBuilder = tBuilder;
		this.cacheSim = cacheSim;
	}
	public abstract SubAutomaton translateInvoke(MethodBuilder mBuilder, InvokeNode in, long staticWCET);
	public void simulateCacheAccess(
			ControlFlowGraph cfg,
			boolean isInvoke,
			Location beforeNode, 
			Transition toHit, Transition toMiss, 
			Location missNode) {
		int pid = javaTranslator.getMethodID(cfg.getMethodInfo());
		tBuilder.getIncomingAttrs(beforeNode).appendUpdate("access_cache("+pid+")");
		toHit.getAttrs().appendGuard("lastHit");
		toMiss.getAttrs().appendGuard("! lastHit");
		long waitTime = cacheSim.getWaitTime(javaTranslator.project.getProcessorModel(),cfg, isInvoke);
		tBuilder.waitAtLocation(missNode, waitTime);
	}
	
}
