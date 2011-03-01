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
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.type.MethodRef;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DirectedMaskSubgraph;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.MaskFunctor;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
 * are not represented in this graph. </p>
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class CallGraph {

    public static final Logger logger = Logger.getLogger(LogConfig.LOG_CODE + ".CallGraph");

    /**
     * Interface for a callgraph construction.
     */
    public interface CallgraphConfig {
        /**
         * Get a set of all execution contexts possibly invoked by a given context.
         * To be consistent with {@link AppInfo#findImplementations(InvokeSite)} and to detect
         * incomplete classes, this must return an empty set if only some of the possible implementations
         * are found (e.g. superclasses are missing).
         *
         * @param context the calling context
         * @return a set of all possible invoked implementations or an empty set if unknown
         */
        Set<ExecutionContext> getInvokedMethods(ExecutionContext context);
    }

    /*---------------------------------------------------------------------------*
     * Graph node and edge classes
     *---------------------------------------------------------------------------*/

    /**
     * A node representing a methodInfo, and stores references to all
     * execution contexts of this method in the callgraph.
     */
    public static class MethodNode implements MethodContainer {
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

        @Override
        public String toString() {
            String txt = (instances.size() == 1 ? "1 instance" : instances.size() + " instances");
            return methodInfo.toString() + " (" + txt + ")";
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

        @Override
        public String toString() {
            return (invokeSites.size() == 1) ? "1 invokesite" : invokeSites.size() + " invokesites";
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

        @Override
        public int hashCode() {
            return source.hashCode() * 31 + target.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null) return false;
            if (!(obj instanceof ContextEdge)) return false;
            ContextEdge other = (ContextEdge) obj;
            return source.equals(other.getSource()) && target.equals(other.getTarget());
        }
    }

    /*---------------------------------------------------------------------------*
     * Internal classes
     *---------------------------------------------------------------------------*/

    /**
     * This listener is attached to the (sub-)callgraph, and updates the methodNodes and mergedGraph
     * structures.
     */
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

    /**
     * This graph is attached to the main full callgraph for every subgraph, and propagates
     * changes to the main graph to the given subgraph.
     */
    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    private class SubgraphUpdateListener implements GraphListener<ExecutionContext, ContextEdge> {
        private final CallGraph subgraph;

        private SubgraphUpdateListener(CallGraph subgraph) {
            this.subgraph = subgraph;
        }
        @Override
        public void edgeAdded(GraphEdgeChangeEvent<ExecutionContext, ContextEdge> e) {
            // first we check if the target is already in the subgraph
            // if no new nodes required, does not change connectivity, simply add edge
            ExecutionContext target = e.getEdge().getTarget();
            if (!subgraph.callGraph.containsVertex(target)) {
                // target must be added first, so we need to clone everything which is reachable from the target
                subgraph.cloneReachableGraph(Collections.singleton(target));
            }
            subgraph.callGraph.addEdge(e.getEdge().getSource(), target);
        }
        @Override
        public void edgeRemoved(GraphEdgeChangeEvent<ExecutionContext, ContextEdge> e) {
            subgraph.removeEdge(e.getEdge(), true);
        }
        @Override
        public void vertexAdded(GraphVertexChangeEvent<ExecutionContext> e) {
            // No need to do anything yet.. since the vertex was just added, there are no edges to it,
            // so it is not reachable
        }
        @Override
        public void vertexRemoved(GraphVertexChangeEvent<ExecutionContext> e) {
            // checks if subgraph contains vertex itself, edges to the node are removed automatically
            subgraph.callGraph.removeVertex(e.getVertex());
        }
    }

    private class ContextFilter implements MaskFunctor<ExecutionContext,ContextEdge> {
        private boolean skipIsolated;
        private boolean skipNoim;

        private ContextFilter(boolean skipIsolated, boolean skipNoim) {
            this.skipIsolated = skipIsolated;
            this.skipNoim = skipNoim;
        }
        @Override
        public boolean isEdgeMasked(ContextEdge edge) {
            // Edges from masked nodes are masked automatically
            return false;
        }
        @Override
        public boolean isVertexMasked(ExecutionContext vertex) {
            if (skipIsolated && callGraph.inDegreeOf(vertex) == 0
                             && callGraph.outDegreeOf(vertex) == 0) {
                return true;
            }
            if (skipNoim && callGraph.inDegreeOf(vertex) == 0
                         && callGraph.outDegreeOf(vertex) == 1) {
                ExecutionContext target = callGraph.outgoingEdgesOf(vertex).iterator().next().getTarget();
                if ("com.jopdesign.sys.JVMHelp".equals(target.getMethodInfo().getClassName()) &&
                    "noim".equals(target.getMethodInfo().getShortName())) {
                    return true;
                }
            }
            return false;
        }
    }

    private class MethodFilter implements MaskFunctor<MethodContainer,Object> {
        private final DirectedGraph<MethodContainer, Object> graph;
        private boolean skipIsolated;
        private boolean skipNoim;

        private MethodFilter(DirectedGraph<MethodContainer,Object> graph, boolean skipIsolated, boolean skipNoim) {
            this.graph = graph;
            this.skipIsolated = skipIsolated;
            this.skipNoim = skipNoim;
        }
        @Override
        public boolean isEdgeMasked(Object edge) {
            // Edges from masked nodes are masked automatically
            return false;
        }
        @Override
        public boolean isVertexMasked(MethodContainer vertex) {
            if (skipIsolated && graph.inDegreeOf(vertex) == 0
                             && graph.outDegreeOf(vertex) == 0) {
                return true;
            }
            if (skipNoim && graph.inDegreeOf(vertex) == 0
                         && graph.outDegreeOf(vertex) == 1) {
                Object edge = graph.outgoingEdgesOf(vertex).iterator().next();
                MethodContainer target = graph.getEdgeTarget(edge);
                // TODO make this check less .. hardcoded
                if ("com.jopdesign.sys.JVMHelp".equals(target.getMethodInfo().getClassName()) &&
                    "noim".equals(target.getMethodInfo().getShortName())) {
                    return true;
                }
            }
            return false;
        }
    }

    //
    // Fields
    // ~~~~~~
    private final Set<ExecutionContext> rootNodes;
    private final CallgraphConfig config;

    private ListenableDirectedGraph<ExecutionContext, ContextEdge> callGraph;
    private DirectedGraph<MethodNode, InvokeEdge> mergedCallGraph;
    
    private Map<CallGraph, SubgraphUpdateListener> subgraphs;
    private CallGraph parent;

    private Set<ClassInfo> classInfos;
    private Map<MethodInfo,MethodNode> methodNodes;
    private boolean loopFree = false;

    //
    // Caching Fields
    // ~~~~~~~~~~~~~~
    private Map<ExecutionContext, Integer> maxDistanceToRoot = null;
    private Map<ExecutionContext, ExecutionContext> maxCallstackDAG = null;
    private Map<ExecutionContext, Integer> subgraphHeight = null;
    private ExecutionContext maxCallStackLeaf = null;
    private Map<MethodInfo,Boolean> leafNodeCache;

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
     * @return a freshly constructed callgraph.
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
     * @return a freshly constructed callgraph.
     */
    public static CallGraph buildCallGraph(MethodInfo rootMethod, CallgraphConfig config)
    {
        ExecutionContext root = new ExecutionContext(rootMethod);
        CallGraph cg = new CallGraph(Collections.singleton(root),config);
        cg.build();
        return cg;
    }

    /**
     * Build a callgraph with all root methods of AppInfo.
     * This also adds all static initializers of all classes to the callgraph.
     *
     * @see AppInfo#getRootMethods()
     * @param appInfo the AppInfo to use
     * @param config the config class to use to build this graph
     * @return a freshly constructed callgraph.
     */
    public static CallGraph buildCallGraph(AppInfo appInfo, CallgraphConfig config) {
        Collection<MethodInfo> rootMethods = appInfo.getRootMethods();
        Set<ExecutionContext> roots = new HashSet<ExecutionContext>(rootMethods.size());

        for (MethodInfo m : rootMethods) {
            roots.add(new ExecutionContext(m));
        }
        for (MethodInfo m : appInfo.getClinitMethods()) {
            roots.add(new ExecutionContext(m));
        }
        for (MethodInfo m : appInfo.getThreadRootMethods()) {
            roots.add(new ExecutionContext(m));
        }

        CallGraph cg = new CallGraph(roots, config);
        cg.build();
        return cg;
    }

    /*---------------------------------------------------------------------------*
     * Init and build callgraph (private)
     *---------------------------------------------------------------------------*/

    /**
     * Initialize a CallGraph object.
     * @param rootMethods The root methods of the callgraph (not abstract).
     * @param config the config class to use to build this graph
     */
    protected CallGraph(Collection<ExecutionContext> rootMethods, CallgraphConfig config) {
        this.rootNodes = new HashSet<ExecutionContext>(rootMethods);
        this.config = config;
        this.subgraphs = new HashMap<CallGraph,SubgraphUpdateListener>(1);

        // We need a custom ContextEdge here to keep the references to the vertices for the removeEdge listener
        this.callGraph = new ListenableDirectedGraph<ExecutionContext,ContextEdge>(
            new DefaultDirectedGraph<ExecutionContext,ContextEdge>(
                new EdgeFactory<ExecutionContext,ContextEdge>() {
                    @Override
                    public ContextEdge createEdge(ExecutionContext sourceVertex, ExecutionContext targetVertex) {
                        return new ContextEdge(sourceVertex,targetVertex);
                    }
                }) );
        this.methodNodes = new HashMap<MethodInfo, MethodNode>();
        this.classInfos = new HashSet<ClassInfo>();
    }

    protected CallGraph(CallGraph parent, Collection<ExecutionContext> rootNodes, CallgraphConfig config) {
        this(rootNodes, config);
        this.parent = parent;
    }
    
    /**
     * Build and initialize everything, perform checks
     */
    private void build() {

        logger.info("Starting construction of callgraph with roots " + MiscUtils.toString(rootNodes, 3));

        this.buildGraph();

        logger.info("Finished constructing callgraph");

        invalidate();
    }

    public void checkAcyclicity() {
        /* Check the callgraph is cycle free */
        for (ExecutionContext rootNode : rootNodes) {

            logger.info("Checking for loops in callgraph starting at "+rootNode);

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
        }
        loopFree = true;
        logger.info("No loops found in callgraph");
    }

    /**
     * Build the callgraph.
     *
     * <p>NEW: now we also use callstrings to get a more precise call graph model</p>
     */
    private void buildGraph() {

        // Note that updating methodNodes and classInfos is now done by the GraphUpdateListener
        callGraph.addGraphListener(new GraphUpdateListener());

        if (parent == null) {
            /* Initialize DFS data structures and lookup maps */
            Stack<ExecutionContext> todo = new Stack<ExecutionContext>();
            for (ExecutionContext rootNode : rootNodes) {
                callGraph.addVertex(rootNode);
                todo.push(rootNode);
            }

            while(! todo.empty()) {
                ExecutionContext current = todo.pop();

                logger.debug("Processing " +current);

                Set<ExecutionContext> invoked = config.getInvokedMethods(current);
                for (ExecutionContext cgn : invoked) {

                    if (!callGraph.containsVertex(cgn)) {
                        callGraph.addVertex(cgn);
                        todo.push(cgn);
                    }
                    logger.trace(" - found invoke of " +cgn);
                    callGraph.addEdge(current, cgn);
                }
            } /* end while */
        } else {
            // build the the graph using all reachable nodes from the parent graph
            cloneReachableGraph(rootNodes);
        }
    }

    /**
     * Copy all reachable nodes (and edges) from the parent graph, starting at the root
     * @param roots where to start cloning
     */
    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    private void cloneReachableGraph(Collection<ExecutionContext> roots) {

        List<ExecutionContext> newNodes = new LinkedList<ExecutionContext>();

        for (ExecutionContext root : roots) {
            DepthFirstIterator<ExecutionContext, ContextEdge> dfs =
                    new DepthFirstIterator<ExecutionContext, ContextEdge>(parent.callGraph, root);

            while (dfs.hasNext()) {
                ExecutionContext ec = dfs.next();
                if (callGraph.addVertex(ec)) {
                    // TODO if a node is already in the subgraph (ie. reachable), skip its children
                    //      (must all be here already or if it is a circle they will be added later)
                    newNodes.add(ec);
                }
            }
        }

        // add all edges between new nodes and edges to existing nodes.
        for (ExecutionContext ec : newNodes) {
            // all outgoing edges of new nodes are new (we do not care about ingoing edges)
            for (ContextEdge e : parent.callGraph.outgoingEdgesOf(ec)) {
                // source and target both must exist now
                callGraph.addEdge(e.getSource(), e.getTarget());
            }
        }

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

    public boolean hasMethod(MethodInfo m) {
        return methodNodes.containsKey(m);
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
     * Get a list of all nodes which are direct successors of a given node.
     * @param node the parent node.
     * @return all its direct successors.
     */
    public List<ExecutionContext> getChildren(ExecutionContext node) {
        Set<ContextEdge> out = callGraph.outgoingEdgesOf(node);
        List<ExecutionContext> childs = new ArrayList<ExecutionContext>(out.size());
        for (ContextEdge e : out) {
            childs.add(e.getTarget());
        }
        return childs;
    }
    
    /**
     * Export the complete callgraph as .dot file
     * @param w Write the graph to this writer. To improve performance, use a buffered writer.
     * @throws IOException if writing fails
     */
    public void exportDOT(Writer w) throws IOException {
        exportDOT(w, false, false, false);
    }

    /**
     * Export the callgraph as a .dot file.
     *
     * @param w Write the graph to this writer. To improve performance, use a buffered writer.
     * @param merged if true, export the merged callgraph instead of the full graph.
     * @param skipIsolated if true, do not export isolated nodes.
     * @param skipNoImp if true, do not export roots with only one edge to com.jopdesign.sys.JVMHelp.noim().
     * @throws IOException if writing fails.
     */
    public void exportDOT(Writer w, boolean merged, final boolean skipIsolated, final boolean skipNoImp) throws IOException {
        AdvancedDOTExporter<MethodContainer, Object> exporter = new AdvancedDOTExporter<MethodContainer, Object>();
        exporter.setGraphAttribute("rankdir", "LR");

        if (merged && mergedCallGraph == null) {
            buildMergedGraph();
        }

        // Why is this an unchecked statement??
        @SuppressWarnings({"unchecked"})
        DirectedGraph<MethodContainer, Object> graph =
                (DirectedGraph<MethodContainer,Object>) (merged ? mergedCallGraph : callGraph);

        if (skipIsolated || skipNoImp) {
            graph = new DirectedMaskSubgraph<MethodContainer, Object>(graph,
                            new MethodFilter(graph, skipIsolated, skipNoImp));
        }

        exporter.exportDOT(w, graph);
    }

    public Set<ClassInfo> getRootClass() {
        Set<ClassInfo> classes = new HashSet<ClassInfo>(2);
        for (ExecutionContext root : rootNodes) {
            classes.add(root.getMethodInfo().getClassInfo());
        }
        return classes;
    }

    /**
     * Get a set of all root methods which were used to construct the callgraph.
     * Note that a root method does not necessarily need to be a root of the graph, even
     * if it is acyclic, and there might be nodes in the graph for this method which are not
     * root nodes.
     *
     * @see #getRootNodes()
     * @return The methods of all root nodes.
     */
    public Set<MethodInfo> getRootMethods() {
        Set<MethodInfo> methods = new HashSet<MethodInfo>();
        for (ExecutionContext root : rootNodes) {
            methods.add(root.getMethodInfo());
        }
        return methods;
    }

    /**
     * Get a set of all root nodes which were used to construct the callgraph.
     * Note that a root node does not necessarily need to be a root of the graph, even
     * if it is acyclic!
     *
     * @return All (initial) roots of the callgraph.
     */
    public Set<ExecutionContext> getRootNodes() {
        return Collections.unmodifiableSet(rootNodes);
    }

    public Set<ClassInfo> getClassInfos() {
        return classInfos;
    }

    /*---------------------------------------------------------------------------*
     * Modify the graph
     *---------------------------------------------------------------------------*/

    /**
     * Remove an edge from the graph.
     *
     * @param source source of the edge
     * @param target target of the edge
     * @param removeUnreachable if true, remove all nodes which are no longer reachable from the root
     * @return true if the edge existed
     */
    public boolean removeEdge(ExecutionContext source, ExecutionContext target, boolean removeUnreachable) {
        ContextEdge edge = callGraph.getEdge(source, target);
        if (edge == null) return false;
        return removeEdge(edge, removeUnreachable);
    }

    /**
     * Remove an edge from the graph.
     *
     * @param edge the edge to remove.
     * @param removeUnreachable if true, remove all nodes which are no longer reachable from the root
     * @return true if the edge existed
     */
    public boolean removeEdge(ContextEdge edge, boolean removeUnreachable) {
        // Edge equals is defined over the touching execution contexts, so we can do this here
        // without worrying about same instances
        boolean exists = callGraph.removeEdge(edge);

        if (exists && removeUnreachable) {
            // do we need to worry about loosing connectivity?
            ExecutionContext target = edge.getTarget();
            Set<ExecutionContext> queue = new HashSet<ExecutionContext>();
            queue.add(target);

            while (!queue.isEmpty()) {
                // take one down ..
                ExecutionContext ec = queue.iterator().next();
                queue.remove(ec);

                if (callGraph.incomingEdgesOf(ec).isEmpty()) {
                    // this node has now become disconnected.. remove it, queue all children
                    queue.addAll(getChildren(ec));
                    // Note that we do not need to worry about any onRemove methods, they are called by the listeners
                    callGraph.removeVertex(ec);
                }
            }
        }
        return exists;
    }

    public boolean removeNode(ExecutionContext context, boolean removeUnreachable) {
        if (removeUnreachable) {
            // we only need to do this if we want to remove reachable nodes, since removing a vertex
            // also removes its edges
            for (ContextEdge e : callGraph.outgoingEdgesOf(context)) {
                removeEdge(e, removeUnreachable);
            }
        }
        // Note that we do not need to worry about any onRemove methods, they are called by the listeners
        return callGraph.removeVertex(context);
    }

    public boolean removeMethod(MethodInfo method, boolean removeUnreachable) {
        MethodNode node = methodNodes.get(method);
        if (node == null) return false;

        // need to copy the list here, since it will be modified when we remove the nodes
        List<ExecutionContext> contexts = new ArrayList<ExecutionContext>(node.getInstances());
        for (ExecutionContext c : contexts) {
            // simply remove all nodes, the rest will be updated by the listeners
            removeNode(c, removeUnreachable);
        }
        return true;
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

    public CallGraph getSubGraph(MethodInfo rootMethod) {
        MethodNode node = getMethodNode(rootMethod);
        return getSubGraph(node.getInstances());
    }

    /**
     * Get a subgraph starting at a given node which contains all reachable nodes and edges.
     * The subgraph is backed by this graph, so any modifications to it will be reflected
     * in the subgraph (but not the other way round!)
     *
     * @param roots root nodes of the new graph.
     * @return a new subgraph, or an existing subgraph which starts at the same root
     */
    public CallGraph getSubGraph(Set<ExecutionContext> roots) {

        // first check if we already have this graph..
        for (CallGraph subgraph : subgraphs.keySet()) {
            if (subgraph.getRootNodes().equals(roots)) {
                return subgraph;
            }
        }

        CallGraph subGraph = new CallGraph(this, roots, config);
        subGraph.build();
        SubgraphUpdateListener listener = new SubgraphUpdateListener(subGraph);
        callGraph.addGraphListener(listener);
        // we use a map here to keep the listener, needed for remove
        // TODO maybe add a listener to subgraph which updates the parent graph when it is modified too?
        subgraphs.put(subGraph,listener);

        return subGraph;
    }

    public void removeSubGraph(CallGraph subgraph) {
        SubgraphUpdateListener listener = subgraphs.get(subgraph);
        callGraph.removeGraphListener(listener);
        subgraphs.remove(subgraph);
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
     * <p>
     * This is only a lookup in the callgraph, and does not check if the invocation is a special invoke,
     * so if callstring length of the callgraph is zero, the results are not correct. Instead use
     * {@link AppInfo#findImplementations(CallString)} which handles all special cases and falls back
     * to the default callgraph.
     * </p>
     *
     * @param cs callstring of the invocation, must contain at least one invokesite.
     * @return a list of all methods which might get invoked by the top invocation of the callstring,
     *         with their callstrings.
     */
    public Set<ExecutionContext> getImplementations(CallString cs) {

        InvokeSite invoke = cs.top();
        Set<ExecutionContext> methods = new HashSet<ExecutionContext>();

        MethodRef invokeeRef = invoke.getInvokeeRef();
        MethodInfo invokee = invokeeRef.getMethodInfo();
        if (invokee == null) {
            // The target of the invoke is unknown .. we wont find any implementations here 
            logger.debug("Tried to find implementations of unknown method "+invokeeRef);
            return methods;
        }

        // if the invoke is not virtual, should we look it up in the graph anyway?
        // We could just return new ExecutionContext(invokee, cs);
        // But that's what AppInfo#findImplementations() is for, here we only lookup the callgraph,
        // but then we cannot have different callgraphs and perform precise lookups on all of them,
        // since that method only looks into the default callgraph (but why would we want multiple
        // graphs anyway?)

        // find all instances of possible invokers
        Set<ExecutionContext> invoker = getNodes(invoke.getMethod());
        for (ExecutionContext invokeNode : invoker) {

            // TODO filter out nodes which do not match the callstring, to speed up things a bit

            for (ContextEdge outEdge : callGraph.outgoingEdgesOf(invokeNode)) {
                CallString cgString = outEdge.getTarget().getCallString();

                // check if the target callstring matches the given callstring
                if (cgString.isEmpty()) {
                    // target has no callstring, must at least override the invoked method
                    if (outEdge.getTarget().getMethodInfo().overrides(invokee, true)) {
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
        int maxHeight = 0;
        for (ExecutionContext rootNode : rootNodes) {
            maxHeight = Math.max(maxHeight, this.subgraphHeight.get(rootNode));
        }
        return maxHeight;
    }

    public ControlFlowGraph getLargestMethod() {
        ControlFlowGraph largest = null;
        int maxBytes = 0;
        for (ExecutionContext rootNode : rootNodes) {
            for(MethodInfo mi : this.getReachableImplementationsSet(rootNode.getMethodInfo())) {
                int bytes = mi.getCode().getNumberOfBytes();
                if(bytes > maxBytes) {
                    largest = mi.getCode().getControlFlowGraph(false);
                    maxBytes = bytes;
                }
            }
        }
        return largest;
    }

    public int getTotalSizeInBytes() {
        int bytes = 0;
        for (ExecutionContext rootNode : rootNodes) {
            for (MethodInfo mi : this.getReachableImplementationsSet(rootNode.getMethodInfo())) {
                 bytes += mi.getCode().getNumberOfBytes();
            }
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

        if (!loopFree) {
            throw new AssertionError("Callgraph needs to be checked for acyclicity first.");
        }
        if (rootNodes.size() != 1) {
            // TODO The problem here is the calculation of maxDistanceToRoot and maxCallStack
            //      The algorithms to calculate them need to be checked/updated to work with multiple roots
            //      The various getters above should already work for multiple roots
            //      Also, it may be nice to know the root of the max callstack...
            throw new AssertionError("Calculating max stack for callgraphs with multiple roots is not supported!");
        }
        ExecutionContext rootNode = rootNodes.iterator().next();

        this.maxDistanceToRoot = new HashMap<ExecutionContext,Integer>();
        this.maxCallStackLeaf = rootNode;
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

        // doing this here will call add(ClassInfo) more often than iterating over all
        // method nodes, but doing it here it is more robust when the graph is modified
        classInfos.add(node.getMethodInfo().getClassInfo());

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

            // TODO we might need to remove the class of the method from classInfos too!
        }

        rootNodes.remove(context);
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

        // graph may not be acyclic anymore, need to check again
        loopFree = false;
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
