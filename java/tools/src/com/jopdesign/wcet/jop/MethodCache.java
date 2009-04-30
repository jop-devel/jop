package com.jopdesign.wcet.jop;

import org.apache.bcel.classfile.Code;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.config.Config;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.graphutils.MiscUtils;
import com.jopdesign.wcet.jop.JOPConfig.CacheImplementation;

public abstract class MethodCache {
	protected Project project;
	protected int cacheSizeWords;
	public MethodCache(Project p, int cacheSizeWords) {
		this.project = p;
		this.cacheSizeWords = cacheSizeWords;
	}
	public static MethodCache getCacheModel(Project p) {
		Config c = p.getConfig();
		switch(c.getOption(JOPConfig.CACHE_IMPL)) {
		case NO_METHOD_CACHE: return new NoMethodCache(p);
		case LRU_CACHE: return BlockCache.fromConfig(p,true);
		case FIFO_CACHE: return BlockCache.fromConfig(p,false);
		case LRU_VARBLOCK_CACHE: return VarBlockCache.fromConfig(p, true);
		case FIFO_VARBLOCK_CACHE: return VarBlockCache.fromConfig(p,false);
		default: throw new AssertionError("Non-exhaustive match on enum: CACHE_IMPL: "+
				                          c.getOption(JOPConfig.CACHE_IMPL));
		}
	}
	public abstract boolean allFit(MethodInfo m);
	public abstract boolean isLRU();
	public abstract boolean fitsInCache(int sizeInWords);
	public abstract int requiredNumberOfBlocks(int sizeInWords);
	/**
	 * Compute the maximal total cache-miss penalty for <strong>invoking and executing</strong>
	 * m.
	 * <p>
	 * Precondition: The set of all methods reachable from <code>m</code> fit into the cache
	 * </p><p>
     * Algorithm: If all methods reachable from <code>m</code> (including <code>m</code>) fit 
     * into the cache, we can compute the WCET of <m> using the {@link ALWAYS_HIT@} cache
     * approximation, and then add the sum of cache miss penalties for every reachable method.
	 * </p><p>
	 * Note that when using this approximation, we attribute the
	 * total cache miss cost to the invocation of that method.
	 * </p><p>
	 * Explanation: We know that there is only one cache miss per method, but for FIFO caches we
	 * do not know when the cache miss will occur (on return or invoke), except for leaf methods.
	 * Let <code>h</code> be the number of cycles hidden by <strong>any</strong> return or 
	 * invoke instructions. Then the cache miss penalty is bounded by <code>(b-h)</code> per 
	 * method.
	 * </p>
     * 
	 * @param m The method invoked
	 * @return the cache miss penalty
	 * 
	 */
	public long getMissOnceCummulativeCacheCost(MethodInfo m, boolean assumeOnInvoke) {
		long miss = 0;
		for(MethodInfo reachable : project.getCallGraph().getReachableImplementations(m)) {
			miss += missOnceCost(reachable, assumeOnInvoke);
		}
		return miss;
	}

	public long missOnceCost(MethodInfo mi, boolean assumeOnInvoke) {
		int words = project.getFlowGraph(mi).getNumberOfWords();
		boolean loadOnInvoke =    project.getCallGraph().isLeafNode(mi) 
		                       || this.isLRU() 
		                       || assumeOnInvoke;
		int thisMiss = project.getProcessorModel().getMethodCacheLoadTime(words,loadOnInvoke);
		Project.logger.info("Cache miss penalty to cumulative cache cost: "+mi+": "+thisMiss);
		return thisMiss;
	}

	/**
	 * Compute the number of cache blocks which might be needed when calling this method
	 * @param invoked
	 * @return the maximum number of cache blocks needed, s.t. we won't run out of cache
	 * blocks when invoking the given method
	 * @throws TypeException 
	 */
	public long getAllFitCacheBlocks(MethodInfo invoked) {
		int size = 0;
		for(MethodInfo mi : project.getCallGraph().getReachableImplementations(invoked)) {
			size+= requiredNumberOfBlocks(project.getSizeInWords(mi));			
		}
		return size;
	}
	/** Check that cache is big enough to hold any method possibly invoked
	 *  Return largest method */
	public MethodInfo checkCache() throws Exception {
		int maxWords = 0;
		MethodInfo largestMethod = null;
		// It is inconvenient for testing to take all methods into account
		for(MethodInfo mi : project.getCallGraph().getImplementedMethods(project.getTargetMethod())) {
			Code code = mi.getCode();
			if(code == null) continue;
			int size = code.getCode().length;
			int words = MiscUtils.bytesToWords(size);
			if(! this.fitsInCache(words)) {
				throw new Exception("Cache to small for target method: "+mi.getFQMethodName() + " / "+ words + " words");
			}
			if(words >= maxWords) {
				largestMethod = mi;
				maxWords = words;
			}
		}
		
// It is inconvenient for testing to take all methods into account
//		for(ClassInfo ci : project.getWcetAppInfo().getCliMap().values()) {
//			for(MethodInfo mi : ci.getMethodInfoMap().values()) {
//				Code code = mi.getCode();
//				if(code == null) continue;
//				int size = code.getLength();
//				int words = MiscUtils.bytesToWords(size);
//				if(! this.fitsInCache(words)) {
//					System.err.println("Warning: does not fit into cache: "+mi.getFQMethodName()+" / "+words+" words");
//				}
//				if(words >= maxWords) {
//					largestMethod = mi;
//					maxWords = words;
//				}
//			}
//		}
		return largestMethod;
	}
	public long getMissOnInvokeCost(ProcessorModel proc, ControlFlowGraph invoked) {
		//System.err.println("Miss on invoke cost: "+invoked+": "+proc.getMethodCacheLoadTime(invoked.getNumberOfWords(), true)+
		//		", for: "+invoked.getNumberOfWords()+"words.");
		return proc.getMethodCacheLoadTime(invoked.getNumberOfWords(), true);
	}

	public long getMissOnReturnCost(ProcessorModel proc, ControlFlowGraph invoker) {
		//System.err.println("Miss on return cost: "+invoker+": "+proc.getMethodCacheLoadTime(invoker.getNumberOfWords(), false));
		return proc.getMethodCacheLoadTime(invoker.getNumberOfWords(), false);
	}
	public long getMaxMissCost(ProcessorModel proc, ControlFlowGraph cfg) {
		long invokeCost=getMissOnInvokeCost(proc,cfg);
		if(! cfg.isLeafMethod()) return Math.max(invokeCost, getMissOnReturnCost(proc,cfg));
		else                     return invokeCost;
	}
	
	/**
	 * Get an upper bound for the miss cost involved in invoking a method of length
	 * <pre>invokedBytes</pre> and returning to a method of length <pre>invokerBytes</pre> 
	 * @param invoker
	 * @param invoked
	 * @return the maximal cache miss penalty for the invoke/return
	 */
	public long getInvokeReturnMissCost(ProcessorModel proc, ControlFlowGraph invoker, ControlFlowGraph invoked) {
		return getMissOnInvokeCost(proc,invoked)+getMissOnReturnCost(proc,invoker);
	}
	public abstract CacheImplementation getName();
}
