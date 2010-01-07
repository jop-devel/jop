package com.jopdesign.wcet.analysis.cache;

import static com.jopdesign.wcet.graphutils.MiscUtils.addToSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.SuperGraph;
import com.jopdesign.wcet.frontend.CallGraph.CallGraphNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.SuperGraph.SuperInvokeEdge;
import com.jopdesign.wcet.frontend.SuperGraph.SuperReturnEdge;
import com.jopdesign.wcet.ipet.ILPModelBuilder;
import com.jopdesign.wcet.ipet.IpetConfig;
import com.jopdesign.wcet.ipet.LinearVector;
import com.jopdesign.wcet.ipet.MaxCostFlow;
import com.jopdesign.wcet.ipet.MaxCostFlow.DecisionVariable;
import com.jopdesign.wcet.jop.MethodCache;

/** Analysis of the variable block Method cache.
 *  Goal: Detect persistence scopes.
 *  This is not really important, but a good demonstration of the technique.
 *  
 *  TODO: [cache-analysis] Use a scopegraph instead of a callgraph
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class MethodCacheAnalysis {
	private Map<CallGraphNode, Long> blocksNeeded;
	private Project project;
	public MethodCacheAnalysis(Project p) {
		this.project = p;
	}
	/**
	 * Analyze the number of blocks needed by each scope.
	 * <h2>Technique</h2>
	 * <p>Traverse the scope graph, create a local ILP, and find maximum number of blocks</p>
	 * <ol>
	 *   <li/> Create an ILP for this scope
	 *   <li/> Add Loop Bound Constraints
	 *   <li/> Add Block Usage Constraints. 
	 *   <ol>
	 *     <li/> For each possibly invoke method {@code M}, add a binary ILP variable {@code b_M} 
	 *     <li/> Let {@code I} index all invoke blocks {@code b_i = invoke M}, and add a constraint
	 *           {@code b_M <= sum(i `in` I) frequency(b_i)}
     *   </ol>
     * </ol>
     * <h2>Explanation</h2>
     * Short Proof: Assume that at most {@code N} blocks used, and those {@code N} blocks
     * correspond to methods {@code M_1} through {@code M_n}. Then there is a path, s.t. 
     * for each method {@code M_k} the frequency of one invoke block {@code b_i = invoke M_k} 
     * is greater than zero. Conversely, if for all invoke blocks {@code b_i = invoke M_k} the
     * frequency is 0, the method is never loaded. The method at the root of the scope graph is 
     * always loaded.
	 */
	public void analyzeBlockUsage() {
		/* Get Method Cache */
		if(! project.getProcessorModel().hasMethodCache()) {
			throw new AssertionError("MethodCacheAnalysis: Processor "+
					                 project.getProcessorModel().getName()+
					                 " has not method cache");
		}
		MethodCache methodCache = project.getProcessorModel().getMethodCache();
		
		/* Prepare return value */
		blocksNeeded = new HashMap<CallGraphNode, Long>();

		/* Top Down the Scope Graph */
		TopologicalOrderIterator<CallGraphNode, DefaultEdge> iter =
			project.getCallGraph().topDownIterator();

		while(iter.hasNext()) {
			CallGraphNode scope = iter.next();
			
			/* Create a supergraph */
			SuperGraph sg = getScopeSuperGraph(scope);

			/* create an ILP graph for all reachable methods */
			String key = "method_cache_analysis:"+scope.toString();
			ILPModelBuilder imb = new ILPModelBuilder(new IpetConfig(project.getConfig()));
			MaxCostFlow<CFGNode, CFGEdge> maxCostFlow = imb.buildGlobalILPModel(key, sg, null);

			/* Add decision variables for all invoked methods, cost (blocks) and constraints */
			Map<MethodInfo, Set<CFGEdge>> accessEdges = getAccessEdges(sg);
			accessEdges.remove(scope.getMethodImpl());
			Map<DecisionVariable, MethodInfo> decisionVariables =
				new HashMap<DecisionVariable, MethodInfo>();

			for(MethodInfo mi : accessEdges.keySet()) {
				DecisionVariable dvar = maxCostFlow.createDecisionVariable();				
				decisionVariables.put(dvar,mi);
				maxCostFlow.addDecisionCost(dvar, methodCache.requiredNumberOfBlocks(mi));
				/* dvar <= sum(i `in` I) frequency(b_i); */
				LinearVector<CFGEdge> ub = new LinearVector<CFGEdge>();
				for(CFGEdge e : accessEdges.get(mi)) {
					ub.add(e,1);
				}
				maxCostFlow.addDecisionUpperBound(dvar, ub);
			}

			/* solve */
			Map<CFGEdge, Long> flowMap = new HashMap<CFGEdge, Long>();
			Map<DecisionVariable, Boolean> cacheMissMap = new HashMap<DecisionVariable, Boolean>();
			double lpCost;
			try {
				lpCost = maxCostFlow.solve(flowMap,cacheMissMap);
			} catch (Exception e) {
				throw new AssertionError("Failed to calculate block number for : "+scope);
			}
			long neededBlocks = (long) (lpCost+0.5);
			neededBlocks+=methodCache.requiredNumberOfBlocks(scope.getMethodImpl());
			this.blocksNeeded.put(scope,neededBlocks);
		}
	}
	
	public Map<CallGraphNode, Long> getBlockUsage() {
		if(blocksNeeded == null) analyzeBlockUsage();
		return blocksNeeded;
	}

	/** Get all access sites per method */
	public static Map<MethodInfo, Set<CFGEdge>> getAccessEdges(SuperGraph sg) {
		Map<MethodInfo, Set<CFGEdge>> accessEdges =
			new HashMap<MethodInfo, Set<CFGEdge>>();
		for(Entry<SuperInvokeEdge, SuperReturnEdge> invokeSite: sg.getSuperEdgePairs().entrySet()) {
			MethodInfo invoked = invokeSite.getKey().getInvokeNode().receiverFlowGraph().getMethodInfo();
			addToSet(accessEdges, invoked, invokeSite.getKey());
			MethodInfo invoker = invokeSite.getKey().getInvokeNode().invokerFlowGraph().getMethodInfo();
			addToSet(accessEdges, invoker, invokeSite.getValue());
		}
		return accessEdges;
	}

	
	private SuperGraph getScopeSuperGraph(CallGraphNode scope) {
		MethodInfo m = scope.getMethodImpl();
		return new SuperGraph(project.getWcetAppInfo(),project.getFlowGraph(m));
	}

}
