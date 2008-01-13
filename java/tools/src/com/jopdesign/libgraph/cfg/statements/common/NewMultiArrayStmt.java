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
package com.jopdesign.libgraph.cfg.statements.common;

import com.jopdesign.libgraph.cfg.statements.NewStmt;
import com.jopdesign.libgraph.struct.ConstantClass;
import com.jopdesign.libgraph.struct.type.ObjectRefType;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class NewMultiArrayStmt extends AbstractStatement implements NewStmt {

    private ConstantClass arrayClass;
    private short dimensions;
    private TypeInfo arrayType;

    public NewMultiArrayStmt(ConstantClass arrayClass, short dimensions) {
        this.arrayClass = arrayClass;
        this.dimensions = dimensions;
        arrayType = new ObjectRefType(arrayClass);
    }

    public ConstantClass getArrayClass() {
        return arrayClass;
    }

    public short getDimensions() {
        return dimensions;
    }

    public TypeInfo getArrayType() {
        return arrayType;
    }

    public boolean canThrowException() {
        return true;
    }

    /**
     * Get the resulting type of this array. If the array dimensions
     * are constant, create the complete type.
     *
     * @return the type of the resulting array.
     */
    public abstract TypeInfo getResultType();
}
