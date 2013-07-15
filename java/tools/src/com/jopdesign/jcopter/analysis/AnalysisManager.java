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
import com.jopdesign.common.code.CallGraph.DUMPTYPE;
import com.jopdesign.common.code.DefaultCallgraphBuilder;
import com.jopdesign.common.config.Config.BadConfigurationError;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.analysis.MethodCacheAnalysis.AnalysisType;
import com.jopdesign.wcet.ipet.IPETConfig.CacheCostCalculationMethod;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Container for various analyses, provide some methods to invalidate/.. all analyses.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AnalysisManager {

    private static final Logger logger = Logger.getLogger(JCopter.LOG_ANALYSIS+".AnalysisManager");

    private final JCopter jcopter;

    private WCAInvoker wcaInvoker;
    private ExecFrequencyAnalysis execFreqAnalysis;
    private MethodCacheAnalysis methodCacheAnalysis;
    private Map<MethodInfo,StacksizeAnalysis> stacksizeMap;

    private CallGraph targetCallGraph;

    public AnalysisManager(JCopter jcopter) {
        this.jcopter = jcopter;
        stacksizeMap = new LinkedHashMap<MethodInfo, StacksizeAnalysis>();
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
     * @param updateWCEP if true, let the wcaInvoker provide a global WCET path and keep it up-to-date
     */
    public void initAnalyses(Set<MethodInfo> targets, AnalysisType cacheAnalysisType,
                             CacheCostCalculationMethod cacheApproximation, boolean useMethodCacheStrategy,
                             Set<MethodInfo> wcaRoots, boolean updateWCEP)
    {
        logger.info("Initializing analyses..");

        Set<MethodInfo> allTargets = new LinkedHashSet<MethodInfo>(targets);
        if (wcaRoots != null) {
            // Just make sure the WCA callgraph is contained in the target graph..
            allTargets.addAll(wcaRoots);

            wcaInvoker = new WCAInvoker(this, wcaRoots, cacheApproximation);
            wcaInvoker.setProvideWCAExecCount(updateWCEP);
            try {
                // need to initialize the WCA Tool before the other analyses since we need the WCA callgraph
                wcaInvoker.initTool();
            } catch (BadConfigurationException e) {
                // TODO or maybe just throw the exception up a few levels more?
                throw new BadConfigurationError(e.getMessage(), e);
            }
        }

        if (wcaRoots != null && wcaRoots.equals(allTargets) && wcaInvoker.getWCACallGraphs().size() == 1) {
            targetCallGraph = wcaInvoker.getWCACallGraphs().iterator().next();
        } else {
            logger.info("Initializing Target Callgraph");
            targetCallGraph = CallGraph.buildCallGraph(allTargets,
                new DefaultCallgraphBuilder(AppInfo.getSingleton().getCallstringLength()));
        }

        // TODO we might want to classify methods depending on whether they are reachable from the wcaRoots
        //      for all non-wca-methods we might want to use different initial analysis data, e.g.
        //      if we use the WCA, we might want to use the IPET WCA to initialize the execCountAnalysis for
        //      wca-methods

        // We can do this as first step (after the callgraph has been created) since it does not use the ExecFrequencyAnalysis
        logger.info("Initializing MethodCacheAnalysis");
        methodCacheAnalysis = new MethodCacheAnalysis(jcopter, cacheAnalysisType, targetCallGraph);
        methodCacheAnalysis.initialize();

        if (wcaRoots != null) {
            logger.info("Initializing WCAInvoker");
            wcaInvoker.initAnalysis(useMethodCacheStrategy);
        }

        // TODO in fact, we might not even need this if we only use the wcaInvoker as provider or some other provider
        logger.info("Initializing ExecFrequencyAnalysis");
        execFreqAnalysis = new ExecFrequencyAnalysis(this, targetCallGraph);
        execFreqAnalysis.initialize();
    }

    public boolean hasWCATargetsOnly() {
        if (wcaInvoker == null) return false;
        return targetCallGraph.getRootMethods().equals(wcaInvoker.getWcaTargets());
    }

    public boolean useWCAInvoker() {
        return wcaInvoker != null;
    }

    public CallGraph getTargetCallGraph() {
        return targetCallGraph;
    }

    public CallGraph getAppInfoCallGraph() {
        return AppInfo.getSingleton().getCallGraph();
    }

    public Set<MethodInfo> getWCAMethods() {
        if (wcaInvoker == null) return Collections.emptySet();

        Set<MethodInfo> methods = new LinkedHashSet<MethodInfo>();
        for (MethodInfo root : wcaInvoker.getWcaTargets()) {
            methods.addAll( targetCallGraph.getReachableImplementationsSet(root) );
        }

        return methods;
    }

    public boolean isWCAMethod(MethodInfo method) {
        if (wcaInvoker == null) return false;

        return wcaInvoker.isWCAMethod(method);
    }

    public Collection<CallGraph> getWCACallGraphs() {
        return wcaInvoker != null ? wcaInvoker.getWCACallGraphs() : Collections.<CallGraph>emptySet();
    }

    /**
     * @return all callgraphs used by the analyses (including the AppInfo callgraph) which are not backed by other callgraphs.
     */
    public Set<CallGraph> getCallGraphs() {
        Set<CallGraph> graphs = new LinkedHashSet<CallGraph>(4);
        graphs.add(getAppInfoCallGraph());
        graphs.add(getTargetCallGraph());
        graphs.addAll(getWCACallGraphs());
        return graphs;
    }

    public void dumpTargetCallgraph(String name, boolean full) {
        try {
            targetCallGraph.dumpCallgraph(jcopter.getJConfig().getConfig(), name, full ? "full" : "merged",
                    targetCallGraph.getRootNodes(), full ? DUMPTYPE.full : DUMPTYPE.merged, false);
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    public ExecFrequencyAnalysis getExecFrequencyAnalysis() {
        return execFreqAnalysis;
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
        if (execFreqAnalysis != null) execFreqAnalysis.clearChangeSet();
        if (methodCacheAnalysis != null) methodCacheAnalysis.clearChangeSet();
    }

    public WCAInvoker getWCAInvoker() {
        return wcaInvoker;
    }

}
