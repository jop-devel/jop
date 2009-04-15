package com.jopdesign.wcet.uppaal.translator.cache;

import java.util.Vector;

import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.jop.VarBlockCache;
import com.jopdesign.wcet.uppaal.model.NTASystem;
import com.jopdesign.wcet.uppaal.translator.SystemBuilder;

public class LRUVarBlockCacheBuilder extends VarBlockCacheBuilder {
	public LRUVarBlockCacheBuilder(Project p, VarBlockCache cache, int numMethods) {
		super(p, cache, numMethods);
	}
	@Override
	protected int numBlocks() {
		return this.cache.getNumBlocks();
	}
	@Override
	public void appendDeclarations(NTASystem system,String NUM_METHODS) {
		super.appendDeclarations(system, NUM_METHODS);
		system.appendDeclaration(
				"/* LRU Cache Sim */"+
				"void access_cache(int mid) {\n"+
				"  int i = 0;\n"+
				"  int sz = NUM_BLOCKS[mid];\n"+
				"  if(cache[sz-1] == mid) {  /* No Change */\n" +
				"    lastHit = true;\n" +
				"    return; \n"+
				"  }\n" +				
				"  lastHit = false;\n"+
				"  /* search */\n" +
				"  for(i = sz; ! lastHit && i < "+numBlocks()+"; i++) {\n"+
				"      if(cache[i] == mid) {\n"+
				"        lastHit = true;\n"+
				"      }\n"+
				"  }\n"+
				" /* i points to the element after tag, move back */"+
				"  for(i = i-1; i >= sz; --i) {\n"+
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
		cacheElems.set(blocksOf(0)-1,0);
		return SystemBuilder.constArray(cacheElems);
	}
	public long getWaitTime(ProcessorModel proc, ControlFlowGraph cfg, boolean isInvoke) {
		return proc.getMethodCacheLoadTime(cfg.getNumberOfWords(), isInvoke);
	}
}
