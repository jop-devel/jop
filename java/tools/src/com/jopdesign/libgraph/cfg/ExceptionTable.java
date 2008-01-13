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
import com.jopdesign.libgraph.struct.ConstantClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ExceptionTable {
    
    private class Handle extends BasicBlock.ExceptionHandler {

        protected Handle(ConstantClass exception) {
            super(exception);
        }

        public ExceptionTable getExceptionTable() {
            return ExceptionTable.this;
        }
        
        public int getHandlerIndex() {
            return handler.indexOf(this);
        }
    }

    private ControlFlowGraph graph;
    private List handler;

    public ExceptionTable(ControlFlowGraph graph) {
        this.graph = graph;
        handler = new ArrayList();
    }


    public ControlFlowGraph getGraph() {
        return graph;
    }

    public BasicBlock.ExceptionHandler addExceptionHandler(BasicBlock block, ConstantClass exception) {
        Handle handle = new Handle(exception);
        handle.setExceptionBlock(block);
        handler.add(handle);
        return handle;
    }

    public BasicBlock.ExceptionHandler addExceptionHandler() {
        return addExceptionHandler(null, null);
    }

    public BasicBlock.ExceptionHandler getExceptionHandler(int i) {
        return (Handle) handler.get(i);
    }

    public List getExceptionHandlers() {
        return Collections.unmodifiableList(handler);
    }

    /**
     * Merge all exceptionhandler with same targetblock and exactly same exceptionclass into one handler.
     */
    public void mergeExceptionHandler() {

    }

}
