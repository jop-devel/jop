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
package com.jopdesign.wcet.uppaal.translator;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.graphutils.ProgressMeasure.RelativeProgress;
import com.jopdesign.common.misc.BadGraphException;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.TreeAnalysis;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import com.jopdesign.wcet.uppaal.translator.cache.CacheSimBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Translate Java threads, either using one process per methods or
 * a single process
 */
public abstract class JavaTranslator {
    /* map methods to node automatons */
    protected Map<MethodInfo, SubAutomaton> methodAutomata;
    protected List<MethodInfo> methodInfos;
    protected UppAalConfig config;
    protected WCETTool project;
    protected MethodInfo root;
    protected SystemBuilder systemBuilder;
    protected CacheSimBuilder cacheSim;
    protected HashMap<MethodInfo, Integer> methodIDs;
    private Map<MethodInfo, Map<ControlFlowGraph.CFGEdge, RelativeProgress<CFGNode>>> progressMap;

    public WCETTool getProject() {
        return project;
    }

    public UppAalConfig getConfig() {
        return config;
    }

    public List<MethodInfo> getMethodInfos() {
        return methodInfos;
    }

    public int getMethodID(MethodInfo m) {
        return methodIDs.get(m);
    }

    public CacheSimBuilder getCacheSim() {
        return cacheSim;
    }

    public Map<ControlFlowGraph.CFGEdge, RelativeProgress<CFGNode>> getProgress(MethodInfo mi) {
        return progressMap.get(mi);
    }

    public void addMethodAutomaton(MethodInfo mi, SubAutomaton auto) {
        this.methodAutomata.put(mi, auto);
    }

    public SubAutomaton getMethodAutomaton(MethodInfo mi) {
        return methodAutomata.get(mi);
    }

    public JavaTranslator(UppAalConfig c, WCETTool p, MethodInfo root) {
        this.config = c;
        this.project = p;
        this.root = root;
        /* Get callgraph */
        CallGraph callGraph = project.getCallGraph();
        // logger.info("Call stack depth: "+callGraph.getMaxHeight());
        if (p.getCallstringLength() != 0) {
            throw new AssertionError("Callstrings are not support for UppAal translation");
        }
        this.methodInfos = callGraph.getReachableImplementations(root, CallString.EMPTY);
        if (!methodInfos.get(0).equals(root)) {
            throw new AssertionError("Bad callgraph: reachable implementations needs to return root as first element");
        }
        /* Create system builder */
        systemBuilder = new SystemBuilder(config, project, callGraph.getMaxHeight() + 1, methodInfos);
        this.methodIDs = new HashMap<MethodInfo, Integer>();
        for (int i = 0; i < methodInfos.size(); i++) {
            MethodInfo mi = methodInfos.get(i);
            methodIDs.put(mi, i);
            ControlFlowGraph cfg = project.getFlowGraph(mi);
            /* insert summary nodes if request */
            if (config.collapseLeaves) {
                try {
                    cfg.insertSummaryNodes();
                } catch (BadGraphException e) {
                    throw new AssertionError("Failed to insert summary nodes: " + e);
                }
            }
        }
        this.methodAutomata = new HashMap<MethodInfo, SubAutomaton>();
        /* Cache sim */
        this.cacheSim = systemBuilder.getCacheSim();
        /* Progress measure */
        TreeAnalysis ta = new TreeAnalysis(project, config.collapseLeaves);
        this.progressMap = ta.getRelativeProgressMap();
        if (config.useProgressMeasure) {
            // TODO: The model checker needs some extra progress space,
            // if he explores loop bodies another time to often (*2 should be safe).
            long maxProgress = ta.getMaxProgress(project.getTargetMethod());
            systemBuilder.declareVariable("int[0," + maxProgress * 2 + "]", "pm", "0");
            systemBuilder.addProgressMeasure("pm");
        }
    }

    protected abstract void translate();

    protected void translateMethod(TemplateBuilder tb,
                                   SubAutomaton methodAutomaton,
                                   int mId,
                                   MethodInfo mi,
                                   InvokeBuilder invokeBuilder) {
        /* get flow graph */
        ControlFlowGraph cfg = project.getFlowGraph(mi);
        MethodBuilder mBuilder =
                new MethodBuilder(this, tb, invokeBuilder,
                        methodAutomaton, mId, cfg);
        mBuilder.build();
    }

    public SystemBuilder getSystem() {
        translate();
        return this.systemBuilder;
    }
}
