/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.wcet.uppaal.translator;

import com.jopdesign.common.code.ControlFlowGraph;
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
	public abstract SubAutomaton translateInvoke(MethodBuilder mBuilder, ControlFlowGraph.InvokeNode in, long staticWCET);
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
		long waitTime = cacheSim.getWaitTime(javaTranslator.project.getWCETProcessorModel(), cfg, isInvoke);
		tBuilder.waitAtLocation(missNode, waitTime);
	}
	
}
