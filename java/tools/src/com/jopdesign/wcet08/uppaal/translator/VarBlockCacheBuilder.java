package com.jopdesign.wcet08.uppaal.translator;

import java.util.Vector;

import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.analysis.BlockWCET;
import com.jopdesign.wcet08.uppaal.UppAalConfig;
import com.jopdesign.wcet08.uppaal.model.NTASystem;

public class VarBlockCacheBuilder extends CacheSimBuilder {
	private int numBlocks;
	private Project project;
	private int blockSize;
	private int numMethods;
	public VarBlockCacheBuilder(Project p, int numMethods) {
		this.project = p;
		this.numMethods = numMethods;
		this.numBlocks = Config.instance().getOption(UppAalConfig.UPPAAL_CACHE_BLOCKS).intValue();
		this.blockSize = Config.instance().getOption(UppAalConfig.UPPAAL_CACHE_BLOCK_WORDS).intValue();
		// logger.info("Var Block cache simulation with "+numBlocks+ " blocks assuming empty cache");
	}
	@Override
	public void appendDeclarations(NTASystem system,String NUM_METHODS) {
		super.appendDeclarations(system,NUM_METHODS);
		system.appendDeclaration(String.format("const int NUM_BLOCKS[%s] = %s;",
				NUM_METHODS,initNumBlocks()));
		system.appendDeclaration(String.format("int[0,%s] cache[%d] = %s;",
				NUM_METHODS,this.numBlocks,initCache(NUM_METHODS)));
		system.appendDeclaration(String.format("bool lastHit;"));
		system.appendDeclaration(
				"void access_cache(int mid) {\n"+
				"  int i = 0;\n"+
				"  int sz = NUM_BLOCKS[mid];\n"+
				"  lastHit = false;\n"+
				"  for(i = 0; i < "+numBlocks+"; i++) {\n"+
				"      if(cache[i] == mid) {\n"+
				"        lastHit = true;\n"+
				"        return;\n"+
				"      }\n"+
				"  }\n"+
				"  for(i = "+(numBlocks-1)+"; i >= sz; i--) {\n"+
				"     cache[i]=cache[i-sz];\n"+
				"  }\n"+
				"  for(i = 0; i < sz; i++) {\n"+
				"     cache[i]=mid;\n"+
				"  }\n"+
				"}\n");
	}
	private StringBuilder initNumBlocks() {
		Vector<Integer> blocksPerMethod = new Vector<Integer>();
		for(int i = 0; i < numMethods; i++) {
			int mBlocks = blocksOf(i);
			if(mBlocks > numMethods) throw new AssertionError("Cache too small");
			blocksPerMethod.add(mBlocks);
		}
		return CacheSimBuilder.constArray(blocksPerMethod);
	}
	private StringBuilder initCache(String NUM_METHODS) {
		Vector<Object> cacheElems = new Vector<Object>();
		cacheElems.add(0);
		int i;
		for(i = 1; i < blocksOf(0); i++) cacheElems.add(0);
		for(; i < numBlocks; i++) cacheElems.add(NUM_METHODS);
		return CacheSimBuilder.constArray(cacheElems);
	}
	private int blocksOf(int id) {
		return BlockWCET.numberOfBlocks(project.getWcetAppInfo().getFlowGraph(id),blockSize);
	}
}
