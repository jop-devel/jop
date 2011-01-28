/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

package com.jopdesign.wcet.ipet;

import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.CallStringProvider;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.CFGEdge;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.LoopBound;
import com.jopdesign.common.code.SuperGraph;
import com.jopdesign.common.code.SymbolicMarker;
import com.jopdesign.common.code.SymbolicMarker.SymbolicMarkerType;
import com.jopdesign.common.graphutils.FlowGraph;
import com.jopdesign.common.graphutils.LoopColoring;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.ipet.IPETBuilder.ExecutionEdge;
import com.jopdesign.wcet.ipet.LinearConstraint.ConstraintType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Purpose: This class provides utility functions to build +IPET models
 *
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 */
public class IPETUtils {

    /**
     * Top level flow constraints: flow out of entry and flow into exit must be one.
     * <pre>sum (e_Entry_x) = 1</pre>
     * <pre>sum (e_y_Exit) = 1</pre>
     */
    public static <V, E, C extends CallStringProvider>
    List<LinearConstraint<IPETBuilder.ExecutionEdge>>
    structuralFlowConstraintsRoot(FlowGraph<V, E> g, IPETBuilder<C> ctx) {

        LinearConstraint<IPETBuilder.ExecutionEdge> entryLhsProto = new LinearConstraint<IPETBuilder.ExecutionEdge>(ConstraintType.Equal);
        entryLhsProto.addLHS(1);
        LinearConstraint<IPETBuilder.ExecutionEdge> exitRhsProto = new LinearConstraint<IPETBuilder.ExecutionEdge>(ConstraintType.Equal);
        exitRhsProto.addRHS(1);

        return structuralFlowConstraints(g, entryLhsProto, exitRhsProto, ctx);
    }

    /**
     * Structural flow constraints with given input and output edges.
     */
    public static <V, E, C extends CallStringProvider>
    List<LinearConstraint<IPETBuilder.ExecutionEdge>>
    structuralFlowConstraints(FlowGraph<V, E> g,
                              Collection<IPETBuilder.ExecutionEdge> inputEdges,
                              Collection<IPETBuilder.ExecutionEdge> outputEdges,
                              IPETBuilder<C> ctx) {

        LinearConstraint<IPETBuilder.ExecutionEdge> entryLhsProto = new LinearConstraint<IPETBuilder.ExecutionEdge>(ConstraintType.Equal);
        for (IPETBuilder.ExecutionEdge e : inputEdges) entryLhsProto.addLHS(e);
        LinearConstraint<IPETBuilder.ExecutionEdge> exitRhsProto = new LinearConstraint<IPETBuilder.ExecutionEdge>(ConstraintType.Equal);
        for (IPETBuilder.ExecutionEdge e : outputEdges) exitRhsProto.addRHS(e);

        return structuralFlowConstraints(g, entryLhsProto, exitRhsProto, ctx);
    }

    /**
     * Structural Flow Constraints:<ul>
     * <li/> The flow of entry and exit is constrained by the given left-hand resp. right-hand sides
     * <li/> For all other nodes, the incoming flow (left-hand) is equal to the outgoing flow (rhs)
     * </ul>
     *
     * @param <V>           node type
     * @param <E>           edge type
     * @param <C>           context type
     * @param graph         the flow graph to generate constraints for
     * @param entryLhsProto the left-hand side of the constraint for the entry node
     * @param exitRhsProto  the right-hand side of the constraint for the exit node
     * @param ctx           the execution context
     * @return the set of structural linear constraints
     */
    public static <V, E, C extends CallStringProvider>
    List<LinearConstraint<IPETBuilder.ExecutionEdge>>
    structuralFlowConstraints(FlowGraph<V, E> graph,
                              LinearConstraint<IPETBuilder.ExecutionEdge> entryLhsProto,
                              LinearConstraint<IPETBuilder.ExecutionEdge> exitRhsProto,
                              IPETBuilder<C> ctx) {

        List<LinearConstraint<IPETBuilder.ExecutionEdge>> constraints = new ArrayList<LinearConstraint<IPETBuilder.ExecutionEdge>>();

        for (V node : graph.vertexSet()) {
            LinearConstraint<IPETBuilder.ExecutionEdge> flowConstraint;

            if (node.equals(graph.getEntry())) {
                flowConstraint = entryLhsProto.clone();
            } else if (node.equals(graph.getExit())) {
                flowConstraint = exitRhsProto.clone();
            } else {
                flowConstraint = new LinearConstraint<IPETBuilder.ExecutionEdge>(ConstraintType.Equal);
            }
            for (E ingoing : graph.incomingEdgesOf(node)) {
                flowConstraint.addLHS(ctx.newEdge(ingoing));
            }
            for (E outgoing : graph.outgoingEdgesOf(node)) {
                flowConstraint.addRHS(ctx.newEdge(outgoing));
            }
            constraints.add(flowConstraint);
        }
        return constraints;
    }

    /**
     * Compute flow constraints: Loop Bound constraints (Control Flow Graph only)
     *
     * @param g   the flow graph
     * @param ctx the invocation context
     * @return A list of flow constraints
     */
    public static <C extends CallStringProvider>
    List<LinearConstraint<IPETBuilder.ExecutionEdge>>
    loopBoundConstraints(ControlFlowGraph g, IPETBuilder<C> ctx) {

        List<LinearConstraint<IPETBuilder.ExecutionEdge>> constraints = new ArrayList<LinearConstraint<IPETBuilder.ExecutionEdge>>();
        // - for each loop with bound B
        // -- sum(exit_loop_edges) * B <= sum(continue_loop_edges)
        LoopColoring<CFGNode, ControlFlowGraph.CFGEdge> loops = g.getLoopColoring();
        for (CFGNode hol : loops.getHeadOfLoops()) {
            //LoopBound loopBound = g.getLoopBound(hol, ctx.getCallString());
            LoopBound loopBound = ctx.getWCETTool().getLoopBound(hol, ctx.getCallString());


            if (loopBound == null) {
                throw new AppInfoError("No loop bound record for head of loop: " + hol + " : " + g.buildLoopBounds());
            }
            for (LinearConstraint<IPETBuilder.ExecutionEdge> loopConstraint : constraintsForLoop(loops, hol, loopBound, ctx)) {
                constraints.add(loopConstraint);
            }
        }
        return constraints;
    }

    /**
     * Generate Loop Constraints
     */
    public static <C extends CallStringProvider>
    List<LinearConstraint<IPETBuilder.ExecutionEdge>>
    constraintsForLoop(LoopColoring<CFGNode, ControlFlowGraph.CFGEdge> loops,
                       CFGNode hol,
                       LoopBound loopBound,
                       IPETBuilder<C> ctx)
    {

        List<LinearConstraint<IPETBuilder.ExecutionEdge>> loopConstraints = new ArrayList<LinearConstraint<IPETBuilder.ExecutionEdge>>();
        /* marker loop constraints */
        for (Entry<SymbolicMarker, Pair<Long, Long>> markerBound : loopBound.getLoopBounds()) {

            /* loop constraint */
            LinearConstraint<IPETBuilder.ExecutionEdge> loopConstraint = new LinearConstraint<IPETBuilder.ExecutionEdge>(ConstraintType.GreaterEqual);
            for (ControlFlowGraph.CFGEdge continueEdge : loops.getBackEdgesTo(hol)) {
                loopConstraint.addRHS(ctx.newEdge(continueEdge));
            }
            /* Multiplicities */
            long lhsMultiplicity = markerBound.getValue().second();
            SymbolicMarker marker = markerBound.getKey();
            if (marker.getMarkerType() == SymbolicMarkerType.OUTER_LOOP_MARKER) {

                CFGNode outerLoopHol;
                outerLoopHol = loops.getLoopAncestor(hol, marker.getOuterLoopDistance());
                if (outerLoopHol == null) {
                    // FIXME: [annotations] This is a user error, not an assertion error
                    throw new AssertionError("Invalid Loop Nest Level");
                }
                for (ControlFlowGraph.CFGEdge exitEdge : loops.getExitEdgesOf(outerLoopHol)) {
                    loopConstraint.addLHS(ctx.newEdge(exitEdge), lhsMultiplicity);
                }
            } else {
                assert (marker.getMarkerType() == SymbolicMarkerType.METHOD_MARKER);
                throw new AssertionError("ILPModelBuilder: method markers not yet supported, sorry");
            }
            loopConstraints.add(loopConstraint);
        }
        return loopConstraints;
    }

    /**
     * Generate constraints for super graph edges
     */
    public static <C extends CallStringProvider>
    LinearConstraint<IPETBuilder.ExecutionEdge>
    superEdgeConstraint(SuperGraph.SuperInvokeEdge in, SuperGraph.SuperReturnEdge out, IPETBuilder<C> ctx) {

        LinearConstraint<IPETBuilder.ExecutionEdge> pairConstraint = new LinearConstraint<IPETBuilder.ExecutionEdge>(ConstraintType.Equal);
        pairConstraint.addLHS(ctx.newEdge(in));
        pairConstraint.addRHS(ctx.newEdge(out));
        return pairConstraint;
    }


    /**
     * Compute flow constraints: Infeasible edge constraints
     *
     * @param g   the flow graph
     * @param ctx the invocation context
     * @return A list of flow constraints
     */
    public static <C extends CallStringProvider>
    List<LinearConstraint<ExecutionEdge>>
    infeasibleEdgeConstraints(ControlFlowGraph g, IPETBuilder<C> ctx) {

        List<LinearConstraint<ExecutionEdge>> constraints = new ArrayList<LinearConstraint<ExecutionEdge>>();
        // - for each infeasible edge
        // -- edge = 0
        for (CFGEdge edge : ctx.getWCETTool().getInfeasibleEdges(g, ctx.getCallString())) {
            LinearConstraint<ExecutionEdge> infeasibleConstraint = new LinearConstraint<ExecutionEdge>(ConstraintType.Equal);
            infeasibleConstraint.addLHS(ctx.newEdge(edge));
            infeasibleConstraint.addRHS(0);
            constraints.add(infeasibleConstraint);
        }
        return constraints;
    }

    /**
     * Compute flow constraints: invoke and return flow have to be equal
     *
     * @param call    the invoke edge
     * @param ret     the return edge
     * @param builder the IPET context
     * @return the linear constraints ensuring that each flow following the invoke is matched
     *         by one returning, and that non-local and local invoke flow coincide
     */
    public static <C extends CallStringProvider>
    List<LinearConstraint<ExecutionEdge>>
    invokeReturnConstraints(SuperGraph.SuperInvokeEdge call, SuperGraph.SuperReturnEdge ret, IPETBuilder<C> builder) {


        ExecutionEdge callEdge = builder.newEdge(call);
        List<LinearConstraint<ExecutionEdge>> constraints = new ArrayList<LinearConstraint<ExecutionEdge>>();

        {
            LinearConstraint<ExecutionEdge> invokeRetCnstr = new LinearConstraint<ExecutionEdge>(ConstraintType.Equal);
            invokeRetCnstr.addLHS(callEdge);
            invokeRetCnstr.addRHS(builder.newEdge(ret));
            constraints.add(invokeRetCnstr);
        }

        {
            LinearConstraint<ExecutionEdge> invokeLocalCnstr = new LinearConstraint<ExecutionEdge>(ConstraintType.Equal);
            invokeLocalCnstr.addLHS(builder.newEdge(call));
            ControlFlowGraph.InvokeNode invokeNode = call.getInvokeNode();
            Set<ControlFlowGraph.CFGEdge> localInvokeEdge = invokeNode.getControlFlowGraph().getGraph().outgoingEdgesOf(invokeNode);
            if (localInvokeEdge.size() == 1) {
                invokeLocalCnstr.addRHS(builder.newEdge(localInvokeEdge.iterator().next()));
            } else {
                throw new AssertionError("More than one outgoing local edge from an invoke node");
            }
            constraints.add(invokeLocalCnstr);
        }

        return constraints;
    }

    /**
     * Split an execution edge into a list of execution edges modelling low-level hardware decisions
     *
     * @param parentEdge the high-level edge to be split
     * @param childEdges low-level edges
     * @return constraint asserting sum f(childEdges) = f(parentEdge)
     */
    public static LinearConstraint<ExecutionEdge> lowLevelEdgeSplit(ExecutionEdge parentEdge, ExecutionEdge... childEdges) {
        LinearConstraint<ExecutionEdge> lc = new LinearConstraint<ExecutionEdge>(ConstraintType.Equal);
        lc.addLHS(parentEdge);
        for (ExecutionEdge childEdge : childEdges) {
            lc.addRHS(childEdge);
        }
        return lc;
    }

    /**
     * Create a max-cost maxflow problem for the given flow graph graph, based on a
     * given node to cost mapping.
     *
     * @param wcetTool    A reference to the WCETTool
     * @param problemName a unique identifier for the problem (for reporting)
     * @param cs          context of the method invocation
     * @param cfg         the graph
     * @param nodeWCET    cost of nodes
     * @return The max-cost maxflow problem
     */
    public static IPETSolver buildLocalILPModel(WCETTool wcetTool, String problemName, CallString cs,
                                                ControlFlowGraph cfg, CostProvider<CFGNode> nodeWCET,
                                                IPETConfig ipetConfig) {

        IPETSolver ipetSolver = new IPETSolver(problemName, ipetConfig);

        IPETBuilder<CallString> builder = new IPETBuilder<CallString>(wcetTool, cs);

        ipetSolver.addConstraints(IPETUtils.structuralFlowConstraintsRoot(cfg.getGraph(), builder));
        ipetSolver.addConstraints(IPETUtils.loopBoundConstraints(cfg, builder));
        ipetSolver.addConstraints(IPETUtils.infeasibleEdgeConstraints(cfg, builder));

        for (CFGNode n : cfg.getGraph().vertexSet()) {
            long nodeCost = nodeWCET.getCost(n);
            for (ControlFlowGraph.CFGEdge e : cfg.getGraph().outgoingEdgesOf(n)) {
                ipetSolver.addEdgeCost(builder.newEdge(e), nodeCost);
            }
        }
        return ipetSolver;
    }
}
