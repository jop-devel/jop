package com.jopdesign.wcet.jop;

import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.misc.AppInfoException;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.processormodel.JOPConfig;
import com.jopdesign.common.processormodel.JOPConfig.CacheImplementation;
import com.jopdesign.wcet.WCETProcessorModel;
import com.jopdesign.wcet.WCETTool;
import org.apache.log4j.Logger;

import java.util.Set;

public abstract class MethodCache {

	protected WCETTool project;
	protected int cacheSizeWords;

	public MethodCache(WCETTool p, int cacheSizeWords) {
		this.project = p;
		this.cacheSizeWords = cacheSizeWords;
	}

	public static MethodCache getCacheModel(WCETTool p) {
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

	public abstract boolean allFit(MethodInfo m, CallString cs);

	public abstract boolean isLRU();

	public abstract boolean fitsInCache(int sizeInWords);

	public abstract int requiredNumberOfBlocks(int sizeInWords);
	
	public int requiredNumberOfBlocks(MethodInfo mi) {
		return requiredNumberOfBlocks(project.getSizeInWords(mi));
	}

	/**
	 * Compute the maximal total cache-miss penalty for <strong>invoking and executing</strong>
	 * m.
	 * <p>
	 * Precondition: The set of all methods reachable from <code>m</code> fit into the cache
	 * </p><p>
     * Algorithm: If all methods reachable from <code>m</code> (including <code>m</code>) fit 
     * into the cache, we can compute the WCET of <m> using the {@code ALWAYS_HIT} cache
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
		for(MethodInfo reachable : project.getCallGraph().getImplementationsReachableFromMethod(m)) {
			miss += missOnceCost(reachable, assumeOnInvoke);
		}
		Logger.getLogger(this.getClass()).debug("getMissOnceCummulativeCacheCost for "+m+"/"+(assumeOnInvoke?"invoke":"return")+":"+miss);
		return miss;
	}

	public long missOnceCost(MethodInfo mi, boolean assumeOnInvoke) {
		int words = project.getFlowGraph(mi).getNumberOfWords();
		boolean loadOnInvoke =    project.getCallGraph().isLeafMethod(mi) 
		                       || this.isLRU() 
		                       || assumeOnInvoke;
		long thisMiss = project.getWCETProcessorModel().getMethodCacheMissPenalty(words,loadOnInvoke);
		WCETTool.logger.info("Cache miss penalty to cumulative cache cost: "+mi+": "+thisMiss);
		return thisMiss;
	}

	/** Get miss penalty for invoking the given method */
	public long getMissOnInvokeCost(WCETProcessorModel proc, ControlFlowGraph cfg) {
		return proc.getMethodCacheMissPenalty(cfg.getNumberOfWords(), true);
	}
	
	/** Get miss penalty for returning to the given method */
	public long getMissOnReturnCost(WCETProcessorModel proc, ControlFlowGraph cfg) {
		return proc.getMethodCacheMissPenalty(cfg.getNumberOfWords(), false);
	}

	/**
	 * Compute the number of cache blocks which might be needed when calling this method
	 * @param invoked
	 * @param cs callstring, or {@code null} if the callstring should be ignored
	 * @return the maximum number of cache blocks needed, s.t. we won't run out of cache
	 * blocks when invoking the given method
	 */
	public long getAllFitCacheBlocks(MethodInfo invoked, CallString cs) {
		int size = 0;
		Set<MethodInfo> methods;
		if(cs == null) {
			methods = project.getCallGraph().getImplementationsReachableFromMethod(invoked);
		} else {
			methods = project.getCallGraph().getReachableImplementations(invoked, cs);
		}
		for(MethodInfo mi : methods) {
			size+= requiredNumberOfBlocks(mi);			
		}
		return size;
	}
	
	/** Check that cache is big enough to hold any method possibly invoked
	 *  Return largest method */
	public MethodInfo checkCache() throws AppInfoException {
		int maxWords = 0;
		MethodInfo largestMethod = null;
		// It is inconvenient for testing to take all methods into account
		for(MethodInfo mi : project.getCallGraph().getImplementedMethods(project.getTargetMethod())) {
			MethodCode code = mi.getCode();
			if(code == null) continue;
			int size = code.getLength();
			int words = MiscUtils.bytesToWords(size);
			if(! this.fitsInCache(words)) {
				throw new AppInfoException("Cache to small for target method: "+mi.getFQMethodName() + " / "+ words + " words");
			}
			if(words >= maxWords) {
				largestMethod = mi;
				maxWords = words;
			}
		}
		
// It is inconvenient for testing to take all methods into account
//		for(ClassInfo ci : project.getAppInfo().getCliMap().values()) {
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

	public long getMaxMissCost(WCETProcessorModel proc, ControlFlowGraph cfg) {
		long invokeCost = proc.getMethodCacheMissPenalty(cfg.getNumberOfWords(), true);
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
	public long getInvokeReturnMissCost(WCETProcessorModel proc, ControlFlowGraph invoker, ControlFlowGraph invoked) {
		return proc.getMethodCacheMissPenalty(invoked.getNumberOfWords(), true) +
		       proc.getMethodCacheMissPenalty(invoker.getNumberOfWords(), false);
	}
	public abstract CacheImplementation getName();
}
