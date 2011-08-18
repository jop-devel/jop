/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010-2011, Benedikt Huber (benedikt.huber@gmail.com)
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

import static com.jopdesign.common.misc.MiscUtils.addToSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lpsolve.LpSolveException;

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

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.BasicBlock;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.Segment;
import com.jopdesign.common.code.SuperGraph;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphNode;
import com.jopdesign.common.misc.Iterators;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.dfa.DFATool;
import com.jopdesign.dfa.analyses.SymbolicAddress;
import com.jopdesign.dfa.analyses.SymbolicPointsTo;
import com.jopdesign.dfa.framework.BoundedSetFactory.BoundedSet;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.analysis.InvalidFlowFactException;
import com.jopdesign.wcet.analysis.cache.ObjectCacheAnalysisDemo.ObjectCacheCost;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETSolver;

/** Analysis of the used object references.
 *  Goal: Detect persistence scopes.
 *  
 *  This is the current consensus:
 *  <ul><li/>One cache line per object
 * <li/> Only consider getfield. putfield does not modify cache
 * <li/> Handle access should be configurable (HANDLE_ACCESS = false or true)
 * </ul>
 * For the general technique, see {@link MethodCacheAnalysis#countDistinctCacheBlocks()}
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
	
	private static final String KEY = ObjectRefAnalysis.class.getCanonicalName();

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


	/**
	 * Purpose: This class encapsulates the results from the object reference DFA 
	 */
	public static class LocalPointsToResult {
		private HashMap<InstructionHandle, ContextMap<CallString, BoundedSet<SymbolicAddress>>> pointsTo;

		private LocalPointsToResult(HashMap<InstructionHandle, ContextMap<CallString, BoundedSet<SymbolicAddress>>> pTo) {
			pointsTo = pTo;
		}
		
		public BoundedSet<SymbolicAddress> get(InstructionHandle ih, CallString cs) {
			ContextMap<CallString, BoundedSet<SymbolicAddress>> csmap = pointsTo.get(ih);
			if(csmap != null) {
				return csmap.get(cs);
			} else {
				return null;
			}
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
	
	public static class AccessCostInfo {
		
		HashMap<SymbolicAddress, Map<SuperGraphNode, Integer>> refAccessSets;
		HashMap<SymbolicAddress, Map<SuperGraphNode, Integer>> blockAccessSets;
		Map<SuperGraphNode,Long> staticCostMap;
		Map<SuperGraphNode,Long> bypassCostMap;

		public AccessCostInfo() {
			refAccessSets = new HashMap<SymbolicAddress,Map<SuperGraphNode, Integer>>();
			blockAccessSets = new HashMap<SymbolicAddress,Map<SuperGraphNode, Integer>>();
			staticCostMap = new HashMap<SuperGraphNode, Long>();
			bypassCostMap = new HashMap<SuperGraphNode, Long>();
		}

		/**
		 * @param node
		 * @return the bypass cost for executing the given node once
		 */
		public long getBypassCost(SuperGraphNode node) {

			return bypassCostMap.get(node);
		}

		/**
		 * @param node
		 * @return
		 */
		public long getMissCost(SuperGraphNode node) {
			
			return staticCostMap.get(node) - getBypassCost(node);
		}

		/**
		 * @return set of all referenced object names
		 */
		public Set<SymbolicAddress> getReferencedObjectNames() {

			return refAccessSets.keySet();
		}

		/**
		 * @return set of all reference blocks for object names
		 */
		public Set<SymbolicAddress> getReferencedBlocks() {

			return blockAccessSets.keySet();
		}

		public void addRefAccess(SymbolicAddress ref, SuperGraphNode node) {

			addAccessSite(refAccessSets, ref, node);
		}

		public void addBlockAccess(SymbolicAddress ref, SuperGraphNode node) {
			
			addAccessSite(blockAccessSets, ref, node);
		}

		public void putBypassCost(SuperGraphNode node, long bypassCost) {

			bypassCostMap.put(node,bypassCost);			
		}

		public void putStaticCost(SuperGraphNode node, long staticCost) {
			
			staticCostMap.put(node, staticCost);
		}

		private void addAccessSite(
				Map<SymbolicAddress, Map<SuperGraphNode, Integer>> accessSets,
				SymbolicAddress ref, 
				SuperGraphNode n) {
			
			Map<SuperGraphNode, Integer> accessSet = accessSets.get(ref);
			if(accessSet == null) {
				accessSet = new HashMap<SuperGraphNode, Integer>();
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
		 * @param segment 
		 * @return
		 */
		public 	Set<Entry<SymbolicAddress, List<SuperGraphEdge>>> getRefAccesses(Segment segment) {
			
			return accessEdges(segment, refAccessSets).entrySet();
		}

		/**
		 * @param segment
		 * @return
		 */
		public Set<Entry<SymbolicAddress, List<SuperGraphEdge>>> getBlockAccesses(
				Segment segment) {

			return accessEdges(segment, blockAccessSets).entrySet();
		}

		/**
		 * Tedious datatype transformation. Tried with iterators, but the result looks reallly horrible.
		 * @param acccessSet
		 * @return
		 */
		private Map<SymbolicAddress, List<SuperGraphEdge>> accessEdges(
				Segment segment, HashMap<SymbolicAddress, Map<SuperGraphNode, Integer>> accessSet) {

			Map<SymbolicAddress, List<SuperGraphEdge>> accessesByAddress = 
					new HashMap<SymbolicAddress, List<SuperGraphEdge>>();
			ArrayList<SuperGraphEdge> accessEdges;
			for(Entry<SymbolicAddress, Map<SuperGraphNode, Integer>> accesses : accessSet.entrySet()) {
				
				accessEdges = new ArrayList<SuperGraphEdge>();
				accessesByAddress.put(accesses.getKey(), accessEdges);
				for( Entry<SuperGraphNode, Integer> access : accesses.getValue().entrySet()) {
					Iterators.addAll(accessEdges, segment.incomingEdgesOf(access.getKey()));
				}
			}
			return accessesByAddress;
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
	@SuppressWarnings("unused")
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
				project.getProjectConfig().callstringLength(), 
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
	public HashSet<String> getSaturatedTypes(Segment segment, LocalPointsToResult usedRefs) {
		
		HashSet<String> topTypes = new HashSet<String>();
		
		for(SuperGraphNode n : segment.getNodes()) {			
		
			BasicBlock bb = n.getCFGNode().getBasicBlock();
			if(bb == null) continue;
			CallString cs = n.getContextCFG().getCallString();
			for(InstructionHandle ih : bb.getInstructions()) {
				BoundedSet<SymbolicAddress> refs;
				if(usedRefs.containsKey(ih)) {
					refs = usedRefs.get(ih,cs);
					String handleType = getHandleType(project, n.getCfg(), ih);
					if(handleType == null) continue;
					if(refs.isSaturated()) {
						topTypes.add(handleType);
					}
				}
			}
		}
		return topTypes;
	}

	/**
	 * return number of distinct tags which might be cached in the given scope
	 * FIXME: use segment instead of scope
	 * @param scope
	 * @return
	 * @throws InvalidFlowFactException 
	 * @throws LpSolveException 
	 */
	public long getMaxCachedTags(ExecutionContext scope) throws InvalidFlowFactException, LpSolveException {
		
		Long maxCachedTags = this.maxCachedTagsAccessed.get(scope);
		if(maxCachedTags != null) return maxCachedTags;

		LocalPointsToResult usedRefs = getUsedRefs(scope);

		/* Create an analysis segment */
		Segment segment = Segment.methodSegment(scope.getMethodInfo(), scope.getCallString(), 
				project, project.getProjectConfig().callstringLength(), project);
		
		this.saturatedTypes.put(scope, getSaturatedTypes(segment,usedRefs));

		/* Compute worst-case number of objects/fields accessed */
		HashSet<SymbolicAddress> usedObjectsSet = new HashSet<SymbolicAddress>();
		ObjectCacheCostModel costModel;
		if(this.fieldAsTag) {
			costModel = ObjectCacheCostModel.COUNT_FIELD_TAGS;			
		} else {
			costModel = ObjectCacheCostModel.COUNT_REF_TAGS;			
		}
		maxCachedTags = computeCacheCost(segment, usedRefs, costModel, usedObjectsSet).getCost();
		
		this.tagSet.put(scope, usedObjectsSet);

		maxCachedTagsAccessed.put(scope,maxCachedTags);
		return maxCachedTags;
	} 
	
	/**
	 * Get maximum cost due to the object cache in the given scope.
	 * Using special cost models such as COUNT_FIELD_TAGS and COUNT_REF_TAGS, this
	 * method can be used to calculate different metrics as well.
	 * FIXME: Use segment instead of scope
	 * @param scope
	 * @param costModel The object cache cost model
	 * @return
	 * @throws InvalidFlowFactException 
	 * @throws LpSolveException 
	 */
	public ObjectCacheCost getMaxCacheCost(ExecutionContext scope, ObjectCacheCostModel costModel)
			throws InvalidFlowFactException, LpSolveException {
		
		LocalPointsToResult usedRefs = getUsedRefs(scope);
		Segment segment = Segment.methodSegment(scope.getMethodInfo(), scope.getCallString(), 
				project, project.getProjectConfig().callstringLength(), project);
		/* Compute worst-case cost */
		HashSet<SymbolicAddress> usedObjectsSet = new HashSet<SymbolicAddress>();
        return computeCacheCost(segment, usedRefs, costModel, usedObjectsSet);
	}

	/**
	 * Compute object cache cost for the given segment
	 * @param segment the segment to consider
	 * @param usedRefs the results from the DFA analysis
	 * @param costModel The object cost model to use
	 * @param usedSetOut <b>out</b> set of all symbolic objects names in the segment (pass in empty set)
	 * @return the object cache cost for the specified segment
	 * @throws InvalidFlowFactException 
	 * @throws LpSolveException 
	 */
	private ObjectCacheCost computeCacheCost(Segment segment, 
								  LocalPointsToResult usedRefs, 
								  ObjectCacheCostModel costModel,
								  HashSet<SymbolicAddress> usedSetOut)
										  throws InvalidFlowFactException, LpSolveException {

		AccessCostInfo accessCostInfo = extractAccessesAndCosts(segment, usedRefs, costModel);

		/* create an ILP graph for all reachable methods */
		String key = KEY+segment.toString();
		
		IPETSolver<SuperGraphEdge> ipetSolver = GlobalAnalysis.buildIpetProblem(project, key, segment, new IPETConfig(project.getConfig()));

		/* We have to add miss edges for each incoming edge of the node, which are constrained by the corresponding supergraph edge frequency */
		
		/* for references */
		Set<SuperGraphEdge> refMissEdges = 
				CachePersistenceAnalysis.addPersistenceSegmentConstraints(segment, accessCostInfo.getRefAccesses(segment), 
						ipetSolver, MiscUtils.<SuperGraphEdge,Long>const1(costModel.getReplaceLineCost()), KEY+"_ref");

		/* and for blocks */
		Set<SuperGraphEdge> blockMissEdges = 
				CachePersistenceAnalysis.addPersistenceSegmentConstraints(segment, accessCostInfo.getBlockAccesses(segment), 
						ipetSolver, MiscUtils.<SuperGraphEdge,Long>const1(costModel.getLoadCacheBlockCost()), KEY+"_block");

		

		/* solve */
		double lpCost;
		Map<SuperGraphEdge, Long> flowMap = new HashMap<SuperGraphEdge,Long>();
		lpCost = ipetSolver.solve(flowMap ,true);
		long cost = (long) (lpCost+0.5);

		return extractCost(segment, cost,flowMap, accessCostInfo, costModel,refMissEdges,blockMissEdges);
	}

	/** Traverse vertex set.
	 * <p>Add vertex to access set of referenced addresses
	 * For references whose type cannot be fully resolved, add a
	 * cost of 1.</p>
	 * FIXME: We should deal with subtyping (or better use storage based alias-analysis)
	 *
	 * @param segment
	 * @param usedRefs
	 * @param costModel
	 */
	private AccessCostInfo extractAccessesAndCosts(
			Segment segment,
			LocalPointsToResult usedRefs,
			ObjectCacheCostModel costModel) {
		
		AccessCostInfo aci = new AccessCostInfo();
		
		for(SuperGraphNode node : segment.getNodes()) {
			/* Compute cost for basic block */
			BasicBlock bb = node.getCFGNode().getBasicBlock();
			if(bb == null) continue;
			long bypassCost = 0;
			long alwaysMissCost = 0;
			CallString cs = node.getContextCFG().getCallString();

			for(InstructionHandle ih : bb.getInstructions()) {
				BoundedSet<SymbolicAddress> refs;

				String handleType = getHandleType(project, node.getCfg(), ih); 				
				if(handleType == null) continue; /* No getfield/handle access */
				if(usedRefs.containsKey(ih)) {					

					int fieldIndex = getFieldIndex(project, node.getCfg(), ih);
					int blockIndex = getBlockIndex(fieldIndex);
					
					if(fieldIndex > this.maxCachedFieldIndex) {
						bypassCost += costModel.getFieldAccessCostBypass();
						continue;
					}
										
					refs = usedRefs.get(ih,cs);		
					if(refs.isSaturated()) {
						alwaysMissCost += costModel.getReplaceLineCost() + costModel.getLoadCacheBlockCost();
					} else {
						for(SymbolicAddress ref : refs.getSet()) {
							aci.addRefAccess(ref,node);
							aci.addBlockAccess(ref.accessArray(blockIndex), node);
						}
					}
				} else {
					WCETTool.logger.error("No DFA results for: "+ih.getInstruction() + " with field " + ((FieldInstruction)ih.getInstruction()).getFieldName(bb.cpg()));
				}
			}
			aci.putBypassCost(node, bypassCost);
			aci.putStaticCost(node, bypassCost + alwaysMissCost);
		}
		return aci;
	}

	/**
	 * 
	 * @param segment the segment analyzed
	 * @param lpCost the lp objective value
	 * @param flowMap the lp assignment for variables
	 * @param accessCostInfo information on object cache cost edges
	 * @param costModel the object cache cost model
	 * @param refMissEdges the object cache cost edges for references
	 * @param blockMissEdges the object cache cost edges for blocks
	 * @return
	 */
	private ObjectCacheCost extractCost(Segment segment,
			long lpCost,
			Map<SuperGraphEdge, Long> flowMap,
			AccessCostInfo accessCostInfo,
			ObjectCacheCostModel costModel,
			Set<SuperGraphEdge> refMissEdges,
			Set<SuperGraphEdge> blockMissEdges) {

		long missCount = 0;        /* miss count */
		long totalMissCost = 0;    /* has to be equal to (cost - bypass cost) */
		long bypassAccesses = 0;   /* bypassed fields accesses */
		long fieldAccesses = 0;    /* cached fields accessed */
		long totalBypassCost = 0;  /* All object accesses * bypass cost */

		for(SuperGraphEdge edge : segment.getEdges()) {
			long edgeFreq = flowMap.get(edge);
			SuperGraphNode node = edge.getTarget();

			/* Compute cost for basic block */
			BasicBlock bb = node.getCFGNode().getBasicBlock();
			if(bb == null) continue;

			long missCost = accessCostInfo.getMissCost(node)   * edgeFreq;
			totalMissCost   += missCost;
			totalBypassCost += accessCostInfo.getBypassCost(node) * edgeFreq;

			/* Calculate number of unpredictable always-miss accesses, and record them */
			long alwaysMissCost = costModel.getReplaceLineCost() + costModel.getLoadCacheBlockCost();
			missCount  +=  missCost / alwaysMissCost;
			
			/* count normal and bypass accesses in the basic block */
			for(InstructionHandle ih : bb.getInstructions()) {
				
				String handleType = getHandleType(project, node.getCfg(), ih); 				
				if(handleType == null) continue; /* No getfield/handle access */					
				if(! isFieldCached(project, node.getCfg(), ih, maxCachedFieldIndex)) {
					bypassAccesses += edgeFreq;
				} else {
					fieldAccesses  += edgeFreq; 
				}
			}
		}
		
		/* For each miss edge, there is an associated cost; moreover
		 * fill-word & single-field: missCount = sum of miss block variables
		 * fill-line: missCount = sum of miss object reference variables
		 */
		
		long totalRefMisses = 0;
		for(SuperGraphEdge refMissEdge : refMissEdges) {
			totalRefMisses += flowMap.get(refMissEdge);
		}
		totalMissCost += costModel.getReplaceLineCost() * totalRefMisses;

		long totalBlockMisses = 0;
		for(SuperGraphEdge blockMissEdge : blockMissEdges) {
			totalBlockMisses += flowMap.get(blockMissEdge);
		}
		totalMissCost += costModel.getLoadCacheBlockCost() * totalBlockMisses;
		missCount += totalBlockMisses;

		if(totalMissCost != lpCost - totalBypassCost) {
			throw new AssertionError(
					String.format("Error in calculating missCost in all fit-area (misscount = %d): %d but should be %d (%d - %d)",
							missCount, totalMissCost,lpCost-totalBypassCost,lpCost,totalBypassCost));
		}

		ObjectCacheCost ocCost = new ObjectCacheCost(missCount, totalMissCost,bypassAccesses, totalBypassCost, fieldAccesses);
		return ocCost;
	}


	/** Get all access sites per method */
	public static Map<MethodInfo, Set<SuperGraph.SuperGraphEdge>> getAccessEdges(SuperGraph sg) {
		Map<MethodInfo, Set<SuperGraph.SuperGraphEdge>> accessEdges =
			new HashMap<MethodInfo, Set<SuperGraph.SuperGraphEdge>>();
		for(Entry<SuperGraph.SuperInvokeEdge, SuperGraph.SuperReturnEdge> invokeSite: sg.getSuperEdgePairs().entrySet()) {
			MethodInfo invoked = invokeSite.getKey().getInvokeNode().receiverFlowGraph().getMethodInfo();
			addToSet(accessEdges, invoked, invokeSite.getKey());
			MethodInfo invoker = invokeSite.getKey().getInvokeNode().invokerFlowGraph().getMethodInfo();
			addToSet(accessEdges, invoker, invokeSite.getValue());
		}
		return accessEdges;
	}
		
	public Set<SymbolicAddress> getUsedSymbolicNames(ExecutionContext scope) throws InvalidFlowFactException, LpSolveException {
		if(! tagSet.containsKey(scope)) getMaxCachedTags(scope);
		return tagSet.get(scope);		
	}
	

	public Object getSaturatedTypes(ExecutionContext scope) throws InvalidFlowFactException, LpSolveException {
		if(! this.saturatedTypes.containsKey(scope)) getMaxCachedTags(scope);
		return this.saturatedTypes.get(scope);
	}
	
	/* Helpers */
	/* ------- */

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

	public static String getHandleType(WCETTool project, ControlFlowGraph cfg, InstructionHandle ih) {
		ConstantPoolGen constPool = cfg.getMethodInfo().getConstantPoolGen();
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
