/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
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

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class jGraphTest {

    public static class TestNode {

        private int i;

        public TestNode(int i) {
            this.i = i;
        }

        @Override
        public int hashCode() {
            return i;
        }

        @Override
        public boolean equals(Object obj) {
            return i == ((TestNode)obj).i;
        }
    }

    public static void main(String[] args) {

        TestNode entry = new TestNode(1);
        TestNode exit = new TestNode(2);

        DirectedGraph<TestNode, DefaultEdge> graph =
                new DefaultDirectedGraph<TestNode, DefaultEdge>(DefaultEdge.class);

        graph.addVertex(entry);
        graph.addVertex(exit);
        graph.addVertex(new TestNode(3));
        graph.addEdge(entry, exit);
        graph.addEdge(entry, new TestNode(3));

        System.out.println(graph.containsVertex(entry));
        System.out.println(graph.containsVertex(new TestNode(3)));
        System.out.println(graph.containsEdge(new TestNode(1), new TestNode(3)));
        System.out.println(graph.containsEdge(exit, new TestNode(1)));

    }
}
