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

import com.jopdesign.libgraph.struct.type.TypeHelper;
import com.jopdesign.libgraph.struct.type.TypeInfo;
import org.apache.log4j.Logger;

/**
 * Container for a reference to a field.
 * As the reference may refer to a subclass of the class which defines the field,
 * the class which was used to access the field is stored too.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ConstantField {

    private ClassInfo classInfo;
    private FieldInfo fieldInfo;
    private String className;
    private String fieldName;
    private String signature;
    private boolean isStatic;

    public ConstantField(ClassInfo classInfo, FieldInfo fieldInfo) {
        this.classInfo = classInfo;
        this.fieldInfo = fieldInfo;
    }

    public ConstantField(String className, String fieldName, String signature, boolean isStatic) {
        this.className = className;
        this.fieldName = fieldName;
        this.signature = signature;
        this.isStatic = isStatic;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public FieldInfo getFieldInfo() {
        return fieldInfo;
    }

    public String getClassName() {
        return classInfo != null ? classInfo.getClassName() : className;
    }

    public String getFieldName() {
        return fieldInfo != null ? fieldInfo.getName() : fieldName;
    }

    public String getSignature() {
        return fieldInfo != null ? fieldInfo.getSignature() : signature;
    }

    public boolean isAnonymous() {
        return classInfo == null || fieldInfo == null;
    }

    public boolean equals(Object obj) {
        if ( !(obj instanceof ConstantField) ) {
            return false;
        }
        ConstantField o = (ConstantField) obj;

        return o.getClassName().equals(getClassName()) && o.getFieldName().equals(getFieldName());
    }

    public boolean isStatic() {
        return fieldInfo != null ? fieldInfo.isStatic() : isStatic;
    }

    public String getFQName() {
        return getClassName() + "#" + getFieldName();
    }

    public TypeInfo getType() {
        if ( fieldInfo != null ) {
            return fieldInfo.getType();
        }
        try {
            return TypeHelper.parseType(null, getSignature());
        } catch (TypeException e) {
            Logger.getLogger(this.getClass()).error("Could not create anonymous type for {"+getSignature()+"}.");
        }
        return null;
    }
}
