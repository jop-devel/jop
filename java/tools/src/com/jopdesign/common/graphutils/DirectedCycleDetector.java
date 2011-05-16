/* Modified code from
 * ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2007, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package com.jopdesign.common.graphutils;

import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * Modified cycle detector, which returns the cycle actually found, if there is one in the
 * given directed graph. The standard cycle detector of JGraphT doesn't report the cycle
 * itself, only the vertices involved - this makes debugging rather tricky.
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 * @author Barak Naveh and Contributors (jgrapht)
 */
public class DirectedCycleDetector {

    @SuppressWarnings({"unchecked"})
    public static <V, E> Pair<List<V>, List<V>> findCycle(DirectedGraph<V, E> g, V start) {
        ProbeIterator pi = new ProbeIterator<V, E>(g, start);
        try {
            while (pi.hasNext()) pi.next();
            return null;
        } catch (CycleDetectedException ex) {
            return new Pair<List<V>, List<V>>(ex.getPrefix(), ex.getCyclePath());
        }
    }

    @SuppressWarnings({"unchecked", "UncheckedExceptionClass"})
    /* exception classes may not be generic */
    private static class CycleDetectedException extends Error {

        private static final long serialVersionUID = 1L;
        private List prefix;
        private List cyclePath;

        public CycleDetectedException(List prefix, List path) {
            super("CycleDetected: " + path + "reachable via " + prefix);
            this.prefix = prefix;
            this.cyclePath = path;
        }

        public List getPrefix() {
            return prefix;
        }

        public List getCyclePath() {
            return cyclePath;
        }
    }

    /**
     * Version of DFS which maintains a backtracking path used to probe for
     * cycles (Modified from JGraphT)
     */
    private static class ProbeIterator<V, E>
            extends DepthFirstIterator<V, E> {
        private List<V> path;
        private DirectedGraph<V, E> graph;

        ProbeIterator(DirectedGraph<V, E> graph, V startVertex) {
            super(graph, startVertex);
            this.graph = graph;
            path = new ArrayList<V>();
        }

        protected void encounterVertexAgain(V vertex, E edge) {
            super.encounterVertexAgain(vertex, edge);

            int i = path.indexOf(vertex);
            if (i > -1) {
                List<V> prefix;
                if (i > 0) prefix = new ArrayList<V>(path.subList(0, i));
                else prefix = new ArrayList<V>();
                List<V> subPath = path.subList(i, path.size());
                subPath.add(vertex);
                throw new CycleDetectedException(prefix, subPath);
            }
        }

        protected V provideNextVertex() {
            V v = super.provideNextVertex();

            // backtrack
            for (int i = path.size() - 1; i >= 0; --i) {
                if (graph.containsEdge(path.get(i), v)) {
                    break;
                }
                path.remove(i);
            }
            path.add(v);
            return v;
        }
    }

}
