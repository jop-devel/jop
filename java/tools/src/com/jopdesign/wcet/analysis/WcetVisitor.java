package com.jopdesign.wcet.analysis;

import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.wcet.Project;

public abstract class WcetVisitor implements ControlFlowGraph.CfgVisitor {
	protected WcetCost cost;
	protected Project project;
	public WcetVisitor(Project p) {
		this.project = p;
		this.cost = null;
	}
	public void visitSpecialNode(ControlFlowGraph.DedicatedNode n) {
	}
	public void visitSummaryNode(ControlFlowGraph.SummaryNode n) {
		throw new AssertionError("summary nodes not supported using this WCET calculation method");
	}

	public abstract void visitInvokeNode(ControlFlowGraph.InvokeNode n);

	public abstract void visitBasicBlockNode(ControlFlowGraph.BasicBlockNode n);

	public WcetCost computeCost(ControlFlowGraph.CFGNode n) {
		this.cost = new WcetCost();
		n.accept(this);
		return cost;
	}
}

