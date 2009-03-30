package com.jopdesign.wcet.uppaal.translator.cache;

import java.util.Vector;

import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.jop.BlockCache;
import com.jopdesign.wcet.uppaal.model.NTASystem;
import com.jopdesign.wcet.uppaal.translator.SystemBuilder;

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
	public long getWaitTime(ProcessorModel proc, ControlFlowGraph cfg, boolean isInvoke) {
		if(isInvoke && assumeEmptyCache) return this.cache.getMissOnInvokeCost(proc, cfg);
		else                             return this.cache.getMaxMissCost(proc, cfg);
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
