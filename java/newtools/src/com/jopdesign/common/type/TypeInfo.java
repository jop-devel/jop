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

import com.jopdesign.common.misc.Ternary;
import org.apache.bcel.generic.Type;

/**
 * TypeInfo is an (immutable) wrapper of a BCEL type, and extends it with some
 * custom methods, e.g. to allow further restriction of the value domain.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class TypeInfo<T extends Type> {

    public static final BasicTypeInfo TYPE_VOID = new BasicTypeInfo(Type.VOID);
    public static final BasicTypeInfo TYPE_BYTE = new BasicTypeInfo(Type.BYTE);
    public static final BasicTypeInfo TYPE_CHAR = new BasicTypeInfo(Type.CHAR);
    public static final BasicTypeInfo TYPE_INT = new BasicTypeInfo(Type.INT);
    public static final BasicTypeInfo TYPE_LONG = new BasicTypeInfo(Type.LONG);
    public static final BasicTypeInfo TYPE_FLOAT = new BasicTypeInfo(Type.FLOAT);
    public static final BasicTypeInfo TYPE_DOUBLE = new BasicTypeInfo(Type.DOUBLE);
    public static final ObjectTypeInfo TYPE_STRING = new ObjectTypeInfo(Type.STRING);

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
    public abstract Ternary canAssignFrom(TypeInfo typeInfo);

    public String getTypeDescriptor() {
        return type.getSignature();
    }
    
    public String toString() {
        return getTypeDescriptor();
    }

    public T getType() {
        return type;
    }

    public static TypeInfo getTypeInfo(Type type) {
        return null;
    }
}
