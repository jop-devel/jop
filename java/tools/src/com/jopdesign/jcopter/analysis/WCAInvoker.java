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
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.CallGraph.ContextEdge;
import com.jopdesign.common.code.CallGraph.DUMPTYPE;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ControlFlowGraph.InvokeNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.graphutils.DFSTraverser;
import com.jopdesign.common.graphutils.DFSTraverser.DFSEdgeType;
import com.jopdesign.common.graphutils.DFSTraverser.DFSVisitor;
import com.jopdesign.common.graphutils.DFSTraverser.EmptyDFSVisitor;
import com.jopdesign.common.graphutils.NodeVisitor;
import com.jopdesign.common.graphutils.TopologicalTraverser;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.wcet.ProjectConfig;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.AnalysisContextLocal;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.analysis.LocalAnalysis;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.analysis.RecursiveWcetAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETConfig.CacheCostCalculationMethod;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class WCAInvoker extends ExecFrequencyProvider {

    private final JCopter jcopter;
    private final Set<MethodInfo> wcaTargets;
    private final AnalysisManager analyses;
    private final Map<ExecutionContext, Map<CFGNode,Long>> wcaNodeFlow;
    private final Map<MethodInfo,Long> execCounts;

    private WCETTool wcetTool;
    private RecursiveWcetAnalysis<AnalysisContextLocal> recursiveAnalysis;
    private CacheCostCalculationMethod cacheApproximation;

    private boolean provideWCAExecCount;

    private long lastWCET;

    private static final Logger logger = Logger.getLogger(JCopter.LOG_ANALYSIS+".WCAInvoker");

    public WCAInvoker(AnalysisManager analyses, Set<MethodInfo> wcaTargets, CacheCostCalculationMethod defaultApproximation) {
        this.analyses = analyses;
        this.jcopter = analyses.getJCopter();
        this.wcaTargets = wcaTargets;
        cacheApproximation = defaultApproximation;
        wcetTool = jcopter.getWcetTool();
        wcaNodeFlow = new LinkedHashMap<ExecutionContext, Map<CFGNode, Long>>();
        execCounts = new LinkedHashMap<MethodInfo, Long>();
    }

    public JCopter getJcopter() {
        return jcopter;
    }

    public Set<MethodInfo> getWcaTargets() {
        return wcaTargets;
    }

    public Collection<CallGraph> getWCACallGraphs() {
        return Collections.singleton(wcetTool.getCallGraph());
    }

    public long getLastWCET() {
        return lastWCET;
    }

    ///////////////////////////////////////////////////////////////////////////////
    // Init WCA results, lookup WCA results
    ///////////////////////////////////////////////////////////////////////////////

    public void initTool() throws BadConfigurationException {

        if (wcaTargets.isEmpty()) {
            throw new BadConfigurationException("No WCA target method is given!");
        }

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
        wcetTool.initialize(false, false);
    }

    public void initAnalysis(boolean useMethodCacheStrategy) {
        IPETConfig ipetConfig = new IPETConfig(wcetTool.getConfig());

        RecursiveStrategy<AnalysisContextLocal,WcetCost> strategy;
        if (useMethodCacheStrategy) {
            strategy = analyses.getMethodCacheAnalysis().createRecursiveStrategy(wcetTool, ipetConfig, cacheApproximation);
        } else {
            if (cacheApproximation.needsInterProcIPET()) {
                strategy = new GlobalAnalysis.GlobalIPETStrategy(ipetConfig);
            } else {
                strategy = new LocalAnalysis(wcetTool, ipetConfig);
            }
        }

        recursiveAnalysis = new RecursiveWcetAnalysis<AnalysisContextLocal>(
                    wcetTool, ipetConfig, strategy);

        // Perform initial analysis
        runAnalysis(wcetTool.getCallGraph().getReversedGraph());

        updateWCEP();
    }


    public boolean isWCAMethod(MethodInfo method) {
        return wcetTool.getCallGraph().containsMethod(method);
    }

    public boolean isOnLocalWCETPath(MethodInfo method, InstructionHandle ih) {

        ControlFlowGraph cfg = method.getCode().getControlFlowGraph(false);
        BasicBlockNode block = cfg.getHandleNode(ih, true);

        // we do not have a block.. this is some exception handling path (hopefully..)
        if (block == null) {
            return false;
        }

        for (ExecutionContext node : wcetTool.getCallGraph().getNodes(method)) {
            Long flow = wcaNodeFlow.get(node).get(block);
            if (flow > 0) return true;
        }

        return false;
    }


    ///////////////////////////////////////////////////////////////////////////////
    // Lookup on global WCET path, WCET-path exec counts of methods
    ///////////////////////////////////////////////////////////////////////////////

    public boolean doProvideWCAExecCount() {
        return provideWCAExecCount;
    }

    public void setProvideWCAExecCount(boolean provideWCAExecCount) {
        this.provideWCAExecCount = provideWCAExecCount;
    }

    public boolean isOnWCETPath(MethodInfo method) {
        return getExecCount(method) > 0;
    }

    @Override
    public long getExecCount(MethodInfo method) {
        Long cnt = execCounts.get(method);
        return cnt != null ? cnt : 0;
    }

    @Override
    public long getExecFrequency(InvokeSite invokeSite, MethodInfo invokee) {

        // if the CFG has been devirtualized, we can look up the frequency for a given invoke directly
        if (!invokeSite.isJVMCall()) {
            ControlFlowGraph cfg = invokeSite.getInvoker().getCode().getControlFlowGraph(false);

            for (CFGNode node : cfg.getGraph().vertexSet()) {
                if (!(node instanceof InvokeNode)) continue;
                InvokeNode inv = (InvokeNode) node;

                if (invokee.equals(inv.getImplementingMethod())) {
                    return getExecFrequency(invokeSite.getInvoker(), inv);
                }
            }
        }

        // else we fall back to the virtual invoke node
        return getExecFrequency(invokeSite);
    }

    @Override
    public long getExecFrequency(MethodInfo method, InstructionHandle ih) {
        ControlFlowGraph cfg = method.getCode().getControlFlowGraph(false);
        BasicBlockNode block = cfg.getHandleNode(ih);

        return getExecFrequency(method, block);
    }

    public long getExecFrequency(MethodInfo method, CFGNode block) {
        long flow = 0;

        for (ExecutionContext node : wcetTool.getCallGraph().getNodes(method)) {
            Long value = wcaNodeFlow.get(node).get(block);
            flow += value;
        }

        return flow;
    }

    @Override
    public Set<MethodInfo> getChangeSet() {
        // If this is used as exec count provider, everything is recalculated, so no need to keep track of changes
        return Collections.emptySet();
    }

    ///////////////////////////////////////////////////////////////////////////////
    // Update results
    ///////////////////////////////////////////////////////////////////////////////

    /**
     * Update the WCA results after a set of methods have been changed. The changesets of analyses
     * in the AnalysisManager are checked for changes too.
     *
     * @param changedMethods a set of methods of which the code has been modified.
     * @return a set of all methods for which the path may have changed.
     */
    public Set<MethodInfo> updateWCA(Collection<MethodInfo> changedMethods) {

        // Now we need to clear all results for all callers of the modified methods as well as the modified methods,
        // and recalculate all results
        CallGraph callGraph = wcetTool.getCallGraph();

        final Set<ExecutionContext> rootNodes = new LinkedHashSet<ExecutionContext>();

        for (MethodInfo root : changedMethods) {
            rootNodes.addAll(callGraph.getNodes(root));
        }

        // we also need to recalculate for new nodes.. we simply go down callstring-length from the changed methods
        final int callstringLength = AppInfo.getSingleton().getCallstringLength();

        DFSVisitor<ExecutionContext,ContextEdge> visitor =
                new EmptyDFSVisitor<ExecutionContext,ContextEdge>() {
                    @Override
                    public boolean visitNode(ExecutionContext parent, ContextEdge edge, ExecutionContext node,
                                             DFSEdgeType type, Collection<ContextEdge> outEdges, int depth)
                    {
                        if (type.isFirstVisit() && !wcaNodeFlow.containsKey(node)) {
                            rootNodes.add(node);
                        }
                        return depth <= callstringLength;
                    }
                };

        DFSTraverser<ExecutionContext,ContextEdge> traverser = new DFSTraverser<ExecutionContext, ContextEdge>(visitor);
        traverser.traverse(callGraph.getGraph(), new ArrayList<ExecutionContext>(rootNodes));

        // since we use the cache analysis for the WCA, we need to update all methods for which the
        // classification changed too
        for (MethodInfo method : analyses.getMethodCacheAnalysis().getClassificationChangeSet()) {
            rootNodes.addAll(callGraph.getNodes(method));
        }

        Set<MethodInfo> changed = runAnalysis(wcetTool.getCallGraph().createInvokeGraph(rootNodes, true));

        updateWCEP();

        return changed;
    }

    ///////////////////////////////////////////////////////////////////////////////
    // Private methods
    ///////////////////////////////////////////////////////////////////////////////

    private Set<MethodInfo> runAnalysis(DirectedGraph<ExecutionContext,ContextEdge> reversed) {
        // Phew. The WCA only runs on acyclic callgraphs, we can therefore assume the
        // reversed graph to be a DAG
        TopologicalOrderIterator<ExecutionContext,ContextEdge> topOrder =
                new TopologicalOrderIterator<ExecutionContext, ContextEdge>(reversed);

        Set<MethodInfo> changed = new LinkedHashSet<MethodInfo>();

        while (topOrder.hasNext()) {
            ExecutionContext node = topOrder.next();

            // At times like this I really wish Java would have type aliases ..
            RecursiveWcetAnalysis<AnalysisContextLocal>.LocalWCETSolution sol =
                    recursiveAnalysis.computeSolution(node.getMethodInfo(),
                                new AnalysisContextLocal(cacheApproximation, node.getCallString()));

            wcaNodeFlow.put(node, sol.getNodeFlowVirtual());

            // TODO some logging would be nice, keep target-method WCET for comparison of speedup
            if (node.getMethodInfo().equals(wcetTool.getTargetMethod())) {
                lastWCET = sol.getCost().getCost();
                logger.info("WCET: "+ lastWCET);
            }

            changed.add(node.getMethodInfo());
        }

        return changed;
    }

    private void setWCETOptions(MethodInfo targetMethod, boolean generateReports) {
        Config config = wcetTool.getConfig();
        config.setOption(ProjectConfig.TARGET_METHOD, targetMethod.getMemberID().toString());
        config.setOption(ProjectConfig.DO_GENERATE_REPORTS, generateReports);
        config.setOption(IPETConfig.DUMP_ILP, false);
        config.getDebugGroup().setOption(ProjectConfig.DUMP_TARGET_CALLGRAPH, DUMPTYPE.off);
    }

    private void updateWCEP() {
        if (!provideWCAExecCount) return;

        execCounts.clear();
        for (MethodInfo root : getWcaTargets()) {
            execCounts.put(root, 1L);
        }

        NodeVisitor<ExecutionContext> visitor = new NodeVisitor<ExecutionContext>() {
            @Override
            public boolean visitNode(ExecutionContext context) {
                MethodInfo method = context.getMethodInfo();
                MethodCode code = method.getCode();

                long ec = getExecCount(method);
                // skip methods which are not on the WCET path.. we can ship iterating over the childs too..
                if (ec == 0) return false;

                // iterate over all blocks in the CFG, find all invokes and add block execution counts to invokees
                ControlFlowGraph cfg = method.getCode().getControlFlowGraph(false);
                for (CFGNode node : cfg.getGraph().vertexSet()) {

                    if (node instanceof InvokeNode) {
                        InvokeNode inv = (InvokeNode) node;

                        long ef = getExecFrequency(method, node);

                        for (MethodInfo invokee : inv.getImplementingMethods()) {
                            addExecCount(invokee, ec * ef);
                        }

                    } else if (node instanceof BasicBlockNode) {
                        // check if we have a JVM invoke here (or an invoke not in a dedicated node..)
                        for (InstructionHandle ih : node.getBasicBlock().getInstructions()) {
                            if (!code.isInvokeSite(ih)) continue;

                            long ef = getExecFrequency(method, node);
                            for (MethodInfo invokee : method.getAppInfo().findImplementations(code.getInvokeSite(ih))) {
                                addExecCount(invokee, ec * ef);
                            }

                        }
                    }

                }
                return true;
            }
        };

        TopologicalTraverser<ExecutionContext,ContextEdge> topOrder =
            new TopologicalTraverser<ExecutionContext, ContextEdge>(wcetTool.getCallGraph().getGraph(),visitor);

        topOrder.traverse();
    }

    private void addExecCount(MethodInfo method, long ec) {
        Long count = execCounts.get(method);
        if (count == null) {
            execCounts.put(method, ec);
        } else {
            execCounts.put(method, ec + count);
        }
    }

}
