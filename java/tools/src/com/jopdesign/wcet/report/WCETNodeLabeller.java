/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.wcet.report;

import com.jopdesign.common.code.BasicBlock;
import com.jopdesign.common.code.CFGExport.FGNodeLabeller;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.wcet.WCETTool;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class WCETNodeLabeller extends FGNodeLabeller {

    private final WCETTool project;

    public WCETNodeLabeller(WCETTool project) {
        this.project = project;
    }

    @Override
    protected void addNodeLabel(BasicBlockNode n, StringBuilder nodeInfo) {
        BasicBlock codeBlock = n.getBasicBlock();
        nodeInfo.append(project.getWCETProcessorModel().basicBlockWCET(
                new ExecutionContext(codeBlock.getMethodInfo()),
                codeBlock)+" Cyc, ");
    }
}
