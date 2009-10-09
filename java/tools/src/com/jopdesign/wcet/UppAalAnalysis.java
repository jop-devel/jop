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

package com.jopdesign.wcet;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.analysis.UppaalAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.config.Config;
import com.jopdesign.wcet.config.Option;
import com.jopdesign.wcet.ipet.IpetConfig;
import com.jopdesign.wcet.jop.JOPConfig;
import com.jopdesign.wcet.report.ReportConfig;
import com.jopdesign.wcet.uppaal.UppAalConfig;

public class UppAalAnalysis {
	private static final String CONFIG_FILE_PROP = "config";
	private static final Logger tlLogger = Logger.getLogger(UppAalAnalysis.class);
	private static final int ECC_TRESHOLD = 400;
	public static Option<?>[][] options = {
		ProjectConfig.projectOptions,
		JOPConfig.jopOptions,
		IpetConfig.ipetOptions,
		UppAalConfig.uppaalOptions,
		ReportConfig.reportOptions
	};
	class WCETEntry {
		MethodInfo target;
		long wcet;
		double searchtime;
		double  solvertime;
		public WCETEntry(MethodInfo target, long wcet, double searchtime, double solvertime) {
			this.target = target;
			this.wcet = wcet;
			this.searchtime = searchtime; 
			this.solvertime = solvertime;
		}
	}
	
	public static void main(String[] args) {
		Config config = Config.instance();
		config.addOptions(options);
		ExecHelper exec = new ExecHelper(UppAalAnalysis.class, "1.0 [deprecated]", tlLogger, CONFIG_FILE_PROP);
		
		exec.initTopLevelLogger();       /* Console logging for top level messages */
		exec.loadConfig(args);           /* Load config */
		UppAalAnalysis inst = new UppAalAnalysis();
		/* run */
		if(! inst.run(exec)) exec.bail("UppAal translation failed");
		tlLogger.info("UppAal translation finished");
	}

	private boolean run(ExecHelper exec) {
		Config c = Config.instance();
		File uppaalOutDir = null;
		Project project = null;
		try { 
			project = new Project(new ProjectConfig(c));
			project.setTopLevelLogger(tlLogger);
			tlLogger.info("Loading project");
			project.load();
			uppaalOutDir = project.getOutDir("uppaal");
		}
		catch (Exception e) { 
			exec.logException("loading project", e); 
			return false; 
		}
		UppaalAnalysis ua = new UppaalAnalysis(tlLogger,project,uppaalOutDir);
		List<MethodInfo> methods = project.getCallGraph().getImplementedMethods(project.getTargetMethod());
		Collections.reverse(methods);
		List<WCETEntry> entries = new Vector<WCETEntry>();
		for( MethodInfo m : methods ) {
			if(project.computeCyclomaticComplexity(m) > ECC_TRESHOLD) {
				tlLogger.info("Skipping UppAal translation for "+m+
						      " because extended cyclomatic compleity "+
						      project.computeCyclomaticComplexity(m) + " > treshold");				
			} else {
				tlLogger.info("Starting UppAal translation for "+m);
				WcetCost wcet;
				try {
					wcet = ua.calculateWCET(m);
					entries.add(new WCETEntry(m,wcet.getCost(),ua.getSearchtime(),ua.getSolvertimemax()));
				} catch (Exception e) {
					exec.logException("Uppaal calculation",e);
					return false;
				}
			}
		}
		for(WCETEntry entry : entries) {
			System.out.println("***" + entry.target.toString());
			System.out.println("    wcet: " + entry.wcet);
			System.out.println("    complex: " + project.computeCyclomaticComplexity(entry.target));
			System.out.println("    searchT: " + entry.searchtime);
			System.out.println("    solverTmax: " + entry.solvertime);			
		}
		return true;
	}

}