/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
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

import com.jopdesign.common.MethodInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * Provide config options and callback methods to setup and build the callgraph.
 *
 * TODO either define all java.lang.Thread implementations/extensions as root-classes, or add special
 *      edges from Thread.start() calls to Thread.run() implementations
 * TODO optionally return all abstract methods/interfaces too
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class DefaultCallgraphConfig implements CallGraph.CallgraphConfig {

    private int callstringLength;

    public DefaultCallgraphConfig(int callstringLength) {
        this.callstringLength = callstringLength;
    }

    public int getCallstringLength() {
        return callstringLength;
    }

    @Override
    public List<ExecutionContext> getInvokedMethods(ExecutionContext context) {

        CallString callstring = context.getCallString();
        ControlFlowGraph currentCFG = context.getMethodInfo().getCode().getControlFlowGraph();
        List<ExecutionContext> newContexts = new LinkedList<ExecutionContext>();

        for(ControlFlowGraph.CFGNode node : currentCFG.getGraph().vertexSet()) {
            if(node instanceof ControlFlowGraph.InvokeNode) {
                ControlFlowGraph.InvokeNode iNode = (ControlFlowGraph.InvokeNode) node;

                List<MethodInfo> methods = getInvokedMethods(context, iNode);

                for(MethodInfo impl : methods) {
                    //System.out.println("Implemented Methods: "+impl+" from "+iNode.getBasicBlock().getMethodInfo().methodId+" in context "+callstring.toStringVerbose());

                    CallString newCallString = callstring.push(iNode, callstringLength);

                    newContexts.add(new ExecutionContext(impl, newCallString));
                }
            }
        }

        return newContexts;
    }

    protected List<MethodInfo> getInvokedMethods(ExecutionContext context, ControlFlowGraph.InvokeNode iNode) {
        return iNode.getVirtualNode().getImplementedMethods(context.getCallString());
    }

}
