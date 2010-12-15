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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import com.jopdesign.dfa.framework.CallString;
import com.jopdesign.dfa.framework.CallStringProvider;
import com.jopdesign.wcet.annotations.LoopBound;
import com.jopdesign.wcet.annotations.SymbolicMarker;
import com.jopdesign.wcet.annotations.SymbolicMarker.SymbolicMarkerType;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.frontend.SuperGraph.SuperInvokeEdge;
import com.jopdesign.wcet.frontend.SuperGraph.SuperReturnEdge;
import com.jopdesign.wcet.graphutils.FlowGraph;
import com.jopdesign.wcet.graphutils.LoopColoring;
import com.jopdesign.wcet.graphutils.Pair;
import com.jopdesign.wcet.ipet.IPETBuilder.ExecutionEdge;
import com.jopdesign.wcet.ipet.LinearConstraint.ConstraintType;

/**
 * Purpose: This class provides utility functions to build +IPET models
 * 
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class IPETUtils {
	
	/**
	 * Top level flow constraints: flow out of entry and flow into exit must be one.
	 *   <pre>sum (e_Entry_x) = 1</pre>
	 *   <pre>sum (e_y_Exit) = 1</pre>
	 */
	public static<V,E,C extends CallStringProvider>
	Vector<LinearConstraint<IPETBuilder.ExecutionEdge>>  
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
	public static<V,E,C extends CallStringProvider>
	Vector<LinearConstraint<IPETBuilder.ExecutionEdge>>  
		structuralFlowConstraints(FlowGraph<V, E> g,
								  Collection<IPETBuilder.ExecutionEdge> inputEdges,
								  Collection<IPETBuilder.ExecutionEdge> outputEdges,
								  IPETBuilder<C> ctx) {

		LinearConstraint<IPETBuilder.ExecutionEdge> entryLhsProto = new LinearConstraint<IPETBuilder.ExecutionEdge>(ConstraintType.Equal);
		for(IPETBuilder.ExecutionEdge e : inputEdges) entryLhsProto.addLHS(e);
		LinearConstraint<IPETBuilder.ExecutionEdge> exitRhsProto = new LinearConstraint<IPETBuilder.ExecutionEdge>(ConstraintType.Equal);
		for(IPETBuilder.ExecutionEdge e : outputEdges) exitRhsProto.addRHS(e);
		
		return structuralFlowConstraints(g, entryLhsProto, exitRhsProto, ctx);
	}

	/**
	 *  Structural Flow Constraints:<ul>
	 *  <li/> The flow of entry and exit is constrained by the given left-hand resp. right-hand sides
	 *  <li/> For all other nodes, the incoming flow (left-hand) is equal to the outgoing flow (rhs)
	 *  </ul>
	 * @param <V> node type
	 * @param <E> edge type
	 * @param <C> context type
	 * @param graph the flow graph to generate constraints for
	 * @param entryRhsProto the left-hand side of the constraint for the entry node
	 * @param exitRhsProto the right-hand side of the constraint for the exit node
	 * @param ctx the execution context
	 * @return the set of structural linear constraints
	 */
	public static<V,E, C extends CallStringProvider>
	Vector<LinearConstraint<IPETBuilder.ExecutionEdge>> 
	    structuralFlowConstraints(FlowGraph<V,E> graph, 
	    						  LinearConstraint<IPETBuilder.ExecutionEdge> entryLhsProto,
	    						  LinearConstraint<IPETBuilder.ExecutionEdge> exitRhsProto,
	    						  IPETBuilder<C> ctx) {

		Vector<LinearConstraint<IPETBuilder.ExecutionEdge>> constraints = new Vector<LinearConstraint<IPETBuilder.ExecutionEdge>>();

		for(V node : graph.vertexSet()) {
			LinearConstraint<IPETBuilder.ExecutionEdge> flowConstraint;

			if(node.equals(graph.getEntry())) {
				flowConstraint = entryLhsProto.clone();
			} else if(node.equals(graph.getExit())) {
				flowConstraint = exitRhsProto.clone();
			} else {
				flowConstraint = new LinearConstraint<IPETBuilder.ExecutionEdge>(ConstraintType.Equal);				
			}
			for(E ingoing : graph.incomingEdgesOf(node)) {
				flowConstraint.addLHS(ctx.newEdge(ingoing));
			}
			for(E outgoing : graph.outgoingEdgesOf(node)) {
				flowConstraint.addRHS(ctx.newEdge(outgoing));
			}
			constraints.add(flowConstraint);
		}		
		return constraints;
	}

	/**
	 * Compute flow constraints: Loop Bound constraints (Control Flow Graph only)
	 * @param g the flow graph
	 * @param cs the invocation context
	 * @return A list of flow constraints
	 */
	public static<C extends CallStringProvider>
	Vector<LinearConstraint<IPETBuilder.ExecutionEdge>> 
		loopBoundConstraints(ControlFlowGraph g, IPETBuilder<C> ctx) {
		
		Vector<LinearConstraint<IPETBuilder.ExecutionEdge>> constraints = new Vector<LinearConstraint<IPETBuilder.ExecutionEdge>>();
		// - for each loop with bound B
		// -- sum(exit_loop_edges) * B <= sum(continue_loop_edges)
		LoopColoring<CFGNode,CFGEdge> loops = g.getLoopColoring();
		for(CFGNode hol : loops.getHeadOfLoops()) {
			LoopBound loopBound = g.getLoopBound(hol,ctx.getCallString());
			if(loopBound == null) {
				throw new Error("No loop bound record for head of loop: " + hol + " : " + g.getLoopBounds());
			}
			for(LinearConstraint<IPETBuilder.ExecutionEdge> loopConstraint : constraintsForLoop(loops, hol,loopBound, ctx))
			{
				constraints.add(loopConstraint);
			}
		}
		return constraints;
	}

	/** Generate Loop Constraints */
	public static<C extends CallStringProvider>
	List<LinearConstraint<IPETBuilder.ExecutionEdge>>
		constraintsForLoop(LoopColoring<CFGNode, CFGEdge> loops, 
						   CFGNode hol,
						   LoopBound loopBound,
						   IPETBuilder<C> ctx) {
		
		Vector<LinearConstraint<IPETBuilder.ExecutionEdge>> loopConstraints = new Vector<LinearConstraint<IPETBuilder.ExecutionEdge>>();
		/* marker loop constraints */
		for(Entry<SymbolicMarker, Pair<Long, Long>> markerBound: loopBound.getLoopBounds()) {

			/* loop constraint */
			LinearConstraint<IPETBuilder.ExecutionEdge> loopConstraint = new LinearConstraint<IPETBuilder.ExecutionEdge>(ConstraintType.GreaterEqual);
			for(CFGEdge continueEdge : loops.getBackEdgesTo(hol)) {
				loopConstraint.addRHS(ctx.newEdge(continueEdge));
			}
			/* Multiplicities */
			long lhsMultiplicity = markerBound.getValue().snd();
			SymbolicMarker marker = markerBound.getKey();
			if(marker.getMarkerType() == SymbolicMarkerType.OUTER_LOOP_MARKER) {

				CFGNode outerLoopHol;
				outerLoopHol = loops.getLoopAncestor(hol, marker.getOuterLoopDistance());
				if(outerLoopHol == null) {
					// FIXME: [annotations] This is a user error, not an assertion error
					throw new AssertionError("Invalid Loop Nest Level"); 
				}
				for(CFGEdge exitEdge : loops.getExitEdgesOf(outerLoopHol)) {
					loopConstraint.addLHS(ctx.newEdge(exitEdge), lhsMultiplicity);
				}				
			} else {
				assert(marker.getMarkerType() == SymbolicMarkerType.METHOD_MARKER);
				throw new AssertionError("ILPModelBuilder: method markers not yet supported, sorry");
			}
			loopConstraints.add(loopConstraint);
		}
		return loopConstraints;
	}
	
	/** Generate constraints for super graph edges */
	public static<C extends CallStringProvider>
	LinearConstraint<IPETBuilder.ExecutionEdge> 
		superEdgeConstraint(SuperInvokeEdge in, SuperReturnEdge out, IPETBuilder<C> ctx) {
		
		LinearConstraint<IPETBuilder.ExecutionEdge> pairConstraint = new LinearConstraint<IPETBuilder.ExecutionEdge>(ConstraintType.Equal);
		pairConstraint.addLHS(ctx.newEdge(in));
		pairConstraint.addRHS(ctx.newEdge(out));
		return pairConstraint;		
	}


	/**
	 * Compute flow constraints: Infeasible edge constraints
	 * @param g the flow graph
	 * @param cs the invocation context
	 * @return A list of flow constraints
	 */
	public static<C extends CallStringProvider> 
	Vector<LinearConstraint<IPETBuilder.ExecutionEdge>>
		infeasibleEdgeConstraints(ControlFlowGraph g, IPETBuilder<C> ctx) {
		
		Vector<LinearConstraint<IPETBuilder.ExecutionEdge>> constraints = new Vector<LinearConstraint<IPETBuilder.ExecutionEdge>>();
		// - for each infeasible edge
		// -- edge = 0
		for(CFGEdge edge : g.getInfeasibleEdges(ctx.getCallString())) {
			LinearConstraint<IPETBuilder.ExecutionEdge> infeasibleConstraint = new LinearConstraint<IPETBuilder.ExecutionEdge>(ConstraintType.Equal);
			infeasibleConstraint.addLHS(ctx.newEdge(edge));
			infeasibleConstraint.addRHS(0);
			constraints.add(infeasibleConstraint);
		}
		return constraints;
	}

	/**
	 * Compute flow constraints: invoke and return flow have to be equal
	 * @param call the invoke edge
	 * @param ret  the return edge
	 * @param builder the IPET context
	 * @return the linear constraints ensuring that each flow following the invoke is matched
	 * 		   by one returning, and that non-local and local invoke flow coincide
	 */
	public static<C extends CallStringProvider>
	List<LinearConstraint<ExecutionEdge>>
		invokeReturnConstraints(SuperInvokeEdge call, SuperReturnEdge ret, IPETBuilder<C> builder) {

		
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
			InvokeNode invokeNode = call.getInvokeNode();
			Set<CFGEdge> localInvokeEdge = invokeNode.getControlFlowGraph().getGraph().outgoingEdgesOf(invokeNode);
			if(localInvokeEdge.size() == 1) {
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
	 * @param parentEdge the high-level edge to be split
	 * @param childEdges low-level edges
	 * @return constraint asserting sum f(childEdges) = f(parentEdge)
	 */
	public static LinearConstraint<ExecutionEdge> lowLevelEdgeSplit(ExecutionEdge parentEdge, ExecutionEdge... childEdges) {
		LinearConstraint<ExecutionEdge> lc = new LinearConstraint<ExecutionEdge>(ConstraintType.Equal);		
		lc.addLHS(parentEdge);
		for(ExecutionEdge childEdge : childEdges) {
			lc.addRHS(childEdge);
		}
		return lc;
	}
	
	/**
	 * Create a max-cost maxflow problem for the given flow graph graph, based on a 
	 * given node to cost mapping.
	 * @param key a unique identifier for the problem (for reporting)
	 * @param callString context of the method invocation
	 * @param g the graph
	 * @param nodeWCET cost of nodes
	 * @return The max-cost maxflow problem
	 */
	public static IPETSolver buildLocalILPModel(String problemName, CallString cs, ControlFlowGraph cfg, CostProvider<CFGNode> nodeWCET,
			IPETConfig ipetConfig) {
		
		IPETSolver ipetSolver = new IPETSolver(problemName, ipetConfig);
		
		IPETBuilder<CallString> builder = new IPETBuilder<CallString>(cs);
		
		ipetSolver.addConstraints( IPETUtils.structuralFlowConstraintsRoot(cfg.getGraph(), builder));
		ipetSolver.addConstraints( IPETUtils.loopBoundConstraints(cfg, builder));
		ipetSolver.addConstraints( IPETUtils.infeasibleEdgeConstraints(cfg, builder));
				
		for(CFGNode n : cfg.getGraph().vertexSet()) {
			long nodeCost = nodeWCET.getCost(n);
			for(CFGEdge e : cfg.getGraph().outgoingEdgesOf(n)) {
				ipetSolver.addEdgeCost(builder.newEdge(e), nodeCost);
			}
		}
		return ipetSolver;
	}		
}
