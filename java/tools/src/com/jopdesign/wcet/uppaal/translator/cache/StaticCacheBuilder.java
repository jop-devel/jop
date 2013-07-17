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
package com.jopdesign.wcet.uppaal.translator.cache;

import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.wcet.WCETProcessorModel;
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
	public long getWaitTime(WCETProcessorModel proc, ControlFlowGraph cfg,boolean isInvoke) {

		return proc.getMethodCache().getMissPenalty(cfg.getNumberOfWords(), isInvoke);
	}
}
