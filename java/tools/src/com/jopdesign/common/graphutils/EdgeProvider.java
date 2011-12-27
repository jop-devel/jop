/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.common.graphutils;

import java.util.Collection;

/**
* @author Stefan Hepp (stefan@stefant.org)
*/
public interface EdgeProvider<V,E> {
    /**
     * Get all outgoing edges of a node. The iterator of the result defines the order in which childs
     * are visited. If an edge is not included in the result, the edge target will not be visited from this
     * node, but it might still be visited from another node.
     *
     * @param node the node to get edges for
     * @return all outgoing edges of the node which should be visited from this node.
     */
    Collection<E> outgoingEdgesOf(V node);

    V getEdgeTarget(E edge);
}
