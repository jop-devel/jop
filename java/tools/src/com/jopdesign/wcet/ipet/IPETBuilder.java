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
package com.jopdesign.wcet.ipet;

import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.CallStringProvider;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.wcet.WCETTool;

/**
 * XXX: Not used any more in the new global analysis framework
 * Purpose: Build context-dependend constraints for IPET problems
 * Regardless of the underlying graphs, IPET graphs consist of ExecutionEdges,
 * and the IPETBuilder is a factory for those edges
 */
@Deprecated
public class IPETBuilder<Ctx extends CallStringProvider> {
    /**
     * One edge in the IPET model (distinguished by context and the represented model)
     */
    public static class ExecutionEdge {

        private CallStringProvider ctx;
        private Object model;

        public ExecutionEdge(CallStringProvider ctx, Object e) {
            this.ctx = ctx;
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
            if (model instanceof ControlFlowGraph.CFGEdge) return (ControlFlowGraph.CFGEdge) model;
            else return null;
        }

        @Override
        public int hashCode() {
            return ctx.hashCode() * 37 + ((model == null) ? 0 : model.hashCode());
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o == this) return true;
            if (o.getClass() != this.getClass()) return false;
            ExecutionEdge other = (ExecutionEdge) o;
            if (this.model == null) {
                if (other.model != null) return false;
            } else {
                if (!this.model.equals(other.model)) return false;
            }
            if (!this.ctx.equals(other.ctx)) return false;
            return true;
        }

        @Override
        public String toString() {
            return String.format("{Edge:%s[%s]}", model.toString(), ctx.toString());
        }

    }

    private WCETTool project;
    private Ctx ctx;

    public IPETBuilder(WCETTool project, Ctx ctx) {
        this.project = project;
        this.ctx = ctx;
    }

    public Ctx getContext() {
        return ctx;
    }

    public WCETTool getWCETTool() {
        return project;
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
     * @return callstring of the current execution context
     */
    public CallString getCallString() {
        return ctx.getCallString();
    }
}