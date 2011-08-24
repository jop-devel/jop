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
import com.jopdesign.wcet.jop.BlockCache;
import com.jopdesign.wcet.uppaal.model.NTASystem;
import com.jopdesign.wcet.uppaal.translator.SystemBuilder;

import java.util.Vector;

public class FIFOCacheBuilder extends DynamicCacheBuilder {
	private String NUM_METHODS;
	private BlockCache cache;
	private int simNumBlocks;
	private boolean assumeEmptyCache;
	public FIFOCacheBuilder(BlockCache blockCache, boolean assumeEmptyCache) {
		
		this.cache = blockCache;
		this.assumeEmptyCache = assumeEmptyCache;
		if(assumeEmptyCache) this.simNumBlocks = blockCache.getNumBlocks();
		else                 this.simNumBlocks = blockCache.getNumBlocks() / 2;
	}
	
	@Override
	public long getWaitTime(WCETProcessorModel proc, ControlFlowGraph cfg, boolean isInvoke) {

		if(isInvoke && assumeEmptyCache) return this.cache.getMissOnInvokeCost(cfg);
		else                             return this.cache.getMaxMissCost(cfg);
	}
	
	@Override
	public void appendDeclarations(NTASystem system,String NUM_METHODS) {
		this.NUM_METHODS = NUM_METHODS;
		appendDeclsN(system);
	}
	public void appendDeclsN(NTASystem system) {
		system.appendDeclaration(String.format("int[0,%s] cache[%d] = %s;",
				NUM_METHODS,simNumBlocks,initCache()));
		system.appendDeclaration(String.format("bool lastHit;"));
		system.appendDeclaration(
				"void access_cache(int mid) {\n"+
				"  int i = 0;\n"+
				"  lastHit = false;\n"+
				"  for(i = 0; i < "+simNumBlocks+"; i++) {\n"+
				"      if(cache[i] == mid) {\n"+
				"        lastHit = true;\n"+
				"        return;\n"+
				"      }\n"+
				"  }\n"+
				"  for(i = "+(simNumBlocks-1)+"; i > 0; i--) {\n"+
				"     cache[i]=cache[i-1];\n"+
				"  }\n"+
				"  cache[0] = mid;\n"+
				"}\n");
	}
	private StringBuilder initCache() {
		Vector<Object> cacheElems = new Vector<Object>();
		cacheElems.add(0);
		for(int i = 1; i < simNumBlocks; i++) cacheElems.add(NUM_METHODS);
		return SystemBuilder.constArray(cacheElems);
	}
}
