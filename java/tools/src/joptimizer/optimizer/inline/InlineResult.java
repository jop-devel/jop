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
package joptimizer.optimizer.inline;

/**
 * This is a container for status results of an inlining process.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class InlineResult {

    private int firstBlock;
    private int newBlocks;
    private int[] checkcodeBlocks;
    private int deltaBytecode;
    private int maxLocals;

    /**
     * Create a new result container.
     *
     * @param firstBlock the id of the first new block.
     * @param newBlocks the number of (continuous) new blocks.
     * @param checkcodeBlocks the id of all blocks created to wrap/check the inlined code.
     * @param deltaBytecode the bytecode size change, not including the size of the inlined code (checkcodesize - invokesize).
     * @param maxLocals the maximum number of local variables used in this range.
     */
    public InlineResult(int firstBlock, int newBlocks, int[] checkcodeBlocks, int deltaBytecode, int maxLocals) {
        this.firstBlock = firstBlock;
        this.newBlocks = newBlocks;
        this.checkcodeBlocks = checkcodeBlocks;
        this.deltaBytecode = deltaBytecode;
        this.maxLocals = maxLocals;
    }

    public int getFirstBlock() {
        return firstBlock;
    }

    public int getNewBlocks() {
        return newBlocks;
    }

    public int[] getCheckcodeBlocks() {
        return checkcodeBlocks;
    }

    public int getDeltaBytecode() {
        return deltaBytecode;
    }

    public int getMaxLocals() {
        return maxLocals;
    }
}
