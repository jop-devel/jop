package com.jopdesign.wcet.analysis.cache;

import static com.jopdesign.wcet.graphutils.MiscUtils.addToSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.bcel.generic.InstructionHandle;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.analyses.SymbolicAddress;
import com.jopdesign.dfa.analyses.SymbolicPointsTo;
import com.jopdesign.dfa.framework.CallString;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.DFAAppInfo;
import com.jopdesign.dfa.framework.BoundedSetFactory.BoundedSet;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.BasicBlock;
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
import com.jopdesign.wcet.ipet.ILPModelBuilder.MapCostProvider;
import com.jopdesign.wcet.ipet.MaxCostFlow.DecisionVariable;

/** Analysis of the used object references.
 *  Goal: Detect persistence scopes.
 *  
 *  TODO: [cache-analysis] Use a scopegraph instead of a callgraph
 *  FIXME: [cache-analysis] Extract common code
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class ObjectRefAnalysis {
	private static final int MAX_SET_SIZE = 32;
	private Map<CallGraphNode, Long> usedReferences;
	private Project project;
	private Map<DecisionVariable, SymbolicAddress> decisionVariables =
		new HashMap<DecisionVariable, SymbolicAddress>();
	private boolean countNonDistinct;
	public ObjectRefAnalysis(Project p) {
		this(p, false);
	}
	public ObjectRefAnalysis(Project p, boolean countNonDistinct) {
		this.project = p;
		this.countNonDistinct = countNonDistinct;
	}
	/**
	 * Analyze the number of references used in each scope.
	 * For the general technique, see {@link com.jopdesign.wcet.analysis.cache.MethodCacheAnalysis#analyzeBlockUsage()}
	 * <h2>Bytecodes using object references</h2>
	 * <ul>
	 *   <li/> getfield (top of stack)
	 *   <li/> putfield (second on stack)
	 *   <li/> arraylen (top of stack)
	 *   <li/> a*load (second on stack)
	 *   <li/> a*store (third on stack)
	 * </ul>
	 * <h2>Reference Analysis</h2>
	 * For each scope, we do the following:
	 * <ol>
	 * <li/> Perform a local, symbolic points-to analysis
	 * <li/> Traverse all instructions, and collect for each reference the basic blocks it might
	 *       be used in. Additionally, the cost of each basic block is set to the number of
	 *       TOP references accessed.
	 * <li/> For each reference, add a decision variable denoting whether it is used at all,
	 *       and add corresponding constraints.
	 * </ol>
	 */
	public void analyzeRefUsage() {
		/* Prepare return value */
		usedReferences = new HashMap<CallGraphNode, Long>();

		CallString emptyCallString = new CallString();
		/* Top Down the Scope Graph */
		TopologicalOrderIterator<CallGraphNode, DefaultEdge> iter =
			project.getCallGraph().topDownIterator();

		while(iter.hasNext()) {
			try{
			CallGraphNode scope = iter.next();
			System.out.println("Running SPT analysis for: "+scope.getMethodImpl().getFQMethodName());
			/* Perform a local symbolic points to analysis */
			DFAAppInfo dfa = project.getDfaProgram();
			SymbolicPointsTo spt = new SymbolicPointsTo(MAX_SET_SIZE);
			dfa.runLocalAnalysis(spt, scope.getMethodImpl().getFQMethodName());
			HashMap<InstructionHandle, ContextMap<CallString, BoundedSet<SymbolicAddress>>> usedRefs =
				spt.getResult();

			/* Create a supergraph */
			SuperGraph sg = getScopeSuperGraph(scope);

			/* create an ILP graph for all reachable methods */
			String key = "object_ref_analysis:"+scope.toString();
			ILPModelBuilder imb = new ILPModelBuilder(new IpetConfig(project.getConfig()));
			Map<CFGNode,Long> costMap = new HashMap<CFGNode, Long>();
			
			/* Traverse vertex set.
			 * Add vertex to access set of referenced addresses
			 * For each occurence of TOP, add cost of 1
			 */
			HashMap<SymbolicAddress, Map<CFGNode, Integer>> accessSets =
				new HashMap<SymbolicAddress,Map<CFGNode, Integer>>();
			for(CFGNode n : sg.vertexSet()) {
				BasicBlock bb = n.getBasicBlock();
				long topCost = 0;
				if(bb == null) continue;
				InstructionHandle ih = bb.getFirstInstruction();
				while(ih.getNext() != null) {
					BoundedSet<SymbolicAddress> refs;
					if(usedRefs.containsKey(ih)) {
						refs = usedRefs.get(ih).get(emptyCallString);
						if(refs.isSaturated() || countNonDistinct) {
							topCost += 1;
						} else {
							for(SymbolicAddress ref : refs.getSet()) {
								addAccessSite(accessSets, ref, n);
							}
						}
					}
					ih = ih.getNext();
				}
				costMap.put(n,topCost);
			}
			
			MaxCostFlow<CFGNode, CFGEdge> maxCostFlow = imb.buildGlobalILPModel(key, sg, 
					new MapCostProvider<CFGNode>(costMap,0));

			/* Add decision variables for all references */
			for(Entry<SymbolicAddress, Map<CFGNode, Integer>> accessEntry : accessSets.entrySet()) {
				SymbolicAddress ref = accessEntry.getKey();
				Map<CFGNode, Integer> accessSet = accessEntry.getValue();
				DecisionVariable dvar = maxCostFlow.createDecisionVariable();				
				decisionVariables .put(dvar,ref);
				maxCostFlow.addDecisionCost(dvar, 1);
				/* dvar <= sum(i `in` I) frequency(b_i) */
				LinearVector<CFGEdge> ub = new LinearVector<CFGEdge>();
				for(Entry<CFGNode, Integer> entry : accessSet.entrySet()) {
					CFGNode node = entry.getKey();
					// we do not really need the count (entry.getValue()) for this constraint
					for(CFGEdge edge : sg.incomingEdgesOf(node)) {
						ub.add(edge,1);						
					}
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
			long accessedReferences = (long) (lpCost+0.5);
			this.usedReferences.put(scope, accessedReferences);
			} catch(NullPointerException nullex) {
				nullex.printStackTrace();				
			}
		}
	}
	
	private void addAccessSite(
			HashMap<SymbolicAddress, Map<CFGNode, Integer>> accessSets,
			SymbolicAddress ref, CFGNode n) {
		Map<CFGNode, Integer> accessSet = accessSets.get(ref);
		if(accessSet == null) {
			accessSet = new HashMap<CFGNode, Integer>();
			accessSets.put(ref, accessSet);
		}
		Integer oldCount = accessSet.get(n);
		if(oldCount == null) {
			accessSet.put(n,1);
		} else {
			accessSet.put(n,oldCount+1);			
		}
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
	public Map<CallGraphNode, Long> getRefUsage() {
		if(usedReferences == null) analyzeRefUsage();
		return usedReferences;
	}

}
