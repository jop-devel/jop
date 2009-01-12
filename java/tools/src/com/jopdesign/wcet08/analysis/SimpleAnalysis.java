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
import com.jopdesign.wcet08.frontend.ControlFlowGraph.SummaryNode;
import com.jopdesign.wcet08.ipet.ILPModelBuilder;
import com.jopdesign.wcet08.ipet.MaxCostFlow;
import com.jopdesign.wcet08.ipet.ILPModelBuilder.CostProvider;
import com.jopdesign.wcet08.report.ClassReport;

/**
 * Simple and fast local analysis with cache approximation.
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class SimpleAnalysis {
	private static final boolean useGlobalIPET = false;
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
	public class WcetSolution {
		private long lpCost;
		private WcetCost cost;
		private Map<CFGNode,Long> nodeFlow;
		private Map<CFGEdge,Long> edgeFlow;
		private DirectedGraph<CFGNode, CFGEdge> graph;
		private Map<CFGNode, WcetCost> nodeCosts;
		public WcetSolution(DirectedGraph<CFGNode,CFGEdge> g, Map<CFGNode,WcetCost> nodeCosts) { 
			this.graph = g;
			this.nodeCosts= nodeCosts;
		}
		public void setSolution(long lpCost, Map<CFGEdge, Long> edgeFlow) {
			this.lpCost = lpCost;
			this.edgeFlow = edgeFlow;
			computeNodeFlow();
			computeCost();
		}
		public long getLpCost() {
			return lpCost;
		}
		public WcetCost getCost() {
			return cost;
		}
		public long getNodeFlow(CFGNode n) {
			return getNodeFlow().get(n);
		}
		public Map<CFGNode, Long> getNodeFlow() {
			return nodeFlow;
		}
		public Map<CFGEdge, Long> getEdgeFlow() {
			return edgeFlow;
		}
		/** Safety check: compare flow*cost to actual solution */
		public void checkConsistentency() {
			if(cost.getCost() != lpCost) {
				throw new AssertionError("The solution implies that the flow graph cost is " 
										 + cost.getCost() + ", but the ILP solver reported "+lpCost);
			}			
		}
		private void computeNodeFlow() {
			nodeFlow = new HashMap<CFGNode, Long>();
			for(CFGNode n : graph.vertexSet()) {
				if(graph.inDegreeOf(n) == 0) nodeFlow.put(n, 0L); // ENTRY and DEAD CODE (no flow)
				else {
					long flow = 0;
					for(CFGEdge inEdge : graph.incomingEdgesOf(n)) {
						flow+=edgeFlow.get(inEdge);
					}
					nodeFlow.put(n, flow);
				}
			}			
		}
		/* Compute cost, sepearting local and non-local cost */
		private void computeCost() {
			cost = new WcetCost();
			for(CFGNode n : graph.vertexSet()) {
				long flow = nodeFlow.get(n);
				cost.addLocalCost(flow * nodeCosts.get(n).getLocalAndCacheCost());
				cost.addNonLocalCost(flow * nodeCosts.get(n).getNonLocalCost());
			}			
		}
	}
	/* provide cost given a node->cost table */
	public static class MapCostProvider<T> implements CostProvider<T> {
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
	private ILPModelBuilder modelBuilder;
	private BlockWCET blockBuilder;

	public SimpleAnalysis(Project project) {
		this.project = project;
		this.appInfo = project.getWcetAppInfo();

		this.wcetMap = new Hashtable<WcetKey,WcetCost>();

		this.modelBuilder = new ILPModelBuilder(project);
		this.blockBuilder = new BlockWCET(project);
	}
	
	/**
	 * WCET analysis of the given method, using some cache approximation scheme.
	 * <ul>
	 *  <li/>{@link CacheApproximation.ALWAYS_HIT}: Assume all method cache accesses are hits
	 *  <li/>{@link CacheApproximation.ALWAYS_MISS}: Assume all method cache accesses are misses
	 *  <li/>{@link CacheApproximation.ANALYSE_REACHABLE}: 
	 *  	<p>If for some invocation of <code>m</code>, all methods reachable from and including <code>m</code>
	 *      fit into the cache, add the cost for missing each method exactly once.
	 *      If the method isn't a leaf, the minimal number cycles is hidden.
	 *      Additionally, add the cost for missing on return.</p>
	 *      <p>Otherwise, assume invoke/return is miss, and analyze the method using ANALYSE_REACHABLE.</p>
	 * @param m the method to be analyzed
	 * @param cacheMode the cache approximation strategy
	 * @return
	 * 
	 * <p>FIXME: Logging/Report need to be cleaned up </p>
	 */
	public WcetCost computeWCET(MethodInfo m, CacheApproximation cacheMode) {
		/* use memoization to speed up analysis */
		WcetKey key = new WcetKey(m,cacheMode);
		if(wcetMap.containsKey(key)) return wcetMap.get(key);

		/* check cache is big enough */
		blockBuilder.checkCache(m); /* TODO: should throw exception */

		/* build wcet map */
		ControlFlowGraph cfg = appInfo.getFlowGraph(m);
		Map<CFGNode,WcetCost> nodeCosts;
		nodeCosts = buildNodeCostMap(cfg,cacheMode);
		WcetSolution sol = runWCETComputation(key.toString(), cfg, cacheMode, nodeCosts);
		sol.checkConsistentency();
		wcetMap.put(key, sol.getCost()); 
		
		/* Logging and Report */
		if(project.reportGenerationActive()) {
			Hashtable<CFGNode, String> nodeFlowCostDescrs = new Hashtable<CFGNode, String>();
			
			for(CFGNode n : cfg.getGraph().vertexSet()) {
				if(sol.getNodeFlow(n) > 0) {
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
						cr.addLineProperty(sourceLine, "cost", oldCost + sol.getNodeFlow(n)*nodeCosts.get(n).getCost());
						for(InstructionHandle ih : basicBlock.getInstructions()) {
							sourceLine = lineNumberTable.getSourceLine(ih.getPosition());
							cr.addLineProperty(sourceLine, "color", "red");
						}
					}
				} else {
					nodeFlowCostDescrs.put(n, ""+nodeCosts.get(n).getCost());
				}
			}
			logger.info("WCET for " + key + ": "+sol.getCost());
			Map<String,Object> stats = new Hashtable<String, Object>();
			stats.put("WCET",sol.getCost());
			stats.put("mode",cacheMode);
			stats.put("all-methods-fit-in-cache",blockBuilder.allFit(m));
			project.getReport().addDetailedReport(m,"WCET_"+cacheMode.toString(),stats,nodeFlowCostDescrs,sol.getEdgeFlow());
		}
		return sol.getCost();
	}
	/**
	 * Compute the WCET of the given control flow graph
	 * @param name name for the ILP problem
	 * @param cfg the control flow graph 
	 * @param cacheMode which cache approximation to use
	 * @param nodeCosts (costs per node which should be used) (if null, calculate cost)
	 * @return the WCET for the given CFG
	 */
	public WcetSolution runWCETComputation(
			String name, 
			ControlFlowGraph cfg,
			CacheApproximation cacheMode,
			Map<CFGNode,WcetCost> nodeCosts) {		
		if(nodeCosts == null)  nodeCosts = buildNodeCostMap(cfg,cacheMode);
		WcetSolution sol = new WcetSolution(cfg.getGraph(),nodeCosts);
		CostProvider<CFGNode> costProvider = new MapCostProvider<CFGNode>(nodeCosts);
		MaxCostFlow<CFGNode,CFGEdge> problem = 
			modelBuilder.buildLocalILPModel(name,cfg, costProvider);
		/* solve ILP */
		/* extract node flow, local cost, cache cost, cummulative cost */
		long maxCost = 0;
		Map<CFGEdge, Long> edgeFlow = new HashMap<CFGEdge, Long>();
		try {
			maxCost = Math.round(problem.solve(edgeFlow));
		} catch (Exception e) {
			throw new Error("Failed to solve LP problem: "+e,e);
		}
		sol.setSolution(maxCost, edgeFlow);
		return sol;
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
			if(n.getBasicBlock() != null || n instanceof SummaryNode) {
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
		public void visitSummaryNode(SummaryNode n) {
			cost.addLocalCost(runWCETComputation("summary", n.getSubGraph(), CacheApproximation.ALWAYS_MISS,null).getCost().getCost());
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
			long cacheCost = BlockWCET.getInvokeReturnMissCost(appInfo.getFlowGraph(invoker),appInfo.getFlowGraph(invoked));				
			long nonLocalCost = computeWCET(invoked, cacheMode).getCost();
			switch(cacheMode) {
			case ALWAYS_HIT:
				cacheCost=0; break;
			case ALWAYS_MISS:
				break;				
			case ANALYSE_REACHABLE:
				/* ALL FIT is unsafe if the invoked method is a leaf 
				 * This is because the cost for accessing the method itself is usually attributes
				 * to some return node, which is missing in the case of leaf methods.
				 * Should be cleaned up !!
				 */
				if(blockBuilder.allFit(invoked) && ! project.getCallGraph().isLeafNode(invoked)) {
					long allMissCost = nonLocalCost + cacheCost;
					long returnCost = BlockWCET.getMissOnReturnCost(appInfo.getFlowGraph(invoker));

					if(! useGlobalIPET) {
						long allFitAhCost = computeWCET(invoked, CacheApproximation.ALWAYS_HIT).getCost();
						long allFitPenalty = totalCacheMissPenalty(invoked);										 
						long allFitCost = allFitAhCost + allFitPenalty  + returnCost;
						if(allFitCost <= allMissCost) {
							cacheCost = allFitPenalty;
							nonLocalCost = allFitAhCost;
						}
					} else {
						GlobalAnalysis ga = new GlobalAnalysis(project);
						WcetCost recCost;
						try { recCost = ga.computeWCET(invoked, CacheApproximation.ALL_FIT); }
						catch (Exception e) { throw new AssertionError(e); }
						long allFitCostGlobal = recCost.getCost() + returnCost;
						cacheCost = returnCost;
						nonLocalCost = recCost.getCost();
					}
				}
				break;
			}
			cost.addNonLocalCost(nonLocalCost);
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
	 * Let <code>h</code> be the number of cycles hidden by <strong>any</strong> return or 
	 * invoke instructions. Then the cache miss penalty is bounded by <code>(b-h)</code> per 
	 * method.
	 * </p><p>
	 * <code>b</code> is given by <code>b = 6 + (n+1) * (2+c)</code>, with <code>n</code>
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
			int words = appInfo.getFlowGraph(n.getMethodImpl()).getNumberOfWords();
			int hidden = project.getCallGraph().isLeafNode(n) ?
					WCETInstruction.INVOKE_HIDDEN_LOAD_CYCLES :
					WCETInstruction.MIN_HIDDEN_LOAD_CYCLES;
			int thisMiss = Math.max(0,WCETInstruction.calculateB(false, words) - hidden); 
			logger.info("Adding cache miss penalty for "+n.getMethodImpl() + " from " + m + ": " + thisMiss);
			miss+=thisMiss;
		}
		return miss;
	}
}
