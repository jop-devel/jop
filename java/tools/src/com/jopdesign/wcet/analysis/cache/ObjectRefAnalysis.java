package com.jopdesign.wcet.analysis.cache;

import static com.jopdesign.wcet.graphutils.MiscUtils.addToSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.bcel.generic.ARRAYLENGTH;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;
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
import com.jopdesign.wcet.graphutils.MiscUtils.Query;
import com.jopdesign.wcet.ipet.ILPModelBuilder;
import com.jopdesign.wcet.ipet.IpetConfig;
import com.jopdesign.wcet.ipet.LinearVector;
import com.jopdesign.wcet.ipet.MaxCostFlow;
import com.jopdesign.wcet.ipet.ILPModelBuilder.MapCostProvider;
import com.jopdesign.wcet.ipet.MaxCostFlow.DecisionVariable;
import com.jopdesign.wcet.jop.JOPConfig;

/** Analysis of the used object references.
 *  Goal: Detect persistence scopes.
 *  
 *  TODO: [cache-analysis] Use a scopegraph instead of a callgraph
 *  FIXME: [cache-analysis] Extract common code
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class ObjectRefAnalysis {
	
	private static final int DEFAULT_SET_SIZE = 64;
	/* This is the current consensus:
	 * - One cache line per object
	 * - Only consider getfield. putfield does not modify cache
	 * - Handle access should be configurable (HANDLE_ACCESS = false or true)
	 */
	private static final boolean ALL_HANDLE_ACCESSES = false; /* Only consider getfield (false) or all handle accesses */
	private static final long UNKNOWN_OBJECT_PENALTY = 1000;

	private boolean countTotalAccessCount;

	private int maxSetSize;

	private Map<CallGraphNode, Long> maxReferencesAccessed;
	private Map<CallGraphNode, Set<SymbolicAddress>> refSet;
	private HashMap<CallGraphNode, Long> maxFieldsAccessed;
	private HashMap<CallGraphNode, Set<SymbolicAddress>> fieldSet;
	private Map<CallGraphNode, Set<String>> saturatedRefSets;

	private Project project;
	private Map<DecisionVariable, SymbolicAddress> decisionVariables =
		new HashMap<DecisionVariable, SymbolicAddress>();

	public ObjectRefAnalysis(Project p, int setSize) {
		this(p, setSize, false);
	}
	
	public ObjectRefAnalysis(Project p, int setSize, boolean countAll) {
		this.project = p;
		this.countTotalAccessCount = countAll;
		this.maxSetSize = setSize;
	}
	
	private class ExecOnceQuery implements Query<InstructionHandle> {
		private ExecuteOnceAnalysis eoAna;
		private CallGraphNode scope;
		public ExecOnceQuery(ExecuteOnceAnalysis eoAnalysis, CallGraphNode scope) {
			this.eoAna = eoAnalysis;
			this.scope = scope;
		}
		public boolean query(InstructionHandle a) {
			CFGNode n = BasicBlock.getHandleNode(a);
			if(n == null) {				
				// Logger.getLogger("Object Cache Analysis").error("No node for instruction "+a);
				return false;
			} else {
				return eoAna.isExecutedOnce(scope, n);
			}
		}
		
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
		CallString emptyCallString = new CallString();

		/* Prepare return value */
		maxReferencesAccessed = new HashMap<CallGraphNode, Long>();
		refSet = new HashMap<CallGraphNode, Set<SymbolicAddress>>();
		maxFieldsAccessed = new HashMap<CallGraphNode, Long>();
		fieldSet = new HashMap<CallGraphNode, Set<SymbolicAddress>>();
		saturatedRefSets = new HashMap<CallGraphNode, Set<String>>();
				
		/* Top Down the Scope Graph */
		TopologicalOrderIterator<CallGraphNode, DefaultEdge> iter =
			project.getCallGraph().topDownIterator();

		ExecuteOnceAnalysis eoAna = new ExecuteOnceAnalysis(project);
		while(iter.hasNext()) {

			CallGraphNode scope = iter.next();
			/* Perform a local symbolic points to analysis */
			DFAAppInfo dfa = project.getDfaProgram();
			SymbolicPointsTo spt = new SymbolicPointsTo(maxSetSize, (int)project.getProjectConfig().callstringLength(), new ExecOnceQuery(eoAna,scope));
			dfa.runLocalAnalysis(spt, scope.getMethodImpl().getFQMethodName());
			HashMap<InstructionHandle, ContextMap<CallString, BoundedSet<SymbolicAddress>>> usedRefs =
				spt.getResult();

			/* Create a supergraph */
			SuperGraph sg = getScopeSuperGraph(scope);

			/* Traverse vertex set (1). Collect those types where we could not resolve
			 * the symbolic object names. (Not too useful in the analysis, but useful
			 * for debugging)
			 */
			HashSet<String> topTypes =
				new HashSet<String>(); // FIXME: We should deal with subtyping!!
			for(CFGNode n : sg.vertexSet()) {
				BasicBlock bb = n.getBasicBlock();
				if(bb == null) continue;
				for(InstructionHandle ih : bb.getInstructions()) {
					BoundedSet<SymbolicAddress> refs;
					if(usedRefs.containsKey(ih)) {
						refs = usedRefs.get(ih).get(emptyCallString);
						String handleType = getHandleType(project, n, ih);
						if(handleType == null) continue;
						if(refs.isSaturated()) {
							topTypes.add(handleType);
						}
					}
				}
			}
			
			this.saturatedRefSets.put(scope, topTypes);

			/* Compute worst-case object count */
			HashSet<SymbolicAddress> usedObjectsSet = new HashSet<SymbolicAddress>();
			long accessedReferences = computeAccessCount(scope, sg, usedRefs, usedObjectsSet, false);
			this.maxReferencesAccessed.put(scope, accessedReferences);
			this.refSet.put(scope, usedObjectsSet);

			/* Compute worst-case field access count (this part will be integrated into
			 * the WCET computation) */
			HashSet<SymbolicAddress> usedFieldsSet = new HashSet<SymbolicAddress>();
			long accessedFields = computeAccessCount(scope, sg, usedRefs, usedFieldsSet, true);
			this.maxFieldsAccessed.put(scope, accessedFields);
			this.fieldSet.put(scope, usedFieldsSet);
			


		}
	}
	
	private long computeAccessCount(CallGraphNode scope, 
									SuperGraph sg, 
									Map<InstructionHandle,ContextMap<CallString,BoundedSet<SymbolicAddress>>> usedRefs, 
									HashSet<SymbolicAddress> usedSetOut, 
									boolean countFields)
	{
		CallString emptyCallString = new CallString();

		/* create an ILP graph for all reachable methods */
		String key = "object_ref_analysis:"+scope.toString();
		ILPModelBuilder imb = new ILPModelBuilder(new IpetConfig(project.getConfig()));
		HashMap<SymbolicAddress, Map<CFGNode, Integer>> accessSets =
			new HashMap<SymbolicAddress,Map<CFGNode, Integer>>();
		Map<CFGNode,Long> costMap = new HashMap<CFGNode, Long>();

		/* Traverse vertex set.
		 * Add vertex to access set of referenced addresses
		 * For references whose type cannot be fully resolved, at a
		 * cost of 1.
		 */
		// FIXME: We should deal with subtyping
		for(CFGNode n : sg.vertexSet()) {
			BasicBlock bb = n.getBasicBlock();
			long topCost = 0;
			if(bb == null) continue;

			for(InstructionHandle ih : bb.getInstructions()) {
				BoundedSet<SymbolicAddress> refs;
				if(usedRefs.containsKey(ih)) {
					String handleType = getHandleType(project, n, ih); 
					if(handleType == null) continue;
					refs = usedRefs.get(ih).get(emptyCallString);						
					if(countTotalAccessCount) {
						topCost += 1;
					} else if(refs.isSaturated()) {
						topCost += 1;
					} else {
						if(! countFields) {
							for(SymbolicAddress ref : refs.getSet()) {
								addAccessSite(accessSets, ref, n);
							}
						} else {
							// Hack to look at field access
							String fieldName =
								((FieldInstruction)ih.getInstruction()).getFieldName(
										bb.cpg());
							for(SymbolicAddress ref : refs.getSet()) {
								addAccessSite(accessSets, ref.access(fieldName), n);
							}
						}
					}
				}
			}
			costMap.put(n,topCost);
		}

		MaxCostFlow<CFGNode, CFGEdge> maxCostFlow = imb.buildGlobalILPModel(key, sg, new MapCostProvider<CFGNode>(costMap,0));

		/* Add decision variables for all references */
		if(! countTotalAccessCount) {
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
		}
		/* solve */
		Map<CFGEdge, Long> flowMap = new HashMap<CFGEdge, Long>();
		Map<DecisionVariable, Boolean> refUseMap = new HashMap<DecisionVariable, Boolean>();
		double lpCost;
		try {
			lpCost = maxCostFlow.solve(flowMap,refUseMap);
		} catch (Exception e) {
			Logger.getLogger(ObjectRefAnalysis.class).error("Failed to calculate references for : "+scope);
			lpCost = 2000000000.0;
		}
		long accessedReferences = (long) (lpCost+0.5);
		
		/* extract solution */
		for(Entry<DecisionVariable, SymbolicAddress> entry : this.decisionVariables.entrySet()) {
			DecisionVariable dvar = entry.getKey();
			if(refUseMap.containsKey(dvar) && refUseMap.get(dvar)) {
				SymbolicAddress addr = entry.getValue();
				usedSetOut.add(addr);
			}
		}
		return accessedReferences;
	}

	private void addAccessSite(
			Map<SymbolicAddress, Map<CFGNode, Integer>> accessSets,
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
	
	public Map<CallGraphNode, Long> getMaxReferencesAccessed() {
		if(maxReferencesAccessed == null) analyzeRefUsage();
		return maxReferencesAccessed;
	}

	public Map<CallGraphNode, Long> getMaxFieldsAccessed() {
		if(maxFieldsAccessed == null) analyzeRefUsage();
		return maxFieldsAccessed;
	}

	
	public Map<CallGraphNode, Set<SymbolicAddress>> getUsedSymbolicNames() {
		if(refSet == null) analyzeRefUsage();
		return refSet;		
	}
	public Map<CallGraphNode, Set<String>> getSaturatedRefSets() {
		return this.saturatedRefSets;
	}

	public static String getHandleType(Project project, CFGNode n, InstructionHandle ih) {
		ConstantPoolGen constPool = n.getControlFlowGraph().getMethodInfo().getConstantPoolGen();
		Instruction instr = ih.getInstruction();
		if(instr instanceof GETFIELD) {
			GETFIELD gf = (GETFIELD) instr;
			ReferenceType refty = gf.getReferenceType(constPool);
			return refty.toString();
		}
		if(! ALL_HANDLE_ACCESSES)
			return null;
		
		if(instr instanceof PUTFIELD) {
			PUTFIELD pf = (PUTFIELD) instr;
			ReferenceType refty = pf.getReferenceType(constPool);
			return refty.toString();			
		}
		if(instr instanceof ArrayInstruction)
		{
			ArrayInstruction ainstr = (ArrayInstruction) instr;
			return "[]";
		}
		if(instr instanceof ARRAYLENGTH) {
			ARRAYLENGTH ainstr = (ARRAYLENGTH) instr;
			return "[]";
		}
		if(instr instanceof INVOKEINTERFACE || instr instanceof INVOKEVIRTUAL)
		{
			return "$header";
		}
		return null;
	}
}
