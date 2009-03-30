package com.jopdesign.wcet.uppaal.translator.cache;

import java.util.Vector;

import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.jop.VarBlockCache;
import com.jopdesign.wcet.uppaal.model.Location;
import com.jopdesign.wcet.uppaal.model.NTASystem;
import com.jopdesign.wcet.uppaal.translator.SystemBuilder;
import com.jopdesign.wcet.uppaal.translator.TemplateBuilder;

public class FIFOVarBlockCacheBuilder extends DynamicCacheBuilder {
	private Project project;
	private VarBlockCache cache;
	private int numMethods;
	private boolean assumeEmptyCache;
	private int simNumBlocks;
	public FIFOVarBlockCacheBuilder(Project p, VarBlockCache cache, int numMethods, boolean assumeEmptyCache) {
		this.project = p;
		this.cache = cache;
		this.numMethods = numMethods;
		this.assumeEmptyCache = assumeEmptyCache;
		if(assumeEmptyCache) simNumBlocks = cache.getNumBlocks();
		else                 simNumBlocks = cache.getNumBlocks() / 2; 
	}
	@Override
	public void appendDeclarations(NTASystem system,String NUM_METHODS) {
		system.appendDeclaration(String.format("const int NUM_BLOCKS[%s] = %s;",
				NUM_METHODS,initNumBlocks()));
		system.appendDeclaration(String.format("int[0,%s] cache[%d] = %s;",
				NUM_METHODS,simNumBlocks,initCache(NUM_METHODS)));
		system.appendDeclaration(String.format("bool lastHit;"));
		system.appendDeclaration(
				"void access_cache(int mid) {\n"+
				"  int i = 0;\n"+
				"  int sz = NUM_BLOCKS[mid];\n"+
				"  lastHit = false;\n"+
				"  for(i = 0; i < "+simNumBlocks+"; i++) {\n"+
				"      if(cache[i] == mid) {\n"+
				"        lastHit = true;\n"+
				"        return;\n"+
				"      }\n"+
				"  }\n"+
				"  for(i = "+(simNumBlocks-1)+"; i >= sz; i--) {\n"+
				"     cache[i]=cache[i-sz];\n"+
				"  }\n"+
				"  for(i = 0; i < sz-1; i++) {\n"+
				"     cache[i] = "+NUM_METHODS+";\n"+
				"  }\n"+
				"  cache[i] = mid;\n"+
				"}\n");
	}
	private StringBuilder initNumBlocks() {
		Vector<Integer> blocksPerMethod = new Vector<Integer>();
		for(int i = 0; i < numMethods; i++) {
			int mBlocks = blocksOf(i);
			if(mBlocks > simNumBlocks) {
				throw new AssertionError("Cache too small for method: "+project.getWcetAppInfo().getFlowGraph(i)+
									     " which requires at least " + mBlocks + " blocks, but only "+
									     simNumBlocks + " are available in the simulation ");
			}
			blocksPerMethod.add(mBlocks);
		}
		return SystemBuilder.constArray(blocksPerMethod);
	}
	private StringBuilder initCache(String NUM_METHODS) {
		Vector<Object> cacheElems = new Vector<Object>();
		for(int i = 0; i < simNumBlocks; i++) cacheElems.add(NUM_METHODS);
		cacheElems.set(blocksOf(0)-1,0);
		return SystemBuilder.constArray(cacheElems);
	}
	private int blocksOf(int id) {
		return project.getProcessorModel().getMethodCache().requiredNumberOfBlocks(project.getWcetAppInfo().getFlowGraph(id).getNumberOfWords());
	}
	public long getWaitTime(ProcessorModel proc, ControlFlowGraph cfg, boolean isInvoke) {
		if(assumeEmptyCache && isInvoke) return cache.getMissOnInvokeCost(proc, cfg);
		else return cache.getMaxMissCost(proc, cfg);
	}

	public void handleProgramEntry(TemplateBuilder builder, Location initLoc) {
		// FIXME: TODO
	}
}
