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

package com.jopdesign.common.code;

import com.jopdesign.common.misc.BadGraphException;

/**
 * Purpose: Represents one node in the supergraph.
 * Corresponds to a CFG in a certain context.
 * A context is represented by
 * <ol><li/> the callstring
 * <li/> the context id
 * </ol> The latter is useful to perform unsharing, which is not callstring based
 *
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 */
public class SuperGraphNode {

    private ControlFlowGraph cfg;

    /**
     * Note that it is also possible to distinguish nodes with the same cfg and callstring
     */
    private SuperGraph.CallContext context;

    public SuperGraphNode(ControlFlowGraph cfg, CallString cs) {
        this(cfg, new SuperGraph.CallContext(cs));
    }

    public SuperGraphNode(ControlFlowGraph cfg, SuperGraph.CallContext ctx) {
        this.cfg = cfg;
        this.context = ctx;
    }

    /**
     * @return the cfg
     */
    public ControlFlowGraph getCfg() {
        return cfg;
    }

    /**
     * @return the callstring
     */
    public CallString getCallString() {
        return context.getCallString();
    }

    /**
     * @return the context, distinguishing two supergraph nodes for the
     *         same control flow graph
     */
    public SuperGraph.CallContext getContext() {
        return context;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cfg == null) ? 0 : cfg.hashCode());
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        SuperGraphNode other = (SuperGraphNode) obj;
        if (cfg == null) {
            if (other.cfg != null)
                return false;
        } else if (!cfg.equals(other.cfg))
            return false;
        if (context == null) {
            if (other.context != null)
                return false;
        } else if (!context.equals(other.context))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SuperGraphNode [" + cfg.getMethodInfo() + ", context=" + context + "]";
    }


}
