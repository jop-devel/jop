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
import com.jopdesign.common.code.DefaultCallgraphBuilder;
import com.jopdesign.common.config.Config.BadConfigurationError;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.analysis.MethodCacheAnalysis.AnalysisType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Container for various analyses, provide some methods to invalidate/.. all analyses.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AnalysisManager {

    private final JCopter jcopter;

    private WCAInvoker wcaInvoker;
    private ExecCountAnalysis execCountAnalysis;
    private MethodCacheAnalysis methodCacheAnalysis;
    private Map<MethodInfo,StacksizeAnalysis> stacksizeMap;

    private CallGraph targetCallGraph;

    public AnalysisManager(JCopter jcopter) {
        this.jcopter = jcopter;
        stacksizeMap = new HashMap<MethodInfo, StacksizeAnalysis>();
    }

    public JCopter getJCopter() {
        return jcopter;
    }

    /**
     * Quick'n'dirty initialization of all analyses.
     * If the analyses get more options or get more complex in the future, this will need some work.
     *
     * @param targets the root methods to use for all analyses and the callgraph.
     * @param cacheAnalysisType cache analysis type
     * @param wcaRoots if not null, initialize the WCA invoker with these roots.
     */
    public void initAnalyses(Collection<MethodInfo> targets, AnalysisType cacheAnalysisType,
                             Collection<MethodInfo> wcaRoots)
    {
        targetCallGraph = CallGraph.buildCallGraph(targets,
                new DefaultCallgraphBuilder(AppInfo.getSingleton().getCallstringLength()));

        // TODO we might want to classify methods depending on whether they are reachable from the wcaRoots
        //      for all non-wca-methods we might want to use different initial analysis data, e.g.
        //      if we use the WCA, we might want to use the IPET WCA to initialize the execCountAnalysis for
        //      wca-methods

        execCountAnalysis = new ExecCountAnalysis(targetCallGraph);
        methodCacheAnalysis = new MethodCacheAnalysis(this, cacheAnalysisType, targetCallGraph);

        if (wcaRoots != null) {
            wcaInvoker = new WCAInvoker(this, wcaRoots);
            try {
                wcaInvoker.initialize();
            } catch (BadConfigurationException e) {
                // TODO or maybe just throw the exception up a few levels more?
                throw new BadConfigurationError(e.getMessage(), e);
            }

            // TODO maybe we could use the initial WCA results to make other analyses more precise if IPET is
            //      used for the initial WCET analysis. Either handle this here or in wcaInvoker.initialize()?
            //      Letting the other analyses use the WCA results is the nicer option, something like
            //      execCountAnalysis.loadWCAResults(wcaInvoker);
        }
    }

    public CallGraph getTargetCallGraph() {
        return targetCallGraph;
    }

    public CallGraph getAppInfoCallGraph() {
        return AppInfo.getSingleton().getCallGraph();
    }

    public Set<MethodInfo> getWCAMethods() {
        if (wcaInvoker == null) return Collections.emptySet();

        Set<MethodInfo> methods = new HashSet<MethodInfo>();
        for (MethodInfo root : wcaInvoker.getWcaTargets()) {
            methods.addAll( targetCallGraph.getReachableImplementationsSet(root) );
        }

        return methods;
    }

    public Collection<CallGraph> getWCACallGraphs() {
        return wcaInvoker != null ? wcaInvoker.getWCACallGraphs() : Collections.<CallGraph>emptySet();
    }

    public List<CallGraph> getCallGraphs() {
        List<CallGraph> graphs = new ArrayList<CallGraph>(4);
        graphs.add(getAppInfoCallGraph());
        graphs.add(getTargetCallGraph());
        graphs.addAll(getWCACallGraphs());
        return graphs;
    }

    public ExecCountAnalysis getExecCountAnalysis() {
        return execCountAnalysis;
    }

    public MethodCacheAnalysis getMethodCacheAnalysis() {
        return methodCacheAnalysis;
    }


    public StacksizeAnalysis getStacksizeAnalysis(MethodInfo methodInfo) {
        StacksizeAnalysis stacksize = stacksizeMap.get(methodInfo);
        if (stacksize == null) {
            stacksize = new StacksizeAnalysis(methodInfo);
            stacksize.analyze();
            stacksizeMap.put(methodInfo, stacksize);
        }
        return stacksize;
    }

    public void clearChangeSets() {
        if (execCountAnalysis != null) execCountAnalysis.clearChangeSet();
        if (methodCacheAnalysis != null) methodCacheAnalysis.clearChangeSet();
    }

    public WCAInvoker getWCAInvoker() {
        return wcaInvoker;
    }
}
