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
package com.jopdesign.libgraph.struct;

import com.jopdesign.libgraph.struct.type.BaseType;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * A class which holds a single typed base value.
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ConstantValue {

    private TypeInfo type;

    private Number value;
    private String txtValue;

    public static final ConstantValue CONST_NULL = new ConstantValue( TypeInfo.CONST_NULLREF );

    public static final ConstantValue CONST_I_MINUS_ONE = new ConstantValue( TypeInfo.CONST_INT, -1 );
    public static final ConstantValue CONST_I_ZERO  = new ConstantValue( TypeInfo.CONST_INT, 0 );
    public static final ConstantValue CONST_I_ONE   = new ConstantValue( TypeInfo.CONST_INT, 1 );
    public static final ConstantValue CONST_I_TWO   = new ConstantValue( TypeInfo.CONST_INT, 2 );
    public static final ConstantValue CONST_I_THREE = new ConstantValue( TypeInfo.CONST_INT, 3 );
    public static final ConstantValue CONST_I_FOUR  = new ConstantValue( TypeInfo.CONST_INT, 4 );
    public static final ConstantValue CONST_I_FIVE  = new ConstantValue( TypeInfo.CONST_INT, 5 );

    public static final ConstantValue CONST_F_ZERO  = new ConstantValue( 0.0f );
    public static final ConstantValue CONST_F_ONE   = new ConstantValue( 1.0f );
    public static final ConstantValue CONST_F_TWO   = new ConstantValue( 2.0f );
    public static final ConstantValue CONST_F_THREE = new ConstantValue( 3.0f );

    public static final ConstantValue CONST_L_ZERO = new ConstantValue( 0l );
    public static final ConstantValue CONST_L_ONE = new ConstantValue( 1l );

    public static final ConstantValue CONST_D_ZERO = new ConstantValue( 0.0d );
    public static final ConstantValue CONST_D_ONE = new ConstantValue( 1.0d );

    public ConstantValue(TypeInfo type) {
        this.type = type;
    }

    public ConstantValue(BaseType type, int value) {
        this.type = type;
        this.value = new Integer(value);
    }

    public ConstantValue(BaseType type, Number value) {
        this.type = type;
        this.value = value;
    }

    public ConstantValue(float value) {
        this.type = TypeInfo.CONST_FLOAT;
        this.value = new Float(value);
    }

    public ConstantValue(long value) {
        this.type = TypeInfo.CONST_LONG;
        this.value = new Long(value);
    }

    public ConstantValue(double value) {
        this.type = TypeInfo.CONST_DOUBLE;
        this.value = new Double(value);
    }

    public ConstantValue(String txt) {
        this.type = TypeInfo.CONST_STRING;
        this.txtValue = txt;
    }

    public TypeInfo getType() {
        return type;
    }

    public int getIntValue() {
        return value.intValue();
    }

    public float getFloatValue() {
        return value.floatValue();
    }

    public long getLongValue() {
        return value.longValue();
    }

    public double getDoubleValue() {
        return value.doubleValue();
    }

    public String getTxtValue() {
        return txtValue;
    }

    public boolean isNull() {
        return type.getType() == TypeInfo.TYPE_NULLREF || (txtValue == null && value == null);
    }

    public String toString() {
        if ( isNull() ) {
            return "null";
        }
        return txtValue != null ? "\"" + txtValue + "\"" : value.toString();
    }
}
