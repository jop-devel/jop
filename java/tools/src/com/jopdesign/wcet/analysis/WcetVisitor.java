package com.jopdesign.wcet.analysis;

import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CfgVisitor;
import com.jopdesign.wcet.frontend.ControlFlowGraph.DedicatedNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.SummaryNode;

public abstract class WcetVisitor implements CfgVisitor {
	protected WcetCost cost;
	protected Project project;
	public WcetVisitor(Project p) {
		this.project = p;
		this.cost = null;
	}
	public void visitSpecialNode(DedicatedNode n) {
	}
	public void visitSummaryNode(SummaryNode n) {
		throw new AssertionError("summary nodes not supported using this WCET calculation method");
	}
	public abstract void visitInvokeNode(InvokeNode n);
	public void visitBasicBlockNode(BasicBlockNode n) {
		cost.addLocalCost(project.getProcessorModel().basicBlockWCET(n.getBasicBlock()));
	}
	public WcetCost computeCost(CFGNode n) {
		this.cost = new WcetCost();
		n.accept(this);
		return cost;
	}
}

