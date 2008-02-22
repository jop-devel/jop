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
package com.jopdesign.libgraph.cfg;

import com.jopdesign.libgraph.cfg.block.BasicBlock;
import com.jopdesign.libgraph.cfg.statements.ControlFlowStmt;
import com.jopdesign.libgraph.cfg.statements.common.ReturnStmt;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.type.MethodSignature;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ControlFlowGraph {

    /**
     * A type for both graph types (stack, quadruple), needed for
     * some optimizer classes.
     */
    public static final int TYPE_BOTH = 0;

    /**
     * The constant for a stackcode-graph.
     */
    public static final int TYPE_STACK = 1;

    /**
     * The constant for a quadruplecode-graph.
     */
    public static final int TYPE_QUAD = 2;

    private class GraphBlock extends BasicBlock {

        protected GraphBlock() {
            super(ControlFlowGraph.this.getType());
        }

        public ControlFlowGraph getGraph() {
            return ControlFlowGraph.this;
        }

        public int getBlockIndex() {
            return ControlFlowGraph.this.blocks.indexOf(this);
        }

        protected void transformTo(int type) throws GraphException {
            super.transformTo(type);
        }
    }

    private boolean modified;
    private MethodSignature signature;

    private List blocks;
    private int graphType;
    private VariableTable variableTable;
    private ExceptionTable exceptionTable;
    private Features features;

    public ControlFlowGraph(MethodSignature signature) {
        this.signature = signature;
        blocks = new LinkedList();
        modified = false;
        graphType = TYPE_STACK;
        variableTable = new VariableTable(this);
        exceptionTable = new ExceptionTable(this);
        features = new Features();
    }
    
    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public MethodSignature getSignature() {
        return signature;
    }

    public VariableTable getVariableTable() {
        return variableTable;
    }

    public ExceptionTable getExceptionTable() {
        return exceptionTable;
    }

    public Features getFeatures() {
        return features;
    }

    /**
     * Get a list of all blocks. This list should not be modified.
     * @return an unmodifiable list of all blocks of this graph.
     */
    public List getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public BasicBlock getBlock(int i) {
        return (BasicBlock) blocks.get(i);
    }

    public int getBlockCount() {
        return blocks.size();
    }

    /**
     * Create a new basicblock.
     * @param pos the new position of the block in the list of blocks.
     * @return a new basicblock.
     */
    public BasicBlock createBlock(int pos) {

        BasicBlock block = new GraphBlock();

        blocks.add(pos, block);

        return block;
    }

    /**
     * Get a set of all blocks containing return-stmts.
     * @return a set of BasicBlocks.
     */
    public Set findReturnBlocks() {

        Set returnBlocks = new HashSet(1);

        for (Iterator it = blocks.iterator(); it.hasNext();) {
            BasicBlock block = (BasicBlock) it.next();
            ControlFlowStmt stmt = block.getControlFlowStmt();
            if ( stmt instanceof ReturnStmt) {
                returnBlocks.add(stmt);
            }
        }
        
        return returnBlocks;
    }

    /**
     * Get the current code type of this graph.
     * @return one of the TYPE_ constants.
     */
    public int getType() {
        return graphType;
    }

    /**
     * transform the code to a new form.
     * @param type the new form, one of {@link #TYPE_STACK} or {@link #TYPE_QUAD}.
     * @return true if the graph has been transformed, false if the graph was already in the requested form.
     * @throws GraphException if conversion of statements fails.
     */
    public boolean transformTo(int type) throws GraphException {
        if ( type == graphType || type == TYPE_BOTH ) {
            return false;
        }

        for (Iterator it = blocks.iterator(); it.hasNext();) {
            GraphBlock block = (GraphBlock) it.next();
                block.transformTo(type);
        }

        // TODO any feature which survives transformation? (getter from Feature)
        features.clearFeatures();

        graphType = type;

        return true;
    }

    /**
     * Get the size of the bytecode of the current graph.
     * This only works if the current graph type is {@link #TYPE_STACK}.
     *
     * @return the size of the corresponding bytecode in bytes or 0 if the graph is not in stack-form.
     */
    public int getBytecodeSize() {
        if ( graphType != TYPE_STACK ) {
            return 0;
        }

        int size = 0;
        for (Iterator it = blocks.iterator(); it.hasNext();) {
            BasicBlock block = (BasicBlock) it.next();
            size += block.getStackCode().getBytecodeSize();
        }

        return size;
    }

}
