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
package com.jopdesign.libgraph.cfg.variable;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class OffsetVariableMapper implements VariableMapper {

    private int offset;

    public OffsetVariableMapper(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public Variable mapVariable(VariableTable toTable, VariableTable fromTable, Variable var) {
        if ( var.isConstant() ) {
            return toTable.getDefaultConstant(var.getConstantValue());
        }
        int index = fromTable.getIndex(var);
        if ( index >= 0 ) {
            return toTable.getDefaultLocalVariable(index + offset);
        } else {
            return toTable.getVariable(var.getName()); 
        }
    }
}
