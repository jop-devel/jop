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

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.analysis.cache.MethodCacheAnalysis;
import com.jopdesign.wcet.uppaal.AnalysisContextUppaal;
import com.jopdesign.wcet.uppaal.Translator;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import com.jopdesign.wcet.uppaal.UppAalConfig.UppaalCacheApproximation;
import com.jopdesign.wcet.uppaal.WcetSearch;
import com.jopdesign.wcet.uppaal.model.DuplicateKeyException;
import com.jopdesign.wcet.uppaal.model.XmlSerializationException;

public class UppaalAnalysis {

    private Logger logger;
    private WCETTool project;
    private double searchtime = 0.0;
    private double solvertimemax = 0.0;
    private UppAalConfig uppaalConfig;

    public UppaalAnalysis(Logger logger, WCETTool project, File outDir) {
        if (project.getCallstringLength() > 0) {
            throw new AssertionError("Callstrings for UPPAAL analysis are not supported");
        }
        this.uppaalConfig = new UppAalConfig(project.getConfig(), outDir);
        this.logger = logger;
        this.project = project;
    }

    public WcetCost computeWCET(MethodInfo targetMethod, long upperBound) throws IOException, DuplicateKeyException, XmlSerializationException {
        if (uppaalConfig.hasComplexityTreshold()) {
            int cc = project.computeCyclomaticComplexity(targetMethod);
            long treshold = uppaalConfig.getComplexityTreshold();
            if (cc > treshold) {
                return computeWCETWithTreshold(targetMethod, treshold);
            }
        }
        return calculateWCET(targetMethod, upperBound);
    }

    public WcetCost computeWCETWithTreshold(MethodInfo targetMethod, long complexityTreshold) {
        RecursiveWcetAnalysis<AnalysisContextUppaal> sa =
                new RecursiveWcetAnalysis<AnalysisContextUppaal>(
                        project, new UppaalTresholdStrategy(this, complexityTreshold));
        return sa.computeCost(targetMethod,
                new AnalysisContextUppaal(uppaalConfig.getCacheApproximation()));
    }

    public WcetCost calculateWCET(MethodInfo m) throws IOException, DuplicateKeyException, XmlSerializationException {
        return calculateWCET(m, -1);
    }

    public WcetCost calculateWCET(MethodInfo m, long ub) throws IOException, DuplicateKeyException, XmlSerializationException {
        Long upperBound = null;
        if (ub > 0) upperBound = ub + 20;
        logger.info("Starting UppAal translation of " + m.getFQMethodName());
        Translator translator = new Translator(uppaalConfig, project);
        translator.translateProgram(m);
        translator.writeOutput();
        logger.info("model and query can be found in " + uppaalConfig.outDir);
        logger.info("model file: " + translator.getModelFile());
        if (uppaalConfig.hasVerifier()) {
            logger.info("Starting verification");
            WcetSearch search = new WcetSearch(project.getConfig(), translator.getModelFile());
            long start = System.nanoTime();
            long wcet = search.searchWCET(upperBound);
            long end = System.nanoTime();
            searchtime += (end - start) / 1E9;
            solvertimemax = Math.max(solvertimemax, search.getMaxSolverTime());
            return WcetCost.totalCost(wcet);
        } else {
            throw new IOException("No verifier binary available. Skipping search");
        }
    }

    public double getSearchtime() {
        return searchtime;
    }

    public double getSolvertimemax() {
        return solvertimemax;
    }

    static class UppaalTresholdStrategy
            implements RecursiveStrategy<AnalysisContextUppaal, WcetCost> {

        private UppaalAnalysis uppaalAnalysis;
        private long treshold;

        public UppaalTresholdStrategy(UppaalAnalysis uppaalAnalysis, long treshold) {
            this.uppaalAnalysis = uppaalAnalysis;
            this.treshold = treshold;
        }

        /* FIXME: Some code duplication with GlobalAnalysis / LocalAnalysis */
        public WcetCost recursiveCost(
                RecursiveAnalysis<AnalysisContextUppaal, WcetCost> stagedAnalysis,
                ControlFlowGraph.InvokeNode n,
                AnalysisContextUppaal ctx) {
        	
            WCETTool project = stagedAnalysis.getWCETTool();
            MethodInfo invoked = n.getImplementingMethod();
            MethodCacheAnalysis mca = new MethodCacheAnalysis(project);
            
            int cc = project.computeCyclomaticComplexity(invoked);
            long invokeReturnCost = mca.getInvokeReturnMissCost(n.getInvokeSite(), CallString.EMPTY);
            long cacheCost, nonLocalExecCost;
            if (cc <= treshold
                    && ctx.getCacheApprox() != UppaalCacheApproximation.ALWAYS_MISS
                    && !project.getCallGraph().isLeafMethod(invoked)
                    && !stagedAnalysis.isCached(invoked, ctx)
                    ) {
                WcetCost uppaalCost;
                WcetCost ubCost = stagedAnalysis.computeCost(invoked, ctx.withCacheApprox(UppaalCacheApproximation.ALWAYS_MISS));
                try {
                    uppaalAnalysis.logger.info("Complexity of " + invoked + " below treshold: " + cc);
                    uppaalCost = uppaalAnalysis.calculateWCET(invoked, ubCost.getCost());
                } catch (Exception e) {
                    throw new AssertionError("Uppaal analysis failed: " + e);
                }
                stagedAnalysis.recordCost(invoked, ctx, uppaalCost);
                // FIXME: uppaal getCacheCost() is 0 at the moment
                cacheCost = invokeReturnCost + uppaalCost.getCacheCost();
                nonLocalExecCost = uppaalCost.getNonCacheCost();
            } else {
                if (cc > treshold) {
                    uppaalAnalysis.logger.info("Complexity of " + invoked + " above treshold: " + cc);
                }
                WcetCost recCost = stagedAnalysis.computeCost(invoked, ctx);
                cacheCost = recCost.getCacheCost() + invokeReturnCost;
                nonLocalExecCost = recCost.getCost() - recCost.getCacheCost();
            }
            WcetCost cost = new WcetCost();
            cost.addNonLocalCost(nonLocalExecCost);
            cost.addCacheCost(cacheCost);
            WCETTool.logger.debug("Recursive WCET computation [GLOBAL IPET]: " + invoked +
                    ". cummulative cache cost: " + cacheCost +
                    " non local execution cost: " + nonLocalExecCost);
            return cost;
        }
    }
}
