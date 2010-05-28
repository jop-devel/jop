package com.jopdesign.wcet.analysis.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.bcel.generic.InstructionHandle;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.framework.CallString;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.analysis.AnalysisContext;
import com.jopdesign.wcet.analysis.AnalysisContextLocal;
import com.jopdesign.wcet.analysis.RecursiveAnalysis;
import com.jopdesign.wcet.analysis.RecursiveWcetAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.analysis.WcetVisitor;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.analysis.cache.ObjectRefAnalysis.ObjectCacheCostModel;
import com.jopdesign.wcet.frontend.CallGraph;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CfgVisitor;
import com.jopdesign.wcet.frontend.ControlFlowGraph.DedicatedNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.SummaryNode;
import com.jopdesign.wcet.ipet.IpetConfig;
import com.jopdesign.wcet.ipet.ILPModelBuilder.CostProvider;
import com.jopdesign.wcet.ipet.ILPModelBuilder.MapCostProvider;
import com.jopdesign.wcet.jop.JOPConfig;

/** A demonstration of the persistence analysis for the object cache
 *  <p>
 *  As we have not yet implemented unsharing (this is not as trivial as it sounds),
 *  we use, once again, a recursive analysis 
 *  </p><p>
 *  We compute a WCET problem, with the following cost model:
 *  A object handle access has cost 1, everything else is cost 0.
 *  Solve the problem once with and once without persistence analysis, and compare costs.
 *  </p>
 *  
 */
public class ObjectCacheAnalysisDemo {
	public static final int DEFAULT_SET_SIZE = 64;

	public static class ObjectCacheCost {
		private long missCost;
		private long bypassCost;
		private long fieldAccesses;
		private long bypassAccesses;
		private long missCount;

		/**
		 * @param missCost2
		 * @param bypassCost2
		 * @param fieldAccesses2
		 */
		public ObjectCacheCost(long missCount, long missCost, long bypassAccesses, long bypassCost, long fieldAccesses) {
			this.missCost = missCost;
			this.bypassCost = bypassCost;
			this.fieldAccesses = fieldAccesses;
			this.missCount = missCount;
			this.bypassAccesses = bypassAccesses;
		}

		public ObjectCacheCost() {
			this(0,0,0,0,0);
		}

		public long getCost()
		{
			return missCost + bypassCost;
		}
		
		public long getBypassCost() { return bypassCost; }
		public long getBypassAccesses() { return this.bypassAccesses; }
		
		public void addBypassCost(long bypassCost, int accesses) {
			this.bypassCost += bypassCost;
			this.bypassAccesses += accesses;			
		}

		public ObjectCacheCost addMissCost(long missCost, int missCount) {
			this.missCost += missCost;
			this.missCount += missCount;
			return this;
		}
		
		/* addition field accesses either hit or miss (but not bypass) */
		public void addAccessToCachedField(long additionalFAs) {
			fieldAccesses += additionalFAs;
		}

		public long getTotalFieldAccesses()
		{
			return bypassAccesses + fieldAccesses;
		}
		
		public long getFieldAccessesWithoutBypass()
		{
			return fieldAccesses;
		}
		/* cache miss count */
		public long getCacheMissCount() {
			return missCount;
		}

		public void addCost(ObjectCacheCost occ) {
			this.missCount += occ.missCount;
			this.missCost += occ.missCost;
			this.bypassAccesses += occ.bypassAccesses;
			this.bypassCost += occ.bypassCost;
			addAccessToCachedField(occ.fieldAccesses);
		}
		
		public String toString() {
			return String.format("%d [miss=%d,bypass=%d,accesses=%d]",getCost(),this.missCost,this.bypassCost,this.fieldAccesses);
		}

		public ObjectCacheCost times(Long value) {
			return new ObjectCacheCost(missCount * value, missCost * value,
					                   bypassAccesses * value, bypassCost * value,
					                   fieldAccesses * value);
		}

	}
	
	public class RecursiveOCacheAnalysis extends
			RecursiveAnalysis<AnalysisContext, ObjectCacheCost> {

		private RecursiveStrategy<AnalysisContext, ObjectCacheCost> recursiveStrategy;

		public RecursiveOCacheAnalysis(Project p, IpetConfig ipetConfig,
				RecursiveStrategy<AnalysisContext, ObjectCacheCost> recursiveStrategy) {
			super(p, ipetConfig);
			this.recursiveStrategy = recursiveStrategy;
		}
		@Override
		protected ObjectCacheCost computeCostOfNode(CFGNode n, AnalysisContext ctx) {
			return new OCacheVisitor(this.getProject(), this, recursiveStrategy, ctx).computeCost(n);
		}

		@Override
		protected CostProvider<CFGNode> getCostProvider(
				Map<CFGNode, ObjectCacheCost> nodeCosts) {
			HashMap<CFGNode, Long> costMap = new HashMap<CFGNode, Long>();
			for(Entry<CFGNode, ObjectCacheCost> entry : nodeCosts.entrySet()) {
				costMap.put(entry.getKey(),entry.getValue().getCost());
			}
			return new MapCostProvider<CFGNode>(costMap, 1000);
		}

		@Override
		protected ObjectCacheCost extractSolution(ControlFlowGraph cfg,
				Map<CFGNode, ObjectCacheCost> nodeCosts,
				long maxCost,
				Map<CFGEdge, Long> edgeFlow) {
			Map <CFGNode, Long> nodeFlow = RecursiveWcetAnalysis.edgeToNodeFlow(cfg.getGraph(),edgeFlow);			
			ObjectCacheCost ocCost = new ObjectCacheCost();
			for(Entry<CFGNode, Long> entry : nodeFlow.entrySet()) {
				ocCost.addCost(nodeCosts.get(entry.getKey()).times(entry.getValue()));
			}
			if(maxCost != ocCost.getCost()) {
				throw new AssertionError(
						String.format("Object Cache Cost: Cost of lp solver (%d) and reconstructed cost (%d) do not coincide",maxCost,ocCost.getCost()));
			}
			return ocCost;
		}

	} 

	/** Visitor for computing the WCET of CFG nodes */
	private class OCacheVisitor implements CfgVisitor {
		private ObjectCacheCost cost;
		private RecursiveAnalysis<AnalysisContext, ObjectCacheCost> recursiveAnalysis;
		private RecursiveStrategy<AnalysisContext, ObjectCacheCost> recursiveStrategy;
		private AnalysisContext context;
		private Project project;

		public OCacheVisitor(
				Project p,
				RecursiveAnalysis<AnalysisContext, ObjectCacheCost> recursiveAnalysis,
				RecursiveStrategy<AnalysisContext, ObjectCacheCost> recursiveStrategy, 
				AnalysisContext ctx
				) {
			this.project = p; 
			this.recursiveAnalysis = recursiveAnalysis;
			this.recursiveStrategy = recursiveStrategy;
			this.context = ctx;
		}
		// Cost ~ number of cache misses
		// TODO: A basic block is a scope too!
		public void visitBasicBlockNode(BasicBlockNode n) {
			long worstCaseMissCost;
			if(jopconfig.objectCacheFillLine()) {
				worstCaseMissCost = jopconfig.getObjectCacheAccessTime(jopconfig.getObjectLineSize());	
			} else {
				worstCaseMissCost = jopconfig.getObjectCacheAccessTime(1);
			}
			for(InstructionHandle ih : n.getBasicBlock().getInstructions()) {
				if(null == ObjectRefAnalysis.getHandleType(project, n, ih)) continue;
				if(! ObjectRefAnalysis.isFieldCached(n.getControlFlowGraph(), ih, jopconfig.getObjectCacheMaxCachedFieldIndex())) {
					cost.addBypassCost(worstCaseMissCost,1);
				} else {
					cost.addMissCost(worstCaseMissCost,1);
					cost.addAccessToCachedField(1); 
				}
			}
		} 

		public void visitInvokeNode(InvokeNode n) {
			visitBasicBlockNode(n);
			if(n.isVirtual()) {
				throw new AssertionError("Invoke node "+n.getReferenced()+" without implementation in WCET analysis - did you preprocess virtual methods ?");
			}
			cost.addCost(recursiveStrategy.recursiveCost(recursiveAnalysis, n, context));
		}

		public void visitSpecialNode(DedicatedNode n) {
		}

		public void visitSummaryNode(SummaryNode n) {
			ControlFlowGraph subCfg = n.getControlFlowGraph();
			cost.addCost(recursiveAnalysis.computeCostUncached(n.toString(), subCfg, new AnalysisContext()));
		}
		public ObjectCacheCost computeCost(CFGNode n) {
			this.cost = new ObjectCacheCost();
			n.accept(this);
			return cost;
		}
	}

	// Ok, a few notes what is probably incorrect at the moment:
	//  a) Cannot handle java implemented methods (I think)
	//  b) invokevirtual also accesses the object, this is not considered
	private class RecursiveWCETOCache
	implements RecursiveStrategy<AnalysisContext,ObjectCacheCost> {
		public ObjectCacheCost recursiveCost(
				RecursiveAnalysis<AnalysisContext,ObjectCacheCost> stagedAnalysis,
				InvokeNode invocation, 
				AnalysisContext ctx) {
			MethodInfo invoked = invocation.getImplementedMethod();
			ObjectCacheCost cost;
			if(allPersistent(invoked, ctx.getCallString())) {
				cost  = getAllFitCost(invoked, ctx.getCallString());
				//System.out.println("Cost for: "+invocation.getImplementedMethod()+" [all fit]: "+cost);
			} else {
				cost = stagedAnalysis.computeCost(invoked, ctx);
				//System.out.println("Cost for: "+invocation.getImplementedMethod()+" [recursive]: "+cost);
			}
			return cost;
		}

	}

	private Project project;
	private JOPConfig jopconfig;
	private ObjectRefAnalysis objRefAnalysis;
	private int maxCachedFieldIndex;
	private ObjectCacheCostModel costModel;
	private boolean assumeAllMiss;

	public ObjectCacheAnalysisDemo(Project p, JOPConfig jopconfig) {
		this.project = p;
		this.jopconfig = jopconfig;
		this.maxCachedFieldIndex = jopconfig.getObjectCacheMaxCachedFieldIndex();
		this.objRefAnalysis = new ObjectRefAnalysis(project, jopconfig.objectCacheFillLine, jopconfig.objectCacheSingleField(), maxCachedFieldIndex, DEFAULT_SET_SIZE);
		this.costModel = getCostModel();		
	}
	
	public void setAssumeAlwaysMiss() {
		this.assumeAllMiss = true;
	}
	
	private ObjectCacheCostModel getCostModel() {
		long loadCacheLineCost;
		long loadFieldCost;
		long fieldAccessCostBypass = jopconfig.getObjectCacheAccessTime(1);
		/* field-as-tag */
		if(jopconfig.objectCacheSingleField()) {
			loadCacheLineCost = 0;
			loadFieldCost = jopconfig.getObjectCacheAccessTime(1); 
		} else if(jopconfig.objectCacheFillLine()) {
			loadCacheLineCost = jopconfig.getObjectCacheAccessTime(jopconfig.getObjectLineSize());
			loadFieldCost = 0;
		} else {
			loadCacheLineCost = 0;
			loadFieldCost = jopconfig.getObjectCacheAccessTime(1);			
		}
		return new ObjectCacheCostModel(loadFieldCost, loadCacheLineCost, fieldAccessCostBypass);
	}

	public ObjectCacheCost computeCost() {
		/* Cache Analysis */
		RecursiveAnalysis<AnalysisContext, ObjectCacheCost> recAna =
			new RecursiveOCacheAnalysis(project, new IpetConfig(project.getConfig()),
					new RecursiveWCETOCache());
		
		return recAna.computeCost(project.getTargetMethod(), new AnalysisContext());
	}

	public long getMaxAccessedTags(MethodInfo invoked, CallString context) {
		if(! context.isEmpty()) {
			throw new AssertionError("Callstrings are not yet supported for object cache analysis");
		}
		return objRefAnalysis.getMaxCachedTags(new CallGraph.CallGraphNode(invoked, context));
	}
 
	private ObjectCacheCost getAllFitCost(MethodInfo invoked, CallString context) {
		if(! context.isEmpty()) {
			throw new AssertionError("Callstrings are not yet supported for object cache analysis");
		}
		return objRefAnalysis.getMaxCacheCost(new CallGraph.CallGraphNode(invoked, context), costModel);
	}


	private boolean allPersistent(MethodInfo invoked, CallString context) {
		if(assumeAllMiss) return false;
		return getMaxAccessedTags(invoked, context) <= jopconfig.getObjectCacheAssociativity();
	}		
	
}
