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

import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.ConstantClass;

/**
 * A TypeInfo for an object reference.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ObjectRefType extends RefTypeInfo {

    private ConstantClass clazz;
    private boolean isNull;

    /**
     * Create a new null or object reference type.
     * @param isNull if true, a null-reference is created, else a 'non-typed' object
     * reference (this can also be a reference to an array!).
     */
    public ObjectRefType(boolean isNull) {
        super(isNull ? TYPE_NULLREF : TYPE_OBJECTREF);
        this.isNull = isNull;
        clazz = null;
    }

    public ObjectRefType(ConstantClass clazz) {
        super(TYPE_OBJECTREF);
        this.clazz = clazz;
        isNull = false;
    }

    /**
     * Create a new TypeInfo for a reference to a known class.
     * @param classInfo the (super) class of the reference.
     */
    public ObjectRefType(ClassInfo classInfo) {
        super(TYPE_OBJECTREF);
        this.clazz = new ConstantClass(classInfo);
        isNull = false;
    }

    /**
     * Create a new TypeInfo for a reference to a class which has not been loaded.
     * @param className the fully qualified classname, dot-separated.
     */
    public ObjectRefType(String className) {
        super(TYPE_OBJECTREF);
        this.clazz = new ConstantClass(className);
        isNull = false;
    }

    public ClassInfo getClassInfo() {
        return clazz.getClassInfo();
    }

    public String getClassName() {
        return clazz.getClassName();
    }

    public String getDescriptor() {
        return "L" + getClassName().replace('.','/') + ";";
    }

    public String getTypeName() {
        if ( isNull ) {
            return "null";
        }
        if ( isUntyped() ) {
            return "reference";
        }
        return getClassName();
    }

    /**
     * Check if this is a null-reference.
     * @return true if this references to null.
     */
    public boolean isNull() {
        return isNull;
    }

    /**
     * Check if this has a classname set in any form.
     * @return true, if no classname is set, or if this is a null reference.
     */
    public boolean isUntyped() {
        return !isNull && (clazz == null || clazz.getClassName() == null);
    }

    public ConstantClass getClassConstant() {
        return clazz;
    }
}
