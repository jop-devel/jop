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
package com.jopdesign.libgraph.struct.type;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BaseType extends AbstractType {

    public BaseType(byte type) {
        super(type);
    }

    public String getDescriptor() {
        switch ( getType() ) {
            case TYPE_VOID: return "V";
            case TYPE_BOOL: return "Z";
            case TYPE_CHAR: return "C";
            case TYPE_BYTE: return "B";
            case TYPE_SHORT: return "S";
            case TYPE_INT: return "I";
            case TYPE_FLOAT: return "F";
            case TYPE_LONG: return "J";
            case TYPE_DOUBLE: return "D";
        }
        return "";
    }

    public String getTypeName() {
        return getTypeName(getType());
    }

    public String getTypeName(int type) {
        switch ( type ) {
            case TYPE_VOID: return "void";
            case TYPE_BOOL: return "boolean";
            case TYPE_CHAR: return "char";
            case TYPE_BYTE: return "byte";
            case TYPE_SHORT: return "short";
            case TYPE_INT: return "int";
            case TYPE_FLOAT: return "float";
            case TYPE_LONG: return "long";
            case TYPE_DOUBLE: return "double";
            case TYPE_REFERENCE: return "reference";
            case TYPE_OBJECTREF: return "Object";
            case TYPE_ARRAYREF: return "Object[]";
            case TYPE_ADDRESS: return "returnAddress";
            case TYPE_NULLREF: return "Object";
        }
        return "";
    }

    public String getMachineTypeName() {
        return getTypeName(getMachineType());
    }

    public static String typeStackToString(TypeInfo[] stack) {
        return typeStackToString(stack, ", ");
    }

    public static String typeStackToString(TypeInfo[] stack, String delim) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < stack.length; i++) {
            TypeInfo typeInfo = stack[i];
            if ( i > 0 ) buf.append(delim);
            buf.append(typeInfo.getTypeName());
        }
        return buf.toString();
    }
}
