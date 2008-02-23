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

import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.PropertyContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class CGMethod implements PropertyContainer {

    private class Invoke extends CGInvoke {

        private class Edge extends CGEdge {

            public Edge(CGMethod invokedMethod) {
                super(invokedMethod);
            }

            public CGInvoke getInvoke() {
                return Invoke.this;
            }
        }

        protected Invoke(ClassInfo invokedClass, MethodInfo invokedMethod) {
            super(invokedClass, invokedMethod);
        }

        public CGMethod getMethod() {
            return CGMethod.this;
        }

        protected CGEdge createEdge(CGMethod method) {
            Edge edge = new Edge(method);
            method.invoker.add(edge);
            return edge;
        }

        protected void unlinkEdge(CGEdge edge) {
            edge.getInvokedMethod().invoker.remove(edge);
        }
    }

    private MethodInfo methodInfo;
    private Map invokes;
    private List invokeList;
    private Set invoker;
    private Map props;

    protected CGMethod(MethodInfo method) {
        this.methodInfo = method;
        this.invokes = new HashMap();
        this.invokeList = new ArrayList();
        this.invoker = new HashSet();
        props = null;
    }

    public abstract CallGraph getGraph();

    public abstract int getIndex();
    
    public MethodInfo getMethodInfo() {
        return methodInfo;
    }

    /**
     * Get a list of all (registered) invocations of this method.
     * @return an unmodifiable list of {@link CGInvoke}.
     */
    public List getInvokes() {
        return Collections.unmodifiableList(invokeList);
    }

    /**
     * Get the invocation container for an invocation of a given (virtual) method.
     * @param invokedClass the class containing the invoked method
     * @param invokedMethod the invoked method
     * @return the invoke container or null if not yet set.
     */
    public CGInvoke getInvoke(ClassInfo invokedClass, MethodInfo invokedMethod) {
        return getInvoke(invokedClass, invokedMethod, false);
    }

    /**
     * Get the invocation container for an invocation of a given (virtual) method.
     * @param invokedClass the class containing the invoked method
     * @param invokedMethod the invoked method
     * @param create if true, and the container is not yet created, create a new one.
     * @return the invoke container, or null if not set and not created.
     */
    public CGInvoke getInvoke(ClassInfo invokedClass, MethodInfo invokedMethod, boolean create) {
        Object key = getKey(invokedClass, invokedMethod);
        CGInvoke invoke = (CGInvoke) invokes.get(key);

        if ( invoke == null && create ) {
            invoke = new Invoke(invokedClass, invokedMethod);
            invokes.put(key, invoke);
            invokeList.add(invoke);
        }
        return invoke;
    }

    public CGInvoke getInvoke(int i) {
        return (CGInvoke) invokeList.get(i);
    }

    public CGInvoke addInvoke(ClassInfo invokedClass, MethodInfo invokedMethod) {
        Object key = getKey(invokedClass, invokedMethod);
        CGInvoke invoke = (CGInvoke) invokes.get(key);

        if ( invoke == null ) {
            invoke = new Invoke(invokedClass, invokedMethod);
            invokes.put(key, invoke);
            invokeList.add(invoke);
        } else {
            invoke.incCount();
        }
        return invoke;
    }

    public boolean removeInvoke(CGInvoke invoke) {
        if (invoke == null) {
            return false;
        }
        CGInvoke i = (CGInvoke) invokes.remove(getKey(invoke));
        return i != null && invokeList.remove(i);
    }

    public void clearInvokes() {
        for (Iterator it = invokes.values().iterator(); it.hasNext();) {
            CGInvoke invoke = (CGInvoke) it.next();
            invoke.clearEdges();
        }
        invokes.clear();
        invokeList.clear();
    }

    public void clearInvokers() {
        for (Iterator it = invoker.iterator(); it.hasNext();) {
            CGEdge edge = (CGEdge) it.next();
            edge.remove();
        }
    }

    /**
     * Get a collection of all invoke edges which refer to this method.
     * @return a collection of {@link CGEdge}.
     */
    public Collection getInvokers() {
        return Collections.unmodifiableCollection(invoker);
    }

    /**
     * Get number of invokes of this method.
     * @param distinct if only the number of distinct invoker methods should be returned.
     * @return the number of invokes of this method.
     */
    public int getInvokeCount(boolean distinct) {
        if ( distinct ) {
            return invoker.size();
        }

        int sum = 0;
        for (Iterator it = invoker.iterator(); it.hasNext();) {
            CGInvoke invoke = ((CGEdge) it.next()).getInvoke();
            sum += invoke.getCount();
        }
        return sum;
    }

    public Object setProperty(Object key, Object value) {
        if ( props == null ) {
            props = new HashMap();
        }
        return props.put(key, value);
    }

    public Object getProperty(Object key) {
        if ( props == null ) {
            return null;
        }
        return props.get(key);
    }

    public Object removeProperty(Object key) {
        if ( props == null ) {
            return null;
        }
        return props.remove(key);
    }

    public boolean containsProperty(Object key) {
        return props != null && props.containsKey(key);
    }

    private Object getKey(CGInvoke invoke) {
        return getKey(invoke.getInvokedClass(), invoke.getInvokedMethod());
    }

    private Object getKey(ClassInfo classInfo, MethodInfo methodInfo) {
        return classInfo.getClassName() + "#" + methodInfo.getName() + methodInfo.getSignature();
    }

    public boolean equals(Object obj) {
        return obj instanceof CGMethod && methodInfo.isSameMethod( ((CGMethod) obj).getMethodInfo() );
    }

    public String toString() {
        return methodInfo.getFQMethodName();
    }
}
