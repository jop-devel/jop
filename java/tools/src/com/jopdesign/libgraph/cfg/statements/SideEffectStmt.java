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

import com.jopdesign.libgraph.struct.ConstantValue;

/**
 * An interface for statements which modify used variables without assigning them.
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface SideEffectStmt extends VariableStmt {

    /**
     * check if a used variable is modified by this stmt.
     * @param var the variable to test.
     * @return true, if the variable may be modified.
     */
    boolean isModified(int var);

    boolean isConstant(int var);

    ConstantValue getConstantValue(int var);
}
