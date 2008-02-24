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

import com.jopdesign.libgraph.cfg.statements.common.ConvertStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadConvert;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackConvert extends ConvertStmt implements StackStatement, StackAssign {
    
    public StackConvert(TypeInfo fromType, TypeInfo toType) {
        super(fromType, toType);
    }

    public TypeInfo[] getPopTypes() {
        return new TypeInfo[] { getFromType() };
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[] { getToType() };
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) {
        Variable s0 = varTable.getDefaultStackVariable(stack.length-1);
        return new QuadStatement[] { new QuadConvert(getFromType(), getToType(), s0, s0) };
    }

    public int getOpcode() {
        switch (getFromType().getMachineType()) {
            case TypeInfo.TYPE_INT:
                switch (getToType().getType()) {
                    case TypeInfo.TYPE_LONG: return 0x85;
                    case TypeInfo.TYPE_FLOAT: return 0x86;
                    case TypeInfo.TYPE_DOUBLE: return 0x87;
                    case TypeInfo.TYPE_BYTE: return 0x91;
                    case TypeInfo.TYPE_CHAR: return 0x92;
                    case TypeInfo.TYPE_SHORT: return 0x93;
                }
                break;
            case TypeInfo.TYPE_LONG:
                switch (getToType().getType()) {
                    case TypeInfo.TYPE_INT: return 0x88;
                    case TypeInfo.TYPE_FLOAT: return 0x89;
                    case TypeInfo.TYPE_DOUBLE: return 0x8a;
                }
                break;
            case TypeInfo.TYPE_FLOAT:
                switch (getToType().getType()) {
                    case TypeInfo.TYPE_INT: return 0x8b;
                    case TypeInfo.TYPE_LONG: return 0x8c;
                    case TypeInfo.TYPE_DOUBLE: return 0x8d;
                }
                break;
            case TypeInfo.TYPE_DOUBLE:
                switch (getToType().getType()) {
                    case TypeInfo.TYPE_INT: return 0x8e;
                    case TypeInfo.TYPE_LONG: return 0x8f;
                    case TypeInfo.TYPE_FLOAT: return 0x90;
                }
                break;
        }
        return -1;
    }

    public int getBytecodeSize() {
        return 1;
    }

    public String getCodeLine() {
        return "convert." + getFromType().getTypeName() + "2" + getToType().getTypeName();
    }

    public ConstantValue[] getConstantValues(ConstantValue[] input) {
        return new ConstantValue[0];
    }
}
