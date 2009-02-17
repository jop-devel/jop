package com.jopdesign.wcet08.analysis;

import org.apache.bcel.generic.InstructionHandle;

import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.frontend.BasicBlock;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CfgVisitor;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.DedicatedNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.SummaryNode;

public class WcetVisitor implements CfgVisitor {
	WcetCost cost;
	Project project;
	public WcetVisitor() {
		this.cost = new WcetCost();
	}
	public void visitSpecialNode(DedicatedNode n) {
	}
	public void visitSummaryNode(SummaryNode n) {
	}
	/* should be overriden by local/global analysis */
	public void visitInvokeNode(InvokeNode n) {
		visitBasicBlockNode(n);
	}
	public void visitBasicBlockNode(BasicBlockNode n) {
		cost.addLocalCost(project.getProcessorModel().basicBlockWCET(n.getBasicBlock()));
	}
}

