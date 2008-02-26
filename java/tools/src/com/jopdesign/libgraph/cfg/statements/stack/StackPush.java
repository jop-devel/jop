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

import com.jopdesign.libgraph.cfg.statements.common.AbstractStatement;
import com.jopdesign.libgraph.cfg.statements.quad.QuadCopy;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackPush extends AbstractStatement implements StackStatement, StackAssign {
    
    private TypeInfo type;
    private ConstantValue value;
    private int poolIndex;

    public static final byte OP_ACONST_NULL = 0x01;
    public static final byte OP_ICONST_0 = 0x03;
    public static final byte OP_LCONST_0 = 0x09;
    public static final byte OP_LCONST_1 = 0x0a;
    public static final byte OP_FCONST_0 = 0x0b;
    public static final byte OP_FCONST_1 = 0x0c;
    public static final byte OP_FCONST_2 = 0x0d;
    public static final byte OP_DCONST_0 = 0x0e;
    public static final byte OP_DCONST_1 = 0x0f;
    public static final byte OP_BIPUSH   = 0x10;
    public static final byte OP_SIPUSH   = 0x11;
    public static final byte OP_LDC      = 0x12;
    public static final byte OP_LDC_W    = 0x13;
    public static final byte OP_LDC2_W   = 0x14;

    public static final int MAX_BYTE = 255;

    public StackPush(ConstantValue value) {
        this.type = value.getType();
        this.value = value;
        this.poolIndex = 0;
    }

    public StackPush(ConstantValue value, int poolIndex) {
        this.type = value.getType();
        this.value = value;
        this.poolIndex = poolIndex;
    }

    public TypeInfo getType() {
        return type;
    }

    public ConstantValue getValue() {
        return value;
    }

    public boolean canThrowException() {
        return false;
    }

    public int getPoolIndex() {
        return poolIndex;
    }

    public void setPoolIndex(int poolIndex) {
        this.poolIndex = poolIndex;
    }

    public String getCodeLine() {
        return "push." + type.getTypeName() + " " + value.toString();
    }

    public TypeInfo[] getPopTypes() {
        return new TypeInfo[0];
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[] { type };
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException {
        Variable s0 = varTable.getDefaultStackVariable(stack.length);
        Variable cval = varTable.getDefaultConstant(value);
        return new QuadStatement[] { new QuadCopy(type, s0, cval) };
    }

    public int getOpcode() {
        switch ( value.getType().getMachineType() ) {
            case TypeInfo.TYPE_INT:
                int iValue = value.getIntValue();
                if ( iValue >= -1 && iValue <= 5 ) {
                    return OP_ICONST_0 + iValue;
                } else if ( iValue >= -128 && iValue <= 127 ) {
                    return OP_BIPUSH;
                } else if ( iValue >= -32768 && iValue <= 32767 ) {
                    return OP_SIPUSH;
                } else {
                    return poolIndex <= MAX_BYTE ? OP_LDC : OP_LDC_W;
                }
            case TypeInfo.TYPE_LONG:
                long lValue = value.getLongValue();
                if ( lValue == 0 ) {
                    return OP_LCONST_0;
                } else if ( lValue == 1 ) {
                    return OP_LCONST_1;
                } else {
                    return OP_LDC2_W;
                }
            case TypeInfo.TYPE_FLOAT:
                float fValue = value.getFloatValue();
                if ( fValue == 0.0f ) {
                    return OP_FCONST_0;
                } else if ( fValue == 1.0f ) {
                    return OP_FCONST_1;
                } else if ( fValue == 2.0f ) {
                    return OP_FCONST_2;
                } else {
                    return poolIndex <= MAX_BYTE ? OP_LDC : OP_LDC_W;
                }
            case TypeInfo.TYPE_DOUBLE:
                double dValue = value.getDoubleValue();
                if ( dValue == 0.0 ) {
                    return OP_DCONST_0;
                } else if ( dValue == 1.0 ) {
                    return OP_DCONST_1;
                } else {
                    return OP_LDC2_W;
                }
            case TypeInfo.TYPE_REFERENCE:
                String txt = value.getTxtValue();
                if ( txt == null ) {
                    return OP_ACONST_NULL;
                } else {
                    return poolIndex <= MAX_BYTE ? OP_LDC : OP_LDC_W;
                }
        }
        return -1;
    }

    public int getBytecodeSize() {
        switch (getOpcode()) {
            case OP_BIPUSH: return 2;
            case OP_SIPUSH: return 3;
            case OP_LDC: return 2;
            case OP_LDC_W: return 3;
            case OP_LDC2_W: return 3;
            default: return 1;
        }
    }

    public ConstantValue[] getConstantValues(ConstantValue[] input) {
        return new ConstantValue[] { value };
    }
}
