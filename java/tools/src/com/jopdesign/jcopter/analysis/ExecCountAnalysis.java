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

package com.jopdesign.jcopter.analysis;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.CallgraphFilter;
import com.jopdesign.common.code.CallgraphTraverser;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.EmptyCallgraphVisitor;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.code.LoopBound;
import com.jopdesign.common.misc.Ternary;
import org.apache.bcel.generic.InstructionHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ExecCountAnalysis {

    private class ExecCountUpdater extends EmptyCallgraphVisitor {
        @Override
        public boolean visitNode(ExecutionContext node, List<ExecutionContext> childs, boolean isRecursion) {
            if (isRecursion) {
                // TODO What shall we do if we encounter a recursive call? Just ignore it for now..
                //      We could assume a max number of iterations and add them up
                return false;
            }

            updateChilds(node, childs);

            return true;
        }
    }

    private class InlineFilter implements CallgraphFilter {

        private final Set<InvokeSite> newInvokeSites;

        private InlineFilter(Set<InvokeSite> newInvokeSites) {
            this.newInvokeSites = newInvokeSites;
        }

        @Override
        public List<ExecutionContext> getChildren(ExecutionContext context) {
            List<ExecutionContext> childs = new LinkedList<ExecutionContext>();
            for (ExecutionContext child : callGraph.getChildren(context)) {
                for (InvokeSite is : child.getCallString()) {
                    if (newInvokeSites.contains(is)) {
                        childs.add(child);
                    }
                }
            }
            return childs;
        }

        @Override
        public List<ExecutionContext> getParents(ExecutionContext context) {
            return null;
        }
    }

    private final Map<ExecutionContext, Long> roots;
    private final CallGraph callGraph;

    private Map<ExecutionContext,Long> nodeCount;
    private Set<MethodInfo> changeSet;

    ////////////////////////////////////////////////////////////////////////////////////
    // Construction, initialization
    ////////////////////////////////////////////////////////////////////////////////////

    public ExecCountAnalysis(Map<ExecutionContext, Long> roots) {
        this.roots = new HashMap<ExecutionContext, Long>(roots);
        callGraph = AppInfo.getSingleton().getCallGraph();
        nodeCount =  new HashMap<ExecutionContext, Long>();
        changeSet = new HashSet<MethodInfo>(1);
    }

    public void initialize() {
        nodeCount.clear();
        nodeCount.putAll(roots);

        CallgraphTraverser traverser  = new CallgraphTraverser(callGraph, new ExecCountUpdater());
        traverser.traverseDown(roots.keySet(), false);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // Query the analysis results
    ////////////////////////////////////////////////////////////////////////////////////

    public long getExecCount(MethodInfo methodInfo) {
        return getExecCount(new ExecutionContext(methodInfo));
    }

    public long getExecCount(ExecutionContext context) {
        long count = 0;

        for (ExecutionContext ec : callGraph.getNodes(context)) {
            Long c = nodeCount.get(ec);
            if (c != null) {
                count += c;
            }
        }

        return count;
    }

    public long getExecCount(InvokeSite invokeSite) {
        return getExecCount(invokeSite.getInvoker(), invokeSite.getInstructionHandle());
    }

    public long getExecCount(MethodInfo method, InstructionHandle ih) {
        return getExecCount(new ExecutionContext(method), ih);
    }

    public long getExecCount(ExecutionContext context, InstructionHandle ih) {
        return getExecCount(context) * getExecFrequency(context, ih);
    }

    public long getExecFrequency(ExecutionContext context, InstructionHandle ih) {
        MethodInfo method = context.getMethodInfo();

        // By loading the CFG, loopbounds are attached to the blocks if the WCA tool is loaded
        ControlFlowGraph cfg = method.getCode().getControlFlowGraph(false);
        BasicBlockNode node = cfg.getHandleNode(ih);

        // TODO go up every enclosing loop, multiply loop bounds
        // TODO if we do not have loop bounds, use a default value per loop nesting level

        LoopBound lb = node.getBasicBlock().getLoopBound();
        if (lb != null) {
            // TODO we might want to choose between upper/lower bound or even use an average value
            return lb.getUpperBound(context);
        }


        return 1;
    }

    public long getExecFrequency(InvokeSite invokeSite) {
        return getExecFrequency(invokeSite.getInvoker(), invokeSite.getInstructionHandle());
    }

    public long getExecFrequency(MethodInfo method, InstructionHandle ih) {
        return getExecFrequency(new ExecutionContext(method), ih);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // Notify of updates of the underlying callgraph, recalculate
    ////////////////////////////////////////////////////////////////////////////////////

    public void clearChangeSet() {
        changeSet.clear();
    }

    public Set<MethodInfo> getChangeSet() {
        return changeSet;
    }

    /**
     * Update the execution counts after inlining.
     * This must be called after the underlying callgraph has been updated!
     *
     * @param invokeSite the inlined invokesite.
     * @param invokee the inlined method.
     * @param newInvokeSites the set of new invokesites in the invoker
     */
    public void inline(InvokeSite invokeSite, MethodInfo invokee, Set<InvokeSite> newInvokeSites) {

        List<ExecutionContext> queue = new ArrayList<ExecutionContext>();

        for (ExecutionContext context : callGraph.getNodes(invokeSite.getInvoker())) {
            for (ExecutionContext child : callGraph.getChildren(context)) {
                if (child.getCallString().isEmpty() && child.getMethodInfo().equals(invokee)) {
                    // there can be at most one such node in the graph.. remove the total exec count of the
                    // inlined invokesite
                    nodeCount.put(child, nodeCount.get(child) - getExecCount(invokeSite));
                }
                else if (!child.getCallString().isEmpty() && newInvokeSites.contains(child.getCallString().top())) {
                    // This is a new node, sum up the execution counts of all invokesite instances
                    addExecCount(child, getExecCount(context, child.getCallString().top().getInstructionHandle()));

                    queue.add(child);
                }
            }
        }

        // update exec counts for all new nodes. A node is new if it contains one of the new invoke sites
        // We do not need to remove exec counts, since all nodes containing the old invokesite are now no
        // longer in the callgraph. We could however remove those nodes from our data structures in a
        // separate step before the callgraph is updated.

        CallgraphTraverser traverser = new CallgraphTraverser(callGraph, new ExecCountUpdater());
        traverser.setFilter( new InlineFilter(newInvokeSites) );
        traverser.traverseDown(queue, false);

        // Despite all that is going on, the only *method* for which something changes in total is the inlined invokee
        changeSet.add(invokee);
    }


    ////////////////////////////////////////////////////////////////////////////////////
    // Private stuff
    ////////////////////////////////////////////////////////////////////////////////////

    private void addExecCount(ExecutionContext node, long count) {
        Long c = nodeCount.get(node);
        long val = (c == null) ? 0 : c;
        val += count;
        nodeCount.put(node, val);
    }

    private void updateChilds(ExecutionContext context, List<ExecutionContext> childs) {

        long ecCount = nodeCount.get(context);
        List<ExecutionContext> emptyCSNodes = new LinkedList<ExecutionContext>();

        for (ExecutionContext child : childs) {
            long count;

            if (!child.getCallString().isEmpty()) {
                // simple case: if we have a callstring, the top entry is the invokesite in the invoker

                //long count = getExecCount(context, child.getCallString().top().getInstructionHandle());
                // This is faster but needs to be changed if we make getExecCount more precise for instructions
                count = ecCount * getExecFrequency(context, child.getCallString().top().getInstructionHandle());

                addExecCount(child, count);
            } else {
                // tricky case: no callstring, we need to find all invokesites in the invoker and sum up all exec counts
                emptyCSNodes.add(child);
            }
        }

        if (emptyCSNodes.isEmpty()) return;

        for (InvokeSite invokeSite : context.getMethodInfo().getCode().getInvokeSites()) {
            long count = getExecCount(context, invokeSite.getInstructionHandle());

            // for all methods which could be invoked, add the exec count
            for (ExecutionContext child : emptyCSNodes) {
                if (invokeSite.canInvoke(child.getMethodInfo()) == Ternary.TRUE) {
                    addExecCount(child, count);
                }
            }

        }
    }

}
