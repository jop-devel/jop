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
		return project.getProcessorModel().getMethodCache().requiredNumberOfBlocks(project.getAppInfo().getFlowGraph(id).getNumberOfWords());
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
				throw new AssertionError("Cache too small for method: "+project.getAppInfo().getFlowGraph(i)+
									     " which requires at least " + mBlocks + " blocks, but only "+
									     numBlocks() + " are available in the simulation ");
			}
			blocksPerMethod.add(mBlocks);
		}
		return SystemBuilder.constArray(blocksPerMethod);
	}


}
