package com.jopdesign.wcet.analysis;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveWCETStrategy;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.jop.MethodCache;
import com.jopdesign.wcet.uppaal.AnalysisContextUppaal;
import com.jopdesign.wcet.uppaal.Translator;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import com.jopdesign.wcet.uppaal.WcetSearch;
import com.jopdesign.wcet.uppaal.UppAalConfig.UppaalCacheApproximation;
import com.jopdesign.wcet.uppaal.model.DuplicateKeyException;
import com.jopdesign.wcet.uppaal.model.XmlSerializationException;

public class UppaalAnalysis {

	private Logger logger;
	private Project project;
	private double searchtime = 0.0;
	private double solvertimemax = 0.0;
	private UppAalConfig uppaalConfig;
	public UppaalAnalysis(Logger logger, Project project, File outDir) {
		this.uppaalConfig = new UppAalConfig(project.getConfig(), outDir);
		this.logger = logger;
		this.project = project;
	}
	public WcetCost computeWCET(MethodInfo targetMethod, long upperBound) throws IOException, DuplicateKeyException, XmlSerializationException {
		if(uppaalConfig.hasComplexityTreshold()) {
			int cc = project.computeCyclomaticComplexity(targetMethod);
			long treshold = uppaalConfig.getComplexityTreshold();
			if(cc > treshold) {
				return computeWCETWithTreshold(targetMethod,treshold);
			}
		}
		return calculateWCET(targetMethod, upperBound);
	}
	public WcetCost computeWCETWithTreshold(MethodInfo targetMethod, long complexityTreshold) {
		RecursiveAnalysis<AnalysisContextUppaal> sa =
			new RecursiveAnalysis< AnalysisContextUppaal>(
					project, new UppaalTresholdStrategy(this,complexityTreshold));
		return sa.computeWCET(targetMethod,
							  new AnalysisContextUppaal(uppaalConfig.getCacheApproximation()));
	}
	public WcetCost calculateWCET(MethodInfo m) throws IOException, DuplicateKeyException, XmlSerializationException {
		return calculateWCET(m,-1);
	}
	public WcetCost calculateWCET(MethodInfo m, long ub) throws IOException, DuplicateKeyException, XmlSerializationException {
		Long upperBound = null;
		if(ub > 0) upperBound = ub + 20;
		logger.info("Starting UppAal translation of " + m.getFQMethodName());
		Translator translator = new Translator(uppaalConfig, project);
		translator.translateProgram(m);
		translator.writeOutput();
		logger.info("model and query can be found in "+uppaalConfig.outDir);
		logger.info("model file: "+translator.getModelFile());
		if(uppaalConfig.hasVerifier()) {
			logger.info("Starting verification");
			WcetSearch search = new WcetSearch(project.getConfig(),translator.getModelFile());
			long start = System.nanoTime();
			long wcet = search.searchWCET(upperBound);
			long end = System.nanoTime();
			searchtime += ((double)(end-start))/1E9;
			solvertimemax = Math.max(solvertimemax,search.getMaxSolverTime());
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
	static class UppaalTresholdStrategy implements RecursiveWCETStrategy<AnalysisContextUppaal> {

		private UppaalAnalysis uppaalAnalysis;
		private long treshold;

		public UppaalTresholdStrategy(UppaalAnalysis uppaalAnalysis, long treshold) {
			this.uppaalAnalysis = uppaalAnalysis;
			this.treshold = treshold;
		}
		/* FIXME: Some code duplication with GlobalAnalysis / LocalAnalysis */
		public WcetCost recursiveWCET(
				RecursiveAnalysis<AnalysisContextUppaal> stagedAnalysis,
				InvokeNode n,
				AnalysisContextUppaal ctx) {
			Project project = stagedAnalysis.getProject();
			MethodInfo invoker = n.getBasicBlock().getMethodInfo();
			MethodInfo invoked = n.getImplementedMethod();
			ProcessorModel proc = project.getProcessorModel();
			MethodCache cache = proc.getMethodCache();
			int cc = project.computeCyclomaticComplexity(invoked);
			long invokeReturnCost = cache.getInvokeReturnMissCost(proc,project.getFlowGraph(invoker),project.getFlowGraph(invoked));
			long cacheCost, nonLocalExecCost;
			if(   cc <= treshold
			   && ctx.getCacheApprox() != UppaalCacheApproximation.ALWAYS_MISS
			   && ! project.getCallGraph().isLeafNode(invoked)
			   && ! stagedAnalysis.isCached(invoked, ctx)
			   ) {
				WcetCost uppaalCost;
				WcetCost ubCost = stagedAnalysis.computeWCET(invoked, ctx.withCacheApprox(UppaalCacheApproximation.ALWAYS_MISS));
				try {
					uppaalAnalysis.logger.info("Complexity of "+invoked+" below treshold: "+cc);
					uppaalCost = uppaalAnalysis.calculateWCET(invoked,ubCost.getCost());
				} catch (Exception e) {
					throw new AssertionError("Uppaal analysis failed: "+e);
				}
				stagedAnalysis.recordCost(invoked,ctx,uppaalCost);
				// FIXME: uppaal getCacheCost() is 0 at the moment
				cacheCost = invokeReturnCost + uppaalCost.getCacheCost();
				nonLocalExecCost = uppaalCost.getNonCacheCost();
			} else {
				if(cc > treshold) {
					uppaalAnalysis.logger.info("Complexity of "+invoked+" above treshold: "+cc);
				}
				WcetCost recCost = stagedAnalysis.computeWCET(invoked, ctx);
				cacheCost = recCost.getCacheCost() + invokeReturnCost ;
				nonLocalExecCost = recCost.getCost() - recCost.getCacheCost();
			}
			WcetCost cost = new WcetCost();
			cost.addNonLocalCost(nonLocalExecCost);
			cost.addCacheCost(cacheCost);
			Project.logger.info("Recursive WCET computation [GLOBAL IPET]: " + invoked.getMethod() +
			        		    ". cummulative cache cost: "+cacheCost+
					            " non local execution cost: "+nonLocalExecCost);
			return cost;
		}
	}
}
