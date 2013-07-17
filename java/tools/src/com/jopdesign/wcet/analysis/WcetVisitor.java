/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.wcet.analysis;

import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ControlFlowGraph.CfgVisitor;
import com.jopdesign.wcet.WCETTool;

public abstract class WcetVisitor implements CfgVisitor {
	protected WcetCost cost;
	protected WCETTool project;

	public WcetVisitor(WCETTool p) {
		this.project = p;
		this.cost = null;
	}

	public void visitVirtualNode(ControlFlowGraph.VirtualNode n) {
	}

	public void visitSummaryNode(ControlFlowGraph.SummaryNode n) {
		throw new AssertionError("summary nodes not supported using this WCET calculation method");
	}

	public abstract void visitInvokeNode(ControlFlowGraph.InvokeNode n);

	public abstract void visitBasicBlockNode(BasicBlockNode n);

	public WcetCost computeCost(CFGNode n) {
		this.cost = new WcetCost();
		n.accept(this);
		return cost;
	}
}

