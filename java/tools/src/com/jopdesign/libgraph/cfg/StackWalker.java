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
import com.jopdesign.libgraph.cfg.block.StackCode;
import com.jopdesign.libgraph.struct.type.BaseType;
import com.jopdesign.libgraph.struct.type.TypeInfo;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * This visitor will update the stack depth information of basic blocks using the
 * stack info of the parent block (in a DFS/BFS search) and a stack emulator.
 * If a loop is found and the stack information is inconsistent or a reference to a block
 * outside the block range is found, the search will terminate (walk will return 'true').
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackWalker implements CFGWalker.Visitor {

    private StackEmulator emulator;
    private static Logger logger = Logger.getLogger(StackWalker.class);

    public StackWalker() {
        this.emulator = new StackEmulator();
    }

    public void setStack(TypeInfo[] stack) {
        emulator.init(stack);
    }

    public void reset() {
    }

    public boolean start(BasicBlock block) throws GraphException {
        StackCode code = block.getStackCode();
        if ( code == null ) {
            return true;
        }

        if ( logger.isDebugEnabled() ) {
            logger.debug("Starting at block " + block.getBlockIndex() + " with stack: " +
                BaseType.typeStackToString(emulator.getCurrentStack()) );
        }

        code.setStartStack(emulator.getCurrentStack());
        emulator.processCode(code);
        code.setEndStack(emulator.getCurrentStack());

        return false;
    }

    public boolean visitBlock(BasicBlock block, BasicBlock parentBlock, int depth, int edgeType) throws GraphException {

        if ( edgeType == EDGLE_OUTSIDE ) {
            return true;
        }

        TypeInfo[] stack = parentBlock.getStackCode().getEndStack();

        if ( logger.isDebugEnabled() ) {
            logger.debug("Visiting block " + block.getBlockIndex() + " from block " + parentBlock.getBlockIndex() +
                ", stack: " +BaseType.typeStackToString(stack));
        }

        if ( edgeType != EDGE_NORMAL ) {
            // for loops, check stack consistency
            if ( !Arrays.equals(stack, block.getStackCode().getStartStack()) ) {
                logger.error("Found inconsistent stack types in loop:");
                logger.error(" from block " + parentBlock.getBlockIndex() + ": " +
                        BaseType.typeStackToString(stack));
                logger.error(" to block " + block.getBlockIndex() + ": " +
                        BaseType.typeStackToString(block.getStackCode().getStartStack()));
                return true;
            }
        } else {
            StackCode code = block.getStackCode();

            code.setStartStack(stack);
            emulator.setStack(stack);
            emulator.processCode(code);
            code.setEndStack(emulator.getCurrentStack());

            if ( logger.isDebugEnabled() ) {
                logger.debug("    Stack at end of block " + block.getBlockIndex() + ": " +
                    BaseType.typeStackToString(code.getEndStack()));
            }
        }

        return false;
    }
}
