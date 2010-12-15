package com.jopdesign.wcet.uppaal.translator.cache;

import java.util.Vector;

import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.jop.VarBlockCache;
import com.jopdesign.wcet.uppaal.model.NTASystem;
import com.jopdesign.wcet.uppaal.translator.SystemBuilder;

public class FIFOVarBlockCacheBuilder extends VarBlockCacheBuilder {
	private boolean assumeEmptyCache;
	private int simNumBlocks;
	@Override
	protected int numBlocks() {
		return simNumBlocks;
	}
	public FIFOVarBlockCacheBuilder(Project p, VarBlockCache cache, 
			                        int numMethods, boolean assumeEmptyCache) {
		super(p,cache,numMethods);
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
		Vector<Object> cacheElems = new Vector<Object>();
		for(int i = 0; i < numBlocks(); i++) cacheElems.add(NUM_METHODS);
		if(assumeEmptyCache) cacheElems.set(blocksOf(0)-1,0);
		return SystemBuilder.constArray(cacheElems);
	}
	
	public long getWaitTime(ProcessorModel proc, ControlFlowGraph cfg, boolean isInvoke) {
		if((assumeEmptyCache && isInvoke) ||
		   cfg.isLeafMethod()) return cache.getMissOnInvokeCost(proc, cfg);
		else return cache.getMaxMissCost(proc, cfg);
	}
}
