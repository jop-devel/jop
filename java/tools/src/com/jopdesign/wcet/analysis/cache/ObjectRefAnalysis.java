/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.wcet.analysis.cache;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.BasicBlock;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.SuperGraph;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.dfa.DFATool;
import com.jopdesign.dfa.analyses.SymbolicAddress;
import com.jopdesign.dfa.analyses.SymbolicPointsTo;
import com.jopdesign.dfa.framework.BoundedSetFactory.BoundedSet;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.analysis.cache.ObjectCacheAnalysisDemo.ObjectCacheCost;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETSolver;
import com.jopdesign.wcet.ipet.MaxCostFlow;
import com.jopdesign.wcet.ipet.MaxCostFlow.DecisionVariable;
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
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.jopdesign.common.misc.MiscUtils.addToSet;

/** Analysis of the used object references.
 *  Goal: Detect persistence scopes.
 *  
 *  This is the current consensus:
 *  <ul><li/>One cache line per object
 * <li/> Only consider getfield. putfield does not modify cache
 * <li/> Handle access should be configurable (HANDLE_ACCESS = false or true)
 * </ul>
 * For the general technique, see {@link MethodCacheAnalysis#analyzeBlockUsage()}
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
		private long loadCacheBlockCost;
		private long replaceLineCost;
		private long fieldAccessCostBypass;

		public ObjectCacheCostModel(long loadCacheBlockCost, long replaceLineCost, long fieldAccessCostBypass)
		{
			this.loadCacheBlockCost = loadCacheBlockCost;
			this.replaceLineCost = replaceLineCost;
			this.fieldAccessCostBypass = fieldAccessCostBypass;
		}
		/**
		 * @return the loadFieldCost
		 */
		public long getCacheBlockCost() {
			return loadCacheBlockCost;
		}

		/**
		 * @return the loadCacheLineCost
		 */
		public long getReplaceLineCost() {
			return replaceLineCost;
		}

		/**
		 * @return the fieldAccessCostBypass
		 */
		public long getFieldAccessCostBypass() {
			return fieldAccessCostBypass;
		}
		/**
		 * @return
		 */
		public long getLoadCacheBlockCost() {
			return this.loadCacheBlockCost;
		}
	}


	/* transparent class for hiding the type of DFA result */
	public static class LocalPointsToResult {
		private HashMap<InstructionHandle, ContextMap<CallString, BoundedSet<SymbolicAddress>>> pointsTo;
		private HashMap<InstructionHandle, BoundedSet<SymbolicAddress>> pointsToNoCallString;

		private LocalPointsToResult(HashMap<InstructionHandle, ContextMap<CallString, BoundedSet<SymbolicAddress>>> pTo) {
			pointsTo = pTo;
			pointsToNoCallString = new HashMap<InstructionHandle, BoundedSet<SymbolicAddress>>();
		}
		
		public BoundedSet<SymbolicAddress> get(InstructionHandle ih) {
			BoundedSet<SymbolicAddress> addrs = pointsToNoCallString.get(ih);
			if(addrs == null) {
				for(BoundedSet<SymbolicAddress> addrs2 : pointsTo.get(ih).values()) {
					if(addrs == null) addrs = addrs2.newBoundedSet();
					addrs.addAll(addrs2);
				}
				pointsToNoCallString.put(ih, addrs);
			}
			return addrs;
		}

		public boolean containsKey(InstructionHandle ih) {
			return pointsTo.containsKey(ih);
		}

		public Set<SymbolicAddress> getAddressSet() {
			Set<SymbolicAddress> addressSet = new HashSet<SymbolicAddress>();
			for(ContextMap<CallString, BoundedSet<SymbolicAddress>> entry : pointsTo.values()) {
				for(BoundedSet<SymbolicAddress> aset : entry.values()) {
					if(! aset.isSaturated()) addressSet.addAll(aset.getSet());
				}
			}
			return addressSet;
		}
		
	}

	/* class for checking whether a basic block is executed as most once in a scope */
	private class ExecOnceQuery implements MiscUtils.Query<InstructionHandle> {
		private ExecuteOnceAnalysis eoAna;
		private ExecutionContext scope;
		public ExecOnceQuery(ExecuteOnceAnalysis eoAnalysis, ExecutionContext scope) {
		    this.eoAna = eoAnalysis;
		    this.scope = scope;
		}
		public boolean query(InstructionHandle a) {
                    ControlFlowGraph cfg = project.getFlowGraph(scope.getMethodInfo());
		    CFGNode n = cfg.getHandleNode(a);
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

	/* Size of a cache block */
	private int blockSize;

	/* ld(blockSize) */
	private int blockIndexBits;

	/* Whether to use a 'single field' cache, i.e., use fields as tags */
	private boolean fieldAsTag;

	/* Maximum number of objects tracked for one reference */
	private int maxSetSize;

	/* Those type which have an unbounded number of objects in the given scope
	 * FIXME: maybe we should switch to allocation sites (more precise, no subtyping) */
	private Map<ExecutionContext, Set<String>> saturatedTypes;

	/* Maximum number of objects which are loaded into the object cache (at least one field has to be accessed) */
	private Map<ExecutionContext, Long> maxCachedTagsAccessed;
	
	/* The set of symbolic object names loaded into the cache (without saturated references) */
	private Map<ExecutionContext, Set<SymbolicAddress>> tagSet;


	private WCETTool project;
	private Map<DecisionVariable, SymbolicAddress> decisionVariables;
	private Set<DecisionVariable> refDecisions;
	
	public ObjectRefAnalysis(WCETTool p,  boolean fieldAsTag, int blockSize, int maxCachedIndex, int setSize) {
		this.project = p;
		this.fieldAsTag = fieldAsTag;
		this.blockSize = blockSize;
		this.maxSetSize = setSize;
		this.maxCachedFieldIndex = maxCachedIndex;

		this.blockIndexBits = 0; 
		for(int i = 1; i < blockSize; i<<=1) {
			blockIndexBits++;
		}

		saturatedTypes = new HashMap<ExecutionContext, Set<String>>();
		maxCachedTagsAccessed = new HashMap<ExecutionContext, Long>();
		tagSet = new HashMap<ExecutionContext, Set<SymbolicAddress>>();
	}
	
	public LocalPointsToResult getUsedRefs(ExecutionContext scope) {
		ExecuteOnceAnalysis eoAna = new ExecuteOnceAnalysis(project);
		DFATool dfa = project.getDfaTool();
		SymbolicPointsTo spt = new SymbolicPointsTo(maxSetSize,
				(int)project.getProjectConfig().callstringLength(), 
				new ExecOnceQuery(eoAna,scope));
		dfa.runLocalAnalysis(spt,scope.getMethodInfo());
		LocalPointsToResult lpt = new LocalPointsToResult(spt.getResult());
		return lpt;
	}
	
	public Set<SymbolicAddress> getAddressSet(ExecutionContext scope) {
		LocalPointsToResult lpt = getUsedRefs(scope);
		return lpt.getAddressSet();
	}
	
	/** Traverse vertex set. Collect those types where we could not resolve
	 * the symbolic object names. (Not too useful in the analysis, but useful
	 * for debugging)
	 */
	public HashSet<String> getSaturatedTypes(SuperGraph sg, LocalPointsToResult usedRefs) {
		HashSet<String> topTypes =
			new HashSet<String>(); 
		for(CFGNode n : sg.allCFGNodes()) {
			BasicBlock bb = n.getBasicBlock();
			if(bb == null) continue;
			for(InstructionHandle ih : bb.getInstructions()) {
				BoundedSet<SymbolicAddress> refs;
				if(usedRefs.containsKey(ih)) {
					refs = usedRefs.get(ih);
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

	public long getMaxCachedTags(ExecutionContext scope)
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
		maxCachedTags = computeCacheCost(scope, sg, usedRefs, usedObjectsSet, costModel).getCost();
		
		this.tagSet.put(scope, usedObjectsSet);

		maxCachedTagsAccessed.put(scope,maxCachedTags);
		return maxCachedTags;
	} 
	
	public ObjectCacheCost getMaxCacheCost(ExecutionContext scope, ObjectCacheCostModel costModel)
	{
		LocalPointsToResult usedRefs = getUsedRefs(scope);
		SuperGraph sg = getScopeSuperGraph(scope);
		/* Compute worst-case cost */
		HashSet<SymbolicAddress> usedObjectsSet = new HashSet<SymbolicAddress>();
        return computeCacheCost(scope, sg, usedRefs, usedObjectsSet, costModel);
	}

	private ObjectCacheCost computeCacheCost(ExecutionContext scope,
							      SuperGraph sg, 
								  LocalPointsToResult usedRefs, 
								  HashSet<SymbolicAddress> usedSetOut,
								  ObjectCacheCostModel costModel)
	{
		CallString emptyCallString = CallString.EMPTY;

		HashMap<SymbolicAddress, Map<CFGNode, Integer>> refAccessSets =
			new HashMap<SymbolicAddress,Map<CFGNode, Integer>>();
		
		HashMap<SymbolicAddress, Map<CFGNode, Integer>> blockAccessSets =
			new HashMap<SymbolicAddress,Map<CFGNode, Integer>>();
		
		Map<CFGNode,Long> costMap = new HashMap<CFGNode, Long>();
		Map<CFGNode,Long> bypassCostMap = new HashMap<CFGNode, Long>();

		/* Traverse vertex set.
		 * Add vertex to access set of referenced addresses
		 * For references whose type cannot be fully resolved, at a
		 * cost of 1.
		 */
		// FIXME: We should deal with subtyping (or better use storage based alias-analysis)
		for(CFGNode node : sg.allCFGNodes()) {
			/* Compute cost for basic block */
			BasicBlock bb = node.getBasicBlock();
			if(bb == null) continue;
			long bypassCost = 0;
			long alwaysMissCost = 0;

			for(InstructionHandle ih : bb.getInstructions()) {
				BoundedSet<SymbolicAddress> refs;

				String handleType = getHandleType(project, node, ih); 				
				if(handleType == null) continue; /* No getfield/handle access */
				
				if(usedRefs.containsKey(ih)) {					
					String fieldName = ((FieldInstruction)ih.getInstruction()).getFieldName(bb.cpg());
					int fieldIndex = getFieldIndex(project, node.getControlFlowGraph(), ih);
					int blockIndex = getBlockIndex(fieldIndex);
					
					if(fieldIndex > this.maxCachedFieldIndex) {
						bypassCost += costModel.getFieldAccessCostBypass();
						continue;
					}
										
					refs = usedRefs.get(ih);						
					if(refs.isSaturated()) {
						alwaysMissCost += costModel.getReplaceLineCost() + costModel.getLoadCacheBlockCost();
					} else {
						for(SymbolicAddress ref : refs.getSet()) {
							addAccessSite(refAccessSets, ref, node);
							addAccessSite(blockAccessSets, ref.accessArray(blockIndex), node);
						}
					}
				} else {
					System.err.println("No DFA results for: "+ih.getInstruction() + " with field " + ((FieldInstruction)ih.getInstruction()).getFieldName(bb.cpg()));
				}
			}
			bypassCostMap.put(node,bypassCost);
			costMap.put(node,bypassCost + alwaysMissCost);
		}
		/* create an ILP graph for all reachable methods */
		String key = "object_ref_analysis:"+scope.toString();
		
		IPETSolver ipet = GlobalAnalysis.buildIpetProblem(project, key, sg, new IPETConfig(project.getConfig()));

		throw new AssertionError("TODO");
//		/* We have to add add split edges for each node, such that {@code e_miss + e_hit = sum e_incoming} */
//		for(Entry<SymbolicAddress, Map<CFGNode, Integer>> accessEntry : refAccessSets.entrySet()) {
//			long cost = costModel.getReplaceLineCost();
//			List<ExecutionEdge> = ipet.addSplitEdges(sumIncoming,2);
//			addDecision(sg, maxCostFlow, accessEntry, costModel.getReplaceLineCost(),true);
//		}
//		
//		
//		/* Add decision variables for all tags */		
//		for(Entry<SymbolicAddress, Map<CFGNode, Integer>> accessEntry : blockAccessSets.entrySet()) {
//			addDecision(sg, maxCostFlow, accessEntry, costModel.getLoadCacheBlockCost(), false);
//		}
//
//		/* solve */
//		Map<CFGEdge, Long> flowMap = new HashMap<CFGEdge, Long>();
//		Map<DecisionVariable, Boolean> refUseMap = new HashMap<DecisionVariable, Boolean>();
//		double lpCost;
//		try {
//			lpCost = maxCostFlow.solve(flowMap,refUseMap);
//		} catch (Exception e) {
//			Logger.getLogger(ObjectRefAnalysis.class).error("Failed to compute object cache cost for : "+scope);
//			lpCost = 2000000000.0;
//		}
//		long cost = (long) (lpCost+0.5);
//		
//		/* extract solution */
//		for(Entry<DecisionVariable, SymbolicAddress> entry : this.decisionVariables.entrySet()) {
//			DecisionVariable dvar = entry.getKey();
//			if(refUseMap.containsKey(dvar) && refUseMap.get(dvar)) {
//				SymbolicAddress addr = entry.getValue();
//				usedSetOut.add(addr);
//				//System.out.println("Used dvar: "+addr);
//			}
//		}
//		return extractCost(sg,costModel,cost,costMap,bypassCostMap,flowMap,refUseMap);
	}

	private ObjectCacheCost extractCost(SuperGraph sg,
			ObjectCacheCostModel costModel,
			long cost,
			Map<CFGNode, Long> costMap,
			Map<CFGNode, Long> bypassCostMap,
			Map<ControlFlowGraph.CFGEdge, Long> edgeFlowMap,
			Map<DecisionVariable, Boolean> refUseMap) {
		throw new AssertionError("TODO");
//		long missCount = 0;  /* miss count */
//		long missCost = 0;   /* has to be equal to (cost - bypass cost) */
//		long bypassAccesses = 0; /* bypassed fields accesses */
//		long fieldAccesses = 0; /* cached fields accessed */
//		long bypassCost = 0; /* All object accesses * bypass cost */
//
//		Map<CFGNode, Long> freqMap = RecursiveWcetAnalysis.edgeToNodeFlow(sg, edgeFlowMap);
//		
//		for(CFGNode node : freqMap.keySet()) {
//			/* Compute cost for basic block */
//			Long nodeFreq = freqMap.get(node);
//			BasicBlock bb = node.getBasicBlock();
//			if(bb == null) continue;
//
//			long bbBypassCost = bypassCostMap.get(node);
//			bypassCost += bbBypassCost * nodeFreq;
//			missCost   += (costMap.get(node) - bbBypassCost) * nodeFreq;
//			/* HACK */
//			long amCost = costModel.getReplaceLineCost() + costModel.getLoadCacheBlockCost();
//			missCount  +=  ((costMap.get(node) - bbBypassCost) * nodeFreq) / amCost;
//			for(InstructionHandle ih : bb.getInstructions()) {
//				String handleType = getHandleType(project, node, ih); 				
//				if(handleType == null) continue; /* No getfield/handle access */					
//				if(! isFieldCached(node.getControlFlowGraph(), ih, maxCachedFieldIndex)) {
//					bypassAccesses += nodeFreq;
//				} else {
//					fieldAccesses += nodeFreq; 
//				}
//			}
//		}
//		/* for each decision variable, there is an associated cost; moreover:
//		 * fill-word & single-field: missCount = field decision variables which are true
//		 * fill-line: missCount = ref   decision variables which are true
//		 */
//		for(Entry<DecisionVariable, Boolean> entry : refUseMap.entrySet()) {
//			DecisionVariable dvar = entry.getKey();
//			if(! entry.getValue()) continue;
//			if(refDecisions.contains(dvar)) {
//				missCost += costModel.getReplaceLineCost();
//			} else {
//				missCost += costModel.getLoadCacheBlockCost();
//				missCount += 1;
//			}
//		}
//		if(missCost != cost - bypassCost) {
//			throw new AssertionError(
//					String.format("Error in calculating missCost in all fit-area (misscount = %d): %d but should be %d (%d - %d)",
//							missCount, missCost,cost-bypassCost,cost,bypassCost));
//		}
//
//		ObjectCacheCost ocCost = new ObjectCacheCost(missCount, missCost,bypassAccesses, bypassCost, fieldAccesses);
//		return ocCost;
	}


	/** Get all access sites per method */
	public static Map<MethodInfo, Set<ControlFlowGraph.CFGEdge>> getAccessEdges(SuperGraph sg) {
		Map<MethodInfo, Set<ControlFlowGraph.CFGEdge>> accessEdges =
			new HashMap<MethodInfo, Set<ControlFlowGraph.CFGEdge>>();
		for(Entry<SuperGraph.SuperInvokeEdge, SuperGraph.SuperReturnEdge> invokeSite: sg.getSuperEdgePairs().entrySet()) {
			MethodInfo invoked = invokeSite.getKey().getInvokeNode().receiverFlowGraph().getMethodInfo();
			addToSet(accessEdges, invoked, invokeSite.getKey());
			MethodInfo invoker = invokeSite.getKey().getInvokeNode().invokerFlowGraph().getMethodInfo();
			addToSet(accessEdges, invoker, invokeSite.getValue());
		}
		return accessEdges;
	}

	
	private SuperGraph getScopeSuperGraph(ExecutionContext scope) {
		MethodInfo m = scope.getMethodInfo();
		return new SuperGraph(project.getAppInfo(),project.getFlowGraph(m), project.getProjectConfig().callstringLength());
	}
		
	public Set<SymbolicAddress> getUsedSymbolicNames(ExecutionContext scope) {
		if(! tagSet.containsKey(scope)) getMaxCachedTags(scope);
		return tagSet.get(scope);		
	}
	

	public Object getSaturatedTypes(ExecutionContext scope) {
		if(! this.saturatedTypes.containsKey(scope)) getMaxCachedTags(scope);
		return this.saturatedTypes.get(scope);
	}
	
	/* Helpers */
	/* ------- */
	private void addDecision(
			SuperGraph sg, MaxCostFlow<CFGNode,ControlFlowGraph.CFGEdge> maxCostFlow, Entry<SymbolicAddress, Map<CFGNode, Integer>> accessEntry,
			long varCost, boolean isRefDecision) {
		throw new AssertionError("TODO");
//		SymbolicAddress refOrField = accessEntry.getKey();
//		Map<CFGNode, Integer> accessSet = accessEntry.getValue();
//		DecisionVariable dvar = maxCostFlow.createDecisionVariable();
//		
//		if(isRefDecision) this.refDecisions.add(dvar);
//		this.decisionVariables.put(dvar,refOrField);
//		
//		maxCostFlow.addDecisionCost(dvar, varCost);
//		/* dvar <= sum(i `in` I) frequency(b_i) */
//		LinearVector<CFGEdge> ub = new LinearVector<CFGEdge>();
//		for(Entry<CFGNode, Integer> entry : accessSet.entrySet()) {
//			CFGNode node = entry.getKey();
//			// we do not really need the count (entry.getValue()) for this constraint
//			for(CFGEdge edge : sg.incomingEdgesOf(node)) {
//				ub.add(edge,1);						
//			}
//		}
//		maxCostFlow.addDecisionUpperBound(dvar, ub);						
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
	private static int getFieldIndex(WCETTool p, ControlFlowGraph cfg, InstructionHandle ih) {
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
	
	private int getBlockIndex(int fieldIndex) {
		return fieldIndex >> blockIndexBits;
	}

	/**
	 * @param maxCachedFieldIndex 
	 * @return whether the field accessed by the given instruction handle is cached
	 */
	public static boolean isFieldCached(WCETTool project, ControlFlowGraph cfg, InstructionHandle ih, int maxCachedFieldIndex) {
		int index = ObjectRefAnalysis.getFieldIndex(project, cfg,ih);
		
		/* Uncached fields are treated separately */ 
		return (index <= maxCachedFieldIndex);
	}

	public static String getHandleType(WCETTool project, CFGNode n, InstructionHandle ih) {
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
