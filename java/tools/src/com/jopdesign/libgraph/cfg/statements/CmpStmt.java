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

/**
 * Common interface for all stmts which can (but not only) do comparisons.
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface CmpStmt extends Statement {

    public static final int OP_EQUAL =             1;
    public static final int OP_NOTEQUAL =          2;
    public static final int OP_GREATER =           3;
    public static final int OP_LESS =              4;
    public static final int OP_GREATER_OR_EQUAL =  5;
    public static final int OP_LESS_OR_EQUAL =     6;

    boolean isConstant();

    boolean getConstantResult();
    
}
