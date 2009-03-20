/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006-2008, Martin Schoeberl (martin@jopdesign.com)
  Copyright (C) 2008-2009, Benedikt Huber (benedikt.huber@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/* Notes: WCET times reported by JOP (noted if JopSIM+cache-timing differs)
 * Method.java: 12039
 * StartKfl.java: 3048-11200
 * StartLift.java: 4638-4772 (JopSIM: 4636-4774) 
 */

package com.jopdesign.wcet;

import org.apache.log4j.Logger;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.analysis.RecursiveAnalysis;
import com.jopdesign.wcet.analysis.UppaalAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveWCETStrategy;
import com.jopdesign.wcet.config.Config;
import com.jopdesign.wcet.config.Option;
import com.jopdesign.wcet.graphutils.MiscUtils;
import com.jopdesign.wcet.ipet.LpSolveWrapper;
import com.jopdesign.wcet.jop.CacheConfig;
import com.jopdesign.wcet.jop.CacheConfig.StaticCacheApproximation;
import com.jopdesign.wcet.report.Report;
import com.jopdesign.wcet.report.ReportConfig;
import com.jopdesign.wcet.uppaal.UppAalConfig;

import static com.jopdesign.wcet.ExecHelper.timeDiff;
/**
 * WCET Analysis for JOP - Executable
 */
public class WCETAnalysis {
	private static final String CONFIG_FILE_PROP = "config";
	public static final String VERSION = "1.0.0"; 
	private static final Logger tlLogger = Logger.getLogger(WCETAnalysis.class);
	public static Option<?>[][] options = {
		ProjectConfig.projectOptions,
		CacheConfig.cacheOptions,
		UppAalConfig.uppaalOptions,
		ReportConfig.options,
	};

	public static void main(String[] args) {
		Config config = Config.instance();
		config.addOptions(options);
		ExecHelper exec = new ExecHelper(WCETAnalysis.class,VERSION,tlLogger,CONFIG_FILE_PROP);
		exec.initTopLevelLogger();       /* Console logging for top level messages */
		exec.loadConfig(args);           /* Load config */
		WCETAnalysis inst = new WCETAnalysis(config);
		/* run */
		if(! inst.run(exec)) exec.bail("WCET Analysis failed");
		tlLogger.info("WCET Analysis finished.");
	}
	private Config config;
	private Project project;
	public WCETAnalysis(Config c) {
		this.config = c;
	}
	private boolean run(ExecHelper exec) {
		project = null;
		ProjectConfig pConfig = new ProjectConfig(config);
		/* Initialize */
		try {
			project = new Project(pConfig);
			project.setTopLevelLooger(tlLogger);
			Report.initVelocity(config);     /* Initialize velocity engine */
			tlLogger.info("Loading project");
			project.load();
			MethodInfo largestMethod = project.getProcessorModel().getMethodCache().checkCache();
			int minWords = MiscUtils.bytesToWords(largestMethod.getCode().getCode().length);
			System.out.println("Minimal Cache Size for target method(words): " 
					         + minWords
					         + " because of "+largestMethod.getFQMethodName());
			project.recordMetric("min-cache-size",largestMethod.getFQMethodName(),minWords);
		} catch (Exception e) {
			exec.logException("Loading project", e);
			return false;
		}
		/* Perf-Test */
//		for(int i = 0; i < 50; i++) { 
//			RecursiveAnalysis<StaticCacheApproximation> an = 
//				new RecursiveAnalysis<StaticCacheApproximation>(project,new RecursiveAnalysis.LocalIPETStrategy());
//			an.computeWCET(project.getTargetMethod(),StaticCacheApproximation.ALWAYS_HIT);			
//		}
//		System.err.println("Total solver time (50): "+LpSolveWrapper.getSolverTime());
//		System.exit(1);
		// new ETMCExport(project).export(project.getOutFile("Spec_"+project.getProjectName()+".txt")); 
		/* Run */
		boolean succeed = false;
		try {
			/* Analysis */
			project.setGenerateWCETReport(false); /* generate reports later */
			tlLogger.info("Cyclomatic complexity: "+project.computeCyclomaticComplexity(project.getTargetMethod()));
			WcetCost mincachecost, ah, am, wcet;
			
			/* Perform a few standard analysis (MIN_CACHE_COST, ALWAYS_HIT, ALWAYS_MISS) */
			{	
				long start,stop;
				/* always hit */
				RecursiveAnalysis<StaticCacheApproximation> an = 
					new RecursiveAnalysis<StaticCacheApproximation>(project,new RecursiveAnalysis.LocalIPETStrategy());
				LpSolveWrapper.resetSolverTime();
				start = System.nanoTime();
				ah = an.computeWCET(project.getTargetMethod(),StaticCacheApproximation.ALWAYS_HIT);			
				stop  = System.nanoTime();
				reportSpecial("always-hit",ah,start,stop,LpSolveWrapper.getSolverTime());
				/* always miss */
				/* FIXME: We don't have  report generation for UPPAAL and global analysis yet */
				if(   project.getProjectConfig().useUppaal()
				   || project.getConfig().getOption(CacheConfig.STATIC_CACHE_APPROX).needsInterProcIPET()) { 
					project.setGenerateWCETReport(true);
				}
				am = an.computeWCET(project.getTargetMethod(),StaticCacheApproximation.ALWAYS_MISS);
				reportSpecial("always-miss",am,0,0,0);
				project.setGenerateWCETReport(false);
				
				/* minimal cache cost */
				boolean missOnceOnInvoke = config.getOption(CacheConfig.ASSUME_MISS_ONCE_ON_INVOKE);
				config.setOption(CacheConfig.ASSUME_MISS_ONCE_ON_INVOKE, true);
				GlobalAnalysis gb = new GlobalAnalysis(project);
				LpSolveWrapper.resetSolverTime();
				start = System.nanoTime();
				mincachecost = gb.computeWCET(project.getTargetMethod(), StaticCacheApproximation.ALL_FIT);
				stop  = System.nanoTime();
				reportSpecial("min-cache-cost",mincachecost, start, stop, LpSolveWrapper.getSolverTime());
				config.setOption(CacheConfig.ASSUME_MISS_ONCE_ON_INVOKE, missOnceOnInvoke);
			}
			tlLogger.info("Starting precise WCET analysis");
			project.setGenerateWCETReport(true);
			if(project.getProjectConfig().useUppaal()) {
				UppaalAnalysis an = new UppaalAnalysis(tlLogger,project,project.getOutDir("uppaal"));
				config.checkPresent(UppAalConfig.UPPAAL_VERIFYTA_BINARY);
				long start = System.nanoTime();
				wcet = an.computeWCET(project.getTargetMethod(),am.getCost());
				long stop  = System.nanoTime();
				reportUppaal(wcet,start,stop,an.getSearchtime(),an.getSolvertimemax());
			} else {
				StaticCacheApproximation staticCacheApprox =
					config.getOption(CacheConfig.STATIC_CACHE_APPROX);
				RecursiveWCETStrategy<StaticCacheApproximation> recStrategy;
				if(staticCacheApprox == StaticCacheApproximation.ALL_FIT) {
					recStrategy = new GlobalAnalysis.GlobalIPETStrategy();
				} else {
					recStrategy = new RecursiveAnalysis.LocalIPETStrategy();
				}
				RecursiveAnalysis<StaticCacheApproximation> an =
					new RecursiveAnalysis<StaticCacheApproximation>(project,recStrategy);
				LpSolveWrapper.resetSolverTime();
				long start = System.nanoTime();
				wcet = an.computeWCET(project.getTargetMethod(),config.getOption(CacheConfig.STATIC_CACHE_APPROX));
				long stop  = System.nanoTime();
				report(wcet,start,stop,LpSolveWrapper.getSolverTime());
			}
			tlLogger.info("WCET analysis finsihed: "+wcet);
			succeed = true;
		} catch (Exception e) {
			exec.logException("analysis", e);
		}
		if(! project.doWriteReport()) {
			tlLogger.info("Ommiting HTML report");
			return succeed;
		}
		try {
			/* Report */
			tlLogger.info("Generating info pages");
			project.getReport().generateInfoPages();
			tlLogger.info("Generating result document");
			project.writeReport();
			tlLogger.info("Generated files are in "+pConfig.getOutDir());
		} catch (Exception e) {
			exec.logException("Report generation", e);
			return false;
		}
		return succeed;
	}
	private void report(WcetCost wcet, long start, long stop,double solverTime) {
		String key = "wcet";
		System.out.println(key+": "+wcet);
		System.out.println(key+".time: " + timeDiff(start,stop));
		System.out.println(key+".solvertime: " + solverTime);
		project.recordResult(wcet,timeDiff(start,stop),solverTime);
		project.getReport().addStat(key, wcet.toString());
	}
	private void reportUppaal(WcetCost wcet, long start, long stop, double searchtime, double solvertimemax) {
		String key = "wcet";
		System.out.println(key+": "+wcet);
		System.out.println(key+".time: " + timeDiff(start,stop));
		System.out.println(key+".searchtime: " + searchtime);
		System.out.println(key+".solvertimemax: " + solvertimemax);
		project.recordResultUppaal(wcet,timeDiff(start,stop),searchtime,solvertimemax);
		project.getReport().addStat(key, wcet.toString());
	}
	private void reportSpecial(String metric, WcetCost cost, long start, long stop, double solverTime) {
		String key = "wcet."+metric;
		System.out.println(key+": "+cost);
		if(start != stop) System.out.println(key+".time: " + timeDiff(start,stop));
		if(solverTime != 0) System.out.println(key+".solvertime: " + solverTime);
		project.recordSpecialResult(metric,cost);
		project.getReport().addStat(key, cost.toString());
	}
}
