package com.jopdesign.wcet08.frontend;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.RETURN;
import org.jgrapht.DirectedGraph;

import com.jopdesign.wcet08.analysis.BlockWCET;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter;
import com.jopdesign.wcet08.graphutils.LoopColoring;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter.DOTLabeller;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter.DOTNodeLabeller;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter.DefaultDOTLabeller;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter.DefaultNodeLabeller;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter.MapLabeller;
import com.jopdesign.wcet08.graphutils.LoopColoring.IterationBranchLabel;

/**
 * Export information about the flow graph and create a DOT graph
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 */
public class CFGExport {
	public class FGCustomNodeLabeller extends   MapLabeller<CFGNode> 
								 	 implements DOTNodeLabeller<CFGNode> {
		public FGCustomNodeLabeller(Map<CFGNode, ?> nodeAnnotations) {
			super(nodeAnnotations);
		}
		public String getLabel(CFGNode object) {
			if(annots.containsKey(object)) {
				return "[" +object.getName() + "] "+ annots.get(object);
			} else {
				return super.getLabel(object);
			}			
		}
		public int getID(CFGNode node) { return node.getId(); }
	}
	public class FGEdgeLabeller extends DefaultDOTLabeller<CFGEdge> {		
		public boolean setAttributes(CFGEdge edge, Map<String,String> ht) {
			super.setAttributes(edge,ht);
			if(flowGraph.getLoopColoring().isBackEdge(edge)) ht.put("arrowhead", "empty");
			if(flowGraph.getLoopColoring().getLoopEntrySet(edge).size() > 0) ht.put("arrowhead", "diamond");
			return true;
		}
		public String getLabel(CFGEdge edge) {
			StringBuilder lab = new StringBuilder();
			switch(edge.getKind()) {
			case EXIT_EDGE : lab.append("return");break;
			case ENTRY_EDGE : lab.append("entry");break;
			default: break;
			}
			IterationBranchLabel<CFGNode> branchLabels = 
				flowGraph.getLoopColoring().getIterationBranchEdges().get(edge);
			if(branchLabels != null && ! branchLabels.isEmpty()) {
				lab.append("{");boolean mark=false;
				if(! branchLabels.getContinues().isEmpty()) {
					lab.append("cont:");
					for(CFGNode n : branchLabels.getContinues()) { lab.append(n.getId()+" "); }
					mark=true;
				}
				if(! branchLabels.getExits().isEmpty()) {
					if(mark) lab.append(";");
					lab.append( "exit:");
					for(CFGNode n : branchLabels.getExits()) { lab.append(n.getId()+" "); }
				}
				lab.append("}");
			}
			return lab.toString();
		}
	}
	public class FGNodeLabeller extends DefaultNodeLabeller<CFGNode> {
		@Override
		public boolean setAttributes(CFGNode node, Map<String,String> ht) {
			if(node instanceof BasicBlockNode) {
				setBasicBlockAttributes((BasicBlockNode) node, ht);
			} else {
				ht.put("label", node.toString());
			}
			return true;
		}
		private void setBasicBlockAttributes(BasicBlockNode n, Map<String,String> ht) {
			BasicBlock codeBlock = n.getBasicBlock();
			Instruction lastInstr = codeBlock.getLastInstruction().getInstruction();
			InvokeInstruction invInstr = 
				(lastInstr instanceof InvokeInstruction) ? ((InvokeInstruction)lastInstr) : null;
			boolean isReturn = lastInstr instanceof RETURN;
			LoopColoring<CFGNode, CFGEdge> loops = flowGraph.getLoopColoring();
			StringBuilder nodeInfo = new StringBuilder();
			nodeInfo.append('#');
			nodeInfo.append(n.getId());
			nodeInfo.append(' ');
			String infoHeader;
			if(invInstr != null) {
				infoHeader = "{invoke "+
							 codeBlock.getAppInfo().getReferenced(codeBlock.getMethodInfo().getCli(), invInstr) +
							 "}";
			} else if(isReturn) {
				infoHeader = "{return ";
			} else if(codeBlock.getBranchInstruction() != null) {
				BranchInstruction instr = codeBlock.getBranchInstruction();
				infoHeader = "{"+ instr.getName() + " ";
			} else {
				infoHeader = "{simple ";
			}
			nodeInfo.append(infoHeader);
			nodeInfo.append("{"+codeBlock.getNumberOfBytes()+" By, ");
			nodeInfo.append(BlockWCET.basicBlockWCETEstimate(codeBlock)+" Cyc");
			if(loops.getHeadOfLoops().contains(n)) {
				nodeInfo.append(", LOOP "+n.getId()+"/"+flowGraph.getLoopBounds().get(n));
			}
			nodeInfo.append("}\n");
			nodeInfo.append(codeBlock.dump());
			ht.put("label",nodeInfo.toString());
			if(! loops.getHeadOfLoops().isEmpty()) {
				if(flowGraph.getLoopColoring().getLoopColors().get(n) == null) {
					WcetAppInfo.logger.error("No loop coloring for node "+n+" (dead code?)");
				} else {
					ht.put("peripheries",""+(1+flowGraph.getLoopColoring().getLoopColors().get(n).size()));
				}
			}
		}		
	}

	private ControlFlowGraph flowGraph;
	private DOTNodeLabeller<CFGNode> nl;
	private DOTLabeller<CFGEdge> el;

	public CFGExport(ControlFlowGraph g) {
		this.flowGraph = g;
	}
	public CFGExport(ControlFlowGraph graph, Map<CFGNode, ?> nodeAnnotations, Map<CFGEdge, ?> edgeAnnotations) {
		this(graph);
		if(nodeAnnotations != null) {
			this.nl = new FGCustomNodeLabeller(nodeAnnotations);
		}
		if(edgeAnnotations != null) {
			this.el = new MapLabeller<CFGEdge>(edgeAnnotations);
		}
	}

	public void exportDOT(Writer writer, DirectedGraph<CFGNode, CFGEdge> graph) throws IOException {
		if(nl == null) nl = new FGNodeLabeller();
		if(el == null) el = new FGEdgeLabeller();
		AdvancedDOTExporter<CFGNode,CFGEdge> dotExport = 
			new AdvancedDOTExporter<CFGNode,CFGEdge>(nl,el);
		dotExport.exportDOT(writer, flowGraph.getGraph());
	}
}
