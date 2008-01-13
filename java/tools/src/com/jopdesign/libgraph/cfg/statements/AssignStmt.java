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

import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * Base interface for all statements which assign a value to a local variable.
 * Note that only one variable can be assigned per statement.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface AssignStmt {

    /**
     * get the variable which is assigned by this statement.
     * @return the variable, or null if no variable is assigned by this instance.
     */
    Variable getAssignedVar();

    TypeInfo getAssignedType();

    void setAssignedVar(Variable newVar);

    /**
     * Check if the results of this statement is always constant (i.e.
     * all arguments are constant).
     *
     * @return true, if the returned values are always constant.
     */
    boolean isConstant();

    ConstantValue evaluateConstantStmt();
    
}
