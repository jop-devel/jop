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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
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
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.BasicBlock;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.Segment;
import com.jopdesign.common.code.SuperGraph;
import com.jopdesign.common.code.SuperGraph.ContextCFG;
import com.jopdesign.common.code.SuperGraph.IntraEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphNode;
import com.jopdesign.common.code.SuperGraph.SuperInvokeEdge;
import com.jopdesign.common.code.SuperGraph.SuperReturnEdge;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.misc.Iterators;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.misc.MiscUtils.F1;
import com.jopdesign.dfa.DFATool;
import com.jopdesign.dfa.analyses.SymbolicAddress;
import com.jopdesign.dfa.analyses.SymbolicPointsTo;
import com.jopdesign.dfa.framework.BoundedSetFactory.BoundedSet;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.analysis.InvalidFlowFactException;
import com.jopdesign.wcet.analysis.cache.ObjectCacheAnalysis.AccessCostInfo;
import com.jopdesign.wcet.annotations.BadAnnotationException;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETSolver;
import com.jopdesign.wcet.jop.ObjectCache;
import com.jopdesign.wcet.jop.ObjectCache.ObjectCacheCost;

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
 *  <ul>
 *  <li/>FIXME: [cache-analysis] Handle subtyping when dealing with aliases, or use a store-based approach
 *  </ul>
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class ObjectCacheAnalysis extends CachePersistenceAnalysis {
	
	private static final String KEY = "wcet.ObjectCacheAnalysis";

	/* Only consider getfield (false) or all handle accesses */
	private static boolean ALL_HANDLE_ACCESSES = false; 
		
	
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
		 * @param node
		 * @return 
		 * @return always miss and bypass cost for the node
		 */
		public long getStaticCost(SuperGraphNode node) {
			if(! staticCostMap.containsKey(node)) return 0;
			return staticCostMap.get(node);
		}
		public Map<SuperGraphNode, Long> getStaticCostMap() {
		
			return staticCostMap;
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
		public 	Set<Entry<SymbolicAddress, Set<SuperGraphEdge>>> getRefAccesses(Segment segment) {
			
			return accessEdges(segment, refAccessSets).entrySet();
		}

		/**
		 * @param segment
		 * @return
		 */
		public Set<Entry<SymbolicAddress, Set<SuperGraphEdge>>> getBlockAccesses(
				Segment segment) {

			return accessEdges(segment, blockAccessSets).entrySet();
		}

		/**
		 * Tedious datatype transformation. Tried with iterators, but the result looks reallly horrible.
		 * @param acccessSet
		 * @return
		 */
		private Map<SymbolicAddress, Set<SuperGraphEdge>> accessEdges(
				Segment segment, HashMap<SymbolicAddress, Map<SuperGraphNode, Integer>> accessSet) {

			Map<SymbolicAddress, Set<SuperGraphEdge>> accessesByAddress = 
					new HashMap<SymbolicAddress, Set<SuperGraphEdge>>();
			for(Entry<SymbolicAddress, Map<SuperGraphNode, Integer>> accesses : accessSet.entrySet()) {
				
				Set<SuperGraphEdge> accessEdges = new HashSet<SuperGraphEdge>();
				accessesByAddress.put(accesses.getKey(), accessEdges);
				for( Entry<SuperGraphNode, Integer> access : accesses.getValue().entrySet()) {
					Iterators.addAll(accessEdges, segment.incomingEdgesOf(access.getKey()));
				}
			}
			return accessesByAddress;
		}

		/**
		 * Dump the information to the given stream
		 * @param out a print stream for output
		 */		
		public void dump(PrintStream out, int indentAmount) {

			String indent = (indentAmount>0?String.format("%"+indentAmount+"s",""):"");
			out.println(indent+"Accessed References: ");
			dumpAccessMap(out, indentAmount+2, refAccessSets);
			out.println(indent+"Accessed Blocks: ");
			dumpAccessMap(out, indentAmount+2, blockAccessSets);
			out.println(indent+"Static Cost Map: ");
			dumpCostMap(out, indentAmount+4,staticCostMap);
			out.println(indent+"Bypass Cost Map: ");
			dumpCostMap(out, indentAmount+4,bypassCostMap);
		}

		private void dumpCostMap(PrintStream out, int indentAmount, Map<SuperGraphNode, Long> costMap) {
			for(Entry<SuperGraphNode, Long> costEntry : costMap.entrySet()) {
				if(costEntry.getValue() == 0) continue;
				out.printf("%"+indentAmount+"s%-40s: %d", "", costEntry.getKey(), costEntry.getValue());
			}
		}

		private void dumpAccessMap(PrintStream out, int indentAmount,
				HashMap<SymbolicAddress, Map<SuperGraphNode, Integer>> accessMap) {
			for(Entry<SymbolicAddress, Map<SuperGraphNode, Integer>> entry : accessMap.entrySet()) {
				out.printf("%"+indentAmount+"s%-30s:", "", entry.getKey());
				for(Entry<SuperGraphNode, Integer> node : entry.getValue().entrySet()) {
					out.printf(" %s [%d]",node.getKey(), node.getValue());
				}
				out.println();
			}			
		}

	}
	
	private static class ObjectCacheIPETModel {

		public Set<SuperGraphEdge> staticCostEdges;
		public AccessCostInfo accessCostInfo;
		public Set<SuperGraphEdge> refMissEdges;
		public Set<SuperGraphEdge> blockMissEdges;
		
	}

	/* Whether to use a 'single field' cache, i.e., use fields as tags */
	private boolean fieldAsTag;

	/* The maximum index for cached fields */
	private int maxCachedFieldIndex;

	/* ld(blockSize) */
	private int blockIndexBits;

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
	private ObjectCache objectCache;
	
	public ObjectCacheAnalysis(WCETTool p,  ObjectCache oc) {
		this.project = p;
		this.objectCache = oc;

		this.fieldAsTag = oc.isFieldCache();
		this.maxSetSize = oc.getAssociativity();
		this.maxCachedFieldIndex = oc.getMaxCachedFieldIndex();

		this.blockIndexBits = 0; 
		for(int i = 1; i < oc.getBlockSize(); i<<=1) {
			blockIndexBits++;
		}

		saturatedTypes = new HashMap<ExecutionContext, Set<String>>();
		maxCachedTagsAccessed = new HashMap<ExecutionContext, Long>();
		tagSet = new HashMap<ExecutionContext, Set<SymbolicAddress>>();
	}
	
	/**
	 * Add always miss cost: for each access to the method cache, add cost of access
	 * @param segment
	 * @param ipetSolver
	 */
	@Override
	public Set<SuperGraphEdge> addMissAlwaysCost(
			Segment segment,			
			IPETSolver<SuperGraphEdge> ipetSolver) {
	
		/* get always miss cost */
		AccessCostInfo missCostInfo = extractAccessesAndCosts(segment, null, objectCache.getCostModel());
		return addStaticCost(segment, missCostInfo, ipetSolver);
	}

	private Set<SuperGraphEdge> addStaticCost(
			Segment segment,
			AccessCostInfo accessInfo,
			IPETSolver<SuperGraphEdge> ipetSolver) {
		
		HashSet<SuperGraphEdge> costEdges = new HashSet<SuperGraphEdge>();
		for(Entry<SuperGraphEdge, Long> entry: nodeToEdgeCost(segment, accessInfo.getStaticCostMap()).entrySet()) {
			costEdges.add(fixedAdditionalCostEdge(entry.getKey(), KEY+"_"+"am", 0, entry.getValue(), ipetSolver));
		}
		return costEdges;
	}

	/**
	 * Add miss once cost: for each method cache persistence segment, add maximum miss cost to the segment entries
	 * @param segment
	 * @param ipetSolver
	 * @param checks
	 * @throws LpSolveException 
	 * @throws InvalidFlowFactException 
	 */
	@Override
	public Set<SuperGraphEdge> addMissOnceCost(Segment segment,
			IPETSolver<SuperGraphEdge> ipetSolver, EnumSet<PersistenceCheck> checks)
					throws InvalidFlowFactException, LpSolveException {

		
		Set<SuperGraphEdge> missCostEdges = new HashSet<SuperGraphEdge>();
		Set<SuperGraphNode> alwaysMissNodes = new HashSet<SuperGraphNode>(); 
		
		Collection<Segment> cover =
				findPersistenceSegmentCover(segment, EnumSet.allOf(PersistenceCheck.class), false, alwaysMissNodes);
		
		int tag = 0;
		for(Segment persistenceSegment : cover) {

			tag++;
			/* Compute cost for persistence segment */
			HashSet<SymbolicAddress> usedSetOut = new HashSet<SymbolicAddress>();
			ObjectCacheCost cost =
					computeCacheCost(persistenceSegment, getUsedRefs(persistenceSegment),
							objectCache.getCostModel(), usedSetOut ); 
			WCETTool.logger.info("O$-addMissOnceCost: "+cost.toString());
			F1<SuperGraphEdge, Long> costModel = MiscUtils.const1(cost.getCost());
			Set<SuperGraphEdge> costEdges = addFixedCostEdges(persistenceSegment.getEntryEdges(), ipetSolver,
					costModel, KEY + "_miss_once", tag);
			missCostEdges.addAll(costEdges);
		}
		
		AccessCostInfo alwaysMissAccessInfo =
				extractAccessesAndCosts(alwaysMissNodes, null, objectCache.getCostModel());
		missCostEdges.addAll(addStaticCost(segment, alwaysMissAccessInfo, ipetSolver));
		return missCostEdges;
	}
	
	/**
	 * Add miss once constraints for all subsegments in the persistence cover of the given segment
	 * @param segment
	 * @param ipetSolver
	 * @return
	 * @throws LpSolveException 
	 * @throws InvalidFlowFactException 
	 */
	@Override
	public Set<SuperGraphEdge> addMissOnceConstraints(Segment segment,
			IPETSolver<SuperGraphEdge> ipetSolver) throws InvalidFlowFactException, LpSolveException {
	
		Set<SuperGraphEdge> missEdges = new HashSet<SuperGraphEdge>();
		Set<SuperGraphNode> alwaysMissNodes = new HashSet<SuperGraphNode>(); 
		
		Collection<Segment> cover =
				findPersistenceSegmentCover(segment, EnumSet.allOf(PersistenceCheck.class), false, alwaysMissNodes);
		
		int segmentCounter = 0;
		for(Segment persistenceSegment : cover) {
			/* we need to distinguish edges which are shared between persistence segments */
			String key = KEY +"_" + (++segmentCounter);
			
			LocalPointsToResult usedRefs = getUsedRefs(persistenceSegment);
			/* Compute worst-case cost */
			HashSet<SymbolicAddress> usedObjectsSet = new HashSet<SymbolicAddress>();
			
			ObjectCacheIPETModel ocim =
					addObjectCacheCostEdges(persistenceSegment, usedRefs, objectCache.getCostModel(), ipetSolver);

			missEdges.addAll(ocim.staticCostEdges);
			missEdges.addAll(ocim.refMissEdges);
			missEdges.addAll(ocim.blockMissEdges);
		}
		
		AccessCostInfo alwaysMissAccessInfo =
				extractAccessesAndCosts(alwaysMissNodes, null, objectCache.getCostModel());
		missEdges.addAll(addStaticCost(segment, alwaysMissAccessInfo, ipetSolver));

		return missEdges;
	}
	

	@Override
	public Set<SuperGraphEdge> addGlobalAllFitConstraints(Segment segment,
			IPETSolver<SuperGraphEdge> ipetSolver) {
		
		Set<SuperGraphEdge> missEdges = new HashSet<SuperGraphEdge>();
		LocalPointsToResult usedRefs = getUsedRefs(segment);
		/* Compute worst-case cost */		
		ObjectCacheIPETModel ocim =
				addObjectCacheCostEdges(segment, usedRefs, objectCache.getCostModel(), ipetSolver);

		missEdges.addAll(ocim.staticCostEdges);
		missEdges.addAll(ocim.refMissEdges);
		missEdges.addAll(ocim.blockMissEdges);		
		return missEdges;
	}

	/**
	 * Compute object cache cost for the given persistence segment
	 * @param segment the segment to consider
	 * @param usedRefs DFA result: the set of objects a reference might point to during one execution
	 *        of the segment
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
	
		/* create an ILP graph for all reachable methods */
		String key = GlobalAnalysis.formatProblemName(KEY, segment.getEntryCFGs().toString());
		
		IPETSolver<SuperGraphEdge> ipetSolver = GlobalAnalysis.buildIpetProblem(project, key, segment, new IPETConfig(project.getConfig()));
	
		ObjectCacheIPETModel ocim = addObjectCacheCostEdges(segment, usedRefs, costModel, ipetSolver);		
	
		/* solve */
		double lpCost;
		Map<SuperGraphEdge, Long> flowMap = new HashMap<SuperGraphEdge,Long>();
		lpCost = ipetSolver.solve(flowMap ,true);
		long cost = (long) (lpCost+0.5);
	
		return extractCost(segment, cost,flowMap, ocim.accessCostInfo, costModel, ocim.refMissEdges, ocim.blockMissEdges);
	}

	/**
	 * @param segment
	 * @param usedRefs
	 * @param costModel
	 * @param ipetSolver
	 * @return
	 */
	private ObjectCacheIPETModel addObjectCacheCostEdges(Segment segment,
			LocalPointsToResult usedRefs, ObjectCacheCostModel costModel,
			IPETSolver<SuperGraphEdge> ipetSolver) {
	
		final AccessCostInfo accessCostInfo = extractAccessesAndCosts(segment, usedRefs, costModel);
	
		ObjectCacheIPETModel model = new ObjectCacheIPETModel();
		model.accessCostInfo = accessCostInfo;
		
		/* cache cost edges (bypass/always miss) */
		model.staticCostEdges =
			   addFixedCostEdges(segment.getEdges(), ipetSolver, new MiscUtils.F1<SuperGraphEdge, Long>() {
				@Override
				public Long apply(SuperGraphEdge v) {
					return accessCostInfo.getStaticCost(v.getTarget());
				}
			   }, KEY+"_static", 0);
					   
		
		/* cache cost edges (miss once) */		
		/* for references */
		model.refMissEdges = 
				addPersistenceSegmentConstraints(segment, accessCostInfo.getRefAccesses(segment), 
						ipetSolver, MiscUtils.<SuperGraphEdge,Long>const1(costModel.getReplaceLineCost()), KEY+"_ref");
	
		/* and for blocks */
		model.blockMissEdges = 
				addPersistenceSegmentConstraints(segment, accessCostInfo.getBlockAccesses(segment), 
						ipetSolver, MiscUtils.<SuperGraphEdge,Long>const1(costModel.getLoadCacheBlockCost()), KEY+"_block");
	
		return model;
	}

	/** Find a segment cover (i.e., a set of segments covering all execution paths)
	 *  where each segment in the set is persistent (a cache persistence region (CPR))
	 *  
	 *  <h2>The simplest algorithm for a segment S (for acyclic callgraphs)</h2>
	 *   <ul><li/>Check whether S itself is CPR; if so, return S
	 *       <li/>Otherwise, create subsegments S' for each invoked method,
	 *       <li/>and single node segments for each access
	 *   </ul>
	 * @param segment the parent segment
	 * @param checks the strategy to use to determine whether a segment is a persistence region
	 * @param avoidOverlap whether overlapping segments should be avoided
	 * @param alwaysMissNodes additional node set considered to be always miss
	 * @return
	 * @throws LpSolveException 
	 * @throws InvalidFlowFactException 
	 */
	protected Collection<Segment> findPersistenceSegmentCover(Segment segment, EnumSet<PersistenceCheck> checks, 
			boolean avoidOverlap, Set<SuperGraphNode> alwaysMissNodes) throws InvalidFlowFactException, LpSolveException {
		
		List<Segment> cover = new ArrayList<Segment>();
	
		/* We currently only support entries to one CFG */
		Set<ContextCFG> entryMethods = new HashSet<ContextCFG>();
		for(SuperGraphEdge entryEdge : segment.getEntryEdges()) {
			entryMethods.add(entryEdge.getTarget().getContextCFG());
		}
		
		ContextCFG entryMethod;
		if(entryMethods.size() != 1) {
			throw new AssertionError("findPersistenceSegmentCover: only supporting segments with unique entry method");
		} else {
			entryMethod = entryMethods.iterator().next();
		}
		if(this.isPersistenceRegion(segment, checks)) {
			cover.add(segment);
		} else {
			/* method sub segments */
			for(Pair<SuperInvokeEdge, SuperReturnEdge> invocation : segment.getCallSitesFrom(entryMethod)) {
				ContextCFG callee = invocation.first().getCallee();
				// System.err.println("Recursively analyzing: "+callee);
				
				Segment subSegment = Segment.methodSegment(callee, segment.getSuperGraph());
				cover.addAll(findPersistenceSegmentCover(subSegment, checks, avoidOverlap, alwaysMissNodes)); 
			}
			/* always miss nodes (not covered) */
			alwaysMissNodes.addAll(segment.getNodes(entryMethod));
		}
		return cover;
	}

	/**
	 * @param segment
	 * @param checks
	 * @return
	 * @throws LpSolveException 
	 * @throws InvalidFlowFactException 
	 */
	private boolean isPersistenceRegion(Segment segment, EnumSet<PersistenceCheck> _checks)
			throws InvalidFlowFactException, LpSolveException {
				
		long distinctCachedTagsAccessed = countDistinctCachedTagsAccessed(contextForSegment(segment));
		WCETTool.logger.info("isPersistenceRegion(): accessing "+distinctCachedTagsAccessed+" distinct tags in "+
				segment+" / N="+getNumberOfWays());
		return distinctCachedTagsAccessed <= getNumberOfWays();
	}

	protected int getNumberOfWays() {
				
		return objectCache.getAssociativity();
	}

	/**
	 * return number of distinct cached tags which might be accessed in the given scope
	 * <p>XXX: use segment instead of scope</p>
	 * @param scope
	 * @return the maximum number of distinct cached tags accessed
	 * @throws InvalidFlowFactException 
	 * @throws LpSolveException 
	 */
	public long countDistinctCachedTagsAccessed(ExecutionContext scope) throws InvalidFlowFactException, LpSolveException {
		
		Long maxCachedTags = this.maxCachedTagsAccessed.get(scope);
		if(maxCachedTags != null) return maxCachedTags;
	
		LocalPointsToResult usedRefs = getUsedRefs(scope);
	
		/* Create an analysis segment */
		Segment segment = Segment.methodSegment(scope.getMethodInfo(), scope.getCallString(), 
				project, project.getCallstringLength(), project);
		
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
	 * @param segment
	 * @return the results of the symbolic address DFA for the given segment
	 */
	public LocalPointsToResult getUsedRefs(Segment segment) {
		
		return getUsedRefs(contextForSegment(segment));
	}

	/**
	 * Get DFA results on symbolic reference names for the given scope
	 * XXX: proper segment support
	 * @param scope
	 * @return
	 */
	public LocalPointsToResult getUsedRefs(ExecutionContext scope) {
		
		ExecuteOnceAnalysis eoAna = new ExecuteOnceAnalysis(project);
		DFATool dfa = project.getDfaTool();
		SymbolicPointsTo spt = new SymbolicPointsTo(maxSetSize,
				project.getCallstringLength(), 
				new ExecOnceQuery(eoAna,scope));
		dfa.runLocalAnalysis(spt,scope);
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
	 * Get maximum cost due to the object cache in the given scope.
	 * Using special cost models such as COUNT_FIELD_TAGS and COUNT_REF_TAGS, this
	 * method can be used to calculate different metrics as well.
	 * <p> XXX: Use segment instead of scope </p>
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
				project, project.getCallstringLength(), project);
		/* Compute worst-case cost */
		HashSet<SymbolicAddress> usedObjectsSet = new HashSet<SymbolicAddress>();
        return computeCacheCost(segment, usedRefs, costModel, usedObjectsSet);
	}

	public Object getSaturatedTypes(ExecutionContext scope) throws InvalidFlowFactException, LpSolveException {
		if(! this.saturatedTypes.containsKey(scope)) countDistinctCachedTagsAccessed(scope);
		return this.saturatedTypes.get(scope);
	}

	public Set<SymbolicAddress> getUsedSymbolicNames(ExecutionContext scope) throws InvalidFlowFactException, LpSolveException {
		if(! tagSet.containsKey(scope)) countDistinctCachedTagsAccessed(scope);
		return tagSet.get(scope);		
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
	private ObjectCache.ObjectCacheCost extractCost(Segment segment,
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
			if(alwaysMissCost > 0) {
				missCount  +=  missCost / alwaysMissCost;
			}
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

		if(totalMissCost + totalBypassCost != lpCost) {
			
			WCETTool.logger.warn(String.format("Error in calculating missCost in all fit-area (misscount = %d): %d but should be %d (%d - %d)",
							missCount, totalMissCost,lpCost-totalBypassCost,lpCost,totalBypassCost));
		}

		ObjectCache.ObjectCacheCost ocCost = new ObjectCache.ObjectCacheCost(missCount, totalMissCost,bypassAccesses, totalBypassCost, fieldAccesses);
		return ocCost;
	}
	
	/* Helpers */
	/* ------- */

	/** Traverse vertex set.
	 * <p>Add vertex to access set of referenced addresses
	 * For references whose type cannot be fully resolved, add a
	 * cost of 1.</p>
	 * <p>FIXME: We should deal with subtyping (or better use storage based alias-analysis)</p>
	 *
	 * @param segment
	 * @param usedRefs the results of the local points-to analysis, or {@code null} for always miss costs
	 * @param costModel
	 */
	private AccessCostInfo extractAccessesAndCosts(
			Segment segment,
			LocalPointsToResult usedRefs,
			ObjectCacheCostModel costModel) {
		
		return extractAccessesAndCosts(segment.getNodes(), usedRefs, costModel);
	}

	/** Traverse vertex set.
	 * <p>Add vertex to access set of referenced addresses
	 * For references whose type cannot be fully resolved, add a
	 * cost of 1.</p>
	 * <p>FIXME: We should deal with subtyping (or better use storage based alias-analysis)</p>
	 *
	 * @param nodes
	 * @param usedRefs the results of the local points-to analysis, or {@code null} for always miss costs
	 * @param costModel
	 */
	private AccessCostInfo extractAccessesAndCosts(
			Iterable<SuperGraphNode> nodes, LocalPointsToResult usedRefs,
			ObjectCacheCostModel costModel) {

		AccessCostInfo aci = new AccessCostInfo();
		
		for(SuperGraphNode node : nodes) {
			/* Compute cost for basic block */
			BasicBlock bb = node.getCFGNode().getBasicBlock();
			if(bb == null) continue;
			long bypassCost = 0;
			long alwaysMissCost = 0;
			CallString cs = node.getContextCFG().getCallString();
	
			for(InstructionHandle ih : bb.getInstructions()) {

				String handleType = getHandleType(project, node.getCfg(), ih); 				
				if(handleType == null) continue; /* No getfield/handle access */
				int fieldIndex = getFieldIndex(project, node.getCfg(), ih);
				int blockIndex = getBlockIndex(fieldIndex);

//				System.err.println("Processing getfield "+ih+ " with field type "+getCachedType(project, node.getCfg(), ih));
				
				if(fieldIndex > this.maxCachedFieldIndex) {
					bypassCost += costModel.getFieldAccessCostBypass();
					continue;
				}
				
				BoundedSet<SymbolicAddress> refs = null;
				if(usedRefs != null) {
					if(! usedRefs.containsKey(ih)) {					
						usedRefs = null;
						WCETTool.logger.error("No DFA results for: "+ih.getInstruction() +
								" with field " + ((FieldInstruction)ih.getInstruction()).getFieldName(bb.cpg()));						
					} else {
						refs = usedRefs.get(ih,cs);	
						if(refs.isSaturated()) refs = null;						
					}
				}
				if(refs == null) {
					alwaysMissCost += costModel.getReplaceLineCost() + costModel.getLoadCacheBlockCost();
				} else {
					for(SymbolicAddress ref : refs.getSet()) {
						aci.addRefAccess(ref,node);
						aci.addBlockAccess(ref.accessArray(blockIndex), node);
						// Handle getfield_long / getfield_double
						if(getCachedType(project, node.getCfg(), ih) == Type.LONG ||
						   getCachedType(project, node.getCfg(), ih) == Type.DOUBLE) {
							if(blockIndex+1 > this.maxCachedFieldIndex) {
								bypassCost += costModel.getFieldAccessCostBypass();
							} else {
								aci.addBlockAccess(ref.accessArray(blockIndex+1), node);								
							}
						}
					}
				}
			}
			aci.putBypassCost(node, bypassCost);
			aci.putStaticCost(node, bypassCost + alwaysMissCost);
		}
		return aci;
	}

	/**
	 * XXX: temporary helper to bridge gap between two representations (segment and scope)
	 * @param segment
	 * @return the corresponding execution context
	 * @throws RuntimeException if the conversion is impossible
	 */
	private ExecutionContext contextForSegment(Segment segment) {

		ContextCFG entry;
		Set<ContextCFG> entries = segment.getEntryCFGs();
		if(entries.size() != 1) {
			throw new RuntimeException("contextForSegment(): Currently we only support a single entry method");
		}
		entry = entries.iterator().next();
		return new ExecutionContext(entry.getCfg().getMethodInfo(), entry.getCallString());
	}

	private int getBlockIndex(int fieldIndex) {
		return fieldIndex >> blockIndexBits;
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

	/**
	 * @return the index of the field accessed by the instruction, or 0 if the instruction
	 * does not access a field
	 */
	private static int getFieldIndex(WCETTool p, ControlFlowGraph cfg, InstructionHandle ih) {
		ConstantPoolGen constPool = cfg.getMethodInfo().getConstantPoolGen();
		Instruction instr = ih.getInstruction();
		if(instr instanceof FieldInstruction) {
			FieldInstruction fieldInstr = (FieldInstruction) instr;
			ReferenceType refType = fieldInstr.getReferenceType(constPool);
			if(!(refType instanceof ObjectType)) {
				throw new RuntimeException("getFieldIndex(): Unsupported object kind: "+refType.getClass());
			}
			ObjectType objType = (ObjectType)refType;
			String klassName = objType.getClassName();			
			String fieldName = fieldInstr.getFieldName(constPool);
			String fieldSig  = fieldInstr.getSignature(constPool);
			return p.getLinkerInfo().getFieldIndex(klassName, fieldName+fieldSig);
		} else {
			return 0;			
		}
	}
	
	/**
	 * @param maxCachedFieldIndex 
	 * @return whether the field accessed by the given instruction handle is cached
	 */
	public static boolean isFieldCached(WCETTool project, ControlFlowGraph cfg, InstructionHandle ih, int maxCachedFieldIndex) {
		int index = ObjectCacheAnalysis.getFieldIndex(project, cfg,ih);
		
		/* Uncached fields are treated separately */ 
		return (index <= maxCachedFieldIndex);
	}
	
	public static Type getCachedType(WCETTool project, ControlFlowGraph cfg, InstructionHandle ih) {
		ConstantPoolGen constPool = cfg.getMethodInfo().getConstantPoolGen();
		Instruction instr = ih.getInstruction();
		if(instr instanceof GETFIELD) {
			GETFIELD gf = (GETFIELD) instr;
			return gf.getFieldType(constPool);
		}
		if(! ALL_HANDLE_ACCESSES) {
			return null;
		} else {
			throw new AssertionError("For O$, only getfield is supported right now");		
		}
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
