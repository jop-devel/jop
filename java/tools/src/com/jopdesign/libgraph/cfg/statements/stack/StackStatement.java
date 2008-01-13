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
package com.jopdesign.libgraph.cfg.statements.stack;

import com.jopdesign.libgraph.cfg.statements.Statement;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * Interface for all statements in stack-form.
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface StackStatement extends Statement {

    /**
     * get the types of the values this stmt pops from the stack.
     * @return the types of the values removed from the stack before new values are pushed.
     */
    TypeInfo[] getPopTypes();

    /**
     * Get the types of the pushed values.
     * @return the type of each pushed value.
     */
    TypeInfo[] getPushTypes();

    /**
     * Get the number of clock cycles for this statement.
     * Returns 0 for statements which do not compile into bytecode.
     *
     * @return the number of clock cycles, or -1 if not known.
     */
    int getClockCycles();

    /**
     * get the quadruple code for this statement
     * @return a list of quad statements
     */
    QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException;
}
