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

import java.util.Map;
import java.util.Set;

import lpsolve.LpSolve;
import lpsolve.VersionInfo;

import org.apache.log4j.Logger;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.analyses.SymbolicAddress;
import com.jopdesign.wcet.analysis.AnalysisContextIpet;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.analysis.LocalAnalysis;
import com.jopdesign.wcet.analysis.AnalysisContextLocal;
import com.jopdesign.wcet.analysis.RecursiveAnalysis;
import com.jopdesign.wcet.analysis.RecursiveWcetAnalysis;
import com.jopdesign.wcet.analysis.TreeAnalysis;
import com.jopdesign.wcet.analysis.UppaalAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.analysis.cache.MethodCacheAnalysis;
import com.jopdesign.wcet.analysis.cache.ObjectCacheAnalysisDemo;
import com.jopdesign.wcet.analysis.cache.ObjectRefAnalysis;
import com.jopdesign.wcet.config.Config;
import com.jopdesign.wcet.config.Option;
import com.jopdesign.wcet.frontend.CallGraph.CallGraphNode;
import com.jopdesign.wcet.graphutils.MiscUtils;
import com.jopdesign.wcet.graphutils.MiscUtils.Function2;
import com.jopdesign.wcet.ipet.IpetConfig;
import com.jopdesign.wcet.ipet.LpSolveWrapper;
import com.jopdesign.wcet.ipet.IpetConfig.StaticCacheApproximation;
import com.jopdesign.wcet.jop.JOPConfig;
import com.jopdesign.wcet.jop.MethodCache;
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
	private static final boolean TESTING_BUILD = true;

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
        // new ConstantCache(project).build().dumpStats();

        /* Perf-Test */
//		for(int i = 0; i < 50; i++) {
//			RecursiveAnalysis<StaticCacheApproximation> an =
//				new RecursiveAnalysis<StaticCacheApproximation>(project,new RecursiveAnalysis.LocalIPETStrategy());
//			an.computeWCET(project.getTargetMethod(),StaticCacheApproximation.ALWAYS_HIT);
//		}
//		System.err.println("Total solver time (50): "+LpSolveWrapper.getSolverTime());
//		System.exit(1);

        //new ETMCExport(project).export(project.getOutFile("Spec_"+project.getProjectName()+".txt"));
        
        if(TESTING_BUILD) {
            testCacheAnalysis();        	
        }
        
        /* Run */
        boolean succeed = false;
        // FIXME: Report generation is a BIG MESS
        // bh will fix this in next revision
        try {
            /* Analysis */
            project.setGenerateWCETReport(false); /* generate reports later */
            exec.info("Cyclomatic complexity: " + project.computeCyclomaticComplexity(project.getTargetMethod()));
            WcetCost mincachecost, ah, am = null, wcet;
            IpetConfig ipetConfig = new IpetConfig(config);
            StaticCacheApproximation preciseApprox = IpetConfig.getPreciseCacheApprox(config);
            if(! project.getProcessorModel().hasMethodCache()) preciseApprox = StaticCacheApproximation.ALWAYS_MISS;
            /* Perform a few standard analysis (MIN_CACHE_COST, ALWAYS_HIT, ALWAYS_MISS) without call strings */
            if(project.getProcessorModel().hasMethodCache()) {
            	long start,stop;

                /* Tree based WCET analysis - has to be equal to ALWAYS_MISS */
                {
                    start = System.nanoTime();
                    TreeAnalysis treeAna = new TreeAnalysis(project, false);
                    long treeWCET = treeAna.computeWCET(project.getTargetMethod());
                    stop = System.nanoTime();
                    reportMetric("progress-measure",treeAna.getMaxProgress(project.getTargetMethod()));
                    reportSpecial("wcet.tree",WcetCost.totalCost(treeWCET),start,stop,0.0);
                }

                RecursiveWcetAnalysis<AnalysisContextLocal> an =
                    new RecursiveWcetAnalysis<AnalysisContextLocal>(
                            project, ipetConfig,
                            new LocalAnalysis(project,ipetConfig));

                /* FIXME: We don't have  report generation for UPPAAL and global analysis yet,
                 * therefore we generate our report here */
                if(project.getProjectConfig().useUppaal() || preciseApprox.needsInterProcIPET()) {
                    project.setGenerateWCETReport(true);
                }
                /* always miss */
                start = System.nanoTime();
                am = an.computeCost(project.getTargetMethod(),new AnalysisContextLocal(StaticCacheApproximation.ALWAYS_MISS));
                stop  = System.nanoTime();
                reportSpecial("always-miss",am,start,stop,LpSolveWrapper.getSolverTime());
                project.setGenerateWCETReport(false);

                /* always hit */
                LpSolveWrapper.resetSolverTime();
                start = System.nanoTime();
                ah = an.computeCost(project.getTargetMethod(), new AnalysisContextLocal(StaticCacheApproximation.ALWAYS_HIT));
                stop  = System.nanoTime();
                reportSpecial("always-hit",ah,start,stop,LpSolveWrapper.getSolverTime());

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

                /* Run uppaal analysis */
                long start = System.nanoTime();
                wcet = an.computeWCET(project.getTargetMethod(),am.getCost());
                long stop  = System.nanoTime();
                reportUppaal(wcet,start,stop,an.getSearchtime(),an.getSolvertimemax());
            } else if(preciseApprox == StaticCacheApproximation.ALL_FIT_REGIONS) {
                RecursiveStrategy<AnalysisContextIpet, WcetCost> recStrategy =
                    new GlobalAnalysis.GlobalIPETStrategy(ipetConfig);
                RecursiveWcetAnalysis<AnalysisContextIpet> an =
                    new RecursiveWcetAnalysis<AnalysisContextIpet>(
                            project,
                            ipetConfig,
                            recStrategy);

                /* Run global analysis */
                LpSolveWrapper.resetSolverTime();
                long start = System.nanoTime();
                wcet = an.computeCost(project.getTargetMethod(),
                                      new AnalysisContextIpet(preciseApprox));
                long stop  = System.nanoTime();
                report(wcet,start,stop,LpSolveWrapper.getSolverTime());
            } else {
                AnalysisContextLocal initialContext = new AnalysisContextLocal(preciseApprox);
                RecursiveStrategy<AnalysisContextLocal, WcetCost> recStrategy =
                	new LocalAnalysis(project, ipetConfig);
                RecursiveWcetAnalysis<AnalysisContextLocal> an =
                    new RecursiveWcetAnalysis<AnalysisContextLocal>(project,ipetConfig,recStrategy);

                /* Run local analysis */
                project.setGenerateWCETReport(true);
                LpSolveWrapper.resetSolverTime();
                long start = System.nanoTime();
                wcet = an.computeCost(project.getTargetMethod(),initialContext);
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

	private void testCacheAnalysis() {
		long start,stop;
		// Method Cache
		//testExactAllFit();
		// Object Cache (total, allfit)
		ObjectRefAnalysis orefAnalysis = new ObjectRefAnalysis(project);
		LpSolveWrapper.resetSolverTime();
        start = System.nanoTime();
		orefAnalysis.analyzeRefUsage();
        stop = System.nanoTime();
		System.err.println(
				String.format("[Object Reference Analysis]: Total time: %.2f s / Total solver time: %.2f s",
						timeDiff(start,stop),
						LpSolveWrapper.getSolverTime()));        

		refUsageTotal_ = new ObjectRefAnalysis(project,true).getRefUsage();
		refUsageNames_ = orefAnalysis.getUsedSymbolicNames();
		Map<CallGraphNode, Long> refUsageDistinct = orefAnalysis.getRefUsage();
		MiscUtils.printMap(System.out, refUsageDistinct, new Function2<CallGraphNode, Long,String>() {
			public String apply(CallGraphNode v1, Long usedRefs) {
				return String.format("%-50s ==> %3d <= %3d (%s)",
						v1.getMethodImpl().getFQMethodName(),
						usedRefs,
						refUsageTotal_.get(v1),
						refUsageNames_.get(v1)
						);
			}        	
		});		
		// Object cache, evaluation
		ObjectCacheAnalysisDemo oca;
		int[] cacheSizes = { 0,1,2,4,8,16,32,64, 128 };
		long accesses = 0;
		for(int cacheSize : cacheSizes) {
			oca = new ObjectCacheAnalysisDemo(project, cacheSize);
			long cost = oca.computeCost();
			double ratio;
			if(cacheSize == 0) { accesses = cost; ratio = 1.0; }
			else               { ratio = (double)(accesses-cost)/(double)accesses; }
			System.out.println(
				String.format("Cache Misses [N=%3d]: %d  (%.2f %%)", cacheSize, cost, ratio*100));				
		}
	}
	private Map<CallGraphNode, Long> refUsageTotal_;
	private Map<CallGraphNode, Set<SymbolicAddress>> refUsageNames_;

	private void testExactAllFit() {
		long start,stop;
        start = System.nanoTime();
		LpSolveWrapper.resetSolverTime();
		MethodCacheAnalysis mcAnalysis = new MethodCacheAnalysis(project);
		mcAnalysis.analyzeBlockUsage();
        stop  = System.nanoTime();
		System.err.println(
				String.format("[Method Cache Analysis]: Total time: %.2f s / Total solver time: %.2f s",
						timeDiff(start,stop),
						LpSolveWrapper.getSolverTime()));        
		Map<CallGraphNode, Long> blockUsage = mcAnalysis.getBlockUsage();
		MiscUtils.printMap(System.out, blockUsage, new Function2<CallGraphNode, Long,String>() {
			public String apply(CallGraphNode v1, Long maxBlocks) {
		        MethodCache mc = project.getProcessorModel().getMethodCache();
				return String.format("%-50s ==> %2d <= %2d",
						v1.getMethodImpl().getFQMethodName(),
						maxBlocks,
						mc.getAllFitCacheBlocks(v1.getMethodImpl()));
			}        	
		});		
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
