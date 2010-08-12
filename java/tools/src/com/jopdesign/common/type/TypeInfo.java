/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.common.type;

import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.misc.Ternary;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;

/**
 * TypeInfo is a (mutable) wrapper of a BCEL type, and extends it with some
 * custom methods, e.g. to allow further restriction of the value domain.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class TypeInfo<T extends Type> {

    public static TypeInfo getTypeInfo(Type type) {
        switch (type.getType()) {
            case Constants.T_UNKNOWN:   return new TypeInfo<Type>(Type.UNKNOWN);
            case Constants.T_VOID:      return new BasicTypeInfo(Type.VOID);
            case Constants.T_BOOLEAN:   return new BasicTypeInfo(Type.BOOLEAN);
            case Constants.T_BYTE:      return new BasicTypeInfo(Type.BYTE);
            case Constants.T_CHAR:      return new BasicTypeInfo(Type.CHAR);
            case Constants.T_SHORT:     return new BasicTypeInfo(Type.SHORT);
            case Constants.T_INT:       return new BasicTypeInfo(Type.INT);
            case Constants.T_LONG:      return new BasicTypeInfo(Type.LONG);
            case Constants.T_FLOAT:     return new BasicTypeInfo(Type.FLOAT);
            case Constants.T_DOUBLE:    return new BasicTypeInfo(Type.DOUBLE);
            case Constants.T_ARRAY:
                return new ArrayTypeInfo((ArrayType) type);
            case Constants.T_REFERENCE:
                if ( type instanceof ObjectType) {
                    return new ObjectTypeInfo((ObjectType) type);
                }
                if ( "<null object>".equals(((ReferenceType)type).getSignature()) ) {
                    return new ReferenceTypeInfo<ReferenceType>(Type.NULL);
                }
                throw new AppInfoError("Unknown BCEL reference type: " + type);
            default:
                throw new AppInfoError("Unknown BCEL type ID: " + type.getType());
        }
    }

    public static TypeInfo[] getTypeInfos(Type[] argumentTypes) {
        if ( argumentTypes == null ) {
            return null;
        }
        TypeInfo[] args = new TypeInfo[argumentTypes.length];
        for ( int i = 0; i < argumentTypes.length; i++ ) {
            args[i] = getTypeInfo(argumentTypes[i]);
        }
        return args;
    }

    public static Type[] getTypes(TypeInfo[] types) {
        Type[] t = new Type[types.length];
        for (int i = 0; i < types.length; i++) {
            t[i] = types[i].getType();
        }
        return t;
    }

    /**
     * Get the type used by the JVM for this type.
     * @param type the type to check.
     * @return one of {@link Constants#T_INT}, {@link Constants#T_LONG}, {@link Constants#T_FLOAT},
     *         {@link Constants#T_DOUBLE}, {@link Constants#T_REFERENCE}, {@link Constants#T_VOID} or
     *         {@link Constants#T_UNKNOWN}.
     */
    public static byte getMachineType(Type type) {
        switch ( type.getType() ) {
            case Constants.T_LONG:
            case Constants.T_FLOAT:
            case Constants.T_DOUBLE:
            case Constants.T_INT:
            case Constants.T_VOID:
            case Constants.T_UNKNOWN:
                return type.getType();

            case Constants.T_BYTE:
            case Constants.T_BOOLEAN:
            case Constants.T_CHAR:
            case Constants.T_SHORT:
                return Constants.T_INT;

            case Constants.T_ARRAY:
            case Constants.T_REFERENCE:
                return Constants.T_REFERENCE;
            default:
                throw new AppInfoError("Unknown BCEL type ID: " + type.getType());
        }
    }

    public static Ternary canAssign(Type to, Type src) {
        // TODO implement
        return Ternary.UNKNOWN;
    }


    private final T type;

    protected TypeInfo(T type) {
        this.type = type;
    }

    /**
     * Test if this type is compatible to another type without cast.
     *
     * @param typeInfo the type to compare to.
     * @return TRUE if a value of typeInfo can be assigned to a variable of this type, FALSE if they are
     *         not compatible, and UNKNOWN if it is unknown (e.g. type not known).
     */
    public Ternary canAssignFrom(TypeInfo typeInfo) {
        return canAssign(getType(), typeInfo.getType());
    }

    public String getTypeDescriptor() {
        return type.getSignature();
    }
    
    public String toString() {
        return getTypeDescriptor();
    }

    public T getType() {
        return type;
    }

    public boolean isArray() {
        return type.getType() == Constants.T_ARRAY;
    }

    /**
     * Get the type used by the JVM for this type.
     * @return one of {@link Constants#T_INT}, {@link Constants#T_LONG}, {@link Constants#T_FLOAT},
     *         {@link Constants#T_DOUBLE}, {@link Constants#T_REFERENCE}, {@link Constants#T_VOID} or
     *         {@link Constants#T_UNKNOWN}.
     */
    public byte getMachineType() {
        return getMachineType(type);
    }
}
