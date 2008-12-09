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

package com.jopdesign.wcet08;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.jopdesign.wcet08.analysis.CacheConfig;
import com.jopdesign.wcet08.uppaal.Translator;
import com.jopdesign.wcet08.uppaal.UppAalConfig;
import com.jopdesign.wcet08.uppaal.WcetSearch;

public class UppAalAnalysis {
	private static final String CONFIG_FILE_PROP = "config";
	private static final Logger tlLogger = Logger.getLogger(UppAalAnalysis.class);


	public static void main(String[] args) {
		Config config = Config.instance();
		config.addOptions(CacheConfig.cacheOptions);
		config.addOptions(UppAalConfig.uppaalOptions);
		ExecHelper exec = new ExecHelper(UppAalAnalysis.class,tlLogger,CONFIG_FILE_PROP);
		
		exec.initTopLevelLogger();       /* Console logging for top level messages */
		exec.loadConfig(args);           /* Load config */
		if(! config.hasReportDir()) {
			exec.bail("No report directory set - UppAal model needs to be written to disk");
		}
		UppAalAnalysis inst = new UppAalAnalysis();
		/* run */
		if(! inst.run(exec)) exec.bail("UppAal translation failed");
		tlLogger.info("UppAal translation finished");
	}

	private boolean run(ExecHelper exec) {
		Project project = new Project();
		project.setTopLevelLooger(tlLogger);
		tlLogger.info("Loading project");
		try { project.load(); }
		catch (Exception e) { exec.logException("loading project", e); return false; }

		tlLogger.info("Starting UppAal translation");
		Translator translator = new Translator(project);
		try {
			translator.translateProgram();
			translator.writeOutput();
		} catch (Throwable e) {
			exec.logException("translating WCET problem to UppAal", e);
		}
		tlLogger.info("model and query can be found in "+Config.instance().getOutDir());
		tlLogger.info("model file: "+translator.getModelFile());
		if(UppAalConfig.hasVerifier()) {
			tlLogger.info("Starting verification");
			WcetSearch search = new WcetSearch(translator.getModelFile());
			long wcet;
			try {
				long start = System.nanoTime();
				wcet = search.searchWCET();
				long end = System.nanoTime();				
				System.out.println("wcet: "+wcet);
				System.out.println("solvertime: "+((double)(end-start))/1E9);
				System.out.println("solvertimelast: "+search.getLastSolverTime());
			} catch (IOException e) {
				exec.logException(" binary searching for WCET using UppAal", e);
			}
		} else {
			tlLogger.info("No verifier binary available. Skipping search");
		}
		return true;
	}

}