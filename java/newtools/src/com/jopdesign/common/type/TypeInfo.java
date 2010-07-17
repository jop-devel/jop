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

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class TypeInfo {

    // TODO implement

    public static final TypeInfo TYPE_FLOAT = null;
    public static final TypeInfo TYPE_DOUBLE = null;
    public static final TypeInfo TYPE_INT = null;
    public static final TypeInfo TYPE_LONG = null;
    public static final TypeInfo TYPE_STRING = null;
    public static final TypeInfo TYPE_BYTE = null;
    public static final TypeInfo Type_UNKNOWN = null;

    /**
     * Test if this type is compatible to another type without cast.
     *
     * @param typeInfo the type to compare to.
     * @return TRUE if a value of typeInfo can be assigned to a variable of this type, FALSE if they are
     *         not compatible, and UNKNOWN if it is unknown (e.g. type not known).
     */
    public abstract Ternary canAssignFrom(TypeInfo typeInfo);

    public abstract String getTypeDescriptor();
    
    public String toString() {
        return getTypeDescriptor();
    }

}
