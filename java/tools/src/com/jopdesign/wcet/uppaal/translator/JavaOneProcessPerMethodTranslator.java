package com.jopdesign.wcet.uppaal.translator;

import java.util.HashMap;
import java.util.Map;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.analysis.RecursiveAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet.graphutils.MiscUtils;
import com.jopdesign.wcet.jop.CacheConfig.StaticCacheApproximation;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import com.jopdesign.wcet.uppaal.model.DuplicateKeyException;
import com.jopdesign.wcet.uppaal.model.Location;
import com.jopdesign.wcet.uppaal.model.Transition;

/**
 * One-Template-Per-Method process translator
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class JavaOneProcessPerMethodTranslator extends JavaTranslator {
	public class InvokeViaSyncBuilder extends InvokeBuilder {

		public InvokeViaSyncBuilder(JavaTranslator mt, 
				                    TemplateBuilder tBuilder) {
			super(mt,tBuilder, mt.cacheSim);
		}
		/* we need a few nodes for translating an invoke node
		 * - A  bbNode (wait, before invoking)
		 * - An invokeNode (for waiting while executing the invoked method)
		 * - Additionally when using a cache:
		 *   - A invokeMissNode (for waiting the miss of the invoked method)
		 *     with (bb -> invokeMissNode, invokeMissNode -> wait)
		 *     and  (access (-> bb), guard (bb -> invokeMissNode), guard (bb -> invokeNode))
		 *   - A returnAccessNode, a returnMissNode and a exitInvokeNode 
		 *     (for waiting the miss of the returned method)
		 * (non-Javadoc)
		 * @see com.jopdesign.wcet.uppaal.translator.InvokeBuilder#translateInvoke(com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode)
		 */
		@Override
		public SubAutomaton translateInvoke(MethodBuilder mBuilder, InvokeNode n, long staticWCET) {
			/* location for executing the code */
			SubAutomaton basicBlock = mBuilder.createBasicBlock(n.getId(),staticWCET);
			Location startInvoke = basicBlock.getEntry(), finishInvoke;		
			Location basicBlockNode = basicBlock.getExit();
			/* location for waiting */
			Location waitInvokeNode = tBuilder.createLocation("INVOKE_WAIT_"+n.getId());
			simulateMethodInvocation(waitInvokeNode, n);
			/* If dynamic cache sim */		
			if(javaTranslator.getCacheSim().isDynamic()) {
				Location invokeMissNode   = tBuilder.createLocation("INVOKE_MISS_"+n.getId());
				Transition toInvokeHit    = tBuilder.createTransition(basicBlockNode, waitInvokeNode);
				Transition toInvokeMiss   = tBuilder.createTransition(basicBlockNode, invokeMissNode);
	                                        tBuilder.createTransition(invokeMissNode, waitInvokeNode);
				simulateCacheAccess(
						n.receiverFlowGraph(),true,
						basicBlockNode,  /* access cache on ingoing transitions */
						toInvokeHit,     /* if hit transition */
						toInvokeMiss,    /* if miss transition */
						invokeMissNode); /* miss node */
				Location returnAccessNode = tBuilder.createCommitedLocation("RETURN_ACCESS_"+n.getId());
				Location returnMissNode   = tBuilder.createLocation("RETURN_MISS_"+n.getId());
				Location exitInvokeNode   = tBuilder.createCommitedLocation("EXIT_INVOKE_"+n.getId());
				                            tBuilder.createTransition(waitInvokeNode, returnAccessNode);
				Transition toReturnHit    = tBuilder.createTransition(returnAccessNode, exitInvokeNode);
				Transition toReturnMiss   = tBuilder.createTransition(returnAccessNode, returnMissNode);
	            							tBuilder.createTransition(returnMissNode, exitInvokeNode);
				simulateCacheAccess(
						n.invokerFlowGraph(),false,
						returnAccessNode, /* access cache on ingoing transitions */
						toReturnHit,      /* if hit transition */
						toReturnMiss,     /* if miss transition */
						returnMissNode); /* miss node */
				finishInvoke = exitInvokeNode;
			} else {
				tBuilder.createTransition(basicBlockNode, waitInvokeNode);
				finishInvoke = waitInvokeNode;
			}
			return new SubAutomaton(startInvoke, finishInvoke);
		}
		public void simulateMethodInvocation(Location waitInvokeLoc, InvokeNode n) {
			if(n.receiverFlowGraph().isLeafMethod() && config.collapseLeaves) {
				RecursiveAnalysis<StaticCacheApproximation> ilpAn = 
					new RecursiveAnalysis<StaticCacheApproximation>(project,new RecursiveAnalysis.LocalIPETStrategy());
				WcetCost wcet = ilpAn.computeWCET(n.getImplementedMethod(), StaticCacheApproximation.ALWAYS_HIT);
				tBuilder.waitAtLocation(waitInvokeLoc, wcet.getCost());
			} else {
				int mid = javaTranslator.getMethodID(n.getImplementedMethod());
				tBuilder.getIncomingAttrs(waitInvokeLoc)
					.setSync(SystemBuilder.methodChannel(mid)+"!");
				tBuilder.getOutgoingAttrs(waitInvokeLoc)
					.setSync(SystemBuilder.methodChannel(mid)+"?");
			}		
		}

	}
	private Map<MethodInfo,TemplateBuilder> processes = new HashMap<MethodInfo, TemplateBuilder>();

	public JavaOneProcessPerMethodTranslator(UppAalConfig c, Project p, MethodInfo root) {
		super(c, p, root);
	}

	@Override
	protected void translate() {
		systemBuilder.addMethodSynchChannels(methodInfos,this.methodIDs);
		String bbClock = systemBuilder.addProcessClock(0);
		/* For each method, create a process */
		for(MethodInfo mi : this.methodInfos) {
			if(project.getCallGraph().isLeafNode(mi) && config.collapseLeaves) continue;
			int pid = getMethodID(mi);
			TemplateBuilder tBuilder =
				new TemplateBuilder(config,
								   MiscUtils.qEncode(mi.getFQMethodName()), pid,
								   bbClock);
			recordLoops(mi,tBuilder);
			processes.put(mi,tBuilder);
			translateMethod(tBuilder, tBuilder.getTemplateAutomaton(), pid, mi, 
							new InvokeViaSyncBuilder(this,tBuilder));
			if(mi.equals(root)) {
				tBuilder.getInitial().setCommited();
				tBuilder.addPostEnd();
			} else {
				tBuilder.addSyncLoop();
			}
			try {
				systemBuilder.addTemplate(pid, 0, tBuilder.getFinalTemplate());
			} catch (DuplicateKeyException e) {
				throw new AssertionError("Unexpected exception when adding template: "+e.getMessage());
			}
		}
	}

	private void recordLoops(MethodInfo mi, TemplateBuilder pb) {
		ControlFlowGraph cfg = project.getFlowGraph(mi);
		for(CFGNode hol: cfg.getLoopColoring().getHeadOfLoops()) {
			LoopBound bound = cfg.getLoopBounds().get(hol);
			int nesting = cfg.getLoopColoring().getLoopColor(hol).size();
			pb.addLoop(hol,nesting,bound);
		}
	}

}
