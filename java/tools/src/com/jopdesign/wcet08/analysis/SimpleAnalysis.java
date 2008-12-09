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

import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.WCETInstruction;
import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.analysis.CacheConfig.CacheApproximation;
import com.jopdesign.wcet08.frontend.BasicBlock;
import com.jopdesign.wcet08.frontend.ControlFlowGraph;
import com.jopdesign.wcet08.frontend.WcetAppInfo;
import com.jopdesign.wcet08.frontend.CallGraph.CallGraphNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.DedicatedNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet08.ipet.LocalAnalysis;
import com.jopdesign.wcet08.ipet.MaxCostFlow;
import com.jopdesign.wcet08.ipet.LocalAnalysis.CostProvider;
import com.jopdesign.wcet08.report.ClassReport;

/**
 * Simple and fast local analysis with cache approximation.
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class SimpleAnalysis {
	class WcetKey {
		MethodInfo m;
		CacheApproximation alwaysHit;
		public WcetKey(MethodInfo m, CacheApproximation mode) {
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
	
	private static final Logger logger = Logger.getLogger(SimpleAnalysis.class);
	private Project project;
	private WcetAppInfo appInfo;
	private Hashtable<WcetKey, WcetCost> wcetMap;
	private Config config;
	private LocalAnalysis localAnalysis;

	public SimpleAnalysis(Project project) {
		this.config = Config.instance();
		this.project = project;
		this.appInfo = project.getWcetAppInfo();
		this.localAnalysis = new LocalAnalysis(project);
		this.wcetMap = new Hashtable<WcetKey,WcetCost>();
	}
	
	/**
	 * WCET analysis of the given method, using some cache approximation scheme.
	 * <ul>
	 *  <li/>{@link CacheApproximation.ALWAYS_HIT}: Assume all method cache accesses are hits
	 *  <li/>{@link CacheApproximation.ALWAYS_MISS}: Assume all method cache accesses are misses
	 *  <li/>{@link CacheApproximation.ANALYSE_REACHABLE}: 
	 *  	<p>If for some invocation of <code>m</code>, all methods reachable from and including <code>m</code>
	 *      fit into the cache, add the cost for missing each method exactly once (minimal cycles hidden
	 *      if the method isn't a leaf, minimal number of hidden cycles on invoke otherwise)</p>
	 *      <p>Otherwise, assume invoke/return is miss, and analyse the method using ANALYSE_REACHABLE.</p>
	 * @param m the method to be analyzed
	 * @param cacheMode the cache approximation strategy
	 * @return
	 */
	public WcetCost computeWCET(MethodInfo m, CacheApproximation cacheMode) {
		/* use memoization to speed up analysis */
		WcetKey key = new WcetKey(m,cacheMode);
		if(wcetMap.containsKey(key)) return wcetMap.get(key);

		/* check cache is big enough */
		checkCache(m); /* TODO: should throw exception */

		/* build wcet map */
		ControlFlowGraph fg = appInfo.getFlowGraph(m);
		Map<CFGNode,WcetCost> nodeCosts = buildNodeCostMap(fg,cacheMode);
		CostProvider<CFGNode> costProvider = new MapCostProvider<CFGNode>(nodeCosts);
		MaxCostFlow<CFGNode,CFGEdge> problem = 
			localAnalysis.buildWCETProblem(key.toString(),fg, costProvider);
		/* solve ILP */
		long maxCost = 0;
		Map<CFGEdge, Long> flowMapOut = new HashMap<CFGEdge, Long>();
		try {
			maxCost = Math.round(problem.solve(flowMapOut));
		} catch (Exception e) {
			throw new Error("Failed to solve LP problem: "+e,e);
		}
		/* extract node flow, local cost, cache cost, cummulative cost */
		Map<CFGNode,Long> nodeFlow = new Hashtable<CFGNode, Long>();
		DirectedGraph<CFGNode, CFGEdge> graph = fg.getGraph();
		for(CFGNode n : fg.getGraph().vertexSet()) {
			if(graph.inDegreeOf(n) == 0) nodeFlow.put(n, 0L); // ENTRY and DEAD CODE (no flow)
			else {
				long flow = 0;
				for(CFGEdge inEdge : graph.incomingEdgesOf(n)) {
					flow+=flowMapOut.get(inEdge);
				}
				nodeFlow.put(n, flow);
			}
		}
		/* Compute cost, sepearting local and non-local cost */
		/* Safety check: compare flow*cost to actual solution */
		WcetCost methodCost = new WcetCost();
		for(CFGNode n : fg.getGraph().vertexSet()) {
			long flow = nodeFlow.get(n);
			methodCost.addLocalCost(flow * nodeCosts.get(n).getLocalAndCacheCost());
			methodCost.addNonLocalCost(flow * nodeCosts.get(n).getNonLocalCost());
		}
		if(methodCost.getCost() != maxCost) {
			throw new AssertionError("The solution implies that the flow graph cost is " 
									 + methodCost.getCost() + ", but the ILP solver reported "+maxCost);
		}
		wcetMap.put(key, methodCost); 
		/* Logging and Report */
		if(Config.instance().doGenerateWCETReport()) {
			Hashtable<CFGNode, String> nodeFlowCostDescrs = new Hashtable<CFGNode, String>();
			
			for(CFGNode n : fg.getGraph().vertexSet()) {
				if(nodeFlow.get(n) > 0) {
					nodeFlowCostDescrs .put(n,nodeCosts.get(n).toString());
					BasicBlock basicBlock = n.getBasicBlock();
					/* prototyping */
					if(basicBlock != null) {
						int pos = basicBlock.getFirstInstruction().getPosition();
						ClassInfo cli = basicBlock.getClassInfo();
						LineNumberTable lineNumberTable = basicBlock.getMethodInfo().getMethod().getLineNumberTable();
						int sourceLine = lineNumberTable.getSourceLine(pos);
						ClassReport cr = project.getReport().getClassReport(cli);
						Long oldCost = (Long) cr.getLineProperty(sourceLine, "cost");
						if(oldCost == null) oldCost = 0L;
						cr.addLineProperty(sourceLine, "cost", oldCost + nodeFlow.get(n)*nodeCosts.get(n).getCost());
						for(InstructionHandle ih : basicBlock.getInstructions()) {
							sourceLine = lineNumberTable.getSourceLine(ih.getPosition());
							cr.addLineProperty(sourceLine, "color", "red");
						}
					}
				} else {
					nodeFlowCostDescrs.put(n, ""+nodeCosts.get(n).getCost());
				}
			}
			logger.info("WCET for " + key + ": "+methodCost);
			Map<String,Object> stats = new Hashtable<String, Object>();
			stats.put("WCET",methodCost);
			stats.put("mode",cacheMode);
			stats.put("all-methods-fit-in-cache",allFit(m));
			project.getReport().addDetailedReport(m,"WCET_"+cacheMode.toString(),stats,nodeFlowCostDescrs,flowMapOut);
		}
		return methodCost;
	}


	/**
	 * map flowgraph nodes to WCET
	 * if the node is a invoke, we need to compute the WCET for the invoked method
	 * otherwise, just take the basic block WCET
	 * @param fg 
	 * @param cacheMode cache approximation mode
	 * @return
	 */
	private Map<CFGNode, WcetCost> 
		buildNodeCostMap(ControlFlowGraph fg,CacheApproximation cacheMode) {
		
		HashMap<CFGNode, WcetCost> nodeCost = new HashMap<CFGNode,WcetCost>();
		for(CFGNode n : fg.getGraph().vertexSet()) {
			if(n.getBasicBlock() != null) {
				nodeCost.put(n, computeCostOfNode(n, cacheMode));
			} else {
				nodeCost.put(n, new WcetCost());
			}
		}
		return nodeCost;
	}
	
	private class WcetVisitor implements ControlFlowGraph.CfgVisitor {
		WcetCost cost;
		private CacheApproximation cacheMode;
		public WcetVisitor(CacheApproximation cacheMode) {
			this.cacheMode = cacheMode;
			this.cost = new WcetCost();
		}
		public void visitSpecialNode(DedicatedNode n) {
		}
		public void visitBasicBlockNode(BasicBlockNode n) {
			BasicBlock bb = n.getBasicBlock();
			for(InstructionHandle ih : bb.getInstructions()) {
				addInstructionCost(n,ih);
			}
		}
		public void visitInvokeNode(InvokeNode n) {
			addInstructionCost(n,n.getInstructionHandle());
			if(n.isInterface()) {
				throw new AssertionError("Invoke node "+n.getReferenced()+" without implementation in WCET analysis - did you preprocess virtual methods ?");
			}
			recursiveWCET(n.getBasicBlock().getMethodInfo(), n.getImplementedMethod());
		}
		private void addInstructionCost(BasicBlockNode n, InstructionHandle ih) {
			Instruction ii = ih.getInstruction();
			if(WCETInstruction.isInJava(ii.getOpcode())) {
				/* FIXME: [NO THROW HACK] */
				if(ii instanceof ATHROW || ii instanceof NEW || 
				   ii instanceof NEWARRAY || ii instanceof ANEWARRAY) {
					logger.error(n.getBasicBlock().getMethodInfo()+": "+
							     "Unable to compute WCET of "+ii+". Approximating with 2000 cycles.");
					cost.addLocalCost(2000L);
				} else {
					visitJavaImplementedBC(n.getBasicBlock(),ih);
				}
			} else {
				int jopcode = project.getWcetAppInfo().getJOpCode(n.getBasicBlock().getClassInfo(), ii);
				int cycles = WCETInstruction.getCycles(jopcode,false,0);
				cost.addLocalCost(cycles);
			}			
		}
		/* add cost for invokestatic + return + the java implemented bc */
		private void visitJavaImplementedBC(BasicBlock bb, InstructionHandle ih) {
			logger.info("Java implemented bytecode: "+ ih.getInstruction());
			MethodInfo javaImpl = project.getWcetAppInfo().getJavaImpl(bb.getClassInfo(), ih.getInstruction());
			int cycles = WCETInstruction.getCycles(new INVOKESTATIC(0).getOpcode(),false,0);
			cost.addLocalCost(cycles);
			recursiveWCET(bb.getMethodInfo(), javaImpl);
		}
		private void recursiveWCET(MethodInfo invoker, MethodInfo invoked) {			
			logger.info("Recursive WCET computation: " + invoked.getMethod());
			long cacheCost = 0L;
			CacheApproximation recursiveMode = cacheMode;
			if(cacheMode == CacheApproximation.ALWAYS_MISS ||
			   ! allFit(invoked)) {
				cacheCost = getInvokeReturnMissCost(
						appInfo.getFlowGraph(invoker),
						appInfo.getFlowGraph(invoked));				
			} else if (cacheMode == CacheApproximation.ANALYSE_REACHABLE) {
				cacheCost = totalCacheMissPenalty(invoked) + 
							BlockWCET.getMissOnReturnCost(appInfo.getFlowGraph(invoker));
				recursiveMode = CacheApproximation.ALWAYS_HIT;				
			}
			cost.addNonLocalCost(computeWCET(invoked, recursiveMode).getCost());
			cost.addCacheCost(cacheCost);
		}
	}
	private WcetCost 
		computeCostOfNode(CFGNode n,CacheApproximation cacheMode) {
		
		WcetVisitor wcetVisitor = new WcetVisitor(cacheMode);
		n.accept(wcetVisitor);
		return wcetVisitor.cost;
	}
	
	/**
	 * Get an upper bound for the miss cost involved in invoking a method of length
	 * <pre>invokedBytes</pre> and returning to a method of length <pre>invokerBytes</pre> 
	 * @param invoker
	 * @param invoked
	 * @return the maximal cache miss penalty for the invoke/return
	 */
	private long getInvokeReturnMissCost(ControlFlowGraph invoker, ControlFlowGraph invoked) {
		return BlockWCET.getMissOnInvokeCost(invoked)+BlockWCET.getMissOnReturnCost(invoker);
	}
	
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
	 * Explanation: We know that there is only one cache miss per method, but we do not know
	 * when it will occur (on return or invoke), except for leaf methods. 
	 * Let <code>H</code> be the number of cyclen hidden by <strong>any</strong> return or 
	 * invoke instructions. Then the cache miss penalty is bounded by <code>(b-h)</code> per 
	 * method.
	 * </p><p>
	 * <code>b</code> is giben by <code>b = 6 + (n+1) * (2+c)</code>, with <code>n</code>
	 * being the method length of the receiver (invoke) or caller (return) in words 
	 * and <code>c</code> being the cache-read wait time.
	 * </p>
     * 
	 * @param m The method invoked
	 * @return the cache miss penalty
	 * 
	 */
	private long totalCacheMissPenalty(MethodInfo m) {
		long miss = 0;
		Iterator<CallGraphNode> iter = project.getCallGraph().getReachableMethods(m);
		while(iter.hasNext()) {
			CallGraphNode n = iter.next();
			if(n.getMethodImpl() == null) continue;
			int words = (appInfo.getFlowGraph(n.getMethodImpl()).getNumberOfBytes() + 3) / 4;
			int hidden = project.getCallGraph().isLeafNode(n) ?
					WCETInstruction.INVOKE_HIDDEN_LOAD_CYCLES :
					WCETInstruction.MIN_HIDDEN_LOAD_CYCLES;
			int thisMiss = Math.max(0,WCETInstruction.calculateB(false, words) - hidden); 
			logger.debug("Adding cache miss penalty for "+n.getMethodImpl() + " from " + m + ": " + thisMiss);
			miss+=thisMiss;
		}
		return miss;
	}
	private void checkCache(MethodInfo m) {
		if(requiredNumberOfBlocks(m) <= config.getOption(CacheConfig.CACHE_BLOCKS)) return;
		throw new AssertionError("Too few cache blocks for "+m+" - requires "+
								 requiredNumberOfBlocks(m) + " but have " +
								 config.getOption(CacheConfig.CACHE_BLOCKS));		
	}
	private boolean allFit(MethodInfo m) {
		return getMaxCacheBlocks(m) <= config.getOption(CacheConfig.CACHE_BLOCKS);
	}

	/**
	 * Compute the number of cache blocks which might be needed when calling this method
	 * @param mi
	 * @return the maximum number of cache blocks needed, s.t. we won't run out of cache
	 * blocks when invoking the given method
	 * @throws TypeException 
	 */
	public long getMaxCacheBlocks(MethodInfo mi) {
		long size = requiredNumberOfBlocks(mi);
		Iterator<CallGraphNode> iter = project.getCallGraph().getReachableMethods(mi);
		while(iter.hasNext()) {
			CallGraphNode n = iter.next();
			if(n.isAbstractNode()) continue;
			size+= requiredNumberOfBlocks(n.getMethodImpl());
		}
		return size;
	}

	private long requiredNumberOfBlocks(MethodInfo m) {
		return BlockWCET.numberOfBlocks(appInfo.getFlowGraph(m),
										config.getOption(CacheConfig.BLOCK_SIZE_WORDS).intValue());
	}
}
