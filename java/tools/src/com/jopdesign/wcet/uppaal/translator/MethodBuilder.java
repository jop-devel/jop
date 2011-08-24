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

import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ControlFlowGraph.CfgVisitor;
import com.jopdesign.common.code.ControlFlowGraph.ReturnNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.graphutils.LoopColoring;
import com.jopdesign.common.graphutils.ProgressMeasure.RelativeProgress;
import com.jopdesign.wcet.WCETProcessorModel;
import com.jopdesign.wcet.analysis.AnalysisContextLocal;
import com.jopdesign.wcet.analysis.LocalAnalysis;
import com.jopdesign.wcet.analysis.RecursiveWcetAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.analysis.cache.MethodCacheAnalysis;
import com.jopdesign.wcet.ipet.IPETConfig.CacheCostCalculationMethod;
import com.jopdesign.wcet.uppaal.model.Location;
import com.jopdesign.wcet.uppaal.model.Transition;
import com.jopdesign.wcet.uppaal.model.TransitionAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
		/* Translate the CFGs nodes */
		for(CFGNode node : cfg.vertexSet()) {
			node.accept(this);
		}
		/* Translate the CFGs edges */
		for(ControlFlowGraph.CFGEdge edge : cfg.edgeSet()) {
			buildEdge(edge);
		}
	}

	public void visitVirtualNode(ControlFlowGraph.VirtualNode n) {
		SubAutomaton localTranslation = null;
		switch(n.getKind()) {
		case ENTRY:  localTranslation = SubAutomaton.singleton(methodAuto.getEntry()); break;
		case EXIT:   localTranslation = SubAutomaton.singleton(methodAuto.getExit()); break;
		case SPLIT:  localTranslation = createSpecialCommited("SPLIT_"+n.getId(), n);break;
		case JOIN:   localTranslation = createSpecialCommited("JOIN_"+n.getId(), n);break;
		case RETURN: localTranslation = createSpecialCommited("RETURN_"+n.getId(), n);break;
		}
		this.nodeTemplates.put(n,localTranslation);
	}
	
	public void visitReturnNode(ReturnNode n) {
		visitVirtualNode(n);
	}
	
	private SubAutomaton createSpecialCommited(String name, ControlFlowGraph.VirtualNode n) {
		Location split = tBuilder.createLocation(name+"_"+this.mId+"_"+n.getId());
		split.setCommited();
		return SubAutomaton.singleton(split);
	}

	public void visitBasicBlockNode(BasicBlockNode n) {
		ExecutionContext ctx = new ExecutionContext(n.getBasicBlock().getMethodInfo());
		SubAutomaton bbLoc =
			createBasicBlock(n.getId(),jTrans.getProject().getWCETProcessorModel().basicBlockWCET(ctx,n.getBasicBlock()));
		this.nodeTemplates.put(n,bbLoc);
	}

	public void visitInvokeNode(ControlFlowGraph.InvokeNode n) {

		ExecutionContext ctx = new ExecutionContext(n.getBasicBlock().getMethodInfo());
		MethodCacheAnalysis mca = new MethodCacheAnalysis(jTrans.getProject());
		WCETProcessorModel proc = jTrans.getProject().getWCETProcessorModel();
		SubAutomaton invokeAuto;
		long staticWCET = proc.basicBlockWCET(ctx, n.getBasicBlock());
		if(jTrans.getCacheSim().isAlwaysMiss()) {
			staticWCET += mca.getInvokeReturnMissCost(n.getInvokeSite(),CallString.EMPTY);
		}
		invokeAuto = invokeBuilder.translateInvoke(this,n,staticWCET);
		this.nodeTemplates.put(n,invokeAuto);
	}

	public void visitSummaryNode(ControlFlowGraph.SummaryNode n) {
		RecursiveWcetAnalysis<AnalysisContextLocal> an =
			new RecursiveWcetAnalysis<AnalysisContextLocal>(
					jTrans.getProject(),
					new LocalAnalysis());
		WcetCost cost = an.runWCETComputation("SUBGRAPH"+n.getId(),
				n.getSubGraph(),
				new AnalysisContextLocal(CacheCostCalculationMethod.ALWAYS_MISS)
		).getTotalCost();
		SubAutomaton sumLoc = createBasicBlock(n.getId(),cost.getCost());
		this.nodeTemplates.put(n,sumLoc);
	}

	private void buildEdge(ControlFlowGraph.CFGEdge edge) {

		Set<CFGNode> hols = cfg.getLoopColoring().getHeadOfLoops();
		Set<ControlFlowGraph.CFGEdge> backEdges = cfg.getLoopColoring().getBackEdges();
		Map<ControlFlowGraph.CFGEdge, LoopColoring.IterationBranchLabel<CFGNode>> edgeColoring =
			cfg.getLoopColoring().getIterationBranchEdges();
		CFGNode src = cfg.getEdgeSource(edge);
		CFGNode target = cfg.getEdgeTarget(edge);
		if(src == cfg.getEntry() && target == cfg.getExit()) return;
		Transition transition = tBuilder.createTransition(
				nodeTemplates.get(src).getExit(),
				nodeTemplates.get(target).getEntry());
		TransitionAttributes attrs = transition.getAttrs();
		LoopColoring.IterationBranchLabel<CFGNode> edgeColor = edgeColoring.get(edge);
		if(jTrans.getConfig().useProgressMeasure) {
			if(src instanceof ControlFlowGraph.InvokeNode) {
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
