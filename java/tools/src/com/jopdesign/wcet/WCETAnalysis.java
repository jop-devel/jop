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
import com.jopdesign.common.code.BasicBlock;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.Segment;
import com.jopdesign.common.code.CallGraph.ContextEdge;
import com.jopdesign.common.code.SuperGraph.ContextCFG;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphNode;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.dfa.DFATool;
import com.jopdesign.wcet.analysis.AnalysisContextLocal;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.analysis.LocalAnalysis;
import com.jopdesign.wcet.analysis.cache.CacheAnalysis.UnsupportedCacheModelException;
import com.jopdesign.wcet.analysis.cache.MethodCacheAnalysis;
import com.jopdesign.wcet.analysis.InvalidFlowFactException;
import com.jopdesign.wcet.analysis.RecursiveWcetAnalysis;
import com.jopdesign.wcet.analysis.TreeAnalysis;
import com.jopdesign.wcet.analysis.UppaalAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETConfig.CacheCostCalculationMethod;
import com.jopdesign.wcet.ipet.LpSolveWrapper;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import com.jopdesign.wcet.uppaal.model.DuplicateKeyException;
import com.jopdesign.wcet.uppaal.model.XmlSerializationException;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MONITORENTER;
import org.apache.bcel.generic.MONITOREXIT;
import org.apache.log4j.Logger;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import lpsolve.LpSolveException;

import static com.jopdesign.wcet.ExecHelper.timeDiff;

/**
 * WCET Analysis for JOP - Executable
 */
public class WCETAnalysis {

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
			exec.info("Cumulative LP/ILP solver time: "+LpSolveWrapper.getTotalSolverTime());
			exec.info("Cumulative WCET Calculation time: "+inst.totalWCETCalculationTime);
		} catch (Exception e) {
			 exec.bail("Worst Case Analysis failed: " + e);
		}  	
    }

    private Config config;
    private WCETTool wcetTool;

    // TODO: Maybe exec can be replaced by something stefan's framework?
    private ExecHelper exec;
    
    private WcetCost wcet;
    private WcetCost alwaysMissCost;
    private WcetCost alwaysHitCost;
	private WcetCost approxCost;
	
    private IPETConfig ipetConfig;
	private double totalWCETCalculationTime = 0.0;

    public WCETAnalysis(WCETTool wcetTool, ExecHelper e) {

        this.wcetTool = wcetTool;
        this.config = wcetTool.getConfig();
        this.exec   = e;
    }

    private boolean run() {

        /* Initialize */
        try {
            wcetTool.setTopLevelLogger(exec.getExecLogger());
            exec.info("Loading project");
            wcetTool.initialize(wcetTool.getProjectConfig().doLoadLinkInfo(), true);
        } catch (Exception e) {
            exec.logException("Loading project", e);
            return false;
        }

        // exploreCacheAnalysis();
        
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

    	MethodInfo targetMethod = wcetTool.getTargetMethod();

        if(wcetTool.getProjectConfig().doObjectCacheAnalysis()) {
            ObjectCacheEvaluation oca = new ObjectCacheEvaluation(wcetTool);
            return oca.run(targetMethod);
        } else {
            return runWCETAnalysis(targetMethod);
        }
        
    }

	private boolean runWCETAnalysis(MethodInfo targetMethod) {
        /* Run */
        ipetConfig = new IPETConfig(config);

        try {
            /* Analysis */

        	/* some metrics and some cheap analyses for comparison and
        	 * report logging (not supported by global/uppaal at the moment) */
            runMetrics(targetMethod); 
            
            if(wcetTool.getProjectConfig().doBlockingTimeAnalysis()) {
            	runBlockingTimeAnalysis(targetMethod);
            }
            
            exec.info("Starting precise WCET analysis");
            /* uppaal */
            if(wcetTool.getProjectConfig().useUppaal()) {
            	runUppaal(targetMethod);
            } 
            /* global IPET */
            else {
            	runGlobal(targetMethod);
            }
            exec.info("WCET analysis finished: "+wcet);
            
            /* report generation */
            return generateReport();

        } catch (Exception e) {
            exec.logException("analysis", e);
            return false;
        }
    }


	private void runMetrics(MethodInfo targetMethod) throws Exception {
	
		/* generate reports later for simple_fit */
		wcetTool.setGenerateWCETReport(false); 
		
		/* check whether the largest method fits into the cache */
        List<MethodInfo> allMethods = wcetTool.getCallGraph().getReachableImplementations(targetMethod);
        MethodInfo largestMethod = MethodCacheAnalysis.checkCache(wcetTool, allMethods); 
        int minWords = MiscUtils.bytesToWords(largestMethod.getCode().getNumberOfBytes());
        reportMetric("min-cache-size",largestMethod.getFQMethodName(),minWords);

	    /* Compute cyclomatic complexity */
		exec.info("Cyclomatic complexity: " + wcetTool.computeCyclomaticComplexity(targetMethod));
	    
		/* Fast, useful cache approximationx */
		CacheCostCalculationMethod cacheApprox = CacheCostCalculationMethod.ALL_FIT_SIMPLE;
	    
	    /* Perform a few standard analysis (MIN_CACHE_COSdT, ALWAYS_HIT, ALWAYS_MISS) without call strings */
		long start,stop;

		/* Tree based WCET analysis - has to be equal to ALWAYS_MISS if no flow facts are used */
		{
			start = System.nanoTime();
			TreeAnalysis treeAna = new TreeAnalysis(wcetTool, false);
			long treeWCET = treeAna.computeWCET(targetMethod);
			stop = System.nanoTime();
			reportMetric("progress-measure",treeAna.getMaxProgress(targetMethod));
			reportSpecial("wcet.tree",WcetCost.totalCost(treeWCET),start,stop,0.0);
		}

		RecursiveWcetAnalysis<AnalysisContextLocal> an =
				new RecursiveWcetAnalysis<AnalysisContextLocal>(
						wcetTool, ipetConfig,
						new LocalAnalysis(wcetTool,ipetConfig));


		/* always miss */
		start = System.nanoTime();
		alwaysMissCost = an.computeCost(targetMethod,new AnalysisContextLocal(CacheCostCalculationMethod.ALWAYS_MISS));
		stop  = System.nanoTime();
		reportSpecial("always-miss",alwaysMissCost,start,stop,LpSolveWrapper.getSolverTime());

		/* always hit */
		LpSolveWrapper.resetSolverTime();
		start = System.nanoTime();
		alwaysHitCost = an.computeCost(targetMethod, new AnalysisContextLocal(CacheCostCalculationMethod.ALWAYS_HIT));
		stop  = System.nanoTime();
		reportSpecial("always-hit",alwaysHitCost,start,stop,LpSolveWrapper.getSolverTime());

		/* simple approx */
		wcetTool.setGenerateWCETReport(true);
		start = System.nanoTime();
		approxCost = an.computeCost(targetMethod,new AnalysisContextLocal(cacheApprox));
		stop  = System.nanoTime();
		reportSpecial("recursive-report",approxCost,start,stop,LpSolveWrapper.getSolverTime());
		wcetTool.setGenerateWCETReport(false);
	}

	private static class SynchronizedBlockResult {
		int id;
		Segment synchronizedSegment;
		CFGNode node;
		InstructionHandle ih;
		WcetCost cost;
		List<SynchronizedBlockResult> nested = new ArrayList<SynchronizedBlockResult>();
		public SynchronizedBlockResult(int id, Segment synchronizedSegment, CFGNode node,
				InstructionHandle ih, WcetCost wcet) {
			this.id = id;
			this.synchronizedSegment = synchronizedSegment;
			this.node = node;
			this.ih = ih;
			this.cost = wcet;
		}
		public void dump(PrintStream ps) {
			ps.println("[" + id + "] " + this.getPosDescr()+" "+cost);
			if(nested.size() > 0) {
				ps.println("  Nested Blocks: ");
				for(SynchronizedBlockResult r : nested) {
					ps.println("    "+r.getPosDescr());
				}
			}
		}
		private String getPosDescr() {
			return node.getBasicBlock().getStartLine();
		}
	}
	private void runBlockingTimeAnalysis(MethodInfo targetMethod)
			throws InvalidFlowFactException, LpSolveException, UnsupportedCacheModelException {

		GlobalAnalysis an = new GlobalAnalysis(wcetTool, ipetConfig);                                    
		CacheCostCalculationMethod requestedCacheApprox = IPETConfig.getRequestedCacheApprox(config);    
		
		/* Find all synchronized segments */
	    Segment target = Segment.methodSegment(targetMethod, CallString.EMPTY,
	    		wcetTool, wcetTool.getCallstringLength(), wcetTool);
	    ArrayList<SynchronizedBlockResult> sBlocks = new ArrayList<SynchronizedBlockResult>();
	    
	    for(ContextCFG ccfg : target.getCallGraphNodes()) {
			for (CFGNode cfgNode : ccfg.getCfg().vertexSet()) {
				if(cfgNode.getBasicBlock() == null) continue;
				for (InstructionHandle ih : cfgNode.getBasicBlock().getInstructions()) {
					if (ih.getInstruction() instanceof MONITORENTER) {
						/* compute synchronized block WCET */
						Segment synchronizedSegment = Segment.synchronizedSegment(ccfg, cfgNode, ih,
								wcetTool,wcetTool.getCallstringLength(), wcetTool);                                                                                                
						wcet = an.computeWCET(targetMethod.getShortName(), synchronizedSegment, requestedCacheApprox); 
						sBlocks.add(new SynchronizedBlockResult(sBlocks.size(), synchronizedSegment, cfgNode, ih, wcet));
					}
				}
			}	    	
	    }
		/* check nested synchronized blocks */
		for(SynchronizedBlockResult sBlock : sBlocks) {
			for(SynchronizedBlockResult otherBlock : sBlocks) {
				if(sBlock == otherBlock) continue;
				for(SuperGraphEdge entryEdge : otherBlock.synchronizedSegment.getEntryEdges()) {
					if(sBlock.synchronizedSegment.includesEdge(entryEdge)) {
						sBlock.nested.add(otherBlock);
						break;
					}
				}
			}
		}
		System.out.println("=== Synchronized Blocks ===");
		for(SynchronizedBlockResult sBlock : sBlocks) {
			sBlock.dump(System.out);
		}
	}


	private void runGlobal(MethodInfo targetMethod)
			throws InvalidFlowFactException, LpSolveException, UnsupportedCacheModelException  {
		
	    CacheCostCalculationMethod requestedCacheApprox = IPETConfig.getRequestedCacheApprox(config);
	
	    GlobalAnalysis an = new GlobalAnalysis(wcetTool, ipetConfig);
	
	    Segment target = Segment.methodSegment(targetMethod, CallString.EMPTY,
	    		wcetTool, wcetTool.getCallstringLength(), wcetTool);
	
	    /* Run global analysis */
    	// for(CacheCostCalculationMethod cacheApprox : CacheCostCalculationMethod.values()) {
    	LpSolveWrapper.resetSolverTime();
    	long start = System.nanoTime();
    	wcet = an.computeWCET(targetMethod.getShortName(), target, requestedCacheApprox);
    	long stop  = System.nanoTime();
		report(wcet, start, stop, LpSolveWrapper.getSolverTime());
	}

	/**
	 * @throws BadConfigurationException 
	 * @throws XmlSerializationException 
	 * @throws DuplicateKeyException 
	 * @throws IOException 
	 */
	private void runUppaal(MethodInfo targetMethod)
			throws BadConfigurationException, IOException, DuplicateKeyException, XmlSerializationException {

		UppaalAnalysis an = new UppaalAnalysis(exec.getExecLogger(),wcetTool,wcetTool.getOutDir("uppaal"));
	    config.checkPresent(UppAalConfig.UPPAAL_VERIFYTA_BINARY);
	
	    /* Run uppaal analysis */
	    long start = System.nanoTime();
	    wcet = an.computeWCET(targetMethod,alwaysMissCost.getCost());
	    long stop  = System.nanoTime();
	    reportUppaal(wcet,start,stop,an.getSearchtime(),an.getSolvertimemax());
	}

	/**
	 * @param mca
	 * @param iter
	 * @throws InvalidFlowFactException
	 */
	@SuppressWarnings("unused")
	private void exploreCacheAnalysis()
			throws InvalidFlowFactException {
	
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
	        Segment segment = Segment.methodSegment(scope.getMethodInfo(), scope.getCallString(), wcetTool,
	        		wcetTool.getCallstringLength(), wcetTool);
	        
	        int availBlocks = wcetTool.getWCETProcessorModel().getMethodCache().getNumBlocks();
	        long total, distinctApprox = -1, distinct = -1;
	        
	        blocks = total = mca.countDistinctBlocksUsed(segment);
	        if(total > availBlocks || true) {
	        	try {
					blocks = distinctApprox = mca.countDistinctBlocksAccessed(segment, false);
	                if(blocks > availBlocks && blocks < availBlocks*2 || true) {
	            		blocks = distinct = mca.countDistinctBlocksAccessed(segment, true);                	
	                }
				} catch (LpSolveException e) {
	        		System.err.println((distinctApprox>=0 ? "I" : "Relaxed ")+"LP Problem too difficult, giving up: "+e);                		
				}
	        }
	        System.out.println(String.format("block-count < %2d [%2d,%2d,%2d] for %-30s @ %s", blocks,
	        		total, distinctApprox, distinct,
	        		scope.getMethodInfo().getFQMethodName(), scope.getCallString().toStringVerbose(false)));
	    }
	    long stop  = System.nanoTime();
	    reportSpecial("block-count",WcetCost.totalCost(blocks),start,stop,LpSolveWrapper.getSolverTime());
	    System.out.println("solver-time: "+LpSolveWrapper.getSolverTime());
	}

	/**
	 * @return
	 */
	private boolean generateReport() {
        if (!wcetTool.getProjectConfig().doGenerateReport()) {
            exec.info("Ommiting HTML report");
            return true;
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
            return false;
        }
		return true;
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
    	totalWCETCalculationTime  += timeDiff(start,stop);
        System.out.println(key+".solvertime: " + solverTime);
        wcetTool.recordResult(wcet,timeDiff(start,stop),solverTime);
        wcetTool.getReport().addStat(key, wcet.toString());
    }

    private void reportUppaal(WcetCost wcet, long start, long stop, double searchtime, double solvertimemax) {
    	
        String key = "wcet";
        System.out.println(key+": "+wcet);
        System.out.println(key+".time: " + timeDiff(start,stop));
    	totalWCETCalculationTime  += timeDiff(start,stop);
        System.out.println(key+".searchtime: " + searchtime);
        System.out.println(key+".solvertimemax: " + solvertimemax);
        wcetTool.recordResultUppaal(wcet,timeDiff(start,stop),searchtime,solvertimemax);
        wcetTool.getReport().addStat(key, wcet.toString());
    }

    private void reportSpecial(String name, WcetCost cost) {
    	
        String key = "wcet."+name;
        System.out.println(key+": "+cost);
        wcetTool.recordSpecialResult(name,cost);
        if (wcetTool.reportGenerationActive()) {
            wcetTool.getReport().addStat(key, cost.toString());
        }
    }
    private void reportSpecial(String metric, WcetCost cost, long start, long stop, double solverTime) {
    	
        reportSpecial(metric,cost);

        String key = "wcet."+metric;
    	System.out.println(key+".time: " + timeDiff(start,stop));
    	totalWCETCalculationTime  += timeDiff(start,stop);
        if(solverTime != 0) System.out.println(key+".solvertime: " + solverTime);
    }
}
