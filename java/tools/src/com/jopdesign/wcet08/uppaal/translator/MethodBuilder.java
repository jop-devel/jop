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
import com.jopdesign.wcet08.Project;
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
	private WcetAppInfo wAppInfo;
	private ControlFlowGraph cfg;
	private TemplateBuilder tBuilder;
	private Map<CFGNode,NodeAutomaton> nodeTemplates;
	private Map<CFGNode,TransitionAttributes> incomingAttrs;
	private Map<CFGNode,TransitionAttributes> outgoingAttrs;
	private boolean isRoot;
	public MethodBuilder(Project p, MethodInfo mi) {
		this.wAppInfo = p.getWcetAppInfo();
		this.cfg = wAppInfo.getFlowGraph(mi);
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
		this.nodeTemplates = new HashMap<CFGNode, NodeAutomaton>();
		this.incomingAttrs = new HashMap<CFGNode, TransitionAttributes>();
		this.outgoingAttrs = new HashMap<CFGNode, TransitionAttributes>();
		
		this.isRoot = isRoot;
		this.tBuilder = new TemplateBuilder("Method"+cfg.getId(),cfg.getId(),cfg.getLoopBounds(),isRoot);
		FlowGraph<CFGNode, CFGEdge> graph = cfg.getGraph();
		/* Translate the CFGs nodes */
		for(CFGNode node : graph.vertexSet()) {
			this.incomingAttrs.put(node, new TransitionAttributes());
			this.outgoingAttrs.put(node, new TransitionAttributes());
			node.accept(this);
		}
		/* Translate the CFGs edges */
		for(CFGEdge edge : graph.edgeSet()) {
			buildEdge(edge);
		}
		new LayoutCFG(100,120).layoutCfgModel(tBuilder.getTemplate());
		return tBuilder.getTemplate();
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
		this.nodeTemplates.put(n,createBasicBlock(n));
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
		attrs.addAttributes(incomingAttrs.get(target));
		attrs.addAttributes(outgoingAttrs.get(src));
	}
	
	private NodeAutomaton createRootEntry(DedicatedNode n) {
		Location initLoc = tBuilder.getInitial();
		initLoc.setCommited();
		outgoingAttrs.get(n)
			.appendUpdate(tBuilder.initLocalCallStackDepth());
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
		outgoingAttrs.get(n)
			 .appendGuard(getId() + " == " + SystemBuilder.ACTIVE_METHOD)
			 .appendUpdate(tBuilder.initLocalCallStackDepth())
     		 .setSync(SystemBuilder.INVOKE_CHAN+"?");
		return NodeAutomaton.singleton(init);
	}
	private NodeAutomaton createExit(DedicatedNode n) {
		Location exit = tBuilder.createLocation("E");
		exit.setCommited();
		Transition t = tBuilder.createTransition(exit,tBuilder.getInitial());
		t.getAttrs().setSync(SystemBuilder.RETURN_CHAN+"!");

		this.incomingAttrs.get(n).
			appendUpdate(String.format("%1$s := %1$s - 1", 
        				 SystemBuilder.CURRENT_CALL_STACK_DEPTH)); 

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
	private NodeAutomaton createBasicBlock(BasicBlockNode n) {
		Location bbNode = tBuilder.createLocation("N"+n.getId());
		long blockWCET = cfg.basicBlockWCETEstimate(n.getBasicBlock());
		
		bbNode.setInvariant(String.format("%s <= %d", 
							TemplateBuilder.LOCAL_CLOCK, 
							blockWCET));
		this.incomingAttrs.get(n).
			appendUpdate(TemplateBuilder.LOCAL_CLOCK + " := 0");
		this.outgoingAttrs.get(n).
			appendGuard(String.format("%s >= %d", 
						TemplateBuilder.LOCAL_CLOCK, 
						blockWCET));

		return NodeAutomaton.singleton(bbNode);		
	}
	private NodeAutomaton createInvoke(InvokeNode n) {
		Location na = createBasicBlock(n).getExit();
		Location inNode = tBuilder.createLocation("IN_"+n.getId());
		Transition t = tBuilder.createTransition(na, inNode);
		t.getAttrs()
			.appendUpdate(String.format("%1$s := %1$s + 1",
											  SystemBuilder.CURRENT_CALL_STACK_DEPTH))
			.setSync(SystemBuilder.INVOKE_CHAN+"!");
		incomingAttrs.get(n)
			.appendUpdate(String.format("%s := %d", 
						  					   SystemBuilder.ACTIVE_METHOD, 
						  					   n.getFlowGraph().getId()));
		outgoingAttrs.get(n)
			.appendGuard(String.format("%s == %s", 
											  SystemBuilder.CURRENT_CALL_STACK_DEPTH, 
											  TemplateBuilder.LOCAL_CALL_STACK_DEPTH))
			.setSync(SystemBuilder.RETURN_CHAN+"?");
		return new NodeAutomaton(na,inNode);
	}

	public int getId() {
		return cfg.getId();
	}
}
