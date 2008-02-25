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
package com.jopdesign.libgraph.struct;

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.GraphException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * A wrapper for the method code itself. Provides functions to create and
 * compile a controlflow graph, as well as some statistics functions.                                                              
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class MethodCode {

    private MethodInfo methodInfo;
    private ControlFlowGraph graph;

    private static final Logger logger = Logger.getLogger(MethodCode.class);

    public MethodCode(MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
    }

    /**
     * Get the current controlflow graph for this method.
     * If the graph has been modified, the code for this method is not uptodate.
     *
     * @return the currently associated controlflowgraph.
     */
    public ControlFlowGraph getGraph() throws GraphException {
        if ( graph == null ) {
            graph = createGraph();
        }
        return graph;
    }

    /**
     * If any changes have been made to the graph, compile the graph and
     * create new code from it.
     */
    public void compileGraph() throws GraphException {
        if ( isGraphModified() ) {
            compileGraph(graph);
        }
    }

    /**
     * check if the graph has been modified and not yet compiled to new code.
     * @return true if the graph has been created and modified.
     */
    public boolean isGraphModified() {
        return graph != null && graph.isModified();
    }

    /**
     * Dismiss any changes made to the controlflow graph for this method.
     */
    public void resetGraph() {
        graph = null;
    }

    /**
     * Set a new graph to this method, can be compiled with compileGraph.
     * The new graph must have the same signature als this method.
     * @param graph the new graph.
     * @return true, if the graph has been set, false if the signature is wrong.
     */
    public boolean setGraph(ControlFlowGraph graph) {
        if ( !graph.getSignature().getSignature().equals(methodInfo.getSignature()) ) {
            return false;
        }
        this.graph = graph;
        return true;
    }

    public ConstantPoolInfo getConstantPoolInfo() {
        return methodInfo.getClassInfo().getConstantPoolInfo();
    }

    public AppStruct getAppStruct() {
        return methodInfo.getClassInfo().getAppStruct();
    }

    public MethodInfo getMethodInfo() {
        return methodInfo;
    }

    public abstract int getMaxStackSize();

    public abstract int getMaxLocals();

    /**
     * get code size in bytes.
     * @return current code size in bytes.
     */
    public abstract int getCodeSize();

    /**
     * get a list of all invoked methods of this method.
     * TODO return also instruction-nr, constantpool-nr?; also return invocations to anonymous classes?
     * @return a list of {@link MethodInvocation} classes for invoked methods.
     * @throws TypeException if referenced class is missing or if any type is used which cannot be loaded.
     */
    public abstract List getInvokedMethods() throws TypeException;

    /**
     * get a list of all referenced contants in this method.
     * @return a list of all indices of referenced constants in the constant pool of the class.
     */
    public abstract int[] getReferencedConstants();

    /**
     * Create a new graph from code. This graph is not linked to this method code.
     * @return a new graph created from the last loaded or compiled code.
     * @throws GraphException if graph creation fails.
     */
    public abstract ControlFlowGraph createGraph() throws GraphException;

    protected abstract void compileGraph(ControlFlowGraph graph) throws GraphException;

}
