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

package com.jopdesign.wcet08;

import org.apache.log4j.Logger;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.analysis.GlobalAnalysis;
import com.jopdesign.wcet08.analysis.RecursiveAnalysis;
import com.jopdesign.wcet08.analysis.UppaalAnalysis;
import com.jopdesign.wcet08.analysis.WcetCost;
import com.jopdesign.wcet08.analysis.RecursiveAnalysis.RecursiveWCETStrategy;
import com.jopdesign.wcet08.config.Config;
import com.jopdesign.wcet08.config.Option;
import com.jopdesign.wcet08.graphutils.MiscUtils;
import com.jopdesign.wcet08.ipet.LpSolveWrapper;
import com.jopdesign.wcet08.jop.CacheConfig;
import com.jopdesign.wcet08.jop.CacheConfig.StaticCacheApproximation;
import com.jopdesign.wcet08.report.Report;
import com.jopdesign.wcet08.report.ReportConfig;
import com.jopdesign.wcet08.uppaal.UppAalConfig;

import static com.jopdesign.wcet08.ExecHelper.timeDiff;
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
	public WCETAnalysis(Config c) {
		this.config = c;
	}
	private boolean run(ExecHelper exec) {
		Project project = null;
		ProjectConfig pConfig = new ProjectConfig(config);
		try {
			project = new Project(pConfig);
			project.setTopLevelLooger(tlLogger);
			Report.initVelocity(config);     /* Initialize velocity engine */
			tlLogger.info("Loading project");
			project.load();
			MethodInfo largestMethod = project.getProcessorModel().getMethodCache().checkCache();
			System.out.println("Minimal Cache Size for target method(words): " 
					         + MiscUtils.bytesToWords(largestMethod.getCode().getLength())
					         + " because of "+largestMethod.getFQMethodName());
		} catch (Exception e) {
			exec.logException("Loading project", e);
			return false;
		}
		boolean succeed = false;
		try {
			/* Analysis */
			project.setGenerateWCETReport(false); /* generate reports later */
			/* Perform a few standard analysis (global ALL_FIT, ALWAYS_HIT, ALWAYS_MISS) */
			tlLogger.info("Cyclomatic complexity: "+project.computeCyclomaticComplexity(project.getTargetMethod()));
			long ahWCET, amWCET;
			{
				tlLogger.info("Global WCET analysis");
				GlobalAnalysis gb = new GlobalAnalysis(project);
				long start = System.nanoTime();
				long stop  = System.nanoTime();
				System.out.println("global-missonce: "+gb.computeWCET(project.getTargetMethod(), StaticCacheApproximation.ALL_FIT));
				System.out.println("time: " + timeDiff(start,stop));
				System.out.println("solvertime: " + LpSolveWrapper.getSolverTime());
		    }
			{				
				RecursiveAnalysis<StaticCacheApproximation> an = 
					new RecursiveAnalysis<StaticCacheApproximation>(project,new RecursiveAnalysis.LocalIPETStrategy());
				WcetCost ahcost = an.computeWCET(project.getTargetMethod(),StaticCacheApproximation.ALWAYS_HIT);			
				tlLogger.info("WCET 'always hit' analysis finsihed: "+ahcost);
				ahWCET = ahcost.getCost();
				System.out.println("ah:"+ahWCET);
				project.getReport().addStat("wcet-always-hit", ahWCET);
	
				/* FIXME: We don't have  report generation for UPPAAL and global analysis yet */
				if(project.getProjectConfig().useUppaal()
				   || project.getConfig().getOption(CacheConfig.STATIC_CACHE_APPROX) == StaticCacheApproximation.ALL_FIT) { 
					project.setGenerateWCETReport(true);
				}
				WcetCost amcost = an.computeWCET(project.getTargetMethod(),StaticCacheApproximation.ALWAYS_MISS);
				amWCET = amcost.getCost();
				tlLogger.info("WCET 'always miss' analysis finished: "+amcost);
				System.out.println("am: "+amWCET);
				project.getReport().addStat("wcet-always-miss", amWCET);
			}
			tlLogger.info("Starting precise WCET analysis");
			project.setGenerateWCETReport(true);
			if(project.getProjectConfig().useUppaal()) {
				UppaalAnalysis an = new UppaalAnalysis(tlLogger,project,project.getOutDir("uppaal"));
				config.checkPresent(UppAalConfig.UPPAAL_VERIFYTA_BINARY);
				long start = System.nanoTime();
				WcetCost uppaalWCET = an.computeWCET(project.getTargetMethod(),amWCET);
				long stop  = System.nanoTime();
				tlLogger.info("WCET analysis [UPPAAL] finished");
				System.out.println("wcet-uppaal: "+uppaalWCET);
				System.out.println("time: "+timeDiff(start,stop));
				System.out.println("solvertimemax: "+an.getSolvertimemax());
				project.getReport().addStat("wcet-uppal", uppaalWCET.getCost());
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
				WcetCost cachewcet = an.computeWCET(project.getTargetMethod(),config.getOption(CacheConfig.STATIC_CACHE_APPROX));
				long stop  = System.nanoTime();
				tlLogger.info("WCET analysis finsihed: "+cachewcet);
				System.out.println("wcet: "+cachewcet.getCost());
				System.out.println("time: "+timeDiff(start,stop));
				System.out.println("solvertime: "+LpSolveWrapper.getSolverTime());
				project.getReport().addStat("wcet", cachewcet.getCost());
			}

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
}
