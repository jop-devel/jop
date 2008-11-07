/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

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
package com.jopdesign.wcet08.analysis;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.log4j.Logger;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.WCETInstruction;
import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.frontend.BasicBlock;
import com.jopdesign.wcet08.frontend.FlowGraph;
import com.jopdesign.wcet08.frontend.CallGraph.CallGraphNode;
import com.jopdesign.wcet08.frontend.FlowGraph.BasicBlockNode;
import com.jopdesign.wcet08.frontend.FlowGraph.DedicatedNode;
import com.jopdesign.wcet08.frontend.FlowGraph.FlowGraphEdge;
import com.jopdesign.wcet08.frontend.FlowGraph.FlowGraphNode;
import com.jopdesign.wcet08.frontend.FlowGraph.InvokeNode;
import com.jopdesign.wcet08.ipet.LocalAnalysis;
import com.jopdesign.wcet08.ipet.MaxCostFlow;

/**
 * Simple and fast local analysis with cache approximation.
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class SimpleAnalysis {
	class WcetKey {
		MethodInfo m;
		WcetMode alwaysHit;
		public WcetKey(MethodInfo m, WcetMode mode) {
			this.m = m; this.alwaysHit = mode;
		}
		@Override
		public boolean equals(Object that) {
			return (that instanceof WcetKey) ? equalsKey((WcetKey) that) : false;
		}
		private boolean equalsKey(WcetKey key) {
			return this.m.equals(key.m) && (this.alwaysHit == key.alwaysHit);
		}
		@Override
		public int hashCode() {
			return m.getFQMethodName().hashCode()+this.alwaysHit.hashCode();
		}
		@Override
		public String toString() {
			return this.m.getFQMethodName()+"["+this.alwaysHit+"]";
		}
	}
	public enum WcetMode { ALWAYS_HIT, ALWAYS_MISS, STATIC_CACHE_ANALYSIS };
	private static final Logger logger = Logger.getLogger(SimpleAnalysis.class);
	private Project project;
	private Hashtable<WcetKey, Long> wcetMap;
	private CacheConfig config;
	private LocalAnalysis localAnalysis;

	public SimpleAnalysis(Project project) {
		this.config = new CacheConfig(Config.instance());
		this.project = project;
		this.localAnalysis = new LocalAnalysis(project);
		this.wcetMap = new Hashtable<WcetKey,Long>();
	}
	/**
	 * This is a simple analysis, which nevertheless incorporates the cache.
	 * We employ the following strategy:
	 *  - If the set of all methods reachable from m (including m) fits into the cache,
	 *    we calculate the 'alwaysHitCache' WCET, and the add the cost for missing each
	 *    method exactly once. 
	 *    NOTE: We do not need to add m, as it will be included in the cost of the caller.
	 *  - If the set of all methods reachable from m (including m) does NOT fit into the cache,
	 *    we compute an actual WCET for the reachable methods, and add the cost for missing
	 *    at the invoke nodes.
	 * @param m
	 * @param alwaysHit
	 * @return
	 */
	public long computeWCET(MethodInfo m, WcetMode cacheMode) {
		WcetKey key = new WcetKey(m,cacheMode);
		if(wcetMap.containsKey(key)) return wcetMap.get(key);
		WcetMode recursiveMode = cacheMode;
		boolean allFit = getMaxCacheBlocks(m) <= config.cacheBlocks();
		boolean localHit = cacheMode == WcetMode.ALWAYS_HIT;
		long missCost = 0;		
		if(cacheMode == WcetMode.STATIC_CACHE_ANALYSIS) {
			if(allFit) {
				recursiveMode = WcetMode.ALWAYS_HIT;
				localHit = true;
				missCost = cacheMissPenalty(m);
			}			
		}
		FlowGraph fg = project.getFlowGraph(m);
		Map<FlowGraphNode,Long> wcets = buildWCETMap(fg,recursiveMode, localHit);
		MaxCostFlow<FlowGraphNode,FlowGraphEdge> problem = localAnalysis.buildWCETProblem(project.getFlowGraph(m), wcets);
		logger.debug("Max-Cost-Flow: "+m.getMethod()+", cache mode: "+cacheMode+
		            ", all methods fit?: "+allFit+ ", cache miss penalty: "+missCost);
		long cost;
		Map<FlowGraphEdge, Long> flowMapOut = new HashMap<FlowGraphEdge, Long>();
		try {
			cost = Math.round(problem.solve(flowMapOut));
		} catch (Exception e) {
			logger.error("Failed to solve LP problem: "+e);
			cost=Long.MAX_VALUE;
		}
		cost+=missCost;
		wcetMap.put(key, cost); 
		// Logging and Report
		{
			logger.info("WCET for " + key + ": "+cost+" (global cache-miss-penalty: "+missCost+")");
			Map<String,Object> stats = new Hashtable<String, Object>();
			stats.put("WCET",cost);
			stats.put("mode",cacheMode);
			stats.put("all-methods-fit-in-cache",allFit);
			stats.put("missCost",missCost);
			project.getReport().addDetailedReport(m,"WCET_"+cacheMode.toString(),stats,wcets,flowMapOut);
		}
		return cost;
	}
	/**
	 * map flowgraph nodes to WCET
	 * if the node is a invoke, we need to compute the WCET for the invoked method
	 * otherwise, just take the basic block WCET
	 * @param fg 
	 * @param recursiveMode WCET mode for recursive calls 
	 * @param localHit whether we compute always-hit-cache WCET (locally)
	 * @return
	 */
	private Map<FlowGraphNode, Long> buildWCETMap(FlowGraph fg,WcetMode recursiveMode, boolean localHit) {
		HashMap<FlowGraphNode, Long> nodeCost = new HashMap<FlowGraphNode,Long>();
		for(FlowGraphNode n : fg.getGraph().vertexSet()) {
			if(n.getCodeBlock() != null) {
				nodeCost.put(n, computeCostOfNode(n, recursiveMode, localHit));
			} else {
				nodeCost.put(n, 0L);
			}
		}
		return nodeCost;
	}
	
	private class WcetVisitor implements FlowGraph.FlowGraphVisitor {
		private long localWcet;
		private long cumWcet;
		private WcetMode recursiveMode;
		private boolean localHit;
		public WcetVisitor(WcetMode recursiveMode, boolean localHit) {
			this.recursiveMode = recursiveMode;
			this.localHit = localHit;
			this.localWcet = 0;
			this.cumWcet = 0;
		}
		public void visitSpecialNode(DedicatedNode n) {
		}
		public void visitBasicBlockNode(BasicBlockNode n) {
			BasicBlock bb = n.getCodeBlock();
			for(InstructionHandle ih : bb.getInstructions()) {
				int jopcode = project.getAppInfo().getJOpCode(n.getCodeBlock().getClassInfo(), ih.getInstruction());
				localWcet += WCETInstruction.getCycles(jopcode,false,0);										
			}
			cumWcet += localWcet;
		}
		public void visitInvokeNode(InvokeNode n) {
			if(n.getImpl() == null) {
				throw new AssertionError("Invoke node "+n.getReferenced()+" without implementation in WCET analysis - did you preprocess virtual methods ?");
			}
			MethodInfo meth = n.getImpl();
			logger.info("Recursive WCET computation: "+meth.getMethod());
			long subCost = computeWCET(meth, recursiveMode);
			if(! localHit) {
				subCost+=getInvokeReturnMissCost(
						n.getCodeBlock().getNumberOfBytes(),
						project.getFlowGraph(meth).getNumberOfBytes());
			}
			cumWcet+=subCost;
		}
	}
	private long computeCostOfNode(FlowGraphNode n,WcetMode recursiveMode, boolean localHit) {
		WcetVisitor wcetVisitor = new WcetVisitor(recursiveMode, localHit);
		n.accept(wcetVisitor);
		return wcetVisitor.cumWcet;
	}
	/**
	 * Get an upper bound for the miss cost involved in invoking a method of length
	 * <pre>invokedBytes</pre> and returning to a method of length <pre>invokerBytes</pre> 
	 * @param invokerBytes
	 * @param invokedByes
	 * @return
	 */
	private long getInvokeReturnMissCost(int invokerBytes, int invokedBytes) {
		int invokerWords = (invokerBytes + 3) / 4;
		int invokedWords = (invokedBytes + 3) / 4;
		int invokerCost = Math.max(0,
									WCETInstruction.calculateB(false, invokedWords) - 
									config.MIN_HIDE_LOAD_CYCLES);
		int invokedCost = Math.max(0,
									WCETInstruction.calculateB(false, invokerWords) - 
									config.MIN_HIDE_LOAD_CYCLES);
		return invokerCost+invokedCost;
	}
	/**
	 * Compute the maximal cache-hit penalty when executing m.
	 * Precondition: The set of all methods reachable from m fit into the cache
	 * Observation: Assume all method invocation are static for now. Then, we may assume the
	 * first and only cache miss for a method n  always occurs on `invokestatic`.
	 * The cache miss penalty in this case (b-37), where b = 6 + (n+1) * (2+c), with n
	 * being the method length in bytes and c the cache-read-wait time.
     * Conclusion: If all methods reachable from m fit into the cache, we can compute the
     * WCET assuming alwaysHit, and then add the sum of cache miss penalties (b[m]-37) for
     * every reachable method m.
     * 
	 * @param m The root method
	 * @return the cache miss penalty
	 * 
	 */
	private long cacheMissPenalty(MethodInfo m) {
		long miss = 0;
		Iterator<CallGraphNode> iter = project.getCallGraph().getReachableMethods(m);
		while(iter.hasNext()) {
			CallGraphNode n = iter.next();
			if(n.getMethodImpl() == null) continue;
			if(n.getMethodImpl().equals(m)) continue;
			int words = (project.getFlowGraph(n.getMethodImpl()).getNumberOfBytes() + 3) / 4;
			miss  += Math.max(0,
							  WCETInstruction.calculateB(false, words) - config.INVOKE_STATIC_HIDE_LOAD_CYCLES);
		}
		return miss;
	}
	/**
	 * Compute the number of cache blocks which might be needed when calling this method
	 * @param m
	 * @return
	 * @throws TypeException 
	 */
	public long getMaxCacheBlocks(MethodInfo m) {
		long size = requiredNumberOfBlocks(m);
		Iterator<CallGraphNode> iter = project.getCallGraph().getReachableMethods(m);
		while(iter.hasNext()) {
			CallGraphNode n = iter.next();
			if(n.isAbstractNode()) continue;			
			size+= requiredNumberOfBlocks(n.getMethodImpl());
		}
		return size;
	}
	private long requiredNumberOfBlocks(MethodInfo m) {
		int M = config.blockSize();
		return ((project.getFlowGraph(m).getNumberOfBytes()+M-1) / M);
	}
}
