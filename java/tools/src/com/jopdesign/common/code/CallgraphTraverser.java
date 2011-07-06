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

package com.jopdesign.common.code;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * This class provides algorithms to traverse a callgraph
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class CallgraphTraverser {

    private static class StackEntry {
        private final ExecutionContext context;
        private final List<ExecutionContext> childQueue;
        private final boolean recursive;

        private StackEntry(ExecutionContext context, List<ExecutionContext> childQueue) {
            this.context = context;
            this.childQueue = childQueue;
            this.recursive = false;
        }

        private StackEntry(ExecutionContext context, List<ExecutionContext> childQueue, boolean recursive) {
            this.context = context;
            this.childQueue = childQueue;
            this.recursive = recursive;
        }

        public ExecutionContext getContext() {
            return context;
        }

        public List<ExecutionContext> getChildQueue() {
            return childQueue;
        }

        public boolean isRecursive() {
            return recursive;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StackEntry that = (StackEntry) o;

            return context.equals(that.getContext());
        }

        @Override
        public int hashCode() {
            return context.hashCode();
        }
    }

    private CallGraph callGraph;
    private CallgraphVisitor visitor;
    private CallgraphFilter filter;

    public CallgraphTraverser(CallGraph callGraph, CallgraphVisitor visitor) {
        this.callGraph = callGraph;
        this.visitor = visitor;
    }

    public CallGraph getCallGraph() {
        return callGraph;
    }

    public CallgraphVisitor getVisitor() {
        return visitor;
    }

    public CallgraphFilter getFilter() {
        return filter;
    }

    public void setFilter(CallgraphFilter filter) {
        this.filter = filter;
    }

    public void traverseDown(ExecutionContext root, boolean skipVisited) {
        traverseDown(Collections.singleton(root), skipVisited);
    }

    public void traverseDown(Collection<ExecutionContext> roots, boolean skipVisited) {
        traverse(roots, skipVisited, true);
    }

    public void traverseUp(Collection<ExecutionContext> roots, boolean skipVisited) {
        traverse(roots, skipVisited, false);
    }

    /**
     * Implements a DFS traversal of the callgraph, starting at the given root nodes.
     * @param roots a set of nodes in the callgraph.
     * @param skipVisited if true, visit reachable nodes only once.
     * @param traverseDown if true, visit children of nodes, if false, visit the parents.
     */
    public void traverse(Collection<ExecutionContext> roots, boolean skipVisited, boolean traverseDown) {

        Set<ExecutionContext> visited = skipVisited ? new HashSet<ExecutionContext>() : null;
        Stack<StackEntry> stack = new Stack<StackEntry>();

        for (ExecutionContext root : roots) {

            List<ExecutionContext> childs = getChilds(root, traverseDown);
            if (!visitor.visitNode(root, childs, false)) {
                continue;
            }
            stack.push(new StackEntry(root, childs));

            while (!stack.isEmpty()) {
                // continue with next child
                StackEntry current = stack.peek();

                if (!current.getChildQueue().isEmpty()) {
                    ExecutionContext next = current.getChildQueue().remove(0);

                    // Check for recursion
                    boolean recursive = current.isRecursive();

                    if (skipVisited) {
                        if (visited.contains(next)) {
                            continue;
                        }
                        visited.add(next);
                    }
                    else if (!recursive) {
                        // TODO we could use a HashSet for this check
                        for (StackEntry entry : stack) {
                            if (entry.getContext().equals(next)) {
                                recursive = true;
                                break;
                            }
                        }
                    }

                    childs = getChilds(next, traverseDown);
                    if (!visitor.visitNode(next, childs, recursive)) {
                        continue;
                    }
                    stack.push(new StackEntry(next, childs, recursive));

                } else {
                    // no childs left, finished visiting the node
                    visitor.finishNode(current.getContext());
                    stack.pop();
                }

            }

        }

    }

    private List<ExecutionContext> getChilds(ExecutionContext context, boolean traverseDown) {

        List<ExecutionContext> childs = null;

        if (filter != null) {
            childs = traverseDown ? filter.getChildren(context) : filter.getParents(context);
        }
        if (childs == null) {
            childs = new LinkedList<ExecutionContext>(
                    traverseDown ? callGraph.getChildren(context) : callGraph.getParents(context) );
        }

        return childs;
    }
}
