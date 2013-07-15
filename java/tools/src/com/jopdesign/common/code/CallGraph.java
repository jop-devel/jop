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
import com.jopdesign.common.ImplementationFinder;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationError;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.config.StringOption;
import com.jopdesign.common.graphutils.AdvancedDOTExporter;
import com.jopdesign.common.graphutils.BackEdgeFinder;
import com.jopdesign.common.graphutils.DirectedCycleDetector;
import com.jopdesign.common.graphutils.GraphUtils;
import com.jopdesign.common.graphutils.InvokeDot;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.AppInfoException;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.misc.Ternary;
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
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.MaskFunctor;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.UnmodifiableDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
 * <p>
 * This callgraph implementation does not require all nodes to have the same callstring length.
 * However, to simplify some algorithms some methods which modify the graph may assume that
 * the callstring length never decreases along any path in the callgraph.. for now.
 * We may later remove this restriction to compress the graph, by merging nodes with different contexts into
 * a single node when the invoked methods are identical for each invokesite. If you want to find all references
 * of an invokesite in the callstrings of the nodes of the graph, you need to go down from the invoker as deep as
 * the maximum callstring length used to create the graph.
 * </p>
 * <p>
 * In any case we require that the set of children for every node does not contain matching execution contexts.
 * Two execution contexts match if they refer to the same method and one of their callstrings is a suffix of the other
 * (note that we do not need to handle the case if the callstrings are equal, since in this case the contexts are equal
 * and the callgraph does not contain duplicate contexts). This implies that if a node has a context with an empty
 * callstring as child it cannot have another context with a nonempty callstring for the same method as child.
 * </p>
 *
 * @see CallgraphBuilder#getInvokedMethods(ExecutionContext)
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class CallGraph implements ImplementationFinder {

    public static final Logger logger = Logger.getLogger(LogConfig.LOG_CODE + ".CallGraph");

    public enum DUMPTYPE { off, full, merged, both }

    // This option should always be added to Config.getDebugGroup()
    private static final StringOption CALLGRAPH_DIR =
            new StringOption("cgdir", "Directory to put the callgraph files into", "${outdir}/callgraph");

    /**
     * Needs to be added to the debug optiongroup if the dumpCallgraph method is used.
     */
    private static final Option[] dumpOptions = { CALLGRAPH_DIR };

    public static void registerOptions(Config config) {
        config.getDebugGroup().addOptions(dumpOptions);
        InvokeDot.registerOptions(config);
    }

    /**
     * Interface for a callgraph construction.
     */
    public interface CallgraphBuilder {
        /**
         * Get a set of all execution contexts possibly invoked by a given context.
         * To be consistent with {@link AppInfo#findImplementations(InvokeSite)} and to detect
         * incomplete classes, this must return an empty set if only some of the possible implementations
         * are found (e.g. superclasses are missing).
         * <p>
         * To simplify some implementations, the callstrings returned by this method should never be
         * shorter than the callstring of the argument.
         * </p>
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
            instances = new LinkedHashSet<ExecutionContext>();
        }

        public MethodInfo getMethodInfo() {
            return methodInfo;
        }

        public Set<ExecutionContext> getInstances() {
            return Collections.unmodifiableSet(instances);
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
            invokeSites = new LinkedHashSet<InvokeSite>();
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
        private final int hash;

        private ContextEdge(ExecutionContext source, ExecutionContext target) {
            this.source = source;
            this.target = target;
            // this result is used *very* often ..
            this.hash = source.hashCode() * 31 + target.hashCode();
        }

        public ExecutionContext getSource() {
            return source;
        }

        public ExecutionContext getTarget() {
            return target;
        }

        @Override
        public int hashCode() {
            return hash;
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
                addMergedGraphEdge(e.getEdge());
            }
            // graph may not be acyclic anymore, need to check again
            acyclic = Ternary.UNKNOWN;
        }

        @Override
        public void edgeRemoved(GraphEdgeChangeEvent<ExecutionContext, ContextEdge> e) {
            if (mergedCallGraph != null) {
                removeMergedGraphEdge(e.getEdge());
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
    private final CallgraphBuilder builder;

    private ListenableDirectedGraph<ExecutionContext, ContextEdge> callGraph;
    private DirectedGraph<MethodNode, InvokeEdge> mergedCallGraph;
    
    private Map<CallGraph, SubgraphUpdateListener> subgraphs;
    private CallGraph parent;

    private Set<ClassInfo> classInfos;
    private Map<MethodInfo,MethodNode> methodNodes;
    private Ternary acyclic = Ternary.UNKNOWN;

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
     * @param builder the builder class to use to build this graph
     * @return a freshly constructed callgraph.
     * @throws MethodNotFoundException if the referenced main method was not found
     */
    public static CallGraph buildCallGraph(AppInfo appInfo, String className, String methodSig,
                                       CallgraphBuilder builder)
                                                    throws MethodNotFoundException
    {
        MethodInfo rootMethod = appInfo.getMethodInfo(className,methodSig);
        return buildCallGraph(rootMethod, builder);
    }

    /**
     * Build a callgraph rooted at the given method
     *
     * @see AppInfo#getMethodInfo(String, String)
     * @param rootMethod The root method of the callgraph
     * @param builder the builder class to use to build this graph
     * @return a freshly constructed callgraph.
     */
    public static CallGraph buildCallGraph(MethodInfo rootMethod, CallgraphBuilder builder)
    {
        return buildCallGraph(Collections.singleton(rootMethod), builder);
    }

    /**
     * Build a callgraph rooted at the given set of methods
     *
     * @param rootMethods The root methods of the callgraph
     * @param builder the builder class to use to build this graph
     * @return a freshly constructed callgraph.
     */
    public static CallGraph buildCallGraph(Collection<MethodInfo> rootMethods, CallgraphBuilder builder)
    {
        List<ExecutionContext> roots = new ArrayList<ExecutionContext>(rootMethods.size());
        for (MethodInfo method : rootMethods) {
            roots.add(new ExecutionContext(method));
        }
        CallGraph cg = new CallGraph(roots, builder);
        cg.build();
        return cg;
    }

    /**
     * Build a callgraph with all root methods of AppInfo.
     * This also adds all static initializers of all classes and all Runnable.run() methods
     * to the callgraph roots.
     *
     * @see AppInfo#getRootMethods()
     * @param appInfo the AppInfo to use
     * @param builder the builder class to use to build this graph
     * @return a freshly constructed callgraph.
     */
    public static CallGraph buildCallGraph(AppInfo appInfo, CallgraphBuilder builder) {
        Collection<MethodInfo> rootMethods = appInfo.getRootMethods();
        Set<ExecutionContext> roots = new LinkedHashSet<ExecutionContext>(rootMethods.size());

        for (MethodInfo m : rootMethods) {
            roots.add(new ExecutionContext(m));
        }
        for (MethodInfo m : appInfo.getClinitMethods()) {
            roots.add(new ExecutionContext(m));
        }
        for (MethodInfo m : appInfo.getThreadRootMethods(false)) {
            roots.add(new ExecutionContext(m));
        }

        CallGraph cg = new CallGraph(roots, builder);
        cg.build();
        return cg;
    }

    /*---------------------------------------------------------------------------*
     * Init and build callgraph (private)
     *---------------------------------------------------------------------------*/

    /**
     * Initialize a CallGraph object.
     * @param rootMethods The root methods of the callgraph (not abstract).
     * @param builder the builder class to use to build this graph
     */
    protected CallGraph(Collection<ExecutionContext> rootMethods, CallgraphBuilder builder) {
        this.rootNodes = new LinkedHashSet<ExecutionContext>(rootMethods);
        this.builder = builder;
        this.subgraphs = new LinkedHashMap<CallGraph,SubgraphUpdateListener>(1);

        // We need a custom ContextEdge here to keep the references to the vertices for the removeEdge listener
        this.callGraph = new ListenableDirectedGraph<ExecutionContext,ContextEdge>(
            new DefaultDirectedGraph<ExecutionContext,ContextEdge>(
                new EdgeFactory<ExecutionContext,ContextEdge>() {
                    @Override
                    public ContextEdge createEdge(ExecutionContext sourceVertex, ExecutionContext targetVertex) {
                        return new ContextEdge(sourceVertex,targetVertex);
                    }
                }) );
        this.methodNodes = new LinkedHashMap<MethodInfo, MethodNode>();
        this.classInfos = new LinkedHashSet<ClassInfo>();
    }

    protected CallGraph(CallGraph parent, Collection<ExecutionContext> rootNodes, CallgraphBuilder builder) {
        this(rootNodes, builder);
        this.parent = parent;
    }
    
    /**
     * Build and initialize everything, perform checks
     */
    private void build() {

        logger.debug("Starting construction of callgraph with roots " + MiscUtils.toString(rootNodes, 3));

        this.buildGraph();

        logger.debug("Finished constructing callgraph");

        invalidate();
    }

    public void checkAcyclicity() throws AppInfoException {
        /* Check the callgraph is cycle free */
        for (ExecutionContext rootNode : rootNodes) {

            logger.debug("Checking for loops in callgraph starting at "+rootNode);

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
                acyclic = Ternary.FALSE;
                throw new AppInfoException(cyclicCallGraphMsg(cycle));
            }
        }
        acyclic = Ternary.TRUE;
        logger.debug("No loops found in callgraph");
    }

    public void setAcyclicity(boolean acyclic) {
        this.acyclic = Ternary.valueOf(acyclic);
    }

    public Ternary getAcyclicity() {
        return acyclic;
    }

    public boolean isAcyclic() {
        if (acyclic == Ternary.UNKNOWN) {
            try {
                checkAcyclicity();
            } catch (AppInfoException e) {
                logger.debug("Found loop in callgraph: "+e.getMessage());
            }
        }
        return acyclic == Ternary.TRUE;
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

                if (logger.isTraceEnabled()) {
                    logger.trace("Processing " +current);
                }

                Set<ExecutionContext> invoked = builder.getInvokedMethods(current);
                for (ExecutionContext cgn : invoked) {

                    if (!callGraph.containsVertex(cgn)) {
                        callGraph.addVertex(cgn);
                        todo.push(cgn);
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace(" - found invoke of " +cgn);
                    }
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

    public EdgeFactory<ExecutionContext,ContextEdge> getEdgeFactory() {
        return callGraph.getEdgeFactory();
    }

    public DirectedGraph<ExecutionContext,ContextEdge> getGraph() {
        return new UnmodifiableDirectedGraph<ExecutionContext, ContextEdge>(callGraph);
    }

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

    public boolean containsMethod(MethodInfo m) {
        return methodNodes.containsKey(m);
    }

    /**
     * Get all nodes matching the given method info.
     *
     * @see #getMethodNode(MethodInfo)
     * @param m the method to check.
     * @return a set of execution contexts of this method in the callgraph, or an empty set if this method has no nodes.
     */
    public Set<ExecutionContext> getNodes(MethodInfo m) {
        if (!methodNodes.containsKey(m)) {
            return Collections.emptySet();
        }
        return methodNodes.get(m).getInstances();
    }

    public Set<ExecutionContext> getNodes() {
        return callGraph.vertexSet();
    }

    /**
     * Get all matching nodes for a context in the graph.
     * @param context an execution context, not necessarily a node in the graph.
     * @return all nodes of the same method with matching callstrings.
     */
    public Set<ExecutionContext> getNodes(ExecutionContext context) {
        return getNodes(context.getCallString(), context.getMethodInfo());
    }

    public Set<ExecutionContext> getNodes(CallString cs, MethodInfo m) {
        Set<ExecutionContext> nodes = new LinkedHashSet<ExecutionContext>();
        for (ExecutionContext ec : methodNodes.get(m).getInstances()) {
            if (ec.getCallString().matches(cs)) {
                nodes.add(ec);
            }
        }
        return nodes;
    }

    /**
     * Get a MethodNode for a given methodInfo.
     * @param m the method to check.
     * @return the methodNode containing a set of all execution contexts of the method, or null if not found.
     */
    public MethodNode getMethodNode(MethodInfo m) {
        return methodNodes.get(m);
    }

    public Collection<ContextEdge> getOutgoingEdges(ExecutionContext node) {
        return callGraph.outgoingEdgesOf(node);
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
     * @param node the invoker
     * @return the keys are the invoke sites in the invoker, the values the set of successors of the invoker node
     *     than can be invoked by that invoke site. If the callgraph is context sensitive, the map might not contain
     *     all invoke sites for which there are no invokees.
     */
    public Map<InvokeSite, Set<ExecutionContext>> getChildsPerInvokeSite(ExecutionContext node) {
        Map<InvokeSite,Set<ExecutionContext>> map = new LinkedHashMap<InvokeSite, Set<ExecutionContext>>();

        List<ExecutionContext> emptyCSNodes = new LinkedList<ExecutionContext>();

        for (ContextEdge edge : callGraph.outgoingEdgesOf(node)) {
            long count;
            ExecutionContext child = edge.getTarget();

            if (!child.getCallString().isEmpty()) {
                // simple case: if we have a callstring, the top entry is the invokesite in the invoker

                Set<ExecutionContext> childs = map.get(child.getCallString().top());
                if (childs == null) {
                    childs = new LinkedHashSet<ExecutionContext>();
                    map.put(child.getCallString().top(), childs);
                }
                childs.add(child);

            } else {
                // tricky case: no callstring, we need to find all invokesites in the invoker
                emptyCSNodes.add(child);
            }
        }

        if (emptyCSNodes.isEmpty()) return map;

        for (InvokeSite invokeSite : node.getMethodInfo().getCode().getInvokeSites()) {

            Set<ExecutionContext> childs = map.get(invokeSite);
            if (childs == null) {
                childs = new LinkedHashSet<ExecutionContext>();
                map.put(invokeSite, childs);
            }

            for (ExecutionContext child : emptyCSNodes) {

                if (invokeSite.canInvoke(child.getMethodInfo()) != Ternary.FALSE) {
                    childs.add(child);
                }
            }
        }

        return map;
    }

    public List<ExecutionContext> getParents(ExecutionContext node) {
        Set<ContextEdge> in = callGraph.incomingEdgesOf(node);
        List<ExecutionContext> parents = new ArrayList<ExecutionContext>(in.size());
        for (ContextEdge e : in) {
            parents.add(e.getSource());
        }
        return parents;
    }

    public Set<ClassInfo> getRootClasses() {
        Set<ClassInfo> classes = new LinkedHashSet<ClassInfo>(2);
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
        Set<MethodInfo> methods = new LinkedHashSet<MethodInfo>();
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

    public Set<MethodInfo> getMethodInfos() {
        return methodNodes.keySet();
    }

    /*---------------------------------------------------------------------------*
     * Modify the graph
     *---------------------------------------------------------------------------*/

    public ContextEdge addEdge(ExecutionContext source, ExecutionContext target) {
        return callGraph.addEdge(source, target);
    }

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
        // Edge equals is defined over the adjacent execution contexts, so we can do this here
        // without worrying about same instances
        boolean exists = callGraph.removeEdge(edge);

        if (exists && removeUnreachable) {
            // do we need to worry about loosing connectivity?
            ExecutionContext target = edge.getTarget();
            Set<ExecutionContext> queue = new LinkedHashSet<ExecutionContext>();
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

    public boolean removeEdges(MethodInfo invoker, MethodInfo invokee, boolean removeUnreachable) {

        MethodNode node = methodNodes.get(invoker);
        if (node == null) return false;

        List<ContextEdge> remove = new LinkedList<ContextEdge>();
        for (ExecutionContext ec : node.getInstances()) {
            for (ContextEdge edge : callGraph.outgoingEdgesOf(ec)) {
                if (edge.getTarget().getMethodInfo().equals(invokee)) {
                    remove.add(edge);
                }
            }
        }

        for (ContextEdge edge : remove) {
            removeEdge(edge, removeUnreachable);
        }

        return !remove.isEmpty();
    }

    public boolean removeNodes(InvokeSite invokeSite, MethodInfo invokee, boolean removeUnreachable) {
        return removeNodes(new CallString(invokeSite), invokee, removeUnreachable);
    }

    /**
     * Remove all nodes from the graph which are invoked in a certain context, i.e. all execution contexts which
     * have the callstring as suffix. This does not remove contexts which have a context callstring shorter than the
     * callstring.
     *
     * @param callstring the callstring leading to the invokee
     * @param invokee the method for which contexts should be removed
     * @param removeUnreachable remove all newly unreachable methods from the callgraph too.
     * @return false if no nodes have been removed.
     */
    public boolean removeNodes(CallString callstring, MethodInfo invokee, boolean removeUnreachable) {
        // find all edges to remove
        MethodNode node = methodNodes.get(invokee);
        if (node == null) return false;

        // need to save nodes to remove into a temp list, because removing them from the graph would modify the instance list
        List<ExecutionContext> remove = new ArrayList<ExecutionContext>(node.getInstances().size());
        for (ExecutionContext ec : node.getInstances()) {
            if (ec.getCallString().hasSuffix(callstring)) {
                // if the node has the given callstring as suffix (i.e. is reached via the callstring), remove it
                remove.add(ec);
            }
        }

        for (ExecutionContext ec : remove) {
            removeNode(ec, removeUnreachable);
        }

        return !remove.isEmpty();
    }

    public boolean removeNode(ExecutionContext context, boolean removeUnreachable) {
        if (removeUnreachable) {
            // we only need to do this if we want to remove unreachable nodes, since removing a vertex
            // also removes its edges
            List<ContextEdge> remove = new ArrayList<ContextEdge>(callGraph.outgoingEdgesOf(context));
            for (ContextEdge e : remove) {
                removeEdge(e, removeUnreachable);
            }
        }
        // we do not need to worry about any onRemove methods, they are called by the listeners
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

    /**
     * Copy an existing node, and replace the callstring of the node with a new callstring.
     * All reachable nodes are also copied using the new callstring if they do not exist.
     *
     * @param source the node to copy
     * @param newContext the new callstring for the new node
     * @param callstringLength the maximum callstring length for new nodes.
     * @return the new node
     */
    public ExecutionContext copyNodeRecursive(ExecutionContext source, CallString newContext, int callstringLength) {

        ExecutionContext root = new ExecutionContext(source.getMethodInfo(), newContext);
        if (callGraph.containsVertex(root)) {
            return root;
        }

        callGraph.addVertex(root);

        List<ExecutionContext> newQueue = new LinkedList<ExecutionContext>();
        List<ExecutionContext> oldQueue = new LinkedList<ExecutionContext>();

        oldQueue.add(source);
        newQueue.add(root);

        while (!newQueue.isEmpty()) {
            ExecutionContext newNode = newQueue.remove(0);
            ExecutionContext oldNode = oldQueue.remove(0);

            // create a copy for every child of oldNode, construct a new callstring
            for (ExecutionContext oldChild : getChildren(oldNode)) {

                CallString newString;
                if (oldChild.getCallString().isEmpty()) {
                    newString = CallString.EMPTY;
                } else {
                    newString = newNode.getCallString().push(oldChild.getCallString().top(), callstringLength);
                }
                ExecutionContext newChild = new ExecutionContext(oldChild.getMethodInfo(), newString);

                if (!callGraph.containsVertex(newChild)) {
                    callGraph.addVertex(newChild);
                    oldQueue.add(oldChild);
                    newQueue.add(newChild);
                }
                callGraph.addEdge(newNode, newChild);
            }
        }

        return root;
    }

    /*
    public void merge(MethodInfo target, Set<CallString> remove, Map<CallString, InvokeSite> invokeMap) {
        int len = AppInfo.getSingleton().getCallstringLength();
        merge(target, remove, invokeMap, new DefaultCallgraphBuilder(len), len);
    }

    public void merge(MethodInfo target, Set<CallString> remove, Map<CallString, InvokeSite> invokeMap,
                      CallgraphBuilder builder)
    {
        merge(target, remove, invokeMap, builder, AppInfo.getSingleton().getCallstringLength());
    }

    public void merge(MethodInfo target, Set<CallString> remove, Map<CallString, InvokeSite> invokeMap,
                      CallgraphBuilder builder, int callstringLength)
    {
        for (ExecutionContext context : getNodes(target)) {
            merge(context, remove, invokeMap, builder, callstringLength);
        }
    }
    */

    /**
     * Merge nodes in the callgraph.
     * <p>
     * This assumes that callstrings never decrease in length along any path in this callgraph.
     * </p>
     *
     * TODO implement. For now we just make sure that all optimizations which modify the callgraph
     *      (permanently) do not rely on an updated callgraph and we rebuild the callgraph and all
     *      analysis-data after the optimization is complete.
     *
     * @param target the node to merge other nodes into
     * @param remove a set of callstrings from the target to the nodes to remove. First item in a
     *               callstring must be an invokeSite in the target method, last entry is the invokeSite
     *               of the node to remove.
     * @param invokeMap map merged invokes to new invokesites. The keys are callstrings from the target to
     *                  the invokesites to replace, the values the new invokesites to use instead. The
     *                  keyset must be a superset of callstrings to all invokesites directly reachable from
     *                  the 'remove' set.
     * @param builder the builder to use to check if an edge is still needed after merging if the node has
     *                callstring-length 0.
     * @param callstringLength the maximum callstring length for new nodes.
     */
    /*
    public void merge(ExecutionContext target, Set<CallString> remove, Map<CallString, InvokeSite> invokeMap,
                      CallgraphBuilder builder, int callstringLength)
    {
        // first we add the new edges so that nodes do not get disconnected to avoid unnecessary
        // graph updates

        // - for all nodes directly reachable from 'removed':
        //    - clone them, create a context with callstring of the target + the new invokesite (retrieved
        //       from invokeMap), add edges from the target to the new nodes
        //       use callstringLength param to limit length
        //    - for all reachable nodes from the cloned nodes:
        //       clone those nodes with new callstrings (callstring of the new node + invokesite of cloned node)
        //           recurse down until no new callstrings are created, limit length by callstringLength


        // Now we need to remove old edges and we remove all new roots which have not been added
        // as roots

        // for all outgoing edges of 'target' and nodes in 'remove' set, check:
        //    - if callstring length of edge-target = 0: we do not know if the target is still used
        //      (e.g if one invokesite with this invokee has been inlined and another invokesite with same
        //       invokee has not), so we need to use the builder to create all invokee nodes for 'target'
        //      and check if the edge-target is still in the created set. If so, keep the edge.
        //      Outgoing edges of 'target' and of nodes in the 'remove' set need to be handled differently..
        //    - else: check the invokesites of the targets along the path from 'target' to the edge-target
        //      if this path is contained in 'remove', if so we can remove the edge.
        //    - for all removed edges: check if edge-target is now has no ingoing edges, if so
        //      check if edge-target is not in the callgraph-roots, and if so remove the node and all
        //      edges, recurse down with this check. Subgraphs and merged-graph are automatically updated.


        // TODO we could also allow registering of callback handlers which will be notified
        //      when callstrings change due to merging. This could be used to update callstrings in
        //      the DFA results etc. so that we do not need to rerun the DFA after inlining
    }
    */

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
            addMergedGraphEdge(edge);
        }
    }

    public DirectedGraph<MethodNode,InvokeEdge> getMergedCallGraph(boolean reversed) {
        buildMergedGraph();
        return reversed ? new EdgeReversedGraph<MethodNode, InvokeEdge>(mergedCallGraph) : mergedCallGraph;
    }

    public SimpleDirectedGraph<MethodNode,InvokeEdge> getAcyclicMergedGraph(boolean reversed) {
        return GraphUtils.createAcyclicGraph(getMergedCallGraph(reversed));
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

        CallGraph subGraph = new CallGraph(this, roots, builder);
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

    public DirectedGraph<ExecutionContext, ContextEdge> getReversedGraph() {
        return new EdgeReversedGraph<ExecutionContext, ContextEdge>(callGraph);
    }

    public DirectedGraph<ExecutionContext, ContextEdge> getAcyclicGraph(boolean reversed) {
        DirectedGraph<ExecutionContext, ContextEdge> graph = reversed ? getReversedGraph() : getGraph();
        if (acyclic == Ternary.TRUE) {
            return new UnmodifiableDirectedGraph<ExecutionContext, ContextEdge>(graph);
        }
        return GraphUtils.createAcyclicGraph(graph);
    }

    public DirectedGraph<ExecutionContext, ContextEdge> createInvokeGraph(Collection<ExecutionContext> roots,
                                                                          boolean reversed)
    {
        // TODO this method is a hotspot, but what can you do? The TopologicalOrderIterator only works on
        //      on full graphs, it does not allow to "start in the middle, because it does not make
        //      much sense". One could implement an own TopOrderIterator, but finding out which nodes are
        //      the 'real' roots (i.e. eliminating all roots which have a path to other roots) may not be much
        //      faster than this code.
        // A better approach would simply be to 'compress' the callgraph once.

        DirectedGraph<ExecutionContext, ContextEdge> invokeGraph =
                new DefaultDirectedGraph<ExecutionContext,ContextEdge>(
                    new EdgeFactory<ExecutionContext,ContextEdge>() {
                        @Override
                        public ContextEdge createEdge(ExecutionContext sourceVertex, ExecutionContext targetVertex) {
                            return new ContextEdge(sourceVertex,targetVertex);
                        }
                    });

        LinkedList<ExecutionContext> queue = new LinkedList<ExecutionContext>(roots);
        for (ExecutionContext root : roots) {
            invokeGraph.addVertex(root);
        }

        // add all incoming edges to the new graph, add new nodes to the queue
        while (!queue.isEmpty()) {
            ExecutionContext node = queue.removeFirst();

            // add all incoming edges
            for (ContextEdge edge : callGraph.incomingEdgesOf(node)) {
                ExecutionContext source = edge.getSource();

                // new node, not yet in queue
                if (!invokeGraph.containsVertex(source)) {
                    invokeGraph.addVertex(source);
                    queue.add(source);
                }

                // add edge
                if (reversed) {
                    invokeGraph.addEdge(node, source);
                } else {
                    invokeGraph.addEdge(source, node);
                }
            }
        }

        return invokeGraph;
    }

    /*---------------------------------------------------------------------------*
     * Various lookup methods
     *---------------------------------------------------------------------------*/

    /**
     * @param invokee the invoked method
     * @return a set of all methods which may directly invoke this method
     */
    public Set<MethodInfo> getDirectInvokers(MethodInfo invokee) {
        MethodNode node = getMethodNode(invokee);
        Set<MethodInfo> invokers = new LinkedHashSet<MethodInfo>();

        for (ExecutionContext ec : node.getInstances()) {
            for (ContextEdge edge : callGraph.incomingEdgesOf(ec)) {
                invokers.add( edge.getSource().getMethodInfo() );
            }
        }

        return invokers;
    }

    public Set<InvokeSite> getInvokeSites(MethodInfo invokee) {
        MethodNode node = getMethodNode(invokee);
        Set<InvokeSite> invokeSites = new LinkedHashSet<InvokeSite>();

        for (ExecutionContext ec : node.getInstances()) {
            if (ec.getCallString().isEmpty()) {
                // no callstring, need to search the invoker 'manually' for invokesites which may invoke this method
                AppInfo appInfo = AppInfo.getSingleton();
                for (ContextEdge edge : callGraph.incomingEdgesOf(ec)) {
                    for (InvokeSite invokeSite : edge.getSource().getMethodInfo().getCode().getInvokeSites()) {
                        if (invokeSite.canInvoke(invokee) != Ternary.FALSE) {
                            invokeSites.add(invokeSite);
                        }
                    }
                }
            } else {
                invokeSites.add(ec.getCallString().top());
            }

        }

        return invokeSites;
    }

    public List<ExecutionContext> getInvokedNodes(ExecutionContext node, InvokeSite invokeSite, MethodInfo invokee) {
        List<ExecutionContext> invoked = new ArrayList<ExecutionContext>(1);
        for (ContextEdge edge : callGraph.outgoingEdgesOf(node)) {
            if (!edge.getTarget().getMethodInfo().equals(invokee)) {
                continue;
            }
            if (edge.getTarget().getCallString().isEmpty() ||
                edge.getTarget().getCallString().top().equals(invokeSite)) {
                invoked.add(edge.getTarget());
            }
        }

        return invoked;
    }

    public Set<MethodInfo> getInvokedMethods(MethodInfo method) {
        MethodNode node = getMethodNode(method);

        Set<MethodInfo> invokees = new LinkedHashSet<MethodInfo>();

        if (mergedCallGraph != null) {
            for (InvokeEdge edge : mergedCallGraph.outgoingEdgesOf(node)) {
                MethodNode invokee = mergedCallGraph.getEdgeTarget(edge);
                invokees.add(invokee.getMethodInfo());
            }
        } else {
            for (ExecutionContext context : node.getInstances()) {
                for (ContextEdge edge : callGraph.outgoingEdgesOf(context)) {
                    invokees.add(edge.getTarget().getMethodInfo());
                }
            }
        }

        return invokees;
    }

    public Collection<ContextEdge> getInvokeEdges(MethodInfo invoker, MethodInfo invokee) {
        MethodNode node = getMethodNode(invoker);
        List<ContextEdge> edges = new ArrayList<ContextEdge>();

        for (ExecutionContext ec : node.getInstances()) {
            for (ContextEdge edge : callGraph.outgoingEdgesOf(ec)) {
                if (edge.getTarget().getMethodInfo().equals(invokee)) {
                    edges.add(edge);
                }
            }
        }

        return edges;
    }

    public BackEdgeFinder<ExecutionContext,ContextEdge> getBackEdgeFinder() {
        return new BackEdgeFinder<ExecutionContext, ContextEdge>(callGraph);
    }

    /**
     * Return a top-down (topological) iterator for the callgraph
     * @return A topological order iterator
     */
    public TopologicalOrderIterator<ExecutionContext, ContextEdge> topDownIterator() {
        return new TopologicalOrderIterator<ExecutionContext, ContextEdge>(callGraph);
    }

    public TopologicalOrderIterator<ExecutionContext, ContextEdge> reverseTopologicalOrder() {
        return new TopologicalOrderIterator<ExecutionContext, ContextEdge>( getReversedGraph() );
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
        Set<MethodInfo> implemented = new LinkedHashSet<MethodInfo>();

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

        ExecutionContext cgNode = this.getNode(rootMethod, cs);

        return getReachableImplementations(cgNode);
    }

    public List<MethodInfo> getReachableImplementations(ExecutionContext cgNode) {
        final List<MethodInfo> implemented = new ArrayList<MethodInfo>();
        final Set<MethodInfo> visited = new LinkedHashSet<MethodInfo>();

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
        ExecutionContext cgNode = this.getNode(rootMethod, cs);

        return getReachableImplementationsSet(cgNode);
    }


    /**
     * Retrieve non-abstract methods reachable from the given call graph node.
     * All callgraph nodes reachable from nodes representing the given a method are collected
     * @param cgNode where to start
     * @return a list of all reachable implementations, sorted in DFS order
     */
    public Set<MethodInfo> getReachableImplementationsSet(ExecutionContext cgNode) {
        Set<MethodInfo> implemented = new LinkedHashSet<MethodInfo>();

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
     * Find all implementing methods for a given non-empty callstring.
     * This method is similar to {@link AppInfo#findImplementations(CallString)}, but is based
     * solemnly on this callgraph, even for non-virtual invokes.
     *
     * @see AppInfo#findImplementations(CallString)
     * @see #findImplementationContexts(CallString)
     * @param cs the non-empty callstring, top element represents the invocation.
     * @return a set of implementing methods of the invokee.
     */
    public Set<MethodInfo> findImplementations(CallString cs) {
        Collection<ExecutionContext> nodes = findImplementationContexts(cs);
        Set<MethodInfo> methods = new LinkedHashSet<MethodInfo>(nodes.size());
        for (ExecutionContext node : nodes) {
            methods.add(node.getMethodInfo());
        }
        return methods;
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
    public Set<ExecutionContext> findImplementationContexts(CallString cs) {
        if (cs.length() == 0) {
            throw new AssertionError("findImplementationContexts: callstring must not be empty!");
        }

        InvokeSite invoke = cs.top();
        Set<ExecutionContext> methods = new LinkedHashSet<ExecutionContext>();

        MethodRef invokeeRef = invoke.getInvokeeRef();

        // if the invoke is not virtual, should we look it up in the graph anyway?
        // We could just return new ExecutionContext(invokee, cs);
        // But that's what AppInfo#findImplementations() is for, here we only lookup the callgraph,
        // but then we cannot have different callgraphs and perform precise lookups on all of them,
        // since that method only looks into the default callgraph (but why would we want multiple
        // graphs anyway?)

        // find all instances of possible invokers
        Set<ExecutionContext> invoker = getNodes(invoke.getInvoker());
        for (ExecutionContext invokeNode : invoker) {

            // TODO filter out nodes which do not match the callstring, to speed up things a bit

            for (ContextEdge outEdge : callGraph.outgoingEdgesOf(invokeNode)) {
                CallString cgString = outEdge.getTarget().getCallString();

                // check if the target callstring matches the given callstring
                if (cgString.isEmpty()) {
                    // target has no callstring, must at least override the invoked method
                    if (invokeeRef.isInterfaceMethod() == Ternary.TRUE) {
                        // for interface methods we have a problem, because we only have the
                        // implementations in the call graph, not the receivers, we cannot
                        // check if the receiver implements the interface..
                        if (outEdge.getTarget().getMethodInfo().implementsMethod(invokeeRef)) {
                            methods.add(outEdge.getTarget());
                        }
                    } else {
                        if (outEdge.getTarget().getMethodInfo().overrides(invokeeRef, true)) {
                            methods.add(outEdge.getTarget());
                        }
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
    public Set<ExecutionContext> getReferencedMethods(MethodInfo m) {
        Set<ExecutionContext> nodes = getNodes(m);
        Set<ExecutionContext> succs = new LinkedHashSet<ExecutionContext>();
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
    public List<MethodInfo> getMaximalCallStack() {
        if(maxCallStackLeaf == null) calculateDepthAndHeight();
        ExecutionContext n = this.maxCallStackLeaf;
        List<MethodInfo> maxCallStack = new ArrayList<MethodInfo>();
        maxCallStack.add(n.getMethodInfo());
        while(maxCallstackDAG.containsKey(n)) {
            n = maxCallstackDAG.get(n);
            maxCallStack.add(n.getMethodInfo());
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
     * Export methods
     *---------------------------------------------------------------------------*/

    public void dumpCallgraph(Config config, String graphName, DUMPTYPE type, boolean skipNoim) {
        try {
            dumpCallgraph(config, graphName, null, null, type, skipNoim);
        } catch (IOException e) {
            logger.warn("Could not dump callgraph '"+graphName+"': "+e.getMessage(), e);
        }
    }

    /**
     * Dump this callgraph or a subgraph to a file and create a png image.
     * If you use this method, add {@link #CALLGRAPH_DIR} to the options.
     *
     * @param config the config containing options for InvokeDot and {@link #CALLGRAPH_DIR}
     * @param graphName the name of the graph, will be used to construct the file name.
     * @param suffix a suffix for the graph name, e.g. to distinguish between various subgraphs
     * @param roots The roots of the subgraph to dump. If null, dump the whole graph. If roots is empty, do nothing.
     * @param type dump the complete graph, dump only the merged graph, or dump both or nothing.
     * @param skipNoim if true, do not include methods which have a single edge to the JVM.noim method.
     * @throws IOException if exporting the file fails.
     */
    public void dumpCallgraph(Config config, String graphName, String suffix, Set<ExecutionContext> roots,
                               DUMPTYPE type, boolean skipNoim)
            throws IOException
    {
        if (type == DUMPTYPE.off) return;
        if (roots != null && roots.isEmpty()) return;

        File outDir;
        try {
            outDir = config.getOutDir(config.getDebugGroup(), CallGraph.CALLGRAPH_DIR);
        } catch (BadConfigurationException e) {
            throw new BadConfigurationError("Could not create output dir "+
                        config.getDebugGroup().getOption(CallGraph.CALLGRAPH_DIR), e);
        }

        CallGraph subGraph = roots == null ? this : getSubGraph(roots);

        if (type == CallGraph.DUMPTYPE.merged || type == CallGraph.DUMPTYPE.both) {
            dumpCallgraph(config, outDir, graphName, suffix, subGraph, true, skipNoim);
        }
        if (type == CallGraph.DUMPTYPE.full || type == CallGraph.DUMPTYPE.both) {
            dumpCallgraph(config, outDir, graphName, suffix, subGraph, false, skipNoim);
        }

        if (roots != null) {
            removeSubGraph(subGraph);
        }
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

    /*---------------------------------------------------------------------------*
     * Private methods
     *---------------------------------------------------------------------------*/

    private void dumpCallgraph(Config config, File outDir, String graphName, String type, CallGraph graph,
                               boolean merged, boolean skipNoim) throws IOException
    {
        String suffix = type != null ? type + "-" : "";
        suffix += (merged) ? "merged" : "full";

        File dotFile = new File(outDir, graphName+"-"+suffix+".dot");
        File pngFile = new File(outDir, graphName+"-"+suffix+".png");

        logger.info("Dumping "+suffix+" callgraph to "+dotFile);

        FileWriter writer = new FileWriter(dotFile);

        graph.exportDOT(writer, merged, false, skipNoim);

        writer.close();

        InvokeDot.invokeDot(config, dotFile, pngFile);
    }

    /**
     * calculate the depth of each node, the height of the subgraph
     * rooted at that node, and a maximum call-stack tree.
     */
    private void calculateDepthAndHeight() {
        if(this.maxDistanceToRoot != null) return; // caching

        if (acyclic != Ternary.TRUE) {
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

        this.maxDistanceToRoot = new LinkedHashMap<ExecutionContext,Integer>();
        this.maxCallStackLeaf = rootNode;
        this.maxCallstackDAG  = new LinkedHashMap<ExecutionContext,ExecutionContext>();
        this.subgraphHeight = new LinkedHashMap<ExecutionContext, Integer>();

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
        leafNodeCache = new LinkedHashMap<MethodInfo, Boolean>();
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

    private void addMergedGraphEdge(ContextEdge edge) {
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

    private void removeMergedGraphEdge(ContextEdge edge) {
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
