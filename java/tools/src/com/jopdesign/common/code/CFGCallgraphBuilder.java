/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.common.code;

import com.jopdesign.common.MethodCode;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import org.apache.bcel.generic.InstructionHandle;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is a simple callgraph builder variant which only adds invokesites found in the CFG of methods
 * (which currently excludes code reachable via exception handlers).
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class CFGCallgraphBuilder extends DefaultCallgraphBuilder {

    public CFGCallgraphBuilder() {
    }

    public CFGCallgraphBuilder(int callstringLength) {
        super(callstringLength);
    }

    public CFGCallgraphBuilder(int callstringLength, boolean useCallgraph) {
        super(callstringLength, useCallgraph);
    }

    @Override
    protected Set<InvokeSite> findInvokeSites(MethodCode code) {
        ControlFlowGraph cfg = code.getControlFlowGraph(false);
        Set<InvokeSite> invokeSites = new LinkedHashSet<InvokeSite>();

        // dead nodes are always removed from the CFG, so no need to traverse down from entry, just visit all nodes
        // Basic blocks however are not removed from the block list..
        for (CFGNode node : cfg.vertexSet()) {
            BasicBlock bb = node.getBasicBlock();
            if (bb == null) continue;
            // if resolveVirtualInvokes has been called, we might visit the same block multiple times,
            // but hey, invokeSites is a set anyway..
            for (InstructionHandle ih : bb.getInstructions()) {
                if (code.isInvokeSite(ih)) {
                    invokeSites.add(code.getInvokeSite(ih));
                }
            }
        }

        return invokeSites;
    }
}
