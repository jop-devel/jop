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
import com.jopdesign.libgraph.cfg.block.QuadCode;
import com.jopdesign.libgraph.cfg.block.StackCode;
import com.jopdesign.libgraph.cfg.statements.AssignStmt;
import com.jopdesign.libgraph.cfg.statements.Statement;
import com.jopdesign.libgraph.cfg.statements.VariableStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableMapper;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BlockCloner {

    public static interface BlockMap {

        int count();

        int getTargetBlockIndex(int nr);

        int getSourceBlockIndex(int nr);

        int mapSourceBlock(int sourceIndex);
    }

    private class ListBlockMap implements BlockMap {

        private int firstNewPos;
        private int firstPos;
        private int lastPos;

        public ListBlockMap(int firstNewPos, int firstPos, int lastPos) {
            this.firstNewPos = firstNewPos;
            this.firstPos = firstPos;
            this.lastPos = lastPos;
        }

        public int count() {
            return lastPos - firstPos;
        }

        public int getTargetBlockIndex(int nr) {
            return firstNewPos + nr;
        }

        public int getSourceBlockIndex(int nr) {
            return firstPos + nr;
        }

        public int mapSourceBlock(int sourceIndex) {
            if ( sourceIndex < firstPos || sourceIndex >= lastPos ) {
                return -1;
            }
            return firstNewPos + sourceIndex - firstPos;
        }

    }

    private ControlFlowGraph graph;
    private VariableMapper mapper;

    private static final Logger logger = Logger.getLogger(BlockCloner.class);

    public BlockCloner(ControlFlowGraph graph, VariableMapper mapper) {
        this.graph = graph;
        this.mapper = mapper;
    }

    public ControlFlowGraph getGraph() {
        return graph;
    }

    public VariableMapper getMapper() {
        return mapper;
    }

    public void setMapper(VariableMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Copy all blocks from the source graph to the target graph.
     * @param toGraph the graph to copy the blocks to.
     * @param firstNewPos the index of the first new block in the target graph.
     * @return the number of new blocks.
     */
    public int copyBlocks(ControlFlowGraph toGraph, int firstNewPos) throws GraphException {
        return copyBlocks(toGraph, firstNewPos, 0, graph.getBlocks().size());
    }

    /**
     * Copy blocks into another graph.
     * @param toGraph the graph to copy the blocks to.
     * @param firstNewPos the index of the first new block in the target graph.
     * @param firstBlock the index of the first block to copy in the source graph.
     * @param lastBlock the index of the next block of the last block to copy in the source graph.
     * @return the number of new blocks.
     */
    public int copyBlocks(ControlFlowGraph toGraph, int firstNewPos, int firstBlock, int lastBlock)
            throws GraphException
    {
        return copyBlocks(toGraph, new ListBlockMap(firstNewPos, firstBlock, lastBlock));
    }

    public int copyBlocks(ControlFlowGraph toGraph, BlockMap map) throws GraphException {

        // create new blocks
        try {
            for ( int i = 0; i < map.count(); i++ ) {
                BasicBlock newBlock = toGraph.createBlock(map.getTargetBlockIndex(i));

                if ( graph.getType() == ControlFlowGraph.TYPE_STACK ) {
                    copyStack(toGraph.getVariableTable(), newBlock.getStackCode(),
                            graph.getBlock(map.getSourceBlockIndex(i)).getStackCode());
                } else {
                    copyQuad(toGraph.getVariableTable(), newBlock.getQuadCode(),
                            graph.getBlock(map.getSourceBlockIndex(i)).getQuadCode());
                }
            }
        } catch (CloneNotSupportedException e) {
            throw new GraphException("Could not clone blocks.", e);
        }

        // link blocks
        for ( int i = 0; i < map.count(); i++ ) {
            copyBlockLinks(toGraph, toGraph.getBlock(map.getTargetBlockIndex(i)),
                    graph.getBlock(map.getSourceBlockIndex(i)), map);
        }

        return map.count();
    }

    private void copyBlockLinks(ControlFlowGraph toGraph, BasicBlock block, BasicBlock oldBlock, BlockMap map) {

        BasicBlock.Edge target;
        int newTarget;

        // copy default target
        target = oldBlock.getNextBlockEdge();
        if ( target != null ) {
            newTarget = map.mapSourceBlock(target.getTargetBlock().getBlockIndex());
            if ( newTarget != -1 ) {
                block.setNextBlock(toGraph.getBlock(newTarget));
            } else {
                logger.warn("Default target of new block not copied, ignored.");
            }
        }

        // copy targets
        for ( int i = 0; i < oldBlock.getTargetCount(); i++ ) {
            target = oldBlock.getTargetEdge(i);
            if ( target != null ) {
                newTarget = map.mapSourceBlock(target.getTargetBlock().getBlockIndex());
                if ( newTarget != -1 ) {
                    block.setTarget(i, toGraph.getBlock(newTarget));
                } else {
                    logger.warn("Target block for target {"+i+"} of new block not copied, ignored.");
                }
            }
        }

        // copy exceptions, exceptionhandler

        // TODO implement
        
    }

    private void copyQuad(VariableTable toTable, QuadCode newCode, QuadCode oldCode)
            throws CloneNotSupportedException
    {

        for (Iterator it = oldCode.getStatements().iterator(); it.hasNext();) {
            QuadStatement stmt = (QuadStatement) ((QuadStatement) it.next()).clone();
            mapVariables(toTable, stmt);
            newCode.addStatement(stmt);
        }
    }

    private void copyStack(VariableTable toTable, StackCode newCode, StackCode oldCode)
            throws CloneNotSupportedException
    {

        for (Iterator it = oldCode.getStatements().iterator(); it.hasNext();) {
            StackStatement stmt = (StackStatement) ((StackStatement) it.next()).clone();
            mapVariables(toTable, stmt);
            newCode.addStatement(stmt);
        }
    }

    private void mapVariables(VariableTable toTable, Statement stmt) {
        if ( stmt instanceof VariableStmt ) {
            Variable[] usedVars = ((VariableStmt)stmt).getUsedVars();
            for (int i = 0; i < usedVars.length; i++) {
                ((VariableStmt)stmt).setUsedVar(i, mapper.mapVariable(toTable,
                        graph.getVariableTable(), usedVars[i]));

            }
        }
        if ( stmt instanceof AssignStmt) {
            Variable var = ((AssignStmt)stmt).getAssignedVar();
            if ( var != null ) {
                ((AssignStmt)stmt).setAssignedVar(mapper.mapVariable(toTable,
                        graph.getVariableTable(), var));
            }
        }
    }
}
