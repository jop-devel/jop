package com.jopdesign.wcet.uppaal.translator;

import java.util.Vector;

import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.jop.VarBlockCache;
import com.jopdesign.wcet.uppaal.model.Location;
import com.jopdesign.wcet.uppaal.model.NTASystem;

public class VarBlockCacheBuilder extends CacheSimBuilder {
	private Project project;
	private VarBlockCache cache;
	private int numMethods;
	public VarBlockCacheBuilder(Project p, VarBlockCache cache, int numMethods, boolean assumeEmptyCache) {
		this.project = p;
		this.cache = cache;
		this.numMethods = numMethods;
		if(! assumeEmptyCache) throw new AssertionError("Empty cache has to be assumed for VARBLOCK cache sim");
		// logger.info("Var Block cache simulation with "+numBlocks+ " blocks assuming empty cache");
	}
	@Override
	public void appendDeclarations(NTASystem system,String NUM_METHODS) {
		super.appendDeclarations(system,NUM_METHODS);
		system.appendDeclaration(String.format("const int NUM_BLOCKS[%s] = %s;",
				NUM_METHODS,initNumBlocks()));
		system.appendDeclaration(String.format("int[0,%s] cache[%d] = %s;",
				NUM_METHODS,cache.getNumBlocks(),initCache(NUM_METHODS)));
		system.appendDeclaration(String.format("bool lastHit;"));
		system.appendDeclaration(
				"void access_cache(int mid) {\n"+
				"  int i = 0;\n"+
				"  int sz = NUM_BLOCKS[mid];\n"+
				"  lastHit = false;\n"+
				"  for(i = 0; i < "+cache.getNumBlocks()+"; i++) {\n"+
				"      if(cache[i] == mid) {\n"+
				"        lastHit = true;\n"+
				"        return;\n"+
				"      }\n"+
				"  }\n"+
				"  for(i = "+(cache.getNumBlocks()-1)+"; i >= sz; i--) {\n"+
				"     cache[i]=cache[i-sz];\n"+
				"  }\n"+
				"  cache[0] = mid;"+
				"  for(i = 1; i < sz; i++) {\n"+
				"     cache[i] = "+NUM_METHODS+";\n"+
				"  }\n"+
				"}\n");
	}
	private StringBuilder initNumBlocks() {
		Vector<Integer> blocksPerMethod = new Vector<Integer>();
		for(int i = 0; i < numMethods; i++) {
			int mBlocks = blocksOf(i);
			if(mBlocks > cache.getNumBlocks()) {
				throw new AssertionError("Cache too small for method: "+project.getWcetAppInfo().getFlowGraph(i)+
									     " which requires at least " + mBlocks + " blocks, but only "+
									     cache.getNumBlocks() + " are available ");
			}
			blocksPerMethod.add(mBlocks);
		}
		return CacheSimBuilder.constArray(blocksPerMethod);
	}
	private StringBuilder initCache(String NUM_METHODS) {
		Vector<Object> cacheElems = new Vector<Object>();
		cacheElems.add(0);
		int i;
		for(i = 1; i < blocksOf(0); i++) cacheElems.add(0);
		for(; i < cache.getNumBlocks(); i++) cacheElems.add(NUM_METHODS);
		return CacheSimBuilder.constArray(cacheElems);
	}
	private int blocksOf(int id) {
		return project.getProcessorModel().getMethodCache().requiredNumberOfBlocks(project.getWcetAppInfo().getFlowGraph(id).getNumberOfWords());
	}
	public void handleProgramEntry(TemplateBuilder builder, Location initLoc) {
	}
}
