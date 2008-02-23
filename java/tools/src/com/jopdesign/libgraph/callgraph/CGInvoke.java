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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class CGInvoke implements PropertyContainer {
    
    private ClassInfo invokedClass;
    private MethodInfo invokedMethod;
    private int count;
    private List methods;
    private Map props;

    protected CGInvoke(ClassInfo invokedClass, MethodInfo invokedMethod) {
        this.invokedClass = invokedClass;
        this.invokedMethod = invokedMethod;
        this.methods = new LinkedList();
        count = 1;
        props = null;
    }

    public abstract CGMethod getMethod();

    public ClassInfo getInvokedClass() {
        return invokedClass;
    }

    public MethodInfo getInvokedMethod() {
        return invokedMethod;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void incCount() {
        count++;
    }

    /**
     * Get a list of edges to the possible invoked methods.
     * @return a collection of {@link CGEdge}.
     */
    public List getEdges() {
        return Collections.unmodifiableList(methods);
    }

    public CGEdge getEdge(int i) {
        return (CGEdge) methods.get(i);
    }

    public CGEdge getEdge(CGMethod method) {
        for (Iterator it = methods.iterator(); it.hasNext();) {
            CGEdge edge = (CGEdge) it.next();
            if ( edge.getInvokedMethod().equals(method) ) {
                return edge;
            }
        }
        return null;
    }

    public int getEdgeCount() {
        return methods.size();
    }

    public CGEdge addInvokedMethod(CGMethod method) {
        CGEdge edge = createEdge(method);
        methods.add(edge);
        return edge;
    }

    public boolean removeInvokedMethod(CGMethod method) {
        CGEdge edge = getEdge(method);
        if ( edge == null ) {
            return false;
        }
        removeEdge(edge);
        return false;
    }

    public boolean removeEdge(CGEdge edge) {
        if ( methods.remove(edge) ) {
            unlinkEdge(edge);
            return true;
        }
        return false;
    }

    public void clearEdges() {
        for (Iterator it = methods.iterator(); it.hasNext();) {
            CGEdge edge = (CGEdge) it.next();
            unlinkEdge(edge);
        }
        methods.clear();
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

    protected abstract CGEdge createEdge(CGMethod method);

    protected abstract void unlinkEdge(CGEdge edge);

}
