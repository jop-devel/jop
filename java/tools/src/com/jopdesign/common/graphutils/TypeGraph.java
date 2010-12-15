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
package com.jopdesign.common.graphutils;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.IOException;
import java.io.Writer;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * TypeGraph based on JGraphT. Supports Interfaces.
 *
 * <p>Modifications to the class hierarchy in {@link AppInfo} are not propagated to
 * this graph.</p>
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class TypeGraph extends DefaultDirectedGraph<ClassInfo, DefaultEdge> {

    private static final long serialVersionUID = 1L;
    private ClassInfo rootNode;

    /**
     * Create and build a new type graph which mirrors the class hierarchy in {@link AppInfo}, using
     * {@code java.lang.Object} as root. Changes to the class hierarchy in {@link AppInfo} or to
     * {@link ClassInfo} are not propagated to this graph once it is created.
     */
    public TypeGraph() {
        super(DefaultEdge.class);
        AppInfo ai = AppInfo.getSingleton();
        build(ai, ai.getClassInfo("java.lang.Object"));
    }

    public ClassInfo getRootNode() {
        return rootNode;
    }

    private void build(AppInfo ai, ClassInfo rootNode) {
        this.rootNode = rootNode;
        Collection<ClassInfo> classes = ai.getClassInfos();

        for(ClassInfo ci : classes) {
            this.addVertex(ci);
            if(ci.getSuperClassInfo() != null) {
                if(ci.isInterface() && ! ci.getSuperClassInfo().isInterface()) {
                    // Interface extends java.lang.Object, ignored
                } else {
                    this.addVertex(ci.getSuperClassInfo());
                    this.addEdge(ci.getSuperClassInfo() ,ci);
                }
            }
            /* interface edges */
            for (String ifaceClass : ci.getInterfaceNames()) {
                ClassInfo iface = ai.getClassInfo(ifaceClass);
                assert iface != null : ("TypeGraph.build: Interface "+ifaceClass+" not found");
                this.addVertex(iface);
                this.addEdge(iface ,ci);
            }
        }
    }

    /**
     * Subtypes (including the type itself) of the type denoted by the given class info
     * @param base
     * @return
     */
    public List<ClassInfo> getSubtypes(ClassInfo base) {
        List<ClassInfo> subTypes = getStrictSubtypes(base);
        subTypes.add(base);
        return subTypes;
    }

    /**
     * Subtypes (not including the type itself) of the type denoted by the given class info
     * @param base
     * @return
     */
    public List<ClassInfo> getStrictSubtypes(ClassInfo base) {
        DepthFirstIterator<ClassInfo, DefaultEdge> iter = new DepthFirstIterator<ClassInfo, DefaultEdge>(this,base);
        iter.setCrossComponentTraversal(false);
        List<ClassInfo> subTypes = new LinkedList<ClassInfo>();
        iter.next();
        while(iter.hasNext()) {
            subTypes.add(iter.next());
        }
        return subTypes;
    }

    /**
     * Top down traversal, using TopologicalOrderIterator
     * @return a top-down traversal of the type graph
     */
    public List<ClassInfo> topDownTraversal() {
        List<ClassInfo> vertices = new LinkedList<ClassInfo>();
        TopologicalOrderIterator<ClassInfo, DefaultEdge> topoIter =
                new TopologicalOrderIterator<ClassInfo, DefaultEdge>(this);
        while(topoIter.hasNext()) {
            vertices.add(topoIter.next());
        }
        return vertices;
    }

    /**
     * Bottom up traversal, using TopologicalOrderIterator
     * @return a bottom-up traversal of the type graph
     */
    public List<ClassInfo> bottomUpTraversal() {
        List<ClassInfo> vertices = topDownTraversal();
        Collections.reverse(vertices);
        return vertices;
    }

    /**
     * Compute a the maximum level of each class
     * If the longest subtype chain
     * {@code Object <: T1 <: T2 <: ... <: T @} has length l+1,
     * T has a maximum level of l.
     * @return
     */
    public Map<ClassInfo,Integer> getLevels() {
        Map<ClassInfo, Integer> levels = new HashMap<ClassInfo, Integer>();
        for(ClassInfo v : topDownTraversal()) {
            int level = 0;
            for(DefaultEdge parentEdge : incomingEdgesOf(v))
            {
                level = Math.max(level, 1 + levels.get(getEdgeSource(parentEdge)));
            }
            levels.put(v,level);
        }
        return levels;
    }

    /**
     * Compute, for each type, the set of subtypes, using a single
     * bottom-up traversal.
     * @return
     */
    public Map<ClassInfo, Set<ClassInfo>> getSubtypeSets() {
        Map<ClassInfo, Set<ClassInfo>> subtypeMap = new HashMap<ClassInfo, Set<ClassInfo>>();
        for(ClassInfo v : bottomUpTraversal()) {
            Set<ClassInfo> subtypes = new HashSet<ClassInfo>();
            for(DefaultEdge kidEdge : outgoingEdgesOf(v))
            {
                ClassInfo subtype = getEdgeTarget(kidEdge);
                subtypes.addAll(subtypeMap.get(subtype));
                subtypes.add(subtype);
            }
            subtypeMap.put(v,subtypes);
        }
        return subtypeMap;
    }

    /**
     * Compute, for each type, the set of subtypes, using a single
     * top-down traversal
     * @return
     */
    public Map<ClassInfo, Set<ClassInfo>> getSupertypeSets() {
        Map<ClassInfo, Set<ClassInfo>> supertypeMap = new HashMap<ClassInfo, Set<ClassInfo>>();
        for(ClassInfo v : topDownTraversal()) {
            Set<ClassInfo> supertypes = new HashSet<ClassInfo>();
            for(DefaultEdge parentEdge : incomingEdgesOf(v))
            {
                ClassInfo supertype = getEdgeSource(parentEdge);
                supertypes.addAll(supertypeMap.get(supertype));
                supertypes.add(supertype);
            }
            supertypeMap.put(v,supertypes);
        }
        return supertypeMap;
    }

    /** Compute the interface conflict graph.<br/>
     *  Two distinct types A and B conflict if either {@code A <: B} or
     *  {@code B <: A}, or if A and B share a common descendant.
     *  Algorithmically, assume A conflicts with B.
     *  Then either<ul>
     *   <li/>A is in the <emph>subtype set</emph> of B or vice versa
     *   <li/>A and B are ancestors of some join node T.
     *  </ul>
     *   A join node is a type which is the root of a single inheritance hierarchy,
     *   but has more than one parent (think: the leaves of the multiple inheritance
     *   hierarchy).
     *  <p>
     *  For detailed information see
     *  <quote>Vitek, J., Horspool, R. N., and Krall, A. 1997. Efficient type inclusion tests.
     *             SIGPLAN Not. 32, 10 (Oct. 1997), 142-157.</quote>
     *  </p>
     *
     * @return
     */
    public SimpleGraph<ClassInfo, DefaultEdge> getInterfaceConflictGraph() {
        SimpleGraph<ClassInfo, DefaultEdge> conflicts = new SimpleGraph<ClassInfo, DefaultEdge>(DefaultEdge.class);
        Map<ClassInfo, Set<ClassInfo>> ancestors = getSupertypeSets();
        Map<ClassInfo, Set<ClassInfo>> subtypes = getSubtypeSets();
        for(ClassInfo v : topDownTraversal()) {
            if(v.isInterface()) conflicts.addVertex(v);
        }
        for(ClassInfo a : conflicts.vertexSet()) {
            for(ClassInfo b: subtypes.get(a)) {
                if(b.isInterface()) conflicts.addEdge(a,b);
            }
        }
        for(ClassInfo joinNode : getJoinNodes()) {
            for(ClassInfo a: ancestors.get(joinNode)) {
                if(! a.isInterface()) continue;
                if(joinNode.isInterface()) conflicts.addEdge(joinNode,a);
                for(ClassInfo b: ancestors.get(joinNode)) {
                    if(b.isInterface() && ! a.equals(b)) conflicts.addEdge(a,b);
                }
            }
        }
        return conflicts;
    }

    /** Compute the set of join nodes<br/>
     *
     *  See <quote>Vitek, J., Horspool, R. N., and Krall, A. 1997. Efficient type inclusion tests.
     *             SIGPLAN Not. 32, 10 (Oct. 1997), 142-157.</quote>
     * @return a map from types to join nodes
     */
    public Set<ClassInfo> getJoinNodes() {
        Set<ClassInfo> joinNodes = new HashSet<ClassInfo>();
        Set<ClassInfo> miNodes = new HashSet<ClassInfo>();
        for(ClassInfo v : bottomUpTraversal()) {
            boolean hasJoinSub = false;
            for(DefaultEdge descEdge : outgoingEdgesOf(v))
            {
                if(miNodes.contains(getEdgeTarget(descEdge))) {
                    hasJoinSub = true;
                }
            }
            boolean isMiNode = hasJoinSub;
            if(inDegreeOf(v) > 1 && ! hasJoinSub) {
                joinNodes.add(v);
                isMiNode = true;
            }
            if(isMiNode) {
                miNodes.add(v);
            }
        }
        return joinNodes;
    }

    /**
     * Assign relative numbering for single inheritance constant time typecheck
     * <ul>
     * <li/>A <: B or A = B iff B.low <= A.low <= B.high
     * <li/> {@code () ==> [l,l]}
     * <li/> {@code ( [l,h1], [h1+1, h2], ..., [h{n-1}+1,hn] ) ==> [l,hn]}
     * </ul>
     *
     * @return
     */
    public Map<ClassInfo,Pair<Integer,Integer>> getRelativeNumbering() {
        Map<ClassInfo, Pair<Integer, Integer>> rn = new HashMap<ClassInfo, Pair<Integer,Integer>>();
        assignRelativeNumbering(rootNode, rn, 0);
        return rn;
    }

    private int assignRelativeNumbering(ClassInfo node, Map<ClassInfo, Pair<Integer, Integer>> rn, int low) {
        int next = low;
        for(DefaultEdge subEdge : outgoingEdgesOf(node)) {
            ClassInfo sub = getEdgeTarget(subEdge);
            if(sub.isInterface()) continue;
            next = assignRelativeNumbering(sub,rn,next+1);
        }
        rn.put(node, new Pair<Integer,Integer>(low,next));
        return next;
    }

    /**
     * Bucket assignment for interfaces.
     * Two interfaces may not share a bucket if they have a common subtype.
     * Uses a simple highest-degree first graph coloring heuristic
     * @param maxBucketEntries
     * @return
     */
    public Map<ClassInfo,Integer> computeInterfaceBuckets(int maxBucketEntries) {
        Map<ClassInfo, Integer> bucketMap = new HashMap<ClassInfo, Integer>();

        SimpleGraph<ClassInfo, DefaultEdge> conflictGraph = getInterfaceConflictGraph();
        List<ClassInfo> ifaces = new LinkedList<ClassInfo>();

        for(ClassInfo v : vertexSet()) {
            if(! v.isInterface()) continue;
            ifaces.add(v);
        }

        Collections.sort(ifaces, new ReverseSubtypeCountComparator(getSubtypeSets()));

        BitSet fullBuckets = new BitSet();
        int[] count = new int[ifaces.size()];
        for(ClassInfo iface : ifaces) {
            BitSet used = (BitSet) fullBuckets.clone();
            for(DefaultEdge conflictEdge: conflictGraph.edgesOf(iface)) {
                ClassInfo conflictNode = conflictGraph.getEdgeSource(conflictEdge);
                // FIXME: ugly JGraphT idiom ?
                if(conflictNode == iface) conflictNode = conflictGraph.getEdgeTarget(conflictEdge);
                if(bucketMap.containsKey(conflictNode)) {
                    used.set(bucketMap.get(conflictNode));
                }
            }
            int color = used.nextClearBit(0);
            count[color]++;
            if(count[color] == maxBucketEntries) {
            	fullBuckets.set(color);
            }
            bucketMap.put(iface,color);
        }
        return bucketMap ;
    }

    private static class ReverseSubtypeCountComparator implements Comparator<ClassInfo> {
        private Map<ClassInfo,Set<ClassInfo>> subTypes;
        public ReverseSubtypeCountComparator(Map<ClassInfo, Set<ClassInfo>> subtypeSets) {
            this.subTypes = subtypeSets;
        }
        public int compare(ClassInfo o1, ClassInfo o2) {
            return new Integer(subTypes.get(o2).size()).compareTo(subTypes.get(o1).size());
        }
    }

    /**
     * Write the type graph in DOT format.
     * @param w
     * @param classFilter If non-null, only classes matching this prefix will be exported
     * @throws IOException
     */
    public void exportDOT(Writer w, String classFilter) throws IOException {
        Set<ClassInfo> subset = null;
        if(classFilter != null) {
            subset = new HashSet<ClassInfo>();
            subset.add(this.rootNode);
            for(ClassInfo ci : this.vertexSet()) {
                if(ci.getClassName().startsWith(classFilter)) {
                    while(ci.getSuperClassInfo() != null) {
                        subset.add(ci);
                        ci = ci.getSuperClassInfo();
                    }
                }
            }
        }
        Subgraph<ClassInfo, DefaultEdge, TypeGraph> subgraph =
            new Subgraph<ClassInfo, DefaultEdge, TypeGraph>(this,subset);
        AdvancedDOTExporter<ClassInfo, DefaultEdge> exporter = new AdvancedDOTExporter<ClassInfo, DefaultEdge>(
                new TgNodeLabeller(),
                null
        );
        exporter.exportDOTDiGraph(w, subgraph);
    }

    private class TgNodeLabeller extends AdvancedDOTExporter.DefaultNodeLabeller<ClassInfo> {
        @Override public String getLabel(ClassInfo ci) {
            return ci.getClassName();
        }

        @Override public boolean setAttributes(ClassInfo ci, Map<String, String> ht) {
            boolean labelled = super.setAttributes(ci, ht);
            if(ci.isInterface()) {
                ht.put("shape","ellipse");
            }
            return labelled;
        }
    }

    // --------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------
    // TESTING using faked ClassInfos
    // --------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------
    /*
    public static TypeGraph readTestTypeGraph(String file) throws IOException {
        TypeGraph testTypeGraph = new TypeGraph();
        Map<String,ClassInfo> classes = new HashMap<String, ClassInfo>();
        BufferedReader fr = new BufferedReader(new FileReader(new File(file)));
        testTypeGraph.rootNode = new FakeClassInfo("java.lang.Object",null,new String[0],false);
        classes.put("java.lang.Object",testTypeGraph.rootNode);
        String l;
        while(null != (l=fr.readLine())) {
            String[] typeInfo = l.split("\\s+");
            if(typeInfo.length <= 2) continue;
            boolean isIface = typeInfo[0].equals("I");
            String obj = typeInfo[1];
            String sup = typeInfo[2];
            String[] ifaces = Arrays.copyOfRange(typeInfo, 3, typeInfo.length);
            ClassInfo ci;
            if(isIface) {
            	ci = new FakeClassInfo(obj,sup,ifaces,true);
            } else {
                ci = new FakeClassInfo(obj,sup,ifaces,false);
            }
            classes.put(obj,ci);
        }
        // fix super ref
        for(ClassInfo ci : classes.values()) {
        	if(ci.clazz.getClassName().equals("java.lang.Object")) continue;
        	String sup = ci.clazz.getSuperclassName();
        	ci.superClass = classes.get(sup);
        }
        testTypeGraph.build(classes);
        return testTypeGraph;
    }
    private TypeGraph() {
        super(DefaultEdge.class);
    }
    private static class FakeClassInfo extends ClassInfo {
        private FakeClassInfo(String name, String sup, String[] ifaces, boolean isIFace) {
            super(null,null);
            initClazz(name, sup, ifaces, isIFace);
        }
        private void initClazz(String name, String sup, String[] ifaces, boolean isIFace) {
            ConstantPoolGen cgp = new ConstantPoolGen();
            int cthis = cgp.addClass(name);
            int csup;
            if(sup != null) csup  = cgp.addClass(sup);
            else csup = 0;
            int[] ifaceIds = new int[ifaces.length];
            for(int i = 0;i < ifaces.length; i++) {
            	ifaceIds[i] = cgp.addClass(ifaces[i]);
            }
            ConstantPool cp = cgp.getConstantPool();
            int flags = 0x0001 | 0x0020; // public
            if(isIFace) flags |= 0x0200;
            this.clazz = new JavaClass(cthis,csup,"test",49,0,flags,cp,ifaceIds,new Field[0],new Method[0], new Attribute[0]);
        }
    }
    */
}
