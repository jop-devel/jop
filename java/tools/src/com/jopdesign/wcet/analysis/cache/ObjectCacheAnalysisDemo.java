package com.jopdesign.wcet.analysis.cache;

import java.util.Map;

import org.apache.bcel.generic.InstructionHandle;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.framework.CallString;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.analysis.AnalysisContext;
import com.jopdesign.wcet.analysis.AnalysisContextIpet;
import com.jopdesign.wcet.analysis.AnalysisContextLocal;
import com.jopdesign.wcet.analysis.RecursiveAnalysis;
import com.jopdesign.wcet.analysis.RecursiveWcetAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.analysis.WcetVisitor;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
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

	public class RecursiveOCacheAnalysis extends
			RecursiveAnalysis<AnalysisContext, Long> {

		private RecursiveStrategy<AnalysisContext, Long> recursiveStrategy;

		public RecursiveOCacheAnalysis(Project p, IpetConfig ipetConfig,
				RecursiveStrategy<AnalysisContext, Long> recursiveStrategy) {
			super(p, ipetConfig);
			this.recursiveStrategy = recursiveStrategy;
		}
		@Override
		protected Long computeCostOfNode(CFGNode n, AnalysisContext ctx) {
			return new OCacheVisitor(this.getProject(), this, recursiveStrategy, ctx).computeCost(n);
		}

		@Override
		protected CostProvider<CFGNode> getCostProvider(
				Map<CFGNode, Long> nodeCosts) {
			return new MapCostProvider<CFGNode>(nodeCosts, 1000);
		}

		@Override
		protected Long extractSolution(ControlFlowGraph cfg,
				Map<CFGNode, Long> nodeCosts, long maxCost,
				Map<CFGEdge, Long> edgeFlowOut) {
			return maxCost;
		}


	}

	/** Visitor for computing the WCET of CFG nodes */
	private class OCacheVisitor implements CfgVisitor {
		private long cost;
		private RecursiveAnalysis<AnalysisContext, Long> recursiveAnalysis;
		private RecursiveStrategy<AnalysisContext, Long> recursiveStrategy;
		private AnalysisContext context;
		private Project project;

		public OCacheVisitor(
				Project p,
				RecursiveAnalysis<AnalysisContext, Long> recursiveAnalysis,
				RecursiveStrategy<AnalysisContext, Long> recursiveStrategy, 
				AnalysisContext ctx
				) {
			this.project = p;
			this.recursiveAnalysis = recursiveAnalysis;
			this.recursiveStrategy = recursiveStrategy;
			this.context = ctx;
		}
		// Cost ~ number of cache misses
		// FIXME: A basic block is a scope too!
		public void visitBasicBlockNode(BasicBlockNode n) {
			for(InstructionHandle ih : n.getBasicBlock().getInstructions()) {
				if(null == ObjectRefAnalysis.getHandleType(project, n, ih)) continue;
				// TODO: give good costs
				cost += 1;
			}
		}

		public void visitInvokeNode(InvokeNode n) {
			visitBasicBlockNode(n);
			if(n.isVirtual()) {
				throw new AssertionError("Invoke node "+n.getReferenced()+" without implementation in WCET analysis - did you preprocess virtual methods ?");
			}
			cost += recursiveStrategy.recursiveCost(recursiveAnalysis, n, context);
		}

		public void visitSpecialNode(DedicatedNode n) {
		}

		public void visitSummaryNode(SummaryNode n) {
			ControlFlowGraph subCfg = n.getControlFlowGraph();
			cost += recursiveAnalysis.computeCostUncached(n.toString(), subCfg, new AnalysisContext());
		}
		public long computeCost(CFGNode n) {
			this.cost = 0;
			n.accept(this);
			return cost;
		}
	}

	// Ok, a few notes what is probably incorrect at the moment:
	//  a) Cannot handle java implemented methods (I think)
	//  b) invokevirtual also accesses the object, this is not considered
	private class RecursiveWCETOCache
	implements RecursiveStrategy<AnalysisContext,Long> {
		public Long recursiveCost(
				RecursiveAnalysis<AnalysisContext,Long> stagedAnalysis,
				InvokeNode invocation, 
				AnalysisContext ctx) {
			MethodInfo invoked = invocation.getImplementedMethod();
			Long cost;
			if(allPersistent(invoked, ctx.getCallString())) {
				if(jopconfig.getObjectCacheFillLine()) {
					cost = getMaxAccessedObjects(invoked, ctx.getCallString());
				} else {
					cost = getMaxAccessedFields(invoked, ctx.getCallString());					
				}
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
	private CallGraph callGraph;
	private boolean assumeAllMiss;

	public ObjectCacheAnalysisDemo(Project p, JOPConfig jopconfig) {
		this(p, jopconfig, jopconfig.getObjectCacheAssociativity() == 0);
	}

	public ObjectCacheAnalysisDemo(Project p, JOPConfig jopconfig, boolean assumeAllMiss) {
		this.project = p;
		this.jopconfig = jopconfig;
		this.callGraph = project.getCallGraph();
		this.assumeAllMiss = assumeAllMiss;
	}
	
	public long computeCost() {
		/* Cache Analysis */
		// objRefAnalysis = new ObjectRefAnalysis(project, DEFAULT_SET_SIZE);
		objRefAnalysis = new ObjectRefAnalysis(project, jopconfig.getObjectLineSize());
		objRefAnalysis.analyzeRefUsage();
		// TODO: Distinguish fill line / fill field here
		RecursiveAnalysis<AnalysisContext, Long> recAna =
			new RecursiveOCacheAnalysis(project, new IpetConfig(project.getConfig()),
					new RecursiveWCETOCache());
		
		return recAna.computeCost(project.getTargetMethod(), new AnalysisContext());
	}

	private long getMaxAccessedObjects(MethodInfo invoked, CallString context) {
		return objRefAnalysis.getMaxReferencesAccessed().get(new CallGraph.CallGraphNode(invoked, context));
	}

	private long getMaxAccessedFields(MethodInfo invoked, CallString context) {
		return objRefAnalysis.getMaxFieldsAccessed().get(new CallGraph.CallGraphNode(invoked, context));
	}

	private boolean allPersistent(MethodInfo invoked, CallString context) {
		if(assumeAllMiss) return false;
		if(jopconfig.isFieldCache()) {
			return getMaxAccessedFields(invoked, context) <= jopconfig.getObjectCacheAssociativity();
		} else {
			return getMaxAccessedObjects(invoked, context) <= jopconfig.getObjectCacheAssociativity();
		}
	}		
	
}
