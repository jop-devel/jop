/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006-2008, Martin Schoeberl (martin@jopdesign.com)
  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

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
import com.jopdesign.wcet08.analysis.CacheConfig;
import com.jopdesign.wcet08.analysis.SimpleAnalysis;
import com.jopdesign.wcet08.analysis.CacheConfig.CacheApproximation;
import com.jopdesign.wcet08.ipet.LpSolveWrapper;
import com.jopdesign.wcet08.report.Report;
/**
 * WCET Analysis for JOP - Executable
 */
public class WCETAnalysis {
	private static final String CONFIG_FILE_PROP = "config";
	private static final Logger tlLogger = Logger.getLogger(WCETAnalysis.class);


	public static void main(String[] args) {
		Config config = Config.instance();
		config.addOptions(CacheConfig.cacheOptions);
		ExecHelper exec = new ExecHelper(WCETAnalysis.class,tlLogger,CONFIG_FILE_PROP);
		
		exec.initTopLevelLogger();       /* Console logging for top level messages */
		exec.loadConfig(args);           /* Load config */
		WCETAnalysis inst = new WCETAnalysis();
		/* run */
		if(! inst.run()) {		
			exec.bail("WCET Analysis failed");
		}
		tlLogger.info("WCET analysis finished");
		if(config.hasReportDir()) {
			tlLogger.info("Results are in "+config.getOutDir()+"/index.html");
		}
	}


	private boolean run() {
		Project project = new Project();
		project.setTopLevelLooger(tlLogger);
		tlLogger.info("Loading project");
		try {
			project.load();
		} catch (Exception e) {
			System.err.println("[ERROR] Loading project failed: "+e);
			e.printStackTrace();
			return false;
		}
		boolean succeed = false;
		try {
			/* Analysis */
//			tlLogger.info("Stack usage analysis");
//			StackSize stackUsage = new StackSize(project);
//			tlLogger.info("Stack usage: "+stackUsage.getStackSize());
			tlLogger.info("Starting WCET analysis");
			SimpleAnalysis an = new SimpleAnalysis(project);
			long start = System.nanoTime();
			long scaWCET = 	an.computeWCET(project.getMeasuredMethod(),CacheApproximation.ANALYSE_REACHABLE).getCost();
			long stop  = System.nanoTime();
			tlLogger.info("WCET 'analyze static reachable' analysis finsihed");
			System.out.println("sca: "+scaWCET);
			System.out.println("time: "+(((double)stop-start) / 1.0E9));
			System.out.println("solvertime: "+LpSolveWrapper.getSolverTime());

			Config.instance().setGenerateWCETReport(false);
			long ahWCET = an.computeWCET(project.getMeasuredMethod(),CacheApproximation.ALWAYS_HIT).getCost();			
			tlLogger.info("WCET 'always hit' analysis finsihed");
			System.out.println("ah:"+ahWCET);

			long amWCET = an.computeWCET(project.getMeasuredMethod(),CacheApproximation.ALWAYS_MISS).getCost();
			tlLogger.info("WCET 'always miss' analysis finished");
			System.out.println("am: "+amWCET);
			project.getReport().addStat("wcet-always-hit", ahWCET);
			project.getReport().addStat("wcet-always-miss", amWCET);
			project.getReport().addStat("wcet-cache-approx", scaWCET);
			succeed = true;
		} catch (Exception e) {
			tlLogger.error(e);
		} catch(AssertionError e) {
			tlLogger.fatal(e);
		}
		if(! Config.instance().hasReportDir()) {
			tlLogger.info("No 'reportdir' set, ommiting HTML report");
			return succeed;
		}
		try {
			/* Initialize output and libraries */
			tlLogger.info("Initializing velocity");
			try {
				Report.initVelocity();
			} catch (Exception e) {
				e.printStackTrace();
				tlLogger.error("Initializing velocity failed");
				return false;
			}
			/* Report */
			tlLogger.info("Generating info pages");
			project.getReport().generateInfoPages();
			tlLogger.info("Generating result document");
			project.getReport().writeReport();
		} catch (Exception e) {
			e.printStackTrace();
			tlLogger.error("Generating report failed");
			return false;
		}
		return succeed;
	}

}
