/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.jopdesign.wcet08.uppaal.translator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.analysis.BlockWCET;
import com.jopdesign.wcet08.analysis.SimpleAnalysis;
import com.jopdesign.wcet08.analysis.CacheConfig.CacheApproximation;
import com.jopdesign.wcet08.analysis.SimpleAnalysis.WcetCost;
import com.jopdesign.wcet08.frontend.ControlFlowGraph;
import com.jopdesign.wcet08.frontend.WcetAppInfo;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CfgVisitor;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.DedicatedNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet08.graphutils.FlowGraph;
import com.jopdesign.wcet08.graphutils.Pair;
import com.jopdesign.wcet08.graphutils.LoopColoring.IterationBranchLabel;
import com.jopdesign.wcet08.uppaal.UppAalConfig;
import com.jopdesign.wcet08.uppaal.UppAalConfig.CacheSim;
import com.jopdesign.wcet08.uppaal.model.Location;
import com.jopdesign.wcet08.uppaal.model.Template;
import com.jopdesign.wcet08.uppaal.model.Transition;
import com.jopdesign.wcet08.uppaal.model.TransitionAttributes;
/**
 * Build UppAal templates for a Java method.
 * We map the CFG's nodes and edges to 
 * {@link Location}s and {@link Transition}s.
 * Nodes are mapped to subgraphs (with unique start and end node),
 * i.e. instances of <code>FlowGraph<Location,Transition></code>
 * 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class MethodBuilder implements CfgVisitor {
	private abstract class SyncBuilder {
		public abstract void methodEntry(Location entry);
		public abstract void methodExit(Location exit, Transition entryToExit);
		protected abstract Location createMethodInvocation(InvokeNode n, Location basicBlockExit);
		public Location invokeMethod(InvokeNode n, Location basicBlockNode) {
			if(project.getCallGraph().isLeafNode(n.getReferenced()) &&
			   Config.instance().getOption(UppAalConfig.UPPAAL_COLLAPSE_LEAVES)) {
				SimpleAnalysis ilpAn = new SimpleAnalysis(project);
				CacheApproximation cacheApprox;
				if(cacheSim == CacheSim.ALWAYS_MISS) {
					cacheApprox = CacheApproximation.ALWAYS_MISS;
				} else {
					cacheApprox = CacheApproximation.ALWAYS_HIT;
				}
				WcetCost wcet = ilpAn.computeWCET(n.getImplementedMethod(), cacheApprox);
				Location inv = tBuilder.createLocation("IN_"+n.getId());
				tBuilder.waitAtLocation(inv, wcet.getCost());
				return inv;
			} else {
				return createMethodInvocation(n,basicBlockNode);
			}
		}
	}
	private class SyncViaVariables extends SyncBuilder {
		public void methodEntry(Location entry) {
			tBuilder.getOutgoingAttrs(entry)
			.appendGuard(getId() + " == " + SystemBuilder.ACTIVE_METHOD)
			.appendUpdate(String.format("%s := %s", 
							TemplateBuilder.LOCAL_CALL_STACK_DEPTH, 
							SystemBuilder.CURRENT_CALL_STACK_DEPTH))
			.setSync(SystemBuilder.INVOKE_CHAN+"?");			
		}
		public void methodExit(Location exit, Transition exitToEntry) {
			exitToEntry.getAttrs().setSync(SystemBuilder.RETURN_CHAN+"!");
			tBuilder.getIncomingAttrs(exit).
				appendUpdate(String.format("%1$s := %1$s - 1", 
	        				 SystemBuilder.CURRENT_CALL_STACK_DEPTH)); 
			
		}
		public Location createMethodInvocation(InvokeNode n, Location basicBlockExit) {
			Location inNode = tBuilder.createLocation("IN_"+n.getId());
			tBuilder.getIncomingAttrs(inNode)
				.appendUpdate(String.format("%1$s := %1$s + 1",
												  SystemBuilder.CURRENT_CALL_STACK_DEPTH))
				.setSync(SystemBuilder.INVOKE_CHAN+"!");
			tBuilder.getIncomingAttrs(basicBlockExit)
				.appendUpdate(String.format("%s := %d", 
							  					   SystemBuilder.ACTIVE_METHOD, 
							  					   n.receiverFlowGraph().getId()));
			tBuilder.getOutgoingAttrs(inNode)
				.appendGuard(String.format("%s == %s", 
												  SystemBuilder.CURRENT_CALL_STACK_DEPTH, 
												  TemplateBuilder.LOCAL_CALL_STACK_DEPTH))
				.setSync(SystemBuilder.RETURN_CHAN+"?");
			return inNode;
		}
	}
	private class SyncViaChannels extends SyncBuilder {
		public void methodEntry(Location entry) {
			tBuilder.getOutgoingAttrs(entry)
			 .setSync(SystemBuilder.methodChannel(cfg.getId())+"?");
		}
		public void methodExit(Location exit, Transition exitToEntry) {
			exitToEntry.getAttrs()
				.setSync(SystemBuilder.methodChannel(cfg.getId())+"!");			
		}
		public Location createMethodInvocation(InvokeNode n, Location _) {
			Location inNode = tBuilder.createLocation("IN_"+n.getId());
			tBuilder.getIncomingAttrs(inNode)
				.setSync(SystemBuilder.methodChannel(n.receiverFlowGraph().getId())+"!");
			tBuilder.getOutgoingAttrs(inNode)
				.setSync(SystemBuilder.methodChannel(n.receiverFlowGraph().getId())+"?");
			return inNode;
		}
	}

	private static class NodeAutomaton extends Pair<Location,Location>{
		private static final long serialVersionUID = 1L;
		public NodeAutomaton(Location entry, Location exit) {
			super(entry, exit);
		}
		public Location getEntry() { return fst(); }
		public Location getExit()  { return snd(); }
		public static NodeAutomaton singleton(Location exit) {
			return new NodeAutomaton(exit,exit);
		}
	}
	private Project project;
	private WcetAppInfo wAppInfo;
	private ControlFlowGraph cfg;
	private TemplateBuilder tBuilder;
	private Map<CFGNode,NodeAutomaton> nodeTemplates;
	private boolean isRoot;
	private SyncBuilder syncBuilder;
	private Config config;
	private CacheSim cacheSim;
	public MethodBuilder(Project p, MethodInfo mi) {
		this.config = Config.instance();
		this.project = p;
		this.wAppInfo = p.getWcetAppInfo();
		this.cfg = wAppInfo.getFlowGraph(mi);
		this.cacheSim = config.getOption(UppAalConfig.UPPAAL_CACHE_SIM);
	}
	/**
	 * To translate the root method
	 * @param fg
	 * @return
	 */
	public Template buildRootMethod() {
		return buildMethod(true);
	}

	public Template buildMethod() {
		return buildMethod(false);
	}
	private Template buildMethod(boolean isRoot) {
		if(config.getOption(UppAalConfig.UPPAAL_ONE_CHANNEL_PER_METHOD)) {
			syncBuilder = new SyncViaChannels();
		} else {
			syncBuilder = new SyncViaVariables();
		}
		this.nodeTemplates = new HashMap<CFGNode, NodeAutomaton>();
		Map<CFGEdge, IterationBranchLabel<CFGNode>> edgeColoring = 
			cfg.getLoopColoring().getIterationBranchEdges();
		this.isRoot = isRoot;
		this.tBuilder = new TemplateBuilder("Method"+cfg.getId(),cfg.getId(),
										    cfg.getLoopBounds());
		this.tBuilder.addDescription("Template for method "+cfg.getMethodInfo());
		FlowGraph<CFGNode, CFGEdge> graph = cfg.getGraph();
		/* Translate the CFGs nodes */
		for(CFGNode node : graph.vertexSet()) {
			node.accept(this);
		}
		/* Translate the CFGs edges */
		for(CFGEdge edge : graph.edgeSet()) {
			buildEdge(edge);
		}
		new LayoutCFG(100,120).layoutCfgModel(tBuilder.getFinalTemplate());
		return tBuilder.getFinalTemplate();
	}
	
	public void visitSpecialNode(DedicatedNode n) {
		NodeAutomaton localTranslation = null;
		switch(n.getKind()) {
		case ENTRY:
			if(isRoot)  localTranslation = createRootEntry(n); 
			else		localTranslation = createEntry(n);
			break;
		case EXIT:
			if(isRoot) localTranslation = createRootExit(n);
			else 	   localTranslation = createExit(n);
			break;
		case SPLIT:
			localTranslation = createSplit(n);break;
		case JOIN:
			localTranslation = createJoin(n);break;
		}
		this.nodeTemplates.put(n,localTranslation);
	}	
	
	public void visitBasicBlockNode(BasicBlockNode n) {
		this.nodeTemplates.put(n,
							   createBasicBlock(n,BlockWCET.basicBlockWCETEstimate(n.getBasicBlock())));
	}
	
	public void visitInvokeNode(InvokeNode n) {
		this.nodeTemplates.put(n,createInvoke(n));		
	}
	
	private void buildEdge(CFGEdge edge) {
		FlowGraph<CFGNode, CFGEdge> graph = cfg.getGraph();
		Set<CFGNode> hols = cfg.getLoopColoring().getHeadOfLoops();
		Set<CFGEdge> backEdges = cfg.getLoopColoring().getBackEdges();
		Map<CFGEdge, IterationBranchLabel<CFGNode>> edgeColoring = 
			cfg.getLoopColoring().getIterationBranchEdges();
		CFGNode src = graph.getEdgeSource(edge);
		CFGNode target = graph.getEdgeTarget(edge);
		Transition transition = tBuilder.createTransition(
				nodeTemplates.get(src).snd(),
				nodeTemplates.get(target).fst());
		TransitionAttributes attrs = transition.getAttrs();
		IterationBranchLabel<CFGNode> edgeColor = edgeColoring.get(edge);
		if(edgeColor != null) {
			for(CFGNode loop : edgeColor.getContinues()) {
				attrs.appendGuard(tBuilder.contLoopGuard(loop));
				attrs.appendUpdate(tBuilder.incrLoopCounter(loop));
			}
			for(CFGNode loop : edgeColor.getExits()) {
				attrs.appendGuard(tBuilder.exitLoopGuard(loop));
			}
		}
		if(hols.contains(target) && ! backEdges.contains(edge)) {
			attrs.appendUpdate(tBuilder.resetLoopCounter(target));
		}
	}
	
	private NodeAutomaton createRootEntry(DedicatedNode n) {
		Location initLoc = tBuilder.getInitial();
		initLoc.setCommited();
		if(! config.getOption(UppAalConfig.UPPAAL_ONE_CHANNEL_PER_METHOD)) {
			tBuilder.getOutgoingAttrs(initLoc)
			.appendUpdate(String.format("%s := %s", 
					TemplateBuilder.LOCAL_CALL_STACK_DEPTH, 
					SystemBuilder.CURRENT_CALL_STACK_DEPTH));
		}
		return NodeAutomaton.singleton(initLoc);
	}
	private NodeAutomaton createRootExit(DedicatedNode n) {
		Location progExit = tBuilder.createLocation("E");
		progExit.setCommited();
		Location pastExit = tBuilder.createLocation("EE");
		tBuilder.createTransition(progExit, pastExit);
		return new NodeAutomaton(progExit,pastExit);
	}
	private NodeAutomaton createEntry(DedicatedNode n) {
		Location init = tBuilder.getInitial();
		syncBuilder.methodEntry(init);
		return NodeAutomaton.singleton(init);
	}
	private NodeAutomaton createExit(DedicatedNode n) {
		Location exit = tBuilder.createLocation("E");
		exit.setCommited();
		Transition t = tBuilder.createTransition(exit,tBuilder.getInitial());
		syncBuilder.methodExit(exit,t);
		return NodeAutomaton.singleton(exit);
	}
	private NodeAutomaton createSplit(DedicatedNode n) {
		Location split = tBuilder.createLocation("SPLIT_"+n.getId());
		split.setCommited();
		return NodeAutomaton.singleton(split);
	}
	private NodeAutomaton createJoin(DedicatedNode n) {
		Location join = tBuilder.createLocation("JOIN"+n.getId());
		join.setCommited();
		return NodeAutomaton.singleton(join);
	}
	private NodeAutomaton createBasicBlock(BasicBlockNode n, long blockWCET) {
		Location bbNode = tBuilder.createLocation("N"+n.getId());
		tBuilder.waitAtLocation(bbNode,blockWCET);
		return NodeAutomaton.singleton(bbNode);		
	}
	private NodeAutomaton createInvoke(InvokeNode n) {
		long blockWCET = BlockWCET.basicBlockWCETEstimate(n.getBasicBlock());
		if(cacheSim == UppAalConfig.CacheSim.ALWAYS_MISS) {
			blockWCET+= BlockWCET.getMissOnInvokeCost(n.receiverFlowGraph());
			blockWCET+= BlockWCET.getMissOnReturnCost(cfg);
		}		
		Location basicBlockNode = createBasicBlock(n,blockWCET).getExit();
		Location invokeNode = syncBuilder.invokeMethod(n,basicBlockNode);
		Transition bbInvTrans = tBuilder.createTransition(basicBlockNode,invokeNode);			
		if(UppAalConfig.isDynamicCacheSim()) {
			Location invokeMissNode = tBuilder.createLocation("CACHEI_"+n.getId());
			Transition bbMissTrans = tBuilder.createTransition(basicBlockNode, invokeMissNode);
			tBuilder.createTransition(invokeMissNode, invokeNode);
			int recID = n.receiverFlowGraph().getId();
			tBuilder.getIncomingAttrs(basicBlockNode).appendUpdate("access_cache("+recID+")");
			tBuilder.onHit(bbInvTrans);
			tBuilder.onMiss(bbMissTrans);
			tBuilder.waitAtLocation(invokeMissNode, BlockWCET.getMissOnInvokeCost(n.receiverFlowGraph()));
		}
		Location invokeExitNode;
		if(UppAalConfig.isDynamicCacheSim()) {
			Location cacheAccess = tBuilder.createLocation("CACHER_"+n.getId());
			cacheAccess.setCommited();
			Transition invAcc = tBuilder.createTransition(invokeNode, cacheAccess);
			invAcc.getAttrs().appendUpdate("access_cache("+cfg.getId()+")");
			invokeExitNode = tBuilder.createLocation("INVEXIT_"+n.getId());
			invokeExitNode.setCommited();
			Location returnMissNode = tBuilder.createLocation("CACHERMISS_"+n.getId());
			Transition accExit = tBuilder.createTransition(cacheAccess, invokeExitNode);
			Transition accMiss = tBuilder.createTransition(cacheAccess, returnMissNode);
			/* missExit */ tBuilder.createTransition(returnMissNode, invokeExitNode);
			tBuilder.onHit(accExit);
			tBuilder.onMiss(accMiss);
			tBuilder.waitAtLocation(returnMissNode, BlockWCET.getMissOnReturnCost(cfg));
		} else {
			 invokeExitNode = invokeNode;			
		}
		return new NodeAutomaton(basicBlockNode,invokeExitNode);
	}

	public int getId() {
		return cfg.getId();
	}
}
