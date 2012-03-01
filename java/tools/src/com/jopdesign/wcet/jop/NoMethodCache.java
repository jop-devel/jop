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
package com.jopdesign.wcet.jop;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.processormodel.JOPConfig.CacheImplementation;
import com.jopdesign.wcet.WCETProcessorModel;
import com.jopdesign.wcet.WCETTool;

public class NoMethodCache extends MethodCache {

	public NoMethodCache(WCETTool p) {
		super(p,0);
	}

	@Override
	public boolean allFit(MethodInfo m, CallString cs) {
		return false;
	}

    @Override
    public boolean allFit(long blocks) {
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
	public long getMissOnReturnCost(WCETProcessorModel proc, ControlFlowGraph invoker) {
		return 0;		
	}
	@Override 
	public long getInvokeReturnMissCost(WCETProcessorModel proc, ControlFlowGraph invoker, ControlFlowGraph invoked) {
		return 0;		
	}

	@Override
	public CacheImplementation getName() {
		return CacheImplementation.NO_METHOD_CACHE;
	}
}
