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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.CallStringProvider;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.LoopBound;
import com.jopdesign.common.code.SymbolicMarker;
import com.jopdesign.common.code.ControlFlowGraph.CFGEdge;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.SymbolicMarker.SymbolicMarkerType;
import com.jopdesign.common.graphutils.FlowGraph;
import com.jopdesign.common.graphutils.LoopColoring;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.annotations.LoopBoundExpr;
import com.jopdesign.wcet.ipet.IPETBuilder.ExecutionEdge;
import com.jopdesign.wcet.ipet.LinearConstraint.ConstraintType;

/**
 * Purpose: This class provides utility functions to build +IPET models
 *
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 */
public class IPETUtils {

	/**
	 * [NEW-GLOBAL-ANALYSIS]
	 * Build a constraint <pre>sum edges = C</pre>
	 * @param edges
	 * @param rhs
	 * @return
	 */
	public static <E>
	LinearConstraint<E>
	constantFlow(Iterable<E> edges, int rhs) {

		LinearConstraint<E> eq = new LinearConstraint<E>(ConstraintType.Equal);
		eq.addRHS(rhs);
        for (E ingoing : edges) {
        	eq.addLHS(ingoing);
        }
        return eq;
	}

	/**
	 * [NEW-GLOBAL-ANALYSIS]
	 * Build a constraint <pre>sum es1 = sum es2</pre>
	 * @param es1
	 * @param es2
	 * @return
	 */
	public static <E>
	LinearConstraint<E>
	flowPreservation(Iterable<E> es1, Iterable<E> es2) {

		LinearConstraint<E> eq = new LinearConstraint<E>(ConstraintType.Equal);
        for (E lhs : es1) {
        	eq.addLHS(lhs);
        }
        for (E rhs : es2) {
        	eq.addRHS(rhs);
        }
        return eq;
	}

	/**
	 * [NEW-GLOBAL-ANALYSIS]
	 * Build a constraint <pre>sum es1 &lt;= sum es2 * k</pre>
	 * @param es1 lhs
	 * @param es2 rhs
	 * @param k   multiplier for the right hand side
	 * @return a constraint that sum(es1) is less than or equal to sum(es2) * k
	 */
	public static <E>
	LinearConstraint<E>
	relativeBound(Iterable<E> es1, Iterable<E> es2, long k) {

		LinearConstraint<E> eq = new LinearConstraint<E>(ConstraintType.LessEqual);
        for (E lhs : es1) {
        	eq.addLHS(lhs);
        }
        for (E rhs : es2) {
        	eq.addRHS(rhs, k);
        }
        return eq;
	}



	/**
     * Top level flow constraints: flow out of entry and flow into exit must be one.
     * <pre>sum (e_Entry_x) = 1</pre>
     * <pre>sum (e_y_Exit) = 1</pre>
     */
	@Deprecated
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
                throw new AppInfoError("No loop bound record for head of loop: " + hol + " : " + g.buildLoopBoundMap());
            }
            for (LinearConstraint<IPETBuilder.ExecutionEdge> loopConstraint : 
            		constraintsForLoop(loops, hol, loopBound, ctx)) {        	
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
    	
    	ExecutionContext eCtx = new ExecutionContext(hol.getControlFlowGraph().getMethodInfo(), ctx.getCallString());
        List<LinearConstraint<IPETBuilder.ExecutionEdge>> loopConstraints = new ArrayList<LinearConstraint<IPETBuilder.ExecutionEdge>>();
        /* marker loop constraints */
        for (Entry<SymbolicMarker, LoopBoundExpr> markerBound : loopBound.getLoopBounds()) {

            /* loop constraint */
            LinearConstraint<IPETBuilder.ExecutionEdge> loopConstraint = new LinearConstraint<IPETBuilder.ExecutionEdge>(ConstraintType.GreaterEqual);
            for (ControlFlowGraph.CFGEdge continueEdge : loops.getBackEdgesTo(hol)) {
                loopConstraint.addRHS(ctx.newEdge(continueEdge));
            }
            /* Multiplicities */
            long lhsMultiplicity = markerBound.getValue().upperBound(eCtx);

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
     * Split an edge into a list of edges modeling low-level hardware decisions
     *
     * @param parentEdge the high-level edge to be split
     * @param childEdges low-level edges
     * @return constraint asserting sum f(childEdges) = f(parentEdge)
     */
    public static<E> LinearConstraint<E> lowLevelEdgeSplit(E parentEdge, E... childEdges) {
        LinearConstraint<E> lc = new LinearConstraint<E>(ConstraintType.Equal);
        lc.addLHS(parentEdge);
        for (E childEdge : childEdges) {
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

        for (CFGNode n : cfg.vertexSet()) {
            long nodeCost = nodeWCET.getCost(n);
            for (ControlFlowGraph.CFGEdge e : cfg.outgoingEdgesOf(n)) {
                ipetSolver.addEdgeCost(builder.newEdge(e), nodeCost);
            }
        }
        return ipetSolver;
    }

}
