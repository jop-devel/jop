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

import com.jopdesign.common.AppSetup;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.Segment;
import com.jopdesign.common.code.CallGraph.ContextEdge;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.dfa.DFATool;
import com.jopdesign.wcet.analysis.AnalysisContextLocal;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.analysis.LocalAnalysis;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.analysis.cache.MethodCacheAnalysis;
import com.jopdesign.wcet.analysis.InvalidFlowFactException;
import com.jopdesign.wcet.analysis.RecursiveWcetAnalysis;
import com.jopdesign.wcet.analysis.TreeAnalysis;
import com.jopdesign.wcet.analysis.UppaalAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETConfig.StaticCacheApproximation;
import com.jopdesign.wcet.ipet.LpSolveWrapper;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import com.jopdesign.wcet.uppaal.model.DuplicateKeyException;
import com.jopdesign.wcet.uppaal.model.XmlSerializationException;
import org.apache.log4j.Logger;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.IOException;
import java.util.Properties;

import lpsolve.LpSolveException;

import static com.jopdesign.wcet.ExecHelper.timeDiff;

/**
 * WCET Analysis for JOP - Executable
 */
public class WCETAnalysis {
    private static final boolean CALCULATE_MINIMUM_CACHE_COST = false;

    public static void main(String[] args) {

        // We set a different output path for this tool if invoked by cmdline
        // Note that WCETTool could also override defaults, but we do not want to change the
        // default value of outdir if WCETTool is invoked from another tool
        Properties defaultProps = new Properties();
        defaultProps.put("outdir", "java/target/wcet/${projectname}");

        AppSetup setup = new AppSetup(defaultProps, false);
        setup.setVersionInfo("1.0.1");
        // We do not load a config file automatically, user has to specify it explicitly to avoid
        // unintentional misconfiguration
        //setup.setConfigFilename(CONFIG_FILE_NAME);
        setup.setUsageInfo("WCETAnalysis", "WCET Analysis tool");

        WCETTool wcetTool = new WCETTool();
        DFATool dfaTool = new DFATool();

        setup.registerTool("dfa", dfaTool, true, false);
        setup.registerTool("wcet", wcetTool);

        setup.addSourceLineOptions(false);
        setup.initAndLoad(args, true, false, false);

        if (setup.useTool("dfa")) {
            wcetTool.setDfaTool(dfaTool);
        }

        ExecHelper exec = new ExecHelper(setup.getConfig(), Logger.getLogger(WCETTool.LOG_WCET+".WCETAnalysis"));
        exec.dumpConfig();               /* Load config */
        exec.checkLibs();                /* check environment */

        WCETAnalysis inst = new WCETAnalysis(wcetTool, exec);

        try {
			inst.run();
			exec.info("Worst Case Analysis finished");
		} catch (Exception e) {
			 exec.bail("Worst Case Analysis failed: " + e);
		}  	
    }

    private Config config;
    private WCETTool wcetTool;
    private ExecHelper exec;
    private WcetCost wcet;
    private WcetCost alwaysMissCost;
    private WcetCost alwaysHitCost;
    private WcetCost minCacheCost;
    private IPETConfig ipetConfig;
	private boolean reportGenerated;

    public WCETAnalysis(WCETTool wcetTool, ExecHelper e) {
        this.wcetTool = wcetTool;
        this.config = wcetTool.getConfig();
        this.exec   = e;
    }

    private boolean run() throws InvalidFlowFactException {
        /* Initialize */
        try {
            wcetTool.setTopLevelLogger(exec.getExecLogger());
            exec.info("Loading project"); // TODO: Maybe exec can be replaced by something stefan's framework?
            wcetTool.initialize(wcetTool.getProjectConfig().doLoadLinkInfo(), true);
            MethodInfo largestMethod = wcetTool.getWCETProcessorModel().getMethodCache().checkCache();
            int minWords = MiscUtils.bytesToWords(largestMethod.getCode().getNumberOfBytes());
            reportMetric("min-cache-size",largestMethod.getFQMethodName(),minWords);
        } catch (Exception e) {
            exec.logException("Loading project", e);
            return false;
        }

    	// Segment Cache Analysis: Experiments
        MethodCacheAnalysis mca = new MethodCacheAnalysis(wcetTool);
        /* iterate top down the scope graph (currently: the call graph) */
        TopologicalOrderIterator<ExecutionContext, ContextEdge> iter =
                wcetTool.getCallGraph().reverseTopologicalOrder();

        LpSolveWrapper.resetSolverTime();
        long blocks = 0;
        long start = System.nanoTime();
        while (iter.hasNext()) {
            ExecutionContext scope = iter.next();
            
            int availBlocks = wcetTool.getWCETProcessorModel().getMethodCache().getNumBlocks();
            long total, distinctApprox = -1, distinct = -1;
            
            blocks = total = mca.countTotalCacheBlocks(scope);
            if(total > availBlocks || true) {
            	blocks = distinctApprox = mca.countDistinctCacheBlocks(scope, false);
                if(blocks > availBlocks && blocks < availBlocks*2 || true) {
                	blocks = distinct = mca.countDistinctCacheBlocks(scope, true);                	
                }
            }
            System.out.println(String.format("block-count < %2d [%2d,%2d,%2d] for %-30s @ %s", blocks,
            		total, distinctApprox, distinct,
            		scope.getMethodInfo().getFQMethodName(), scope.getCallString().toStringVerbose(false)));
        }
        long stop  = System.nanoTime();
        reportSpecial("block-count",WcetCost.totalCost(blocks),start,stop,LpSolveWrapper.getSolverTime());
        System.out.println("solver-time: "+LpSolveWrapper.getSolverTime());
//      return true;
        
        
        // project.getLinkerInfo().dump(System.out);
        // new ConstantCache(project).build().dumpStats();

        /* Perf-Test */
//        for(int i = 0; i < 50; i++) {
//            RecursiveAnalysis<StaticCacheApproximation> an =
//                new RecursiveAnalysis<StaticCacheApproximation>(project,new RecursiveAnalysis.LocalIPETStrategy());
//            an.computeWCET(project.getTargetMethod(),StaticCacheApproximation.ALWAYS_HIT);
//        }
//        System.err.println("Total solver time (50): "+LpSolveWrapper.getSolverTime());
//        System.exit(1);

        
        if(wcetTool.getProjectConfig().doObjectCacheAnalysis()) {
            ObjectCacheAnalysis oca = new ObjectCacheAnalysis(wcetTool);
            return oca.run();
        } else {
            return runWCETAnalysis();
        }
        
    }
    
    private boolean runWCETAnalysis() {
        /* Run */
        ipetConfig = new IPETConfig(config);
        boolean succeed = true;
        // FIXME: Report generation is a BIG MESS
        // bh wants to fix this soon
        try {
            /* Analysis */
        	reportGenerated = false;
            computeMetrics(); /* some metrics, some cheap analysis for comparison and report if not supported by precise analysis */
            exec.info("Starting precise WCET analysis");
            computeWCET();
        } catch (Exception e) {
            exec.logException("analysis", e);
            succeed = false;
        }
        if (!wcetTool.getProjectConfig().doGenerateReport()) {
            exec.info("Ommiting HTML report");
            return succeed;
        }
        try {
            /* Report */
            exec.info("Generating info pages");
            wcetTool.getReport().generateInfoPages();
            exec.info("Generating result document");
            wcetTool.writeReport();
            exec.info("Generated files are in " + wcetTool.getProjectConfig().getProjectDir());
        } catch (Exception e) {
            exec.logException("Report generation", e);
            succeed = false;
        }
        return succeed;
    }

        
    private void computeMetrics() throws Exception {
        StaticCacheApproximation preciseApprox = IPETConfig.getPreciseCacheApprox(config);
        wcetTool.setGenerateWCETReport(false); /* generate reports later (except preciseApprox does not support reports) */

        exec.info("Cyclomatic complexity: " + wcetTool.computeCyclomaticComplexity(wcetTool.getTargetMethod()));
        if(! wcetTool.getWCETProcessorModel().hasMethodCache()) preciseApprox = StaticCacheApproximation.ALWAYS_MISS;
        /* Perform a few standard analysis (MIN_CACHE_COST, ALWAYS_HIT, ALWAYS_MISS) without call strings */
        if(wcetTool.getWCETProcessorModel().hasMethodCache()) {
            long start,stop;

            /* Tree based WCET analysis - has to be equal to ALWAYS_MISS */
            {
                start = System.nanoTime();
                TreeAnalysis treeAna = new TreeAnalysis(wcetTool, false);
                long treeWCET = treeAna.computeWCET(wcetTool.getTargetMethod());
                stop = System.nanoTime();
                reportMetric("progress-measure",treeAna.getMaxProgress(wcetTool.getTargetMethod()));
                reportSpecial("wcet.tree",WcetCost.totalCost(treeWCET),start,stop,0.0);
            }

            RecursiveWcetAnalysis<AnalysisContextLocal> an =
                new RecursiveWcetAnalysis<AnalysisContextLocal>(
                        wcetTool, ipetConfig,
                        new LocalAnalysis(wcetTool,ipetConfig));

            /* FIXME: We don't have  report generation for UPPAAL and global analysis yet,
             * therefore we generate our report here */
            if(wcetTool.getProjectConfig().useUppaal() || preciseApprox.needsInterProcIPET()) {
                wcetTool.setGenerateWCETReport(true);
                reportGenerated = true;
            }
            /* always miss */
            start = System.nanoTime();
            alwaysMissCost = an.computeCost(wcetTool.getTargetMethod(),new AnalysisContextLocal(StaticCacheApproximation.ALWAYS_MISS));
            stop  = System.nanoTime();
            reportSpecial("always-miss",alwaysMissCost,start,stop,LpSolveWrapper.getSolverTime());
            wcetTool.setGenerateWCETReport(false);

            /* always hit */
            LpSolveWrapper.resetSolverTime();
            start = System.nanoTime();
            alwaysHitCost = an.computeCost(wcetTool.getTargetMethod(), new AnalysisContextLocal(StaticCacheApproximation.ALWAYS_HIT));
            stop  = System.nanoTime();
            reportSpecial("always-hit",alwaysHitCost,start,stop,LpSolveWrapper.getSolverTime());

            /* minimal cache cost (too expensive for large problems) */
            if(CALCULATE_MINIMUM_CACHE_COST)  {                
                IPETConfig mmcConfig = ipetConfig.clone();
                mmcConfig.setAssumeMissOnceOnInvoke(true);
                GlobalAnalysis gb = new GlobalAnalysis(wcetTool, mmcConfig);
                LpSolveWrapper.resetSolverTime();
                start = System.nanoTime();
                AnalysisContextLocal initialContext = new AnalysisContextLocal(StaticCacheApproximation.GLOBAL_ALL_FIT, CallString.EMPTY);
                minCacheCost = gb.computeWCET(wcetTool.getTargetMethod(), initialContext);
                stop  = System.nanoTime();
                reportSpecial("min-cache-cost",minCacheCost, start, stop, LpSolveWrapper.getSolverTime());
            }
        }        
    }

    private void computeWCET() throws IOException, DuplicateKeyException, XmlSerializationException, Config.BadConfigurationException, InvalidFlowFactException {
        StaticCacheApproximation preciseApprox = IPETConfig.getPreciseCacheApprox(config);
        wcetTool.setGenerateWCETReport(! reportGenerated);

        if(wcetTool.getProjectConfig().useUppaal()) {
            UppaalAnalysis an = new UppaalAnalysis(exec.getExecLogger(),wcetTool,wcetTool.getOutDir("uppaal"));
            config.checkPresent(UppAalConfig.UPPAAL_VERIFYTA_BINARY);

            /* Run uppaal analysis */
            long start = System.nanoTime();
            wcet = an.computeWCET(wcetTool.getTargetMethod(),alwaysMissCost.getCost());
            long stop  = System.nanoTime();
            reportUppaal(wcet,start,stop,an.getSearchtime(),an.getSolvertimemax());
        } else if(preciseApprox == StaticCacheApproximation.ALL_FIT_REGIONS) {
        	GlobalAnalysis an = new GlobalAnalysis(wcetTool, ipetConfig);
        	
//            RecursiveStrategy<AnalysisContextLocal, WcetCost> recStrategy =
//                new GlobalAnalysis.GlobalIPETStrategy(ipetConfig);
//            RecursiveWcetAnalysis<AnalysisContextLocal> an =
//                new RecursiveWcetAnalysis<AnalysisContextLocal>(
//                        wcetTool,
//                        ipetConfig,
//                        recStrategy);
//            wcet = an.computeCost(wcetTool.getTargetMethod(),
//                    new AnalysisContextLocal(preciseApprox));

        	String targetName = wcetTool.getTargetName();
        	Segment target = Segment.methodSegment(wcetTool, wcetTool.getTargetMethod(),
        			CallString.EMPTY, wcetTool.getProjectConfig().callstringLength());

        	/* Run global analysis */
            LpSolveWrapper.resetSolverTime();
            try {
                long start = System.nanoTime();
				wcet = an.computeWCET(targetName, target, preciseApprox);
	            long stop  = System.nanoTime();
	            report(wcet,start,stop,LpSolveWrapper.getSolverTime());
			} catch (LpSolveException e) {
				e.printStackTrace();
			}
            
        } else {
            AnalysisContextLocal initialContext = new AnalysisContextLocal(preciseApprox);
            RecursiveStrategy<AnalysisContextLocal, WcetCost> recStrategy =
                new LocalAnalysis(wcetTool, ipetConfig);
            RecursiveWcetAnalysis<AnalysisContextLocal> an =
                new RecursiveWcetAnalysis<AnalysisContextLocal>(wcetTool,ipetConfig,recStrategy);

            /* Run local analysis */
            LpSolveWrapper.resetSolverTime();
            long start = System.nanoTime();
            wcet = an.computeCost(wcetTool.getTargetMethod(),initialContext);
            long stop  = System.nanoTime();
            report(wcet,start,stop,LpSolveWrapper.getSolverTime());
        }
        exec.info("WCET analysis finished: "+wcet);
    }

    private void reportMetric(String metric, Object... args) {
        wcetTool.recordMetric(metric, args);
        System.out.print(metric+":");
        for(Object o : args) System.out.print(" "+o);
        System.out.println("");
    }

    private void report(WcetCost wcet, long start, long stop,double solverTime) {
        String key = "wcet";
        System.out.println(key+": "+wcet);
        System.out.println(key+".time: " + timeDiff(start,stop));
        System.out.println(key+".solvertime: " + solverTime);
        wcetTool.recordResult(wcet,timeDiff(start,stop),solverTime);
        wcetTool.getReport().addStat(key, wcet.toString());
    }

    private void reportUppaal(WcetCost wcet, long start, long stop, double searchtime, double solvertimemax) {
        String key = "wcet";
        System.out.println(key+": "+wcet);
        System.out.println(key+".time: " + timeDiff(start,stop));
        System.out.println(key+".searchtime: " + searchtime);
        System.out.println(key+".solvertimemax: " + solvertimemax);
        wcetTool.recordResultUppaal(wcet,timeDiff(start,stop),searchtime,solvertimemax);
        wcetTool.getReport().addStat(key, wcet.toString());
    }

    private void reportSpecial(String metric, WcetCost cost, long start, long stop, double solverTime) {
        String key = "wcet."+metric;
        System.out.println(key+": "+cost);
        if(start != stop) System.out.println(key+".time: " + timeDiff(start,stop));
        if(solverTime != 0) System.out.println(key+".solvertime: " + solverTime);
        wcetTool.recordSpecialResult(metric,cost);
        if (wcetTool.reportGenerationActive()) {
            wcetTool.getReport().addStat(key, cost.toString());
        }
    }
}
