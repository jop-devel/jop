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

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.wcet.WCETProcessorModel;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.jop.VarBlockCache;
import com.jopdesign.wcet.uppaal.model.NTASystem;
import com.jopdesign.wcet.uppaal.translator.SystemBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FIFOVarBlockCacheBuilder extends VarBlockCacheBuilder {
	private boolean assumeEmptyCache;
	private int simNumBlocks;
	@Override
	protected int numBlocks() {
		return simNumBlocks;
	}
	public FIFOVarBlockCacheBuilder(WCETTool p, VarBlockCache cache,
			                        Set<MethodInfo> methods, boolean assumeEmptyCache) {
		super(p,cache,methods);
		this.assumeEmptyCache = assumeEmptyCache;
		if(assumeEmptyCache) simNumBlocks = cache.getNumBlocks();
		else                 simNumBlocks = cache.getNumBlocks() / 2; 
	}
	@Override
	public void appendDeclarations(NTASystem system,String NUM_METHODS) {
		super.appendDeclarations(system, NUM_METHODS);
		system.appendDeclaration(
				"void access_cache(int mid) {\n"+
				"  int i = 0;\n"+
				"  int sz = NUM_BLOCKS[mid];\n"+
				"  lastHit = false;\n"+
				"  for(i = 0; i < "+numBlocks()+"; i++) {\n"+
				"      if(cache[i] == mid) {\n"+
				"        lastHit = true;\n"+
				"        return;\n"+
				"      }\n"+
				"  }\n"+
				"  for(i = "+(numBlocks()-1)+"; i >= sz; i--) {\n"+
				"     cache[i]=cache[i-sz];\n"+
				"  }\n"+
				"  for(i = 0; i < sz-1; i++) {\n"+
				"     cache[i] = "+NUM_METHODS+";\n"+
				"  }\n"+
				"  cache[i] = mid;\n"+
				"}\n");
	}
	
	protected StringBuilder initCache(String NUM_METHODS) {
		List<Object> cacheElems = new ArrayList<Object>();
		for(int i = 0; i < numBlocks(); i++) cacheElems.add(NUM_METHODS);
        // TODO check: this has been "blocksOf(0)", check if this is the same!!
        MethodInfo method = project.getAppInfo().getMainMethod();
		if(assumeEmptyCache) cacheElems.set(blocksOf(method)-1,0);
		return SystemBuilder.constArray(cacheElems);
	}
	
	public long getWaitTime(WCETProcessorModel proc, ControlFlowGraph cfg, boolean isInvoke) {
		if((assumeEmptyCache && isInvoke) ||
		   cfg.isLeafMethod()) return cache.getMissOnInvokeCost(cfg);
		else return cache.getMaxMissCost(cfg);
	}
}
