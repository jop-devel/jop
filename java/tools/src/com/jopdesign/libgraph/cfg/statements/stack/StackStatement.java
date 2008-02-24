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
 * Interface for all instructions in stack-form.
 * 
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface StackStatement extends Statement {

    /**
     * Get the types of the values this stmt pops from the stack.
     * @return the types of the values removed from the stack before new values are pushed.
     */
    TypeInfo[] getPopTypes();

    /**
     * Get the types of the pushed values.
     * @return the type of each pushed value.
     */
    TypeInfo[] getPushTypes();

    /**
     * Get the quadruple code for this statement.
     * If this statement is a control-flow statement, the last statement of the returned list must also
     * be a controlflow statement.
     *
     * @param stack    the current types on the stack.
     * @param varTable the current variable table, used to get variables for stack-depth.
     * @return a list of quad statements
     */
    QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException;

    /**
     * Get the bytecode opcode of the corresponding jvm instruction of this statement.
     * @return the opcode of the corresponding jvm instruction, or -1 for statements which do not compile to JVM instructions.
     */
    int getOpcode();

    /**
     * Get the size of this instruction in bytes.
     * @return the size of this instruction in bytes or 0 for statements which are not compiled.
     */
    int getBytecodeSize();

}
