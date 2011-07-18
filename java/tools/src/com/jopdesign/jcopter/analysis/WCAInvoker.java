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

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.CallGraph.ContextEdge;
import com.jopdesign.common.code.CallGraph.DUMPTYPE;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.wcet.ProjectConfig;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.AnalysisContextLocal;
import com.jopdesign.wcet.analysis.LocalAnalysis;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.analysis.RecursiveWcetAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.ipet.IPETConfig;
import org.apache.bcel.generic.InstructionHandle;
import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class WCAInvoker {

    private final JCopter jcopter;
    private final Collection<MethodInfo> wcaTargets;
    private final AnalysisManager analyses;
    private final Map<ExecutionContext, Map<CFGNode,Long>> wcaNodeFlow;

    private WCETTool wcetTool;
    private RecursiveWcetAnalysis<AnalysisContextLocal> recursiveAnalysis;
    private boolean useMethodCacheStrategy;

    public WCAInvoker(AnalysisManager analyses, Collection<MethodInfo> wcaTargets) {
        this.analyses = analyses;
        this.jcopter = analyses.getJCopter();
        this.wcaTargets = wcaTargets;
        wcetTool = jcopter.getWcetTool();
        useMethodCacheStrategy = true;
        wcaNodeFlow = new HashMap<ExecutionContext, Map<CFGNode, Long>>();
    }

    public JCopter getJcopter() {
        return jcopter;
    }

    public Collection<MethodInfo> getWcaTargets() {
        return wcaTargets;
    }

    public void initialize() throws BadConfigurationException {

        if (wcaTargets.size() != 1) {
            // TODO To support this, we would either need to split the WCA tool into the tool itself
            //      (which does configuration stuff and holds global results) and a Project class per target
            //      which represents the analysis for one target which holds the wcet-callgraph and is passed
            //      to all analyses,
            //      or we would need to rerun the WCA for each target every time a method is optimized,
            //      or we would need to support multiple roots for the WCETTool callgraph (and its analyses)
            throw new BadConfigurationException("Currently only a single WCA target is supported.");
        }

        setWCETOptions(wcaTargets.iterator().next(), false);

        // Init WCA tool
        wcetTool.initialize();

        IPETConfig ipetConfig = new IPETConfig(wcetTool.getConfig());

        RecursiveStrategy<AnalysisContextLocal,WcetCost> strategy;
        if (useMethodCacheStrategy) {
            strategy = analyses.getMethodCacheAnalysis().createRecursiveStrategy(wcetTool, ipetConfig);
        } else {
            strategy = new LocalAnalysis(wcetTool, ipetConfig);
        }

        recursiveAnalysis = new RecursiveWcetAnalysis<AnalysisContextLocal>(
                    wcetTool, ipetConfig, strategy);

        // Perform initial analysis
        runAnalysis(wcetTool.getCallGraph().getReversedGraph());
    }


    public boolean isWCAMethod(MethodInfo method) {
        return wcetTool.getCallGraph().containsMethod(method);
    }

    public boolean isOnWCETPath(MethodInfo method, InstructionHandle ih) {

        ControlFlowGraph cfg = method.getCode().getControlFlowGraph(false);
        BasicBlockNode block = cfg.getHandleNode(ih);

        for (ExecutionContext node : wcetTool.getCallGraph().getNodes(method)) {
            Long flow = wcaNodeFlow.get(node).get(block);
            if (flow > 0) return true;
        }

        return false;
    }

    /**
     * Update the WCA results after a set of methods have been changed. The changesets of analyses
     * in the AnalysisManager are checked for changes too.
     *
     * @param changedMethods a set of methods of which the code has been modified.
     */
    public void updateWCA(Collection<MethodInfo> changedMethods) {

        Set<MethodInfo> roots = new HashSet<MethodInfo>(changedMethods);

        // since we use the cache analysis for the WCA, we need to update all methods for which the
        // classification changed too
        roots.addAll(analyses.getMethodCacheAnalysis().getClassificationChangeSet());

        // Now we need to clear all results for all callers of the modified methods as well as the modified methods,
        // and recalculate all results
        CallGraph callGraph = wcetTool.getCallGraph();

        List<ExecutionContext> rootNodes = new ArrayList<ExecutionContext>(roots.size());
        for (MethodInfo root : roots) {
            rootNodes.addAll(callGraph.getNodes(root));
        }

        runAnalysis(wcetTool.getCallGraph().createInvokeGraph(rootNodes, true));
    }

    public Collection<CallGraph> getWCACallGraphs() {
        return Collections.singleton(wcetTool.getCallGraph());
    }

    private void runAnalysis(DirectedGraph<ExecutionContext,ContextEdge> reversed) {
        // Phew. The WCA only runs on acyclic callgraphs, we can therefore assume the
        // reversed graph to be a DAG
        TopologicalOrderIterator<ExecutionContext,ContextEdge> topOrder =
                new TopologicalOrderIterator<ExecutionContext, ContextEdge>(reversed);

        MethodCacheAnalysis cacheAnalysis = analyses.getMethodCacheAnalysis();

        while (topOrder.hasNext()) {
            ExecutionContext node = topOrder.next();

            // At times like this I really wish Java would have type aliases ..
            RecursiveWcetAnalysis<AnalysisContextLocal>.LocalWCETSolution sol =
                    recursiveAnalysis.computeSolution(node.getMethodInfo(),
                                cacheAnalysis.getAnalysisContext(node.getCallString()));

            wcaNodeFlow.put(node, sol.getNodeFlow());

            // TODO some logging would be nice, keep target-method WCET for comparison of speedup
        }
    }

    private void setWCETOptions(MethodInfo targetMethod, boolean generateReports) {
        Config config = wcetTool.getConfig();
        config.setOption(ProjectConfig.TARGET_METHOD, targetMethod.getMemberID().toString());
        config.setOption(ProjectConfig.DO_GENERATE_REPORTS, generateReports);
        config.setOption(ProjectConfig.DO_GENERATE_REPORTS, false);
        config.setOption(ProjectConfig.DUMP_TARGET_CALLGRAPH, DUMPTYPE.off);
        config.setOption(IPETConfig.DUMP_ILP, false);
    }
}
