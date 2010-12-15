package com.jopdesign.wcet.ipet;

import com.jopdesign.common.code.CallStringProvider;
import com.jopdesign.common.code.ControlFlowGraph;

/**
 * Purpose: Build context-dependend constraints for IPET problems
 * Regardless of the underlying graphs, IPET graphs consist of ExecutionEdges,
 * and the IPETBuilder is a factory for those edges
 *
 */
public class IPETBuilder<Ctx extends CallStringProvider> {
	/** One edge in the IPET model (distinguished by context and the represented model) */
	public static class ExecutionEdge {
		
		private CallStringProvider ctx;
		private Object model;
	
		public ExecutionEdge(CallStringProvider ctx, Object e) {
			this.ctx  = ctx;
			this.model = e;
		}
	
		public Object getModel() {
			return this.model;
		}
	
		/**
		 * @return if applicable, the corresponding control flow graph edge modeled by this edge,
		 *         and null otherwise
		 */
		public ControlFlowGraph.CFGEdge getModelledEdge() {
			if(model instanceof ControlFlowGraph.CFGEdge) return (ControlFlowGraph.CFGEdge) model;
			else return null;
		}
		
		@Override public int hashCode() {
			return ctx.hashCode() * 37 + ((model == null) ? 0 : model.hashCode());			
		}
		@Override public boolean equals(Object o) {
			if(o == null) return false;
			if(o == this) return true;
			if(o.getClass() != this.getClass()) return false;
			ExecutionEdge other = (ExecutionEdge) o;
			if(this.model == null) {
				if(other.model != null) return false;
			} else {
				if(! this.model.equals(other.model)) return false;
			}
			if(! this.ctx.equals(other.ctx)) return false;
			return true;
		}
		@Override public String toString() {
			return String.format("{Edge:%s[%s]}",model.toString(),ctx.toString());
		}
		
	}

	private Ctx ctx;

	public IPETBuilder(Ctx ctx) {
		this.ctx = ctx;
	}
	public Ctx getContext() {
		return ctx;
	}
	public void changeContext(Ctx newCtx) {
		this.ctx = newCtx;
	}

	/**
	 * Create a new execution edge in the current context
	 */
	public IPETBuilder.ExecutionEdge newEdge(Object model) {
		return new IPETBuilder.ExecutionEdge(ctx, model);
	}

	/**
	 * Create a new execution edge in a different context
	 */
	public IPETBuilder.ExecutionEdge newEdgeInContext(Object model, CallContext otherContext) {
		return new IPETBuilder.ExecutionEdge(otherContext, model);
	}
	
	/**
	 * @return callstring of the current execution context
	 */
	public CallString getCallString() {
		return ctx.getCallString();
	}
}