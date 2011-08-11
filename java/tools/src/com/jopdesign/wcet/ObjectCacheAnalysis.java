/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.wcet;

import com.jopdesign.common.code.CallGraph.ContextEdge;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.processormodel.JOPConfig;
import com.jopdesign.dfa.analyses.SymbolicAddress;
import com.jopdesign.wcet.analysis.InvalidFlowFactException;
import com.jopdesign.wcet.analysis.cache.MethodCacheAnalysis;
import com.jopdesign.wcet.analysis.cache.ObjectCacheAnalysisDemo;
import com.jopdesign.wcet.analysis.cache.ObjectCacheAnalysisDemo.ObjectCacheCost;
import com.jopdesign.wcet.analysis.cache.ObjectCacheEvaluation;
import com.jopdesign.wcet.analysis.cache.ObjectCacheEvaluation.OCacheAnalysisResult;
import com.jopdesign.wcet.analysis.cache.ObjectCacheEvaluation.OCacheMode;
import com.jopdesign.wcet.analysis.cache.ObjectRefAnalysis;
import com.jopdesign.wcet.ipet.LpSolveWrapper;
import com.jopdesign.wcet.jop.MethodCache;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jopdesign.wcet.ExecHelper.timeDiff;

public class ObjectCacheAnalysis {
	/* generator for object cache timings */
	private interface ObjectCacheTiming {
		int  loadTime(int words);
		void setObjectCacheTiming(JOPConfig jopConfig, int lineSize);
	}
	
	private static class OCTimingUni implements ObjectCacheTiming {
		private int accessCycles;
		private int delay;
		private int cyclesPerWord;

		public OCTimingUni(int accessCycles, int delay, int cyclesPerWord) {
			this.accessCycles = accessCycles;
			this.delay = delay;
			this.cyclesPerWord = cyclesPerWord;
		}
		
		public int loadTime(int words) {
			return delay + words * cyclesPerWord;			
		}
		
		public void setObjectCacheTiming(JOPConfig jopConfig, int blockSize) {
			jopConfig.setObjectCacheHitCycles(accessCycles);
			jopConfig.setObjectCacheLoadFieldCycles(accessCycles + loadTime(1));
			jopConfig.setObjectCacheLoadBlockCycles(accessCycles + loadTime(blockSize));
		}
		
		public String toString() {
			return String.format("S(D)RAM [access=%d, delay=%d, cycles-per-word=%d]",
					accessCycles,delay,cyclesPerWord);
		}
	}

	private static class OCTimingCmp implements ObjectCacheTiming {
		private int cores;
		private int wordsPerSlot;

		private int accessCycles;
		private int cyclesPerWord;
		private int delay;
		/* FIXME: may too conservative for wordsPerSlot > 1; do we have to wait (n-1) s + s-1 cycles before 
		 * loading the first word even for large slot length?? */
		public OCTimingCmp(int cores, int wordsPerSlot, int accessCycles, int delay, int cyclesPerWord) {
			this.cores = cores;
			this.wordsPerSlot = wordsPerSlot;
			
			this.accessCycles = accessCycles;			
			this.delay = delay;
			this.cyclesPerWord = cyclesPerWord;			
		}
		
		// slot length = time to transmit wordsPerSlot words
		public int getSlotLength() { 
			return delay + cyclesPerWord*wordsPerSlot; 
		}
		
		// tmax (conservative, as I do not know CMP well enough for a more precise formula)
		//    W .. words to transmit
		//    S .. words per slot 
		// tmax = s-1 + n * s * ceil(W/S)
		public int loadTime(int words) {
			int s = getSlotLength();
			int maxRounds = ((words+wordsPerSlot-1)/wordsPerSlot);
			return (s-1) + cores * s * maxRounds;
		}

		public void setObjectCacheTiming(JOPConfig jopConfig, int cacheBlockSize) {			
			jopConfig.setObjectCacheHitCycles(accessCycles);
			jopConfig.setObjectCacheLoadFieldCycles(accessCycles + loadTime(1));
			jopConfig.setObjectCacheLoadBlockCycles(accessCycles + loadTime(cacheBlockSize));
		}
		
		public String toString() {
			return String.format("SRAM [cores=%d,slotlength(l=4)=%d,access=%d,delay=%d,cycles-per-word=%d]",
					cores,getSlotLength(),accessCycles,delay,cyclesPerWord);
		}
	}

	
	
	private WCETTool project;

	public ObjectCacheAnalysis(WCETTool project) {
		this.project = project;
	}
	
	public boolean run() {
		evaluateObjectCache();		
		return true;
	}

	private void evaluateObjectCache() {
		long start,stop;

        // TODO check if we can/need to get jopconfig elsewhere
		JOPConfig jopconfig = new JOPConfig(project.getConfig());

		// Method Cache
		//testExactAllFit();

		// Object Cache (debugging)

		ObjectRefAnalysis orefAnalysis = new ObjectRefAnalysis(project, false, 1, 65536, ObjectCacheAnalysisDemo.DEFAULT_SET_SIZE);
		TopologicalOrderIterator<ExecutionContext, ContextEdge> cgIter = this.project.getCallGraph().topDownIterator();
		while(cgIter.hasNext()) {
			ExecutionContext scope = cgIter.next();
			Set<SymbolicAddress> addresses = orefAnalysis.getAddressSet(scope);
			String entryString = String.format("%-50s ==> |%d|%s ; Saturated Types: (%s)",
						scope,
						addresses.size(),
						orefAnalysis.getAddressSet(scope),
						orefAnalysis.getSaturatedTypes(scope));
			System.out.println("  "+entryString);
		}
		
		// Object cache, evaluation
		PrintStream pStream;
		ExecHelper.TeePrintStream oStream;
		try {
			 pStream = new PrintStream(project.getProjectConfig().getOutFile("ocache","eval.txt"));
			 oStream = new ExecHelper.TeePrintStream(System.out, pStream);
		} catch (FileNotFoundException e) {
			 oStream = new ExecHelper.TeePrintStream(System.out, null);
		}
		ObjectCacheAnalysisDemo oca;
		ObjectCacheTiming configs[] = { 
				new OCTimingUni(0,0,2),  // sram,uni:  2 cycles field cost, 2*w cycles line cost
				new OCTimingUni(0,10,2), // sdram,uni: 10+2*w cycles for each w-word access
				new OCTimingCmp(8, 1, 0, 0, 2), // SRAM, cmp, 2 cycles word load cost, s=2
				new OCTimingCmp(8, 4, 0, 10, 2) // SDRAM, cmp, 18 cycles quadword load cost, s=18
		};

		OCacheMode[] modes = { OCacheMode.BLOCK_FILL, OCacheMode.SINGLE_FIELD };
		List<OCacheAnalysisResult> samples = new ArrayList<OCacheAnalysisResult>();
		int[] cacheWays = { 0, 2, 4, 8, 16, 32, 64, 512 }; // need to be in ascending order
		int[] lineSizesObjCache   = { 4, 8, 16, 32};
		int[] lineSizesFieldCache = { 1 };
		int[] blockSizesObjCache  = { 1, 2, 4, 8, 16 };
		int[] lineSizes;
		for(int configId=0; configId < configs.length; configId++) {
			ObjectCacheTiming ocConfig = configs[configId];

			oStream.println("---------------------------------------------------------");
			oStream.println("Object Cache Configuration: "+ocConfig);
			oStream.println("---------------------------------------------------------");
			
			for(OCacheMode mode : modes) {
				long maxCost = 0;
				//			long cacheMisses = Long.MAX_VALUE;
				String modeString;
				lineSizes = lineSizesObjCache;
				if(mode == OCacheMode.BLOCK_FILL) modeString = "fill-block";
				else {
					modeString = "field-as-tag";
					lineSizes = lineSizesFieldCache;
				}
				boolean first = true;
				for(int lineSize : lineSizes) {
					for(int blockSize : blockSizesObjCache) {
						if(blockSize > lineSize) continue;
						if(mode == OCacheMode.BLOCK_FILL) {
							
						} else {
							if(blockSize > 1) continue;
						}
						/* Configure object cache timing */
						ocConfig.setObjectCacheTiming(jopconfig, blockSize);

						/* We have to take field access count of cache size = 0; our analysis otherwise does not assign
						 * sensible field access counts (thats the fault of the IPET method)
						 */
						long totalFieldAccesses = -1, cachedFieldAccesses = -1;
						double bestCyclesPerAccessForConfig = Double.POSITIVE_INFINITY;
						double bestHitRate = 0.0;
						long bestCostPerConfig = Long.MAX_VALUE;

						// assume cacheSizes are in ascending order
						for(int ways : cacheWays) {
							jopconfig.setObjectCacheAssociativity(ways);
							jopconfig.setObjectCacheBlockSize(blockSize);				
							jopconfig.setObjectCacheFieldTag(mode == OCacheMode.SINGLE_FIELD);
							jopconfig.setObjectCacheLineSize(lineSize);
							oca = new ObjectCacheAnalysisDemo(project, jopconfig);

							double cyclesPerAccess, hitRate;
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
							if(cyclesPerAccess < bestCyclesPerAccessForConfig || ways <= 1) {
								bestCyclesPerAccessForConfig = cyclesPerAccess;
							}
							/* hit rate is defined as: 1 - ((cache misses+accesses to bypassed fields) / total field accesses (with n=0) */
							long missAccesses = ocCost.getCacheMissCount() + ocCost.getBypassCount();
							hitRate = (1 - ((double)missAccesses / (double)totalFieldAccesses));						
							if(hitRate > bestHitRate || ways <= 1) {
								bestHitRate = hitRate;
							}

							if(first) {
								oStream.println(String.format("***** ***** MODE = %s ***** *****\n",modeString));
								oStream.println(String.format(" - max tags accessed (upper bound) = %d, max fields accesses = %d",
										oca.getMaxAccessedTags(project.getTargetMethod(), CallString.EMPTY), totalFieldAccesses)
								);						
								first = false;
							}					

							String report = String.format(" + Cycles Per Access [N=%3d,l=%2d,b=%2d]: %.2f (%d total cost, %.2f %% cost of no cache, %d bypass cost)", //, %.2f %% 'hitrate')", 
									ways, lineSize, blockSize, bestCyclesPerAccessForConfig, cost, bestRatio*100, ocCost.getBypassCost());
							if(bestCostPerConfig > cost) {
								report += String.format(" # (analysis cost increased by %.2f %% for this associativity)",ratio*100);
							}
							oStream.println(report);
							if(mode != OCacheMode.SINGLE_FIELD) {
								OCacheAnalysisResult sample =
									new ObjectCacheEvaluation.OCacheAnalysisResult(ways, lineSize, blockSize, configId, 
																				   bestHitRate, bestCyclesPerAccessForConfig, ocCost);
								samples.add(sample);
							}
						}
					}
				}
			}
		}
		OCacheAnalysisResult.dumpBarPlot(samples, oStream);		
		OCacheAnalysisResult.dumpPlot(samples, oStream);
		OCacheAnalysisResult.dumpLatex(samples, oStream);
	} 
	
	private void testExactAllFit() throws InvalidFlowFactException {
		long start,stop;
        start = System.nanoTime();
		LpSolveWrapper.resetSolverTime();
		MethodCacheAnalysis mcAnalysis = new MethodCacheAnalysis(project);
		mcAnalysis.countDistinctCacheBlocks();
        stop  = System.nanoTime();
		System.err.println(
				String.format("[Method Cache Analysis]: Total time: %.2f s / Total solver time: %.2f s",
						timeDiff(start,stop),
						LpSolveWrapper.getSolverTime()));        
		Map<ExecutionContext, Long> blockUsage = mcAnalysis.getBlockUsage();
		MiscUtils.printMap(System.out, blockUsage, new MiscUtils.F2<ExecutionContext, Long, String>() {
            public String apply(ExecutionContext v1, Long maxBlocks) {
                MethodCache mc = project.getWCETProcessorModel().getMethodCache();
                return String.format("%-50s ==> %2d <= %2d",
                        v1.getMethodInfo().getFQMethodName(),
                        maxBlocks,
                        mc.getAllFitCacheBlocks(v1.getMethodInfo(), null));
            }
        });
	}

}
