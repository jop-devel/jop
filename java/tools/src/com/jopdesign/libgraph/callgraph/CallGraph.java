/*
 * Copyright (c) 2007,2008, Stefan Hepp
 *
 * This file is part of JOPtimizer.
 *
 * JOPtimizer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JOPtimizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.libgraph.callgraph;

import com.jopdesign.libgraph.struct.AppStruct;
import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.MethodInvocation;
import com.jopdesign.libgraph.struct.TypeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class CallGraph {

    private class Method extends CGMethod {

        private int index;

        protected Method(MethodInfo method) {
            super(method);
            index = -1;
        }

        public CallGraph getGraph() {
            return CallGraph.this;
        }

        public int getIndex() {
            return index;
        }
    }

    private AppStruct appStruct;
    private List rootMethods;
    private Map methods;
    private List methodList;

    private boolean sorted;
    private boolean keepSorted;

    public CallGraph(AppStruct appStruct) {
        this.appStruct = appStruct;
        this.rootMethods = new LinkedList();
        this.methods = new HashMap();
        this.methodList = new ArrayList();
        sorted = false;
        keepSorted = false;
    }

    public AppStruct getAppStruct() {
        return appStruct;
    }

    public boolean isSorted() {
        return sorted;
    }

    public boolean doKeepSorted() {
        return keepSorted;
    }

    public void setKeepSorted(boolean keepSorted) {
        this.keepSorted = keepSorted;
    }

    public List getMethods() {
        return Collections.unmodifiableList(methodList);
    }

    public CGMethod getMethod(MethodInfo method) {
        return getMethod(method, false);
    }

    public CGMethod getMethod(MethodInfo method, boolean create) {
        Object key = getKey(method);
        CGMethod newMethod = (CGMethod) methods.get(key);
        if ( newMethod == null && create ) {
            newMethod = new Method(method);
            addMethod(newMethod, false);
        }
        return newMethod;
    }

    public CGMethod getMethod(int index) {
        return (CGMethod) methodList.get(index);
    }

    public CGMethod addMethod(MethodInfo methodInfo) {
        CGMethod newMethod = new Method(methodInfo);
        addMethod(newMethod, false);
        return newMethod;
    }

    public void removeMethod(CGMethod method) {
        Object key = getKey(method);
        method = (CGMethod) methods.remove(key);
        if ( method != null ) {
            method.clearInvokes();
            method.clearInvokers();

            int idx = method.getIndex();
            if (idx == -1 ) {
                idx = methodList.indexOf(method);
            }
            if ( idx > -1 ) {
                methodList.remove(idx);
                for (int i = idx; i < methodList.size(); i++) {
                    Method m = (Method) methodList.get(i);
                    m.index = i;
                }
            }
        }
    }

    public void addRoot(ClassInfo classInfo) {
        for (Iterator it = classInfo.getMethodInfos().iterator(); it.hasNext();) {
            MethodInfo methodInfo = (MethodInfo) it.next();
            addRoot(methodInfo);
        }
    }

    public CGMethod addRoot(MethodInfo methodInfo) {
        CGMethod method = getMethod(methodInfo, true);
        rootMethods.add(method);
        return method;
    }

    public int getMethodCount() {
        return methodList.size();
    }

    public void buildGraph() throws TypeException {
        methods.clear();
        methodList.clear();
        for (Iterator it = rootMethods.iterator(); it.hasNext();) {
            CGMethod root = (CGMethod) it.next();
            addMethod(root, true);
        }

        for (Iterator it = rootMethods.iterator(); it.hasNext();) {
            CGMethod root = (CGMethod) it.next();
            buildCallGraph(root);
        }
    }

    /**
     * Check if the graph is connected, find and mark recursive invokes, and sort the methods topologically.
     * If not all methods can be reached by the root methods, sorting the graph fails.
     * <p>
     * The current implementation uses a DFS search to accomplish this.
     * </p>
     * NOTICE somehow return the unvisited methods, or visit unconnected subgraphs too
     * NOTICE maybe use Shortest Path First or Breadth First Search to get smaller invoke depths in graphs with recursions.
     *
     * @return true, if all methods have been visited and are now sorted.
     */
    public boolean sortMethods() {

        // TODO implement and use CallGraph-View and generic DFS
        DFSSort sort = new DFSSort(methodList);

        sorted = sort.sort(rootMethods);

        if ( sorted ) {
            CGMethod[] methods = sort.getSortedMethods();
            methodList = new ArrayList(methods.length);

            for (int i = 0; i < methods.length; i++) {
                Method method = (Method) methods[i];
                method.index = i;
                methodList.add(method);
            }
        }

        return sorted;
    }

    /**
     * Get a collection of the root methods of this graph.
     * @return a collection of {@link CGMethod}.
     */
    public List getRootMethods() {
        return Collections.unmodifiableList(rootMethods);
    }

    private void addMethod(CGMethod method, boolean isRoot) {

        methods.put(getKey(method), method);

        ((Method)method).index = methodList.size();
        methodList.add(method);

        // TODO honor keepSorted
        sorted = false;
    }

    private void buildCallGraph(CGMethod root) throws TypeException {

        if ( root.getMethodInfo().isAbstract() ) {
            return;
        }

        // list of non-abstract methods only
        List queue = new LinkedList();
        queue.add(root);

        while ( !queue.isEmpty() ) {
            CGMethod method = (CGMethod) queue.remove(0);

            List invokes = method.getMethodInfo().getMethodCode().getInvokedMethods();
            for (Iterator it = invokes.iterator(); it.hasNext();) {
                MethodInvocation invoke = (MethodInvocation) it.next();

                // check if a similar invocation has already been visited in this method
                CGInvoke iv = method.getInvoke(invoke.getInvokedClass(), invoke.getInvokedMethod());
                if ( iv == null ) {
                    iv = method.addInvoke(invoke.getInvokedClass(), invoke.getInvokedMethod());

                    // now, find all implementing methods, add non-abstract methods to graph and queue
                    Set impls = invoke.findImplementations();
                    for (Iterator it2 = impls.iterator(); it2.hasNext();) {
                        MethodInfo methodInfo = (MethodInfo) it2.next();
                        if ( methodInfo.isAbstract() ) {
                            continue;
                        }

                        CGMethod invoked = getMethod(methodInfo, false);
                        if ( invoked == null ) {
                            invoked = addMethod(methodInfo);
                            queue.add(invoked);
                        } else {
                            // NOTICE check for recursive invoke here (invoke-stacks)?
                        }
                        iv.addInvokedMethod(invoked);
                    }

                } else {
                    iv.incCount();
                }

            }

        }
    }

    private Object getKey(CGMethod method) {
        return getKey(method.getMethodInfo());
    }

    private Object getKey(MethodInfo method) {
        return method.getFQMethodName();
    }
}

class DFSSort {

    private int cnt;
    private CGMethod[] sortedMethods;
    private List methods;
    private int[] order;


    DFSSort(List methods) {
        sortedMethods = new CGMethod[methods.size()];
        order = new int[methods.size()];
        this.methods = methods;
    }

    private void visit(CGMethod method) {
        // set current method as 'work in progress'
        int index = method.getIndex();
        order[index] = -2;

        // visit all invoked methods
        int invokes = method.getInvokes().size();
        for (int i = 0; i < invokes; i++) {
            CGInvoke invoke = method.getInvoke(i);
            for (int j = 0; j < invoke.getEdgeCount(); j++) {
                CGEdge edge = invoke.getEdge(j);
                int k = order[edge.getInvokedMethod().getIndex()];
                if ( k == -1 ) {
                    visit(edge.getInvokedMethod());
                } else if ( k == -2 ) {
                    edge.setRecursion(true);
                }
            }
        }

        order[index] = --cnt;
        sortedMethods[cnt] = method;
    }

    public boolean sort(List rootMethods) {

        cnt = methods.size();
        for (int i = 0; i < cnt; i++) {
            order[i] = -1;
        }

        // NOTICE to find all methods, simply iterate over all methods
        for (Iterator it = rootMethods.iterator(); it.hasNext();) {
            CGMethod method = (CGMethod) it.next();
            if ( order[method.getIndex()] == -1 ) {
                visit(method);
            }
        }

        return cnt == 0;
    }

    public CGMethod[] getSortedMethods() {
        return sortedMethods;
    }

}
