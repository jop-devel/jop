package com.jopdesign.wcet;

import static com.jopdesign.wcet.ExecHelper.timeDiff;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jopdesign.dfa.analyses.SymbolicAddress;
import com.jopdesign.dfa.framework.CallString;
import com.jopdesign.wcet.analysis.cache.MethodCacheAnalysis;
import com.jopdesign.wcet.analysis.cache.ObjectCacheAnalysisDemo;
import com.jopdesign.wcet.analysis.cache.ObjectCacheEvaluation;
import com.jopdesign.wcet.analysis.cache.ObjectCacheAnalysisDemo.ObjectCacheCost;
import com.jopdesign.wcet.analysis.cache.ObjectCacheEvaluation.OCacheAnalysisResult;
import com.jopdesign.wcet.analysis.cache.ObjectCacheEvaluation.OCacheMode;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.CallGraph.CallGraphNode;
import com.jopdesign.wcet.graphutils.MiscUtils;
import com.jopdesign.wcet.graphutils.MiscUtils.Function2;
import com.jopdesign.wcet.ipet.LpSolveWrapper;
import com.jopdesign.wcet.jop.JOPConfig;
import com.jopdesign.wcet.jop.MethodCache;

public class ObjectCacheAnalysis {
	/* generator for object cache timings */
	private interface ObjectCacheTiming {
		public int  loadTime(int words);
		public void setObjectCacheTiming(JOPConfig jopConfig, int lineSize);
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
		
		public void setObjectCacheTiming(JOPConfig jopConfig, int lineSize) {
			jopConfig.objectCacheHitCycles = accessCycles;
			jopConfig.objectCacheLoadFieldCycles = accessCycles + loadTime(1);
			jopConfig.objectCacheLoadLineCycles = accessCycles + loadTime(lineSize);
		}
		
		public String toString() {
			return String.format("S(D)RAM [access=%d, delay=, cycles-per-word=%d]",
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

		public void setObjectCacheTiming(JOPConfig jopConfig, int lineSize) {			
			jopConfig.objectCacheHitCycles = accessCycles;
			jopConfig.objectCacheLoadFieldCycles = accessCycles + loadTime(1);
			jopConfig.objectCacheLoadLineCycles = accessCycles + loadTime(lineSize);
		}
		
		public String toString() {
			return String.format("SRAM [cores=%d,slotlength(l=4)=%d,access=%d,delay=%d,cycles-per-word=%d]",
					cores,getSlotLength(),accessCycles,delay,cyclesPerWord);
		}
	}

	
	
	private Project project;

	public ObjectCacheAnalysis(Project project) {
		this.project = project;
	}
	
	public boolean run() {
		evaluateObjectCache();		
		return true;
	}

	private void evaluateObjectCache() {
		long start,stop;
		JOPConfig jopconfig = new JOPConfig(project);

		// Method Cache
		//testExactAllFit();

		// Object Cache (debugging)
//		Map<CallGraphNode, Long> refUsageTotal; 
//		Map<CallGraphNode, Set<SymbolicAddress>> refUsageNames;
//		Map<CallGraphNode, Set<String>> refUsageSaturatedTypes;
//		Map<CallGraphNode, Long> refUsageDistinct;
//		Map<CallGraphNode, Long> fieldUsageDistinct;
//
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
		ObjectCacheTiming configs[] = { 
				new OCTimingUni(0,0,2),  // sram,uni:  2 cycles field cost, 2*w cycles line cost
				new OCTimingUni(0,10,2), // sdram,uni: 10+2*w cycles for each w-word access
				new OCTimingCmp(8, 1, 0, 0, 2), // SRAM, cmp, 2 cycles word load cost, s=2
				new OCTimingCmp(8, 4, 0, 10, 2) // SDRAM, cmp, 18 cycles quadword load cost, s=18
		};

		OCacheMode[] modes = { OCacheMode.WORD_FILL, OCacheMode.LINE_FILL, OCacheMode.SINGLE_FIELD };
		List<OCacheAnalysisResult> samples = new ArrayList<OCacheAnalysisResult>();
		int[] cacheWays = { 0,1,2,4,8,16,32, 64 }; // need to be in ascending order
		int[] lineSizesObjCache  = { 1,2,4,8,16,32};
		int[] lineSizesFieldCache = { 1 };
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
				if(mode == OCacheMode.WORD_FILL) modeString = "fill-word";
				else if(mode == OCacheMode.LINE_FILL) modeString = "fill-line";
				else {
					modeString = "field-as-tag";
					lineSizes = lineSizesFieldCache;
				}
				boolean first = true;
				for(int lineSize : lineSizes) {
					/* Configure object cache timing */
					ocConfig.setObjectCacheTiming(jopconfig, lineSize);

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
							new ObjectCacheEvaluation.OCacheAnalysisResult(mode, ways, lineSize, configId, hitRate, bestCyclesPerAccessForConfig, ocCost);
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

}
