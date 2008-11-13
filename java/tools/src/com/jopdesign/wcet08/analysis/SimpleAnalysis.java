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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;

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
import com.jopdesign.wcet08.ipet.LocalAnalysis.CostProvider;

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
	public class WcetCost implements Serializable {
		private static final long serialVersionUID = 1L;
		private long localCost = 0;
		private long cacheCost = 0;
		private long nonLocalCost = 0;
		public WcetCost() { }
		public long getCost() { return nonLocalCost+getLocalAndCacheCost(); }
		
		public long getLocalCost() { return localCost; }
		public long getCacheCost() { return cacheCost; }
		public long getNonLocalCost() { return nonLocalCost; }
		
		public void addLocalCost(long c) { this.localCost += c; }
		public void addNonLocalCost(long c) { this.nonLocalCost += c; }
		public void addCacheCost(long c) { this.cacheCost += c; }
		
		public long getLocalAndCacheCost() { return this.localCost + this.cacheCost; }
		@Override public String toString() {
			if(getCost() == 0) return "0";
			return ""+getCost()+" (local: "+localCost+",cache: "+cacheCost+",non-local: "+nonLocalCost+")";
		}
		public WcetCost getFlowCost(Long flow) {
			WcetCost flowcost = new WcetCost();
			if(this.getCost() == 0 || flow == 0) return flowcost;
			flowcost.localCost = localCost*flow;
			flowcost.cacheCost = cacheCost*flow;
			flowcost.nonLocalCost = nonLocalCost*flow;
			if(flowcost.getCost() / flow != getCost()) {
				throw new ArithmeticException("getFlowCost: Arithmetic Error");
			}
			return flowcost;
		}
	}
	/* provide cost given a node->cost table */
	private class MapCostProvider<T> implements CostProvider<T> {
		private Map<T, WcetCost> costMap;
		public MapCostProvider(Map<T,WcetCost> costMap) {
			this.costMap = costMap;
		}
		public long getCost(T obj) {
			WcetCost cost = costMap.get(obj);
			if(cost == null) throw new NullPointerException("Missing entry for "+obj+" in cost map");
			return cost.getCost();
		}
		
	}
	
	/**
	 * Supported WCET computations: Assume all method cache accesses are miss 
	 * (<code>ALWAYS_MISS</code>), analyse the set of reachable methods
	 * (<code>ANALYSE_REACHABLE</code) or
	 * assume (unsafe !) all are hit (<code>ALWAYS_HIT</code>),
	 *
	 */
	public enum WcetMode { ALWAYS_HIT, ALWAYS_MISS, ANALYSE_REACHABLE };

	private static final Logger logger = Logger.getLogger(SimpleAnalysis.class);
	private Project project;
	private Hashtable<WcetKey, WcetCost> wcetMap;
	private CacheConfig config;
	private LocalAnalysis localAnalysis;

	public SimpleAnalysis(Project project) {
		this.config = new CacheConfig(Config.instance());
		this.project = project;
		this.localAnalysis = new LocalAnalysis(project);
		this.wcetMap = new Hashtable<WcetKey,WcetCost>();
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
	public WcetCost computeWCET(MethodInfo m, WcetMode cacheMode) {
		/* use a cache to speed up analysis */
		WcetKey key = new WcetKey(m,cacheMode);
		if(wcetMap.containsKey(key)) return wcetMap.get(key);

		/* analyse reachable */
		WcetMode recursiveMode = cacheMode;
		boolean allFit = getMaxCacheBlocks(m) <= config.cacheBlocks();
		boolean localHit = cacheMode == WcetMode.ALWAYS_HIT;
		long missCost = 0;		
		if(cacheMode == WcetMode.ANALYSE_REACHABLE) {
			if(allFit) {
				recursiveMode = WcetMode.ALWAYS_HIT;
				localHit = true;
				missCost = cacheMissPenalty(m);
			}			
		}
		/* build wcet map */
		FlowGraph fg = project.getFlowGraph(m);
		Map<FlowGraphNode,WcetCost> nodeCosts = buildNodeCostMap(fg,recursiveMode, localHit);
		CostProvider<FlowGraphNode> costProvider = new MapCostProvider<FlowGraphNode>(nodeCosts);
		MaxCostFlow<FlowGraphNode,FlowGraphEdge> problem = 
			localAnalysis.buildWCETProblem(key.toString(),project.getFlowGraph(m), costProvider);
		logger.debug("Max-Cost-Flow: "+m.getMethod()+", cache mode: "+cacheMode+
		            ", all methods fit?: "+allFit+ ", cache miss penalty: "+missCost);
		/* solve ILP */
		long maxCost = 0;
		Map<FlowGraphEdge, Long> flowMapOut = new HashMap<FlowGraphEdge, Long>();
		try {
			maxCost = Math.round(problem.solve(flowMapOut));
		} catch (Exception e) {
			throw new Error("Failed to solve LP problem",e);
		}
		/* extract node flow, local cost, cache cost, cummulative cost */
		Map<FlowGraphNode,Long> nodeFlow = new Hashtable<FlowGraphNode, Long>();
		DirectedGraph<FlowGraphNode, FlowGraphEdge> graph = fg.getGraph();
		for(FlowGraphNode n : fg.getGraph().vertexSet()) {
			if(graph.inDegreeOf(n) == 0) nodeFlow.put(n, 0L); // ENTRY and DEAD CODE (no flow)
			else {
				long flow = 0;
				for(FlowGraphEdge inEdge : graph.incomingEdgesOf(n)) {
					flow+=flowMapOut.get(inEdge);
				}
				nodeFlow.put(n, flow);
			}
		}
		/* Compute cost, sepearting local and non-local cost */
		/* Safety check: compare flow*cost to actual solution */
		WcetCost methodCost = new WcetCost();
		for(FlowGraphNode n : fg.getGraph().vertexSet()) {
			long flow = nodeFlow.get(n);
			methodCost.addLocalCost(flow * nodeCosts.get(n).getLocalAndCacheCost());
			methodCost.addNonLocalCost(flow * nodeCosts.get(n).getNonLocalCost());
		}
		if(methodCost.getCost() != maxCost) {
			throw new AssertionError("The solution implies that the flow graph cost is " 
									 + methodCost.getCost() + ", but the ILP solver reported "+maxCost);
		}
		methodCost.addCacheCost(missCost);
		wcetMap.put(key, methodCost); 
		/* Logging and Report */
		if(Config.instance().doGenerateWCETReport()) {
			Hashtable<FlowGraphNode, WcetCost> nodeFlowCosts = 
				new Hashtable<FlowGraphNode, WcetCost>();
			for(FlowGraphNode n : fg.getGraph().vertexSet()) {
				WcetCost nfc = nodeCosts.get(n).getFlowCost(nodeFlow.get(n));
				nodeFlowCosts .put(n,nfc);
			}
			logger.info("WCET for " + key + ": "+methodCost);
			Map<String,Object> stats = new Hashtable<String, Object>();
			stats.put("WCET",methodCost);
			stats.put("mode",cacheMode);
			stats.put("all-methods-fit-in-cache",allFit);
			stats.put("missCost",missCost);
			project.getReport().addDetailedReport(m,"WCET_"+cacheMode.toString(),stats,nodeFlowCosts,flowMapOut);
		}
		return methodCost;
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
	private Map<FlowGraphNode, WcetCost> 
		buildNodeCostMap(FlowGraph fg,WcetMode recursiveMode, boolean localHit) {
		
		HashMap<FlowGraphNode, WcetCost> nodeCost = new HashMap<FlowGraphNode,WcetCost>();
		for(FlowGraphNode n : fg.getGraph().vertexSet()) {
			if(n.getCodeBlock() != null) {
				nodeCost.put(n, computeCostOfNode(n, recursiveMode, localHit));
			} else {
				nodeCost.put(n, new WcetCost());
			}
		}
		return nodeCost;
	}
	
	private class WcetVisitor implements FlowGraph.FlowGraphVisitor {
		WcetCost cost;
		private WcetMode recursiveMode;
		private boolean localHit;
		public WcetVisitor(WcetMode recursiveMode, boolean localHit) {
			this.recursiveMode = recursiveMode;
			this.localHit = localHit;
			this.cost = new WcetCost();
		}
		public void visitSpecialNode(DedicatedNode n) {
		}
		public void visitBasicBlockNode(BasicBlockNode n) {
			BasicBlock bb = n.getCodeBlock();
			for(InstructionHandle ih : bb.getInstructions()) {
				int jopcode = project.getWcetAppInfo().getJOpCode(n.getCodeBlock().getClassInfo(), ih.getInstruction());
				cost.addLocalCost(WCETInstruction.getCycles(jopcode,false,0));										
			}
		}
		public void visitInvokeNode(InvokeNode n) {
			if(n.getImpl() == null) {
				throw new AssertionError("Invoke node "+n.getReferenced()+" without implementation in WCET analysis - did you preprocess virtual methods ?");
			}
			MethodInfo meth = n.getImpl();
			logger.info("Recursive WCET computation: "+meth.getMethod());
			cost.addNonLocalCost(computeWCET(meth, recursiveMode).getCost());
			if(! localHit) {
				cost.addCacheCost(getInvokeReturnMissCost(
						n.getCodeBlock().getNumberOfBytes(),
						project.getFlowGraph(meth).getNumberOfBytes()));
				
			}
		}
	}
	private WcetCost 
		computeCostOfNode(FlowGraphNode n,WcetMode recursiveMode, boolean localHit) {
		
		WcetVisitor wcetVisitor = new WcetVisitor(recursiveMode, localHit);
		n.accept(wcetVisitor);
		return wcetVisitor.cost;
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
