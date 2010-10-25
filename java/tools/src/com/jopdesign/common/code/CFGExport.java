/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)
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

package com.jopdesign.common.code;

import com.jopdesign.common.graph.AdvancedDOTExporter;
import com.jopdesign.common.graph.LoopColoring;
import com.jopdesign.common.logger.LogConfig;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Export information about the flow graph and create a DOT graph
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 */
public class CFGExport {

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_CFG + ".CFGExport");

	public class FGCustomNodeLabeller extends AdvancedDOTExporter.MapLabeller<ControlFlowGraph.CFGNode>
								 	 implements AdvancedDOTExporter.DOTNodeLabeller<ControlFlowGraph.CFGNode> {
		public FGCustomNodeLabeller(Map<ControlFlowGraph.CFGNode, ?> nodeAnnotations) {
			super(nodeAnnotations);
		}
		public String getLabel(ControlFlowGraph.CFGNode object) {
			if(annots.containsKey(object)) {
				return "[" +object.getName() + "] "+ annots.get(object);
			} else {
				return super.getLabel(object);
			}
		}
		public int getID(ControlFlowGraph.CFGNode node) { return node.getId(); }
	}
	public class FGEdgeLabeller extends AdvancedDOTExporter.DefaultDOTLabeller<ControlFlowGraph.CFGEdge> {
		public boolean setAttributes(ControlFlowGraph.CFGEdge edge, Map<String,String> ht) {
			super.setAttributes(edge,ht);
			if(flowGraph.getLoopColoring().isBackEdge(edge)) ht.put("arrowhead", "empty");
			if(flowGraph.getLoopColoring().getLoopEntrySet(edge).size() > 0) ht.put("arrowhead", "diamond");
			return true;
		}
		public String getLabel(ControlFlowGraph.CFGEdge edge) {
			StringBuilder lab = new StringBuilder();
			switch(edge.getKind()) {
			case EXIT_EDGE : lab.append("return");break;
			case ENTRY_EDGE : lab.append("entry");break;
			default: break;
			}
			LoopColoring.IterationBranchLabel<ControlFlowGraph.CFGNode> branchLabels =
				flowGraph.getLoopColoring().getIterationBranchEdges().get(edge);
			if(branchLabels != null && ! branchLabels.isEmpty()) {
				lab.append("{");boolean mark=false;
				if(! branchLabels.getContinues().isEmpty()) {
					lab.append("cont:");
					for(ControlFlowGraph.CFGNode n : branchLabels.getContinues()) { lab.append(n.getId()+" "); }
					mark=true;
				}
				if(! branchLabels.getExits().isEmpty()) {
					if(mark) lab.append(";");
					lab.append( "exit:");
					for(ControlFlowGraph.CFGNode n : branchLabels.getExits()) { lab.append(n.getId()+" "); }
				}
				lab.append("}");
			}
			return lab.toString();
		}
	}
	public class FGNodeLabeller extends AdvancedDOTExporter.DefaultNodeLabeller<ControlFlowGraph.CFGNode> {
		@Override
		public boolean setAttributes(ControlFlowGraph.CFGNode node, Map<String,String> ht) {
			if(node instanceof ControlFlowGraph.BasicBlockNode) {
				setBasicBlockAttributes((ControlFlowGraph.BasicBlockNode) node, ht);
			} else {
				ht.put("label", node.toString());
			}
			return true;
		}
		private void setBasicBlockAttributes(ControlFlowGraph.BasicBlockNode n, Map<String,String> ht) {
			BasicBlock codeBlock = n.getBasicBlock();
			Instruction lastInstr = codeBlock.getLastInstruction().getInstruction();
			boolean isReturn = lastInstr instanceof ReturnInstruction;
			LoopColoring<ControlFlowGraph.CFGNode, ControlFlowGraph.CFGEdge> loops = flowGraph.getLoopColoring();
			StringBuilder nodeInfo = new StringBuilder();
			nodeInfo.append('#');
			nodeInfo.append(n.getId());
			nodeInfo.append(' ');
			String infoHeader;
			if(n instanceof ControlFlowGraph.InvokeNode) {
				ControlFlowGraph.InvokeNode in = (ControlFlowGraph.InvokeNode) n;
				infoHeader = "{invoke "+ (in.isVirtual()
						                  ? ("virtual "+in.getReferenced())
						                  : in.getImplementedMethod().getFQMethodName()) +"}";
			} else if(isReturn) {
				infoHeader = "{return}";
			} else if(codeBlock.getBranchInstruction() != null) {
				BranchInstruction instr = codeBlock.getBranchInstruction();
				infoHeader = "{"+ instr.getName() + "}";
			} else {
				infoHeader = "{simple}";
			}
			nodeInfo.append(infoHeader);
			nodeInfo.append("{"+codeBlock.getNumberOfBytes()+" By, ");
			nodeInfo.append(n.getBasicBlock().getAppInfo().getProcessorModel().basicBlockWCET(
					new ExecutionContext(codeBlock.getMethodInfo()),
					codeBlock)+" Cyc");
			if(loops.getHeadOfLoops().contains(n)) {
                /* -- TODO comment for commit
				nodeInfo.append(", LOOP "+n.getId()+"/"+flowGraph.getLoopBounds().get(n));
				-- */
			}
			nodeInfo.append("}\n");
			nodeInfo.append(codeBlock.dump());
			ht.put("label",nodeInfo.toString());
			if(! loops.getHeadOfLoops().isEmpty()) {
				if(flowGraph.getLoopColoring().getLoopColors().get(n) == null) {
					logger.error("No loop coloring for node "+n+" (dead code?)");
				} else {
					ht.put("peripheries",""+(1+flowGraph.getLoopColoring().getLoopColors().get(n).size()));
				}
			}
		}
	}

	private ControlFlowGraph flowGraph;
	private AdvancedDOTExporter.DOTNodeLabeller<ControlFlowGraph.CFGNode> nl;
	private AdvancedDOTExporter.DOTLabeller<ControlFlowGraph.CFGEdge> el;

	public CFGExport(ControlFlowGraph g) {
		this.flowGraph = g;
	}
	public CFGExport(ControlFlowGraph graph, Map<ControlFlowGraph.CFGNode, ?> nodeAnnotations, Map<ControlFlowGraph.CFGEdge, ?> edgeAnnotations) {
		this(graph);
		if(nodeAnnotations != null) {
			this.nl = new FGCustomNodeLabeller(nodeAnnotations);
		}
		if(edgeAnnotations != null) {
			this.el = new AdvancedDOTExporter.MapLabeller<ControlFlowGraph.CFGEdge>(edgeAnnotations);
		}
	}

	public void exportDOT(Writer writer, DirectedGraph<ControlFlowGraph.CFGNode, ControlFlowGraph.CFGEdge> graph) throws IOException {
		if(nl == null) nl = new FGNodeLabeller();
		if(el == null) el = new FGEdgeLabeller();
		AdvancedDOTExporter<ControlFlowGraph.CFGNode, ControlFlowGraph.CFGEdge> dotExport =
			new AdvancedDOTExporter<ControlFlowGraph.CFGNode, ControlFlowGraph.CFGEdge>(nl,el);
		dotExport.exportDOT(writer, flowGraph.getGraph());
	}
}
