package com.jopdesign.wcet.uppaal.translator.cache;

import java.util.Vector;

import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.jop.VarBlockCache;
import com.jopdesign.wcet.uppaal.model.NTASystem;
import com.jopdesign.wcet.uppaal.translator.SystemBuilder;

public abstract class VarBlockCacheBuilder extends DynamicCacheBuilder {
	protected Project project;
	protected VarBlockCache cache;
	protected int numMethods;
	public VarBlockCacheBuilder(Project p, VarBlockCache cache, int numMethods) {
		this.project = p;
		this.cache = cache;
		this.numMethods = numMethods;
	}
	protected abstract int numBlocks();
	protected abstract StringBuilder initCache(String NUM_METHODS);
	
	protected int blocksOf(int id) {
		return project.getProcessorModel().getMethodCache().requiredNumberOfBlocks(project.getWcetAppInfo().getFlowGraph(id).getNumberOfWords());
	}
	@Override
	public void appendDeclarations(NTASystem system,String NUM_METHODS) {
		system.appendDeclaration(String.format("const int NUM_BLOCKS[%s] = %s;",
				NUM_METHODS,initNumBlocks()));
		system.appendDeclaration(String.format("int[0,%s] cache[%d] = %s;",
				NUM_METHODS,numBlocks(),initCache(NUM_METHODS)));
		system.appendDeclaration(String.format("bool lastHit;"));
	}

	protected StringBuilder initNumBlocks() {
		Vector<Integer> blocksPerMethod = new Vector<Integer>();
		for(int i = 0; i < numMethods; i++) {
			int mBlocks = blocksOf(i);
			if(mBlocks > numBlocks()) {
				throw new AssertionError("Cache too small for method: "+project.getWcetAppInfo().getFlowGraph(i)+
									     " which requires at least " + mBlocks + " blocks, but only "+
									     numBlocks() + " are available in the simulation ");
			}
			blocksPerMethod.add(mBlocks);
		}
		return SystemBuilder.constArray(blocksPerMethod);
	}


}
