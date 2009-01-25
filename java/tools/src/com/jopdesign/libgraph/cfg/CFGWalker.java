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
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * This class implements a generic DFS search on a CFG.
 * Inserting/deleting blocks during a search may result in undefined behaviour.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class CFGWalker {

    public static interface Visitor {

        static final int EDGE_NORMAL = 0;
        static final int EDGE_BACK = 1;
        static final int EDGE_FORWARD = 2;
        static final int EDGE_CROSS = 3;
        static final int EDGLE_OUTSIDE = 4;

        void reset();

        boolean start(BasicBlock block) throws GraphException;

        boolean visitBlock(BasicBlock block, BasicBlock parentBlock, int depth, int edgeType) throws GraphException;
    }

    private ControlFlowGraph graph;
    private Visitor visitor;
    private int firstBlock;
    private int lastBlock;

    // hold first/last block calculated since first start.
    // curLastBlock stores the index of the block after the last block.
    private int curFirstBlock;
    private int curLastBlock;
    // -1: not visited, 0: discovered, >0: visited
    private int[] visited;
    private int step;

    private static Logger logger = Logger.getLogger(CFGWalker.class);

    public CFGWalker(ControlFlowGraph graph, Visitor visitor) {
        this.graph = graph;
        this.visitor = visitor;
        firstBlock = -1;
        lastBlock = -1;
    }

    public CFGWalker(ControlFlowGraph graph, Visitor visitor, int firstBlock, int lastBlock) {
        this.graph = graph;
        this.visitor = visitor;
        this.firstBlock = firstBlock;
        this.lastBlock = lastBlock;
    }

    public void setBlockRange(int firstBlock, int lastBlock) {
        this.firstBlock = firstBlock;
        this.lastBlock = lastBlock;
        reset();
    }

    public Visitor getVisitor() {
        return visitor;
    }

    public void reset() {
        visited = null;
        visitor.reset();
    }

    public boolean walkDFS(int startBlock) throws GraphException {

        init();

        BasicBlock block = graph.getBlock(startBlock);
        if (visitor.start(block)) {
            visited[startBlock - curFirstBlock] = 1;
            return true;
        }

        step = 1;

        return visitDFS(block, startBlock, 1);
    }

    private boolean visitDFS(BasicBlock parent, int parentId, int depth) throws GraphException {
        boolean found = false;

        visited[parentId - curFirstBlock] = 0;

        // descent default edge
        BasicBlock.Edge edge = parent.getNextBlockEdge();
        if ( edge != null ) {
            found = descentDFS(parent, edge.getTargetBlock(), depth);
        }

        // descent controlflow-edges
        Iterator it = parent.getTargetEdges().iterator();
        while (it.hasNext() && !found) {
            edge = (BasicBlock.Edge) it.next();
            found = descentDFS(parent, edge.getTargetBlock(), depth);
        }

        // finished all sub-nodes, or terminated search
        visited[parentId - curFirstBlock] = step++;

        return found;
    }

    private boolean descentDFS(BasicBlock parent, BasicBlock block, int depth) throws GraphException {
        boolean found;

        // TODO store blockindex with blocks to make this faster
        int blockId = block.getBlockIndex();
        int edgeType = Visitor.EDGE_NORMAL;

        // check target
        if ( blockId < curFirstBlock || blockId >= curLastBlock ) {
            edgeType = Visitor.EDGLE_OUTSIDE;
        } else if ( visited[blockId - curFirstBlock] > 0 ) {
            edgeType = Visitor.EDGE_CROSS;
        } else if ( visited[blockId - curFirstBlock] == 0 ) {
            edgeType = Visitor.EDGE_BACK;
        }

        found = visitor.visitBlock(block, parent, depth, edgeType);

        if ( !found && edgeType == Visitor.EDGE_NORMAL ) {
            if (visitDFS(block, blockId, depth+1)) {
                found = true;
            }
        }

        return found;
    }

    /**
     * Check if all blocks have been visited.
     * @return true if all blocks have been visited, else false.
     */
    public boolean allVisited() {
        if ( visited == null ) {
            return false;
        }

        for (int i = 0; i < visited.length; i++) {
            if (visited[i] == -1) return false;
        }

        return true;
    }

    private void init() {

        if ( visited != null ) {
            return;
        }

        if ( firstBlock == -1 ) {
            curFirstBlock = 0;
        } else if ( firstBlock >= graph.getBlockCount() ) {
            curFirstBlock = graph.getBlockCount();
        } else {
            curFirstBlock = firstBlock;
        }

        if ( lastBlock == -1 || lastBlock >= graph.getBlockCount() ) {
            curLastBlock = graph.getBlockCount();
        } else {
            curLastBlock = lastBlock + 1;
        }

        visited = new int[curLastBlock - curFirstBlock];
        for (int i = 0; i < visited.length; i++) {
            visited[i] = -1;
        }
    }
}
