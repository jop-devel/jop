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
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.SuperGraph;
import com.jopdesign.wcet.frontend.CallGraph.CallGraphNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.BasicBlockNode;
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
 *  This is the current consensus:
 *  <ul><li/>One cache line per object
 * <li/> Only consider getfield. putfield does not modify cache
 * <li/> Handle access should be configurable (HANDLE_ACCESS = false or true)
 * </ul>
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
 * 
 *  <ul><li/>TODO: [cache-analysis] Use a scopegraph instead of a callgraph
 *  <li/>FIXME: [cache-analysis] Extract common code
 *  <li/>FIXME: [cache-analysis] Handle subtyping when dealing with aliases, or use a store-based approach
 *  </ul>
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class ObjectRefAnalysis {
	
	/* Only consider getfield (false) or all handle accesses */
	private static boolean ALL_HANDLE_ACCESSES = false; 
		
	
	/* Simple Cost Models for our Object Cache */
	public static class ObjectCacheCostModel {
		public static final ObjectCacheCostModel COUNT_REF_TAGS = new ObjectCacheCostModel(0,1,0);;
		public static final ObjectCacheCostModel COUNT_FIELD_TAGS = new ObjectCacheCostModel(1,0,0);
		private long loadFieldCost;
		private long loadCacheLineCost;
		private long fieldAccessCostHit;
		private long fieldAccessCostBypass;

		public ObjectCacheCostModel(long loadFieldCost, long loadCacheLineCost,long fieldAccessCostBypass)
		{
			this.loadFieldCost = loadFieldCost;
			this.loadCacheLineCost = loadCacheLineCost;
			this.fieldAccessCostBypass = fieldAccessCostBypass;
		}
		/**
		 * @return the loadFieldCost
		 */
		public long getLoadFieldCost() {
			return loadFieldCost;
		}

		/**
		 * @return the loadCacheLineCost
		 */
		public long getLoadCacheLineCost() {
			return loadCacheLineCost;
		}

		/**
		 * @return the fieldAccessCostHit
		 */
		public long getFieldAccessCostHit() {
			return fieldAccessCostHit;
		}

		/**
		 * @return the fieldAccessCostBypass
		 */
		public long getFieldAccessCostBypass() {
			return fieldAccessCostBypass;
		}
	}


	/* transparent class for hiding the type of DFA result */
	public static class LocalPointsToResult {
		private HashMap<InstructionHandle, ContextMap<CallString, BoundedSet<SymbolicAddress>>> pointsTo;		
		private LocalPointsToResult(HashMap<InstructionHandle, ContextMap<CallString, BoundedSet<SymbolicAddress>>> pTo) {
			pointsTo = pTo;
		}
	}

	/* class for checking whether a basic block is executed as most once in a scope */
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
				Logger.getLogger("Object Cache Analysis").info("No node for instruction "+a);
				return false;
			} else {
				return eoAna.isExecutedOnce(scope, n);
			}
		}		
	}
	
	/* The maximum index for cached fields */
	private int maxCachedFieldIndex;

	/* Whether to use a 'single field' cache, i.e., use fields as tags */
	private boolean fieldAsTag;

	/* Maximum number of objects tracked for one reference */
	private int maxSetSize;

	/* Those type which have an unbounded number of objects in the given scope
	 * FIXME: maybe we should switch to allocation sites (more precise, no subtyping) */
	private Map<CallGraphNode, Set<String>> saturatedTypes;

	/* Maximum number of objects which are loaded into the object cache (at least one field has to be accessed) */
	private Map<CallGraphNode, Long> maxCachedTagsAccessed;
	
	/* The set of symbolic object names loaded into the cache (without saturated references) */
	private Map<CallGraphNode, Set<SymbolicAddress>> tagSet;


	private Project project;
	private Map<DecisionVariable, SymbolicAddress> decisionVariables =
		new HashMap<DecisionVariable, SymbolicAddress>();


	public ObjectRefAnalysis(Project p,  boolean fieldAsTag, int maxCachedIndex, int setSize) {
		this.project = p;
		this.fieldAsTag = fieldAsTag;
		this.maxSetSize = setSize;
		this.maxCachedFieldIndex = maxCachedIndex;

		saturatedTypes = new HashMap<CallGraphNode, Set<String>>();
		maxCachedTagsAccessed = new HashMap<CallGraphNode, Long>();
		tagSet = new HashMap<CallGraphNode, Set<SymbolicAddress>>();
	}
	
	public LocalPointsToResult getUsedRefs(CallGraphNode scope) {
		ExecuteOnceAnalysis eoAna = new ExecuteOnceAnalysis(project);
		DFAAppInfo dfa = project.getDfaProgram();
		SymbolicPointsTo spt = new SymbolicPointsTo(maxSetSize, 
				(int)project.getProjectConfig().callstringLength(), 
				new ExecOnceQuery(eoAna,scope));
		dfa.runLocalAnalysis(spt,scope.getMethodImpl().getFQMethodName());
		LocalPointsToResult lpt = new LocalPointsToResult(spt.getResult());
		return lpt;
	}
	
	/** Traverse vertex set. Collect those types where we could not resolve
	 * the symbolic object names. (Not too useful in the analysis, but useful
	 * for debugging)
	 */
	public HashSet<String> getSaturatedTypes(SuperGraph sg, LocalPointsToResult usedRefs) {
		HashSet<String> topTypes =
			new HashSet<String>(); 
		for(CFGNode n : sg.vertexSet()) {
			BasicBlock bb = n.getBasicBlock();
			if(bb == null) continue;
			for(InstructionHandle ih : bb.getInstructions()) {
				BoundedSet<SymbolicAddress> refs;
				if(usedRefs.pointsTo.containsKey(ih)) {
					refs = usedRefs.pointsTo.get(ih).get(new CallString());
					String handleType = getHandleType(project, n, ih);
					if(handleType == null) continue;
					if(refs.isSaturated()) {
						topTypes.add(handleType);
					}
				}
			}
		}		
		return topTypes;
	}

	public long getMaxCachedTags(CallGraphNode scope)
	{
		Long maxCachedTags = this.maxCachedTagsAccessed.get(scope);
		if(maxCachedTags != null) return maxCachedTags;

		LocalPointsToResult usedRefs = getUsedRefs(scope);

		/* Create a supergraph */
		SuperGraph sg = getScopeSuperGraph(scope);
		
		this.saturatedTypes.put(scope, getSaturatedTypes(sg,usedRefs));

		/* Compute worst-case number of objects/fields accessed */
		HashSet<SymbolicAddress> usedObjectsSet = new HashSet<SymbolicAddress>();
		ObjectCacheCostModel costModel;
		if(this.fieldAsTag) {
			costModel = ObjectCacheCostModel.COUNT_FIELD_TAGS;			
		} else {
			costModel = ObjectCacheCostModel.COUNT_REF_TAGS;			
		}
		maxCachedTags = computeCacheCost(scope, sg, usedRefs, usedObjectsSet, costModel);
		
		this.tagSet.put(scope, usedObjectsSet);

		maxCachedTagsAccessed.put(scope,maxCachedTags);
		return maxCachedTags;
	}
	
	public long getMaxCacheCost(CallGraphNode scope, ObjectCacheCostModel costModel)
	{
		LocalPointsToResult usedRefs = getUsedRefs(scope);
		SuperGraph sg = getScopeSuperGraph(scope);
		/* Compute worst-case cost */
		HashSet<SymbolicAddress> usedObjectsSet = new HashSet<SymbolicAddress>();
		long cost = computeCacheCost(scope, sg, usedRefs, usedObjectsSet, costModel);
		return cost;		
	}

	private long computeCacheCost(CallGraphNode scope, 
									SuperGraph sg, 
									LocalPointsToResult usedRefs, 
									HashSet<SymbolicAddress> usedSetOut,
									ObjectCacheCostModel costModel)
	{
		CallString emptyCallString = new CallString();
		HashMap<SymbolicAddress, Map<CFGNode, Integer>> refAccessSets =
			new HashMap<SymbolicAddress,Map<CFGNode, Integer>>();
		HashMap<SymbolicAddress, Map<CFGNode, Integer>> fieldAccessSets =
			new HashMap<SymbolicAddress,Map<CFGNode, Integer>>();
		Map<CFGNode,Long> costMap = new HashMap<CFGNode, Long>();

		/* Traverse vertex set.
		 * Add vertex to access set of referenced addresses
		 * For references whose type cannot be fully resolved, at a
		 * cost of 1.
		 */
		// FIXME: We should deal with subtyping		
		for(CFGNode n : sg.vertexSet()) {
			/* Compute cost for basic block */
			BasicBlock bb = n.getBasicBlock();
			if(bb == null) continue;
			long bypassCost = 0;
			long alwaysMissCost = 0;

			for(InstructionHandle ih : bb.getInstructions()) {
				BoundedSet<SymbolicAddress> refs;

				String handleType = getHandleType(project, n, ih); 				
				if(handleType == null) continue; /* No getfield/handle access */
				
				if(usedRefs.pointsTo.containsKey(ih)) {					
					String fieldName = ((FieldInstruction)ih.getInstruction()).getFieldName(bb.cpg());
					
					if(! isFieldCached(n.getControlFlowGraph(), ih, maxCachedFieldIndex)) {
						bypassCost += costModel.getFieldAccessCostBypass();
						continue;
					}
										
					refs = usedRefs.pointsTo.get(ih).get(emptyCallString);						
					if(refs.isSaturated()) {
						alwaysMissCost += costModel.getLoadCacheLineCost() + costModel.getLoadFieldCost();
					} else {
						for(SymbolicAddress ref : refs.getSet()) {
							addAccessSite(refAccessSets, ref, n);
							addAccessSite(fieldAccessSets, ref.access(fieldName), n);
						}
					}
				} else {
					System.err.println("No DFA results for: "+ih.getInstruction() + " with field " + ((FieldInstruction)ih.getInstruction()).getFieldName(bb.cpg()));
				}
			}
			costMap.put(n,bypassCost + alwaysMissCost);
		}
		/* create an ILP graph for all reachable methods */
		String key = "object_ref_analysis:"+scope.toString();
		ILPModelBuilder imb = new ILPModelBuilder(new IpetConfig(project.getConfig()));
		MaxCostFlow<CFGNode, CFGEdge> maxCostFlow = imb.buildGlobalILPModel(key, sg, new MapCostProvider<CFGNode>(costMap,0));

		/* Add decision variables for all tags */		
		for(Entry<SymbolicAddress, Map<CFGNode, Integer>> accessEntry : refAccessSets.entrySet()) {
			addDecision(sg, maxCostFlow, accessEntry, costModel.getLoadCacheLineCost());
		}
		for(Entry<SymbolicAddress, Map<CFGNode, Integer>> accessEntry : fieldAccessSets.entrySet()) {
			addDecision(sg, maxCostFlow, accessEntry, costModel.getLoadFieldCost());
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
		long cost = (long) (lpCost+0.5);
		
		/* extract solution */
		for(Entry<DecisionVariable, SymbolicAddress> entry : this.decisionVariables.entrySet()) {
			DecisionVariable dvar = entry.getKey();
			if(refUseMap.containsKey(dvar) && refUseMap.get(dvar)) {
				SymbolicAddress addr = entry.getValue();
				usedSetOut.add(addr);
				//System.out.println("Used dvar: "+addr);
			}
		}
		return cost;
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
		
	public Set<SymbolicAddress> getUsedSymbolicNames(CallGraphNode scope) {
		if(! tagSet.containsKey(scope)) getMaxCachedTags(scope);
		return tagSet.get(scope);		
	}
	
	public Map<CallGraphNode, Set<String>> getSaturatedRefSets() {
		return this.saturatedTypes;
	}
	
	/* Helpers */
	/* ------- */
	private void addDecision(
			SuperGraph sg, MaxCostFlow<CFGNode,CFGEdge> maxCostFlow, Entry<SymbolicAddress, Map<CFGNode, Integer>> accessEntry,
			long varCost) {
		SymbolicAddress ref = accessEntry.getKey();
		Map<CFGNode, Integer> accessSet = accessEntry.getValue();
		DecisionVariable dvar = maxCostFlow.createDecisionVariable();				
		decisionVariables .put(dvar,ref);
		maxCostFlow.addDecisionCost(dvar, varCost);
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

	private void addAccessSite(
			Map<SymbolicAddress, Map<CFGNode, Integer>> accessSets,
			SymbolicAddress ref, 
			CFGNode n) {
		
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

	/**
	 * @return the index of the field accessed by the instruction, or 0 if the instruction
	 * does not access a field
	 */
	private static int getFieldIndex(Project p, ControlFlowGraph cfg, InstructionHandle ih) {
		ConstantPoolGen constPool = cfg.getMethodInfo().getConstantPoolGen();
		Instruction instr = ih.getInstruction();
		if(instr instanceof FieldInstruction) {
			FieldInstruction fieldInstr = (FieldInstruction) instr;
			String klassName = fieldInstr.getClassName(constPool);
			String fieldName = fieldInstr.getFieldName(constPool);
			return p.getLinkerInfo().getFieldIndex(klassName, fieldName);
		} else {
			return 0;			
		}
	}

	/**
	 * @param maxCachedFieldIndex 
	 * @return whether the field accessed by the given instruction handle is cached
	 */
	public static boolean isFieldCached(ControlFlowGraph cfg, InstructionHandle ih, int maxCachedFieldIndex) {
		Project p = cfg.getAppInfo().getProject();
		int index = ObjectRefAnalysis.getFieldIndex(p, cfg,ih);
		
		/* Uncached fields are treated separately */
		return (index <= maxCachedFieldIndex);
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
			//ArrayInstruction ainstr = (ArrayInstruction) instr;
			return "[]";
		}
		if(instr instanceof ARRAYLENGTH) {
			//ARRAYLENGTH ainstr = (ARRAYLENGTH) instr;
			return "[]";
		}
		if(instr instanceof INVOKEINTERFACE || instr instanceof INVOKEVIRTUAL)
		{
			return "$header";
		}
		return null;
	}


}
