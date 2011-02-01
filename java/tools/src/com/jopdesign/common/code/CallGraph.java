/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)
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
package com.jopdesign.common.code;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.graphutils.AdvancedDOTExporter;
import com.jopdesign.common.graphutils.DirectedCycleDetector;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.common.type.MethodRef;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * <p>Java call graph, whose nodes represent control flow graphs and
 *     dynamic dispatches.</p>
 * <p>If some instruction in the flow graph represented by {@code MethodImplNode m1}
 * possibly invokes a {@code MethodImplNode m2},there is an edge from {@code m1}
 * to {@code m2}.</p>
 *
 * <p>Note that this callgraph only contains MethodInfos, not MethodRefs, so invocations of unknown methods
 * are not represented in this graph.</p>
 *
 * TODO allow multiple roots, additionally initialize from AppInfo directly (use AppInfo.getRoots(),
 *      use all methods in root-classes as root; provide method find all 'real' roots)
 * TODO support for callgraph thinning (ie. remove edges/methods/...)
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class CallGraph {

    /**
     * Interface for a callgraph construction.
     */
    public interface CallgraphConfig {
        List<ExecutionContext> getInvokedMethods(ExecutionContext context);
    }

    /*---------------------------------------------------------------------------*
     * Graph node and edge classes
     *---------------------------------------------------------------------------*/

    /**
     * A node representing a methodInfo, and stores references to all
     * execution contexts of this method in the callgraph.
     */
    public static class MethodNode {
        private final MethodInfo methodInfo;
        private final Set<ExecutionContext> instances;

        public MethodNode(MethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            instances = new HashSet<ExecutionContext>();
        }

        public MethodInfo getMethodInfo() {
            return methodInfo;
        }

        public Set<ExecutionContext> getInstances() {
            return instances;
        }

        protected void addInstance(ExecutionContext context) {
            instances.add(context);
        }

        protected void removeInstance(ExecutionContext context) {
            instances.remove(context);
        }
    }

    /**
     * An edge representing a possible invokation of a method B by a method A, and
     * stores all invoke sites of A which might call B.
     */
    public static class InvokeEdge {
        private final Set<InvokeSite> invokeSites;

        public InvokeEdge() {
            invokeSites = new HashSet<InvokeSite>();
        }

        public Set<InvokeSite> getInvokeSites() {
            return invokeSites;
        }

        protected void addInvokeSite(InvokeSite site) {
            invokeSites.add(site);
        }

        protected void removeInvokeSite(InvokeSite invokeSite) {
            invokeSites.remove(invokeSite);
        }
    }

    public static class ContextEdge {
        private final ExecutionContext source;
        private final ExecutionContext target;

        private ContextEdge(ExecutionContext source, ExecutionContext target) {
            this.source = source;
            this.target = target;
        }

        public ExecutionContext getSource() {
            return source;
        }

        public ExecutionContext getTarget() {
            return target;
        }
    }

    /*---------------------------------------------------------------------------*
     * Internal classes
     *---------------------------------------------------------------------------*/

    private class GraphUpdateListener implements GraphListener<ExecutionContext, ContextEdge> {
        @Override
        public void edgeAdded(GraphEdgeChangeEvent<ExecutionContext, ContextEdge> e) {
            if (mergedCallGraph != null) {
                onAddCallGraphEdge(e.getEdge());
            }
        }

        @Override
        public void edgeRemoved(GraphEdgeChangeEvent<ExecutionContext, ContextEdge> e) {
            if (mergedCallGraph != null) {
                onRemoveCallGraphEdge(e.getEdge());
            }
        }

        @Override
        public void vertexAdded(GraphVertexChangeEvent<ExecutionContext> e) {
            // we always call this because we need to update mergedNodes
            onAddExecutionContext(e.getVertex());
        }

        @Override
        public void vertexRemoved(GraphVertexChangeEvent<ExecutionContext> e) {
            // we always call this because we need to update mergedNodes
            onRemoveExecutionContext(e.getVertex());
        }
    }

    //
    // Fields
    // ~~~~~~
    private final ExecutionContext rootNode;
    private final CallgraphConfig config;

    private ListenableDirectedGraph<ExecutionContext, ContextEdge> callGraph;
    private DirectedGraph<MethodNode, InvokeEdge> mergedCallGraph;

    private HashSet<ClassInfo> classInfos;
    private HashMap<MethodInfo,MethodNode> methodNodes;

    //
    // Caching Fields
    // ~~~~~~~~~~~~~~
    private HashMap<ExecutionContext, Integer> maxDistanceToRoot = null;
    private HashMap<ExecutionContext, ExecutionContext> maxCallstackDAG = null;
    private HashMap<ExecutionContext, Integer> subgraphHeight = null;
    private ExecutionContext maxCallStackLeaf = null;
    private HashMap<MethodInfo,Boolean> leafNodeCache;

    /*---------------------------------------------------------------------------*
     * Constructor methods
     *---------------------------------------------------------------------------*/

    /**
     * Build a callgraph rooted at the given method
     *
     * @see AppInfo#getMethodInfo(String, String)
     * @param appInfo   The application (with classes loaded)
     * @param className The class where the root method of the callgraph is located
     * @param methodSig The root method of the call graph. Either a plain method name
     *                   (e.g. "measure"), if unique, or a method with signature (e.g. "measure()Z")
     * @param config the config class to use to build this graph
     * @return a freshly built callgraph.
     * @throws MethodNotFoundException if the referenced main method was not found
     */
    public static CallGraph buildCallGraph(AppInfo appInfo, String className, String methodSig,
                                       CallgraphConfig config)
                                                    throws MethodNotFoundException
    {
        MethodInfo rootMethod = appInfo.getMethodInfo(className,methodSig);
        return buildCallGraph(rootMethod, config);
    }

    /**
     * Build a callgraph rooted at the given method
     *
     * @see AppInfo#getMethodInfo(String, String)
     * @param rootMethod The root method of the callgraph
     * @param config the config class to use to build this graph
     * @return a freshly built callgraph.
     */
    public static CallGraph buildCallGraph(MethodInfo rootMethod, CallgraphConfig config)
    {
        CallGraph cg = new CallGraph(rootMethod,config);
        cg.build();
        return cg;
    }

    /*---------------------------------------------------------------------------*
     * Init and build callgraph (private)
     *---------------------------------------------------------------------------*/

    /**
     * Initialize a CallGraph object.
     * @param rootMethod The root method of the callgraph (not abstract).
     * @param config the config class to use to build this graph
     */
    protected CallGraph(MethodInfo rootMethod, CallgraphConfig config) {
        this.rootNode = new ExecutionContext(rootMethod, CallString.EMPTY);
        this.config = config;

        // We need a custom ContextEdge here to keep the references to the vertices for the removeEdge listener
        this.callGraph = new ListenableDirectedGraph<ExecutionContext,ContextEdge>(
            new DefaultDirectedGraph<ExecutionContext,ContextEdge>(
                new EdgeFactory<ExecutionContext,ContextEdge>() {
                    @Override
                    public ContextEdge createEdge(ExecutionContext sourceVertex, ExecutionContext targetVertex) {
                        return new ContextEdge(sourceVertex,targetVertex);
                    }
                }) );
    }

    /**
     * Build and initialize everything, perform checks
     */
    private void build() {
        this.buildGraph();

        /* Compute set of classes */
        classInfos = new HashSet<ClassInfo>();
        for(MethodNode node : methodNodes.values()) {
                classInfos.add(node.getMethodInfo().getClassInfo());
        }

        /* Check the callgraph is cycle free */
        Pair<List<ExecutionContext>,List<ExecutionContext>> cycle =
            DirectedCycleDetector.findCycle(callGraph,rootNode);
        if(cycle != null) {
            // maybe make dumping the whole graph optional :)
            /*
            for(DefaultEdge e : callGraph.edgeSet()) {
                ExecutionContext src = callGraph.getEdgeSource(e);
                ExecutionContext target = callGraph.getEdgeTarget(e);
                System.err.println(""+src+" --> "+target);
            }
            */
            throw new AssertionError(cyclicCallGraphMsg(cycle));
        }

        invalidate();
    }
	
    /**
     * Build the callgraph.
     *
     * <p>NEW: now we also use callstrings to get a more precise call graph model</p>
     */
    private void buildGraph() {
        /* Initialize DFS data structures and lookup maps */
        Stack<ExecutionContext> todo = new Stack<ExecutionContext>();
        todo.push(rootNode);

        methodNodes = new HashMap<MethodInfo, MethodNode>();

        callGraph.addGraphListener(new GraphUpdateListener());

        callGraph.addVertex(rootNode);

        while(! todo.empty()) {
            ExecutionContext current = todo.pop();

            List<ExecutionContext> invoked = config.getInvokedMethods(current);
            for (ExecutionContext cgn : invoked) {

            if (!callGraph.containsVertex(cgn)) {
                callGraph.addVertex(cgn);
                todo.push(cgn);
            }

            callGraph.addEdge(current, cgn);
                    }
        } /* end while */
    }

    /*---------------------------------------------------------------------------*
     * Various getters, access to nodes
     *---------------------------------------------------------------------------*/

    /**
     * Get node for a method info and call context
     * @param m the method
     * @param cs the call string to use (if null, {@link CallString#EMPTY} is used).
     * @return a new execution context.
     */
    public ExecutionContext getNode(MethodInfo m, CallString cs) {
        return new ExecutionContext(m,cs == null ? CallString.EMPTY : cs);
    }
	
    /**
     * Check if the callgraph contains a given method with a given callstring.
     * @param m the method
     * @param cs the call string to use (if null, {@link CallString#EMPTY} is used).
     * @return true if the given call graph node is present in the call graph
     */
    public boolean hasNode(MethodInfo m, CallString cs) {
        return callGraph.containsVertex(new ExecutionContext(m,cs == null ? CallString.EMPTY : cs));
    }
	
    /**
     * Get all nodes matching the given method info.
     *
     * @see #getMethodNode(MethodInfo)
     * @param m the method to check.
     * @return a set of execution contexts of this method in the callgraph.
     * @throws AssertionError if the method has no contexts in the callgraph
     */
    public Set<ExecutionContext> getNodes(MethodInfo m) {
        if (!methodNodes.containsKey(m)) {
            throw new AssertionError("No callgraph nodes for "+ m);
        }
        return methodNodes.get(m).getInstances();
    }

    /**
     * Get a MethodNode for a given methodInfo.
     * @param m the method to check.
     * @return the methodNode containing a set of all execution contexts of the method, or null if not found.
     */
    public MethodNode getMethodNode(MethodInfo m) {
        return methodNodes.get(m);
    }

    /**
     * Export callgraph as .dot file
     * @param w Write the graph to this writer. To improve performance, to use a buffered writer.
     * @throws IOException if writing fails
     */
    public void exportDOT(Writer w) throws IOException {
        new AdvancedDOTExporter<ExecutionContext, ContextEdge>().exportDOT(w, this.callGraph);
    }

    public ClassInfo getRootClass() {
        return rootNode.getMethodInfo().getClassInfo();
    }

    public MethodInfo getRootMethod() {
        return rootNode.getMethodInfo();
    }

    public ExecutionContext getRootNode() {
        return rootNode;
    }

    public Set<ClassInfo> getClassInfos() {
        return classInfos;
    }


    /*---------------------------------------------------------------------------*
     * Merge graph, subgraphs
     *---------------------------------------------------------------------------*/

    /**
     * Build a second graph with a single node per method to get a less precise call graph model.
     * Used to get all execution contexts per method and all invoked methods per InvokeSite.
     * This graph is backed by the main graph, so all changes to the main graph are reflected in this graph.
     */
    public void buildMergedGraph() {
        if (mergedCallGraph != null) return;
        this.mergedCallGraph = new DefaultDirectedGraph<MethodNode, InvokeEdge>(
                new EdgeFactory<MethodNode, InvokeEdge>() {
                    @Override
                    public InvokeEdge createEdge(MethodNode source, MethodNode target) {
                        return new InvokeEdge();
                    }
                });

        // nodes are already uptodate, add them to the graph
        for (MethodNode node : methodNodes.values()) {
            mergedCallGraph.addVertex(node);
        }

        // for all edges in callGraph, add or update the edge in this graph
        for (ContextEdge edge : callGraph.edgeSet()) {
            onAddCallGraphEdge(edge);
        }
    }

    /*---------------------------------------------------------------------------*
     * Various lookup methods
     *---------------------------------------------------------------------------*/

    /**
     * Return a top-down (topological) iterator for the callgraph
     * @return A topological order iterator
     */
    public TopologicalOrderIterator<ExecutionContext, ContextEdge> topDownIterator() {
        return new TopologicalOrderIterator<ExecutionContext, ContextEdge>(callGraph);
    }

    /**
     * Get non-abstract methods, in topological order.
     *
     * Requires an acyclic callgraph.
     * @param rootMethod start with this method
     * @return a list of all non-abstract reachable methods, in topological order.
     */
    public List<MethodInfo> getReachableImplementations(MethodInfo rootMethod) {
        List<MethodInfo> implemented = new ArrayList<MethodInfo>();

        Set<MethodInfo> reachable = getReachableImplementationsSet(rootMethod);

        TopologicalOrderIterator<ExecutionContext, ContextEdge> ti = topDownIterator();
        while(ti.hasNext()) {
            MethodInfo m = ti.next().getMethodInfo();
            if(m != null && reachable.contains(m)) implemented.add(m);
        }
        return implemented;
    }

    /**
     * Retrieve non-abstract methods reachable from the given method.
     * All callgraph nodes reachable from nodes representing the given a method are collected
     *
     * @param rootMethod start method
     * @return a list of all reachable methods, sorted in topological order
     */
    public Set<MethodInfo> getReachableImplementationsSet(MethodInfo rootMethod) {
        Set<MethodInfo> implemented = new HashSet<MethodInfo>();

        for(ExecutionContext cgNode : methodNodes.get(rootMethod).getInstances()) {
            DepthFirstIterator<ExecutionContext, ContextEdge> ti =
                    new DepthFirstIterator<ExecutionContext, ContextEdge>(callGraph,cgNode);
            ti.setCrossComponentTraversal(false);
            while(ti.hasNext()) {
                MethodInfo m = ti.next().getMethodInfo();
                if(m == null) throw new AssertionError("Abstract method in callgraph");
                implemented.add(m);
            }
        }
        return implemented;
    }

    public List<MethodInfo> getReachableImplementations(MethodInfo rootMethod, CallString cs) {

        if(! this.hasNode(rootMethod, cs)) {
            throw new AssertionError("CallGraph#getReachableImplementations: no such node: "+
                new ExecutionContext(rootMethod, cs));
        }

        final List<MethodInfo> implemented = new ArrayList<MethodInfo>();
        final Set<MethodInfo> visited = new HashSet<MethodInfo>();

        ExecutionContext cgNode = this.getNode(rootMethod, cs);
        DepthFirstIterator<ExecutionContext, ContextEdge> ti =
                new DepthFirstIterator<ExecutionContext, ContextEdge>(callGraph,cgNode);
        ti.setCrossComponentTraversal(false);
        ti.addTraversalListener(new TraversalListenerAdapter<ExecutionContext,ContextEdge>() {
            @Override
            public void vertexFinished(VertexTraversalEvent<ExecutionContext> e) {
                MethodInfo m = e.getVertex().getMethodInfo();
                if (m == null) throw new AssertionError("Abstract method in callgraph");
                if (visited.add(m)) {
                    implemented.add(m);
                }
            }
        });

        while(ti.hasNext()) {
            ti.next();
        }
        Collections.reverse(implemented);
        return implemented;
    }

    /**
     * Retrieve non-abstract methods reachable from the given call graph node.
     * All callgraph nodes reachable from nodes representing the given a method are collected
     * @param rootMethod where to start
     * @param cs callstring of the invocation of the method, a node with this callstring (same length!) must exist.
     * @return a list of all reachable implementations, sorted in DFS order
     */
    public Set<MethodInfo> getReachableImplementationsSet(MethodInfo rootMethod, CallString cs) {

        if(! this.hasNode(rootMethod, cs)) {
            throw new AssertionError("CallGraph#getReachableImplementations: no such node: "+
                new ExecutionContext(rootMethod, cs));
        }
        Set<MethodInfo> implemented = new HashSet<MethodInfo>();

        ExecutionContext cgNode = this.getNode(rootMethod, cs);
        DepthFirstIterator<ExecutionContext, ContextEdge> ti =
                new DepthFirstIterator<ExecutionContext, ContextEdge>(callGraph,cgNode);
        ti.setCrossComponentTraversal(false);
        while(ti.hasNext()) {
            MethodInfo m = ti.next().getMethodInfo();
            if (m == null) throw new AssertionError("Abstract method in callgraph");
            implemented.add(m);
        }
        return implemented;
    }

    /**
     * For a given non-empty callstring, find all implementations which might get called by the last
     * invocation in the callstring, i.e. find all methods which might appear in the next entry of the
     * callstring. 
     *
     * @param cs callstring of the invocation, must contain at least one invokesite.
     * @return a list of all methods which might get invoked by the top invocation of the callstring,
     *         with their callstrings.
     */
    public Set<ExecutionContext> getImplementations(CallString cs) {

        InvokeSite invoke = cs.top();
        Set<ExecutionContext> methods = new HashSet<ExecutionContext>();
        MethodRef invokee = invoke.getInvokeeRef();

        // find all instances of possible invokers
        Set<ExecutionContext> invoker = getNodes(invoke.getMethod());
        for (ExecutionContext invokeNode : invoker) {

            // TODO filter out nodes which do not match the callstring, to speed up things a bit

            for (ContextEdge outEdge : callGraph.outgoingEdgesOf(invokeNode)) {
                CallString cgString = outEdge.getTarget().getCallString();

                // check if the target callstring matches the given callstring
                if (cgString.isEmpty()) {
                    // target has no callstring, must at least override the invoked method
                    if (outEdge.getTarget().getMethodInfo().overrides(invokee.getMethodInfo(), true)) {
                        methods.add(outEdge.getTarget());
                    }
                } else {
                    // if one of the callstrings is a suffix of the other, this context is a possible invocation
                    if (cs.matches(cgString)) {
                        methods.add(outEdge.getTarget());
                    }
                }
            }
        }

        return methods;
    }

    /**
     * @param m invoker method
     * @return methods possibly directly invoked from the given method
     */
    public List<ExecutionContext> getReferencedMethods(MethodInfo m) {
        Set<ExecutionContext> nodes = getNodes(m);
        List<ExecutionContext> succs = new ArrayList<ExecutionContext>();
        for(ExecutionContext node : nodes) {
            for(ContextEdge e : callGraph.outgoingEdgesOf(node)) {
                succs.add(callGraph.getEdgeTarget(e));
            }
        }
        return succs;
    }

    /**
     * @param node a callgraph node
     * @return true when the given method does not invoke any other methods
     */
    public boolean isLeafNode(ExecutionContext node) {
        return callGraph.outDegreeOf(node) == 0;
    }

    public boolean isLeafMethod(MethodInfo mi) {
        /* Using caching, as this method is used quite often */
        Boolean isLeafNode = leafNodeCache.get(mi);
        if(isLeafNode != null) return isLeafNode;
        isLeafNode = true;
        for(ExecutionContext node : getNodes(mi)) {
            if(! isLeafNode(node)) {
                isLeafNode = false;
                break;
            }
        }
        leafNodeCache.put(mi,isLeafNode);
        return isLeafNode;
    }

    /**
     * Get the maximum height of the call stack.
     * <p>A leaf method has height 1, an abstract method's height is the
     * maximum height of its children, and the height of an implemented method
     * is the maximum height of its children + 1. <p>
     * @return the maximum call stack
     */
    public List<ExecutionContext> getMaximalCallStack() {
        if(maxCallStackLeaf == null) calculateDepthAndHeight();
        ExecutionContext n = this.maxCallStackLeaf;
        List<ExecutionContext> maxCallStack = new ArrayList<ExecutionContext>();
        maxCallStack.add(n);
        while(maxCallstackDAG.containsKey(n)) {
            n = maxCallstackDAG.get(n);
            maxCallStack.add(n);
        }
        Collections.reverse(maxCallStack);
        return maxCallStack;
    }

    public int getMaxHeight() {
        calculateDepthAndHeight();
        return this.subgraphHeight.get(this.rootNode);
    }

    public ControlFlowGraph getLargestMethod() {
        ControlFlowGraph largest = null;
        int maxBytes = 0;
        for(MethodInfo mi : this.getReachableImplementationsSet(this.rootNode.getMethodInfo())) {
            ControlFlowGraph cfg = mi.getCode().getControlFlowGraph(false);
            int bytes = cfg.getNumberOfBytes();
            if(bytes > maxBytes) {
                largest = cfg;
                maxBytes = bytes;
            }
        }
        return largest;
    }

    public int getTotalSizeInBytes() {
        int bytes = 0;
        for (MethodInfo mi : this.getReachableImplementationsSet(this.rootNode.getMethodInfo())) {
             bytes += mi.getCode().getControlFlowGraph(false).getNumberOfBytes();
        }
        return bytes;
    }


    /*---------------------------------------------------------------------------*
     * Private methods
     *---------------------------------------------------------------------------*/

    /**
     * calculate the depth of each node, the height of the subgraph
     * rooted at that node, and a maximum call-stack tree.
     */
    private void calculateDepthAndHeight() {
        if(this.maxDistanceToRoot != null) return; // caching
        this.maxDistanceToRoot = new HashMap<ExecutionContext,Integer>();
        this.maxCallStackLeaf = this.getRootNode();
        this.maxCallstackDAG  = new HashMap<ExecutionContext,ExecutionContext>();
        this.subgraphHeight = new HashMap<ExecutionContext, Integer>();

        /* calculate longest distance to root and max call stack DAG */
        List<ExecutionContext> toList = new ArrayList<ExecutionContext>();
        TopologicalOrderIterator<ExecutionContext, ContextEdge> toIter =
                new TopologicalOrderIterator<ExecutionContext, ContextEdge>(callGraph);

        int globalMaxDist = 0;
        while(toIter.hasNext()) {
            ExecutionContext node = toIter.next();
            toList.add(node);
            int maxDist = 0;
            ExecutionContext maxCallStackPred = null;
            for(ContextEdge e : callGraph.incomingEdgesOf(node)) {
                ExecutionContext pred = callGraph.getEdgeSource(e);
                int distViaPred = maxDistanceToRoot.get(pred) + 1;
                if(distViaPred > maxDist) {
                    maxDist = distViaPred;
                    maxCallStackPred = pred;
                }
            }
            this.maxDistanceToRoot.put(node,maxDist);
            if(maxCallStackPred != null) this.maxCallstackDAG.put(node,maxCallStackPred);
            if(maxDist > globalMaxDist) this.maxCallStackLeaf = node;
        }

        /* calculate subgraph height */
        Collections.reverse(toList);
        for(ExecutionContext n : toList) {
            int maxHeight = 0;
            for(ContextEdge e : callGraph.outgoingEdgesOf(n)) {
                int predHeight = subgraphHeight.get(callGraph.getEdgeTarget(e));
                maxHeight = Math.max(maxHeight, predHeight + 1);
            }
            subgraphHeight.put(n, maxHeight);
        }
    }

    private void invalidate() {
        maxCallStackLeaf = null;
        maxDistanceToRoot = null;
        maxCallstackDAG = null;
        subgraphHeight = null;
        leafNodeCache = new HashMap<MethodInfo, Boolean>();
    }

    /**
     * Add a context to the callGraph as vertex and add it to the methodNodes map.
     * @param context the context to add.
     */
    private void onAddExecutionContext(ExecutionContext context) {
        MethodNode node = methodNodes.get(context.getMethodInfo());
        if (node == null) {
            node = new MethodNode(context.getMethodInfo());
            methodNodes.put(context.getMethodInfo(), node);
        }
        node.addInstance(context);

        if (mergedCallGraph != null) {
            mergedCallGraph.addVertex(node);
        }
    }

    private void onRemoveExecutionContext(ExecutionContext context) {
        MethodNode node = methodNodes.get(context.getMethodInfo());
        node.removeInstance(context);

        if (node.getInstances().isEmpty()) {
            methodNodes.remove(context.getMethodInfo());

            if (mergedCallGraph != null) {
                mergedCallGraph.removeVertex(node);
            }
        }
    }

    private void onAddCallGraphEdge(ContextEdge edge) {
        MethodNode invoker = methodNodes.get(edge.getSource().getMethodInfo());
        MethodNode invokee = methodNodes.get(edge.getTarget().getMethodInfo());

        InvokeEdge invoke = mergedCallGraph.getEdge(invoker, invokee);
        if (invoke == null) {
            invoke = mergedCallGraph.addEdge(invoker, invokee);
        }

        if (edge.getTarget().getCallString().length() > 0) {
            invoke.addInvokeSite(edge.getTarget().getCallString().top());
        }
    }

    private void onRemoveCallGraphEdge(ContextEdge edge) {
        MethodNode invoker = methodNodes.get(edge.getSource().getMethodInfo());
        MethodNode invokee = methodNodes.get(edge.getTarget().getMethodInfo());

        // check all incoming edges if there is an edge to the invoker left
        boolean remove = true;
        for (ExecutionContext ctx : invokee.getInstances()) {
            for (ContextEdge srcEdge : callGraph.incomingEdgesOf(ctx)) {
                if (srcEdge.getSource().getMethodInfo().equals(invoker.getMethodInfo())) {
                    remove = false;
                    break;
                }
            }
        }

        if (remove) {
            mergedCallGraph.removeEdge(invoker, invokee);
        } else {
            InvokeEdge invoke = mergedCallGraph.getEdge(invoker, invokee);
            if (edge.getTarget().getCallString().length() > 0) {
                invoke.removeInvokeSite(edge.getTarget().getCallString().top());
            }
        }
    }

    private String cyclicCallGraphMsg(Pair<List<ExecutionContext>, List<ExecutionContext>> cycleWithPrefix) {
        List<ExecutionContext> cycle = cycleWithPrefix.second();
        List<ExecutionContext> prefix = cycleWithPrefix.first();
        StringBuffer sb = new StringBuffer();
        sb.append("Cyclic Callgraph !\n");
        sb.append("One cycle is:\n");
        for(ExecutionContext cn : cycle) sb.append("  ").append(cn).append("\n");
        sb.append("Reachable via:\n");
        for(ExecutionContext cn : prefix) sb.append("  ").append(cn).append("\n");
        return sb.toString();
    }

}
