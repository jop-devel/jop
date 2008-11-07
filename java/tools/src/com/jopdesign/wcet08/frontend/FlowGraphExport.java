package com.jopdesign.wcet08.frontend;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.RETURN;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import com.jopdesign.wcet08.frontend.FlowGraph.BasicBlockNode;
import com.jopdesign.wcet08.frontend.FlowGraph.FlowGraphEdge;
import com.jopdesign.wcet08.frontend.FlowGraph.FlowGraphNode;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter.DOTLabeller;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter.DOTNodeLabeller;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter.DefaultDOTLabeller;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter.DefaultNodeLabeller;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter.MapLabeller;

/**
 * Export information about the flow graph and create a DOT graph
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 */
public class FlowGraphExport {
	public class FGCustomNodeLabeller extends   MapLabeller<FlowGraphNode> 
								 	 implements DOTNodeLabeller<FlowGraphNode> {
		public FGCustomNodeLabeller(Map<FlowGraphNode, ?> nodeAnnotations) {
			super(nodeAnnotations);
		}
		public String getLabel(FlowGraphNode object) {
			if(annots.containsKey(object)) {
				return "[" +object.getId() + "]"+ annots.get(object);
			} else {
				return super.getLabel(object);
			}			
		}
		public int getID(FlowGraphNode node) { return node.getId(); }
	}
	public class FGEdgeLabeller extends DefaultDOTLabeller<FlowGraphEdge> {		
		public boolean setAttributes(FlowGraphEdge edge, Map<String,String> ht) {
			super.setAttributes(edge,ht);
			if(flowGraph.isBackEdge(edge)) ht.put("arrowhead", "empty");
			if(flowGraph.getLoopEntrySet(edge).size() > 0) ht.put("arrowhead", "diamond");
			return true;
		}
		public String getLabel(FlowGraphEdge edge) {
			StringBuilder lab = new StringBuilder();
			switch(edge.getKind()) {
			case EXIT_EDGE : lab.append("return");break;
			case ENTRY_EDGE : lab.append("entry");break;
			default: break;
			}
			Set<FlowGraphNode> exits = flowGraph.getLoopExitSet(edge);
			if(exits.size() > 0) {
				lab.append("{exit ");
				for(FlowGraphNode n : exits) { lab.append(n.getId()); }
				lab.append("}");
			}
			return lab.toString();
		}
	}
	public class FGNodeLabeller extends DefaultNodeLabeller<FlowGraphNode> {
		@Override
		public boolean setAttributes(FlowGraphNode node, Map<String,String> ht) {
			if(node instanceof BasicBlockNode) {
				setBasicBlockAttributes((BasicBlockNode) node, ht);
			} else {
				ht.put("label", node.toString());
			}
			return true;
		}
		private void setBasicBlockAttributes(BasicBlockNode n, Map<String,String> ht) {
			BasicBlock codeBlock = n.getCodeBlock();
			Instruction lastInstr = codeBlock.getLastInstruction().getInstruction();
			InvokeInstruction invInstr = 
				(lastInstr instanceof InvokeInstruction) ? ((InvokeInstruction)lastInstr) : null;
			boolean isReturn = lastInstr instanceof RETURN;
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
			nodeInfo.append(flowGraph.basicBlockWCETEstimate(codeBlock)+" Cyc");
			if(flowGraph.isHeadOfLoop(n)) {
				nodeInfo.append(", LOOP "+n.getId()+"/"+flowGraph.getLoopBounds().get(n));
			}
			nodeInfo.append("}");
			for(InstructionHandle ih: codeBlock.getInstructions()) {
				nodeInfo.append("\\n");
				nodeInfo.append(ih.toString());
			}			
			ht.put("label",nodeInfo.toString());
			if(! flowGraph.getHeadOfLoops().isEmpty()) {
				ht.put("peripheries",""+(1+flowGraph.getLoopColoring().getLoopColors().get(n).size()));
			}
		}		
	}

	private FlowGraph flowGraph;
	private DOTNodeLabeller<FlowGraphNode> nl;
	private DOTLabeller<FlowGraphEdge> el;

	public FlowGraphExport(FlowGraph g) {
		this.flowGraph = g;
	}
	public FlowGraphExport(FlowGraph graph, Map<FlowGraphNode, ?> nodeAnnotations, Map<FlowGraphEdge, ?> edgeAnnotations) {
		this(graph);
		if(nodeAnnotations != null) {
			this.nl = new FGCustomNodeLabeller(nodeAnnotations);
		}
		if(edgeAnnotations != null) {
			this.el = new MapLabeller<FlowGraphEdge>(edgeAnnotations);
		}
	}

	public void exportDOT(Writer writer, DirectedGraph<FlowGraphNode, FlowGraphEdge> graph) throws IOException {
		if(nl == null) nl = new FGNodeLabeller();
		if(el == null) el = new FGEdgeLabeller();
		AdvancedDOTExporter<FlowGraphNode,FlowGraphEdge> dotExport = 
			new AdvancedDOTExporter<FlowGraphNode,FlowGraphEdge>(nl,el);
		dotExport.exportDOT(writer, flowGraph.getGraph());
	}
}
