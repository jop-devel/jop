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

import java.util.Map.Entry;

import lpsolve.LpSolve;
import lpsolve.VersionInfo;

import org.apache.log4j.Logger;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.analysis.RecursiveAnalysis;
import com.jopdesign.wcet.analysis.TreeAnalysis;
import com.jopdesign.wcet.analysis.UppaalAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveWCETStrategy;
import com.jopdesign.wcet.config.Config;
import com.jopdesign.wcet.config.Option;
import com.jopdesign.wcet.graphutils.MiscUtils;
import com.jopdesign.wcet.ipet.IpetConfig;
import com.jopdesign.wcet.ipet.LpSolveWrapper;
import com.jopdesign.wcet.ipet.IpetConfig.StaticCacheApproximation;
import com.jopdesign.wcet.jop.ConstantCache;
import com.jopdesign.wcet.jop.JOPConfig;
import com.jopdesign.wcet.jop.LinkerInfo.LinkInfo;
import com.jopdesign.wcet.report.Report;
import com.jopdesign.wcet.report.ReportConfig;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import com.jopdesign.wcet.uppaal.WcetSearch;

import static com.jopdesign.wcet.ExecHelper.timeDiff;

/**
 * WCET Analysis for JOP - Executable
 */
public class WCETAnalysis {
	private static final String CONFIG_FILE_PROP = "config";
	public static final String VERSION = "1.0.1";

	public static Option<?>[][] options = {
		ProjectConfig.projectOptions,
		JOPConfig.jopOptions,
		IpetConfig.ipetOptions,
		UppAalConfig.uppaalOptions,
		ReportConfig.reportOptions
	};

	public static void main(String[] args) {
		Config config = Config.instance();
		config.addOptions(options);
		ExecHelper exec = new ExecHelper(WCETAnalysis.class,VERSION,Logger.getLogger(WCETAnalysis.class),CONFIG_FILE_PROP);
		exec.initTopLevelLogger();       /* Console logging for top level messages */
		exec.loadConfig(args);           /* Load config */
		WCETAnalysis inst = new WCETAnalysis(config,exec);
		/* check environment */
		inst.checkLibs();
		/* run */
		if(! inst.run()) exec.bail("WCET Analysis failed");
		exec.info("WCET Analysis finished");
	}

	private Config config;
	private Project project;
	private ExecHelper exec;

	public WCETAnalysis(Config c, ExecHelper e) {
		this.config = c;
		this.exec   = e;
	}

	private void checkLibs() {
		try {
			VersionInfo v = LpSolve.lpSolveVersion();
			exec.info("Using lp_solve for Java, v"+
					v.getMajorversion()+"."+v.getMinorversion()+
					" build "+v.getBuild()+" release "+v.getRelease());
		} catch(UnsatisfiedLinkError ule) {
			exec.bail("Failed to load the lp_solve Java library: "+ule);
		}
		if(config.getOption(ProjectConfig.USE_UPPAAL)) {
			String vbinary = config.getOption(UppAalConfig.UPPAAL_VERIFYTA_BINARY);
			try {
				String version = WcetSearch.getVerifytaVersion(vbinary);
				exec.info("Using uppaal/verifyta: "+vbinary+" version "+version);
			} catch(Exception fne) {
				exec.bail("Failed to run uppaal verifier: "+fne);
			}
		}
	}

	private boolean run() {
		project = null;
		ProjectConfig pConfig = new ProjectConfig(config);
		/* Initialize */
		try {
			project = new Project(pConfig);
			project.setTopLevelLogger(exec.getExecLogger());
			Report.initVelocity(config);     /* Initialize velocity engine */
			exec.info("Loading project");
			project.load();
			MethodInfo largestMethod = project.getProcessorModel().getMethodCache().checkCache();
			int minWords = MiscUtils.bytesToWords(largestMethod.getCode().getCode().length);
			reportMetric("min-cache-size",largestMethod.getFQMethodName(),minWords);
		} catch (Exception e) {
			exec.logException("Loading project", e);
			return false;
		}

		// project.getLinkerInfo().dump(System.out);
		new ConstantCache(project).build().dumpStats();

		/* Tree based WCET analysis - has to be equal to ALWAYS_MISS */
		{
			long start,stop;
			start = System.nanoTime();
			TreeAnalysis treeAna = new TreeAnalysis(project, false);
			long treeWCET = treeAna.computeWCET(project.getTargetMethod());
			stop = System.nanoTime();
			reportMetric("progress-measure",treeAna.getMaxProgress(project.getTargetMethod()));
			reportSpecial("wcet.tree",WcetCost.totalCost(treeWCET),start,stop,0.0);
		}

		/* Perf-Test */
//		for(int i = 0; i < 50; i++) {
//			RecursiveAnalysis<StaticCacheApproximation> an =
//				new RecursiveAnalysis<StaticCacheApproximation>(project,new RecursiveAnalysis.LocalIPETStrategy());
//			an.computeWCET(project.getTargetMethod(),StaticCacheApproximation.ALWAYS_HIT);
//		}
//		System.err.println("Total solver time (50): "+LpSolveWrapper.getSolverTime());
//		System.exit(1);

		//new ETMCExport(project).export(project.getOutFile("Spec_"+project.getProjectName()+".txt"));

		/* Run */
		boolean succeed = false;
		// FIXME: Report generation is a BIG MESS
		// bh will fix this in next revision
		try {
			/* Analysis */
			project.setGenerateWCETReport(false); /* generate reports later */
			exec.info("Cyclomatic complexity: "+project.computeCyclomaticComplexity(project.getTargetMethod()));
			WcetCost mincachecost, ah, am, wcet;
			IpetConfig ipetConfig = new IpetConfig(config);
			StaticCacheApproximation preciseApprox = IpetConfig.getPreciseCacheApprox(config);
			/* Perform a few standard analysis (MIN_CACHE_COST, ALWAYS_HIT, ALWAYS_MISS) */
			{
				long start,stop;

				/* always hit */
				RecursiveAnalysis<StaticCacheApproximation> an =
					new RecursiveAnalysis<StaticCacheApproximation>(
							project, ipetConfig,
							new RecursiveAnalysis.LocalIPETStrategy(ipetConfig));
				LpSolveWrapper.resetSolverTime();
				start = System.nanoTime();
				ah = an.computeWCET(project.getTargetMethod(),StaticCacheApproximation.ALWAYS_HIT);
				stop  = System.nanoTime();
				reportSpecial("always-hit",ah,start,stop,LpSolveWrapper.getSolverTime());

				/* always miss */
				/* FIXME: We don't have  report generation for UPPAAL and global analysis yet */
				if(project.getProjectConfig().useUppaal() || preciseApprox.needsInterProcIPET()) {
					project.setGenerateWCETReport(true);
				}
				am = an.computeWCET(project.getTargetMethod(),StaticCacheApproximation.ALWAYS_MISS);
				reportSpecial("always-miss",am,0,0,0);
				project.setGenerateWCETReport(false);

				/* minimal cache cost */
				IpetConfig mmcConfig = ipetConfig.clone();
				mmcConfig.assumeMissOnceOnInvoke = true;
				GlobalAnalysis gb = new GlobalAnalysis(project, mmcConfig);
				LpSolveWrapper.resetSolverTime();
				start = System.nanoTime();
				mincachecost = gb.computeWCET(project.getTargetMethod(), StaticCacheApproximation.GLOBAL_ALL_FIT);
				stop  = System.nanoTime();
				reportSpecial("min-cache-cost",mincachecost, start, stop, LpSolveWrapper.getSolverTime());
			}
			exec.info("Starting precise WCET analysis");
			project.setGenerateWCETReport(false);
			if(project.getProjectConfig().useUppaal()) {
				UppaalAnalysis an = new UppaalAnalysis(exec.getExecLogger(),project,project.getOutDir("uppaal"));
				config.checkPresent(UppAalConfig.UPPAAL_VERIFYTA_BINARY);
				long start = System.nanoTime();
				wcet = an.computeWCET(project.getTargetMethod(),am.getCost());
				long stop  = System.nanoTime();
				reportUppaal(wcet,start,stop,an.getSearchtime(),an.getSolvertimemax());
			} else {
				RecursiveWCETStrategy<StaticCacheApproximation> recStrategy;
				if(preciseApprox == StaticCacheApproximation.ALL_FIT_REGIONS) {
					recStrategy = new GlobalAnalysis.GlobalIPETStrategy(ipetConfig);
				} else {
					project.setGenerateWCETReport(true);
					recStrategy = new RecursiveAnalysis.LocalIPETStrategy(ipetConfig);
				}
				RecursiveAnalysis<StaticCacheApproximation> an =
					new RecursiveAnalysis<StaticCacheApproximation>(project,ipetConfig,recStrategy);
				LpSolveWrapper.resetSolverTime();
				long start = System.nanoTime();
				wcet = an.computeWCET(project.getTargetMethod(),preciseApprox);
				long stop  = System.nanoTime();
				report(wcet,start,stop,LpSolveWrapper.getSolverTime());
			}
			exec.info("WCET analysis finsihed: "+wcet);
			succeed = true;
		} catch (Exception e) {
			exec.logException("analysis", e);
		}
		if(! project.doWriteReport()) {
			exec.info("Ommiting HTML report");
			return succeed;
		}
		try {
			/* Report */
			exec.info("Generating info pages");
			project.getReport().generateInfoPages();
			exec.info("Generating result document");
			project.writeReport();
			exec.info("Generated files are in "+pConfig.getOutDir());
		} catch (Exception e) {
			exec.logException("Report generation", e);
			return false;
		}
		return succeed;
	}

	private void reportMetric(String metric, Object... args) {
		project.recordMetric(metric, args);
		System.out.print(metric+":");
		for(Object o : args) System.out.print(" "+o);
		System.out.println("");
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
