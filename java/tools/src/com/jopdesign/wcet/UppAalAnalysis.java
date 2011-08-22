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

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.AppSetup;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.dfa.DFATool;
import com.jopdesign.wcet.analysis.UppaalAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class UppAalAnalysis {

    private static final Logger tlLogger = Logger.getLogger(UppAalAnalysis.class);
    private static final int ECC_TRESHOLD = 400;

    class WCETEntry {
        MethodInfo target;
        long wcet;
        double searchtime;
        double solvertime;

        public WCETEntry(MethodInfo target, long wcet, double searchtime, double solvertime) {
            this.target = target;
            this.wcet = wcet;
            this.searchtime = searchtime;
            this.solvertime = solvertime;
        }
    }

    public static void main(String[] args) {

        // We set a different output path for this tool if invoked by cmdline
        // Note that WCETTool could also override defaults, but we do not want to change the
        // default value of outdir if WCETTool is invoked from another tool
        Properties defaultProps = new Properties();
        defaultProps.put("outdir", "java/target/wcet/${projectname}");

        AppSetup setup = new AppSetup(defaultProps, false);
        setup.setVersionInfo("1.0 [deprecated]");
        // We do not load a config file automatically, user has to specify it explicitly to avoid
        // unintentional misconfiguration
        //setup.setConfigFilename(CONFIG_FILE_NAME);
        setup.setUsageInfo("UppAllAnalysis", "UppAll WCET Analysis");

        WCETTool wcetTool = new WCETTool();
        DFATool dfaTool = new DFATool();

        setup.registerTool("dfa", dfaTool, true, false);
        setup.registerTool("wcet", wcetTool);

        @SuppressWarnings("unused")
		AppInfo appInfo = setup.initAndLoad(args, true, false, false);

        if (setup.useTool("dfa")) {
            wcetTool.setDfaTool(dfaTool);
        }

        ExecHelper exec = new ExecHelper(setup.getConfig(), tlLogger);

        exec.dumpConfig();
        UppAalAnalysis inst = new UppAalAnalysis(wcetTool);
        /* run */
        if (!inst.run(exec)) exec.bail("UppAal translation failed");
        tlLogger.info("UppAal translation finished");
    }

    private WCETTool project;

    public UppAalAnalysis(WCETTool wcetTool) {
        project = wcetTool;
    }

    private boolean run(ExecHelper exec) {
        File uppaalOutDir;
        try {
            project.setTopLevelLogger(tlLogger);
            tlLogger.info("Loading project");
            project.initialize(true, true);
            uppaalOutDir = project.getOutDir("uppaal");
        }
        catch (Exception e) {
            exec.logException("loading project", e);
            return false;
        }
        UppaalAnalysis ua = new UppaalAnalysis(tlLogger, project, uppaalOutDir);
        List<MethodInfo> methods = project.getCallGraph().getReachableImplementations(project.getTargetMethod());
        Collections.reverse(methods);
        List<WCETEntry> entries = new ArrayList<WCETEntry>();
        for (MethodInfo m : methods) {
            if (project.computeCyclomaticComplexity(m) > ECC_TRESHOLD) {
                tlLogger.info("Skipping UppAal translation for " + m +
                        " because extended cyclomatic complexity " +
                        project.computeCyclomaticComplexity(m) + " > treshold");
            } else {
                tlLogger.info("Starting UppAal translation for " + m);
                WcetCost wcet;
                try {
                    wcet = ua.calculateWCET(m);
                    entries.add(new WCETEntry(m, wcet.getCost(), ua.getSearchtime(), ua.getSolvertimemax()));
                } catch (Exception e) {
                    exec.logException("Uppaal calculation", e);
                    return false;
                }
            }
        }
        for (WCETEntry entry : entries) {
            System.out.println("***" + entry.target.toString());
            System.out.println("    wcet: " + entry.wcet);
            System.out.println("    complex: " + project.computeCyclomaticComplexity(entry.target));
            System.out.println("    searchT: " + entry.searchtime);
            System.out.println("    solverTmax: " + entry.solvertime);
        }
        return true;
    }
    
}