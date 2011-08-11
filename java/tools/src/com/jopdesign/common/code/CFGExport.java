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

import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGEdge;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.graphutils.AdvancedDOTExporter;
import com.jopdesign.common.graphutils.AdvancedDOTExporter.DOTLabeller;
import com.jopdesign.common.graphutils.AdvancedDOTExporter.DOTNodeLabeller;
import com.jopdesign.common.graphutils.LoopColoring;
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

    public class FGCustomNodeLabeller extends AdvancedDOTExporter.MapLabeller<CFGNode>
            implements AdvancedDOTExporter.DOTNodeLabeller<CFGNode> {
        public FGCustomNodeLabeller(Map<CFGNode, ?> nodeAnnotations) {
            super(nodeAnnotations);
        }

        public String getLabel(CFGNode object) {
            if (annots.containsKey(object)) {
                return "[" + object.getName() + "] " + annots.get(object);
            } else {
                return super.getLabel(object);
            }
        }

        public int getID(CFGNode node) {
            return node.getId();
        }
    }

    public class FGEdgeLabeller extends AdvancedDOTExporter.DefaultDOTLabeller<ControlFlowGraph.CFGEdge> {

        public boolean setAttributes(ControlFlowGraph.CFGEdge edge, Map<String, String> ht) {
            super.setAttributes(edge, ht);
            if (flowGraph.getLoopColoring().isBackEdge(edge)) ht.put("arrowhead", "empty");
            if (flowGraph.getLoopColoring().getLoopEntrySet(edge).size() > 0) ht.put("arrowhead", "diamond");
            return true;
        }

        public String getLabel(ControlFlowGraph.CFGEdge edge) {
            StringBuilder lab = new StringBuilder();
            switch (edge.getKind()) {
                case EXIT_EDGE:
                    lab.append("return");
                    break;
                case ENTRY_EDGE:
                    lab.append("entry");
                    break;
                default:
                    break;
            }
            LoopColoring.IterationBranchLabel<CFGNode> branchLabels =
                    flowGraph.getLoopColoring().getIterationBranchEdges().get(edge);
            if (branchLabels != null && !branchLabels.isEmpty()) {
                lab.append("{");
                boolean mark = false;
                if (!branchLabels.getContinues().isEmpty()) {
                    lab.append("cont:");
                    for (CFGNode n : branchLabels.getContinues()) {
                        lab.append(n.getId() + " ");
                    }
                    mark = true;
                }
                if (!branchLabels.getExits().isEmpty()) {
                    if (mark) lab.append(";");
                    lab.append("exit:");
                    for (CFGNode n : branchLabels.getExits()) {
                        lab.append(n.getId() + " ");
                    }
                }
                lab.append("}");
            }
            return lab.toString();
        }
    }

    public static class FGNodeLabeller extends AdvancedDOTExporter.DefaultNodeLabeller<CFGNode> {
        @Override
        public boolean setAttributes(CFGNode node, Map<String, String> ht) {
            if (node instanceof BasicBlockNode) {
                setBasicBlockAttributes((BasicBlockNode) node, ht);
            } else {
                ht.put("label", node.toString());
            }
            return true;
        }

        protected void addNodeLabel(BasicBlockNode n, StringBuilder nodeInfo) {
        }

        private void setBasicBlockAttributes(BasicBlockNode n, Map<String, String> ht) {
            BasicBlock codeBlock = n.getBasicBlock();
            ControlFlowGraph flowGraph = n.getControlFlowGraph();
            Instruction lastInstr = codeBlock.getLastInstruction().getInstruction();
            boolean isReturn = lastInstr instanceof ReturnInstruction;
            LoopColoring<CFGNode, ControlFlowGraph.CFGEdge> loops = flowGraph.getLoopColoring();
            StringBuilder nodeInfo = new StringBuilder();
            nodeInfo.append('#');
            nodeInfo.append(n.getId());
            nodeInfo.append(' ');
            String infoHeader;
            if (n instanceof ControlFlowGraph.InvokeNode) {
                ControlFlowGraph.InvokeNode in = (ControlFlowGraph.InvokeNode) n;
                infoHeader = "{invoke " + (in.isVirtual()
                        ? ("virtual " + in.getReferenced())
                        : in.getImplementingMethod().getFQMethodName()) + "}";
            } else if (isReturn) {
                infoHeader = "{return}";
            } else if (codeBlock.getBranchInstruction() != null) {
                BranchInstruction instr = codeBlock.getBranchInstruction();
                infoHeader = "{" + instr.getName() + "}";
            } else {
                infoHeader = "{simple}";
            }
            nodeInfo.append(infoHeader);
            nodeInfo.append("{" + codeBlock.getNumberOfBytes() + " By, ");
            addNodeLabel(n, nodeInfo);
            if (loops.getHeadOfLoops().contains(n)) {
                nodeInfo.append("LOOP " + n.getId() + "/" + n.getLoopBound());
            }
            nodeInfo.append("}\n");
            nodeInfo.append(codeBlock.dump());
            ht.put("label", nodeInfo.toString());
            if (!loops.getHeadOfLoops().isEmpty()) {
                if (flowGraph.getLoopColoring().getLoopColors().get(n) == null) {
                    logger.error("No loop coloring for node " + n + " (dead code?)");
                } else {
                    ht.put("peripheries", "" + (1 + flowGraph.getLoopColoring().getLoopColors().get(n).size()));
                }
            }
        }
    }

    private ControlFlowGraph flowGraph;
    private AdvancedDOTExporter.DOTNodeLabeller<CFGNode> nl;
    private AdvancedDOTExporter.DOTLabeller<ControlFlowGraph.CFGEdge> el;

    public CFGExport(ControlFlowGraph g) {
        this.flowGraph = g;
    }

    public CFGExport(ControlFlowGraph flowGraph, DOTNodeLabeller<CFGNode> nl, DOTLabeller<CFGEdge> el) {
        this.flowGraph = flowGraph;
        this.nl = nl;
        this.el = el;
    }

    public CFGExport(ControlFlowGraph graph, Map<CFGNode, ?> nodeAnnotations, Map<ControlFlowGraph.CFGEdge, ?> edgeAnnotations) {
        this(graph);
        if (nodeAnnotations != null) {
            this.nl = new FGCustomNodeLabeller(nodeAnnotations);
        }
        if (edgeAnnotations != null) {
            this.el = new AdvancedDOTExporter.MapLabeller<ControlFlowGraph.CFGEdge>(edgeAnnotations);
        }
    }

    public void exportDOT(Writer writer, DirectedGraph<CFGNode, ControlFlowGraph.CFGEdge> graph) throws IOException {
        if (nl == null) nl = new FGNodeLabeller();
        if (el == null) el = new FGEdgeLabeller();
        AdvancedDOTExporter<CFGNode, ControlFlowGraph.CFGEdge> dotExport =
                new AdvancedDOTExporter<CFGNode, ControlFlowGraph.CFGEdge>(nl, el);
        dotExport.exportDOT(writer, flowGraph.getGraph());
    }
}
