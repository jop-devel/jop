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

public class LRUCacheBuilder extends DynamicCacheBuilder {
	private BlockCache cache;
	public LRUCacheBuilder(BlockCache blockCache) {
		this.cache = blockCache;
		// logger.info("LRU cache simulation with "+numBlocks+ " blocks");
	}
	@Override
	public void appendDeclarations(NTASystem system,String NUM_METHODS) {
		appendDeclsN(system,NUM_METHODS);
	}
	public void appendDeclsN(NTASystem system,String NUM_METHODS) {
		system.appendDeclaration(String.format("int[0,%s] cache[%d] = %s;",
				NUM_METHODS,cache.getNumBlocks(),initCache(NUM_METHODS,cache.getNumBlocks())));
		system.appendDeclaration(String.format("bool lastHit;"));
		system.appendDeclaration(
				"void access_cache(int mid) {\n" +
				"  lastHit = false;\n"+
				"  if(cache[0] == mid) {\n"+
				"    lastHit = true;\n"+
				"  } else {\n"+
				"    int i = 0;\n"+
				"    int last = cache[0];\n"+
				"    for(i = 0; i < "+(cache.getNumBlocks()-1)+" && (! lastHit); i++) {\n"+
				"      int next = cache[i+1];\n"+
				"      if(next == mid) {\n"+
				"        lastHit = true;\n"+
				"      }\n"+ 
				"      cache[i+1] = last;\n"+
				"      last = next;\n"+
				"    }\n"+
				"    cache[0] = mid;\n"+
				"  }\n"+
				"}\n");
	}
	private String initCache(String NUM_METHODS,int numBlocks) {
		Vector<String>initElems = new Vector<String>();
		initElems.add(""+0);
		for(int i = 1; i < numBlocks; i++) initElems.add(NUM_METHODS);
		return SystemBuilder.constArray(initElems).toString();
	}
	@Override
	public long getWaitTime(WCETProcessorModel proc, ControlFlowGraph cfg,boolean isInvoke) {

		return proc.getMethodCache().getMissPenalty(cfg.getNumberOfWords(), isInvoke);
	}
}
