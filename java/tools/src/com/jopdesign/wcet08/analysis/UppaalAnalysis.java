package com.jopdesign.wcet08.analysis;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.ProcessorModel;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.analysis.RecursiveAnalysis.RecursiveWCETStrategy;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet08.jop.CacheConfig;
import com.jopdesign.wcet08.jop.MethodCache;
import com.jopdesign.wcet08.jop.CacheConfig.DynCacheApproximation;
import com.jopdesign.wcet08.jop.CacheConfig.StaticCacheApproximation;
import com.jopdesign.wcet08.uppaal.Translator;
import com.jopdesign.wcet08.uppaal.UppAalConfig;
import com.jopdesign.wcet08.uppaal.WcetSearch;
import com.jopdesign.wcet08.uppaal.model.DuplicateKeyException;
import com.jopdesign.wcet08.uppaal.model.XmlSerializationException;

public class UppaalAnalysis {

	private MethodInfo target;
	private Logger logger;
	private Project project;
	private File outDir;
	private double searchtime;
	private double solvertimemax;
	public UppaalAnalysis(Logger logger, Project project, File outDir) {
		this.logger = logger;
		this.project = project;
		this.outDir = outDir;
	}
	public WcetCost computeWCET(MethodInfo targetMethod, long upperBound) throws IOException, DuplicateKeyException, XmlSerializationException {
		if(project.getProjectConfig().hasUppaalComplexityTreshold()) {
			int cc = project.computeCyclomaticComplexity(targetMethod);
			int treshold = project.getProjectConfig().getUppaalComplexityTreshold().intValue();
			if(cc > treshold) {
				return computeWCETWithTreshold(targetMethod,treshold);
			}
		}
		return calculateWCET(targetMethod, upperBound);
	}
	public WcetCost computeWCETWithTreshold(MethodInfo targetMethod, int uppaalComplexityTreshold) {
		RecursiveAnalysis<DynCacheApproximation> sa = 
			new RecursiveAnalysis<DynCacheApproximation>(project,new UppaalTresholdStrategy(this,uppaalComplexityTreshold));
		return sa.computeWCET(targetMethod, project.getConfig().getOption(CacheConfig.DYNAMIC_CACHE_APPROX));
	}
	public WcetCost calculateWCET(MethodInfo m) throws IOException, DuplicateKeyException, XmlSerializationException {
		return calculateWCET(m,-1);
	}
	public WcetCost calculateWCET(MethodInfo m, long ub) throws IOException, DuplicateKeyException, XmlSerializationException {
		searchtime = 0;
		Long upperBound = null;
		if(ub > 0) upperBound = ub;
		solvertimemax = 0;
		logger.info("Starting UppAal translation of " + m.getFQMethodName());
		Translator translator = new Translator(project, outDir);
		translator.translateProgram(m);
		translator.writeOutput();
		logger.info("model and query can be found in "+outDir);
		logger.info("model file: "+translator.getModelFile());
		if(UppAalConfig.hasVerifier(project.getConfig())) {
			logger.info("Starting verification");
			WcetSearch search = new WcetSearch(project.getConfig(),translator.getModelFile());
			long start = System.nanoTime();
			long wcet = search.searchWCET(upperBound);
			long end = System.nanoTime();		
			this.searchtime = ((double)(end-start))/1E9;
			this.solvertimemax = search.getMaxSolverTime();
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
	static class UppaalTresholdStrategy implements RecursiveWCETStrategy<DynCacheApproximation> {

		private UppaalAnalysis uppaalAnalysis;
		private int treshold;

		public UppaalTresholdStrategy(UppaalAnalysis uppaalAnalysis, int treshold) {
			this.uppaalAnalysis = uppaalAnalysis;
			this.treshold = treshold;
		}
		/* FIXME: Some code duplication with GlobalAnalysis / LocalAnalysis */
		public WcetCost recursiveWCET(
				RecursiveAnalysis<DynCacheApproximation> stagedAnalysis,
				InvokeNode n,
				DynCacheApproximation ctx) {
			Project project = stagedAnalysis.getProject();
			MethodInfo invoker = n.getBasicBlock().getMethodInfo(); 
			MethodInfo invoked = n.getImplementedMethod();
			ProcessorModel proc = project.getProcessorModel();
			MethodCache cache = proc.getMethodCache();
			int cc = project.computeCyclomaticComplexity(invoked);
			long returnCost = cache.getMissOnReturnCost(proc, project.getFlowGraph(invoker));
			long invokeReturnCost = cache.getInvokeReturnMissCost(proc,project.getFlowGraph(invoker),project.getFlowGraph(invoked));
			long cacheCost, nonLocalExecCost;

			if(cc < treshold && ctx != DynCacheApproximation.ALWAYS_MISS) {
				WcetCost uppaalCost;
				WcetCost ubCost = stagedAnalysis.computeWCET(invoked, DynCacheApproximation.ALWAYS_MISS);
				try {
					uppaalCost = uppaalAnalysis.calculateWCET(invoked,ubCost.getCost());
				} catch (Exception e) {
					throw new AssertionError("Uppaal analysis failed: "+e);
				}
				cacheCost = returnCost + uppaalCost.getCacheCost(); // FIXME: Not accurate at the moment
				nonLocalExecCost = uppaalCost.getNonCacheCost();				
			} else {
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
