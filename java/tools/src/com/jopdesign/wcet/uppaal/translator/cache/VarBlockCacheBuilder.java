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

import com.jopdesign.common.MethodInfo;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.jop.VarBlockCache;
import com.jopdesign.wcet.uppaal.model.NTASystem;
import com.jopdesign.wcet.uppaal.translator.SystemBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class VarBlockCacheBuilder extends DynamicCacheBuilder {
    protected WCETTool project;
    protected VarBlockCache cache;
    protected Set<MethodInfo> methods;

    public VarBlockCacheBuilder(WCETTool p, VarBlockCache cache, Set<MethodInfo> methods) {
        this.project = p;
        this.cache = cache;
        this.methods = methods;
    }

    protected abstract int numBlocks();

    protected abstract StringBuilder initCache(String NUM_METHODS);

    protected int blocksOf(MethodInfo method) {
        // FIXME old code used ids instead of MethodInfo, check for correctness !
        return project.getWCETProcessorModel().getMethodCache().requiredNumberOfBlocks(
                            method.getCode().getNumberOfWords() );
    }

    @Override
    public void appendDeclarations(NTASystem system, String NUM_METHODS) {
        system.appendDeclaration(String.format("const int NUM_BLOCKS[%s] = %s;",
                NUM_METHODS, initNumBlocks()));
        system.appendDeclaration(String.format("int[0,%s] cache[%d] = %s;",
                NUM_METHODS, numBlocks(), initCache(NUM_METHODS)));
        system.appendDeclaration(String.format("bool lastHit;"));
    }

    protected StringBuilder initNumBlocks() {
        List<Integer> blocksPerMethod = new ArrayList<Integer>();
        for (MethodInfo method : methods) {
            if (method.isAbstract()) {
                // TODO should we skip if the method is native too?
                continue;
            }
            int mBlocks = blocksOf(method);
            if (mBlocks > numBlocks()) {
                throw new AssertionError("Cache too small for method: " + method +
                        " which requires at least " + mBlocks + " blocks, but only " +
                        numBlocks() + " are available in the simulation ");
            }
            blocksPerMethod.add(mBlocks);
        }
        return SystemBuilder.constArray(blocksPerMethod);
    }


}
