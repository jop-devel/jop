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
package com.jopdesign.libgraph.cfg.block;

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.ExceptionTable;
import com.jopdesign.libgraph.cfg.Features;
import com.jopdesign.libgraph.cfg.GraphException;
import com.jopdesign.libgraph.cfg.StackEmulator;
import com.jopdesign.libgraph.cfg.statements.ControlFlowStmt;
import com.jopdesign.libgraph.cfg.statements.Statement;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.struct.ConstantClass;
import com.jopdesign.libgraph.struct.PropertyContainer;
import com.jopdesign.libgraph.struct.TypeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Basic class for a basic block..
 * 
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class BasicBlock implements PropertyContainer {

    public class Edge {

        private BasicBlock target;
        private int targetNr;

        protected Edge() {
            target = null;
            targetNr = -1;
        }

        protected Edge(int targetNr) {
            this.targetNr = targetNr;
        }

        /**
         * Check if this edge goes to the nextblock of a block.
         * @return true, if this edge does not create a controlflow-change.
         */
        public boolean isNextBlockEdge() {
            return targetNr == -1;
        }

        /**
         * Check if this edge is still links two blocks together.
         * @return true, if this is a valid edge instance.
         */
        public boolean isValidEdge() {
            return targetNr != -2;
        }

        /**
         * Get the number of the target of the source block of this edge.
         * @return the number of the target, or -1 for the nextblock edge.
         */
        public int getTargetNr() {
            return targetNr;
        }

        public BasicBlock getSourceBlock() {
            return BasicBlock.this;
        }

        public BasicBlock getTargetBlock() {
            return target;
        }

        /**
         * Get the index of this egde in the ingoingedges list of the target block.
         * @return this edges index, or -1 if not linked.
         */
        public int getIngoingEdgeIndex() {
            if ( target == null ) {
                return -1;
            }
            return target.ingoingEdges.indexOf(this);
        }

        public BasicBlock splitEdge() {
            return null;
        }

        public void remove() {
            if ( targetNr == -1 ) {
                BasicBlock.this.unsetNextBlock();
            } else if ( targetNr != -2 ) {
                BasicBlock.this.clearTarget(targetNr);
            }
        }

        protected void invalidate() {
            unsetTarget();
            targetNr = -2;
        }

        protected void unsetTarget() {
            if ( target == null ) {
                return;
            }
            target.ingoingEdges.remove(this);
            target = null;
        }

        protected void setTarget(int pos, BasicBlock block) {
            unsetTarget();
            if ( block != null ) {
                target = block;
                target.ingoingEdges.add(pos, this);
            }
        }
    }

    public abstract static class ExceptionHandler {

        private ConstantClass exceptionClass;
        private Set handledBlocks;
        private BasicBlock exceptionBlock;

        protected ExceptionHandler(ConstantClass exception) {
            this.exceptionClass = exception;
            handledBlocks = new HashSet();
        }

        /**
         * Get the index of this handler in the exception table.
         * @return the position in the exception table.
         */
        public abstract int getHandlerIndex();

        public abstract ExceptionTable getExceptionTable();

        public void setExceptionClass(ConstantClass exceptionClass) {
            this.exceptionClass = exceptionClass;
        }

        /**
         * Get the class this exceptionhandler handles.
         * @return the classinfo of the handled exception, or null for a catchall handler.
         */
        public ConstantClass getExceptionClass() {
            return exceptionClass;
        }

        public void setExceptionBlock(BasicBlock block) {
            if ( exceptionBlock != null ) {
                exceptionBlock.handledExceptions.remove(this);
            }
            exceptionBlock = block;
            if ( exceptionBlock != null ) {
                exceptionBlock.handledExceptions.add(this);
            }
        }

        public BasicBlock getExceptionBlock() {
            return exceptionBlock;
        }

        /**
         * Get a set of all blocks which are handled by this exceptionhandler.
         * @return a set of BasicBlocks. Do not modify this set.
         */
        public Set getHandledBlocks() {
            return Collections.unmodifiableSet(handledBlocks);
        }

    }

    private StackCode stackCode;
    private QuadCode quadCode;
    private BasicBlock dominator;
    private Edge nextBlock;
    private List ingoingEdges;
    private List targets;
    private List exceptionHandlers;
    private Set handledExceptions;
    private Map props;

    protected BasicBlock(int type) {
        if ( type == ControlFlowGraph.TYPE_QUAD ) {
            quadCode = new QuadCode(this);
            stackCode = null;
        } else {
            stackCode = new StackCode(this);
            quadCode = null;
        }
        nextBlock = null;
        dominator = null;
        ingoingEdges = new ArrayList(2);
        targets = new ArrayList(1);
        exceptionHandlers = new ArrayList(1);
        handledExceptions = new HashSet(1);
        props = new HashMap(1);
    }

    public abstract ControlFlowGraph getGraph();

    /**
     * Get the number of this block in the graph's blocklist.
     * @return the number of this block.
     */
    public abstract int getBlockIndex();

    public BasicBlock getDominator() {
        return dominator;
    }

    public void setDominator(BasicBlock dominator) {
        this.dominator = dominator;
    }

    /**
     * Get the edge to next block, which will be taken if no
     * controlflow statement is set or its branches are not taken.
     * @return the edge to the default next block, or null if not set.
     */
    public Edge getNextBlockEdge() {
        return nextBlock;
    }

    /**
     * Set the next block which will be executed if the last statement does not change the control-flow.
     * @param block the next default block, or null to clear the next block.
     * @return the new edge, or null if the next block is cleared.
     */
    public Edge setNextBlock(BasicBlock block) {
        if ( block == null ) {
            unsetNextBlock();
            return null;
        }
        if ( nextBlock != null && nextBlock.getTargetBlock() == block ) {
            return nextBlock;
        }
        return setNextBlock(block.getIngoingEdges().size(), block);
    }

    public void unsetNextBlock() {
        if ( nextBlock != null ) {
            nextBlock.invalidate();
            nextBlock = null;
        }
    }

    public boolean hasNextBlock() {
        return nextBlock != null;
    }

    /**
     * Set the next block which will be executed if the last statement does not change the control-flow.
     * The new edge will be inserted into the targetblocks edgelist at a given position.
     *
     * @param index the new position in the targets edgelist of this edge, must be 0 to list.size().
     * @param block the new target block.
     * @return the new edge.
     */
    public Edge setNextBlock(int index, BasicBlock block) {
        if ( nextBlock == null ) {
            nextBlock = new Edge();
        }
        nextBlock.setTarget(index, block);
        return nextBlock;
    }

    /**
     * Get a list of all ingoing edges.
     * @return an unmodifiable list of all ingoing edges.
     */
    public List getIngoingEdges() {
        return Collections.unmodifiableList(ingoingEdges);
    }

    /**
     * Get the number of currently defined targets.
     *
     * @return the number of targets.
     */
    public int getTargetCount() {
        return targets.size();
    }

    /**
     * Get the edge for a given target index.
     * @param i the index of the target.
     * @return the edge to the target, or null if not set.
     */
    public Edge getTargetEdge(int i) {
        if ( i < 0 || i >= targets.size() ) {
            return null;
        }
        return (Edge) targets.get(i);
    }

    /**
     * Get a list of all Edges for all targets of the controlflow-statement
     * of this block.
     * @return a unmodifiable list of Edges, excluding the default edge to the next block.
     */
    public List getTargetEdges() {
        return Collections.unmodifiableList(targets);
    }

    public Edge setTarget(int i, BasicBlock target) {
        return setTarget(i, target != null ? target.ingoingEdges.size() : 0, target);
    }

    public Edge setTarget(int i, int inPos, BasicBlock target) {
        for ( int j = targets.size(); j <= i; j++ ) {
            targets.add(null);
        }

        Edge edge = (Edge) targets.get(i);
        if ( edge == null && target != null ) {
            edge = new Edge(i);
            targets.set(i, edge);
        } else if ( edge != null && target == null ) {
            edge.invalidate();
            targets.set(i, null);
            edge = null;
        }

        if ( edge != null ) {
            edge.setTarget(inPos, target);
        }

        return edge;
    }

    public Edge addTarget(BasicBlock target) {
        return addTarget(targets.size(), target != null ? target.ingoingEdges.size() : 0, target);
    }

    public Edge addTarget(int i, BasicBlock target) {
        return addTarget(i, target != null ? target.ingoingEdges.size() : 0, target);
    }

    public Edge addTarget(int i, int inPos, BasicBlock target) {
        Edge edge = new Edge(i);
        edge.setTarget(inPos, target);
        targets.add(i, edge);           

        // update targetnr of next edges
        for ( int j = i + 1; j < targets.size(); j++ ) {
            Edge e1 = (Edge) targets.get(j);
            if ( e1 != null ) {
                e1.targetNr = j;
            }
        }

        return edge;
    }

    /**
     * Create a new block and set as target.
     * 
     * @param i the new target number of the block, will be inserted
     * @return the edge to the new target
     */
    public Edge createTarget(int i) {
        BasicBlock block = getGraph().createBlock(getBlockIndex()+1);
        block.copyExceptionHandlersFrom(this);
        return addTarget(i, block);
    }

    public void removeTarget(int i) {
        if ( i >= targets.size() ) {
            return;
        }
        Edge edge = (Edge) targets.get(i);
        if ( edge != null ) {
            edge.invalidate();
        }
        targets.remove(i);

        // update targetnr of next edges
        for ( int j = i; j < targets.size(); j++ ) {
            Edge e1 = (Edge) targets.get(j);
            if ( e1 != null ) {
                e1.targetNr = j;
            }
        }
    }

    public void clearTarget(int i) {
        setTarget(i, null);
    }

    public void clearTargets() {
        for (Iterator it = targets.iterator(); it.hasNext();) {
            Edge edge = (Edge) it.next();
            if ( edge != null ) {
                edge.invalidate();
            }
        }
        targets.clear();
    }

    public boolean isExceptionHandlerBlock() {
        return handledExceptions.size() != 0;
    }

    /**
     * Get the exception handlers for which this block is an exception handler block.
     *
     * @return a set of exceptionhandlers. Do not modify this set.
     */
    public Set getHandledExceptions() {
        return handledExceptions;
    }

    /**
     * Get a list of all registered exception handlers for this block.
     * @return a list of exceptionHandler. Do not modify this list.
     */
    public List getExceptionHandlers() {
        return exceptionHandlers;
    }

    /**
     * Get the class info of the handled exception, or null if this handles all exceptions.
     *
     * @return the classinfo of the handled exception, or null for all exceptions.
     */
    public ConstantClass getHandledException() {

        // TODO find common subclass of all handled exception classes.
        
        if ( handledExceptions.size() == 1 ) {
            ExceptionHandler handler = (ExceptionHandler) handledExceptions.iterator().next();
            return handler.getExceptionClass();
        }

        return null;
    }

    public void addExceptionHandler(ExceptionHandler handler) {
        exceptionHandlers.add(handler);
        handler.handledBlocks.add(this);
    }

    public ExceptionHandler getExceptionHandler(int i) {
        return (ExceptionHandler) exceptionHandlers.get(i);
    }

    public void clearExceptionHandlers() {
        for (Iterator it = exceptionHandlers.iterator(); it.hasNext();) {
            ExceptionHandler handler = (ExceptionHandler) it.next();
            handler.handledBlocks.remove(this);
        }
        exceptionHandlers.clear();
    }

    public void copyExceptionHandlersFrom(BasicBlock block) {

        clearExceptionHandlers();

        for (Iterator it = block.getExceptionHandlers().iterator(); it.hasNext();) {
            ExceptionHandler handler = (ExceptionHandler) it.next();
            addExceptionHandler(handler);
        }
    }

    public int getJSRTargetCount() {
        // TODO implement JSR edges
        return 0;
    }

    public Object setProperty(Object key, Object value) {
        return props.put(key, value);
    }

    public Object getProperty(Object key) {
        return props.get(key);
    }

    public Object removeProperty(Object key) {
        return props.remove(key);
    }

    public boolean containsProperty(Object key) {
        return props.containsKey(key);
    }

    /**
     * get the controlflow-statement of this block, if any.
     * @return the last statement of the code if it is a ControlFlowStmt, else null.
     */
    public ControlFlowStmt getControlFlowStmt() {
        List code = getCodeBlock().getStatements();
        if ( code.size() > 0 ) {
            Statement stmt = (Statement) code.get(code.size() - 1);
            if ( stmt instanceof ControlFlowStmt ) {
                return (ControlFlowStmt) stmt;
            }
        }
        return null;
    }

    /**
     * Get the stackcode container for this block.
     * Returns null if graph-type is not TYPE_STACK.
     * @return the stackcode container or null if this contains quad-code.
     */
    public StackCode getStackCode() {
        return stackCode;
    }

    /**
     * Get the quadcode container for this block.
     * Returns null if the graph-type is not TYPE_QUAD.
     * @return the quadcode container or null if this contains stack-code.
     */
    public QuadCode getQuadCode() {
        return quadCode;
    }

    public CodeBlock getCodeBlock() {
        return stackCode != null ? (CodeBlock)stackCode : (CodeBlock)quadCode;
    }

    /**
     * Split this block, starting with instruction at index pos
     * and copy all exception handlers to new block.
     *
     * @param pos the index of the first instruction in the new block.
     * @return the new block.
     */
    public BasicBlock splitBlock(int pos) {

        BasicBlock next = getGraph().createBlock(getBlockIndex()+1);

        // relink targets
        if ( nextBlock != null ) {
            next.setNextBlock(nextBlock.getIngoingEdgeIndex(), nextBlock.getTargetBlock());
        }
        setNextBlock(next);
        for ( int i = 0; i < getTargetCount(); i++ ) {
            Edge target = getTargetEdge(i);
            if ( target != null ) {
                next.setTarget(i, target.getIngoingEdgeIndex(), target.getTargetBlock());
            }
        }
        clearTargets();

        // move code
        if ( stackCode != null ) {
            stackCode.moveStatements(pos, stackCode.size(), next.getStackCode(), 0);
            // TODO if feature is set, simulate stack, set stack
            getGraph().getFeatures().removeFeature(Features.FEATURE_STACK_INFO);
        }
        if ( quadCode != null ) {
            quadCode.moveStatements(pos, quadCode.size(), next.getQuadCode(), 0);
        }

        // copy exception handler
        next.copyExceptionHandlersFrom(this);

        return next;
    }

    protected void transformTo(int type) throws GraphException {
        if ( type == ControlFlowGraph.TYPE_QUAD && stackCode != null ) {
            quadCode = new QuadCode(this);

            StackEmulator emu = new StackEmulator();
            emu.init(stackCode.getStartStack());

            for (Iterator it = stackCode.getStatements().iterator(); it.hasNext();) {
                StackStatement stmt = (StackStatement) it.next();
                QuadStatement[] qs;
                try {
                    qs = stmt.getQuadCode(emu.getCurrentStack(), getGraph().getVariableTable());
                } catch (TypeException e) {
                    throw new GraphException("Could not create quad statements.",e);
                }
                for (int i = 0; i < qs.length; i++) {
                    qs[i].setLineNumber(stmt.getLineNumber());
                    quadCode.addStatement(qs[i]);
                }
                emu.processStmt(stmt);
            }

            stackCode = null;
        }
        if ( type == ControlFlowGraph.TYPE_STACK && quadCode != null ) {
            stackCode = new StackCode(this);

            for (Iterator it = quadCode.getStatements().iterator(); it.hasNext();) {
                QuadStatement stmt = (QuadStatement) it.next();
                StackStatement[] ss;
                try {
                    ss = stmt.getStackCode(getGraph().getVariableTable());
                } catch (TypeException e) {
                    throw new GraphException("Could not create stack statements.",e);
                }
                for (int i = 0; i < ss.length; i++) {
                    ss[i].setLineNumber(stmt.getLineNumber());
                    stackCode.addStatement(ss[i]);
                }
            }
            quadCode = null;
        }
    }

}
