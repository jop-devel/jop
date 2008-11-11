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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.jopdesign.wcet08.Config.BadConfigurationError;
import com.jopdesign.wcet08.Config.MissingConfigurationError;
import com.jopdesign.wcet08.analysis.CacheConfig;
import com.jopdesign.wcet08.analysis.SimpleAnalysis;
import com.jopdesign.wcet08.analysis.SimpleAnalysis.WcetMode;
import com.jopdesign.wcet08.report.Report;

/**
 * WCET Analysis for JOP - Executable
 */
public class WCETAnalysis {
	private static final String CONFIG_FILE_PROP = "config";
	private static final Logger tlLogger = Logger.getLogger(WCETAnalysis.class);
	private Config config;

	public WCETAnalysis(Config config) {
		this.config = config;
	}

	private static void exitUsage() {
		System.err.println(
				"Usage:\n  java "+
				"-D"+CONFIG_FILE_PROP+"=file://<path-to-config> "+
				WCETAnalysis.class.getCanonicalName() + " [OPTIONS]");
		System.err.println("Example:\n  "+
				"java -D"+CONFIG_FILE_PROP+"=file:///home/jop/myconf.props "+
				WCETAnalysis.class.getName() + 
				" -"+CacheConfig.BLOCK_SIZE+" 32" +
				" -"+Config.ROOT_CLASS_NAME+" wcet.Method\n");
		System.err.print  ("OPTIONS can be configured using system properties");
		System.err.println(", supplying a property file or as command line arguments");
		printOptionDescrs(Config.optionDescrs);
		printOptionDescrs(CacheConfig.optionDescrs);
		System.err.println("\nSee 'wcet.properties' for an example configuration");
		System.exit(1);		
	}

	private static void printOptionDescrs(String[][] optionDescrs) {
		for(String[] option : optionDescrs) {
			System.err.println("    "+option[0]+" ... "+option[1]);
		}
	}

	public static void main(String[] args) {
		/* Console logging for top level messages */
		ConsoleAppender consoleApp = new ConsoleAppender(new PatternLayout(), ConsoleAppender.SYSTEM_ERR);
		consoleApp.setName("TOP-LEVEL");
		consoleApp.setThreshold(Level.INFO);
		consoleApp.setLayout(new PatternLayout("[WCETAnalysis %-6rms] %m%n"));
		tlLogger.addAppender(consoleApp);

		/* Get config */
		try {
			String[] argsrest = Config.load(System.getProperty(CONFIG_FILE_PROP),args);
			Config c = Config.instance();
			if(argsrest.length != 0 || c.helpRequested()) exitUsage();
			tlLogger.info("Configuration: "+Config.instance().getOptions());
			c.checkPresent(Config.CLASSPATH_PROPERTY);
			c.checkPresent(Config.SOURCEPATH_PROPERTY);
			c.checkPresent(Config.ROOT_CLASS_NAME);
			if(Config.instance().hasReportDir()) {
				tlLogger.info("Initializing Output");
				Config.instance().initializeReport();
			}
		} catch (MissingConfigurationError e) {
			System.err.println("[FATAL] Missing option: "+e.getMessage());
			exitUsage();
		} catch(BadConfigurationError e) { 
			System.err.println("[FATAL] Bad option: "+e.getMessage());
			exitUsage();					
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("----------------------------------------");
			System.err.println("[FATAL] Loading configuration failed");
			System.exit(1);
		}
				
		Config config = Config.instance();
		WCETAnalysis inst = new WCETAnalysis(config);
		/* run */
		if(! inst.run()) {
			System.err.println("[ERROR] WCET Analysis failed");
			System.exit(1);
		} else {
			tlLogger.info("WCET analysis finished");
			if(config.hasReportDir()) {
				tlLogger.info("Results are in "+config.getOutDir()+"/index.html");
			}
		}
	}

	private boolean run() {
		Project project = new Project();
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
			tlLogger.info("Starting analysis");
			SimpleAnalysis an = new SimpleAnalysis(project);

			long scaWCET = 	an.computeWCET(project.getRootMethod(),WcetMode.ANALYSE_REACHABLE).getCost();
			tlLogger.info("WCET simple cache analysis finsihed");
			System.out.println("sca: "+scaWCET);
			
			config.setGenerateWCETReport(false);
			long ahWCET = an.computeWCET(project.getRootMethod(),WcetMode.ALWAYS_HIT).getCost();			
			tlLogger.info("WCET always hit analysis finsihed");
			System.out.println("ah:"+ahWCET);
			
			long amWCET = an.computeWCET(project.getRootMethod(),WcetMode.ALWAYS_MISS).getCost();
			tlLogger.info("WCET always miss analysis finished");
			System.out.println("am: "+amWCET);
			
			project.getReport().addStat("wcet-always-hit", ahWCET);
			project.getReport().addStat("wcet-always-miss", amWCET);
			project.getReport().addStat("wcet-cache-approx", scaWCET);
			succeed = true;
		} catch (Exception e) {
			System.err.println("[ERROR] "+e);
			e.printStackTrace();			
		} catch(AssertionError e) {
			System.err.println("[FATAL] "+e);
			e.printStackTrace();			
		}
		if(! config.hasReportDir()) {
			tlLogger.info("No 'reportdir' set, ommiting HTML report");
			return succeed;
		}
		try {
			/* Initialize output and libraries */
			tlLogger.info("Initializing velocity");
			try {
				Report.initVelocity();
			} catch (Exception e) {
				System.err.println("[ERROR] Initializing velocity failed");
				e.printStackTrace();
				return false;
			}
			/* Report */
			tlLogger.info("Generating info pages");
			project.getReport().generateInfoPages();
			tlLogger.info("Generating result document");
			project.getReport().writeReport();
		} catch (Exception e) {
			System.err.println("[ERROR] Generating report failed");
			e.printStackTrace();
			return false;
		}
		return succeed;
	}

}
