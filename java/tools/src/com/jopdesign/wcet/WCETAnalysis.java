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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import lpsolve.LpSolve;
import lpsolve.VersionInfo;

import org.apache.log4j.Logger;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.analyses.SymbolicAddress;
import com.jopdesign.dfa.framework.CallString;
import com.jopdesign.wcet.analysis.AnalysisContextIpet;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.analysis.LocalAnalysis;
import com.jopdesign.wcet.analysis.AnalysisContextLocal;
import com.jopdesign.wcet.analysis.RecursiveWcetAnalysis;
import com.jopdesign.wcet.analysis.TreeAnalysis;
import com.jopdesign.wcet.analysis.UppaalAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.analysis.cache.MethodCacheAnalysis;
import com.jopdesign.wcet.analysis.cache.ObjectCacheAnalysisDemo;
import com.jopdesign.wcet.analysis.cache.ObjectCacheEvaluation;
import com.jopdesign.wcet.analysis.cache.ObjectRefAnalysis;
import com.jopdesign.wcet.analysis.cache.ObjectCacheAnalysisDemo.ObjectCacheCost;
import com.jopdesign.wcet.analysis.cache.ObjectCacheEvaluation.OCacheAnalysisResult;
import com.jopdesign.wcet.analysis.cache.ObjectCacheEvaluation.OCacheMode;
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
	private static final boolean CALCULATE_MINIMUM_CACHE_COST = false;

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

        
        if(project.getProjectConfig().doObjectCacheAnalysis()) {
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

                /* minimal cache cost (too expensive for large problems) */
                if(CALCULATE_MINIMUM_CACHE_COST)  {                
                	IpetConfig mmcConfig = ipetConfig.clone();
                	mmcConfig.assumeMissOnceOnInvoke = true;
                	GlobalAnalysis gb = new GlobalAnalysis(project, mmcConfig);
                	LpSolveWrapper.resetSolverTime();
                	start = System.nanoTime();
                	mincachecost = gb.computeWCET(project.getTargetMethod(), StaticCacheApproximation.GLOBAL_ALL_FIT);
                	stop  = System.nanoTime();
                	reportSpecial("min-cache-cost",mincachecost, start, stop, LpSolveWrapper.getSolverTime());
                }
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

		JOPConfig jopconfig = new JOPConfig(project);

		// Method Cache
		//testExactAllFit();
		// Object Cache (total, allfit)
		Map<CallGraphNode, Long> refUsageTotal; 
		Map<CallGraphNode, Set<SymbolicAddress>> refUsageNames;
		Map<CallGraphNode, Set<String>> refUsageSaturatedTypes;
		Map<CallGraphNode, Long> refUsageDistinct;
		Map<CallGraphNode, Long> fieldUsageDistinct;

//		ObjectRefAnalysis orefAnalysisCountAll = new ObjectRefAnalysis(project, 65536, ObjectCacheAnalysisDemo.DEFAULT_SET_SIZE,true);
//		refUsageTotal = orefAnalysisCountAll.getMaxReferencesAccessed();
		
	//	ObjectRefAnalysis orefAnalysis = new ObjectRefAnalysis(project, 65536, ObjectCacheAnalysisDemo.DEFAULT_SET_SIZE);
//		LpSolveWrapper.resetSolverTime();
//        start = System.nanoTime();
//		orefAnalysis.analyzeRefUsage();
//        stop = System.nanoTime();
//		System.err.println(
//				String.format("[Object Reference Analysis]: Total time: %.2f s / Total solver time: %.2f s",
//						timeDiff(start,stop),
//						LpSolveWrapper.getSolverTime()));   
//		refUsageNames = orefAnalysis.getUsedSymbolicNames();
//		refUsageSaturatedTypes = orefAnalysis.getSaturatedRefSets();
//		refUsageDistinct = orefAnalysis.getMaxReferencesAccessed();
//		fieldUsageDistinct = orefAnalysis.getMaxFieldsAccessed();
		
//		for(Entry<CallGraphNode, Long> entry : refUsageDistinct.entrySet()) {
//			CallGraphNode node = entry.getKey();
//			Long usedRefs = entry.getValue();
//			String entryString = String.format("%-50s ==> %3d (%3d fields) <= %3d (%s) ; Saturated Types: (%s)",
//						node.getMethodImpl().methodId,
//						usedRefs,
//						fieldUsageDistinct.get(node),
//						refUsageTotal.get(node),
//						refUsageNames.get(node),
//						refUsageSaturatedTypes.get(node).toString()
//						);
//			System.out.println("  "+entryString);
//		}
		// Object cache, evaluation
		PrintStream pStream;
		ExecHelper.TeePrintStream oStream;
		try {
			 pStream = new PrintStream(project.getOutFile("ocache_eval.txt"));
			 oStream = new ExecHelper.TeePrintStream(System.out, pStream);
		} catch (FileNotFoundException e) {
			 oStream = new ExecHelper.TeePrintStream(System.out, null);
		}
		ObjectCacheAnalysisDemo oca;
		int[][] configs = { 
				{ 2,0,1 },  // sram,uni
				{ 2,10,4},  // sdram,uni 
				{ 17,0,1},  // sram, cmp8, s=2, tmax= 9*2-1 = 17
				{ 2,153,4}  // sdram, cmp8 (4-word burst), s=18, tmax= 8*18 - 1 + 10 * 2*w = 153+2w
			};
		OCacheMode[] modes = { OCacheMode.WORD_FILL, OCacheMode.LINE_FILL, OCacheMode.SINGLE_FIELD };
		List<OCacheAnalysisResult> samples = new ArrayList<OCacheAnalysisResult>();
		int[] cacheWays = { 0,1,2,4,8,16,32, 64 }; // need to be in ascending order
		int[] lineSizesObjCache  = { 1,2,4,8,16,32};
		int[] lineSizesFieldCache = { 1 };
		int[] lineSizes;
		for(int configId=0; configId < configs.length; configId++) {
			int[] ocConfig = configs[configId];
			jopconfig.objectCacheCyclesPerWord = ocConfig[0];
			jopconfig.objectCacheAccessDelay = ocConfig[1];
			jopconfig.objectCacheMaxBurst = ocConfig[2];
			oStream.println("---------------------------------------------------------");
			oStream.println("Object Cache Configuration: "+configId);
			oStream.println("---------------------------------------------------------");
			for(OCacheMode mode : modes) {
				long maxCost = 0;
				//			long cacheMisses = Long.MAX_VALUE;
				String modeString;
				lineSizes = lineSizesObjCache;
				if(mode == OCacheMode.WORD_FILL) modeString = "fill-word";
				else if(mode == OCacheMode.LINE_FILL) modeString = "fill-line";
				else {
					modeString = "field-as-tag";
					lineSizes = lineSizesFieldCache;
				}
				boolean first = true;
				for(int lineSize : lineSizes) {
					/* We have to take field access count of cache size = 0; our analysis otherwise does not assign
					 * sensible field access counts (thats the fault of the IPET method)
					 */
					long totalFieldAccesses = -1, cachedFieldAccesses = -1;
					double bestCyclesPerAccessForConfig = Double.POSITIVE_INFINITY;
					long bestCostPerConfig = Long.MAX_VALUE;
					// assume cacheSizes are in ascending order
					for(int ways : cacheWays) {
						boolean useFillLine = (mode==OCacheMode.LINE_FILL) && ways>0; 
						jopconfig.setObjectCacheAssociativity(ways);
						jopconfig.setObjectCacheFillLine(useFillLine);				
						jopconfig.setObjectCacheFieldTag(mode == OCacheMode.SINGLE_FIELD);
						jopconfig.setObjectCacheLineSize(lineSize);
						oca = new ObjectCacheAnalysisDemo(project, jopconfig);

						double cyclesPerAccess;
						ObjectCacheCost ocCost = oca.computeCost(); 
						long cost = ocCost.getCost();
						if(cost < bestCostPerConfig) bestCostPerConfig = cost;

						double bestRatio,ratio;
						if(ways == 0) { 
							maxCost = cost; 
							totalFieldAccesses = ocCost.getTotalFieldAccesses();
							cachedFieldAccesses = ocCost.getFieldAccessesWithoutBypass();
							bestRatio = 1.0; 
							ratio = 1.0;
						} else  { 
							bestRatio = (double)bestCostPerConfig/(double)maxCost;
							ratio = (double)cost/(double)maxCost; 
						}
						cyclesPerAccess = (double)cost / (double)totalFieldAccesses ;						
						if(cyclesPerAccess < bestCyclesPerAccessForConfig) bestCyclesPerAccessForConfig = cyclesPerAccess;

						/* hit rate is defined as: 1 - (cache misses / accesses to cached fields (with n=0) */
						double hitRate = (1 - ((double)ocCost.getCacheMissCount() / (double)cachedFieldAccesses));
						
						if(first) {
							oStream.println(String.format("***** ***** MODE = %s ***** *****\n",modeString));
							oStream.println(String.format(" - max tags accessed (upper bound) = %d, max fields accesses = %d",
									oca.getMaxAccessedTags(project.getTargetMethod(), CallString.EMPTY), totalFieldAccesses)
							);						
							first = false;
						}					
						
						String report = String.format(" + Cycles Per Access [N=%3d,l=%2d]: %.2f (%d total cost, %.2f %% cost of no cache, %d bypass cost)", //, %.2f %% 'hitrate')", 
								ways, lineSize, bestCyclesPerAccessForConfig, cost, bestRatio*100, ocCost.getBypassCost());
						if(bestCostPerConfig > cost) {
							report += String.format(" # (analysis cost increased by %.2f %% for this associativity)",ratio*100);
						}
						oStream.println(report);
						OCacheAnalysisResult sample =
							new ObjectCacheEvaluation.OCacheAnalysisResult(mode, ways, lineSize, configId, hitRate, bestCyclesPerAccessForConfig);
						samples.add(sample);
					}
				}
			}
		}
		OCacheAnalysisResult.dumpLatex(samples, oStream);
	} 
	
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
