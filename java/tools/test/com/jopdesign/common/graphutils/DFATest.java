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

import com.jopdesign.common.graphutils.DFSTraverser.DFSEdgeType;
import com.jopdesign.common.graphutils.DFSTraverser.DFSVisitor;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collection;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class DFATest {

    public static void main(String[] args) {

        DirectedGraph<Integer,DefaultEdge> graph =
                new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);

        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);

        graph.addEdge(1,2);
        graph.addEdge(2,3);
        graph.addEdge(3,4);
        graph.addEdge(4,5);

        graph.addEdge(1,4);
        graph.addEdge(2,5);

        graph.addEdge(3,2);
        graph.addEdge(5,4);

        DFSVisitor<Integer,DefaultEdge> visitor = new DFSVisitor<Integer, DefaultEdge>() {
            @Override
            public boolean visitNode(Integer parent, DefaultEdge edge, Integer node, DFSEdgeType type, Collection<DefaultEdge> outEdges, int depth) {
                System.out.println("Visiting "+node+": parent "+parent+", type "+type);
                return true;
            }

            @Override
            public void finishNode(Integer parent, DefaultEdge edge, Integer node, DFSEdgeType type, Collection<DefaultEdge> outEdges, int depth) {
                System.out.println("=> finished "+node+": parent "+parent+", type "+type);
            }
        };

        DFSTraverser<Integer,DefaultEdge> traverser = new DFSTraverser<Integer, DefaultEdge>(visitor);
        traverser.traverse(graph);

    }
}
