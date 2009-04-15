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
package com.jopdesign.wcet.uppaal.translator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.analysis.RecursiveAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CfgVisitor;
import com.jopdesign.wcet.frontend.ControlFlowGraph.DedicatedNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.SummaryNode;
import com.jopdesign.wcet.graphutils.FlowGraph;
import com.jopdesign.wcet.graphutils.LoopColoring.IterationBranchLabel;
import com.jopdesign.wcet.graphutils.ProgressMeasure.RelativeProgress;
import com.jopdesign.wcet.ipet.IpetConfig.StaticCacheApproximation;
import com.jopdesign.wcet.uppaal.model.Location;
import com.jopdesign.wcet.uppaal.model.Transition;
import com.jopdesign.wcet.uppaal.model.TransitionAttributes;
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
	/* input */
	private final JavaTranslator jTrans;
	private final InvokeBuilder invokeBuilder;
	private final SubAutomaton methodAuto;
	private final int mId;
	private final ControlFlowGraph cfg;
	private final TemplateBuilder tBuilder;
	/* state */
	private Map<CFGNode,SubAutomaton> nodeTemplates = new HashMap<CFGNode, SubAutomaton>();
	public MethodBuilder(JavaTranslator jt, TemplateBuilder tBuilder, InvokeBuilder invokeBuilder,
			             SubAutomaton methodAuto, int mId, ControlFlowGraph cfg) {
		this.jTrans = jt;
		this.tBuilder = tBuilder;
		this.mId = mId;
		this.cfg = cfg;
		this.methodAuto = methodAuto;
		this.invokeBuilder = invokeBuilder;
	}
	public void build() {
		this.nodeTemplates = new HashMap<CFGNode, SubAutomaton>();
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
	}
	
	public void visitSpecialNode(DedicatedNode n) {
		SubAutomaton localTranslation = null;
		switch(n.getKind()) {
		case ENTRY: localTranslation = SubAutomaton.singleton(methodAuto.getEntry()); break;
		case EXIT:  localTranslation = SubAutomaton.singleton(methodAuto.getExit()); break;
		case SPLIT: localTranslation = createSpecialCommited("SPLIT_"+n.getId(), n);break;
		case JOIN:  localTranslation = createSpecialCommited("JOIN_"+n.getId(), n);break;
		}
		this.nodeTemplates.put(n,localTranslation);
	}	
	private SubAutomaton createSpecialCommited(String name, DedicatedNode n) {
		Location split = tBuilder.createLocation(name+"_"+this.mId+"_"+n.getId());
		split.setCommited();
		return SubAutomaton.singleton(split);
	}
	
	public void visitBasicBlockNode(BasicBlockNode n) {
		SubAutomaton bbLoc = 
			createBasicBlock(n.getId(),jTrans.getProject().getProcessorModel().basicBlockWCET(n.getBasicBlock()));
		this.nodeTemplates.put(n,bbLoc);
	}
	
	public void visitInvokeNode(InvokeNode n) {
		ProcessorModel proc = jTrans.getProject().getProcessorModel();
		SubAutomaton invokeAuto;
		long staticWCET = proc.basicBlockWCET(n.getBasicBlock());
		if(jTrans.getCacheSim().isAlwaysMiss()) {
			staticWCET+=proc.getInvokeReturnMissCost(n.invokerFlowGraph(),n.receiverFlowGraph());
		}
		invokeAuto = invokeBuilder.translateInvoke(this,n,staticWCET);
		this.nodeTemplates.put(n,invokeAuto);		
	}
	
	public void visitSummaryNode(SummaryNode n) {
		RecursiveAnalysis<StaticCacheApproximation> an = 
			new RecursiveAnalysis<StaticCacheApproximation>(
					jTrans.getProject(),
					new RecursiveAnalysis.LocalIPETStrategy());
		WcetCost cost = an.runWCETComputation("SUBGRAPH"+n.getId(), n.getSubGraph(), StaticCacheApproximation.ALWAYS_MISS)
				          .getTotalCost();
		SubAutomaton sumLoc = createBasicBlock(n.getId(),cost.getCost());
		this.nodeTemplates.put(n,sumLoc);
	}
	private void buildEdge(CFGEdge edge) {
		FlowGraph<CFGNode, CFGEdge> graph = cfg.getGraph();
		Set<CFGNode> hols = cfg.getLoopColoring().getHeadOfLoops();
		Set<CFGEdge> backEdges = cfg.getLoopColoring().getBackEdges();
		Map<CFGEdge, IterationBranchLabel<CFGNode>> edgeColoring = 
			cfg.getLoopColoring().getIterationBranchEdges();
		CFGNode src = graph.getEdgeSource(edge);
		CFGNode target = graph.getEdgeTarget(edge);
		if(src == cfg.getEntry() && target == cfg.getExit()) return;
		Transition transition = tBuilder.createTransition(
				nodeTemplates.get(src).getExit(),
				nodeTemplates.get(target).getEntry());
		TransitionAttributes attrs = transition.getAttrs();
		IterationBranchLabel<CFGNode> edgeColor = edgeColoring.get(edge);
		if(jTrans.getConfig().useProgressMeasure) {
			if(src instanceof InvokeNode) {
				attrs.appendUpdate("pm := pm + 1");
			} else {
				RelativeProgress<CFGNode> progress = jTrans.getProgress(cfg.getMethodInfo()).get(edge);
				String progressExpr = "pm := pm + " + progress.staticDiff;
				for(Entry<CFGNode, Long> loopDiff : progress.loopDiff.entrySet()) {
					progressExpr += String.format(" - %d * %s",loopDiff.getValue(),
							tBuilder.getLoopVar(loopDiff.getKey()));
				}
				attrs.appendUpdate(progressExpr);
			}
		}
		if(edgeColor != null) {
			for(CFGNode loop : edgeColor.getContinues()) {
				attrs.appendGuard(tBuilder.contLoopGuard(loop));
				attrs.appendUpdate(tBuilder.incrLoopCounter(loop));
			}
			for(CFGNode loop : edgeColor.getExits()) {
				attrs.appendGuard(tBuilder.exitLoopGuard(loop));
				attrs.appendUpdate(tBuilder.resetLoopCounter(loop));
			}
		}
		if(hols.contains(target) && ! backEdges.contains(edge)) {
			attrs.appendUpdate(tBuilder.resetLoopCounter(target));
		}
	}
	
	SubAutomaton createBasicBlock(int nID, long blockWCET) {
		Location bbNode = tBuilder.createLocation("N"+this.mId+"_"+nID);
		tBuilder.waitAtLocation(bbNode,blockWCET);
		return SubAutomaton.singleton(bbNode);		
	}

	public int getId() {
		return mId;
	}
}
