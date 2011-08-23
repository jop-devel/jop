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
package com.jopdesign.wcet.analysis;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGEdge;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ControlFlowGraph.CfgVisitor;
import com.jopdesign.common.code.ControlFlowGraph.ReturnNode;
import com.jopdesign.common.code.LoopBound;
import com.jopdesign.common.graphutils.ProgressMeasure;
import com.jopdesign.common.graphutils.ProgressMeasure.RelativeProgress;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.cache.MethodCacheAnalysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * While implementing progress measure, I found that they can be used for tree based
 * WCET analysis. Why not ?
 * This can be implemented in almost linear (graph size) time,
 * but our suboptimal implementation depends the depth of the loop nest tree.
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class TreeAnalysis {
    private class LocalCostVisitor extends WcetVisitor {
        private AnalysisContext ctx;
		private MethodCacheAnalysis mca;

        public LocalCostVisitor(AnalysisContext c, WCETTool p) {
            super(p);
            mca = new MethodCacheAnalysis(p);
            ctx = c;
        }

        @Override
        public void visitInvokeNode(ControlFlowGraph.InvokeNode n) {

        	MethodInfo method = n.getImplementingMethod();
            /* deal with pruned (infeasible) receivers */
        	if(! methodWCET.containsKey(method)) {
        		WCETTool.logger.info("Pruned InvokeNode: "+n.getImplementingMethod());
        		cost.addNonLocalCost(Long.MIN_VALUE);
        		return;
        	}        	
            visitBasicBlockNode(n);
            cost.addCacheCost(mca.getInvokeReturnMissCost(n.getInvokeSite(), ctx.getCallString()));
            cost.addNonLocalCost(methodWCET.get(method));
        }

        @Override
        public void visitBasicBlockNode(BasicBlockNode n) {
            cost.addLocalCost(project.getWCETProcessorModel().basicBlockWCET(ctx.getExecutionContext(n), n.getBasicBlock()));
        }

		@Override
		public void visitReturnNode(ReturnNode n) {
			
		}
    }

    private class ProgressVisitor implements CfgVisitor {
        private Map<MethodInfo, Long> subProgress;

        public ProgressVisitor(Map<MethodInfo, Long> subProgress) {
            this.subProgress = subProgress;
        }

        private long progress;

        public void visitBasicBlockNode(BasicBlockNode n) {
            progress = 1;
        }

        public void visitInvokeNode(ControlFlowGraph.InvokeNode n) {
            /* deal with pruned (infeasible) receivers */
        	if(! subProgress.containsKey(n.getImplementingMethod())) {
        		WCETTool.logger.info("Pruned InvokeNode: "+n.getImplementingMethod());
        		progress = Long.MIN_VALUE; /* not possible */
        		return;
        	}
            Long aLong = subProgress.get(n.getImplementingMethod());
            long invokedProgress = aLong;
            progress = 1 + invokedProgress;
        }

        public void visitVirtualNode(ControlFlowGraph.VirtualNode n) {
            progress = 1;
        }

        public void visitReturnNode(ControlFlowGraph.ReturnNode n) {
        	visitVirtualNode(n);
        }

        public void visitSummaryNode(ControlFlowGraph.SummaryNode n) {
            progress = 1;
        }

        public long getProgress(CFGNode n) {
            n.accept(this);
            return progress;
        }
    }

    private WCETTool project;
    private HashMap<MethodInfo, Long> methodWCET;
    private Map<MethodInfo, Map<ControlFlowGraph.CFGEdge, RelativeProgress<CFGNode>>> relativeProgress
            = new HashMap<MethodInfo, Map<ControlFlowGraph.CFGEdge, RelativeProgress<CFGNode>>>();
    private HashMap<MethodInfo, Long> maxProgress = new HashMap<MethodInfo, Long>();
    private boolean filterLeafMethods;

    public TreeAnalysis(WCETTool p, boolean filterLeafMethods) {
        this.project = p;
        this.filterLeafMethods = filterLeafMethods;
        computeProgress(p.getTargetMethod(), CallString.EMPTY);
    }

    /* FIXME: filter leaf methods is really a ugly hack,
         * but needs some work to play nice with uppaal eliminate-leaf-methods optimizations
         */

    public void computeProgress(MethodInfo targetMethod, CallString cs) {
        List<MethodInfo> reachable = project.getCallGraph().getReachableImplementations(targetMethod,cs);
        Collections.reverse(reachable);
        for (MethodInfo mi : reachable) {
            ControlFlowGraph cfg = project.getFlowGraph(mi);
            Map<CFGNode, Long> localProgress = new HashMap<CFGNode, Long>();
            ProgressVisitor progressVisitor = new ProgressVisitor(maxProgress);
            for (CFGNode n : cfg.vertexSet()) {
                localProgress.put(n, progressVisitor.getProgress(n));
            }
            ProgressMeasure<CFGNode, CFGEdge> pm =
                    new ProgressMeasure<CFGNode, ControlFlowGraph.CFGEdge>(cfg.getGraph(), cfg.getLoopColoring(),
                            extractUBs(cfg.buildLoopBoundMap()), localProgress);
            long progress = pm.getMaxProgress().get(cfg.getExit());
            /* FIXME: _UGLY_ hack */
            if (filterLeafMethods && cfg.isLeafMethod()) {
                maxProgress.put(mi, 0L);
            } else {
                maxProgress.put(mi, progress);
            }
            relativeProgress.put(mi, pm.computeRelativeProgress());
        }
        System.out.println("Progress Measure (max): " + maxProgress.get(targetMethod));
    }

    public Map<MethodInfo, Map<ControlFlowGraph.CFGEdge, RelativeProgress<CFGNode>>> getRelativeProgressMap() {
        return this.relativeProgress;
    }

    public Long getMaxProgress(MethodInfo mi) {
        return this.maxProgress.get(mi);
    }

    private Map<CFGNode, Long> extractUBs(Map<CFGNode, LoopBound> loopBounds) {

    	Map<CFGNode, Long> ubMap = new HashMap<CFGNode, Long>();
    	
    	for (Entry<CFGNode, LoopBound> entry : loopBounds.entrySet()) {
    		
    		MethodInfo mi = entry.getKey().getControlFlowGraph().getMethodInfo();
            ExecutionContext eCtx = new ExecutionContext(mi);
            ubMap.put(entry.getKey(), entry.getValue().getUpperBound(eCtx));
        }
        return ubMap;
    }

    public long computeWCET(MethodInfo targetMethod) {
        this.methodWCET = new HashMap<MethodInfo, Long>();
        List<MethodInfo> reachable = project.getCallGraph().getReachableImplementations(targetMethod);
        Collections.reverse(reachable);
        for (MethodInfo mi : reachable) {
            ControlFlowGraph cfg = project.getFlowGraph(mi);
            Map<CFGNode, Long> localCost = new HashMap<CFGNode, Long>();
            LocalCostVisitor lcv = new LocalCostVisitor(new AnalysisContextCallString(CallString.EMPTY), project);
            for (CFGNode n : cfg.vertexSet()) {
                localCost.put(n, lcv.computeCost(n).getCost());
            }
            ProgressMeasure<CFGNode, ControlFlowGraph.CFGEdge> pm =
                    new ProgressMeasure<CFGNode, ControlFlowGraph.CFGEdge>(cfg.getGraph(), cfg.getLoopColoring(),
                            extractUBs(cfg.buildLoopBoundMap()), localCost);
            long wcet = pm.getMaxProgress().get(cfg.getExit());
            methodWCET.put(mi, wcet);
        }
        return methodWCET.get(targetMethod);
    }

}
