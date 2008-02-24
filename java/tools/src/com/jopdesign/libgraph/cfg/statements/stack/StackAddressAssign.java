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

import com.jopdesign.libgraph.cfg.statements.common.AddressAssignStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadAddressAssign;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackAddressAssign extends AddressAssignStmt implements StackStatement, StackAssign {

    public StackAddressAssign() {
    }

    public TypeInfo[] getPopTypes() {
        return new TypeInfo[0];
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[] { TypeInfo.CONST_ADDRESSREF };
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) {
        return new QuadStatement[] { new QuadAddressAssign( varTable.getDefaultStackVariable( stack.length )) };
    }

    public int getOpcode() {
        return -1;
    }

    public int getBytecodeSize() {
        return 0;
    }

    public String getCodeLine() {
        return "push @returnAddress";
    }

    public ConstantValue[] getConstantValues(ConstantValue[] input) {
        return null;
    }
}
