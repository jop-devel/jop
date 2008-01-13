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
package com.jopdesign.libgraph.cfg.statements;

import com.jopdesign.libgraph.cfg.block.BasicBlock;
import com.jopdesign.libgraph.cfg.block.CodeBlock;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StmtHandle {

    private CodeBlock code;
    private int pos;
    private Statement stmt;

    public StmtHandle(CodeBlock code, int pos, Statement stmt) {
        this.code = code;
        this.pos = pos;
        this.stmt = stmt;
    }

    public BasicBlock getBlock() {
        return code.getBasicBlock();
    }

    public CodeBlock getCode() {
        return code;
    }

    public int getPosition() {
        return pos;
    }

    public Statement getStatement() {
        return stmt;
    }

    public BasicBlock splitBefore() {
        return code.getBasicBlock().splitBlock(pos);
    }

    public BasicBlock splitAfter() {
        return code.getBasicBlock().splitBlock(pos+1);
    }
}
