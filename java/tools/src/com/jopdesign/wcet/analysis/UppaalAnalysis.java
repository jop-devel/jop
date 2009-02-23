package com.jopdesign.wcet.analysis;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveWCETStrategy;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.jop.CacheConfig;
import com.jopdesign.wcet.jop.MethodCache;
import com.jopdesign.wcet.jop.CacheConfig.DynCacheApproximation;
import com.jopdesign.wcet.uppaal.Translator;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import com.jopdesign.wcet.uppaal.WcetSearch;
import com.jopdesign.wcet.uppaal.model.DuplicateKeyException;
import com.jopdesign.wcet.uppaal.model.XmlSerializationException;

public class UppaalAnalysis {

	private Logger logger;
	private Project project;
	private File outDir;
	private double searchtime = 0.0;
	private double solvertimemax = 0.0;
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
		Long upperBound = null;
		if(ub > 0) upperBound = ub;
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
			long invokeReturnCost = cache.getInvokeReturnMissCost(proc,project.getFlowGraph(invoker),project.getFlowGraph(invoked));
			long cacheCost, nonLocalExecCost;
			if(   cc <= treshold 
			   && ctx != DynCacheApproximation.ALWAYS_MISS
			   && ! project.getCallGraph().isLeafNode(invoked)
			   && ! stagedAnalysis.isCached(invoked, ctx)
			   ) {
				WcetCost uppaalCost;
				WcetCost ubCost = stagedAnalysis.computeWCET(invoked, DynCacheApproximation.ALWAYS_MISS);
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
