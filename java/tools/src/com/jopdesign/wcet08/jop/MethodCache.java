package com.jopdesign.wcet08.jop;

import java.util.Iterator;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.ProcessorModel;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.config.Config;
import com.jopdesign.wcet08.frontend.ControlFlowGraph;
import com.jopdesign.wcet08.frontend.CallGraph.CallGraphNode;
import com.jopdesign.wcet08.jop.CacheConfig.CacheImplementation;

public abstract class MethodCache {
	protected Project project;
	protected int cacheSizeWords;
	public MethodCache(Project p, int cacheSizeWords) {
		this.project = p;
		this.cacheSizeWords = cacheSizeWords;
	}
	public static MethodCache getCacheModel(Project p) {
		Config c = p.getConfig();
		switch(c.getOption(CacheConfig.CACHE_IMPL)) {
		case NO_METHOD_CACHE: return new NoMethodCache(p);
		case LRU_CACHE: return BlockCache.fromConfig(p,true);
		case FIFO_CACHE: return BlockCache.fromConfig(p,false);
		case FIFO_VARBLOCK_CACHE: return VarBlockCache.fromConfig(p,false);
		default: throw new AssertionError("Non-exhaustive match on enum: CACHE_IMPL: "+
				                          c.getOption(CacheConfig.CACHE_IMPL));
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
	public long getMissOnceCummulativeCacheCost(MethodInfo m) {
		long miss = 0;
		Iterator<CallGraphNode> iter = project.getCallGraph().getReachableMethods(m);
		while(iter.hasNext()) {
			CallGraphNode n = iter.next();
			miss+=missOnceCost(n.getMethodImpl());
		}
		return miss;
	}
	public long missOnceCost(MethodInfo mi) {
		int words = project.getFlowGraph(mi).getNumberOfWords();
		boolean loadOnInvoke =    project.getCallGraph().isLeafNode(mi) 
		                       || this.isLRU() 
		                       || project.getConfig().getOption(CacheConfig.ASSUME_MISS_ONCE_ON_INVOKE);
		int thisMiss = project.getProcessorModel().getMethodCacheLoadTime(words,loadOnInvoke);
		Project.logger.info("Cache miss penalty to cumulative cache cost: "+mi+": "+thisMiss);
		return thisMiss;
	}

	/**
	 * Compute the number of cache blocks which might be needed when calling this method
	 * @param mi
	 * @return the maximum number of cache blocks needed, s.t. we won't run out of cache
	 * blocks when invoking the given method
	 * @throws TypeException 
	 */
	public long getAllFitCacheBlocks(MethodInfo mi) {
		int size = 0;
		Iterator<CallGraphNode> iter = project.getCallGraph().getReachableMethods(mi);
		while(iter.hasNext()) {
			CallGraphNode n = iter.next();
			size+= requiredNumberOfBlocks(project.getSizeInWords(n.getMethodImpl()));
		}
		return size;
	}
	public void checkCache(MethodInfo m) throws Exception {
		if(! this.fitsInCache(project.getSizeInWords(m))) {
			throw new Exception("Method cache is too small: method "+
					            m.getFQMethodName() + " does not fit into the cache");
		}
	}
	public long getMissOnInvokeCost(ProcessorModel proc, ControlFlowGraph invoked) {
		//System.err.println("Miss on invoke cost: "+invoked+": "+proc.getMethodCacheLoadTime(invoked.getNumberOfWords(), true));
		//System.err.println("Miss on return cost: "+invoked+": "+proc.getMethodCacheLoadTime(invoked.getNumberOfWords(), false));
		return proc.getMethodCacheLoadTime(invoked.getNumberOfWords(), true);
	}

	public long getMissOnReturnCost(ProcessorModel proc, ControlFlowGraph invoker) {
		return proc.getMethodCacheLoadTime(invoker.getNumberOfWords(), false);
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
